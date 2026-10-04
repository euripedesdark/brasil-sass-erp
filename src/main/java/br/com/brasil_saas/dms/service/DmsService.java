package br.com.brasil_saas.dms.service;
import br.com.brasil_saas.dms.model.*;
import java.util.List;
import java.util.Map;
public interface DmsService {
    List<DmsDocumento> documentos(Long empresaId, String categoria, String status);
    DmsDocumento salvar(Long empresaId, DmsDocumento d);
    DmsVersao novaVersao(Long empresaId, Long documentoId, String nomeArquivo, String contentType, byte[] conteudo, String comentario);
    List<DmsVersao> versoes(Long empresaId, Long documentoId);
    Map<String, Object> download(Long empresaId, Long versaoId);
    DmsAprovacao solicitarAprovacao(Long empresaId, Long documentoId, Integer versao, String aprovador);
    List<DmsAprovacao> aprovacoes(Long empresaId, Long documentoId);
    DmsAprovacao decidir(Long empresaId, Long userId, Long aprovacaoId, boolean aprovar, String comentario);
    List<DmsDocumento> retencao(Long empresaId);
    void excluir(Long empresaId, Long id);
}
