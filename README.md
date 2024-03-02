# E-Commerce Microservices using Saga Choreography Pattern
This repository contains the E-Commerce microservices using Saga Choreography pattern for data consistency.

## How to run the application using Docker

1. Run `mvn clean install` to build the applications and create the docker image locally.
2. Run `docker-compose --profile services up` to start the applications.

## How to run the application without Docker

1. Run `mvn clean install` by going inside each folder to build the applications.
2. After that run `mvn spring-boot:run` by going inside each folder to start the applications.

