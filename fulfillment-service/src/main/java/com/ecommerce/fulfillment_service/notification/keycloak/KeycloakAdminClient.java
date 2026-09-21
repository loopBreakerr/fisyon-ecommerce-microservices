package com.ecommerce.fulfillment_service.notification.keycloak;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.List;

/**
 * Keycloak Admin REST API'sinden "admin" rolundeki tum kullanicilarin id'lerini
 * ceker. core-service'te ayni is icin Feign kullanilmisti, ama fulfillment-service'in
 * pom.xml'inde spring-cloud-starter-openfeign hic yok (ve baska hicbir yerde
 * Feign kullanilmiyor) - sirf bu tek GET cagrisi icin butun spring-cloud-openfeign
 * bagimliligini (+ BOM'unu) projeye eklemek yerine, KeycloakTokenService'te zaten
 * kullandigimiz RestClient ile ayni yaklasimla yapiyoruz. Sonuc, sipariş
 * basina Keycloak'a gereksiz yuk binmesin diye 5 dakikalik bir memory cache'te tutuluyor.
 */
@Service
public class KeycloakAdminClient {

    private static final long CACHE_TTL_SECONDS = 300;

    private final RestClient restClient;
    private final KeycloakTokenService tokenService;
    private final String baseUrl;
    private final String realm;

    private volatile CachedAdminIds cachedAdminIds;

    public KeycloakAdminClient(KeycloakTokenService tokenService,
                               @Value("${keycloak.admin.base-url}") String baseUrl,
                               @Value("${keycloak.admin.realm}") String realm) {
        this.restClient = RestClient.create();
        this.tokenService = tokenService;
        this.baseUrl = baseUrl;
        this.realm = realm;
    }

    public synchronized List<String> getAdminUserIds() {
        if (cachedAdminIds != null && cachedAdminIds.isStillValid()) {
            return cachedAdminIds.ids();
        }

        List<KeycloakUserResponse> users = restClient.get()
                .uri(baseUrl + "/admin/realms/" + realm + "/roles/admin/users")
                .header("Authorization", "Bearer " + tokenService.getValidToken())
                .retrieve()
                .body(new ParameterizedTypeReference<List<KeycloakUserResponse>>() { });

        List<String> ids = users == null ? List.of() : users.stream().map(KeycloakUserResponse::getId).toList();
        cachedAdminIds = new CachedAdminIds(ids, Instant.now().plusSeconds(CACHE_TTL_SECONDS));
        return ids;
    }

    private record CachedAdminIds(List<String> ids, Instant expiresAt) {
        boolean isStillValid() {
            return Instant.now().isBefore(expiresAt);
        }
    }
}
