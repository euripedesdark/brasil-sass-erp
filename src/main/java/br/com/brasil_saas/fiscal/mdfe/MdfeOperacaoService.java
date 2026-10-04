package br.com.brasil_saas.fiscal.mdfe;

import br.com.brasil_saas.fiscal.model.Mdfe;
import br.com.brasil_saas.fiscal.repository.MdfeRepository;
import br.com.brasil_saas.shared.exception.BusinessException;
import com.fincatto.documentofiscal.mdfe3.classes.nota.MDFe;
import com.fincatto.documentofiscal.mdfe3.classes.nota.consulta.MDFeNotaConsultaRetorno;
import com.fincatto.documentofiscal.mdfe3.webservices.WSFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Operacao completa do MDF-e 3.00: emissao sincrona, consulta, cancelamento e
 * encerramento. O XML e montado pela camada de transporte e assinado/transmitido
 * pela Fincatto com o certificado A1 configurado no ERP.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MdfeOperacaoService {

    private static final Pattern PROTOCOLO = Pattern.compile("<nProt>([^<]+)</nProt>");
    private final MdfeRepository repository;
    private final MdfeEmissaoService configuracao;

    @Transactional
    public Map<String,Object> emitir(Long empresaId, String xml) {
        exigirXml(xml);
        ConfigCertificadoDocumentoFiscal conf = configuracao.configuracoes();
        try (WSFacade facade = new WSFacade(conf.comoMDFe())) {
            MDFe nota = conf.comoMDFe().getPersister().read(MDFe.class, xml);
            var retorno = facade.envioRecepcaoSinc(nota);
            var resposta = retorno.getRetorno();
            MDFe assinado = retorno.getMDFEAssinado();
            String chave = assinado.getInfo().getChaveAcesso();

            Mdfe documento = repository.findByEmpresaIdAndChaveAcesso(empresaId, chave)
                    .orElseGet(Mdfe::new);
            documento.setEmpresaId(empresaId);
            documento.setChaveAcesso(chave);
            documento.setNumero(assinado.getInfo().getIdentificacao().getNumero().longValue());
            documento.setSerie(String.valueOf(assinado.getInfo().getIdentificacao().getSerie()));
            documento.setDataEmissao(assinado.getInfo().getIdentificacao().getDataEmissao().toLocalDateTime());
            documento.setUfInicio(assinado.getInfo().getIdentificacao().getUnidadeFederativaInicio());
            documento.setUfFim(assinado.getInfo().getIdentificacao().getUnidadeFederativaFim());
            documento.setStatus(status(resposta.getStatus()));
            documento.setXml(assinado.toString());
            repository.save(documento);

            Map<String,Object> out=base(documento);
            out.put("cStat", resposta.getStatus());
            out.put("xMotivo", resposta.getMotivo());
            out.put("protocolo", resposta.getMdfProtocolo() == null || resposta.getMdfProtocolo().getProtocoloInfo() == null
                    ? null : resposta.getMdfProtocolo().getProtocoloInfo().getNumeroProtocolo());
            out.put("xmlAssinado", assinado.toString());
            return out;
        } catch(Exception e) {
            log.error("Falha na emissão do MDF-e da empresa {}", empresaId, e);
            throw new BusinessException("Falha na emissão do MDF-e: " + mensagem(e));
        }
    }

    public Map<String,Object> consultar(Long empresaId, String chave) {
        Mdfe documento=repository.findByEmpresaIdAndChaveAcesso(empresaId, normalizarChave(chave))
                .orElseThrow(() -> new BusinessException("MDF-e não encontrado para esta empresa."));
        try (WSFacade facade = new WSFacade(configuracao.configuracoes().comoMDFe())) {
            MDFeNotaConsultaRetorno retorno=facade.consultaMdfe(documento.getChaveAcesso());
            Map<String,Object> out=base(documento);
            out.put("cStat", retorno.getStatus());
            out.put("xMotivo", retorno.getMotivo());
            out.put("protocolo", retorno.getProtocolo() == null || retorno.getProtocolo().getProtocoloInfo() == null
                    ? null : retorno.getProtocolo().getProtocoloInfo().getNumeroProtocolo());
            return out;
        } catch(Exception e) {
            throw new BusinessException("Falha na consulta do MDF-e: " + mensagem(e));
        }
    }

    @Transactional
    public Map<String,Object> cancelar(Long empresaId, Long id, String motivo) {
        Mdfe documento=carregar(empresaId,id);
        String protocolo=extrairProtocolo(documento.getXml());
        if(protocolo==null) throw new BusinessException("MDF-e sem protocolo de autorização armazenado; consulte a SEFAZ antes de cancelar.");
        if(motivo==null || motivo.trim().length()<15) throw new BusinessException("O motivo do cancelamento deve ter pelo menos 15 caracteres.");
        try(WSFacade facade=new WSFacade(configuracao.configuracoes().comoMDFe())) {
            var retorno=facade.cancelaMdfe(documento.getChaveAcesso(),protocolo,motivo.trim());
            var info=retorno.getEventoRetorno();
            Map<String,Object> out=new LinkedHashMap<>();
            out.put("id",id); out.put("chaveAcesso",documento.getChaveAcesso());
            out.put("cStat",info==null?null:info.getCodigoStatus());
            out.put("xMotivo",info==null?null:info.getMotivo());
            if(info!=null && Integer.valueOf(135).equals(info.getCodigoStatus())) {
                documento.setStatus("CANCELADO"); repository.save(documento);
            }
            return out;
        }catch(Exception e){ throw new BusinessException("Falha no cancelamento do MDF-e: "+mensagem(e)); }
    }

    @Transactional
    public Map<String,Object> encerrar(Long empresaId, Long id, String codigoMunicipio, String uf) {
        Mdfe documento=carregar(empresaId,id);
        String protocolo=extrairProtocolo(documento.getXml());
        if(protocolo==null) throw new BusinessException("MDF-e sem protocolo de autorização armazenado.");
        if(codigoMunicipio==null || !codigoMunicipio.matches("\\d{7}")) throw new BusinessException("Informe o código IBGE de 7 dígitos do município de encerramento.");
        try(WSFacade facade=new WSFacade(configuracao.configuracoes().comoMDFe())) {
            var retorno=facade.encerramento(documento.getChaveAcesso(),protocolo,codigoMunicipio.trim(),java.time.LocalDate.now(),
                    com.fincatto.documentofiscal.DFUnidadeFederativa.valueOf(uf.trim().toUpperCase()));
            var info=retorno.getEventoRetorno();
            Map<String,Object> out=new LinkedHashMap<>();
            out.put("id",id); out.put("chaveAcesso",documento.getChaveAcesso());
            out.put("cStat",info==null?null:info.getCodigoStatus());
            out.put("xMotivo",info==null?null:info.getMotivo());
            if(info!=null && Integer.valueOf(135).equals(info.getCodigoStatus())) {
                documento.setStatus("ENCERRADO"); repository.save(documento);
            }
            return out;
        }catch(Exception e){ throw new BusinessException("Falha no encerramento do MDF-e: "+mensagem(e)); }
    }

    private Mdfe carregar(Long empresaId,Long id){
        return repository.findById(id).filter(d->empresaId.equals(d.getEmpresaId()))
                .orElseThrow(()->new BusinessException("MDF-e não encontrado para esta empresa."));
    }
    private Map<String,Object> base(Mdfe d){
        Map<String,Object> out=new LinkedHashMap<>();
        out.put("id",d.getId()); out.put("numero",d.getNumero()); out.put("serie",d.getSerie());
        out.put("chaveAcesso",d.getChaveAcesso()); out.put("dataEmissao",d.getDataEmissao());
        out.put("status",d.getStatus()); out.put("ufInicio",d.getUfInicio()); out.put("ufFim",d.getUfFim());
        out.put("xml",d.getXml()); return out;
    }
    private String status(String cStat){ if("100".equals(cStat))return "AUTORIZADA"; if(cStat==null)return "PENDENTE"; return "REJEITADA"; }
    private String extrairProtocolo(String xml){if(xml==null)return null; Matcher m=PROTOCOLO.matcher(xml); return m.find()?m.group(1):null;}
    private String normalizarChave(String chave){if(chave==null||chave.replaceAll("\\D","").length()!=44)throw new BusinessException("Informe uma chave de acesso MDF-e válida com 44 dígitos.");return chave.replaceAll("\\D","");}
    private void exigirXml(String xml){if(xml==null||xml.isBlank())throw new BusinessException("Informe o XML do MDF-e.");if(!xml.contains("<MDFe")||!xml.contains("<infMDFe"))throw new BusinessException("XML inválido: esperado MDF-e 3.00.");}
    private String mensagem(Exception e){String m=e.getMessage();return m==null?e.getClass().getSimpleName():(m.length()>500?m.substring(0,500)+"...":m);}
}
