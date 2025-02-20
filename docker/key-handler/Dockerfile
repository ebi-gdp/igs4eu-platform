# Perform the extraction in a separate builder container
ARG TARGET_PLATFORM
FROM --platform=${TARGET_PLATFORM} eclipse-temurin:21-jre-jammy AS builder
WORKDIR /builder
ARG MODULE_NAME
# This points to the built jar file in the target folder
ARG JAR_FILE=$MODULE_NAME/target/*.jar
# Copy the jar file to the working directory and rename it to application.jar
COPY ${JAR_FILE} application.jar
# Extract the jar file using an efficient layout
RUN java -Djarmode=tools -jar application.jar extract --layers --destination extracted

# This is the runtime container
FROM --platform=${TARGET_PLATFORM} eclipse-temurin:21-jre-jammy
WORKDIR /application

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

# Copy the extracted jar contents from the builder container into the working directory in the runtime container
# Every copy step creates a new docker layer
# This allows docker to only pull the changes it really needs
COPY --from=builder /builder/extracted/dependencies/ ./
COPY --from=builder /builder/extracted/spring-boot-loader/ ./
COPY --from=builder /builder/extracted/snapshot-dependencies/ ./
COPY --from=builder /builder/extracted/application/ ./
# Start the application jar - this is not the uber jar used by the builder
ENTRYPOINT ["java", "-jar", "application.jar"]
