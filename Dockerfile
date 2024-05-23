FROM eclipse-temurin:17-jre-alpine

RUN apk update && apk upgrade
      
WORKDIR /app
      
COPY target/*.jar /app/app.jar
      
EXPOSE 8125
      
CMD ["java", "-jar", "app.jar"]