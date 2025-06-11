# Pipeline Manager Service

The Pipeline Manager is a core service and the entry point for most operations related to dataset management, genetic scoring pipeline execution, and Globus file operations.

## Key responsibilities

### Dataset management
- Handles **creation, deletion, and retrieval** of datasets.
- Stores metadata for **datasets, Globus files, and pipeline executions** in the database.

### File handling & integration with Globus
- Interacts with the **File Handler service** to manage file operations.
- Triggers **Globus file service** operations on **Guest Collection** and validates files on **Globus**.

### PGS catalog integration
- Queries **PGS Catalog APIs** to fetch **Trait IDs** and **Publication IDs**.

### Pipeline submission & execution
- Allows users to submit **genetic scoring pipelines** via:
   - **PGS IDs**
   - **Trait selection**
   - **Publication selection**
- Enforces **submission restrictions** using a **request limiter** (per user, per day).
- **Triggers pipeline jobs** by sending JSON messages to **Kafka**.
- Listens to the **pipeline-status topic** for execution updates.

### Automated dataset cleanup
- Provides an API (protected via **Basic Auth**) for **cron jobs** to delete expired datasets.

### Notifications & reports
- **Sends email notifications** to users upon **pipeline completion**, indicating success or failure.
- Allows users to **download polygenic risk score reports**.

The **Pipeline Manager** is a central service that orchestrates **dataset operations, pipeline executions, and file interactions**, ensuring seamless integration between platform components.

# GCP running dependencies

To run the **Pipeline Manager Service**, considering two options:
- **On GCP Kubernetes (GKE)**
- **Locally**

### Database Access
This service requires access to a **database** for storing platform data.

| Environment          | Authentication Method                                                 | Notes                                                                                                                                                                                                     |
|----------------------|-----------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| GCP Kubernetes (GKE) | Kubernetes Service Account (KSA) bound to a GCP Service Account (GSA) | The GSA must have **permissions to connect to and operate the database**.                                                                                                                                 |
| Local Development    | Key file for GCP Service Account                                      | Set the path of the key file to the environment variable: <br> `GOOGLE_APPLICATION_CREDENTIALS=<path-to-key-file>` (Not recommended for production). <br/>You can download this key from GCP web console. |

Ensure these dependencies are correctly set up before running the **Pipeline Manager Service**.

Refer to the configurations below. Properties with values starting with @ are placeholders that should be defined in Maven's `settings.xml` file.
`E.g. @datasource.intervene.host@`

Once you add config/properties inside `settings.xml`, build the project. Upon successful build you can run application locally.
Please check following dependant services are up & running.

# Application properties

## Application configuration `application.properties`

| Property                   | Value                     | Description                                      |
|----------------------------|---------------------------|--------------------------------------------------|
| `spring.application.name`  | `igs4eu-pipeline-manager` | Defines the name of the Spring Boot application. |
| `spring.webflux.base-path` | `/pipeline-manager`       | Sets the base path for WebFlux endpoints.        |
| `spring.profiles.active`   | `${ENV:dev}`              | Specifies the active Spring profile.             |

## Metrics configuration

| Property                                         | Value                            | Description                                     |
|--------------------------------------------------|----------------------------------|-------------------------------------------------|
| `management.endpoints.web.exposure.include`      | `health,metrics,info,prometheus` | Defines which management endpoints are exposed. |
| `management.observations.key-values.application` | `${spring.application.name}`     | Associates metrics with the application name.   |

## Application configuration `application-{env}.properties`

| Property      | Value  | Description                                         |
|---------------|--------|-----------------------------------------------------|
| `server.port` | `8020` | Defines the port on which the application will run. |

## Datasource configuration

| Property                                               | Value                             | Description                                 |
|--------------------------------------------------------|-----------------------------------|---------------------------------------------|
| `datasource.pipeline-manager.host`                     | `@datasource.intervene.host@`     | Hostname for the database connection.       |
| `datasource.pipeline-manager.username`                 | `@datasource.intervene.username@` | Database username.                          |
| `datasource.pipeline-manager.password`                 | `@datasource.intervene.password@` | Database password.                          |
| `datasource.pipeline-manager.port`                     | `5432`                            | Port for the PostgreSQL database.           |
| `datasource.pipeline-manager.database`                 | `intervene-dev`                   | Database name.                              |
| `datasource.pipeline-manager.schema`                   | `geneticscoresplatform`           | Database schema used for genetic scores.    |
| `datasource.pipeline-manager.ssl-mode`                 | `disable`                         | SSL mode for database connection.           |
| `datasource.pipeline-manager.driver-class-name`        | `org.postgresql.Driver`           | JDBC driver for PostgreSQL.                 |
| `datasource.pipeline-manager.hikari.maximum-pool-size` | `3`                               | Maximum connections in the connection pool. |
| `datasource.pipeline-manager.hikari.minimum-idle`      | `1`                               | Minimum number of idle connections.         |
| `datasource.pipeline-manager.initialization-mode`      | `embedded`                        | Database initialization mode.               |

## Database configuration

| Property                     | Value | Description                                      |
|------------------------------|-------|--------------------------------------------------|
| `expired-dataset.batch-size` | `50`  | Number of expired datasets processed in a batch. |

## OAuth2 configuration

| Property                                                | Value                                     | Description                         |
|---------------------------------------------------------|-------------------------------------------|-------------------------------------|
| `spring.security.oauth2.resourceserver.jwt.issuer-uri`  | `https://login.aai.lifescience-ri.eu/oidc/`    | Issuer URI for JWT authentication.  |
| `spring.security.oauth2.resourceserver.jwt.jwk-set-uri` | `https://login.aai.lifescience-ri.eu/oidc/jwk` | JWK set URI for JWT authentication. |

## Kafka configuration

| Property                                              | Value              | Description                                   |
|-------------------------------------------------------|--------------------|-----------------------------------------------|
| `spring.kafka.bootstrap-servers`                      | `localhost:9092`   | Kafka broker addresses.                       |
| `spring.kafka.consumer.auto-offset-reset`             | `earliest`         | Consumer offset reset strategy.               |
| `spring.kafka.consumer.heartbeat.interval.ms`         | `10000`            | Interval for Kafka consumer heartbeats.       |
| `spring.kafka.consumer.properties.session.timeout.ms` | `60000`            | Session timeout for Kafka consumers.          |
| `spring.kafka.consumer.max-poll-records`              | `1`                | Maximum number of records polled per request. |
| `spring.kafka.consumer.enable-auto-commit`            | `false`            | Auto-commit setting for Kafka consumers.      |
| `spring.kafka.consumer.concurrency`                   | `1`                | Concurrency level for Kafka consumers.        |
| `spring.kafka.producer.properties.linger.ms`          | `1`                | Producer linger time for batching.            |
| `kafka.pipeline-launch.topic`                         | `pipeline-launch`  | Topic for pipeline launch events.             |
| `kafka.pipeline-status.topic`                         | `pipeline-status`  | Topic for pipeline status updates.            |
| `kafka.group.instance-id`                             | `pipeline-manager` | Kafka group instance ID.                      |
| `kafka.listener.max.poll.interval.ms`                 | `3600000`          | Max poll interval for Kafka listeners.        |
| `kafka.listener.default.poll.timeout.ms`              | `500000`           | Default poll timeout for Kafka listeners.     |

## Internal service configuration

| Property                                               | Value                                                                 | Description                                               |
|--------------------------------------------------------|-----------------------------------------------------------------------|-----------------------------------------------------------|
| `intervene.file-handler.base-url`                      | `http://file-handler.{namespace}.svc.cluster.local:8080/file-handler` | Base URL for the File Handler Service.                    |
| `intervene.file-handler.globus.user-details.uri`       | `/globus/user`                                                        | API endpoint for retrieving Globus user details.          |
| `intervene.file-handler.globus.list-files-dir.uri`     | `/globus/guest-collection`                                            | API to list files in the Globus guest collection.         |
| `intervene.file-handler.globus.create-dir.uri`         | `/globus/guest-collection`                                            | API to create directories in the Globus guest collection. |
| `intervene.file-handler.globus.delete-dir.uri`         | `/globus/guest-collection`                                            | API to delete directories in the Globus guest collection. |
| `intervene.file-handler.cloud.list-files.uri`          | `/cloud/storage/files`                                                | API to list files stored in the cloud.                    |
| `intervene.file-handler.cloud.stream-files.uri`        | `/cloud/storage/stream-file`                                          | API to stream files from the cloud.                       |
| `intervene.user-manager.base-url`                      | `http://user-manager.{namespace}.svc.cluster.local:8080/user-manager` | Base URL for the User Manager Service.                    |
| `intervene.user-manager.user-account.uri`              | `/user/account`                                                       | API to retrieve user account details.                     |
| `intervene.user-manager.user-account.consent-data.uri` | `${intervene.user-manager.user-account.uri}/consent/data`             | API to fetch user consent data.                           |
| `intervene.key-handler.base-url`                       | `http://key-handler.{namespace}.svc.cluster.local:8080/key-handler`   | Base URL for the Key Handler Service.                     |
| `intervene.key-handler.keys.uri`                       | `/key`                                                                | API for key management.                                   |
| `intervene.pipeline-request.base-url`                  | `http://localhost:6060`                                               | Base URL for the Pipeline Request Service.                |
| `intervene.pipeline-request.uri`                       | `/launch`                                                             | API to trigger a pipeline request.                        |

`{namespace}` will change according to env. for production, it can be `intervene-prod`. e.g.<br/> `http://user-manager.intervene-prod.svc.cluster.local:8080/user-manager`.

## Cloud storage configuration

### Embassy S3

| Property                                | Value                                     | Description                          |
|-----------------------------------------|-------------------------------------------|--------------------------------------|
| `ebi-embassy.s3.endpoint`               | `https://uk1s3.embassy.ebi.ac.uk`         | S3 endpoint for EBI Embassy storage. |
| `ebi-embassy.s3.credentials.access-key` | `@ebi-embassy.s3.credentials.access-key@` | Access key for Embassy S3 storage.   |
| `ebi-embassy.s3.credentials.secret-key` | `@ebi-embassy.s3.credentials.secret-key@` | Secret key for Embassy S3 storage.   |
| `ebi-embassy.s3.credentials.region`     | `@ebi-embassy.s3.credentials.region@`     | Region for Embassy S3 storage.       |

### Allas S3

| Property                          | Value                               | Description                          |
|-----------------------------------|-------------------------------------|--------------------------------------|
| `allas.s3.endpoint`               | `https://a3s.fi`                    | S3 endpoint for Allas cloud storage. |
| `allas.s3.credentials.access-key` | `@allas.s3.credentials.access-key@` | Access key for Allas S3 storage.     |
| `allas.s3.credentials.secret-key` | `@allas.s3.credentials.secret-key@` | Secret key for Allas S3 storage.     |
| `allas.s3.credentials.region`     | `@allas.s3.credentials.region@`     | Region for Allas S3 storage.         |

### GCP cloud storage

| Property                           | Value                      | Description                                         |
|------------------------------------|----------------------------|-----------------------------------------------------|
| `cloud.gcp.project-id`             | `@cloud.gcp.project-id@`   | Google Cloud Project ID.                            |
| `cloud.storage.bucket-name-format` | `intervene-dev-%s-results` | Format for naming storage buckets in GCP.           |
| `cloud.storage.bucket-prefix`      | `results/%s/score`         | Prefix used for storing result files in the bucket. |

## Email configuration

| Property                                                   | Value                        | Description                                     |
|------------------------------------------------------------|------------------------------|-------------------------------------------------|
| `spring.mail.host`                                         | `outgoing.geneticscores.org` | SMTP server for outgoing emails.                |
| `spring.mail.port`                                         | `587`                        | Port for the mail server.                       |
| `spring.mail.username`                                     | `@intervene.email.username@` | Username for SMTP authentication.               |
| `spring.mail.password`                                     | `@intervene.email.password@` | Password for SMTP authentication.               |
| `spring.mail.properties.mail.smtp.auth`                    | `false`                      | Disables SMTP authentication.                   |
| `spring.mail.properties.mail.smtp.starttls.enable`         | `true`                       | Enables STARTTLS for secure email transmission. |
| `spring.mail.properties.mail.smtp.ssl.checkserveridentity` | `false`                      | Disables SSL server identity verification.      |

## Email template configuration

| Property                  | Value                             | Description                                                               |
|---------------------------|-----------------------------------|---------------------------------------------------------------------------|
| `spring.thymeleaf.prefix` | `file:/application/config/email/` | Location of the email templates.                                          |
| `spring.thymeleaf.suffix` | `.html`                           | File extension for the templates.                                         |
| `spring.thymeleaf.cache`  | `false`                           | Disables template caching for real-time updates. Set true for production. |

### Example templates
Success email template. `pipeline-success.html`.
```
# Success email template. File name: pipeline-success.html. Make sure you create this file at the provided template location

<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title>Pipeline Success Notification</title>
</head>
<body>
<p>Dear <span th:text="${firstName}"></span> <span th:text="${lastName}"></span>,</p>
<p>Pipeline <strong th:text="${pipelineId}"></strong> has been completed successfully!</p>
<p>Please find the download link to the result files</p>
<p><a th:href="${platformURL}" target="_blank">Download files</a></p>
<p><strong>INTERVENE Team</strong></p>
</body>
</html>
```

Failure email template. `pipeline-failure.html`.
```
# Failure email template. File name: pipeline-failure.html. Make sure you create this file at the provided template location

<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title>Pipeline Failure Notification</title>
    <style>
        table, th, td {
            border: 1px solid #FF0000;
            border-collapse: collapse;
            text-align: left;
            padding: 10px;
        }
    </style>
</head>
<body>
<p>Dear <span th:text="${firstName}"></span> <span th:text="${lastName}"></span>,</p>
<p>Your pipeline instance <strong th:text="${pipelineId}"></strong> has failed.</p>
<p>Please find the error details below:</p>
<table>
    <tr>
        <th>Flag</th>
        <th>Value</th>
    </tr>
    <tr>
        <td>Status</td>
        <td>Error</td>
    </tr>
    <tr>
        <td>Trace name</td>
        <td th:text="${traceName}"></td>
    </tr>
    <tr>
        <td>Trace exit</td>
        <td th:text="${traceExit}"></td>
    </tr>
</table>
<p><strong>INTERVENE Team</strong></p>
</body>
</html>
```

## Redis configuration

| Property                        | Value       | Description            |
|---------------------------------|-------------|------------------------|
| `spring.data.redis.host`        | `localhost` | Redis server hostname. |
| `spring.data.redis.port`        | `6379`      | Redis server port.     |
| `spring.data.redis.client-type` | `lettuce`   | Redis client type.     |

## Logging configuration

| Property                                | Value                                                          | Description                               |
|-----------------------------------------|----------------------------------------------------------------|-------------------------------------------|
| `logging.pattern.level`                 | `%5p [${spring.application.name:},%X{traceId:-},%X{spanId:-}]` | Logging pattern with tracing information. |
| `logging.level.root`                    | `INFO`                                                         | Root logging level.                       |
| `logging.level.org.springframework.web` | `INFO`                                                         | Logging level for Spring Web.             |

## Metrics configuration

| Property                                  | Value  | Description                            |
|-------------------------------------------|--------|----------------------------------------|
| `management.tracing.enabled`              | `true` | Enables distributed tracing.           |
| `management.tracing.sampling.probability` | `1.0`  | Probability for sampling tracing data. |

## Swagger configuration

| Property                       | Value           | Description                                                    |
|--------------------------------|-----------------|----------------------------------------------------------------|
| `springdoc.api-docs.path`      | `/v3/api-docs`  | Path for API documentation.                                    |
| `springdoc.api-docs.enabled`   | `true`          | Enable API documentation.                                      |
| `springdoc.swagger-ui.enabled` | `true`          | Enable Swagger UI.                                             |
| `springdoc.swagger-ui.url`     | `/openapi.yaml` | Defines the `openapi.yaml` location inside `resources/static`. |

## Pipeline configuration

| Property                          | Value | Description                                            |
|-----------------------------------|-------|--------------------------------------------------------|
| `pipeline.limit.per-day.per-user` | `5`   | Maximum number of pipelines a user can launch per day. |

# Build & run application

1. Build the application as mentioned in parent `README.md` file after making necessary application properties changes.
2. Make sure dependant services are up & running.
3. Run the application
   ```
   # Once you build the service, jar file generates inside target dir.
   java -jar target/{jar file}
   ```
   Example
   ```
   java -jar target/pipeline-manager-5.0.9-SNAPSHOT.jar
   ```
   Alternatively you can run the project via IDE e.g. IntelliJ IDEA.

   Ignore email server connection error! works on GCP.

4. Access Open API documentation
   ```
   # You will need access token. Log in using frontend application, generates Access token stored in DB.
   http://localhost:8020/pipeline-manager/webjars/swagger-ui/index.html
   ```
