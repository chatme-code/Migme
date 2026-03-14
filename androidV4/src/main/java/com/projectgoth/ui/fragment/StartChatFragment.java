/**
 * Copyright (c) 2013 Project Goth
 *
 * StartChatFragment.java
 * Created Jul 24, 2013, 1:48:42 PM
 */

package com.projectgoth.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.AbsoluteSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mig33.diggle.events.Events;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.mime.MimeData;
import com.projectgoth.b.data.mime.MimeTypeDataModel;
import com.projectgoth.blackhole.enums.MessageType;
import com.projectgoth.common.Config;
import com.projectgoth.common.Constants;
import com.projectgoth.common.ShareManager;
import com.projectgoth.common.Theme;
import com.projectgoth.common.ThemeValues;
import com.projectgoth.common.Tools;
import com.projectgoth.common.WebURL;
import com.projectgoth.controller.ChatController;
import com.projectgoth.controller.ProgressDialogController;
import com.projectgoth.datastore.ChatDatastore;
import com.projectgoth.datastore.Session;
import com.projectgoth.datastore.UserDatastore;
import com.projectgoth.events.GAEvent;
import com.projectgoth.i18n.I18n;
import com.projectgoth.imagefetcher.UIUtils;
import com.projectgoth.model.ChatConversation;
import com.projectgoth.model.ContextMenuItem;
import com.projectgoth.model.MenuOption;
import com.projectgoth.nemesis.model.ContactGroup;
import com.projectgoth.nemesis.model.Friend;
import com.projectgoth.nemesis.model.Sticker;
import com.projectgoth.ui.activity.ActionHandler;
import com.projectgoth.ui.activity.AlertHandler;
import com.projectgoth.ui.activity.AlertHandler.TextInputListener;
import com.projectgoth.ui.activity.CustomActionBarConfig;
import com.projectgoth.ui.activity.CustomActionBarConfig.NavigationButtonState;
import com.projectgoth.ui.activity.CustomActionBarConfig.OverflowButtonState;
import com.projectgoth.ui.activity.FragmentHandler;
import com.projectgoth.ui.fragment.FriendListFragment.CheckboxChangeListener;
import com.projectgoth.ui.fragment.FriendListFragment.ContactGroupListener;
import com.projectgoth.ui.fragment.FriendListFragment.FriendListItemActionType;
import com.projectgoth.ui.fragment.FriendListFragment.FriendsListListener;
import com.projectgoth.ui.listener.ContextMenuItemListener;
import com.projectgoth.ui.widget.ButtonEx;
import com.projectgoth.ui.widget.GifImageView;
import com.projectgoth.ui.widget.MessageInputPanel;
import com.projectgoth.ui.widget.PopupMenu.OnPopupMenuListener;
import com.projectgoth.ui.widget.RelativeLayoutEx;
import com.projectgoth.ui.widget.ScrollViewEx;
import com.projectgoth.ui.widget.SelectedItemBox;
import com.projectgoth.ui.widget.SelectedItemLabel;

import java.util.ArrayList;
import java.util.List;

/**
 * @author mapet
 * 
 */
public class StartChatFragment extends BaseFragment implements OnClickListener, FriendsListListener,
        CheckboxChangeListener, TextWatcher, ContextMenuItemListener, ContactGroupListener,
        SelectedItemBox.SelectedItemBoxListener, MessageInputPanel.MessageInputPanelListener {

    /**
     * should be a List<String> usernames
     */
    public static final String  PARAM_INITIAL_SELECTED_USERS  = "PARAM_INITIAL_SELECTED_USERS";
    public static final String  PARAM_START_CHAT_ACTION       = "PARAM_START_CHAT_ACTION";
    public static final String  PARAM_CONVERSATION_ID         = "PARAM_CONVERSATION_ID";

    private RelativeLayoutEx    mStartChatContainer;
    private SelectedItemBox     mSelectedUserContainer;
    private ScrollViewEx        mScrollContainer;
    private ImageView           mSearchIcon;
    private RelativeLayout      mSearchBarContainer;
    private FrameLayout         mFriendListContainer;
    private FriendListFragment  mFriendList;
    private RelativeLayout      bottomMenuBar;
    private ButtonEx            startChatButton;
    private FrameLayout         msgInputPanelContainer;
    private MessageInputPanel   msgInputPanel;
    private View                emptyView;

    private StartChatActionType mAction;
    private String              mConversationId;

    private List<String>        mPreselectedUsers;
    private List<Friend>        mPreselectedUsersListData     = new ArrayList<Friend>();
    private List<Friend>        mSelectedUsersListData        = new ArrayList<Friend>();
    private int                 selectedFriendsCount;
    private int                 mFriendListContainerHeight    = 0;
    private int                 mSearchBarContainerBaseHeight = 0;
    private boolean             mDisplayAllContacts;
    private boolean             isIMContactsSelectable        = false;
    private boolean             isPerformFilter               = false;

    public enum StartChatActionType {
        START_NEW_CHAT(0), INVITE_FRIENDS(1), ADD_TO_GROUP_CHAT(2), ADD_TO_PRIVATE_CHAT(3), SHARE_TO_NEW_CHAT(4);

        private int type;

        private StartChatActionType(int type) {
            this.type = type;
        }

        public int getType() {
            return this.type;
        }

        public static StartChatActionType fromValue(int type) {
            for (StartChatActionType listType : values()) {
                if (listType.getType() == type) {
                    return listType;
                }
            }
            return START_NEW_CHAT;
        }
    }

    public interface StartChatListener {

        public void onChatCreated(String conversationId);
    }

    @Override
    protected void readBundleArguments(Bundle args) {
        super.readBundleArguments(args);
        mAction = StartChatActionType.fromValue(args.getInt(PARAM_START_CHAT_ACTION));
        mConversationId = args.getString(PARAM_CONVERSATION_ID);
        mPreselectedUsers = args.getStringArrayList(PARAM_INITIAL_SELECTED_USERS);

        if (mPreselectedUsers != null) {
            for (String username : mPreselectedUsers) {
                Friend friend = UserDatastore.getInstance().findMig33User(username);
                if (friend != null) {
                    mPreselectedUsersListData.add(friend);
                }
            }
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_start_chat;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mSelectedUsersListData != null) {
            for (Friend friend : mSelectedUsersListData) {
                if (friend != null) {
                    mFriendList.removeFriendFromList(friend);
                    friend.setChecked(false);
                    mFriendList.addFriendToList(friend);
                }
            }
        }
        // reset the status of selectable
        if (!isIMContactsSelectable) {
            mFriendList.disableIMContactsSelection();
            isIMContactsSelectable = false;
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mStartChatContainer = (RelativeLayoutEx) view.findViewById(R.id.start_chat_container);

        mSearchBarContainer = (RelativeLayout) view.findViewById(R.id.search_user_container);

        mSelectedUserContainer = (SelectedItemBox) view.findViewById(R.id.selected_container);
        mSelectedUserContainer.setOnClickListener(this);
        mSelectedUserContainer.setListener(this);

        mSearchIcon = (ImageView) view.findViewById(R.id.search_icon);

        mScrollContainer = (ScrollViewEx) view.findViewById(R.id.selected_scroll_view);
        mScrollContainer.setMaxHeight(ApplicationEx.getDimension(R.dimen.sel_usr_box_max_h));

        updateSelectedCounter();

        mDisplayAllContacts = mAction == StartChatActionType.START_NEW_CHAT ? true : false;

        mFriendListContainer = (FrameLayout) view.findViewById(R.id.friend_list_container);

        mFriendList = FragmentHandler.getInstance().createFriendListFragment(mDisplayAllContacts,
                FriendListItemActionType.CHECKBOX, (ArrayList<String>) mPreselectedUsers, false);
        mFriendList.setFriendsListListener(this);
        mFriendList.setContactGroupListener(this);

        addChildFragment(R.id.friend_list_container, mFriendList);

        bottomMenuBar = (RelativeLayout) view.findViewById(R.id.bottom_menu_bar);
        bottomMenuBar.setBackgroundColor(Theme.getColor(ThemeValues.BOTTOM_BAR_BG_COLOR));
        // Keep bottom view always showing
        // mFriendList.setBottomBarMenu(bottomMenuBar);

        startChatButton = (ButtonEx) view.findViewById(R.id.start_chat_button);
        startChatButton.setOnClickListener(this);

        msgInputPanelContainer = (FrameLayout) view.findViewById(R.id.msg_input_panel);

        switch (mAction) {
            case START_NEW_CHAT:
                startChatButton.setVisibility(View.GONE);
                msgInputPanelContainer.setVisibility(View.VISIBLE);
                msgInputPanel = new MessageInputPanel();
                msgInputPanel.setListener(this);
                mStartChatContainer.setKeyboardListener(msgInputPanel);
                addChildFragment(R.id.msg_input_panel, msgInputPanel);
                break;
            case INVITE_FRIENDS:
                startChatButton.setText(I18n.tr("INVITE"));
                startChatButton.setVisibility(View.VISIBLE);
                msgInputPanelContainer.setVisibility(View.GONE);
                break;
            case ADD_TO_GROUP_CHAT:
            case ADD_TO_PRIVATE_CHAT:
                startChatButton.setText(I18n.tr("ADD"));
                startChatButton.setVisibility(View.VISIBLE);
                msgInputPanelContainer.setVisibility(View.GONE);
                break;
            case SHARE_TO_NEW_CHAT:
                startChatButton.setVisibility(View.GONE);
                msgInputPanelContainer.setVisibility(View.VISIBLE);
                msgInputPanel = new MessageInputPanel();
                msgInputPanel.setListener(this);
                mStartChatContainer.setKeyboardListener(msgInputPanel);
                addChildFragment(R.id.msg_input_panel, msgInputPanel);
                break;
        }

        // update title by action
        setTitle(getTitle());

        showOrHideEmptyView();

    }

    private void showOrHideEmptyView() {
        // setup empty view if it has no friend
        if (mAction == StartChatActionType.START_NEW_CHAT && !UserDatastore.getInstance().hasFriend()) {
            emptyView = LayoutInflater.from(getActivity()).inflate(R.layout.start_chat_empty_view, null);
            setupEmptyView(emptyView);

            // add the empty view
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            mStartChatContainer.addView(emptyView, params);

            mSearchBarContainer.setVisibility(View.GONE);
            bottomMenuBar.setVisibility(View.GONE);
            mFriendListContainer.setVisibility(View.GONE);

        } else {
            if (emptyView != null && emptyView.getParent() == mStartChatContainer) {
                mStartChatContainer.removeView(emptyView);
            }
            mSearchBarContainer.setVisibility(View.VISIBLE);
            bottomMenuBar.setVisibility(View.VISIBLE);
            mFriendListContainer.setVisibility(View.VISIBLE);
            mFriendListContainer.getViewTreeObserver().addOnGlobalLayoutListener(
                    new ViewTreeObserver.OnGlobalLayoutListener() {

                        @Override
                        public void onGlobalLayout() {
                            if (mFriendListContainerHeight == 0 && mFriendListContainer.getHeight() != 0) {
                                int cellHeight = ApplicationEx.getDimension(R.dimen.contact_pic_size_medium);
                                mFriendListContainerHeight = mFriendListContainer.getHeight() - cellHeight;
                                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                                    mFriendListContainer.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                                } else {
                                    mFriendListContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                                }
                                updateFriendListContainerHeight();
                            }
                        }
                    });
            mSearchBarContainer.getViewTreeObserver().addOnGlobalLayoutListener(
                    new ViewTreeObserver.OnGlobalLayoutListener() {

                        int lastHeight = 0;

                        @Override
                        public void onGlobalLayout() {
                            int containerHeight = mSearchBarContainer.getHeight();
                            if (lastHeight != containerHeight) {
                                if (mSearchBarContainerBaseHeight == 0) {
                                    mSearchBarContainerBaseHeight = containerHeight;
                                }
                                if (mFriendListContainerHeight != 0) {
                                    mFriendListContainerHeight -= (containerHeight - lastHeight);
                                    updateFriendListContainerHeight();
                                }
                                lastHeight = containerHeight;
                            }
                        }
                    });
        }
    }

    private void setupEmptyView(View emptyView) {

        GifImageView gifView = (GifImageView) emptyView.findViewById(R.id.empty_chat_list_icon);
        gifView.setOnClickListener(this);
        gifView.setCyclePlay(false);
        gifView.setGifId(R.drawable.bubble_burst);
        gifView.startAnimation();

        TextView hint = (TextView) emptyView.findViewById(R.id.hint);
        hint.setText(I18n.tr("Ready to chat?"));

        TextView hint2 = (TextView) emptyView.findViewById(R.id.hint2);
        String finalText = I18n.tr("Start by making friends.");
        String link = I18n.tr("making friends");
        SpannableString spannableStr = new SpannableString(finalText);
        UIUtils.setLinkSpan(spannableStr, finalText, link, new UIUtils.LinkClickListener() {

            @Override
            public void onClick() {
                ActionHandler.getInstance().displayInviteFriends(getActivity());
            }
        });
        hint2.setMovementMethod(LinkMovementMethod.getInstance());
        hint2.setText(spannableStr);

        TextView recommendTitle = (TextView) emptyView.findViewById(R.id.recommend_title);
        recommendTitle.setText(I18n.tr("Recommended people"));

        ProfileListFragment recommendList = FragmentHandler.getInstance().getRecommendedUsersFragment();
        recommendList.setShouldUpdateActionBarOnAttach(false);
        addChildFragment(R.id.recommend_list, recommendList);
    }

    private void addUser(Friend friend) {

        if (friend.isIMContact() || (friend.isFusionContact() && isIMContactSelected())) {
            // IM contact is single selection , so we uncheck the selected one
            for (Friend f : mSelectedUsersListData) {
                f.setChecked(false);
                removeUserFromContainer(f);
            }
            mSelectedUsersListData.clear();
        }

        mSelectedUsersListData.add(friend);
        addUserToContainer(friend);
        updateSelectedCounter();

        if (mDisplayAllContacts) {
            if (isIMContactsSelectable && isMultipleFusionContactsSelected()) {
                // disable the IM contacts selection
                mFriendList.disableIMContactsSelection();
                isIMContactsSelectable = false;
            }
        }

        // update the friend list
        friend.setChecked(true);
        mFriendList.notifyDataSetChanged(false);

        // update the icon on the top left
        mSearchIcon.setImageResource(R.drawable.ad_chat_grey);

        // scroll to bottom for it has exceeded the max height
        mScrollContainer.post(new Runnable() {

            @Override
            public void run() {
                mScrollContainer.smoothScrollTo(0, mScrollContainer.getHeight());
            }
        });

        if (mAction == StartChatActionType.START_NEW_CHAT) {
            setupMsgInputPanel();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        setupMsgInputPanel();
    }

    private void setupMsgInputPanel() {
        if (mAction == StartChatActionType.SHARE_TO_NEW_CHAT) {
            msgInputPanel.onlyEnableTextSending();
            return;
        }

        if (msgInputPanel != null) {
            if (hasContactSelected()) {
                msgInputPanel.showEnabledSendButtons();
            } else {
                msgInputPanel.showDisabledSendButtons();
            }
        }

        if (msgInputPanel != null) {
            if (isSingleFusionContactSelected()) {
                msgInputPanel.setInitialRecipient(mSelectedUsersListData.get(0).getDisplayName());
            } else {
                msgInputPanel.setInitialRecipient(Constants.BLANKSTR);
            }
        }

    }

    private boolean isSingleFusionContactSelected() {
        if (mSelectedUsersListData != null && mSelectedUsersListData.size() == 1) {
            if (mSelectedUsersListData.get(0).isFusionContact()) {
                return true;
            }
        }
        return false;
    }

    private boolean isMultipleFusionContactsSelected() {
        if (mSelectedUsersListData != null && mSelectedUsersListData.size() >= 2) {
            if (mSelectedUsersListData.get(0).isFusionContact()) {
                return true;
            }
        }
        return false;
    }

    private boolean isIMContactSelected() {
        if (mSelectedUsersListData != null && mSelectedUsersListData.size() >= 1) {
            if (mSelectedUsersListData.get(0).isIMContact()) {
                return true;
            }
        }
        return false;
    }

    private boolean hasContactSelected() {
        if (mSelectedUsersListData != null && mSelectedUsersListData.size() > 0) {
            return true;
        }

        return false;
    }

    private void removeUser(Friend friend) {
        mSelectedUsersListData.remove(friend);
        updateSelectedCounter();

        if (mDisplayAllContacts) {
            if (!isIMContactsSelectable && !isMultipleFusionContactsSelected()) {
                mFriendList.disableIMContactsSelection();
                isIMContactsSelectable = false;
            }
        }

        removeUserFromContainer(friend);

        // update friend list
        friend.setChecked(false);
        mFriendList.notifyDataSetChanged(false);

        // update the icon on the top left
        if (mSelectedUsersListData.size() == 0) {
            mSearchIcon.setImageResource(R.drawable.ad_search_grey);
        } else {
            mSearchIcon.setImageResource(R.drawable.ad_chat_grey);
        }

        if (mAction == StartChatActionType.START_NEW_CHAT) {
            setupMsgInputPanel();
        }

    }

    private void updateSelectedCounter() {
        if (mSelectedUsersListData != null) {
            selectedFriendsCount = mSelectedUsersListData.size();
        }

        // TODO update title with the counter on action bar
        if (selectedFriendsCount > 0) {
            String title = getTitle();
            String counter = " (" + selectedFriendsCount + ")";
            String newTitle = title + counter;
            SpannableString spannableString = new SpannableString(newTitle);
            int start = newTitle.indexOf(counter);
            AbsoluteSizeSpan textSizeSpan = new AbsoluteSizeSpan(
                    (int) ApplicationEx.getDimension(R.dimen.text_size_large));
            spannableString.setSpan(textSizeSpan, start, start + counter.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            setTitle(spannableString);
        } else if (selectedFriendsCount == 0) {
            String title = getTitle();
            setTitle(title);
        }
    }

    private void handleStartChatButtonClick() {
        final List<Friend> friendsList = mSelectedUsersListData;

        if (!friendsList.isEmpty()) {

            ChatConversation conversation = ChatDatastore.getInstance().getChatConversationWithId(mConversationId);

            switch (mAction) {
                case START_NEW_CHAT:
                case ADD_TO_PRIVATE_CHAT:
                case SHARE_TO_NEW_CHAT:
                    startChat(null);
                    break;
                case INVITE_FRIENDS:
                    if (conversation != null) {
                        for (Friend friend : friendsList) {
                            String message = String.format(I18n.tr("Join me in the %s chat room."),
                                    "+[" + conversation.getChatId() + "]");
                            ChatController.getInstance().sendPrivateChatMessage(friend.getUsername(), message);
                        }
                    }
                    break;
                case ADD_TO_GROUP_CHAT:
                    GAEvent.Chat_AddPeopleToChat.send();
                    if (friendsList.size() > 0 && conversation != null) {
                        ActionHandler.getInstance().inviteToGroupChat(conversation.getChatId(), friendsList,
                                MessageType.FUSION);
                    }
                    break;
            }

            closeFragment();

        } else {
            Toast.makeText(getActivity(), I18n.tr("Select users"), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onClick(View view) {
        if (view instanceof SelectedItemLabel) {

            Friend friend = ((SelectedItemLabel) view).getFriend();
            removeUser(friend);

        } else {

            int viewId = view.getId();

            switch (viewId) {
                case R.id.empty_chat_list_icon:
                {
                    GifImageView gifView = (GifImageView) view;

                    if (!gifView.isAnimating()) {
                        gifView.startAnimation();
                    }
                }
                    break;
                case R.id.selected_container:
                    mSelectedUserContainer.showInputCursor();
                    break;
                case R.id.start_chat_button:
                    handleStartChatButtonClick();
                    break;
            }
        }
    }

    @Override
    public void onFriendItemClicked(View v, Friend friend) {
        if (!friend.isSelectable()) {
            return;
        }

        if (!friend.isFriendSelected()) {
            addUser(friend);
        } else {
            removeUser(friend);
        }
    }

    private void addUserToContainer(Friend friend) {
        SelectedItemLabel label = new SelectedItemLabel(ApplicationEx.getContext());
        label.setFriend(friend);
        label.setOnClickListener(this);

        mSelectedUserContainer.addItem(label);

        if (isPerformFilter) {
            mSelectedUserContainer.clearTextInput();
        }
    }

    private void removeUserFromContainer(Friend friend) {
        int count = mSelectedUserContainer.getChildCount();
        for (int i = 0; i < count; i++) {
            View childView = mSelectedUserContainer.getChildAt(i);
            if (childView instanceof SelectedItemLabel) {
                SelectedItemLabel label = (SelectedItemLabel) childView;
                if (friend == label.getFriend()) {
                    mSelectedUserContainer.removeViewAt(i);
                    break;
                }
            }
        }
    }

    @Override
    public void onFriendItemLongPressed(View v, Friend friend) {
        ArrayList<ContextMenuItem> menuItemList = getContextMenuOptions(friend);
        Tools.showContextMenu(friend.getDisplayName(), menuItemList, this);
    }

    private ArrayList<ContextMenuItem> getContextMenuOptions(Friend data) {
        ArrayList<ContextMenuItem> menuItems = new ArrayList<ContextMenuItem>();
        menuItems.add(new ContextMenuItem(I18n.tr("View profile"), R.id.option_item_view_profile, data));
        menuItems.add(new ContextMenuItem(I18n.tr("Chat"), R.id.option_item_chat, data));
        menuItems.add(new ContextMenuItem(I18n.tr("Send gift"), R.id.option_item_send_gift, data));
        menuItems.add(new ContextMenuItem(I18n.tr("Report abuse"), R.id.option_item_report_abuse, data));
        if (mDisplayAllContacts && mFriendList.isFusionFriendsGrouped()) {
            menuItems.add(new ContextMenuItem(I18n.tr("Move to group"), R.id.option_item_move_to_group, data));
        }
        menuItems.add(new ContextMenuItem(I18n.tr("Block/Mute"), R.id.option_item_block, data));
        menuItems.add(new ContextMenuItem(I18n.tr("Unfriend"), R.id.option_item_remove_friend, data));
        return menuItems;
    }

    @Override
    protected void registerReceivers() {
        registerEvent(Events.Contact.REMOVED);
        registerEvent(Events.Contact.RECEIVED);
    }

    @Override
    protected void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Events.Contact.REMOVED)) {
            showOrHideEmptyView();
        } else if (intent.getAction().equals(Events.Contact.RECEIVED)) {
            showOrHideEmptyView();
        }
    }

    @Override
    public void onCheckboxClicked(View v, Friend friend) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        mFriendList.afterTextChanged(s);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public CustomActionBarConfig getActionBarConfig() {
        CustomActionBarConfig config = new CustomActionBarConfig();
        config.setNavigationButtonState(NavigationButtonState.BACK);
        config.setShowOverflowButtonState(OverflowButtonState.POPUP);
        return config;
    }

    @Override
    protected String getTitle() {
        String title = I18n.tr("New chat");
        if (mAction != null) {
            switch (mAction) {
                case START_NEW_CHAT:
                    title = I18n.tr("New chat");
                    break;
                case ADD_TO_PRIVATE_CHAT:
                    title = I18n.tr("Add");
                    break;
                case ADD_TO_GROUP_CHAT:
                    title = I18n.tr("Add");
                    break;
                case INVITE_FRIENDS:
                    title = I18n.tr("Invite");
                    break;
                default:
                    break;
            }
        }
        return title;
    }

    @Override
    protected int getTitleIcon() {
        return R.drawable.ad_chat_white;
    }

    @Override
    public ArrayList<MenuOption> getMenuOptions() {
        ArrayList<MenuOption> menuItems = new ArrayList<MenuOption>();
        if (mDisplayAllContacts) {

            MenuOption groupFriendsListOption = new MenuOption(I18n.tr("View by groups"), R.drawable.ad_grp_white,
                    new MenuOption.MenuAction() {

                        @Override
                        public void onAction(MenuOption option, boolean isSelected) {
                            mFriendList.setFusionFriendsGrouped(isSelected);
                            mFriendList.notifyDataSetChanged(true);
                        }

                    });
            groupFriendsListOption.setMenuOptionType(MenuOption.MenuOptionType.CHECKABLE);
            groupFriendsListOption.setChecked(mFriendList.isFusionFriendsGrouped());
            menuItems.add(groupFriendsListOption);

            menuItems.add(new MenuOption(I18n.tr("New group"), R.drawable.ad_plus_white,
                    R.id.option_item_add_contact_group));
        }

        if (mFriendList.showOnlineFriendsOnly()) {
            menuItems.add(new MenuOption(I18n.tr("Show offline friends"), R.drawable.ad_visible_white,
                    R.id.option_item_show_offline_contacts));
        } else {
            menuItems.add(new MenuOption(I18n.tr("Hide offline friends"), R.drawable.ad_invisible_white,
                    R.id.option_item_hide_offline_contacts));
        }

        return menuItems;
    }

    @Override
    public OnPopupMenuListener getPopupMenuListener() {
        return this;
    }

    @Override
    public void onMenuOptionClicked(MenuOption menuOption) {
        switch (menuOption.getActionId()) {
            case R.id.option_item_hide_offline_contacts:
            {
                Session.getInstance().setShowOnlineFriendsOnly(true);
                mFriendList.setShowOnlineFriendsOnly(true);
                mFriendList.notifyDataSetChanged(true);
                break;
            }
            case R.id.option_item_show_offline_contacts:
            {
                Session.getInstance().setShowOnlineFriendsOnly(false);
                mFriendList.setShowOnlineFriendsOnly(false);
                mFriendList.notifyDataSetChanged(true);
                break;
            }
            case R.id.option_item_group_fusion_friends:
            {
                mFriendList.setFusionFriendsGrouped(true);
                mFriendList.notifyDataSetChanged(true);
                break;
            }
            case R.id.option_item_ungroup_fusion_friends:
            {
                mFriendList.setFusionFriendsGrouped(false);
                mFriendList.notifyDataSetChanged(true);
                break;
            }
            case R.id.option_item_add_contact_group:
            {
                AlertHandler.showTextInputDialog(getActivity(), new TextInputListener() {

                    @Override
                    public void onOk(String groupName) {
                        UserDatastore.getInstance().requestAddContactGroup(groupName);
                    }

                    @Override
                    public void onCancel() {
                    }
                }, I18n.tr("Create contact group")).show();
                break;
            }
            default:
                break;

        }
    }

    @Override
    public void onContextMenuItemClick(ContextMenuItem menuItem) {
        Friend friend;

        int id = menuItem.getId();
        switch (id) {
            case R.id.option_item_view_profile:
                friend = (Friend) menuItem.getData();
                ActionHandler.getInstance().displayProfile(getActivity(), friend.getUsername());
                break;
            case R.id.option_item_chat:
                friend = (Friend) menuItem.getData();
                ActionHandler.getInstance().displayPrivateChat(getActivity(), friend.getUsername());
                break;
            case R.id.option_item_send_gift:
                friend = (Friend) menuItem.getData();
                ActionHandler.getInstance().displayStore(getActivity(), friend.getUsername());
                break;
            case R.id.option_item_report_abuse:
                friend = (Friend) menuItem.getData();
                String urlReport = String.format(WebURL.URL_REPORT_USER, friend.getUsername());
                ActionHandler.getInstance().displayBrowser(getActivity(), urlReport);
                break;
            case R.id.option_item_move_to_group:
                friend = (Friend) menuItem.getData();
                ActionHandler.getInstance().moveFriend(getActivity(), friend.getContactID(), friend.getGroupID());
                break;
            case R.id.option_item_block:
                friend = (Friend) menuItem.getData();
                ActionHandler.getInstance().blockFriend(getActivity(), null, friend.getDisplayName(),
                        friend.getUsername());
                break;
            case R.id.option_item_remove_friend:
                friend = (Friend) menuItem.getData();
                ActionHandler.getInstance().removeFriend(getActivity(), friend.getDisplayName(), friend.getContactID());
                break;
            case R.id.option_item_add_contact_group:
                AlertHandler.showTextInputDialog(getActivity(), new TextInputListener() {

                    @Override
                    public void onOk(String groupName) {
                        UserDatastore.getInstance().requestAddContactGroup(groupName);
                    }

                    @Override
                    public void onCancel() {
                    }
                }, I18n.tr("Create contact group")).show();

                break;
            case R.id.option_item_rename_contact_group:
            {
                ContactGroup contactGroup = (ContactGroup) menuItem.getData();
                final int groupId = contactGroup.getGroupID();
                String title = String.format(I18n.tr("Rename contact group %s"), contactGroup.getGroupName());
                AlertHandler.showTextInputDialog(getActivity(), new TextInputListener() {

                    @Override
                    public void onOk(String newGroupName) {
                        UserDatastore.getInstance().requestUpdateContactGroup(groupId, newGroupName);
                    }

                    @Override
                    public void onCancel() {
                    }
                }, title).show();

                break;
            }
            case R.id.option_item_remove_contact_group:
            {
                ContactGroup contactGroup = (ContactGroup) menuItem.getData();
                final int groupId = contactGroup.getGroupID();
                ActionHandler.getInstance().removeContactGroup(getActivity(), groupId, contactGroup.getGroupName());
                break;
            }
            default:
                break;
        }
    }

    @Override
    public void onContactGroupItemLongPressed(ContactGroup contactGroup) {
        String title = I18n.tr("Options");
        ArrayList<ContextMenuItem> menuItemList = getContactGroupContextMenuOptions(contactGroup);
        Tools.showContextMenu(title, menuItemList, this);
    }

    @Override
    public void onContactGroupItemToggle(ContactGroup contactGroup) {

    }

    private ArrayList<ContextMenuItem> getContactGroupContextMenuOptions(ContactGroup data) {
        ArrayList<ContextMenuItem> menuItems = new ArrayList<ContextMenuItem>();
        menuItems.add(new ContextMenuItem(I18n.tr("Add contact group"), R.id.option_item_add_contact_group, data));
        if (data.getGroupID() > 0) {
            menuItems.add(new ContextMenuItem(I18n.tr("Rename contact group"), R.id.option_item_rename_contact_group,
                    data));
            menuItems.add(new ContextMenuItem(I18n.tr("Delete contact group"), R.id.option_item_remove_contact_group,
                    data));
        }

        return menuItems;
    }

    @Override
    public void performFilter(final String filterString) {
        if (filterString.equals(Constants.BLANKSTR)) {
            isPerformFilter = false;
        } else {
            isPerformFilter = true;
        }
        mFriendList.performFilter(filterString);
    }

    @Override
    public void onSendMessageButtonClick() {
        // go to the chat screen, creating conversation if not exist
        // and send the message
        if (msgInputPanel != null) {
            startChat(new StartChatListener() {

                @Override
                public void onChatCreated(String conversationId) {
                    ProgressDialogController.getInstance().hideProgressDialog();
                    GAEvent.Chat_SendTextMessage.send();
                    switch (mAction) {
                        case SHARE_TO_NEW_CHAT:
                            ArrayList<MimeData> dataList = new ArrayList<MimeData>(MimeTypeDataModel.parse(
                                    ShareManager.mCurrentShareMimeType, ShareManager.mCurrentShareMimeData));

                            if (dataList != null && dataList.size() > 0) {
                                ChatController.getInstance().sendShareMessage(conversationId,
                                        msgInputPanel.getTextMessage(), ShareManager.mCurrentShareUrl, dataList);
                            }

                            ShareManager.clearShareData();

                            closeActivity();
                            break;
                        default:
                            ChatController.getInstance()
                                    .sendChatMessage(conversationId, msgInputPanel.getTextMessage());
                            closeActivity();
                            break;
                    }
                }
            });
        }
    }

    public void onGiftIconClick() {
        // get selected username list
        ArrayList<String> selectedUsers = getSelectedUsernameList();

        if (!hasContactSelected())
            return;

        final String initialRecipient = selectedUsers.size() > 1 ? selectedUsers.get(0) : "";
        ActionHandler.getInstance().displayGiftCenterFragment(getActivity(), selectedUsers, initialRecipient);
    }

    private ArrayList<String> getSelectedUsernameList() {
        ArrayList<Friend> friendsList = new ArrayList<Friend>();
        if (!mPreselectedUsersListData.isEmpty()) {
            friendsList.addAll(mPreselectedUsersListData);
        }
        if (mSelectedUsersListData != null) {
            friendsList.addAll(mSelectedUsersListData);
        }

        ArrayList<String> usernameList = new ArrayList<String>();
        for (Friend friend : friendsList) {
            usernameList.add(friend.getUsername());
        }

        return usernameList;
    }

    public void onStickerSelect(final Sticker sticker) {
        startChat(new StartChatListener() {

            @Override
            public void onChatCreated(String conversationId) {
                ProgressDialogController.getInstance().hideProgressDialog();
                GAEvent.Chat_SendStickerUi.send(sticker.getPackId());
                ChatController.getInstance().sendSticker(conversationId, sticker);
                closeActivity();
            }
        });
    }

    public void onPhotoClick(final byte[] photo) {
        startChat(new StartChatListener() {

            @Override
            public void onChatCreated(String conversationId) {
                ProgressDialogController.getInstance().hideProgressDialog();
                GAEvent.Chat_SendImage.send(photo.length);
                ChatController.getInstance().sendPhotoMessage(conversationId, photo, getActivity());
                closeActivity();
            }
        });
    }

    private void startChat(StartChatListener listener) {
        GAEvent.Chat_StartChat.send();

        ArrayList<Friend> friendsList = new ArrayList<Friend>();

        if (!hasContactSelected())
            return;

        friendsList.addAll(mSelectedUsersListData);

        if (!mPreselectedUsersListData.isEmpty()) {
            friendsList.addAll(mPreselectedUsersListData);
        }

        String conversationId = null;

        if (friendsList.size() == 1) {
            Friend friend = friendsList.get(0);
            if (friend.isFusionContact()) {
                conversationId = ActionHandler.getInstance().displayPrivateChat(getActivity(),
                        friendsList.get(0).getUsername());
                if (listener != null)
                    listener.onChatCreated(conversationId);
            }
        } else if (friendsList.size() >= 2) {
            ActionHandler.getInstance().startGroupChat(friendsList, MessageType.FUSION, listener);
            ProgressDialogController.getInstance().showProgressDialog(getActivity(), ProgressDialogController.ProgressType.Creating);
        }
    }

    private void closeActivity() {
        getActivity().finish();
    }

    private void updateFriendListContainerHeight() {
        ViewGroup.LayoutParams layoutParams;
        switch (mAction) {
            case START_NEW_CHAT: {
                if (msgInputPanel == null) {
                    return;
                }
                layoutParams = mFriendListContainer.getLayoutParams();
                int offset = 0;
                if (msgInputPanel.isStickerDrawerShown() || msgInputPanel.isEmoticonDrawerShown()) {
                    offset = msgInputPanel.getDrawerHeight();
                } else if (msgInputPanel.isKeyboardDisplayed()) {
                    offset = Config.getInstance().getSoftKeyboardHeight();
                }
                if (layoutParams != null && mFriendListContainerHeight > 0) {
                    layoutParams.height = mFriendListContainerHeight - offset;
                    mFriendListContainer.requestLayout();
                }
                break;
            }
            case INVITE_FRIENDS:
            case ADD_TO_GROUP_CHAT:
            case ADD_TO_PRIVATE_CHAT: {
                layoutParams = mFriendListContainer.getLayoutParams();
                if (layoutParams != null && mFriendListContainerHeight > 0) {
                    layoutParams.height = mFriendListContainerHeight;
                    mFriendListContainer.requestLayout();
                }
                break;
            }
        }
    }

    public void onEmotionSelectionShown() {
        updateFriendListContainerHeight();
    };

    public void onEmotionSelectionHidden() {
        updateFriendListContainerHeight();
    };

    public void onStickerSelectionShown() {
        updateFriendListContainerHeight();
    };

    public void onStickerSelectionHidden() {
        updateFriendListContainerHeight();
    };

    public void onKeyboardShown() {
        updateFriendListContainerHeight();
    };

    public void onKeyboardHidden() {
        if (msgInputPanel != null) {
            if (msgInputPanel.isEmoticonDrawerShown() || msgInputPanel.isStickerDrawerShown()) {
                // If emotion or sticker drawer shown don't need to reset
                // container height
                return;
            }
            updateFriendListContainerHeight();
        }
    };

    public void onTextInputChanged(CharSequence s, int start, int before, int count) {
    };
}
