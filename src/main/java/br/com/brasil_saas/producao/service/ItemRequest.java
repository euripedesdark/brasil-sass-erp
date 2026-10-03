package br.com.brasil_saas.producao.service;

import java.math.BigDecimal;

public record ItemRequest(Long produtoId, BigDecimal quantidade) {}
