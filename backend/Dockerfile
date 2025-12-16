FROM openjdk:17-alpine

COPY build/libs/backend-0.0.1-SNAPSHOT.jar /backend.jar
COPY wait-for-db.sh /wait-for-db.sh

CMD ["/wait-for-db.sh", "java", "-jar", "/backend.jar"]