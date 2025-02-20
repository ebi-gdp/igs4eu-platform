# Genetic Scoring Pipeline Presentation Layer (Backend Microservices)

## Overview
This project is a Java Spring Boot application managed with Maven. It consists of five modules, each responsible for a specific aspect of the system.

## Modules
For more information refer to `README.md` defied inside each module.
1. **File Handler** (file-handler)
    - Manages file transfers using Globus, Amazon S3, and Google Cloud Buckets.
    - Depends on/ interacts with
      ```
      1. Common Module (direct dependency).
      2. Interacts with User Manager (Checks for user identity & DPA).
      3. Globus Integration.
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
      7. Kakfka (Deployed on K8S). 
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

### Making Changes
1. Check out a new branch from GitHub:
   ```bash
   git checkout -b feature/<feature-branch>
   ```
2. Modify the necessary module(s).
3. Build the entire project to ensure consistency:
   ```bash
   mvn clean install
   ```

## Run application locally
Refer to individual application properties defined in a module, defined these properties in your maven settings.xml Once the project builds successfully, you can run the application locally & deploy on GCP.
Further instructions are defined in module's README.md.
```
1. PostgreSQL instance.
2. Kafka.
3. Connects to GCP secret manager. Needs service account key generated on GCP console. 
Export GOOGLE_APPLICATION_CREDENTIALS variable with referring to service account key file path.
4. Redis instance.
```
Please refer to google documentation [Application Default Credentials](https://cloud.google.com/docs/authentication/application-default-credentials). You can ignore email server connection error! works on GCP. 

### Releasing Code
Once the project builds successfully, prepare for release:
```bash
mvn -Darguments=-DskipTests release:clean release:prepare
```
Provide appropriate version number based on changes e.g. Major, minor & patch etc.

This command cleans up previous release data and prepares a new release while skipping tests.

***IMPORTANT***:
After releasing the project, check tag on [igs4eu-platform](https://github.com/ebi-gdp/igs4eu-platform/tags). 
You should see the tag you have just released. This repository is synced with Gitlab, navigate to Gitlab instance [igs4eu-platform-gitlab](https://gitlab.ebi.ac.uk/gdp/igs4eu-platform).
Further steps to build, containerize & deploy have been mentioned on [Confluence](https://www.ebi.ac.uk/seqdb/confluence/display/GDP/Genetic+Scoring+Platform+Deployment+Guide).

