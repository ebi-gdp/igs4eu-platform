FROM --platform=linux/amd64 eclipse-temurin:21-jre-jammy AS builder
ARG MODULE_NAME
ARG JAR_FILE=$MODULE_NAME/target/*.jar
COPY ${JAR_FILE} application.jar
RUN java -Djarmode=layertools -jar application.jar extract

FROM --platform=linux/amd64 eclipse-temurin:21-jre-jammy

## Build crypt4gh library & its dependencies
# Install python package
RUN apt-get update && \
    apt-get install -y python3 python3-pip python3-venv expect

# Set environment variable to include pipx in PATH
ENV PATH=/root/.local/bin:$PATH

# Install pipx
RUN pip3 install --user pipx && \
    pipx ensurepath

# Install crypt4gh using pipx
RUN pipx install crypt4gh

# Copy dependencies from builder image
COPY --from=builder dependencies/ ./
COPY --from=builder snapshot-dependencies/ ./
COPY --from=builder spring-boot-loader/ ./
COPY --from=builder application/ ./
ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]
