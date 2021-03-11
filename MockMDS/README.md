# MockMDS

This module contains Mock MOSIP Device service implementation as per defined [SPEC](https://docs.mosip.io/platform/biometrics/mosip-device-service-specification) 

Biometric data for registration capture is from files/MockMDS/registration

Biometric data for auth capture is from files/MockMDS/auth

Java 11 is a prerequisite

### Supported SPEC versions

0.9.5

### Defaults

server.port=4501

### Build and run

Execute the below command to build module

> mvn clean install

target folder is created on successful build, run.sh / run.bat executable can be found under target folder.

otherwise, we could simply run Main class

> org.biometric.provider.ProviderApplication

Sample command: `java -cp provider-0.0.1-SNAPSHOT.jar;lib\* io.mosip.mock.sbi.test.TestMockSBI "mosip.mock.sbi.device.purpose=Regis
                 tration" "mosip.mock.sbi.biometric.type=Biometric Device"`