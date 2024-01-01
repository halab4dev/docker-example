This is a simple example of application running with docker, which include 2 containers:
- Mongo db
- A java application that insert and read data from mongo

## Requirement
- [Docker](https://docs.docker.com/install/linux/docker-ce/binaries/)
- [Docker-compose](https://docs.docker.com/compose/install/)

## Instruction
- Clone source code
- Build jar file
```shell
docker run \
  --rm \
  -v $(pwd):/opt/maven \
  -v ~/.m2:/root/.m2 \
  -w /opt/maven \
  --name docker-example-builder \
  maven:3.8.6-amazoncorretto-17 \
  mvn clean install
```
- Start services `sudo docker-compose up`
- The result should be like this
```
docker-example-application | ===== Application information =====
docker-example-application | Name: Docker example
docker-example-application | Description: Docker compose example, include mongodb and a java application
docker-example-application | Created date: Mon Jan 01 23:05:00 GMT 2024
docker-example-application | Last run time: Mon Jan 01 16:08:46 GMT 2024
```

## Explanation
### Docker compose file
This is the content of docker compose file
```yaml
# Version of docker compose. See https://docs.docker.com/compose/compose-file/compose-versioning/#versioning
version: "3.8"

networks:
  docker-example:
    driver: bridge

services:

  mongodb:
    image: mongo:7.0 # name of image in docker hub
    container_name: docker-example-mongo
    ports:
      - ${MONGO_PORT}:27017 # map listen port of container with port of running computer
    env_file:
      - .env
    environment:
      - MONGO_INITDB_ROOT_USERNAME=${MONGO_USERNAME}
      - MONGO_INITDB_ROOT_PASSWORD=${MONGO_PASSWORD}
    volumes:
      - ./data/mongo:/data # map folder /data in container with folder ./data/mongo in running computer
    networks:
      - docker-example

  application:
    image: amazoncorretto:17-alpine3.16
    container_name: docker-example-application
    env_file:
      - .env
      - .env.application
    depends_on:
      - mongodb
    volumes:
      - ./target:/app
    command: java -jar /app/docker-example-1.0-SNAPSHOT.jar
    networks:
      - docker-example
```
#### The docker compose version
```yaml
version: "3.8"
```
The first line is [the version of docker compose file](https://docs.docker.com/compose/compose-file/compose-versioning/)

#### Services and network
```yaml
networks:
  docker-example:
    driver: bridge
    
services:

  mongodb:
    ...
    networks:
      - docker-example

  application:
    ...
    networks:
      - docker-example
```
Our system has 2 service: a mongodb and a java application, they must be in the same network to connect to each other. 
Name the services and network as you like.

##### Mongodb service
```yaml
services:

  mongodb:
  image: mongo:7.0 # name of image in docker hub
  container_name: docker-example-mongo
  ports:
    - ${MONGO_PORT}:27017 # map listen port of container with port of running computer
  env_file:
    - .env
  environment:
    - MONGO_INITDB_ROOT_USERNAME=${MONGO_USERNAME}
    - MONGO_INITDB_ROOT_PASSWORD=${MONGO_PASSWORD}
  volumes:
    - ./data/mongo:/data # map folder /data in container with folder ./data/mongo in running computer
  networks:
    - docker-example
```
- Field `image`: use [mongo image](https://hub.docker.com/_/mongo/) on docker hub
- Field `container_name`: set the container name
- Field `ports`: Open port to connect to mongo from outside of container
- Field `environment`: set initial `root` account with username and password from `.env` file
- Field `volumes`: synchronize folder `/data` in the container with folder `./data/mongo` in the computer

##### Application service
```yaml
    application:
    image: amazoncorretto:17-alpine3.16
    container_name: docker-example-application
    env_file:
      - .env
      - .env.application
    depends_on:
      - mongodb
    volumes:
      - ./target:/app
    command: java -jar /app/docker-example-1.0-SNAPSHOT.jar
    networks:
      - docker-example
```
- Field `image`: use [amazoncorretto image](https://hub.docker.com/_/amazoncorretto) on docker hub
- Field `container_name`: set the container name
- Field `depends_on`: this container depends on `mongodb` containers, it will start after `mongodb`
- Field `volumes`: synchronize folder `/app` in the container with folder `./target` in the 
computer.
- Field `command`: run this command when container start (Start java application).

### Java application env file (`.env.application`)
```properties
MONGO_HOST=docker-example-mongo
MONGO_PORT=27017
MONGO_USERNAME="dbuser"
MONGO_PASSWORD="dbpassword"
MONGO_DATABASE=docker_example_db
MONGO_URI=mongodb://${MONGO_USERNAME}:${MONGO_PASSWORD}@${MONGO_HOST}:${MONGO_PORT}/${MONGO_DATABASE}?authSource=admin
```
The java application must connect mongo db with host, port, username and  password. The `MONGO_HOST` must be 
`docker-example-mongo` - the name of mongo db container
