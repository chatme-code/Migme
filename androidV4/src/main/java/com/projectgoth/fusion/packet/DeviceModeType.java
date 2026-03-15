/**
 * Copyright (c) 2013 Project Goth
 *
 * DeviceModeType.java
 * Created Sep 11, 2013, 11:38:54 AM
 */

package com.projectgoth.fusion.packet;

/**
 * @author cherryv
 * 
 */
public enum DeviceModeType {
    NORMAL(0), SLEEP(1);

    private int value;

    private DeviceModeType(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

    public static DeviceModeType fromValue(Integer value) {
        return fromValue(value, NORMAL);
    }

    public static DeviceModeType fromValue(Integer value, DeviceModeType defaultValue) {
        if (value == null) {
            return defaultValue;
        }

        for (DeviceModeType e : DeviceModeType.values()) {
            if (e.value() == value) {
                return e;
            }
        }

        return defaultValue;
    }
}
