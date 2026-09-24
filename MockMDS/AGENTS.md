# MockMDS

```
standalone SBI · NOT Boot · 4501–4600 · TestMockSBI→SBIMockService
├─ Face · FingerSlap|Single · IrisDouble|Single
├─ Biometric Devices/{Modality}/ · Keys/*.p12
└─ admin POST /admin/status|score|delay|profile
```

```
run (cwd=MockMDS/; .local/)
├─ reg/JP2000: run-local_reg.bat|.sh → init|start|smoke|stop|test|all
└─ auth/WSQ:   run-local_auth.bat|.sh  (java cwd=target/)
```

```
deps: biometrics-util · kernel-core · jackson2 · okhttp · jose4j
build: mvn … "-Dgpg.skip=true" · cfg→target/
```
