/**
 * Copyright (c) 2013 Project Goth
 *
 * GlobalSearchPreviewFragment.java
 * Created Oct 15, 2014, 10:19:15 AM
 */

package com.projectgoth.ui.fragment;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mig33.diggle.events.Events;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.Post;
import com.projectgoth.common.TextUtils;
import com.projectgoth.common.Tools;
import com.projectgoth.controller.FriendsController;
import com.projectgoth.controller.ProgressDialogController;
import com.projectgoth.datastore.PostsDatastore;
import com.projectgoth.datastore.UserDatastore;
import com.projectgoth.enums.PostListType;
import com.projectgoth.i18n.I18n;
import com.projectgoth.listener.PostViewListener;
import com.projectgoth.listener.ProfileViewListener;
import com.projectgoth.nemesis.model.User;
import com.projectgoth.ui.activity.ActionHandler;
import com.projectgoth.ui.activity.CustomActionBarConfig;
import com.projectgoth.ui.activity.CustomActionBarConfig.NavigationButtonState;
import com.projectgoth.ui.fragment.GlobalSearchFragment.SearchType;
import com.projectgoth.ui.holder.PostViewHolder;
import com.projectgoth.ui.holder.ProfileViewHolder;


/**
 * This class serves as a preview for globally searched people and posts.
 * @author angelorohit
 */
public class GlobalSearchPreviewFragment extends BaseSearchFragment implements OnClickListener {
    
    private TextView peopleSectionTitle;
    private TextView postsSectionTitle;
    
    private TextView searchMorePeopleFooter;
    private TextView searchMorePostsFooter;
    
    private LinearLayout profileHoldersContainer;
    private LinearLayout postHoldersContainer;
    
    private int peopleSectionCount = 0;
    private int postsSectionCount = 0;
    
    private String currentSearchString;
    
    private PostViewListener postViewListener;
    private ProfileViewListener profileViewListener;
    
    private static final int SEARCH_RESULTS_LIMIT = 3;
    
    private static final LayoutInflater INFLATER = LayoutInflater.from(ApplicationEx.getContext());
    
    /* (non-Javadoc)
     * @see com.projectgoth.ui.fragment.BaseFragment#getLayoutId()
     */
    @Override
    protected int getLayoutId() {
        return R.layout.people_and_post_search_preview;
    }
    
    @Override
    protected void readBundleArguments(Bundle args) {
        super.readBundleArguments(args);
    }
    
    @Override
    public CustomActionBarConfig getActionBarConfig() {
        CustomActionBarConfig config = super.getActionBarConfig();
        config.setNavigationButtonState(NavigationButtonState.BACK);
        return config;
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        peopleSectionTitle = (TextView) view.findViewById(R.id.people_section_title);
        postsSectionTitle = (TextView) view.findViewById(R.id.posts_section_title);
        searchMorePeopleFooter = (TextView) view.findViewById(R.id.search_more_people);
        searchMorePostsFooter = (TextView) view.findViewById(R.id.search_more_posts);
        profileHoldersContainer = (LinearLayout) view.findViewById(R.id.profile_holders_container);
        postHoldersContainer = (LinearLayout) view.findViewById(R.id.post_holders_container);
        
        updatePeopleSectionWithCount(peopleSectionCount);
        updatePostsSectionWithCount(postsSectionCount);
        
        searchMorePeopleFooter.setOnClickListener(this);
        searchMorePostsFooter.setOnClickListener(this);
        peopleSectionTitle.setOnClickListener(this);
        postsSectionTitle.setOnClickListener(this);

        postViewListener = new PostViewListener(getActivity(), PostListType.SEARCH_POSTS);
        profileViewListener = new ProfileViewListener(getActivity(), ProfileListFragment.ProfileListType.SEARCH_RESULTS);
        
        currentSearchString = getFilterText();
        checkAndPerformGlobalSearch(currentSearchString);
    }
    
    @Override
    protected void performGlobalSearch(final String searchString) {
        super.performGlobalSearch(searchString);
        currentSearchString = searchString;
        fetchDataForSearchParam(searchString);
        
        focusSearchBox(false);

        Tools.showToast(getActivity(), I18n.tr("Searching"));
    }
    
    @Override
    protected void registerReceivers() {
        registerEvent(Events.Profile.FETCH_SEARCHED_USERS_COMPLETED);
        registerEvent(Events.Profile.FETCH_SEARCHED_USERS_ERROR);
        
        registerEvent(Events.Profile.RECEIVED);
        registerEvent(Events.User.FOLLOWED);
        registerEvent(Events.User.ALREADY_FOLLOWING);
        registerEvent(Events.User.PENDING_APPROVAL);

        registerEvent(Events.User.UNFOLLOWED);
        registerEvent(Events.User.REQUESTING_FOLLOWING);
        registerEvent(Events.User.NOT_FOLLOWING);

        registerEvent(Events.User.FOLLOW_ERROR);
        registerEvent(Events.User.UNFOLLOW_ERROR);
        
        registerEvent(Events.Post.FETCH_FOR_SEARCH_COMPLETED);
        registerEvent(Events.Post.FETCH_FOR_SEARCH_ERROR);
        
        registerEvent(Events.Post.WATCHED);
        registerEvent(Events.Post.UNWATCHED);
        registerEvent(Events.Post.TAGGED);
    }
    
    @Override
    protected void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        
        if (action.equals(Events.Profile.FETCH_SEARCHED_USERS_COMPLETED)) {
            final String searchParam = intent.getStringExtra(Events.Misc.Extra.SEARCH_QUERY);
            if (currentSearchString.equals(searchParam)) {
                final int totalHitCount = intent.getIntExtra(Events.Misc.Extra.TOTAL_SEARCH_HITS, 0);
                updatePeopleSectionWithCount(totalHitCount);
                fetchPeopleDataForSearchParam(currentSearchString);
                ProgressDialogController.getInstance().hideProgressDialog();
            }
        } else if (action.equals(Events.Profile.FETCH_SEARCHED_USERS_ERROR)) {
            ProgressDialogController.getInstance().hideProgressDialog();
            Tools.showToastForIntent(context, intent);
        } else if (action.equals(Events.User.FOLLOWED) || action.equals(Events.User.ALREADY_FOLLOWING)
                || action.equals(Events.User.PENDING_APPROVAL) || action.equals(Events.User.UNFOLLOWED)
                || action.equals(Events.User.REQUESTING_FOLLOWING) || action.equals(Events.User.NOT_FOLLOWING)) {            
            final String username = intent.getStringExtra(Events.User.Extra.USERNAME);
            updateHolderForProfileWithUsername(username);
            Tools.showToastForIntent(context, intent);
        } else if (action.equals(Events.User.FOLLOW_ERROR) || action.equals(Events.User.UNFOLLOW_ERROR)) {
            Tools.showToastForIntent(context, intent);
        } else if (action.equals(Events.Post.FETCH_FOR_SEARCH_COMPLETED)) {
            ProgressDialogController.getInstance().hideProgressDialog();
            final String searchParam = intent.getStringExtra(Events.Misc.Extra.SEARCH_QUERY);
            if (currentSearchString.equals(searchParam)) {
                final int totalHitCount = intent.getIntExtra(Events.Misc.Extra.TOTAL_SEARCH_HITS, 0);
                updatePostsSectionWithCount(totalHitCount);
                fetchPostsDataForSearchParam(currentSearchString);
                ProgressDialogController.getInstance().hideProgressDialog();
            }
        } else if (action.equals(Events.Post.TAGGED) || 
                action.equals(Events.Post.WATCHED) || 
                action.equals(Events.Post.UNWATCHED)) {
            final String postId = intent.getStringExtra(Events.Post.Extra.ID);
            updateHolderForPostWithId(postId);
        } else if (action.equals(Events.Post.FETCH_FOR_SEARCH_ERROR)) {
            ProgressDialogController.getInstance().hideProgressDialog();
            Tools.showToastForIntent(context, intent);
        } else if (action.equals(Events.Profile.RECEIVED)) {
            fetchPeopleDataForSearchParam(currentSearchString);
        }
    }
    
    private void updatePeopleSectionWithCount(final int count) {
        peopleSectionCount = count;
        peopleSectionTitle.setText(String.format("%s (%d)", I18n.tr("People"), count));
    }
    
    private void updatePostsSectionWithCount(final int count) {
        postsSectionCount = count;
        postsSectionTitle.setText(String.format("%s (%d)", I18n.tr("Post"), count));
    }
    
    private void fetchDataForSearchParam(final String searchString) {
        fetchPeopleDataForSearchParam(searchString);
        fetchPostsDataForSearchParam(searchString);
    }
    
    private void fetchPeopleDataForSearchParam(final String searchString) {
        if (!TextUtils.isEmpty(searchString)) {
            List<User> profileList = UserDatastore.getInstance().getUsersFromSearch(
                    searchString, 0, SEARCH_RESULTS_LIMIT, false, false);

            if (profileList.size() > SEARCH_RESULTS_LIMIT) {
                profileList = profileList.subList(0, SEARCH_RESULTS_LIMIT) ;
            }

            if (profileList != null) {
                if (peopleSectionCount == 0) {
                    updatePeopleSectionWithCount(profileList.size());
                }
                updatePeopleData(profileList);
                ProgressDialogController.getInstance().hideProgressDialog();
            }
        }
    }
    
    private void fetchPostsDataForSearchParam(final String searchString) {
        if (!TextUtils.isEmpty(searchString)) {
            List<Post> postList = PostsDatastore.getInstance().getPostsFromSearch(
                    searchString, 0, SEARCH_RESULTS_LIMIT, false, false);

            if (postList.size() > SEARCH_RESULTS_LIMIT) {
                postList = postList.subList(0, SEARCH_RESULTS_LIMIT) ;
            }

            if (postList != null) {
                if (postsSectionCount == 0) {
                    updatePostsSectionWithCount(postList.size());
                }
                updatePostsData(postList);
                ProgressDialogController.getInstance().hideProgressDialog();
            }
        }
    }
    
    private void updatePeopleData(final List<User> profileList) {
        profileHoldersContainer.removeAllViews();
        if (!profileList.isEmpty()) {
            searchMorePeopleFooter.setText(String.format(I18n.tr("See all people for \"%s\""), currentSearchString));
            searchMorePeopleFooter.setVisibility(View.VISIBLE);
            
            for (final User user : profileList) {
                if (user != null) {
                    final View view = INFLATER.inflate(R.layout.holder_list_item, null);
                    ProfileViewHolder holder = new ProfileViewHolder(view);
                    holder.setBaseViewListener(profileViewListener);
                    holder.setData(user,
                            FriendsController.getInstance().isChangeRelationshipWithUserInProgress(user.getUsername()),
                            false);
                    
                    view.setTag(R.id.holder, holder);
                    profileHoldersContainer.addView(view);
                }
            }
        } else {
            searchMorePeopleFooter.setVisibility(View.GONE);
        }
    }
    
    private void updatePostsData(final List<Post> postList) {
        postHoldersContainer.removeAllViews();
        if (!postList.isEmpty()) {
            searchMorePostsFooter.setText(String.format(I18n.tr("See all posts for \"%s\""), currentSearchString));
            searchMorePostsFooter.setVisibility(View.VISIBLE);
            
            for (final Post post : postList) {
                if (post != null) {
                    final View view = INFLATER.inflate(R.layout.holder_post_list_item, null);
                    PostViewHolder holder = new PostViewHolder(getActivity(), view,
                            new ConcurrentHashMap<String, SpannableStringBuilder>());
                    holder.setBaseViewListener(postViewListener);
                    holder.setWatchable(true);
                    holder.setData(post);

                    view.setTag(R.id.holder, holder);
                    postHoldersContainer.addView(view);
                }
            }
            
        } else {
            searchMorePostsFooter.setVisibility(View.GONE);
        }
    }
    
    private void updateHolderForProfileWithUsername(final String username) {
        final ProfileViewHolder holder = getHolderForProfileWithUsername(username);
        if (holder != null) {
            final User user = UserDatastore.getInstance().getUserWithUsername(username, false);
            if (user != null) {
                holder.setData(user,
                        FriendsController.getInstance().isChangeRelationshipWithUserInProgress(user.getUsername()),
                        false);
            }
        } 
    }

    private void updateHolderForPostWithId(final String postId) {
        final PostViewHolder holder = getHolderForPostId(postId);
        if (holder != null) {
            final Post post = PostsDatastore.getInstance().getPost(postId, false);
            if (post != null) {
                holder.setData(post);
            }
        }
    }

    private ProfileViewHolder getHolderForProfileWithUsername(final String username) {
        if (!TextUtils.isEmpty(username)) {
            final int profileHoldersContainerChildCount = profileHoldersContainer.getChildCount();
            for (int index = 0; index < profileHoldersContainerChildCount; ++index) {
                ProfileViewHolder holder = (ProfileViewHolder) profileHoldersContainer.getChildAt(index).getTag(R.id.holder);
                if (holder.getData().getUsername().equals(username)) {
                    return holder;
                }
            }
        }
        
        return null;
    }
    
    private PostViewHolder getHolderForPostId(final String postId) {
        if (!TextUtils.isEmpty(postId)) {
            final int postHoldersContainerChildCount = postHoldersContainer.getChildCount();
            for (int index = 0; index < postHoldersContainerChildCount; ++index) {
                PostViewHolder holder = (PostViewHolder) postHoldersContainer.getChildAt(index).getTag(R.id.holder);
                if (holder.getData().getId().equals(postId)) {
                    return holder;
                }
            }
        }
        
        return null;
    }

    /* (non-Javadoc)
     * @see android.view.View.OnClickListener#onClick(android.view.View)
     */
    @Override
    public void onClick(View v) {
        if (Tools.hideVirtualKeyboard(getActivity())) {
            //If software keyboard showing just hide it and do nothing
            return;
        }
        final int viewId = v.getId();
        switch (viewId) {
            case R.id.search_more_people:
                ActionHandler.getInstance().displayGlobalSearch(getActivity(), SearchType.PEOPLE, currentSearchString);
                break;
            case R.id.search_more_posts:
                ActionHandler.getInstance().displayGlobalSearch(getActivity(), SearchType.POST, currentSearchString);
                break;
        }
    }

    @Override
    protected String getTitle() {
        return I18n.tr("Search");
    }
    
    @Override
    protected int getTitleIcon() {
        return R.drawable.ad_feed_white;
    }
}
