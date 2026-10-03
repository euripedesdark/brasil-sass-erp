package br.com.brasil_saas.fiscal.busca;

import br.com.brasil_saas.fiscal.model.PalavraChave;
import br.com.brasil_saas.fiscal.repository.PalavraChaveRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Busca de código fiscal que o usuário faz sem saber o código.
 *
 * <p>O cadastro fiscal hoje só tem descrição, e no ISSQN nem isso. Quem cadastra
 * um café não sabe que é 09012100; sabe que é café. E a evidência de que isso
 * custa caro está no próprio banco: 4 produtos com NCM {@code 1905.21} (arroz,
 * que é capítulo 10) e 7 medicamentos em {@code 3004.90}, que tem 68 códigos
 * possíveis.
 *
 * <p><b>O ranking é calculado aqui e a tela só exibe.</b> Cada resultado volta
 * com o motivo da correspondência. Sem isso a busca vira adivinhação, e ninguém
 * confia numa busca em que não entende o resultado.
 */
@Service
@RequiredArgsConstructor
public class BuscaFiscalService {

    /**
     * Peso de cada nível. O código manda mais que a palavra porque quem digita
     * {@code 22030000} sabe o que quer; quem digita {@code cerveja} está
     * descobrindo, e precisa ver o motivo para confiar.
     */
    private static final int PESO_CODIGO_EXATO = 100;
    private static final int PESO_CODIGO_PREFIXO = 90;
    private static final int PESO_PALAVRA_EXATA = 80;
    private static final int PESO_PALAVRA_PREFIXO = 70;

    /** Abaixo disso o termo é curto demais para ser palavra. */
    private static final int MINIMO_PALAVRA = 3;

    private final PalavraChaveRepository palavras;
    private final ConsultaCatalogo catalogo;

    @Transactional(readOnly = true)
    public List<Resultado> buscar(final String termo, final String tabela, final int limite) {
        if (!NormalizacaoFiscal.temConteudo(termo)) {
            return List.of();
        }
        final int teto = limite <= 0 ? 20 : Math.min(limite, 100);
        final Map<String, Resultado> melhores = new LinkedHashMap<>();

        // 1 e 2: o código. A chave é o código normalizado, para "2203.00",
        // "220300" e "22030000" caírem no MESMO resultado em vez de aparecerem
        // três vezes — o que aconteceria se a chave fosse o texto digitado.
        final String cod = NormalizacaoFiscal.codigo(termo);
        if (!cod.isEmpty()) {
            for (final Catalogo c : Catalogo.ativos(tabela)) {
                for (final ConsultaCatalogo.Registro r : catalogo.porCodigo(c.tabelaFisica(), cod)) {
                    mistura(melhores, c.tabela() + ":" + r.codigo(), c, r,
                            pontuacaoDe(r, cod), motivoDe(r, cod));
                }
            }
        }

        // 3 e 4: a palavra. Exata primeiro, prefixo depois: "cerveja" é palavra
        // e "cervej" é começo de palavra.
        final String txt = NormalizacaoFiscal.texto(termo);
        if (!txt.isEmpty()) {
            for (final PalavraChave p : palavras.porPalavraExata(txt)) {
                if (Catalogo.aceita(p.getTabela(), tabela)) {
                    misturaPorPalavra(melhores, p, p.getPalavra(),
                            PESO_PALAVRA_EXATA + p.getPeso(), txt);
                }
            }
            if (txt.length() >= MINIMO_PALAVRA) {
                for (final PalavraChave p : palavras.porPrefixo(txt)) {
                    if (Catalogo.aceita(p.getTabela(), tabela)) {
                        misturaPorPalavra(melhores, p, p.getPalavra(),
                                PESO_PALAVRA_PREFIXO + p.getPeso(), txt);
                    }
                }
            }
        }

        return melhores.values().stream()
                .sorted(Comparator.comparingInt((Resultado r) -> r.pontuacao()).reversed()
                        .thenComparing(Resultado::codigo))
                .limit(teto)
                .toList();
    }

    private void misturaPorPalavra(final Map<String, Resultado> mapa, final PalavraChave p,
                                   final String palavra, final int base, final String termo) {
        final Catalogo c = Catalogo.para(p.getTabela());
        final Optional<ConsultaCatalogo.Registro> reg =
                catalogo.porCodigoExato(c.tabelaFisica(), p.getCodigo());
        if (reg.isEmpty()) {
            return;
        }
        final String chave = c.tabela() + ":" + p.getCodigo();
        final Resultado r = new Resultado(c.tabela(), p.getCodigo(), reg.get().descricao(),
                base, "palavra-chave: " + palavra + (palavra.equals(termo) ? "" : " (prefixo)"));
        final Resultado atual = mapa.get(chave);
        if (atual == null || r.pontuacao() > atual.pontuacao()) {
            mapa.put(chave, r);
        }
    }

    private int pontuacaoDe(final ConsultaCatalogo.Registro r, final String codNormalizado) {
        final String doRegistro = NormalizacaoFiscal.codigo(r.codigo());
        return doRegistro.equals(codNormalizado) ? PESO_CODIGO_EXATO : PESO_CODIGO_PREFIXO;
    }

    private String motivoDe(final ConsultaCatalogo.Registro r, final String codNormalizado) {
        return pontuacaoDe(r, codNormalizado) == PESO_CODIGO_EXATO
                ? "código exato"
                : "prefixo do código";
    }

    /**
     * Guarda o melhor resultado de cada código. Sem isso "malte" devolveria o
     * mesmo 22030000 várias vezes, uma por descrição em que a palavra aparece.
     */
    private void mistura(final Map<String, Resultado> mapa, final String chave, final Catalogo c,
                         final ConsultaCatalogo.Registro r, final int pontos, final String motivo) {
        final Resultado novo = new Resultado(c.tabela(), r.codigo(), r.descricao(), pontos, motivo);
        final Resultado atual = mapa.get(chave);
        if (atual == null || novo.pontuacao() > atual.pontuacao()) {
            mapa.put(chave, novo);
        }
    }

    /** Uma linha da resposta: o código, e por que ele apareceu. */
    public record Resultado(String tabela, String codigo, String descricao,
                            int pontuacao, String motivo) {
    }

    /**
     * Os três cadastros que a busca atende.
     */
    public enum Catalogo {

        NCM("ncm", "bc_fis_ncm"),
        ISSQN("issqn", "bc_fis_issqn"),
        CFOP("cfop", "bc_fis_cfop");

        private static final Map<String, Catalogo> POR_NOME = new java.util.HashMap<>();

        static {
            for (final Catalogo c : values()) {
                POR_NOME.put(c.tabela, c);
            }
        }

        private final String tabela;
        private final String tabelaFisica;

        Catalogo(final String tabela, final String tabelaFisica) {
            this.tabela = tabela;
            this.tabelaFisica = tabelaFisica;
        }

        public String tabela() {
            return tabela;
        }

        public static boolean aceita(final String doCatalogo, final String filtro) {
            return filtro == null || filtro.isBlank() || doCatalogo.equalsIgnoreCase(filtro);
        }

        public static List<Catalogo> ativos(final String filtro) {
            return List.of(values()).stream().filter(c -> aceita(c.tabela, filtro)).toList();
        }

        public static Catalogo para(final String tabela) {
            final Catalogo c = POR_NOME.get(tabela.toLowerCase());
            if (c == null) {
                throw new IllegalArgumentException("Catalogo fiscal desconhecido: " + tabela);
            }
            return c;
        }

        public String tabelaFisica() {
            return tabelaFisica;
        }
    }
}
