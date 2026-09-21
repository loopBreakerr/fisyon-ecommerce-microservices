package com.ecommerce.client.external.keycloak;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;

/**
 * Sadece IKeycloakAdminClient'a ozel Feign konfigurasyonu - @FeignClient'in
 * "configuration" attribute'u ile verildigi icin BILEREK @Configuration
 * ANNOTASYONU TASIMIYOR: aksi halde Spring'in genel component scan'i bu
 * RequestInterceptor'i TUM Feign clientlarina (ICatalogClient, ICartClient vb.)
 * global olarak uygular ve onlara da yanlislikla Keycloak Bearer token'i
 * enjekte eder.
 */
public class KeycloakFeignConfig {

    @Bean
    public RequestInterceptor keycloakAuthInterceptor(KeycloakTokenService keycloakTokenService) {
        return requestTemplate ->
                requestTemplate.header("Authorization", "Bearer " + keycloakTokenService.getValidToken());
    }
}
