package ar.edu.itba.pod.tpe2.client.query1;

import ar.edu.itba.pod.tpe2.client.utils.QueryConfig;
import ar.edu.itba.pod.tpe2.client.utils.parsing.BaseArguments;
import ar.edu.itba.pod.tpe2.client.utils.HazelcastConfig;
import ar.edu.itba.pod.tpe2.client.utils.parsing.QueryParser;
import ar.edu.itba.pod.tpe2.client.utils.parsing.QueryParserFactory;
import ar.edu.itba.pod.tpe2.client.utils.TimestampLogger;
import ar.edu.itba.pod.tpe2.models.City;
import ar.edu.itba.pod.tpe2.models.infraction.Infraction;
import ar.edu.itba.pod.tpe2.models.ticket.adapters.Ticket;
import ar.edu.itba.pod.tpe2.query1.Query1Collator;
import ar.edu.itba.pod.tpe2.query1.Query1CombinerFactory;
import ar.edu.itba.pod.tpe2.query1.Query1Mapper;
import ar.edu.itba.pod.tpe2.query1.Query1ReducerFactory;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.*;
import com.hazelcast.map.impl.LegacyAsyncMap;
import com.hazelcast.mapreduce.Job;
import com.hazelcast.mapreduce.JobTracker;
import com.hazelcast.mapreduce.KeyValueSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.*;

import javax.management.timer.Timer;
import java.io.IOException;
import java.security.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import static ar.edu.itba.pod.tpe2.client.utils.CSVUtils.*;

@Slf4j
public class Query1Client {
    private static final String QUERY_NAME = "query1";
    private static final String QUERY_RESULT_HEADER = "Infraction;Tickets";
    private static final String CNP = "g7-"; // Cluster Name Prefix
    private static final String TIME_OUTPUT_FILE = "time1.txt";
    private static final String QUERY_OUTPUT_FILE = "query1.csv";

    public static void main(String[] args) {

        QueryParser parser = QueryParserFactory.getParser(QUERY_NAME);

        BaseArguments arguments;
        try {
            arguments = parser.getArguments(args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            return;
        }

        QueryConfig queryConfig = new QueryConfig(QUERY_OUTPUT_FILE, TIME_OUTPUT_FILE);
        City city = arguments.getCity();

        // Hazelcast client Config
        HazelcastInstance hazelcastInstance = HazelcastConfig.configureHazelcastClient(arguments);
        TimestampLogger timeLog = new TimestampLogger(arguments.getOutPath(), queryConfig.getTimeOutputFile());


        try {
            timeLog.logStartReading();
            // Parse infractions
            Map<String, Infraction> infractions = new ConcurrentHashMap<>();
            parseInfractions(arguments.getInPath(), city, infractions);
            // Parse tickets
            IMap<Long, Ticket> ticketMap = hazelcastInstance.getMap(CNP + QUERY_NAME + "ticketMap");
            ticketMap.clear();
            IList<Ticket> ticketList = hazelcastInstance.getList(CNP + QUERY_NAME + "ticketList");
            ticketList.clear();

            // measure time
            long start, end;

            // Timing parseTicketsToMap
            start = System.nanoTime();
            parseTicketsToMap(arguments.getInPath(), city, hazelcastInstance, ticketMap, ticket -> hasInfraction(ticket, infractions));
            end = System.nanoTime();
            System.out.println("parseTicketsToMap took: " + (end - start) / 1_000_000 + " ms");

            // Timing parseTickets
            start = System.nanoTime();
            parseTickets(arguments.getInPath(), city, ticketList, ticket -> hasInfraction(ticket, infractions));
            end = System.nanoTime();
            System.out.println("parseTickets took: " + (end - start) / 1_000_000 + " ms");

            timeLog.logEndReading();

            JobTracker jobTracker = hazelcastInstance.getJobTracker(CNP + QUERY_NAME + "jobTracker");
            KeyValueSource<String, Ticket> source = KeyValueSource.fromList(ticketList);

            Job<String, Ticket> job = jobTracker.newJob(source);
            timeLog.logStartMapReduce();
            Map<String, Integer> result = job
                    .mapper(new Query1Mapper())
                    .combiner(new Query1CombinerFactory())
                    .reducer(new Query1ReducerFactory())
                    .submit(new Query1Collator(infractions))
                    .get();
            timeLog.logEndMapReduce();

            List<String> outputLines = result
                    .entrySet()
                    .stream()
                    .map(entry -> infractions.get(entry.getKey()).getDescription() + ";" + entry.getValue())
                    .toList();

            writeQueryResults(arguments.getOutPath(), queryConfig.getQueryOutputFile(), QUERY_RESULT_HEADER, outputLines);
            timeLog.writeTimestamps();
            ticketList.clear();
        } catch (IOException  e) {
            System.out.println("Error reading CSV files or processing MapReduce job");
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            HazelcastClient.shutdownAll();
        }
    }

    public static boolean hasInfraction(Ticket ticket, Map<String, Infraction> infractions) {
        return infractions.containsKey(ticket.getInfractionCode());
    }

}
