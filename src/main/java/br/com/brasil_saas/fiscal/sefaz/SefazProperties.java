package br.com.brasil_saas.fiscal.sefaz;
import lombok.Getter; import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
@Component @ConfigurationProperties(prefix = "brasil-saas.fiscal.sefaz") @Getter @Setter
public class SefazProperties {
    private boolean enabled = false;          // <-- chave mestra: false = não conecta na SEFAZ
    private String ambiente = "HOMOLOGACAO";  // HOMOLOGACAO | PRODUCAO
    private int timeoutMs = 30000;
}
