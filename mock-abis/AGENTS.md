# mock-abis

```
Boot · AMQ · H2 · :8081 /v1/mock-abis-service · profile=local
├─ ProxyAbisApplication → Listener → Insert|Identify → out-queue
├─ ExpectationCache · id=SHA256(b64decode(bdb))
└─ run-local.sh|.bat → init|start|smoke|stop|test|all|docker
   └─ activemq/: docker-compose up   # before start
```

```
deps: kernel-core · biometrics-api · jackson2 · cloud · springdoc3.1.1
build: mvn … "-Dgpg.skip=true"
```
