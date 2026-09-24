# mosip-mock-services

```
JDK21 · Maven3.9+ · Boot 4.1.1 · parent mosip-mock-services · NO kernel-bom
└─ mvn clean install "-Dgpg.skip=true"
```

```
prereq
├─ ../commons/kernel → kernel-core 1.4.1-SNAPSHOT
├─ ../bio-utils → kernel-biometrics-api · biometrics-util 1.4.1-SNAPSHOT
└─ ../converters → kernel-bio-converter 1.4.1-SNAPSHOT (mock-sdk)
```

```
reactor
├─ mock-abis   # Boot ABIS mock · ActiveMQ/JMS · H2 · :8081
├─ mock-mv     # Boot Manual Verification · ActiveMQ · in-memory · :8081
├─ mock-sdk    # lib JAR · IBioApiV2 SampleSDKV2 · no Boot repackage
└─ MockMDS     # standalone SBI · TestMockSBI · ports 4501–4600
```

```
rules
├─ siblings omit <version>
├─ libs (mock-sdk): no Boot repackage
├─ Jackson2 via spring-boot-jackson2 (not Jackson3 default)
├─ ban: kernel-bom
└─ nested AGENTS.md wins when editing that module
```
