package com.archcore.security.config;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.ResourceLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JweDecryptionIntegrationTest {

    private static final String PRIVATE_KEY_PATH = "keys/test-private.pem";
    private static final String PUBLIC_KEY_PATH = "keys/test-public.pem";

    private static final String ACTUAL_JWE_TOKEN =
            "eyJraWQiOiJhcmNoY29yZS1lbmMta2V5IiwiZW5jIjoiQTI1NkdDTSIsImFsZyI6IlJTQS1PQUVQLTI1NiJ9" +
                    ".IQazzLOVnzfWW-DPZRK25W43Q4WKoO74SNigOhz2u6c5WAteGIlo1Xe5LKMrfHy_C_rZUHjk4A-14TG4U9t2dR8AWo3h-0jq6Rm" +
                    "_xgcgUfqam4W0qJ5k-ctEoWLSEwDP-wUjzOwBYYhgp8dMGCDskvU3x6eT5HR6zlkOZoGD0DHbX-0ez_3rI1I-VUGVTw6i3v4sKk" +
                    "gEZP64NdTMgyv_fueZchW6_b9g6cXm-tS0jZXSNjPycw09LdxvTc2p04UIogQIrdKzYGPeXBgeosKQI4JXxjjVbfCckNY9hflCw" +
                    "RpLy88izhPAeD7CTUkQBAlbcG0NmW-UQzM_SnPAQ4BHaA" +
                    ".dja82wJkZi9uevyB" +
                    ".3F1yXTYwGiYggjmImh9bGiTkEBC6rMzZ__nnxPY42HYxrXgvYjU68jI3xFZAYlPd4ZouIjNxOk_IyWgPPVofS3pzOg67o7LiFy" +
                    "kYiAIte3ZCEh2ppdej2Vq_26GAkY7Y3aP7yHrr8EdFe48LXMicWY4gDcWcWHvvlQHVbqneL4KXfVuejvZRoAmWtbaXxEXI3Nnrz" +
                    "46ziVRitCInM5JRbNgiYhxPUiexY8-nlGaAmvwj4VmSgjMtKOSx-KSdjWyzLlD7GheBOT9ucl6TwwxYDKEj5KkE1NYxsG3y9b2VD" +
                    "eMyYdG60YZ_y8ToGLPE8QK0ITw9ZK5bHRwxY9hq1CgTf1Zs0lRl5UWfVVlxz_xy2HYDW6N6cAcuQLsBrR28I8Umj-0P_qXcFoBu" +
                    "UJJtciIyuA23kxoNNyPEkZj8-jJRNIswZZmuVC5oFVnrv8YwKBmnJvnX_ggvEmH0GxgtNxh-PelpJuIgZPfPmMqkRKufwSPtdRWm" +
                    "AH5PyUWZMEpPuDnb2swDF96nLodv4xpY9iTko40QMryyv8usjIv7Ogm-sfSJosX6DNO2iudr_QkrJTQtxSAYqoITVsxJz0sWOluU" +
                    "hzuJiEjjyW7Aac6gucs4YQtAtlpnsoI3pdr40mNoUp2gKqTe1PJRnqy7An5ImuU0PJTU06WXxtgnsNxamJPYJHDjZhNgwkQkFcPN" +
                    "Z-p3osvB82yoOSgct2oalcfvIK3TSxACm4TpDwBOGMinAeQYcn7Y4tg5P8XzK3798fulXaoYOT8C_g98JDmAqK8jwm3KncGR1zho" +
                    "ntRZl2vnzkLEx5ZwK5yoNlZDZARnmanieo-x9pGE4evS8I5ogkZYVmlT60i-Lu8Yn11iatGISTF_noH-7Fw_bJmRSRcR0VT-jsKM" +
                    "L79LzQMrw4fzgLx_1YErKnoGp-nMB6STFfwF5CpgZ0oTBE7wUR_DsC-t-eWpsTjhhfaw3PTlgLfColvkKRCOpFayLTv_HHv5v0-" +
                    "0_KwG3OBWYyCw0NAyUA7MLXv6BQtyJZHIiOoot0ejJgt2nQMAqZ27cllBjO6czLnZLa4QjRDpSfiznGkDRjOULgu6J5foa0XbyZt" +
                    "pbv1p1EAdOvDcq9hPr78nvamFZM4miXSzdR9UyM42Twc3dmV4ud8Cc6st_RBBnhYYSoCMgx9FcJdqruXk6GIB73xMftkh8OG92Wk" +
                    "26Wl70kJ5C1Tfs61uk6Ucsr58EaH_o5OH5G9x-wejyVUhYHfGqxw2b6y4UhBx1JafXuCh8acDrlGEPv0ClSPn8TJXgNEOy3xnkO" +
                    "lQV7qkp9YwljD_hSS6_5HcmVCbQqPDC9usxXUnO7lKL6lf1Fk6YIrGfHwbCt_1HOcXFqE0scUKXiL4Z35lfcu_NbZ_Kmu5sX3LG" +
                    "iZISciDkpRbAWu3fPrJj98ggGWQ_8zd7x6Jfdl4ObqN8YiQIlW4tqDDyTts9IwOkSGSnrNauQ" +
                    ".rlWI3c1VvJqIYkGR2UbVzg";

    private ResourceLoader resourceLoader;
    private JweProperties jweProperties;

    @BeforeEach
    void setUp() {
        resourceLoader = mock(ResourceLoader.class);
        jweProperties = new JweProperties();
        when(resourceLoader.getResource(any(String.class)))
                .thenAnswer(invocation -> {
                    String location = invocation.getArgument(0);
                    return new ClassPathResource(location.replace("classpath:", ""));
                });
    }

    @Test
    void shouldDecryptRealJweTokenAndParseInnerJwt() throws Exception {
        RSAPrivateKey privateKey = loadPrivateKey(PRIVATE_KEY_PATH);
        RSAPublicKey publicKey = loadPublicKey(PUBLIC_KEY_PATH);

        System.out.println("=== Step 1: Parse JWE Header ===");
        JWEObject jweObject = JWEObject.parse(ACTUAL_JWE_TOKEN);
        assertEquals(JWEAlgorithm.RSA_OAEP_256, jweObject.getHeader().getAlgorithm());
        assertEquals(EncryptionMethod.A256GCM, jweObject.getHeader().getEncryptionMethod());
        assertEquals("archcore-enc-key", jweObject.getHeader().getKeyID());
        System.out.println("JWE header valid: RSA-OAEP-256 / A256GCM / kid=archcore-enc-key");

        System.out.println("\n=== Step 2: Decrypt JWE with private key ===");
        jweObject.decrypt(new RSADecrypter(privateKey));
        String decryptedPayload = jweObject.getPayload().toString();
        assertNotNull(decryptedPayload);
        assertTrue(decryptedPayload.startsWith("eyJ"), "Decrypted payload should be a JWT (starts with eyJ)");
        System.out.println("Decrypted payload (first 100 chars): " + decryptedPayload.substring(0, Math.min(100, decryptedPayload.length())));

        System.out.println("\n=== Step 3: Parse inner SignedJWT ===");
        SignedJWT signedJWT = SignedJWT.parse(decryptedPayload);
        assertNotNull(signedJWT.getHeader());
        assertNotNull(signedJWT.getJWTClaimsSet());
        System.out.println("JWT header: " + signedJWT.getHeader().toJSONObject());

        System.out.println("\n=== Step 4: Inspect claims ===");
        var claims = signedJWT.getJWTClaimsSet();
        System.out.println("Subject: " + claims.getSubject());
        System.out.println("Issuer: " + claims.getIssuer());
        System.out.println("Audience: " + claims.getAudience());
        System.out.println("Issued at: " + claims.getIssueTime());
        System.out.println("Expires at: " + claims.getExpirationTime());
        System.out.println("Not before: " + claims.getNotBeforeTime());
        System.out.println("NOW: " + new Date());

        boolean isExpired = claims.getExpirationTime() != null && claims.getExpirationTime().before(new Date());
        System.out.println("IS EXPIRED: " + isExpired);

        if (isExpired) {
            long expiredSecondsAgo = (new Date().getTime() - claims.getExpirationTime().getTime()) / 1000;
            System.out.println("Expired " + expiredSecondsAgo + " seconds ago (" + (expiredSecondsAgo / 60) + " minutes)");
        }

        System.out.println("Realm access roles: " + claims.getClaim("realm_access"));

        System.out.println("\n=== Step 5: Test NestedJweJWTProcessor + NimbusJwtDecoder ===");
        String jwkSetUri = "http://localhost:8080/realms/archcore-dev/protocol/openid-connect/certs";
        NestedJweJWTProcessor processor = new NestedJweJWTProcessor(
                privateKey, publicKey, "archcore-enc-key", "RSA-OAEP-256", "A256GCM", jwkSetUri);
        org.springframework.security.oauth2.jwt.NimbusJwtDecoder nimbusDecoder =
                new org.springframework.security.oauth2.jwt.NimbusJwtDecoder(processor);

        try {
            org.springframework.security.oauth2.jwt.Jwt jwt = nimbusDecoder.decode(ACTUAL_JWE_TOKEN);
            System.out.println("NestedJweJWTProcessor + NimbusJwtDecoder SUCCESS!");
            System.out.println("Subject: " + jwt.getSubject());
            System.out.println("Issuer: " + jwt.getIssuer());
            System.out.println("Expires: " + jwt.getExpiresAt());
            System.out.println("All claims: " + jwt.getClaims());
        } catch (org.springframework.security.oauth2.jwt.JwtException e) {
            System.out.println("Expected JwtException (expired token): " + e.getMessage());
            assertTrue(e.getMessage().contains("Expired JWT"),
                    "Should fail with expired JWT error, got: " + e.getMessage());
            System.out.println("Claims verification works - expired token correctly rejected!");
        }
    }

    private RSAPrivateKey loadPrivateKey(String location) throws Exception {
        String pem = Files.readString(Path.of("src/main/resources/" + location));
        String cleaned = pem.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        byte[] keyBytes = Base64.getDecoder().decode(cleaned);
        return (RSAPrivateKey) KeyFactory.getInstance("RSA")
                .generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
    }

    private RSAPublicKey loadPublicKey(String location) throws Exception {
        String pem = Files.readString(Path.of("src/main/resources/" + location));
        String cleaned = pem.replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        byte[] keyBytes = Base64.getDecoder().decode(cleaned);
        return (RSAPublicKey) KeyFactory.getInstance("RSA")
                .generatePublic(new X509EncodedKeySpec(keyBytes));
    }
}
