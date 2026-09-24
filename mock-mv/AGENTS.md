# mock-mv

```
Boot · AMQ · in-mem · :8081 /v1/mockmv · profile=local
├─ ProxyMvApplication → QueueListener → MockMvDecisionService
├─ APPROVED|REJECTED · ExpectationCache(RID)
└─ run-local.sh|.bat → init|start|smoke|stop|test|all|docker
```

```
deps: kernel-core · jackson2 · cloud · springdoc3.1.1
build: mvn … "-Dgpg.skip=true"
```
