@echo off
REM Brasil SaaS ERP - Serviço de Relatórios e BI (Windows)
REM Porta: 8086

set SERVICE_NAME=BrasilSaasRelatorios
set JAR_PATH=C:\brasil-saas\brasil-saas-erp.jar
set JAVA_OPTS=-Xmx2560m -Xms768m -XX:+UseG1GC
set SPRING_PROFILE=servico-relatorios
set SERVER_PORT=8086

echo Iniciando %SERVICE_NAME% na porta %SERVER_PORT%...
java %JAVA_OPTS% -jar "%JAR_PATH%" --spring.profiles.active=%SPRING_PROFILE% --server.port=%SERVER_PORT%
