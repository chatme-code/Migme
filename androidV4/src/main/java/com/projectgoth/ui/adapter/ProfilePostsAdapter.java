/**
 * Copyright (c) 2013 Project Goth
 *
 * ProfilePostsAdapter.java
 * Created Oct 3, 2014, 10:03:39 AM
 */

package com.projectgoth.ui.adapter;

import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.Post;
import com.projectgoth.common.Constants;
import com.projectgoth.enums.PostListType;
import com.projectgoth.i18n.I18n;
import com.projectgoth.imagefetcher.UIUtils;
import com.projectgoth.listener.PostViewListener;
import com.projectgoth.ui.activity.ActionHandler;
import com.projectgoth.ui.fragment.ShareboxFragment.ShareboxActionType;
import com.projectgoth.ui.holder.PostViewHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ProfilePostsAdapter extends BaseExpandableListAdapter {

    FragmentActivity                                            mActivity                      = null;
//    private List<ProfileTabData>                                mCategoryData                  = new ArrayList<ProfileTabData>();
    private List<Post>                                          mPostList                      = new ArrayList<Post>();
    private LayoutInflater                                      mInflater;
    private PostListType                                        mType;

    protected ConcurrentHashMap<String, SpannableStringBuilder> spannableCache                 = new ConcurrentHashMap<String, SpannableStringBuilder>();
    private PostViewListener                                    postViewListener;
    private boolean                                             isSelf;
    private boolean                                             isProfilePrivate;
    private String                                              username;
    private boolean                                             isLoadingPosts;

    private static final int                                    NOTIFY_DATASET_CHANGED_MESSAGE = 0;
    private static final long                                   DATASET_CHANGED_DELAY          = 500;

    //@formatter:off
    private Handler adapterHandler = new Handler(new Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case NOTIFY_DATASET_CHANGED_MESSAGE:
                    notifyDataSetChanged();
                    return true;
            }
            return false;
        }
    });
    //@formatter:on

    public ProfilePostsAdapter(FragmentActivity activity, PostListType type) {
        super();
        mActivity = activity;
        mInflater = LayoutInflater.from(ApplicationEx.getContext());
        mType = type;
        postViewListener = new PostViewListener(activity, type);
    }

    @Override
    public Object getGroup(int groupPosition) {
//        if (groupPosition < mCategoryData.size()) {
//            return mCategoryData.get(groupPosition);
//        }
        return null;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
//        ProfileTabHolder categoryView = null;
//        ProfileTabData group = (ProfileTabData) getGroup(groupPosition);
//
//        if (convertView == null) {
//            convertView = mInflater.inflate(R.layout.profile_tab, parent, false);
//            categoryView = new ProfileTabHolder(convertView, true, false);
//            convertView.setTag(categoryView);
//        } else {
//            categoryView = (ProfileTabHolder) convertView.getTag();
//        }
//
//        if (group != null && categoryView != null) {
//            categoryView.setData(group);
//        }
//        
//        categoryView.setProfileTabListener(mListener);

        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        if (mPostList == null || mPostList.isEmpty()) {
            // 1 extra for the empty scenario.
            return 1;
        }
        
        return mPostList.size();
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        if (mPostList != null && childPosition < mPostList.size()) {
            return mPostList.get(childPosition);
        }
        return null;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView,
            ViewGroup parent) {
        Post post = (Post) getChild(groupPosition, childPosition);
        
        if (post == null) {
            return createEmptyView();
        }
        
        PostViewHolder postViewHolder;
        if (convertView == null || convertView.getTag(R.id.holder) == null) {
            convertView = mInflater.inflate(R.layout.profile_post_item, parent, false);
            postViewHolder = new PostViewHolder(mActivity , convertView, spannableCache);
            convertView.setTag(R.id.holder, postViewHolder);
        } else {
            postViewHolder = (PostViewHolder) convertView.getTag(R.id.holder);
        }

        if (mType == PostListType.GROUP_POSTS) {
            postViewHolder.setWatchable(false);
        } else {
            postViewHolder.setWatchable(true);
        }

        postViewHolder.setBaseViewListener(postViewListener);
        postViewHolder.setData(post);

        return convertView;
    }

    private View createEmptyView() {
        // This is the empty scenario.
        ViewGroup emptyScenarioContainer;

        if (isLoadingPosts) {
            emptyScenarioContainer = (ViewGroup) mInflater.inflate(R.layout.profile_empty_view_loading, null);
            TextView loadingText = (TextView) emptyScenarioContainer.findViewById(R.id.loading_text);
            loadingText.setText(I18n.tr("Loading"));
        } else {
            emptyScenarioContainer = (ViewGroup) mInflater.inflate(R.layout.empty_text_view_elements, null);
            TextView title = (TextView) emptyScenarioContainer.findViewById(R.id.empty_text_title);
            TextView hint = (TextView) emptyScenarioContainer.findViewById(R.id.empty_text_hint);

            if (isSelf) {
                title.setText(I18n.tr("Hey. Why so quiet?"));
                final String link = I18n.tr("Share a thought or photo now");
                final String finalHint = String.format("%s.", link);
                SpannableString spannable = new SpannableString(finalHint);

                UIUtils.setLinkSpan(spannable, finalHint, link, null);
                hint.setText(spannable);
                hint.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        ActionHandler.getInstance().displaySharebox(mActivity,
                                ShareboxActionType.CREATE_NEW_POST,
                                null, null, null, true);
                    }
                });
            } else {
                if (!isProfilePrivate) {
                    title.setText(I18n.tr("No posts yet."));
                    hint.setText(I18n.tr("Check back here soon."));
                } else {
                    title.setText(String.format(I18n.tr("%s\'s profile is private"), username));
                    hint.setText(Constants.BLANKSTR);
                }
            }

            emptyScenarioContainer.setTag(R.id.holder, null);
        }


        return emptyScenarioContainer;
    }

    public void setPostList(List<Post> postList) {
        mPostList = postList;
        scheduleUIUpdate();
    }

    /**
     * Schedule the {@link #notifyDataSetChanged()} in a later time.
     * 
     * check {@link #DATASET_CHANGED_DELAY} and
     * {@link #NOTIFY_DATASET_BLOCK_DELAY} for the delay settings
     */
    private void scheduleUIUpdate() {
        if (!adapterHandler.hasMessages(NOTIFY_DATASET_CHANGED_MESSAGE)) {
            adapterHandler.sendEmptyMessageDelayed(NOTIFY_DATASET_CHANGED_MESSAGE, DATASET_CHANGED_DELAY);
        }
    }

    public void setUsername(String username) {
        this.username = username;
    }
    
    public void setProfilePrivacy(boolean isProfilePrivate) {
        this.isProfilePrivate = isProfilePrivate;
    }
    
    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public int getGroupCount() {
        return 1;
    }

    @Override
    public int getGroupType(int group) {
        return 0;
    }

    @Override
    public int getGroupTypeCount() {
        return 1;
    }

    @Override
    public long getGroupId(int i) {
        return 0;
    }

    @Override
    public int getChildType(int group, int child) {
        return 0;
    }

    @Override
    public int getChildTypeCount() {
        return 1;
    }

    @Override
    public long getChildId(int group, int child) {
        return child;
    }

    @Override
    public boolean isChildSelectable(int i, int i2) {
        return false;
    }

    public void setSelf(boolean isSelf) {
        this.isSelf = isSelf;
    }

    public boolean isLoadingPosts() {
        return isLoadingPosts;
    }

    public void setLoadingPosts(boolean isLoadingPosts) {
        this.isLoadingPosts = isLoadingPosts;
    }
}
