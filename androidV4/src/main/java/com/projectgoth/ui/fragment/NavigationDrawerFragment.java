/**
 * Copyright (c) migme 2014
 *
 * NavigationDrawerFragment.java
 * Created Aug 20, 2014, 9:55:01 AM
 */

package com.projectgoth.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mig33.diggle.events.Events;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.Banner;
import com.projectgoth.b.data.Profile;
import com.projectgoth.blackhole.enums.PresenceType;
import com.projectgoth.common.Config;
import com.projectgoth.common.Constants;
import com.projectgoth.common.NUEManager;
import com.projectgoth.common.SharedPrefsManager;
import com.projectgoth.common.Tools;
import com.projectgoth.common.WebURL;
import com.projectgoth.controller.BannerController;
import com.projectgoth.controller.BannerController.Placement;
import com.projectgoth.controller.FriendsController;
import com.projectgoth.controller.ProgressDialogController;
import com.projectgoth.datastore.MusicDatastore;
import com.projectgoth.datastore.Session;
import com.projectgoth.datastore.UserDatastore;
import com.projectgoth.events.GAEvent;
import com.projectgoth.i18n.I18n;
import com.projectgoth.imagefetcher.ImageHandler;
import com.projectgoth.model.MenuOption;
import com.projectgoth.model.MenuOption.MenuOptionType;
import com.projectgoth.ui.UrlHandler;
import com.projectgoth.ui.activity.ActionHandler;
import com.projectgoth.ui.adapter.MenuAdapter;
import com.projectgoth.ui.widget.ImageViewEx;
import com.projectgoth.ui.widget.MiniProfile;
import com.projectgoth.ui.widget.PopupMenu;
import com.projectgoth.ui.widget.UserMiniDetails;
import com.projectgoth.ui.widget.tooltip.ToolTip;
import com.projectgoth.ui.widget.tooltip.ToolTipView;
import com.projectgoth.util.AndroidLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment used for managing interactions for and presentation of a navigation
 * drawer.
 * 
 * @author angelorohit
 */

public class NavigationDrawerFragment extends BaseProfileFragment implements View.OnClickListener, ToolTipView.OnToolTipViewClickedListener {

    private MiniProfile               mMiniProfile;
    private UserMiniDetails           userMiniDetails;
    private MenuAdapter               mAdapter;
    private Session                   mSession                = Session.getInstance();
    private static final String       LOG_TAG                 = AndroidLogger.makeLogTag(NavigationDrawerFragment.class);

    private PopupMenu                 mPopupMenu;
    private ImageView                 mPopupMenuMarker;

    private PresenceType              newPresence;
    private Profile                   mProfile;
    View                              footer;
    TextView                          menuStores;
    TextView                          menuMusic;

    private ImageViewEx               bannerImage;
    
    private Banner                    banner;

    /**
     * Remember the position of the selected item.
     */
    private static final String       STATE_SELECTED_POSITION = "selected_navigation_drawer_position";

    /**
     * A pointer to the current callbacks instance (the Activity).
     */
    private NavigationDrawerCallbacks callbacks;

    /**
     * Helper component that ties the action bar to the navigation drawer.
     */
    private ActionBarDrawerToggle     drawerToggle;

    private DrawerLayout              drawerLayout;
    private View                      fragmentContainerView;

    private int                       currentSelectedPosition = 0;
    private boolean                   userLearnedDrawer;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Add null check again, if sharePrefsManager is null, re-initialize resources
        ApplicationEx application = ApplicationEx.getInstance();
        SharedPrefsManager sharedPrefsManager = application.getSharedPrefsManager();
        // Whether or not the user has demonstrated awareness of the drawer.
        userLearnedDrawer = sharedPrefsManager.didUserLearnDrawer();

        if (savedInstanceState != null) {
            currentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
        }

        // Select either the default item (0) or the last selected item.
        selectItem(currentSelectedPosition);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of
        // actions in the action bar.
        setHasOptionsMenu(true);
    }

    public boolean isDrawerOpen() {
        return drawerLayout != null && drawerLayout.isDrawerOpen(fragmentContainerView);
    }

    /**
     * Users of this fragment must call this method to set up the navigation
     * drawer interactions.
     * 
     * @param drawerLayout
     *            The DrawerLayout containing this fragment's UI.
     */
    public void setup(final DrawerLayout drawerLayout) {
        fragmentContainerView = getView().findViewById(R.id.navigation_drawer);
        this.drawerLayout = drawerLayout;
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        // set a custom shadow that overlays the main content when the drawer opens
        this.drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the navigation drawer and the action bar app icon.
        drawerToggle = new ActionBarDrawerToggle(getActivity(), NavigationDrawerFragment.this.drawerLayout,
                R.drawable.ic_drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);

                if (!isAdded()) {
                    return;
                }

                // calls onPrepareOptionsMenu()
                getActivity().supportInvalidateOptionsMenu();

                mMiniProfile.resetStatusMsg(getActivity());
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

                if (!isAdded()) {
                    return;
                }

                if (!userLearnedDrawer) {
                    // The user manually opened the drawer; store this flag to
                    // prevent auto-showing the navigation drawer automatically in the future.
                    userLearnedDrawer = true;
                    ApplicationEx.getInstance().getSharedPrefsManager().setUserLearnedDrawer(true);
                }

                // calls onPrepareOptionsMenu()
                getActivity().supportInvalidateOptionsMenu();

                mMiniProfile.resetStatusMsg(getActivity());
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            }
        };

        // Defer code dependent on restoration of previous instance state.
        this.drawerLayout.post(new Runnable() {

            @Override
            public void run() {
                drawerToggle.syncState();
            }
        });

        this.drawerLayout.setDrawerListener(drawerToggle);
    }

    private void selectItem(int position) {
        currentSelectedPosition = position;
        closeDrawer();
        if (callbacks != null) {
            callbacks.onNavigationDrawerItemSelected(position);
        }
    }

    private void closeDrawer() {
        if (drawerLayout != null) {
            drawerLayout.closeDrawer(fragmentContainerView);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        setShouldUpdateActionBarOnAttach(false);
        super.onAttach(activity);
        try {
            callbacks = (NavigationDrawerCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setPullToRefreshEnabled(false);
        mList.setDivider(null);
        updateMiniProfile();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateMiniProfile();
        updateBanner();
    }

    @Override
    protected View createHeaderView() {
        View header = LayoutInflater.from(getActivity()).inflate(R.layout.header_contact_list, null);
        mMiniProfile = (MiniProfile) header.findViewById(R.id.mini_profile);

        mPopupMenu = new PopupMenu(getActivity());
        mPopupMenuMarker = (ImageView) header.findViewById(R.id.overflow_marker);
        mPopupMenu.setMenuOptions(getPresenceMenuOptions());
        mPopupMenu.setMarker(mPopupMenuMarker);

        mMiniProfile.setOnClickListener(this);
        return header;
    }

    @Override
    protected View createFooterView() {
        footer = LayoutInflater.from(getActivity()).inflate(R.layout.navigation_drawer_footer, null);
        setupFooterListener(footer);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(NUEManager.getInstance().shouldShowNUE(LOG_TAG)) {
                    addFirstToolTipView();
                }
            }
        }, Constants.NUE_TOOLTIP_DELAY);
        
        bannerImage = (ImageViewEx) footer.findViewById(R.id.banner_image);
        bannerImage.setBorder(false);
        bannerImage.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                if (banner != null) {
                    GAEvent.LeftPanel_Banner.send();
                    UrlHandler.displayUrl(getActivity(), banner.getUrl());
                    closeDrawer();
                }
            }
        });
        
        return footer;
    }
    
    private void updateBanner() {

        banner = BannerController.getInstance().getBanner(Placement.SIDEBAR);
        if (banner != null) {
            BannerController.setBannerIntoImage(banner, bannerImage);
        }
    }

    private void addFirstToolTipView() {
        View customView = LayoutInflater.from(this.getActivity()).inflate(R.layout.tooltip_title_subtitle, null);
        ((TextView) customView.findViewById(R.id.title)).setText(I18n.tr("Tap to see details"));
        ((TextView) customView.findViewById(R.id.subtitle)).setText(I18n.tr("Gifts, badges, and fans."));
        mToolTipView = mToolTipFrameLayout.showToolTipForView(
                new ToolTip().withContentView(customView)
                        .withColor(ApplicationEx.getColor(R.color.edit_text_input_color))
                        .withAnimationType(ToolTip.ANIMATIONTYPE_FROMMASTERVIEW).withShadow(false), userMiniDetails);
        mToolTipView.setOnToolTipViewClickedListener(this);
    }

    private void addSecondToolTipView() {
        View customView = LayoutInflater.from(this.getActivity()).inflate(R.layout.tooltip_title_subtitle, null);
        ((TextView) customView.findViewById(R.id.title)).setText(I18n.tr("Visit the store"));
        ((TextView) customView.findViewById(R.id.subtitle)).setText(I18n.tr("for stickers, gifts, and avatar items."));
        mToolTipView = mToolTipFrameLayout.showToolTipForView(
                new ToolTip().withContentView(customView)
                        .withColor(ApplicationEx.getColor(R.color.edit_text_input_color))
                        .withAnimationType(ToolTip.ANIMATIONTYPE_FROMMASTERVIEW).withShadow(false), menuStores);
        mToolTipView.setOnToolTipViewClickedListener(null);
    }

    private void setupFooterListener(View footer) {
        userMiniDetails = (UserMiniDetails) footer.findViewById(R.id.user_mini_details);
        userMiniDetails.setOnClickListener(this);
        TextView menuGames = (TextView) footer.findViewById(R.id.menu_games);
        menuStores = (TextView) footer.findViewById(R.id.menu_store);
        menuMusic = (TextView) footer.findViewById(R.id.menu_music);
        TextView menuSettings = (TextView) footer.findViewById(R.id.menu_settings);
        menuGames.setOnClickListener(this);
        menuStores.setOnClickListener(this);
        menuMusic.setOnClickListener(this);
        menuSettings.setOnClickListener(this);
        
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callbacks = null;
    }

    @Override
    protected void registerReceivers() {
        registerEvent(Events.User.STATUS_MESSAGE_SET);
        registerEvent(Events.User.PRESENCE_SET);
        registerEvent(Events.Contact.IM_STATUS_CHANGED);
        registerEvent(Events.Profile.RECEIVED);
        registerEvent(Events.Banner.FETCH_COMPLETED);
        registerEvent(Events.Banner.FETCH_ERROR);
        registerEvent(Events.Banner.SWITCH_BANNER);
        registerEvent(Events.User.FETCH_BADGES_COMPLETED);
    }

    @Override
    protected void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (action.equals(Events.User.STATUS_MESSAGE_SET)) {
            String statusMsg = mSession.getStatusMessage();
            mMiniProfile.setStatusMessage(statusMsg);

        } else if (action.equals(Events.User.PRESENCE_SET)) {
            PresenceType newPresence = mSession.getPresence();
            mMiniProfile.setPresenceIcon(newPresence);

        } else if (action.equals(Events.Profile.RECEIVED) || action.equals(Events.User.FETCH_BADGES_COMPLETED)) {
            Bundle data = intent.getExtras();
            String profileUsername = data.getString(Events.User.Extra.USERNAME);
            if (profileUsername.equalsIgnoreCase(mSession.getUsername())) {
                updateMiniProfile();
            }

            mAdapter.setMenuOptionList(getAllMenuListOptions());
            mAdapter.notifyDataSetChanged();
        } else if (action.equals(Events.Banner.FETCH_COMPLETED) || action.equals(Events.Banner.SWITCH_BANNER)) {
            updateBanner();
        } else if (action.equals(Events.Banner.FETCH_ERROR)) {
            Tools.showToastForIntent(context, intent);
        } else if (action.equals(Events.Contact.IM_STATUS_CHANGED)) {
            ProgressDialogController.getInstance().hideProgressDialog();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, currentSelectedPosition);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Forward the new configuration the drawer toggle component.
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // If the drawer is open, show the global app actions in the action bar.
        // See also showGlobalContextActionBar, which controls the top-left area
        // of the action bar.
        // if (drawerLayout != null && isDrawerOpen()) {
        // inflater.inflate(R.menu.global, menu);
        // }
        // super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return drawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    @Override
    protected BaseAdapter createAdapter() {
        mAdapter = new MenuAdapter();
        mAdapter.setMenuOptionList(getAllMenuListOptions());
        isHeaderEnabled = true;
        isFooterEnabled = true;
        return mAdapter;
    }

    private List<MenuOption> getAllMenuListOptions() {
        return new ArrayList<MenuOption>();
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        String username = mSession.getUsername();
        switch (viewId) {
            case R.id.presence:
            case R.id.presence_arrow:
                GAEvent.LeftPanel_ChangePresence.send();
                showPresencePopupMenu();
                break;
            case R.id.status_tick:
                GAEvent.LeftPanel_EditStatus.send();
                mMiniProfile.updateStatus(v.getContext());
                break;
            case R.id.display_pic:
            case R.id.username:
            case R.id.user_basic_details:
                GAEvent.LeftPanel_GoProfile.send();
                ActionHandler.getInstance().displayMainProfile(getActivity(), username);
                closeDrawer();
                break;
            case R.id.menu_gifts:
                GAEvent.LeftPanel_GiftList.send();

                if (!Config.getInstance().isMyGiftsEnabled()) {
                    int count = 0;
                    if (mProfile != null) {
                        count = mProfile.getNumOfGiftsReceived();
                    }
                    ActionHandler.getInstance().displayBrowser(getActivity(),
                            String.format(WebURL.URL_GIFTS_RECEIVED, username),
                            String.format(I18n.tr("Gifts (%d)"), count), R.drawable.ad_gift_white);
                } else {
                    ActionHandler.getInstance().displayMyGifts(getActivity(),
                            Session.getInstance().getUserId());
                }

                closeDrawer();
                break;
            case R.id.menu_badges:
                GAEvent.LeftPanel_BadgeList.send();
                ActionHandler.getInstance().displayBadgesList(getActivity(), username);
                closeDrawer();
                break;
            case R.id.menu_fans:
                GAEvent.LeftPanel_FanList.send();
                ActionHandler.getInstance().displayFollowersList(getActivity(), username);
                closeDrawer();
                break;
            case R.id.menu_games:
                GAEvent.LeftPanel_Game.send();
                gotoGames();
                break;
            case R.id.menu_music:
                GAEvent.LeftPanel_Music.send();
                gotoMusic();
                break;
            case R.id.menu_store:
                GAEvent.LeftPanel_Store.send();
                gotoStores();
                break;
            case R.id.menu_settings:
                GAEvent.LeftPanel_Settings.send();
                gotoSettings();
                break;
        }
    }

    private void gotoGames() {
        ActionHandler.getInstance().displayGameCentre(getActivity());
        closeDrawer();
    }
    
    private void gotoMusic() {
        ActionHandler.getInstance().displayMusicFragment(getActivity());
        closeDrawer();
    }

    private void gotoStores() {
        ActionHandler.getInstance().displayStore(getActivity(), null);
        closeDrawer();
    }

    private void gotoSettings() {
        ActionHandler.getInstance().displaySettings(getActivity(), SettingsFragment.SettingsGroupType.FIRST_LEVEL);
        closeDrawer();
    }

    @Override
    public void onToolTipViewClicked(ToolTipView toolTipView) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                addSecondToolTipView();
                NUEManager.getInstance().alreadyShownNUE(LOG_TAG);
            }
        }, Constants.NUE_TOOLTIP_DELAY);
    }

    /**
     * Callbacks interface that all activities using this fragment must
     * implement.
     */
    public static interface NavigationDrawerCallbacks {

        /**
         * Called when an item in the navigation drawer is selected.
         */
        void onNavigationDrawerItemSelected(int position);
    }

    /**
     * Helper method to toggle the drawer state
     */
    public void toggleDrawer() {
        // for [non-login] users
        if (Session.getInstance().isBlockUsers()) {
            ActionHandler.getInstance().displayLoginDialogFragment(getActivity());
            return;
        }
        if (isDrawerOpen()) {
            drawerLayout.closeDrawer(Gravity.LEFT);
        } else {
            drawerLayout.openDrawer(Gravity.LEFT);
        }
        if(!MusicDatastore.getInstance().isSupportByDeezer()){
            menuMusic.setVisibility(View.GONE);
        }else{
            menuMusic.setVisibility(View.VISIBLE);
        }
    }

    private void updateMiniProfile() {
        Session session = Session.getInstance();
        String username = session.getUsername();
        mMiniProfile.setUsername(username);

        mProfile = UserDatastore.getInstance().getProfileWithUsername(mSession.getUsername(), false);
        if (mProfile != null) {
            mMiniProfile.setLabels(mProfile.getLabels());
            mMiniProfile.setMigLevelNumber(Constants.BLANKSTR + mProfile.getMigLevel());

            ImageHandler.getInstance().loadDisplayPictureOfUser(mMiniProfile.getDisplayPicture(), username,
                    mProfile.getDisplayPictureType(), Config.getInstance().getDisplayPicSizeNormal(), true);

            userMiniDetails.updateMiniDetails(mProfile);
        }

        mMiniProfile.setPresenceIcon(Session.getInstance().getPresence());
        mMiniProfile.setStatusMessage(Session.getInstance().getStatusMessage());
    }

    private ArrayList<MenuOption> getPresenceMenuOptions() {
        ArrayList<MenuOption> menuItems = new ArrayList<MenuOption>();

        menuItems.add(new MenuOption(Tools.getPresenceText(true, PresenceType.AVAILABLE), Tools
                .getBitmap(R.drawable.ic_presence_online), R.id.action_presence_available, null,
                MenuOptionType.SELECTABLE, isSelectedPresence(PresenceType.AVAILABLE), false, null));
        menuItems.add(new MenuOption(Tools.getPresenceText(true, PresenceType.AWAY), Tools
                .getBitmap(R.drawable.ic_presence_away), R.id.action_presence_away, null, MenuOptionType.SELECTABLE,
                isSelectedPresence(PresenceType.AWAY), false, null));
        menuItems.add(new MenuOption(Tools.getPresenceText(true, PresenceType.BUSY), Tools
                .getBitmap(R.drawable.ic_presence_busy), R.id.action_presence_busy, null, MenuOptionType.SELECTABLE,
                isSelectedPresence(PresenceType.BUSY), false, null));
        menuItems.add(new MenuOption(Tools.getPresenceText(true, PresenceType.OFFLINE), Tools
                .getBitmap(R.drawable.ic_presence_offline), R.id.action_presence_offline, null,
                MenuOptionType.SELECTABLE, isSelectedPresence(PresenceType.OFFLINE), false, null));

        return menuItems;
    }

    private boolean isSelectedPresence(PresenceType presenceType) {
        if (presenceType == mSession.getPresence()) {
            return true;
        }
        return false;
    }

    private void showPresencePopupMenu() {
        mPopupMenu.setPopupAnchor(mPopupMenuMarker);
        mPopupMenu.setPopupGravity(Gravity.LEFT | Gravity.TOP);
        mPopupMenu.setXYOffset(0, mPopupMenu.mAnchorRect.bottom);
        mPopupMenu.setPopupMenuListener(this);
        mPopupMenu.show(true);
    }

    @Override
    public void onMenuOptionClicked(MenuOption menuOption) {
        switch (menuOption.getActionId()) {
            case R.id.action_presence_available:
                newPresence = PresenceType.AVAILABLE;
                break;
            case R.id.action_presence_away:
                newPresence = PresenceType.AWAY;
                break;
            case R.id.action_presence_busy:
                newPresence = PresenceType.BUSY;
                break;
            case R.id.action_presence_offline:
                newPresence = PresenceType.OFFLINE;
                break;
        }

    }

    @Override
    public void onPopupMenuDismissed() {
        if (newPresence != null) {
            FriendsController.getInstance().requestSetPresence(newPresence);
            updateMiniProfile();
            newPresence = null;
        }
    }

}
