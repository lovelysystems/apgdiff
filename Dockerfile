FROM openjdk:14-jdk-alpine
COPY *-fat.jar /usr/local/lib/apgdiff.jar
ENTRYPOINT ["java", "-jar", "/usr/local/lib/apgdiff.jar"]
