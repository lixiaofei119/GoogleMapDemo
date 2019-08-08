package com.lxf.googlemapsdemo.bean;

/**
 * @author: lixiaofei
 * @date: 2019/2/14
 * @version: 1.0.0
 */
public class MyLocationBean {

    private double altitude;
    private double latitude;
    private double longitude;
    private String provider;
    private float speed;
    private long time;

    public MyLocationBean() {
    }

    public MyLocationBean(double altitude, double latitude, double longitude, String provider, float speed, long time) {
        this.altitude = altitude;
        this.latitude = latitude;
        this.longitude = longitude;
        this.provider = provider;
        this.speed = speed;
        this.time = time;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "MyLocationBean{" +
                "altitude=" + altitude +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", provider='" + provider + '\'' +
                ", speed=" + speed +
                ", time=" + time +
                '}';
    }
}
