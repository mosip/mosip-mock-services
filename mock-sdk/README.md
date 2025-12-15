# Mock Biometric SDK

## 📌 Overview
The **MockSDK** repository provides a mock implementation of the [IBioAPIV2](https://github.com/mosip/bio-utils/blob/master/kernel-biometrics-api/src/main/java/io/mosip/kernel/biometrics/spi/IBioApiV2.java) interface. It performs operations such as 1:N match, segmentation, extraction, quality analysis, and format conversion. It simulates interactions with SDK components and is designed for testing and integration within the MOSIP ecosystem without requiring actual biometric hardware or proprietary SDKs.

## ✨ Features
-   **Biometric Operations**: Simulates 1:N matching, segmentation, extraction, and quality checks.
-   **Format Conversion**: Supports mock conversion between biometric formats.
-   **Standard Compliance**: Implements the `IBioApiV2` interface as per MOSIP specifications.
-   **Unit Testing**: Ideal for mocking biometric operations during unit and integration testing.

## 🛠️ Services
-   **SampleSDKV2**: The main implementation class (`io.mosip.mock.sdk.impl.SampleSDKV2`) that provides the mock functionality.

## ⚙️ Local Setup
Since `mock-sdk` is a library, setting it up involves building it and including it as a dependency in your project.

## 📋 Pre requisites
Ensure you have the following installed before proceeding:
-   **Java 21**: The project requires Java 21.0.3 or later.
-   **Maven**: For building the project.
-   **Git**: To clone the repository.

## 🗄️ Database Setup
-   **Not Applicable**: This module is a stateless library and does not require a database.

## 📝 Configurations
To use this mock SDK implementation in other MOSIP modules (e.g., `biosdk-services`), configure the following properties in the consumer application:

```properties
biosdk_class=io.mosip.mock.sdk.impl.SampleSDKV2
mosip.role.biosdk.getservicestatus=REGISTRATION_PROCESSOR
biosdk_bioapi_impl=io.mosip.mock.sdk.impl.SampleSDKV2
```

## 🏗️ Build locally
To build the library locally:

1.  **Clone the repository**:
    ```bash
    git clone https://github.com/mosip/mosip-mock-services.git
    cd mosip-mock-services/mock-sdk
    ```

2.  **Build the project**:
    ```bash
    mvn clean install -Dgpg.skip=true
    ```
    This will install the `mock-sdk` JAR into your local Maven repository.

## 🚀 Deployment
-   **Not Applicable**: This module is a library jar and is not deployed as a standalone service. It is used as a dependency in other services.

## ⬆️ Upgrade
To upgrade:
1.  Git pull latest changes.
2.  Update dependencies in `pom.xml`.
3.  Rebuild: `mvn clean install`.

## 📚 Documentation

### Product Documentation
For more details on the Biometric SDK specifications:
[MOSIP Biometric SDK Documentation](https://docs.mosip.io/1.1.5/biometrics/biometric-sdk)

## 🤝 Contribution & Community
We welcome contributions from everyone!

[Check here](https://docs.mosip.io/1.2.0/community/code-contributions) to learn how you can contribute code to this application.

If you have any questions or run into issues while trying out the application, feel free to post them in the [MOSIP Community](https://community.mosip.io/) — we’ll be happy to help you out.

[![GitHub Issues](https://img.shields.io/badge/GitHub-Issues-181717?style=flat&logo=github&logoColor=white)](https://github.com/mosip/mosip-mock-services/issues)

## 📝 License
![License: MPL 2.0](https://img.shields.io/badge/License-MPL_2.0-brightgreen.svg)

This project is licensed under the terms of [Mozilla Public License 2.0](LICENSE).
See the [LICENSE](LICENSE) file for full license details.