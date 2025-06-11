# Key Handler Service

The Key Handler Service is responsible for Crypt4gh key management, including key generation, encryption, storage, and deletion.

# Key responsibilities

### Secure key storage
- **Encrypts private keys** using the **AES algorithm**.
- Stores **encrypted private keys** securely in **GCP Secret Manager**.

### Integration with crypt4gh & key pair generation
- Generates **private & public key pairs** using **crypt4gh library**.
- The **crypt4gh python library** is bundled as part of the **docker build**.
- The service invokes **crypt4gh operations** from within the Java Spring Boot application.

### Key deletion API
- Provides an API to **trigger private key deletion**, ensuring secure removal from **GCP Secret Manager**.

The **Key Handler Service** plays a crucial role in managing **secure key generation, encryption, and storage** for Crypt4gh-based encryption workflows.

# GCP running dependencies

To run the **Key Handler Service**, considering two options:
- **On GCP Kubernetes (GKE)**
- **Locally**

### GCP Secret Manager Access
This service requires access to **GCP Secret Manager** to store encrypted private keys.  
Authentication depends on the environment:

| Environment          | Authentication Method                                                 | Notes                                                                                                                                                |
|----------------------|-----------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------|
| GCP Kubernetes (GKE) | Kubernetes Service Account (KSA) bound to a GCP Service Account (GSA) | The GSA must have **permissions to store and retrieve secrets from GCP Secret Manager**.                                                             |
| Local Development    | Key file for GCP Service Account                                      | Set the path of the key file to the environment variable: <br> `GOOGLE_APPLICATION_CREDENTIALS=<path-to-key-file>` (Not recommended for production). |

Ensure these dependencies are correctly configured before running the **Key Handler Service**.

Refer to the configurations below. Properties with values starting with @ are placeholders that should be defined in Maven's `settings.xml` file.
`E.g. @crypt4gh.shell-path@`

Once you add config/properties inside `settings.xml`, build the project. Upon successful build you can run application locally.
Please check following dependant services are up & running.

# Application properties

## Application configuration `application.properties`

| Property                   | Value                | Description                                      |
|----------------------------|----------------------|--------------------------------------------------|
| `spring.application.name`  | `igs4eu-key-handler` | Defines the name of the Spring Boot application. |
| `spring.webflux.base-path` | `/key-handler`       | Sets the base path for WebFlux endpoints.        |
| `spring.profiles.active`   | `${ENV:dev}`         | Specifies the active Spring profile.             |

## Metrics configuration

| Property                                         | Value                            | Description                                     |
|--------------------------------------------------|----------------------------------|-------------------------------------------------|
| `management.endpoints.web.exposure.include`      | `health,metrics,info,prometheus` | Defines which management endpoints are exposed. |
| `management.observations.key-values.application` | `${spring.application.name}`     | Associates metrics with the application name.   |

## Application configuration `application-{env}.properties`

| Property      | Value  | Description                                   |
|---------------|--------|-----------------------------------------------|
| `server.port` | `8040` | Defines the port the application will run on. |

## GCP configuration

| Property                        | Value                   | Description                            |
|---------------------------------|-------------------------|----------------------------------------|
| `gcp.project-id`                | `{project-id}`          | Specifies the Google Cloud Project ID. |
| `gcp.secret.path-prefix`        | `{project-path-prefix}` | Path prefix for secrets in GCP.        |
| `gcp.region`                    | `{region}`              | Defines the GCP region.                |
| `gcp.secret-manager.config.ttl` | `2592000`               | Time-to-live for secrets in seconds.   |

## Crypt4gh configuration

| Property                        | Value                    | Description                                   |
|---------------------------------|--------------------------|-----------------------------------------------|
| `crypt4gh.shell-path`           | `@crypt4gh.shell-path@`  | Defines the shell used for Crypt4gh.          |
| `crypt4gh.binary-path`          | `@crypt4gh.binary-path@` | Path to the Crypt4gh binary.                  |
| `crypt4gh.keys.base-path`       | `@crypt4gh.binary-path@` | Base path for Crypt4gh keys.                  |
| `crypt4gh.private-key.password` | `${SEC_KEY_PASSWD}`      | Password for encrypting crypt4gh private key. |

## Spring security OAuth2 client registration

| Property                                                | Value                                     | Description                    |
|---------------------------------------------------------|-------------------------------------------|--------------------------------|
| `spring.security.oauth2.resourceserver.jwt.issuer-uri`  | `https://login.aai.lifescience-ri.eu/oidc/`    | Defines the OAuth2 issuer URI. |
| `spring.security.oauth2.resourceserver.jwt.jwk-set-uri` | `https://login.aai.lifescience-ri.eu/oidc/jwk` | Specifies the JWK set URI.     |

## Basic authentication

| Property              | Value                   | Description                        |
|-----------------------|-------------------------|------------------------------------|
| `basic.auth.username` | `@basic.auth.username@` | Username for basic authentication. |
| `basic.auth.password` | `@basic.auth.password@` | Password for basic authentication. |

## Internal service configuration

| Property                                  | Value                                                                 | Description                        |
|-------------------------------------------|-----------------------------------------------------------------------|------------------------------------|
| `intervene.user-manager.base-url`         | `http://user-manager.{namespace}.svc.cluster.local:8080/user-manager` | Base URL for user manager service. |
| `intervene.user-manager.user-account.uri` | `/user/account`                                                       | User account endpoint.             |

`{namespace}` will change according to env. for production, it can be `intervene-prod`. e.g.<br/> `http://user-manager.intervene-prod.svc.cluster.local:8080/user-manager`.

## Metrics configuration

| Property                                         | Value  | Description                              |
|--------------------------------------------------|--------|------------------------------------------|
| `management.tracing.enabled`                     | `true` | Enables tracing.                         |
| `management.tracing.sampling.probability`        | `1.0`  | Sets the sampling probability.           |
| `management.tracing.baggage.correlation.enabled` | `true` | Enables baggage correlation for tracing. |

## Reactor configuration

| Property                             | Value  | Description                            |
|--------------------------------------|--------|----------------------------------------|
| `spring.reactor.context-propagation` | `auto` | Enables automatic context propagation. |

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
   java -jar target/key-handler-5.0.9-SNAPSHOT.jar
   ```
   Alternatively you can run the project via IDE e.g. IntelliJ IDEA.
4. Access Open API documentation
   ```
   # You will need access token. Log in using frontend application, generates Access token stored in DB.
   http://localhost:8040/key-handler/webjars/swagger-ui/index.html
   ```
