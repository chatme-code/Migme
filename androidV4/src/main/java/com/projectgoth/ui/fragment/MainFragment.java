/**
 * Copyright (c) 2013 Project Goth
 *
 * MainFragment.java
 * Created Sep 3, 2014, 2:03:19 PM
 */

package com.projectgoth.ui.fragment;

import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.view.View;

import com.projectgoth.R;
import com.projectgoth.common.Config;
import com.projectgoth.common.ConnectionDetail;
import com.projectgoth.i18n.I18n;
import com.projectgoth.ui.activity.BaseCustomFragmentActivity;
import com.projectgoth.ui.activity.FragmentHandler;
import com.projectgoth.ui.adapter.InfiniteViewPagerAdapter;
import com.projectgoth.ui.fragment.ChatManagerFragment.ChatManagerTabSwitchListener;
import com.projectgoth.ui.widget.AllAccessButton;
import com.projectgoth.ui.widget.InfiniteViewPager;

/**
 * The main fragment that contains the three main screens as well as the All
 * Access Button.
 * 
 * @author angelorohit
 */
public class MainFragment extends BaseFragment implements ChatManagerTabSwitchListener, BaseFragment.FragmentLifecycleListener {
    
    public enum ViewPagerFragmentIndex {
        DISCOVER(0), FEEDS(1), CHATMANAGER(2);
        
        private int value;
        ViewPagerFragmentIndex(final int value) {
            this.value = value;
        }
        
        public int getValue() {
            return value;
        }
    }
    
    private class ShowFragmentState {

        private BaseFragment           fragmentToShow;
        private ViewPagerFragmentIndex index;
        private boolean                smoothScroll;
        private boolean                shouldShowChatroomsTab;

        public ShowFragmentState(final BaseFragment fragmentToShow, final ViewPagerFragmentIndex index,
                final boolean smoothScroll, final boolean shouldShowChatroomsTab) {
            this.fragmentToShow = fragmentToShow;
            this.index = index;
            this.smoothScroll = smoothScroll;
            this.shouldShowChatroomsTab = shouldShowChatroomsTab;
        }
    }
    
    private class UpdateActionBarForFragmentState {
        private BaseFragment fragmentToUpdate;
        private int          index;
        
        public UpdateActionBarForFragmentState(final BaseFragment fragmentToUpdate, final int index) {
            this.fragmentToUpdate = fragmentToUpdate;
            this.index = index;
        }
    }

    /**
     * The all-access button that will be used for navigation within the
     * MainFragment.
     */
    private AllAccessButton    allAccessButton;

    /**
     * A list of the fragments that have been put into the view pager.
     */
    private List<BaseFragment> pagerFragments;

    /**
     * The {@link InfiniteViewPager} that is used in this fragment to cycle
     * between fragments.
     */
    private InfiniteViewPager  pager                                            = null;

    private static int         DEFAULT_INITIAL_DISPLAY_PAGER_INDEX              = 1;
    
    private ShowFragmentState               showFragmentState                   = null;
    private UpdateActionBarForFragmentState updateActionBarForFragmentState     = null;

    /**
     * This routine automatically updates the action bar based on the fragment at the given index in the View Pager.
     *
     * @param The index of the fragment which will decide the action bar look and feel.
     */
    private void updateActionBarForItem(int index) {
        BaseFragment fragmentToUpdate = pagerFragments.get(index);
        if (fragmentToUpdate.wasViewCreated()) {
            updateActionBarForFragmentState = null;
            ((BaseCustomFragmentActivity) getActivity()).updateActionBarForFragment(fragmentToUpdate);
        } else {
            updateActionBarForFragmentState = new UpdateActionBarForFragmentState(fragmentToUpdate, index);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        setShouldUpdateActionBarOnAttach(false);
        super.onAttach(activity);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        setShouldUpdateActionBarOnAttach(false);

        // Create pager fragments
        pagerFragments = createPagerFragments();
        final int fragmentCount = pagerFragments.size();

        // Create pager adapter (who holds and manages the fragments)
        InfiniteViewPagerAdapter pagerAdapter = new InfiniteViewPagerAdapter(getChildFragmentManager());
        pagerAdapter.setPagerFragments(pagerFragments);

        // Setup the pager
        pager = (InfiniteViewPager) view.findViewById(R.id.pager);
        pager.addOnPageChangeListener(new SimpleOnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                pauseWebViewTimer(position);
                updateActionBarForItem(position);
            }
        });
        pager.setOffscreenPageLimit(fragmentCount);
        pager.setAdapter(pagerAdapter);
        pager.setAllowSwiping(false);

        // Setup All Access Button
        allAccessButton = (AllAccessButton) view.findViewById(R.id.all_access_button);
        allAccessButton.setViewPager(pager);

        // By default, set miniblog as current page
        pager.setCurrentItem(DEFAULT_INITIAL_DISPLAY_PAGER_INDEX, false);
    }

    public void showFragmentAtIndex(final ViewPagerFragmentIndex index, final boolean smoothScroll,
                                    final boolean shouldShowChatroomsTab) {
        final BaseFragment fragment = getPagerFragmentForIndex(index);
        if (fragment != null) {
            if (fragment.wasViewCreated()) {
                if (index.equals(ViewPagerFragmentIndex.CHATMANAGER)) {
                    final ChatManagerFragment chatManagerFragment = (ChatManagerFragment) fragment;
                    if (shouldShowChatroomsTab) {
                        chatManagerFragment.showChatroomList();
                    } else {
                        chatManagerFragment.showChatList();
                    }
                }

                final int fragmentPos = getPositionOfPagerFragmentForIndex(index);
                if (fragmentPos != -1) {
                    pager.setCurrentItem(fragmentPos, smoothScroll);
                }

                showFragmentState = null;
            } else {
                showFragmentState = new ShowFragmentState(fragment, index, smoothScroll, shouldShowChatroomsTab);
            }
        }
    }

    private BaseFragment getPagerFragmentForIndex(final ViewPagerFragmentIndex index) {
        final Class<?> clsType = getClassTypeForFragmentOfIndex(index);
        if (clsType != null) {
            for (BaseFragment fragment : pagerFragments) {
                if (fragment.getClass().equals(clsType)) {
                    return fragment;
                }
            }
        }

        return null;
    }

    private int getPositionOfPagerFragmentForIndex(final ViewPagerFragmentIndex index) {
        final Class<?> clsType = getClassTypeForFragmentOfIndex(index);
        if (clsType != null) {
            int count = 0;
            for (BaseFragment fragment : pagerFragments) {
                if (fragment.getClass().equals(clsType)) {
                    return count;
                }
                ++count;
            }
        }

        return -1;
    }

    private Class<?> getClassTypeForFragmentOfIndex(final ViewPagerFragmentIndex index) {
        switch (index) {
            case DISCOVER:
                return BrowserFragment.class;
            case FEEDS:
                return PostListFragment.class;
            case CHATMANAGER:
                return ChatManagerFragment.class;
            default:
                return null;
        }
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        final int position = pager.getCurrentItem();
        allAccessButton.updateContextMenuForFragmentAtSelectedIndex(position);
        updateActionBarForItem(position);
    }

    private List<BaseFragment> createPagerFragments() {
        List<BaseFragment> pagerFragments = new LinkedList<BaseFragment>();

        final ConnectionDetail detail = Config.getInstance().getConnectionDetail();
        final BaseFragment browserFragment = FragmentHandler.getInstance().getBrowserFragment(detail.getDiscoverServer(),
                true, null, null, I18n.tr("Explore"), R.drawable.ad_explore_white);
        pagerFragments.add(browserFragment);

        final BaseFragment feedsListFragment = FragmentHandler.getInstance().getFeedsListFragment();
        pagerFragments.add(feedsListFragment);

        final ChatManagerFragment chatManagerFragment = FragmentHandler.getInstance().getChatManagerFragment();
        chatManagerFragment.setTabSwitchListener(this);
        pagerFragments.add(chatManagerFragment);

        for (BaseFragment fragment : pagerFragments) {
            fragment.setFragmentLifecycleListener(this);
            fragment.setShouldUpdateActionBarOnAttach(false);
        }

        return pagerFragments;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_main;
    }

    @Override
    public void onChatManagerTabSwitched(int position) {
        int currItem = pager.getCurrentItem();
        allAccessButton.updateContextMenuForFragmentAtSelectedIndex(currItem);
        updateActionBarForItem(currItem);
    }

    @Override
    public void onViewCreated(BaseFragment fragment) {
        // Check if the MainFragment is supposed to go to the fragment that was just created.
        if (showFragmentState != null &&
                showFragmentState.fragmentToShow.getClass().equals(fragment.getClass())) {
            showFragmentAtIndex(
                    showFragmentState.index, showFragmentState.smoothScroll, showFragmentState.shouldShowChatroomsTab);
        } else if (updateActionBarForFragmentState != null &&
                updateActionBarForFragmentState.fragmentToUpdate.getClass().equals(fragment.getClass())) {
            updateActionBarForItem(updateActionBarForFragmentState.index);
        }
    }

    public void pauseWebViewTimer(int selectedIndex) {
        if (pagerFragments == null) {
            InfiniteViewPagerAdapter adapter = ((InfiniteViewPagerAdapter) pager.getAdapter());
            pagerFragments = adapter.getPagerFragments();
        } else {
            BaseFragment fragment = pagerFragments.get(selectedIndex);
            if (fragment instanceof BrowserFragment) {
                ((BrowserFragment) fragment).manualResumeTimer();
            } else {
                for (int i = 0; i < pagerFragments.size(); i++) {
                    BaseFragment tempFragment = pagerFragments.get(i);
                    if (tempFragment instanceof BrowserFragment) {
                        ((BrowserFragment) tempFragment).manualPauseTimer(true);
                        break;
                    }
                }
            }

        }
    }

}
