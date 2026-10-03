@echo off
REM Brasil SaaS ERP - Serviço de IA Corporativa (Windows)
REM Porta: 8087

set SERVICE_NAME=BrasilSaasIA
set JAR_PATH=C:\brasil-saas\brasil-saas-erp.jar
set JAVA_OPTS=-Xmx3584m -Xms1024m -XX:+UseG1GC
set SPRING_PROFILE=servico-ia
set SERVER_PORT=8087

echo Iniciando %SERVICE_NAME% na porta %SERVER_PORT%...
java %JAVA_OPTS% -jar "%JAR_PATH%" --spring.profiles.active=%SPRING_PROFILE% --server.port=%SERVER_PORT%
