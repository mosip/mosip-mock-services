# mock-abis

```
Boot ABIS mock · ActiveMQ/JMS · :8081 /v1/mock-abis-service
├─ entry: ProxyAbisApplication
├─ flow: Listener → Insert/Identify → outbound queue
├─ ExpectationCache · id = SHA256(base64_decode(bdb))
├─ H2 default · PostgreSQL optional
└─ profile local: no auth token (application-local.properties)
```

```
run
├─ ./run-local.sh init | start | smoke | stop | test | all | docker
├─ run-local.bat … (Windows cmd)
├─ activemq/: docker-compose up  (before start)
└─ profile local · :8081 · /v1/mock-abis-service
```

```
deps: kernel-core · kernel-biometrics-api · spring-boot-jackson2 · spring-cloud-starter-bootstrap · springdoc 3.1.1
parent: mosip-mock-services 1.4.1-SNAPSHOT · NO kernel-bom
build: mvn clean install "-Dgpg.skip=true"
test:  mvn test "-Dgpg.skip=true" [-Dtest=Class#method]
```
