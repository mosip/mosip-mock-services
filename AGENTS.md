# mosip-mock-services

```
JDK21 · Maven3.9+ · Boot4.1.1 · parent · NO kernel-bom
└─ mvn clean install "-Dgpg.skip=true"
```

```
prereq (1.4.1-SNAPSHOT)
├─ ../commons/kernel → kernel-core
├─ ../bio-utils → biometrics-api · biometrics-util
└─ ../converters → kernel-bio-converter (mock-sdk)
```

```
reactor
├─ mock-abis  # Boot ABIS · AMQ · H2 · :8081 /v1/mock-abis-service
├─ mock-mv    # Boot MV · AMQ · :8081 /v1/mockmv
├─ mock-sdk   # lib JAR · SampleSDKV2 · no Boot repackage
└─ MockMDS    # standalone SBI · 4501–4600
```

```
rules
├─ siblings omit <version> · Jackson2 · ban kernel-bom
├─ libs: no Boot repackage · nested AGENTS.md wins
└─ GC/JVM extras → Helm values.yaml (not Dockerfile CMD)
```
