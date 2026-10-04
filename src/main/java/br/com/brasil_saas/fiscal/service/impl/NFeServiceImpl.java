package br.com.brasil_saas.fiscal.service.impl;

import br.com.brasil_saas.fiscal.sefaz.SefazConfig;
import br.com.brasil_saas.fiscal.service.CertificateService;
import br.com.brasil_saas.fiscal.service.NFeService;
import br.com.brasil_saas.fiscal.service.NFeEmissaoValidator;
import br.com.brasil_saas.core.model.Empresa;
import br.com.brasil_saas.core.repository.EmpresaRepository;
import br.com.brasil_saas.vendas.model.PedidoVenda;
import br.com.swconsultoria.nfe.Nfe;
import br.com.swconsultoria.nfe.dom.ConfiguracoesNfe;
import br.com.swconsultoria.nfe.dom.enuns.DocumentoEnum;
import br.com.swconsultoria.nfe.dom.enuns.EstadosEnum;
import br.com.swconsultoria.nfe.dom.retornos.TRetConsSitNFe;
import br.com.swconsultoria.nfe.util.XmlNfeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "brasil-saas.fiscal.sefaz.enabled", havingValue = "true")
public class NFeServiceImpl implements NFeService {

    private final CertificateService certificateService;
    private final SefazConfig sefazConfig;
    private final EmpresaRepository empresaRepository;
    private final NFeEmissaoValidator emissaoValidator;

    @Override
    public String emitirNFe(Long empresaId, PedidoVenda pedido) throws Exception {
        if (pedido == null || pedido.getId() == null) {
            throw new IllegalArgumentException("Pedido de venda obrigatório para emissão de NF-e");
        }

        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa não encontrada: " + empresaId));
        emissaoValidator.validar(empresa, pedido);

        ConfiguracoesNfe config = carregarConfiguracoes(empresa);
        log.info("Pré-validação fiscal concluída para pedidoId={} empresaId={} UF={}",
                pedido.getId(), empresaId, config.getEstado());

        // A transmissão somente deve ocorrer depois que o documento fiscal
        // completo (TEnviNFe) for montado, assinado e persistido. Não retornar
        // protocolo fictício: isso faria o ERP registrar uma autorização que
        // nunca existiu na SEFAZ.
        throw new UnsupportedOperationException(
                "Emissão NF-e ainda não pode ser transmitida: montagem do TEnviNFe fiscal pendente");
    }

    @Override
    public String cancelarNFe(Long empresaId, String chaveAcesso, String motivo) throws Exception {
        if (chaveAcesso == null || !chaveAcesso.matches("\\d{44}")) {
            throw new IllegalArgumentException("Chave de acesso deve conter 44 dígitos");
        }
        if (motivo == null || motivo.trim().length() < 15 || motivo.trim().length() > 255) {
            throw new IllegalArgumentException("Motivo de cancelamento deve conter entre 15 e 255 caracteres");
        }
        carregarConfiguracoes(empresaId);
        throw new UnsupportedOperationException(
                "Cancelamento NF-e ainda não pode ser transmitido: evento de cancelamento pendente");
    }

    @Override
    public String consultarSituacao(Long empresaId, String chaveAcesso) throws Exception {
        if (chaveAcesso == null || !chaveAcesso.matches("\\d{44}")) {
            throw new IllegalArgumentException("Chave de acesso deve conter 44 dígitos");
        }
        ConfiguracoesNfe config = carregarConfiguracoes(empresaId);
        TRetConsSitNFe retorno = Nfe.consultaXml(config, chaveAcesso, DocumentoEnum.NFE);
        return XmlNfeUtil.objectToXml(retorno);
    }

    private ConfiguracoesNfe carregarConfiguracoes(Long empresaId) throws Exception {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa não encontrada: " + empresaId));
        return carregarConfiguracoes(empresa);
    }

    private ConfiguracoesNfe carregarConfiguracoes(Empresa empresa) throws Exception {
        if (empresa.getUf() == null || empresa.getUf().isBlank()) {
            throw new IllegalStateException("UF da empresa não cadastrada para emissão de NF-e");
        }

        EstadosEnum uf;
        try {
            uf = EstadosEnum.valueOf(empresa.getUf().trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException(
                    "UF da empresa não suportada pela biblioteca NF-e: " + empresa.getUf(), ex);
        }
        return sefazConfig.montar(empresa.getId(), uf);
    }
}
