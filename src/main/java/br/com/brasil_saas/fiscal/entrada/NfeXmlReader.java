package br.com.brasil_saas.fiscal.entrada;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.zip.GZIPInputStream;

/**
 * Le o XML de uma NFe e devolve o que a entrada de estoque precisa.
 *
 * <h3>Por que ler XML em vez de consultar a SEFAZ</h3>
 * Dois motivos, e o segundo o principal:
 * <ol>
 *   <li>A SEFAZ exige o certificado do destinatario e a consulta por chave tem
 *       cota. Ler o arquivo que o fornecedor ja mandou nao gasta cota.</li>
 *   <li>O ERP <b>nao tem integracao com SEFAZ</b>. O {@code NFeServiceImpl} tem
 *       89 linhas e tres TODOs. Nao existe cliente de consulta funcionando,
 *       entao "consultar na SEFAZ" hoje nao e opcao: e escrever do zero.</li>
 * </ol>
 * Ler o XML resolve o caso comum e nao depende da SEFAZ ficar de pe. A
 * consulta por chave fica para quando o cliente de SEFAZ existir.
 *
 * <h3>Os dois formatos que aparecem na pratica</h3>
 * O arquivo pode vir com o prefixo {@code nfe} antes do nome, e o nome do
 * arquivo costuma ser a chave de acesso:
 * <pre>
 *   35260341068753000116550080006464051177461672.xml
 *   35260561412110008997650550000153511149915393-nfe.xml
 *   &lt;?xml version="1.0"?&gt;&lt;nfeProc ...&gt;   ou   &lt;nfe&gt;
 * </pre>
 * Por isso a chave e lida do XML, e o nome do arquivo so e consultado como
 * reserva. O XML e a fonte: confiarem no nome e um caminho curto para cadastrar
 * a nota errada.
 *
 * <h3>A assinatura nao e conferida</h3>
 * Esta classe nao valida a assinatura digital, e isso e uma decisao e nao uma
 * omissao. O XML e assinado pela SEFAZ, com um certificado ICP-Brasil cujo
 * portfolio tem de ser verificado contra a lista de revogados — validacao de
 *assinatura e trabalho de biblioteca, nao de parser. O que este servico garante
 * e o estrutural: se o XML nao parsear, a entrada nao entra.
 *
 * <p>Para uso interno, com nota que o proprio fornecedor entregou, o risco e
 * baixo. Para dar entrada de estoque a partir de um XML de terceiro, isso
 * precisa de validacao de assinatura antes.
 */
@Slf4j
@Service
public class NfeXmlReader {

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /** O que a tela precisa mostrar antes de confirmar a entrada. */
    public record Leitura(
            String chave, String numero, String serie, LocalDate emissao,
            String emitenteCnpj, String emitenteNome,
            String destinatarioCnpj, String destinatarioNome,
            String naturezaOperacao, String cfopNota,
            BigDecimal valorTotal, BigDecimal valorDesconto,
            BigDecimal valorFrete, BigDecimal valorIcms,
            BigDecimal valorIpi, BigDecimal valorPis, BigDecimal valorCofins,
            List<Item> itens) {

        /** Soma dos itens, sem o desconto. E o que a tela compara com {@link #valorTotal}. */
        public BigDecimal somaItens() {
            return itens.stream()
                    .map(Item::valorTotal)
                    .filter(java.util.Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        /**
         * O que o estoque tem que receber: soma dos itens menos o desconto.
         *
         * <p>Existe porque as duas coisas sao numeros diferentes e nenhum dos
         * dois e o erro. Numa das notas de teste a soma dos itens da 377,24 e
         * o {@code vNF} da 293,98, porque a nota tem {@code vDesc} de 83,26.
         * Dar entrada pela soma infla o estoque no valor do desconto; dar
         * entrada item a item pelo {@code vProd} faz o mesmo. O desconto e da
         * nota, entao e subtraido.
         */
        public BigDecimal valorLiquido() {
            return somaItens().subtract(valorDesconto == null ? BigDecimal.ZERO : valorDesconto);
        }
    }

    /**
     * Um item da nota.
     *
     * @param cProd   codigo do produto no fornecedor. Serve para casar com o que
     *                ja existe cadastrado, e e o que o ERP usa para
     *               conciliar.
     * @param ean     codigo de barras, ou null. Casa melhor que o cProd, porque
     *                o mesmo produto recebe codigo de fornecedor diferente em
     *                cada um. Ver {@link #normalizaEan}: "SEM GTIN" nao e um
     *                codigo de barras, e um texto que ocupa o campo.
     * @param cest    CEST do item. So existe a partir da reforma; pode vir vazio.
     * @param ncm     NCM com 8 digitos. O padrao e o que a prefeitura e a
     *                SEFAZ exigem, e o que {@code bc_fis_ncm} guarda.
     */
    public record Item(
            int numeroItem, String cProd, String ean, String descricao,
            String ncm, String cest, String cfop, String unidade,
            BigDecimal quantidade, BigDecimal valorUnitario, BigDecimal valorTotal) {
    }

    /**
     * Le o XML de um arquivo enviado.
     *
     * @param arquivo o XML, compactado ou nao
     */
    public Leitura ler(MultipartFile arquivo) {
        byte[] bruto = descompacta(arquivo);
        String xml = new String(bruto, StandardCharsets.UTF_8);
        return lerXml(xml, arquivo.getOriginalFilename());
    }

    /** Le de um array de bytes. O chamador decide de onde veio. */
    public Leitura ler(byte[] bruto, String nomeOriginal) {
        return lerXml(new String(descompacta(bruto), StandardCharsets.UTF_8), nomeOriginal);
    }

    // ------------------------------------------------------------------ //

    /**
     * Descompacta se for GZIP.
     *
     * <p>Nota exportada por alguns emissores vem em .gz com extensao .xml.
     * Abrir direto da um {@code NotAMarkup} e {@code NullPointerException}
     * quatro linhas adiante, sem falar de gzip.
     */
    private byte[] descompacta(MultipartFile arquivo) {
        try {
            byte[] bruto = arquivo.getBytes();
            return descompacta(bruto);
        } catch (IOException e) {
            throw new IllegalArgumentException("Nao foi possivel ler o arquivo enviado: "
                    + e.getMessage(), e);
        }
    }

    private byte[] descompacta(byte[] bruto) {
        if (bruto == null || bruto.length < 2) return bruto;
        // Cabecalho GZIP: 1f 8b
        boolean gzip = (bruto[0] & 0xff) == 0x1f && (bruto[1] & 0xff) == 0x8b;
        if (!gzip) return bruto;
        try (GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(bruto))) {
            return in.readAllBytes();
        } catch (IOException e) {
            throw new IllegalArgumentException("O XML parece compactado mas nao abriu: "
                    + e.getMessage(), e);
        }
    }

    /**
     * O arquivo enviado e um PDF?
     *
     * <p>Existe porque e o erro mais comum, e a mensagem generica de "nao e um
     * XML" nao ajuda: o usuario mandou o que a prefeitura mandou no e-mail e
     * acha que fez certo. O PDF da DANFE e o documento impresso, sem os dados
     * estruturados que a entrada precisa. Dizer isso em uma frase e mais util
     * do que recusar e deixar o usuario procurando o arquivo certo.
     */
    private boolean ehPdf(String xml, String nome) {
        if (xml != null && xml.startsWith("%PDF")) return true;
        return nome != null && nome.toLowerCase(Locale.ROOT).endsWith(".pdf");
    }

    private Leitura lerXml(String xml, String nomeOriginal) {
        if (xml == null || !xml.contains("<")) {
            throw new IllegalArgumentException("O conteudo enviado nao e um XML de NFe");
        }

        // Sem infNFe nao e NFe. Sem esta checagem, um XML qualquer — um
        // pom.xml, um DANFE em HTML renomeado, uma nota cancelada com o
        // envelope da SEFAZ — passa, devolve um plano com zero itens e zero
        // valores, e o usuario confirma uma nota vazia. A mensagem tem que
        // dizer o que faltou, e nao so recusar: o erro comum aqui e o
        // usuario ter mandado o PDF, e a resposta tem que apontar isso.
        String infNFe = bloco(xml, "infNFe");
        if (infNFe == null) {
            if (ehPdf(xml, nomeOriginal)) {
                throw new IllegalArgumentException(nomeOriginal != null
                        ? "'" + nomeOriginal + "' e um PDF, e nao um XML. A prefeitura manda "
                          + "o XML da nota no e-mail ou no portal; o PDF e so a DANFE "
                          + "impressa, que nao tem os dados estruturados da entrada."
                        : "O arquivo enviado e um PDF, e nao um XML de NFe");
            }
            throw new IllegalArgumentException(nomeOriginal != null
                    ? "'" + nomeOriginal + "' nao tem a tag infNFe, que e o que identifica uma NFe"
                    : "O XML enviado nao tem a tag infNFe, que e o que identifica uma NFe");
        }

        String chave = primeiro(xml, "chNFe", "Chave");
        if (chave == null || chave.length() != 44) {
            // Reserva: o nome do arquivo costuma ser a chave. So quando o XML
            // nao tem. Um NFe sempre tem chNFe, entao chegar aqui significa
            // arquivo trocado.
            chave = chaveDoNome(nomeOriginal);
        }

        String emit = bloco(infNFe, "emit");
        String dest = bloco(infNFe, "dest");

        List<Item> itens = new ArrayList<>();
        int n = 0;
        for (String det : blocos(xml, "det")) {
            n++;
            BigDecimal qtd = decimal(primeiro(det, "qCom", "qUnCom"), BigDecimal.ZERO);
            BigDecimal unit = decimal(primeiro(det, "vUnCom", "vUnNCom"), BigDecimal.ZERO);
            BigDecimal total = decimal(primeiro(det, "vProd", "vUnCom"),
                    qtd.multiply(unit));
            itens.add(new Item(
                    inteiro(primeiro(det, "nItem"), n),
                    primeiro(det, "cProd", "cCodItem"),
                    normalizaEan(primeiro(det, "cEAN", "cEANTrib")),
                    primeiro(det, "xProd", "xNome"),
                    normalizaNcm(primeiro(det, "NCM", "ncm")),
                    primeiro(det, "CEST", "cest"),
                    primeiro(det, "CFOP", "cfop"),
                    primeiro(det, "uCom", "uUnCom"),
                    qtd, unit, total));
        }

        // Os totais ficam em ICMSTot, dentro de infNFe. A busca e pelo bloco e
        // nao pelo documento inteiro porque vICMS, vIPI, vPIS e vCOFINS
        // aparecem TAMBEM dentro de cada item, em ICMS/IPI. Sem o bloco, o
        // valor lido seria o do primeiro item, e o ICMS da nota viria a soma de
        // uma coisa com o total de outra.
        String tot = bloco(infNFe, "ICMSTot");
        if (tot == null) tot = infNFe;

        return new Leitura(
                chave,
                primeiro(infNFe, "nNF", "NNF"),
                primeiro(infNFe, "serie", "nNFSerie"),
                data(primeiro(infNFe, "dhEmi", "dEmi", "dhEmissao")),
                soDigitos(emit == null ? null : primeiro(emit, "CNPJ")),
                emit == null ? null : primeiro(emit, "xNome", "xFant"),
                soDigitos(dest == null ? null : primeiro(dest, "CNPJ", "CPF")),
                primeiro(dest, "xNome", "Nome"),
                primeiro(infNFe, "natOp", "NaturezaOperacao"),
                itens.isEmpty() ? null : itens.get(0).cfop(),
                decimal(primeiro(tot, "vNF"), BigDecimal.ZERO),
                decimal(primeiro(tot, "vDesc"), BigDecimal.ZERO),
                decimal(primeiro(tot, "vFrete"), BigDecimal.ZERO),
                decimal(primeiro(tot, "vICMS"), BigDecimal.ZERO),
                decimal(primeiro(tot, "vIPI"), BigDecimal.ZERO),
                decimal(primeiro(tot, "vPIS"), BigDecimal.ZERO),
                decimal(primeiro(tot, "vCOFINS"), BigDecimal.ZERO),
                itens);
    }

    /**
     * Codigo de barras, ou null quando o campo nao tem um.
     *
     * <p>Quando o produto nao tem GTIN, o emissor escreve um texto no lugar —
     * {@code SEM GTIN} na nota de teste da farmácia, mas {@code SEM CODIGO},
     * {@code NAO SE APLICA} e outros aparecem em notas de outros emissores. O
     * campo e obrigatorio no XSD, entao nao pode ficar vazio, e o emissor
     * preenche com o que tem.
     *
     * <p>Guardar esse texto como codigo de barras tem duas consequencias ruins:
     * ele casa com qualquer outro produto que veio da mesma nota, porque o
     * valor e identico; e ele aparece na etiqueta. Nenhum dos dois e
     * desejavel, entao o que nao for digito vira null. A busca por GTIN e a
     * que sempre deve ter o cProd como segunda opcao.
     */
    private String normalizaEan(String ean) {
        if (ean == null) return null;
        // Um GTIN de verdade e so digito. Qualquer letra significa que o
        // emissor escreveu um texto no lugar.
        String v = ean.trim();
        return v.matches("\\d{8,14}") ? v : null;
    }

    /**
     * NCM para 8 digitos, so quando o original tem 8.
     *
     * <p>Complementar com zero <b>nao</b> e o que se deve fazer. {@code 847130}
     * no XSD quer dizer que e um codigo de 6 digitos aceito, e o valor real
     * depende do item. Preencher vira {@code 84713000}, que e outro produto.
     * Deixar como veio, e avisar na tela o que nao tem 8 digitos.
     */
    private String normalizaNcm(String ncm) {
        String v = soDigitos(ncm);
        return v.isEmpty() ? null : v;
    }

    /** Extrai a chave do nome do arquivo, se o nome for a chave. */
    private String chaveDoNome(String nome) {
        if (nome == null) return null;
        String digitos = nome.replaceAll("\\D", "");
        // 44 Certain: a chave. Mais que isso, o nome tem outros numeros e
        // escolher o primeiro bloco de 44 seria chute.
        return digitos.length() == 44 ? digitos : null;
    }

    // --- acesso a tag ------------------------------------------------- //

    private String primeiro(String xml, String... tags) {
        if (xml == null) return null;
        for (String tag : tags) {
            String v = tag(xml, tag);
            if (v != null) return v;
        }
        return null;
    }

    private String tag(String xml, String tag) {
        // O nome da tag pode ter prefixo de namespace (ex: nfe:NCM).
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("<(?:[A-Za-z0-9_.-]+:)?" + java.util.regex.Pattern.quote(tag)
                        + "\\b[^>]*>(.*?)</(?:[A-Za-z0-9_.-]+:)?" + java.util.regex.Pattern.quote(tag) + ">",
                        java.util.regex.Pattern.DOTALL)
                .matcher(xml);
        return m.find() ? limpa(m.group(1)) : null;
    }

    /** O conteudo de um elemento que abre e fecha, com aninhamento. */
    private String bloco(String xml, String tag) {
        if (xml == null) return null;
        java.util.regex.Matcher abre = java.util.regex.Pattern
                .compile("<" + tag + "\\b[^>]*>").matcher(xml);
        if (!abre.find()) return null;
        int inicio = abre.end();
        int fim = indiceFechamento(xml, tag, inicio);
        return fim < 0 ? null : xml.substring(inicio, fim);
    }

    private int indiceFechamento(String xml, String tag, int inicio) {
        java.util.regex.Pattern abre = java.util.regex.Pattern
                .compile("<" + tag + "\\b[^>]*>");
        java.util.regex.Pattern fecha = java.util.regex.Pattern
                .compile("</" + tag + ">");
        int nivel = 1, i = inicio;
        while (nivel > 0) {
            MatcherHelper h = MatcherHelper.avanca(xml, abre, fecha, i);
            if (h.fim) break;
            nivel += h.abriu ? 1 : -1;
            i = h.posicao;
        }
        return nivel == 0 ? i : -1;
    }

    private List<String> blocos(String xml, String tag) {
        List<String> achados = new ArrayList<>();
        if (xml == null) return achados;
        java.util.regex.Pattern abre = java.util.regex.Pattern
                .compile("<" + tag + "\\b[^>]*>");
        java.util.regex.Matcher m = abre.matcher(xml);
        while (m.find()) {
            int fim = indiceFechamento(xml, tag, m.end());
            if (fim > m.end()) achados.add(xml.substring(m.end(), fim));
        }
        return achados;
    }

    private static String limpa(String v) {
        return v == null ? null : v.replaceAll("<[^>]+>", "").trim();
    }

    private String soDigitos(String v) {
        return v == null ? null : v.replaceAll("\\D", "");
    }

    private BigDecimal decimal(String v, BigDecimal padrao) {
        if (v == null || v.isBlank()) return padrao;
        try {
            return new BigDecimal(v.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            log.warn("valor numerico ilegivel: '{}'", v);
            return padrao;
        }
    }

    private int inteiro(String v, int padrao) {
        try {
            return Integer.parseInt(v);
        } catch (Exception e) {
            return padrao;
        }
    }

    private LocalDate data(String v) {
        if (v == null || v.isBlank()) return null;
        // A NFe usa AAAA-MM-DDThh:mm:ss-03:00
        try {
            return LocalDate.parse(v.substring(0, 10), DATA);
        } catch (Exception e) {
            try {
                return LocalDate.parse(v.substring(0, 8), DateTimeFormatter.ofPattern("ddMMyyyy"));
            } catch (Exception e2) {
                return null;
            }
        }
    }

    /** Casa a proxima abertura ou fechamento a partir de uma posicao. */
    private static final class MatcherHelper {
        final boolean abriu, fim;
        final int posicao;

        MatcherHelper(boolean abriu, boolean fim, int posicao) {
            this.abriu = abriu; this.fim = fim; this.posicao = posicao;
        }

        static MatcherHelper avanca(String xml, java.util.regex.Pattern abre,
                                   java.util.regex.Pattern fecha, int de) {
            java.util.regex.Matcher a = abre.matcher(xml);
            java.util.regex.Matcher f = fecha.matcher(xml);
            boolean achouA = a.find(de);
            boolean achouF = f.find(de);
            if (!achouA && !achouF) return new MatcherHelper(false, true, xml.length());
            if (achouA && (!achouF || a.start() < f.start())) {
                return new MatcherHelper(true, false, a.end());
            }
            return new MatcherHelper(false, false, f.end());
        }
    }
}
