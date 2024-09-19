FROM maven:3.9.9-amazoncorretto-17 as build
WORKDIR /app
COPY . /app
RUN mvn clean install -DskipTests

FROM amazoncorretto:17-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar /app/app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]