# MockMDS

## 📌 Overview
This module provides a mock implementation of the MOSIP Device Service, designed to comply with the defined [SBI specification](https://docs.mosip.io/1.2.0/modules/partner-management-services/pms-existing/device-provider-partner#sbi-secure-biometric-interface). Biometric data for capture is stored in the files/MockMDS/ directory.

## ✨ Features
-   **SBI Compliance**: Implements standard SBI endpoints for device discovery, info, and capture.
-   **Dual Mode**: Supports both **Registration** and **Authentication** device profiles.
-   **Configurable Scenarios**:
    -   Set device status (Ready, Busy, Not Ready).
    -   Simulate different biometric quality scores.
    -   introduce delays in API responses.
    -   Switch between different user profiles for capture data.
-   **File-Based Simulation**: Uses local files to mock biometric data capture.

## 🛠️ Services
-   **MockMDS Service**: The standalone service that acts as the MDS provider.

## ⚙️ Local Setup
This module is primarily designed to be run locally as a Java application to serve the MDS requests on your machine.

### 📋 Pre requisites
Ensure you have the following installed:
-   **Java**: Version 21.0.3
-   **Maven**: For building the project
-   **Git**: To clone the repository
-   **Postman (optional)**: For testing the APIs

### 🏗️ Build
1.  **Clone the repository**:
    ```bash
    git clone https://github.com/mosip/mosip-mock-services.git
    cd mosip-mock-services/MockMDS
    ```
2.  **Build the Project**:
    ```bash
    mvn clean install -Dmaven.test.skip=true -Dgpg.skip=true
    ```

### 📝 Configurations
1.  **Modify Configuration Files**:
    -   Edit `DeviceDiscovery.json`, `DeviceInfo.json`, and `DigitalId.json` if changes are required for devices.
2.  **Certificates**:
    -   Place the `device-partner.p12` and `ftm-partner.p12` files into the folder:
        `Biometric Devices\{all different modality}\Keys`
3.  **Update Properties**:
    -   Update the `resources/application.properties` file with the following values:
        ```properties
        mosip.mock.sbi.file.{modality}.keys.keyalias={alias}
        mosip.mock.sbi.file.{modality}.keys.keystorepwd={password}

        mosip.auth.server.url=https://{env}/v1/authmanager/authenticate/clientidsecretkey
        mosip.auth.appid=regproc
        mosip.auth.clientid=mosip-regproc-client
        mosip.auth.secretkey={password}

        mosip.ida.server.url=https://{env}/idauthentication/v1/internal/getCertificate?applicationId=IDA&referenceId=IDA-FIR
        ```

## ☕ Running the Application
You can run the mock service in two modes:

### 1. For Authentication Devices
-   **Using Script**: Use the `run_auth.bat` executable located in the target folder.
-   **Using Java**:
    Execute the Main class directly:
    ```bash
    java -cp mock-mds-1.3.0-SNAPSHOT.jar;lib\* io.mosip.mock.sbi.test.TestMockSBI \
    "mosip.mock.sbi.device.purpose=Auth" \
    "mosip.mock.sbi.biometric.type=Biometric Device" \
    "mosip.mock.sbi.biometric.image.type=WSQ"
    ```

### 2. For Registration Devices
-   **Using Script**: Use the `run_reg.bat` executable located in the target folder.
-   **Using Java**:
    Execute the Main class directly:
    ```bash
    java -cp mock-mds-1.3.0-SNAPSHOT.jar;lib\* io.mosip.mock.sbi.test.TestMockSBI \
    "mosip.mock.sbi.device.purpose=Registration" \
    "mosip.mock.sbi.biometric.type=Biometric Device"
    ```

## 🚀 Deployment
-   **Docker**: Currently, this module does not have a standard Dockerfile for containerized deployment in the main flow, as MDS usually runs on the client machine (Windows/Android) rather than the server. Setup is typically done directly on the Mock MDS host machine.

## 📚 Documentation
-   **MDS Specification**: [MOSIP Device Service Specification](https://docs.mosip.io/1.1.5/biometrics/mosip-device-service-specification)

### API Endpoints
The service exposes specific endpoints for controlling the mock behavior:

| Endpoint | Method | Description | Json |
| :--- | :--- | :--- | :--- |
| `/admin/status` | POST | Set the device status ["Ready", "Busy", "Not Ready" or "Not Registered"] | `{"type": "Biometric Device","deviceStatus": "Ready"}` |
| `/admin/score` | POST | Set the biometric quality score | `{"type": "Biometric Device","qualityScore": "44.44",fromIso" : false}` |
| `/admin/delay` | POST | Set the delay response for api calls [RCAPTURE, CAPTURE, MOSIPDINFO, MOSIPDISC, STREAM] | `{"type": "Biometric Device","delay": "10000","method":["RCAPTURE"]}` |
| `/admin/profile` | POST | Set the profile to get biometric data ["Default", "Profile1", "Profile2"] | `{"type": "Biometric Device","profileId": "Profile1"}` |

## 🤝 Contribution & Community
We welcome contributions from everyone!

[Check here](https://docs.mosip.io/1.2.0/community/code-contributions) to learn how you can contribute code to this application.

If you have any questions or run into issues while trying out the application, feel free to post them in the [MOSIP Community](https://community.mosip.io/) — we’ll be happy to help you out.

[![GitHub Issues](https://img.shields.io/badge/GitHub-Issues-181717?style=flat&logo=github&logoColor=white)](https://github.com/mosip/mosip-mock-services/issues)

## 📝 License
![License: MPL 2.0](https://img.shields.io/badge/License-MPL_2.0-brightgreen.svg)

This project is licensed under the terms of [Mozilla Public License 2.0](LICENSE).
See the [LICENSE](LICENSE) file for full license details.