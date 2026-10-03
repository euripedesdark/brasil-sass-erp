@echo off
REM Brasil SaaS ERP - Serviço de Vendas e Compras (Windows)
REM Porta: 8084

set SERVICE_NAME=BrasilSaasVendasCompras
set JAR_PATH=C:\brasil-saas\brasil-saas-erp.jar
set JAVA_OPTS=-Xmx1536m -Xms512m -XX:+UseG1GC
set SPRING_PROFILE=servico-vendas-compras
set SERVER_PORT=8084

echo Iniciando %SERVICE_NAME% na porta %SERVER_PORT%...
java %JAVA_OPTS% -jar "%JAR_PATH%" --spring.profiles.active=%SPRING_PROFILE% --server.port=%SERVER_PORT%
