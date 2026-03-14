package com.projectgoth.notification.system.impl;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;


import java.util.Arrays;
import java.util.List;

import com.projectgoth.notification.system.ShortcutBadgeException;
import com.projectgoth.notification.system.ShortcutBadger;

/**
 * @author Gernot Pansy
 */
public class ApexHomeBadger extends ShortcutBadger {

    private static final String INTENT_UPDATE_COUNTER = "com.anddoes.launcher.COUNTER_CHANGED";
    private static final String PACKAGENAME = "package";
    private static final String COUNT = "count";
    private static final String CLASS = "class";

    public ApexHomeBadger(Context context) {
        super(context);
    }

    @Override
    protected void executeBadge(int badgeCount) throws ShortcutBadgeException {

        Intent intent = new Intent(INTENT_UPDATE_COUNTER);
        intent.putExtra(PACKAGENAME, getContextPackageName());
        intent.putExtra(COUNT, badgeCount);
        intent.putExtra(CLASS, getEntryActivityName());
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    @Override
    public List<String> getSupportLaunchers() {
        return Arrays.asList("com.anddoes.launcher");
    }
}