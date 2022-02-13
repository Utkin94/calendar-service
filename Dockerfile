FROM openjdk:11-slim

WORKDIR ./home/app

COPY ./target/calendar-service-0.0.1-SNAPSHOT.jar ./

EXPOSE 8080

CMD java -jar calendar-service-0.0.1-SNAPSHOT.jar