# run with docker run -p <your port of choice>:5000 .
# from https://spring.io/blog/2018/11/08/spring-boot-in-a-container

FROM openjdk:8-jdk-alpine
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]