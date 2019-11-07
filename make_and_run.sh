#!/bin/sh

mvn package
if [ $? -eq 0 ]
then
  cp target/fbz_server-1.0.0-jar-with-dependencies.jar server.jar
  java -jar server.jar $1
fi
