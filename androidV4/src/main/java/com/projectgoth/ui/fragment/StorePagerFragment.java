/**
 * Copyright (c) 2013 Project Goth
 *
 * StorePagerFragment.java
 * Created Nov 22, 2013, 5:07:14 PM
 */

package com.projectgoth.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import com.projectgoth.R;
import com.projectgoth.common.Theme;
import com.projectgoth.common.ThemeValues;
import com.projectgoth.common.WebURL;
import com.projectgoth.datastore.Session;
import com.projectgoth.events.GAEvent;
import com.projectgoth.i18n.I18n;
import com.projectgoth.model.MenuOption;
import com.projectgoth.ui.activity.ActionHandler;
import com.projectgoth.ui.activity.CustomActionBarConfig;
import com.projectgoth.ui.activity.CustomActionBarConfig.NavigationButtonState;
import com.projectgoth.ui.adapter.StorePagerAdapter;
import com.projectgoth.ui.widget.PagerSlidingTabHeader;

import java.util.ArrayList;

/**
 * @author mapet
 * 
 */
public class StorePagerFragment extends BaseSearchFragment implements OnPageChangeListener{

    protected ViewPager viewPager;
    private StorePagerAdapter     mAdapter;
    private PagerSlidingTabHeader mTabs;

    private int                   selectedTab             = 0;
    private String                initialRecipient;

    public static final String    PARAM_SELECTED_TAB      = "PARAM_SELECTED_TAB";
    public static final String    PARAM_INITIAL_RECIPIENT = "PARAM_INITIAL_RECIPIENT";

    @Override
    protected void readBundleArguments(Bundle args) {
        super.readBundleArguments(args);
        selectedTab = args.getInt(PARAM_SELECTED_TAB, 0);
        initialRecipient = args.getString(PARAM_INITIAL_RECIPIENT);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_store;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewPager = (ViewPager) view.findViewById(R.id.pager);
        mAdapter = createAdapter(getChildFragmentManager());
        viewPager.setAdapter(mAdapter);

        mTabs = (PagerSlidingTabHeader) view.findViewById(R.id.tabs);
        mTabs.setViewPager(viewPager);

        viewPager.setBackgroundColor(Theme.getColor(ThemeValues.LIGHT_BACKGROUND_COLOR));
        viewPager.setOnPageChangeListener(this);

        viewPager.setCurrentItem(selectedTab);
    }

    protected StorePagerAdapter createAdapter(FragmentManager fragmentManager) {
        mAdapter = new StorePagerAdapter(fragmentManager, getActivity(), initialRecipient);
        return mAdapter;
    }

    @Override
    public void onPageScrollStateChanged(int pos) {
    }

    @Override
    public void onPageScrolled(int pos, float arg1, int arg2) {
    }

    @Override
    public void onPageSelected(int position) {
        selectedTab = position;
        mAdapter.setCurrentPos(position);
        setTitle(storeTitles[position]);
        setTitleIcon(storeIcons[position]);
        mTabs.notifyDataSetChanged();
        switch (mAdapter.getSelectedPagerType()) {
            case EMOTICONS:
                GAEvent.Store_VisitEmoticonFragment.send();
                break;
            case AVATAR:
                GAEvent.Store_VisitAvatarFragment.send();
                break;
        }
    }

    private static String[] storeTitles = {
            I18n.tr("Gifts"),
            I18n.tr("Stickers"),
            I18n.tr("Emoticons"),
            I18n.tr("Avatars")
    };

    private static int[] storeIcons = {
            R.drawable.ad_gift_white,
            R.drawable.ad_sticker_white,
            R.drawable.ad_emoticon_white,
            R.drawable.ad_avatar_white
    };

    @Override
    public ArrayList<MenuOption> getMenuOptions() {
        ArrayList<MenuOption> menuItems = new ArrayList<MenuOption>();

        MenuOption myCredit = new MenuOption(I18n.tr("My credit"), Session.getInstance().getAccountBalance(), null, R.id.action_recharge_clicked, I18n.tr("RECHARGE"),
                MenuOption.MenuOptionType.ACTIONABLE, true, false, new MenuOption.MenuAction() {
            @Override
            public void onAction(MenuOption option, boolean isSelected) {
                GAEvent.Chat_SendGiftUiRecharge.send();
                ActionHandler.getInstance().displayRechargeCreditsFromChat(getActivity(), WebURL.URL_ACCOUNT_SETTINGS,
                        I18n.tr("Buy credit"), R.drawable.ad_credit_white);
            }
        });

        myCredit.setViewType(MenuOption.MenuViewType.SINGLE);


        menuItems.add(myCredit);

        return menuItems;
    }

    @Override
    protected String getTitle() {
        return I18n.tr("Gifts");
    }

    @Override
    protected int getTitleIcon() {
        return  R.drawable.ad_gift_white;
    }

    @Override
    public CustomActionBarConfig getActionBarConfig() {
        CustomActionBarConfig config = super.getActionBarConfig();
        config.setNavigationButtonState(NavigationButtonState.BACK);
        config.setShowOverflowButtonState(CustomActionBarConfig.OverflowButtonState.POPUP);
        config.setOverflowIcon(R.drawable.ad_credit);
        config.setActionBarOverflowListener(new CustomActionBarConfig.CustomActionBarOverflowListener() {
            @Override
            public void onOverflowIconClicked() {
                GAEvent.Store_CheckMyCredits.send();
            }
        });
        return config;
    }

    @Override
    protected void performGlobalSearch(final String searchString) {
        ActionHandler.getInstance().displayStoreSearchPreview(getActivity(), searchString,
                initialRecipient, mAdapter.getSelectedPagerType());
    }

    @Override
    public void onSearchButtonPressed() {
        switch (mAdapter.getSelectedPagerType()) {
            case GIFTS:
                GAEvent.Store_SearchGift.send();
                break;
            case STICKERS:
                GAEvent.Store_SearchSticker.send();
                break;
        }
        super.onSearchButtonPressed();
    }

}
