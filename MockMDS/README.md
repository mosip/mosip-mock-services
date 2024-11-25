# MockMDS

## About

This module provides a mock implementation of the MOSIP Device Service, designed to comply with the defined [SBI specification](https://docs.mosip.io/1.2.0/modules/partner-management-services/pms-existing/device-provider-partner#sbi-secure-biometric-interface). Biometric data for capture is stored in the files/MockMDS/ directory.   

## Table of Contents

	- Prerequisites
	- Setting Up Locally
	- Running the Application
	- Configurations
	- APIs Provided

### Prerequisites
	
Before you begin, ensure the following tools and software are installed:

	- Java: Version 21.0.3
	- Maven: For building the project
	- Git: To clone the repository
	- Postman (optional): For testing the APIs

### Setting Up Locally

Follow the steps below to clone and set up the MockMDS service locally:

1. Clone the repository:

```bash
   git clone https://github.com/mosip/mosip-mock-services.git
   cd mosip-mock-services/MockMDS
```

2. Build the Project
Use Maven to build the project and resolve dependencies.

3. Modify Configuration Files

Update the following files and settings as needed:

a) Edit the files DeviceDiscovery.json, DeviceInfo.json, and DigitalId.json if changes are required for devices.

b) Place the device-partner.p12 and ftm-partner.p12 files into the folder:

	Biometric Devices\{all different modality}\Keys

c) Update the resources/application.properties file with the following values:

		mosip.mock.sbi.file.{modality}.keys.keyalias={alias}
		mosip.mock.sbi.file.{modality}.keys.keystorepwd={password}
		
		mosip.auth.server.url=https://{env}/v1/authmanager/authenticate/clientidsecretkey
		mosip.auth.appid=regproc
		mosip.auth.clientid=mosip-regproc-client
		mosip.auth.secretkey={password}
	
		mosip.ida.server.url=https://{env}/idauthentication/v1/internal/getCertificate?applicationId=IDA&referenceId=IDA-FIR
	

Follow the steps below to build and run the module:

1. **Build the Module**  

   Execute the following command:  

```sh
   mvn clean install -Dmaven.test.skip=true -Dgpg.skip=true
```
2. **Run the Application**

	 a) For Authentication Devices

	- Use the run_auth.bat executable located in the target folder.
	- Alternatively, execute the Main class directly using the following command:


```sh
java -cp mock-mds-1.3.0-SNAPSHOT.jar;lib\* io.mosip.mock.sbi.test.TestMockSBI \
"mosip.mock.sbi.device.purpose=Auth" \
"mosip.mock.sbi.biometric.type=Biometric Device" \
"mosip.mock.sbi.biometric.image.type=WSQ"
```

	b) For Registration Devices

	- Use the run_reg.bat executable located in the target folder.
	- Alternatively, execute the Main class directly using the following command:	

```sh
java -cp mock-mds-1.3.0-SNAPSHOT.jar;lib\* io.mosip.mock.sbi.test.TestMockSBI \
"mosip.mock.sbi.device.purpose=Registration" \
"mosip.mock.sbi.biometric.type=Biometric Device"
```


## APIs
API documentation is available [here](https://docs.mosip.io/1.1.5/biometrics/mosip-device-service-specification).

The following APIs are exposed by the MockMDS service to custom test settings:

| Endpoint              | Method | Description                     | Json                                                   |
|-----------------------|--------|---------------------------------|--------------------------------------------------------|
| `/admin/status`       | POST   | Set the device status ["Ready", "Busy", "Not Ready" or "Not Registered"]   | {"type": "Biometric Device","deviceStatus": "Ready"}
| `/admin/score`       | POST   | Set the biometric quality score     | {"type": "Biometric Device","qualityScore": "44.44",fromIso" : false}
| `/admin/delay`       | POST   | Set the delay response for api calls [RCAPTURE, CAPTURE, MOSIPDINFO, MOSIPDISC, STREAM]   | {"type": "Biometric Device","delay": "10000","method":["RCAPTURE"]}
| `/admin/profile`       | POST   | Set the profile to get biometric data ["Default", "Profile1", "Profile2"]   | {"type": "Biometric Device","profileId": "Profile1"}


## License
This project is licensed under the terms of [Mozilla Public License 2.0](LICENSE).