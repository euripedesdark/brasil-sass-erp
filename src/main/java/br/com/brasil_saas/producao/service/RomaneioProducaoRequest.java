package br.com.brasil_saas.producao.service;

import java.time.LocalDate;
import java.util.List;

public record RomaneioProducaoRequest(
    String numero,
    Long producaoId,
    LocalDate dataRomaneio,
    String destino,
    Long responsavelId,
    Long veiculoId,
    String status,
    String observacoes,
    List<RomaneioProducaoItemRequest> itens
) {}
