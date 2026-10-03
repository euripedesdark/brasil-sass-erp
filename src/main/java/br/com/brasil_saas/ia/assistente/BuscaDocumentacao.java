package br.com.brasil_saas.ia.assistente;

import br.com.brasil_saas.fiscal.busca.NormalizacaoFiscal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Busca na documentação interna do projeto.
 *
 * <p>É a terceira fonte, depois da fiscal e dos dados. Ela existe porque parte
 * das regras do ERP só está escrita: por que uma coluna é {@code varchar(8)}, o
 * que o watchdog do NFS-e aprendeu com o incidente de 26/09, por que a busca
 * normaliza texto em Java e não em SQL. O modelo não sabe disso, e a resposta
 * fica incompleta sem isso.
 *
 * <p><b>Por que varrer arquivo em vez de embeddings.</b> O projeto tem
 * {@code EmbeddingService}, mas ele indexa o que alguém se deu ao trabalho de
 * mandar indexar. Varrer {@code docs/} na hora é lento demais para ficar no
 * caminho de toda pergunta — e não dá para indexar 400 arquivos de documentação
 * a cada pergunta. A escolha aqui é o oposto: {@link #cacheia()} lê uma vez e
 * guarda em memória, e a busca é sobre o texto normalizado, com a mesma regra da
 * busca fiscal. Embedding é o próximo passo, se a escala pedir.
 */
@Component
public class BuscaDocumentacao {

    private static final int MAXIMO_RESULTADOS = 4;
    private static final int TRECHO = 220;

    @Value("${brasil-saas.ia.assistente.docs-pasta:docs}")
    private String pastaDocs;

    private volatile Cache cache;

    private record Cache(List<Documento> documentos) {
    }

    private record Documento(Path caminho, String relativo, List<String> linhasNormalizadas) {
    }

    public List<Achado> buscar(final String termo) {
        // Só as palavras que dizem algo sobre o assunto. "qual" e "usar" estão
        // em quase toda documentação e em quase toda pergunta: pontuar por elas
        // devolve um monte de arquivo irrelevante com pontuação alta — que foi
        // exatamente a primeira versão, que trouxe README_MODULES.md e
        // COBERTURA_FRONTEND_BACKEND.md para a pergunta "qual NCM para cerveja".
        final List<String> palavras = TermoDeBusca.palavrasRelevantes(termo);
        if (palavras.isEmpty()) {
            return List.of();
        }
        final List<Achado> achados = new ArrayList<>();

        for (final Documento doc : cacheia().documentos()) {
            int pontuacao = 0;
            int primeiraLinha = -1;
            for (int i = 0; i < doc.linhasNormalizadas().size(); i++) {
                final String linha = doc.linhasNormalizadas().get(i);
                int daLinha = 0;
                for (final String p : palavras) {
                    if (linha.contains(p)) {
                        daLinha++;
                    }
                }
                if (daLinha > 0) {
                    pontuacao += daLinha;
                    if (primeiraLinha < 0) {
                        primeiraLinha = i;
                    }
                }
            }
            if (pontuacao > 0) {
                achados.add(Achado.de(
                        "documentacao", "documento",
                        doc.relativo(),
                        trecho(doc, primeiraLinha),
                        "a documentação menciona \"" + String.join(" ", palavras) + "\"",
                        "arquivo do projeto",
                        doc.relativo()));
            }
        }
        achados.sort(Comparator.comparing(Achado::detalhe).reversed());
        return achados.stream().limit(MAXIMO_RESULTADOS).toList();
    }

    private String trecho(final Documento doc, final int linha) {
        try {
            final List<String> brutas = Files.readAllLines(doc.caminho());
            final int fim = Math.min(brutas.size(), linha + 2);
            final StringBuilder sb = new StringBuilder();
            for (int i = Math.max(0, linha); i < fim; i++) {
                final String l = brutas.get(i).trim();
                if (l.startsWith("#") || l.isEmpty()) {
                    continue;
                }
                sb.append(l).append(' ');
                if (sb.length() > TRECHO) {
                    break;
                }
            }
            return sb.toString().trim();
        } catch (IOException e) {
            return "";
        }
    }

    private Cache cacheia() {
        final Cache atual = cache;
        if (atual != null) {
            return atual;
        }
        final List<Documento> docs = new ArrayList<>();
        try (Stream<Path> arquivos = Files.walk(Path.of(pastaDocs))) {
            arquivos.filter(p -> p.toString().endsWith(".md")).forEach(p -> {
                try {
                    final List<String> norm = new ArrayList<>();
                    for (final String l : Files.readAllLines(p)) {
                        norm.add(NormalizacaoFiscal.texto(l));
                    }
                    docs.add(new Documento(p, Path.of(pastaDocs).relativize(p).toString(), norm));
                } catch (IOException ignorada) {
                    // documento ilegível não impede os outros de entrarem
                }
            });
        } catch (IOException e) {
            return new Cache(List.of());
        }
        final Cache novo = new Cache(List.copyOf(docs));
        cache = novo;
        return novo;
    }
}
