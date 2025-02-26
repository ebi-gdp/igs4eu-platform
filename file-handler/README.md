# File Handler Service

The **File Handler Service** manages all **file operations** for the **genetic scoring platform**, including **Globus file service operations**, user account validation, cloud storage interactions, and automated file cleanup.

# Key responsibilities

### Globus file management
- **Creates directories** on the **Guest Collection**.
- **Assigns permissions** to user directories for controlled access.

### User globus account management
- **Manages user’s Globus account** within the platform.
- **Validates** if the provided **email ID** maps to a **valid Globus account**.

### Cloud storage operations
- **Connects to GCP buckets** to:
   - **List** risk score result files.
   - **Stream** polygenic risk score (PRS) result files on demand.

### Directory deletion & cleanup
- Provides **APIs to delete directories** from **Guest Collection**.
- Deletion is triggered:
   - **By users** when they **delete datasets** from the frontend.
   - **By Cron Jobs** as part of the **expired dataset deletion process**.

The **File Handler Service** ensures **secure, efficient, and automated** file management across **Globus** and **GCP storage**, enabling seamless **file access, cleanup, and validation workflows**.

# GCP running dependencies

To run the File Handler Service, considering two options:
- **On GCP Kubernetes (GKE)**
- **Locally**

### GCP buckets access
This service requires access to **GCP Buckets** for streaming & accessing files.  
Depending on the environment, authentication is handled differently:

| Environment          | Authentication Method                                                 | Notes                                                                                                                                                |
|----------------------|-----------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------|
| GCP Kubernetes (GKE) | Kubernetes Service Account (KSA) bound to a GCP Service Account (GSA) | The GSA must have **permissions to access GCP Buckets**.                                                                                             |
| Local Development    | Key file for GCP Service Account                                      | Set the path of the key file to the environment variable: <br> `GOOGLE_APPLICATION_CREDENTIALS=<path-to-key-file>` (Not recommended for production). |

### Globus API credentials
- **Globus Client App Credentials** must be correctly populated.
- This service interacts with **Globus APIs** for file operations and user account validation.

Ensure these dependencies are correctly configured before running the **File Handler Service**.

Refer to the configurations below. Properties with values starting with @ are placeholders that should be defined in Maven's `settings.xml` file.
`E.g. @globus.aai.client-id@`

Once you add config/properties inside `settings.xml`, build the project. Upon successful build you can run application locally.
Please check following dependant services are up & running.

# Application properties

## Application configuration - `application.properties`
| Property                   | Value                 | Description                                      |
|----------------------------|-----------------------|--------------------------------------------------|
| `spring.application.name`  | `igs4eu-file-handler` | Defines the name of the Spring Boot application. |
| `spring.webflux.base-path` | `/file-handler`       | Sets the base path for WebFlux endpoints.        |
| `spring.profiles.active`   | `${ENV:dev}`          | Specifies the active Spring profile.             |

## Metrics configuration
| Property                                         | Value                            | Description                                     |
|--------------------------------------------------|----------------------------------|-------------------------------------------------|
| `management.endpoints.web.exposure.include`      | `health,metrics,info,prometheus` | Defines which management endpoints are exposed. |
| `management.observations.key-values.application` | `${spring.application.name}`     | Associates metrics with the application name.   |

## Application configuration - `application-{dev}.properties`
| Property      | Value  | Description                             |
|---------------|--------|-----------------------------------------|
| `server.port` | `8010` | The port on which the application runs. |

## Apache HttpClient connection configuration
| Property                                  | Value   | Description                                       |
|-------------------------------------------|---------|---------------------------------------------------|
| `webclient.connection.pipe-size`          | `4096`  | Pipe buffer size for the HTTP client.             |
| `webclient.connection.connection-timeout` | `5`     | Timeout in seconds for establishing a connection. |
| `webclient.connection.socket-timeout`     | `0`     | Socket timeout (0 means infinite).                |
| `webclient.connection.read-write-timeout` | `30000` | Read/write timeout in milliseconds.               |

## Download configuration
| Property                                 | Value    | Description                                           |
|------------------------------------------|----------|-------------------------------------------------------|
| `file.download.buffer-size`              | `4096`   | Buffer size for file downloads.                       |
| `file.download.parallel.connections.max` | `3`      | Maximum number of parallel connections for downloads. |
| `file.download.parallel.file-size`       | `200000` | Maximum file size for parallel downloads.             |

## File download retry configuration
| Property                                       | Value   | Description                                              |
|------------------------------------------------|---------|----------------------------------------------------------|
| `file.download.retry.strategy`                 | `FIXED` | Retry strategy (EXPONENTIAL/FIXED).                      |
| `file.download.retry.attempts.max`             | `3`     | Maximum retry attempts.                                  |
| `file.download.retry.attempts.delay`           | `1000`  | Delay between retries in milliseconds (for exponential). |
| `file.download.retry.attempts.maxDelay`        | `30000` | Maximum delay in exponential retries.                    |
| `file.download.retry.attempts.multiplier`      | `2`     | Multiplier for exponential backoff.                      |
| `file.download.retry.attempts.back-off-period` | `2000`  | Fixed backoff period in milliseconds.                    |

## OAuth credentials
| Property                      | Value                                                                    | Description                      |
|-------------------------------|--------------------------------------------------------------------------|----------------------------------|
| `ega.aai.access-token.uri`    | `https://ega.ebi.ac.uk:8443/ega-openid-connect-server/token`             | Access token URI for EGA AAI.    |
| `ega.aai.client-id`           | `@ega.aai.client-id@`                                                    | Client ID for EGA AAI.           |
| `ega.aai.client-secret`       | `@ega.aai.client-secret@`                                                | Client secret for EGA AAI.       |
| `ega.aai.scopes`              | `openid`                                                                 | OAuth scopes.                    |
| `ega.aai.username`            | `@ega.aai.username@`                                                     | Username for EGA AAI.            |
| `ega.aai.password`            | `@ega.aai.password@`                                                     | Password for EGA AAI.            |
| `globus.aai.access-token.uri` | `https://auth.globus.org/v2/oauth2/token`                                | Access token URI for Globus AAI. |
| `globus.aai.client-id`        | `@globus.aai.client-id@`                                                 | Client ID for Globus AAI.        |
| `globus.aai.client-secret`    | `@globus.aai.client-secret@`                                             | Client secret for Globus AAI.    |
| `globus.aai.scopes`           | `openid,email,profile,urn:globus:auth:scope:transfer.api.globus.org:all` | OAuth scopes for Globus AAI.     |

## Basic authentication
| Property              | Value                   | Description          |
|-----------------------|-------------------------|----------------------|
| `basic.auth.username` | `@basic.auth.username@` | Basic auth username. |
| `basic.auth.password` | `@basic.auth.password@` | Basic auth password. |

## EGA configuration
| Property                | Value                        | Description                 |
|-------------------------|------------------------------|-----------------------------|
| `ega.data-api.url`      | `https://ega.ebi.ac.uk:8052` | EGA Data API URL.           |
| `ega.file.storage.path` | `/data/gdp/ega/file/`        | Storage path for EGA files. |

## Globus configuration
| Property                                  | Value                                                                | Description                         |
|-------------------------------------------|----------------------------------------------------------------------|-------------------------------------|
| `globus.managed-collection.endpoint-id`   | `@globus.managed-collection.endpoint-id@`                            | Managed collection endpoint ID.     |
| `globus.guest-collection.endpoint-id`     | `@globus.guest-collection.endpoint-id@`                              | Guest collection endpoint ID.       |
| `globus.guest-collection.home-path`       | `/~/upload/intervene-igs4eu-platform-dev-env`                        | Home path for guest collection.     |
| `globus.endpoint.mkdir.uri`               | `/operation/endpoint/${globus.managed-collection.endpoint-id}/mkdir` | URI for making directories.         |
| `globus.endpoint.access.uri`              | `/endpoint/${globus.guest-collection.endpoint-id}/access`            | URI for accessing endpoints.        |
| `globus.endpoint.list-files.uri`          | `/operation/endpoint/${globus.managed-collection.endpoint-id}/ls`    | URI for listing files.              |
| `globus.endpoint.list-files.limit`        | `10`                                                                 | Limit for listing files.            |
| `globus.endpoint.submission-id.uri`       | `/submission_id`                                                     | Submission ID URI.                  |
| `globus.endpoint.delete-dir.uri`          | `/delete`                                                            | URI for deleting directories.       |
| `globus.shared-endpoint.display-name`     | `ebi#gdp#intervene#`                                                 | Display name for shared endpoint.   |
| `globus.shared-endpoint.description`      | `${globus.shared-endpoint.display-name}`                             | Description of the shared endpoint. |
| `globus.shared-endpoint.owner-string`     | `IGS4EU Platform`                                                    | Owner string for shared endpoint.   |
| `globus.shared-endpoint.contact-email`    | `username@ebi.ac.uk`                                                 | Contact email for shared endpoint.  |
| `globus.shared-endpoint.organization`     | `EBI`                                                                | Organization name.                  |
| `globus.shared-endpoint.force-encryption` | `true`                                                               | Whether to force encryption.        |
| `globus.data-api.url`                     | `https://transfer.api.globusonline.org/v0.10`                        | Globus Data API URL.                |
| `globus.auth-api.url`                     | `https://auth.globus.org/v2/api`                                     | Globus Auth API URL.                |
| `globus.auth-api.credentials`             | `${GLOBUS_AUTH_API_CREDENTIALS}`                                     | Globus authentication credentials.  |

## Platform configuration
| Property                      | Value | Description         |
|-------------------------------|-------|---------------------|
| `pipeline-execution.platform` | `GCP` | Execution platform. |

## Cloud storage configuration
| Property               | Value                    | Description                                       |
|------------------------|--------------------------|---------------------------------------------------|
| `cloud.gcp.project-id` | `@cloud.gcp.project-id@` | Google Cloud project ID. Change according to env. |

## Spring security OAuth2 client registration
| Property                                                | Value                                     | Description     |
|---------------------------------------------------------|-------------------------------------------|-----------------|
| `spring.security.oauth2.resourceserver.jwt.issuer-uri`  | `https://login.elixir-czech.org/oidc/`    | JWT issuer URI. |
| `spring.security.oauth2.resourceserver.jwt.jwk-set-uri` | `https://login.elixir-czech.org/oidc/jwk` | JWK set URI.    |

## Logging configuration
| Property                                             | Value                                                          | Description                               |
|------------------------------------------------------|----------------------------------------------------------------|-------------------------------------------|
| `logging.pattern.level`                              | `%5p [${spring.application.name:},%X{traceId:-},%X{spanId:-}]` | Logging pattern.                          |
| `logging.level.root`                                 | `INFO`                                                         | Root logging level.                       |
| `logging.level.uk.ac.ebi.gdp.intervene.file.handler` | `INFO`                                                         | Log level for File Handler.               |
| `logging.level.org.apache.http`                      | `TRACE`                                                        | Log level for Apache HTTP.                |
| `logging.level.httpclient.wire`                      | `ERROR`                                                        | Log level for HTTP client wire.           |
| `logging.level.org.apache.commons.httpclient`        | `ERROR`                                                        | Log level for Apache Commons Http Client. |
| `logging.level.org.springframework.web`              | `INFO`                                                         | Log level for Spring Web.                 |
| `logging.level.reactor.netty.http.client`            | `INFO`                                                         | Log level for Netty Http Client.          |

## Metrics configuration
| Property                                         | Value  | Description                 |
|--------------------------------------------------|--------|-----------------------------|
| `management.tracing.enabled`                     | `true` | Enable tracing.             |
| `management.tracing.sampling.probability`        | `1.0`  | Tracing sample probability. |
| `management.tracing.baggage.correlation.enabled` | `true` | Enable baggage correlation. |

## Reactor configuration
| Property                             | Value  | Description               |
|--------------------------------------|--------|---------------------------|
| `spring.reactor.context-propagation` | `auto` | Context propagation mode. |

## Internal service configuration
| Property                                  | Value                                                                 | Description                        |
|-------------------------------------------|-----------------------------------------------------------------------|------------------------------------|
| `intervene.user-manager.base-url`         | `http://user-manager.{namespace}.svc.cluster.local:8080/user-manager` | Base URL for user manager service. |
| `intervene.user-manager.user-account.uri` | `/user/account`                                                       | User account URI.                  |

`{namespace}` will change according to env. for production, it can be `intervene-prod`. e.g.<br/> `http://user-manager.intervene-prod.svc.cluster.local:8080/user-manager`.

## Swagger configuration

| Property                       | Value           | Description                                                    |
|--------------------------------|-----------------|----------------------------------------------------------------|
| `springdoc.api-docs.path`      | `/v3/api-docs`  | Path for API documentation.                                    |
| `springdoc.api-docs.enabled`   | `true`          | Enable API documentation.                                      |
| `springdoc.swagger-ui.enabled` | `true`          | Enable Swagger UI.                                             |
| `springdoc.swagger-ui.url`     | `/openapi.yaml` | Defines the `openapi.yaml` location inside `resources/static`. |

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
   java -jar target/file-handler-5.0.9-SNAPSHOT.jar
   ```
   Alternatively you can run the project via IDE e.g. IntelliJ IDEA.
4. Access Open API documentation
   ```
   # You will need access token. Log in using frontend application, generates Access token stored in DB.
   http://localhost:8010/file-handler/webjars/swagger-ui/index.html
   ```
