/**
 * Copyright (c) 2013 Project Goth
 *
 * ChatManagerPagerFragment.java
 * Created Mar 5, 2014, 3:15:46 PM
 */

package com.projectgoth.ui.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.common.Constants;
import com.projectgoth.common.Logger;
import com.projectgoth.common.NUEManager;
import com.projectgoth.common.Theme;
import com.projectgoth.common.ThemeValues;
import com.projectgoth.common.Tools;
import com.projectgoth.datastore.Session;
import com.projectgoth.events.GAEvent;
import com.projectgoth.i18n.I18n;
import com.projectgoth.ui.activity.ActionHandler;
import com.projectgoth.ui.activity.CustomActionBar.CustomActionBarListener;
import com.projectgoth.ui.activity.CustomActionBarConfig;
import com.projectgoth.ui.adapter.ChatManagerPagerAdapter;
import com.projectgoth.ui.fragment.BaseSearchFragment.Mode;
import com.projectgoth.ui.fragment.StartChatFragment.StartChatActionType;
import com.projectgoth.ui.widget.PopupMenu.OnPopupMenuListener;
import com.projectgoth.ui.widget.allaccessbutton.ContextAction;
import com.projectgoth.ui.widget.allaccessbutton.ContextActionListener;
import com.projectgoth.ui.widget.allaccessbutton.PageData;
import com.projectgoth.ui.widget.tooltip.ToolTip;
import com.projectgoth.ui.widget.tooltip.ToolTipRelativeLayout;
import com.projectgoth.ui.widget.tooltip.ToolTipView;
import com.projectgoth.util.AndroidLogger;

/**
 * @author mapet
 * 
 */
public class ChatManagerFragment extends BaseViewPagerFragment implements ContextActionListener,
        ToolTipView.OnToolTipViewClickedListener {

    private static final String          LOG_TAG             = AndroidLogger.makeLogTag(ChatManagerFragment.class);

    private static final int             NEW_CHAT            = 0;
    private static final int             NEW_CHATROOM        = 1;
    private static final int             ADD_INVITE_FRIENDS  = 2;
    private static final int             SEARCH              = 3;

    private final PageData               recentChatsPageData = createRecentChatsPageData();
    private final PageData               chatRoomsPageData   = createChatRoomsPageData();
    private final PageData               friendListPageData  = createFriendListPageData();

    private LinearLayout                 tabContainer;
    private ImageView                    chatListTab;
    private ImageView                    friendListTab;
    private ImageView                    chatroomTab;
    private ToolTipRelativeLayout        mToolTipFrameLayout;
    private ToolTipView                  mToolTipView;
    private ViewPager                    viewPager;

    private ChatManagerPagerAdapter      pagerAdapter;

    private Tab                          selectedTab         = Tab.CHAT_LIST;

    private int                          titleIcon           = -1;
    private String                       title               = null;
    private CustomActionBarConfig        actionBarConfig     = null;
    private CustomActionBarListener      actionBarListener   = null;
    private OnPopupMenuListener          popupMenuListener   = null;

    private ChatManagerTabSwitchListener tabSwitchListener;

    /**
     * A simple listener that can be used to inform an external receiver whether
     * an action to select a tab was performed.
     * 
     * @author angelorohit
     */
    public interface ChatManagerTabSwitchListener {

        public void onChatManagerTabSwitched(int position);
    };
    
    private enum Tab {
        CHAT_LIST, FRIEND_LIST, CHATROOM_LIST;

        public static Tab fromOrdinal(int ordinal) {
            for (Tab tab : values()) {
                if (tab.ordinal() == ordinal) {
                    return tab;
                }
            }
            return null;
        }
    }

    public ChatManagerFragment() {
        super();
        setShouldUpdateActionBarOnAttach(false);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_chat_manager;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tabContainer = (LinearLayout) view.findViewById(R.id.tabs_container);
        tabContainer.setBackgroundColor(Theme.getColor(ThemeValues.LIGHT_BACKGROUND_COLOR));

        viewPager = (ViewPager) view.findViewById(R.id.pager);
        viewPager.setOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                Logger.info.log(LOG_TAG, "onPageSelected: ", position);
                Tab newTab = Tab.fromOrdinal(position);

                if (newTab != null) {
                    selectedTab = newTab;
                    initTab();
                    if (tabSwitchListener != null) {
                        tabSwitchListener.onChatManagerTabSwitched(position);
                    }
                    backToFilterableMode();
                }

                // for [non-login] users
                // just display the first page.
                if (Session.getInstance().isBlockUsers() && position != 0){
                    showChatList();
                    ActionHandler.getInstance().displayLoginDialogFragment(getActivity());
                    return;
                }
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // DO NOTHING
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // DO NOTHING
            }
        });

        chatListTab = (ImageView) view.findViewById(R.id.chat_list_tab);
        chatListTab.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                showChatList();
            }
        });

        friendListTab = (ImageView) view.findViewById(R.id.friend_list_tab);
        friendListTab.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                showFriendList();
            }
        });
        
        chatroomTab = (ImageView) view.findViewById(R.id.chatroom_tab);
        chatroomTab.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                showChatroomList();
            }
        });

        mToolTipFrameLayout = (ToolTipRelativeLayout) view.findViewById(R.id.chat_manager_container);

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                if (NUEManager.getInstance().shouldShowNUE(LOG_TAG)) {
                    addFirstToolTipView();
                }
            }
        }, Constants.NUE_TOOLTIP_DELAY);

        showChatList();
        initTab();

        invokeOnViewCreated();
    }

    @Override
    protected FragmentStatePagerAdapter createAdapter(FragmentManager fragmentManager) {
        pagerAdapter = new ChatManagerPagerAdapter(fragmentManager, getActivity());
        pagerAdapter.setFragmentLifecycleListener(new FragmentLifecycleListener() {
            
            @Override
            public void onViewCreated(BaseFragment fragment) {
                initTab();
            }
        });
        return pagerAdapter;
    }

    private void addFirstToolTipView() {
        mToolTipView = mToolTipFrameLayout.showToolTipForView(
                new ToolTip().withText(I18n.tr("See recent chats here."))
                        .withColor(ApplicationEx.getColor(R.color.edit_text_input_color))
                        .withAnimationType(ToolTip.ANIMATIONTYPE_FROMMASTERVIEW).withShadow(false), chatListTab);
        mToolTipView.setOnToolTipViewClickedListener(this);
    }

    private void addSecondToolTipView() {
        mToolTipView = mToolTipFrameLayout.showToolTipForView(
                new ToolTip().withText(I18n.tr("Join a chat room here."))
                        .withColor(ApplicationEx.getColor(R.color.edit_text_input_color))
                        .withAnimationType(ToolTip.ANIMATIONTYPE_FROMMASTERVIEW).withShadow(false), chatroomTab);
        mToolTipView.setOnToolTipViewClickedListener(null);
    }

    public void showChatList() {
        if (selectedTab != Tab.CHAT_LIST) {
            viewPager.setCurrentItem(Tab.CHAT_LIST.ordinal(), true);
            backToFilterableMode();
        }
    }

    public void showChatroomList() {
        // for [non-login] users
        if (Session.getInstance().isBlockUsers()){
            ActionHandler.getInstance().displayLoginDialogFragment(getActivity());
            return;
        }

        if (selectedTab != Tab.CHATROOM_LIST) {
            viewPager.setCurrentItem(Tab.CHATROOM_LIST.ordinal(), true);
            backToFilterableMode();
        }
    }
    
    public void showFriendList() {
        // for [non-login] users
        if (Session.getInstance().isBlockUsers()){
            ActionHandler.getInstance().displayLoginDialogFragment(getActivity());
            return;
        }

        if (selectedTab != Tab.FRIEND_LIST) {
            viewPager.setCurrentItem(Tab.FRIEND_LIST.ordinal(), true);
            backToFilterableMode();
        }
    }

    private void initTab() {
        chatListTab.setSelected(selectedTab == Tab.CHAT_LIST);
        chatroomTab.setSelected(selectedTab == Tab.CHATROOM_LIST);
        friendListTab.setSelected(selectedTab == Tab.FRIEND_LIST);

        BaseFragment fragment = pagerAdapter.getCachedFragment(selectedTab.ordinal());
        if (fragment != null) {
            titleIcon = fragment.getTitleIcon();
            title = fragment.getTitle();
            actionBarConfig = fragment.getActionBarConfig();
            actionBarListener = fragment.getCustomActionBarListener();
            popupMenuListener = fragment.getPopupMenuListener();
        } else {
            titleIcon = -1;
            title = null;
            actionBarConfig = null;
            actionBarListener = null;
            popupMenuListener = null;
        }
    }

    @Override
    protected int getTitleIcon() {
        if (titleIcon != -1) {
            return titleIcon;
        }
        return super.getTitleIcon();
    }

    @Override
    protected String getTitle() {
        if (!TextUtils.isEmpty(title)) {
            return title;
        }
        return super.getTitle();
    }

    @Override
    public CustomActionBarConfig getActionBarConfig() {

        actionBarConfig = getSelectedChildActionBarConfig();

        if (actionBarConfig != null) {
            return actionBarConfig;
        }
        return super.getActionBarConfig();
    }

    private CustomActionBarConfig getSelectedChildActionBarConfig() {
        BaseFragment fragment = pagerAdapter.getCachedFragment(selectedTab.ordinal());
        if (fragment != null) {
            return fragment.getActionBarConfig();
        } else {
            return null;
        }
    }

    @Override
    public CustomActionBarListener getCustomActionBarListener() {
        if (actionBarListener != null) {
            return actionBarListener;
        }
        return super.getCustomActionBarListener();
    }

    @Override
    public OnPopupMenuListener getPopupMenuListener() {
        if (popupMenuListener != null) {
            return popupMenuListener;
        }
        return super.getPopupMenuListener();
    }

    @Override
    public PageData getPageData() {
        switch (selectedTab) {
            case CHAT_LIST:
                return recentChatsPageData;
            case CHATROOM_LIST:
                return chatRoomsPageData;
            case FRIEND_LIST:
                return friendListPageData;
        }
        return recentChatsPageData;
    }

    private final PageData createRecentChatsPageData() {
        return new PageData(R.drawable.ad_chat_orange)
                .addAction(new ContextAction(NEW_CHAT, R.drawable.ad_chatadd_white, this))
                .addAction(new ContextAction(ADD_INVITE_FRIENDS, R.drawable.ad_userinvite_white, this))
                .addAction(new ContextAction(SEARCH, R.drawable.ad_search_white, this));
    }

    private final PageData createChatRoomsPageData() {
        return new PageData(R.drawable.ad_chat_orange)
                .addAction(new ContextAction(NEW_CHATROOM, R.drawable.ad_chatroomadd_white, this))
                .addAction(new ContextAction(ADD_INVITE_FRIENDS, R.drawable.ad_userinvite_white, this))
                .addAction(new ContextAction(SEARCH, R.drawable.ad_search_white, this));
    }
    
    private final PageData createFriendListPageData() {
        return new PageData(R.drawable.ad_chat_orange)
                .addAction(new ContextAction(NEW_CHAT, R.drawable.ad_chatadd_white, this))
                .addAction(new ContextAction(ADD_INVITE_FRIENDS, R.drawable.ad_userinvite_white, this))
                .addAction(new ContextAction(SEARCH, R.drawable.ad_search_white, this));
    }

    public void setTabSwitchListener(final ChatManagerTabSwitchListener listener) {
        tabSwitchListener = listener;
    }

    @Override
    public void executeAction(int actionId) {
        // for [non-login] users
        if (Session.getInstance().isBlockUsers()){
            ActionHandler.getInstance().displayLoginDialogFragment(getActivity());
            return;
        }

        switch (actionId) {
            case NEW_CHAT:
                GAEvent.Chat_MainButtonStartChat.send();
                ActionHandler.getInstance().displayStartChat(getActivity(), StartChatActionType.START_NEW_CHAT, null,
                        null);
                break;
            case NEW_CHATROOM:
                GAEvent.Chat_MainButtonCreateChatroom.send();
                ActionHandler.getInstance().displayCreateChatroom(getActivity());
                break;
            case ADD_INVITE_FRIENDS:
                GAEvent.Chat_MainButtonFindFriends.send();
                ActionHandler.getInstance().displayInviteFriends(getActivity());
                break;
            case SEARCH:
                // Do the same as pressing the search icon on the action bar
                if (chatListTab.isSelected()) {
                    GAEvent.Chat_MainButtonSearchChat.send();
                } else if (chatroomTab.isSelected()) {
                    GAEvent.Chat_MainButtonSearchChatroom.send();
                }
                onSearchButtonPressed();
                break;
        }
    }

    @Override
    public void onSearchButtonPressed() {
        BaseFragment fragment = pagerAdapter.getCachedFragment(selectedTab.ordinal());
        if (fragment instanceof BaseSearchFragment) {
            ((BaseSearchFragment) fragment).setMode(Mode.FILTERING);
        }

        switch (selectedTab) {
            case CHAT_LIST:
                GAEvent.Chat_LaunchPeopleSearch.send();
                break;
            case CHATROOM_LIST:
                GAEvent.Chat_LaunchChatroomSearch.send();
                break;
            case FRIEND_LIST:
                break;
        }
    }

    @Override
    public void onToolTipViewClicked(ToolTipView toolTipView) {
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                addSecondToolTipView();
                NUEManager.getInstance().alreadyShownNUE(LOG_TAG);
            }
        }, Constants.NUE_TOOLTIP_DELAY);
    }

    private void backToFilterableMode() {
        //If switch tab need to give up filtering mode
        BaseFragment fragment = pagerAdapter.getCachedFragment(selectedTab.ordinal());
        if (fragment instanceof BaseSearchFragment) {
            Mode mode = ((BaseSearchFragment) fragment).getMode();
            if (mode == Mode.FILTERING) {
                //Back to filterable mode need to clear filter text
                ((BaseSearchFragment) fragment).clearFilterText();
                ((BaseSearchFragment) fragment).setMode(Mode.FILTERABLE);
            }
            Tools.hideVirtualKeyboard(getActivity());
        }
    }
}
