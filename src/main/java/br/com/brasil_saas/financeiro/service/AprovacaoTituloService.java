package br.com.brasil_saas.financeiro.service;

import br.com.brasil_saas.financeiro.dto.FinanceiroDtos.*;
import java.util.List;

/**
 * Workflow de aprovação de títulos financeiros.
 * Cobre o item 1 da Fase 1 do Relatório de Paridade Funcional ERP (25/09/2026):
 * motor de aprovação com níveis, segregação de funções e trilha de auditoria.
 */
public interface AprovacaoTituloService {

    /** Solicita aprovação de um título (ABERTO → PENDENTE_APROVACAO). */
    List<AprovacaoResponse> solicitar(Long empresaId, Long usuarioId, Long tituloId, SolicitacaoAprovacaoRequest request);

    /** Aprova o nível corrente da solicitação. Ao concluir todos os níveis, libera o título (→ ABERTO). */
    AprovacaoResponse aprovar(Long empresaId, Long usuarioId, Long aprovacaoId, DecisaoAprovacaoRequest request);

    /** Rejeita a solicitação: cancela pendências restantes e devolve o título ao status ABERTO com trilha. */
    AprovacaoResponse rejeitar(Long empresaId, Long usuarioId, Long aprovacaoId, DecisaoAprovacaoRequest request);

    /** Lista aprovações pendentes visíveis ao usuário (fila de trabalho). */
    List<AprovacaoResponse> pendentes(Long empresaId, Long usuarioId);

    /** Histórico/trilha de aprovação de um título. */
    List<AprovacaoResponse> porTitulo(Long empresaId, Long tituloId);
}
