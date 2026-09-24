# mock-mv

```
Boot Manual Verification mock · ActiveMQ · :8081 /v1/mockmv
├─ entry: ProxyMvApplication
├─ flow: QueueListener → MockMvDecisionService
├─ decision: APPROVED|REJECTED · per-RID ExpectationCache override
└─ no DB — all in-memory
```

```
run
├─ ./run-local.sh init | start | smoke | stop | test | all | docker
├─ run-local.bat … (Windows cmd)
├─ optional: SPRING_CLOUD_CONFIG_URI
└─ profile local · :8081 · /v1/mockmv
```

```
deps: kernel-core · spring-cloud 2025.1.3 · spring-boot-jackson2 · springdoc 3.1.1
parent: mosip-mock-services 1.4.1-SNAPSHOT · NO kernel-bom
build: mvn clean install "-Dgpg.skip=true"
test:  mvn test "-Dgpg.skip=true" [-Dtest=Class#method]
```
