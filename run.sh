#!/bin/bash

./gradlew shadowJar
java -jar build/libs/tss-all.jar

# sudo docker run --name dev-postgres \
#   -e POSTGRES_USER=dev \
#   -e POSTGRES_PASSWORD=devpass \
#   -e POSTGRES_DB=devdb \
#   -p 5432:5432 \
#   -v pgdata:/var/lib/postgresql \
#   -v "$(pwd)/src/main/resources/db/migration/V1__init_schema.sql:/docker-entrypoint-initdb.d/init_schema.sql:ro" \
#   -d postgres:latest
