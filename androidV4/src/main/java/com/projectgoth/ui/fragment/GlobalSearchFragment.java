/**
 * Copyright (c) 2013 Project Goth
 *
 * SearchResultsFragment.java
 * Created Aug 12, 2013, 6:07:22 PM
 */

package com.projectgoth.ui.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import com.projectgoth.R;
import com.projectgoth.common.Constants;
import com.projectgoth.common.Tools;
import com.projectgoth.common.WebURL;
import com.projectgoth.events.GAEvent;
import com.projectgoth.i18n.I18n;
import com.projectgoth.listener.SectionUpdateListener;
import com.projectgoth.model.StorePagerItem;
import com.projectgoth.ui.activity.CustomActionBarConfig;
import com.projectgoth.ui.activity.CustomActionBarConfig.NavigationButtonState;
import com.projectgoth.ui.activity.FragmentHandler;

/**
 * @author cherryv
 * 
 */
public class GlobalSearchFragment extends BaseSearchFragment implements SectionUpdateListener {

    public static final int CONTAINER_VIEW_ID = R.id.search_results;
    
    public static final String  PARAM_SEARCH_TYPE        = "PARAM_SEARCH_TYPE";
    
    private SearchType          mSearchType = SearchType.PEOPLE;
    private TextView            mSectionTitle;
    
    public enum SearchType {
        PEOPLE(0), POST(1), CHATROOM(2), GROUP(3), TOPIC(4), GIFT(5), STICKER(6), EMOTICON(7), AVATAR(8);

        private int value;

        private SearchType(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }

        public static SearchType fromValue(int value) {
            for (SearchType searchType : values()) {
                if (searchType.getValue() == value) {
                    return searchType;
                }
            }
            return PEOPLE;
        }
    };
    
    @Override
    protected void readBundleArguments(Bundle args) {
        super.readBundleArguments(args);

        mSearchType = SearchType.fromValue(args.getInt(PARAM_SEARCH_TYPE, 0));        
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_search_results;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSectionTitle = (TextView) view.findViewById(R.id.section_title);
        checkAndPerformGlobalSearch(getFilterText());
    }

    private void displayPeopleSearchResults(String searchString) {
        setSectionCount(0);
        ProfileListFragment fragment = FragmentHandler.getInstance().getSearchedUsersFragment(searchString);
        fragment.setShouldUpdateActionBarOnAttach(false);
        fragment.setSectionUpdateListener(this);
        addChildFragment(CONTAINER_VIEW_ID, fragment);
    }

    private void displayPostSearchResults(String searchString) {
        setSectionCount(0);
        PostListFragment fragment = FragmentHandler.getInstance().getSearchedPostFragment(searchString);
        fragment.setShouldUpdateActionBarOnAttach(false);
        fragment.setSectionUpdateListener(this);
        addChildFragment(CONTAINER_VIEW_ID, fragment);
    }

    private void displayTopicSearchResults(String searchString) {
        setSectionCount(0);
        PostListFragment fragment = FragmentHandler.getInstance().getHotTopicResultsFragment(searchString);
        fragment.setShouldUpdateActionBarOnAttach(false);
        fragment.setSectionUpdateListener(this);
        addChildFragment(CONTAINER_VIEW_ID, fragment);
    }

    private void displayStoreSearchResults(StorePagerItem.StorePagerType type, String searchString) {
        setSectionCount(0);
        StoreSearchFragment fragment = FragmentHandler.getInstance().getStoreSearchResultsFragment(
                type, searchString, null);
        fragment.setShouldUpdateActionBarOnAttach(false);
        fragment.setSectionUpdateListener(this);
        addChildFragment(CONTAINER_VIEW_ID, fragment);
    }

    private void displayChatroomSearchResults(String searchString) {
        GAEvent.Chat_GlobalChatroomSearch.send();
        String url = String.format(WebURL.URL_CHATROOM_SEARCH, searchString, searchString);
        BrowserFragment browserFragment = FragmentHandler.getInstance().getBrowserFragment(url);
        browserFragment.setShouldUpdateActionBarOnAttach(false);
        addChildFragment(CONTAINER_VIEW_ID, browserFragment);
        setSectionTitle(null);
    }
    
    /*+
    private void displayGroupSearchResults(String searchString) {
        String url = String.format(WebURL.URL_SEARCH_GROUP, searchString);
        BrowserFragment browserFragment = FragmentHandler.getInstance().getBrowserFragment(url);
        addChildFragment(ContainerViewId, browserFragment);
    }
    */

    @Override
    public CustomActionBarConfig getActionBarConfig() {
        CustomActionBarConfig config = super.getActionBarConfig();
        config.setNavigationButtonState(NavigationButtonState.BACK);
        return config;
    }
    
    @Override
    protected void performGlobalSearch(final String searchString) {
        super.performGlobalSearch(searchString);
        switch (mSearchType) {
            case PEOPLE:
                displayPeopleSearchResults(searchString);
                break;
            case POST:
                displayPostSearchResults(searchString);
                break;
            case CHATROOM:
                displayChatroomSearchResults(searchString);
                break;
            case GROUP:
                //+ displayGroupSearchResults(searchString);
                break;
            case TOPIC:
                displayTopicSearchResults(searchString);
                break;
            case GIFT:
                displayStoreSearchResults(StorePagerItem.StorePagerType.GIFTS, searchString);
                break;
            case STICKER:
                displayStoreSearchResults(StorePagerItem.StorePagerType.STICKERS, searchString);
                break;
        }

        Tools.showToast(getActivity(), I18n.tr("Searching"));
    }
    
    @Override
    public void setSectionCount(final int count) {
        final String sectionName = getSectionNameForType(mSearchType);
        if (!TextUtils.isEmpty(sectionName)) {
            setSectionTitle(String.format("%s (%d)", sectionName, count));
        }
    }
    
    private static String getSectionNameForType(final SearchType searchType) {
        switch (searchType) {
            case PEOPLE:
                return I18n.tr("People");
            case POST:
                return I18n.tr("Posts");
            case TOPIC:
                return I18n.tr("Topics");
            case GIFT:
                return I18n.tr("Gifts");
            case STICKER:
                return I18n.tr("Stickers");
            case EMOTICON:
                return I18n.tr("Emoticons");
            case AVATAR:
                return I18n.tr("Avatar");
            default:
                return Constants.BLANKSTR;
        }
    }
    
    private void setSectionTitle(final String title) {
        if (TextUtils.isEmpty(title)) {
            mSectionTitle.setVisibility(View.GONE);
        } else {
            mSectionTitle.setText(title);
            mSectionTitle.setVisibility(View.VISIBLE);
        }
    }

}
