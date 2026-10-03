@echo off
REM Brasil SaaS ERP - Serviço de RH (Windows)
REM Porta: 8085

set SERVICE_NAME=BrasilSaasRH
set JAR_PATH=C:\brasil-saas\brasil-saas-erp.jar
set JAVA_OPTS=-Xmx1536m -Xms512m -XX:+UseG1GC
set SPRING_PROFILE=servico-rh
set SERVER_PORT=8085

echo Iniciando %SERVICE_NAME% na porta %SERVER_PORT%...
java %JAVA_OPTS% -jar "%JAR_PATH%" --spring.profiles.active=%SPRING_PROFILE% --server.port=%SERVER_PORT%
