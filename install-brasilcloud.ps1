<#
.SYNOPSIS
BrasilCloudERP - Instalador Platform (Windows)
Compatível com: Windows 10/11, Windows Server 2019/2022
Requisitos Atualizados: JDK 25 Oracle, Node.js 20+, Maven 3.9+, Git
Arquitetura: Spring Boot 4.1.1 (Tomcat Embutido) + PrimeReact (Vite)
Servidor Web: Apache Tomcat 10+ (embutido no Spring Boot)
Banco de Dados: PostgreSQL (recomendado para tabelas grandes)
#>

$ErrorActionPreference = "Stop"

if (!([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)) {
    Write-Host "ERRO: Execute como Administrador." -ForegroundColor Red; exit 1
}

$BASE_DIR = "C:\opt\brasilcloud"
$PROJECT_DIR = "$env:USERPROFILE\brasilcloud-web"

Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "  BrasilCloudERP - Instalador de Ambiente (JDK 25 + React)" -ForegroundColor Cyan
Write-Host "  Spring Boot 4.1.1 (Tomcat) + PrimeReact + PostgreSQL" -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan

Write-Host "`nVerificando gerenciador de pacotes..." -ForegroundColor Cyan
$pm = "winget"
if (!(Get-Command winget -ErrorAction SilentlyContinue)) {
    Write-Host "Winget não encontrado. Instalando Chocolatey..." -ForegroundColor Yellow
    Set-ExecutionPolicy Bypass -Scope Process -Force
    [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072
    iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))
    $pm = "choco"
}

# Lista de pacotes atualizada para JDK 25 e Node.js 20+
$Packages = @(
    @{ Win = "Git.Git"; Choco = "git"; Desc = "Git (controle de versão)" },
    @{ Win = "Python.Python.3.12"; Choco = "python3"; Desc = "Python 3.12" },
    @{ Win = "OpenJS.NodeJS.LTS"; Choco = "nodejs-lts"; Desc = "Node.js 20+ LTS" },
    @{ Win = "Oracle.JDK.25"; Choco = "openjdk25"; Desc = "Oracle JDK 25" },
    @{ Win = "Apache.Maven"; Choco = "maven"; Desc = "Apache Maven 3.9+" },
    @{ Win = "PostgreSQL.PostgreSQL"; Choco = "postgresql"; Desc = "PostgreSQL (banco de dados)" }
)

Write-Host "`n[1/6] Instalando dependências principais..." -ForegroundColor Green
foreach ($Pkg in $Packages) {
    $Nome = $Pkg.Desc
    Write-Host "  Instalando: $Nome" -ForegroundColor Gray
    
    if ($pm -eq "winget") {
        try {
            winget install -e --id $($Pkg.Win) --accept-package-agreements --accept-source-agreements --silent --force | Out-Null
            Write-Host "  ✓ $Nome instalado via winget" -ForegroundColor Green
        } catch {
            Write-Host "  ! Falha no winget, tentando chocolatey..." -ForegroundColor Yellow
            choco install $($Pkg.Choco) -y --force | Out-Null
            Write-Host "  ✓ $Nome instalado via chocolatey" -ForegroundColor Green
        }
    } else {
        choco install $($Pkg.Choco) -y --force | Out-Null
        Write-Host "  ✓ $Nome instalado via chocolatey" -ForegroundColor Green
    }
}

# Verificar e atualizar PATH
Write-Host "`n[2/6] Atualizando variáveis de ambiente..." -ForegroundColor Green
$env:Path = [System.Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path","User")

# Instalar SDKMAN para Windows (gerenciador de versões Java/Maven/Node)
Write-Host "`n[3/6] Verificando SDKMAN..." -ForegroundColor Green
$SdkmanDir = "$env:USERPROFILE\.sdkman"
if (!(Test-Path $SdkmanDir)) {
    Write-Host "  Instalando SDKMAN para Windows..." -ForegroundColor Yellow
    # SDKMAN não tem instalador oficial para Windows, usar scoop ou instalar manualmente
    if (Get-Command scoop -ErrorAction SilentlyContinue) {
        scoop install sdkman | Out-Null
        Write-Host "  ✓ SDKMAN instalado via scoop" -ForegroundColor Green
    } else {
        Write-Host "  ! Scoop não encontrado. SDKMAN será instalado manualmente se necessário." -ForegroundColor Yellow
    }
} else {
    Write-Host "  ✓ SDKMAN já está instalado" -ForegroundColor Green
}

# Criar diretórios base
Write-Host "`n[4/6] Criando estrutura de diretórios..." -ForegroundColor Green
New-Item -ItemType Directory -Force -Path $BASE_DIR | Out-Null
New-Item -ItemType Directory -Force -Path "$BASE_DIR\logs" | Out-Null
Write-Host "  ✓ Diretórios criados em $BASE_DIR" -ForegroundColor Green

# Configurar Firewall do Windows
$PortsTCP = @(80, 443, 5432, 8080)
Write-Host "`n[5/6] Configurando firewall do Windows..." -ForegroundColor Green
Remove-NetFirewallRule -DisplayName "BrasilCloudERP (TCP)" -ErrorAction SilentlyContinue
New-NetFirewallRule -DisplayName "BrasilCloudERP (TCP)" -Direction Inbound -Action Allow -Protocol TCP -LocalPort $PortsTCP | Out-Null
Write-Host "  ✓ Portas liberadas: $($PortsTCP -join ', ')" -ForegroundColor Green

# Configurar hosts (opcional)
$HostsPath = "$env:windir\System32\drivers\etc\hosts"
$Domain = "brasilcloud.local"
if (!(Select-String -Path $HostsPath -Pattern "\b$Domain\b" -Quiet)) {
    Add-Content -Path $HostsPath -Value "`n127.0.0.1`t$Domain"
    Write-Host "  ✓ Domínio $Domain adicionado ao hosts" -ForegroundColor Green
}

# Executar build do frontend e backend
Write-Host "`n[6/6] Configurando projeto BrasilCloudERP..." -ForegroundColor Green
if (Test-Path "$PROJECT_DIR\pom.xml") {
    Write-Host "  Projeto encontrado em $PROJECT_DIR" -ForegroundColor Cyan
    Set-Location -Path $PROJECT_DIR
    
    # Build do Frontend React
    if (Test-Path "src\main\resources\static\react\package.json") {
        Write-Host "  Instalando dependências do frontend (npm)..." -ForegroundColor Gray
        Set-Location "src\main\resources\static\react"
        npm install --legacy-peer-deps | Out-Null
        Write-Host "  Compilando frontend React para produção..." -ForegroundColor Gray
        npm run build | Out-Null
        Write-Host "  ✓ Frontend compilado com sucesso" -ForegroundColor Green
        Set-Location $PROJECT_DIR
    }
    
    # Pré-download de dependências Maven
    Write-Host "  Baixando dependências Maven (isso pode demorar)..." -ForegroundColor Gray
    mvn -B -q dependency:go-offline 2>$null
    Write-Host "  ✓ Dependências Maven pré-baixadas" -ForegroundColor Green
    
    # Inicializar Git se necessário
    if (!(Test-Path ".git")) {
        git init | Out-Null
        git add . | Out-Null
        git commit -m "Commit inicial - BrasilCloudERP com Spring Boot 4.1.1 + PrimeReact" | Out-Null
        Write-Host "  ✓ Repositório Git inicializado" -ForegroundColor Green
    }
} else {
    Write-Host "  ! Aviso: Projeto não encontrado em $PROJECT_DIR." -ForegroundColor Yellow
    Write-Host "  Clone o repositório primeiro: git clone https://github.com/euripedesdark/brasil-sass-erp.git" -ForegroundColor Yellow
}

# Resumo final
Write-Host "`n============================================================" -ForegroundColor Cyan
Write-Host "  INSTALAÇÃO CONCLUÍDA COM SUCESSO!" -ForegroundColor Green
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "`nAmbiente configurado:" -ForegroundColor White

# Verificar versões instaladas
try {
    $javaVersion = java -version 2>&1 | Select-String "version" | Select-Object -First 1
    Write-Host "  - Java: $javaVersion" -ForegroundColor Green
} catch { Write-Host "  - Java: Não verificado" -ForegroundColor Yellow }

try {
    $mvnVersion = mvn -version 2>$null | Select-String "Apache Maven" | Select-Object -First 1
    Write-Host "  - Maven: $($mvnVersion.Line)" -ForegroundColor Green
} catch { Write-Host "  - Maven: Não verificado" -ForegroundColor Yellow }

try {
    $nodeVersion = node --version
    Write-Host "  - Node.js: $nodeVersion" -ForegroundColor Green
} catch { Write-Host "  - Node.js: Não verificado" -ForegroundColor Yellow }

try {
    $npmVersion = npm --version
    Write-Host "  - NPM: $npmVersion" -ForegroundColor Green
} catch { Write-Host "  - NPM: Não verificado" -ForegroundColor Yellow }

Write-Host "  - Servidor Web: Apache Tomcat (embutido Spring Boot)" -ForegroundColor Green
Write-Host "  - Banco: PostgreSQL (recomendado para tabelas grandes)" -ForegroundColor Green

Write-Host "`nPRÓXIMOS PASSOS:" -ForegroundColor Yellow
Write-Host "  1. Configure seu banco de dados em src\main\resources\application.properties" -ForegroundColor White
Write-Host "     (host, porta, database, usuário, senha)" -ForegroundColor Gray
Write-Host "  2. Execute o sistema: mvnw.cmd spring-boot:run" -ForegroundColor White
Write-Host "  3. Acesse: http://localhost:8080" -ForegroundColor White

Write-Host "`nARQUITETURA ATUAL:" -ForegroundColor Cyan
Write-Host "  - Backend: Spring Boot 4.1.1 + Spring Data JPA + Hibernate" -ForegroundColor White
Write-Host "  - Servidor: Apache Tomcat 10+ (embutido)" -ForegroundColor White
Write-Host "  - Frontend: PrimeReact 10.8 + React 19 + Vite" -ForegroundColor White
Write-Host "  - API: RESTful com paginação para performance" -ForegroundColor White
Write-Host "  - JDK: Oracle 25" -ForegroundColor White
Write-Host "============================================================" -ForegroundColor Cyan