package br.com.brasil_saas.fiscal.config;

import br.com.swconsultoria.certificado.Certificado;
import br.com.swconsultoria.certificado.CertificadoService;
import br.com.swconsultoria.certificado.exception.CertificadoException;
import br.com.swconsultoria.nfe.dom.ConfiguracoesNfe;
import br.com.swconsultoria.nfe.dom.enuns.AmbienteEnum;
import br.com.swconsultoria.nfe.dom.enuns.DocumentoEnum;
import br.com.swconsultoria.nfe.dom.enuns.EstadosEnum;
import lombok.extern.slf4j.Slf4j;

import java.io.FileNotFoundException;

/**
 * Fábrica de {@link ConfiguracoesNfe} para a biblioteca java-nfe.
 * <p>
 * A emissão real de NF-e está desativada por padrão (ver propriedade
 * {@code brasil-saas.fiscal.sefaz.enabled}). Quando desativada, os métodos de
 * serviço lançam {@link IllegalStateException} com orientação clara.
 */
@Slf4j
public final class DynamicNFeConfig {

    private DynamicNFeConfig() {
    }

    /**
     * Cria as configurações da SEFAZ a partir dos dados da empresa.
     *
     * @param ufCodigo          código IBGE da UF (ex.: 35 para SP)
     * @param ambienteCodigo    1=PRODUCAO, 2=HOMOLOGACAO
     * @param certificadoPath   caminho do .pfx
     * @param certificadoSenha  senha do .pfx
     * @param pastaSchemas      caminho da pasta schemas (pode ser nulo)
     * @return configuração pronta para uso com {@link br.com.swconsultoria.nfe.Nfe}
     * @throws CertificadoException se o certificado não puder ser carregado
     * @throws FileNotFoundException se o arquivo do certificado não existir
     */
    public static ConfiguracoesNfe criar(Integer ufCodigo, String ambienteCodigo,
                                          String certificadoPath, String certificadoSenha,
                                          String pastaSchemas) throws CertificadoException, FileNotFoundException {
        EstadosEnum estado = EstadosEnum.getByCodigoIbge(String.valueOf(ufCodigo));
        if (estado == null) {
            throw new IllegalArgumentException("UF não suportada pela biblioteca java-nfe: " + ufCodigo);
        }
        AmbienteEnum ambiente = AmbienteEnum.getByCodigo(ambienteCodigo);
        if (ambiente == null) {
            throw new IllegalArgumentException("Ambiente deve ser 1 (Produção) ou 2 (Homologação): " + ambienteCodigo);
        }
        Certificado certificado = CertificadoService.certificadoPfx(certificadoPath, certificadoSenha);
        String schemas = pastaSchemas != null ? pastaSchemas : "schemas";
        return ConfiguracoesNfe.criarConfiguracoes(estado, ambiente, certificado, schemas);
    }

    public static DocumentoEnum modeloPadrao() {
        return DocumentoEnum.NFE;
    }
}
