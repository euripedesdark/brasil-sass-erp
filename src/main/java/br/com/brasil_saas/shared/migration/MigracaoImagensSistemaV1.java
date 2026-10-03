package br.com.brasil_saas.shared.migration;

import br.com.brasil_saas.shared.image.ImagemDocumento;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Map;

@Component
@ConditionalOnProperty(
        name = "brasil-saas.migracao.imagens.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class MigracaoImagensSistemaV1 implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(MigracaoImagensSistemaV1.class);

    private static final String MIGRATION_ID = "BRASIL_SAAS_IMAGENS_SISTEMA_V1";
    private static final String MIGRATION_COLLECTION = "sistema_migracoes";

    private final MongoTemplate mongoTemplate;

    public MigracaoImagensSistemaV1(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        Query migrationQuery = Query.query(Criteria.where("_id").is(MIGRATION_ID));

        if (mongoTemplate.exists(migrationQuery, MIGRATION_COLLECTION)) {
            return;
        }

        try {
            ImagemDocumento login = carregarImagem(
                    "classpath:/static/images/tela-login.jpeg",
                    "SISTEMA_LOGIN",
                    "tela-login.jpeg",
                    "image/jpeg"
            );

            ImagemDocumento background = carregarImagem(
                    "classpath:/static/images/tela-inicial.jpeg",
                    "SISTEMA_BACKGROUND",
                    "tela-inicial.jpeg",
                    "image/jpeg"
            );

            mongoTemplate.remove(
                    Query.query(Criteria.where("tipoEntidade").in("SISTEMA_LOGIN", "SISTEMA_BACKGROUND")),
                    ImagemDocumento.class
            );

            mongoTemplate.insert(login);
            mongoTemplate.insert(background);

            mongoTemplate.getCollection(MIGRATION_COLLECTION).insertOne(
                    new org.bson.Document()
                            .append("_id", MIGRATION_ID)
                            .append("executadaEm", LocalDateTime.now())
                            .append("versao", 1)
                            .append("imagens", Map.of(
                                    "SISTEMA_LOGIN", "tela-login.jpeg",
                                    "SISTEMA_BACKGROUND", "tela-inicial.jpeg"
                            ))
            );

            log.info("Migração de imagens de sistema concluída (SISTEMA_LOGIN, SISTEMA_BACKGROUND).");
        } catch (IOException e) {
            log.warn(
                    "Imagens de sistema ausentes no classpath ({}). " +
                    "Coloque tela-login.jpeg e tela-inicial.jpeg em src/main/resources/static/images/ " +
                    "e remova o documento {} da coleção {} para reexecutar a migração.",
                    e.getMessage(), MIGRATION_ID, MIGRATION_COLLECTION
            );
        } catch (Exception e) {
            log.error("Falha na migração de imagens de sistema: {}", e.getMessage(), e);
        }
    }

    private ImagemDocumento carregarImagem(
            String resourcePath,
            String tipoEntidade,
            String nomeArquivo,
            String contentType
    ) throws IOException {
        ClassPathResource resource = new ClassPathResource(resourcePath.replace("classpath:/", ""));

        if (!resource.exists() || !resource.isReadable()) {
            throw new IOException("Imagem da migração não encontrada no classpath: " + resourcePath);
        }

        byte[] conteudo;
        try (InputStream inputStream = resource.getInputStream()) {
            conteudo = inputStream.readAllBytes();
        }

        if (conteudo.length == 0) {
            throw new IOException("Imagem da migração está vazia: " + resourcePath);
        }

        ImagemDocumento imagem = new ImagemDocumento();
        imagem.setEmpresaId(null);
        imagem.setTipoEntidade(tipoEntidade);
        imagem.setEntidadeId(null);
        imagem.setNomeArquivo(nomeArquivo);
        imagem.setContentType(contentType);
        imagem.setTamanho(conteudo.length);
        imagem.setConteudo(conteudo);
        imagem.setCriadoEm(LocalDateTime.now());
        imagem.setAtualizadoEm(LocalDateTime.now());

        return imagem;
    }
}
