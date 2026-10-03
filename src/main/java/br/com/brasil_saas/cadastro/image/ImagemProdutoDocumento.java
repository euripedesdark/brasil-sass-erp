package br.com.brasil_saas.cadastro.image;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document("produto_imagens")
@Getter @Setter @NoArgsConstructor
public class ImagemProdutoDocumento {
    @Id private String id;
    private Long empresaId;
    private Long produtoId;
    private String nomeArquivo;
    private String contentType;
    private long tamanho;
    private byte[] conteudo;
    private LocalDateTime criadoEm = LocalDateTime.now();
    private boolean vinculado;
}
