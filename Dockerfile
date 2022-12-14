FROM eclipse-temurin:17-jre as builder
WORKDIR application
ARG JAR_FILE=timestamps-service/build/libs/*[^a-zA-Z]*.jar
COPY ${JAR_FILE} application.jar
RUN java -Djarmode=layertools -jar application.jar extract

# the second stage of our build will copy the extracted layers
FROM eclipse-temurin:17-jre
WORKDIR application
COPY --from=builder application/dependencies/ ./
COPY --from=builder application/spring-boot-loader/ ./
COPY --from=builder application/snapshot-dependencies/ ./
COPY --from=builder application/application/ ./

EXPOSE 8080/tcp

ENV SERVER_PORT 8080

ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]
