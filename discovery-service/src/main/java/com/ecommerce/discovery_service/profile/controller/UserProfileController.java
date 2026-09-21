package com.ecommerce.discovery_service.profile.controller;

import com.ecommerce.discovery_service.profile.model.UserProfileModel;
import com.ecommerce.discovery_service.profile.model.UserProfileRequest;
import com.ecommerce.discovery_service.profile.service.UserProfileService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserProfileController {

    private final UserProfileService userProfileService;

    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @GetMapping("/profile")
    public UserProfileModel getMyProfile(@AuthenticationPrincipal Jwt jwt) {
        return userProfileService.getMyProfile(jwt.getSubject());
    }

    @PutMapping("/profile")
    public UserProfileModel createOrUpdateProfile(@RequestBody UserProfileRequest request,
                                                   @AuthenticationPrincipal Jwt jwt) {
        return userProfileService.createOrUpdateProfile(jwt.getSubject(), request);
    }
}
