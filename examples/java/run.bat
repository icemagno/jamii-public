@echo off
echo [%DATE% %TIME%] Iniciando compilacao do exemplo (mvn clean package)...
cd /d "%~dp0"
call mvn clean package
if %ERRORLEVEL% neq 0 (
    echo.
    echo [ERRO] Falha ao compilar a aplicacao.
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo [%DATE% %TIME%] Compilado com sucesso! Iniciando a aplicacao na porta 8080...
echo Para parar a aplicacao, feche esta janela ou pressione Ctrl+C.
echo.
java -jar target/jamii-sdk-usage-example-0.1.jar
pause
