package com.ecommerce.core_service.admin.service.impl;

import com.ecommerce.client.external.keycloak.IKeycloakAdminClient;
import com.ecommerce.core_service.admin.model.CustomerSummaryModel;
import com.ecommerce.core_service.admin.model.SellerSummaryModel;
import com.ecommerce.core_service.admin.service.IAdminUserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminUserServiceImpl implements IAdminUserService {

    private final IKeycloakAdminClient keycloakAdminClient;
    private final String realm;

    public AdminUserServiceImpl(IKeycloakAdminClient keycloakAdminClient,
                                @Value("${keycloak.admin.realm}") String realm) {
        this.keycloakAdminClient = keycloakAdminClient;
        this.realm = realm;
    }

    @Override
    public List<SellerSummaryModel> getAllSellers() {
        return keycloakAdminClient.getUsersByRole(realm, "seller")
                .stream()
                .map(user -> new SellerSummaryModel(user.getId(), user.getUsername(), user.getEmail()))
                .toList();
    }

    @Override
    public List<CustomerSummaryModel> getAllCustomers() {
        return keycloakAdminClient.getUsersByRole(realm, "customer")
                .stream()
                .map(user -> new CustomerSummaryModel(user.getId(), user.getUsername(), user.getEmail()))
                .toList();
    }
}
