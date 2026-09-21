package com.ecommerce.api_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
                .authorizeExchange(exchange -> exchange
                        .pathMatchers(HttpMethod.OPTIONS).permitAll()
                        //CORS preflight (OPTIONS) istekleri Authorization header'i tasimaz,
                        //bu yuzden auth disi birakiliyor; diger tum metodlar icin kural ayni.
                        .pathMatchers(HttpMethod.GET, "/api/catalog/products/**", "/api/catalog/categories/**", "/api/catalog/products/images/**").permitAll()
                        //Urun/kategori listeleme herkese acik; downstream core-service kendi
                        //SecurityConfig'inde /products/mine gibi auth gereken alt-yollari zaten
                        //ayrica korumaya devam ediyor, burasi sadece Gateway'in blanket auth
                        //kuralindan bu iki genel-okuma yolunu istisna tutuyor.
                        .anyExchange().authenticated()
                        //gatewaye gelen her istek kimlik doğrulaması (gecerli bi JWT) gerektirir.
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {}));
                // "Kimlik doğrulamasını JWT ile yap, ve JWT'yi doğrularken application.yml'deki issuer-uri ayarını kullan."
                // bu satır springe yazdıgımız issuer-uri'yi oto baglar, ekstra bi şeye gerek kalmadan spring sec bunu kendisi halleder

        return http.build();
    }
}