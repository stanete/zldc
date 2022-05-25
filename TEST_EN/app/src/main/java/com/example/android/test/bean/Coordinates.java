package com.example.android.test.bean;

/**
 * 经纬度
 */
public class Coordinates {

    public Coordinates() { }

    public Coordinates(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }
    /**
     * 经度
     */
    private double longitude;
    /**
     * 纬度
     */
    private double latitude;

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    @Override
    public String toString() {
        return longitude +"," + latitude;
    }
}
