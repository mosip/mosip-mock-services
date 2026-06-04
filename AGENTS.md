# AGENTS.md

This file provides guidance to AI agents when working with code in this repository.

## Overview

MOSIP Mock Services is a multi-module Java repository that provides non-production simulations of components in the [MOSIP](https://mosip.io) identity platform. Each module is an independent Maven project — there is no parent/aggregator pom. All modules target **Java 21** and use **Maven 3.9.x**.

The four modules and their roles:

| Module | Artifact | Purpose |
|--------|----------|---------|
| `MockMDS` | `mock-mds` | Simulates MOSIP Device Service (SBI spec) — runs as a local standalone Java app, not a Spring Boot server |
| `mock-abis` | `mock-abis` | Spring Boot service simulating ABIS (biometric deduplication). Consumes/produces ActiveMQ messages; exposes REST/Swagger |
| `mock-mv` | `mock-mv` | Spring Boot service simulating Manual Verification. Queue-driven (ActiveMQ); exposes REST/Swagger |
| `mock-sdk` | `mock-sdk` | Library JAR implementing `IBioApiV2` (MOSIP biometric SDK interface). Not a deployable service |

## Build Commands

Each module must be built individually from its own directory:

```bash
# Build any module (skip GPG signing required for local builds)
cd mock-abis && mvn clean install -Dgpg.skip=true
cd mock-mv  && mvn clean install -Dgpg.skip=true
cd mock-sdk && mvn clean install -Dgpg.skip=true
cd MockMDS  && mvn clean install -Dmaven.test.skip=true -Dgpg.skip=true
```

## Running Tests

```bash
# Run all tests in a module
cd mock-abis && mvn test -Dgpg.skip=true

# Run a single test class
cd mock-abis && mvn test -Dtest=ProxyAbisInsertServiceImplTest -Dgpg.skip=true

# Run a single test method
cd mock-abis && mvn test -Dtest=ProxyAbisInsertServiceImplTest#testInsert -Dgpg.skip=true
```

The same `-Dtest=` pattern works for `mock-mv`, `mock-sdk`, and `MockMDS`.

## Running the Services

### mock-abis (Spring Boot, port 8081)

Requires ActiveMQ. Start a local one first:
```bash
cd mock-abis/activemq && docker-compose up
```

Create `mock-abis/src/main/resources/registration-processor-abis.json` from the sample file, then run:
```bash
cd mock-abis
java -Dloader.path=lib/kernel-auth-adapter-1.4.0-SNAPSHOT.jar \
  -Dlocal.development=true -Dabis.bio.encryption=false \
  -Dspring.profiles.active=local \
  -jar target/mock-abis-1.4.0-SNAPSHOT.jar
```

Swagger: `http://localhost:8081/v1/mock-abis-service/swagger-ui/index.html`

### mock-mv (Spring Boot, port 8081)

```bash
cd mock-mv
java -Dspring.profiles.active=default \
  -Dspring.cloud.config.uri=http://localhost:51000/config \
  -jar target/mock-mv-1.4.0-SNAPSHOT.jar
```

Swagger: `http://localhost:8081/v1/mockmv/swagger-ui.html`

### MockMDS (standalone Java, ports 4501–4600)

After building, run from `MockMDS/target/`:

```bash
# Authentication devices (WSQ image format)
java -cp mock-mds-1.4.0-SNAPSHOT.jar;lib\* io.mosip.mock.sbi.test.TestMockSBI \
  "mosip.mock.sbi.device.purpose=Auth" \
  "mosip.mock.sbi.biometric.type=Biometric Device" \
  "mosip.mock.sbi.biometric.image.type=WSQ"

# Registration devices (JP2000 image format)
java -cp mock-mds-1.4.0-SNAPSHOT.jar;lib\* io.mosip.mock.sbi.test.TestMockSBI \
  "mosip.mock.sbi.device.purpose=Registration" \
  "mosip.mock.sbi.biometric.type=Biometric Device"
```

## Architecture Notes

### mock-abis

- **Queue flow**: `Listener` (JMS `MessageListener`) receives messages from ActiveMQ → dispatches to `ProxyAbisInsertService` (INSERT) or calls Identify logic → publishes response back to outbound queue.
- **Expectation system**: `ExpectationCache` (in-memory map) allows pre-configuring forced responses/errors for specific biometric hashes. Useful for testing error scenarios without real biometric data.
- **Biometric hash**: The expectation `id` field is `SHA256(base64_decode(bdb))` — hashing the ISO image, not the BDB field directly.
- **Encryption**: Partner-based encryption (`abis.bio.encryption=true`) requires `cbeff.p12` in the resources folder.
- **Database**: H2 in-memory by default; PostgreSQL is available via commented-out config in `bootstrap.properties`.
- **Spring profile `local`**: Disables auth adapter enforcement — all endpoints accessible without tokens (`application-local.properties`).
- **Partner certificates**: Stored under `~/files/keystore/` at runtime (resolved relative to `user.home`).

### mock-mv

- Same queue-driven pattern as mock-abis: `QueueListener` extends abstract `Listener` → `MockMvDecisionService` applies the configured decision (APPROVED/REJECTED) or a per-RID expectation override.
- No database. All state is in-memory.
- Reads queue config from Spring Cloud Config server; for local development, override `spring.cloud.config.uri`.

### MockMDS

- Not a Spring Boot application. Entry point is `TestMockSBI.main()` which starts `SBIMockService` — an embedded HTTP server listening on a port in the range `4501–4600`.
- Device helpers are split by modality and subtype: `SBIFaceHelper`, `SBIFingerSlapHelper`, `SBIFingerSingleHelper`, `SBIIrisDoubleHelper`, `SBIIrisSingleHelper`. Each reads biometric data from files under `Biometric Devices/{Modality}/`.
- Device certificates (`.p12` files) must be placed in `Biometric Devices/{Modality}/Keys/` before running.
- Admin endpoints (POST `/admin/status`, `/admin/score`, `/admin/delay`, `/admin/profile`) control mock behavior at runtime.

### mock-sdk

- Pure library; no HTTP layer. Implements `IBioApiV2` in `SampleSDKV2`.
- Used as a dependency inside `biosdk-services`. The class `io.mosip.mock.sdk.impl.SampleSDKV2` is configured via `biosdk_class` property in that service.
- Set `mosip.mocksdk.extraction=true` to return random biometric data of configurable size (for performance testing only).

## Key Configuration Files

| File | Purpose |
|------|---------|
| `mock-abis/src/main/resources/bootstrap.properties` | Port, datasource, ActiveMQ, Spring Cloud Config |
| `mock-abis/src/main/resources/application-local.properties` | Local dev flags (`local.development`, `abis.bio.encryption`) |
| `mock-abis/src/main/resources/registration-processor-abis-sample.json` | Template for ActiveMQ queue config (copy to `registration-processor-abis.json`) |
| `mock-mv/src/main/resources/bootstrap.properties` | Port, Spring Cloud Config URI |
| `MockMDS/src/main/resources/application.properties` | Port range, device file paths, cert aliases, auth server URL |

## CI / Publishing

GitHub Actions (`.github/workflows/push-trigger.yml`) builds each module independently using reusable workflows from `mosip/kattu`. GPG signing is required for publishing to Nexus — omit with `-Dgpg.skip=true` for local builds. Docker images are built only for `mock-abis` and `mock-mv`; MockMDS runs on-device (Windows/Android) and has no Dockerfile.