package com.example.lab5_starter;

import java.io.Serializable;

public class City implements Serializable {

    // attributes
    private String name;
    private String province;

    // constructor
    public City(String name, String province) {
        this.name = name;
        this.province = province;
    }

    public String getCityName() {
        return name;
    }

    public void setCityName(String name) {
        this.name = name;
    }

    public String getProvinceName() {
        return province;
    }

    public void setProvinceName(String province) {
        this.province = province;
    }

}