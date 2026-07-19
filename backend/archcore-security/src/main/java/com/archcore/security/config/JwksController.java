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

    private static final String KEY_ID = "archcore-enc-key";
    private static final String JWE_ALGORITHM = "RSA-OAEP";

    private final RSAPublicKey publicKey;

    public JwksController(RSAPublicKey archcorePublicKey) {
        this.publicKey = archcorePublicKey;
    }

    @GetMapping(value = "/.well-known/jwks.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getJwks() {
        JWKSet jwkSet = new JWKSet(
            new RSAKey.Builder(this.publicKey)
                .keyID(KEY_ID)
                .keyUse(KeyUse.ENCRYPTION)
                .algorithm(new com.nimbusds.jose.Algorithm(JWE_ALGORITHM))
                .build()
        );
        return jwkSet.toJSONObject();
    }
}
