
package com.projectgoth.ui.fragment;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mig33.diggle.events.Events;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.Banner;
import com.projectgoth.b.data.Post;
import com.projectgoth.b.data.Profile;
import com.projectgoth.b.enums.EveryoneOrFollowerAndFriendPrivacyEnum;
import com.projectgoth.common.Constants;
import com.projectgoth.common.Logger;
import com.projectgoth.common.Tools;
import com.projectgoth.controller.BannerController;
import com.projectgoth.controller.BannerController.Placement;
import com.projectgoth.controller.ProgressDialogController;
import com.projectgoth.datastore.PostsDatastore;
import com.projectgoth.datastore.Session;
import com.projectgoth.datastore.UserDatastore;
import com.projectgoth.enums.PostListType;
import com.projectgoth.events.AppEvents;
import com.projectgoth.events.GAEvent;
import com.projectgoth.i18n.I18n;
import com.projectgoth.imagefetcher.UIUtils;
import com.projectgoth.listener.SectionUpdateListener;
import com.projectgoth.music.deezer.DeezerPlayerManager;
import com.projectgoth.nemesis.enums.PostCategoryTypeEnum;
import com.projectgoth.nemesis.enums.RequestTypeEnum;
import com.projectgoth.nemesis.utils.RetryConfig;
import com.projectgoth.ui.UrlHandler;
import com.projectgoth.ui.activity.ActionHandler;
import com.projectgoth.ui.activity.CustomActionBarConfig;
import com.projectgoth.ui.activity.CustomActionBarConfig.NavigationButtonState;
import com.projectgoth.ui.activity.CustomActionBarConfig.OverflowButtonState;
import com.projectgoth.ui.adapter.PostListAdapter;
import com.projectgoth.ui.adapter.ProfilePagerAdapter.HeaderPlaceHolderInterface;
import com.projectgoth.ui.fragment.ShareboxFragment.ShareboxActionType;
import com.projectgoth.ui.fragment.ShareboxFragment.ShareboxSubActionType;
import com.projectgoth.ui.widget.ImageViewEx;
import com.projectgoth.ui.widget.allaccessbutton.ContextAction;
import com.projectgoth.ui.widget.allaccessbutton.ContextActionListener;
import com.projectgoth.ui.widget.allaccessbutton.PageData;
import com.projectgoth.util.AndroidLogger;
import com.projectgoth.util.PostUtils;

/**
 * PostListFragment.java
 * 
 * @author dangui
 */

public class PostListFragment extends BaseListFragment implements
        OnClickListener, ContextActionListener {

    private static final String   LOG_TAG                   = AndroidLogger.makeLogTag(PostListFragment.class);

    private final PageData        pageData                  = createPageData();

    private static final int      TEXT_POST                 = 0;
    private static final int      PHOTO_POST                = 1;
    private static final int      SEARCH                    = 2;

    // default increment of list items load more function
    private static final int      LOAD_MORE_INCREMENT       = 15;

    public static final String    PARAM_POST_LIST_TYPE      = "POST_LIST_TYPE";
    public static final String    PARAM_REQUESTING_USERID   = "PARAM_REQUESTING_USERID";
    public static final String    PARAM_REQUESTING_USERNAME = "PARAM_REQUESTING_USERNAME";
    public static final String    PARAM_SEARCH_STRING       = "PARAM_SEARCH_STRING";
    public static final String    PARAM_GROUP_ID            = "GROUP_ID";
    public static final String    PARAM_NUM_POSTS           = "NUM_POSTS";
    public static final String    PARAM_FEED_PRIVACY        = "FEED_PRIVACY";

    private View                  mEmptyView;
    private int                   mTopMarginOfEmptyView;
    private PostListAdapter       mPostListAdapter;
    private PostListType          mType                     = PostListType.HOME_FEEDS;
    private String                mUserId;
    private String                mUserName;
    private boolean               isVerified;
    private boolean               isSelf                    = true;
    protected String              groupId;
    private String                mSearchString;
    private int                   numPosts;
    private int                   feedPrivacy;

    private int                   mPostsLoadLimit = LOAD_MORE_INCREMENT;
    private int                   mOffsetDelta = 0;

    private SectionUpdateListener sectionUpdateListener;

    private View                  bannerContainer;
    private ImageViewEx           bannerImage;
    private boolean               isBannerEnabled;
    
    private boolean               isShowBanner;

    private Banner                banner;

    private final static int      DEFAULT_CLEAR_SHAREBOX_DELAY = 500;
    private final static int      REFRESH_LIST_DELAY = 500;

    
    private boolean               hasHeaderPlaceHolder;
    
    private View                  headerPlaceholder;
    private int                   firstStopIndex            = -1;
    private int                   lastStopIndex             = -1;
    private String                deezerIdPrefix            = "post-";
  //@formatter:off
    private HeaderPlaceHolderInterface headerImplementation = new HeaderPlaceHolderInterface() {
        
        @Override
        public void updatePlaceholderHeader(int height) {
            if (headerPlaceholder != null) {
                int placeHolderHeight = height - getListDividerHeight();
                if(headerPlaceholder.getMeasuredHeight() != placeHolderHeight) {
                    ListView.LayoutParams layoutParams = new ListView.LayoutParams(1, placeHolderHeight);
                    headerPlaceholder.setLayoutParams(layoutParams);
                }
            }
            mTopMarginOfEmptyView = height;
            /* 20150108 freddie.w:
               sometimes showOrHideEmptyViewIfNeeded was called
               before headerPlaceholder has measured height, also assign margin here again
            */
            changeEmptyViewMargins(0, mTopMarginOfEmptyView, 0, 0);
        }
        
        @Override
        public View getHeaderPlaceholder() {
            headerPlaceholder.setTag(getListDividerHeight());
            return headerPlaceholder;
        }
        
        @Override
        public void adjustScroll(int minTranslation, final int scrollHeight) {
//            Logger.info.log("ProfileFragment", "LIST: FirstVisiblePosition: " , mList.getFirstVisiblePosition(), " scrollHeight: ", scrollHeight);
//            if (scrollHeight == minTranslation && mList.getFirstVisiblePosition() >= 1) {
//                return;
//            }
//            // TODO: setSelectionFromTop does not work properly 
//            // for the pull to refresh list view custom view, handle it properly 
//            mList.setSelectionFromTop(1, scrollHeight);
        }
    };

    private int getListDividerHeight() {
        if (mList != null) {
            return mList.getDividerHeight();
        }
        return 0;
    }
    //@formatter:on
    
    @Override
    protected void readBundleArguments(Bundle args) {
        super.readBundleArguments(args);
        int typeId = args.getInt(PARAM_POST_LIST_TYPE);
        groupId = args.getString(PARAM_GROUP_ID);
        mType = PostListType.fromId(typeId);
        mSearchString = args.getString(PARAM_SEARCH_STRING);

        mUserId = args.getString(PARAM_REQUESTING_USERID);
        if (TextUtils.isEmpty(mUserId)) {
            mUserId = Session.getInstance().getUserId();
        }
        mUserName = args.getString(PARAM_REQUESTING_USERNAME);
        if (TextUtils.isEmpty(mUserName)) {
            mUserName = Session.getInstance().getUsername();
        }
        isSelf = Session.getInstance().isSelfByUserId(mUserId);

        numPosts = args.getInt(PARAM_NUM_POSTS);
        feedPrivacy = args.getInt(PARAM_FEED_PRIVACY);
        
        isBannerEnabled = mType == PostListType.HOME_FEEDS;
    }

    @Override
    public void onRefresh() {
        if(Session.getInstance().isNetworkConnected()) {
            mPostsLoadLimit = LOAD_MORE_INCREMENT;
            refreshData(true);
        } else {
            setRefreshDone();
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // for the background of the list container to be seen
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mPullList.getLayoutParams();
        int postListMargin = ApplicationEx.getDimension(R.dimen.post_list_margin);
        params.setMargins(postListMargin, 0, postListMargin, 0);
        mPullList.setLayoutParams(params);
        
        int height = ApplicationEx.getDimension(R.dimen.post_list_divider_height);
        mList.setDividerHeight(height);

        if (isBannerEnabled) {
            isShowBanner = true;
            updateBanner();
        }
        
        invokeOnViewCreated();
        updateVerify();
    }
    
    @Override
    public void onClick(View v) {

    }
    
    public void setSectionUpdateListener(SectionUpdateListener listener) {
        this.sectionUpdateListener = listener;
    }

    @Override
    protected BaseAdapter createAdapter() {
        mPostListAdapter = new PostListAdapter(getActivity(), mType);
        return mPostListAdapter;
    }
    
    @Override
    protected View createHeaderView() {
        if (hasHeaderPlaceHolder) {
            mIsHeaderEnabled = true;
            headerPlaceholder = new View(ApplicationEx.getContext());
            return headerPlaceholder;
        } else if (isBannerEnabled) {
            mIsHeaderEnabled = true;
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            final View header = inflater.inflate(R.layout.header_banner_layout, mList, false);

            bannerContainer = header.findViewById(R.id.container);
            bannerImage = (ImageViewEx) header.findViewById(R.id.banner_image);
            bannerImage.setBorder(false);
            bannerImage.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (banner != null) {
                        GAEvent.Miniblog_DiscoverBanner.send(banner.getImageUrl());
                        //TODO (NON-LOGIN) FOR LOGIN DIALOG ENTRY.
                        UrlHandler.displayUrl(getActivity(), banner.getUrl());
                    }

                }
            });

            final View close_button = header.findViewById(R.id.close_btn);
            close_button.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    isShowBanner = false;
                    if (bannerContainer != null) {
                        bannerContainer.setVisibility(View.GONE);
                        changeEmptyViewMargins(0, 0, 0, 0);
                    }
                }
            });

            return header;
        }
        return null;
    }
    
    private void updateBanner() {
        if (!isBannerEnabled) {
            return;
        }
        
        if (isShowBanner) {
            banner = BannerController.getInstance().getBanner(Placement.HOMEFEEDS);
            if (banner != null) {
                BannerController.setBannerIntoImage(banner, bannerImage);
                if (bannerContainer != null) {
                    bannerContainer.setVisibility(View.VISIBLE);
                }
            } else {
                if (bannerContainer != null) {
                    bannerContainer.setVisibility(View.GONE);
                }
            }
        } else {
            if (bannerContainer != null) {
                bannerContainer.setVisibility(View.GONE);
            }
        }
    }

    protected void refreshData(final boolean shouldForceFetch) {
        List<Post> postList = null;

        boolean justLoadFromCache;

        if (mList.getFirstVisiblePosition() >= LOAD_MORE_INCREMENT && !shouldForceFetch) {
            //if user has scrolled down and viewing an old post, do not fetch new posts when coming back (eg. go to spp and back),
            //otherwise the list jumps when new posts arrived then user needs to scroll down again and again to continue reading
            justLoadFromCache = true;
        } else {
            justLoadFromCache = false;
            // if user is on first page of post list, just refresh the first page, prevent it from loading
            // too many posts together
            mPostsLoadLimit = LOAD_MORE_INCREMENT;
        }

        switch (mType) {
            case HOME_FEEDS:
            {
                postList = PostsDatastore.getInstance().getFeedsPostsList(0, mPostsLoadLimit, shouldForceFetch, justLoadFromCache);
                updateVerify();
                updateBanner();
                break;
            }
            case MENTION_LIST:
            {
                postList = PostsDatastore.getInstance().getMentionsPostsList(0, mPostsLoadLimit, shouldForceFetch
                        , justLoadFromCache);
                break;
            }
            case TOPIC_POSTS:
            {
                if (!TextUtils.isEmpty(mSearchString)) {
                    postList = PostsDatastore.getInstance().getPostsFromTopic(mSearchString, 0, mPostsLoadLimit,
                            shouldForceFetch, justLoadFromCache);
                }
                break;
            }
            case WATCHED_POSTS:
            {
                postList = PostsDatastore.getInstance().getWatchedPostsList(0, mPostsLoadLimit, shouldForceFetch, justLoadFromCache);
                break;
            }
            case GROUP_POSTS:
            {
                postList = PostsDatastore.getInstance().getGroupFeedsPostsList(groupId, 0, mPostsLoadLimit,
                        shouldForceFetch, justLoadFromCache);
                break;
            }
            case PROFILE_POSTS:
            {
                postList = PostsDatastore.getInstance().getUserPostsList(mUserId, 0, mPostsLoadLimit,
                        shouldForceFetch, justLoadFromCache);
                break;
            }
            case SEARCH_POSTS:
            {
                if (!TextUtils.isEmpty(mSearchString)) {
                    postList = PostsDatastore.getInstance().getPostsFromSearch(mSearchString,  0, mPostsLoadLimit,
                            shouldForceFetch, justLoadFromCache);
                }
                break;
            }
        }

        if (postList != null) {
            if (sectionUpdateListener != null) {
                sectionUpdateListener.setSectionCount(postList.size());
            }
            mPostListAdapter.setPostList(postList);
            mPostsLoadLimit = postList.size();
        }
        mPostListAdapter.notifyDataSetChanged();
    }

    protected void loadMorePosts() {

        //the offset should be existing post number excluding promoted posts
        int offset = mPostListAdapter.getPostNum(true) + mOffsetDelta;

        switch (mType) {
            case HOME_FEEDS: {
                PostsDatastore.getInstance().getFeedsPostsList(offset, LOAD_MORE_INCREMENT, true, false);
                break;
            }
            case MENTION_LIST:
            {
                PostsDatastore.getInstance().getMentionsPostsList(offset, LOAD_MORE_INCREMENT, true, false);
                break;
            }
            case TOPIC_POSTS:
            {
                if (!TextUtils.isEmpty(mSearchString)) {
                    PostsDatastore.getInstance().getPostsFromTopic(mSearchString, offset, LOAD_MORE_INCREMENT, true, false);
                }
                break;
            }
            case WATCHED_POSTS:
            {
                PostsDatastore.getInstance().getWatchedPostsList(offset, LOAD_MORE_INCREMENT, true, false);
                break;
            }
            case GROUP_POSTS:
            {
                PostsDatastore.getInstance().getGroupFeedsPostsList(groupId, offset, LOAD_MORE_INCREMENT, true, false);
                break;
            }
            case PROFILE_POSTS:
            {
                PostsDatastore.getInstance().getUserPostsList(mUserId, offset, LOAD_MORE_INCREMENT, true, false);
                break;
            }
            case SEARCH_POSTS:
            {
                if (!TextUtils.isEmpty(mSearchString)) {
                    PostsDatastore.getInstance().getPostsFromSearch(mSearchString, offset, LOAD_MORE_INCREMENT, true, false);
                }
                break;
            }
        }
    }

    /**
     *   just show the results of posts from cache to  prevent it from fetching posts again caused by
     *   the fetch posts offset issue when receiving the any event to show the updated posts.
     *
     * */
    protected void showPostsForCategory() {
        List<Post> postList = null;
        switch (mType) {
            case HOME_FEEDS:
            {
                postList = PostsDatastore.getInstance().getFeedsPostsList();
                break;
            }
            case MENTION_LIST:
            {
                postList = PostsDatastore.getInstance().getMentionsPostsList();
                break;
            }
            case TOPIC_POSTS:
            {
                if (!TextUtils.isEmpty(mSearchString)) {
                    postList = PostsDatastore.getInstance().getPostsFromTopic(mSearchString);
                }
                break;
            }
            case WATCHED_POSTS:
            {
                postList = PostsDatastore.getInstance().getWatchedPostsList();
                break;
            }
            case GROUP_POSTS:
            {
                postList = PostsDatastore.getInstance().getGroupFeedsPostsList(groupId);
                break;
            }
            case PROFILE_POSTS:
            {
                postList = PostsDatastore.getInstance().getUserPostsList(mUserId);
                break;
            }
            case SEARCH_POSTS:
            {
                if (!TextUtils.isEmpty(mSearchString)) {
                    postList = PostsDatastore.getInstance().getPostsFromSearch(mSearchString);
                }
                break;
            }
        }

        if (postList != null) {
            if (sectionUpdateListener != null) {
                sectionUpdateListener.setSectionCount(postList.size());
            }
            mPostListAdapter.setPostList(postList);
            mPostsLoadLimit = postList.size();
        }
        mPostListAdapter.notifyDataSetChanged();
    }

    @Override
    protected void updateListData() {
        refreshData(false);
        showOrHideEmptyViewIfNeeded();
    }

    private void updateListOnDataChanged() {
        // just show results of posts rather than calling refreshData
        showPostsForCategory();
        showOrHideEmptyViewIfNeeded();
    }

    private boolean isRefreshTaskScheduled = false;
    private void refreshPostListDelay() {
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
                        updateListOnDataChanged();
                    }
                });
            }
        }
    };

    @Override
    protected void registerReceivers() {
        switch (mType) {
            case HOME_FEEDS:
                registerEvent(Events.Post.FETCH_HOMEFEEDS_COMPLETED);
                registerEvent(Events.Post.SENT);
                registerEvent(Events.Post.DRAFT_CREATED);
                registerEvent(Events.Post.SEND_ERROR);
                registerEvent(AppEvents.NetworkService.DISCONNECTED);
                registerEvent(AppEvents.NetworkService.STARTED);
                registerEvent(Events.Post.DELETED);
                break;
            case MENTION_LIST:
                registerEvent(Events.Post.FETCH_MENTIONS_COMPLETED);
                break;
            case TOPIC_POSTS:
                registerEvent(Events.Post.FETCH_FOR_HOTTOPIC_COMPLETED);
                registerEvent(Events.Post.FETCH_FOR_SEARCH_ERROR);
                break;
            case WATCHED_POSTS:
                registerEvent(Events.Post.FETCH_WATCHLIST_COMPLETED);
                break;
            case GROUP_POSTS:
                registerEvent(Events.Post.FETCH_GROUP_FEEDS_COMPLETED);
                break;
            case PROFILE_POSTS:
                registerEvent(Events.Post.FETCH_USERPOSTS_COMPLETED);
                break;
            case SEARCH_POSTS:
                registerEvent(Events.Post.FETCH_FOR_SEARCH_COMPLETED);
                registerEvent(Events.Post.FETCH_FOR_SEARCH_ERROR);
                break;
        }
        registerEvent(Events.Post.BEGIN_FETCH_FOR_CATEGORY);
        registerEvent(Events.Post.WATCHED);
        registerEvent(Events.Post.UNWATCHED);
        registerEvent(Events.Post.TAGGED);
        registerEvent(Events.Post.FETCH_FOR_CATEGORY_ERROR);
        registerEvent(AppEvents.NetworkService.ERROR);
        if (isBannerEnabled) {
            registerEvent(Events.Banner.FETCH_COMPLETED);
            registerEvent(Events.Banner.FETCH_ERROR);
            registerEvent(Events.Banner.SWITCH_BANNER);
        }
        registerEvent(Events.Emoticon.RECEIVED);
        registerEvent(Events.Emoticon.BITMAP_FETCHED);
        registerEvent(Events.Post.FOOTPRINT_BMP_FETCHED);
        registerDataFetchedByMimeDataEvents();
    }

    @Override
    protected void onReceive(Context context, Intent intent) {

        final String action = intent.getAction();

        if ((action.equals(Events.Post.FETCH_HOMEFEEDS_COMPLETED) && mType == PostListType.HOME_FEEDS)
                || (action.equals(Events.Post.FETCH_MENTIONS_COMPLETED) && mType == PostListType.MENTION_LIST)
                || (action.equals(Events.Post.FETCH_WATCHLIST_COMPLETED) && mType == PostListType.WATCHED_POSTS)) {
            setLoadingData(false);
            updateListOnDataChanged();
            setRefreshDone();

            if (mType == PostListType.HOME_FEEDS) {
                int offset = intent.getIntExtra(Events.Misc.Extra.OFFSET, -1);
                int limit = intent.getIntExtra(Events.Misc.Extra.LIMIT, -1);
                mOffsetDelta = PostUtils.adjustPostsLoadingMoreOffset(
                        offset, limit, isPostCategoryEnd(), mPostListAdapter.getPostNum(true));
            }

        } else if (action.equals(Events.Post.FETCH_GROUP_FEEDS_COMPLETED) && mType == PostListType.GROUP_POSTS) {
            String groupId = intent.getStringExtra(Events.Group.Extra.ID);
            if (groupId.equals(this.groupId)) {
                setLoadingData(false);
                updateListOnDataChanged();
                setRefreshDone();
            }
        } else if (action.equals(Events.Post.FETCH_USERPOSTS_COMPLETED) && mType == PostListType.PROFILE_POSTS) {
            String userId = intent.getStringExtra(Events.User.Extra.ID);
            if (mUserId.equals(userId)) {
                setLoadingData(false);
                updateListOnDataChanged();
                setRefreshDone();
            }
        } else if ((action.equals(Events.Post.FETCH_FOR_SEARCH_COMPLETED) && mType == PostListType.SEARCH_POSTS)
                || (action.equals(Events.Post.FETCH_FOR_HOTTOPIC_COMPLETED) && mType == PostListType.TOPIC_POSTS)) {
            ProgressDialogController.getInstance().hideProgressDialog();
            String searchParam = intent.getStringExtra(Events.Misc.Extra.SEARCH_QUERY);
            Logger.debug.log(LOG_TAG, "onReceive: SearchResult: Query: ", searchParam, ", origQuery: ", mSearchString);
            if (mSearchString.equals(searchParam)) {
                if (sectionUpdateListener != null) {
                    final int totalHitCount = intent.getIntExtra(Events.Misc.Extra.TOTAL_SEARCH_HITS, 0);
                    sectionUpdateListener.setSectionCount(totalHitCount);
                }
                setLoadingData(false);
                updateListOnDataChanged();
                setRefreshDone();
            }
        } else if (action.equals(Events.Post.TAGGED)) {
            updateListOnDataChanged();
        } else if (action.equals(Events.Post.WATCHED) || action.equals(Events.Post.UNWATCHED)) {
            updateListOnDataChanged();
            Tools.showToastForIntent(getActivity(), intent);
        } else if (action.equals(Events.Post.BEGIN_FETCH_FOR_CATEGORY)) {
            String postCategory = intent.getStringExtra(Events.Post.Extra.CATEGORY);
            if (isPostCategoryForThisPostList(postCategory)) {
                setLoadingData(true, true);
            }
        } else if (action.equals(Events.Post.FETCH_FOR_CATEGORY_ERROR)) {
            String postCategory = intent.getStringExtra(Events.Post.Extra.CATEGORY);
            if (isPostCategoryForThisPostList(postCategory)) {
                setLoadingData(false, true);
                setRefreshDone();
            }
            // for AD-1467 to track behaviour of v2.3.xxx
            if (UIUtils.hasICS()) {
                Tools.showToastForIntent(context, intent);
            } else {
                String message = intent.getStringExtra(AppEvents.Misc.Extra.FORMATTED_MESSAGE);
                Logger.error.log("FETCH_FOR_CATEGORY_ERROR", message);
            }

        } else if (action.equals(Events.Post.FETCH_FOR_SEARCH_ERROR)) {
            ProgressDialogController.getInstance().hideProgressDialog();
            Tools.showToastForIntent(context, intent);
            setLoadingData(false, true);
            setRefreshDone();
        } else if (action.equals(Events.Banner.FETCH_COMPLETED) || action.equals(Events.Banner.SWITCH_BANNER)) {
            updateBanner();
        } else if (action.equals(Events.Banner.FETCH_ERROR)) {
            Tools.showToastForIntent(context, intent);
        } else if (action.equals(Events.Profile.RECEIVED)) {
            String username = intent.getStringExtra(Events.User.Extra.USERNAME);
            if (mType == PostListType.HOME_FEEDS ) {
                updateVerify();
            }

            if (this.mUserName != null && this.mUserName.equalsIgnoreCase(username)) {
                //check getTitle , only WATCHED_POSTS needs this now
                if (mType == PostListType.WATCHED_POSTS ) {
                    updateActionBar(getActivity());
                }
            }
            //refresh the list when receiving profile shared in post
            refreshPostListDelay();
        } else if (action.equals(AppEvents.NetworkService.DISCONNECTED)) {
            updateListOnDataChanged();
            setRefreshDone();
        } else if (action.equals(AppEvents.NetworkService.STARTED)) {
        } else if (action.equals(Events.Post.SENT)) {
            onRefresh();
        } else if (action.equals(Events.Post.DRAFT_CREATED)) {
            //String draftId = intent.getStringExtra(Events.Post.Extra.ID);
            //Tools.showToast(getActivity(), I18n .tr("DRAFT_CREATED MADE") + draftId);
            if(!Session.getInstance().isNetworkConnected()) {
                updateListOnDataChanged();
                setRefreshDone();
            }
        } else if (action.equals(AppEvents.NetworkService.ERROR) || action.equals(Events.Post.SEND_ERROR) || action.equals(Events.Post.DELETED)) {
            updateListOnDataChanged();
            setRefreshDone();
        } else if (action.equals(Events.Emoticon.RECEIVED) || action.equals(Events.Emoticon.BITMAP_FETCHED)
            || action.equals(Events.Post.FOOTPRINT_BMP_FETCHED)) {
            refreshPostListDelay();
        } else if (isCompleteDataForMimeDataFetched(action)) {
            //received mime data eg. post or deezer radio in post
            refreshPostListDelay();
        }
    }

    protected void updateActionBar(Activity activity) {
        if (mType != PostListType.PROFILE_POSTS) {
            super.updateActionBar(activity);
        }
    }

    private void updateVerify() {
        Profile profile = UserDatastore.getInstance().getProfileWithUsername(Session.getInstance().getUsername(), false);
        if (profile != null && profile.getLabels() != null) {
            isVerified = profile.getLabels().isVerified();
        }
    }

    protected void setRefreshDone() {
        setPullToRefreshComplete();
        hideLoadingMore();
    }

    /**
     * @param postCategory
     * @return
     */
    private boolean isPostCategoryForThisPostList(String postCategory) {

        if (postCategory == null) {
            return false;
        }

        if (mType == PostListType.HOME_FEEDS && postCategory.equals(PostCategoryTypeEnum.FEEDS.value())
                || mType == PostListType.MENTION_LIST && postCategory.equals(PostCategoryTypeEnum.MENTIONS.value())
                || mType == PostListType.WATCHED_POSTS
                && postCategory.equals(PostCategoryTypeEnum.WATCHEDPOSTS.value())
                || mType == PostListType.PROFILE_POSTS
                && postCategory.equals(PostCategoryTypeEnum.USERPOSTS.value())
                || mType == PostListType.SEARCH_POSTS
                && postCategory.equals(PostCategoryTypeEnum.SEARCH_POSTS.value())
                // TODO: there's no type for group posts
                // || mType == PostListType.GROUP_POSTS &&
                // postCatogery.equals(PostCategoryTypeEnum.FEEDS.value())
                || mType == PostListType.TOPIC_POSTS
                && postCategory.equals(PostCategoryTypeEnum.SEARCH_HOT_TOPIC_POSTS.value())) {
            return true;
        }

        return false;
    }

    @Override
    protected void onListEndReached() {
        super.onListEndReached();

        if (!isPostCategoryEnd()) {
            showLoadingMore();
            loadMorePosts();
        }

    }

    /**
     * @return mEmptyView
     */
    private View createEmptyView() {
        mIsHeaderEnabled = true;
        Context context = Tools.ensureContext(getActivity());

        if (context != null) {
            if (mIsLoadingData) {
                mEmptyView = LayoutInflater.from(context).inflate(R.layout.empty_view_loading, mList, false);
                ImageView loadingIcon = (ImageView) mEmptyView.findViewById(R.id.loading_icon);
                loadingIcon.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.loading_icon_rotate));
            } else {
                mEmptyView = LayoutInflater.from(context).inflate(R.layout.empty_view_text, mList, false);
                setEmptyText();
            }
        }
        return mEmptyView;
    }

    private void setEmptyText() {
        String intro, hint, link, finalHint;
        SpannableString spannable;
        TextView emptyTitle = (TextView) mEmptyView.findViewById(R.id.empty_text_title);
        TextView emptyHint = (TextView) mEmptyView.findViewById(R.id.empty_text_hint);

        intro = mType.getEmptyViewTitle(isSelf);
        hint = mType.getSpannableText(isSelf);
        link = Constants.BLANKSTR;

        switch (mType) {
            case MENTION_LIST:
                finalHint = String.format(hint, link);
                spannable = new SpannableString(finalHint);
                break;
            case WATCHED_POSTS:
                finalHint = String.format(hint, link);
                spannable = new SpannableString(finalHint);
                break;
            case PROFILE_POSTS:
                if (!isSelf) {
                    link = mUserName;
                    // check if the profile is private
                    if (numPosts > 0
                            && feedPrivacy == EveryoneOrFollowerAndFriendPrivacyEnum.FRIEND_OR_FOLLOWER.value()) {
                        hint = I18n.tr("%s's feed is private.");
                    }
                }

                finalHint = String.format(hint, link);
                spannable = new SpannableString(finalHint);
                break;
            case HOME_FEEDS:
            case TOPIC_POSTS:
            case GROUP_POSTS:
            case SEARCH_POSTS:
            default:
                intro = Constants.BLANKSTR;
                hint = Constants.BLANKSTR;
                link = Constants.BLANKSTR;
                finalHint = Constants.BLANKSTR;
                spannable = new SpannableString(finalHint);
                break;
        }

        emptyTitle.setText(intro);
        emptyHint.setMovementMethod(LinkMovementMethod.getInstance());
        emptyHint.setText(spannable);

        if (TextUtils.isEmpty(intro))
            emptyTitle.setVisibility(View.GONE);
        if (TextUtils.isEmpty(finalHint))
            emptyHint.setVisibility(View.GONE);
    }

    @Override
    protected void showOrHideEmptyViewIfNeeded() {
        if (mType.equals(PostListType.GROUP_POSTS)) {
            return;
        }
        if (isPostListEmpty()) {
            setListEmptyView(createEmptyView());
            /* 20150107 freddie.w:
               empty view didn't set header height,
               add the top margin if list view has the header
            */
            int normalMargin = ApplicationEx.getDimension(R.dimen.normal_margin);
            if (headerPlaceholder != null) {
                changeEmptyViewMargins(0, headerPlaceholder.getMeasuredHeight() + normalMargin, 0, 0);
            }
            if (bannerContainer != null) {
                changeEmptyViewMargins(0, bannerContainer.getMeasuredHeight() + normalMargin, 0, 0);
            }
            mEmptyViewContainer.setVisibility(View.VISIBLE);

        }
        else {
            mEmptyViewContainer.setVisibility(View.GONE);
        }

        if (isPostListEmpty() && mIsLoadingData) {
            addRetryRequests();
        } else {
            removeRetryRequests();
        }
    }

    private void addRetryRequests() {
        switch (mType) {
            case HOME_FEEDS:
                RetryConfig.getInstance().addRetryRequest(RequestTypeEnum.GET_HOME_FEEDS);
                break;
            case MENTION_LIST:
                RetryConfig.getInstance().addRetryRequest(RequestTypeEnum.GET_MENTIONS);
                break;
            case WATCHED_POSTS:
                RetryConfig.getInstance().addRetryRequest(RequestTypeEnum.GET_WATCHLIST);
                break;
            case PROFILE_POSTS:
                RetryConfig.getInstance().addRetryRequest(RequestTypeEnum.GET_POSTS);
                break;
            case SEARCH_POSTS:
                RetryConfig.getInstance().addRetryRequest(RequestTypeEnum.SEARCH_POSTS);
                break;
            case TOPIC_POSTS:
                RetryConfig.getInstance().addRetryRequest(RequestTypeEnum.SEARCH_HOT_TOPIC_POSTS);
                break;
        }
    }

    private void removeRetryRequests() {
        switch (mType) {
            case HOME_FEEDS:
                RetryConfig.getInstance().removeRetryRequest(RequestTypeEnum.GET_HOME_FEEDS);
                break;
            case MENTION_LIST:
                RetryConfig.getInstance().removeRetryRequest(RequestTypeEnum.GET_MENTIONS);
                break;
            case WATCHED_POSTS:
                RetryConfig.getInstance().removeRetryRequest(RequestTypeEnum.GET_WATCHLIST);
                break;
            case PROFILE_POSTS:
                RetryConfig.getInstance().removeRetryRequest(RequestTypeEnum.GET_POSTS);
                break;
            case SEARCH_POSTS:
                RetryConfig.getInstance().removeRetryRequest(RequestTypeEnum.SEARCH_POSTS);
                break;
            case TOPIC_POSTS:
                RetryConfig.getInstance().removeRetryRequest(RequestTypeEnum.SEARCH_HOT_TOPIC_POSTS);
                break;
        }
    }

    private void changeEmptyViewMargins(int left, int top, int right, int bottom) {
        if (mIsHeaderEnabled && mEmptyView != null) {
            FrameLayout.LayoutParams emptyViewLayoutParams =
                    new FrameLayout.LayoutParams(mEmptyView.getLayoutParams());
            emptyViewLayoutParams.setMargins(left, top, right, bottom);
            mEmptyView.setLayoutParams(emptyViewLayoutParams);
        }
    }

    private boolean isPostListEmpty() {
        return (mPostListAdapter.getCount() == 0);
    }

    
    @Override
    protected int getTitleIcon() {
        if (mType == PostListType.MENTION_LIST) {
            return R.drawable.ad_mention_white;
        } else if (mType == PostListType.WATCHED_POSTS) {
            return R.drawable.ad_favourite_white;
        }
        return R.drawable.ad_feed_white;
    }

    @Override
    public void updateTitle() {
        if(!getCurrentTitle().matches(getTitle())) {
            super.updateTitle();
            if(mType == PostListType.HOME_FEEDS) {
                showTitleAnimation();
            }
        }
    }

    @Override
    public  void updateIcon() {
        if(getCurrentTitleIconTag() != getTitleIcon()) {
            boolean shouldSkipAnimation = (getCurrentTitleIconTag() == 0);
            super.updateIcon();
            if(mType == PostListType.HOME_FEEDS && !shouldSkipAnimation) {
                showTitleIconAnimation();
            }
        }
    }

    @Override
    protected String getTitle() {
        switch (mType) {
            case MENTION_LIST:
                return I18n.tr("Mentions");
            case WATCHED_POSTS:
            {
                return createWatchedPostTitle();
            }
            case TOPIC_POSTS:
                return (mSearchString.startsWith(Constants.HASH_TAG)) ? 
                    mSearchString : Constants.HASH_TAG + mSearchString;
            case SEARCH_POSTS:
                return I18n.tr("Search results");
            default:
                return I18n.tr("Feed");
        }
    }

    private String createWatchedPostTitle(){
        if (!TextUtils.isEmpty(mUserName)) {
            Profile profile = UserDatastore.getInstance().getProfileWithUsername(mUserName, false);
            if (profile != null) {
                return String.format(I18n.tr("Favorites (%d)"), profile.getNumOfWatchPosts());
            }
        }
        return I18n.tr("Favorites");
    }

    @Override
    public CustomActionBarConfig getActionBarConfig() {
        if (mType == PostListType.PROFILE_POSTS) {
            return null;
        }
        CustomActionBarConfig config = super.getActionBarConfig();

        if (mType == PostListType.MENTION_LIST || 
            mType == PostListType.WATCHED_POSTS ||
            mType == PostListType.TOPIC_POSTS) {
            config.setNavigationButtonState(NavigationButtonState.BACK);
        } else {
            config.setNavigationButtonState(NavigationButtonState.HANDBURGUER);
            config.setShowOverflowButtonState(OverflowButtonState.ALERT);
        }
        return config;
    }
    
    @Override
    public PageData getPageData() {
        return pageData;
    }
    
    private final PageData createPageData() {
        return new PageData(R.drawable.ad_feed_orange)
            .addAction(new ContextAction(TEXT_POST, R.drawable.ad_post_white, this))
            .addAction(new ContextAction(PHOTO_POST, R.drawable.ad_camera_white, this))
            .addAction(new ContextAction(SEARCH, R.drawable.ad_search_white, this));
    }

    @Override
    public void executeAction(int actionId) {
        // for [non-login] users
        if (Session.getInstance().isBlockUsers()){
            ActionHandler.getInstance().displayLoginDialogFragment(getActivity());
            return;
        }

        switch (actionId) {
            case TEXT_POST:
                GAEvent.Miniblog_MainButtonCreatePost.send();
                if (isVerified) {
                    ActionHandler.getInstance().displaySharebox(null, ShareboxActionType.CREATE_LONG_POST,
                            null, null, null, true);
                } else {
                    ActionHandler.getInstance().displaySharebox(null, ShareboxActionType.CREATE_NEW_POST,
                            null, null, null, true);
                }
                break;
            case PHOTO_POST:
                GAEvent.Miniblog_MainButtonPhotoPost.send();
                if (isVerified) {
                    ActionHandler.getInstance().displaySharebox(null, ShareboxActionType.CREATE_LONG_POST,
                            null, null, null, null, true, ShareboxSubActionType.OPEN_CAMERA);
                } else {
                    ActionHandler.getInstance().displaySharebox(null, ShareboxActionType.CREATE_NEW_POST,
                            null, null, null, null, true, ShareboxSubActionType.OPEN_CAMERA);
                }
                break;
            case SEARCH:
                GAEvent.Miniblog_MainButtonSearch.send();
                ActionHandler.getInstance().displayHotTopics(getActivity());
                break;
            default:
                break;
        }
    }

    @Override
    protected View createFooterView() {
        return createLoadingView();
    }
    
    @Override
    protected void showLoadingMore() {
        int paddingTop = 0;
        int paddingBottom = ApplicationEx.getDimension(R.dimen.normal_padding);
        showLoadingMore(paddingTop, paddingBottom);
    }

    /**
     * @param hasHeaderPlaceHolder
     *            the hasHeaderPlaceHolder to set
     */
    public void setHasHeaderPlaceHolder(boolean hasHeaderPlaceHolder) {
        this.hasHeaderPlaceHolder = hasHeaderPlaceHolder;
    }
    
    @Override
    public HeaderPlaceHolderInterface getHeaderPlaceHolderImplementation() {
        return headerImplementation;
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {

        super.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);

        if (firstVisibleItem >= 2) {
            if (DeezerPlayerManager.getInstance().isPlaying()) {

                Post firstPost = (Post) mList.getAdapter().getItem(mList.getFirstVisiblePosition());

                //FIXME: [Freddie] the last item might be null, need to check why add null in the last item

                Post lastPost = (Post) mList.getAdapter().getItem(mList.getLastVisiblePosition());
                if (DeezerPlayerManager.getInstance().getCurrentPlayerId().equals(firstPost.getId())) {
                    firstStopIndex = mList.getFirstVisiblePosition() + 1;
                }

                if (lastPost != null && DeezerPlayerManager.getInstance().getCurrentPlayerId().equals(deezerIdPrefix + lastPost.getId())) {
                    lastStopIndex = mList.getLastVisiblePosition() - 1;
                }

                if (firstStopIndex == mList.getFirstVisiblePosition() || lastStopIndex == mList.getLastVisiblePosition()) {
                    //FIXME: not stop radioplayer due to spec change, if we need to stop player after scroll out. add stop here
                    if (firstStopIndex == mList.getFirstVisiblePosition()) {
                        firstStopIndex = -1;
                    }
                    if (lastStopIndex == mList.getLastVisiblePosition()) {
                        lastStopIndex = -1;
                    }
                }
            }
        }
    }

   private boolean isPostCategoryEnd() {
       boolean ret = false;
       switch (mType) {
           case HOME_FEEDS: {
               ret = PostsDatastore.getInstance().isFeedsEnded();
               break;
           }
           case MENTION_LIST:
           {
               ret = PostsDatastore.getInstance().isMentionsPostsEnded();
               break;
           }
           case TOPIC_POSTS:
           {
               if (!TextUtils.isEmpty(mSearchString)) {
                   ret = PostsDatastore.getInstance().isPostsFromTopicEnded(mSearchString);
               }
               break;
           }
           case WATCHED_POSTS:
           {
               ret = PostsDatastore.getInstance().isWatchedPostsEnded();
               break;
           }
           case GROUP_POSTS:
           {
               ret = PostsDatastore.getInstance().isGroupFeedsPostsEnded(groupId);
               break;
           }
           case PROFILE_POSTS:
           {
               ret = PostsDatastore.getInstance().isUserPostsEnded(mUserId);
               break;
           }
           case SEARCH_POSTS:
           {
               if (!TextUtils.isEmpty(mSearchString)) {
                   ret = PostsDatastore.getInstance().isPostsFromSearchEnded(mSearchString);
               }
               break;
           }
       }

       return ret;
   }

}
