FROM eclipse-temurin:19.0.2_7-jre-jammy as builder
ARG MODULE_NAME
ARG JAR_FILE=$MODULE_NAME/target/*.jar
COPY ${JAR_FILE} application.jar
RUN java -Djarmode=layertools -jar application.jar extract

FROM eclipse-temurin:19.0.2_7-jre-jammy
COPY --from=builder dependencies/ ./
COPY --from=builder snapshot-dependencies/ ./
COPY --from=builder spring-boot-loader/ ./
COPY --from=builder application/ ./
ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]
