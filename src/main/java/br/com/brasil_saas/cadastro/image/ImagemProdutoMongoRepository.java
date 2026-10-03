package br.com.brasil_saas.cadastro.image;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ImagemProdutoMongoRepository extends MongoRepository<ImagemProdutoDocumento, String> {
    List<ImagemProdutoDocumento> findByVinculadoFalse();
}
