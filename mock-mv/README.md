# Mock Manual Verification (MV)

## 📌 Overview
The `mock-mv` service simulates the Manual Verification (MV) component in MOSIP. It reads requests from a queue and provides mock responses for external "manual adjudication" and "verification" systems. This is useful for testing registration processor flows without connecting to actual manual verification systems.

## ✨ Features
-   **MV Simulation**: Simulates the decision-making process of Manual Verification (Approved/Rejected).
-   **Queue Integration**: Consumes verification requests from ActiveMQ and publishes responses.
-   **Configurable Expectations**: Allows setting up specific decisions (APPROVED/REJECTED) and response delays via API.
-   **Swagger Interface**: Provides a user-friendly interface to configure mock behavior.

## 🛠️ Services
-   **mock-mv-service**: The core service that processes verification requests and manages mock logic.

## ⚙️ Local Setup
There are two primary ways to deploy the `mock-mv` module locally:
1.  **[Local Setup using Docker Image](#local-setup-using-docker-image)**: Recommended for consistent environment setup.
2.  **[Local Setup by Building Docker Image](#local-setup-by-building-docker-image)**: If you need to build from source.
3.  **[Local Setup (JAR)](#local-setup-using-jar)**: Useful for development and debugging.

## 📋 Pre requisites
Before setting up the project, ensure you have the following prerequisites:
-   **Java 21**: The project requires Java 21.0.3 or later.
-   **Maven**: For building the project.
-   **Docker**: For containerized deployment.
-   **ActiveMQ**: A local or remote ActiveMQ instance is required for queue operations.
-   **Git**: To clone the repository.

## 🗄️ Database Setup
-   **Not Applicable**: This module does not use a standalone database for persistence; it primarily operates using in-memory configurations and message queues.

## 📝 Configurations
The service connects to ActiveMQ and other internal components.
-   **ActiveMQ Configuration**:
    You may need to configure ActiveMQ connection details in `application.properties` or via environment variables if not using default local settings.
-   **Mock Decision**:
    The default decision can be configured via API or properties (`mock.mv.decision`).

## 🐳 Local Setup using docker image
To run using an existing Docker image (if available in your registry):
1.  Run the following command:
    ```bash
    docker run -p 8081:8081 mock-mv
    ```
    *(Note: Ensure ActiveMQ is running and reachable).*

## 🏗️ Local Setup by building docker image
To build the image locally and run:
1.  **Build the Project**:
    ```bash
    mvn clean install -Dgpg.skip=true
    ```
2.  **Build Docker Image**:
    ```bash
    docker build --file Dockerfile --tag mock-mv .
    ```
3.  **Run Container**:
    ```bash
    docker run -p 8081:8081 mock-mv
    ```

## ☕ Local Setup using JAR
Recommended for active development.
1.  **Build**:
    ```bash
    mvn clean install -Dgpg.skip=true
    ```
2.  **Run**:
    ```bash
    java -XX:-UseG1GC -XX:-UseParallelGC -XX:-UseShenandoahGC -XX:+ExplicitGCInvokesConcurrent \
    -XX:+UseZGC -XX:+ZGenerational -XX:+UnlockExperimentalVMOptions -XX:+UseStringDeduplication \
    -XX:+HeapDumpOnOutOfMemoryError -XX:+UseCompressedOops -XX:MaxGCPauseMillis=200 \
    -Dfile.encoding=UTF-8 \
    -Dspring.cloud.config.label="master" \
    -Dspring.profiles.active="default"  \
    -Dspring.cloud.config.uri="http://localhost:51000/config" \
    --add-opens java.xml/jdk.xml.internal=ALL-UNNAMED \
    --add-opens java.base/java.lang.reflect=ALL-UNNAMED \
    --add-opens java.base/java.lang.stream=ALL-UNNAMED \
    --add-opens java.base/java.time=ALL-UNNAMED \
    --add-opens java.base/java.time.LocalDate=ALL-UNNAMED \
    --add-opens java.base/java.time.LocalDateTime=ALL-UNNAMED \
    --add-opens java.base/java.io.Reader=ALL-UNNAMED \
    --add-opens java.base/java.util.Optional=ALL-UNNAMED \
    --add-opens java.base/java.time.LocalDateTime.date=ALL-UNNAMED \
    -jar target/mock-mv-1.3.0-SNAPSHOT.jar
    ```
    *(Note: Adjust the JAR version and config URI as needed).*

## 🚀 Deployment
For deploying in a Kubernetes environment (like Sandbox), the service is deployed as a Docker container. Refer to the specific MOSIP deployment scripts and Helm charts for environmental configuration.

## ⬆️ Upgrade
To upgrade:
1.  Git pull latest changes.
2.  Update dependencies in `pom.xml`.
3.  Rebuild: `mvn clean install`.
4.  Rebuild Docker image.

## 📚 Documentation

### API Documentation
API endpoints, base URL (repo name), and mock server details are available via Swagger documentation:
[Swagger UI Local](http://localhost:8081/v1/mockmv/swagger-ui/index.html#/)

### Product Documentation
For more details on Manual Adjudication integration:
[MOSIP Documentation](https://docs.mosip.io/1.1.5/modules/registration-processor/deduplication-and-manual-adjudication#manual-adjudication)

## 🤝 Contribution & Community
We welcome contributions from everyone!

[Check here](https://docs.mosip.io/1.2.0/community/code-contributions) to learn how you can contribute code to this application.

If you have any questions or run into issues while trying out the application, feel free to post them in the [MOSIP Community](https://community.mosip.io/) — we’ll be happy to help you out.

[![GitHub Issues](https://img.shields.io/badge/GitHub-Issues-181717?style=flat&logo=github&logoColor=white)](https://github.com/mosip/mosip-mock-services/issues)

## 📝 License
![License: MPL 2.0](https://img.shields.io/badge/License-MPL_2.0-brightgreen.svg)

This project is licensed under the terms of [Mozilla Public License 2.0](LICENSE).
See the [LICENSE](LICENSE) file for full license details.
