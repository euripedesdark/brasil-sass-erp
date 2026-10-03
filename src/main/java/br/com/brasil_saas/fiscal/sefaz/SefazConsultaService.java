package br.com.brasil_saas.fiscal.sefaz;

import br.com.swconsultoria.nfe.Nfe;
import br.com.swconsultoria.nfe.dom.ConfiguracoesNfe;
import br.com.swconsultoria.nfe.dom.enuns.ConsultaDFeEnum;
import br.com.swconsultoria.nfe.dom.enuns.DocumentoEnum;
import br.com.swconsultoria.nfe.dom.enuns.EstadosEnum;
import br.com.swconsultoria.nfe.dom.enuns.PessoaEnum;
import br.com.swconsultoria.nfe.schemas.RetDistDFeInt;
import br.com.swconsultoria.nfe.schemas.TRetConsSitNFe;
import br.com.swconsultoria.nfe.schemas.TRetConsStatServ;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Consulta de notas na SEFAZ usando certificado (java-nfe / Samuel Oliveira).
 * Compativel com java-nfe 4.1.x: todas as classes de schema ficam em
 * br.com.swconsultoria.nfe.schemas e a distribuicao DF-e e feita por enums.
 */
@Slf4j @Service @RequiredArgsConstructor
public class SefazConsultaService {
    private final SefazConfig sefazConfig;

    public TRetConsStatServ statusServico(Long empresaId, EstadosEnum uf) throws Exception {
        ConfiguracoesNfe config = sefazConfig.montar(empresaId, uf);
        return Nfe.statusServico(config, DocumentoEnum.NFE);
    }

    public TRetConsSitNFe consultarPorChave(Long empresaId, EstadosEnum uf, String chaveAcesso) throws Exception {
        ConfiguracoesNfe config = sefazConfig.montar(empresaId, uf);
        return Nfe.consultaXml(config, chaveAcesso, DocumentoEnum.NFE);
    }

    /** Distribuicao DF-e: busca notas destinadas ao CNPJ (por ultNSU ou por chave). */
    public RetDistDFeInt distribuicaoDFe(Long empresaId, EstadosEnum ufAutor, String cnpj,
                                         String ultNsu, String chaveAcesso) throws Exception {
        ConfiguracoesNfe config = sefazConfig.montar(empresaId, ufAutor);
        boolean porChave = chaveAcesso != null && !chaveAcesso.isBlank();
        ConsultaDFeEnum tipo = porChave ? ConsultaDFeEnum.CHAVE : ConsultaDFeEnum.NSU;
        String valor = porChave ? chaveAcesso : (ultNsu == null ? "000000000000000" : ultNsu);
        return Nfe.distribuicaoDfe(config, PessoaEnum.JURIDICA, cnpj, tipo, valor);
    }
}
