@echo off
echo ==================================================
echo   JAMII BLOCKCHAIN - START BLOCK EXPLORER
echo ==================================================
echo.
echo [%DATE% %TIME%] Compilando o Explorador de Blocos (mvn clean package)...
cd /d "%~dp0"
call mvn clean package
if %ERRORLEVEL% neq 0 (
    echo.
    echo [ERRO] Falha ao compilar a aplicacao.
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo [%DATE% %TIME%] Compilado com sucesso! Iniciando o Explorador de Blocos na porta 8081...
echo Para parar a aplicacao, pressione Ctrl+C ou feche esta janela.
echo.
java -jar target/jamii-block-explorer-0.1.jar
pause
