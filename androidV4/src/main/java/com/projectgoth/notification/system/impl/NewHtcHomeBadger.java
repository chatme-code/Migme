package com.projectgoth.notification.system.impl;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import java.util.Arrays;
import java.util.List;

import com.projectgoth.notification.system.ShortcutBadgeException;
import com.projectgoth.notification.system.ShortcutBadger;

/**
 * @author Leo Lin
 */
public class NewHtcHomeBadger extends ShortcutBadger {

    public static final String INTENT_UPDATE_SHORTCUT = "com.htc.launcher.action.UPDATE_SHORTCUT";
    public static final String INTENT_SET_NOTIFICATION = "com.htc.launcher.action.SET_NOTIFICATION";
    public static final String PACKAGENAME = "packagename";
    public static final String COUNT = "count";
    public static final String EXTRA_COMPONENT = "com.htc.launcher.extra.COMPONENT";
    public static final String EXTRA_COUNT = "com.htc.launcher.extra.COUNT";

    public NewHtcHomeBadger(Context context) {
        super(context);
    }

    @Override
    protected void executeBadge(int badgeCount) throws ShortcutBadgeException {

        Intent intent1 = new Intent(INTENT_SET_NOTIFICATION);
        ComponentName localComponentName = new ComponentName(getContextPackageName(), getEntryActivityName());
        intent1.putExtra(EXTRA_COMPONENT, localComponentName.flattenToShortString());
        intent1.putExtra(EXTRA_COUNT, badgeCount);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent1);

        Intent intent = new Intent(INTENT_UPDATE_SHORTCUT);
        intent.putExtra(PACKAGENAME, getContextPackageName());
        intent.putExtra(COUNT, badgeCount);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    @Override
    public List<String> getSupportLaunchers() {
        return Arrays.asList("com.htc.launcher");
    }
}