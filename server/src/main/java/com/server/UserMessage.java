package com.server;

public class UserMessage {
    private String locationName;
    private String locationDescription;
    private String locationCity;

    public UserMessage(String locationName, String locationDescription, String locationCity) {
        this.locationName = locationName;
        this.locationDescription = locationDescription;
        this.locationCity = locationCity;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getLocationDescription() {
        return locationDescription;
    }

    public void setLocationDescription(String locationDescription) {
        this.locationDescription = locationDescription;
    }

    public String getLocationCity() {
        return locationCity;
    }

    public void setLocationCity(String locationCity) {
        this.locationCity = locationCity;
    }
}