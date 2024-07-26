FROM openjdk:17-oracle

CMD ["./gradlew", "clean", "build"]

VOLUME /tmp

ARG JAR_FILE=build/libs/*.jar

COPY ${JAR_FILE} app.jar

RUN mkdir -p /app/logs
VOLUME /app/logs

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app.jar"]
