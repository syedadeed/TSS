#!/bin/bash

# ./gradlew test
./gradlew shadowJar
java -jar build/libs/tss-all.jar

# sudo docker run --name dev-postgres \
#   -e POSTGRES_USER=dev \
#   -e POSTGRES_PASSWORD=devpass \
#   -e POSTGRES_DB=devdb \
#   -p 5432:5432 \
#   -v pgdata:/var/lib/postgresql \
#   -d postgres:latest
