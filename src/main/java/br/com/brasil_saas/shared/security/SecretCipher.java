package br.com.brasil_saas.shared.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Criptografia de segredos armazenados no banco.
 *
 * A chave mestra nunca fica no PostgreSQL. Ela vem de
 * APP_SECURITY_SECRET_ENCRYPTION_KEY e serve apenas para proteger credenciais
 * de integrações, como a chave secreta da Stripe.
 */
@Component
public class SecretCipher {
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_SIZE = 12;
    private static final int TAG_BITS = 128;

    @Value("${app.security.secret-encryption-key:}")
    private String masterKey;

    public String encrypt(String plainText) {
        if (plainText == null || plainText.isBlank()) return null;
        try {
            byte[] iv = new byte[IV_SIZE];
            new SecureRandom().nextBytes(iv);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, key(), new GCMParameterSpec(TAG_BITS, iv));
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            byte[] payload = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, payload, 0, iv.length);
            System.arraycopy(encrypted, 0, payload, iv.length, encrypted.length);
            return Base64.getEncoder().encodeToString(payload);
        } catch (Exception e) {
            throw new IllegalStateException("Não foi possível criptografar a credencial da integração", e);
        }
    }

    public String decrypt(String cipherText) {
        if (cipherText == null || cipherText.isBlank()) return null;
        try {
            byte[] payload = Base64.getDecoder().decode(cipherText);
            if (payload.length <= IV_SIZE) throw new IllegalArgumentException("Payload criptografado inválido");
            byte[] iv = new byte[IV_SIZE];
            byte[] encrypted = new byte[payload.length - IV_SIZE];
            System.arraycopy(payload, 0, iv, 0, IV_SIZE);
            System.arraycopy(payload, IV_SIZE, encrypted, 0, encrypted.length);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key(), new GCMParameterSpec(TAG_BITS, iv));
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Não foi possível descriptografar a credencial da integração", e);
        }
    }

    private SecretKeySpec key() {
        if (masterKey == null || masterKey.isBlank()) {
            throw new IllegalStateException(
                "APP_SECURITY_SECRET_ENCRYPTION_KEY não configurada; não é seguro armazenar credenciais de integração");
        }
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(masterKey.getBytes(StandardCharsets.UTF_8));
            return new SecretKeySpec(digest, "AES");
        } catch (Exception e) {
            throw new IllegalStateException("Não foi possível preparar a chave mestra", e);
        }
    }
}
