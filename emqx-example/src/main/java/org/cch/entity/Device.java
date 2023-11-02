package org.cch.entity;

import java.util.Random;

public class Device {
    private String deviceName;
    private int humidity = 0;
    private int temp = 0;
    private Random random = new Random();

    public Device() {}

    public Device(String deviceName, int humidity, int temp) {
        this.deviceName = deviceName;
        this.humidity = humidity;
        this.temp = temp;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public int getHumidity() {
        return humidity;
    }

    public void setHumidity(int humidity) {
        this.humidity = humidity;
    }

    public int getTemp() {
        return temp;
    }

    public void setTemp(int temp) {
        this.temp = temp;
    }

    @Override
    public String toString() {
        return "Device{" +
                "deviceName='" + deviceName + '\'' +
                ", humidity=" + humidity +
                ", temp=" + temp +
                '}';
    }
}
