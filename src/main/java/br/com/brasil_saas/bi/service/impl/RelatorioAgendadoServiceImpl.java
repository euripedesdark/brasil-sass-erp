package br.com.brasil_saas.bi.service.impl;

import br.com.brasil_saas.bi.dto.RelatorioAgendadoRequest;
import br.com.brasil_saas.bi.model.Relatorio;
import br.com.brasil_saas.bi.model.RelatorioAgendado;
import br.com.brasil_saas.bi.repository.RelatorioAgendadoRepository;
import br.com.brasil_saas.bi.repository.RelatorioRepository;
import br.com.brasil_saas.bi.service.RelatorioAgendadoService;
import br.com.brasil_saas.bi.service.RelatorioService;
import br.com.brasil_saas.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class RelatorioAgendadoServiceImpl implements RelatorioAgendadoService {

    private final RelatorioAgendadoRepository relatorioAgendadoRepository;
    private final RelatorioRepository relatorioRepository;
    private final RelatorioService relatorioService;
    private final JavaMailSender mailSender;

    @Override
    @Transactional
    public RelatorioAgendado criar(Long empresaId, RelatorioAgendadoRequest request) {
        Relatorio relatorio = relatorioRepository.findById(request.relatorioId())
                .orElseThrow(() -> new ResourceNotFoundException("Relatorio nao encontrado"));

        if (!relatorio.getEmpresaId().equals(empresaId)) {
            throw new ResourceNotFoundException("Relatorio nao pertence a esta empresa");
        }

        RelatorioAgendado agendado = new RelatorioAgendado();
        agendado.setEmpresaId(empresaId);
        agendado.setRelatorio(relatorio);
        agendado.setNome(request.nome());
        agendado.setDescricao(request.descricao());
        agendado.setFrequencia(request.frequencia());
        agendado.setIntervaloDias(request.intervaloDias());
        agendado.setProximaExecucao(request.proximaExecucao());
        agendado.setAtivo(request.ativo());
        agendado.setEmailDestinatarios(request.emailDestinatarios());
        agendado.setFormato(request.formato());
        agendado.setParametros(objectToJson(request.parametros()));
        agendado.setDataCriacao(LocalDateTime.now());

        return relatorioAgendadoRepository.save(agendado);
    }

    @Override
    @Transactional
    public RelatorioAgendado atualizar(Long empresaId, Long id, RelatorioAgendadoRequest request) {
        RelatorioAgendado agendado = relatorioAgendadoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Relatorio agendado nao encontrado"));

        if (!agendado.getEmpresaId().equals(empresaId)) {
            throw new ResourceNotFoundException("Relatorio agendado nao pertence a esta empresa");
        }

        Relatorio relatorio = relatorioRepository.findById(request.relatorioId())
                .orElseThrow(() -> new ResourceNotFoundException("Relatorio nao encontrado"));
        if (!relatorio.getEmpresaId().equals(empresaId)) {
            throw new ResourceNotFoundException("Relatorio nao pertence a esta empresa");
        }

        agendado.setRelatorio(relatorio);
        agendado.setNome(request.nome());
        agendado.setDescricao(request.descricao());
        agendado.setFrequencia(request.frequencia());
        agendado.setIntervaloDias(request.intervaloDias());
        agendado.setProximaExecucao(request.proximaExecucao());
        agendado.setAtivo(request.ativo());
        agendado.setEmailDestinatarios(request.emailDestinatarios());
        agendado.setFormato(request.formato());
        agendado.setParametros(objectToJson(request.parametros()));

        return relatorioAgendadoRepository.save(agendado);
    }

    @Override
    @Transactional(readOnly = true)
    public RelatorioAgendado buscarPorId(Long empresaId, Long id) {
        RelatorioAgendado agendado = relatorioAgendadoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Relatorio agendado nao encontrado"));

        if (!agendado.getEmpresaId().equals(empresaId)) {
            throw new ResourceNotFoundException("Relatorio agendado nao pertence a esta empresa");
        }

        return agendado;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RelatorioAgendado> listarPorEmpresa(Long empresaId) {
        return relatorioAgendadoRepository.findAllActiveByEmpresaId(empresaId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RelatorioAgendado> listarPendentes(Long empresaId) {
        return relatorioAgendadoRepository.findPendingExecutions(empresaId, LocalDateTime.now());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RelatorioAgendado> listarPorFrequencia(Long empresaId, String frequencia) {
        return relatorioAgendadoRepository.findByEmpresaIdAndFrequencia(empresaId, frequencia);
    }

    @Override
    @Transactional
    public void excluir(Long empresaId, Long id) {
        RelatorioAgendado agendado = relatorioAgendadoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Relatorio agendado nao encontrado"));

        if (!agendado.getEmpresaId().equals(empresaId)) {
            throw new ResourceNotFoundException("Relatorio agendado nao pertence a esta empresa");
        }

        relatorioAgendadoRepository.delete(agendado);
    }

    @Override
    @Transactional
    public void executarAgendado(Long empresaId, Long id) {
        RelatorioAgendado agendado = buscarPorId(empresaId, id);

        try {
            // Executar relatorio
            Map<String, Object> parametros = jsonToObject(agendado.getParametros());
            Map<String, Object> resultado = relatorioService.executarRelatorio(
                    empresaId, 
                    agendado.getRelatorio().getId(), 
                    parametros
            );

            // Gerar arquivo
            ByteArrayOutputStream output = switch (agendado.getFormato().toUpperCase()) {
                case "PDF" -> relatorioService.gerarPdf(empresaId, agendado.getRelatorio().getId(), parametros);
                case "EXCEL" -> relatorioService.gerarExcel(empresaId, agendado.getRelatorio().getId(), parametros);
                case "CSV" -> relatorioService.gerarCsv(empresaId, agendado.getRelatorio().getId(), parametros);
                default -> null;
            };

            // Enviar por email
            if (output != null && agendado.getEmailDestinatarios() != null && !agendado.getEmailDestinatarios().isBlank()) {
                enviarEmail(agendado, output);
            }

            // Atualizar data de execucao
            agendado.setUltimaExecucao(LocalDateTime.now());
            agendado.setProximaExecucao(calcularProximaExecucao(agendado));
            relatorioAgendadoRepository.save(agendado);

        } catch (Exception e) {
            System.err.println("Erro ao executar relatorio agendado ID " + id + ": " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void definirProximaExecucao(Long empresaId, Long id, LocalDateTime proximaExecucao) {
        RelatorioAgendado agendado = buscarPorId(empresaId, id);
        if (proximaExecucao == null) {
            throw new IllegalArgumentException("A proxima execucao e obrigatoria");
        }
        agendado.setProximaExecucao(proximaExecucao);
        relatorioAgendadoRepository.save(agendado);
    }

    @Override
    @Transactional
    public void agendarProximaExecucao(Long empresaId, Long id) {
        RelatorioAgendado agendado = buscarPorId(empresaId, id);
        LocalDateTime proxima = calcularProximaExecucao(agendado);
        agendado.setProximaExecucao(proxima);
        relatorioAgendadoRepository.save(agendado);
    }

    // Executa automaticamente a cada hora
    @Scheduled(fixedRate = 3600000) // 1 hora = 3600000 ms
    @Transactional
    public void executarPendentes() {
        List<RelatorioAgendado> pendentes = relatorioAgendadoRepository.findAllPendingExecutions(LocalDateTime.now());
        
        for (RelatorioAgendado agendado : pendentes) {
            try {
                executarAgendado(agendado.getEmpresaId(), agendado.getId());
            } catch (Exception e) {
                System.err.println("Erro ao executar relatorio agendado ID " + agendado.getId() + ": " + e.getMessage());
            }
        }
    }

    private LocalDateTime calcularProximaExecucao(RelatorioAgendado agendado) {
        LocalDateTime agora = LocalDateTime.now();

        return switch (agendado.getFrequencia().toUpperCase()) {
            case "DIARIO" -> agora.plusDays(1);
            case "SEMANAL" -> agora.plusWeeks(1);
            case "QUINZENAL" -> agora.plusWeeks(2);
            case "MENSAL" -> agora.plusMonths(1);
            case "TRIMESTRAL" -> agora.plusMonths(3);
            case "ANUAL" -> agora.plusYears(1);
            case "CUSTOM" -> agora.plusDays(agendado.getIntervaloDias() != null ? agendado.getIntervaloDias() : 1);
            default -> agora.plusDays(1);
        };
    }

    private void enviarEmail(RelatorioAgendado agendado, ByteArrayOutputStream output) {
        try {
            String[] emails = agendado.getEmailDestinatarios().split(",");
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(emails);
            message.setSubject("Relatorio: " + agendado.getNome());
            message.setText("Segue em anexo o relatorio: " + agendado.getDescricao());
            // TODO: Adicionar anexo (requer MimeMessage)
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Erro ao enviar email: " + e.getMessage());
        }
    }

    private String objectToJson(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return "{}";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) {
                sb.append(",");
            }
            sb.append("\"").append(entry.getKey()).append("\":\"").append(entry.getValue()).append("\"");
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }

    private Map<String, Object> jsonToObject(String json) {
        // Implementacao simplificada - em producao, usar Jackson/ObjectMapper
        Map<String, Object> result = new HashMap<>();
        if (json == null || json.isBlank() || !json.startsWith("{") || !json.endsWith("}")) {
            return result;
        }
        // Remover chaves
        String content = json.substring(1, json.length() - 1);
        String[] pairs = content.split(",");
        for (String pair : pairs) {
            String[] keyValue = pair.split(":", 2);
            if (keyValue.length == 2) {
                String key = keyValue[0].replace("\"", "").trim();
                String value = keyValue[1].replace("\"", "").trim();
                result.put(key, value);
            }
        }
        return result;
    }
}
