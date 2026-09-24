# MockMDS

```
standalone SBI · NOT Boot · ServerSocket 4501–4600 · TestMockSBI→SBIMockService
├─ Face · FingerSlap|Single · IrisDouble|Single
├─ Biometric Devices/{Modality}/ · Keys/*.p12 · java cwd MUST be target/
├─ SBI verbs: MOSIPDISC|/device · MOSIPDINFO|/info · CAPTURE|RCAPTURE|/capture · STREAM|/stream
├─ admin POST /admin/status|score|delay|profile
└─ static Swagger: GET /swagger-ui/ · /v3/api-docs  (rewrites servers→bound port)
```

```
run (cwd=MockMDS/; .local/)
├─ reg/JP2000: run-local-reg.bat|.sh → init|start|smoke|stop|test|all
└─ auth/WSQ:   run-local-auth.bat|.sh  (java cwd=target/)
```

```
deps: biometrics-util · kernel-core · jackson2 · okhttp · jose4j
build: mvn … "-Dgpg.skip=true" · cfg→target/ · swagger-ui→target/swagger-ui/
landmine: quote "Biometric Device" in CLI args · NO Boot repackage · NO springdoc
```
