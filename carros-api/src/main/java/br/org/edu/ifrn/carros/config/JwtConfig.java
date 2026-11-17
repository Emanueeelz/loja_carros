package br.org.edu.ifrn.carros.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
public class JwtConfig {

    @Bean
    public SecretKey jwtKey(@Value("${app.jwt.secret}") String secret) {
        byte[] chave;
        try {
            chave = Base64.getDecoder().decode(secret);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("JWT_SECRET inválido.", e);
        }
        if (chave.length < 32) {
            throw new IllegalStateException(
                    "JWT_SECRET deve conter pelo menos 32 bytes.");
        }
        return new SecretKeySpec(chave, "HmacSHA256");
    }
    @Bean
    public JwtEncoder jwtEncoder(SecretKey jwtKey) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(jwtKey));
    }
    @Bean
    public JwtDecoder jwtDecoder(SecretKey jwtKey) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withSecretKey(jwtKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();

        decoder.setJwtValidator(
                JwtValidators.createDefaultWithIssuer("carros-api"));
        return decoder;
    }
}
