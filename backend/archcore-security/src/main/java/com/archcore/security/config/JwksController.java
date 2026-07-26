package com.archcore.security.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.interfaces.RSAPublicKey;
import java.util.Map;

@RestController
public class JwksController {

    private final RSAPublicKey publicKey;
    private final JweProperties jweProperties;

    public JwksController(RSAPublicKey archcorePublicKey, JweProperties jweProperties) {
        this.publicKey = archcorePublicKey;
        this.jweProperties = jweProperties;
    }

    @GetMapping(value = "/.well-known/jwks.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getJwks() {
        JWKSet jwkSet = new JWKSet(
            new RSAKey.Builder(this.publicKey)
                .keyID(jweProperties.getKeyId())
                .keyUse(KeyUse.ENCRYPTION)
                .algorithm(new com.nimbusds.jose.Algorithm(jweProperties.getJweAlgorithm()))
                .build()
        );
        return jwkSet.toJSONObject();
    }
}
