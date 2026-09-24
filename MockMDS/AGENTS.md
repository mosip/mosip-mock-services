# MockMDS

```
standalone SBI mock · NOT a Boot deployable · ports 4501–4600
├─ entry: TestMockSBI.main → SBIMockService
├─ helpers: Face · FingerSlap · FingerSingle · IrisDouble · IrisSingle
├─ data: Biometric Devices/{Modality}/ · Keys/*.p12 required
└─ admin POST: /admin/status|score|delay|profile
```

```
run-local (from MockMDS/; logs/PID in .local/)
├─ Registration/JP2000: run-local-reg.bat | ./run-local-reg.sh
│    init | start | smoke | stop | test | all
└─ Auth/WSQ:            run-local-auth.bat | ./run-local-auth.sh
     (same commands; java cwd = target/)
```

```
deps: biometrics-util · kernel-core · spring-boot-jackson2 · okhttp · jose4j
parent: mosip-mock-services 1.4.1-SNAPSHOT · NO kernel-bom
build: mvn clean install "-Dgpg.skip=true"
cfg:   application.properties (also copied to target/)
```
