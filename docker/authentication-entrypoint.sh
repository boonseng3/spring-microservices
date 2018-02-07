#!/bin/sh

while !  nc -z mysql 3306 ; do
    echo "Waiting for MySql server"
    sleep 2
done
echo "MySql server up"

while !  nc -z eureka 8761 ; do
    echo "Waiting for Eureka server"
    sleep 2
done
echo "Eureka server up"

while !  nc -z redis 6379 ; do
    echo "Waiting for Redis server"
    sleep 2
done
echo "Redis server up"

java -jar -Xms1024m /home/service/authentication-0.0.1-SNAPSHOT.jar

