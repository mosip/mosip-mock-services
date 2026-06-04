[![Maven Package upon a push](https://github.com/mosip/mosip-mock-services/actions/workflows/push-trigger.yml/badge.svg?branch=develop)](https://github.com/mosip/mosip-mock-services/actions/workflows/push-trigger.yml)


# MOSIP Mock Services

The MOSIP Mock Services repository provides simulations of various components used within the MOSIP platform. These mock services are intended solely for non-production environments and should be replaced with actual components in production deployments.

## 📦 Modules

The repository encompasses several modules, each emulating a specific MOSIP component. Below is an overview of each module, along with links to their respective internal README files for detailed information:

### 🧪 MockMDS
This module simulates the MOSIP Device Service (MDS) as per the defined Secure Biometric Interface (SBI) specifications. It provides biometric data for capture, facilitating testing and integration without the need for actual biometric devices.  
[Read more](MockMDS/README.md)

### 🧬 mock-abis
This module emulates the Automated Biometric Identification System (ABIS) functionality, enabling testing of biometric deduplication and identification processes in non-production MOSIP deployments. It also offers a Swagger API to test INSERT and IDENTIFY operations.  
[Read more](mock-abis/README.md)

### 🧩 mock-sdk
This module provides a mock implementation of the MOSIP Software Development Kit (SDK), allowing developers to simulate SDK interactions and test their applications without relying on the actual SDK.
[Read more](mock-sdk/README.md)

### 🛂 mock-mv
This module simulates the Master Data Validator (MV) service, which is responsible for validating master data within the MOSIP ecosystem. It aids in testing validation processes during development.
[Read more](mock-mv/README.md)

### 🔐 softhsm
This module offers a mock implementation of a Hardware Security Module (HSM) using SoftHSM. It is used for cryptographic operations in a non-production environment, enabling developers to test security features without requiring physical HSM hardware.

## 🚀 Usage
For comprehensive details on each module, including setup instructions and usage guidelines, please refer to their respective README files linked above.

**Note:** Ensure that these mock services are utilized exclusively in development and testing environments. For production deployments, replace them with the corresponding real components to maintain system integrity and security.

## 📝 License
![License: MPL 2.0](https://img.shields.io/badge/License-MPL_2.0-brightgreen.svg)

This project is licensed under the terms of [Mozilla Public License 2.0](LICENSE).  
See the [LICENSE](LICENSE) file for full license details.