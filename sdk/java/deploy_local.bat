@echo off
echo [%DATE% %TIME%] Iniciando build e instalacao do Jamii Java SDK no repositorio Maven local...
cd /d "%~dp0"
call mvn clean install
if %ERRORLEVEL% neq 0 (
    echo.
    echo [ERRO] Falha ao instalar o SDK no repositorio local.
    pause
    exit /b %ERRORLEVEL%
)
echo.
echo [SUCESSO] Jamii Java SDK instalado com sucesso no repositorio Maven local (~/.m2/repository).
pause
