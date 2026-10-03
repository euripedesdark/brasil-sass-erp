# Script para executar o Brasil SaaS ERP
param(
    [string]$Profile = "prod",
    [switch]$InstallService = $false
)

$JarFile = "target\brasil-saas-erp-1.0.0-SNAPSHOT.jar"
$ServiceName = "BrasilSaaS"

if (-not (Test-Path $JarFile)) {
    Write-Host "Erro: Arquivo $JarFile não encontrado!" -ForegroundColor Red
    exit 1
}

if ($InstallService) {
    # Instalar como serviço Windows
    Write-Host "Instalando como serviço Windows..." -ForegroundColor Green
    
    # Verificar se o winsw está disponível
    if (-not (Get-Command "winsw.exe" -ErrorAction SilentlyContinue)) {
        Write-Host "Baixe winsw.exe para instalar como serviço" -ForegroundColor Yellow
        Write-Host "https://github.com/winsw/winsw" -ForegroundColor Yellow
        exit 1
    }
    
    # Criar arquivo de configuração do serviço
    @"
<?xml version="1.0" encoding="UTF-8"?>
<service>
    <id>$ServiceName</id>
    <name>$ServiceName</name>
    <description>Brasil SaaS ERP Service</description>
    <executable>java</executable>
    <arguments>-jar $JarFile --spring.profiles.active=$Profile</arguments>
    <logmode>rotate</logmode>
    <startmode>Automatic</startmode>
</service>
"@ | Out-File -FilePath "$ServiceName.xml" -Encoding UTF8
    
    # Instalar o serviço
    Start-Process -FilePath "winsw.exe" -ArgumentList "install $ServiceName.xml" -Wait
    
    Write-Host "Serviço instalado com sucesso!" -ForegroundColor Green
} else {
    # Executar normalmente
    Write-Host "Executando $JarFile..." -ForegroundColor Green
    Start-Process -FilePath "java" -ArgumentList "-jar", $JarFile, "--spring.profiles.active=$Profile"
}