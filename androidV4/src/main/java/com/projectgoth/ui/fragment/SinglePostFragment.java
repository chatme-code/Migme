
package com.projectgoth.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mig33.diggle.events.Events;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.HotTopic;
import com.projectgoth.b.data.HotTopicsResult;
import com.projectgoth.b.data.Post;
import com.projectgoth.b.data.TagEntity;
import com.projectgoth.b.data.ThirdPartySites;
import com.projectgoth.b.data.UserTagId;
import com.projectgoth.b.enums.PostOriginalityEnum;
import com.projectgoth.common.Config;
import com.projectgoth.common.Constants;
import com.projectgoth.common.ShareManager;
import com.projectgoth.common.SpannableBuilder;
import com.projectgoth.common.Theme;
import com.projectgoth.common.ThemeValues;
import com.projectgoth.common.Tools;
import com.projectgoth.common.WebURL;
import com.projectgoth.controller.PostsController;
import com.projectgoth.controller.ThirdPartySitesController;
import com.projectgoth.datastore.PostsDatastore;
import com.projectgoth.datastore.Session;
import com.projectgoth.datastore.SystemDatastore;
import com.projectgoth.events.AppEvents;
import com.projectgoth.events.GAEvent;
import com.projectgoth.i18n.I18n;
import com.projectgoth.listener.PostViewListener;
import com.projectgoth.listener.SinglePostViewListener;
import com.projectgoth.model.BarItem;
import com.projectgoth.model.ContextMenuItem;
import com.projectgoth.model.LocationListItem;
import com.projectgoth.model.MenuOption;
import com.projectgoth.model.MenuOption.MenuAction;
import com.projectgoth.model.MenuUtils;
import com.projectgoth.nemesis.enums.PostCategoryTypeEnum;
import com.projectgoth.nemesis.enums.PostPrivacyEnum;
import com.projectgoth.nemesis.enums.ReplyPermissionEnum;
import com.projectgoth.nemesis.enums.RequestTypeEnum;
import com.projectgoth.nemesis.model.Sticker;
import com.projectgoth.nemesis.utils.RetryConfig;
import com.projectgoth.ui.activity.ActionHandler;
import com.projectgoth.ui.activity.AlertHandler;
import com.projectgoth.ui.activity.CustomActionBarConfig;
import com.projectgoth.ui.activity.CustomActionBarConfig.NavigationButtonState;
import com.projectgoth.ui.activity.CustomActionBarConfig.OverflowButtonState;
import com.projectgoth.ui.adapter.EmotionalFootprintGridAdapter;
import com.projectgoth.ui.adapter.ReplyListAdapter;
import com.projectgoth.ui.fragment.ShareboxFragment.ShareboxActionType;
import com.projectgoth.ui.holder.PostViewHolder;
import com.projectgoth.ui.listener.ContextMenuItemListener;
import com.projectgoth.ui.widget.EditTextEx;
import com.projectgoth.ui.widget.EmoticonGridView;
import com.projectgoth.ui.widget.HeaderBar;
import com.projectgoth.ui.widget.LinearLayoutEx;
import com.projectgoth.ui.widget.PopupListFilter;
import com.projectgoth.ui.widget.MessageInputPanel;
import com.projectgoth.ui.widget.PopupMenu;
import com.projectgoth.ui.widget.TextViewEx;
import com.projectgoth.util.LocationUtils;
import com.projectgoth.util.PostUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;

public class SinglePostFragment extends BaseListFragment implements OnClickListener, ContextMenuItemListener,
        MessageInputPanel.MessageInputPanelListener, PopupListFilter.PopupListFilterListener {

    public static final String              PARAM_POST_ID               = "postId";
    public static final String              PARAM_IS_POST_IN_GROUP      = "isPostInGroup";
    public static final String              PARAM_SELECTED_TAB          = "PARAM_SELECTED_TAB";
    public static final String              PARAM_IS_REPLY_OR_RESHARE   = "PARAM_IS_REPLY_OR_RESHARE";
    private static final int                LOAD_MORE_INCREMENT         = 15;
    private static final int                PINNED_BACKGROUND_ALPHA     = 240;
    private View                            mPostItem;
    private HeaderBar                       mHeaderBar;
    private HeaderBar                       mStickyHeader;
    private View                            mEmptyView;
    private FrameLayout                     emptyViewContainer;
    private EmoticonGridView                mEmoteGrid;
    private ReplyListAdapter                mReplyListAdapter;
    private @NonNull String                 mPostId;
    private Post                            mPost;
    private PostViewHolder                  mPostViewHolder;
    private boolean                         mIsPostInGroup;
    private HeaderTab                       mSelectedTabParam;
    private int                             mReplyLoadLimit             = LOAD_MORE_INCREMENT;
    private int                             mReshareLoadLimit           = LOAD_MORE_INCREMENT;
    private boolean                         mIsLoadingReplies = false;
    private boolean                         mIsLoadingReshares = false;
    private int                             mRepliesOffsetDelta         = 0;
    private int                             mResharesOffsetDelta        = 0;
    private HeaderTab                       mHeaderTab;
    private PostViewListener                mPostViewListener;
    private EmotionalFootprintGridAdapter   mEmotionalGridAdapter;
    private GridView                        mEmotionGridView;
    private RelativeLayout                  mEmotionalGridWrapper;
    private RelativeLayout                  mMessageInputPanelWrapper;
    private RelativeLayout                  mRepostMessageWrapper;
    private MessageInputPanel               mMessageInputPanel;
    private EditTextEx                      mChatField;
    private EditText                        mRepostInputField;
    private ImageView                       mPopupMenuMarker;
    private boolean                         mIsTwitterTurnedOff;
    private boolean                         mIsFacebookTurnedOff;
    private ImageView                       mTwitterButton;
    private ImageView                       mFacebookButton;
    private ImageView                       mPrivacyButton;
    private ImageView                       mPostButton;
    private ImageView                       mEmoticIconButton;
    private TextView                        mCountContainer;
    private LinearLayoutEx                  mWrapper;
    private PopupListFilter                 mPopupListFilter;
    private final static int                HOT_TOPICS_LIMIT            = 30;
    private final static String             COUNTER_MAX_VALUE           = "300";
    private int                             mSelectedTab;
    private boolean                         mIsReplyOrReshare;
    private boolean                         mIsFirstMessageAligned      = false;

    public enum HeaderTab {
        REPLY_TAB, RESHARE_TAB, EMOTE_TAB;

        public static HeaderTab fromOrdinal(int ordinal) {
            for (HeaderTab tab : values()) {
                if (ordinal == tab.ordinal()) {
                    return tab;
                }
            }
            return REPLY_TAB;
        }
    }

    //@formatter:off
    private ConcurrentHashMap<String, SpannableStringBuilder> spannableCache = new ConcurrentHashMap<String, SpannableStringBuilder>();
    //@formatter:on

    public SinglePostFragment() {
        super();
    }

    @Override
    protected void readBundleArguments(Bundle args) {
        super.readBundleArguments(args);
        mPostId = args.getString(PARAM_POST_ID);
        mPost = PostsDatastore.getInstance().getPost(mPostId, false);
        mIsPostInGroup = args.getBoolean(PARAM_IS_POST_IN_GROUP, false);
        mSelectedTab = args.getInt(PARAM_SELECTED_TAB);
        mSelectedTabParam = HeaderTab.fromOrdinal(mSelectedTab);
        mIsReplyOrReshare = args.getBoolean(PARAM_IS_REPLY_OR_RESHARE, false);

    }

    @Override
    public void onRefresh() {
        if(Session.getInstance().isNetworkConnected()) {
            refreshData(true);
        } else {
            setPullToRefreshComplete();
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // Need to create postViewListener before calling super because it calls
        // createHeaderView, which needs a postViewListener
        mPostViewListener = new SinglePostViewListener(getActivity());

        super.onViewCreated(view, savedInstanceState);

        mList.setDivider(null);
        mList.setBackgroundColor(ApplicationEx.getColor(R.color.dark_gray_background));
        addSpaceFooterView();

        mStickyHeader = (HeaderBar) view.findViewById(R.id.header_container);

        initHeader();
        initEmotionalGridView(view);
        initReplyMessageWrapper(view);
        initRepostMessageWrapper(view);

        // set the default highlighted tab
        switchHeaderTab(mSelectedTabParam);

        if (mSelectedTabParam == HeaderTab.EMOTE_TAB) {
            mEmotionalGridWrapper.setVisibility(View.VISIBLE);
        } else if (mSelectedTabParam == HeaderTab.REPLY_TAB) {
            mMessageInputPanelWrapper.setVisibility(View.VISIBLE);
        } else if (mSelectedTabParam == HeaderTab.RESHARE_TAB) {
            mRepostMessageWrapper.setVisibility(View.VISIBLE);
        }

        //hide header view to align sticky header if entering from reply or repost icon
        if(mIsReplyOrReshare) {
            setHeaderItemVisible(false);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_single_post;
    }

    private void initRepostMessageWrapper(View view) {
        mRepostMessageWrapper = (RelativeLayout) view.findViewById(R.id.repost_message_wrapper);
        mEmoticIconButton = (ImageView) view.findViewById(R.id.attach_emoticon_button);
        mTwitterButton = (ImageView) view.findViewById(R.id.twitter_button);
        mFacebookButton = (ImageView) view.findViewById(R.id.facebook_button);
        mPrivacyButton = (ImageView) view.findViewById(R.id.privacy_button);
        mPopupMenuMarker = (ImageView) view.findViewById(R.id.overflow_marker);
        mPostButton = (ImageView) view.findViewById(R.id.post_button);
        mCountContainer = (TextView) view.findViewById(R.id.char_count_container);
        mCountContainer.setText(COUNTER_MAX_VALUE);
        mRepostInputField = (EditText) view.findViewById(R.id.repost_input_field);
        mRepostInputField.setHint(I18n.tr("Repost"));
        bindOnClickListener(this, R.id.repost_input_field, R.id.twitter_button, R.id.facebook_button,
                R.id.privacy_button, R.id.post_button, R.id.attach_emoticon_button);
        createPrivacyOptionsPopupMenu();
        resetPrivacyDisplay();

    }

    private void initReplyMessageWrapper(View view) {
        mWrapper = (LinearLayoutEx) view.findViewById(R.id.wrapper);
        mMessageInputPanelWrapper = (RelativeLayout) view.findViewById(R.id.message_panel_wrapper);
        mMessageInputPanel = (MessageInputPanel) getFragmentManager().findFragmentById(R.id.msg_input_panel);
        //In some platforms, need to use child fragment manager to get the fragment
        if (mMessageInputPanel == null) {
            mMessageInputPanel = (MessageInputPanel) getChildFragmentManager().findFragmentById(R.id.msg_input_panel);
        }

        mMessageInputPanel.setFromSinglePostFragment(true);
        mMessageInputPanel.setPostId(mPostId);
        mMessageInputPanel.setListener(this);
        mChatField = mMessageInputPanel.getEditText();
        mChatField.setOnEditorActionListener(this);
        int padding = (int) getResources().getDimension(R.dimen.normal_padding);
        mChatField.setPadding(mChatField.getPaddingLeft(), padding,
                mChatField.getPaddingRight(), padding);
        mChatField.setHint(I18n.tr("Leave your comment"));
        mWrapper.setKeyboardListener(mMessageInputPanel);

        mChatField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count == 1) {
                    String typed = s.subSequence(start, start + count).toString();
                    if (typed.equals(Constants.MENTIONS_TAG)) {
                        showMentionList(PopupListFilter.ListType.mentionList);
                    } else if (typed.equals(Constants.HASH_TAG)) {
                        showMentionList(PopupListFilter.ListType.hotTopicList);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void initEmotionalGridView(View view) {
        mEmotionalGridWrapper = (RelativeLayout) view.findViewById(R.id.emotional_grid_wrapper);
        mEmotionGridView = (GridView) view.findViewById(R.id.emotional_footprints_grid);
        mEmotionalGridAdapter = new EmotionalFootprintGridAdapter(getActivity());
        mEmotionGridView.setAdapter(mEmotionalGridAdapter);
        mEmotionGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int tagId = (int) view.getTag();
                onEmotionalFootprintClicked(mPostId, tagId);
            }
        });
        TextView title = (TextView) view.findViewById(R.id.title);
        title.setText(I18n.tr("I feel"));
    }

    private void initHeader() {
        BarItem replyItem = new BarItem(R.drawable.post_action_reply) {

            @Override
            public void onPress() {
                mEmotionalGridWrapper.setVisibility(View.GONE);
                mRepostMessageWrapper.setVisibility(View.GONE);
                mMessageInputPanelWrapper.setVisibility(View.VISIBLE);
                GAEvent.SinglePostPage_ReplyClick.send();
                switchHeaderTab(HeaderTab.REPLY_TAB);

            }
        };

        BarItem repostItem = new BarItem(R.drawable.post_action_repost) {

            @Override
            public void onPress() {
                mEmotionalGridWrapper.setVisibility(View.GONE);
                mMessageInputPanelWrapper.setVisibility(View.GONE);
                mRepostMessageWrapper.setVisibility(View.VISIBLE);
                GAEvent.SinglePostPage_RepostClick.send();
                switchHeaderTab(HeaderTab.RESHARE_TAB);
            }
        };

        BarItem emoteItem = new BarItem(R.drawable.ad_emotibot) {

            @Override
            public void onPress() {
                mEmotionalGridWrapper.setVisibility(View.VISIBLE);
                mMessageInputPanelWrapper.setVisibility(View.GONE);
                mRepostMessageWrapper.setVisibility(View.GONE);
                switchHeaderTab(HeaderTab.EMOTE_TAB);
            }
        };

        mHeaderBar.setItem(HeaderTab.REPLY_TAB.ordinal(), replyItem);
        mHeaderBar.setItem(HeaderTab.RESHARE_TAB.ordinal(), repostItem);
        mHeaderBar.setItem(HeaderTab.EMOTE_TAB.ordinal(), emoteItem);

        mStickyHeader.setItem(HeaderTab.REPLY_TAB.ordinal(), replyItem);
        mStickyHeader.setItem(HeaderTab.RESHARE_TAB.ordinal(), repostItem);
        mStickyHeader.setItem(HeaderTab.EMOTE_TAB.ordinal(), emoteItem);
        mStickyHeader.getBackground().setAlpha(PINNED_BACKGROUND_ALPHA);
        mStickyHeader.setVisibility(View.INVISIBLE);
    }

    @Override
    protected BaseAdapter createAdapter() {
        mReplyListAdapter = new ReplyListAdapter(getActivity(), spannableCache);
        mReplyListAdapter.setReplyItemListener(mPostViewListener);
        return mReplyListAdapter;
    }

    @Override
    protected View createFooterView() {
        View loadingView = createLoadingView();
        loadingView.setBackgroundColor(ApplicationEx.getColor(R.color.dark_gray_background));
        return loadingView;
    }

    private void addSpaceFooterView() {
        View footer = new View(ApplicationEx.getContext());
        ListView.LayoutParams layoutParams = new ListView.LayoutParams(1, ApplicationEx.getDimension(R.dimen.bottom_menu_bar_height));
        footer.setLayoutParams(layoutParams);
        mList.addFooterView(footer);
    }

    @Override
    protected View createHeaderView() {
        // create header
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View headerView = inflater.inflate(R.layout.header_single_post_page, null);
        headerView.setBackgroundColor(Theme.getColor(ThemeValues.LIGHT_BACKGROUND_COLOR));

        // reuse the PostViewHolder of the PostListFragment
        mPostItem = headerView.findViewById(R.id.post_container);

        mPostViewHolder = new PostViewHolder(getActivity(), mPostItem, spannableCache);
        mPostViewHolder.setWatchable(!mIsPostInGroup);
        mPostViewHolder.setBaseViewListener(mPostViewListener);
        mPostViewHolder.setForSinglePostPage();

        emptyViewContainer = (FrameLayout) headerView.findViewById(R.id.empty_view_container);

        mHeaderBar = (HeaderBar) headerView.findViewById(R.id.spp_actions_container);
        mEmoteGrid = (EmoticonGridView) headerView.findViewById(R.id.emote_grid);

        mEmoteGrid.setListener(new EmoticonGridView.OnItemClickListener() {

            @Override
            public void onItemClick(UserTagId data) {
                ActionHandler.getInstance().displayProfile(getActivity(), data.getUsername());
            }
        });
        return headerView;
    }

    @Override
    public void onResume() {
        super.onResume();

        refreshData(true);

        RetryConfig.getInstance().addRetryRequest(RequestTypeEnum.GET_POST);
        RetryConfig.getInstance().addRetryRequest(RequestTypeEnum.GET_REPLIES);
        RetryConfig.getInstance().addRetryRequest(RequestTypeEnum.GET_RESHARES);
    }

    @Override
    public void onPause() {
        super.onPause();

        RetryConfig.getInstance().removeRetryRequest(RequestTypeEnum.GET_POST);
        RetryConfig.getInstance().removeRetryRequest(RequestTypeEnum.GET_REPLIES);
        RetryConfig.getInstance().removeRetryRequest(RequestTypeEnum.GET_RESHARES);
    }

    private void refreshData(final boolean shouldForceFetch) {
        updateSinglePostData(shouldForceFetch);
        switch (mHeaderTab) {
            case REPLY_TAB:
                refreshReplies(shouldForceFetch);
                break;
            case RESHARE_TAB:
                refreshReshares(shouldForceFetch);
                break;
            case EMOTE_TAB:
                updateEmotes();
                break;
        }
    }

    private void updateSinglePostData(final boolean shouldForceFetch) {
        mPost = PostsDatastore.getInstance().getPost(mPostId, shouldForceFetch);
        if (mPost != null) {
            mPostViewHolder.setData(mPost);

            int replyCt = PostUtils.getRepliesCounter(mPost);
            int reshareCt = PostUtils.getResharesCounter(mPost);
            int emoteCt = PostUtils.getFootprintCounter(mPost);

            mHeaderBar.setCount(HeaderTab.REPLY_TAB.ordinal(), replyCt);
            mHeaderBar.setCount(HeaderTab.RESHARE_TAB.ordinal(), reshareCt);
            mHeaderBar.setCount(HeaderTab.EMOTE_TAB.ordinal(), emoteCt);

            mStickyHeader.setCount(HeaderTab.REPLY_TAB.ordinal(), replyCt);
            mStickyHeader.setCount(HeaderTab.RESHARE_TAB.ordinal(), reshareCt);
            mStickyHeader.setCount(HeaderTab.EMOTE_TAB.ordinal(), emoteCt);

            mReplyListAdapter.setReplyOrReshareToOwnPost(PostUtils.isMyPost(mPost));
        }
        else{
            //TODO: we need UX design to handle this abnormal condition with null post
            //TODO: Currently pass this null post for further try catch section
        }
    }

    private boolean isDisplayingReplies() {
        return mHeaderTab == HeaderTab.REPLY_TAB;
    }

    private boolean isDisplayingReshares() {
        return mHeaderTab == HeaderTab.RESHARE_TAB;
    }

    private void updateEmotes() {
        mReplyListAdapter.setRepliesOrReshares(null);
        mReplyListAdapter.notifyDataSetChanged();

        if (mPost != null) {
            TagEntity tagEntity = mPost.getTagEntity();
            if (tagEntity != null) {
                mEmoteGrid.setData(tagEntity);
            }
            resetEmptyView();
        }
    }

    private void refreshReplies(final boolean shouldForceFetch) {
        if (mPost != null) {
            if (isDisplayingReplies()) {
                List<Post> repliesList = PostsDatastore.getInstance().getRepliesForPostWithId(mPost.getId(), 0,
                        mReplyLoadLimit, shouldForceFetch, false);

                mReplyListAdapter.setRepliesOrReshares(repliesList);
                mReplyListAdapter.notifyDataSetChanged();
            }
            resetEmptyView();
        }
    }

    private void refreshReshares(final boolean shouldForceFetch) {
        if (mPost != null) {
            List<Post> resharesList = PostsDatastore.getInstance().getResharesForPostWithId(mPost.getId(), 0,
                    mReshareLoadLimit, shouldForceFetch, false);

            if (isDisplayingReshares()) {
                mReplyListAdapter.setRepliesOrReshares(resharesList);
                mReplyListAdapter.notifyDataSetChanged();
            }
            resetEmptyView();
        }
    }

    private void updateRepliesOnReceiveData() {
        if (mPost != null) {
            if (isDisplayingReplies()) {
                List<Post> repliesList = PostsDatastore.getInstance().getRepliesForPostWithId(mPost.getId());
                mReplyListAdapter.setRepliesOrReshares(repliesList);
                mReplyListAdapter.notifyDataSetChanged();
            }
            resetEmptyView();
        }
    }

    private void updateResharesOnReceiveData() {
        if (mPost != null) {
            List<Post> resharesList = PostsDatastore.getInstance().getResharesForPostWithId(mPost.getId());

            if (isDisplayingReshares()) {
                mReplyListAdapter.setRepliesOrReshares(resharesList);
                mReplyListAdapter.notifyDataSetChanged();
            }
            resetEmptyView();
        }
    }

    @Override
    protected void registerReceivers() {
        registerEvent(Events.Post.SINGLE_POST_RECEIVED);
        registerEvent(Events.Post.FETCH_REPLIES_COMPLETED);
        registerEvent(Events.Post.FETCH_RESHARES_COMPLETED);
        registerEvent(Events.Profile.RECEIVED);
        registerEvent(Events.Post.WATCHED);
        registerEvent(Events.Post.UNWATCHED);
        registerEvent(Events.Post.LOCKED);
        registerEvent(Events.Post.UNLOCKED);
        registerEvent(Events.Post.FETCH_TAG_OPTIONS_COMPLETED);
        registerEvent(Events.Post.TAGGED);
        registerEvent(Events.Post.DELETED);
        registerEvent(Events.Post.BEGIN_FETCH_FOR_CATEGORY);
        registerEvent(Events.Post.FETCH_FOR_CATEGORY_ERROR);
        registerEvent(Events.Post.SENT);
        registerEvent(AppEvents.NetworkService.ERROR);
        registerEvent(Events.Emoticon.RECEIVED);
        registerEvent(Events.Emoticon.BITMAP_FETCHED);
        registerEvent(Events.Application.FETCH_THIRD_PARTY_SETTINGS_COMPLETED);
        registerEvent(Events.MigStore.Item.PURCHASED);
        registerEvent(Events.MigStore.Item.PURCHASE_ERROR);
        registerEvent(Events.Profile.FETCH_MENTION_AUTOCOMPLETE_COMPLETED);
        registerEvent(Events.HotTopic.FETCH_ALL_COMPLETED);
        registerDataFetchedByMimeDataEvents();

    }

    @Override
    protected void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (action.equals(Events.Profile.RECEIVED)) {
            Bundle data = intent.getExtras();
            String username = data.getString(Events.User.Extra.USERNAME);
            if (mPost != null && username.equalsIgnoreCase(mPost.getAuthor().getUsername())) {
                setPullToRefreshComplete();
            }
        } else if (action.equals(Events.Post.WATCHED) || action.equals(Events.Post.UNWATCHED)) {
            String postId = intent.getStringExtra(Events.Post.Extra.ID);
            if (this.mPostId.equals(postId) && mPost != null) {
                updateSinglePostData(false);
                Tools.showToastForIntent(context, intent);
            }
        } else if (action.equals(Events.Post.LOCKED) || action.equals(Events.Post.UNLOCKED)) {
            String postId = intent.getStringExtra(Events.Post.Extra.ID);
            if (this.mPostId.equals(postId) && mPost != null) {
                updateSinglePostData(false);
                Tools.showToastForIntent(context, intent);
            }
        } else if (action.equals(Events.Post.SINGLE_POST_RECEIVED)) {
            refreshData(false);
            setPullToRefreshComplete();
        } else if (action.equals(Events.Post.FETCH_REPLIES_COMPLETED)) {
            String postId = intent.getStringExtra(Events.Post.Extra.ID);
            if (this.mPostId.equals(postId)) {
                mIsLoadingReplies = false;
                if (isDisplayingReplies()) {
                    hideLoadingMore();
                    updateRepliesOnReceiveData();
                    setPullToRefreshComplete();

                    int offset = intent.getIntExtra(Events.Misc.Extra.OFFSET, -1);
                    int limit = intent.getIntExtra(Events.Misc.Extra.LIMIT, -1);
                    mRepliesOffsetDelta = PostUtils.adjustPostsLoadingMoreOffset(
                            offset, limit, isRepliesEnd(), mReplyListAdapter.getCount());

                }

                if (mIsReplyOrReshare) {
                    autoAlignFirstMessage();
                }
            }
        } else if (action.equals(Events.Post.FETCH_RESHARES_COMPLETED)) {
            String postId = intent.getStringExtra(Events.Post.Extra.ID);
            if (this.mPostId.equals(postId)) {
                mIsLoadingReshares = false;
                if (isDisplayingReshares()) {
                    hideLoadingMore();
                    updateResharesOnReceiveData();
                    setPullToRefreshComplete();

                    int offset = intent.getIntExtra(Events.Misc.Extra.OFFSET, -1);
                    int limit = intent.getIntExtra(Events.Misc.Extra.LIMIT, -1);
                    mResharesOffsetDelta = PostUtils.adjustPostsLoadingMoreOffset(
                            offset, limit, isResharesEnd(), mReplyListAdapter.getCount());
                }

                if (mIsReplyOrReshare) {
                    autoAlignFirstMessage();
                }
            }
            setPullToRefreshComplete();

        } else if (action.equals(Events.Post.FETCH_TAG_OPTIONS_COMPLETED)) {
            updateEmotes();
        } else if (action.equals(Events.Post.TAGGED)) {
            String postId = intent.getStringExtra(Events.Post.Extra.ID);
            if (this.mPostId.equals(postId)) {
                updateSinglePostData(false);
                updateEmotes();
            }
        } else if (action.equals(Events.Post.DELETED)) {
            String postId = intent.getStringExtra(Events.Post.Extra.ID);
            if (postId != null && postId.equals(this.mPostId)) {
                Tools.showToastForIntent(context, intent);
                this.closeFragment();
            } else {
                mReplyListAdapter.removeReplyOrReshare(postId);
                Tools.showToastForIntent(context, intent);
            }
        } else if (action.equals(Events.Post.BEGIN_FETCH_FOR_CATEGORY)) {
            String postCategory = intent.getStringExtra(Events.Post.Extra.CATEGORY);
            if (postCategory.equals(PostCategoryTypeEnum.REPLIES.value())) {
                mIsLoadingReplies = true;
                resetEmptyView();
            } else if (postCategory.equals(PostCategoryTypeEnum.RESHARES.value())) {
                mIsLoadingReshares = true;
                resetEmptyView();
            }
        } else if (action.equals(Events.Post.FETCH_FOR_CATEGORY_ERROR)) {
            setHeaderItemVisible(true);
            String postCategory = intent.getStringExtra(Events.Post.Extra.CATEGORY);
            if (postCategory.equals(PostCategoryTypeEnum.REPLIES.value())) {
                mIsLoadingReplies = false;
                resetEmptyView();
                if (isDisplayingReplies()) {
                    hideLoadingMore();
                    setPullToRefreshComplete();
                }

            } else if (postCategory.equals(PostCategoryTypeEnum.RESHARES.value())) {
                mIsLoadingReshares = false;
                resetEmptyView();
                if (isDisplayingReshares()) {
                    hideLoadingMore();
                    setPullToRefreshComplete();
                }
            }
        } else if (action.equals(Events.Post.DELETE_ERROR)) {
            Tools.showToastForIntent(context, intent);
        } else if (action.equals(Events.Post.SENT)) {
            //refresh to see the new one just sent
            refreshReplies(true);
            refreshReshares(true);
        } else if (action.equals(AppEvents.NetworkService.ERROR)) {
            setHeaderItemVisible(true);
            setPullToRefreshComplete();
        } else if (action.equals(Events.Emoticon.RECEIVED) || action.equals(Events.Emoticon.BITMAP_FETCHED)) {
            updateSinglePostData(false);
            updateRepliesOnReceiveData();
            updateResharesOnReceiveData();
        }  else if (isCompleteDataForMimeDataFetched(action)) {
            updateSinglePostData(false);
            updateRepliesOnReceiveData();
            updateResharesOnReceiveData();
            refreshData(false);
        } else if (action.equals(Events.Application.FETCH_THIRD_PARTY_SETTINGS_COMPLETED)) {
            refreshThirdPartySitesSettings();
        } else if (action.equals(Events.MigStore.Item.PURCHASED)) {
            refreshData(true);
        } else if (action.equals(Events.MigStore.Item.PURCHASE_ERROR)) {
            Tools.showToastForIntent(context, intent);
        } else if (action.equals(Events.Profile.FETCH_MENTION_AUTOCOMPLETE_COMPLETED)) {
            refreshMentionList();
        } else if (action.equals(Events.HotTopic.FETCH_ALL_COMPLETED)) {
            refreshHotTopics();
        }
    }

    private void resetPrivacyDisplay() {
        if (postPrivacy == PostPrivacyEnum.AUTHOR_ONLY) {
            mPrivacyButton.setImageResource(R.drawable.ad_private_grey);
        } else if (postPrivacy == PostPrivacyEnum.FRIENDS) {
            mPrivacyButton.setImageResource(R.drawable.ad_userppl_grey);
        } else {
            mPrivacyButton.setImageResource(R.drawable.ad_public_grey);
        }
        mPopupMenu.setMarkerOn(replyPermission == ReplyPermissionEnum.EVERYONE);
    }

    private PopupMenu mPopupMenu;

    private void createPrivacyOptionsPopupMenu() {
        mPopupMenu = new PopupMenu(getActivity());
        mPopupMenu.setMenuOptions(getPrivacyMenuOptions());
        mPopupMenu.setPopupGravity(Gravity.RIGHT | Gravity.BOTTOM);
        mPopupMenu.setPopupMenuListener(this);
        mPopupMenu.setMarker(mPopupMenuMarker);
    }

    private List<MenuOption> getPrivacyMenuOptions() {
        List<MenuOption> menuItems = new ArrayList<MenuOption>();
        menuItems.add(new MenuOption(I18n.tr("Public"),
                R.drawable.ad_public_grey, R.id.action_privacy_public_clicked, MenuOption.MenuOptionType.SELECTABLE,
                (postPrivacy.value() == PostPrivacyEnum.EVERYONE.value()), false));
        menuItems.add(new MenuOption(I18n.tr("Friends"),
                R.drawable.ad_userppl_grey, R.id.action_privacy_friends_clicked, MenuOption.MenuOptionType.SELECTABLE,
                (postPrivacy.value() == PostPrivacyEnum.FRIENDS.value()), false));
        menuItems.add(new MenuOption(I18n.tr("Private"),
                R.drawable.ad_private_grey, R.id.action_privacy_private_clicked, MenuOption.MenuOptionType.SELECTABLE,
                (postPrivacy.value() == PostPrivacyEnum.AUTHOR_ONLY.value()), false));

        MenuOption allowRepliesOption = new MenuOption(I18n.tr("Allow replies"), R.drawable.ad_reply_white, new MenuAction() {

            @Override
            public void onAction(MenuOption option, boolean isSelected) {
                replyPermission = isSelected ? ReplyPermissionEnum.EVERYONE : ReplyPermissionEnum.NONE;
                resetPrivacyDisplay();
            }

        });
        allowRepliesOption.setMenuOptionType(MenuOption.MenuOptionType.CHECKABLE);
        allowRepliesOption.setChecked(replyPermission.value() == ReplyPermissionEnum.EVERYONE.value());
        allowRepliesOption.setDismissPopupOnClick(false);
        menuItems.add(allowRepliesOption);

        return menuItems;
    }

    private void setFacebook(boolean enable) {
        mFacebookButton.setTag(enable);
        mFacebookButton.setImageResource((enable) ? R.drawable.ad_facebook_blue : R.drawable.ad_facebook_grey);
    }

    private void setTwitter(boolean enable) {
        mTwitterButton.setTag(enable);
        mTwitterButton.setImageResource((enable) ? R.drawable.ad_twitter_blue : R.drawable.ad_twitter_grey);
    }

    private void refreshThirdPartySitesSettings() {
        if (mSelectedTabParam == HeaderTab.RESHARE_TAB) {
            ThirdPartySites thirdPartySitesStatus = ThirdPartySitesController.getInstance().getThirdPartySitesStatus();
            ThirdPartySites thirdPartySitesLinked = ThirdPartySitesController.getInstance().getThirdPartySitesLinked();

            if (thirdPartySitesStatus != null) {
                if (thirdPartySitesStatus.getFacebookStatus()) {
                    mFacebookButton.setVisibility(View.VISIBLE);
                    if (thirdPartySitesLinked != null) {
                        // Only turn on facebook if it was not explicitly turned off by the user.
                        if (!mIsFacebookTurnedOff) {
                            setFacebook(thirdPartySitesLinked.getFacebookStatus());
                        }
                    } else {
                        setFacebook(false);
                    }
                } else {
                    mFacebookButton.setVisibility(View.GONE);
                }

                if (thirdPartySitesStatus.getTwitterStatus()) {
                    mTwitterButton.setVisibility(View.VISIBLE);
                    if (thirdPartySitesLinked != null) {
                        // Only turn on facebook if it was not explicitly turned off by the user.
                        if (!mIsTwitterTurnedOff) {
                            setTwitter(thirdPartySitesLinked.getTwitterStatus());
                        }
                    } else {
                        setTwitter(false);
                    }
                } else {
                    mTwitterButton.setVisibility(View.GONE);
                }
            }
        }
    }


    private void showPrivacyOptionsPopupMenu() {
        mPopupMenu.setPopupAnchor(mPopupMenuMarker);
        mPopupMenu.showAtLocation(0,
                Config.getInstance().getScreenHeight() - mPopupMenu.mAnchorRect.top, true);
    }

    @Override
    public void onClick(View v) {

        int id = v.getId();
        switch (id) {
            case R.id.action_bar_spp:
                if (mPost != null) {
                    ActionHandler.getInstance().displayProfile(getActivity(), mPost.getAuthor().getUsername());
                }
                break;
            case R.id.privacy_button:
                showPrivacyOptionsPopupMenu();
                break;
            case R.id.twitter_button:
            {
                GAEvent.Miniblog_CreatePostShareTwitter.send();

                ThirdPartySites linkedSites = ThirdPartySitesController.getInstance().getThirdPartySitesLinked();
                if (linkedSites != null) {
                    if (linkedSites.getTwitterStatus()) {
                        if (mTwitterButton.getTag() != null) {
                            mIsTwitterTurnedOff = (Boolean) mTwitterButton.getTag();
                            setTwitter(!mIsTwitterTurnedOff);
                        }
                    } else {
                        ActionHandler.getInstance().displayBrowser(getActivity(), WebURL.URL_THIRD_PARTY_SITES_SETTINGS,
                                I18n.tr("Connect Apps"), R.drawable.ad_setting_white);

                    }
                }
                break;
            }
            case R.id.facebook_button:
            {
                GAEvent.Miniblog_CreatePostShareFacebook.send();

                ThirdPartySites linkedSites = ThirdPartySitesController.getInstance().getThirdPartySitesLinked();
                if (linkedSites != null) {
                    if (linkedSites.getFacebookStatus()) {
                        if (mFacebookButton.getTag() != null) {
                            mIsFacebookTurnedOff = (Boolean) mFacebookButton.getTag();
                            setFacebook(!mIsFacebookTurnedOff);
                        }
                    } else {
                        ActionHandler.getInstance().displayBrowser(getActivity(), WebURL.URL_THIRD_PARTY_SITES_SETTINGS,
                                I18n.tr("Connect Apps"), R.drawable.ad_setting_white);

                    }
                }
                break;
            }
            case R.id.post_button:
                if (isAllowRepost()) {
                    GAEvent.Miniblog_CreatePost.send();
                    Tools.showToast(getActivity(), I18n.tr("Sending"));
                    handleSendMessage(ShareboxActionType.REPOST);
                }
                break;
            case R.id.repost_input_field:
                if (isAllowRepost()) {
                    ActionHandler.getInstance().displaySharebox(getActivity(), ShareboxActionType.REPOST, mPostId, null, null, true);
                }
                break;
            case R.id.attach_emoticon_button:
                if (isAllowRepost()) {
                    ActionHandler.getInstance().displaySharebox(getActivity(), ShareboxActionType.REPOST,
                            mPostId, null, null, true, ShareboxFragment.ShareboxSubActionType.OPEN_EMOTICON_DRAWER);
                }
                break;
        }
    }

    private boolean isAllowRepost() {
        // When sharing, if it is your own post, bypass usual checks.
        boolean isMyPost = PostUtils.isMyPost(mPost);
        if (!isMyPost && !PostUtils.canPostBeReshared(mPost)) {
            Tools.showToast(getActivity(), I18n.tr("Private post."));
            return false;
        } else if (mIsPostInGroup) {
            Tools.showToast(getActivity(), I18n.tr("Oops. Group posts can't be reposted for now."));
            return false;
        } else {
            return true;
        }
    }

    private void handleShareAction() {
        if (isAllowRepost()) {
            ShareManager.sharePost(getActivity(), GAEvent.SinglePostPage_Share, mPost);
        }
    }

    private void switchHeaderTab(HeaderTab tab) {
        if (tab == null || tab == mHeaderTab) {
            return;
        }
        mSelectedTabParam = tab;
        mHeaderTab = tab;
        mHeaderBar.setSelectedIndex(tab.ordinal());
        mStickyHeader.setSelectedIndex(tab.ordinal());

        boolean isEmote   = (tab == HeaderTab.EMOTE_TAB);
        boolean isReply   = (tab == HeaderTab.REPLY_TAB);
        boolean isReshare = (tab == HeaderTab.RESHARE_TAB);

        mEmoteGrid.setVisibility(isEmote ? View.VISIBLE : View.GONE);

        if (isReply) {
            refreshReplies(false);
        }

        if (isReshare) {
            refreshThirdPartySitesSettings();
            refreshReshares(false);
        }

        if (isEmote) {
            updateEmotes();
        }
    }

    private void resetEmptyView() {
        boolean showEmptyMessage = false;
        switch (mHeaderTab) {
            case EMOTE_TAB:
                showEmptyMessage = mEmoteGrid.isEmpty();
                break;
            case REPLY_TAB:
            case RESHARE_TAB:
                showEmptyMessage = mReplyListAdapter.isEmpty();
                break;
        }

        if (showEmptyMessage) {
            emptyViewContainer.removeAllViews();
            emptyViewContainer.addView(createEmptyView());
            emptyViewContainer.setVisibility(View.VISIBLE);
        } else {
            emptyViewContainer.setVisibility(View.GONE);
        }
    }

    private String getEmptyText(HeaderTab tab) {
        switch (tab) {
            case EMOTE_TAB:
                return I18n.tr("Whatcha feeling? Add an emoticon.");
            case REPLY_TAB:
                return I18n.tr("Any thoughts? Be the first to reply.");
            case RESHARE_TAB:
                return I18n.tr("Like this post? Share it now.");
        }
        return Constants.BLANKSTR;
    }

    @Override
    public void onContextMenuItemClick(ContextMenuItem menuItem) {
        int id = menuItem.getId();
        switch (id) {
            case R.id.action_open_browser:
                String url = (String) menuItem.getData();
                ActionHandler.getInstance().displayBrowser(getActivity(), url);
                break;
        }
    }

    public void onEmotionalFootprintClicked(String postId, int tagId) {
        GAEvent.SinglePostPage_EmotionFootprint.send();
        PostsController.getInstance().requestTagPost(postId, tagId);
    }

    @Override
    protected void onListEndReached() {
        super.onListEndReached();


        if (isDisplayingReplies()) {
            if(!isRepliesEnd()) {
                showLoadingMore();
                loadMoreReplies();
            }
        } else if (isDisplayingReshares()) {
            if(!isResharesEnd()) {
                showLoadingMore();
                loadMoreReshares();
            }
        }

    }

    private boolean isRepliesEnd() {
        return PostsDatastore.getInstance().isRepliesForPostEnded(mPostId);
    }

    private boolean isResharesEnd() {
        return PostsDatastore.getInstance().isResharesForPostEnded(mPostId);
    }

    private void loadMoreReplies() {
        int offset = mReplyListAdapter.getCount() + mRepliesOffsetDelta;
        PostsDatastore.getInstance().getRepliesForPostWithId(mPostId, offset, LOAD_MORE_INCREMENT, true, false);
    }

    private void loadMoreReshares() {
        int offset = mReplyListAdapter.getCount() + mResharesOffsetDelta;
        PostsDatastore.getInstance().getResharesForPostWithId(mPostId, offset, LOAD_MORE_INCREMENT, true, false);
    }

    @Override
    public CustomActionBarConfig getActionBarConfig() {
        CustomActionBarConfig config = super.getActionBarConfig();
        config.setNavigationButtonState(NavigationButtonState.BACK);
        config.setShowOverflowButtonState(OverflowButtonState.POPUP);
        return config;
    }

    @Override
    protected String getTitle() {
        return I18n.tr("Post");
    }

    @Override
    protected int getTitleIcon() {
        return R.drawable.ad_feed_white;
    }

    @Override
    public ArrayList<MenuOption> getMenuOptions() {
        ArrayList<MenuOption> menuItems = new ArrayList<MenuOption>();

        if (mPost == null) {
            return menuItems;
        }

        MenuOption favourite = MenuUtils.createFavouritePost(getActivity(), mPostId, mPost.getIsWatching(), mIsPostInGroup);
        MenuOption share = new MenuOption(I18n.tr("Share"), R.drawable.ad_share_white, new MenuAction() {

            @Override
            public void onAction(MenuOption option, boolean isSelected) {
                handleShareAction();
            }
        });

        MenuOption report = null;
        if (!PostUtils.isMyPost(mPost)) {
            report = MenuUtils.createReportUser(getActivity(), PostUtils.getPostAuthorUsername(mPost));
        }

        MenuOption lock = null;
        if (PostUtils.isMyPost(mPost)) {
            lock = MenuUtils.createLockUnLockPost(getActivity(), mPostId, PostUtils.isPostLocked(mPost));
        }

        MenuOption delete = null;
        if (PostUtils.isMyPost(mPost) || Session.getInstance().isGlobalAdmin()) {
            delete = new MenuOption(I18n.tr("Delete post"), R.drawable.ad_delete_white, new MenuAction() {

                @Override
                public void onAction(MenuOption option, boolean isSelected) {
                    AlertHandler.getInstance().showDeletePostDialog(getActivity(), null, mPostId);
                }
            });
        }

        menuItems.add(favourite);
        menuItems.add(share);
        if (report != null) {
            menuItems.add(report);
        }
        if (lock != null) {
            menuItems.add(lock);
        }
        if (delete != null) {
            menuItems.add(delete);
        }

        return menuItems;
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        super.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);

        if (mHeaderBar != null && mStickyHeader != null) {
            int[] headerLoc = new int[2];
            mHeaderBar.getLocationOnScreen(headerLoc);
            int headerBarY = headerLoc[1];

            int[] stickyLoc = new int[2];
            mStickyHeader.getLocationOnScreen(stickyLoc);
            int stickyY = stickyLoc[1];

            if (headerBarY <= stickyY) {
                mStickyHeader.setVisibility(View.VISIBLE);
            } else {
                mStickyHeader.setVisibility(View.INVISIBLE);
            }
        }
    }

    private View createEmptyView() {
        if (shouldDisplayLoading() && !isShowingLoadingMore()) {
            mEmptyView = LayoutInflater.from(getActivity()).inflate(R.layout.empty_view_loading, null);
            ImageView loadingIcon = (ImageView) mEmptyView.findViewById(R.id.loading_icon);
            loadingIcon.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.loading_icon_rotate));
        } else {
            mEmptyView = LayoutInflater.from(getActivity()).inflate(R.layout.spp_empty_view_text, null);
            TextView emptyText = (TextView) mEmptyView;
            emptyText.setText(getEmptyText(mHeaderTab));
        }

        return mEmptyView;
    }

    private boolean shouldDisplayLoading() {
        if (mHeaderTab == HeaderTab.REPLY_TAB && mIsLoadingReplies) {
            return true;
        } else if(mHeaderTab == HeaderTab.RESHARE_TAB && mIsLoadingReshares) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onSendMessageButtonClick() {
        if (PostUtils.isPostLocked(mPost)) {
            Tools.showToast(getActivity(), I18n.tr("Post locked."));
        } else {
            handleSendMessage(ShareboxActionType.REPLY_POST);
            mChatField.getEditableText().clear();
        }
    }

    @Override
    public void onGiftIconClick() {
        String rootPostId = mPostId;
        Post rootPost = mPost.getRootPost();

        ArrayList<String> recipientList = new ArrayList<>();
        recipientList.add(mPost.getAuthor().getUsername());

        if (rootPost != null) {
            String rootPostAuthorName = mPost.getRootPost().getAuthor().getUsername();
            if (!recipientList.contains(rootPostAuthorName)) {
                recipientList.add(rootPostAuthorName);
            }
            rootPostId = mPost.getRootPost().getId();
        }

        ViewGroup contentViewGroup = (ViewGroup) mPostItem.getTag(R.id.content_views_container);
        if (contentViewGroup != null) {
            addMentionedPeopleToRecipientList(recipientList, contentViewGroup);
        }
        ViewGroup rootContentViewGroup = (ViewGroup) mPostItem.getTag(R.id.root_content_views_container);
        if (rootContentViewGroup != null) {
            addMentionedPeopleToRecipientList(recipientList, rootContentViewGroup);
        }

        ActionHandler.getInstance().displaySendGiftFragment(getActivity(), recipientList, SendGiftFragment.ActionType.GIFT_TO_POST, rootPostId, mPostId);
    }

    private void addMentionedPeopleToRecipientList(List<String> recipientList, ViewGroup contentViewGroup) {
        for (int i=0; i<contentViewGroup.getChildCount(); i++) {
            View childView = contentViewGroup.getChildAt(i);
            if (childView instanceof TextViewEx) {
                String fullText = ((TextViewEx) childView).getFullText();
                Matcher m = SpannableBuilder.MENTIONS_PATTERN.matcher(fullText);
                while (m.find()) {
                    String username = m.group(0).substring(1);
                    if (!recipientList.contains(username)) {
                        recipientList.add(username);
                    }
                }
            }
        }
    }

    @Override
    public void onStickerSelect(Sticker sticker) {

    }

    @Override
    public void onPhotoClick(byte[] photo) {

    }

    @Override
    public void onEmotionSelectionShown() {

    }

    @Override
    public void onEmotionSelectionHidden() {

    }

    @Override
    public void onStickerSelectionShown() {

    }

    @Override
    public void onStickerSelectionHidden() {

    }

    @Override
    public void onKeyboardShown() {

    }

    @Override
    public void onKeyboardHidden() {

    }

    private LocationListItem selectedLocationListItem = null;
    private PostPrivacyEnum postPrivacy = PostPrivacyEnum.EVERYONE;
    private ReplyPermissionEnum replyPermission = ReplyPermissionEnum.EVERYONE;

    private void handleSendMessage(ShareboxActionType action) {
        boolean postToTwitter = false;
        boolean postToFacebook = false;

        if (mTwitterButton.getTag() != null) {
            postToTwitter = (Boolean) mTwitterButton.getTag();
        }

        if (mFacebookButton.getTag() != null) {
            postToFacebook = (Boolean) mFacebookButton.getTag();
        }

        String body = mChatField.getText().toString();
        GAEvent.Miniblog_CreatePost.send();
        Tools.showToast(getActivity(), I18n.tr("Sending"));

        Post post = PostsDatastore.getInstance().getPost(mPostId, false);

        String parentId = null;
        String rootId = null;
        PostOriginalityEnum originality = PostOriginalityEnum.ORIGINAL;
        final com.projectgoth.b.data.Location location = LocationUtils.makeLocationFromLocationListItem(selectedLocationListItem);

        Boolean showInFeeds = false;

        if (post != null) {
            originality = post.getOriginality();
            if (originality == PostOriginalityEnum.ORIGINAL) {
                parentId = post.getId();
                rootId = post.getId();
                // replyToRoot = false;
            } else if (originality == PostOriginalityEnum.REPLY || originality == PostOriginalityEnum.RESHARE) {
                parentId = post.getId();
                if (post.getRootPost() != null) {
                    rootId = post.getRootPost().getId();
                }
            }
        }
        //always send text message without photo
        if (action == ShareboxActionType.REPLY_POST || action == ShareboxActionType.REPLY_POST_IN_GROUP) {
            PostsDatastore.getInstance().replyPost(body, null, parentId, rootId, true, showInFeeds,
                    replyPermission, postPrivacy, location);
        } else if (action == ShareboxActionType.REPOST) {
            PostsDatastore.getInstance().resharePost(body, parentId, rootId, postToFacebook, postToTwitter,
                    replyPermission, postPrivacy, location);
        }
    }

    private void showMentionList(PopupListFilter.ListType listType) {
        if (mPopupListFilter != null) {
            //remove the old one
            mMainContainer.removeView(mPopupListFilter);
        }
        //create it
        mPopupListFilter = new PopupListFilter(getActivity());
        mPopupListFilter.setListener(this);
        //add it
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mMainContainer.addView(mPopupListFilter, params);
        mMessageInputPanelWrapper.setVisibility(View.GONE);
        mEmotionalGridWrapper.setVisibility(View.GONE);
        mPopupListFilter.requestInputFocus();

        if (listType == PopupListFilter.ListType.mentionList) {
            refreshMentionList();
        } else {
            refreshHotTopics();
        }
    }

    private void hideMentionList() {
        mMainContainer.removeView(mPopupListFilter);
        mMessageInputPanelWrapper.setVisibility(View.VISIBLE);
        mPopupListFilter = null;
        showKeyboardForReply();
    }

    @Override
    public void onPopupListItemSelected(String selectedItemText, PopupListFilter.ListType listType) {
        if (!TextUtils.isEmpty(selectedItemText)) {
            mChatField.append(selectedItemText + Constants.SPACESTR);
        }
        hideMentionList();
    }


    private void refreshMentionList() {
        ArrayList<String> usernameList =
                SystemDatastore.getInstance().getMentions(Session.getInstance().getUserId());

        //add post author
        String authorUsername = PostUtils.getPostAuthorUsername(mPost);
        if(!TextUtils.isEmpty(authorUsername)) {
            if (!usernameList.contains(authorUsername))
                usernameList.add(authorUsername);
        }

        if (mPopupListFilter != null) {
            mPopupListFilter.refreshList(usernameList, PopupListFilter.ListType.mentionList);
        }
    }

    private void refreshHotTopics() {
        HotTopicsResult result = PostsDatastore.getInstance().getHotTopics(HOT_TOPICS_LIMIT);

        if (result != null) {
            List<HotTopic> hotTopics = Arrays.asList(result.getResult());
            List<String> resultList = new ArrayList<String>();

            for (HotTopic topic : hotTopics) {
                String topicName = topic.getName();
                if (!topicName.startsWith(Constants.HASH_TAG)) {
                    topicName = Constants.HASH_TAG + topicName;
                }
                resultList.add(topicName);
            }

            if (mPopupListFilter != null) {
                mPopupListFilter.refreshList(resultList, PopupListFilter.ListType.hotTopicList);
            }
        }
    }

    private void showKeyboardForReply() {
        mChatField.requestFocus();
        Tools.showVirtualKeyboard(getActivity(), mChatField);
    }

    private void autoAlignFirstMessage() {
        if (!mIsFirstMessageAligned && mIsReplyOrReshare) {
            setHeaderItemVisible(true);
            mList.setSelectionFromTop(2, mStickyHeader.getHeight());
            mIsFirstMessageAligned = true;
        }
    }

    private void setHeaderItemVisible(boolean isVisible) {
        if (isVisible) {
            mPostItem.setVisibility(View.VISIBLE);
        } else {
            mPostItem.setVisibility(View.GONE);
        }
        setPullToRefreshEnabled(isVisible);
    }

}