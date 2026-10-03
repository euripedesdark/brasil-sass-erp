@echo off
REM Brasil SaaS ERP - Serviço de Cadastro (Windows)
REM Porta: 8081

set SERVICE_NAME=BrasilSaasCadastro
set JAR_PATH=C:\brasil-saas\brasil-saas-erp.jar
set JAVA_OPTS=-Xmx1536m -Xms512m -XX:+UseG1GC
set SPRING_PROFILE=servico-cadastro
set SERVER_PORT=8081

echo Iniciando %SERVICE_NAME% na porta %SERVER_PORT%...
java %JAVA_OPTS% -jar "%JAR_PATH%" --spring.profiles.active=%SPRING_PROFILE% --server.port=%SERVER_PORT%
