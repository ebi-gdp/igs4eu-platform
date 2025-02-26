# User Manager Service
The User Manager service is responsible for handling platform user accounts and managing their association with Life Science AAI authentication.

# Key responsibilities

### Platform user account & AAI account mapping:
* When a user logs in via Life Science AAI, the service checks for an existing platform account, if account doesn't exist, the user is prompted to create one.

### Data processing agreement (DPA) management:
* Maintains user consent agreements. 
* Stores DPA PDF content in Base64-encoded format within the database.
* Allows users to give or revoke their consent as needed.

### Centralized user consent validation:
* All microservices must check this service for user consent before granting API access. Let users 

### User account retrieval:
* Other microservices query this service using the Access Token to retrieve user account details.

This service acts as the single source of truth for user consent management and account details across the platform.

# GCP running dependencies

To run the **User Manager Service**, considering two options:
- **On GCP Kubernetes (GKE)**
- **Locally**

### Database Access
This service requires access to a **database** for storing platform data.

| Environment          | Authentication Method                                                 | Notes                                                                                                                                                                                                     |
|----------------------|-----------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| GCP Kubernetes (GKE) | Kubernetes Service Account (KSA) bound to a GCP Service Account (GSA) | The GSA must have **permissions to connect to and operate the database**.                                                                                                                                 |
| Local Development    | Key file for GCP Service Account                                      | Set the path of the key file to the environment variable: <br> `GOOGLE_APPLICATION_CREDENTIALS=<path-to-key-file>` (Not recommended for production). <br/>You can download this key from GCP web console. |

Refer to the configurations below. Properties with values starting with @ are placeholders that should be defined in Maven's `settings.xml` file.
`E.g. @datasource.intervene.host@`

Once you add config/properties inside `settings.xml`, build the project. Upon successful build you can run application locally.
Please check following dependant services are up & running.

# Application properties

## Application configuration `application.properties`

| Property                   | Value                 | Description                                      |
|----------------------------|-----------------------|--------------------------------------------------|
| `spring.application.name`  | `igs4eu-user-manager` | Defines the name of the Spring Boot application. |
| `spring.webflux.base-path` | `/user-manager`       | Sets the base path for WebFlux endpoints.        |
| `spring.profiles.active`   | `${ENV:dev}`          | Specifies the active Spring profile.             |

## Metrics configuration

| Property                                         | Value                            | Description                                     |
|--------------------------------------------------|----------------------------------|-------------------------------------------------|
| `management.endpoints.web.exposure.include`      | `health,metrics,info,prometheus` | Defines which management endpoints are exposed. |
| `management.observations.key-values.application` | `${spring.application.name}`     | Associates metrics with the application name.   |

## Application configuration `application-{env}.properties`

| Property      | Value  | Description                                         |
|---------------|--------|-----------------------------------------------------|
| `server.port` | `8030` | Defines the port on which the application will run. |

## Datasource configuration

| Property                                           | Value                             | Description                                 |
|----------------------------------------------------|-----------------------------------|---------------------------------------------|
| `datasource.user-manager.host`                     | `@datasource.intervene.host@`     | Hostname for the database connection.       |
| `datasource.user-manager.username`                 | `@datasource.intervene.username@` | Database username.                          |
| `datasource.user-manager.password`                 | `@datasource.intervene.password@` | Database password.                          |
| `datasource.user-manager.port`                     | `5432`                            | Port for the PostgreSQL database.           |
| `datasource.user-manager.database`                 | `intervene`                       | Database name.                              |
| `datasource.user-manager.schema`                   | `geneticscores-dev`               | Database schema used for genetic scores.    |
| `datasource.user-manager.ssl-mode`                 | `disable`                         | SSL mode for database connection.           |
| `datasource.user-manager.driver-class-name`        | `org.postgresql.Driver`           | JDBC driver for PostgreSQL.                 |
| `datasource.user-manager.hikari.maximum-pool-size` | `3`                               | Maximum connections in the connection pool. |
| `datasource.user-manager.hikari.minimum-idle`      | `1`                               | Minimum number of idle connections.         |
| `datasource.user-manager.initialization-mode`      | `embedded`                        | Database initialization mode.               |

## OIDC configuration

| Property             | Value       | Description                                    |
|----------------------|-------------|------------------------------------------------|
| `oidc.user-info.uri` | `/userinfo` | Endpoint for retrieving OIDC user information. |

## Spring security OAuth2 configuration

| Property                                                | Value                                     | Description                         |
|---------------------------------------------------------|-------------------------------------------|-------------------------------------|
| `spring.security.oauth2.resourceserver.jwt.issuer-uri`  | `https://login.elixir-czech.org/oidc/`    | Issuer URI for JWT authentication.  |
| `spring.security.oauth2.resourceserver.jwt.jwk-set-uri` | `https://login.elixir-czech.org/oidc/jwk` | JWK set URI for JWT authentication. |

## Elixir OIDC configuration

| Property                                 | Value                                  | Description                              |
|------------------------------------------|----------------------------------------|------------------------------------------|
| `elixir.oidc.url`                        | `https://login.elixir-czech.org/oidc/` | Base URL for Elixir OIDC authentication. |
| `elixir.oidc.request.connection.timeout` | `30`                                   | Connection timeout for OIDC requests.    |
| `elixir.oidc.request.read.timeout`       | `30`                                   | Read timeout for OIDC requests.          |

## Logging configuration

| Property                                             | Value                                                          | Description                                 |
|------------------------------------------------------|----------------------------------------------------------------|---------------------------------------------|
| `logging.pattern.level`                              | `%5p [${spring.application.name:},%X{traceId:-},%X{spanId:-}]` | Logging pattern with tracing information.   |
| `logging.level.root`                                 | `INFO`                                                         | Root logging level.                         |
| `logging.level.uk.ac.ebi.gdp.intervene.user.manager` | `INFO`                                                         | Logging level for the User Manager service. |
| `logging.level.org.springframework.web`              | `INFO`                                                         | Logging level for Spring Web.               |
| `logging.level.org.hibernate`                        | `INFO`                                                         | Logging level for Hibernate.                |
| `logging.level.org.springframework.security`         | `INFO`                                                         | Logging level for Spring Security.          |

## Metrics configuration

| Property                                         | Value  | Description                              |
|--------------------------------------------------|--------|------------------------------------------|
| `management.tracing.enabled`                     | `true` | Enables distributed tracing.             |
| `management.tracing.sampling.probability`        | `1.0`  | Probability for sampling tracing data.   |
| `management.tracing.baggage.correlation.enabled` | `true` | Enables baggage correlation for tracing. |

## Basic authentication configuration

| Property              | Value                   | Description                        |
|-----------------------|-------------------------|------------------------------------|
| `basic.auth.username` | `@basic.auth.username@` | Username for basic authentication. |
| `basic.auth.password` | `@basic.auth.password@` | Password for basic authentication. |

## Reactor configuration

| Property                             | Value  | Description                                       |
|--------------------------------------|--------|---------------------------------------------------|
| `spring.reactor.context-propagation` | `auto` | Enables automatic context propagation in Reactor. |

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
   java -jar target/user-manager-5.0.9-SNAPSHOT.jar
   ```
   Alternatively you can run the project via IDE e.g. IntelliJ IDEA.
4. Access Open API documentation
   ```
   # You will need access token. Log in using frontend application, generates Access token stored in DB.
   http://localhost:8030/user-manager/webjars/swagger-ui/index.html
   ```
