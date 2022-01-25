# Mock ABIS
A module to mock  ABIS functionality for testing non-production MOSIP deployments

## Requirements
* Java 11
* Partner certificate (in case of encryption is enabled)

## Setup
Important and Mandatory
In case of partner based encryption. Upload the certificate, by using swagger upload certificate request

### Server deployment (for sandbox deployment)
Docker based

Steps:
* Go to REPO_ROOT/mock-abis.
* Build the code: `mvn clean install -Dmaven.test.skip=true -Dgpg.skip=true`.
* Create a docker image: docker build . --file Dockerfile --tag mock-abis.
* Push the docket image to docker registry. You can directly use these images for running mock ABIS.

**Please check Dockerfile for passing env properties**

Swagger url: `https://<hostname>/v1/mock-abis-service/swagger-ui.html#/`


### Local dev against Server (for testing against server)
This section is for the developers to run mock-abis locally against MOSIP server

Steps:
* Go to REPO_ROOT/mock-abis.
* Setting ABIS queue conf (here queue details will be for server's queue):
  1) Create registration-processor-abis.json in resources.
  2) Copy the contents of registration-processor-abis-sample.json to registration-processor-abis.json.
  3) Update registration-processor-abis.json with the correct queue details.
* Build the code: `mvn clean install -Dmaven.test.skip=true -Dgpg.skip=true`.
* Run the jar: `java -jar -Dlocal.development=true -Dabis.bio.encryption=true -Dspring.profiles.active=local -Dmosip_host=https://<server hostname> target/mockabis-service.jar`.

Flags:
* local.development (true: whenever running locally, this will take the registration-processor-abis.json from resources)
* abis.bio.encryption (true: in case if partner based encryption)
* mosip_host (hostname of the MOSIP server)

Swagger url: `http://localhost:8081/v1/mock-abis-service/swagger-ui.html#/`

### Fully local (for active development)
This section is for developers to run mock-abis locally against a queue, messages can be inserted directly to the queue and respective mock-abis responses can be analysed.

Requirements:
* Local ActiveMQ server

Running ActiveMQ locally:
* Go to REPO_ROOT/mock-abis/activemq
* use the docker compose file to create a local activemq server: `docker-compose up`
* Open activemq web console: `http://localhost:8161/`

Steps:
* Go to REPO_ROOT/mock-abis.
* Setting ABIS queue conf (here queue details will be for local queue):
  1) Create registration-processor-abis.json in resources.
  2) Copy the contents of registration-processor-abis-sample.json to registration-processor-abis.json.
  3) Update registration-processor-abis.json with the correct queue details.
* Build the code: `mvn clean install -Dmaven.test.skip=true -Dgpg.skip=true`.
* Run the jar: `java -jar -Dlocal.development=true -Dabis.bio.encryption=true -Dspring.profiles.active=local -Dmosip_host=https://<server hostname> target/mockabis-service.jar`.
* Add message directly to queue and view responses from mock ABIS

Flags:
* local.development (true: whenever running locally)
* abis.bio.encryption (true: in case if partner based encryption)
* mosip_host (hostname of the MOSIP server)

Swagger url: `http://localhost:8081/v1/mock-abis-service/swagger-ui.html#/`

Upload API is mandatory each time when application is started.This will used for configuring application.
### Upload API

Url: http://{host}/v1/mock-abis-service/abis/upload

Method: POST

Request:
		password: password
		alias: cbeff
		keystore: PKCS12
		
		and upload cbeff.p12 file
		
Response:
```text
"Successfully uploaded file"
```


## APIs for configuration and expectation setting
[Sample expectations](./docs/sampleExpectations.md)

### Update configuration

Url: http://{host}/v1/mock-abis-service/api/v0/proxyabisconfig/configure

Method: POST

Request:
```json
{
	"findDuplicate": "false"
}
```

Response:
```text
Successfully updated the configuration
```

### Get configuration
Url: http://{host}/v1/mock-abis-service/api/v0/proxyabisconfig/configure

Method: GET

Response:
```json
{
  "findDuplicate": false
}
```

### Set Expectation

Url: http://{host}/v1/mock-abis-service/api/v0/proxyabisconfig/expectation

Method: POST

Request:
```json
{
  "id": "<Hash of the biometric>",
  "version": "xxxxx",
  "requesttime": "2021-05-05T05:44:58.525Z",
  "actionToInterfere": "Identify/ Insert",
  "forcedResponse": "Error",
  "errorCode": "",
  "delayInExecution": "",
  "gallery": {
    "referenceIds": [
      {
        "referenceId": "<Hash of the duplicate biometric>"
      }
    ]
  }
}
```

Response:
```text
Successfully inserted expectation $expectation_id
```

### Get Expectations

Url: http://{host}/v1/mock-abis-service/api/v0/proxyabisconfig/expectation

Method: GET

Response:
```json
{
    "abshd": {
        "id": "abshd",
        "version": "xxxxx",
        "requesttime": "2021-05-05T05:44:58.525Z",
        "actionToInterfere": "Identify/ Insert",
        "errorCode": "",
        "delayInExecution": "",
        "forcedResponse": "Error/Success",
        "gallery": {
            "referenceIds": [
                {
                  "referenceId": "xxxxxx"
                },
                {
                  "referenceId": "xxxxxx"
                }
            ]
        }
    },
    "dffefe": {
        "id": "dffefe",
        "version": "xxxxx",
        "requesttime": "2021-05-05T05:44:58.525Z",
        "actionToInterfere": "Identify/ Insert",
        "forcedResponse": "Error/Success",
        "errorCode": "",
        "delayInExecution": "",
        "gallery": {
            "referenceIds": [
                {
                  "referenceId": "xxxx"
                },
                {
                  "referenceId": "xxxxxx"
                }
            ]
        }
    }
}
```

### Delete Expectation

Url: http://{host}/v1/mock-abis-service/api/v0/proxyabisconfig/expectation/{id}

Method: DELETE

Response:
```text
Successfully deleted expectation $expectation_id
```

## Tips & tricks

1) While setting the expectation the hash of iso image should be taken, directly taking bdb hash will not work.
```text
formula for hash: SHA256_hash(base64_decode(bdb))
```

2) Use get cached biometics to check whether the hashes are proper.

## Developer (tips and trick)
This section is for the developers, for developing this modules fast & efficiently

1) Use local profile: `-Dspring.profiles.active=local`. Pass this as VM options

2) Pass: `mosip_host=https://<mosip host>` as env variable.

3) Setting ABIS queue conf:
* Create registration-processor-abis.json in resources
* Copy the contents of registration-processor-abis-sample.json to registration-processor-abis.json
* Update registration-processor-abis.json with the correct queue details

By performing the above steps, you are ready to run mock-ABIS in local machine