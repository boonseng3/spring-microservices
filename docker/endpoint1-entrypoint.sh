#!/bin/sh
while !  nc -z eureka 8761 ; do
    echo "Waiting for Eureka server"
    sleep 2
done
echo "Eureka server up"

java -jar -Xms1024m /home/service/endpoint1-0.0.1-SNAPSHOT.jar

