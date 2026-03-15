/**
 * Copyright (c) 2013 Project Goth
 *
 * SettingsItem.java
 * Created Sep 4, 2013, 1:48:39 PM
 */

package com.projectgoth.model;

import android.text.TextUtils;
import com.projectgoth.R;
import com.projectgoth.events.GAEvent;
import com.projectgoth.ui.fragment.SettingsFragment.SettingsGroupType;

import java.util.HashMap;
import java.util.Map;

/**
 * @author mapet
 * 
 */
public class SettingsItem {

    private String              label;
    private SettingsViewType    viewType;
    private SettingsGroupType   groupType;
    private int                 actionId;
    private Map<String, String> actionParams;
    private Object              data;
    private int                 iconResource;
    private GAEvent             gaEvent;

    public static final int     DEFAULT_PARAM = R.id.value_mig33_command;

    public enum SettingsViewType {
        DEFAULT(0), GROUP(1), TOGGLE(2), TEXT(3), SUBTITLE(4), EDIT(5), TOGGLEnEDIT(6);

        private int mValue;

        private SettingsViewType(final int value) {
            mValue = value;
        }

        public int getValue() {
            return this.mValue;
        }

        public static SettingsViewType fromValue(int value) {
            for (SettingsViewType type : values()) {
                if (type.mValue == value) {
                    return type;
                }
            }
            return SettingsViewType.DEFAULT;
        }
    }
    
    public SettingsItem(String label, int actionId, SettingsViewType viewType) {
        this(label, actionId, null, viewType, null, null, null);
    }

    public SettingsItem(String label, int actionId, SettingsViewType viewType, GAEvent gaEvent) {
        this(label, actionId, null, viewType, null, null, gaEvent);
    }

    public SettingsItem(String label, int actionId, SettingsViewType viewType, Object data, GAEvent gaEvent) {
        this(label, actionId, null, viewType, null, data, gaEvent);
    }
    
    public SettingsItem(String label, int actionId, SettingsViewType viewType, Object data, int icon) {
        this(label, actionId, null, viewType, null, data, null);
        this.iconResource = icon;
    }

    public SettingsItem(String label, int actionId, String defaultParam, GAEvent gaEvent) {
        this(label, actionId, defaultParam, SettingsViewType.DEFAULT, null, null, gaEvent);
    }

    public SettingsItem(String label, int actionId, SettingsViewType viewType, SettingsGroupType groupType, GAEvent gaEvent) {
        this(label, actionId, null, viewType, groupType, null, gaEvent);
    }   
    
    public SettingsItem(String label, int actionId, String defaultParam, SettingsViewType viewType,
            SettingsGroupType groupType, Object data, GAEvent gaEvent) {
        this.label = label;
        this.actionId = actionId;
        this.viewType = viewType;
        this.groupType = groupType;
        this.data = data;
        this.gaEvent = gaEvent;
        
        actionParams = new HashMap<String, String>();
        if (!TextUtils.isEmpty(defaultParam)) {
            setParam(DEFAULT_PARAM, defaultParam);
        }
    }


    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public SettingsViewType getViewType() {
        return viewType;
    }

    public void setViewType(SettingsViewType viewType) {
        this.viewType = viewType;
    }

    public int getActionId() {
        return actionId;
    }

    public void setActionId(int actionId) {
        this.actionId = actionId;
    }

    public String getParam() {
        return getParam(DEFAULT_PARAM);
    }

    public String getParam(int key) {
        return getParam(String.valueOf(key));
    }

    public String getParam(String key) {
        synchronized (actionParams) {
            return actionParams.get(key);
        }
    }

    public void setParam(String param) {
        setParam(DEFAULT_PARAM, param);
    }

    public void setParam(int key, String param) {
        setParam(String.valueOf(key), param);
    }

    public void setParam(String key, String param) {
        synchronized (actionParams) {
            actionParams.put(key, param);
        }
    }

    public SettingsGroupType getGroupType() {
        return groupType;
    }

    public void setGroupType(SettingsGroupType groupType) {
        this.groupType = groupType;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
    
    /**
     * @return the iconResource
     */
    public int getIconResource() {
        return iconResource;
    }

    
    /**
     * @param iconResource the iconResource to set
     */
    public void setIconResource(int iconResource) {
        this.iconResource = iconResource;
    }

    public GAEvent getGAEvent() {
        return gaEvent;
    }

}
