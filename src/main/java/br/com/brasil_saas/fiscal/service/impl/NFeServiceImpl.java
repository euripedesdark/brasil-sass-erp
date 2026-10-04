package br.com.brasil_saas.fiscal.service.impl;

import br.com.brasil_saas.fiscal.sefaz.SefazConfig;
import br.com.brasil_saas.fiscal.service.CertificateService;
import br.com.brasil_saas.fiscal.service.NFeService;
import br.com.brasil_saas.fiscal.service.NFeEmissaoValidator;
import br.com.brasil_saas.fiscal.model.Nfe;
import br.com.brasil_saas.fiscal.repository.NfeRepository;
import br.com.brasil_saas.cadastro.repository.ClienteRepository;
import br.com.swconsultoria.nfe.dom.Evento;
import br.com.swconsultoria.nfe.schema.envEventoCancNFe.TEnvEvento;
import br.com.swconsultoria.nfe.schema.envEventoCancNFe.TRetEnvEvento;
import br.com.swconsultoria.nfe.util.CancelamentoUtil;
import br.com.swconsultoria.nfe.util.RetornoUtil;

import java.time.LocalDateTime;
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
    private final NfeRepository nfeRepository;
    private final ClienteRepository clienteRepository;

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
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa não encontrada: " + empresaId));
        ConfiguracoesNfe config = carregarConfiguracoes(empresa);

        Nfe nota = nfeRepository.findByEmpresaIdAndChaveAcessoAndDeletedAtIsNull(empresaId, chaveAcesso)
                .orElseThrow(() -> new IllegalArgumentException("NF-e não encontrada para a empresa: " + chaveAcesso));

        if (nota.getProtocolo() == null || nota.getProtocolo().isBlank()) {
            throw new IllegalStateException("NF-e não possui protocolo de autorização para cancelamento");
        }
        if ("CANCELADA".equalsIgnoreCase(nota.getStatus())) {
            return nota.getProtocolo();
        }
        if (!"AUTORIZADA".equalsIgnoreCase(nota.getStatus())) {
            throw new IllegalStateException("Somente NF-e autorizada pode ser cancelada. Status atual: " + nota.getStatus());
        }

        Evento evento = new Evento();
        evento.setChave(chaveAcesso);
        evento.setProtocolo(nota.getProtocolo());
        evento.setCnpj(normalizarDocumento(empresa.getCnpj(), 14));
        evento.setMotivo(motivo.trim());
        evento.setDataEvento(LocalDateTime.now());

        TEnvEvento enviEvento = CancelamentoUtil.montaCancelamento(evento, config);
        TRetEnvEvento retorno = Nfe.cancelarNfe(config, enviEvento, true, DocumentoEnum.NFE);
        RetornoUtil.validaCancelamento(retorno);

        if (retorno.getRetEvento() == null || retorno.getRetEvento().isEmpty()
                || retorno.getRetEvento().get(0).getInfEvento() == null) {
            throw new IllegalStateException("SEFAZ não retornou o resultado do cancelamento");
        }

        var resultado = retorno.getRetEvento().get(0).getInfEvento();
        nota.setStatus("CANCELADA");
        if (resultado.getNProt() != null && !resultado.getNProt().isBlank()) {
            nota.setProtocolo(resultado.getNProt());
        }
        nota.setXml(br.com.swconsultoria.nfe.util.XmlNfeUtil.objectToXml(retorno));
        nfeRepository.save(nota);

        return resultado.getNProt();
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

    private String normalizarDocumento(String documento, int tamanho) {
        if (documento == null) throw new IllegalArgumentException("CNPJ do emitente não cadastrado");
        String digitos = documento.replaceAll("\\D", "");
        if (digitos.length() != tamanho) throw new IllegalStateException("CNPJ do emitente inválido");
        return digitos;
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
