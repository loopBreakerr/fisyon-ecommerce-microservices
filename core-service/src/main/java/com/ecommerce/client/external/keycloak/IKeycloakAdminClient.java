package com.ecommerce.client.external.keycloak;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "keycloak-admin", url = "${keycloak.admin.base-url}", configuration = KeycloakFeignConfig.class)
public interface IKeycloakAdminClient {

    @GetMapping("/admin/realms/{realm}/roles/{role}/users")
    List<KeycloakUserResponse> getUsersByRole(@PathVariable("realm") String realm, @PathVariable("role") String role);
}
