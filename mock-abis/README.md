# Mock ABIS
A module to mock  ABIS functionality for testing non-production MOSIP deployments

## Expectation functionality

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