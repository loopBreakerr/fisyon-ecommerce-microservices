package com.ecommerce.discovery_service.profile.service.impl;

import com.ecommerce.discovery_service.profile.entity.UserProfile;
import com.ecommerce.discovery_service.profile.model.UserProfileModel;
import com.ecommerce.discovery_service.profile.model.UserProfileRequest;
import com.ecommerce.discovery_service.profile.repository.UserProfileRepository;
import com.ecommerce.discovery_service.profile.service.UserProfileService;
import org.springframework.stereotype.Service;

@Service
public class UserProfileServiceImpl implements UserProfileService {

    private final UserProfileRepository userProfileRepository;

    public UserProfileServiceImpl(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    @Override
    public UserProfileModel getMyProfile(String userId) {
        UserProfile profile = userProfileRepository.findByUserId(userId).orElseThrow();
        return toModel(profile);
    }

    @Override
    public UserProfileModel createOrUpdateProfile(String userId, UserProfileRequest request) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseGet(() -> {
                    UserProfile newProfile = new UserProfile();
                    newProfile.setUserId(userId);
                    return newProfile;
                });

        profile.setPhoneNumber(request.getPhoneNumber());
        profile.setDateOfBirth(request.getDateOfBirth());
        profile.setDefaultAddressLine(request.getDefaultAddressLine());
        profile.setDefaultCity(request.getDefaultCity());
        profile.setDefaultPostalCode(request.getDefaultPostalCode());
        profile.setDefaultCountry(request.getDefaultCountry());
        profile.setPreferences(request.getPreferences());

        UserProfile saved = userProfileRepository.save(profile);
        return toModel(saved);
    }

    private UserProfileModel toModel(UserProfile profile) {
        return new UserProfileModel(
                profile.getId(),
                profile.getUuid(),
                profile.getUserId(),
                profile.getPhoneNumber(),
                profile.getDateOfBirth(),
                profile.getDefaultAddressLine(),
                profile.getDefaultCity(),
                profile.getDefaultPostalCode(),
                profile.getDefaultCountry(),
                profile.getPreferences(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }
}
