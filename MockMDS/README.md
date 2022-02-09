# MockMDS

This module contains Mock MOSIP Device service implementation as per defined [SPEC](https://docs.mosip.io/platform/biometrics/mosip-device-service-specification) 

Biometric data for registration capture is from Profile/ folder in iso format

Biometric data for auth capture is from Profile/ folder in iso format

Valid device Certificates should be present in Biometric Devices/ [Modality] /Keys/
Certificate alias and password should be updated in application.properties for each modality

Multiple instance can be run, It automatically choose its port.

Java 11 is a prerequisite

### Supported SPEC versions

0.9.5

### Defaults

server.minport=4501
server.maxport=4600

### Build and run

Execute the below command to build module

> mvn clean install

target folder is created on successful build, run.sh / run.bat / run_auth.bat executable can be found under target folder.

	run.bat is to run it as Registration Device.
	
	run_auth.bat is to run it as Auth device.

otherwise, we could simply run Main class

> io.mosip.mock.sbi.test.TestMockSBI by passing the below arguments

	"mosip.mock.sbi.device.purpose=[Auth/Registration]" "mosip.mock.sbi.biometric.type=[Biometric Device/Face/Finger/Iris]"
