package br.com.brasil_saas.shared.image;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Documento binário guardado no MongoDB (coleção "documentos").
 *
 * Companion de {@link ImagemDocumento}, que atende a coleção "imagens".
 * A diferença é o propósito: aqui entram arquivos de negócio (XML de NF-e,
 * PDF de contrato, planilha de fechamento, anexos), não apenas imagens.
 *
 * O conteúdo é o binário em si, nunca um caminho ou link para o disco: se o
 * registro existe, os bytes estão no Mongo.
 */
@Document("documentos")
@CompoundIndex(name = "ix_doc_empresa_tipo_entidade", def = "{'empresaId': 1, 'tipoEntidade': 1, 'entidadeId': 1}")
@Getter
@Setter
@NoArgsConstructor
public class DocumentoArquivo {

    @Id
    private String id;

    /** Tenant dono do documento. Null apenas para recursos globais do sistema. */
    private Long empresaId;

    /** Tipo: nfe_xml, contrato_pdf, anexo_os, fechamento_planilha... */
    private String tipoEntidade;

    /**
     * Modulo dono do documento (financeiro, fiscal, cadastro...).
     *
     * E o que divide a colecao: a entrega filtra por aqui, entao um usuario
     * sem o modulo liberado nao recebe o documento, mesmo que o id dele seja
     * conhecido. Documento sensivel (certificado) fica em modulo com
     * exige_superuser, que so o SUPERUSER enxerga.
     */
    private String modulo;

    /** Id da entidade dona (nota, ordem de serviço, contrato...). */
    private Long entidadeId;

    private String nomeArquivo;

    private String contentType;

    private long tamanho;

    /** Checksum SHA-256 do conteúdo, para deduplicar e detectar corrupção. */
    private String hash;

    /** Binário propriamente dito. */
    private byte[] conteudo;

    private LocalDateTime criadoEm = LocalDateTime.now();

    private LocalDateTime atualizadoEm = LocalDateTime.now();
}
