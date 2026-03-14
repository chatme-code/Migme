/**
 * Copyright (c) 2013 Project Goth
 *
 * ChatFragment.java
 * Created Jun 13, 2013, 3:59:28 PM
 */

package com.projectgoth.ui.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.ClipboardManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.mig33.diggle.events.Events;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.Profile;
import com.projectgoth.b.data.mime.DeezerMimeData;
import com.projectgoth.b.data.mime.MimeData;
import com.projectgoth.b.data.mime.StickerMimeData;
import com.projectgoth.b.data.mime.TextPlainMimeData;
import com.projectgoth.blackhole.enums.ImType;
import com.projectgoth.blackhole.enums.MessageType;
import com.projectgoth.blackhole.enums.PresenceType;
import com.projectgoth.common.Config;
import com.projectgoth.common.Constants;
import com.projectgoth.common.Logger;
import com.projectgoth.common.NUEManager;
import com.projectgoth.common.ShareManager;
import com.projectgoth.common.SharedPrefsManager;
import com.projectgoth.common.Theme;
import com.projectgoth.common.ThemeValues;
import com.projectgoth.common.Tools;
import com.projectgoth.common.WebURL;
import com.projectgoth.controller.ChatController;
import com.projectgoth.controller.ChatroomColorController;
import com.projectgoth.datastore.ChatDatastore;
import com.projectgoth.datastore.EmoticonDatastore;
import com.projectgoth.datastore.Session;
import com.projectgoth.datastore.UserDatastore;
import com.projectgoth.enums.UsedChatItemType;
import com.projectgoth.events.GAEvent;
import com.projectgoth.i18n.I18n;
import com.projectgoth.imagefetcher.ImageHandler;
import com.projectgoth.listener.ChatSyncOnLoadPreMsgClickListener;
import com.projectgoth.listener.MimeContentViewListener;
import com.projectgoth.model.ChatConversation;
import com.projectgoth.model.ContextMenuItem;
import com.projectgoth.model.MenuOption;
import com.projectgoth.model.Message;
import com.projectgoth.model.PinMessageData;
import com.projectgoth.model.UsedChatItem;
import com.projectgoth.music.deezer.DeezerPlaybackChat;
import com.projectgoth.music.deezer.DeezerPlayerManager;
import com.projectgoth.nemesis.enums.ChatTypeEnum;
import com.projectgoth.nemesis.model.ChatParticipant;
import com.projectgoth.nemesis.model.ChatRoomCategory;
import com.projectgoth.nemesis.model.Friend;
import com.projectgoth.nemesis.model.Sticker;
import com.projectgoth.notification.AlertListener;
import com.projectgoth.notification.NotificationType;
import com.projectgoth.ui.activity.ActionHandler;
import com.projectgoth.ui.activity.AlertHandler;
import com.projectgoth.ui.activity.AlertHandler.TextInputListener;
import com.projectgoth.ui.activity.CustomActionBarConfig;
import com.projectgoth.ui.activity.CustomActionBarConfig.NavigationButtonState;
import com.projectgoth.ui.activity.CustomActionBarConfig.OverflowButtonState;
import com.projectgoth.ui.activity.CustomPopupActivity;
import com.projectgoth.ui.activity.FragmentHandler;
import com.projectgoth.ui.adapter.MessageListAdapter;
import com.projectgoth.ui.fragment.ShareboxFragment.ShareboxActionType;
import com.projectgoth.ui.fragment.StartChatFragment.StartChatActionType;
import com.projectgoth.ui.holder.BaseViewHolder.BaseViewListener;
import com.projectgoth.ui.holder.PinnedMessageViewHolder;
import com.projectgoth.ui.listener.ContextMenuItemListener;
import com.projectgoth.ui.widget.EditTextEx;
import com.projectgoth.ui.widget.HeightAdjustableFrameLayout;
import com.projectgoth.ui.widget.LinearLayoutEx;
import com.projectgoth.ui.widget.PopupListFilter;
import com.projectgoth.ui.widget.MessageInputPanel;
import com.projectgoth.ui.widget.ObservableListView;
import com.projectgoth.ui.widget.PopupMenu.OnPopupMenuListener;
import com.projectgoth.ui.widget.SlidingPanelContainer;
import com.projectgoth.ui.widget.UserBasicDetails;
import com.projectgoth.ui.widget.tooltip.ToolTip;
import com.projectgoth.ui.widget.tooltip.ToolTipEnum;
import com.projectgoth.ui.widget.tooltip.ToolTipRelativeLayout;
import com.projectgoth.ui.widget.tooltip.ToolTipView;
import com.projectgoth.util.AndroidLogger;
import com.projectgoth.util.ArrayUtils;
import com.projectgoth.util.CrashlyticsLog;
import com.projectgoth.util.LogUtils;
import com.projectgoth.util.NetworkUtils;
import com.wunderlist.slidinglayer.SlidingLayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author mapet
 */

@SuppressWarnings("deprecation")
public class ChatFragment extends BaseFragment implements OnClickListener, OnEditorActionListener, BaseViewListener<Message>,
        TextWatcher, ContextMenuItemListener, OnScrollListener,
        ObservableListView.ListViewObserver, ToolTipView.OnToolTipViewClickedListener,
        SlidingPanelContainer.SlidingPanelContainerListener, MessageInputPanel.MessageInputPanelListener,
        MimeContentViewListener, SlidingLayer.OnInteractListener, PopupListFilter.PopupListFilterListener {

    //TODO: add mSliddingLayer
    private SlidingLayer                mSlidingLayer;
    private static final String         COVER_URL_FORMAT                = "http://api.deezer.com/radio/%d/image";
    public static int                   currentDeezerPlayId             = 0;

    // A flag that determines whether the chat messages for this fragment have
    // been loaded or not.
    private boolean                     mDidLoadChatMessages            = false;
    private SlidingPanelContainer       mPanelContainer;
    private RelativeLayout              mCenterPanel;
    private LinearLayoutEx              mMainContainer;
    private ObservableListView          mMessagesList;
    private MessageInputPanel           mMsgInputPanel;
    private EditTextEx                  mChatField;
    private HeightAdjustableFrameLayout mChatInputContainer;
    private LinearLayout                mPinContainer;
    private @NonNull
    MessageListAdapter                  mMessageListAdapter;

    private String                      mConversationId                   = null;
    private String                      mConversationChatId               = null;
    private ChatTypeEnum                mChatType                         = ChatTypeEnum.UNKNOWN;
    private MessageType                 mImMessageType                    = MessageType.UNKNOWN;
    private ChatConversation            mConversation;
    private ChatRoomCategory            mChatRoomCategory;

    private UserBasicDetails            mHeaderUserInfo;
    private View                        mHeaderChatInfo;
    private View                        mChatIcon;
    private ImageView                   mPinMessageHiddenIndicator;
    private TextView                    mUserCounter;
    private TextView                    mChatName;
    private DeezerPlaybackChat          mDeezerPlaybackChat;
    private PopupListFilter             mPopupListFilter;

    private ToolTipRelativeLayout       mToolTipFrameLayout;
    private ToolTipView                 mToolTipView;
    private View                        mGiftIcon;

    private PinnedMessageViewHolder     mPinnedMessageViewHolder;
    private boolean                     mIsChatRoomAdmin;
    private boolean                     mIsFirstReceivedPinnedState       = true;

    private SharedPrefsManager          mSharedPrefsManager;

    // To identify whether this chat fragment was opened by clicking system notifications
    private boolean                     mIsFromSystemNotification         = false;

    // This is only created/set if it's a group chat/chat room.
    private ParticipantListFragment     participantListFragment           = null;

    // This footer view is added to the bottom of the message view to represent the missing
    // height of the collapsible input box. Without it, the list view cannot be told to scroll
    // below the bottom of the messages, and we can't pull up the hidden input box.
    private View                        mFooterView;

    // The list view does not have a "getScrollYOffset" equivalent method, so we use this
    // counter to aggregate all delta-y values that have occurred.
    private int                         chatContainerHeightAdjust         = 0;

    // If we scroll up a lot, we don't necessarily want to have to scroll all the way back
    // down to the bottom to make the input show again. This is a cap of this value.
    private static final int            MAX_SCROLL_TO_MAKE_INPUT_APPEAR   = 300;

    // This is a guard band required because resizing the footer view can cause the
    // determined scroll Y offset to change, which is then interpreted as requiring
    // a change to the input box. Ugh.
    private static final int            INPUT_SCROLL_DEAD_ZONE            = 100;

    private final static int            REFRESH_LIST_DELAY                = 500;

    private static final String         LOG_TAG                           = AndroidLogger.makeLogTag(ChatFragment.class);

    public static final String          PARAM_CONVERSATION_ID             = "PARAM_CONVERSATION_ID";
    public static final String          PARAM_CONVERSATION_CHATID         = "PARAM_CONVERSATION_CHATID";
    public static final String          PARAM_CHAT_TYPE                   = "PARAM_CHAT_TYPE";
    public static final String          PARAM_IM_MESSAGE_TYPE             = "PARAM_IM_MESSAGE_TYPE";
    public static final String          PARAM_CHATROOM                    = "PARAM_CHATROOM";
    public static final String          PARAM_IS_FROM_SYSTEM_NOTIFICATION = "PARAM_IS_FROM_SYSTEM_NOTIFICATION";

    /* (non-Javadoc)
     * @see android.support.v4.app.Fragment#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            loadDataFromBundle(savedInstanceState);
        }
    }

    private void loadDataFromBundle(Bundle args) {
        mConversationId = args.getString(PARAM_CONVERSATION_ID);
        mConversationChatId = args.getString(PARAM_CONVERSATION_CHATID);
        mIsFromSystemNotification = args.getBoolean(PARAM_IS_FROM_SYSTEM_NOTIFICATION);
        mChatType = ChatTypeEnum.fromValue(args.getByte(PARAM_CHAT_TYPE, ChatTypeEnum.UNKNOWN.getValue()));
        mImMessageType = MessageType.fromValue(args.getByte(PARAM_IM_MESSAGE_TYPE, MessageType.UNKNOWN.getValue()));
    }

    @Override
    protected void readBundleArguments(Bundle args) {
        super.readBundleArguments(args);

        // We do this check here because the conversationId might have been read from onCreate savedInstanceState.
        if (TextUtils.isEmpty(mConversationId) && TextUtils.isEmpty(mConversationChatId)) {
            loadDataFromBundle(args);
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(PARAM_CONVERSATION_ID, mConversationId);
        outState.putString(PARAM_CONVERSATION_CHATID, mConversationChatId);
        outState.putByte(PARAM_CHAT_TYPE, mChatType.getValue());
        outState.putByte(PARAM_IM_MESSAGE_TYPE, mImMessageType.getValue());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected final int getLayoutId() {
        return R.layout.fragment_chat;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (!loadConversation()) {
            getActivity().finish();
            return;
        }

        mSharedPrefsManager = ApplicationEx.getInstance().getSharedPrefsManager();
        mDeezerPlaybackChat = (DeezerPlaybackChat) view.findViewById(R.id.deezer_playback_chat);
        mPinContainer = (LinearLayout) view.findViewById(R.id.pin_content_container);
        mPanelContainer = (SlidingPanelContainer) view.findViewById(R.id.background_frame);
        mCenterPanel = (RelativeLayout) view.findViewById(R.id.center_panel);
        mMainContainer = (LinearLayoutEx) view.findViewById(R.id.chat_fragment);
        mMessagesList = (ObservableListView) view.findViewById(R.id.chat_list);
        mMsgInputPanel = (MessageInputPanel) getFragmentManager().findFragmentById(R.id.chat_input_panel);
        //In some platforms, need to use child fragment manager to get the fragment
        if (mMsgInputPanel == null) {
            mMsgInputPanel = (MessageInputPanel) getChildFragmentManager().findFragmentById(R.id.chat_input_panel);
        }
        mMsgInputPanel.setListener(this);
        mChatField = mMsgInputPanel.getEditText();
        mChatField.addTextChangedListener(this);
        mChatField.setOnEditorActionListener(this);
        mChatInputContainer = mMsgInputPanel.getChatInputContainer();
        final String initialRecipient = mConversation.isMigPrivateChat() ? mConversation.getDisplayName() : Constants.BLANKSTR;
        mMsgInputPanel.setInitialRecipient(initialRecipient);
        mMsgInputPanel.setSendButtonState();
        mMainContainer.setKeyboardListener(mMsgInputPanel);

        // Create message list adapter
        mMessageListAdapter = new MessageListAdapter(getActivity());
        mMessageListAdapter.setMessageItemClickListener(this);
        mMessageListAdapter.setMimeItemLongClickListener(this);
        mMessageListAdapter.setConversationId(mConversationId);
        mMessagesList.setRecyclerListener(mMessageListAdapter);

        // Add header
        mMessagesList.addHeaderView(createLoadingView(false));

        // Add footer
        mFooterView = new View(ApplicationEx.getContext());
        setFooterViewHeight(0);
        mMessagesList.addFooterView(mFooterView);

        mMessageListAdapter.setIsMigPrivateChat(mConversation.isMigPrivateChat());
        Message latestViewedMsgLastTime = mConversation.getMessage(mConversation.getLatestViewedMsgId());
        if (latestViewedMsgLastTime != null) {
            mMessageListAdapter.setLatestViewedMessageLastTime(latestViewedMsgLastTime.getMessageId());
        }

        mMessagesList.setAdapter(mMessageListAdapter);
        mMessagesList.setOnScrollListener(this);
        mMessagesList.setListViewObserver(this);

        if (mConversation.isGroupChat() || mConversation.isChatroom()) {
            participantListFragment = FragmentHandler.getInstance().getParticipantListFragment(mConversationId, mConversation.isGroupChat(), mConversation.isChatroom());
            addChildFragment(R.id.right_panel, participantListFragment);
        } else if (!mConversation.isIMChat()) {
            MiniProfileChatFragment miniProfileFragment = FragmentHandler.getInstance().getMiniProfileChatFragment(mConversation.getChatId());
            addChildFragment(R.id.right_panel, miniProfileFragment);
        }
        mPanelContainer.setSwipeView(mMessagesList);
        mPanelContainer.setListener(this);

        ChatListFragment chatListFragment =
                FragmentHandler.getInstance().getChatListFragment(false, false);
        addChildFragment(R.id.left_panel, chatListFragment);



        mToolTipFrameLayout = (ToolTipRelativeLayout) view.findViewById(R.id.activity_main_tooltipframelayout);

        mGiftIcon = mMsgInputPanel.getGiftIcon();

        if (NUEManager.getInstance().shouldShowNUE(LOG_TAG)) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (getActivity() != null) {
                        addFirstToolTipView();
                    }
                }
            }, Constants.NUE_TOOLTIP_DELAY);
        }

        mPinnedMessageViewHolder = new PinnedMessageViewHolder(getActivity(), view, mMessageListAdapter.getSpannableCache());
        mSlidingLayer = (SlidingLayer) view.findViewById(R.id.slidingLayer1);
        mSlidingLayer.setOnInteractListener(this);
        setSlidingLayerConfig();

        mPinMessageHiddenIndicator = (ImageView) view.findViewById(R.id.hidden_pin_tab);
        mPinMessageHiddenIndicator.setOnClickListener(this);

        mIsChatRoomAdmin = mConversation.isChatRoomAdmin();

        setupOnBackPressListener();

        mSlidingLayer.setDrawingCacheEnabled(false);
    }

    private void setSlidingLayerConfig() {
        LayoutParams rlp = mSlidingLayer.getLayoutParams();
        mSlidingLayer.setStickTo(SlidingLayer.STICK_TO_TOP);
        mSlidingLayer.setLayoutParams(rlp);
        mSlidingLayer.setShadowSize(0);
        mSlidingLayer.setShadowDrawable(null);
        mSlidingLayer.setOffsetDistance(0);
        int deezerPlaybackChatHiddenPinTabHeight = ApplicationEx.getDimension(R.dimen.chat_pin_hidden_tab_area_height);
        mSlidingLayer.setPreviewOffsetDistance(deezerPlaybackChatHiddenPinTabHeight);
    }


    private void addSecondToolTipView() {
        View customView = LayoutInflater.from(this.getActivity()).inflate(R.layout.tooltip_subtitle_title, null);
        ((TextView) customView.findViewById(R.id.title)).setText(I18n.tr("Send a gift."));
        ((TextView) customView.findViewById(R.id.subtitle)).setText(I18n.tr("Make chat more fun."));
        ToolTipView toolTipView = mToolTipFrameLayout.showToolTipForView(
                new ToolTip()
                        .withContentView(customView)
                        .withColor(ApplicationEx.getColor(R.color.edit_text_input_color))
                        .withAnimationType(ToolTip.ANIMATIONTYPE_FROMMASTERVIEW)
                        .withShadow(false),
                mGiftIcon);
        toolTipView.setOnToolTipViewClickedListener(null);
    }


    @Override
    protected void onShowFragment() {
        super.onShowFragment();

        initConversation();

        refreshMessageList();
        showLastMessage();

        //When process is not killed, RECEIVED_PINNED_STATE is not triggered while app is in background,
        //but pinned message might have been changed. Do check here.
        if (!isSameAsLastShownPinnedMessage() && mConversation.getPinnedMessage() != null) {
            mSharedPrefsManager.setShouldShowPinnedMessage(mConversationId, true);
        }
        showOrHidePinnedMessage();
    }

    @Override
    protected void onHideFragment() {
        super.onHideFragment();

        ChatController.getInstance().setActiveConversationId(Constants.BLANKSTR);
        Tools.hideVirtualKeyboard(getActivity());
    }

    private void initConversation() {
        ChatController.getInstance().setActiveConversationId(mConversationId);
        updateActionBar(getActivity());
        mConversation.resetUnreadMessageCounter();

        if (mConversation.isChatroom() && !mConversation.isJoined()) {
            ChatController.getInstance().requestJoinChatRoom(mConversation.getChatId(), true);
        }

        ChatController.getInstance().sendGetMessagesOnChatShown(mConversationId);
    }

    private void refreshMessageList() {
        Logger.debug.log(LOG_TAG, "refreshMessageList: ", mConversationId);
        mMessageListAdapter.setMessagesList(mConversation.cloneMessageList());
    }

    private boolean isRefreshTaskScheduled = false;

    private void refreshMessageListDelay() {
        if (!isRefreshTaskScheduled) {
            isRefreshTaskScheduled = true;
            new Timer().schedule(new RefreshListTask(), REFRESH_LIST_DELAY);
        }
    }

    //tells handler to send a message
    class RefreshListTask extends TimerTask {

        @Override
        public void run() {
            if (getActivity() != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        isRefreshTaskScheduled = false;
                        refreshMessageList();
                    }
                });
            }
        }
    }


    private void showMentionList() {
        if (mPopupListFilter != null) {
            //remove the old one
            mCenterPanel.removeView(mPopupListFilter);
        }
        //create it
        mPopupListFilter = new PopupListFilter(getActivity());
        mPopupListFilter.setListener(this);
        //add it
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        mCenterPanel.addView(mPopupListFilter, params);
        mPopupListFilter.requestInputFocus();
        refreshMentionList();
    }

    private void hideMentionList() {
        mCenterPanel.removeView(mPopupListFilter);
        mPopupListFilter = null;
        showKeyboard();
    }

    @Override
    public void onPopupListItemSelected(String selectedItemText, PopupListFilter.ListType listType) {
        if (!TextUtils.isEmpty(selectedItemText)) {
            mChatField.append(selectedItemText + Constants.SPACESTR);
        }
        hideMentionList();
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();

        switch (viewId) {
            case R.id.user_details:
                handleUserDetailClicked();
                break;
            case R.id.chat_details:
            case R.id.chatroom_icon:
            case R.id.group_chat_icon:
                Logger.debug.log(LogUtils.TAG_MAIN_UI, "Participant list icon has been clicked!");
                showChatParticipants();
                break;
            case R.id.hidden_pin_tab:
                mSharedPrefsManager.setShouldShowPinnedMessage(mConversationId, !mSharedPrefsManager.getShouldShowPinnedMessage(mConversationId));
                showOrHidePinnedMessage();
                break;
        }
    }

    @Override
    protected void registerReceivers() {
        registerEvent(Events.ChatConversation.RECEIVED);
        registerEvent(Events.ChatMessage.RECEIVED);
        registerEvent(Events.ChatConversation.ChatRoom.DISCONNECTED);
        registerEvent(Events.Profile.RECEIVED);
        registerEvent(Events.ChatParticipant.FETCH_ALL_COMPLETED);
        registerEvent(Events.ChatConversation.NAME_CHANGED);
        registerEvent(Events.Emoticon.RECEIVED);
        registerEvent(Events.Emoticon.BITMAP_FETCHED);
        registerEvent(Events.ChatParticipant.JOINED);
        registerEvent(Events.ChatParticipant.LEFT);
        registerEvent(Events.ChatParticipant.ChatRoom.KICK_ERROR);
        registerEvent(Events.ChatConversation.GroupChat.CREATE_ERROR);
        registerEvent(Events.Contact.PRESENCE_CHANGED);
        registerEvent(Events.ChatMessage.FETCH_COMPLETED);
        registerEvent(Events.ChatMessage.MESSAGES_LOADED);
        registerEvent(Events.ChatMessage.BEGIN_FETCH);
        registerEvent(Events.Contact.FETCH_IM_ICONS_COMPLETED);
        registerEvent(Events.ChatConversation.SEND_GIFT_OK);
        registerEvent(Events.ChatConversation.SEND_GIFT_ERROR);
        registerEvent(Events.ChatMessage.SENT);
        registerEvent(Events.ChatMessage.SEND_ERROR);
        registerEvent(Events.ChatMessage.RECEIVED_BY_RECIPIENT);
        registerEvent(Events.ChatMessage.SENDING);
        registerEvent(Events.ChatRoom.FAVOURITED);
        registerEvent(Events.ChatRoom.UNFAVOURITED);
        registerEvent(Events.ChatRoom.FAVOURITE_ERROR);
        registerEvent(Events.ChatRoom.UNFAVOURITE_ERROR);
        registerEvent(Events.ChatMessage.RECEIVED_PINNED_STATE);
        registerEvent(Events.ChatMessage.NO_PINNED_MESSAGE);
        registerEvent(Events.Login.SUCCESS);
        //for all mime data related events
        registerDataFetchedByMimeDataEvents();
    }

    @Override
    protected void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Logger.debug.logWithTrace(LOG_TAG, getClass(), mConversationId);

        if (action.equals(Events.ChatMessage.SENT) || action.equals(Events.ChatMessage.SEND_ERROR)
                || action.equals(Events.ChatMessage.RECEIVED_BY_RECIPIENT) || action.equals(Events.ChatMessage.SENDING)) {
            Bundle data = intent.getExtras();
            String messageId = data.getString(Events.ChatMessage.Extra.ID);
            if (mConversation.getMessage(messageId) != null) {
                refreshMessageList();
            }
        } else if (action.equals(Events.ChatMessage.RECEIVED)) {
            String convId = intent.getStringExtra(Events.ChatConversation.Extra.ID);
            Logger.debug.flog(LOG_TAG, "NEW_CHAT_MESSAGE_RECEIVED for %s - current: %s", convId, mConversationId);
            if (convId != null && convId.equals(mConversationId)) {
                refreshMessageList();
            }
        } else if (isMyProfileReceived(intent)) {
            if (mConversation.isPrivateChat()) {
                updateUserDetailsHeader();
            }
            // it could be in profile mime data
            refreshMessageListDelay();
        } else if (action.equals(Events.Contact.PRESENCE_CHANGED)) {
            Bundle data = intent.getExtras();
            String username = data.getString(Events.Contact.Extra.USERNAME);
            if (mConversation.isPrivateChat()
                    && username.equalsIgnoreCase(mConversation.getChatId())) {
                updateUserDetailsHeader();
            }
        } else if (action.equals(Events.ChatConversation.NAME_CHANGED)
                || action.equals(Events.ChatConversation.RECEIVED)) {
            updateChatDetailsHeader();
        } else if (action.equals(Events.Emoticon.RECEIVED) || action.equals(Events.Emoticon.BITMAP_FETCHED)) {
            refreshMessageList();
        } else if (action.equals(Events.ChatParticipant.FETCH_ALL_COMPLETED)
                || action.equals(Events.ChatConversation.ChatRoom.DISCONNECTED)
                || action.equals(Events.ChatParticipant.JOINED) || action.equals(Events.ChatParticipant.LEFT)) {
            updateChatDetailsHeader();
            updateGroupChatIcon();
            refreshMentionList();

            if (participantListFragment != null) {
                participantListFragment.updateParticipantsData(true);
            }

            mIsChatRoomAdmin = mConversation.isChatRoomAdmin();
        } else if (action.equals(Events.ChatParticipant.ChatRoom.KICK_ERROR)) {
            refreshMessageList();
        } else if (action.equals(Events.ChatConversation.GroupChat.CREATE_ERROR)) {
            Bundle data = intent.getExtras();
            String errorMsg = data.getString(Events.Misc.Extra.ERROR_MESSAGE);
            Tools.showToast(context, errorMsg);
        } else if (action.equals(Events.ChatMessage.FETCH_COMPLETED)
                || action.equals(Events.ChatMessage.MESSAGES_LOADED)) {
            Bundle data = intent.getExtras();
            String chatId = data.getString(Events.ChatConversation.Extra.CHAT_ID);

            if (chatId != null && chatId.equals(mConversation.getChatId())) {
                this.mMessageListAdapter.updateLatestViewedMessageIndex();

                boolean isPreviousMsgFetched = data.getBoolean(Events.ChatConversation.Extra.FETCH_PRE_MSG, false);

                //handle previous messages fetched from server or loaded from db
                if (isPreviousMsgFetched || action.equals(Events.ChatMessage.MESSAGES_LOADED)) {
                    final int index = mMessagesList.getFirstVisiblePosition();
                    final View v = mMessagesList.getChildAt(index);
                    int top = (v == null) ? 0 : v.getTop();
                    final int oldCount = mMessageListAdapter.getCount();

                    refreshMessageList();

                    final int newCount = mMessageListAdapter.getCount();

                    // Scroll to the top of the messages that were seen before
                    // loading new ones.
                    mMessagesList.setSelectionFromTop(newCount - oldCount, top);

                    if (action.equals(Events.ChatMessage.FETCH_COMPLETED)) {
                        boolean keepLoadingViewHeight = oldCount != newCount;
                        hideLoadingMore(keepLoadingViewHeight);
                    }
                } else {
                    //when latest message of chat fetched.
                    refreshMessageList();
                }

                // If this is the first time all the chat messages have been loaded,
                // then scroll to the bottom.
                if (!mDidLoadChatMessages) {
                    showLastMessage();
                    mDidLoadChatMessages = true;
                }

            } else if (chatId == null) {
                Crashlytics.log(Log.ERROR, "CHAT_ID_NULL", "ChatFragment.onReceive Events.GET_MESSAGES_COMPLETE");
            }
        } else if (action.equals(Events.ChatMessage.BEGIN_FETCH)) {
            Bundle data = intent.getExtras();
            String chatId = data.getString(Events.ChatConversation.Extra.CHAT_ID);
            if (chatId.equals(mConversation.getChatId())) {
                if (!mConversation.isConversationSynced()) {
                    Tools.showToast(getActivity(), I18n.tr("Syncing messages"));
                }
            }
        } else if (action.equals(Events.Contact.FETCH_IM_ICONS_COMPLETED)) {
            if (mConversation.isIMChat()) {
                updateUserDetailsHeader();
            }
        } else if (action.equals(Events.ChatConversation.SEND_GIFT_OK)) {
            FragmentHandler.getInstance().clearBackStack();
            ActionHandler.getInstance().displayGiftSent(getActivity());

            Bundle data = intent.getExtras();
            String conversationId = data.getString(Events.ChatConversation.Extra.ID);
            if (conversationId != null && conversationId.equals(mConversation.getId())) {
                showLastMessage();
            }

        } else if (action.equals(Events.ChatConversation.SEND_GIFT_ERROR)) {
            FragmentHandler.getInstance().clearBackStack();

            Bundle data = intent.getExtras();
            String conversationId = data.getString(Events.ChatConversation.Extra.ID);
            if (conversationId != null && conversationId.equals(mConversation.getId())) {
                showLastMessage();
            }

        } else if (action.equals(Events.ChatRoom.FAVOURITED)) {
            Tools.showToast(getActivity(), I18n.tr("Added to favorites"));
        } else if (action.equals(Events.ChatRoom.UNFAVOURITED)) {
            Tools.showToast(getActivity(), I18n.tr("Removed from favorites"));
        } else if (action.equals(Events.ChatRoom.FAVOURITE_ERROR) || action.equals(Events.ChatRoom.UNFAVOURITE_ERROR)) {
            Tools.showToastForIntent(context, intent);
        } else if (action.equals(Events.ChatMessage.RECEIVED_PINNED_STATE) || action.equals(Events.ChatMessage.NO_PINNED_MESSAGE)) {
            Bundle data = intent.getExtras();
            String conversationId = data.getString(Events.ChatConversation.Extra.ID);
            if (conversationId != null && conversationId.equals(mConversation.getId())) {
                if (!isSameAsLastShownPinnedMessage() || !mIsFirstReceivedPinnedState) {
                    mSharedPrefsManager.setShouldShowPinnedMessage(conversationId, true);
                }
                showOrHidePinnedMessage();
                mIsFirstReceivedPinnedState = false;
            }
        } else if (isCompleteDataForMimeDataFetched(action)) {
            refreshMessageListDelay();
            //received  mime data eg. post or deezer radio in chat message
            if (action.equals(Events.DEEZER.FETCH_RADIO_COMPLETED)) {
                mDeezerPlaybackChat.setTrackData();
            }
        } else if (action.equals(Events.Login.SUCCESS)) {
            // Login again when the connection is gone and back; try to join chat room again
            if (mConversation != null && mConversation.isChatroom() && !mConversation.isJoined()) {
                ChatController.getInstance().requestJoinChatRoom(mConversation.getChatId(), true);
            }
        }
    }

    private boolean isMyProfileReceived(Intent intent) {
        String action = intent.getAction();
        if (action.equals(Events.Profile.RECEIVED)) {
            String profileUsername = intent.getStringExtra(Events.User.Extra.USERNAME);
            if (profileUsername != null &&
                    profileUsername.equalsIgnoreCase(mConversation.getChatId())) {
                return true;
            }
        }

        return false;
    }

    void showOrHidePinnedMessage() {
        Message pinnedMessage = mConversation.getPinnedMessage();
        if (pinnedMessage != null) {
            showPinnedMessage(pinnedMessage);
        } else {
            closePinnedMessage();
        }
    }

    private void handleCreateGroupChat() {
        ArrayList<String> users = new ArrayList<String>();

        if (mConversation.isPrivateChat()) {
            users.add(mConversation.getChatId());
            ActionHandler.getInstance()
                    .displayStartChat(getActivity(), StartChatActionType.ADD_TO_PRIVATE_CHAT, null, users);

        } else if (mConversation.isGroupChat()) {
            List<ChatParticipant> participant = mConversation.getParticipants(true);
            for (ChatParticipant chatParticipant : participant) {
                users.add(chatParticipant.getUsername());
            }

            ActionHandler.getInstance().displayStartChat(getActivity(), StartChatActionType.ADD_TO_GROUP_CHAT,
                    mConversationId, users);
        }
    }

    @Override
    public OnPopupMenuListener getPopupMenuListener() {
        return this;
    }

    @Override
    public CustomActionBarConfig getActionBarConfig() {
        CustomActionBarConfig config = new CustomActionBarConfig();
        config.setCustomViewLayoutSrc(R.layout.action_bar_chat);
        config.setNavigationButtonState(NavigationButtonState.BACK);
        config.setShowOverflowButtonState(OverflowButtonState.POPUP);
        config.setShowUnreadCountOnBack(true);
        return config;
    }

    @Override
    public void initCustomViewInCustomActionBar(View customView) {
        // Logger.debug.logWithTrace(LogUtils.TAG_MAIN_UI, getClass());

        if (customView != null) {
            mHeaderUserInfo = (UserBasicDetails) customView.findViewById(R.id.user_details);
            mHeaderChatInfo = customView.findViewById(R.id.chat_details);

            if (mConversation == null) {
                Log.w(LOG_TAG, "Conversation is null at initCustomViewInCustomActionBar. " + "mConversationId = " + mConversationId);
                return;
            }

            if (mConversation.isIMChat()) {
                mHeaderChatInfo.setVisibility(View.GONE);
                mHeaderUserInfo.setVisibility(View.VISIBLE);
                updateUserDetailsHeader();
            } else if (mConversation.isPrivateChat()) {
                mHeaderChatInfo.setVisibility(View.GONE);
                mHeaderUserInfo.setVisibility(View.VISIBLE);
                mHeaderUserInfo.setOnClickListener(this);
                updateUserDetailsHeader();
            } else {
                mHeaderUserInfo.setVisibility(View.GONE);

                mChatName = (TextView) customView.findViewById(R.id.chat_name);
                TextView chatroomIcon = (TextView) customView.findViewById(R.id.chatroom_icon);
                ImageView groupChatIcon = (ImageView) customView.findViewById(R.id.group_chat_icon);

                if (mConversation.isChatroom()) {
                    mChatIcon = chatroomIcon;
                    groupChatIcon.setVisibility(View.GONE);
                    chatroomIcon.setVisibility(View.VISIBLE);

                    chatroomIcon.setText(String.valueOf(Character.toUpperCase(mConversation.getDisplayName().charAt(0))));
                    GradientDrawable bg = (GradientDrawable) chatroomIcon.getBackground();
                    bg.setColor(ChatroomColorController.getInstance().getChatroomColor(mConversation.getChatId()));

                } else if (mConversation.isGroupChat()) {
                    mChatIcon = groupChatIcon;
                    chatroomIcon.setVisibility(View.GONE);
                    groupChatIcon.setVisibility(View.VISIBLE);
                    ImageHandler.loadGroupChatIcon(groupChatIcon, mConversation, Config.getInstance()
                            .getDisplayPicSizeNormal());
                }

                mUserCounter = (TextView) customView.findViewById(R.id.participant_counter);
                mUserCounter.setTextColor(Theme.getColor(ThemeValues.LIGHT_TEXT_COLOR));
                mUserCounter.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                        ApplicationEx.getDimension(R.dimen.text_size_small));

                mHeaderChatInfo.setVisibility(View.VISIBLE);
                mHeaderChatInfo.setOnClickListener(this);
                mChatIcon.setOnClickListener(this);
                updateChatDetailsHeader();
            }
        }
    }

    private void updateGroupChatIcon() {
        if (mConversation.isGroupChat()) {
            if (mChatIcon != null) {
                ImageHandler.loadGroupChatIcon((ImageView) mChatIcon, mConversation, Config.getInstance()
                        .getDisplayPicSizeNormal());
            }
        }
    }

    private void handleSendMessage() {

        GAEvent.Chat_SendTextMessage.send();

        String message = mChatField.getText().toString();

        ChatController.getInstance().sendChatMessage(mConversationId, message);
        mChatField.setText(Constants.BLANKSTR);

        showLastMessage();
    }

    private void showLastMessage() {
        // scroll to bottom after sending a message
        mMessagesList.post(new Runnable() {

            public void run() {
                mMessagesList.setSelection(mMessagesList.getCount() - 1);
            }
        });
    }

    @Override
    public ArrayList<MenuOption> getMenuOptions() {
        ArrayList<MenuOption> menuItems = new ArrayList<MenuOption>();
        if (mConversation.isIMChat()) {
            menuItems.add(new MenuOption(I18n.tr("Leave chat"), R.drawable.ad_chatleave_white,
                    R.id.action_leave_chat));

        } else if (mConversation.isPrivateChat()) {
            menuItems.add(new MenuOption(I18n.tr("Share profile"), R.drawable.ad_share_white,
                    R.id.action_share_profile));
            menuItems.add(new MenuOption(I18n.tr("Add people"), R.drawable.ad_useradd_white,
                    R.id.action_create_group_chat));
            menuItems.add(new MenuOption(I18n.tr("Transfer credit"), R.drawable.ad_credit_white,
                    R.id.option_item_transfer_credit));
            menuItems.add(new MenuOption(I18n.tr("Report abuse"), R.drawable.ad_report_white,
                    R.id.option_item_report_abuse));

            if (mConversation.hasPinnedMessage()) {
                menuItems.add(new MenuOption(I18n.tr("Unpin item"), R.drawable.ic_deezer_pin_white,
                        R.id.option_item_unpin_item));
            }

            if (mConversation.isMuted()) {
                menuItems.add(new MenuOption(I18n.tr("Unmute"), R.drawable.ad_mute_white,
                        R.id.option_item_unmute));
            } else {
                menuItems.add(new MenuOption(I18n.tr("Mute"), R.drawable.ad_mute_white,
                        R.id.option_item_mute));
            }

            menuItems.add(new MenuOption(I18n.tr("Leave chat"), R.drawable.ad_chatleave_white,
                    R.id.action_leave_chat));

        } else if (mConversation.isChatroom()) {
            menuItems.add(new MenuOption(I18n.tr("Share chatroom"), R.drawable.ad_share_white,
                    R.id.action_share_chatroom));

            menuItems.add(new MenuOption(I18n.tr("View participants"), R.drawable.ad_view_white,
                    R.id.action_view_participants));

            menuItems.add(new MenuOption(I18n.tr("Invite people"), R.drawable.ad_useradd_white,
                    R.id.action_invite_people));

            mChatRoomCategory = ChatDatastore.getInstance().getChatRoomCategoryForChatConversation(mConversation); 
            if (mChatRoomCategory != null) {
                if (mChatRoomCategory.canBeDeleted()) {
                    menuItems.add(new MenuOption(I18n.tr("Remove from favorites"), R.drawable.ad_favourite_white,
                            R.id.option_addremove_favourite_chatroom));
                } else {
                    menuItems.add(new MenuOption(I18n.tr("Add to favorites"), R.drawable.ad_favourite_white,
                            R.id.option_addremove_favourite_chatroom));
                }
            }

            menuItems.add(new MenuOption(I18n.tr("Room info"), R.drawable.ad_info_white,
                    R.id.action_chatroom_info));
            menuItems.add(new MenuOption(I18n.tr("Report abuse"), R.drawable.ad_report_white,
                    R.id.option_item_report_abuse));

            if (mConversation.hasPinnedMessage() && mIsChatRoomAdmin) {
                menuItems.add(new MenuOption(I18n.tr("Unpin item"), R.drawable.ic_deezer_pin_white,
                        R.id.option_item_unpin_item));
            }

            if (mConversation.isMuted()) {
                menuItems.add(new MenuOption(I18n.tr("Unmute"), R.drawable.ad_mute_white,
                        R.id.option_item_unmute));
            } else {
                menuItems.add(new MenuOption(I18n.tr("Mute"), R.drawable.ad_mute_white,
                        R.id.option_item_mute));
            }

            menuItems.add(new MenuOption(I18n.tr("Leave chat"), R.drawable.ad_chatleave_white,
                    R.id.action_leave_chat));

        } else if (mConversation.isGroupChat()) {
            menuItems.add(new MenuOption(I18n.tr("View participants"), R.drawable.ad_view_white,
                    R.id.action_view_participants));
            menuItems.add(new MenuOption(I18n.tr("Add people"), R.drawable.ad_useradd_white,
                    R.id.action_add_to_group_chat));
            menuItems.add(new MenuOption(I18n.tr("Change group chat name"), R.drawable.ad_edit_white,
                    R.id.action_change_group_chat_name));
            menuItems.add(new MenuOption(I18n.tr("Report abuse"), R.drawable.ad_report_white,
                    R.id.option_item_report_abuse));

            if (mConversation.hasPinnedMessage()) {
                menuItems.add(new MenuOption(I18n.tr("Unpin item"), R.drawable.ic_deezer_pin_white,
                        R.id.option_item_unpin_item));
            }

            if (mConversation.isMuted()) {
                menuItems.add(new MenuOption(I18n.tr("Unmute"), R.drawable.ad_mute_white,
                        R.id.option_item_unmute));
            } else {
                menuItems.add(new MenuOption(I18n.tr("Mute"), R.drawable.ad_mute_white,
                        R.id.option_item_mute));
            }

            menuItems.add(new MenuOption(I18n.tr("Leave chat"), R.drawable.ad_chatleave_white,
                    R.id.action_leave_chat));
        }

        return menuItems;
    }

    private void handleChatDetailClicked() {
        // Logger.debug.logWithTrace(LogUtils.TAG_MAIN_UI, getClass());
        mConversation = ChatDatastore.getInstance().getChatConversationWithId(mConversationId);
        if (mConversation.isGroupChat()) {
            AlertDialog.Builder builder = AlertHandler.showTextInputDialog(getActivity(), new TextInputListener() {

                @Override
                public void onOk(String data) {
                    if (data != null) {
                        ChatController.getInstance().requestChangeChatName(mConversationId, data);
                    }
                }

                @Override
                public void onCancel() {
                    // DO NOTHING
                }
            }, I18n.tr("Change group chat name"));

            try {
                builder.show();
            } catch (Exception e) {
                Logger.error.log(LOG_TAG, e);
            }
        }
    }

    private void handleUserDetailClicked() {
        if (mConversation.isMigPrivateChat()) {
            mPanelContainer.showPanel(SlidingPanelContainer.LayoutDirection.RIGHT);
        }
    }

    private void showChatParticipants() {
        mPanelContainer.showPanel(SlidingPanelContainer.LayoutDirection.RIGHT);
    }

    private void leaveChat() {
        final String chatId = mConversation.getChatId();
        AlertListener leaveAlertListener = new AlertListener() {

            @Override
            public void onDismiss() {
                // DO NOTHING
            }

            @Override
            public void onConfirm() {
                ChatController.getInstance().requestLeaveGroupChat(chatId, mConversation.getImMessageType().getImType());
                closeFragment();
            }
        };

        if (Session.getInstance().isNetworkConnected()) {
            ActionHandler.getInstance().leaveChatConversation(getActivity(), leaveAlertListener, mConversationId);
        }
        if (!mConversation.isGroupChat()) {
            // since leaving a group chat requires an alert to be displayed
            // first, closing of the fragment will only be called when user
            // confirms
            ChatDatastore.getInstance().addToLeavePrivateChatList(mConversationId);
            closeFragment();
        }
    }

    private void updateUserDetailsHeader() {
        String username = mConversation.getChatId();
        if (!TextUtils.isEmpty(username)) {
            mHeaderUserInfo.showMainIcon();
            mHeaderUserInfo.setUsernameColor(Theme.getColor(ThemeValues.LIGHT_TEXT_COLOR));

            if (mConversation.isIMChat()) {
                mHeaderUserInfo.setUsername(mConversation.getDisplayName());
                // set presence icon for IM contacts
                Friend friend = UserDatastore.getInstance().findUser(username);
                if (friend != null) {
                    mHeaderUserInfo.setIMPresenceIcon(friend.getIMType(), friend.getPresence());
                } else {
                    ImType imType = mConversation.getImMessageType().getImType();
                    if (imType != null) {
                        mHeaderUserInfo.setIMPresenceIcon(imType, PresenceType.OFFLINE);
                    }
                }
            } else {
                mHeaderUserInfo.setUsername(mConversation.getDisplayName());

                Profile userProfile = UserDatastore.getInstance().getProfileWithUsername(username, false);
                if (userProfile != null) {
                    mHeaderUserInfo.setLabels(userProfile.getLabels(), false);
                    mHeaderUserInfo.setUserImage(userProfile);
                }

                // check if user is a friend
                Friend friendDetails = UserDatastore.getInstance().findMig33User(username);
                if (friendDetails != null) {
                    mHeaderUserInfo.setPresenceIcon(friendDetails.getPresence());
                }
            }
        }
    }

    private void updateChatDetailsHeader() {
        if (mChatName != null) {
            mChatName.setText(mConversation.getDisplayName());
            mChatName.setTextColor(Theme.getColor(ThemeValues.LIGHT_TEXT_COLOR));
        }
        if (mUserCounter != null) {
            int count = mConversation.getParticipantsCount(true);
            if (count > 0) {
                mUserCounter.setText(" (" + String.valueOf(count) + ")");
                mUserCounter.setVisibility(View.VISIBLE);
            } else {
                mUserCounter.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void refreshMentionList() {
        List<ChatParticipant> participants = ChatDatastore.getInstance().getParticipantsForChatConversationWithId(
                mConversationId, true);

        ArrayList<String> usernameList = new ArrayList<>();
        if (participants != null) {
            for (ChatParticipant participant : participants) {
                usernameList.add(participant.getUsername());
            }
        }
        if (mPopupListFilter != null) {
            mPopupListFilter.refreshList(usernameList, PopupListFilter.ListType.mentionList);
        }
    }

    private void resetInputContainerHeight() {
        chatContainerHeightAdjust = 0;
        mChatInputContainer.setHeightAdjust(0);
        setFooterViewHeight(0);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEND || actionId == EditorInfo.IME_NULL) {
            handleSendMessage();
        }
        return true;
    }

    /*
     * @see
     * com.projectgoth.ui.fragment.BaseFragment#handleNotificationAvailable(
     * com.projectgoth.notification.StatusAlert.NotificationType,
     * java.lang.String)
     */
    @Override
    public void handleNotificationAvailable(NotificationType type, String notificationId) {
        if (type == NotificationType.CHAT_NOTIFICATION) {
            Logger.debug.logWithTrace(LogUtils.TAG_NOTIFICATION, getClass(), notificationId);
            updateNotifications();

            if (!mConversationId.equals(notificationId)) {
                super.handleNotificationAvailable(type, notificationId);
            }
        } else {
            super.handleNotificationAvailable(type, notificationId);
        }
    }

    /*
     * @see com.projectgoth.ui.fragment.BaseFragment#updateNotifications()
     */
    @Override
    protected void updateNotifications() {
        ApplicationEx.getInstance().getNotificationHandler().removeNotification(mConversationId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.projectgoth.ui.holder.BaseViewHolder.BaseViewListener#onItemClick
     * (android.view.View, java.lang.Object)
     */
    @Override
    public void onItemClick(View v, final Message data) {
        int viewId = v.getId();
        switch (viewId) {
            case R.id.message_avatar:
                if (data.isFusionMessage()) {
                    GAEvent.Chat_OpenMiniprofile.send();
                    ActionHandler.getInstance().displayProfile(getActivity(), data.getSender());
                }
                break;
            case R.id.container:
            case R.id.message_container:
            case R.id.content_view:
                // this is to hide emoticon or sticker drawer or keyboard when touching the area that has nothing to click
                if (mMsgInputPanel.isEmoticonDrawerShown()) {
                    mMsgInputPanel.hideEmoticonDrawer();
                }

                if (mMsgInputPanel.isStickerDrawerShown()) {
                    mMsgInputPanel.hideStickerDrawer();
                }

                Tools.hideVirtualKeyboard(getActivity());

                if (data.hasFailed()) {
                    List<ContextMenuItem> menuItemList = getFailedMessageContextMenuOptions(data);
                    Tools.showContextMenu(I18n.tr("Message"), menuItemList, this);
                }

                break;
            default:
                break;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.projectgoth.ui.holder.BaseViewHolder.BaseViewListener#onItemLongClick
     * (android.view.View, java.lang.Object)
     */
    @Override
    public void onItemLongClick(View v, Message data) {
        int viewId = v.getId();
        switch (viewId) {
            case R.id.message_container:
                MimeData mimeData = data.hasOneMimeData() ? data.getFirstMimeData() : null;
                ArrayList<ContextMenuItem> menuItemList = getLongClickMenuOptions(data, mimeData);
                if (menuItemList.size() > 0) {
                    Tools.showContextMenu(I18n.tr("mig"), menuItemList, this);
                }
                break;
            default:
                Logger.error.log(LOG_TAG, "viewId: " + viewId);
                break;
        }

    }

    @Override
    public void onContentViewLongClick(Message msg, MimeData mimeData) {

        ArrayList<ContextMenuItem> menuItemList = getLongClickMenuOptions(msg, mimeData);
        if (menuItemList.size() > 0) {
            Tools.showContextMenu(I18n.tr("mig"), menuItemList, this);
        }
    }

    /**
     * @return List of menu items when someone long clicks a chat
     */
    private ArrayList<ContextMenuItem> getLongClickMenuOptions(Message message, MimeData mimeData) {
        ArrayList<ContextMenuItem> menuItems = new ArrayList<ContextMenuItem>();

        if (message.isStringContent()) {
            menuItems.add(new ContextMenuItem(I18n.tr("Copy"), R.id.option_item_copy, message));
            menuItems.add(new ContextMenuItem(I18n.tr("Share"), R.id.option_item_share, message));
        }

        if (mimeData != null) {

            if (!mConversation.isChatroom() || mIsChatRoomAdmin) {
                PinMessageData pinData = new PinMessageData(message, mimeData);
                menuItems.add(new ContextMenuItem(I18n.tr("Pin"), R.id.option_item_pin, pinData));
            }
        }

        return menuItems;
    }

    /**
     * Creates and returns a list of context menu options to be displayed when
     * the user taps on a failed chat message.
     *
     * @param data Extra data to be used as a tag
     * @return A {@link java.util.List} containing {@link com.projectgoth.model.ContextMenuItem}.
     */
    private List<ContextMenuItem> getFailedMessageContextMenuOptions(final Message data) {
        List<ContextMenuItem> menuItems = new ArrayList<ContextMenuItem>();
        menuItems.add(new ContextMenuItem(I18n.tr("Try again"), R.id.option_item_retry, data));
        return menuItems;
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (!mConversation.isMigPrivateChat()) {
            if (count == 1) {
                String typed = s.subSequence(start, start + count).toString();
                if (typed.equals(Constants.MENTIONS_TAG)) {
                    showMentionList();
                }
            }
        }
    }

    private void showKeyboard() {
        mChatField.requestFocus();
        Tools.showVirtualKeyboard(getActivity(), mChatField);
    }

    @Override
    public void onMenuOptionClicked(MenuOption menuOption) {
        switch (menuOption.getActionId()) {
            case R.id.action_invite_people:
                GAEvent.Chat_DropdownAddPeopleToChat.send();
                ActionHandler.getInstance().displayStartChat(getActivity(), StartChatActionType.INVITE_FRIENDS,
                        mConversationId, new ArrayList<String>());
                break;
            case R.id.option_item_transfer_credit:
                GAEvent.Chat_DropdownTransferCredits.send();
                ActionHandler.getInstance().displayBrowser(getActivity(),
                        String.format(WebURL.URL_TRANSFER_CREDITS, mConversation.getChatId()), I18n.tr("Transfer credit"),
                        R.drawable.ad_credit_white);
                break;
            case R.id.option_item_report_abuse:
                ActionHandler.getInstance().displayBrowser(getActivity(), String.format(WebURL.URL_REPORT_USER,
                        mConversation.getChatId()), I18n.tr("Report"), R.drawable.ad_report_white);
                break;
            case R.id.option_item_unpin_item:
                unPinMessage(mConversation.getPinnedMessage());
                break;
            case R.id.action_chatroom_info:
                ActionHandler.getInstance().displayBrowser(getActivity(), String.format(WebURL.URL_CHATROOM_INFO,
                        mConversation.getChatId()), I18n.tr("Room info"), R.drawable.ad_info_white);
                break;
            case R.id.action_create_group_chat:
            case R.id.action_add_to_group_chat:
                handleCreateGroupChat();
                break;
            case R.id.action_leave_chat:
                leaveChat();
                break;
            case R.id.action_change_group_chat_name:
                handleChatDetailClicked();
                break;
            case R.id.action_view_participants:
                showChatParticipants();
                break;
            case R.id.option_addremove_favourite_chatroom:
                if (mChatRoomCategory != null) {
                    if (mChatRoomCategory.canBeDeleted()) {
                        ChatDatastore.getInstance().requestRemoveFavouriteChatRoom(mConversation.getDisplayName(),
                                mChatRoomCategory.getID());
                    } else {
                        GAEvent.Chat_DropdownAddFavorite.send();
                        ChatDatastore.getInstance().requestAddFavouriteChatRoom(mConversation.getDisplayName());
                    }
                }
                break;
            case R.id.action_share_profile:
                ShareManager.shareProfile(getActivity(), mConversation.getChatId());
                break;
            case R.id.action_share_chatroom:
                ShareManager.shareChatroom(getActivity(), mConversation);
                break;
            case R.id.option_item_mute:
                ChatController.getInstance().setMute(true, mConversation);
                Tools.showToast(getActivity(), I18n.tr("Muted conversation"));
                break;
            case R.id.option_item_unmute:
                ChatController.getInstance().setMute(false, mConversation);
                Tools.showToast(getActivity(), I18n.tr("Unmuted conversation"));
                break;
        }
    }

    @Override
    public void onContextMenuItemClick(ContextMenuItem menuItem) {
        int id = menuItem.getId();
        Object data = menuItem.getData();
        Message message = null;
        PinMessageData pinData = null;
        if (data instanceof Message) {
            message = (Message) data;
        } else if (data instanceof PinMessageData) {
            pinData = (PinMessageData) data;
        }

        switch (id) {
            case R.id.option_item_copy:
                ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(
                        Context.CLIPBOARD_SERVICE);
                clipboard.setText(message.getMessage());
                break;
            case R.id.option_item_share:
                ActionHandler.getInstance().displaySharebox(getActivity(), ShareboxActionType.CREATE_NEW_POST, null,
                        message.getMessage(), null, true);
                break;
            case R.id.option_item_retry:
                if (ArrayUtils.containsObjectOfType(message.getMimeDataList(), TextPlainMimeData.class) && message.isOutgoing()) {
                    GAEvent.Chat_RetryTextMessage.send();
                } else if (ArrayUtils.containsObjectOfType(message.getMimeDataList(), StickerMimeData.class) && message.isOutgoing()) {
                    GAEvent.Chat_RetryStickerUi.send();
                }

                ChatController.getInstance().sendChatMessage(mConversation, message);
            case R.id.option_item_pin:
                pinMessage(pinData);
                break;

            default:
                break;
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        super.onScrollStateChanged(view, scrollState);

        if (view == mMessagesList) {
            if (scrollState == OnScrollListener.SCROLL_STATE_IDLE && isListAtTop(mMessagesList)) {
                final Message data = mMessageListAdapter.getItem(0);
                Long timestampStart = mConversation.getPreviousMessageTimestamp(data);
                List<Message> messages = new ArrayList<Message>();
                // load from storage first
                if (timestampStart == null) {
                    // it is the load more on the top
                    messages = ChatDatastore.getInstance().loadMoreMessageFromStorage(mConversation,
                            data.getLongTimestamp());
                }

                if (messages.size() > 0) {
                    this.mMessageListAdapter.updateLatestViewedMessageIndex();
                    //sync the delivery status of the old messages loaded from storage 
                    ChatController.getInstance().sendGetMsgDeliveryStatus(mConversationId, messages);
                } else {
                    // fetch from server
                    if (!data.isLoadingPreviousMessages()) {
                        data.setLoadingPreviousMessages(true);

                        showLoadingMore();

                        Long start = timestampStart;
                        Long end = data.getLongTimestamp();

                        ChatSyncOnLoadPreMsgClickListener listener = new ChatSyncOnLoadPreMsgClickListener(
                                data.getMessageId(), this.mConversationId);
                        ChatController.getInstance().sendGetMessages(this.mConversationId, start, end, listener);
                    }
                }
            }

            mDidLoadChatMessages = true;
        }
    }

    @Override
    public void onListViewScrolled(int deltaY) {
        if (mMsgInputPanel.isKeyboardDisplayed() || mMsgInputPanel.isStickerDrawerShown()
                || mMsgInputPanel.isEmoticonDrawerShown()) {

            // Aggregate all of the delta Ys into how much the view has been adjusted by.
            chatContainerHeightAdjust -= deltaY;

            // If the footer view is visible, because scrolling has overshot the end,
            // Then we need to take the visible height of the footer and adjust
            // accordingly.
            if (mFooterView.getParent() == mMessagesList &&
                    mFooterView.getTop() < mMessagesList.getHeight()) {
                int displayOverhang = mMessagesList.getHeight() - mFooterView.getTop();
                chatContainerHeightAdjust += displayOverhang;
            }

            // Clamp our adjust height to between
            // -MAX_SCROLL_TO_MAKE_INPUT_APPEAR and
            // INPUT_SCROLL_DEAD_ZONE
            if (chatContainerHeightAdjust < -MAX_SCROLL_TO_MAKE_INPUT_APPEAR) {
                chatContainerHeightAdjust = -MAX_SCROLL_TO_MAKE_INPUT_APPEAR;
            } else if (chatContainerHeightAdjust > INPUT_SCROLL_DEAD_ZONE) {
                chatContainerHeightAdjust = INPUT_SCROLL_DEAD_ZONE;
            }

            // Adjust the input view height, and make the footer height provide the
            // truncated height
            int previousFooterHeight = -mChatInputContainer.getVisibleHeightAdjust();
            mChatInputContainer.setHeightAdjust(Math.min(chatContainerHeightAdjust, 0));
            int footerHeight = -mChatInputContainer.getVisibleHeightAdjust();
            if (footerHeight != previousFooterHeight) {
                setFooterViewHeight(footerHeight);
            }
        }
    }

    private void setFooterViewHeight(int height) {
        LayoutParams layoutParams = mFooterView.getLayoutParams();
        if (layoutParams == null) {
            layoutParams = new ListView.LayoutParams(1, height, ListView.ITEM_VIEW_TYPE_HEADER_OR_FOOTER);
            mFooterView.setLayoutParams(layoutParams);
        } else {
            layoutParams.height = height;
            mFooterView.requestLayout();
        }
    }

    /**
     * Checks whether the given {@link android.widget.AbsListView} is scrolled to the top.
     *
     * @param listView The {@link android.widget.AbsListView} to be checked.
     * @return true if scrolled to the top and false otherwise.
     */
    private boolean isListAtTop(final AbsListView listView) {
        if (listView.getChildCount() == 0) {
            // If there are no children in the listview, then
            // the list is at the top.
            return true;
        }

        // Get the first visible view in the listview and check if it is
        // at the top.
        final View topView = listView.getChildAt(listView.getFirstVisiblePosition());
        return (topView != null && topView.getTop() == 0);
    }

    private boolean isListAtBottom(final AbsListView listView) {
        if (listView.getChildCount() == 0) {
            return true;
        }

        if (mMessagesList.getLastVisiblePosition() < this.mMessageListAdapter.getLatestViewedMessageIndex()) {
            return false;
        }

        return true;
    }

    @Override
    public void onPanelOpen(View view, SlidingPanelContainer.LayoutDirection direction) {

        Tools.hideVirtualKeyboard(view, getActivity());

        switch (direction) {
            case LEFT:
                // If we reveal the left panel, we have swiped right
                GAEvent.Chat_SwipeRight.send();
                break;

            case RIGHT:
                // If we reveal the right panel, we have swiped left
                GAEvent.Chat_SwipeLeft.send();
                break;

            default:
                break;
        }

        if (mToolTipView != null && NUEManager.getInstance().shouldShowNUE(LOG_TAG)) {
            mToolTipView.onClick(mToolTipView);
        }
    }

    @Override
    public void onPanelClosed(View view, SlidingPanelContainer.LayoutDirection direction) {
        GAEvent.Chat_SwipeCenter.send();
    }

    @Override
    public void onToolTipViewClicked(ToolTipView toolTipView) {
        mToolTipView = null;
        NUEManager.getInstance().alreadyShownNUE(LOG_TAG);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                addSecondToolTipView();
            }
        }, Constants.NUE_TOOLTIP_DELAY);
    }

    private void addFirstToolTipView() {
        View customView = LayoutInflater.from(this.getActivity()).inflate(R.layout.tooltip_title_subtitle_image_title_subtitle, null);
        ((TextView) customView.findViewById(R.id.title)).setText(I18n.tr("Swipe left"));
        ((TextView) customView.findViewById(R.id.subtitle)).setText(I18n.tr("to see chat details."));
        ((TextView) customView.findViewById(R.id.title2)).setText(I18n.tr("Swipe right"));
        ((TextView) customView.findViewById(R.id.title2)).setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ad_swipe_white, 0, 0);
        ((TextView) customView.findViewById(R.id.subtitle2)).setText(I18n.tr("to see all recent chats."));
        mToolTipView = mToolTipFrameLayout.showToolTipInPosition(
                new ToolTip()
                        .withContentView(customView)
                        .withColor(ApplicationEx.getColor(R.color.edit_text_input_color))
                        .withAnimationType(ToolTip.ANIMATIONTYPE_FADE)
                        .withShadow(false),
                ToolTipEnum.CENTER);
        mToolTipView.setOnToolTipViewClickedListener(this);
    }

    private void showPinnedMessage(Message message) {

        //deezer setup
        List<MimeData> mimeDataList = message.getMimeDataList();
        DeezerMimeData deezerData = null;
        boolean isDeezer = false;

        for (MimeData data : mimeDataList) {
            if (DeezerMimeData.class.isInstance(data)) {
                deezerData = (DeezerMimeData) data;
                isDeezer = true;
            }
        }

        if (deezerData != null) {
            long radioId = deezerData.getLongId();
            GAEvent.Deezer_PinToChat.send(radioId);
            long currentRadioId = DeezerPlayerManager.getInstance().getBgPlayingRadioId();
            if (currentRadioId != DeezerPlayerManager.INVALID_RADIO_ID) {
                if (currentRadioId != radioId) {
                    DeezerPlayerManager.getInstance().setBgPlayingRadioId(radioId);
                    mDeezerPlaybackChat.playPinnedSong(radioId);
                } else {
                    mDeezerPlaybackChat.setTrackData();
                }
            } else {
                DeezerPlayerManager.getInstance().setBgPlayingRadioId(radioId);
                mDeezerPlaybackChat.playPinnedSong(radioId);
            }
        }

        if (isDeezer) {
            showDeezerPinnedView();
        } else {
            showDefaultPinnedView(message);
        }

        if (mSharedPrefsManager.getShouldShowPinnedMessage(mConversationId)) {
            mSlidingLayer.openLayer(true);
            mSharedPrefsManager.setLastShownPinnedMessageId(message.getMessageId());
        } else {
            mSlidingLayer.openPreview(true);
        }
    }

    private void pinMessage(PinMessageData pinData) {
        // send packet
        ChatController.getInstance().sendPinnedChatMessageToServer(pinData);
        // it will be shown when receiving the ok response
    }

    private void unPinMessage(Message message) {
        ChatController.getInstance().sendUnpinnedChatMessageToServer(message);
        closePinnedMessage();
    }

    private void closePinnedMessage() {

        boolean isDeezerPinned = false;

        Message msg = mConversation.getPinnedMessage();
        List<MimeData> mimeDataList = new ArrayList<MimeData>();

        if (msg != null) {
            mimeDataList = msg.getMimeDataList();
        }

        if (mimeDataList != null) {
            for (MimeData data : mimeDataList) {
                if (DeezerMimeData.class.isInstance(data)) {
                    isDeezerPinned = true;
                }
            }
        }

        if (isDeezerPinned) {
            DeezerPlayerManager.getInstance().pause();
            showDeezerPinnedView();
        }

        mSlidingLayer.closeLayer(true);
    }

    private void showDeezerPinnedView() {
        mPinContainer.setVisibility(View.GONE);
        mDeezerPlaybackChat.setVisibility(View.VISIBLE);
    }

    private void showDefaultPinnedView(Message message) {
        mPinnedMessageViewHolder.setData(message);
        mDeezerPlaybackChat.setVisibility(View.GONE);
        mPinContainer.setVisibility(View.VISIBLE);
    }

    /**
     * Load mConversation from cache and database, may recreate one if chatId is not empty.
     *
     * @return true if mConversation is loaded
     */
    private boolean loadConversation() {
        if (!TextUtils.isEmpty(mConversationId)) {
            mConversation = ChatDatastore.getInstance().getChatConversationWithId(mConversationId);
        }

        if (mConversation == null && !TextUtils.isEmpty(mConversationChatId) && !mChatType.equals(ChatTypeEnum.UNKNOWN) && !mImMessageType.equals(MessageType.UNKNOWN)) {
            mConversation = ChatDatastore.getInstance().findOrCreateConversation(mChatType, mConversationChatId, mImMessageType);
        }

        if (mConversation == null && ChatDatastore.getInstance().isInLeavePrivateChatList(mConversationId)) {
            return false;
        }

        if (mConversation == null) {
            StringBuilder builder = new StringBuilder();
            builder.append("conversation is null in loadConversation(), conversation is not in LeavePrivateChatList");
            builder.append(", conversationId : " + mConversationId);
            builder.append(", conversationChatId : " + mConversationChatId);
            builder.append(", chatType : " + mChatType);
            builder.append(", imMessageType : " + mImMessageType);
            CrashlyticsLog.log(new NullPointerException(), builder.toString());
            return false;
        }

        mConversationId = mConversation.getId();
        return true;
    }

    private boolean isSameAsLastShownPinnedMessage() {
        Message pinnedMsg = mConversation.getPinnedMessage();
        if (pinnedMsg != null) {
            if (pinnedMsg.getMessageId().equals(mSharedPrefsManager.getLastShownPinnedMessageId())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onSendMessageButtonClick() {
        handleSendMessage();
    }

    @Override
    public void onGiftIconClick() {
        //final String initialRecipient = mConversation.isMigPrivateChat() ? mConversation.getDisplayName() : "";
        //ActionHandler.getInstance().displayGiftCenterFragment(getActivity(), mConversationId, initialRecipient);

        ArrayList<String> recipients = new ArrayList<>();
        if (mConversation.isChatroom() || mConversation.isGroupChat()) {
            List<ChatParticipant> participant = mConversation.getParticipants(true);
            for (ChatParticipant chatParticipant : participant) {
                recipients.add(chatParticipant.getUsername());
            }
        } else {
            recipients.add(mConversation.getChatId());
        }
        ActionHandler.getInstance().displaySendGiftFragment(getActivity(), recipients,
                SendGiftFragment.ActionType.GIFT_IN_CHAT, mConversationId);
    }

    @Override
    public void onStickerSelect(Sticker sticker) {
        GAEvent.Chat_SendStickerUi.send(sticker.getPackId());

        ChatController.getInstance().sendSticker(mConversationId, sticker);

        UsedChatItem usedChatItem = new UsedChatItem(UsedChatItemType.STICKER, sticker.getMainHotkey());
        EmoticonDatastore.getInstance().addUsedChatItemToUsedCache(usedChatItem);
        showLastMessage();
    }



    @Override
    public void onPhotoClick(byte[] photo) {

        if (!NetworkUtils.isNetworkAvailable(getActivity())) {
            Tools.showToast(getActivity(), I18n.tr("No internet, sending failed!"), Toast.LENGTH_LONG);
            return;
        }

        GAEvent.Chat_SendImage.send(photo.length);
        ChatController.getInstance().sendPhotoMessage(mConversationId, photo, getActivity());
        showLastMessage();
    }

    @Override
    public void onEmotionSelectionShown() {

    }

    @Override
    public void onEmotionSelectionHidden() {
        resetInputContainerHeight();
    }

    @Override
    public void onStickerSelectionShown() {

    }

    @Override
    public void onStickerSelectionHidden() {
        resetInputContainerHeight();
    }

    @Override
    public void onKeyboardShown() {
        if (isListAtBottom(mMessagesList)) {
            showLastMessage();
        }
    }

    public void upatePlaybackUI() {
        if (mDeezerPlaybackChat != null) {
            mDeezerPlaybackChat.updateUI();
            mDeezerPlaybackChat.setTrackData();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        upatePlaybackUI();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        DeezerPlayerManager.getInstance().detachPlayback();
    }

    @Override
    public void onKeyboardHidden() {
        resetInputContainerHeight();
    }

    //Interaction listener for sliding layer.
    @Override
    public void onOpen() {
    }

    @Override
    public void onOpened() {
        mSharedPrefsManager.setShouldShowPinnedMessage(mConversationId, true);
    }

    @Override
    public void onShowPreview() {
        mSharedPrefsManager.setShouldShowPinnedMessage(mConversationId, false);
    }

    @Override
    public void onPreviewShowed() {
    }

    @Override
    public void onClose() {
    }

    @Override
    public void onClosed() {
    }

    private void setupOnBackPressListener() {
        final Activity activity = getActivity();

        if (activity != null && activity instanceof CustomPopupActivity) {
            ((CustomPopupActivity) activity).setOnBackPresslistener(new CustomPopupActivity.OnBackPressListener() {

                @Override
                public boolean onBackPress() {
                    if (mIsFromSystemNotification) {
                        ActionHandler.getInstance().goToMyChats();
                    }
                    if (activity != null) {
                        activity.finish();
                    }
                    return true;
                }
            });
        }
    }

    @Override
    public void onBackIconPressed() {
        final Activity activity = getActivity();
        if (activity != null) {
            if (mIsFromSystemNotification) {
                ActionHandler.getInstance().goToMyChats();
            } else {
                activity.onBackPressed();
            }
        }
    }


}


