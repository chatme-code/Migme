/**
 * Copyright (c) 2013 Project Goth
 *
 * FragmentHandler.java
 * Created May 30, 2013, 12:26:43 AM
 */

package com.projectgoth.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.BackStackEntry;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.Gravity;

import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.enums.EveryoneOrFollowerAndFriendPrivacyEnum;
import com.projectgoth.blackhole.enums.MessageType;
import com.projectgoth.blackhole.model.Captcha;
import com.projectgoth.common.Logger;
import com.projectgoth.common.Tools;
import com.projectgoth.controller.StoreController.StoreItemFilterType;
import com.projectgoth.datastore.DataCache;
import com.projectgoth.datastore.Session;
import com.projectgoth.enums.PostListType;
import com.projectgoth.events.AppEvents;
import com.projectgoth.imagefetcher.UIUtils;
import com.projectgoth.model.LocationListItem;
import com.projectgoth.model.StorePagerItem;
import com.projectgoth.music.deezer.DeezerDetailListFragment;
import com.projectgoth.nemesis.enums.ChatTypeEnum;
import com.projectgoth.ui.fragment.AccountBalanceFragment;
import com.projectgoth.ui.fragment.AttachmentFragment;
import com.projectgoth.ui.fragment.AttachmentPagerFragment;
import com.projectgoth.ui.fragment.BadgeInfoFragment;
import com.projectgoth.ui.fragment.BadgesFragment;
import com.projectgoth.ui.fragment.BaseDialogFragment;
import com.projectgoth.ui.fragment.BaseFragment;
import com.projectgoth.ui.fragment.BaseListFragment;
import com.projectgoth.ui.fragment.BaseSearchFragment;
import com.projectgoth.ui.fragment.BaseSearchFragment.Mode;
import com.projectgoth.ui.fragment.BrowserFragment;
import com.projectgoth.ui.fragment.CaptchaFragment;
import com.projectgoth.ui.fragment.ChatFragment;
import com.projectgoth.ui.fragment.ChatListFragment;
import com.projectgoth.ui.fragment.ChatManagerFragment;
import com.projectgoth.ui.fragment.ChatroomListFragment;
import com.projectgoth.ui.fragment.CreateChatroomFragment;
import com.projectgoth.ui.fragment.FriendListFragment;
import com.projectgoth.ui.fragment.FriendListFragment.FriendListItemActionType;
import com.projectgoth.ui.fragment.FullProfileFragment;
import com.projectgoth.ui.fragment.GameCentreFragment;
import com.projectgoth.ui.fragment.GameDetailFragment;
import com.projectgoth.ui.fragment.GameDetailInformationFragment;
import com.projectgoth.ui.fragment.GiftCategoryFragment;
import com.projectgoth.ui.fragment.GiftCategoryParentFragment;
import com.projectgoth.ui.fragment.GiftCenterCategoryListFragment;
import com.projectgoth.ui.fragment.GiftCenterFragment;
import com.projectgoth.ui.fragment.GiftCenterGiftCategoryFragment;
import com.projectgoth.ui.fragment.GiftFragment;
import com.projectgoth.ui.fragment.GiftPreviewFragment;
import com.projectgoth.ui.fragment.GiftPurchasedFragment;
import com.projectgoth.ui.fragment.GiftRecipientSelectionFragment;
import com.projectgoth.ui.fragment.GiftSentFragment;
import com.projectgoth.ui.fragment.GiftStoreFragment;
import com.projectgoth.ui.fragment.GlobalSearchFragment;
import com.projectgoth.ui.fragment.GlobalSearchFragment.SearchType;
import com.projectgoth.ui.fragment.GlobalSearchPreviewFragment;
import com.projectgoth.ui.fragment.GroupPageFragment;
import com.projectgoth.ui.fragment.HotTopicsFragment;
import com.projectgoth.ui.fragment.InterstitialBannerFragment;
import com.projectgoth.ui.fragment.InviteFriendsFragment;
import com.projectgoth.ui.fragment.LocationListFragment;
import com.projectgoth.ui.fragment.LoginDialogFragment;
import com.projectgoth.ui.fragment.LoginFormFragment;
import com.projectgoth.ui.fragment.MainFragment;
import com.projectgoth.ui.fragment.MiniProfileChatFragment;
import com.projectgoth.ui.fragment.MiniProfilePopupFragment;
import com.projectgoth.ui.fragment.MusicFragment;
import com.projectgoth.ui.fragment.MusicGenreFilterFragment;
import com.projectgoth.ui.fragment.MyGiftsAllListFragment;
import com.projectgoth.ui.fragment.MyGiftsCardListFragment;
import com.projectgoth.ui.fragment.MyGiftsCategoryFragment;
import com.projectgoth.ui.fragment.MyGiftsListFragment;
import com.projectgoth.ui.fragment.MyGiftsOverviewFilterFragment;
import com.projectgoth.ui.fragment.MyGiftsOverviewFilterFragment.MyGiftsOverviewFilterType;
import com.projectgoth.ui.fragment.MyGiftsOverviewFilterFragment.MyGiftsOverviewSortingListener;
import com.projectgoth.ui.fragment.MyGiftsOverviewFragment;
import com.projectgoth.ui.fragment.MyGiftsPagerFragment;
import com.projectgoth.ui.fragment.MyStickersFragment;
import com.projectgoth.ui.fragment.ParticipantListFragment;
import com.projectgoth.ui.fragment.PhotoViewerFragment;
import com.projectgoth.ui.fragment.PositiveAlertFragment;
import com.projectgoth.ui.fragment.PostListFragment;
import com.projectgoth.ui.fragment.ProfileFragment;
import com.projectgoth.ui.fragment.ProfileInfoFragment;
import com.projectgoth.ui.fragment.ProfileListFragment;
import com.projectgoth.ui.fragment.ProfileListFragment.ProfileListType;
import com.projectgoth.ui.fragment.RequestFollowFragment;
import com.projectgoth.ui.fragment.SendGiftFragment;
import com.projectgoth.ui.fragment.SettingsFragment;
import com.projectgoth.ui.fragment.SettingsFragment.SettingsGroupType;
import com.projectgoth.ui.fragment.ShareInChatFragment;
import com.projectgoth.ui.fragment.ShareToFragment;
import com.projectgoth.ui.fragment.ShareToFragment.ShareItemListener;
import com.projectgoth.ui.fragment.ShareboxFragment;
import com.projectgoth.ui.fragment.ShareboxFragment.ShareboxActionType;
import com.projectgoth.ui.fragment.ShareboxFragment.ShareboxSubActionType;
import com.projectgoth.ui.fragment.SignupEmailFragment;
import com.projectgoth.ui.fragment.SignupEmailResultExpiredFragment;
import com.projectgoth.ui.fragment.SignupEmailResultSuccessFragment;
import com.projectgoth.ui.fragment.SignupEmailResultTimeoutFragment;
import com.projectgoth.ui.fragment.SignupEmailResultUsedFragment;
import com.projectgoth.ui.fragment.SignupEmailVerifyFragment;
import com.projectgoth.ui.fragment.SignupEmailVerifyingFragment;
import com.projectgoth.ui.fragment.SignupFacebookFailFragment;
import com.projectgoth.ui.fragment.SignupFacebookSuccessFragment;
import com.projectgoth.ui.fragment.SignupPasswordFragment;
import com.projectgoth.ui.fragment.SignupUsernameFragment;
import com.projectgoth.ui.fragment.SignupVerifyFragment;
import com.projectgoth.ui.fragment.SinglePostFragment;
import com.projectgoth.ui.fragment.SinglePostFragment.HeaderTab;
import com.projectgoth.ui.fragment.SinglePostGiftFragment;
import com.projectgoth.ui.fragment.StartChatFragment;
import com.projectgoth.ui.fragment.StartChatFragment.StartChatActionType;
import com.projectgoth.ui.fragment.StickerPackDetailsFragment;
import com.projectgoth.ui.fragment.StickerStoreFragment;
import com.projectgoth.ui.fragment.StoreFilterFragment;
import com.projectgoth.ui.fragment.StoreFilterFragment.StoreFilterType;
import com.projectgoth.ui.fragment.StoreFilterFragment.StoreSortingListener;
import com.projectgoth.ui.fragment.StorePagerFragment;
import com.projectgoth.ui.fragment.StoreSearchFragment;
import com.projectgoth.ui.fragment.StoreSearchPreviewFragment;
import com.projectgoth.ui.fragment.UnlockedGiftFragment;
import com.projectgoth.ui.fragment.UnlockedGiftListFragment;
import com.projectgoth.util.FragmentUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


/**
 * 
 * Central class that determines UI navigation between activities (screens) or
 * fragments. Handles transition between fragments and activities. Composed of
 * static methods that switches fragments and activities. Accessibility of the
 * methods is set to "default" so we restrict access of these to the Activities
 * only (same package). This is because we don't want fragments to manage
 * navigation to other fragments. They should always be handled by the Activity.
 * 
 * @author cherryv
 * 
 */
public class FragmentHandler {

    private DataCache<BaseFragment>     mFragmentDisplayCache;
    private List<CustomPopupActivity>   mCustomPopupActivityCache = new ArrayList<CustomPopupActivity>();
    private static MainFragment         mainFragment = null;
    private static final String         DIALOG_TAG   = "DIALOG_TAG";
    public static final String          CLEAR_TASK_IN_GINGERBREAD = "CLEAR_TASK_IN_GINGERBREAD";
    public static final String          REGISTER_TOKEN_KEY = "RegisterToken";
    public static final String          LOGIN_FRAGMENT = "loginActivity.fragment";

    private static class FragmentHandlerHolder {
        static final FragmentHandler sINSTANCE = new FragmentHandler();
    }

    public static FragmentHandler getInstance() {
        return FragmentHandlerHolder.sINSTANCE;
    }

    private FragmentHandler() {
        try {
            mFragmentDisplayCache = new DataCache<BaseFragment>(4, false);
        } catch (Exception e) {
            Logger.error.log(DIALOG_TAG, e);
        }
    }
    
    public MainFragment getMainFragment() {
        if (mainFragment == null) {
            return createNewMainFragment();
        }
        
        return mainFragment;
    }
    
    private MainFragment createNewMainFragment() {
        mainFragment = new MainFragment();
        return mainFragment;
    }
    
    public void addCustomPopupActivityToCache(final CustomPopupActivity activity) {
        if (mCustomPopupActivityCache.contains(activity)) {
            removeCustomPopupActivityFromCache(activity);
        }
        
        mCustomPopupActivityCache.add(activity);
    }
    
    public void removeCustomPopupActivityFromCache(final CustomPopupActivity activity) {
        mCustomPopupActivityCache.remove(activity);
    }
    
    public void clearAllCustomPopActivities() {
        for (CustomPopupActivity activity : mCustomPopupActivityCache) {
            activity.finish();
        }
        
        mCustomPopupActivityCache.clear();
    }

    /**
     * Generates a unique ID for this fragment by taking its class name and
     * appending a unique id with it.
     * 
     * @param fragment
     *            Fragment to generate an id for
     * @return The unique ID that can be used by this fragment
     */
    public String generateFragmentId(BaseFragment fragment) {
        UUID uuid = UUID.randomUUID();
        return fragment.getClass().getSimpleName() + "-" + uuid.toString();
    }

    /**
     * Stores a fragment in the {@link #mFragmentDisplayCache}.
     * 
     * @param fragment
     *            Fragment to store
     * @return The fragment id, or unique key used to store the fragment in the
     *         {@link #mFragmentDisplayCache}. You will need this key to
     *         retrieve the fragment again later on.
     */
    private String cacheFragment(BaseFragment fragment) {
        String fragmentId = generateFragmentId(fragment);
        mFragmentDisplayCache.cacheData(fragmentId, fragment);
        return fragmentId;
    }

    /**
     * Helper method to start a new activity with null value handling
     * 
     * @param context
     *            Context used to start the new activity
     * @param intent
     *            Intent of new activity to start
     */
    private void startActivity(Context context, Intent intent) {
        context = Tools.ensureContext(context);
        if (context != null) {
            context.startActivity(intent);
        }
    }

    /**
     * 
     * @param manager
     * @return
     */
    private FragmentManager ensureFragmentManager(FragmentManager manager) {
        if (manager == null) {
            BaseFragmentActivity currentActivity = ApplicationEx.getInstance().getCurrentActivity();
            if (currentActivity != null) {
                manager = currentActivity.getSupportFragmentManager();
            } else {
                return null;
            }
        }

        return manager;
    }

    /**
     * Opens and displays the Login screen
     * 
     * @param context
     */
    public void showLoginActivity(Context context) {
        showLoginActivity(context, null);
    }

    public void showLoginActivity(Context context, boolean clearTask) {
        showLoginActivity(context, clearTask, null, null);
    }

    public void showLoginActivity(Context context, String token) {
        showLoginActivity(context, false, token, null);
    }

    public void showLoginActivity(Context context, boolean clearTask, String token, LoginActivity.PreloadedFragmentKey key) {
        // ensure we don't have null context
        context = Tools.ensureContext(context);
        Intent intent = new Intent(context, LoginActivity.class);
        if (token != null) {
            intent.putExtra(REGISTER_TOKEN_KEY, token);
        }
        if (key != null) {
            intent.putExtra(LOGIN_FRAGMENT, key.ordinal());
        }
        if (clearTask) {
            if (!UIUtils.hasHoneycomb()) {
                intent.putExtra(CLEAR_TASK_IN_GINGERBREAD, true);
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        } else {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
        startActivity(context, intent);
    }

    /**
     * Opens and display the MainDrawerLayoutActivity as main screen
     * 
     * @param context
     */
    void showMainActivity(Context context) {
        // ensure we don't have null context
        context = Tools.ensureContext(context);

        Intent intent = new Intent(context, MainDrawerLayoutActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(context, intent);
    }

    /**
     * Initializes the fragments that will be contained in the
     * MainDrawerLayoutActivity
     * 
     * @param manager
     */
    void initMainDrawerLayoutFragments(FragmentManager manager) {
        manager = ensureFragmentManager(manager);
        if (manager == null) {
            return;
        }

        manager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        showFragment(manager, createNewMainFragment(), false);
    }
    
    FragmentHandler addFragment(FragmentActivity activity, BaseFragment fragment) {
        FragmentManager manager = null;
        if (activity == null) {
            manager = ensureFragmentManager(manager);
        } else {
            manager = activity.getSupportFragmentManager();
        }
            
        if (manager != null) {
            manager.beginTransaction().add(R.id.content_frame, fragment, generateFragmentId(fragment)).commit();
        }
        
        return this;
    }

    /**
     * Shows any fragment referred to by fragmentId that is stored in the
     * {@link #mFragmentDisplayCache} in the content frame of the currently
     * shown activity.
     * 
     * @param manager
     *            The FragmentManager of the currently shown activity.
     * @param fragmentId
     *            The unique identifier of the fragment to be shown that is
     *            stored in the {@link #mFragmentDisplayCache}
     * @param addToBackStack
     *            Override to save previous transaction to back stack.
     */
    void showFragmentWithId(FragmentManager manager, final String fragmentId, boolean addToBackStack) {
        BaseFragment fragment = mFragmentDisplayCache.removeData(fragmentId);
        showFragment(manager, fragment, addToBackStack);
    }
    
    void showFragmentWithId(FragmentManager manager, final String fragmentId, boolean addToBackStack, 
            boolean shouldShowTransition) {
        BaseFragment fragment = mFragmentDisplayCache.removeData(fragmentId);
        showFragment(manager, fragment, addToBackStack, shouldShowTransition);
    }

    /**
     * Shows any type of BaseFragment in the content frame of the activity
     * passed to this method. The {@link FragmentManager} of this
     * {@link FragmentActivity} is used to display the fragment.
     * 
     * Internally calls
     * {@link #showFragment(FragmentManager, BaseFragment, boolean)} method.
     * 
     * @param activity
     *            Activity where the BaseFragment will be displayed.
     * @param fragment
     *            The BaseFragment to be shown.
     */
    void showFragment(FragmentActivity activity, final BaseFragment fragment) {
        showFragment(activity != null ? activity.getSupportFragmentManager() : null, fragment, true);
    }

    void showFragment(FragmentActivity activity, final BaseFragment fragment, boolean addToBackStack) {
        showFragment(activity != null ? activity.getSupportFragmentManager() : null, fragment, addToBackStack);
    }

    /**
     * Shows any type of BaseFragment in the content frame of the currently
     * shown BaseFragmentActivity.
     * 
     * This internally calls
     * {@link #showFragment(FragmentManager, BaseFragment, boolean)} where
     * saving to back stack is enabled.
     * 
     * @param manager
     *            The FragmentManager of the currently shown activity.
     * @param fragment
     *            The BaseFragment to be shown.
     */
    void showFragment(FragmentManager manager, final BaseFragment fragment) {
        showFragment(manager, fragment, true);
    }
    
    void showFragment(FragmentManager manager, final BaseFragment fragment, boolean addToBackStack) {
        showFragment(manager, fragment, addToBackStack, false);
    }

    /**
     * Shows any type of BaseFragment in the content frame of the currently
     * shown BaseFragmentActivity.
     * 
     * @param manager
     *            The FragmentManager of the currently shown activity.
     * @param fragment
     *            The BaseFragment to be shown.
     * @param addToBackStack
     *            Override to save previous transaction to back stack. More
     *            often than not, this is enabled.
     */
    void showFragment(FragmentManager manager, final BaseFragment fragment, boolean addToBackStack,
            boolean shouldShowFragmentTransition) {
        manager = ensureFragmentManager(manager);

        if (manager != null && fragment != null) {
            try {
                FragmentTransaction transaction = manager.beginTransaction();
                
                if (shouldShowFragmentTransition) {
                    transaction.setCustomAnimations(R.anim.slide_in_top, R.anim.slide_out_top);
                }
                
                // We assume that all activities will have the content_frame
                // element to contain the main fragment
                transaction.replace(R.id.content_frame, fragment, generateFragmentId(fragment));
                if (addToBackStack) {
                    transaction.addToBackStack(null);
                }
                transaction.commit();
            } catch (Exception e) {
                // NOTE: Exception might occur if content_frame is not found in
                // the current layout
                Logger.error.log(DIALOG_TAG, e);
            }
        }
    }

    /**
     * Displays the supplied {@link BaseFragment} inside the
     * {@link PopupActivity} which is designed to look like a "pop-up" window.
     * 
     * This internally calls {@link #showFragmentWithIdAsPopup(Context, String)}
     * as the fragment needs to be added to the {@link #mFragmentDisplayCache}
     * for it to be displayed in the {@link PopupActivity}.
     * 
     * @param context
     *            Context or activity to be used to create the
     *            {@link PopupActivity}
     * @param fragment
     *            The BaseFragment to be shown.
     */
    void showFragmentAsPopup(Context context, BaseFragment fragment) {
        showFragmentAsPopup(context, fragment, true);
    }

    void showFragmentAsPopup(Context context, BaseFragment fragment, boolean shouldShowCloseBtn) {
        String fragmentId = cacheFragment(fragment);
        showFragmentWithIdAsPopup(context, fragmentId, shouldShowCloseBtn);
    }

    /**
     * Starts a {@link PopupActivity} and opens the specified fragment in there.
     * Fragment to open is determined by the fragmentId of the fragment that is
     * stored in {@link #mFragmentDisplayCache}. If the fragment is not found,
     * the activity will still be displayed but the content area will be blank.
     * 
     * @param context
     *            Context used to start the {@link PopupActivity}
     * @param fragmentId
     *            Id of the fragment to open as stored in
     *            {@link #mFragmentDisplayCache}
     */
    void showFragmentWithIdAsPopup(Context context, String fragmentId, boolean shouldShowCloseBtn) {
        // ensure we don't have null context
        context = Tools.ensureContext(context);

        Intent intent = new Intent(context, PopupActivity.class);
        intent.putExtra(AppEvents.Application.Extra.FRAGMENT_ID, fragmentId);
        intent.putExtra(PopupActivity.PARAM_SHOW_CLOSE_BUTTON, shouldShowCloseBtn);
        startActivity(context, intent);
    }

    void showFragmentAsCustomTranslucentPopup(Context context, BaseFragment fragment) {
        String fragmentId = cacheFragment(fragment);
        showFragmentWithIdAsCustomPopup(context, fragmentId, false, true);
    }
    
    void showFragmentAsCustomPopup(Context context, BaseFragment fragment) {
        String fragmentId = cacheFragment(fragment);
        showFragmentWithIdAsCustomPopup(context, fragmentId, false, false);
    }
    
    void showFragmentAsCustomPopup(Context context, BaseFragment fragment,
            boolean shouldShowActivityTransition, boolean useTranslucentActivity) {
        String fragmentId = cacheFragment(fragment);
        showFragmentWithIdAsCustomPopup(context, fragmentId, shouldShowActivityTransition, useTranslucentActivity);
    }

    void showFragmentWithIdAsCustomPopup(Context context, String fragmentId,
            boolean shouldShowActivityTransition, boolean useTranslucentActivity) {
        // ensure we don't have null context
        context = Tools.ensureContext(context);

        Intent intent = null;
        if (useTranslucentActivity) {
            intent = new Intent(context, CustomPopupTranslucentActivity.class);
        } else {
            intent = new Intent(context, CustomPopupActivity.class);
        }

        intent.putExtra(AppEvents.Application.Extra.FRAGMENT_ID, fragmentId);
        intent.putExtra(CustomPopupActivity.PARAM_SHOULD_SHOW_TRANSITION, shouldShowActivityTransition);
        //AD-1470 calling startActivity() from outside of an Activity context requires Intent.FLAG_ACTIVITY_NEW_TASK
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        startActivity(context, intent);
    }

    void showFragmentAsSingleTop(Context context, BaseFragment fragment) {
        context = Tools.ensureContext(context);

        String fragmentId = cacheFragment(fragment);
        Intent intent = new Intent(context, CustomPopupActivity.class);
        intent.putExtra(AppEvents.Application.Extra.FRAGMENT_ID, fragmentId);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(context, intent);
    }
    
    void showFragmentWithIdAsCustomPopupForResult(Activity activity, BaseFragment fragment, int requestCode) {
        String fragmentId = cacheFragment(fragment);

        if (activity == null) {
            activity = ApplicationEx.getInstance().getCurrentActivity();
        }

        if (activity != null) {
            Intent intent = new Intent(activity, CustomPopupActivity.class);
            intent.putExtra(AppEvents.Application.Extra.FRAGMENT_ID, fragmentId);

            activity.startActivityForResult(intent, requestCode);
        }
    }

    void showFragmentAsDialog(FragmentActivity activity, BaseDialogFragment fragment) {
        showFragmentAsDialog(activity != null ? activity.getSupportFragmentManager() : null, fragment, false, true);
    }

    void showFragmentAsDialog(FragmentActivity activity, BaseDialogFragment fragment, boolean removePreviousDialog) {
        showFragmentAsDialog(activity != null ? activity.getSupportFragmentManager() : null, fragment, false,
                removePreviousDialog);
    }

    void showFragmentAsDialog(FragmentManager manager, BaseDialogFragment fragment, boolean addToBackStack,
            boolean removePreviousDialog) {
        manager = ensureFragmentManager(manager);

        if (manager != null && fragment != null) {
            FragmentTransaction transaction = manager.beginTransaction();
            Fragment prevDialog = manager.findFragmentByTag(DIALOG_TAG);
            if (prevDialog != null && removePreviousDialog) {
                transaction.remove(prevDialog);
            }
            if (addToBackStack) {
                transaction.addToBackStack(null);
            }

            try {
                fragment.setShouldUpdateActionBarOnAttach(false);
                fragment.show(transaction, DIALOG_TAG);
            } catch (IllegalStateException e) {
                Logger.error.log(DIALOG_TAG, e);
            }
        }
    }
    
    /**
     * Checks whether a {@link Fragment} is currently being shown as a dialog.
     * @param fragment The {@link Fragment} to be checked.
     * @return  true if the Fragment is being shown as a dialog and false otherwise.
     */
    public boolean isFragmentShownAsDialog(Fragment fragment) {
        return (fragment.getActivity().getSupportFragmentManager().findFragmentByTag(DIALOG_TAG) == fragment);
    }

    /**
     * Pops everything in the back stack of this activity, except for the root
     * fragment
     * 
     * @param activity
     */
    void popToRootFragment(FragmentActivity activity) {
        FragmentManager manager = (activity != null ? activity.getSupportFragmentManager() : null);
        manager = ensureFragmentManager(manager);

        if (manager != null) {
            manager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }

    void popLastFragment(FragmentActivity activity) {
        FragmentManager manager = (activity != null ? activity.getSupportFragmentManager() : null);
        manager = ensureFragmentManager(manager);

        if (manager != null) {
            manager.popBackStackImmediate();
        }
    }

    public CaptchaFragment getCaptchaFragment(final Captcha captcha) {
        if (captcha != null) {
            CaptchaFragment captchaFragment = new CaptchaFragment(captcha);
            return captchaFragment;
        }

        return null;
    }

    public InviteFriendsFragment getInviteFriendsFragment() {
        return new InviteFriendsFragment();
    }

    /**
     * Creates a {@link BrowserFragment}.
     * 
     * @param url
     *            Url parameter to pass to the {@link BrowserFragment}
     * @return An instance of a {@link BrowserFragment}
     */
    public BrowserFragment getBrowserFragment(final String url) {
        return getBrowserFragment(url, false, null, null, null, 0);
    }

    /**
     * Creates a {@link BrowserFragment}.
     * 
     * @param url
     *            Url parameter to pass to the {@link BrowserFragment}
     * @param isFullyConstructedUrl
     *            Whether the url that is passed as a parameter is fully
     *            constructed or is a sub-url.
     * @return An instance of a {@link BrowserFragment}
     */
    public BrowserFragment getBrowserFragment(final String url, final boolean isFullyConstructedUrl) {
        return getBrowserFragment(url, isFullyConstructedUrl, null, null, null, 0);
    }
    
    public BrowserFragment getBrowserFragment(final String url, final boolean isFullyConstructedUrl, 
            final String title, final Drawable titleIcon, final String pageTitle, final int pageIcon) {
        BrowserFragment browserFragment = new BrowserFragment();

        Bundle args = new Bundle();
        args.putString(BrowserFragment.PARAM_LAUNCH_URL, url);
        args.putBoolean(BrowserFragment.PARAM_IS_FULL_URL, isFullyConstructedUrl);
        args.putBoolean(BaseDialogFragment.SAVED_IS_TRANSPARENT, true);
        browserFragment.setArguments(args);
        browserFragment.setDialogTitle(title, titleIcon);
        
        if (!TextUtils.isEmpty(pageTitle) && pageIcon > 0) {
            browserFragment.setBrowserTitle(pageTitle, pageIcon);
        }

        return browserFragment;

    }

    public ChatroomListFragment getChatroomListFragment(boolean shouldShowSearch) {
        ChatroomListFragment fragment = new ChatroomListFragment();
        if (shouldShowSearch) {
            Bundle args = new Bundle();
            args.putInt(BaseSearchFragment.PARAM_INITIAL_MODE, Mode.FILTERABLE.value());
            fragment.setArguments(args);
        }
        
        return fragment;
    }

    public ShareInChatFragment getShareInChatFragment(String url, String mimeType, String mimeData) {
        ShareInChatFragment fragment = new ShareInChatFragment();

        Bundle args = new Bundle();
        args.putString(ShareInChatFragment.PARAM_URL, url);
        args.putString(ShareInChatFragment.PARAM_MIMETYPE, mimeType);
        args.putString(ShareInChatFragment.PARAM_MIMEDATA, mimeData);
        fragment.setArguments(args);

        return fragment;
    }

    public GiftStoreFragment getGiftsFragment(String initialRecipient) {
        GiftStoreFragment fragment = new GiftStoreFragment();
        Bundle args = new Bundle();
        if (initialRecipient != null) {
            args.putString(GiftStoreFragment.PARAM_INITIAL_RECIPIENT, initialRecipient);
        }
        fragment.setArguments(args);
        return fragment;
    }
    
    public UnlockedGiftListFragment getUnlockedGiftListFragment() {
        return new UnlockedGiftListFragment();
    }
    
    public StickerStoreFragment getStickerStoreFragment() {
        return new StickerStoreFragment();
    }
    
    public StickerPackDetailsFragment getStickerPackDetailsFragment(int packId, int referenceId) {
        StickerPackDetailsFragment fragment = new StickerPackDetailsFragment();
        
        Bundle args = new Bundle();
        args.putInt(StickerPackDetailsFragment.PARAM_PACK_ID, packId);
        args.putInt(StickerPackDetailsFragment.PARAM_PACK_REFERENCE_ID, referenceId);
        fragment.setArguments(args);
        
        return fragment;
    }

    public PositiveAlertFragment getPositiveAlertFragment(String alertId) {
        PositiveAlertFragment fragment = new PositiveAlertFragment(alertId);
        
        Bundle args = new Bundle();
        args.putBoolean(BaseDialogFragment.SAVED_IS_TRANSPARENT, true);
        args.putBoolean(BaseDialogFragment.SAVED_SHOULD_DISMISS_ON_TOUCH, true);
        args.putBoolean(BaseDialogFragment.IS_POSITIVE_ALERT, true);
        fragment.setArguments(args);

        return fragment;
    }

    public InterstitialBannerFragment getInterstitialBannerFragment(String title, String message, String actionUrl) {
        InterstitialBannerFragment fragment = new InterstitialBannerFragment(title, message, actionUrl);
        Bundle args = new Bundle();
        args.putBoolean(BaseDialogFragment.SAVED_IS_TRANSPARENT, true);
        args.putBoolean(BaseDialogFragment.SAVED_SHOULD_DISMISS_ON_TOUCH, true);
        fragment.setArguments(args);

        return fragment;
    }

    /**
     * Creates a post list fragment
     * 
     * @param listType
     *            List type for post list as defined by {@link PostListType}
     * @return The created {@link PostListFragment}
     */
    private PostListFragment createPostListFragment(PostListType listType, String userId, String userName,
            String groupId, String searchParam, boolean enableHeader, int numOfPosts,
            EveryoneOrFollowerAndFriendPrivacyEnum feedPrivacy) {
        PostListFragment postList = new PostListFragment();

        Bundle args = new Bundle();
        args.putInt(PostListFragment.PARAM_POST_LIST_TYPE, listType.getId());
        if (!TextUtils.isEmpty(userId)) {
            args.putString(PostListFragment.PARAM_REQUESTING_USERID, userId);
        }
        if (!TextUtils.isEmpty(userName)) {
            args.putString(PostListFragment.PARAM_REQUESTING_USERNAME, userName);
        }
        if (!TextUtils.isEmpty(searchParam)) {
            args.putString(PostListFragment.PARAM_SEARCH_STRING, searchParam);
        }
        if (!TextUtils.isEmpty(groupId)) {
            args.putString(PostListFragment.PARAM_GROUP_ID, groupId);
        }
        args.putInt(PostListFragment.PARAM_NUM_POSTS, numOfPosts);
        if (feedPrivacy != null) {
            args.putInt(PostListFragment.PARAM_FEED_PRIVACY, feedPrivacy.value());
        }
        args.putBoolean(BaseListFragment.PARAM_ENABLE_HEADER, enableHeader);

        postList.setArguments(args);

        return postList;
    }
    
    public ProfileInfoFragment getProfileInfoFragment(String username) {
        ProfileInfoFragment fragment = new ProfileInfoFragment();
        Bundle args = new Bundle();
        args.putString(ProfileInfoFragment.PARAM_USERNAME, username);
        fragment.setArguments(args);
        return fragment;
    }

    public PostListFragment getUserPostFragment(String userId, String userName, int numOfPosts,
            EveryoneOrFollowerAndFriendPrivacyEnum feedPrivacy) {
        return createPostListFragment(PostListType.PROFILE_POSTS, userId, userName, null, null, false, numOfPosts,
                feedPrivacy);
    }

    public PostListFragment getSearchedPostFragment(String searchParam) {
        return createPostListFragment(PostListType.SEARCH_POSTS, null, null, null, searchParam, false, 0, null);
    }

    public PostListFragment getWatchedPostsFragment() {
        return createPostListFragment(PostListType.WATCHED_POSTS, null, null, null, null, false, 0, null);
    }

    public PostListFragment getHotTopicResultsFragment(String topic) {
        return createPostListFragment(PostListType.TOPIC_POSTS, null, null, null, topic, false, 0, null);
    }

    public PostListFragment getFeedsListFragment() {
        return createPostListFragment(PostListType.HOME_FEEDS, null, null, null, null, false, 0, null);
    }

    public PostListFragment getMentionsListFragment() {
        return createPostListFragment(PostListType.MENTION_LIST, null, null, null, null, false, 0, null);
    }

    public PostListFragment getGroupPostsFragment(String groupId) {
        return createPostListFragment(PostListType.GROUP_POSTS, null, null, groupId, null, false, 0, null);
    }
    
    public HotTopicsFragment getHotTopicsFragment() {
        HotTopicsFragment fragment = new HotTopicsFragment();
        Bundle args = new Bundle();
        args.putInt(BaseSearchFragment.PARAM_INITIAL_MODE, Mode.FILTERING.value());
        args.putBoolean(BaseSearchFragment.PARAM_SHOULD_AUTOFOCUS_SEARCH_BOX, false);
        args.putBoolean(BaseSearchFragment.PARAM_CAN_EXIT_FILTERING_MODE, false);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * @param isPostInGroup
     * @param object
     * @param id
     */
    public SinglePostFragment getSinglePostFragment(@NonNull String postId, boolean isPostInGroup, HeaderTab selectedTab, boolean isReplyOrReshare) {
        SinglePostFragment fragment = new SinglePostFragment();

        Bundle args = new Bundle();
        args.putBoolean(SinglePostFragment.PARAM_IS_POST_IN_GROUP, isPostInGroup);
        args.putString(SinglePostFragment.PARAM_POST_ID, postId);
        args.putBoolean(SinglePostFragment.PARAM_IS_REPLY_OR_RESHARE, isReplyOrReshare);
        if (selectedTab != null) {
            args.putInt(SinglePostFragment.PARAM_SELECTED_TAB, selectedTab.ordinal());
        }
        fragment.setArguments(args);

        return fragment;
    }


    public ProfileFragment getProfileFragment(String username) {
        ProfileFragment fragment = new ProfileFragment();
        
        Bundle args = new Bundle();
        args.putString(ProfileFragment.PARAM_USERNAME, username.toLowerCase());
        fragment.setArguments(args);

        return fragment;
    }
    
    public MiniProfilePopupFragment getMiniProfilePopupFragment(String username) {
        MiniProfilePopupFragment fragment = new MiniProfilePopupFragment();
        
        Bundle args = new Bundle();
        args.putString(ProfileFragment.PARAM_USERNAME, username.toLowerCase());
        args.putInt(BaseDialogFragment.SAVED_THEME, R.style.DialogFragment_Mini_Profile);
        args.putBoolean(BaseDialogFragment.SAVED_DIMISS_TOUCH_OUTSIDE, true);
        fragment.setArguments(args);

        return fragment;
    }

    public MiniProfileChatFragment getMiniProfileChatFragment(String username) {
        MiniProfileChatFragment fragment = new MiniProfileChatFragment();
        
        Bundle args = new Bundle();
        args.putString(ProfileFragment.PARAM_USERNAME, username.toLowerCase());
        fragment.setArguments(args);

        return fragment;
    }

    /**
     * @param username
     */
    public FullProfileFragment getFullProfileFragment(String username) {
        FullProfileFragment fragment = new FullProfileFragment();
        
        Bundle args = new Bundle();
        args.putString(FullProfileFragment.PARAM_USERNAME, username.toLowerCase());
        fragment.setArguments(args);

        return fragment;
    }
    
    public LocationListFragment getLocationListFragment(LocationListFragment.EventListener listener, LocationListItem selectedItem) {
        LocationListFragment fragment = new LocationListFragment(listener, selectedItem);

        Bundle args = new Bundle();
        args.putInt(BaseSearchFragment.PARAM_INITIAL_MODE, Mode.FILTERING.value());
        args.putBoolean(BaseListFragment.PARAM_SHOULD_DISPLAY_LIST_MESSAGE, true);
        fragment.setArguments(args);
        
        return fragment;
    }

    /**
     * @param username
     */
    public RequestFollowFragment getRequestFollowFragment(String username) {
        RequestFollowFragment fragment = new RequestFollowFragment();

        Bundle args = new Bundle();
        args.putString(RequestFollowFragment.PARAM_USERNAME, username.toLowerCase());
        args.putBoolean(BaseDialogFragment.SAVED_SHOULD_DISMISS_ON_TOUCH, true);
        fragment.setArguments(args);

        return fragment;
    }

    /**
     * @param username
     */
    public BadgesFragment getBadgesFragment(String username) {
        BadgesFragment fragment = new BadgesFragment();
        Bundle args = new Bundle();
        args.putString(BadgesFragment.PARAM_USERNAME, username.toLowerCase());
        fragment.setArguments(args);

        return fragment;
    }

    /**
     * Display the ChatFragment based on a conversationId
     * 
     * @param manager
     * @param conversationId
     */
    public ChatFragment getChatFragmentForId(String conversationId) {
        ChatFragment chatFragment = new ChatFragment();

        Bundle args = new Bundle();
        args.putString(ChatFragment.PARAM_CONVERSATION_ID, conversationId);
        chatFragment.setArguments(args);

        return chatFragment;
    }
    
    public ChatFragment getChatFragmentForChatId(String chatId, ChatTypeEnum chatType,
                                                 MessageType imMessageType,
                                                 boolean isFromSystemNotification) {
        ChatFragment chatFragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putString(ChatFragment.PARAM_CONVERSATION_CHATID, chatId);
        args.putByte(ChatFragment.PARAM_CHAT_TYPE, chatType.getValue());
        args.putByte(ChatFragment.PARAM_IM_MESSAGE_TYPE, imMessageType.getValue());
        args.putBoolean(ChatFragment.PARAM_IS_FROM_SYSTEM_NOTIFICATION, isFromSystemNotification);
        chatFragment.setArguments(args);

        return chatFragment;
    }

    public ChatListFragment getChatListFragment(boolean allowToFilter, boolean isForSelectChat) {
        ChatListFragment fragment = new ChatListFragment();

        Bundle args = new Bundle();

        if (allowToFilter) {
            args.putInt(BaseSearchFragment.PARAM_INITIAL_MODE, Mode.FILTERABLE.value());
        }
        args.putBoolean(ChatListFragment.PARAM_IS_SELECT_CHAT, isForSelectChat);

        fragment.setArguments(args);

        return fragment;
    }
    
    /**
     * Shows the showParticipantListFragment
     * 
     * @param manager
     *            The FragmentManager of the currently shown activity.
     */
    public ParticipantListFragment getParticipantListFragment(String conversationId, boolean isGroupChat,
            boolean isChatroom) {
        ParticipantListFragment participantList = new ParticipantListFragment();

        Bundle args = new Bundle();
        args.putString(ChatFragment.PARAM_CONVERSATION_ID, conversationId);
        args.putBoolean(ChatFragment.PARAM_CHATROOM, isChatroom);
        participantList.setArguments(args);

        return participantList;
    }

    /**
     * Display the StartChatFragment
     * 
     * @param manager
     * @param preselectedUsers
     */
    public StartChatFragment getStartChatFragment(StartChatActionType action, String conversationId,
            ArrayList<String> preselectedUsers) {
        StartChatFragment fragment = new StartChatFragment();

        Bundle args = new Bundle();
        args.putInt(StartChatFragment.PARAM_START_CHAT_ACTION, action.getType());
        args.putStringArrayList(StartChatFragment.PARAM_INITIAL_SELECTED_USERS, preselectedUsers);
        args.putString(StartChatFragment.PARAM_CONVERSATION_ID, conversationId);
        fragment.setArguments(args);

        return fragment;
    }

    public CreateChatroomFragment getCreateChatroomFragment() {

        CreateChatroomFragment fragment = new CreateChatroomFragment();

        return fragment;
    }

    public FriendListFragment createFriendListFragment(boolean displayGroupedContacts,
            FriendListItemActionType actionType, ArrayList<String> preselectedUsers, 
            boolean showOnlineFriendsOnly) {

        FriendListFragment myFriendList = new FriendListFragment();
        
        Bundle args = new Bundle();
        args.putBoolean(FriendListFragment.PARAM_ENABLE_GROUPED_CONTACTS, displayGroupedContacts);
        args.putInt(FriendListFragment.PARAM_LIST_ITEM_ACTION_TYPE, actionType.getType());
        if (preselectedUsers != null) {
            args.putStringArrayList(FriendListFragment.PARAM_INITIAL_SELECTED_USERS, preselectedUsers);
        }
        args.putBoolean(FriendListFragment.PARAM_SHOW_ONLINE_FRIENDS_ONLY, showOnlineFriendsOnly);
        myFriendList.setArguments(args);

        return myFriendList;
    }
    
    public GlobalSearchFragment createGlobalSearchFragment(SearchType searchType, String searchParam) {
        GlobalSearchFragment fragment = new GlobalSearchFragment();

        Bundle args = new Bundle();
        args.putInt(GlobalSearchFragment.PARAM_SEARCH_TYPE, searchType.getValue());
        args.putInt(BaseSearchFragment.PARAM_INITIAL_MODE, Mode.FILTERING.value());
        args.putString(BaseSearchFragment.PARAM_INITIAL_FILTER_TEXT, searchParam);
        args.putBoolean(BaseSearchFragment.PARAM_CAN_EXIT_FILTERING_MODE, false);
        fragment.setArguments(args);

        return fragment;
    }
    
    public GlobalSearchPreviewFragment createGlobalSearchPreviewFragment(String searchParam) {
        GlobalSearchPreviewFragment fragment = new GlobalSearchPreviewFragment();
        
        Bundle args = new Bundle();
        args.putInt(BaseSearchFragment.PARAM_INITIAL_MODE, Mode.FILTERING.value());
        args.putString(BaseSearchFragment.PARAM_INITIAL_FILTER_TEXT, searchParam);
        args.putBoolean(BaseSearchFragment.PARAM_CAN_EXIT_FILTERING_MODE, false);
        fragment.setArguments(args);
        
        return fragment;
    }

    public StoreSearchPreviewFragment createStoreSearchPreviewFragment(String searchParam, String initialRecipient,
                                                                       StorePagerItem.StorePagerType pagerType) {
        StoreSearchPreviewFragment fragment = new StoreSearchPreviewFragment();

        Bundle args = new Bundle();
        args.putInt(BaseSearchFragment.PARAM_INITIAL_MODE, Mode.FILTERING.value());
        args.putString(BaseSearchFragment.PARAM_INITIAL_FILTER_TEXT, searchParam);
        args.putBoolean(BaseSearchFragment.PARAM_CAN_EXIT_FILTERING_MODE, false);
        args.putInt(StoreSearchPreviewFragment.STORE_ITEM_TYPE, pagerType.getValue());
        if (initialRecipient != null) {
            args.putString(StoreSearchPreviewFragment.PARAM_INITIAL_RECIPIENT, initialRecipient);
        }
        fragment.setArguments(args);

        return fragment;

    }

    /**
     * Creates an instance of {@link ProfileListFragment} with the given
     * {@link ProfileListType}.
     * 
     * @param type
     *            The {@link ProfileListType} for the
     *            {@link ProfileListFragment}.
     * @param userName
     *            The username of the user for whom the profiles will be
     *            displayed.
     * @param searchString
     *            A string that contains the search query if the fragment is of
     *            type {@link ProfileListType#SEARCH_RESULTS}.
     * @param mode
     *            The filter {@link BaseSearchFragment.Mode} to be used for display of the fragment. If this value is 
     *            null then {@link BaseSearchFragment.Mode#NO_FILTER} is used.
     * @return
     */
    private ProfileListFragment createProfileListFragment(ProfileListType type, String userName, String searchString,
            Mode mode) {
        ProfileListFragment profileList = new ProfileListFragment();

        Bundle args = new Bundle();
        args.putInt(ProfileListFragment.PARAM_PROFILE_LIST_TYPE, type.getType());
        if (!TextUtils.isEmpty(userName)) {
            args.putString(ProfileListFragment.PARAM_REQUESTING_USERNAME, userName);
        }
        if (!TextUtils.isEmpty(searchString)) {
            args.putString(ProfileListFragment.PARAM_SEARCH_STRING, searchString);
        }
        
        if (mode != null) {
            args.putInt(BaseSearchFragment.PARAM_INITIAL_MODE, mode.value());
        }

        profileList.setArguments(args);

        return profileList;
    }

    /**
     * Creates a {@link ProfileListFragment} of type
     * {@link ProfileListType#SEARCH_RESULTS}
     * 
     * @param searchString
     *            A String containing the search query.
     * @return An instance of {@link ProfileListFragment}
     */
    public ProfileListFragment getSearchedUsersFragment(String searchString) {
        return createProfileListFragment(ProfileListType.SEARCH_RESULTS, null, searchString, null);
    }

    /**
     * Creates a {@link ProfileListFragment} of type
     * {@link ProfileListType#FOLLOWERS}
     * 
     * @param userName
     *            The name of the user who followers will be displayed.
     * @param showSearch
     *            Whether a filterable search mode should be used for display or not.
     * @return An instance of {@link ProfileListFragment}.
     */
    public ProfileListFragment getFollowersListFragment(String userName, boolean showSearch) {
        return createProfileListFragment(ProfileListType.FOLLOWERS, userName, null, Mode.FILTERABLE);
    }

    /**
     * Creates a {@link ProfileListFragment} of type
     * {@link ProfileListType#FOLLOWING}
     * 
     * @param userName
     *            The name of the user who following will be displayed.
     * @param showSearch
     *            Whether a filterable search mode should be used for display or not.
     * @return An instance of {@link ProfileListFragment}.
     */
    public ProfileListFragment getFollowingListFragment(String userName, boolean showSearch) {
        return createProfileListFragment(ProfileListType.FOLLOWING, userName, null, Mode.FILTERABLE);
    }

    public ProfileListFragment getRecommendedUsersFragment() {
        return createProfileListFragment(ProfileListType.RECOMMENDED_PEOPLE, Session.getInstance().getUsername(), null,
                Mode.FILTERABLE);
    }

    public ProfileListFragment getRecommendedUsersLiteFragment() {
        return createProfileListFragment(ProfileListType.RECOMMENDED_PEOPLE_LITE, Session.getInstance().getUsername(), null,
                Mode.FILTERABLE);
    }

    public ProfileListFragment getRecommendedContactsFragment() {
        return createProfileListFragment(ProfileListType.PEOPLE_YOU_KNOW, Session.getInstance().getUsername(), null,
                Mode.FILTERABLE);
    }

    /**
     * @param username
     * @param id
     * @return
     */
    public BadgeInfoFragment getBadgeInfoFragment(String username, Integer badgeId) {
        BadgeInfoFragment fragment = new BadgeInfoFragment();
        Bundle args = new Bundle();
        args.putString(BadgeInfoFragment.PARAM_USERNAME, username.toLowerCase());
        args.putInt(BadgeInfoFragment.PARAM_BADGE_ID, badgeId);

        fragment.setArguments(args);

        return fragment;
    }

    public GroupPageFragment getGroupFragmnet(String groupId) {
        GroupPageFragment groupFragment = new GroupPageFragment();
        Bundle args = new Bundle();
        args.putInt(PostListFragment.PARAM_POST_LIST_TYPE, PostListType.GROUP_POSTS.getId());
        args.putString(PostListFragment.PARAM_GROUP_ID, groupId);

        groupFragment.setArguments(args);

        return groupFragment;
    }

    public ShareboxFragment getShareboxFragment(ShareboxActionType action, String postId, String prefix,
            Uri presetPhotoUri, String groupId, boolean allowPostWhenReply, ShareboxSubActionType subAction) {
        ShareboxFragment shareboxFragment = new ShareboxFragment();

        Bundle args = new Bundle();
        args.putInt(ShareboxFragment.PARAM_SHAREBOX_ACTION, action.getType());
        args.putString(ShareboxFragment.PARAM_SHAREBOX_POST_ID, postId);
        args.putString(ShareboxFragment.PARAM_SHAREBOX_PREFIX, prefix);
        args.putParcelable(ShareboxFragment.PARAM_SHAREBOX_PHOTOURI, presetPhotoUri);
        args.putString(ShareboxFragment.PARAM_SHAREBOX_GROUP_ID, groupId);
        args.putBoolean(ShareboxFragment.PARAM_SHAREBOX_ALLOW_POST_WHEN_REPLY, allowPostWhenReply);
        args.putBoolean(BaseDialogFragment.SAVED_IS_TRANSPARENT, true);
        args.putInt(ShareboxFragment.PARAM_SHAREBOX_SUBACTION, subAction.getType());
        shareboxFragment.setArguments(args);

        return shareboxFragment;
    }

    public SettingsFragment getSettingsFragment(SettingsGroupType type) {
        SettingsFragment settingsFragment = new SettingsFragment();

        Bundle args = new Bundle();
        args.putInt(SettingsFragment.PARAM_SETTINGS_GROUP_TYPE, type.getType());
        settingsFragment.setArguments(args);

        return settingsFragment;
    }
    
    public GameCentreFragment getGameCentreFragment(){
        GameCentreFragment gameFragment = new GameCentreFragment();
        return gameFragment;
    }
    
    public DeezerDetailListFragment getDeezerDetailListFragment(){
        DeezerDetailListFragment fragment = new DeezerDetailListFragment();
        return fragment;
    }
    
    public ShareToFragment getShareToFragment(ShareItemListener listener){
        ShareToFragment shareToFragment = new ShareToFragment();
        Bundle args = new Bundle();

        args.putInt(BaseDialogFragment.SAVED_THEME, R.style.DialogFragment_Push_Up);
        args.putInt(BaseDialogFragment.SAVED_GRAVITY, Gravity.BOTTOM);
        shareToFragment.setArguments(args);
        shareToFragment.setShareItemListener(listener);
        return shareToFragment;
    }

    public ShareToFragment getShareToFragmentForDeezer(ShareItemListener listener){
        ShareToFragment shareToFragment = new ShareToFragment();
        Bundle args = new Bundle();
        args.putBoolean(BaseDialogFragment.FROM_DEEZER, true);
        args.putInt(BaseDialogFragment.SAVED_THEME, R.style.DialogFragment_Push_Up);
        args.putInt(BaseDialogFragment.SAVED_GRAVITY, Gravity.BOTTOM);
        shareToFragment.setArguments(args);
        shareToFragment.setShareItemListener(listener);
        return shareToFragment;
    }

    public LoginDialogFragment getLoginDialogFragment(){
        LoginDialogFragment fragment = new LoginDialogFragment();

        Bundle args = new Bundle();
        args.putInt(BaseDialogFragment.SAVED_THEME, R.style.DialogFragment_login);
        args.putInt(BaseDialogFragment.SAVED_GRAVITY, Gravity.BOTTOM);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Creates a {@link PhotoViewerFragment}
     * 
     * @param imageUrl
     *            The url of the photo to be displayed in the fragment.
     * @param photoSender
     *            The username of the photo sender.
     * @param photoBitmap
     *            A Bitmap that can be displayed as the photo in the fragment.
     *            If this value is non-null, then the Bitmap is used instead of
     *            the imageUrl.
     * @param shouldAllowSaveToDevice
     *            Whether the "save to device" icon should be shown in the
     *            {@link PhotoViewerFragment} or not.
     * @param isUrlForWebPage
     *            Whether the image should be displayed in a web view or not.
     *            Currently, we only show gif images in a web view.
     * @return The {@link PhotoViewerFragment} that was created.
     */
    public PhotoViewerFragment getPhotoViewerFragment(String imageUrl, String photoSender, Bitmap photoBitmap, 
            boolean shouldAllowSaveToDevice, boolean isUrlForWebPage) {
        PhotoViewerFragment photoViewerFragment = new PhotoViewerFragment();

        Bundle args = new Bundle();
        args.putString(PhotoViewerFragment.PARAM_IMAGE_URL, imageUrl);
        args.putString(PhotoViewerFragment.PARAM_PHOTO_SENDER, photoSender);
        args.putBoolean(PhotoViewerFragment.PARAM_ALLOW_SAVE_TO_DEVICE, shouldAllowSaveToDevice);
        args.putBoolean(PhotoViewerFragment.PARAM_IS_URL_FOR_WEB_PAGE, isUrlForWebPage);
        photoViewerFragment.setArguments(args);

        photoViewerFragment.setImageBitmap(photoBitmap);

        return photoViewerFragment;
    }

    public StorePagerFragment getStoreFragment(int selectedIndex, String initialRecipient) {
        StorePagerFragment fragment = new StorePagerFragment();

        Bundle args = new Bundle();
        args.putInt(StorePagerFragment.PARAM_SELECTED_TAB, selectedIndex);
        args.putInt(BaseSearchFragment.PARAM_INITIAL_MODE, Mode.FILTERABLE.value());
        if(initialRecipient != null) {
            args.putString(StorePagerFragment.PARAM_INITIAL_RECIPIENT, initialRecipient);
        }

        fragment.setArguments(args);

        return fragment;
    }

    public GiftCategoryFragment getGiftCategoryFragment(StoreItemFilterType giftFilterType, String categoryId,
            String categoryName, String searchString, float minPrice, float maxPrice, boolean isInChat,
            String conversationId, String initialRecipient) {
        GiftCategoryFragment fragment = new GiftCategoryFragment();

        Bundle args = new Bundle();

        if (giftFilterType != null) {
            args.putInt(GiftCategoryFragment.PARAM_GIFT_FILTER_TYPE, giftFilterType.getValue());
        }
        if (categoryId != null) {
            args.putString(GiftCategoryFragment.PARAM_GIFT_CATEGORY_ID, categoryId);
        }
        args.putString(GiftCategoryFragment.PARAM_GIFT_CATEGORY_NAME, categoryName);
        args.putString(GiftCategoryFragment.PARAM_SEARCH_STRING, searchString);
        if(initialRecipient != null) {
            args.putString(GiftCategoryFragment.PARAM_INITIAL_RECIPIENT, initialRecipient);
        }

        args.putFloat(GiftCategoryFragment.PARAM_SEARCH_MIN_PRICE, minPrice);
        args.putFloat(GiftCategoryFragment.PARAM_SEARCH_MAX_PRICE, maxPrice);

        args.putBoolean(GiftCategoryFragment.PARAM_IS_IN_CHAT, isInChat);
        if (isInChat) {
            args.putString(GiftCategoryFragment.PARAM_CONVERSATION_ID, conversationId);
        }

        fragment.setArguments(args);

        return fragment;
    }

    public GiftFragment getGiftFragment(String giftId, String initialRecipient, String counterId) {
        GiftFragment fragment = new GiftFragment();

        Bundle args = new Bundle();
        args.putString(GiftFragment.PARAM_GIFT_ITEM_ID, giftId);
        if(initialRecipient != null) {
            args.putString(GiftFragment.PARAM_INITIAL_RECIPIENT, initialRecipient);
        }
        if(counterId != null) {
            args.putString(GiftFragment.PARAM_COUNTER_ID, counterId);
        }
        fragment.setArguments(args);

        return fragment;
    }
    
    public UnlockedGiftFragment getUnlockedGiftFragment(String giftId) {
        UnlockedGiftFragment fragment = new UnlockedGiftFragment();
        
        Bundle args = new Bundle();
        args.putString(GiftFragment.PARAM_GIFT_ITEM_ID, giftId);
        fragment.setArguments(args);
        
        return fragment;
    }

    public GiftPurchasedFragment getGiftPurchasedFragment(String giftId, String recipients) {
        GiftPurchasedFragment fragment = new GiftPurchasedFragment();

        Bundle args = new Bundle();
        args.putString(GiftPurchasedFragment.PARAM_GIFT_ITEM_ID, giftId);
        args.putString(GiftPurchasedFragment.PARAM_GIFT_RECIPIENTS, recipients);
        fragment.setArguments(args);

        return fragment;
    }

    public GiftCenterFragment getGiftCenterFragment(String conversationId, String initialRecipient) {
        GiftCenterFragment fragment = new GiftCenterFragment();

        Bundle args = new Bundle();
        args.putString(GiftCenterFragment.PARAM_CONVERSATION_ID, conversationId);
        if(initialRecipient != null) {
            args.putString(GiftCenterFragment.PARAM_INITIAL_RECIPIENT, initialRecipient);
        }
        fragment.setArguments(args);

        return fragment;
    }

    public SinglePostGiftFragment getSinglePostGiftFragment(String rootPostId, String parentPostId) {
        SinglePostGiftFragment fragment = new SinglePostGiftFragment();

        Bundle args = new Bundle();
        args.putString(SinglePostGiftFragment.PARAM_POST_ROOT_ID, rootPostId);
        args.putString(SinglePostGiftFragment.PARAM_POST_PARENT_ID, parentPostId);
        fragment.setArguments(args);

        return fragment;
    }

    public GiftCenterFragment getGiftCenterFragment(ArrayList<String> selectedUser, String initialRecipient) {
        GiftCenterFragment fragment = new GiftCenterFragment();

        Bundle args = new Bundle();
        args.putStringArrayList(GiftCenterFragment.PARAM_SELECTED_USERS, selectedUser);
        if(initialRecipient != null) {
            args.putString(GiftCenterFragment.PARAM_INITIAL_RECIPIENT, initialRecipient);
        }
        fragment.setArguments(args);

        return fragment;
    }

    public SendGiftFragment getSendGiftFragment(ArrayList<String> recipientList,
                                                SendGiftFragment.ActionType actionType,
                                                String conversationId) {
        SendGiftFragment fragment = new SendGiftFragment();

        Bundle args = new Bundle();

        args.putStringArrayList(SendGiftFragment.PARAM_RECIPIENT, recipientList);
        args.putInt(SendGiftFragment.PARAM_ACTION_TYPE, actionType.getType());
        args.putString(SendGiftFragment.PARAM_CONVERSATION_ID, conversationId);

        fragment.setArguments(args);

        return fragment;
    }

    public SendGiftFragment getSendGiftFragment(ArrayList<String> recipientList,
                                                SendGiftFragment.ActionType actionType,
                                                String rootPostId, String postId) {
        SendGiftFragment fragment = new SendGiftFragment();

        Bundle args = new Bundle();

        args.putStringArrayList(SendGiftFragment.PARAM_RECIPIENT, recipientList);
        args.putInt(SendGiftFragment.PARAM_ACTION_TYPE, actionType.getType());
        args.putString(SendGiftFragment.PARAM_POST_ROOT_ID, rootPostId);
        args.putString(SendGiftFragment.PARAM_POST_ID, postId);

        fragment.setArguments(args);

        return fragment;
    }

    public GiftCenterGiftCategoryFragment getGiftCenterGiftCategoryFragment(StoreItemFilterType giftFilterType,
            String conversationId, String giftCategoryId, String giftCategoryName, String initialRecipient,
            ArrayList<String> selectedUsers) {
        
        GiftCenterGiftCategoryFragment fragment = new GiftCenterGiftCategoryFragment();

        Bundle args = new Bundle();
        args.putBoolean(GiftCenterGiftCategoryFragment.PARAM_IS_FROM_SINGLE_POST, false);
        args.putInt(GiftCenterGiftCategoryFragment.PARAM_GIFT_FILTER_TYPE, giftFilterType.getValue());
        args.putString(GiftCenterGiftCategoryFragment.PARAM_CONVERSATION_ID, conversationId);
        args.putString(GiftCenterGiftCategoryFragment.PARAM_GIFT_CATEGORY_ID, giftCategoryId);
        args.putString(GiftCenterGiftCategoryFragment.PARAM_GIFT_CATEGORY_NAME, giftCategoryName);
        if(initialRecipient != null) {
            args.putString(GiftCenterGiftCategoryFragment.PARAM_INITIAL_RECIPIENT, initialRecipient);
        }
        if (selectedUsers != null) {
            args.putStringArrayList(GiftCenterGiftCategoryFragment.PARAM_SELECTED_USERS, selectedUsers);
        }

        fragment.setArguments(args);

        return fragment;
    }

    public GiftCenterGiftCategoryFragment getSinglePostGiftCategoryFragment(StoreItemFilterType giftFilterType, String rootPostId, String parentPosttId) {

        GiftCenterGiftCategoryFragment fragment = new GiftCenterGiftCategoryFragment();

        Bundle args = new Bundle();
        args.putBoolean(GiftCenterGiftCategoryFragment.PARAM_IS_FROM_SINGLE_POST, true);
        args.putString(GiftCenterGiftCategoryFragment.PARAM_POST_ROOT_ID, rootPostId);
        args.putString(GiftCenterGiftCategoryFragment.PARAM_POST_PARENT_ID, parentPosttId);
        args.putInt(GiftCenterGiftCategoryFragment.PARAM_GIFT_FILTER_TYPE, giftFilterType.getValue());

        fragment.setArguments(args);

        return fragment;
    }

    public GiftCategoryParentFragment getGiftCategoryParentFragment(StoreItemFilterType giftFilterType,
            String conversationId, String giftCategoryId, String giftCategoryName, String initialRecipient,
            ArrayList<String> selectedUsers) {
        
        GiftCategoryParentFragment fragment = new GiftCategoryParentFragment();

        Bundle args = new Bundle();
        args.putInt(GiftCategoryParentFragment.PARAM_GIFT_FILTER_TYPE, giftFilterType.getValue());
        args.putString(GiftCategoryParentFragment.PARAM_CONVERSATION_ID, conversationId);
        args.putString(GiftCategoryParentFragment.PARAM_GIFT_CATEGORY_ID, giftCategoryId);
        args.putString(GiftCategoryParentFragment.PARAM_GIFT_CATEGORY_NAME, giftCategoryName);
        if(initialRecipient != null) {
            args.putString(GiftCategoryParentFragment.PARAM_INITIAL_RECIPIENT, initialRecipient);
        }
        if (selectedUsers != null) {
            args.putStringArrayList(GiftCategoryParentFragment.PARAM_SELECTED_USERS, selectedUsers);
        }

        fragment.setArguments(args);

        return fragment;
    }

    public GiftCenterCategoryListFragment getGiftCenterCategoryListFragment(String conversationId, String initialRecipient, ArrayList<String> selectedUsers) {
        GiftCenterCategoryListFragment fragment = new GiftCenterCategoryListFragment();

        Bundle args = new Bundle();
        args.putString(GiftCenterCategoryListFragment.PARAM_CONVERSATION_ID, conversationId);
        if(initialRecipient != null) {
            args.putString(GiftCenterCategoryListFragment.PARAM_INITIAL_RECIPIENT, initialRecipient);
        }
        if (selectedUsers != null) {
            args.putStringArrayList(GiftCenterCategoryListFragment.PARAM_SELECTED_USERS, selectedUsers);
        }

        fragment.setArguments(args);

        return fragment;
    }

    public GiftPreviewFragment getSinglePostGiftPreviewFragment(String giftId, String rootPostId, String parentPostId) {
        GiftPreviewFragment fragment = new GiftPreviewFragment();

        Bundle args = new Bundle();
        args.putString(GiftPreviewFragment.PARAM_POST_ROOT_ID, rootPostId);
        args.putString(GiftPreviewFragment.PARAM_POST_PARENT_ID, parentPostId);
        args.putBoolean(GiftPreviewFragment.PARAM_IS_FROM_SINGLE_POST, true);
        args.putString(GiftPreviewFragment.PARAM_GIFT_ITEM_ID, giftId);
        fragment.setArguments(args);

        return fragment;
    }

    public GiftPreviewFragment getGiftPreviewFragment(String giftId, String conversationId, boolean isFromRecent, 
            String selectedRecipient, ArrayList<String> selectedUsers) {
        GiftPreviewFragment fragment = new GiftPreviewFragment();

        Bundle args = new Bundle();
        args.putString(GiftPreviewFragment.PARAM_GIFT_ITEM_ID, giftId);
        args.putString(GiftPreviewFragment.PARAM_CONVERSATION_ID, conversationId);
        args.putBoolean(GiftPreviewFragment.PARAM_IS_FROM_RECENT, isFromRecent);
        args.putString(GiftPreviewFragment.PARAM_SELECTED_RECIPIENT, selectedRecipient);
        args.putStringArrayList(GiftPreviewFragment.PARAM_SELECTED_USERS, selectedUsers);
        fragment.setArguments(args);

        return fragment;
    }
    
    public GiftRecipientSelectionFragment getGiftRecipientSelectionFragment(String giftId, String conversationId, 
            boolean isFromRecent, ArrayList<String> selectedUsers) {
        GiftRecipientSelectionFragment fragment = new GiftRecipientSelectionFragment();

        Bundle args = new Bundle();
        args.putString(GiftRecipientSelectionFragment.PARAM_GIFT_ITEM_ID, giftId);
        args.putString(GiftRecipientSelectionFragment.PARAM_CONVERSATION_ID, conversationId);
        args.putBoolean(GiftPreviewFragment.PARAM_IS_FROM_RECENT, isFromRecent);
        args.putStringArrayList(GiftPreviewFragment.PARAM_SELECTED_USERS, selectedUsers);

        fragment.setArguments(args);

        return fragment;
    }

    public StoreSearchFragment getStoreSearchResultsFragment(StorePagerItem.StorePagerType type, String searchParam,
                                                             String initialRecipient) {
        StoreSearchFragment fragment = new StoreSearchFragment();

        Bundle args = new Bundle();
        args.putInt(BaseSearchFragment.PARAM_INITIAL_MODE, Mode.FILTERING.value());
        args.putString(BaseSearchFragment.PARAM_INITIAL_FILTER_TEXT, searchParam);
        args.putBoolean(BaseSearchFragment.PARAM_CAN_EXIT_FILTERING_MODE, false);
        args.putInt(StoreSearchFragment.STORE_ITEM_TYPE, type.getValue());
        if (initialRecipient != null) {
            args.putString(StoreSearchPreviewFragment.PARAM_INITIAL_RECIPIENT, initialRecipient);
        }

        fragment.setArguments(args);

        return fragment;
    }

    public ChatManagerFragment getChatManagerFragment() {
        ChatManagerFragment fragment = new ChatManagerFragment();
        return fragment;
    }
    
    public AttachmentFragment getAttachmentFragment(int packId, int numOfColumn) {
        AttachmentFragment fragment = new AttachmentFragment();
        Bundle args = new Bundle();
        args.putInt(AttachmentPagerFragment.PARAM_PACK_ID, packId);
        args.putInt(AttachmentPagerFragment.PARAM_PAGER_COLUMNS, numOfColumn);
        fragment.setArguments(args);

        return fragment;
    }

    public LoginFormFragment getLoginFragment() {
        return new LoginFormFragment();
    }
    
    public GiftSentFragment getGiftSentFragment() {
        GiftSentFragment fragment = new GiftSentFragment();
        
        Bundle args = new Bundle();
        args.putInt(BaseDialogFragment.SAVED_THEME, R.style.DialogFragment_Gift);
        fragment.setArguments(args);
        
        return fragment;
    }
    
    public AccountBalanceFragment getAccountBalanceFragment() {
        AccountBalanceFragment fragment = new AccountBalanceFragment();
        
        Bundle args = new Bundle();
        args.putInt(BaseDialogFragment.SAVED_THEME, R.style.DialogFragment_Gift);
        fragment.setArguments(args);
        
        return fragment;
    }

    public StoreFilterFragment getStoreFilterFragment(StoreFilterType filterType, int storeType,
                                                      StoreSortingListener listener, int categoryId, String giftFilterType) {
        StoreFilterFragment fragment = new StoreFilterFragment();

        Bundle args = new Bundle();
        args.putInt(StoreFilterFragment.PARAM_FILTER_TYPE, filterType.getType());
        args.putString(StoreFilterFragment.PARAM_GIFT_FILTER_TYPE, giftFilterType);
        args.putInt(StoreFilterFragment.PARAM_CATEGORY_ID, categoryId);
        args.putInt(StoreFilterFragment.PARAM_STORE_TYPE, storeType);
        args.putInt(BaseDialogFragment.SAVED_THEME, R.style.DialogFragment_Push_Up);
        args.putInt(BaseDialogFragment.SAVED_GRAVITY, Gravity.BOTTOM);
        fragment.setArguments(args);
        fragment.setStoreSortingListener(listener);

        return fragment;
    }

    public MyStickersFragment getMyStickersFragment() {
        return new MyStickersFragment();
    }
    
    public MyGiftsPagerFragment getMyGiftsPagerFragment(String userId) {
        MyGiftsPagerFragment fragment = new MyGiftsPagerFragment();

        Bundle args = new Bundle();
        args.putString(FragmentUtils.PARAM_USERID, userId);
        fragment.setArguments(args);

        return fragment;
    }
    
    @SuppressWarnings("rawtypes")
    public MyGiftsOverviewFragment getMyGiftsOverviewFragment(String userId) {
        MyGiftsOverviewFragment fragment = new MyGiftsOverviewFragment();

        Bundle args = new Bundle();
        args.putString(FragmentUtils.PARAM_USERID, userId);
        fragment.setArguments(args);

        return fragment;
    }
    
    public MyGiftsOverviewFilterFragment getMyGiftsOverviewFilterFragment(MyGiftsOverviewFilterType filterType,
            int selectedFilter, MyGiftsOverviewSortingListener listener, String userId) {

        MyGiftsOverviewFilterFragment fragment = new MyGiftsOverviewFilterFragment();

        Bundle args = new Bundle();
        args.putInt(MyGiftsOverviewFilterFragment.PARAM_FILTER_TYPE, filterType.getType());
        args.putInt(MyGiftsOverviewFilterFragment.PARAM_SELECTED_FILTER, selectedFilter);
        args.putInt(BaseDialogFragment.SAVED_THEME, R.style.DialogFragment_Push_Up);
        args.putInt(BaseDialogFragment.SAVED_GRAVITY, Gravity.BOTTOM);
        args.putString(FragmentUtils.PARAM_USERID, userId);
        fragment.setArguments(args);
        fragment.setMyGiftsOverviewSortingListener(listener);

        return fragment;
    }

    public MyGiftsListFragment getMyGiftsListFragment(String userId) {
        MyGiftsListFragment fragment = new MyGiftsListFragment();

        Bundle args = new Bundle();
        args.putString(FragmentUtils.PARAM_USERID, userId);
        fragment.setArguments(args);

        return fragment;
    }

    public MyGiftsAllListFragment getMyAllGiftsListFragment(String userId) {
        MyGiftsAllListFragment fragment = new MyGiftsAllListFragment();

        Bundle args = new Bundle();
        args.putString(FragmentUtils.PARAM_USERID, userId);
        fragment.setArguments(args);

        return fragment;
    }

    public MyGiftsCategoryFragment getMyGiftsCategoryFragment(int categoryIdx, MyGiftsCategoryFragment.CategoryListener listener,
                                                              String userId) {
        MyGiftsCategoryFragment fragment = new MyGiftsCategoryFragment();
        Bundle args = new Bundle();
        args.putInt(BaseDialogFragment.SAVED_THEME, R.style.DialogFragment_Push_Up);
        args.putInt(BaseDialogFragment.SAVED_GRAVITY, Gravity.BOTTOM);
        args.putInt(MyGiftsCategoryFragment.PARAM_IDX, categoryIdx);
        args.putString(FragmentUtils.PARAM_USERID, userId);
        fragment.setArguments(args);
        fragment.setCategoryListener(listener);
        return fragment;
    }

    public MyGiftsCardListFragment getMyGiftsCardListFragment(String title, int category, boolean filter, String userId) {
        MyGiftsCardListFragment fragment = new MyGiftsCardListFragment();
        Bundle args = new Bundle();
        args.putString(MyGiftsCardListFragment.PARAM_TITLE, title);
        args.putBoolean(MyGiftsCardListFragment.PARAM_SHOW_FILTER, filter);
        args.putInt(MyGiftsCardListFragment.PARAM_CATEGORY, category);
        args.putString(FragmentUtils.PARAM_USERID, userId);
        fragment.setArguments(args);
        return fragment;
    }

    public MusicFragment getMusicFragment() {
        return new MusicFragment();
    }

    public MusicGenreFilterFragment getMusicGenreFilterFragment(int selectedIndex, MusicGenreFilterFragment.MusicGenreFilterListener listener) {
        MusicGenreFilterFragment fragment = new MusicGenreFilterFragment();

        Bundle args = new Bundle();
        args.putInt(MusicGenreFilterFragment.PARAM_SELECTED_FILTER, selectedIndex);
        args.putInt(BaseDialogFragment.SAVED_THEME, R.style.DialogFragment_Push_Up);
        args.putInt(BaseDialogFragment.SAVED_GRAVITY, Gravity.BOTTOM);
        fragment.setArguments(args);
        fragment.setMusicGenreFilterListener(listener);

        return fragment;
    }

    public void clearBackStack() {
        FragmentManager manager = ApplicationEx.getInstance().getCurrentActivity().getSupportFragmentManager();
        if (manager.getBackStackEntryCount() > 0) {
            FragmentManager.BackStackEntry first = (BackStackEntry) manager.getBackStackEntryAt(0);
            manager.popBackStack(first.getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }

    public SignupFacebookFailFragment getSignupFacebookFailFragment() {
        return new SignupFacebookFailFragment();
    }

    public SignupFacebookSuccessFragment getSignupFacebookSuccessFragment() {
        return new SignupFacebookSuccessFragment();
    }

    public SignupUsernameFragment getSignupUsernameFragment() {
        return new SignupUsernameFragment();
    }

    public SignupPasswordFragment getSignupPasswordFragment() {
        return new SignupPasswordFragment();
    }

    public SignupEmailFragment getSignupEmailFragment() {
        return new SignupEmailFragment();
    }

    public SignupVerifyFragment getSignupVerifyFragment() {
        return new SignupVerifyFragment();
    }

    public SignupEmailVerifyFragment getSignupEmailVerifyFragment() {
        return new SignupEmailVerifyFragment();
    }

    public SignupEmailResultUsedFragment getSignupEmailResultUsedFragment() {
        return new SignupEmailResultUsedFragment();
    }

    public SignupEmailResultTimeoutFragment getSignupEmailResultTimeoutFragment() {
        return new SignupEmailResultTimeoutFragment();
    }

    public SignupEmailResultExpiredFragment getSignupEmailResultExpiredFragment() {
        return new SignupEmailResultExpiredFragment();
    }

    public SignupEmailResultSuccessFragment getSignupEmailResultSuccessFragment() {
        return new SignupEmailResultSuccessFragment();
    }

    public SignupEmailVerifyingFragment getSignupEmailVerifingFragment() {
        return new SignupEmailVerifyingFragment();
    }

    public BaseFragment getGameDetailPageFragment(GameDetailPageFragmentType gameDetailPageFragmentType, String gameId){
        BaseFragment fragment = null;
        switch (gameDetailPageFragmentType){
            case Main:
                fragment = new GameDetailFragment();
                break;
            case Information:
                fragment = new GameDetailInformationFragment();
                break;
            case Rate:
                break;
            case Rank:
                break;
            case Friends:
                break;

        }

        Bundle bundle = new Bundle();
        //String serializedGameItem = JsonParseUtils.serializeGame(gameItem);
        bundle.putString(GameDetailFragment.KEY_GAME_ITEM, gameId);
        fragment.setArguments(bundle);
        return fragment;
    }

    public static enum GameDetailPageFragmentType{Main, Information, Rate, Rank, Friends};

}
