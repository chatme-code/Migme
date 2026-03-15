/**
 * Copyright (c) 2013 Project Goth
 *
 * HotTopicsFragment.java
 * Created Aug 16, 2013, 6:44:25 PM
 */

package com.projectgoth.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import com.mig33.diggle.events.Events;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.HotTopic;
import com.projectgoth.b.data.HotTopicsResult;
import com.projectgoth.common.Constants;
import com.projectgoth.common.Tools;
import com.projectgoth.controller.ProgressDialogController;
import com.projectgoth.datastore.PostsDatastore;
import com.projectgoth.i18n.I18n;
import com.projectgoth.ui.activity.ActionHandler;
import com.projectgoth.ui.activity.CustomActionBarConfig;
import com.projectgoth.ui.activity.CustomActionBarConfig.NavigationButtonState;
import com.projectgoth.ui.widget.FlowLayout;

/**
 * @author cherryv
 * 
 */
public class HotTopicsFragment extends BaseSearchFragment implements OnClickListener {

    private static final int        HOT_TOPICS_LIMIT = 30;

    private FlowLayout              mHotTopics;
    private TextView                title;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_hot_topics;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mHotTopics = (FlowLayout) view.findViewById(R.id.hot_topics_container);
        title = (TextView) view.findViewById(R.id.hot_topic_title);
        title.setText(getTitle());
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshData();
    }

    @Override
    protected void registerReceivers() {
        registerEvent(Events.HotTopic.FETCH_ALL_COMPLETED);
        registerEvent(Events.HotTopic.FETCH_ALL_ERROR);
    }

    @Override
    protected void onReceive(Context context, Intent intent) {
        ProgressDialogController.getInstance().hideProgressDialog();
        String action = intent.getAction();
        if (action.equals(Events.HotTopic.FETCH_ALL_COMPLETED)) {
            refreshData();
        } else if (action.equals(Events.HotTopic.FETCH_ALL_ERROR)) {
            Tools.showToastForIntent(context, intent);
        }
    }

    private void refreshData() {
        HotTopicsResult result = PostsDatastore.getInstance().getHotTopics(HOT_TOPICS_LIMIT);
        if (result != null) {
            mHotTopics.removeAllViews();
            HotTopic[] hotTopics = result.getResult();
            for (HotTopic topic : hotTopics) {
                addTopicView(topic);
            }
        } else {
            ProgressDialogController.getInstance().showProgressDialog(getActivity(),
                    ProgressDialogController.ProgressType.Search);
        }
    }

    private void addTopicView(HotTopic topic) {
        final LayoutInflater inflater = LayoutInflater.from(ApplicationEx.getContext());
        TextView topicView = (TextView) inflater.inflate(R.layout.hot_topic_view, null, false);
        
        topicView.setText(topic.getName());
        topicView.setTag(topic);
        topicView.setOnClickListener(this);

        mHotTopics.addView(topicView);
    }

    @Override
    public void onClick(View v) {
        if (Tools.hideVirtualKeyboard(getActivity())) {
            //If software keyboard showing just hide it and do nothing
            return;
        }
        Object viewTag = v.getTag();
        if (viewTag != null && viewTag instanceof HotTopic) {
            HotTopic topic = (HotTopic) viewTag;
            ActionHandler.getInstance().displayHotTopicPosts(getActivity(), topic.getName());
        }
    }

    @Override
    public CustomActionBarConfig getActionBarConfig() {
        CustomActionBarConfig config = super.getActionBarConfig();
        config.setNavigationButtonState(NavigationButtonState.BACK);
        return config;
    }
    
    @Override
    protected void performGlobalSearch(final String searchString) {
        super.performGlobalSearch(searchString);
        
        ActionHandler.getInstance().displayGlobalSearchPreview(getActivity(), searchString);
    }
    

    @Override
    protected String getTitle() {
        return I18n.tr("Trending topics");
    }
    
    @Override
    protected int getTitleIcon() {
        return R.drawable.ad_feed_white;
    }
}
