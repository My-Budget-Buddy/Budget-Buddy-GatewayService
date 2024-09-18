FROM alpine:latest

RUN apk update && apk upgrade
RUN apk add --no-cache openjdk17-jre

WORKDIR /app

COPY target/*.jar /app/app.jar

EXPOSE 8125

CMD ["java", "-jar", "app.jar"]