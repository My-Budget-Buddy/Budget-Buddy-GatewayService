FROM maven:3.8.3-amazoncorretto-17 as build
WORKDIR /app
COPY . /app
RUN mvn clean install -DskipTests

FROM openjdk:17
WORKDIR /app
COPY --from=build /app/target/*.jar /app/app.jar