# Arquivos Não Referenciados no Projeto SYSFLUXO

## 📁 Diretórios de Backup (Código Antigo)

### `old-controller-backup/`
- **OrdemServicoApiController.java** - Controller antigo de OS (substituído pela nova versão PrimeReact)

### `old-java-backup/`
- **CadastrosController.java**
- **EstruturaEmpresarialController.java**
- **FinanceiroController.java** - Controller financeiro legado
- **LoginViewController.java**
- **ModuloController.java**
- **MovimentosController.java**
- **OperacoesAvancadasController.java**
- **ParametrosAuxiliaresController.java**
- **RelatoriosController.java** - Controller de relatórios antigo
- **ViewCadastrosController.java**

### `old-service-backup/`
- **CadastrosService.java**
- **ClienteFornecedorService.java**
- **ClienteService.java**
- **FinanceiroService.java** - Service financeiro legado
- **MovimentosService.java**

## 📄 Scripts e Arquivos Avulsos

### Raiz do Projeto
- **compilar.py** - Script Python para compilação (não integrado ao Maven)
- **importa_ncm.py** - Script Python para importação de NCM (não integrado ao sistema)
- **pom.xml2** - Cópia duplicada do pom.xml (provavelmente backup de versão anterior)

## 🔍 Observações

1. **Backups de Controllers e Services**: Os arquivos nas pastas `old-*` são versões antigas do código que foram substituídas pelas novas implementações com:
   - Spring MVC tradicional (ao invés de WebFlux reativo)
   - Paginação com Pageable
   - Compatibilidade JDK 25
   - Integração com PrimeReact

2. **Scripts Python**: 
   - `compilar.py`: Pode ser útil para automação, mas não está integrado ao build Maven
   - `importa_ncm.py`: Script de importação de dados NCM, pode ser convertido para Java ou integrado como job agendado

3. **pom.xml2**: Arquivo duplicado que pode ser removido com segurança

## ✅ Ação Recomendada

Estes arquivos **NÃO** estão sendo usados pelo sistema atual e podem ser:
- Movidos para um repositório de backup externo
- Excluídos após confirmação de que não há código útil não migrado
- Documentados em um arquivo de CHANGELOG para referência futura

---
**Data da Análise**: Setembro/2024  
**Status do Sistema**: Refatorado para Spring MVC + PrimeReact + JDK 25
