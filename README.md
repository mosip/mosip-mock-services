[![Maven Package upon a push](https://github.com/mosip/mosip-mock-services/actions/workflows/push-trigger.yml/badge.svg?branch=develop)](https://github.com/mosip/mosip-mock-services/actions/workflows/push-trigger.yml)

# MOSIP Mock Services

Non-production simulations of MOSIP platform components. Use these for local development and testing only — replace with real services in production.

| | |
|---|---|
| JDK | 21 |
| Maven | 3.9+ |
| Spring Boot | 4.1.1 |
| Version | 1.4.1-SNAPSHOT |
| License | [MPL 2.0](LICENSE) |

---

## Modules

| Module | Type | Port | Purpose |
|--------|------|------|---------|
| [mock-abis](mock-abis/) | Boot service | `8081` `/v1/mock-abis-service` | ABIS insert / identify mock (ActiveMQ + H2) |
| [mock-mv](mock-mv/) | Boot service | `8081` `/v1/mockmv` | Manual verification decision mock |
| [mock-sdk](mock-sdk/) | Library JAR | — | Sample `IBioApiV2` BioSDK |
| [MockMDS](MockMDS/) | Standalone SBI | `4501–4600` | Device / MDS biometric capture mock |
| [softhsm](softhsm/) | SoftHSM | — | Crypto HSM stand-in |

---

## Prerequisites

1. **JDK 21** and **Maven 3.9+** on `PATH`
2. Install sibling SNAPSHOTs locally (or resolve from OSSRH snapshots):

```bash
# from sibling checkouts
cd ../commons/kernel && mvn clean install -Dgpg.skip=true -DskipTests
cd ../bio-utils && mvn clean install -Dgpg.skip=true -DskipTests
cd ../converters/kernel-bio-converter && mvn clean install -Dgpg.skip=true -DskipTests   # for mock-sdk
```

3. **ActiveMQ** for `mock-abis` / `mock-mv` (example):

```bash
cd mock-abis/activemq && docker-compose up -d
```

---

## Build

From the repository root (reactor):

```bash
mvn clean install "-Dgpg.skip=true"
```

Or build a single module:

```bash
cd mock-abis && mvn clean install "-Dgpg.skip=true"
cd mock-mv  && mvn clean install "-Dgpg.skip=true"
```

---

## Run locally (Boot services)

`run-local` scripts live **only** in the runnable Boot modules (`mock-abis`, `mock-mv`), same pattern as `kernel-bio-converter`.

### mock-abis

```bash
cd mock-abis
./run-local.sh init      # package (also copies abis queue JSON from sample if missing)
./run-local.sh start     # background · profile=local · :8081
./run-local.sh smoke     # actuator health + swagger
./run-local.sh stop
./run-local.sh all       # init + test + start + smoke
./run-local.sh docker    # build & run image
```

Windows cmd:

```bat
cd mock-abis
run-local.bat init
run-local.bat start
run-local.bat smoke
```

Endpoints after start:

- Health: http://127.0.0.1:8081/v1/mock-abis-service/actuator/health
- Swagger: http://127.0.0.1:8081/v1/mock-abis-service/swagger-ui/index.html

### mock-mv

```bash
cd mock-mv
./run-local.sh init
./run-local.sh start     # profile=default · :8081
./run-local.sh smoke
./run-local.sh stop
```

Windows cmd:

```bat
cd mock-mv
run-local.bat init
run-local.bat start
```

Endpoints:

- Health: http://127.0.0.1:8081/v1/mockmv/actuator/health
- Swagger: http://127.0.0.1:8081/v1/mockmv/swagger-ui/index.html

> Both services default to port **8081**. To run them together, override one port, e.g. `set PORT=8082` / `PORT=8082 ./run-local.sh start`.

Optional env: `PORT`, `SPRING_PROFILES_ACTIVE`, `SPRING_CLOUD_CONFIG_URI`, `IMAGE`, `JDK_JAVA_OPTIONS`.

---

## Other modules

### mock-sdk

Library only — no HTTP server. Depend on `io.mosip.mock.sdk:mock-sdk` and set `biosdk_class=io.mosip.mock.sdk.impl.SampleSDKV2`.

### MockMDS

Standalone SBI (not Spring Boot fat-jar). Prefer helpers from `MockMDS/`:

```bat
cd MockMDS
run-local-reg.bat init
run-local-reg.bat start
```

Or after `mvn clean install` in `MockMDS/`, from `MockMDS/target/` (quotes around `Biometric Device` are required):

```bat
java -cp mock-mds-1.4.1-SNAPSHOT.jar;lib\* io.mosip.mock.sbi.test.TestMockSBI ^
  "mosip.mock.sbi.device.purpose=Registration" ^
  "mosip.mock.sbi.biometric.type=Biometric Device"
```

Place device `.p12` keys under `Biometric Devices/{Modality}/Keys/` before starting. See [MockMDS/README.md](MockMDS/README.md).

---

## Documentation

| Doc | Content |
|-----|---------|
| [AGENTS.md](AGENTS.md) | Agent / contributor quick map |
| [NOTICE](NOTICE) / [licenses/](licenses/) | Third-party attributions (MPL matrix) |
| Module READMEs | Deeper setup for each service |

---

## License

Licensed under the [Mozilla Public License 2.0](LICENSE).
