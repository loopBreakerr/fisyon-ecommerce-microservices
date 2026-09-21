package com.ecommerce.discovery_service.profile.service;

import com.ecommerce.discovery_service.profile.model.UserProfileModel;
import com.ecommerce.discovery_service.profile.model.UserProfileRequest;

public interface UserProfileService {

    UserProfileModel getMyProfile(String userId);

    UserProfileModel createOrUpdateProfile(String userId, UserProfileRequest request);
}
