# Genetic Scoring Pipeline Backend Microservices (Presentation Layer)

## Overview
This project is a Java Spring Boot application managed with Maven. It consists of five modules, each responsible for a specific aspect of the system.
Look for `application.properties` & `application-{env}.properties` for each microservice. There are total 3 environments dev, test & prod.

These microservices handle Genetic Scoring Platform's backend operations. Divided into diff. microservices to support whole functionality.
Services communicate internally via `WebClient`.

You can check out this project & import to preferred IDE e.g. IntelliJ IDEA.

## Modules
For more information refer to `README.md` defied inside each module.
1. **File Handler** (file-handler)
    - Manages file transfers using Globus, Amazon S3, and Google Cloud Buckets.
    - Depends on/ interacts with
      ```
      1. Common Module (direct dependency).
      2. Interacts with User Manager service (Checks for user identity & DPA).
      3. Interacts with Globus APIs.
      4. Google Buckets Integration.
      ``` 

2. **Key Handler** (key-handler)
    - Handles the generation of Crypt4GH keys.
    - Encrypts and securely stores keys in Google Secret Manager.
    - Depends on/ interacts with
      ```
      1. Common Module (direct dependency).
      2. Interacts with User Manager (Checks for user identity & DPA).
      3. Google Secret Manager Integration.
      4. Crypt4gh Python Library Integration (Docker file).
      ```

3. **Pipeline Manager** (pipeline-manager)
    - Manages dataset creation.
    - Handles pipeline submissions.
    - Email notifications.
    - Result downloads.
    - Depends on/ interacts with
      ```
      1. Common Module (direct dependency).
      2. Interacts with User Manager (Checks for user identity & DPA).
      3. Interacts with Key Handler.
      4. Interacts with File Handler.
      5. PostgreSQL (GCP SaaS).
      6. Redis (Deployed on K8S).
      7. Kafka (Deployed on K8S). 
      8. Email server (geneticscores.org).
      ```

4. **User Manager** (user-manager)
    - Manages user account/identity.
    - Handles DPA consent.
    - Depends on/ interacts with
      ```
      1. Common Module (direct dependency).
      2. PostgreSQL (GCP SaaS).
      ```

5. **Common Module** (intervene-commons)
    - Provides shared libraries and common code for the above modules.

### All services are secured by OAuth2 - Spring Security; interacts with AAI server to validate access token. Currently integrates with Life Science AAI.

## Services intercommunication

Inter-microservice communication take place via `WebClient`, Access Token gets passed/propagated to the service(s) being called.

| Service            | Interacts with | Purpose                                                                        |
|--------------------|----------------|--------------------------------------------------------------------------------|
| `File Handler`     | `User Manager` | Checks for `DPA` consent.                                                      |
| `Pipeline Manager` | `User Manager` | Checks for `DPA` consent.                                                      |
|                    | `File Handler` | 1. Globus File Operations<br/> 2. Streaming result files from GCP buckets.     |
|                    | `Key Handler`  | Crypt4gh key generation & storing encrypted secret keys on GCP secret manager. |
| `Key Handler`      | `User Manager` | Checks for `DPA` consent.                                                      |

## Prerequisites

Before you can build and run this project, ensure that the following tools/softwares are installed on your system:

### 1. **Java Development Kit (JDK)**
- **Version**: Java 19 or later (Tested on version 19).
- **Installation**:
   - [Download JDK from Oracle](https://www.oracle.com/java/technologies/javase-downloads.html) or [OpenJDK](https://openjdk.java.net/).
   - Set `JAVA_HOME` environment variable to java installation path. You can check whether it has already been set.
     ```
     # Unix system
     echo $JAVA_HOME
     
     # Windows system
     echo %JAVA_HOME%
     ```
     if it is empty, then follow below steps according to your system. This is shell/commandline local variable.
     ```
     # Unix system
     export JAVA_HOME=/path/to/jdk/installation
     
     # Windows system
     set JAVA_HOME=/path/to/jdk/installation
     ```
     In case you want more details, you can follow [Set JAVA_HOME Variable](https://www.baeldung.com/java-home-on-windows-mac-os-x-linux).
- **Verify installation**:
  ```bash
  java -version
  ```

### 2. **Apache Maven** [optional]
(This is an optional step, you can use maven wrapper included in this project, refer to [Build the project](#how-to-build) section)
- **Version**: Maven 3.9.1 or later (Tested on version 3.9.1, you can try lower version if it works).
- **Installation**:
   - [Download Apache Maven](https://maven.apache.org/download.cgi).
   - Follow the installation steps and set the `M2_HOME` and `JAVA_HOME` environment variables if not set.
- **Verify installation**:
  ```bash
  mvn -v
  ```

### 3. **Git**
- **Version**: git 2.39 or later (Tested on version 2.39.5, you can try lower version if it works).
- **Installation**:
   - [Download Git](https://git-scm.com/downloads).
   - Follow the installation steps.
- **Verify installation**:
  ```bash
  git -v
  ```
---

## How to build?

Follow these steps to build the project:

1. **Clone the repository**:
   If you haven't already, clone the project repository to your local machine.
   ```bash
   # Clone git repo.
   git clone https://github.com/ebi-gdp/igs4eu-platform.git
   
   # Get into cloned directory.
   cd igs4eu-platform
   ```

2. **Build the project**: [2 options, either can be used]
   1. Use Maven bundled in this project to compile and package the project (preferred).
      ```bash
      # Unix system
      ./mvnw clean install
      
      # Windows system
      mvnw.cmd clean install
      ``` 
      OR
   2. Use Maven installed on your system to compile and package the project.
      ```bash
      mvn clean install
      ```
      This command will download necessary dependencies, compile the source code, and generate a `.jar` file in the `target/` directory. 
      
      You can also build individual modules, navigate to respective module directory & run the maven build command
      ```bash
      # Follow same process for all remaining modules except intervene-commons, it's better to build whole project as mentioned above in case changes in intervene-commons
      cd file-handler
      mvn clean package
      ``` 
## Development Workflow

### Making changes
1. Check out a new branch from GitHub
   ```bash
   git checkout -b <feature-branch>
   ```
2. Make necessary changes in the module(s)/project.
3. Build the project to ensure changes are complied!
   ```bash
   mvn clean package
   ```
4. Commit the changes if you are okay with it! You can follow `Git` best practices to commit changes.
5. Push the branch e.g.
   ```bash
   git push origin <feature-branch>
   ```
6. In case of code changes, `gitlab` pipeline is expected to trigger. Once gitlab pipeline executes successfully for a branch, raise a pull request. If pull request doesn't have any conflicts, you are good to merge branch with `main` branch.
7. After merging changes to `main` branch you can release the project. Deployment steps have been mentioned under `Deployments` section below.

## Run application locally
Refer to individual application properties defined in a module, defined these properties in your maven `settings.xml` Once the project builds successfully, you can run the application locally & deploy on GCP.
Further instructions are defined in each module's `README.md` file under `Build & run application` section. Needs service account key generated on GCP console.
Export `GOOGLE_APPLICATION_CREDENTIALS` variable with referring to service account key file path. This key file is required in order to access GCP services locally.
Refer to google documentation [Application Default Credentials](https://cloud.google.com/docs/authentication/application-default-credentials).

Make sure all modules are built properly with appropriate properties. Run all 4 services, by default it should run on their assigned ports.
Check whether all services are running. Open API has been integrated & same can be accessed as below for each service
```
http://localhost:8010/file-handler/webjars/swagger-ui/index.html

http://localhost:8020/pipeline-manager/webjars/swagger-ui/index.html

http://localhost:8030/user-manager/webjars/swagger-ui/index.html

http://localhost:8040/key-handler/webjars/swagger-ui/index.html
```

### Releasing Code
Once the project builds successfully, prepare for release, you can use maven wrapper as shown below or maven installed on your system.
```bash
./mvnw -Darguments=-DskipTests release:clean release:prepare
```
Provide appropriate version number based on changes e.g. Major, minor & patch etc.

This command cleans up previous release data and prepares a new release while skipping tests.

***IMPORTANT***:
After releasing the project, check tag on [igs4eu-platform](https://github.com/ebi-gdp/igs4eu-platform/tags). 
You should see the tag you have just released. This repository is synced with Gitlab, navigate to Gitlab instance [igs4eu-platform-gitlab](https://gitlab.ebi.ac.uk/gdp/igs4eu-platform).

### Deployments - 2 options
#### 1. Gitlab CI/CD
Gitlab setup detailed document can be found at [Deploy using Gitlab](https://www.ebi.ac.uk/seqdb/confluence/x/c5NEE)

#### 2. Helm charts without CI/CD
Microservices can be deployed in 2 ways via CI/CD using `gitlab` or individually using `helm` charts; `gitlab` also uses `helm` charts. Use `gitlab` for deployments, it's preferred way.
Use direct `helm` command in case only individual services are to be deployed e.g. for testing code on deployment. `helm` charts scripts are defined under `deployments` directory for each module.

Helm chart structure
```
deployments
  |- files
     |- application-gcp-{env}-env.properties
  |- templates
     |- configmap.yaml
     |- gcp-deployment.yaml
  |- Chart.yaml
  |- values-gcp-{env}-env.yaml
```
Example
```
deployments
  |- files
     |- application-gcp-dev-env.properties
     |- application-gcp-test-env.properties
     |- application-gcp-prod-env.properties
  |- templates
     |- configmap.yaml
     |- gcp-deployment.yaml
  |- Chart.yaml
  |- values-gcp-dev-env.yaml
  |- values-gcp-test-env.yaml
  |- values-gcp-prod-env.yaml 
```
This is standard helm format, in order to deploy services, it is important to look for `files` & `values` files. Whenever there are any property file changes
make sure you update property files. In case change differs according env. then make those changes inside values files. Deployment file refers values files according to env.
Also when you release underlying application's new version, you can update `appVersion` inside `Chart.yaml`.

You can deploy individual services directly using `helm` charts. Sometimes after individual deployments, dependant services requires restart, if you face connection issues with other services then restart calling & dependant pods. 
Run these commands from project's home directory. Before you run these command make sure you have updated `helm` files e.g. `application.properties`. Code should have been released with latest version & `docker` image has been created for the services.
`image.tag` sets `docker` image tag, you can use temporary tag in case want to test deployment OR use actual released tag for deployment.

1. Build docker image without CI/CD
   Build the project as mentioned [How to build](#How-to-build) & generate jar file for service & then run the following
   ```
   # File Handler
   docker build -f docker/Dockerfile -t dockerhub.ebi.ac.uk/gdp/igs4eu-platform:file-handler-1.0.0-dev --build-arg MODULE_NAME=file-handler --build-arg TARGET_PLATFORM=linux/amd64 .
   
   # Pipeline Manager
   docker build -f docker/Dockerfile -t dockerhub.ebi.ac.uk/gdp/igs4eu-platform:pipeline-manager-1.0.0-dev --build-arg MODULE_NAME=pipeline-manager --build-arg TARGET_PLATFORM=linux/amd64 .
   
   # User Manager
   docker build -f docker/Dockerfile -t dockerhub.ebi.ac.uk/gdp/igs4eu-platform:user-manager-1.0.0-dev --build-arg MODULE_NAME=user-manager --build-arg TARGET_PLATFORM=linux/amd64 .
   ```
   
   Docker image for `key-handler` service is diff. because of additional dependencies, follow this command
   ```
   docker build -f docker/key-handler/Dockerfile -t dockerhub.ebi.ac.uk/gdp/igs4eu-platform:key-handler-1.0.0-dev --build-arg MODULE_NAME=key-handler --build-arg TARGET_PLATFORM=linux/amd64 .
   ```
2. Push docker image(s) to gitlab container registry, make sure you have access to gitlab project.
   ```
   docker push dockerhub.ebi.ac.uk/gdp/igs4eu-platform:file-handler-1.0.0-dev
   
   docker push dockerhub.ebi.ac.uk/gdp/igs4eu-platform:pipeline-manager-1.0.0-dev
   
   docker push dockerhub.ebi.ac.uk/gdp/igs4eu-platform:user-manager-1.0.0-dev
   
   docker push dockerhub.ebi.ac.uk/gdp/igs4eu-platform:key-handler-1.0.0-dev
   ```
3. Run helm command to deploy service(s). Run only those commands which are needed i.e. only those services which needs to be deployed!
   ```
   helm upgrade --install {module-name} ./{module-name}/deployments -f ./{module-name}/deployments/values-gcp-{env}-env.yaml \
   --namespace {namespace} \
   --set image.tag={image-released-tag}
   ```   
   Select appropriate `module-name`, `namespace`, `values-file` & `GitHub` released tag.

   Examples:
   ```
   helm upgrade --install file-handler ./file-handler/deployments -f ./file-handler/deployments/values-gcp-dev-env.yaml \
   --namespace intervene-dev \
   --set image.tag=1.0.0
   ```
   
   ```
   helm upgrade --install key-handler ./key-handler/deployments -f ./key-handler/deployments/values-gcp-dev-env.yaml \
   --namespace intervene-dev \
   --set image.tag=1.0.0
   ```

   ```
   helm upgrade --install pipeline-manager ./pipeline-manager/deployments -f ./pipeline-manager/deployments/values-gcp-dev-env.yaml \
   --namespace intervene-dev \
   --set image.tag=1.0.0
   ```

   ```
   helm upgrade --install user-manager ./user-manager/deployments -f ./user-manager/deployments/values-gcp-dev-env.yaml \
   --namespace intervene-dev \
   --set image.tag=1.0.0
   ```

Check deployment status on kubernetes cluster. e.g. 
   ```
   # Change namespace according to env.
   kubectl get pods -n intervene-dev
   ```

You can delete release by running following command
```
# command
helm delete {release-name}

# example
helm delete user-manager
```

### Important NOTE
This is important, in case if you don't deploy all the services together, sometimes pods are unable to communicate with each other via service endpoints,
restart the pods by deleting exiting pods e.g.
```
kubectl -n intervene-dev delete po {pod-name}
```
