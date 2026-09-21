package com.ecommerce.core_service.admin.service;

import com.ecommerce.core_service.admin.model.CustomerSummaryModel;
import com.ecommerce.core_service.admin.model.SellerSummaryModel;

import java.util.List;

public interface IAdminUserService {

    List<SellerSummaryModel> getAllSellers();

    List<CustomerSummaryModel> getAllCustomers();
}
