@rem
@rem Copyright 2015 the original author or authors.
@rem
@rem Licensed under the Apache License, Version 2.0 (the "License");
@rem you may not use this file except in compliance with the License.
@rem You may obtain a copy of the License at
@rem
@rem      https://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing, software
@rem distributed under the License is distributed on an "AS IS" BASIS,
@rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem See the License for the specific language governing permissions and
@rem limitations under the License.
@rem

@if "%DEBUG%"=="" @echo off
@rem ##########################################################################
@rem
@rem  Project_DS_Giacomello_Vecellio startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.
@rem This is normally unused
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%..

@rem Resolve any "." and ".." in APP_HOME to make it shorter.
for %%i in ("%APP_HOME%") do set APP_HOME=%%~fi

@rem Add default JVM options here. You can also use JAVA_OPTS and PROJECT_DS_GIACOMELLO_VECELLIO_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS=

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if %ERRORLEVEL% equ 0 goto execute

echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto execute

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:execute
@rem Setup the command line

set CLASSPATH=%APP_HOME%\lib\Project_DS_Giacomello_Vecellio.jar;%APP_HOME%\lib\akka-projection-core_2.13-1.4.2.jar;%APP_HOME%\lib\akka-persistence-query_2.13-2.8.1.jar;%APP_HOME%\lib\akka-persistence_2.13-2.8.1.jar;%APP_HOME%\lib\akka-stream_2.13-2.8.1.jar;%APP_HOME%\lib\akka-actor-typed_2.13-2.8.1.jar;%APP_HOME%\lib\akka-slf4j_2.13-2.8.1.jar;%APP_HOME%\lib\akka-actor_2.13-2.8.1.jar;%APP_HOME%\lib\ssl-config-core_2.13-0.6.1.jar;%APP_HOME%\lib\scala-java8-compat_2.13-1.0.0.jar;%APP_HOME%\lib\scala-library-2.13.10.jar;%APP_HOME%\lib\akka-protobuf-v3_2.13-2.8.1.jar;%APP_HOME%\lib\reactive-streams-1.0.4.jar;%APP_HOME%\lib\slf4j-api-1.7.36.jar;%APP_HOME%\lib\config-1.4.2.jar


@rem Execute Project_DS_Giacomello_Vecellio
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %PROJECT_DS_GIACOMELLO_VECELLIO_OPTS%  -classpath "%CLASSPATH%" DHTSystem %*

:end
@rem End local scope for the variables with windows NT shell
if %ERRORLEVEL% equ 0 goto mainEnd

:fail
rem Set variable PROJECT_DS_GIACOMELLO_VECELLIO_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd.exe /c_ return code!
set EXIT_CODE=%ERRORLEVEL%
if %EXIT_CODE% equ 0 set EXIT_CODE=1
if not ""=="%PROJECT_DS_GIACOMELLO_VECELLIO_EXIT_CONSOLE%" exit %EXIT_CODE%
exit /b %EXIT_CODE%

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
