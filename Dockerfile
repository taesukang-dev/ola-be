FROM openjdk:11-jre-slim

WORKDIR /home/ubuntu/

COPY ola-0.0.1-SNAPSHOT.jar .

CMD java -jar ola-0.0.1-SNAPSHOT.jar