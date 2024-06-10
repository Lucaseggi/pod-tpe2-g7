# tpe2-g7
POD

## Descripción

## Contenidos
- [Instalación](#instalación)
   - [Prerrequisitos](#prerrequisitos)
   - [Pasos](#pasos)
- [Uso](#uso)
   - [Servidor](#servidor)
   - [Cliente](#cliente)
      - [Query 1: Total de Multas por Tipo de Infracción](#query-1-total-de-multas-por-tipo-de-infracción)
      - [Query 2: Top 3 infracciones más populares de cada barrio](#query-2-top-3-infracciones-más-populares-de-cada-barrio)
      - [Query 3: Top N agencias con mayor porcentaje de recaudación](#query-3-top-n-agencias-con-mayor-porcentaje-de-recaudación)
      - [Query 4: Patente con más infracciones de cada barrio en el rango [from, to]](#query-4-patente-con-más-infracciones-de-cada-barrio-en-el-rango-from-to)
      - [Query 5: Pares de infracciones que tienen, en grupos de a cientos, el mismo promedio de monto de multa](#query-5-pares-de-infracciones-que-tienen-en-grupos-de-a-cientos-el-mismo-promedio-de-monto-de-multa)
- [Integrantes del Grupo](#integrantes-del-grupo)

## Instalación

### Prerrequisitos
- Java 17 o mayor
- Maven
- Hazelcast 3.6.8

### Pasos
1. Clonar el repositorio:
   ```bash
   git clone https://github.com/Lucaseggi/tpe2-g7.git
   ```
   
2. Moverse al directrio del proyecto:
   ```bash
   cd tpe2-g7
   ```
   
3. Compilar el proyecto:
   ```bash
    mvn clean install
    ```
   
   
## Uso
Moverse a la carpeta de los scripts de ejecución.
```bash
cd ./resources/scripts
```
Aquí encontrará los scripts de ejecución para levantar un nodo en el cluster de hazelcast y los scripts para ejecutar las queries.

### Servidor

Para lanzar una instancia de hazelcast:
```bash
sh server.sh -Dname=<cluster_name> -Dpass=<cluster_password> -Dinterfaces='<ip1>;<ip2>;...' -Dport=<port_number> 
```

| Parametro      | Opciones            | Descripción                                             | Opcional | Valor por defecto |
|----------------|---------------------|--------------------------------------------------------|----------|-------------------|
| `-Dname`       | `cluster_name`      | Define el nombre del cluster al cual se desea conectar | SI       | `g7`              |
| `-Dpass`       | `cluster_password`  | Contraseña de cluster de hazelcast seleccionado        | SI       | `g7-pass`         |
| `-Dinterfaces` | `'<ip1>;<ip2>;...'` | Interfaces que probará Hazelcast                       | SI       | `192.168.0.*`     |
| `-Dport`       | `port_number`       | Puerto donde se desa correr la instancia               | SI       | `5701`            |

### Cliente

Las diferentes queries requeiren de una fuente de información para ser procesadas (debe tener ambos archivos `ticketsX.csv` y `infractionsX.csv` donde `X` es o bien NYC o CHI). Cada query generará como resultado un archivo en formato CSV con lo obtenido por la query y otro en formato TXT con los tiempos de ejecución.
#### Query 1: Total de Multas por Tipo de Infracción
```bash
sh query1.sh -Daddresses='<ip1>:<port1>;<ip2>:<port2>;...' -Dcity=[NYC | CHI] -DinPath=<input_path> -DoutPath=<output_path>
```


| Parametro       | Opciones                            | Descripción                                                      | Opcional | Valor por defecto |
|-----------------|-------------------------------------|------------------------------------------------------------------|----------|-------------------|
| `-Daddresses`   | `'<ip1>:<port1>;<ip2>:<port2>;...'` | Direcciones y puertos de los nodos que se quieren usar           | NO       | Ninguno           |
| `-Dcity`        | `city`                              | Ciudad de interés                                                | NO       | Ninguno           |
| `-DinPath`      | `input_path`                        | Ruta al directorio de los archivos fuente                        | NO       | Ninguno     |
| `-DoutPath`     | `output_path`                       | Ruta al direcotrio donde se generarán los archivos de resultados | NO       | Ninguno            |
| `-DclusterName` | `cluster_name`      | Define el nombre del cluster al cual se desea conectar | SI       | `g7`              |
| `-DclusterPass` | `cluster_password`  | Contraseña de cluster de hazelcast seleccionado        | SI       | `g7-pass`         |


#### Query 2: Top 3 infracciones más populares de cada barrio
```bash
sh query1.sh -Daddresses='<ip1>:<port1>;<ip2>:<port2>;...' -Dcity=[NYC | CHI] -DinPath=<input_path> -DoutPath=<output_path>
```


| Parametro       | Opciones                            | Descripción                                                      | Opcional | Valor por defecto |
|-----------------|-------------------------------------|------------------------------------------------------------------|----------|-------------------|
| `-Daddresses`   | `'<ip1>:<port1>;<ip2>:<port2>;...'` | Direcciones y puertos de los nodos que se quieren usar           | NO       | Ninguno           |
| `-Dcity`        | `city`                              | Ciudad de interés                                                | NO       | Ninguno           |
| `-DinPath`      | `input_path`                        | Ruta al directorio de los archivos fuente                        | NO       | Ninguno     |
| `-DoutPath`     | `output_path`                       | Ruta al direcotrio donde se generarán los archivos de resultados | NO       | Ninguno            |
| `-DclusterName` | `cluster_name`      | Define el nombre del cluster al cual se desea conectar | SI       | `g7`              |
| `-DclusterPass` | `cluster_password`  | Contraseña de cluster de hazelcast seleccionado        | SI       | `g7-pass`         |

#### Query 3: Top N agencias con mayor porcentaje de recaudación
```bash
sh query1.sh -Daddresses='<ip1>:<port1>;<ip2>:<port2>;...' -Dcity=[NYC | CHI] -DinPath=<input_path> -DoutPath=<output_path> -Dn=<number>
```

| Parametro       | Opciones                            | Descripción                                                      | Opcional | Valor por defecto |
|-----------------|-------------------------------------|------------------------------------------------------------------|----------|-------------------|
| `-Daddresses`   | `'<ip1>:<port1>;<ip2>:<port2>;...'` | Direcciones y puertos de los nodos que se quieren usar           | NO       | Ninguno           |
| `-Dcity`        | `city`                              | Ciudad de interés                                                | NO       | Ninguno           |
| `-DinPath`      | `input_path`                        | Ruta al directorio de los archivos fuente                        | NO       | Ninguno     |
| `-DoutPath`     | `output_path`                       | Ruta al direcotrio donde se generarán los archivos de resultados | NO       | Ninguno            |
| `-Dn`           | `number`                            | Cantidad de agencias que se quiere ver en el Top                 | NO       | Ninguno            |
| `-DclusterName` | `cluster_name`                      | Define el nombre del cluster al cual se desea conectar           | SI       | `g7`              |
| `-DclusterPass` | `cluster_password`                  | Contraseña de cluster de hazelcast seleccionado                  | SI       | `g7-pass`         |


#### Query 4: Patente con más infracciones de cada barrio en el rango [from, to]
```bash
sh query1.sh -Daddresses='<ip1>:<port1>;<ip2>:<port2>;...' -Dcity=[NYC | CHI] -DinPath=<input_path> -DoutPath=<output_path> -Dfrom=<from_date> -Dto=<to_date>
```

| Parametro       | Opciones                            | Descripción                                                             | Opcional | Valor por defecto |
|-----------------|-------------------------------------|-------------------------------------------------------------------------|----------|-------------------|
| `-Daddresses`   | `'<ip1>:<port1>;<ip2>:<port2>;...'` | Direcciones y puertos de los nodos que se quieren usar                  | NO       | Ninguno           |
| `-Dcity`        | `city`                              | Ciudad de interés                                                       | NO       | Ninguno           |
| `-DinPath`      | `input_path`                        | Ruta al directorio de los archivos fuente                               | NO       | Ninguno     |
| `-DoutPath`     | `output_path`                       | Ruta al direcotrio donde se generarán los archivos de resultados        | NO       | Ninguno            |
| `-Dfrom`        | `from_date`                         | Fecha a partir de la cual es de interes analizar en formato (dd/MM/yyy) | NO       | Ninguno            |
| `-Dto`          | `to_date`                           | Fecha hasta la cual es de interes analizar formato (dd/MM/yyy)          | NO       | Ninguno            |
| `-DclusterName` | `cluster_name`                      | Define el nombre del cluster al cual se desea conectar                  | SI       | `g7`              |
| `-DclusterPass` | `cluster_password`                  | Contraseña de cluster de hazelcast seleccionado                         | SI       | `g7-pass`         |


#### Query 5: Pares de infracciones que tienen, en grupos de a cientos, el mismo promedio de monto de multa
```bash
sh query1.sh -Daddresses='<ip1>:<port1>;<ip2>:<port2>;...' -Dcity=[NYC | CHI] -DinPath=<input_path> -DoutPath=<output_path>
```

| Parametro       | Opciones                            | Descripción                                                      | Opcional | Valor por defecto |
|-----------------|-------------------------------------|------------------------------------------------------------------|----------|-------------------|
| `-Daddresses`   | `'<ip1>:<port1>;<ip2>:<port2>;...'` | Direcciones y puertos de los nodos que se quieren usar           | NO       | Ninguno           |
| `-Dcity`        | `city`                              | Ciudad de interés                                                | NO       | Ninguno           |
| `-DinPath`      | `input_path`                        | Ruta al directorio de los archivos fuente                        | NO       | Ninguno     |
| `-DoutPath`     | `output_path`                       | Ruta al direcotrio donde se generarán los archivos de resultados | NO       | Ninguno            |
| `-DclusterName` | `cluster_name`      | Define el nombre del cluster al cual se desea conectar | SI       | `g7`              |
| `-DclusterPass` | `cluster_password`  | Contraseña de cluster de hazelcast seleccionado        | SI       | `g7-pass`         |


## Integrantes del Grupo
- [Christian Ijjas](https://github.com/cijjas) - Legajo: 63555
- [Luca Seggiaro](https://github.com/Lucaseggi) - Legajo: 62855
- [Manuel Dithurbide](https://github.com/manudithur) - Legajo: 62057
- [Tobias Perry](https://github.com/TobiasPerry) - Legajo: 62064
