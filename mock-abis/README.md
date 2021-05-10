# Mock ABIS
A module to mock  ABIS functionality for testing non-production MOSIP deployments

[Sample expectations](./docs/sampleExpectations.md)

## APIs for configuration and expectation setting

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
        "referenceId": "xxxxxx"
      },
      {
        "referenceId": "xxxxxx"
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
                  "referenceId": "xxxxxx"
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

## Developer (tips and trick)
This section is for the developers, for developing this modules fast & efficiently

1) Use local profile: `-Dspring.profiles.active=local`. Pass this as VM options

2) Pass: `mosip_host=https://<mosip host>` as env variable.

3) Setting ABIS queue conf:
* Create registration-processor-abis.json in resources
* Copy the contents of registration-processor-abis-sample.json to registration-processor-abis.json
* Update registration-processor-abis.json with the correct queue details

By performing the above steps, you are ready to run mock-ABIS in local machine