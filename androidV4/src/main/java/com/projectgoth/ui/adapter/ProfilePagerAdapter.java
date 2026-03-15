/**
 * Copyright (c) 2013 Project Goth
 *
 * ProfilePagerAdapter.java
 * Created Aug 21, 2013, 2:56:51 PM
 */

package com.projectgoth.ui.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.SparseArray;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

import com.projectgoth.b.data.Privacy;
import com.projectgoth.b.data.Profile;
import com.projectgoth.b.enums.EveryoneOrFollowerAndFriendPrivacyEnum;
import com.projectgoth.enums.ViewPagerType;
import com.projectgoth.i18n.I18n;
import com.projectgoth.model.ViewPagerItem;
import com.projectgoth.ui.fragment.BaseFragment;
import com.projectgoth.ui.widget.PagerSlidingTabStrip.CustomTabProvider;
import com.projectgoth.util.FragmentUtils;

/**
 * @author dangui
 * 
 */
public class ProfilePagerAdapter extends BasePagerAdapter<ViewPagerItem> implements CustomTabProvider {

    private Profile                                 profile;

    private SparseArray<HeaderPlaceHolderInterface> headerInterfaces;

    private PagerScrollListener                     pagerScrollListener;

    private HeaderDataProvider                      headerDataProvider;

    public abstract class PagerScrollListener implements OnScrollListener {

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount, -1);
        }

        public abstract void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount,
                int position);
    }

    /**
     * Header placeholder interface to interact with view pager items
     * 
     * @author warrenbalcos
     */
    public interface HeaderPlaceHolderInterface {

        public void updatePlaceholderHeader(int height);

        public View getHeaderPlaceholder();

        public void adjustScroll(int minTranslation, int scrollHeight);
    }

    public interface HeaderDataProvider {

        public int getHeaderSize();
    }

    public ProfilePagerAdapter(FragmentManager fm, Context context) {
        super(fm, context);

        headerInterfaces = new SparseArray<HeaderPlaceHolderInterface>();
    }

    @Override
    protected ArrayList<ViewPagerItem> createItemList() {
        ArrayList<ViewPagerItem> items = new ArrayList<ViewPagerItem>();

        ViewPagerItem item = null;
        if (profile != null) {
            item = new ViewPagerItem(I18n.tr("Info"), ViewPagerType.PROFILE_INFO);
            item.setPagerScrollListener(pagerScrollListener);
            item.setHasHeaderPlaceHolder(true);
            items.add(item);

            item = new ViewPagerItem(I18n.tr("Posts"), ViewPagerType.PROFILE_POST_LIST);
            item.setPagerScrollListener(pagerScrollListener);
            item.setHasHeaderPlaceHolder(true);
            items.add(item);
        }

        return items;
    }

    @Override
    public View getCustomTabView(int position) {
        String title = getPageTitle(position).toString();
        return createPageOption(title, 0, 0, position);
    }

    @Override
    public Fragment getItem(int position) {
        BaseFragment fragment = null;

        if (profile == null) {
            return null;
        }

        ViewPagerItem item = getPagerItem(position);

        Bundle args = new Bundle();
        switch (item.getType()) {
            case PROFILE_POST_LIST:
                args.putString(FragmentUtils.PARAM_USERID, String.valueOf(profile.getId()));
                args.putString(FragmentUtils.PARAM_USERNAME, profile.getUsername());
                args.putInt(FragmentUtils.PARAM_NUMOFPOSTS, profile.getNumOfPosts());

                Privacy privacy = profile.getPrivacy();
                EveryoneOrFollowerAndFriendPrivacyEnum feed = EveryoneOrFollowerAndFriendPrivacyEnum.EVERYONE;
                if (privacy != null) {
                    feed = privacy.getFeed();
                }
                args.putInt(FragmentUtils.PARAM_FEEDPRIVACY, feed.value());
                break;
            case PROFILE_INFO:
                args.putString(FragmentUtils.PARAM_USERNAME, profile.getUsername());
            default:
                break;
        }

        item.setArgs(args);
        item.setPostion(position);
        fragment = FragmentUtils.getFragmentByType(item);
        headerInterfaces.put(position, fragment.getHeaderPlaceHolderImplementation());
        return fragment;
    }

    /**
     * Returns the index of the first {@link ViewPagerItem} that matches the
     * given {@link ViewPagerType}
     * 
     * @param viewPagerType
     *            The {@link ViewPagerType} to be matched.
     * @return The index of the matched {@link ViewPagerItem} or -1 if no match
     *         was found.
     */
    public int getItemPositionWithViewPagerType(final ViewPagerType viewPagerType) {
        int result = -1;
        if (viewPagerType != null) {
            for (int i = 0; i < items.size(); ++i) {
                ViewPagerItem viewPagerItem = items.get(i);
                if (viewPagerItem != null && viewPagerItem.getType() == viewPagerType) {
                    return i;
                }
            }
        }

        return result;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return items.get(position).getLabel();
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
        notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetChanged() {
        if (profile != null) {
            items = createItemList();
            setPagerItemList(items);
            super.notifyDataSetChanged();
        }
        if (headerDataProvider != null) {
            updatePlaceholderHeader(headerDataProvider.getHeaderSize());
        }
    }

    public void initPlaceHolderHeader() {
        if (headerDataProvider != null) {
            updatePlaceholderHeader(headerDataProvider.getHeaderSize());
        }
    }

    @Override
    public void onPositionChanged(int newPosition) {
    }

    public void setPagerScrollListener(PagerScrollListener pagerScrollListener) {
        this.pagerScrollListener = pagerScrollListener;
    }

    private void updatePlaceholderHeader(int height) {
        int size = headerInterfaces.size();
        for (int i = 0; i < size; i++) {
            HeaderPlaceHolderInterface item = headerInterfaces.get(i);
            if (item != null) {
                item.updatePlaceholderHeader(height);
            }
        }
    }

    public View getCurrentHeaderPlaceholder() {
        HeaderPlaceHolderInterface item = headerInterfaces.get(getCurrentPos());
        View view = null;
        if (item != null) {
            view = item.getHeaderPlaceholder();
        }
        return view;
    }

    /**
     * @param headerDataProvider
     *            the headerDataProvider to set
     */
    public void setHeaderDataProvider(HeaderDataProvider headerDataProvider) {
        this.headerDataProvider = headerDataProvider;
    }

    public void adjustScroll(int minTranslation, int scrollHeight) {
        int size = headerInterfaces.size();
        for (int i = 0; i < size; i++) {
            HeaderPlaceHolderInterface item = headerInterfaces.get(i);
            if (item != null && !isCurrentPosition(i)) {
                item.adjustScroll(minTranslation, scrollHeight);
            }
        }
    }
    
    private boolean isCurrentPosition(int position) {
        return getCurrentPos() == position;
    }

}
