package br.com.brasil_saas.fiscal.sped;
import br.com.swconsultoria.efd.icms.registros.EfdIcms;
import br.com.swconsultoria.efd.icms.registros.bloco0.Bloco0;
import br.com.swconsultoria.efd.icms.registros.blocoC.BlocoC;
import br.com.swconsultoria.efd.icms.registros.blocoC.RegistroC001;
import br.com.swconsultoria.efd.icms.registros.blocoC.RegistroC100;
import br.com.swconsultoria.efd.icms.registros.blocoC.RegistroC170;
import br.com.brasil_saas.cadastro.model.Pessoa;
import br.com.brasil_saas.cadastro.repository.PessoaRepository;
import br.com.brasil_saas.fiscal.sped.SpedEfdController.PedidoEfd;
import br.com.brasil_saas.fiscal.model.Nfe;
import br.com.brasil_saas.fiscal.model.NfeItem;
import br.com.brasil_saas.fiscal.repository.NfeItemRepository;
import br.com.brasil_saas.fiscal.repository.NfeRepository;
import br.com.brasil_saas.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
@Service @RequiredArgsConstructor
public class EfdPeriodoService {
    private final NfeRepository nfes;
    private final NfeItemRepository itens;
    private final PessoaRepository pessoas;
    private final SpedEfdIcmsService efd;
    private static final DateTimeFormatter DIA = DateTimeFormatter.ofPattern("ddMMyyyy");
    @Transactional(readOnly = true)
    public Map<String, Object> gerarPeriodo(Long empresaId, PedidoEfd pedido) {
        LocalDate ini = inicio(pedido.competencia());
        LocalDateTime de = ini.atStartOfDay();
        LocalDateTime ate = ini.plusMonths(1).atStartOfDay();
        List<Nfe> notas = new ArrayList<>();
        notas.addAll(nfes.findByEmpresaIdAndTipoOperacaoAndDataEmissaoBetweenAndDeletedAtIsNull(empresaId, "S", de, ate));
        notas.addAll(nfes.findByEmpresaIdAndTipoOperacaoAndDataEmissaoBetweenAndDeletedAtIsNull(empresaId, "E", de, ate));
        notas = notas.stream().filter(n -> cancelada(n.getStatus()) == false).sorted((a, b) -> b.getDataEmissao().compareTo(a.getDataEmissao())).toList();
        if (notas.isEmpty()) throw new BusinessException("Sem documentos fiscais no periodo");
        EfdIcms documento = new EfdIcms();
        Bloco0 b0 = new Bloco0();
        documento.setBloco0(b0);
        b0.setRegistro0000(efd.cabecalho(pedido.competencia(), pedido.cnpj(), pedido.nome(), pedido.uf(), pedido.ie(), pedido.codMun(), pedido.im(), pedido.indPerfil(), pedido.indAtiv()));
        if (pedido.cep() != null) b0.setRegistro0100(efd.contadorEmitente(pedido.nome(), pedido.cnpj(), pedido.cep(), pedido.endereco(), pedido.numero(), pedido.complemento(), pedido.bairro(), pedido.telefone(), pedido.email(), pedido.codMun()));
        Map<Long, String> partes = new LinkedHashMap<>();
        Map<String, NfeItem> prods = new LinkedHashMap<>();
        for (Nfe n : notas) {
            if (n.getPessoaId() != null && partes.containsKey(n.getPessoaId()) == false) {
                Pessoa p = pessoas.findById(n.getPessoaId()).filter(x -> empresaId.equals(x.getEmpresaId())).orElse(null);
                if (p != null) {
                    String cod = "P" + p.getId();
                    boolean fisica = "FISICA".equalsIgnoreCase(p.getTipo());
                    b0.getRegistro0150().add(efd.participante(cod, p.getNome(), fisica ? null : p.getDocumento(), fisica ? p.getDocumento() : null, null, null));
                    partes.put(n.getPessoaId(), cod);
                }
            }
            for (NfeItem it : itens.findByNfeIdOrderByNumeroItem(n.getId())) {
                String cod = it.getCodigoProduto() != null && it.getCodigoProduto().isBlank() == false ? it.getCodigoProduto() : "PROD-" + it.getProdutoId();
                prods.putIfAbsent(cod, it);
            }
        }
        for (var e : prods.entrySet()) {
            NfeItem it = e.getValue();
            b0.getRegistro0200().add(efd.produto(e.getKey(), e.getKey(), it.getUnidade() == null ? "UN" : it.getUnidade(), it.getNcm(), it.getCest(), it.getAliquotaIcms() == null ? null : it.getAliquotaIcms().toPlainString(), null));
        }
        BlocoC bc = new BlocoC();
        documento.setBlocoC(bc);
        RegistroC001 c001 = new RegistroC001();
        c001.setInd_mov("0");
        bc.setRegistroC001(c001);
        int docs = 0; int ignoradas = 0;
        for (Nfe n : notas) {
            String codPart = n.getPessoaId() == null ? null : partes.get(n.getPessoaId());
            if (codPart == null) { ignoradas++; continue; }
            boolean entrada = "E".equalsIgnoreCase(n.getTipoOperacao());
            RegistroC100 c = new RegistroC100();
            c.setInd_oper(entrada ? "0" : "1");
            c.setInd_emit(entrada ? "1" : "0");
            c.setCod_part(codPart);
            c.setCod_mod("55");
            c.setCod_sit("00");
            c.setSer(n.getSerie());
            c.setNum_doc(n.getNumero() == null ? null : String.valueOf(n.getNumero()));
            c.setChv_nfe(n.getChaveAcesso());
            c.setDt_doc(n.getDataEmissao() == null ? null : n.getDataEmissao().format(DIA));
            c.setDt_e_s(n.getDataEmissao() == null ? null : n.getDataEmissao().format(DIA));
            c.setVl_doc(moeda(n.getValorTotal()));
            c.setVl_merc(moeda(n.getValorProdutos()));
            c.setVl_desc(moeda(n.getValorDesconto()));
            c.setVl_frt(moeda(n.getValorFrete()));
            c.setVl_icms(moeda(n.getValorIcms()));
            c.setVl_ipi(moeda(n.getValorIpi()));
            c.setVl_pis(moeda(n.getValorPis()));
            c.setVl_cofins(moeda(n.getValorCofins()));
            for (NfeItem it : itens.findByNfeIdOrderByNumeroItem(n.getId())) {
                RegistroC170 i170 = new RegistroC170();
                i170.setNum_item(it.getNumeroItem() == null ? null : String.valueOf(it.getNumeroItem()));
                i170.setCod_item(it.getCodigoProduto() != null && it.getCodigoProduto().isBlank() == false ? it.getCodigoProduto() : "PROD-" + it.getProdutoId());
                i170.setQtd(it.getQuantidade() == null ? null : it.getQuantidade().toPlainString());
                i170.setUnid(it.getUnidade());
                i170.setVl_item(moeda(it.getValorTotal()));
                i170.setCfop(it.getCfop() != null ? it.getCfop() : n.getCfop());
                i170.setAliq_icms(it.getAliquotaIcms() == null ? null : it.getAliquotaIcms().toPlainString());
                i170.setVl_icms(moeda(it.getValorIcms()));
                i170.setAliq_ipi(it.getAliquotaIpi() == null ? null : it.getAliquotaIpi().toPlainString());
                i170.setVl_ipi(moeda(it.getValorIpi()));
                c.getRegistroC170().add(i170);
            }
            bc.getRegistroC100().add(c);
            docs++;
        }
        if (docs == 0) throw new BusinessException("Nenhum documento com participante identificado");
        String conteudo = efd.gerar(documento);
        List<String> linhas = List.of(conteudo.split("\\R"));
        Map<String, Object> saida = new LinkedHashMap<>();
        saida.put("sucesso", true);
        saida.put("competencia", pedido.competencia());
        saida.put("documentos", docs);
        saida.put("ignoradas", ignoradas);
        saida.put("totalLinhas", linhas.size());
        saida.put("conteudo", conteudo);
        return saida;
    }
    private LocalDate inicio(String competencia) {
        try {
            String[] q = competencia.split("/");
            return LocalDate.of(Integer.parseInt(q[1]), Integer.parseInt(q[0]), 1);
        } catch (Exception e) { throw new BusinessException("Competencia invalida, use MM/AAAA"); }
    }
    private boolean cancelada(String status) {
        return status != null && status.trim().toUpperCase().startsWith("CANCEL");
    }
    private String moeda(BigDecimal v) {
        if (v == null || v.signum() == 0) return null;
        return v.setScale(2, RoundingMode.HALF_UP).toPlainString().replace(".", ",");
    }
}
