package com.cch.payloay;


public class Device {
    public static final Class<?> cl = null;
    public String deviceName;
    public int humidity = 0;
    public int temp = 0;

    public Device() {}

    public Device(String deviceName, int humidity, int temp) {
        this.deviceName = deviceName;
        this.humidity = humidity;
        this.temp = temp;
    }

    @Override
    public String toString() {
        return "Device [deviceName=" + deviceName + ", humidity=" + humidity + ", temp=" + temp + "]";
    }

}
