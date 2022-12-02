# MockMDS

## Overview
This module contains Mock MOSIP Device service implementation as per defined [SBI specification](https://docs.mosip.io/1.2.0/biometrics/secure-biometric-interface).  Biometric data for capture is available at `files/MockMDS/` 

## Defaults
server.port=4501

## Build and run

Execute the below command to build module:

```sh
mvn clean install
```

`run.sh / run.bat` executable can be found under target folder.  Or run Main class `io.mosip.mock.sbi.test.TestMockSBI` directly.

