#!/usr/bin/env bash
# MockMDS Registration local runner — Linux, macOS, Windows Git Bash/MSYS.
# Windows cmd: use run-local-reg.bat
#
#   ./run-local-reg.sh init | start | smoke | stop | test | all
# Optional: JDK_JAVA_OPTIONS
set -euo pipefail

MODULE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LOCAL_DIR="${MODULE_DIR}/.local"
LOG_DIR="${LOCAL_DIR}/logs"
PID_DIR="${LOCAL_DIR}/pids"
TARGET_DIR="${MODULE_DIR}/target"
PID_FILE="${PID_DIR}/mock-mds-reg.pid"
PORT_FILE="${PID_DIR}/mock-mds-reg.port"
LOG_FILE="${LOG_DIR}/mock-mds-reg.log"
MODULE="mock-mds"
SERVICE="mock-mds-reg"
PURPOSE="Registration"
BIOMETRIC_TYPE="Biometric Device"
MAIN_CLASS="io.mosip.mock.sbi.test.TestMockSBI"
UNAME_S="$(uname -s 2>/dev/null || echo unknown)"
MIN_PORT="${MIN_PORT:-4501}"
MAX_PORT="${MAX_PORT:-4600}"

MVN_SKIP=(
  "-DskipTests"
  "-Dgpg.skip=true"
  "-Dmaven.javadoc.skip=true"
)

usage() {
  cat <<EOF
Local MockMDS — Registration (JP2000), ports ${MIN_PORT}–${MAX_PORT}

  Linux / macOS / Git Bash:
    ./run-local-reg.sh init | start | smoke | stop | test | all

  Windows cmd:
    run-local-reg.bat init | start | smoke | stop | test | all

Optional: JDK_JAVA_OPTIONS MIN_PORT MAX_PORT

  all = init + test + start + smoke
EOF
  print_endpoints
  exit "${1:-0}"
}

need_cmd() {
  command -v "$1" >/dev/null 2>&1 || {
    echo "error: '$1' is required on PATH" >&2
    exit 1
  }
}

ensure_dirs() {
  mkdir -p "$LOG_DIR" "$PID_DIR"
}

print_endpoints() {
  local port="?"
  [[ -f "$PORT_FILE" ]] && port="$(cat "$PORT_FILE" 2>/dev/null || echo "?")"
  echo
  echo "${SERVICE}  purpose=${PURPOSE}  image=JP2000  port=${port}"
  echo "  range      ${MIN_PORT}–${MAX_PORT} (first free; see application.properties)"
  if [[ "$port" != "?" && -n "$port" ]]; then
    echo "  base       http://127.0.0.1:${port}/"
    echo "  admin      POST http://127.0.0.1:${port}/admin/status|score|delay|profile"
    echo "  swagger    http://127.0.0.1:${port}/swagger-ui/index.html"
    echo "  openapi    http://127.0.0.1:${port}/v3/api-docs"
  fi
  echo "  log        ${LOG_FILE}"
  echo
}

check_prereqs() {
  need_cmd java
  need_cmd mvn
  echo "os: ${UNAME_S}"
  local ver
  ver="$(java -version 2>&1 | head -n 1 || true)"
  echo "java: $ver"
  if ! echo "$ver" | grep -E '"21[\. "]' >/dev/null 2>&1; then
    echo "warn: JDK 21 is required. Continuing anyway." >&2
  fi
}

mvn_mod() {
  (
    cd "$MODULE_DIR"
    mvn "$@"
  )
}

find_app_jar() {
  local jar
  if [[ ! -d "$TARGET_DIR" ]]; then
    echo "error: not packaged. Run: $0 init" >&2
    exit 1
  fi
  if [[ ! -d "${TARGET_DIR}/lib" ]]; then
    echo "error: ${TARGET_DIR}/lib missing. Run: $0 init" >&2
    exit 1
  fi
  for jar in "$TARGET_DIR"/${MODULE}-*.jar; do
    [[ -f "$jar" ]] || continue
    case "$jar" in
      *sources*|*javadoc*) continue ;;
    esac
    echo "$jar"
    return 0
  done
  echo "error: no ${MODULE}-*.jar in ${TARGET_DIR}. Run: $0 init" >&2
  exit 1
}

is_running() {
  [[ -f "$PID_FILE" ]] || return 1
  kill -0 "$(cat "$PID_FILE")" 2>/dev/null
}

read_port_from_log() {
  local port=""
  if [[ -f "$LOG_FILE" ]]; then
    port="$(grep -Eo 'SBI Proxy Service started on port [0-9]+' "$LOG_FILE" 2>/dev/null | tail -n 1 | grep -Eo '[0-9]+$' || true)"
  fi
  printf '%s' "$port"
}

port_listening() {
  local port="$1"
  if command -v python3 >/dev/null 2>&1; then
    python3 -c 'import socket,sys
s=socket.socket(); s.settimeout(1)
try:
  s.connect(("127.0.0.1", int(sys.argv[1]))); sys.exit(0)
except Exception:
  sys.exit(1)
finally:
  s.close()' "$port" >/dev/null 2>&1
    return $?
  fi
  if command -v curl >/dev/null 2>&1; then
    curl -sS -o /dev/null --connect-timeout 1 --max-time 2 "http://127.0.0.1:${port}/" >/dev/null 2>&1
    return $?
  fi
  return 1
}

cmd_init() {
  check_prereqs
  echo "==> packaging ${MODULE} (skip tests)"
  mvn_mod clean package "${MVN_SKIP[@]}"
  echo "init complete"
}

cmd_test() {
  check_prereqs
  echo "==> maven tests"
  mvn_mod test "-Dgpg.skip=true" "-Dmaven.javadoc.skip=true"
}

cmd_start() {
  echo
  echo "==> run-local-reg start"
  ensure_dirs
  check_prereqs
  if is_running; then
    echo "${SERVICE} already running (pid $(cat "$PID_FILE"))"
    print_endpoints
    return 0
  fi
  rm -f "$LOG_FILE" "$PORT_FILE"
  local jar jar_name
  jar="$(find_app_jar)"
  jar_name="$(basename "$jar")"
  echo "==> starting ${SERVICE} from ${jar_name} (cwd=${TARGET_DIR})"
  echo "    purpose=${PURPOSE}"
  echo "    biometric.type=${BIOMETRIC_TYPE}"
  echo "    image.type=JP2000 (default)"
  print_endpoints

  # Paths relative to target/: Biometric Devices, Profile, application.properties, lib/
  local java_args=()
  if [[ -n "${JDK_JAVA_OPTIONS:-}" ]]; then
    # shellcheck disable=SC2206
    java_args+=(${JDK_JAVA_OPTIONS})
  fi
  java_args+=(
    "-Dfile.encoding=UTF-8"
    -cp "${jar_name}:lib/*"
    "$MAIN_CLASS"
    "mosip.mock.sbi.device.purpose=${PURPOSE}"
    "mosip.mock.sbi.biometric.type=${BIOMETRIC_TYPE}"
  )
  (
    cd "$TARGET_DIR"
    nohup java "${java_args[@]}" >"$LOG_FILE" 2>&1 &
    echo $! >"$PID_FILE"
  )
  echo "pid $(cat "$PID_FILE")  log ${LOG_FILE}"
  echo "==> waiting for SBI Proxy Service (up to 60s) ..."
  wait_ready 60
  echo
  echo "${SERVICE} ready"
  print_endpoints
}

wait_ready() {
  local timeout="${1:-60}"
  local elapsed=0
  local port
  while [[ "$elapsed" -lt "$timeout" ]]; do
    if ! is_running; then
      echo "error: process exited during startup. See ${LOG_FILE}" >&2
      tail -n 80 "$LOG_FILE" >&2 || true
      return 1
    fi
    port="$(read_port_from_log)"
    if [[ -n "$port" ]]; then
      echo "$port" >"$PORT_FILE"
      return 0
    fi
    if grep -qiE 'error|Exception|no port available' "$LOG_FILE" 2>/dev/null \
      && ! grep -q 'SBI Proxy Service started on port' "$LOG_FILE" 2>/dev/null; then
      # allow early INFO noise; fail only if process died (handled above) or hard fail lines
      if grep -q 'no port available\|Cannot open port\|Please check' "$LOG_FILE" 2>/dev/null; then
        echo "error: startup failed. See ${LOG_FILE}" >&2
        tail -n 80 "$LOG_FILE" >&2 || true
        return 1
      fi
    fi
    echo "    ... still starting (${elapsed}s / ${timeout}s)"
    sleep 2
    elapsed=$((elapsed + 2))
  done
  echo "error: MockMDS did not become ready within ${timeout}s" >&2
  tail -n 80 "$LOG_FILE" >&2 || true
  return 1
}

http_post_admin_status() {
  local port="$1"
  local url="http://127.0.0.1:${port}/admin/status"
  local body='{"type":"Biometric Device","deviceStatus":"Ready"}'
  if command -v curl >/dev/null 2>&1; then
    curl -sS -o /dev/null -w "%{http_code}" --connect-timeout 2 --max-time 5 \
      -X POST -H "Content-Type: application/json" -d "$body" "$url" 2>/dev/null || echo "000"
    return 0
  fi
  echo "000"
}

cmd_stop() {
  ensure_dirs
  if ! is_running; then
    rm -f "$PID_FILE" "$PORT_FILE"
    echo "${SERVICE} is not running"
    return 0
  fi
  local pid
  pid="$(cat "$PID_FILE")"
  echo "==> stopping ${SERVICE} (${pid})"
  kill "$pid" 2>/dev/null || true
  local i
  for i in 1 2 3 4 5 6 7 8 9 10; do
    kill -0 "$pid" 2>/dev/null || break
    sleep 1
  done
  if kill -0 "$pid" 2>/dev/null; then
    kill -9 "$pid" 2>/dev/null || true
  fi
  rm -f "$PID_FILE" "$PORT_FILE"
  echo "stopped."
}

cmd_smoke() {
  ensure_dirs
  local timeout="${1:-60}"
  local elapsed=0
  local port
  echo "==> waiting for ${SERVICE} (up to ${timeout}s)"
  while [[ "$elapsed" -lt "$timeout" ]]; do
    if [[ -f "$PID_FILE" ]] && ! is_running; then
      echo "error: process exited. See ${LOG_FILE}" >&2
      tail -n 40 "$LOG_FILE" >&2 || true
      return 1
    fi
    port="$(cat "$PORT_FILE" 2>/dev/null || true)"
    [[ -z "$port" ]] && port="$(read_port_from_log)"
    if [[ -n "$port" ]]; then
      echo "$port" >"$PORT_FILE"
      local code
      code="$(http_post_admin_status "$port")"
      echo "admin/status  http://127.0.0.1:${port}/admin/status  HTTP ${code}"
      # SBI may not return standard 200; accept any non-000 connect + process alive
      if is_running && [[ "$code" != "000" ]]; then
        echo "smoke ok (port ${port})"
        print_endpoints
        return 0
      fi
      if is_running && port_listening "$port"; then
        echo "smoke ok (port ${port} listening)"
        print_endpoints
        return 0
      fi
    fi
    sleep 2
    elapsed=$((elapsed + 2))
  done
  echo "error: not healthy. See ${LOG_FILE}" >&2
  tail -n 80 "$LOG_FILE" >&2 || true
  return 1
}

cmd_all() {
  echo "==> all: init + test + start + smoke"
  cmd_init
  cmd_test
  cmd_start
  cmd_smoke
}

main() {
  local cmd="${1:-}"
  shift || true
  case "$cmd" in
    -h|--help|help) usage 0 ;;
    init) cmd_init ;;
    test) cmd_test ;;
    start) cmd_start ;;
    stop) cmd_stop ;;
    smoke) cmd_smoke "${1:-60}" ;;
    all) cmd_all ;;
    "") usage 1 ;;
    *) echo "error: unknown command '$cmd'" >&2; usage 1 ;;
  esac
}

main "$@"
