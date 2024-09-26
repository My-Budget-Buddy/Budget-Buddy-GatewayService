FROM alpine:latest
  
  RUN apk update && apk upgrade && apk add openjdk17-jre
  
  WORKDIR /app
        
  COPY target/*.jar /app/app.jar
        
  EXPOSE 8125
        
  CMD ["java", "-jar", "app.jar"]