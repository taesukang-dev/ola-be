FROM openjdk:11-jre-slim

WORKDIR /home/ubuntu/

COPY demo-0.0.1-SNAPSHOT.jar .

CMD java -jar demo-0.0.1-SNAPSHOT.jar