FROM amazoncorretto:17.0.11
COPY build/libs/auto_enter_view-0.0.1-SNAPSHOT.jar auto_enter_view-0.0.1-SNAPSHOT.jar
CMD ["java", "-jar", "auto_enter_view-0.0.1-SNAPSHOT.jar"]