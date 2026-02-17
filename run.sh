#!/bin/bash

./gradlew shadowJar
java -jar build/libs/tss-all.jar
