FROM openjdk:17-alpine
WORKDIR /app
COPY . /app
RUN apk update && apk upgrade && apk add openjdk17-jdk maven && mvn clean package -DskipTests

FROM openjdk:17-alpine
RUN apk update && apk upgrade && apk add openjdk17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar /app/app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]