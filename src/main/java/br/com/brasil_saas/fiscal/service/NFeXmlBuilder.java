package br.com.brasil_saas.fiscal.service;

import br.com.brasil_saas.cadastro.model.Cliente;
import br.com.brasil_saas.cadastro.model.Endereco;
import br.com.brasil_saas.cadastro.model.Produto;
import br.com.brasil_saas.cadastro.repository.ClienteRepository;
import br.com.brasil_saas.cadastro.repository.ProdutoRepository;
import br.com.brasil_saas.cadastro.repository.MunicipioRepository;
import br.com.brasil_saas.core.model.Empresa;
import br.com.brasil_saas.fiscal.model.RegraTributaria;
import br.com.brasil_saas.vendas.model.ItemPedidoVenda;
import br.com.brasil_saas.vendas.model.PedidoVenda;
import br.com.swconsultoria.nfe.dom.ConfiguracoesNfe;
import br.com.swconsultoria.nfe.dom.enuns.DocumentoEnum;
import br.com.swconsultoria.nfe.schemas.*;
import br.com.swconsultoria.nfe.schemas.TNFe.InfNFe;
import br.com.swconsultoria.nfe.schemas.TNFe.InfNFe.Det;
import br.com.swconsultoria.nfe.schemas.TNFe.InfNFe.Det.Imposto;
import br.com.swconsultoria.nfe.schemas.TNFe.InfNFe.Det.Imposto.COFINS;
import br.com.swconsultoria.nfe.schemas.TNFe.InfNFe.Det.Imposto.COFINS.COFINSAliq;
import br.com.swconsultoria.nfe.schemas.TNFe.InfNFe.Det.Imposto.ICMS;
import br.com.swconsultoria.nfe.schemas.TNFe.InfNFe.Det.Imposto.PIS;
import br.com.swconsultoria.nfe.schemas.TNFe.InfNFe.Det.Imposto.PIS.PISAliq;
import br.com.swconsultoria.nfe.schemas.TNFe.InfNFe.Det.Prod;
import br.com.swconsultoria.nfe.schemas.TNFe.InfNFe.Total;
import br.com.swconsultoria.nfe.schemas.TNFe.InfNFe.Total.ICMSTot;
import br.com.swconsultoria.nfe.util.ChaveUtil;
import br.com.swconsultoria.nfe.util.ConstantesUtil;
import br.com.swconsultoria.nfe.util.XmlNfeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class NFeXmlBuilder {
    private final ClienteRepository clienteRepository;
    private final ProdutoRepository produtoRepository;
    private final RegraTributariaService regraTributariaService;
    private final MunicipioRepository municipioRepository;

    public TEnviNFe build(ConfiguracoesNfe config, Empresa empresa, PedidoVenda pedido, int serie, int numero) {
        if (empresa == null || pedido == null) throw new IllegalArgumentException("Empresa e pedido sao obrigatorios");
        if (serie < 1 || serie > 999) throw new IllegalArgumentException("Serie NF-e deve estar entre 1 e 999");
        if (numero < 1 || numero > 999_999_999) throw new IllegalArgumentException("Numero NF-e deve estar entre 1 e 999999999");
        if (pedido.getItens() == null || pedido.getItens().isEmpty()) throw new IllegalStateException("Pedido sem itens fiscais");

        Cliente cliente = clienteRepository.findByIdAndEmpresaIdAndDeletedAtIsNullWithPessoa(pedido.getClienteId(), empresa.getId())
                .orElseThrow(() -> new IllegalStateException("Cliente do pedido nao encontrado"));
        Endereco endereco = enderecoPrincipal(cliente);
        String cnpj = digits(empresa.getCnpj());
        LocalDateTime emissao = LocalDateTime.now();
        String cnf = ChaveUtil.completarComZerosAEsquerda(
                String.valueOf(Math.floorMod(Objects.hash(empresa.getId(), pedido.getId(), emissao), 100_000_000)), 8);

        ChaveUtil chaveUtil = new ChaveUtil(config.getEstado(), cnpj, DocumentoEnum.NFE.getModelo(), serie, numero, "1", cnf, emissao);
        InfNFe inf = new InfNFe();
        inf.setId(chaveUtil.getChaveNF());
        inf.setVersao(ConstantesUtil.VERSAO.NFE);
        inf.setIde(ide(config, empresa, pedido, serie, numero, "1", cnf, chaveUtil.getDigitoVerificador(), emissao, endereco));
        inf.setEmit(emit(empresa));
        inf.setDest(dest(cliente, endereco));

        Totais totais = new Totais();
        for (ItemPedidoVenda item : pedido.getItens()) {
            if (item.getProdutoId() == null) throw new IllegalStateException("Item " + item.getNumeroItem() + " nao possui produto");
            Produto produto = produtoRepository.findByIdAndEmpresaIdAndDeletedAtIsNull(item.getProdutoId(), empresa.getId())
                    .orElseThrow(() -> new IllegalStateException("Produto nao encontrado na empresa: " + item.getProdutoId()));
            RegraTributaria regra = regraTributariaService.resolver(empresa, produto, endereco.getUf());
            inf.getDet().add(detalhe(item, produto, regra, totais));
        }
        inf.setTotal(total(totais, pedido));
        InfNFe.Transp transp = new InfNFe.Transp();
        transp.setModFrete("9");
        inf.setTransp(transp);
        inf.setPag(pag(pedido.getValorTotal()));

        TNFe nfe = new TNFe();
        nfe.setInfNFe(inf);
        TEnviNFe lote = new TEnviNFe();
        lote.setVersao(ConstantesUtil.VERSAO.NFE);
        lote.setIdLote(String.valueOf(Math.abs(Objects.hash(empresa.getId(), pedido.getId()))));
        lote.setIndSinc("1");
        lote.getNFe().add(nfe);
        return lote;
    }

    private InfNFe.Ide ide(ConfiguracoesNfe config, Empresa empresa, PedidoVenda pedido, int serie, int numero,
                           String tipoEmissao, String cnf, String cdv, LocalDateTime emissao, Endereco destino) {
        InfNFe.Ide ide = new InfNFe.Ide();
        ide.setCUF(config.getEstado().getCodigoIbge());
        ide.setCNF(cnf);
        ide.setNatOp("VENDA DE MERCADORIA");
        ide.setMod(DocumentoEnum.NFE.getModelo());
        ide.setSerie(String.valueOf(serie));
        ide.setNNF(String.valueOf(numero));
        ide.setDhEmi(XmlNfeUtil.dataNfe(emissao, null));
        ide.setTpNF("1");
        ide.setIdDest(empresa.getUf().equalsIgnoreCase(destino.getUf()) ? "1" : "2");
        ide.setCMunFG(required(empresa.getCodigoIbge(), "Codigo IBGE da empresa"));
        ide.setTpImp("1");
        ide.setTpEmis(tipoEmissao);
        ide.setCDV(cdv);
        ide.setTpAmb(config.getAmbiente().getCodigo());
        ide.setFinNFe("1");
        ide.setIndFinal("0");
        ide.setIndPres(indPres(pedido));
        ide.setProcEmi("0");
        ide.setVerProc("BrasilCloud ERP");
        return ide;
    }

    private String indPres(PedidoVenda pedido) {
        String origem = pedido.getOrigem();
        if (origem == null) return "9";
        String v = origem.trim().toUpperCase();
        if (v.contains("PRESENC")) return "1";
        if (v.contains("INTERNET") || v.contains("ECOMMERCE") || v.contains("E-COMMERCE")) return "2";
        if (v.contains("TELEFONE")) return "3";
        return "9";
    }

    private InfNFe.Emit emit(Empresa empresa) {
        InfNFe.Emit emit = new InfNFe.Emit();
        emit.setCNPJ(digits(empresa.getCnpj()));
        emit.setXNome(required(empresa.getRazaoSocial(), "Razao social da empresa"));
        TEnderEmi e = new TEnderEmi();
        String endereco = required(empresa.getEndereco(), "Endereco da empresa");
        e.setXLgr(endereco.length() > 60 ? endereco.substring(0, 60) : endereco);
        e.setNro(required(empresa.getNumero(), "Numero do endereco da empresa"));
        e.setXCpl(empresa.getComplemento());
        e.setXBairro(required(empresa.getBairro(), "Bairro da empresa"));
        e.setCMun(required(empresa.getCodigoIbge(), "Codigo IBGE da empresa"));
        e.setXMun(municipioRepository.findByCodigoIbge(required(empresa.getCodigoIbge(), "Codigo IBGE da empresa"))
                .orElseThrow(() -> new IllegalStateException("Municipio do emitente nao encontrado pelo codigo IBGE")).getNome());
        e.setUF(TUfEmi.valueOf(required(empresa.getUf(), "UF da empresa").toUpperCase()));
        e.setCEP(digits(empresa.getCep()));
        e.setCPais("1058");
        e.setXPais("Brasil");
        if (empresa.getTelefone() != null) e.setFone(digits(empresa.getTelefone()));
        emit.setEnderEmit(e);
        emit.setIE(required(empresa.getInscricaoEstadual(), "Inscricao estadual da empresa"));
        emit.setCRT(crt(empresa.getRegimeTributario()));
        return emit;
    }

    private InfNFe.Dest dest(Cliente cliente, Endereco endereco) {
        InfNFe.Dest dest = new InfNFe.Dest();
        String documento = digits(cliente.getPessoa().getDocumento());
        if (documento.length() == 14) dest.setCNPJ(documento);
        else if (documento.length() == 11) dest.setCPF(documento);
        else throw new IllegalStateException("Documento do cliente deve ter 11 ou 14 digitos");
        dest.setXNome(required(cliente.getPessoa().getNome(), "Nome do destinatario"));
        TEndereco e = new TEndereco();
        e.setXLgr(required(endereco.getLogradouro(), "Logradouro do destinatario"));
        e.setNro(required(endereco.getNumero(), "Numero do destinatario"));
        e.setXCpl(endereco.getComplemento());
        e.setXBairro(required(endereco.getBairro(), "Bairro do destinatario"));
        if (endereco.getMunicipio() == null) throw new IllegalStateException("Municipio do destinatario nao cadastrado");
        e.setCMun(required(endereco.getMunicipio().getCodigoIbge(), "Codigo IBGE do municipio"));
        e.setXMun(required(endereco.getMunicipio().getNome(), "Nome do municipio"));
        e.setUF(TUf.valueOf(required(endereco.getUf(), "UF do destinatario").toUpperCase()));
        e.setCEP(digits(endereco.getCep()));
        e.setCPais("1058");
        e.setXPais("Brasil");
        if (cliente.getPessoa().getTelefone() != null) e.setFone(digits(cliente.getPessoa().getTelefone()));
        dest.setEnderDest(e);
        if (cliente.getPessoa().getEmail() != null && !cliente.getPessoa().getEmail().isBlank()) dest.setEmail(cliente.getPessoa().getEmail().trim());
        if (cliente.getPessoa().getJuridica() != null && cliente.getPessoa().getJuridica().getInscricaoEstadual() != null
                && !cliente.getPessoa().getJuridica().getInscricaoEstadual().isBlank()) {
            dest.setIE(cliente.getPessoa().getJuridica().getInscricaoEstadual().trim());
            dest.setIndIEDest("1");
        } else dest.setIndIEDest("9");
        return dest;
    }

    private Det detalhe(ItemPedidoVenda item, Produto produto, RegraTributaria regra, Totais totais) {
        BigDecimal qtd = item.getQuantidade();
        BigDecimal unit = item.getValorUnitario();
        BigDecimal valor = item.getValorTotal() != null ? item.getValorTotal() : qtd.multiply(unit).setScale(2, RoundingMode.HALF_UP);
        if (qtd == null || qtd.signum() <= 0 || unit == null || unit.signum() < 0) throw new IllegalStateException("Quantidade/valor invalidos no item " + item.getNumeroItem());

        Det det = new Det();
        det.setNItem(String.valueOf(item.getNumeroItem()));
        Prod p = new Prod();
        p.setCProd(required(produto.getCodigo(), "Codigo do produto"));
        if (produto.getCodigoBarras() != null && !produto.getCodigoBarras().isBlank()) {
            p.setCEAN(produto.getCodigoBarras().trim());
            p.setCEANTrib(produto.getCodigoBarras().trim());
        } else {
            p.setCEAN("SEM GTIN");
            p.setCEANTrib("SEM GTIN");
        }
        p.setXProd(required(item.getDescricao() != null ? item.getDescricao() : produto.getNome(), "Descricao do produto"));
        p.setNCM(required(produto.getNcm(), "NCM do produto"));
        if (produto.getCest() != null && !produto.getCest().isBlank()) p.setCEST(produto.getCest());
        p.setCFOP(required(produto.getCfopPadrao(), "CFOP do produto"));
        String unidade = required(produto.getUnidadeMedida() != null ? produto.getUnidadeMedida().getSigla() : null, "Unidade do produto");
        p.setUCom(unidade); p.setQCom(decimal(qtd, 4)); p.setVUnCom(decimal(unit, 4)); p.setVProd(money(valor));
        p.setUTrib(unidade); p.setQTrib(decimal(qtd, 4)); p.setVUnTrib(decimal(unit, 4)); p.setIndTot("1");
        det.setProd(p);
        det.setImposto(imposto(regra, valor, totais));
        totais.vProd = totais.vProd.add(valor);
        totais.vDesc = totais.vDesc.add(item.getValorDesconto() == null ? BigDecimal.ZERO : item.getValorDesconto());
        return det;
    }

    private Imposto imposto(RegraTributaria regra, BigDecimal base, Totais totais) {
        Imposto imposto = new Imposto();
        String cstIcms = required(regra.getCstIcms(), "CST ICMS");
        ICMS icms = new ICMS();
        BigDecimal aliquota = nvl(regra.getAliquotaIcms());
        if ("00".equals(cstIcms)) {
            ICMS.ICMS00 x = new ICMS.ICMS00(); x.setOrig("0"); x.setCST("00"); x.setModBC("0"); x.setVBC(money(base)); x.setPICMS(decimal(aliquota, 2));
            BigDecimal v = base.multiply(aliquota).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP); x.setVICMS(money(v)); icms.setICMS00(x); totais.vIcms = totais.vIcms.add(v);
        } else if ("60".equals(cstIcms)) {
            ICMS.ICMS60 x = new ICMS.ICMS60(); x.setOrig("0"); x.setCST("60"); x.setVBCSTRet("0.00"); x.setPST("0.00"); x.setVICMSSTRet("0.00"); x.setVICMSSubstituto("0.00"); icms.setICMS60(x);
        } else throw new IllegalStateException("CST ICMS " + cstIcms + " ainda nao possui mapeamento JAXB seguro no builder");

        PIS pis = new PIS(); String cstPis = required(regra.getCstPis(), "CST PIS");
        if ("01".equals(cstPis) || "02".equals(cstPis)) {
            PISAliq x = new PISAliq(); x.setCST(cstPis); x.setVBC(money(base)); x.setPPIS(decimal(nvl(regra.getAliquotaPis()), 2));
            BigDecimal v = base.multiply(nvl(regra.getAliquotaPis())).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP); x.setVPIS(money(v)); pis.setPISAliq(x); totais.vPis = totais.vPis.add(v);
        } else throw new IllegalStateException("CST PIS " + cstPis + " ainda nao possui mapeamento seguro no builder");

        COFINS cofins = new COFINS(); String cstCofins = required(regra.getCstCofins(), "CST COFINS");
        if ("01".equals(cstCofins) || "02".equals(cstCofins)) {
            COFINSAliq x = new COFINSAliq(); x.setCST(cstCofins); x.setVBC(money(base)); x.setPCOFINS(decimal(nvl(regra.getAliquotaCofins()), 2));
            BigDecimal v = base.multiply(nvl(regra.getAliquotaCofins())).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP); x.setVCOFINS(money(v)); cofins.setCOFINSAliq(x); totais.vCofins = totais.vCofins.add(v);
        } else throw new IllegalStateException("CST COFINS " + cstCofins + " ainda nao possui mapeamento seguro no builder");

        imposto.getContent().add(new ObjectFactory().createTNFeInfNFeDetImpostoICMS(icms));
        imposto.getContent().add(new ObjectFactory().createTNFeInfNFeDetImpostoPIS(pis));
        imposto.getContent().add(new ObjectFactory().createTNFeInfNFeDetImpostoCOFINS(cofins));
        return imposto;
    }

    private Total total(Totais t, PedidoVenda pedido) {
        Total total = new Total(); ICMSTot x = new ICMSTot();
        x.setVBC(t.vIcms.signum() == 0 ? "0.00" : money(t.vProd)); x.setVICMS(money(t.vIcms)); x.setVICMSDeson("0.00");
        x.setVFCP("0.00"); x.setVFCPST("0.00"); x.setVFCPSTRet("0.00"); x.setVBCST("0.00"); x.setVST("0.00"); x.setVProd(money(t.vProd));
        x.setVFrete(money(nvl(pedido.getValorFrete()))); x.setVSeg("0.00"); x.setVDesc(money(t.vDesc)); x.setVII("0.00"); x.setVIPI("0.00"); x.setVIPIDevol("0.00");
        x.setVPIS(money(t.vPis)); x.setVCOFINS(money(t.vCofins)); x.setVOutro("0.00");
        x.setVNF(money(t.vProd.subtract(t.vDesc).add(nvl(pedido.getValorFrete())))); total.setICMSTot(x); return total;
    }

    private InfNFe.Pag pag(BigDecimal total) {
        InfNFe.Pag pag = new InfNFe.Pag(); InfNFe.Pag.DetPag det = new InfNFe.Pag.DetPag(); det.setTPag("90"); det.setVPag("0.00"); pag.getDetPag().add(det); return pag;
    }

    private Endereco enderecoPrincipal(Cliente cliente) {
        if (cliente.getPessoa().getEnderecos() == null || cliente.getPessoa().getEnderecos().isEmpty()) throw new IllegalStateException("Cliente sem endereco");
        return cliente.getPessoa().getEnderecos().stream().filter(e -> Boolean.TRUE.equals(e.getPrincipal())).findFirst().orElse(cliente.getPessoa().getEnderecos().get(0));
    }

    private String crt(String regime) {
        if (regime == null || regime.isBlank()) throw new IllegalStateException("Regime tributario da empresa nao cadastrado");
        String v = regime.trim().toUpperCase();
        if (v.contains("SIMPLES") && v.contains("EXCESSO")) return "2";
        if (v.contains("SIMPLES")) return "1";
        if (v.contains("NORMAL") || v.contains("LUCRO")) return "3";
        if (v.matches("[123]")) return v;
        throw new IllegalStateException("Regime tributario nao reconhecido: " + regime);
    }

    private static String required(String v, String msg) { if (v == null || v.isBlank()) throw new IllegalStateException(msg); return v.trim(); }
    private static String digits(String v) { return v == null ? "" : v.replaceAll("\\D", ""); }
    private static String decimal(BigDecimal v, int scale) { return nvl(v).setScale(scale, RoundingMode.HALF_UP).toPlainString(); }
    private static String money(BigDecimal v) { return nvl(v).setScale(2, RoundingMode.HALF_UP).toPlainString(); }
    private static BigDecimal nvl(BigDecimal v) { return v == null ? BigDecimal.ZERO : v; }
    private static final class Totais { BigDecimal vProd=BigDecimal.ZERO, vDesc=BigDecimal.ZERO, vIcms=BigDecimal.ZERO, vPis=BigDecimal.ZERO, vCofins=BigDecimal.ZERO; }
}