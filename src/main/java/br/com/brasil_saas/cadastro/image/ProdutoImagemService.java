package br.com.brasil_saas.cadastro.image;

import br.com.brasil_saas.cadastro.model.Produto;
import br.com.brasil_saas.cadastro.model.ProdutoImagem;
import br.com.brasil_saas.cadastro.repository.ProdutoImagemRepository;
import br.com.brasil_saas.cadastro.repository.ProdutoRepository;
import br.com.brasil_saas.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ProdutoImagemService {
    private final ImagemProdutoMongoRepository mongoRepository;
    private final ProdutoRepository produtoRepository;
    private final ProdutoImagemRepository imagemRepository;

    @Transactional
    public ImagemProdutoDocumento salvarUpload(Long produtoId, MultipartFile arquivo, Boolean principal) throws IOException {
        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado"));
        if (arquivo.isEmpty() || arquivo.getContentType() == null || !arquivo.getContentType().startsWith("image/")) {
            throw new IllegalArgumentException("Envie um arquivo de imagem válido");
        }
        if (arquivo.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("A imagem não pode exceder 10 MB");
        }
        return salvar(produto, arquivo.getOriginalFilename(), arquivo.getContentType(), arquivo.getBytes(), Boolean.TRUE.equals(principal));
    }

    @Transactional
    public ImagemProdutoDocumento salvar(Produto produto, String nomeArquivo, String contentType, byte[] conteudo, boolean principal) {
        ImagemProdutoDocumento documento = new ImagemProdutoDocumento();
        documento.setEmpresaId(produto.getEmpresaId());
        documento.setProdutoId(produto.getId());
        documento.setNomeArquivo(nomeArquivo);
        documento.setContentType(contentType);
        documento.setTamanho(conteudo.length);
        documento.setConteudo(conteudo);
        documento.setVinculado(true);
        documento = mongoRepository.save(documento);
        criarReferenciaSql(produto, documento.getId(), principal);
        return documento;
    }

    @Transactional
    public void vincularPendentes() {
        for (ImagemProdutoDocumento documento : mongoRepository.findByVinculadoFalse()) {
            localizarProduto(documento.getNomeArquivo()).ifPresent(produto -> {
                documento.setEmpresaId(produto.getEmpresaId());
                documento.setProdutoId(produto.getId());
                documento.setVinculado(true);
                mongoRepository.save(documento);
                criarReferenciaSql(produto, documento.getId(), false);
            });
        }
    }

    /**
     * Imagens de um produto, na ordem de exibicao.
     *
     * O repository ja tinha a consulta (usada para calcular a ordem do proximo
     * upload), mas nao havia rota: a tela so conseguia ler uma imagem se
     * ainda tivesse o id em maos. Depois de recarregar a pagina, a imagem
     * sumia da tela mesmo continuando gravada.
     */
    public java.util.List<br.com.brasil_saas.cadastro.model.ProdutoImagem> listar(Long produtoId) {
        return imagemRepository.findByProdutoIdAndDeletedAtIsNullOrderByOrdem(produtoId);
    }

    public ImagemProdutoDocumento buscar(String imagemId) {
        return mongoRepository.findById(imagemId)
                .orElseThrow(() -> new ResourceNotFoundException("Imagem não encontrada"));
    }

    public ImagemProdutoDocumento armazenarPendente(String nomeArquivo, String contentType, byte[] conteudo) {
        ImagemProdutoDocumento documento = new ImagemProdutoDocumento();
        documento.setNomeArquivo(nomeArquivo);
        documento.setContentType(contentType);
        documento.setTamanho(conteudo.length);
        documento.setConteudo(conteudo);
        documento.setVinculado(false);
        return mongoRepository.save(documento);
    }

    public java.util.Optional<Produto> localizarProduto(String nomeArquivo) {
        String base = nomeArquivo.replaceFirst("(?i)\\.[^.]+$", "");
        if (base.matches("produto-\\d+")) {
            return produtoRepository.findById(Long.valueOf(base.substring("produto-".length())));
        }
        String codigo = base.split("[_-]", 2)[0];
        return produtoRepository.findFirstByCodigoIgnoreCaseAndDeletedAtIsNull(codigo);
    }

    private void criarReferenciaSql(Produto produto, String imagemId, boolean principal) {
        ProdutoImagem imagem = new ProdutoImagem();
        imagem.setEmpresaId(produto.getEmpresaId());
        imagem.setProduto(produto);
        imagem.setUrl("/api/cadastro/produtos/" + produto.getId() + "/imagens/" + imagemId);
        imagem.setOrdem(imagemRepository.findByProdutoIdAndDeletedAtIsNullOrderByOrdem(produto.getId()).size());
        imagem.setPrincipal(principal);
        imagem.setCreatedAt(LocalDateTime.now());
        imagemRepository.save(imagem);
    }
}
