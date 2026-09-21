package com.ecommerce.discovery_service.profile.model;

import java.time.LocalDate;

public class UserProfileRequest {

    private String phoneNumber;
    private LocalDate dateOfBirth;
    private String defaultAddressLine;
    private String defaultCity;
    private String defaultPostalCode;
    private String defaultCountry;
    private String preferences;

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getDefaultAddressLine() { return defaultAddressLine; }
    public void setDefaultAddressLine(String defaultAddressLine) { this.defaultAddressLine = defaultAddressLine; }

    public String getDefaultCity() { return defaultCity; }
    public void setDefaultCity(String defaultCity) { this.defaultCity = defaultCity; }

    public String getDefaultPostalCode() { return defaultPostalCode; }
    public void setDefaultPostalCode(String defaultPostalCode) { this.defaultPostalCode = defaultPostalCode; }

    public String getDefaultCountry() { return defaultCountry; }
    public void setDefaultCountry(String defaultCountry) { this.defaultCountry = defaultCountry; }

    public String getPreferences() { return preferences; }
    public void setPreferences(String preferences) { this.preferences = preferences; }
}
