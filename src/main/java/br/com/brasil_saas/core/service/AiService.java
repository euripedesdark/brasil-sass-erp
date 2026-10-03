package br.com.brasil_saas.core.service;

public interface AiService {
    /**
     * Traduz linguagem natural para uma query SQL válida para PostgreSQL.
     *
     * @param naturalLanguage A frase do usuário (ex: "Quantos clientes cadastrei no mês passado?")
     * @param schemaContext Informações sobre as tabelas e colunas para dar contexto à IA
     * @return A query SQL gerada
     */
    String translateNL2SQL(String naturalLanguage, String schemaContext);

    /**
     * Explica o resultado de uma query SQL em linguagem natural.
     *
     * @param query A query executada
     * @param result O resultado retornado do banco
     * @return Uma explicação amigável do resultado
     */
    String explainResult(String query, String result);
}
