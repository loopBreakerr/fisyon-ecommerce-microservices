package com.ecommerce.fulfillment_service.notification.keycloak;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.time.Instant;

/**
 * Keycloak Admin REST API'sine erisim icin client_credentials grant'iyla
 * access token alir ve memory'de cache'ler. Token suresi dolmadan (5 saniyelik
 * bir pay birakarak) tekrar istek atmaz, dolmussa yeniler.
 *
 * core-service'teki com.ecommerce.core_service.admin.keycloak.KeycloakTokenService
 * ile birebir ayni desen (ayni Keycloak client - core-service-admin-client -
 * bilerek paylasiliyor, fulfillment-service icin ayri bir client acilmadi).
 */
@Service
public class KeycloakTokenService {

    private static final long EXPIRY_SAFETY_MARGIN_SECONDS = 5;

    private final RestClient restClient;
    private final String tokenUri;
    private final String clientId;
    private final String clientSecret;

    private volatile CachedToken cachedToken;

    public KeycloakTokenService(@Value("${keycloak.admin.token-uri}") String tokenUri,
                                @Value("${keycloak.admin.client-id}") String clientId,
                                @Value("${keycloak.admin.client-secret}") String clientSecret) {
        this.restClient = RestClient.create();
        this.tokenUri = tokenUri;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public synchronized String getValidToken() {
        if (cachedToken != null && cachedToken.isStillValid()) {
            return cachedToken.accessToken();
        }

        cachedToken = fetchNewToken();
        return cachedToken.accessToken();
    }

    private CachedToken fetchNewToken() {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);

        TokenResponse response = restClient.post()
                .uri(tokenUri)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(TokenResponse.class);

        if (response == null || response.getAccessToken() == null) {
            throw new IllegalStateException("Keycloak'tan token alınamadı");
        }

        Instant expiresAt = Instant.now().plusSeconds(response.getExpiresIn() - EXPIRY_SAFETY_MARGIN_SECONDS);
        return new CachedToken(response.getAccessToken(), expiresAt);
    }

    private record CachedToken(String accessToken, Instant expiresAt) {
        boolean isStillValid() {
            return Instant.now().isBefore(expiresAt);
        }
    }

    /**
     * Keycloak token endpoint'inin donduru cevabin sadece ihtiyacimiz olan alanlari.
     */
    public static class TokenResponse {

        @JsonProperty("access_token")
        private String accessToken;

        @JsonProperty("expires_in")
        private long expiresIn;

        public String getAccessToken() { return accessToken; }
        public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

        public long getExpiresIn() { return expiresIn; }
        public void setExpiresIn(long expiresIn) { this.expiresIn = expiresIn; }
    }
}
