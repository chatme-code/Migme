/**
 * Copyright (c) 2013 Project Goth
 *
 * ActionBarHelper.java
 * Created Jul 17, 2013, 3:36:24 PM
 */

package com.projectgoth.ui.activity;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.datastore.Session;
import com.projectgoth.util.AndroidLogger;
import com.projectgoth.common.Logger;
import com.projectgoth.datastore.AlertsDatastore;
import com.projectgoth.model.MenuOption;
import com.projectgoth.model.MenuOption.MenuOptionType;
import com.projectgoth.ui.activity.CustomActionBarConfig.NavigationButtonState;
import com.projectgoth.ui.activity.CustomActionBarConfig.OverflowButtonState;
import com.projectgoth.ui.widget.ActionButton;
import com.projectgoth.ui.widget.ActionButton.ActionButtonClickListener;
import com.projectgoth.ui.widget.PopupMenu;
import com.projectgoth.ui.widget.PopupMenu.OnPopupMenuListener;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author cherryv
 * 
 */
public class CustomActionBar implements ActionButtonClickListener {

    private static final String         LOG_TAG          = AndroidLogger.makeLogTag(CustomActionBar.class);

    private WeakReference<Context>      mContextRef;

    private OnPopupMenuListener         mPopupMenuListener;
    private CustomActionBarListener     mActionBarListener;
    private CustomActionBarConfig       mConfig;

    private View                        mActionBar;
    private ActionButton                mNavigationButton;
    private ActionButton                mSearchButton;
    private ActionButton                mOverflowButton;
    private View                        mCustomView;
    private int                         mCustomViewIndex;
    private RelativeLayout.LayoutParams layoutParams;

    private View                        mDefaultCustomView;
    private PopupMenu                   mPopupMenu;
    private ImageView                   mPopupMenuMarker;

    private boolean                     isBackShown      = false;
    
    public interface CustomActionBarListener {

        /**
         * Method to be implemented by the activity or fragment to add menu
         * options to the popup menu when custom overflow is clicked in the
         * action bar.
         * 
         * @return List of MenuOption to add to the popup menu.
         */
        public ArrayList<MenuOption> getMenuOptions();

        /**
         * Displayed at the left-most part of the action bar. Icon can either be
         * the menu or back. This is an implementation of how that icon is
         * supposed to be handled.
         */
        public void onNavigationIconPressed();

        /**
         * Displayed at the right-most part of the action bar. This is an
         * implementation of how that icon is supposed to be handled.
         */
        public void onOverflowButtonPressed(final OverflowButtonState state);

        /**
         * This is an implementation of how the back icon is supposed to be
         * handled (if back is displayed).
         */
        public void onBackIconPressed();
        
        /**
         * This is an implementation of how the search icon is supposed to be
         * handled (if displayed).
         */
        public void onSearchButtonPressed();

        /**
         * Initialize items of the custom view
         * 
         * @param customView
         *            The custom view to be added to the action bar
         */

        public void initCustomViewInCustomActionBar(View customView);
        
    }

    /**
     * Provides a default implementation of action bar elements such as the
     * navigation button and the overflow icon.
     * 
     * @param menuOptionsListener
     *            {@link OnPopupMenuListener} which will handle clicks from the
     *            overflow menu.
     */
    public CustomActionBar(Context context, CustomActionBarConfig actionBarConfig, CustomActionBarListener listener,
            OnPopupMenuListener popupMenuListener) {
        this.mContextRef = new WeakReference<Context>(context);
        this.mActionBarListener = listener;
        this.mPopupMenuListener = popupMenuListener;
        initActionBar();
        updateActionBarConfig(actionBarConfig);
    }

    private void initActionBar() {
        Context context = mContextRef.get();
        if (context == null) {
            return;
        }

        mActionBar = LayoutInflater.from(context).inflate(R.layout.action_bar, null);

        mNavigationButton = (ActionButton) mActionBar.findViewById(R.id.action_navigation);
        mNavigationButton.setActionButtonClickListener(this);

        mOverflowButton = (ActionButton) mActionBar.findViewById(R.id.action_overflow);
        mOverflowButton.setActionButtonClickListener(this);
        
        mSearchButton = (ActionButton) mActionBar.findViewById(R.id.action_search);
        mSearchButton.setActionButtonClickListener(this);

        mPopupMenu = new PopupMenu(context);
        if (mPopupMenuListener != null) {
            mPopupMenu.setPopupMenuListener(mPopupMenuListener);
        }

        mPopupMenuMarker = (ImageView) mActionBar.findViewById(R.id.overflow_marker);
        mPopupMenu.setMarker(mPopupMenuMarker);

        mCustomView = mActionBar.findViewById(R.id.custom_view);
        ViewGroup parent = (ViewGroup) mActionBar;
        mCustomViewIndex = parent.indexOfChild(mCustomView);
        layoutParams = (RelativeLayout.LayoutParams) mCustomView.getLayoutParams();

        mDefaultCustomView = LayoutInflater.from(context).inflate(R.layout.action_bar_default, null);
    }

    public void deinitActionBar() {
        mActionBar = null;
        mCustomView = null;
        mDefaultCustomView = null;
        mPopupMenuListener = null;
        mActionBarListener = null;
        mNavigationButton = null;
        mSearchButton = null;
        mOverflowButton = null;
        mPopupMenuMarker = null;
        mPopupMenu = null;
        mConfig = null;
    }

    public void updateActionBarConfig(CustomActionBarConfig config) {
        if (config == null) {
            // ignore null configurations
            return;
        }

        Context context = mContextRef.get();
        if (context == null) {
            return;
        }

        isBackShown = config.getNavigationButtonState() == NavigationButtonState.BACK;
        if (isBackShown) {
            mNavigationButton.setNormalIcon(R.drawable.ad_arrowleft_green);
        } else {
            mNavigationButton.setNormalIcon(R.drawable.ad_menu_green);
        }

        mConfig = config;
        final OverflowButtonState overflowButtonState = config.getOverflowButtonState();
        
        switch(overflowButtonState) {
            case NONE:
                setOverflowButtonIcon(0);
                break;
            case POPUP:
                setOverflowButtonIcon(R.drawable.ad_vmore_white);
                break;
            case ALERT:
                setOverflowButtonIcon(R.drawable.ad_alert_green);
                break;
            default:
                break;
        }

        if(config.getOverflowIcon() != null) {
            setOverflowButtonIcon(config.getOverflowIconId
                    ());
        }

        updateActionBarUnreadAlertCount(false);
        
        mSearchButton.setVisibility(config.isShowSearchButton()? View.VISIBLE : View.GONE);

        int customLayout = config.getCustomViewLayoutSrc();
        if (customLayout > -1) {
            View newView = LayoutInflater.from(context).inflate(customLayout, null);
            updateCustomView(newView);
        } else {
            updateCustomView(mDefaultCustomView);
        }
    }
    
    private void setOverflowButtonIcon(final int iconResId) {
        if (iconResId > 0) {
            mOverflowButton.setVisibility(View.VISIBLE);
            mOverflowButton.setNormalIcon(iconResId);
        } else {
            mOverflowButton.setVisibility(View.GONE);
        }
    }

    private void updateCustomView(View view) {
        if (view != null) {
            Logger.debug.log(LOG_TAG, "Updating custom view in action bar... ", view.getId(), ", currId: ",
                    mCustomView.getId());

            if (view != null) {
                if (mCustomView.getId() != view.getId()) {
                    mCustomView = view;
                    ViewGroup parent = (ViewGroup) mActionBar;
                    parent.removeViewAt(mCustomViewIndex);
                    parent.addView(view, mCustomViewIndex, layoutParams);
                }

                // for cases wherein two instances of same fragment is shown on
                // top of another (e.g. ChatFragment), then we must update the
                // header content, but not re-add it
                if (mActionBarListener != null) {
                    mActionBarListener.initCustomViewInCustomActionBar(mCustomView);
                }
            }
        }
    }

    protected View getActionBar() {
        return this.mActionBar;
    }

    @Override
    public void onActionButtonClicked(ActionButton button) {
        if (button == mOverflowButton) {
            mConfig.onActionBarOverflowIconClicked();
            final OverflowButtonState overflowButtonState = mConfig.getOverflowButtonState();
            switch (overflowButtonState)  {
                case NONE:
                    // Do nothing.
                    break;
                case POPUP:
                    showPopupMenuOptions();
                    break;
                default:
                    if (mActionBarListener != null) {
                        mActionBarListener.onOverflowButtonPressed(overflowButtonState);
                    }
                    break;
            }
        } else if (button == mSearchButton && mActionBarListener != null) {
            mActionBarListener.onSearchButtonPressed();
        } else if (button == mNavigationButton && mActionBarListener != null) {
            if (!isBackShown) {
                mActionBarListener.onNavigationIconPressed();
            } else {
                mActionBarListener.onBackIconPressed();
            }
        }
    }

    public void setOnMenuOptionsClickListener(OnPopupMenuListener listener) {
        this.mPopupMenuListener = listener;
        if (mPopupMenu != null) {
            mPopupMenu.setPopupMenuListener(mPopupMenuListener);
        }
    }

    public void setActionBarListener(CustomActionBarListener listener) {
        this.mActionBarListener = listener;
    }

    public void updateCustomActionBar(CustomActionBarConfig config, CustomActionBarListener abListener,
            OnPopupMenuListener menuListener) {
        setActionBarListener(abListener);
        setOnMenuOptionsClickListener(menuListener);
        updateActionBarConfig(config);
    }

    /**
     * This method is used for updating the unread alert count on action bar
     * @param  shouldFurtherFetch  Indicate whether should further fetch alerts from server
     */
    public void onActionBarUpdateAvailable(boolean shouldFurtherFetch) {
        updateActionBarUnreadAlertCount(shouldFurtherFetch);
    }


    private void updateActionBarUnreadAlertCount(final boolean shouldFurtherFetch) {
        if (!Session.getInstance().isLoggedIn()) {
            return;
        }

                // get unread counts
                final int unreadCount = AlertsDatastore.getInstance().getUnreadNotificationCount(shouldFurtherFetch);
                final int unreadMessageCount = ApplicationEx.getInstance().getNotificationHandler().getAllUnreadMessagesCount();
                final int unreadMentionCount = ApplicationEx.getInstance().getNotificationHandler().getUnreadMentionCount();
                final int finalUnreadCount = unreadCount + unreadMessageCount + unreadMentionCount;

                // update
                if (mOverflowButton != null && mNavigationButton != null) {
                    mOverflowButton.post(new Runnable() {
                        @Override
                        public void run() {
                            if (mOverflowButton == null || mNavigationButton == null) {
                                return;
                            }

                            if (mConfig.getOverflowButtonState() == OverflowButtonState.ALERT) {
                                mOverflowButton.setCounter(finalUnreadCount);
                            } else {
                                mOverflowButton.setCounter(0);
                            }

                            if (mConfig.getNavigationButtonState() == NavigationButtonState.BACK &&
                                    mConfig.isShowUnreadCountOnBack()) {
                                mNavigationButton.setCounter(unreadMessageCount);
                            }
                        }
                    });
                }

    }
    
    public void showPopupMenuOptions() {
        if (mActionBarListener != null) {
            List<MenuOption> options = mActionBarListener.getMenuOptions();

            if (mPopupMenu != null && options != null && options.size() > 0) {
                mPopupMenu.setMenuOptions(options);
                mPopupMenu.setPopupAnchor(mPopupMenuMarker);
                mPopupMenu.setPopupGravity(Gravity.RIGHT | Gravity.TOP);
                mPopupMenu.setXYOffset(0, mPopupMenu.mAnchorRect.bottom);
                
                // Set the marker on if the first item in the list is checked and gravity is TOP.
                final MenuOption firstMenuOption = options.get(0); 
                mPopupMenu.setMarkerOn(firstMenuOption.getMenuOptionType().equals(MenuOptionType.CHECKABLE) && 
                        firstMenuOption.isChecked());
                
                mPopupMenu.show(true);
            }
        }
    }

}
