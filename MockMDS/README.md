# MockMDS

Standalone **MOSIP Device Service (SBI)** mock for local Registration Client and Auth testing.
Not a Spring Boot deployable — runs as a classpath Java app on ports **4501–4600**.

| | |
|---|---|
| **JDK** | 21 |
| **Maven** | 3.9+ |
| **Module** | `mock-mds` `1.4.1-SNAPSHOT` |
| **Entry** | `io.mosip.mock.sbi.test.TestMockSBI` |
| **Ports** | `4501`–`4600` (first free; `application.properties`) |
| **Spec** | [SBI / Device Provider](https://docs.mosip.io/1.2.0/modules/partner-management-services/pms-existing/device-provider-partner#sbi-secure-biometric-interface) |
| **License** | [MPL 2.0](../LICENSE) |

---

## What it does

| Feature | Detail |
|---------|--------|
| SBI endpoints | Device discovery, info, capture / rcapture, stream |
| Dual purpose | **Registration** (JP2000) and **Auth** (WSQ) |
| Admin controls | Status, quality score, delay, profile |
| Data | File-based biometrics under `Biometric Devices/` and `Profile/` |

---

## Prerequisites

- **JDK 21**, **Maven 3.9+**
- Local SNAPSHOTs (or your snapshot repo):
  - `commons/kernel` → `kernel-core` **1.4.1-SNAPSHOT**
  - `bio-utils` → `biometrics-util` **1.4.1-SNAPSHOT**
- Keystores under `Biometric Devices/{modality}/Keys/` (`device-partner.p12`, `ftm-partner.p12`, etc.)
- Optional: Postman for SBI / admin APIs

---

## Build

From this module:

```bash
cd MockMDS
mvn clean install "-Dgpg.skip=true"
```

| Goal | Command |
|------|---------|
| Skip tests | `mvn clean package -DskipTests "-Dgpg.skip=true"` |
| Package via helper | `run-local-reg.bat init` / `./run-local-reg.sh init` |

After package, `target/` contains:

| Path | Role |
|------|------|
| `mock-mds-*.jar` | App JAR |
| `lib/` | Runtime dependencies |
| `Biometric Devices/`, `Profile/`, `files/` | Device data & samples |
| `application.properties` | Ports, paths, auth URLs |

---

## Local setup (recommended)

Same helper style as [`kernel-bio-converter`](https://github.com/mosip/converters/tree/master/kernel-bio-converter):
`init` · `start` · `smoke` · `stop` · `test` · `all`.

| | |
|---|---|
| Working directory | **`MockMDS/`** |
| Logs / PIDs | **`.local/`** |
| Java cwd | **`target/`** (required for relative device paths) |

### Helper scripts

| Mode | Windows cmd | Linux / macOS / Git Bash |
|------|-------------|--------------------------|
| Registration (JP2000) | `run-local-reg.bat` | `./run-local-reg.sh` |
| Auth (WSQ) | `run-local-auth.bat` | `./run-local-auth.sh` |

### Commands

| Command | What it does |
|---------|----------------|
| `init` | Maven `clean package` (skip tests) |
| `start` | Start MockMDS, wait until `SBI Proxy Service started on port …` |
| `smoke` | `POST /admin/status` on the bound port |
| `stop` | Stop the process (frees JAR for rebuild) |
| `test` | Maven unit tests |
| `all` | `init` + `test` + `start` + `smoke` |
| _(no args)_ / `help` | Usage + endpoints |

### Windows (cmd)

```bat
cd MockMDS
run-local-reg.bat init
run-local-reg.bat start
run-local-reg.bat smoke
```

Auth:

```bat
run-local-auth.bat init
run-local-auth.bat start
run-local-auth.bat smoke
```

```bat
run-local-reg.bat stop
run-local-auth.bat stop
run-local-reg.bat all
```

### Windows PowerShell

```powershell
cd MockMDS
.\run-local-reg.bat init
.\run-local-reg.bat start
.\run-local-reg.bat smoke
.\run-local-auth.bat start
```

### Linux / macOS / WSL / Git Bash

```bash
cd MockMDS
chmod +x run-local-reg.sh run-local-auth.sh
./run-local-reg.sh init
./run-local-reg.sh start
./run-local-reg.sh smoke
```

```bash
./run-local-auth.sh start
./run-local-reg.sh stop
./run-local-auth.sh all
```

### Environment variables

| Variable | Default | Meaning |
|----------|---------|---------|
| `JDK_JAVA_OPTIONS` | _(empty)_ | Extra JVM options |
| `MIN_PORT` / `MAX_PORT` | `4501` / `4600` | Display only (real range is `application.properties`) |

Reg and Auth use **separate** PID/log files under `.local/`, so both can run (each takes the next free port in range).

---

## Configuration

Edit **`application.properties`** (source and the copy under `target/` after package):

| Key area | Examples |
|----------|----------|
| Ports | `server.minport=4501`, `server.maxport=4600` |
| Bind address | `server.serveripaddress=127.0.0.1` |
| Keystores | `mosip.mock.sbi.file.{modality}.keys.*` |
| Auth / IDA (optional) | `mosip.auth.server.url`, `mosip.ida.server.url` |

Also review device JSON under `Biometric Devices/{Face|Finger|Iris}/` (`DeviceDiscovery.json`, `DeviceInfo.json`, `DigitalId.json`).

Place partner keystores in:

```text
Biometric Devices/{modality}/Keys/device-partner.p12
Biometric Devices/{modality}/Keys/ftm-partner.p12
```

---

## Manual launch (optional)

Prefer `run-local-reg` / `run-local-auth` above. To start Java directly from **`target/`** after build (same quoting as the original launcher):

### Registration (JP2000)

Windows (cmd), from `target/`:

```bat
java -cp mock-mds-1.4.1-SNAPSHOT.jar;lib\* io.mosip.mock.sbi.test.TestMockSBI ^
  "mosip.mock.sbi.device.purpose=Registration" ^
  "mosip.mock.sbi.biometric.type=Biometric Device"
```

Linux / macOS / Git Bash, from `target/`:

```bash
java -cp "mock-mds-1.4.1-SNAPSHOT.jar:lib/*" io.mosip.mock.sbi.test.TestMockSBI \
  "mosip.mock.sbi.device.purpose=Registration" \
  "mosip.mock.sbi.biometric.type=Biometric Device"
```

### Auth (WSQ)

Windows (cmd), from `target/`:

```bat
java -cp mock-mds-1.4.1-SNAPSHOT.jar;lib\* io.mosip.mock.sbi.test.TestMockSBI ^
  "mosip.mock.sbi.device.purpose=Auth" ^
  "mosip.mock.sbi.biometric.type=Biometric Device" ^
  "mosip.mock.sbi.biometric.image.type=WSQ"
```

Linux / macOS / Git Bash, from `target/`:

```bash
java -cp "mock-mds-1.4.1-SNAPSHOT.jar:lib/*" io.mosip.mock.sbi.test.TestMockSBI \
  "mosip.mock.sbi.device.purpose=Auth" \
  "mosip.mock.sbi.biometric.type=Biometric Device" \
  "mosip.mock.sbi.biometric.image.type=WSQ"
```

The quotes around `Biometric Device` are required — without them the value splits into two arguments and startup fails.


## Admin APIs

Base URL: `http://127.0.0.1:{port}/` (port printed by `start` / stored in `.local/pids/mock-mds-*.port`).

| Endpoint | Method | Body example |
|----------|--------|--------------|
| `/admin/status` | POST | `{"type":"Biometric Device","deviceStatus":"Ready"}` |
| `/admin/score` | POST | `{"type":"Biometric Device","qualityScore":"44.44","fromIso":false}` |
| `/admin/delay` | POST | `{"type":"Biometric Device","delay":"10000","method":["RCAPTURE"]}` |
| `/admin/profile` | POST | `{"type":"Biometric Device","profileId":"Profile1"}` |

### Swagger UI (admin only)

Static OpenAPI + Swagger UI (no springdoc). Served by MockMDS after `init` copies `swagger-ui/` → `target/swagger-ui/`.

| URL | |
|-----|---|
| Swagger UI | `http://127.0.0.1:{port}/swagger-ui/index.html` |
| OpenAPI | `http://127.0.0.1:{port}/v3/api-docs` |

Use the **bound** `{port}` from `start` / `.local/pids/*.port` (first free in 4501–4600). Do not hardcode `4501` if another process already holds it — open Swagger on the printed port; `/v3/api-docs` rewrites `servers.url` to that host:port so Try it out hits the same instance.

UI assets load Swagger UI 5.32.14 from jsDelivr (needs network for the browser).
`deviceStatus`: `Ready` · `Busy` · `Not Ready` · `Not Registered`  
`profileId`: `Default` · `Profile1` · `Profile2` · …  
`method`: `RCAPTURE` · `CAPTURE` · `MOSIPDINFO` · `MOSIPDISC` · `STREAM`

---

## Deployment

MockMDS is meant to run on the **client machine** (Windows / Android host), not as a cluster service. There is no standard production Dockerfile in this module.

---

## Docs & community

- [MDS / SBI specification](https://docs.mosip.io/1.1.5/biometrics/mosip-device-service-specification)
- [Code contributions](https://docs.mosip.io/1.2.0/community/code-contributions)
- [MOSIP Community](https://community.mosip.io/)
- [GitHub Issues](https://github.com/mosip/mosip-mock-services/issues)

---

## License

![License: MPL 2.0](https://img.shields.io/badge/License-MPL_2.0-brightgreen.svg)

Licensed under [Mozilla Public License 2.0](../LICENSE). See [NOTICE](../NOTICE) for third-party attributions.
