package br.com.brasil_saas.fiscal.sp;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Confere a geometria da cadeia de assinatura do RPS.
 *
 * <p>Quando a assinatura esta errada, a prefeitura nao diz o que espera: ela
 * devolve o erro <b>1206</b> ecoando a string que ela calculou, e o
 * desenvolvedor precisa casar posicao por posicao. Fixar a geometria em teste
 * evita que o proximo erro custar uma ida a producao.
 */
class NfseSpSignerCadeiaTest {

    /**
     * String que a Prefeitura de Sao Paulo devolveu em 25/09/2026 no erro 1206.
     * E a referencia do que a prefeitura ESPERA receber, posicao a posicao.
     */
    private static final String ESTRUTURA_ESPERADA =
            "02130033BC   000000000001202609251NN00000000000010000000000000000000101100086946749120";

    /** A mesma cadeia, anotada posicao a posicao. */
    private static final String ANOTADA =
            "02130033" +      // 1  IM do prestador, 8 digitos
            "BC   " +         // 2  serie, 5, alinhada a esquerda
            "000000000001" +  // 3  numero do RPS, 12
            "20260925" +      // 4  data AAAAMMDD
            "1" +             // 5  tributacao
            "N" +             // 6  status
            "N" +             // 7  ISS retido
            "000000000000100" + // 8 valor dos servicos, 15
            "000000000000000" + // 9 deducoes, 15
            "00101" +         // 10 codigo do servico, 5 (0101 com zeros a esquerda)
            "1" +             // 11 indicador CPF do tomador
            "00086946749120";  // 12 CPF do tomador, 14 (11 + 3 zeros)

    private final NfseSpSigner signer = new NfseSpSigner(null);

    private NfseSpSigner.DadosAssinaturaRps dados(boolean leiaute2) {
        return new NfseSpSigner.DadosAssinaturaRps(
                "2130033", "BC", "1", "2026-09-25", "1", "N", false,
                "1.00",           // Valor dos Servicos
                leiaute2 ? "1.00" : null, // ValorFinalCobrado (so no leiaute 2)
                "0.00", "0101",
                "86946749120", true,
                null, false, false,
                leiaute2);
    }

    @Test
    void cadeiaDoLeiaute1Tem86PosicoesNaGeometriaCerta() {
        String c = signer.montarAssinaturaRps(dados(false));

        assertEquals(86, c.length(),
                "Itens 1 a 12 do manual 4.3.2 somam 86 posicoes. Obtida: " + c);

        // a prova mais forte: a cadeia inteira tem de ser identica, byte a
        // byte, a string que a prefeitura devolveu no erro 1206
        assertEquals(ESTRUTURA_ESPERADA, c,
                "a cadeia tem de bater com a que a prefeitura calculou");

        assertEquals(ANOTADA.substring(0, 8), c.substring(0, 8), "pos 1: IM com 8");
        assertEquals(ANOTADA.substring(8, 13), c.substring(8, 13), "pos 2: serie a esquerda");
        assertEquals(ANOTADA.substring(13, 25), c.substring(13, 25), "pos 3: numero com 12");
        assertEquals(ANOTADA.substring(25, 33), c.substring(25, 33), "pos 4: AAAAMMDD");
        assertEquals("1", c.substring(33, 34), "pos 5: tributacao");
        assertEquals("N", c.substring(34, 35), "pos 6: status");
        assertEquals("N", c.substring(35, 36), "pos 7: ISS retido");
        assertEquals(ANOTADA.substring(36, 51), c.substring(36, 51), "pos 8: valor dos servicos");
        assertEquals(ANOTADA.substring(51, 66), c.substring(51, 66), "pos 9: deducoes");
        assertEquals(ANOTADA.substring(66, 71), c.substring(66, 71), "pos 10: codigo do servico");
        assertEquals("1", c.substring(71, 72), "pos 11: indicador CPF");
        assertEquals(ANOTADA.substring(72, 86), c.substring(72, 86), "pos 12: CPF com 14");
    }

    /**
     * A posicao 1 e a posicao 8 mudam no leiaute 2. Foi usar a regra do leiaute 1
     * no 2 (ou vice-versa) que produziu o erro 1206.
     */
    @Test
    void leiaute2Usa12DigitosNaImEValorFinalCobrado() {
        String v2 = signer.montarAssinaturaRps(dados(true));
        // no leiaute 2 a posicao 1 tem 4 digitos a mais, entao tudo desloca 4
        final int deslocamento = 4;
        assertEquals("000002130033", v2.substring(0, 12), "pos 1 no leiaute 2: IM com 12");
        assertEquals("000000000000100",
                v2.substring(36 + deslocamento, 51 + deslocamento),
                "pos 8 no leiaute 2: ValorFinalCobrado");

        String v1 = signer.montarAssinaturaRps(dados(false));
        assertEquals("02130033", v1.substring(0, 8), "pos 1 no leiaute 1: IM com 8");
    }

    /**
     * O XSD tipa ISSRetido como boolean, mas a assinatura usa S/N. A versao em
     * Ruby comparava {@code valor.to_s === true}, sempre falso: uma nota COM
     * ISS retido era assinada como se nao tivesse.
     */
    @Test
    void issRetidoViraSQuandoVerdadeiro() {
        NfseSpSigner.DadosAssinaturaRps d = new NfseSpSigner.DadosAssinaturaRps(
                "2130033", "BC", "1", "2026-09-25", "1", "N", true,
                "1.00", null, "0.00", "0101", "86946749120", true, null, false, false, false);

        assertEquals('S', signer.montarAssinaturaRps(d).charAt(35), "pos 7 com ISS retido deve ser S");
    }

    /**
     * Sem intermediario a cadeia tem 86 posicoes. Acessar as posicoes 13 a 15
     * mesmo assim produz uma assinatura que a prefeitura recusa: foi um bug
     * real desta implementacao, pego por este teste.
     */
    @Test
    void semIntermediarioACadeiaParaEm86() {
        assertEquals(86, signer.montarAssinaturaRps(dados(false)).length());
    }

    /** Com intermediario, somam-se as posicoes 13 a 15: 102 no total. */
    @Test
    void comIntermediarioACadeiaTem102() {
        NfseSpSigner.DadosAssinaturaRps d = new NfseSpSigner.DadosAssinaturaRps(
                "2130033", "BC", "1", "2026-09-25", "1", "N", false,
                "1.00", null, "0.00", "0101", "86946749120", true,
                "00000000000192", false, false, false);

        String c = signer.montarAssinaturaRps(d);
        assertEquals(102, c.length(), "86 + 3 posicoes do intermediario");
        assertEquals('2', c.charAt(86), "pos 13: indicador 2 = CNPJ");
        assertEquals("00000000000192", c.substring(87, 101), "pos 14: CNPJ com 14");
        assertEquals('N', c.charAt(101), "pos 15: ISS retido do intermediario");
    }

    /** Tomador nao informado: indicador 3 mais 14 zeros. */
    @Test
    void tomadorNaoInformadoPreencheComZeros() {
        NfseSpSigner.DadosAssinaturaRps d = new NfseSpSigner.DadosAssinaturaRps(
                "2130033", "BC", "1", "2026-09-25", "1", "N", false,
                "1.00", null, "0.00", "0101", null, false, null, false, false, false);

        String c = signer.montarAssinaturaRps(d);
        assertEquals('3', c.charAt(71), "pos 11: indicador 3");
        assertEquals("00000000000000", c.substring(72, 86), "pos 12: 14 zeros");
    }
}
