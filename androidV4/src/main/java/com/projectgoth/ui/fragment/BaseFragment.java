/**
 * Copyright (c) 2013 Project Goth
 *
 * BaseFragment.java.java
 * Created May 30, 2013, 12:34:53 AM
 */

package com.projectgoth.ui.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract.Events;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.common.Constants;
import com.projectgoth.common.Logger;
import com.projectgoth.common.Tools;
import com.projectgoth.controller.ProgressDialogController;
import com.projectgoth.controller.StatusBarController;
import com.projectgoth.i18n.I18n;
import com.projectgoth.model.MenuOption;
import com.projectgoth.notification.NotificationType;
import com.projectgoth.service.NetworkService;
import com.projectgoth.ui.activity.ActionHandler;
import com.projectgoth.ui.activity.BaseCustomFragmentActivity;
import com.projectgoth.ui.activity.BaseFragmentActivity;
import com.projectgoth.ui.activity.CustomActionBar.CustomActionBarListener;
import com.projectgoth.ui.activity.CustomActionBarConfig;
import com.projectgoth.ui.activity.CustomActionBarConfig.OverflowButtonState;
import com.projectgoth.ui.activity.CustomActionBarSupporter;
import com.projectgoth.ui.activity.FragmentHandler;
import com.projectgoth.ui.activity.MainDrawerLayoutActivity;
import com.projectgoth.ui.activity.MenuBarAnimation;
import com.projectgoth.ui.activity.MenuBarAnimation.AnimationType;
import com.projectgoth.ui.adapter.ProfilePagerAdapter.HeaderPlaceHolderInterface;
import com.projectgoth.ui.adapter.ProfilePagerAdapter.PagerScrollListener;
import com.projectgoth.ui.widget.ButtonEx;
import com.projectgoth.ui.widget.GifImageView;
import com.projectgoth.ui.widget.PopupMenu.OnPopupMenuListener;
import com.projectgoth.ui.widget.allaccessbutton.PageData;
import com.projectgoth.ui.widget.util.ButtonUtil;

/**
 * @author cherryv
 * 
 */
public abstract class BaseFragment extends Fragment implements OnScrollListener,
        CustomActionBarSupporter, CustomActionBarListener, OnPopupMenuListener {
    
    public interface FragmentLifecycleListener {
        public void onViewCreated(final BaseFragment fragment);
    }

    protected FragmentEventListener     eventListener;
    private FragmentReceiver            mReceiver;

    private boolean                     isAtBottom                    = false;
    private boolean                     refreshOnScrollingStop        = false;
    private boolean                     isScrolling                   = false;

    private boolean                     shouldUpdateActionBarOnCreate = true;

    // scroll direction variables
    // ------
    private int                         oldTop;
    private int                         oldFirstVisibleItem;
    // ------

    private MenuBarAnimation            bottomBarMenuAnim;
    private boolean                     isBottomBarAutoHide           = true;

    private TextView                    title;
    private ImageView                   titleIcon;
    // used as footer or header view showing data is loading in many screens
    private View                        loadingMoreView;

    protected boolean                   wasViewCreated                = false;
    protected FragmentLifecycleListener fragmentLifecycleListener     = null;

    // View Pager related variables
    private PagerScrollListener         pagerScrollListener;
    private int                         pagerPosition                 = -1;

    public GifImageView                 mProgressImage;

    private boolean                     isShowingLoadingMore = false;

    // no connection disable buttons
    private List<ButtonEx>              mNoConnectionDisableButtonList = new ArrayList<ButtonEx>();
    private List<ImageButton>           mNoConnectionDisableImageButtonList = new ArrayList<ImageButton>();

    /**
     * Override this method to read data from bundle arguments.
     * 
     * @param bundleArgs
     *            The {@link android.os.Bundle} containing arguments from which
     *            data is to be read.
     */
    protected void readBundleArguments(final Bundle bundleArgs) {
        // Do nothing here.
    }

    @Override
    public void onDestroyView() {
        wasViewCreated = false;
        super.onDestroyView();
    }
    
    public boolean wasViewCreated() {
        return wasViewCreated;
    }
    
    public void setFragmentLifecycleListener(final FragmentLifecycleListener listener) {
        fragmentLifecycleListener = listener;
    }

    protected abstract int getLayoutId();

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Note: don't update the action bar here because onAttach is called
        // before readBundleArguments and it will get updated incorrectly.
        // Instead, the action bar is now updated in onViewCreated.
        //   updateActionBar(activity);

        if (activity instanceof FragmentEventListener) {
            eventListener = (FragmentEventListener) activity;
        } else {
            // TODO: Change all throwing and handling of exceptions
            throw new ClassCastException(activity.toString() + " must implement FragmentEventListener");
        }
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	// Send GA screen view.
        Tracker tracker = ApplicationEx.getTracker();
        if (tracker != null) {
            tracker.setScreenName(this.getClass().getSimpleName());
            tracker.send(new HitBuilders.AppViewBuilder().build());
        }
    	
        return inflater.inflate(getLayoutId(), container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        final Bundle args = getArguments();
        if (args != null) {
            readBundleArguments(args);
//            args.clear();
        }

        if (shouldUpdateActionBarOnCreate) {
            updateActionBar(getActivity());
        }

        mProgressImage = (GifImageView) view.findViewById(R.id.progress);
        if (mProgressImage != null) {
            mProgressImage.setGifId(R.drawable.ad_alien_load);
        }
    }
    
    protected void invokeOnViewCreated() {
        wasViewCreated = true;
        if (fragmentLifecycleListener != null) {
            fragmentLifecycleListener.onViewCreated(this);
        }
    }

    /**
     * Helper method to update the action bar if the data is not yet ready 
     * when the fragment is attached. 
     */
    protected void updateActionBar(final Activity activity) {
        if (activity instanceof BaseCustomFragmentActivity) {
            ((BaseCustomFragmentActivity) activity).updateActionBarForFragment(this);
        }
    }

    @Override
    public void onResume() {
        // Logger.debug.logWithTrace(LogUtils.TAG_MAIN_UI, getClass());
        super.onResume();

        // TODO: Investigate/optimize later
        // if this fragment is added to a ViewPager, onResume() will be called
        // even if this fragment is not yet the currently visible fragment (e.g.
        // it's the fragment before or after the current visible fragment).
        // what we can do in the future is check getUserVisibleHint() method
        // first before calling onShowFragment. we encounter a problem with
        // swiping though as onResume() and onPause() will not be re-called.
        onShowFragment();

        // reset the value to fix a bug, if we go to leave the screen
        // immediately after the bar sliding down
        // and open the screen again, the slide down cannot be triggered when
        // the first scroll happens
    }

    @Override
    public void onPause() {
        // Logger.debug.logWithTrace(LogUtils.TAG_MAIN_UI, getClass());
        super.onPause();
        onHideFragment();
    }

    @Override
    public void onStop() {
        super.onStop();
        hideLoadProgressDialog();
    }

    @Override
    public void onDestroy() {
        // Logger.debug.logWithTrace(LogUtils.TAG_MAIN_UI, getClass());
        super.onDestroy();
        eventListener = null;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (eventListener != null) {
            eventListener.onDetachFragment(getTag());
        }
    }

    protected void onShowFragment() {
        // Logger.debug.logWithTrace(LogUtils.TAG_MAIN_UI, getClass(),
        //  "isVisible: ", isVisible(), ": userHint: ", getUserVisibleHint());
        registerReceivers();
        updateNotifications();
        if (eventListener != null) {
            eventListener.onShowFragment(this);
        }

        updateButtonClickableStatusAccordingToNetworkStatus();

    }

    protected void onHideFragment() {
        //Logger.debug.logWithTrace(LogUtils.TAG_MAIN_UI, getClass(), "isVisible: ",
        //      isVisible(), ": userHint: ", getUserVisibleHint());
        unregisterReceiver();
        if (eventListener != null) {
            eventListener.onHideFragment(this);
        }
    }

    /**
     * Utility method for registering broadcast events to the parent Activity
     * 
     * @param event
     */
    protected void registerEvent(String event) {
        if (mReceiver == null) {
            mReceiver = new FragmentReceiver();
        }

        BaseFragmentActivity activity = (BaseFragmentActivity) getActivity();
        if (activity != null) {
            LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(activity);
            localBroadcastManager.registerReceiver(mReceiver, new IntentFilter(event));
        }
    }

    /**
     * Automatically called when Fragment is hidden ({@link #onPause()}) Utility
     * method for unregistering the broadcast receiver from parent Activity
     */
    private void unregisterReceiver() {
        BaseFragmentActivity activity = (BaseFragmentActivity) getActivity();
        if (activity != null && mReceiver != null) {
            LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(activity);
            localBroadcastManager.unregisterReceiver(mReceiver);
        }
    }

    /**
     * Automatically called when Fragment becomes visible ({@link #onResume()})
     * Implement to register broadcast events that will be handled by this
     * fragment Call {@link #registerEvent(String)} to add events that fragment
     * should handle <code>registerEvent(Events.EVENT_CHAT_DATA_CHANGED)</code>
     */
    protected void registerReceivers() {}

    /**
     * Implement to handle different broadcast events that the fragment
     * registered to listen to
     * 
     * @param context
     * @param intent
     */
    protected void onReceive(Context context, Intent intent) {}

    public class FragmentReceiver extends BroadcastReceiver {

        public void onReceive(Context context, Intent intent) {
            BaseFragment.this.onReceive(context, intent);
        }
    }

    /**
     * Helper method to add another fragment inside this fragment
     * 
     * @param containerViewId
     *            Layout view id where the child fragment will be placed
     * @param childFragment
     *            Child fragment to add
     */
    protected void addChildFragment(int containerViewId, BaseFragment childFragment) {
        if (childFragment != null) {
            FragmentManager childManager = getChildFragmentManager();
            FragmentTransaction fragmentTransaction = childManager.beginTransaction();
            fragmentTransaction.replace(containerViewId, childFragment, FragmentHandler.getInstance()
                    .generateFragmentId(childFragment));
            fragmentTransaction.commitAllowingStateLoss();
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T getParent(Fragment frag, Class<T> callbackInterface) {
        Fragment parentFragment = frag.getParentFragment();
        if (parentFragment != null && callbackInterface.isInstance(parentFragment)) {
            return (T) parentFragment;
        } else {
            FragmentActivity activity = frag.getActivity();
            if (activity != null && callbackInterface.isInstance(activity)) {
                return (T) activity;
            }
        }
        return null;
    }

    protected void closeFragment() {
        FragmentActivity activity = getActivity();
        if (activity != null) {
            FragmentManager manager = activity.getSupportFragmentManager();
            if (manager.findFragmentByTag(getTag()) != null) {
                activity.onBackPressed();
            }
        }
    }

    /**
     * Default implementation of handling the
     * {@link Events#STATUS_NOTIFICATION_AVAILABLE} event. By default, this will
     * show the status notification in the notification manager.
     * 
     * Other fragments can override this method if there is special condition
     * required before a status notification is displayed. For example, if the
     * user is in the home list fragment, then no new notifications for chat
     * should be displayed.
     */
    public void handleNotificationAvailable(NotificationType type, String notificationId) {
        ApplicationEx.getInstance().getNotificationHandler().showStatusNotification();
    }

    /**
     * Called whenever the fragment is displayed. This is convenience method for
     * updating notifications (usually removing them) when a specific fragment
     * is already displayed. By default it does nothing, but fragments can
     * override this method if needed.
     * 
     * For example, {@link ChatListFragment} would need to remove all
     * chat-related notifications whenever it is displayed.
     */
    protected void updateNotifications() {
        // DO NOTHING
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

        if (pagerScrollListener != null) {
            pagerScrollListener.onScrollStateChanged(view, scrollState);
        }
        
        setScrolling(scrollState != SCROLL_STATE_IDLE);

        if (refreshOnScrollingStop && scrollState == SCROLL_STATE_IDLE) {
            onListEndReached();
            refreshOnScrollingStop = false;
        }
    }

    protected void onListEndReached() {
    }

    private void setScrolling(boolean isScrolling) {
        this.isScrolling = isScrolling;
    }

    public boolean isScrolling() {
        return isScrolling;
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        
        if (pagerScrollListener != null) {
            pagerScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount, pagerPosition);
        }
        
        if ((firstVisibleItem + visibleItemCount) == totalItemCount && totalItemCount != 0) {
            if (!isAtBottom && isScrolling()) {
                isAtBottom = true;
                // from not at bottom to at bottom
                refreshOnScrollingStop = true;
            }
        } else {
            isAtBottom = false;
        }

        detectScroll(view, firstVisibleItem);
    }

    private void detectScroll(AbsListView absListView, int firstVisibleItem) {
        View view = absListView.getChildAt(0);
        int top = (view == null) ? 0 : view.getTop();

        if (firstVisibleItem == oldFirstVisibleItem) {
            if (top > oldTop) {
                doScrollUp();
            } else if (top < oldTop) {
                doScrollDown();
            }
        } else {
            if (firstVisibleItem < oldFirstVisibleItem) {
                doScrollUp();
            } else {
                doScrollDown();
            }
        }

        oldTop = top;
        oldFirstVisibleItem = firstVisibleItem;
    }

    private void doScrollDown() {
        if (bottomBarMenuAnim != null && isBottomBarAutoHide) {
            bottomBarMenuAnim.hide();
        }
        onScrollDown();
    }
    
    private void doScrollUp() {
        if (bottomBarMenuAnim != null) {
            bottomBarMenuAnim.show();
        }
        onScrollUp();
    }
    
    protected void onScrollUp(){}

    protected void onScrollDown() {}

    protected void setBottomBarMenu(View bottomMenuBar) {
        if (bottomMenuBar != null) {
            if (bottomBarMenuAnim == null) {
                bottomBarMenuAnim = new MenuBarAnimation(bottomMenuBar);
                bottomBarMenuAnim.setShowAnimation(AnimationType.SLIDE_FROM_BOTTOM);
                bottomBarMenuAnim.setHideAnimation(AnimationType.SLIDE_TO_BOTTOM);
            } else {
                bottomBarMenuAnim.setView(bottomMenuBar);
            }
            bottomMenuBar.setVisibility(View.VISIBLE);
            bottomBarMenuAnim.show();
        }
    }

    /**
     * @return the isBottomBarAutoHide
     */
    public boolean isBottomBarAutoHide() {
        return isBottomBarAutoHide;
    }

    /**
     * @param isBottomBarAutoHide the isBottomBarAutoHide to set
     */
    public void setBottomBarAutoHide(boolean isBottomBarAutoHide) {
        this.isBottomBarAutoHide = isBottomBarAutoHide;
    }

    @Override
    public CustomActionBarConfig getActionBarConfig() {
        CustomActionBarConfig config = new CustomActionBarConfig();
        config.setCustomViewLayoutSrc(R.layout.action_bar_default);
        return config;
    }

    @Override
    public CustomActionBarListener getCustomActionBarListener() {
        return this;
    }

    @Override
    public OnPopupMenuListener getPopupMenuListener() {
        return null;
    }
    
    @Override
    public void onMenuOptionClicked(MenuOption menuOption) {
    }
    
    @Override
    public void onPopupMenuDismissed() {
    }
    
    @Override
    public ArrayList<MenuOption> getMenuOptions() {
        return null;
    }

    @Override
    public void onNavigationIconPressed() {
        Tools.hideVirtualKeyboard(getActivity());
        Activity activity = ApplicationEx.getInstance().getCurrentActivity();
        if (activity instanceof MainDrawerLayoutActivity) {
            ((MainDrawerLayoutActivity) activity).toggleDrawer();
        }
    }

    @Override
    public void onOverflowButtonPressed(final OverflowButtonState state) {
        if (state == OverflowButtonState.ALERT) {
            ActionHandler.getInstance().displayAlerts(null);
        }
    }

    @Override
    public void onBackIconPressed() {
        Activity activity = getActivity();
        if (activity != null) {
            activity.onBackPressed();
        }
    }
    
    @Override
    public void onSearchButtonPressed() {
        // do nothing
    }

    @Override
    public void initCustomViewInCustomActionBar(View customView) {
        // The default look of the action bar.
        titleIcon = (ImageView) customView.findViewById(R.id.ab_title_icon);
        title = (TextView) customView.findViewById(R.id.ab_title);

        updateIcon();
        updateTitle();
    }

    protected void updateTitle() {
        title.setText(getTitle());
    }

    protected void updateIcon() {
        final int titleIconRes = getTitleIcon();
        if (titleIconRes > 0 && titleIcon != null) {
            titleIcon.setImageResource(titleIconRes);
            titleIcon.setTag(titleIconRes);
        }
    }

    protected void showTitleIconAnimation() {
        Context context = ApplicationEx.getContext();
        if (titleIcon != null && context != null) {
            titleIcon.setAnimation(AnimationUtils.loadAnimation(context, R.anim.push_right_in));
        }
    }

    protected void showTitleAnimation() {
        Context context = ApplicationEx.getContext();
        if (title != null && context != null) {
            title.setAnimation(AnimationUtils.loadAnimation(context, R.anim.push_right_in));
        }
    }

    protected int getTitleIcon() {
        return R.drawable.ad_explore_white;
    }

    protected String getTitle() {
        return I18n.tr("mig");
    }

    protected String getCurrentTitle() {
        return title.getText().toString();
    }

    protected int getCurrentTitleIconTag() {
        Object tag = null;
        try{
            tag = titleIcon.getTag();
        }
        catch(NullPointerException e){
            StringBuilder builder = new StringBuilder();
            builder.append("getCustomViewLayoutSrc() of ActionBarConifg -> " + getActionBarConfig().getCustomViewLayoutSrc()+"\n");
            builder.append("getLayoutId() -> " + this.getLayoutId()+"\n");
            builder.append("R.layout.action_bar_default ID -> " + R.layout.action_bar_default+"\n");
            Logger.error.log(builder.toString(), e);
        } finally {
            if(tag != null) {
                return (Integer) tag;
            }
            return 0;
        }
    }

    protected void setTitle(CharSequence newTitle) {
        if(title != null) {
            title.setText(newTitle);
        }
    }

    protected void setTitleIcon(int iconResourceId) {
        if(titleIcon != null) {
            titleIcon.setImageResource(iconResourceId);
        }
    }

    public void setShouldUpdateActionBarOnAttach(final boolean state) {     //+ TODO: rename to setShouldUpdateActionBarOnCreate
        shouldUpdateActionBarOnCreate = state;
    }

    public PageData getPageData() {
        throw new UnsupportedOperationException("Function call must be overridden by subclass!");
    }

    protected void bindOnClickListener(View.OnClickListener listener, int... viewIds) {
        for (int id : viewIds) {
            View view = getView().findViewById(id);
            if(view != null) view.setOnClickListener(listener);
        }
    }

    /** create the loading view. It is to be added as header or footer
     * of a ListView before setAdapter called */
    protected View createLoadingView(boolean isFooterView) {
        loadingMoreView = LayoutInflater.from(getActivity()).inflate(R.layout.loading_more, null);

        //hide it by default
        hideLoadingMore(isFooterView);
        return loadingMoreView;
    }

    protected View createLoadingView() {
        return createLoadingView(true);
    }

    public boolean isShowingLoadingMore() {
        return isShowingLoadingMore;
    }

    /**
     * @param keepHeight
     *    if it is true the loading view is invisible with the same size it is shown, this is to
     *    prevent the list from jumping when it is shown and the height changed. if it false, then
     *    the height is 0
     */
    protected void hideLoadingMore(boolean keepHeight) {
        isShowingLoadingMore = false;

        ImageView loadingIcon = (ImageView) loadingMoreView.findViewById(R.id.loading_icon);
        loadingIcon.clearAnimation();

        if (keepHeight) {
            loadingMoreView.setVisibility(View.INVISIBLE);
        } else {
            // change the height of loading icon
            ViewGroup.LayoutParams params =  loadingIcon.getLayoutParams();
            params.height = 0;
            params.width = 0;
            loadingIcon.setLayoutParams(params);

            // change the height of header view
            loadingMoreView.setPadding(0,0,0,0);
            setLoadingMoreViewHeight(0);
            loadingMoreView.setVisibility(View.GONE);
        }

    }

    protected void hideLoadingMore() {
        hideLoadingMore(true);
    }

    protected void showLoadingMore() {
        int paddingTop = ApplicationEx.getDimension(R.dimen.normal_padding);
        int paddingBottom = ApplicationEx.getDimension(R.dimen.normal_padding);
        showLoadingMore(paddingTop, paddingBottom);
    }

    protected void showLoadingMore(int paddingTop, int paddingBottom) {
        ImageView loadingIcon = (ImageView) loadingMoreView.findViewById(R.id.loading_icon);

        // change the height of loading icon
        ViewGroup.LayoutParams params =  loadingIcon.getLayoutParams();
        params.height = ApplicationEx.getDimension(R.dimen.icon_height_xsmall);
        params.width = ApplicationEx.getDimension(R.dimen.icon_width_xsmall);
        loadingIcon.setLayoutParams(params);

        // change the height of header view
        loadingMoreView.setPadding(0, paddingTop, 0, paddingBottom);
        setLoadingMoreViewHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        loadingMoreView.setVisibility(View.VISIBLE);
        loadingIcon.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.loading_icon_rotate));

        isShowingLoadingMore = true;
    }

    private void setLoadingMoreViewHeight(int height) {
        ViewGroup.LayoutParams layoutParams = loadingMoreView.getLayoutParams();
        if(layoutParams == null) {
            layoutParams = new ListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height, ListView.ITEM_VIEW_TYPE_HEADER_OR_FOOTER);
            loadingMoreView.setLayoutParams(layoutParams);
        } else {
            layoutParams.height = height;
            loadingMoreView.requestLayout();
        }
    }

    /**
     * @param pagerScrollListener the pagerScrollListener to set
     */
    public void setPagerScrollListener(PagerScrollListener pagerScrollListener) {
        this.pagerScrollListener = pagerScrollListener;
    }

    /**
     * @return the pagerPosition
     */
    public int getPagerPosition() {
        return pagerPosition;
    }

    /**
     * @param pagerPosition the pagerPosition to set
     */
    public void setPagerPosition(int pagerPosition) {
        this.pagerPosition = pagerPosition;
    }

    /**
     * Method stub for the header place holders
     * 
     * TODO: need to incorporate this feature to the framework for reuse in other places as well. 
     * 
     * @return
     */
    public HeaderPlaceHolderInterface getHeaderPlaceHolderImplementation() {
        return null;
    }

    protected void showLoadProgressDialog() {
        ProgressDialogController.getInstance().showProgressDialog(getActivity(),
                ProgressDialogController.ProgressType.Loading);
    }

    protected void hideLoadProgressDialog() {
        ProgressDialogController.getInstance().hideProgressDialog();
    }

    /**
     *  this events are all for fetching complete data by mime data. e.g. we have only post id in
     *  post mime data, when displaying it, we fetch the complete Post data by the post id it
     */
    protected void registerDataFetchedByMimeDataEvents() {
        //for profile mime data
        registerEvent(com.mig33.diggle.events.Events.Profile.RECEIVED);
        //for post mime data
        registerEvent(com.mig33.diggle.events.Events.Post.SINGLE_POST_RECEIVED);
        //deezer mime data
        registerEvent(com.mig33.diggle.events.Events.DEEZER.FETCH_RADIO_COMPLETED);
        //flicker mime data
        registerEvent(com.mig33.diggle.events.Events.Mime.FETCH_FLICKR_COMPLETED);
        //chatroom info mime data
        registerEvent(com.mig33.diggle.events.Events.ChatRoom.FETCH_CHATROOM_INFO_SUCCESS);
        //soundcloud mime data
        registerEvent(com.mig33.diggle.events.Events.Mime.FETCH_OEMBED_COMPLETED);
    }

    protected boolean isCompleteDataForMimeDataFetched(String action) {
        return  (action.equals(com.mig33.diggle.events.Events.Profile.RECEIVED)
                ||action.equals(com.mig33.diggle.events.Events.Post.SINGLE_POST_RECEIVED)
                ||action.equals(com.mig33.diggle.events.Events.DEEZER.FETCH_RADIO_COMPLETED)
                ||action.equals(com.mig33.diggle.events.Events.Mime.FETCH_FLICKR_COMPLETED)
                ||action.equals(com.mig33.diggle.events.Events.Mime.FETCH_OEMBED_COMPLETED)
                ||action.equals(com.mig33.diggle.events.Events.ChatRoom.FETCH_CHATROOM_INFO_SUCCESS));
    }

    private void updateButtonClickableStatusAccordingToNetworkStatus() {
        NetworkService networkService = ApplicationEx.getInstance().getNetworkService();
        if (networkService != null) {
            if (networkService.isNetworkAvailable()) {
                enableNoConnectionDisableButton();
            } else {
                disableNoConnectionDisableButton();
            }
        }
    }

    protected void addButtonToNoConnectionDisableButtonList(final ButtonEx button) {
        if (this.mNoConnectionDisableButtonList != null) {
            this.mNoConnectionDisableButtonList.add(button);
        }
    }

    protected void addButtonToNoConnectionDisableButtonList(final ImageButton imageButton) {
        if (this.mNoConnectionDisableImageButtonList != null) {
            this.mNoConnectionDisableImageButtonList.add(imageButton);
        }
    }

    public void disableNoConnectionDisableButton() {
        if (mNoConnectionDisableButtonList != null) {
            for (ButtonEx button : mNoConnectionDisableButtonList) {
                button.setClickable(false);
                button.setType(ButtonUtil.BUTTON_TYPE_GRAY);
            }
        }

        if (mNoConnectionDisableImageButtonList != null) {
            for (ImageButton imageButton : mNoConnectionDisableImageButtonList) {
                imageButton.setClickable(false);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    imageButton.setAlpha(Constants.IMAGE_BUTTON_UNCLICK_ALPHA);
                }
            }
        }

    }

    public void enableNoConnectionDisableButton() {
        if (mNoConnectionDisableButtonList != null) {
            for (ButtonEx button : mNoConnectionDisableButtonList) {
                button.setClickable(true);
                button.setType(ButtonUtil.BUTTON_TYPE_ORANGE);
            }
        }

        if (mNoConnectionDisableImageButtonList != null) {
            for (ImageButton imageButton : mNoConnectionDisableImageButtonList) {
                imageButton.setClickable(true);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    imageButton.setAlpha(1f);
                }
            }
        }

    }

    public void showUpConnectionStatusBar() {
        StatusBarController.getInstance().showNoConnectionStatus(this);
    }

    public void dismissConnectionStatusBar() {
        StatusBarController.getInstance().dismissConnectionStatusBar(this);
    }

}
