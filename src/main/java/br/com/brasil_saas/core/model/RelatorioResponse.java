package br.com.brasil_saas.core.model;

import lombok.Data;
import java.util.Map;
import java.util.List;

@Data
public class RelatorioResponse {
    private String nomeRelatorio;
    private String periodo;
    private List<Map<String, Object>> dados;
    private Map<String, Object> resumo; // Totais, Médias, etc.
}
