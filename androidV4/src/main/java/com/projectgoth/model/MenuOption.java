/**
 * Copyright (c) 2013 Project Goth
 *
 * MenuConfigItemView.java
 * Created Jul 9, 2013, 6:39:18 PM
 */

package com.projectgoth.model;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.widget.Checkable;
import com.projectgoth.R;
import com.projectgoth.common.Tools;

import java.util.HashMap;
import java.util.Map;

/**
 * @author cherryv
 * 
 */
public class MenuOption implements Checkable {

    private String              title;
    private String              subTitle;
    private Bitmap              icon;
    private Bitmap              iconUnchecked;
    private MenuViewType        viewType;
    private MenuOptionType      optionType;
    private int                 actionId;
    private Map<String, String> actionParams;
    private String              event;
    private String              label;
    private boolean             dismissPopupOnClick;

    public static final int     DEFAULT_PARAM          = R.id.value_mig33_command;

    private boolean             isSelected;
    
    private MenuAction          action;
    
    public interface MenuAction {
        public void onAction(MenuOption option, boolean isSelected);
    }

    public enum MenuViewType {
        LIST(0), GRID(1), SINGLE(2);

        private int mValue;

        private MenuViewType(final int value) {
            mValue = value;
        }

        public int getValue() {
            return this.mValue;
        }

        public static MenuViewType fromValue(int value) {
            for (MenuViewType type : values()) {
                if (type.mValue == value) {
                    return type;
                }
            }
            return MenuViewType.LIST;
        }
    }

    public enum MenuOptionType {
        LABEL(0), CHECKABLE(1), SELECTABLE(2), ACTIONABLE(3);

        private int mValue;

        private MenuOptionType(final int value) {
            mValue = value;
        }

        public int getValue() {
            return this.mValue;
        }

        public static MenuOptionType fromValue(int value) {
            for (MenuOptionType type : values()) {
                if (type.mValue == value) {
                    return type;
                }
            }
            return MenuOptionType.LABEL;
        }
    }

    public MenuOption(String title, MenuAction action) {
        this(title, null, -1, null, MenuOptionType.LABEL, false, true, action);
    }
    
    public MenuOption(String title, int iconResId, MenuAction action) {
        this(title, Tools.getBitmap(iconResId), -1, null, MenuOptionType.LABEL, false, true, action);
    }
    
    public MenuOption(String title, int iconResId, int actionId) {
        this(title, iconResId, actionId, null, MenuOptionType.LABEL, false);
    }

    public MenuOption(String title, Bitmap icon, int actionId) {
        this(title, icon, actionId, null, MenuOptionType.LABEL, false, true, null);
    }

    public MenuOption(String title, int iconResId, int actionId, String defaultParam) {
        this(title, Tools.getBitmap(iconResId), actionId, defaultParam, MenuOptionType.LABEL, false, true, null);
    }
    
    public MenuOption(String title, int iconResId, int actionId, String defaultParam, MenuOptionType menuOptionType,
            boolean menuOptionSelected) {
        this(title, Tools.getBitmap(iconResId), actionId, defaultParam, menuOptionType, menuOptionSelected, true, null);
    }
    
    public MenuOption(String title, int iconResId, int actionId, MenuOptionType menuOptionType, boolean menuOptionSelected,
            boolean dismissPopupOnClick) {
        this(title, Tools.getBitmap(iconResId), actionId, null, menuOptionType, menuOptionSelected, dismissPopupOnClick, null);
    }

    public MenuOption(String title, Bitmap icon, int actionId, MenuOptionType menuOptionType, boolean menuOptionSelected,
            boolean dismissPopupOnClick) {
        this(title, icon, actionId, null, menuOptionType, menuOptionSelected, dismissPopupOnClick, null);
    }

    public MenuOption(String title, Bitmap icon, int actionId, String defaultParam, MenuOptionType menuOptionType,
                      boolean menuOptionSelected, boolean dismissPopupOnClick, MenuAction action) {
        this(title, null, icon, actionId, defaultParam, menuOptionType, menuOptionSelected, dismissPopupOnClick, action);
    }

    public MenuOption(String title, String subTitle, Bitmap icon, int actionId, String defaultParam, MenuOptionType menuOptionType,
            boolean menuOptionSelected, boolean dismissPopupOnClick, MenuAction action) {
        this.title = title;
        this.subTitle = subTitle;
        this.icon = icon;
        this.actionId = actionId;
        viewType = MenuViewType.LIST;
        optionType = menuOptionType;
        isSelected = menuOptionSelected;
        this.dismissPopupOnClick = dismissPopupOnClick;

        actionParams = new HashMap<String, String>();
        if (!TextUtils.isEmpty(defaultParam)) {
            setParam(DEFAULT_PARAM, defaultParam);
        }
        
        this.action = action;
    }
    
    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title
     *            the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the title
     */
    public String getSubTitle() {
        return subTitle;
    }

    /**
     * @param subTitle
     *            the subTitle to set
     */
    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    /**
     * @return the icon
     */
    public Bitmap getIcon() {
        if (isSelected) {
            return icon;
        } else {
            return iconUnchecked == null ? icon : iconUnchecked;
        }
    }

    /**
     * @param icon
     *            the icon to set
     */
    public void setIcon(Bitmap icon) {
        this.icon = icon;
    }

    /**
     * @return the viewType
     */
    public MenuViewType getViewType() {
        return viewType;
    }

    /**
     * @param viewType
     *            the viewType to set
     */
    public void setViewType(MenuViewType viewType) {
        this.viewType = viewType;
    }

    /**
     * @return the actionId
     */
    public int getActionId() {
        return actionId;
    }

    /**
     * @param actionId
     *            the actionId to set
     */
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

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    /**
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * @param label
     *            the label to set
     */
    public void setLabel(String label) {
        this.label = label;
    }

    public static void logMenuEvent(MenuOption menuOption) {
    }

    @Override
    public void setChecked(boolean checked) {
        isSelected = checked;
    }

    @Override
    public boolean isChecked() {
        return isSelected;
    }

    @Override
    public void toggle() {
        isSelected = !isSelected;
    }
    
    public void setDismissPopupOnClick(boolean dismissPopup) {
        dismissPopupOnClick = dismissPopup;
    }
    
    public boolean shouldDismissPopupOnClick() {
        return dismissPopupOnClick;
    }
    
    public void setMenuOptionType(MenuOptionType optionType) {
        this.optionType = optionType;
    }
    
    public MenuOptionType getMenuOptionType() {
        return optionType;
    }

    /**
     * @return the action
     */
    public MenuAction getAction() {
        return action;
    }

    /**
     * @param action the action to set
     */
    public void setAction(MenuAction action) {
        this.action = action;
    }

    /**
     * @param iconUnchecked the iconUnchecked to set
     */
    public void setIconUnChecked(Bitmap icon) {
        this.iconUnchecked = icon;
    }
    
}
