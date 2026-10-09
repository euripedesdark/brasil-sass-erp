package br.com.brasil_saas.enterprise.service;

import br.com.brasil_saas.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

/** Planejamento de entrega: agrupa entregas em ordens de transporte (uma por carga) com paradas. */
@Service
@RequiredArgsConstructor
public class TmsPlanejamentoService {
    private final JdbcTemplate jdbc;

    @Transactional
    public List<Map<String, Object>> planejar(Long empresaId, String origem, BigDecimal capPeso, BigDecimal capVolume,
                                              Long transportadoraId, LocalDateTime saida,
                                              List<PlanejadorCarga.Entrega> entregas) {
        if (entregas == null || entregas.isEmpty()) throw new BusinessException("Nenhuma entrega informada");
        for (var e : entregas) {
            if (e.referenciaTipo() == null || e.referenciaId() == null) throw new BusinessException("Entrega sem documento de origem");
            Integer ja = jdbc.queryForObject("select count(*) from brasil_saas.bc_tms_parada where empresa_id=? and referencia_tipo=? " +
                    "and referencia_id=? and status<>'CANCELADA'", Integer.class, empresaId, e.referenciaTipo(), e.referenciaId());
            if (ja != null && ja > 0) throw new BusinessException("Documento " + e.referenciaTipo() + " " + e.referenciaId() + " já está planejado");
        }
        List<PlanejadorCarga.Carga> cargas = PlanejadorCarga.planejar(entregas, capPeso, capVolume);
        List<Map<String, Object>> ordens = new ArrayList<>();
        long base = System.currentTimeMillis();
        int n = 1;
        for (var c : cargas) {
            String numero = "TMS-" + base + "-" + n++;
            Long ordemId = jdbc.queryForObject("insert into brasil_saas.bc_tms_ordem(empresa_id,numero,origem,destino,transportadora_id," +
                            "status,data_prevista_saida,peso,volume) values (?,?,?,?,?,'PLANEJADA',?,?,?) returning id",
                    Long.class, empresaId, numero, origem, c.destino(), transportadoraId,
                    saida == null ? null : Timestamp.valueOf(saida), c.peso(), c.volume());
            int seq = 1;
            for (var e : c.entregas()) {
                jdbc.update("insert into brasil_saas.bc_tms_parada(empresa_id,ordem_id,sequencia,tipo,localizacao,status," +
                                "referencia_tipo,referencia_id,peso,volume) values (?,?,?,'ENTREGA',?,'PENDENTE',?,?,?,?)",
                        empresaId, ordemId, seq++, e.destino(), e.referenciaTipo(), e.referenciaId(), e.peso(), e.volume());
            }
            Map<String, Object> o = new LinkedHashMap<>();
            o.put("ordemId", ordemId); o.put("numero", numero); o.put("destino", c.destino());
            o.put("peso", c.peso()); o.put("volume", c.volume()); o.put("paradas", c.entregas().size());
            ordens.add(o);
        }
        return ordens;
    }
}
