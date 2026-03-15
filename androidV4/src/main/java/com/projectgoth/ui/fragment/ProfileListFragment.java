/**
 * Copyright (c) 2013 Project Goth
 *
 * ProfileListFragment.java
 * Created Jun 7, 2013, 10:27:32 AM
 */

package com.projectgoth.ui.fragment;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mig33.diggle.events.Events;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.Profile;
import com.projectgoth.common.Constants;
import com.projectgoth.common.Tools;
import com.projectgoth.controller.ProgressDialogController;
import com.projectgoth.datastore.Session;
import com.projectgoth.datastore.UserDatastore;
import com.projectgoth.events.AppEvents;
import com.projectgoth.events.GAEvent;
import com.projectgoth.i18n.I18n;
import com.projectgoth.imagefetcher.UIUtils;
import com.projectgoth.imagefetcher.UIUtils.LinkClickListener;
import com.projectgoth.listener.SectionUpdateListener;
import com.projectgoth.model.ProfileCategory;
import com.projectgoth.nemesis.enums.RequestTypeEnum;
import com.projectgoth.nemesis.model.User;
import com.projectgoth.nemesis.utils.RetryConfig;
import com.projectgoth.ui.activity.ActionHandler;
import com.projectgoth.ui.activity.CustomActionBarConfig;
import com.projectgoth.ui.activity.CustomActionBarConfig.NavigationButtonState;
import com.projectgoth.ui.adapter.ProfileListAdapter;
import com.projectgoth.ui.fragment.GlobalSearchFragment.SearchType;

/**
 * @author mapet
 * 
 */
public class ProfileListFragment extends BaseListFragment {

    public static final String PARAM_PROFILE_LIST_TYPE   = "PARAM_PROFILE_LIST_TYPE";
    public static final String PARAM_REQUESTING_USERNAME = "PARAM_REQUESTING_USERNAME";
    public static final String PARAM_SEARCH_STRING       = "PARAM_SEARCH_STRING";

    private ProfileListAdapter mProfileListAdapter;
    private ProfileListType    mListType;
    private View               emptyView;
    private String             mUsername;
    private boolean            isSelf;
    private String             mSearchString;

    private TextView           mSearchMoreView;

    // default increment of list items load more function
    private static final int   LOAD_MORE_INCREMENT       = 20;
    private int                mProfileLoadLimit = LOAD_MORE_INCREMENT;

    private int                numNewFollowers           = 0;

    
    private SectionUpdateListener sectionUpdateListener;

    public enum ProfileListType {
        FOLLOWERS(0), FOLLOWING(1), SEARCH_RESULTS(2), RECOMMENDED_PEOPLE(3), PEOPLE_YOU_KNOW(4), RECOMMENDED_PEOPLE_LITE(5);

        private int type;

        private ProfileListType(int type) {
            this.type = type;
        }

        public int getType() {
            return this.type;
        }

        public static ProfileListType fromValue(int type) {
            for (ProfileListType listType : values()) {
                if (listType.getType() == type) {
                    return listType;
                }
            }
            return SEARCH_RESULTS;
        }

        //@formatter:off
        public String getTitle(boolean isSelf) {
            String ownTitle, friendTitle;
            switch (this) {
                case FOLLOWERS:
                    ownTitle = I18n.tr("No fan? Fret not.");
                    friendTitle = I18n.tr("No fans yet.");
                    break;
                case FOLLOWING:
                    ownTitle = I18n.tr("Not sure who to add?");
                    friendTitle = I18n.tr("Not a fan yet.");
                    break;
                case SEARCH_RESULTS:
                    friendTitle = ownTitle = I18n.tr("Shucks. We can't find anyone with this username.");
                    break;
                case RECOMMENDED_PEOPLE:
                case RECOMMENDED_PEOPLE_LITE:
                    ownTitle = I18n.tr("No recommended users now. Try again later.");
                    friendTitle = Constants.BLANKSTR;
                    break;
                case PEOPLE_YOU_KNOW:
                    ownTitle = I18n.tr("No friends found.");
                    friendTitle = Constants.BLANKSTR;
                    break;
                default:
                    ownTitle = Constants.BLANKSTR;
                    friendTitle = Constants.BLANKSTR;
                    break;
            }
            if (isSelf) {
                return ownTitle;
            } else {
                return friendTitle;
            }
        }
        //@formatter:on

        private String getSpannableText(boolean isSelf) {
            String ownSpanText, friendSpanText;
            switch (this) {
                case FOLLOWERS:
                    ownSpanText = I18n.tr("Get active on mig and people will add you soon.");
                    friendSpanText = I18n.tr("Be the first, add %s now.");
                    break;
                case FOLLOWING:
                    ownSpanText = I18n.tr("Start with %s.");
                    friendSpanText = I18n.tr("Recommend some users to %s.");
                    break;
                case SEARCH_RESULTS:
                    ownSpanText = Constants.BLANKSTR;
                    friendSpanText = Constants.BLANKSTR;
                    break;
                default:
                    ownSpanText = Constants.BLANKSTR;
                    friendSpanText = Constants.BLANKSTR;
                    break;
            }
            if (isSelf) {
                return ownSpanText;
            } else {
                return friendSpanText;
            }
        }
    }
    
    public void setSectionUpdateListener(SectionUpdateListener listener) {
        this.sectionUpdateListener = listener;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    protected View createHeaderView() {
        View headerView = null;

        if (isSelf && numNewFollowers > 0) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            headerView = inflater.inflate(R.layout.header_profile_list, null);
            TextView title = (TextView) headerView.findViewById(R.id.new_follower_container);
            title.setText(String.format(I18n.tr("You have %d new fans"), numNewFollowers));
        }

        return headerView;
    }

    @Override
    protected void readBundleArguments(Bundle args) {
        super.readBundleArguments(args);
        
        final String username = args.getString(PARAM_REQUESTING_USERNAME);
        if (TextUtils.isEmpty(username)) {
            mUsername = Session.getInstance().getUsername();
        } else {
            mUsername = username;
        }

        //isSelf & numNewFollowers have to be set before createHeaderView
        isSelf = Session.getInstance().isSelfByUsername(mUsername);
        numNewFollowers = isSelf ? UserDatastore.getInstance().getNewFollowersCount() : 0;

        mSearchString = args.getString(PARAM_SEARCH_STRING);
        mListType = ProfileListType
                .fromValue(args.getInt(PARAM_PROFILE_LIST_TYPE, ProfileListType.SEARCH_RESULTS.getType()));
        
        createSearchMoreView();
    };

    @Override
    protected BaseAdapter createAdapter() {
        mProfileListAdapter = new ProfileListAdapter(getActivity(), mListType);
        mIsHeaderEnabled = true;
        mProfileListAdapter.setNewFollowersCount(numNewFollowers);
        return mProfileListAdapter;
    }

    @Override
    protected void updateListData() {
        refreshData(false);
    }

    private void refreshData(final boolean shouldForceFetch) {

        boolean justLoadFromCache;

        if (mList.getFirstVisiblePosition() >= LOAD_MORE_INCREMENT && !shouldForceFetch) {
            //if user has scrolled down then do not fetch new profiles when coming back (eg.
            // go to someone's profile page and back), otherwise the list jumps
            justLoadFromCache = true;
        } else {
            justLoadFromCache = false;
            // if user is on first page of profile list, just refresh the first page, prevent it from loading
            // too many profiles together
            mProfileLoadLimit = LOAD_MORE_INCREMENT;
        }

        switch (mListType) {
            case FOLLOWERS:
                updateDataWithFollowers(shouldForceFetch, justLoadFromCache);
                break;
            case FOLLOWING:
                updateDataWithFollowing(shouldForceFetch, justLoadFromCache);
                break;
            case SEARCH_RESULTS:
                updateDataWithSearchedUsers(shouldForceFetch, justLoadFromCache);
                break;
            case RECOMMENDED_PEOPLE:
            case RECOMMENDED_PEOPLE_LITE:
                updateDataWithRecommendedPeople(shouldForceFetch, justLoadFromCache);
                break;
            case PEOPLE_YOU_KNOW:
                updateDataWithPeopleYouKnow(shouldForceFetch, justLoadFromCache);
                break;
        }
    }

    private void updateListOnDataChanged() {
        // just show results of profiles rather than calling refreshData
        showProfilesForCategory();
    }

    private void showProfilesForCategory() {
        List<User> userList = null;
        switch (mListType) {
            case FOLLOWERS:
                userList = UserDatastore.getInstance().getFollowerUsersWithName(mUsername);
                break;
            case FOLLOWING:
                userList = UserDatastore.getInstance().getFollowingUsersWithName(mUsername);
                break;
            case SEARCH_RESULTS:
                userList = UserDatastore.getInstance().getUsersFromSearch(mSearchString);
                if (sectionUpdateListener != null && userList != null) {
                    sectionUpdateListener.setSectionCount(userList.size());
                }
                break;
            case RECOMMENDED_PEOPLE:
            case RECOMMENDED_PEOPLE_LITE:
                userList = UserDatastore.getInstance().getRecommendedUsers(Session.getInstance().getUserId());
                break;
            case PEOPLE_YOU_KNOW:
                userList = UserDatastore.getInstance().getRecommendedContacts(Session.getInstance().getUserId());
                break;
        }

        if (userList != null) {
            updateAdapterData(userList);
        }
    }

    @Override
    public void onRefresh() {
        if(Session.getInstance().isNetworkConnected()) {
            mProfileLoadLimit = LOAD_MORE_INCREMENT;
            refreshData(true);
        } else {
            updateListData();
            setRefreshDone();
        }
    }

    @Override
    protected void registerReceivers() {
        switch (mListType) {
            case FOLLOWERS:
                registerEvent(Events.Profile.FETCH_FOLLOWERS_COMPLETED);
                registerEvent(Events.Profile.FETCH_FOLLOWERS_ERROR);
                break;
            case FOLLOWING:
                registerEvent(Events.Profile.FETCH_FOLLOWING_COMPLETED);
                registerEvent(Events.Profile.FETCH_FOLLOWING_ERROR);
                break;
            case SEARCH_RESULTS:
                registerEvent(Events.Profile.FETCH_SEARCHED_USERS_COMPLETED);
                registerEvent(Events.Profile.FETCH_SEARCHED_USERS_ERROR);
                break;
            case RECOMMENDED_PEOPLE:
            case RECOMMENDED_PEOPLE_LITE:
                registerEvent(Events.Profile.FETCH_RECOMMENDED_USERS_COMPLETED);
                registerEvent(Events.Profile.FETCH_RECOMMENDED_USERS_ERROR);
                registerEvent(Events.Profile.FETCH_SEARCHED_USERS_ERROR);
                break;
            case PEOPLE_YOU_KNOW:
                registerEvent(Events.Profile.FETCH_RECOMMENDED_CONTACTS_COMPLETED);
                registerEvent(Events.Profile.FETCH_RECOMMENDED_CONTACTS_ERROR);
                registerEvent(Events.Profile.FETCH_SEARCHED_USERS_ERROR);
                break;
        }

        registerEvent(Events.User.FOLLOWED);
        registerEvent(Events.User.ALREADY_FOLLOWING);
        registerEvent(Events.User.PENDING_APPROVAL);

        registerEvent(Events.User.UNFOLLOWED);
        registerEvent(Events.User.REQUESTING_FOLLOWING);
        registerEvent(Events.User.NOT_FOLLOWING);

        registerEvent(Events.User.FOLLOW_ERROR);
        registerEvent(Events.User.UNFOLLOW_ERROR);
        registerEvent(Events.Profile.BEGIN_FETCH_FOR_CATEGORY);

        registerEvent(AppEvents.Notification.NEW_FOLLOWER_NOTIFICATION);
        registerEvent(Events.Profile.RECEIVED);
        registerEvent(Events.Contact.REMOVED);
        registerEvent(AppEvents.NetworkService.ERROR);
    }

    @Override
    protected void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String username = intent.getStringExtra(Events.User.Extra.USERNAME);
        ProgressDialogController.getInstance().hideProgressDialog();

        if (action.equals(Events.Profile.FETCH_FOLLOWING_COMPLETED)
                || action.equals(Events.Profile.FETCH_FOLLOWERS_COMPLETED)) {
            if (!TextUtils.isEmpty(mUsername) && mUsername.equalsIgnoreCase(username)) {
                setLoadingData(false);
                updateListOnDataChanged();
                setRefreshDone();
            }
        } else if (action.equals(Events.Profile.FETCH_RECOMMENDED_USERS_COMPLETED)
                || action.equals(Events.Profile.FETCH_RECOMMENDED_CONTACTS_COMPLETED)) {
            setLoadingData(false);
            updateListOnDataChanged();
            setRefreshDone();
        } else if (action.equals(AppEvents.Notification.NEW_FOLLOWER_NOTIFICATION)) {
            if (mListType == ProfileListType.FOLLOWERS) {
                if (isSelf) {
                    //refresh but not force fetch because it's fetched before sending
                    // NEW_FOLLOWER_NOTIFICATION
                    numNewFollowers = UserDatastore.getInstance().getNewFollowersCount();
                    mProfileListAdapter.setNewFollowersCount(numNewFollowers);
                    refreshData(false);
                }
            }
        } else if (action.equals(Events.Profile.BEGIN_FETCH_FOR_CATEGORY)) {
            final String profileCategory = intent.getStringExtra(Events.Profile.Extra.CATEGORY);
            if (isProfileCategoryTypeForThisProfileList(ProfileCategory.Type.fromValue(profileCategory))) {
                setLoadingData(true, true);
            }
        } else if (action.equals(Events.Profile.FETCH_FOLLOWING_ERROR)
                || action.equals(Events.Profile.FETCH_FOLLOWERS_ERROR)) {
            if (!TextUtils.isEmpty(mUsername) && mUsername.equalsIgnoreCase(username)) {
                Tools.showToastForIntent(context, intent);
            }
            setLoadingData(false, true);
            setRefreshDone();
        } else if (action.equals(Events.Profile.FETCH_SEARCHED_USERS_COMPLETED)) {

            String searchParam = intent.getStringExtra(Events.Misc.Extra.SEARCH_QUERY);
            if (mSearchString.equals(searchParam)) {
                setLoadingData(false, true);
                updateListOnDataChanged();
                setRefreshDone();
            }
        } else if (action.equals(Events.Profile.FETCH_SEARCHED_USERS_ERROR)
                || action.equals(Events.Profile.FETCH_RECOMMENDED_USERS_ERROR)
                || action.equals(Events.Profile.FETCH_RECOMMENDED_CONTACTS_ERROR)) {
            setLoadingData(false, true);
            setRefreshDone();
            Tools.showToastForIntent(context, intent);
        } else if (action.equals(Events.User.FOLLOWED) || action.equals(Events.User.ALREADY_FOLLOWING)
                || action.equals(Events.User.PENDING_APPROVAL) || action.equals(Events.User.UNFOLLOWED)
                || action.equals(Events.User.REQUESTING_FOLLOWING) || action.equals(Events.User.NOT_FOLLOWING)
                || action.equals(Events.Contact.REMOVED)) {
            updateListOnDataChanged();
            Tools.showToastForIntent(context, intent);
        } else if (action.equals(Events.User.FOLLOW_ERROR) || action.equals(Events.User.UNFOLLOW_ERROR)) {
            Tools.showToastForIntent(context, intent);
            updateListOnDataChanged();
        } else if (action.equals(Events.Profile.RECEIVED)) {
            if (this.mUsername.equalsIgnoreCase(username)) {
                updateActionBar(getActivity());
            }
            updateListOnDataChanged();
        } else if (action.equals(AppEvents.NetworkService.ERROR)) {
            setPullToRefreshComplete();
        }
    }

    protected void setRefreshDone() {
        setPullToRefreshComplete();
        hideLoadingMore();
    }

    void notifyDataSetChanged() {
        mProfileListAdapter.notifyDataSetChanged();
        showOrHideEmptyViewIfNeeded();
    }

    protected void showOrHideEmptyViewIfNeeded() {
        if (isProfileListEmpty()) {
            if (mIsLoadingData) {
                switch (mListType) {
                    case SEARCH_RESULTS:
                        Tools.showToast(getActivity(), I18n.tr("Searching"));
                        mEmptyViewContainer.setVisibility(View.GONE);
                        break;
                    default:
                        setListEmptyView(createEmptyView());
                        mEmptyViewContainer.setVisibility(View.VISIBLE);
                        break;
                }
            } else {
                setListEmptyView(createEmptyView());
                mEmptyViewContainer.setVisibility(View.VISIBLE);
            }
            removeSearchAllFooter();
        } else {
            addSearchAllFooter();
            mEmptyViewContainer.setVisibility(View.GONE);
        }

        if (isProfileListEmpty() && mIsLoadingData) {
            addRetryRequests();
        } else {
            removeRetryRequests();
        }

    }

    private void addSearchAllFooter() {
        // if ProfileListType is RECOMMENDED_PEOPLE_LITE then need to add see all button
        if (mListType == ProfileListType.RECOMMENDED_PEOPLE_LITE) {
            LayoutInflater inflater = LayoutInflater.from(ApplicationEx.getContext());
            TextView searchMoreView = (TextView) inflater.inflate(R.layout.footer_search_more_text, null, false);
            searchMoreView.setVisibility(View.VISIBLE);
            searchMoreView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.text_size_normal));
            searchMoreView.setText(I18n.tr("See all recommended people"));
            searchMoreView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    GAEvent.Chat_ClickRecommendedUsers.send();
                    ActionHandler.getInstance().displayRecommendedUsers(getActivity());
                }
            });
            mProfileListAdapter.setSearchMoreView(searchMoreView);
        }
    }

    private void removeSearchAllFooter() {
        if (mListType == ProfileListType.RECOMMENDED_PEOPLE_LITE) {
            mProfileListAdapter.setSearchMoreView(null);
        }
    }

    private void addRetryRequests() {
        switch (mListType) {
            case SEARCH_RESULTS:
                RetryConfig.getInstance().addRetryRequest(RequestTypeEnum.SEARCH_USERS);
                break;
            case FOLLOWING:
                RetryConfig.getInstance().addRetryRequest(RequestTypeEnum.GET_FOLLOWING);
                break;
            case FOLLOWERS:
                RetryConfig.getInstance().addRetryRequest(RequestTypeEnum.GET_FOLLOWERS);
                break;
            case RECOMMENDED_PEOPLE:
            case RECOMMENDED_PEOPLE_LITE:
                RetryConfig.getInstance().addRetryRequest(RequestTypeEnum.RECOMMENDED_PEOPLE);
                break;
            case PEOPLE_YOU_KNOW:
                RetryConfig.getInstance().addRetryRequest(RequestTypeEnum.RECOMMENDED_CONTACTS);
                break;
        }
    }

    private void removeRetryRequests() {
        switch (mListType) {
            case SEARCH_RESULTS:
                RetryConfig.getInstance().removeRetryRequest(RequestTypeEnum.SEARCH_USERS);
                break;
            case FOLLOWING:
                RetryConfig.getInstance().removeRetryRequest(RequestTypeEnum.GET_FOLLOWING);
                break;
            case FOLLOWERS:
                RetryConfig.getInstance().removeRetryRequest(RequestTypeEnum.GET_FOLLOWERS);
                break;
            case RECOMMENDED_PEOPLE:
            case RECOMMENDED_PEOPLE_LITE:
                RetryConfig.getInstance().removeRetryRequest(RequestTypeEnum.RECOMMENDED_PEOPLE);
                break;
            case PEOPLE_YOU_KNOW:
                RetryConfig.getInstance().removeRetryRequest(RequestTypeEnum.RECOMMENDED_CONTACTS);
                break;
        }
    }

    /**
     * @return
     */
    private boolean isProfileListEmpty() {
        return (mProfileListAdapter.getCount() == 0);
    }   
    
    private void createSearchMoreView() {
        final LayoutInflater inflater = LayoutInflater.from(ApplicationEx.getContext());
        mSearchMoreView = (TextView) inflater.inflate(R.layout.footer_search_more_text, null, false);
        mSearchMoreView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAndPerformGlobalSearch(getFilterText());
            }
        });
    }

    @Override
    protected void performGlobalSearch(final String searchString) {
        super.performGlobalSearch(searchString);
        ActionHandler.getInstance().displayGlobalSearch(getActivity(), SearchType.PEOPLE, searchString);
    }

    /**
     * @return emptyView
     */
    private View createEmptyView() {
        mIsHeaderEnabled = true;
        if (!mIsLoadingData) {
            emptyView = LayoutInflater.from(getActivity()).inflate(R.layout.empty_view_text, null);
            setEmptyText();
        } else {
            emptyView = LayoutInflater.from(getActivity()).inflate(R.layout.empty_view_loading, null);
            ImageView loadingIcon = (ImageView) emptyView.findViewById(R.id.loading_icon);
            loadingIcon.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.loading_icon_rotate));
        }

        return emptyView;
    }

    private void setEmptyText() {
        String intro, hint, link, finalHint;
        SpannableString spannable;
        TextView emptyTitle = (TextView) emptyView.findViewById(R.id.empty_text_title);
        TextView emptyHint = (TextView) emptyView.findViewById(R.id.empty_text_hint);

        intro = mListType.getTitle(isSelf);
        hint = mListType.getSpannableText(isSelf);
        link = Constants.BLANKSTR;

        switch (mListType) {
            case FOLLOWERS:
                if (!isSelf && !TextUtils.isEmpty(mUsername)) {
                    link = mUsername;
                }
                finalHint = String.format(hint, link);
                spannable = new SpannableString(finalHint);
                break;
            case FOLLOWING:
                if (!isSelf && !TextUtils.isEmpty(mUsername)) {
                    hint = String.format(hint, mUsername);
                }

                link = I18n.tr("Recommended people");
                finalHint = String.format(hint, link);
                spannable = new SpannableString(finalHint);

                if (isSelf) {
                    UIUtils.setLinkSpan(spannable, finalHint, link, new LinkClickListener() {

                        @Override
                        public void onClick() {
                            ActionHandler.getInstance().displayRecommendedUsers(getActivity());
                        }
                    });
                }
                break;
            case SEARCH_RESULTS:
                finalHint = String.format(hint, link);
                spannable = new SpannableString(finalHint);
                break;
            case RECOMMENDED_PEOPLE:
            case RECOMMENDED_PEOPLE_LITE:
            case PEOPLE_YOU_KNOW:
                finalHint = String.format(hint, link);
                spannable = new SpannableString(finalHint);
                break;
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
        
        emptyView.setVisibility(View.VISIBLE);
    }

    /**
     * @param profileCategoryType
     * @return
     */
    private boolean isProfileCategoryTypeForThisProfileList(final ProfileCategory.Type profileCategoryType) {
        if (profileCategoryType != null) {
            if ((mListType == ProfileListType.FOLLOWERS && profileCategoryType == ProfileCategory.Type.FOLLOWER)
                    || (mListType == ProfileListType.FOLLOWING && profileCategoryType == ProfileCategory.Type.FOLLOWING)
                    || (mListType == ProfileListType.RECOMMENDED_PEOPLE || mListType == ProfileListType.RECOMMENDED_PEOPLE_LITE && profileCategoryType == ProfileCategory.Type.RECOMMENDED_USERS)
                    || (mListType == ProfileListType.PEOPLE_YOU_KNOW && profileCategoryType == ProfileCategory.Type.RECOMMENDED_CONTACTS)
                    || (mListType == ProfileListType.SEARCH_RESULTS && profileCategoryType == ProfileCategory.Type.SEARCH_USERS)) {
                return true;
            }
        }

        return false;
    }

    @Override
    protected void onListEndReached() {
        super.onListEndReached();

        // Only load-more if there is no filter text entered by the user.
        if (TextUtils.isEmpty(getFilterText())) {
            if (mListType != ProfileListType.RECOMMENDED_PEOPLE_LITE && !isProfileCategoryEnd()) {
                showLoadingMore();
                loadMoreProfiles();
            }
        }
    }

    private void loadMoreProfiles() {
        int offset = mProfileListAdapter.getCount();

        switch (mListType) {
            case FOLLOWERS:
                UserDatastore.getInstance().getFollowerUsersWithName(mUsername, offset, LOAD_MORE_INCREMENT, true, false);
                break;
            case FOLLOWING:
                UserDatastore.getInstance().getFollowingUsersWithName(mUsername, offset, LOAD_MORE_INCREMENT, true, false);
                break;
            case SEARCH_RESULTS:
                UserDatastore.getInstance().getUsersFromSearch(mSearchString, offset, LOAD_MORE_INCREMENT, true, false);
                break;
            case RECOMMENDED_PEOPLE:
            case RECOMMENDED_PEOPLE_LITE:
                UserDatastore.getInstance().getRecommendedUsers(Session.getInstance().getUserId(),
                        offset, LOAD_MORE_INCREMENT, true, false);
                break;
            case PEOPLE_YOU_KNOW:
                UserDatastore.getInstance().getRecommendedContacts(Session.getInstance().getUserId(),
                        offset, LOAD_MORE_INCREMENT, true, false);
                break;
        }
    }

    private boolean isProfileCategoryEnd() {
        boolean ret = false;

        switch (mListType) {
            case FOLLOWERS:
                ret = UserDatastore.getInstance().isFollowerListEnded(mUsername);
                break;
            case FOLLOWING:
                ret = UserDatastore.getInstance().isFollowingListEnded(mUsername);
                break;
            case SEARCH_RESULTS:
                ret = UserDatastore.getInstance().isSearchUsersListEnded(mSearchString);
                break;
            case RECOMMENDED_PEOPLE:
            case RECOMMENDED_PEOPLE_LITE:
                ret = UserDatastore.getInstance().isRecommendedUsersEnded(mUsername);
                break;
            case PEOPLE_YOU_KNOW:
                ret = UserDatastore.getInstance().isRecommendedContactsEnded(mUsername);
                break;
        }

        return ret;
    }

    private void updateDataWithFollowers(final boolean shouldForceFetch, final boolean justLoadFromCache) {
        List<User> followers = UserDatastore.getInstance().getFollowerUsersWithName(mUsername, 0, mProfileLoadLimit,
                shouldForceFetch, justLoadFromCache);
        updateAdapterData(followers);
    }

    private void updateDataWithFollowing(final boolean shouldForceFetch, final boolean justLoadFromCache) {
        List<User> following = UserDatastore.getInstance().getFollowingUsersWithName(mUsername, 0,
                mProfileLoadLimit, shouldForceFetch, justLoadFromCache);
        updateAdapterData(following);
    }

    private void updateDataWithSearchedUsers(final boolean shouldForceFetch, final boolean justLoadFromCache) {
        List<User> userList = UserDatastore.getInstance().getUsersFromSearch(mSearchString, 0, mProfileLoadLimit,
                shouldForceFetch, justLoadFromCache);
        if (userList != null) {
            if (sectionUpdateListener != null) {
                sectionUpdateListener.setSectionCount(userList.size());
            }
            updateAdapterData(userList);
        } else {
            ProgressDialogController.getInstance().showProgressDialog(getActivity(),
                    ProgressDialogController.ProgressType.Search);
        }
    }

    private void updateDataWithRecommendedPeople(final boolean shouldForceFetch, final boolean justLoadFromCache) {
        List<User> userList = UserDatastore.getInstance().getRecommendedUsers(Session.getInstance().getUserId(),
                0, mProfileLoadLimit, shouldForceFetch, justLoadFromCache);
        if (userList != null) {
            updateAdapterData(userList);
        }
    }

    private void updateDataWithPeopleYouKnow(final boolean shouldForceFetch, final boolean justLoadFromCache) {
        List<User> userList = UserDatastore.getInstance().getRecommendedContacts(Session.getInstance().getUserId(),
                0, mProfileLoadLimit, shouldForceFetch, justLoadFromCache);
        if (userList != null) {
            updateAdapterData(userList);
        }
    }

    private void updateAdapterData(List<User> data) {
        if (data != null) {
            // set data to adapter
            mProfileListAdapter.setUserList(data);
            mProfileLoadLimit = data.size();
        }
        showOrHideEmptyViewIfNeeded();
        
        if (mode == Mode.FILTERING) {
            performFilter(getFilterText());
        }
    }

    @Override
    public void performFilter(final String filterString) {
        super.performFilter(filterString);

        if (!TextUtils.isEmpty(filterString)) {
            mSearchMoreView.setVisibility(View.VISIBLE);
            mSearchMoreView.setText(String.format(I18n.tr("Find more people for \"%s\""), filterString));
            mProfileListAdapter.setSearchMoreView(mSearchMoreView);
        } else {
            mSearchMoreView.setVisibility(View.GONE);
            mProfileListAdapter.setSearchMoreView(null);
        }
        
        mProfileListAdapter.filterAndRefresh(filterString);
    }

    @Override
    public CustomActionBarConfig getActionBarConfig() {
        CustomActionBarConfig config = super.getActionBarConfig();
        config.setNavigationButtonState(NavigationButtonState.BACK);
        return config;
    }

    @Override
    protected String getTitle() {
        if (mListType != null) {
            switch (mListType) {
                case FOLLOWERS:
                {
                    String title = I18n.tr("Fans");
                    if (!TextUtils.isEmpty(mUsername)) {
                        Profile profile = UserDatastore.getInstance().getProfileWithUsername(mUsername, false);
                        if (profile != null) {
                            title = String.format(I18n.tr("Fans (%d)"), profile.getNumOfFollowers());
                        }
                    } 
                    return title;
                }
                case FOLLOWING:
                {
                    String title = I18n.tr("Fan of");
                    if (!TextUtils.isEmpty(mUsername)) {
                        Profile profile = UserDatastore.getInstance().getProfileWithUsername(mUsername, false);
                        if (profile != null) {
                            title = String.format(I18n.tr("Fan of (%d)"), profile.getNumOfFollowing());
                        }
                    }
                    return title;
                }
                case PEOPLE_YOU_KNOW:
                    return I18n.tr("People you already know");
                case RECOMMENDED_PEOPLE:
                case RECOMMENDED_PEOPLE_LITE:
                    return I18n.tr("Recommended people");
                case SEARCH_RESULTS:
                    return I18n.tr("Search results");
            }
        }

        return Constants.BLANKSTR;
    }

    @Override
    protected int getTitleIcon() {
        switch (mListType) {
            case FOLLOWING:
                return R.drawable.ad_fanoftitle_white;
            case RECOMMENDED_PEOPLE:
            case RECOMMENDED_PEOPLE_LITE:
                return R.drawable.ad_recommended_white;
            default:
                return R.drawable.ad_fanof_white;
        }

    }

    @Override
    protected View createFooterView() {
        return createLoadingView();
    }
}
