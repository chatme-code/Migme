package com.projectgoth.enums;

import android.support.annotation.NonNull;
import com.projectgoth.R;
import com.projectgoth.b.enums.AlertTypeEnum;
import com.projectgoth.i18n.I18n;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jtlim on 25/10/14.
 */
public enum AlertTypeTitleInfo {

    UNKNOWN(null, I18n.tr("Notification"), R.drawable.ad_alert_white),
    VIRTUAL_GIFT(AlertTypeEnum.VIRTUALGIFT_ALERT, I18n.tr("Gift"), R.drawable.ad_gift_white),
    GROUP_INVITE(AlertTypeEnum.GROUP_INVITE, I18n.tr("Group"), R.drawable.ad_grp_white),
    NEW_BADGE(AlertTypeEnum.NEW_BADGE_ALERT, I18n.tr("Badge"), R.drawable.ad_badge_white),
    MIGLEVEL_INCREASE(AlertTypeEnum.MIGLEVEL_INCREASE_ALERT, I18n.tr("Level"), R.drawable.ad_alert_white),
    INCOMING_CREDIT_TRANSFER(AlertTypeEnum.INCOMING_CREDIT_TRANSFER_ALERT, I18n.tr("Account"), R.drawable.ad_credit_white),
    ;

    public String  text;
    public int     icon;

    private static class MapHolder
    {
        private static Map<AlertTypeEnum, AlertTypeTitleInfo> typeToTitleInfoMap = new HashMap<AlertTypeEnum, AlertTypeTitleInfo>();
    }

    private AlertTypeTitleInfo(AlertTypeEnum type, String text, int icon) {
        this.text = text;
        this.icon = icon;

        MapHolder.typeToTitleInfoMap.put(type, this);
    }

    public static @NonNull
    AlertTypeTitleInfo getTitleInfo(AlertTypeEnum type) {
        AlertTypeTitleInfo result = MapHolder.typeToTitleInfoMap.get(type);
        if(result != null) return result;
        return UNKNOWN;
    }
}
