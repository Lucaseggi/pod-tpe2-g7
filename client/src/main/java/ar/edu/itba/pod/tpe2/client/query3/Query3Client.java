package ar.edu.itba.pod.tpe2.client.query3;

import ar.edu.itba.pod.tpe2.client.utils.HazelcastConfig;
import ar.edu.itba.pod.tpe2.client.utils.QueryConfig;
import ar.edu.itba.pod.tpe2.client.utils.TimestampLogger;
import ar.edu.itba.pod.tpe2.client.utils.parsing.BaseArguments;
import ar.edu.itba.pod.tpe2.client.utils.parsing.QueryParser;
import ar.edu.itba.pod.tpe2.client.utils.parsing.QueryParserFactory;
import ar.edu.itba.pod.tpe2.models.infraction.Infraction;
import ar.edu.itba.pod.tpe2.models.ticket.Ticket;
import ar.edu.itba.pod.tpe2.query1.Query1Collator;
import ar.edu.itba.pod.tpe2.query1.Query1CombinerFactory;
import ar.edu.itba.pod.tpe2.query1.Query1Mapper;
import ar.edu.itba.pod.tpe2.query1.Query1ReducerFactory;
import ar.edu.itba.pod.tpe2.query3.Query3Collator;
import ar.edu.itba.pod.tpe2.query3.Query3CombinerFactory;
import ar.edu.itba.pod.tpe2.query3.Query3Mapper;
import ar.edu.itba.pod.tpe2.query3.Query3ReducerFactory;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IList;
import com.hazelcast.mapreduce.Job;
import com.hazelcast.mapreduce.JobTracker;
import com.hazelcast.mapreduce.KeyValueSource;
import org.apache.commons.cli.ParseException;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import static ar.edu.itba.pod.tpe2.client.utils.CSVUtils.*;

public class Query3Client {

    private static final String QUERY_NAME = "query3";
    private static final String QUERY_RESULT_HEADER = "Issuing Agency;Percentage";
    private static final String CNP = "g7-"; // Cluster Name Prefix

    public static void main(String[] args) {

        QueryParser parser = QueryParserFactory.getParser(QUERY_NAME);

        Query3Arguments arguments;
        try {
            arguments = (Query3Arguments) parser.getArguments(args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            return;
        }
        QueryConfig queryConfig = new QueryConfig(QUERY_NAME + ".csv", "time3.txt");

        // Hazelcast client Config
        HazelcastInstance hazelcastInstance = HazelcastConfig.configureHazelcastClient(arguments);

        TimestampLogger timeLog = new TimestampLogger(arguments.getOutPath(), queryConfig.getTimeOutputFile());

        try {

            String city = arguments.getCity();

            // Load infractions from CSV
            timeLog.logStartReading();

            Map<String, Infraction> infractions = new ConcurrentHashMap<>();
            parseInfractions(arguments.getInPath(), city, infractions);

            // Load tickets from CSV
            IList<Ticket> ticketList = hazelcastInstance.getList(CNP + "ticketList");
            ticketList.clear();
            parseTickets(arguments.getInPath(), city, ticketList, ticket -> hasInfraction(ticket, infractions));

            timeLog.logEndReading();

            JobTracker jobTracker = hazelcastInstance.getJobTracker(CNP + QUERY_NAME + "jobTracker");
            KeyValueSource<String, Ticket> source = KeyValueSource.fromList(ticketList);

            Job<String, Ticket> job = jobTracker.newJob(source);
            timeLog.logStartMapReduce();
            Map<String, String> result = job
                    .mapper(new Query3Mapper())
                    .combiner(new Query3CombinerFactory())
                    .reducer(new Query3ReducerFactory())
                    .submit(new Query3Collator(arguments.getN()))
                    .get();
            timeLog.logEndMapReduce();

            List<String> outputLines = result
                    .entrySet()
                    .stream()
                    .map(entry -> entry.getKey() + ";" + entry.getValue())
                    .toList();

            writeQueryResults(arguments.getOutPath(), queryConfig.getQueryOutputFile(), QUERY_RESULT_HEADER, outputLines);
            timeLog.writeTimestamps();
            ticketList.clear();
        } catch (IOException e) {
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
