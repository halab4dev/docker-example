This is a simple example of application running with docker, which include 2 containers:
- Mongo db
- A java application that insert and read data from mongo

## Requirement
- [JDK 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
- [Maven](https://maven.apache.org/install.html)
- [Docker](https://docs.docker.com/install/linux/docker-ce/binaries/)
- [Docker-compose](https://docs.docker.com/compose/install/)

## Instruction
- Clone source code
- Build jar file with maven
- Copy jar file to `/opt/docker/application`
- Start services `sudo docker-compose -f docker-compose-example.yml up`
- The result should be like this
```
application-docker | ===== Application information =====
application-docker | Name: Docker example
application-docker | Description: Docker compose example, include mongodb and a java application
application-docker | Created date: Thu May 17 00:00:00 UTC 2018
application-docker | Last run time: Thu May 17 09:57:26 UTC 2018
```

## Explanation
### Docker compose file
This is the content of docker compose file
```yaml
version: "3"

# Containers
services:

  mongodb:
    image: mongo
    container_name: mongo-docker
    ports:
      - "27017:27017"
    environment:
      - MONGO_INITDB_ROOT_USERNAME=halab
      - MONGO_INITDB_ROOT_PASSWORD=superman
    volumes:
      - "/opt/docker/mongo:/data/db"
    networks:
      - docker-example

  application:
    image: openjdk:8
    container_name: application-docker
    depends_on:
      - mongodb
    volumes:
      - /opt/docker/application:/opt/application
    command: java -jar /opt/application/docker-example-1.0-SNAPSHOT.jar
    networks:
      - docker-example

networks: 
  docker-example:
```
#### The docker compose version
```yaml
version: "3
```
The first line is [the version of docker compose file](https://docs.docker.com/compose/compose-file/compose-versioning/)

#### Services and network
```yaml
services:

  mongodb:
    ...
    networks:
      - docker-example

  application:
    ...
    networks:
      - docker-example

networks: 
  docker-example:
```
Our system has 2 service: a mongodb and a java application, they must be in the same network to connect to each other. 
Name the services and network as you like.

##### Mongodb service
```yaml
services:

  mongodb:
    image: mongo
    container_name: mongo-docker
    ports:
      - "27017:27017"
    environment:
      - MONGO_INITDB_ROOT_USERNAME=halab
      - MONGO_INITDB_ROOT_PASSWORD=superman
    volumes:
      - "/opt/docker/mongo:/data/db"
    networks:
      - docker-example
```
- Field `image`: use [mongo image](https://hub.docker.com/_/mongo/) on docker hub
- Field `container_name`: set the container name
- Field `ports`: Open port to connect to mongo from out side of container
- Field `environment`: set initial `root` account with username `halab` and password `superman`
- Field `volumes`: synchronize folder `data/db` in the container with folder `/opt/docker/mongo` in the computer

##### Application service
```yaml
  application:
    image: openjdk:8
    container_name: application-docker
    depends_on:
      - mongodb
    volumes:
      - /opt/docker/application:/opt/application
    command: java -jar /opt/application/docker-example-1.0-SNAPSHOT.jar
    networks:
      - docker-example
```
- Field `image`: use [open jdk image](https://hub.docker.com/_/openjdk/) on docker hub
- Field `container_name`: set the container name
- Field `depends_on`: this container depends on `mongodb` containers, it will start after `mongodb`
- Field `volumes`: synchronize folder `/opt/application` in the container with folder `/opt/docker/application` in the 
computer. We will copy jar file to this location.
- Field `command`: run this command when container start (Start java application).

### Java application
```java
public class Application {

    private static final String DB_HOST = "mongo-docker";
    private static final int DB_PORT = 27017;
    private static final String DB_USER = "halab";
    private static final String DB_PASSWORD = "superman";
    private static final String DB_AUTHENTICATION_DB = "admin";
    ...
    public static MongoClient connectDatabase() {
        MongoCredential credential = MongoCredential.createCredential(DB_USER, DB_AUTHENTICATION_DB, 
                DB_PASSWORD.toCharArray());
        MongoClientOptions option = new MongoClientOptions.Builder().build();
        return new MongoClient(new ServerAddress(DB_HOST, DB_PORT), credential, option);
    }
    ...
}
```
The java application must connect mongo db with username, password set in mongodb service. The `DB_HOST` must be 
`mongo-docker` - the name of mongo db container
