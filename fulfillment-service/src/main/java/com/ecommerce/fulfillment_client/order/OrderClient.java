package com.ecommerce.fulfillment_client.order;

import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * core-service'in /orders/{id} endpoint'ine RestClient ile çağrı yapar - Feign değil,
 * çünkü fulfillment-service'te (notification/keycloak entegrasyonunda da yaptığımız gibi)
 * tek bir GET çağrısı için Feign + spring-cloud BOM'unu projeye eklemek istemiyoruz.
 *
 * core-service ile fulfillment-service GERÇEKTEN ayrı JVM/deployable oldukları için
 * (payment/order'ın aksine, onlar core-service içinde aynı process'te), bu çağrı
 * network üzerinden gidiyor - core-service yanıt vermezse/yavaşsa asılı kalmamak icin
 * 3 saniyelik connect/read timeout ayarlandı.
 */
@Service
public class OrderClient {

    private static final int TIMEOUT_MS = 3000;

    private final RestClient restClient;

    public OrderClient() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(TIMEOUT_MS);
        requestFactory.setReadTimeout(TIMEOUT_MS);
        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

    public OrderResponse getOrderById(Long orderId, String jwtToken) {
        return restClient.get()
                .uri("http://localhost:8081/orders/{id}", orderId)
                .header("Authorization", "Bearer " + jwtToken)
                .retrieve()
                .body(OrderResponse.class);
    }
}
