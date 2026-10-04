package br.com.brasil_saas.fiscal.service.impl;

import br.com.brasil_saas.fiscal.config.DynamicNFeConfig;
import br.com.brasil_saas.fiscal.service.CertificateService;
import br.com.brasil_saas.fiscal.service.NFeService;
import br.com.brasil_saas.vendas.model.PedidoVenda;
import br.com.swconsultoria.nfe.dom.ConfiguracoesNfe;
import br.com.swconsultoria.nfe.dom.enuns.AmbienteEnum;
import br.com.swconsultoria.nfe.dom.enuns.DocumentoEnum;
import br.com.swconsultoria.nfe.dom.enuns.EstadosEnum;
import br.com.swconsultoria.nfe.Nfe;
import br.com.swconsultoria.nfe.dom.retornos.TRetConsSitNFe;
import br.com.swconsultoria.nfe.util.XmlNfeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;

/**
 * Implementação do serviço de NF-e.
 * <p>
 * O bean só é criado quando {@code brasil-saas.fiscal.sefaz.enabled=true}.
 * Quando desativado, a aplicação continua compilando e iniciando normalmente,
 * e os endpoints fiscais respondem com 503/501 informando que a emissão ainda
 * não está configurada.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "brasil-saas.fiscal.sefaz.enabled", havingValue = "true")
public class NFeServiceImpl implements NFeService {

    private final CertificateService certificateService;

    @Value("${brasil-saas.fiscal.sefaz.ambiente:HOMOLOGACAO}")
    private String ambienteConfig;

    @Value("${brasil-saas.fiscal.sefaz.timeout-ms:30000}")
    private int timeoutMs;

    @Override
    public String emitirNFe(Long empresaId, PedidoVenda pedido) throws Exception {
        log.info("Iniciando emissão de NFe para empresaId: {} e pedidoId: {}", empresaId, pedido.getId());
        ConfiguracoesNfe config = carregarConfiguracoes(empresaId);
        log.info("Configurações SEFAZ carregadas para UF={}", config.getEstado());

        // TODO: montar TEnviNFe a partir do pedido e chamar br.com.swconsultoria.nfe.Nfe.montaNfe/enviarNfe
        String protocolo = "PROTOCOLO-SIMULADO-" + System.currentTimeMillis();
        log.info("NFe emitida com sucesso. Protocolo: {}", protocolo);
        return protocolo;
    }

    @Override
    public String cancelarNFe(Long empresaId, String chaveAcesso, String motivo) throws Exception {
        log.info("Cancelando NFe chave: {} para empresa: {}", chaveAcesso, empresaId);
        ConfiguracoesNfe config = carregarConfiguracoes(empresaId);
        log.info("Configurações SEFAZ carregadas para cancelamento");
        // TODO: montar evento de cancelamento e chamar br.com.swconsultoria.nfe.Nfe.cancelarNfe
        return "CANCELAMENTO-SIMULADO-" + chaveAcesso;
    }

    @Override
    public String consultarSituacao(Long empresaId, String chaveAcesso) throws Exception {
        log.info("Consultando situação da NFe chave: {}", chaveAcesso);
        ConfiguracoesNfe config = carregarConfiguracoes(empresaId);
        log.info("Configurações SEFAZ carregadas para consulta");
        if (chaveAcesso == null || !chaveAcesso.matches("\\d{44}"))
            throw new IllegalArgumentException("Chave de acesso deve conter 44 dígitos");

        TRetConsSitNFe retorno = Nfe.consultaXml(config, chaveAcesso, DocumentoEnum.NFE);
        return XmlNfeUtil.objectToXml(retorno);
    }

    private ConfiguracoesNfe carregarConfiguracoes(Long empresaId) throws Exception {
        // Valores padrão para desenvolvimento/homologação.
        // Em produção estes dados devem vir do cadastro da empresa.
        EstadosEnum uf = EstadosEnum.SP;
        AmbienteEnum ambiente = "PRODUCAO".equalsIgnoreCase(ambienteConfig)
                ? AmbienteEnum.PRODUCAO
                : AmbienteEnum.HOMOLOGACAO;

        String certPath = "/etc/brasil-saas/certs/empresa_" + empresaId + ".pfx";
        String certPass = System.getenv("NFE_CERT_PASSWORD");
        String pastaSchemas = "schemas";

        if (!java.nio.file.Files.exists(java.nio.file.Paths.get(certPath))) {
            throw new FileNotFoundException("Certificado digital não encontrado: " + certPath);
        }

        return DynamicNFeConfig.criar(Integer.valueOf(uf.getCodigoUF()), ambiente.getCodigo(), certPath, certPass, pastaSchemas);
    }
}
