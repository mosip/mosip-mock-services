# Mock Manual Verification (MV)

## About
* This service reads request from queue and mocks response for external "manual adjudication" and "verification" systems.

## Table of Contents
	Prerequisites
	Setting Up Locally
	Running the Application
	Configurations
	APIs Provided

### Prerequisites
	Ensure you have the following installed before proceeding:

	Java: Version 21.0.3
	Maven: For building the project
	Git: To clone the repository
	Postman (optional): For testing the APIs

### Server deployment (for sandbox deployment)
	Docker based
	
	Steps:
	* Go to REPO_ROOT/mock-mv.
	* Build the code: `mvn clean install -Dgpg.skip=true`.
	* Create a docker image: docker build . --file Dockerfile --tag mock-abis.
	* Push the docket image to docker registry. You can directly use these images for running mock ABIS.
	
	**Please check Dockerfile for passing env properties**
	
	Swagger url: `https://<hostname>/v1/mockmv/swagger-ui.html#/`

## APIs for configuration and expectation setting

### Update configuration

Url: http://{host}/v1/mockmv/config/configureMockMv

Method: POST

Request:
```json
{
  "mockMvDescision": "APPROVED"
}
```

Response:
```text
Successfully updated the configuration
```

### Get configuration
Url: http://{host}/v1/mockmv/config/configureMockMv

Method: GET

Response:
```json
{
  "mockMvDescision": "APPROVED"
}
```

### Set Expectation

Url: http://{host}/v1/mockmv/config/expectationMockMv

Method: POST

Request:
```json
{
  "mockMvDecision": "REJECTED",
  "delayResponse": 30,
  "rid": "10332103161016320241119230824"
}
```

Response:
```text
Successfully inserted expectation $expectation_id
```

### Get Expectations

Url: http://{host}/v1/mockmv/config/expectationMockMv

Method: GET

Response:
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

### Delete Expectation

Url: http://{host}/v1/mockmv/config/expectation/{id}

Method: DELETE

Response:
```text
Successfully deleted expectation $expectation_id
```

## Configurations
This section is for the developers, for developing this modules fast & efficiently

1) The configuration `mock.mv.decision` can be changed to "APPROVED" or "REJECTED" to set expected response.

2) ActiveMQ queue need to setup using application.properties:

## APIs
API documentation is available 
[here](https://docs.mosip.io/1.1.5/modules/registration-processor/deduplication-and-manual-adjudication#manual-adjudication).

## License
This project is licensed under the terms of [Mozilla Public License 2.0](LICENSE).

