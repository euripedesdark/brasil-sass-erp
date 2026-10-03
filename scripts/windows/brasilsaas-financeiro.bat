@echo off
REM Brasil SaaS ERP - Serviço Financeiro (Windows)
REM Porta: 8082

set SERVICE_NAME=BrasilSaasFinanceiro
set JAR_PATH=C:\brasil-saas\brasil-saas-erp.jar
set JAVA_OPTS=-Xmx1536m -Xms512m -XX:+UseG1GC
set SPRING_PROFILE=servico-financeiro
set SERVER_PORT=8082

echo Iniciando %SERVICE_NAME% na porta %SERVER_PORT%...
java %JAVA_OPTS% -jar "%JAR_PATH%" --spring.profiles.active=%SPRING_PROFILE% --server.port=%SERVER_PORT%
