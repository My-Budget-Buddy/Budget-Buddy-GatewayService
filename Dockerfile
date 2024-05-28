FROM public.ecr.aws/c1x4i8c4/alpine:latest as build
WORKDIR /app
COPY . /app
RUN apk update && apk upgrade && apk add openjdk17-jdk maven && mvn clean package -DskipTests

FROM public.ecr.aws/c1x4i8c4/alpine:latest
RUN apk update && apk upgrade && apk add openjdk17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar /app/app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]