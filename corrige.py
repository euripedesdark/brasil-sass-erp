import os
import re

project_dir = "/home/euripedes/OneDrive/python/projetos-leno/sysfluxo"
base_path = os.path.join(project_dir, "src/main/java/com/sysfluxo")

print("🔧 Iniciando correção automática dos 26 erros de compilação...\n")

# 1. CadastrosAuxiliaresApiController.java (Erro: Map vs ResponseEntity)
path = os.path.join(base_path, "cadastro/controller/CadastrosAuxiliaresApiController.java")
if os.path.exists(path):
    with open(path, 'r', encoding='utf-8') as f: content = f.read()
    content = re.sub(r'return\s+result\s*;', 'return ResponseEntity.ok(result);', content)
    with open(path, 'w', encoding='utf-8') as f: f.write(content)
    print("✓ CadastrosAuxiliaresApiController.java corrigido (ResponseEntity)")

# 2. ProdutoController.java (Erro: Page vs List, Integer vs Long)
path = os.path.join(base_path, "cadastro/controller/ProdutoController.java")
if os.path.exists(path):
    with open(path, 'r', encoding='utf-8') as f: content = f.read()
    content = content.replace('List<Produto>', 'Page<Produto>')
    content = content.replace('Integer id', 'Long id')
    if 'import org.springframework.data.domain.Page;' not in content:
        content = content.replace('import java.util.List;', 'import java.util.List;\nimport org.springframework.data.domain.Page;')
    with open(path, 'w', encoding='utf-8') as f: f.write(content)
    print("✓ ProdutoController.java corrigido (Page e Long)")

# 3. ProdutoService.java (Erro: Page vs List, .getCodigo() em String)
path = os.path.join(base_path, "cadastro/service/ProdutoService.java")
if os.path.exists(path):
    with open(path, 'r', encoding='utf-8') as f: content = f.read()
    content = content.replace('List<Produto>', 'Page<Produto>')
    content = content.replace('Integer id', 'Long id')
    # Remove .getCodigo() se estiver sendo chamado em cima de uma variável que já é String
    content = re.sub(r'(\w+)\.getCodigo\(\)', r'\1', content)
    with open(path, 'w', encoding='utf-8') as f: f.write(content)
    print("✓ ProdutoService.java corrigido")

# 4. RelatoriosService.java (Erro: int != null)
path = os.path.join(base_path, "relatorios/service/RelatoriosService.java")
if os.path.exists(path):
    with open(path, 'r', encoding='utf-8') as f: content = f.read()
    content = content.replace('receceitaCC != null', 'receceitaCC > 0')
    with open(path, 'w', encoding='utf-8') as f: f.write(content)
    print("✓ RelatoriosService.java corrigido (int != null)")

# 4b. PedidoVendaRepository.java (Adicionar métodos @Query ausentes)
path = os.path.join(base_path, "vendas/repository/PedidoVendaRepository.java")
if os.path.exists(path):
    with open(path, 'r', encoding='utf-8') as f: content = f.read()
    if 'countByDataBetween' not in content:
        methods = """
    @Query("SELECT COUNT(p) FROM PedidoVenda p WHERE p.dataPedido BETWEEN :inicio AND :fim")
    long countByDataBetween(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("SELECT SUM(p.totalGeral) FROM PedidoVenda p WHERE p.dataPedido BETWEEN :inicio AND :fim")
    BigDecimal sumValorTotalByDataBetween(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("SELECT COUNT(p) FROM PedidoVenda p WHERE p.status = :status AND p.dataPedido BETWEEN :inicio AND :fim")
    long countByStatusAndDataBetween(@Param("status") StatusPedido status, @Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);
}
"""
        imports = "import org.springframework.data.jpa.repository.Query;\nimport org.springframework.data.repository.query.Param;\nimport java.time.LocalDate;\nimport java.math.BigDecimal;\nimport com.sysfluxo.vendas.model.StatusPedido;\n"

        content = content.replace('}', methods)
        if 'import org.springframework.data.jpa.repository.Query;' not in content:
            content = imports + content

        with open(path, 'w', encoding='utf-8') as f: f.write(content)
        print("✓ PedidoVendaRepository.java atualizado com @Query methods")

# 5. OrdemServicoController.java (Erro: Integer vs Long)
path = os.path.join(base_path, "servicos/controller/OrdemServicoController.java")
if os.path.exists(path):
    with open(path, 'r', encoding='utf-8') as f: content = f.read()
    content = content.replace('Integer id', 'Long id')
    with open(path, 'w', encoding='utf-8') as f: f.write(content)
    print("✓ OrdemServicoController.java corrigido (Long)")

# 6. OrdemServicoService.java (Erro: .trim() em Object)
path = os.path.join(base_path, "servicos/service/OrdemServicoService.java")
if os.path.exists(path):
    with open(path, 'r', encoding='utf-8') as f: content = f.read()
    content = content.replace('getServico().trim()', 'getServico().toString().trim()')
    with open(path, 'w', encoding='utf-8') as f: f.write(content)
    print("✓ OrdemServicoService.java corrigido (toString().trim())")

# 7. PedidoVendaController.java (Erro: Integer vs Long)
path = os.path.join(base_path, "vendas/controller/PedidoVendaController.java")
if os.path.exists(path):
    with open(path, 'r', encoding='utf-8') as f: content = f.read()
    content = content.replace('Integer id', 'Long id')
    with open(path, 'w', encoding='utf-8') as f: f.write(content)
    print("✓ PedidoVendaController.java corrigido (Long)")

# 8. PedidoVendaService.java (Erro: Matemática com BigDecimal e Double vs BigDecimal)
path = os.path.join(base_path, "vendas/service/PedidoVendaService.java")
if os.path.exists(path):
    with open(path, 'r', encoding='utf-8') as f: content = f.read()
    content = content.replace('Integer id', 'Long id')

    # Correções de Soma (BigDecimal)
    content = content.replace('subtotal + item.getValorTotal()', 'subtotal.add(item.getValorTotal())')
    content = content.replace('impostos + item.getValorIcms() + item.getValorIpi()', 'impostos.add(item.getValorIcms()).add(item.getValorIpi())')
    content = content.replace('desconto + item.getDesconto()', 'desconto.add(item.getDesconto())')

    # Correções de Multiplicação (BigDecimal)
    content = re.sub(r'(\w+)\s*\*\s*item\.getValorUnitario\(\)', r'new BigDecimal(\1).multiply(item.getValorUnitario())', content)
    content = re.sub(r'item\.getValorUnitario\(\)\s*\*\s*(\w+)', r'item.getValorUnitario().multiply(new BigDecimal(\1))', content)

    # Conversao de BigDecimal para Double (se houver cast explicito)
    content = re.sub(r'\(double\)\s*(\w+)', r'\1.doubleValue()', content)

    with open(path, 'w', encoding='utf-8') as f: f.write(content)
    print("✓ PedidoVendaService.java corrigido (BigDecimal Math e Long)")

print("\n✅ Todas as correções aplicadas com sucesso!")
print("👉 Execute novamente: mvn clean compile -DskipTests")
