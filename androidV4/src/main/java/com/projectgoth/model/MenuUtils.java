/**
 * Copyright (c) 2013 Project Goth
 *
 * MenuUtils.java
 * Created Sep 23, 2014, 6:16:54 PM
 */

package com.projectgoth.model;

import android.support.v4.app.FragmentActivity;
import com.projectgoth.R;
import com.projectgoth.common.Tools;
import com.projectgoth.common.WebURL;
import com.projectgoth.i18n.I18n;
import com.projectgoth.model.MenuOption.MenuAction;
import com.projectgoth.model.MenuOption.MenuOptionType;
import com.projectgoth.ui.activity.ActionHandler;

/**
 * @author warrenbalcos
 * 
 */
public class MenuUtils {

    /**
     * Create a {@link MenuOption} item for reporting a user
     * 
     * @param activity
     * @param userName
     * @return
     */
    public static MenuOption createReportUser(final FragmentActivity activity, final String userName) {
        if (userName != null) {
            MenuOption report = new MenuOption(I18n.tr("Report"), new MenuAction() {

                @Override
                public void onAction(MenuOption option, boolean isSelected) {
                    ActionHandler.getInstance().displayBrowser(activity,
                            String.format(WebURL.URL_REPORT_USER, userName));
                }
            });
            report.setIcon(Tools.getBitmap(R.drawable.ad_report_white));
            return report;
        }
        return null;
    }

    /**
     * Create a {@link MenuOption} for favourite of a posts
     * 
     * @param activity
     * @param postId
     * @param isWatching
     * @param isPostInGroup
     * @return
     */
    public static MenuOption createFavouritePost(final FragmentActivity activity, final String postId,final boolean isWatching, final boolean isPostInGroup) {
        MenuOption favourite = new MenuOption(I18n.tr("Favorite"), new MenuAction() {

            @Override
            public void onAction(MenuOption option, boolean isSelected) {
                if (isPostInGroup) {
                    Tools.showToast(activity, I18n.tr("Oops. Group posts can't be added to favorites for now."));
                } else {
                    ActionHandler.getInstance().watchOrUnwatchPost(postId, !isWatching);
                }
            }
        });
        favourite.setMenuOptionType(MenuOptionType.CHECKABLE);
        favourite.setChecked(isWatching);
        favourite.setIcon(Tools.getBitmap(R.drawable.ad_favourite_pink));
        favourite.setIconUnChecked(Tools.getBitmap(R.drawable.ad_favourite_white));
        return favourite;
    }

    public static MenuOption createLockUnLockPost(final FragmentActivity activity, final String postId,
            final boolean isLocked) {
        MenuOption lock = new MenuOption(isLocked ? I18n.tr("Unlock post") : I18n.tr("Lock post"), new MenuAction() {

            @Override
            public void onAction(MenuOption option, boolean isSelected) {
                ActionHandler.getInstance().lockOrUnlockPost(postId, !isLocked);
            }
        });
        lock.setMenuOptionType(MenuOptionType.CHECKABLE);
        lock.setChecked(isLocked);
        lock.setIcon(Tools.getBitmap(R.drawable.ad_private_white));
        lock.setIconUnChecked(Tools.getBitmap(R.drawable.ad_public_white));

        return lock;
    }

}
