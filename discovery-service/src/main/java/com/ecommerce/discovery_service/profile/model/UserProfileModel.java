package com.ecommerce.discovery_service.profile.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class UserProfileModel {

    private final Long id;
    private final UUID uuid;
    private final String userId;
    private final String phoneNumber;
    private final LocalDate dateOfBirth;
    private final String defaultAddressLine;
    private final String defaultCity;
    private final String defaultPostalCode;
    private final String defaultCountry;
    private final String preferences;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public UserProfileModel(Long id, UUID uuid, String userId, String phoneNumber, LocalDate dateOfBirth,
                             String defaultAddressLine, String defaultCity, String defaultPostalCode,
                             String defaultCountry, String preferences, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.uuid = uuid;
        this.userId = userId;
        this.phoneNumber = phoneNumber;
        this.dateOfBirth = dateOfBirth;
        this.defaultAddressLine = defaultAddressLine;
        this.defaultCity = defaultCity;
        this.defaultPostalCode = defaultPostalCode;
        this.defaultCountry = defaultCountry;
        this.preferences = preferences;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public UUID getUuid() { return uuid; }
    public String getUserId() { return userId; }
    public String getPhoneNumber() { return phoneNumber; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public String getDefaultAddressLine() { return defaultAddressLine; }
    public String getDefaultCity() { return defaultCity; }
    public String getDefaultPostalCode() { return defaultPostalCode; }
    public String getDefaultCountry() { return defaultCountry; }
    public String getPreferences() { return preferences; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
