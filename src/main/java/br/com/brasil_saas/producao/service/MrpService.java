package br.com.brasil_saas.producao.service;
import java.util.List;
import java.util.Map;
public interface MrpService {
 List<Map<String,Object>> simular(Long empresaId, MrpRequest request);
}