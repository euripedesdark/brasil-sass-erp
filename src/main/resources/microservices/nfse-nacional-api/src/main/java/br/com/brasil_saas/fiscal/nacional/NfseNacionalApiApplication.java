package br.com.brasil_saas.fiscal.nacional;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * API da NFS-e Nacional (LC 1418/2015), contra o Ambiente de Dados Nacional.
 *
 * <p> É a segunda implementacao do contrato {@code /api/nfse-sp}, a mesma que
 * a bridge Ruby atende. O ERP aponta para uma porta e nao sabe qual das duas
 * respondeu: quem chama nao percebe a troca.
 *
 * <p> <b>Por que uma API nova, e nao um parametro na de Sao Paulo.</b> Os dois
 * Web Services nao tem nada em comum alem do nome da rota:
 *
 * <table border="1">
 *   <caption>Prefeitura de SP x nacional</caption>
 *   <tr><th></th><th>Sao Paulo</th><th>Nacional</th></tr>
 *   <tr><td>documento</td><td>RPS</td><td>DPS</td></tr>
 *   <tr><td>formato</td><td>XML SOAP</td><td>JSON</td></tr>
 *   <tr><td>assinatura</td><td>XMLDSig + cadeia de 86 posicoes</td><td>XMLDSig</td></tr>
 *   <tr><td>identificacao</td><td>chave nacional de 44 posicoes</td><td>chave + NSU</td></tr>
 *   <tr><td>ambiente</td><td>um so, sem homologacao</td><td>producao e restrita</td></tr>
 *   <tr><td>IBS/CBS</td><td>nao tem</td><td>tem, e e obrigatorio</td></tr>
 * </table>
 *
 * <p> Um unico servico com dois formatos dentro vira um {@code if} em cada
 * metodo, e o dia que um terceiro municipio entrar com um terceiro layout
 * sobra o caminho de manutencao. Duas implementacoes, mesmo contrato.
 *
 * @see NfseNacionalController contrato que o ERP consome
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class NfseNacionalApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(NfseNacionalApiApplication.class, args);
    }
}
