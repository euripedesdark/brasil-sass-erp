package br.com.brasil_saas.producao.service;
import java.math.BigDecimal;
public record MrpRequest(Long produtoId, BigDecimal quantidade) {}