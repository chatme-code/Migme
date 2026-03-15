
package com.projectgoth.ui.fragment;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.mig33.diggle.events.Events;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.common.Constants;
import com.projectgoth.common.TextUtils;
import com.projectgoth.common.Tools;
import com.projectgoth.controller.ChatController;
import com.projectgoth.datastore.Session;
import com.projectgoth.events.GAEvent;
import com.projectgoth.i18n.I18n;
import com.projectgoth.model.ChatConversation;
import com.projectgoth.model.ContextMenuItem;
import com.projectgoth.notification.NotificationType;
import com.projectgoth.ui.activity.ActionHandler;
import com.projectgoth.ui.activity.CustomActionBarConfig;
import com.projectgoth.ui.activity.CustomActionBarConfig.OverflowButtonState;
import com.projectgoth.ui.adapter.ChatListAdapter;
import com.projectgoth.ui.fragment.GlobalSearchFragment.SearchType;
import com.projectgoth.ui.fragment.StartChatFragment.StartChatActionType;
import com.projectgoth.ui.holder.BaseViewHolder.BaseViewListener;
import com.projectgoth.ui.holder.BasicListFooterViewHolder;
import com.projectgoth.ui.listener.ContextMenuItemListener;
import com.projectgoth.ui.widget.GifImageView;

public class ChatListFragment extends BaseListFragment implements BaseViewListener<ChatConversation>,
        ContextMenuItemListener, OnClickListener {

    private ChatListAdapter                                   mChatListAdapter;
    private RelativeLayout                                    container;
    private ImageView                                         chatOption;
    private View                                              emptyView;
    private View                                              footerView;

    private ConcurrentHashMap<String, SpannableStringBuilder> spannableCache = new ConcurrentHashMap<String, SpannableStringBuilder>();

    private BaseViewListener externalListener;
    private boolean isForSelectChat;

    public static final String     PARAM_IS_SELECT_CHAT                = "PARAM_IS_SELECT_CHAT";
    
    public ChatListFragment() {
        super();
        setShouldUpdateActionBarOnAttach(false);
    }

    @Override
    protected void readBundleArguments(Bundle args) {
        super.readBundleArguments(args);

        isForSelectChat = args.getBoolean(PARAM_IS_SELECT_CHAT, false);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_chat_list;
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        container = (RelativeLayout) view.findViewById(R.id.container);

        setPullToRefreshEnabled(false);
        
        invokeOnViewCreated();
    }

    @Override
    public void updateTitle()
    {
        if(!getCurrentTitle().matches(getTitle())) {
            super.updateTitle();
            showTitleAnimation();
        }
    }

    @Override
    public  void updateIcon() {
        if(getCurrentTitleIconTag() == 0 || getCurrentTitleIconTag() != getTitleIcon()) {
            super.updateIcon();
            showTitleIconAnimation();
        }
    }

    @Override
    protected void onShowFragment() {
        super.onShowFragment();

        notifyDataSetChanged(true);
    }

    @Override
    public void onItemClick(View v, ChatConversation conv) {
        if (Tools.hideVirtualKeyboard(getActivity())) {
            //If software keyboard showing just hide it and do nothing
            return;
        }
        if (conv != null) {
            if(isFiltering()) GAEvent.Chat_ClickChatFilterResult.send();
            if (v.getId() == R.id.icon) {
                if (conv.isMigPrivateChat()) {
                    ActionHandler.getInstance().displayProfile(getActivity(), conv.getDisplayName());
                }
            } else {
                ActionHandler.getInstance().displayChatConversation(getActivity(), conv.getId());
            }
        }
    }

    @Override
    public void onItemLongClick(View v, ChatConversation conv) {
        ArrayList<ContextMenuItem> menuItems = generateMenuItems(conv);

        String title = null;
        title = conv.getDisplayName();
        
        Tools.showContextMenu(title, menuItems, this);
    }

    private ArrayList<ContextMenuItem> generateMenuItems(ChatConversation conv) {
        ArrayList<ContextMenuItem> menuItems = new ArrayList<ContextMenuItem>();

        if (conv.isPinned()) {
            menuItems.add(new ContextMenuItem(I18n.tr("Unpin"), R.id.option_item_unpin, conv));
        } else {
            menuItems.add(new ContextMenuItem(I18n.tr("Pin"), R.id.option_item_pin, conv));
        }

        menuItems.add(new ContextMenuItem(I18n.tr("Close"), R.id.option_item_close, conv));

        if (conv.isMuted()) {
            menuItems.add(new ContextMenuItem(I18n.tr("Unmute"), R.id.option_item_unmute, conv));
        } else {
            menuItems.add(new ContextMenuItem(I18n.tr("Mute"), R.id.option_item_mute, conv));
        }

        return menuItems;
    }

    @Override
    public void onContextMenuItemClick(ContextMenuItem menuItem) {
        int id = menuItem.getId();
        ChatConversation conv = (ChatConversation) menuItem.getData();
        switch (id) {
            case R.id.option_item_pin:
                ChatController.getInstance().pin(conv);
                notifyDataSetChanged(true);
                break;
            case R.id.option_item_unpin:
                ChatController.getInstance().unpin(conv);
                notifyDataSetChanged(true);
                break;
            case R.id.option_item_close:
                // NOTE: null listener will use the default one in the
                // AlertHandler
                ActionHandler.getInstance().leaveChatConversation(getActivity(), null, conv.getId());
                notifyDataSetChanged(true);                
                break;
            case R.id.option_item_mute:
                ChatController.getInstance().setMute(true, conv, this);
                break;
            case R.id.option_item_unmute:
                ChatController.getInstance().setMute(false, conv, this);
                break;
            default:
                break;
        }
    }

    public void setExternalListener(BaseViewListener externalListener) {
        this.externalListener = externalListener;
    }

    @Override
    protected BaseAdapter createAdapter() {
        mChatListAdapter = new ChatListAdapter(spannableCache);
        mChatListAdapter.setChatItemListener(externalListener == null ? this : externalListener);
        mChatListAdapter.setForSelectChat(isForSelectChat);
        return mChatListAdapter;
    }

    @Override
    public void onPause() {
        super.onPause();
        Tools.hideVirtualKeyboard(getActivity());
    }

    @Override
    protected void updateListData() {
        ArrayList<ChatConversation> chatList = ChatController.getInstance().getSortedChatList();
        mChatListAdapter.setChatList(chatList);
        mChatListAdapter.notifyDataSetChanged();
    }

    /**
     * some cases like updating a presence and display picture of a contact do
     * not need reload data while to add or remove a conversation requires to
     * reload data
     * 
     * @param reloadData
     */
    public void notifyDataSetChanged(boolean reloadData) {
        if (reloadData) {
            updateListData();
            // when the list is filtered, it could also be empty, we don't what
            // to show the empty view then, so we do it this way here
            showOrHideEmptyViewIfNeeded();
        } else {
            mChatListAdapter.notifyDataSetChanged();
        }
    }

    protected void showOrHideEmptyViewIfNeeded() {
        if (isHomeListEmpty() && mode != Mode.FILTERING) {
            // show emptyView
            if (emptyView == null) {
                emptyView = createEmptyView();
            }
            // prevent it from being added multiple times
            if (emptyView.getParent() != null) {
                ((ViewGroup) emptyView.getParent()).removeView(emptyView);
            }

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT);
            ((ViewGroup) container).addView(emptyView, params);
            emptyView.setVisibility(View.VISIBLE);

        } else {
            // hide emptyView
            if (emptyView != null && emptyView.getParent() != null) {
                ((ViewGroup) emptyView.getParent()).removeView(emptyView);
            }
        }
    }

    private boolean isHomeListEmpty() {
        if (mChatListAdapter.getCount() == 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void registerReceivers() {
        registerEvent(Events.ChatMessage.RECEIVED);
        registerEvent(Events.Contact.PRESENCE_CHANGED);
        registerEvent(Events.Contact.DISPLAY_PICTURE_CHANGED);
        registerEvent(Events.ChatConversation.RECEIVED);
        registerEvent(Events.ChatConversation.PrivateChat.LEFT);
        registerEvent(Events.ChatConversation.GroupChat.LEFT);
        registerEvent(Events.ChatConversation.ChatRoom.LEFT);
        registerEvent(Events.ChatConversation.PrivateChat.LEAVE_ERROR);
        registerEvent(Events.ChatConversation.GroupChat.LEAVE_ERROR);
        registerEvent(Events.ChatConversation.ChatRoom.LEAVE_ERROR);
        registerEvent(Events.Group.LEFT);
        registerEvent(Events.Emoticon.RECEIVED);
        registerEvent(Events.Emoticon.BITMAP_FETCHED);
        registerEvent(Events.ChatParticipant.FETCH_ALL_COMPLETED);
        registerEvent(Events.ChatParticipant.JOINED);
        registerEvent(Events.ChatParticipant.LEFT);
        registerEvent(Events.Contact.IM_STATUS_CHANGED);
        registerEvent(Events.Contact.FETCH_IM_ICONS_COMPLETED);
        registerEvent(Events.ChatRoom.RECEIVED);
        registerEvent(Events.ChatRoom.FAVOURITED);
        registerEvent(Events.ChatConversation.MUTED_RECEIVED);
    }

    @Override
    protected void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        if (action.equals(Events.ChatMessage.RECEIVED) || action.equals(Events.ChatConversation.RECEIVED)
                || action.equals(Events.ChatConversation.PrivateChat.LEFT)
                || action.equals(Events.ChatConversation.GroupChat.LEFT)
                || action.equals(Events.ChatConversation.ChatRoom.LEFT) || action.equals(Events.Group.LEFT)) {
            notifyDataSetChanged(true);
        } else if (action.equals(Events.Contact.PRESENCE_CHANGED)
                || action.equals(Events.Contact.DISPLAY_PICTURE_CHANGED) || action.equals(Events.Profile.RECEIVED)
                || action.equals(Events.Emoticon.RECEIVED) || action.equals(Events.Emoticon.BITMAP_FETCHED)
                || action.equals(Events.ChatParticipant.FETCH_ALL_COMPLETED)
                || action.equals(Events.Contact.IM_STATUS_CHANGED)
                || action.equals(Events.Contact.FETCH_IM_ICONS_COMPLETED)
                || action.equals(Events.ChatRoom.RECEIVED)
                || action.equals(Events.ChatRoom.FAVOURITED)
                || action.equals(Events.ChatParticipant.JOINED)
                || action.equals(Events.ChatParticipant.LEFT) || action.equals(Events.ChatConversation.MUTED_RECEIVED)) {
            notifyDataSetChanged(false);
        } else if (action.equals(Events.ChatConversation.PrivateChat.LEAVE_ERROR)
                || action.equals(Events.ChatConversation.GroupChat.LEAVE_ERROR)
                || action.equals(Events.ChatConversation.ChatRoom.LEAVE_ERROR)) {
            Tools.showToastForIntent(context, intent);
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        super.onScrollStateChanged(view, scrollState);
    }

    @Override
    public void handleNotificationAvailable(NotificationType type, String notificationId) {
        if (type == NotificationType.CHAT_NOTIFICATION) {
            // all chat notifications should be ignored and removed
            updateNotifications();
        } else {
            super.handleNotificationAvailable(type, notificationId);
        }
    }

    @Override
    protected void updateNotifications() {
        ApplicationEx.getInstance().getNotificationHandler()
                .removeAllNotifications(NotificationType.CHAT_NOTIFICATION, false);
    }

    private View createEmptyView() {
        View emptyView = LayoutInflater.from(getActivity()).inflate(R.layout.empty_view_home_list, mList, false);

        GifImageView gifView = (GifImageView) emptyView.findViewById(R.id.empty_chat_list_icon);
        gifView.setOnClickListener(this);
        gifView.setCyclePlay(false);
        gifView.setGifId(R.drawable.bubble_burst);
        gifView.startAnimation();
        
        TextView question = (TextView) emptyView.findViewById(R.id.empty_chat_list_question);

        String str = I18n.tr("New on chat?");
        question.setText(str);
        
        TextView hint = (TextView) emptyView.findViewById(R.id.empty_chat_list_hint);
        str = I18n.tr("Welcome!");
        hint.setText(str);
        
        TextView startChatlink = (TextView) emptyView.findViewById(R.id.start_chat_link);
        startChatlink.setMovementMethod(LinkMovementMethod.getInstance());
        str = I18n.tr("Buzz a friend now.");
        startChatlink.setText(str);
        
        startChatlink.setOnClickListener(this);

        return emptyView;
    }
    
    public int getCount(){
        if(mChatListAdapter == null) return 0;
        return mChatListAdapter.getCount();
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        switch (viewId) {
            case R.id.empty_chat_list_icon:
            {
                GifImageView gifView = (GifImageView) view;
                
                if (!gifView.isAnimating() ) {
                    gifView.startAnimation();
                }
            }
                break;
            case R.id.start_chat_link:
                // for [non-login] users
                if (Session.getInstance().isBlockUsers()){
                    ActionHandler.getInstance().displayLoginDialogFragment(getActivity());
                    return;
                }
                ActionHandler.getInstance().displayStartChat(getActivity(), StartChatActionType.START_NEW_CHAT, null, null);
                break;
                
            default:
                break;
        }
    }
    
    @Override
    public void performFilter(final String filterString) {
        super.performFilter(filterString);
        if (isFiltering()) {
            boolean hasFilter = !TextUtils.isEmpty(filterString);
            int newVisibility =  hasFilter? View.VISIBLE : View.GONE;
            if (newVisibility != footerView.getVisibility()) {
                footerView.setVisibility(newVisibility);
            }
            if (hasFilter) {
                // Update the footer
                String newLabel = String.format(I18n.tr("Find people for %s"), filterString);
                ((BasicListFooterViewHolder) footerView.getTag(R.id.holder_footer)).setLabel(newLabel);
            }
        }
        mChatListAdapter.setFilter(filterString);
    }
    
    @Override
    protected void onModeChanged(Mode newMode) {
        super.onModeChanged(newMode);
        
        if (newMode == Mode.FILTERING && emptyView != null) {
            emptyView.setVisibility(View.GONE);
        } else {
            showOrHideEmptyViewIfNeeded();
        }
    }

    @Override
    protected int getTitleIcon() {
        return R.drawable.ad_chat_white;
    }

    @Override
    protected String getTitle() {
        return I18n.tr("Chats");
    }

    @Override
    public CustomActionBarConfig getActionBarConfig() {
        CustomActionBarConfig config = super.getActionBarConfig();
        config.setShowSearchButton(false);
        config.setShowOverflowButtonState(OverflowButtonState.ALERT);
        return config;
    }

    @Override
    protected View createFooterView() {
        LayoutInflater inflater = LayoutInflater.from(getActivity());   
        footerView = inflater.inflate(R.layout.holder_list_footer, mList, false);

        BasicListFooterViewHolder footerViewHolder = new BasicListFooterViewHolder(footerView);
        footerView.setTag(R.id.holder_footer, footerViewHolder);
        
        footerViewHolder.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Tools.hideVirtualKeyboard(getActivity())) {
                    //If software keyboard showing just hide it and do nothing
                    return;
                }
                ActionHandler.getInstance().displayGlobalSearch(getActivity(), SearchType.PEOPLE, mChatListAdapter.getFilter());
            }
        });
        footerView.setVisibility(View.GONE);
        
        return footerView;
    }

    @Override
    protected void performGlobalSearch(final String searchString) {
        super.performGlobalSearch(searchString);
        ActionHandler.getInstance().displayGlobalSearch(getActivity(), SearchType.PEOPLE, searchString);
    }
    
    private String                         filterKeyword                     = Constants.BLANKSTR;
    private boolean                        isListFiltered                    = false;
    
    public boolean isListFiltered() {
        return isListFiltered;
    }

    public void setListFiltered(boolean isListFiltered) {
        this.isListFiltered = isListFiltered;
    }

    public String getFilterKeyword() {
        return filterKeyword;
    }

    public void setFilterKeyword(String filterKeyword) {
        this.filterKeyword = filterKeyword;
        mChatListAdapter.setFilter(filterKeyword);
    }
    
    public void filter(String keyword) {
        if (!TextUtils.isEmpty(keyword)) {
            filterKeyword = keyword;
            
            setListFiltered(true);
            setFilterKeyword(keyword);

        } else {
            setListFiltered(false);
            setFilterKeyword(Constants.BLANKSTR);

        }
    }
}