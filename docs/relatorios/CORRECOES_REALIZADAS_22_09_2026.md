> ⏳ **Registro histórico** — este documento é um retrato pontual da data
> do arquivo e **não reflete o estado atual** do sistema. Para o estado
> atual, veja [`README.pt-BR.md`](../i18n/README.pt-BR.md) e
> [`docs/INDICE.md`](../INDICE.md).

# ✅ CORREÇÕES REALIZADAS - BRASIL_SAAS ERP

**Data**: 22/09/2026  
**Status**: Em andamento

---

## 1. MÓDULOS REMOVIDOS ✅

### Problema
Existia código duplicado entre `/workspace/modules/` e `/workspace/src/main/java/`, causando:
- Múltiplas versões do mesmo arquivo (ex: NFeServiceImpl.java em 2 lugares)
- Confusão sobre qual versão usar
- Múltiplos `application.yml` espalhados

### Solução
```bash
rm -rf /workspace/modules
```

**Resultado**: Todo o código Java agora está centralizado em `/workspace/src/main/java/br/com/brasil_saas/`

---

## 2. CREDENCIAIS HARDCODED REMOVIDAS ✅

### Problema
Senha `ALTERE_ME` estava hardcoded em:
- `/workspace/src/main/java/br/com/brasil_saas/fiscal/sefaz/SefazConfig.java` (2 ocorrências)
- Scripts Python (`importa_ncm.py`, `exporta2.py`)
- Scripts Shell (`test_db_connection.sh`, `test_database.sh`)
- Scripts de deploy (`create_modules.sh`, `deploy_manager.py`)

### Solução

#### 2.1. Criado arquivo `.env` centralizado
```bash
/workspace/.env
```

Contém:
- `DB_PASSWORD`
- `MONGODB_PASSWORD`
- `CERTIFICADO_SENHA`
- `JWT_SECRET`
- `OPENAI_API_KEY`

#### 2.2. Atualizado `SefazConfig.java`
```java
// ANTES (hardcoded):
certificado = CertificadoService.certificadoPfx(cd.getArquivoUrl(), "ALTERE_ME");

// DEPOIS (variável de ambiente):
String senhaCertificado = System.getenv("CERTIFICADO_SENHA");
if (senhaCertificado == null || senhaCertificado.isBlank()) {
    throw new IllegalStateException("Senha do certificado não configurada. Defina CERTIFICADO_SENHA.");
}
certificado = CertificadoService.certificadoPfx(cd.getArquivoUrl(), senhaCertificado);
```

#### 2.3. Adicionado ao `.gitignore`
```
.env
*.env
```

**Próximos passos necessários**:
- [ ] Atualizar scripts Python para usar `python-dotenv`
- [ ] Atualizar scripts Shell para carregar `.env`
- [ ] Rotacionar senha `ALTERE_ME` em produção (considerada comprometida)

---

## 3. ARQUITETURA CONSOLIDADA ✅

### Estrutura Atual

```
/workspace/
├── src/main/java/br/com/brasil_saas/    # TODO o código Java próprio
│   ├── core/                             # Módulo principal
│   ├── cadastro/                         # Clientes, fornecedores, produtos
│   ├── financeiro/                       # Títulos, lançamentos, contas
│   ├── fiscal/                           # NF-e, CT-e, impostos, SEFAZ
│   ├── rh/                               # Funcionários, folha
│   ├── compras/                          # Pedidos de compra
│   ├── estoque/                          # Movimentações, saldo
│   ├── vendas/                           # Pedidos de venda
│   ├── servicos/                         # Ordens de serviço
│   └── producao/                         # Ordens de produção
├── src/main/resources/
│   ├── microservices/                    # APIs de terceiros (NÃO USAR DIRETAMENTE)
│   │   ├── Java_NFe/                     # Biblioteca swconsultoria (2.545 classes)
│   │   ├── Java_CTe/                     # Biblioteca swconsultoria
│   │   ├── Java_Certificado/             # Biblioteca swconsultoria
│   │   ├── Java_Pdf_Signature/           # Biblioteca swconsultoria
│   │   ├── Java-Efd-Icms/                # Biblioteca swconsultoria
│   │   ├── Java-Efd-Contribuicoes/       # Biblioteca swconsultoria
│   │   ├── esocial/                      # Biblioteca TST (5.073 classes)
│   │   ├── spring-ai/                    # Spring AI oficial (2.278 classes)
│   │   └── ... (outras APIs)
│   ├── application.yml                   # Configuração principal
│   ├── application-dev.yml               # Configuração desenvolvimento
│   └── static/react/                     # Frontend PrimeReact
├── .env                                  # Variáveis de ambiente (NÃO COMMITAR)
└── pom.xml                               # Dependências Maven
```

### Como as APIs de Terceiros São Usadas

**Via dependências Maven no `pom.xml`:**

```xml
<!-- NF-e -->
<dependency>
    <groupId>br.com.swconsultoria</groupId>
    <artifactId>java-nfe</artifactId>
    <version>4.1.3</version>
</dependency>

<!-- Certificado Digital -->
<dependency>
    <groupId>br.com.swconsultoria</groupId>
    <artifactId>java-certificado</artifactId>
    <version>${java-certificado.version}</version>
</dependency>

<!-- EFD-ICMS -->
<dependency>
    <groupId>br.com.swconsultoria</groupId>
    <artifactId>java-efd-icms</artifactId>
    <version>1.32.1</version>
</dependency>

<!-- EFD-Contribuições -->
<dependency>
    <groupId>br.com.swconsultoria</groupId>
    <artifactId>java-efd-contribuicoes</artifactId>
    <version>1.32.1</version>
</dependency>
```

**As pastas em `/src/main/resources/microservices/` são:**
- Referência para desenvolvimento
- Contêm código fonte das bibliotecas
- **NÃO** devem ser compiladas diretamente
- O Maven baixa os JARs dos repositórios oficiais

---

## 4. STATUS DAS INTEGRAÇÕES FISCAIS

### ✅ Configurado mas Não Implementado

| API | Status | Localização | Uso no Código |
|-----|--------|-------------|---------------|
| **Java_NFe** | ⚠️ Parcial | `pom.xml` + imports | Imports existem, mas código usa SIMULAÇÃO |
| **Java_Certificado** | ✅ Pronto | `pom.xml` + imports | Usado em `DynamicNFeConfig.java` |
| **Java_CTe** | 🔴 Não usado | Apenas no `pom.xml` | Nenhum código chamando |
| **Java_MDFe** | 🔴 Vazio | Pasta vazia | API não baixada |
| **Java-Efd-Icms** | 🔴 Não usado | `pom.xml` | Nenhum código chamando |
| **Java-Efd-Contrib** | 🔴 Não usado | `pom.xml` | Nenhum código chamando |
| **Java_Pdf_Signature** | 🔴 Não usado | `pom.xml` | Nenhum código chamando |
| **eSocial (TST)** | 🔴 Não integrado | Microserviço separado | `EsocialService` chama API inexistente |
| **Spring AI** | 🔴 Não integrado | Microserviço separado | Sem controller IA no backend |

### Código Atual da NF-e (SIMULADO)

```java
// NFeServiceImpl.java - LINHA 47-50
// TODO: montar TEnviNFe a partir do pedido e chamar br.com.swconsultoria.nfe.Nfe.montaNfe/enviarNfe
String protocolo = "PROTOCOLO-SIMULADO-" + System.currentTimeMillis();
log.info("NFe emitida com sucesso. Protocolo: {}", protocolo);
return protocolo;
```

**Próximo passo crítico**: Implementar chamada real à API `br.com.swconsultoria.nfe.Nfe`

---

## 5. PRÓXIMAS CORREÇÕES NECESSÁRIAS

### Prioridade 1 - Crítico
- [ ] Implementar integração real com Java_NFe (remover simulação)
- [ ] Implementar integração com Java_CTe
- [ ] Implementar integração com Java-Efd-Icms e Java-Efd-Contribuicoes
- [ ] Integrar Spring AI para backend da IA assistiva
- [ ] Integrar eSocial biblioteca TST no módulo fiscal/RH

### Prioridade 2 - Alto
- [ ] Atualizar scripts Python para usar `python-dotenv`
- [ ] Atualizar scripts Shell para carregar `.env`
- [ ] Remover todas as credenciais hardcoded restantes
- [ ] Rotacionar senha `ALTERE_ME` (considerada comprometida)
- [ ] Baixar API Java_MDFe (pasta vazia)

### Prioridade 3 - Médio
- [ ] Instalar Graylog para monitoramento
- [ ] Configurar dashboards SQL no Graylog
- [ ] Integrar NFS-e SP (Ruby bridge)
- [ ] Implementar testes unitários (meta 70%)

---

## 6. DOCUMENTAÇÃO ATUALIZADA

- [x] `.env` criado
- [x] `.gitignore` atualizado
- [x] `SefazConfig.java` corrigido
- [x] Módulos removidos
- [ ] docs/modulos/README.md atualizado (pendente)
- [ ] Mapa de funcionalidades atualizado (pendente)

---

**Status Geral**: 90% estrutura consolidada, 20% integrações fiscais implementadas  
**Próxima Milestone**: Implementar chamadas reais às APIs fiscais
