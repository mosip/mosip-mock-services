# Sample expectations

## Insert

### Return error
This expectation will make the ABIS to return an error with failureResponse as errorCode

```json
{
  "id": "b57038791f59de0d43cd9c06dd2c621888d3170ed725ca9cb7f70122519b8484",
  "version": "xxxxx",
  "requesttime": "2021-05-05T05:44:58.525Z",
  "actionToInterfere": "Insert",
  "forcedResponse": "Error",
  "errorCode": "4",
  "delayInExecution": "",
  "gallery": null
}
```

### Delay execution
This expectation will make the ABIS to return a delayed response.

_delayInExecution parameter has to be provided in seconds_

```json
{
  "id": "b57038791f59de0d43cd9c06dd2c621888d3170ed725ca9cb7f70122519b8484",
  "version": "xxxxx",
  "requesttime": "2021-05-05T05:44:58.525Z",
  "actionToInterfere": "Insert",
  "forcedResponse": "Error",
  "errorCode": "4",
  "delayInExecution": "5",
  "gallery": null
}
```


## Identify

### Return duplicate
This expectation will make the ABIS to return a duplicate of the mentioned gallery

```json
{
  "id": "b57038791f59de0d43cd9c06dd2c621888d3170ed725ca9cb7f70122519b8484",
  "version": "xxxxx",
  "requesttime": "2021-05-05T05:44:58.525Z",
  "actionToInterfere": "Identify",
  "forcedResponse": "Duplicate",
  "errorCode": "",
  "delayInExecution": "",
  "gallery": {
    "referenceIds": [
      {
        "referenceId": "bbe2a7932e9f228c21504a63a5ef1b829ad99478d0ca3b75a159b7342260314a"
      }
    ]
  }
}
```

### Return not duplicate
This expectation will make the ABIS to return "not duplicate"

```json
{
  "id": "b57038791f59de0d43cd9c06dd2c621888d3170ed725ca9cb7f70122519b8484",
  "version": "xxxxx",
  "requesttime": "2021-05-05T05:44:58.525Z",
  "actionToInterfere": "Identify",
  "forcedResponse": "Duplicate",
  "errorCode": "",
  "delayInExecution": "",
  "gallery": null
}
```

### Return error
This expectation will make the ABIS to return an error with failureResponse as errorCode

```json
{
  "id": "b57038791f59de0d43cd9c06dd2c621888d3170ed725ca9cb7f70122519b8484",
  "version": "xxxxx",
  "requesttime": "2021-05-05T05:44:58.525Z",
  "actionToInterfere": "Identify",
  "forcedResponse": "Error",
  "errorCode": "4",
  "delayInExecution": "",
  "gallery": null
}
```

### Delay execution
This expectation will make the ABIS to return a delayed response.

_delayInExecution parameter has to be provided in seconds_

```json
{
  "id": "b57038791f59de0d43cd9c06dd2c621888d3170ed725ca9cb7f70122519b8484",
  "version": "xxxxx",
  "requesttime": "2021-05-05T05:44:58.525Z",
  "actionToInterfere": "Identify",
  "forcedResponse": "Error",
  "errorCode": "4",
  "delayInExecution": "5",
  "gallery": null
}
```

