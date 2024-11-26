# Mock Biometric SDK



# MockSDK

The **MockSDK** repository provides a mock implementation of [IBioAPI](https://github.com/mosip/commons/blob/master/kernel/kernel-biometrics-api/src/main/java/io/mosip/kernel/biometrics/spi/IBioApi.java) to perform 1:N match, segmentation, extraction, quality, converter etc.It simulates interactions with SDK components and is designed for testing and integration within the MOSIP ecosystem.

---

## Table of Contents
	- [Prerequisites](#prerequisites)
	- [Setting Up Locally](#setting-up-locally)
	- [Running the Application](#running-the-application)
	- [Configurations](#configurations)
	- [APIs Provided](#apis-provided)
	- [License](#license)

---

## Prerequisites

Ensure you have the following installed before proceeding:

	- Java: Version 21.0.3
	- Maven: For building the project
	- Git: To clone the repository
	- Postman (optional): For testing the APIs

---

## Setting Up Locally

### Steps to Set Up:

1. **Clone the repository**

```bash
	   git clone https://github.com/mosip/mosip-mock-services.git
	   cd mosip-mock-services/mock-sdk
```

2. **Build the project**
Use Maven to build the project and resolve dependencies.

```bash
   mvn clean install -Dgpg.skip=true
```

---

## Running the Application
Used as reference implementation for biosdk-services[https://github.com/mosip/biosdk-services].

## Configurations 
In biosdk-services below values are required for mockSDK implementation

	biosdk_class=io.mosip.mock.sdk.impl.SampleSDKV2
	mosip.role.biosdk.getservicestatus=REGISTRATION_PROCESSOR
	biosdk_bioapi_impl=io.mosip.mock.sdk.impl.SampleSDKV2


---

## APIs Provided

MockSDK follows implementation based on [Mosip Spec][https://docs.mosip.io/1.1.5/biometrics/biometric-sdk]:


---


## License

This project is licensed under the [MOSIP License](LICENSE).  

---