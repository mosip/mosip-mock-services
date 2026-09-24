@echo off
setlocal EnableExtensions EnableDelayedExpansion
REM Standalone Windows cmd runner for MockMDS Registration (no .ps1).
REM Linux / macOS / Git Bash: use run-local-reg.sh
REM
REM   run-local-reg.bat init | start | smoke | stop | test | all
REM Optional: JDK_JAVA_OPTIONS MIN_PORT MAX_PORT

set "MODULE_DIR=%~dp0"
if "%MODULE_DIR:~-1%"=="\" set "MODULE_DIR=%MODULE_DIR:~0,-1%"

set "LOCAL_DIR=%MODULE_DIR%\.local"
set "LOG_DIR=%LOCAL_DIR%\logs"
set "PID_DIR=%LOCAL_DIR%\pids"
set "TARGET_DIR=%MODULE_DIR%\target"
set "PID_FILE=%PID_DIR%\mock-mds-reg.pid"
set "PORT_FILE=%PID_DIR%\mock-mds-reg.port"
set "LOG_FILE=%LOG_DIR%\mock-mds-reg.log"
set "ERR_FILE=%LOG_DIR%\mock-mds-reg.err.log"
set "MODULE=mock-mds"
set "SERVICE=mock-mds-reg"
set "PURPOSE=Registration"
set "BIOMETRIC_TYPE=Biometric Device"
set "MAIN_CLASS=io.mosip.mock.sbi.test.TestMockSBI"
if not defined MIN_PORT set "MIN_PORT=4501"
if not defined MAX_PORT set "MAX_PORT=4600"

set "CMD=%~1"
if "%CMD%"=="" goto :usage
if /I "%CMD%"=="-h" goto :usage
if /I "%CMD%"=="--help" goto :usage
if /I "%CMD%"=="help" goto :usage
if /I "%CMD%"=="init" goto :init
if /I "%CMD%"=="test" goto :test
if /I "%CMD%"=="start" goto :start
if /I "%CMD%"=="stop" goto :stop
if /I "%CMD%"=="smoke" goto :smoke
if /I "%CMD%"=="all" goto :all

echo error: unknown command '%CMD%'
goto :usage

:usage
echo Local MockMDS - Registration ^(JP2000^), ports %MIN_PORT%-%MAX_PORT%
echo.
echo   run-local-reg.bat init     package this module
echo   run-local-reg.bat start    run from target/ and wait until SBI is ready
echo   run-local-reg.bat smoke    POST /admin/status on the bound port
echo   run-local-reg.bat stop
echo   run-local-reg.bat test     Maven tests
echo   run-local-reg.bat all      init + test + start + smoke
echo.
echo Optional: JDK_JAVA_OPTIONS MIN_PORT MAX_PORT
echo.
call :print_endpoints
exit /b 1

:ensure_dirs
if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"
if not exist "%PID_DIR%" mkdir "%PID_DIR%"
exit /b 0

:check_prereqs
where java >nul 2>&1
if errorlevel 1 (
  echo error: java is required on PATH
  exit /b 1
)
where mvn >nul 2>&1
if errorlevel 1 (
  echo error: mvn is required on PATH
  exit /b 1
)
exit /b 0

:print_endpoints
set "DISP_PORT=?"
if exist "%PORT_FILE%" set /p DISP_PORT=<"%PORT_FILE%"
echo.
echo %SERVICE%  purpose=%PURPOSE%  image=JP2000  port=!DISP_PORT!
echo   range      %MIN_PORT%-%MAX_PORT% ^(first free; see application.properties^)
if not "!DISP_PORT!"=="?" if defined DISP_PORT (
  echo   base       http://127.0.0.1:!DISP_PORT!/
  echo   admin      POST http://127.0.0.1:!DISP_PORT!/admin/status^|score^|delay^|profile
  echo   swagger    http://127.0.0.1:!DISP_PORT!/swagger-ui/index.html
  echo   openapi    http://127.0.0.1:!DISP_PORT!/v3/api-docs
)
echo   log        %LOG_FILE%
echo.
exit /b 0

:find_jar
set "APP_JAR="
set "APP_JAR_NAME="
if not exist "%TARGET_DIR%" (
  echo error: not packaged. Run: %~nx0 init
  exit /b 1
)
if not exist "%TARGET_DIR%\lib" (
  echo error: %TARGET_DIR%\lib missing. Run: %~nx0 init
  exit /b 1
)
for %%F in ("%TARGET_DIR%\%MODULE%-*.jar") do (
  set "CAND=%%~nxF"
  echo !CAND! | findstr /I /C:"sources" /C:"javadoc" >nul
  if errorlevel 1 (
    set "APP_JAR=%%~fF"
    set "APP_JAR_NAME=%%~nxF"
    goto :find_jar_done
  )
)
:find_jar_done
if not defined APP_JAR (
  echo error: no %MODULE%-*.jar in target. Run: %~nx0 init
  exit /b 1
)
exit /b 0

:is_running
if not exist "%PID_FILE%" exit /b 1
set /p CHECK_PID=<"%PID_FILE%"
if not defined CHECK_PID exit /b 1
tasklist /FI "PID eq !CHECK_PID!" 2>nul | findstr /I "!CHECK_PID!" >nul
if errorlevel 1 exit /b 1
exit /b 0

:read_port_from_log
set "PARSED_PORT="
if not exist "%LOG_FILE%" exit /b 0
for /f "tokens=*" %%L in ('findstr /C:"SBI Proxy Service started on port" "%LOG_FILE%" 2^>nul') do (
  for %%P in (%%L) do set "PARSED_PORT=%%P"
)
exit /b 0

:init
call :check_prereqs
if errorlevel 1 exit /b 1
echo ==^> packaging %MODULE% ^(skip tests^)
pushd "%MODULE_DIR%"
call mvn clean package -DskipTests "-Dgpg.skip=true" "-Dmaven.javadoc.skip=true"
set "RC=%ERRORLEVEL%"
popd
if not "%RC%"=="0" exit /b %RC%
echo init complete
exit /b 0

:test
call :check_prereqs
if errorlevel 1 exit /b 1
echo ==^> maven tests
pushd "%MODULE_DIR%"
call mvn test "-Dgpg.skip=true" "-Dmaven.javadoc.skip=true"
set "RC=%ERRORLEVEL%"
popd
exit /b %RC%

:start
echo.
echo ==^> run-local-reg start
call :ensure_dirs
call :check_prereqs
if errorlevel 1 exit /b 1
call :is_running
if not errorlevel 1 (
  set /p OLD_PID=<"%PID_FILE%"
  echo %SERVICE% already running ^(pid !OLD_PID!^)
  call :print_endpoints
  exit /b 0
)
if exist "%PID_FILE%" del /q "%PID_FILE%" >nul 2>&1
if exist "%PORT_FILE%" del /q "%PORT_FILE%" >nul 2>&1
if exist "%LOG_FILE%" del /q "%LOG_FILE%" >nul 2>&1
if exist "%ERR_FILE%" del /q "%ERR_FILE%" >nul 2>&1

call :find_jar
if errorlevel 1 exit /b 1

echo ==^> starting %SERVICE% from !APP_JAR_NAME! ^(cwd=%TARGET_DIR%^)
echo     purpose=%PURPOSE%
echo     biometric.type=%BIOMETRIC_TYPE%
echo     image.type=JP2000 ^(default^)
call :print_endpoints

set "LAUNCH_PS1=%LOCAL_DIR%\launch-mock-mds-reg.ps1"
REM Match original argv (quoted Biometric Device). Extra quotes needed for Start-Process.
> "%LAUNCH_PS1%" echo $ErrorActionPreference = 'Stop'
>> "%LAUNCH_PS1%" echo $argList = New-Object System.Collections.Generic.List[string]
>> "%LAUNCH_PS1%" echo $argList.Add^('-Dfile.encoding=UTF-8'^)
>> "%LAUNCH_PS1%" echo $argList.Add^('-cp'^)
>> "%LAUNCH_PS1%" echo $argList.Add^('!APP_JAR_NAME!;lib\*'^)
>> "%LAUNCH_PS1%" echo $argList.Add^('%MAIN_CLASS%'^)
>> "%LAUNCH_PS1%" echo $argList.Add^('mosip.mock.sbi.device.purpose=%PURPOSE%'^)
>> "%LAUNCH_PS1%" echo $argList.Add^('"mosip.mock.sbi.biometric.type=%BIOMETRIC_TYPE%"'^)
>> "%LAUNCH_PS1%" echo $p = Start-Process -FilePath 'java' -ArgumentList $argList.ToArray^(^) -WorkingDirectory '%TARGET_DIR%' -RedirectStandardOutput '%LOG_FILE%' -RedirectStandardError '%ERR_FILE%' -WindowStyle Hidden -PassThru
>> "%LAUNCH_PS1%" echo Set-Content -LiteralPath '%PID_FILE%' -Value $p.Id -Encoding ascii
powershell -NoProfile -ExecutionPolicy Bypass -File "%LAUNCH_PS1%"
if errorlevel 1 (
  echo error: failed to start java process
  exit /b 1
)

echo ==^> waiting for SBI Proxy Service ^(up to 60s^) ...
call :wait_ready 60
if errorlevel 1 exit /b 1

if exist "%PID_FILE%" (
  set /p STARTED_PID=<"%PID_FILE%"
  echo pid !STARTED_PID!  log %LOG_FILE%
)
echo.
echo %SERVICE% ready
call :print_endpoints
exit /b 0

:wait_ready
set /a WAIT_MAX=%~1
if "!WAIT_MAX!"=="" set /a WAIT_MAX=60
set /a WAIT_ELAPSED=0
:wait_ready_loop
if !WAIT_ELAPSED! GEQ !WAIT_MAX! goto :wait_ready_fail

call :is_running
if errorlevel 1 (
  echo error: process exited during startup. See %LOG_FILE%
  if exist "%LOG_FILE%" type "%LOG_FILE%"
  exit /b 1
)

call :read_port_from_log
if defined PARSED_PORT (
  > "%PORT_FILE%" echo !PARSED_PORT!
  exit /b 0
)

if exist "%LOG_FILE%" (
  findstr /C:"no port available" /C:"Cannot open port" /C:"Please check" "%LOG_FILE%" >nul 2>&1
  if not errorlevel 1 (
    echo error: startup failed. See %LOG_FILE%
    type "%LOG_FILE%"
    exit /b 1
  )
)

echo     ... still starting ^(!WAIT_ELAPSED!s / !WAIT_MAX!s^)
ping -n 3 127.0.0.1 >nul
set /a WAIT_ELAPSED+=2
goto :wait_ready_loop

:wait_ready_fail
echo error: MockMDS did not become ready within !WAIT_MAX!s
if exist "%LOG_FILE%" type "%LOG_FILE%"
exit /b 1

:stop
call :ensure_dirs
set "STOP_PID="
if exist "%PID_FILE%" set /p STOP_PID=<"%PID_FILE%"
if not defined STOP_PID (
  echo %SERVICE% is not running
  exit /b 0
)
echo ==^> stopping %SERVICE% ^(%STOP_PID%^)
taskkill /PID %STOP_PID% /T /F >nul 2>&1
del /q "%PID_FILE%" >nul 2>&1
del /q "%PORT_FILE%" >nul 2>&1
echo stopped.
exit /b 0

:http_ok
set "HTTP_CODE=000"
set "HTTP_URL=%~1"
set "HTTP_JSON_FILE=%~2"
set "HTTP_CODE_FILE=%LOCAL_DIR%\http-code-reg.txt"
if "%HTTP_JSON_FILE%"=="" (
  curl.exe -sS -o NUL -w "%%{http_code}" --connect-timeout 1 --max-time 2 "%HTTP_URL%" > "%HTTP_CODE_FILE%" 2>nul
) else (
  curl.exe -sS -o NUL -w "%%{http_code}" --connect-timeout 1 --max-time 2 -X POST -H "Content-Type: application/json" --data-binary "@%HTTP_JSON_FILE%" "%HTTP_URL%" > "%HTTP_CODE_FILE%" 2>nul
)
if exist "%HTTP_CODE_FILE%" (
  for /f "usebackq delims=" %%C in ("%HTTP_CODE_FILE%") do set "HTTP_CODE=%%C"
  del /q "%HTTP_CODE_FILE%" >nul 2>&1
)
if not defined HTTP_CODE set "HTTP_CODE=000"
if "!HTTP_CODE!"=="" set "HTTP_CODE=000"
exit /b 0

:port_listening
set "PL_PORT=%~1"
powershell -NoProfile -Command "try { $c=New-Object Net.Sockets.TcpClient; $c.ReceiveTimeout=1000; $c.SendTimeout=1000; $c.Connect('127.0.0.1',%PL_PORT%); $c.Close(); exit 0 } catch { exit 1 }" >nul 2>&1
exit /b %ERRORLEVEL%

:smoke
call :ensure_dirs
echo ==^> waiting for %SERVICE% ^(up to 60s^)
set /a ELAPSED=0
set "SMOKE_JSON=%LOCAL_DIR%\smoke-status-reg.json"
> "%SMOKE_JSON%" echo {"type":"Biometric Device","deviceStatus":"Ready"}
:smoke_loop
if %ELAPSED% GEQ 60 goto :smoke_fail
if exist "%PID_FILE%" (
  call :is_running
  if errorlevel 1 (
    echo error: process exited. See %LOG_FILE%
    if exist "%LOG_FILE%" type "%LOG_FILE%"
    exit /b 1
  )
)
set "SMOKE_PORT="
if exist "%PORT_FILE%" set /p SMOKE_PORT=<"%PORT_FILE%"
if not defined SMOKE_PORT (
  call :read_port_from_log
  if defined PARSED_PORT (
    set "SMOKE_PORT=!PARSED_PORT!"
    > "%PORT_FILE%" echo !PARSED_PORT!
  )
)
if defined SMOKE_PORT (
  call :http_ok "http://127.0.0.1:!SMOKE_PORT!/admin/status" "!SMOKE_JSON!"
  echo admin/status  http://127.0.0.1:!SMOKE_PORT!/admin/status  HTTP !HTTP_CODE!
  if not "!HTTP_CODE!"=="000" (
    echo smoke ok ^(port !SMOKE_PORT!^)
    call :print_endpoints
    exit /b 0
  )
  rem SBI admin may not return normal HTTP; accept TCP listen or live PID
  call :port_listening !SMOKE_PORT!
  if not errorlevel 1 (
    echo smoke ok ^(port !SMOKE_PORT! listening^)
    call :print_endpoints
    exit /b 0
  )
  call :is_running
  if not errorlevel 1 (
    echo smoke ok ^(port !SMOKE_PORT! / process alive^)
    call :print_endpoints
    exit /b 0
  )
)
ping -n 3 127.0.0.1 >nul
set /a ELAPSED+=2
goto :smoke_loop

:smoke_fail
echo error: not healthy. See %LOG_FILE%
if exist "%LOG_FILE%" type "%LOG_FILE%"
exit /b 1

:all
echo ==^> all: init + test + start + smoke
call :init
if errorlevel 1 exit /b 1
call :test
if errorlevel 1 exit /b 1
call :start
if errorlevel 1 exit /b 1
call :smoke
exit /b %ERRORLEVEL%
