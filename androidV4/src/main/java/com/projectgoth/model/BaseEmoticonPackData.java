/**
 * Copyright (c) 2013 Project Goth
 *
 * BaseEmoticonPackData.java
 * Created Dec 11, 2014, 4:45:54 PM
 */

package com.projectgoth.model;

import com.projectgoth.nemesis.model.BaseEmoticonPack;

/**
 * @author mapet
 * 
 */
public class BaseEmoticonPackData {

    private BaseEmoticonPack baseEmoticonPack;
    private boolean          isOwnEmoticonPack;
    private boolean          isEnable = true;

    public BaseEmoticonPackData() {
    }

    public BaseEmoticonPackData(BaseEmoticonPack baseEmoticonPack, boolean isOwnEmoticonPack) {
        this.baseEmoticonPack = baseEmoticonPack;
        this.isOwnEmoticonPack = isOwnEmoticonPack;
    }

    public void setBaseEmoticonPack(BaseEmoticonPack baseEmoticonPack) {
        this.baseEmoticonPack = baseEmoticonPack;
    }

    public BaseEmoticonPack getBaseEmoticonPack() {
        return baseEmoticonPack;
    }

    public void setIsOwnPack(boolean isOwnEmoticonPack) {
        this.isOwnEmoticonPack = isOwnEmoticonPack;
    }

    public boolean isOwnPack() {
        return isOwnEmoticonPack;
    }

    public boolean isEnable() {
        return isEnable;
    }

    public void setEnable(boolean isEnable) {
        this.isEnable = isEnable;
    }
}
