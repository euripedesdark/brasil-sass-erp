package br.com.brasil_saas.financeiro.controller;

/**
 * Dado invalido enviado pelo cliente na geracao de boleto/remessa.
 *
 * Distinta de IllegalArgumentException de proposito: o handler global trata
 * IllegalArgumentException como erro interno (500), e entrada invalida da tela
 * e problema do cliente, entao precisa sair como 400.
 */
public class DadosInvalidosException extends RuntimeException {

    public DadosInvalidosException(String message) {
        super(message);
    }
}
