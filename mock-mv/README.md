# Mock Manual Verification (MV)

## 📌 Overview
The `mock-mv` service reads request from queue and mocks response for external "manual adjudication" and "verification" systems.

## ✨ Features
-   **MV Simulation**: Simulates the decision-making process of Manual Verification (Approved/Rejected).
-   **Queue Integration**: Consumes verification requests from ActiveMQ and publishes responses.
-   **Configurable Expectations**: Allows setting up specific decisions (APPROVED/REJECTED) and response delays via API.
-   **Swagger Interface**: Provides a user-friendly interface to configure mock behavior.

## 🛠️ Services
-   **mock-mv-service**: The core service that processes verification requests and manages mock logic.

## ⚙️ Local Setup
There are two primary ways to deploy the `mock-mv` module locally:
1.  **[Server Deployment using Docker Image](#-server-deployment-for-sandbox-deployment)**: Recommended for consistent environment setup.
2.  **[Local Setup (JAR)](#-local-setup-using-jar)**: Useful for development and debugging.

## 📋 Pre requisites
Before setting up the project, ensure you have the following prerequisites:
-   **Java**: Version 21 
-   **Maven**: 3.9.x 
-   **Git**: To clone the repository
-   **Postman (optional)**: For testing the APIs
-   **Docker**: For containerized deployment.
-   **ActiveMQ**: A local or remote ActiveMQ instance is required for queue operations.

## 🗄️ Database Setup
-   **Not Applicable**: This module does not use a standalone database for persistence; it primarily operates using in-memory configurations and message queues.

## 📝 Configurations
This section is for the developers, for developing this modules fast & efficiently.

1.  The configuration `mock.mv.decision` can be changed to "APPROVED" or "REJECTED" to set expected response.
2.  ActiveMQ queue need to setup using `application.properties`.

## 🐳 Server deployment (for sandbox deployment)
### Docker based

Steps:
1.  **Go to Project Root**: Navigate to `mock-mv`.
2.  **Build the code**:
    ```bash
    mvn clean install -Dgpg.skip=true
    ```
3.  **Create a docker image**:
    ```bash
    docker build . --file Dockerfile --tag mock-mv
    ```
4.  **Push the docker image**:
    Push the image to your docker registry. You can also directly use these images for running mock Manual Verification.
    ```bash
    docker push <your-registry>/mock-mv:latest
    ```

**Note**: Please check `Dockerfile` for passing env properties.

**Swagger URL**: `https://<hostname>/v1/mockmv/swagger-ui.html#/`

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
For deploying in a Kubernetes environment (like Sandbox), the service is deployed as a Docker container. Refer to [Sandbox Deployment](https://docs.mosip.io/1.2.0/deploymentnew/v3-installation).

## ⬆️ Upgrade
To upgrade:
1.  Git pull latest changes.
2.  Update dependencies in `pom.xml`.
3.  Rebuild: `mvn clean install`.
4.  Rebuild Docker image.

## 📚 Documentation

### APIs for configuration and expectation setting

#### Update configuration
**URL**: `http://{host}/v1/mockmv/config/configureMockMv`
**Method**: `POST`

**Request**:
```json
{
  "mockMvDescision": "APPROVED"
}
```

**Response**:
```text
Successfully updated the configuration
```

#### Get configuration
**URL**: `http://{host}/v1/mockmv/config/configureMockMv`
**Method**: `GET`

**Response**:
```json
{
  "mockMvDescision": "APPROVED"
}
```

#### Set Expectation
**URL**: `http://{host}/v1/mockmv/config/expectationMockMv`
**Method**: `POST`

**Request**:
```json
{
  "mockMvDecision": "REJECTED",
  "delayResponse": 30,
  "rid": "10332103161016320241119230824"
}
```

**Response**:
```text
Successfully inserted expectation $expectation_id
```

#### Get Expectations
**URL**: `http://{host}/v1/mockmv/config/expectationMockMv`
**Method**: `GET`

**Response**:
```json
{
  "10332103161016320241119230824": {
    "mockMvDecision": "REJECTED",
    "delayResponse": 0,
    "rid": "10332103161016320241119230824"
  },
  "10332103161009120241119230259": {
    "mockMvDecision": "REJECTED",
    "delayResponse": 0,
    "rid": "10332103161009120241119230259"
  }
}
```

#### Delete Expectation
**URL**: `http://{host}/v1/mockmv/config/expectation/{id}`
**Method**: `DELETE`

**Response**:
```text
Successfully deleted expectation $expectation_id
```

### API Documentation
API documentation is available [here](https://docs.mosip.io/1.1.5/modules/registration-processor/deduplication-and-manual-adjudication#manual-adjudication).

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
