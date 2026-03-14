/**
 * Copyright (c) 2013 Project Goth
 *
 * ProfileTabHolder.java
 * Created Oct 2, 2014, 12:05:19 AM
 */

package com.projectgoth.ui.holder;

import android.view.View;
import android.widget.ImageView;
import com.projectgoth.R;

/**
 * @author mapet
 * 
 */
public class ProfileTabHolder extends BaseViewHolder<Object> {

    private ImageView          profileInfoTab;
    private ImageView          profilePostsTab;
    private ProfileTabListener profileTabListener;
    
    private SelectedTab selectedTab = SelectedTab.Info;

    public enum SelectedTab {
        Info, Posts;

        public static SelectedTab fromValue(int value) {
            for (SelectedTab tab : values()) {
                if (tab.ordinal() == value) {
                    return tab;
                }
            }
            return Info;
        }
    }

    public interface ProfileTabListener {

        public void onProfileInfoIconClicked();

        public void onProfilePostsIconClicked();

    }

    public ProfileTabHolder(View rootView) {
        super(rootView);
        profileInfoTab = (ImageView) rootView.findViewById(R.id.profile_info);
        profilePostsTab = (ImageView) rootView.findViewById(R.id.profile_posts);
        
        profileInfoTab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (profileTabListener != null) {
                    profileTabListener.onProfileInfoIconClicked();
                }
            }
        });

        profilePostsTab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (profileTabListener != null) {
                    profileTabListener.onProfilePostsIconClicked();
                }
            }
        });
    }
    
    @Override
    public void setData(Object data) {
        super.setData(data);
    }

    public void setProfileTabListener(ProfileTabListener listener) {
        profileTabListener = listener;
    }

    /**
     * @return the selectedTab
     */
    public SelectedTab getSelectedTab() {
        return selectedTab;
    }

    /**
     * @param selectedTab the selectedTab to set
     */
    public void setSelectedTab(SelectedTab selectedTab) {
        this.selectedTab = selectedTab;
        refreshTabs();
    }

    private void refreshTabs() {
        profileInfoTab.setSelected(selectedTab == SelectedTab.Info);
        profilePostsTab.setSelected(selectedTab == SelectedTab.Posts);
    }
    
}
