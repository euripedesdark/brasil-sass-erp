package br.com.brasil_saas.shared.image;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document("imagens")
@Getter
@Setter
@NoArgsConstructor
public class ImagemDocumento {
    @Id
    private String id;
    
    private Long empresaId;
    
    // Tipo: empresa_logo, usuario_foto, funcionario_foto, cliente_logo, fornecedor_logo
    private String tipoEntidade;
    
    private Long entidadeId;  // ID da entidade associada (empresa, usuario, cliente, etc.)
    
    private String nomeArquivo;
    
    private String contentType;
    
    private long tamanho;
    
    /** Checksum SHA-256 do conteudo, usado para deduplicar a migracao em lote. */
    private String hash;
    
    private byte[] conteudo;
    
    private LocalDateTime criadoEm = LocalDateTime.now();
    
    private LocalDateTime atualizadoEm = LocalDateTime.now();
}