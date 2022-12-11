package com.solucioneshr.soft.downloadtest;

public class MessageProgressValue {
    private Integer value = 0;
    private String valueDown = "";

    public MessageProgressValue(Integer value, String valueDown) {
        this.value = value;
        this.valueDown = valueDown;
    }

    public Integer getValue() {
        return value;
    }

    public String getValueDown() {
        return valueDown;
    }
}
