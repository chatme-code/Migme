/**
 * Copyright (c) 2013 Project Goth
 *
 * ShareboxFragment.java
 * Created Aug 12, 2013, 9:36:52 AM
 */

package com.projectgoth.ui.fragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mig33.diggle.events.Events;
import com.migme.commonlib.enums.ImageFileType;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.HotTopic;
import com.projectgoth.b.data.HotTopicsResult;
import com.projectgoth.b.data.Post;
import com.projectgoth.b.data.Profile;
import com.projectgoth.b.data.ThirdPartySites;
import com.projectgoth.b.data.mime.MimeData;
import com.projectgoth.b.data.mime.MimeTypeDataModel;
import com.projectgoth.b.data.mime.TextRichMimeData;
import com.projectgoth.b.enums.PostOriginalityEnum;
import com.projectgoth.common.Config;
import com.projectgoth.common.Constants;
import com.projectgoth.common.Logger;
import com.projectgoth.common.ShareManager;
import com.projectgoth.common.Theme;
import com.projectgoth.common.ThemeValues;
import com.projectgoth.common.Tools;
import com.projectgoth.common.WebURL;
import com.projectgoth.controller.ThirdPartySitesController;
import com.projectgoth.datastore.PostsDatastore;
import com.projectgoth.datastore.Session;
import com.projectgoth.datastore.SystemDatastore;
import com.projectgoth.datastore.UserDatastore;
import com.projectgoth.enums.AttachmentType;
import com.projectgoth.events.GAEvent;
import com.projectgoth.i18n.I18n;
import com.projectgoth.imagefetcher.UIUtils;
import com.projectgoth.listener.OnSizeChangedListener;
import com.projectgoth.model.ContextMenuItem;
import com.projectgoth.model.InputString;
import com.projectgoth.model.LocationListItem;
import com.projectgoth.model.MenuOption;
import com.projectgoth.model.MenuOption.MenuAction;
import com.projectgoth.model.MenuOption.MenuOptionType;
import com.projectgoth.model.ShareBoxStateData;
import com.projectgoth.model.ShareBoxStateData.PhotoTypeEnum;
import com.projectgoth.nemesis.enums.PostPrivacyEnum;
import com.projectgoth.nemesis.enums.ReplyPermissionEnum;
import com.projectgoth.nemesis.model.BaseEmoticon;
import com.projectgoth.nemesis.model.RequestParams.FormData;
import com.projectgoth.notification.AlertListener;
import com.projectgoth.ui.activity.ActionHandler;
import com.projectgoth.ui.activity.AlertHandler;
import com.projectgoth.ui.fragment.AttachmentPagerFragment.BaseAttachmentFragmentListener;
import com.projectgoth.ui.fragment.AttachmentPhotoFragment.PhotoEventListener;
import com.projectgoth.ui.holder.SimplePostPreviewHolder;
import com.projectgoth.ui.holder.content.ContentViewFactory;
import com.projectgoth.ui.holder.content.ContentViewHolder;
import com.projectgoth.ui.listener.ContextMenuItemListener;
import com.projectgoth.ui.listener.KeyboardListener;
import com.projectgoth.ui.widget.AutoCompleteTextViewEx;
import com.projectgoth.ui.widget.PopupMenu;
import com.projectgoth.ui.widget.RelativeLayoutEx;
import com.projectgoth.ui.widget.ScrollViewEx;
import com.projectgoth.util.AndroidLogger;
import com.projectgoth.util.AnimUtils;
import com.projectgoth.util.ChatUtils;
import com.projectgoth.util.LocationUtils;
import com.projectgoth.util.PostUtils;
import com.projectgoth.util.mime.MimeUtils;

/**
 * @author mapet
 * 
 */
public class ShareboxFragment extends BaseDialogFragment implements OnClickListener, TextWatcher,
        BaseAttachmentFragmentListener, KeyboardListener, PhotoEventListener, OnSizeChangedListener,
        ContextMenuItemListener, LocationListFragment.EventListener, ScrollViewEx.TouchEventsListener {

	private static final String LOG_TAG = AndroidLogger.makeLogTag(ShareboxFragment.class);

	private ScrollViewEx mScrollView;
	private RelativeLayoutEx mSharebox;
	private AutoCompleteTextViewEx mShareEditText;
	private TextView mCharCountContainer;
	private ImageView mEmoticonButton;
	private TextView mLocationText;
	private ImageView mPostButton;
	private ImageView mPrivacyButton;
    private ImageView mAdButton;
	
	private PopupMenu mPopupMenu;
	private ImageView mPopupMenuMarker;

	private ImageView mAttachPhotoButton;
    private ImageView mAttachGalleryButton;
    private ImageView mAttchEmoticonButton;
	private RelativeLayout mPhotoThumbBox;
	private ImageView mPhotoRemoveButton;
	private ImageView mPhotoPreviewAndButton;

	private ImageView mTwitterButton;
	private ImageView mFacebookButton;

	private FrameLayout mEmoticonDrawerGrid;
    private LinearLayout mimeContentContainer;

	private ArrayAdapter<String> mMentionsAdapter;
	private ArrayAdapter<String> mHotTopicsAdapter;

	private boolean isEmoticonDrawerDisplayed;
	private boolean showEmoticonsOnKeyboardHidden;
	private boolean isKeyboardDisplayed;

	// Maintains whether twitter and facebook were explicitly turned off by the
	// user.
	private boolean isTwitterTurnedOff;
	private boolean isFacebookTurnedOff;

	private ReplyPermissionEnum replyPermission = ReplyPermissionEnum.EVERYONE;
	private PostPrivacyEnum postPrivacy = PostPrivacyEnum.EVERYONE;
	private static final int HOT_TOPICS_LIMIT = 30;

	private int textCounterLeft = Constants.MAX_MESSAGE_LENGTH;

	private String postId;
	private String prefix;
	private Uri presetPhotoUri;
	private String groupId;
	private ShareboxActionType action = ShareboxActionType.CREATE_NEW_POST;
    private ShareboxSubActionType subAction = ShareboxSubActionType.NONE;
	private boolean allowPostWhenReply = true;
    private boolean mIsFromSinglePostFragment = false;

	private LocationListItem selectedLocationListItem = null;

    private boolean shouldSaveDataOnDestroy = true;

	public static final String PARAM_SHAREBOX_POST_ID = "PARAM_SHAREBOX_POST_ID";
	public static final String PARAM_SHAREBOX_PREFIX = "PARAM_SHAREBOX_PREFIX";
	public static final String PARAM_SHAREBOX_PHOTOURI = "PARAM_SHAREBOX_PHOTOURI";
	public static final String PARAM_SHAREBOX_GROUP_ID = "PARAM_SHAREBOX_GROUP_ID";
	public static final String PARAM_SHAREBOX_ACTION = "PARAM_SHAREBOX_ACTION";
    public static final String PARAM_SHAREBOX_SUBACTION = "PARAM_SHAREBOX_SUBACTION";
	public static final String PARAM_SHAREBOX_ALLOW_POST_WHEN_REPLY = "PARAM_SHAREBOX_ALLOW_POST_WHEN_REPLY";

	private Bitmap mPhoto;

	private static ShareBoxStateData stateData = new ShareBoxStateData();

    private OnShareBoxDimissListener mOnShareBoxDimissListener;

    public interface OnShareBoxDimissListener {
        void onDismiss(String body);
    }
    public void setOnShareBoxDismissListener(OnShareBoxDimissListener onShareBoxDismissListener) {
        mOnShareBoxDimissListener = onShareBoxDismissListener;
    }


    public enum ShareboxActionType {
        CREATE_NEW_POST(0), CREATE_LONG_POST(1), REPLY_POST(3), REPOST(4), CREATE_NEW_POST_IN_GROUP(5), REPLY_POST_IN_GROUP(6),
        SHARE_TO_POST(7);

        private int type;

        private ShareboxActionType(int type) {
            this.type = type;
        }

        public int getType() {
            return this.type;
        }

        public static ShareboxActionType fromValue(int type) {
            for (ShareboxActionType listType : values()) {
                if (listType.getType() == type) {
                    return listType;
                }
            }
            return CREATE_NEW_POST;
        }
    }

    public enum ShareboxSubActionType {
        NONE(0), OPEN_CAMERA(1), OPEN_GALLERY(2), OPEN_EMOTICON_DRAWER(3);

        private int type;

        private ShareboxSubActionType(int type) {
            this.type = type;
        }

        public int getType() {
            return this.type;
        }

        public static ShareboxSubActionType fromValue(int type) {
            for (ShareboxSubActionType listType : values()) {
                if (listType.getType() == type) {
                    return listType;
                }
            }
            return NONE;
        }
    }

    @Override
    protected void readBundleArguments(Bundle args) {
        super.readBundleArguments(args);
        action = ShareboxActionType.fromValue(args.getInt(PARAM_SHAREBOX_ACTION,
                ShareboxActionType.CREATE_NEW_POST.getType()));
        subAction = ShareboxSubActionType.fromValue(args.getInt(PARAM_SHAREBOX_SUBACTION,
                ShareboxSubActionType.NONE.getType()));
        postId = args.getString(PARAM_SHAREBOX_POST_ID);
        prefix = args.getString(PARAM_SHAREBOX_PREFIX);
        presetPhotoUri = args.getParcelable(PARAM_SHAREBOX_PHOTOURI);
        groupId = args.getString(PARAM_SHAREBOX_GROUP_ID);
        allowPostWhenReply = args.getBoolean(PARAM_SHAREBOX_ALLOW_POST_WHEN_REPLY);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_sharebox;
    }

    @Override
    public void onDestroyView() {
        if (shouldSaveDataOnDestroy) {
            saveShareboxState();
        }
        super.onDestroyView();
    }
    
    @Override
    protected void onHideFragment() {
        Tools.hideVirtualKeyboard(getActivity());
        super.onHideFragment();
    }

    @Override
    public void onAttach(Activity activity) {
        setShouldUpdateActionBarOnAttach(false);
        super.onAttach(activity);
    }

    @Override
    protected void onShowFragment() {
        super.onShowFragment();
        
        isTwitterTurnedOff = isFacebookTurnedOff = true;
        
        initDataCache();
        refreshThirdPartySitesSettings();

        if (mIsFromSinglePostFragment) {
            if (action == ShareboxActionType.REPLY_POST) {
                mAttachGalleryButton.setVisibility(View.VISIBLE);
                mAttchEmoticonButton.setVisibility(View.VISIBLE);
                mEmoticonButton.setVisibility(View.GONE);
            } else if (action == ShareboxActionType.REPOST) {
                mEmoticonButton.setVisibility(View.GONE);
                mAttchEmoticonButton.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSharebox = (RelativeLayoutEx) view.findViewById(R.id.sharebox);

        if(subAction != null && subAction == ShareboxSubActionType.OPEN_CAMERA){
            mSharebox.setVisibility(View.GONE);
            ActionHandler.getInstance().takePhoto(this, Constants.REQ_PIC_FROM_CAMERA_FOR_POST, false);
            postDelayShowView();
        } else if(subAction != null && subAction == ShareboxSubActionType.OPEN_GALLERY) {
            mSharebox.setVisibility(View.GONE);
            ActionHandler.getInstance().pickFromGallery(this, Constants.REQ_PIC_FROM_GALLERY_FOR_POST);
            postDelayShowView();
        } else {
            mSharebox.setVisibility(View.VISIBLE);
        }

        mScrollView = (ScrollViewEx) view.findViewById(R.id.scrollview);
        mScrollView.setTouchEventsListener(this);

        if (action == ShareboxActionType.CREATE_LONG_POST) {
            mScrollView.setScrollbarFadingEnabled(false);
        }


        mSharebox.setOnSizeChangedListener(this);

        mShareEditText = (AutoCompleteTextViewEx) view.findViewById(R.id.share_field);
        mShareEditText.setThreshold(0);
        setupAutoCompleteDropDownPostion();

        mCharCountContainer = (TextView) view.findViewById(R.id.char_count_container);
        
        mEmoticonButton = (ImageView) view.findViewById(R.id.emoticon_button);
        mLocationText = (TextView) view.findViewById(R.id.location_text);
        mPostButton = (ImageView) view.findViewById(R.id.post_button);
        mPrivacyButton = (ImageView) view.findViewById(R.id.privacy_button);
        mAdButton = (ImageView) view.findViewById(R.id.ad_button);
                
        mPopupMenuMarker = (ImageView) view.findViewById(R.id.overflow_marker);

        mAttachPhotoButton = (ImageView) view.findViewById(R.id.attach_photo_button);
        mAttachGalleryButton = (ImageView) view.findViewById(R.id.attach_gallery_button);
        mAttchEmoticonButton = (ImageView) view.findViewById(R.id.attach_emoticon_button);
        mPhotoThumbBox = (RelativeLayout) view.findViewById(R.id.thumbnail_box);
        mPhotoRemoveButton = (ImageView) view.findViewById(R.id.close_button);
        mPhotoPreviewAndButton = (ImageView) view.findViewById(R.id.photo_preview_and_button);            

        mTwitterButton = (ImageView) view.findViewById(R.id.twitter_button);
        mFacebookButton = (ImageView) view.findViewById(R.id.facebook_button);
        mEmoticonDrawerGrid = (FrameLayout) view.findViewById(R.id.emoticon_grid);
        
        mMentionsAdapter = new ArrayAdapter<String>(getActivity(), R.layout.holder_auto_complete_text);
        mHotTopicsAdapter = new ArrayAdapter<String>(getActivity(), R.layout.holder_auto_complete_text);

        if (action == ShareboxActionType.CREATE_LONG_POST) {
            textCounterLeft = Constants.MAX_LONG_POST_MESSAGE_LENGTH;
            mPrivacyButton.setVisibility(View.INVISIBLE);
            mAdButton.setVisibility(View.VISIBLE);
        } else {
            textCounterLeft = Constants.MAX_MESSAGE_LENGTH;
            mPrivacyButton.setVisibility(View.VISIBLE);
            mAdButton.setVisibility(View.INVISIBLE);
        }

        mCharCountContainer.setText(String.valueOf(textCounterLeft));

        mEmoticonButton.setOnClickListener(this);


        if (canShowLocationInPost()) {
        	mLocationText.setVisibility(View.VISIBLE);
        	mLocationText.setHint(I18n.tr("Add location"));
        	mLocationText.setOnClickListener(this);
        } else {
        	mLocationText.setVisibility(View.GONE);
        }
        
        mAttachPhotoButton.setOnClickListener(this);
        mAttachGalleryButton.setOnClickListener(this);
        mAttchEmoticonButton.setOnClickListener(this);
        mPhotoPreviewAndButton.setOnClickListener(this);
        mPhotoRemoveButton.setOnClickListener(this);
        mPostButton.setOnClickListener(this);
        mSharebox.setKeyboardListener(this);
        mShareEditText.addTextChangedListener(this);

        mTwitterButton.setOnClickListener(this);
        mFacebookButton.setOnClickListener(this);
        mPrivacyButton.setOnClickListener(this);

        Drawable emoticonGridBackground = Theme.getRoundedRectDrawable(ThemeValues.EMOTICON_GRID_BACKGROUND);
        UIUtils.setBackground(mEmoticonDrawerGrid, emoticonGridBackground);

        switch (action) {
            case CREATE_NEW_POST:
                mShareEditText.setHint(I18n.tr("Tell your story!"));
                break;
            case CREATE_LONG_POST:
                mShareEditText.setHint(I18n.tr("What's on your mind?"));
                break;
            case REPLY_POST:
                mShareEditText.setHint(I18n.tr("Leave your comment"));
                mPrivacyButton.setVisibility(View.GONE);
                break;
            case REPOST:
                mShareEditText.setHint(I18n.tr("Repost"));
                mAttachPhotoButton.setVisibility(View.INVISIBLE);
                
                // Get the root post and set it as data on the SimplePostPreviewHolder.
                Post post = PostsDatastore.getInstance().getPost(postId, false);
                if (post != null) {
                	if (post.getRootPost() != null) {
                		post = post.getRootPost();
                	}
                	
            		final ViewGroup originalPostPreviewContainer = 
                    		(ViewGroup) view.findViewById(R.id.original_post_preview_container);
                    originalPostPreviewContainer.setVisibility(View.VISIBLE);
                    SimplePostPreviewHolder postPreviewHolder = 
                    		new SimplePostPreviewHolder(originalPostPreviewContainer);
                    
                    postPreviewHolder.setData(post);
                }
                
                break;
            case CREATE_NEW_POST_IN_GROUP:
                mShareEditText.setHint(String.format(
                        I18n.tr("To share this on the group page, include <%s> in your post."),
                        groupId));
                break;
            case REPLY_POST_IN_GROUP:
                mShareEditText.setHint(I18n.tr("Any thoughts?"));
                mAttachPhotoButton.setVisibility(View.INVISIBLE);
                break;
            case SHARE_TO_POST:
                mShareEditText.setHint(I18n.tr("Any thoughts?"));
                mAttachPhotoButton.setVisibility(View.INVISIBLE);
                mimeContentContainer = (LinearLayout) view.findViewById(R.id.mime_content_container);
                // add preview of mime data
                applyMimeDataPreviewView();
                break;
        }

        if (prefix != null) {
            mShareEditText.append(prefix);
        }
        
        if (presetPhotoUri != null) {
            try {
                final Bitmap photo = Tools.resizeAndRotateImage(getActivity(), presetPhotoUri, 
                		Constants.DEFAULT_PHOTO_SIZE, Constants.DEFAULT_PHOTO_SIZE);
                onPhotoSendPhoto(photo);
            } catch (IOException e) {
                Logger.debug.log(LOG_TAG, e);
            }
        }

        initAttachmentDrawers();
        setPostButtonState();
        
        // Don't load the sharebox state if there is a preset content to be put into the share box.
        if (prefix == null && presetPhotoUri == null) {
            loadShareboxState();
        }
        
        createPrivacyOptionsPopupMenu();
        resetPrivacyDisplay();

        getKeyboardBackOnShareBox();

        if (subAction != null && subAction == ShareboxSubActionType.OPEN_EMOTICON_DRAWER) {
            mEmoticonButton.performClick();
        }
    }

    private void postDelayShowView(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mSharebox.setVisibility(View.VISIBLE);
            }
        }, 2000);
    }

    private void checkUrlPattern() {
        String text = mShareEditText.getText() != null ? mShareEditText.getText().toString() : Constants.BLANKSTR;
        InputString inputString;

        if (action == ShareboxActionType.CREATE_LONG_POST) {
            inputString = Tools.checkInputField(text, true, Constants.MAX_LONG_POST_MESSAGE_LENGTH,
                    Constants.MAX_LINK_LENGTH, true);
        } else {
            inputString = Tools.checkInputField(text, true, Constants.MAX_MESSAGE_LENGTH,
                    Constants.MAX_LINK_LENGTH, true);
        }

        textCounterLeft = inputString.counterLeft;

        if (mCharCountContainer != null) {
            mCharCountContainer.setText(String.valueOf(textCounterLeft));
        }

        setPostButtonState();
    }

    private void setPostButtonState() {
    	final boolean shouldEnable = ((hasContentChanges() || action == ShareboxActionType.REPOST ||
                action == ShareboxActionType.SHARE_TO_POST) && textCounterLeft >= 0);
    	mPostButton.setEnabled(shouldEnable);
        mPostButton.setSelected(shouldEnable);
    }

    private final void initDataCache() {
        ThirdPartySitesController.getInstance().getThirdPartySitesStatus();
        ThirdPartySitesController.getInstance().getThirdPartySitesLinked();
        PostsDatastore.getInstance().getHotTopics(HOT_TOPICS_LIMIT);
    }
    
    private void initAttachmentDrawers() {
        AttachmentPagerFragment mEmoticonGrid = new AttachmentPagerFragment();
        mEmoticonGrid.setAttachmentListener(this);

        Bundle args = new Bundle();

        args.putInt(AttachmentPagerFragment.PARAM_ATTACHMENT_TYPE, AttachmentType.EMOTICON.value);
        args.putInt(AttachmentPagerFragment.PARAM_PACK_ID, AttachmentType.EMOTICON.value);
        mEmoticonGrid.setArguments(args);

        addChildFragment(R.id.emoticon_grid, mEmoticonGrid);
    }
    
    private void showEmoticonDrawerSmooth() {
        Tools.hideVirtualKeyboard(getActivity(), mShareEditText);
        showEmoticonsOnKeyboardHidden = true;
    }

    private void showEmoticonDrawer() {
        Tools.hideVirtualKeyboard(getActivity(), mShareEditText);
        //reset height based on the soft keyboard
        LayoutParams params = mEmoticonDrawerGrid.getLayoutParams();
        params.height = Config.getInstance().getSoftKeyboardHeight() - ApplicationEx.getDimension(R.dimen.share_box_margin);
        
        // Ensure that the emoticon drawer is at least two-fifths of the screen height.
        final int minHeightForEmotionDrawer = (int) (Config.getInstance().getScreenHeight() * 0.4f); 
        if (params.height < minHeightForEmotionDrawer) {
        	params.height = minHeightForEmotionDrawer;
        }
        
        mEmoticonDrawerGrid.setVisibility(View.VISIBLE);
        isEmoticonDrawerDisplayed = true;
        mEmoticonButton.setSelected(true);
    }

    private void hideEmoticonDrawer() {
        if (isEmoticonDrawerDisplayed) {
            mEmoticonDrawerGrid.setVisibility(View.GONE);
            isEmoticonDrawerDisplayed = false;
            mEmoticonButton.setSelected(false);
        }
    }

    private void showMentionList() {
        refreshMentions();
        mShareEditText.setAdapter(mMentionsAdapter);
    }

    private boolean isMentionListShown() {
        return mShareEditText.isPopupShowing() && (mShareEditText.getAdapter() == mMentionsAdapter);
    }

    private void showHotTopicsList() {
        refreshHotTopics();
        mShareEditText.setAdapter(mHotTopicsAdapter);
    }

    private boolean isHotTopicsListShown() {
        return mShareEditText.isPopupShowing() && (mShareEditText.getAdapter() == mHotTopicsAdapter);
    }

    private void refreshMentions() {
        ArrayList<String> usernameList =
                SystemDatastore.getInstance().getMentions(Session.getInstance().getUserId());

        if (usernameList != null && usernameList.size() > 0) {
            mMentionsAdapter.clear();

            for (String username : usernameList) {
            	username = Constants.MENTIONS_TAG + username;
            	mMentionsAdapter.add(username);
            }
        }
    }

    private void refreshHotTopics() {
        HotTopicsResult result = PostsDatastore.getInstance().getHotTopics(HOT_TOPICS_LIMIT);

        if (result != null) {
            List<HotTopic> hotTopics = Arrays.asList(result.getResult());
            mHotTopicsAdapter.clear();

            for (HotTopic topic : hotTopics) {
            	String topicName = topic.getName();
            	if (!topicName.startsWith(Constants.HASH_TAG)) {
            		topicName = Constants.HASH_TAG + topicName;
            	}
                mHotTopicsAdapter.add(topicName);
            }
        }
    }

    private void refreshThirdPartySitesSettings() {
        if (shouldShowSocialButtons()) {
            ThirdPartySites thirdPartySitesStatus = ThirdPartySitesController.getInstance().getThirdPartySitesStatus();
            ThirdPartySites thirdPartySitesLinked = ThirdPartySitesController.getInstance().getThirdPartySitesLinked();

            if (thirdPartySitesStatus != null) {
                if (thirdPartySitesStatus.getFacebookStatus()) {
                    mFacebookButton.setVisibility(View.VISIBLE);
                    if (thirdPartySitesLinked != null) {
                        // Only turn on facebook if it was not explicitly turned off by the user.
                        if (!isFacebookTurnedOff) {
                            setFacebook(thirdPartySitesLinked.getFacebookStatus());
                        }
                    } else {
                        setFacebook(false);
                    }
                } else {
                    mFacebookButton.setVisibility(View.GONE);
                }

                if (thirdPartySitesStatus.getTwitterStatus()) {
                    mTwitterButton.setVisibility(View.VISIBLE);
                    if (thirdPartySitesLinked != null) {
                        // Only turn on facebook if it was not explicitly turned off by the user.
                        if (!isTwitterTurnedOff) {
                            setTwitter(thirdPartySitesLinked.getTwitterStatus());
                        }
                    } else {
                        setTwitter(false);
                    }
                } else {
                    mTwitterButton.setVisibility(View.GONE);
                }
            }
        }
    }

    private boolean shouldShowSocialButtons() {
        return (action == ShareboxActionType.CREATE_NEW_POST || action == ShareboxActionType.CREATE_LONG_POST || action == ShareboxActionType.REPOST);
    }
    
    private boolean canShowLocationInPost() {
        return (Config.getInstance().isLocationInPostEnabled() && (action == ShareboxActionType.CREATE_NEW_POST || action == ShareboxActionType.CREATE_LONG_POST));
    }

    private void setFacebook(boolean enable) {
        mFacebookButton.setTag(enable);
        mFacebookButton.setImageResource((enable) ? R.drawable.ad_facebook_blue : R.drawable.ad_facebook_grey); 
    }

    private void setTwitter(boolean enable) {
        mTwitterButton.setTag(enable);
        mTwitterButton.setImageResource((enable) ? R.drawable.ad_twitter_blue : R.drawable.ad_twitter_grey);
    }

    private void handlePost(String body) {

        boolean postToTwitter = false;
        boolean postToFacebook = false;
        
        final com.projectgoth.b.data.Location location = LocationUtils.makeLocationFromLocationListItem(selectedLocationListItem);

        if (mTwitterButton.getTag() != null) {
            postToTwitter = (Boolean) mTwitterButton.getTag();
        }

        if (mFacebookButton.getTag() != null) {
            postToFacebook = (Boolean) mFacebookButton.getTag();
        }

        if (action == ShareboxActionType.CREATE_NEW_POST || action == ShareboxActionType.CREATE_LONG_POST) {
            if (mPhoto != null) {
                //no need to resize again here, it's already resized when it's loaded previously, which is good for memory usage
                FormData data = PostUtils.preparePhotoFormData(ImageFileType.JPG, Tools.getBitmapDataForUpload(mPhoto, false));
                
                PostsDatastore.getInstance().createNewPost(body, data, postToFacebook, postToTwitter, replyPermission,
                        postPrivacy, location, null);
            } else {
                
                PostsDatastore.getInstance().createNewPost(body, null, postToFacebook, postToTwitter, replyPermission,
                        postPrivacy, location, null);
            }
        } else if (action == ShareboxActionType.SHARE_TO_POST) {
            //plain text for clients that cannot handle the mime data
            String text = body + Constants.SPACESTR + ShareManager.mCurrentShareUrl;
            if (TextUtils.isEmpty(body)) {
                text = ShareManager.mCurrentShareUrl;
            }

            ArrayList<MimeData> dataList = new ArrayList<MimeData>(MimeTypeDataModel.parse(
                    ShareManager.mCurrentShareMimeType, ShareManager.mCurrentShareMimeData));
            //add the text
            if (!TextUtils.isEmpty(body)) {
                String[] hotkeys = ChatUtils.getHotkeysInString(body);
                TextRichMimeData textRichMimeData = TextRichMimeData.createFromText(body, null, hotkeys);
                dataList.add(0, textRichMimeData);
            }
            //convert the list to json string
            String mimeJsonStr = MimeUtils.convertMimeDataToJson(dataList);

            PostsDatastore.getInstance().createNewPost(text, null, false, false, replyPermission, postPrivacy, location, mimeJsonStr);

            ShareManager.clearShareData();

        } else if (action == ShareboxActionType.CREATE_NEW_POST_IN_GROUP) {
            
            if (mPhoto != null) {
                //no need to resize again here, it's already resized when it's loaded previously, which is good for memory usage
                FormData data = PostUtils.preparePhotoFormData(ImageFileType.JPG, Tools.getBitmapDataForUpload(mPhoto, false));
                PostsDatastore.getInstance().createNewPost(body, data, false, false, replyPermission, postPrivacy, location, null);
            } else {
                PostsDatastore.getInstance().createNewPost(body, null, false, false, replyPermission, postPrivacy, location, null);
            }
        } else {
            Post post = PostsDatastore.getInstance().getPost(postId, false);

            String parentId = null;
            String rootId = null;
            PostOriginalityEnum originality = PostOriginalityEnum.ORIGINAL;

            Boolean showInFeeds = false;

            if (post != null) {
                originality = post.getOriginality();
                if (originality == PostOriginalityEnum.ORIGINAL) {
                    parentId = post.getId();
                    rootId = post.getId();
                    // replyToRoot = false;
                } else if (originality == PostOriginalityEnum.REPLY || originality == PostOriginalityEnum.RESHARE) {
                    parentId = post.getId();
                    if (post.getRootPost() != null) {
                        rootId = post.getRootPost().getId();
                    }
                }
            }

            if (action == ShareboxActionType.REPLY_POST || action == ShareboxActionType.REPLY_POST_IN_GROUP) {
                if (mPhoto != null) {
                    //no need to resize again here, it's already resized when it's loaded previously, which is good for memory usage
                    FormData data = PostUtils.preparePhotoFormData(ImageFileType.JPG, Tools.getBitmapDataForUpload(mPhoto, false));
                    PostsDatastore.getInstance().replyPost(body, data, parentId, rootId, true, showInFeeds,
                            replyPermission, postPrivacy, location);
                } else {
                    PostsDatastore.getInstance().replyPost(body, null, parentId, rootId, true, showInFeeds,
                            replyPermission, postPrivacy, location);
                }

            } else if (action == ShareboxActionType.REPOST) {
                PostsDatastore.getInstance().resharePost(body, parentId, rootId, postToFacebook, postToTwitter,
                        replyPermission, postPrivacy, location);
            }

        }

        //if post draft created, we don't save the data not sent
        shouldSaveDataOnDestroy = !isActionCreatingPostDraft();

        if (getShowsDialog()) {
            dismiss();
        } else {
            closeFragment();
        }

    }

    private boolean isActionCreatingPostDraft() {
        if(action == ShareboxActionType.CREATE_NEW_POST || action == ShareboxActionType.CREATE_LONG_POST ||
                action == ShareboxActionType.SHARE_TO_POST || action == ShareboxActionType.CREATE_NEW_POST_IN_GROUP) {
            return true;
        } else {
            return false;
        }

    }

    @Override
    protected void registerReceivers() {
        registerEvent(Events.Profile.FETCH_MENTION_AUTOCOMPLETE_COMPLETED);
        registerEvent(Events.Application.FETCH_THIRD_PARTY_SETTINGS_COMPLETED);
        registerEvent(Events.HotTopic.FETCH_ALL_COMPLETED);
        if (action == ShareboxActionType.SHARE_TO_POST) {
            registerDataFetchedByMimeDataEvents();
        }
    }

    @Override
    protected void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(Events.Profile.FETCH_MENTION_AUTOCOMPLETE_COMPLETED)) {
            if (isMentionListShown()) {
                showMentionList();
            }
        } else if (action.equals(Events.Application.FETCH_THIRD_PARTY_SETTINGS_COMPLETED)) {
            refreshThirdPartySitesSettings();
        } else if (action.equals(Events.HotTopic.FETCH_ALL_COMPLETED)) {
            if (isHotTopicsListShown()) {
                showHotTopicsList();
            }
        } else if(isCompleteDataForMimeDataFetched(action)) {
            applyMimeDataPreviewView();
        }
    }

    public void setIsFromSinglePostFragment(boolean isFromSinglePostFragment) {
        mIsFromSinglePostFragment = isFromSinglePostFragment;
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();

        switch (viewId) {
            case R.id.attach_emoticon_button:
            case R.id.emoticon_button:
                if (isEmoticonDrawerDisplayed) {
                    mEmoticonButton.setSelected(false);
                    hideEmoticonDrawer();
                } else {
                    GAEvent.Miniblog_CreatePostEmoticon.send();
                    mEmoticonButton.setSelected(true);
                    
                    if (isKeyboardDisplayed) {
                        showEmoticonDrawerSmooth();
                    } else {
                        showEmoticonDrawer();
                    }
                }
                break;
            case R.id.location_text:
                GAEvent.Miniblog_CreatePostAddLocation.send();
                onLocationButtonClicked();
                break;
            case R.id.attach_photo_button:
                if (mIsFromSinglePostFragment) {
                    subAction = ShareboxSubActionType.NONE;
                    ActionHandler.getInstance().takePhoto(this, Constants.REQ_PIC_FROM_CAMERA_FOR_POST, false);
                } else {
                    GAEvent.Miniblog_CreatePostAddImage.send();
                    onAttachPhotoButtonClicked();
                }
            	break;
            case R.id.attach_gallery_button:
                ActionHandler.getInstance().pickFromGallery(this, Constants.REQ_PIC_FROM_GALLERY_FOR_POST);
                break;
            case R.id.photo_preview_and_button:
                if (mPhotoRemoveButton.getVisibility() == View.INVISIBLE) {
                    ArrayList<ContextMenuItem> menuItemList = getContextMenuOptions();
                    Tools.showContextMenu(I18n.tr("Add photo"), menuItemList, this);
                } else {
                    ActionHandler.getInstance().displayPhotoViewerFragmentForSharebox(getActivity(), ((BitmapDrawable) mPhotoPreviewAndButton.getDrawable()).getBitmap());
                }
                break;
            case R.id.post_button:
                String body = mShareEditText.getText().toString();

                if (hasContentChanges() || action == ShareboxActionType.REPOST ||
                        action == ShareboxActionType.SHARE_TO_POST) {
                    GAEvent.Miniblog_CreatePost.send();
                    Tools.showToast(getActivity(), I18n.tr("Sending"));
                    handlePost(body);
                } else {

                    String actionMessage = I18n.tr("Type something to share.");

                    if (action == ShareboxActionType.CREATE_NEW_POST) {
                        actionMessage = I18n.tr("Type something or add a photo.");
                    }
                    Toast.makeText(getActivity(), actionMessage, Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.close_button:
                showRemovePhotoDialog();
                break;
            case R.id.privacy_button:
            showPrivacyOptionsPopupMenu();
            break;
            case R.id.twitter_button:
            {
                GAEvent.Miniblog_CreatePostShareTwitter.send();

                ThirdPartySites linkedSites = ThirdPartySitesController.getInstance().getThirdPartySitesLinked();
                if (linkedSites != null) {
                    if (linkedSites.getTwitterStatus()) {
                        if (mTwitterButton.getTag() != null) {
                            isTwitterTurnedOff = (Boolean) mTwitterButton.getTag();
                            setTwitter(!isTwitterTurnedOff);
                        }
                    } else {
                        ActionHandler.getInstance().displayBrowser(getActivity(), WebURL.URL_THIRD_PARTY_SITES_SETTINGS, 
                                I18n.tr("Connect Apps"), R.drawable.ad_setting_white);
                        if (getShowsDialog()) {
                            dismiss();
                        }
                    }
                }
                break;
            }
            case R.id.facebook_button:
            {
                GAEvent.Miniblog_CreatePostShareFacebook.send();

                ThirdPartySites linkedSites = ThirdPartySitesController.getInstance().getThirdPartySitesLinked();
                if (linkedSites != null) {
                    if (linkedSites.getFacebookStatus()) {
                        if (mFacebookButton.getTag() != null) {
                            isFacebookTurnedOff = (Boolean) mFacebookButton.getTag();
                            setFacebook(!isFacebookTurnedOff);
                        }
                    } else {
                        ActionHandler.getInstance().displayBrowser(getActivity(), WebURL.URL_THIRD_PARTY_SITES_SETTINGS, 
                                I18n.tr("Connect Apps"), R.drawable.ad_setting_white);
                        if (getShowsDialog()) {
                            dismiss();
                        }
                    }
                }
                break;
            }
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // only check for @ or #
        if (count == 1) {
            String typed = s.subSequence(start, start + count).toString();
            if (typed.equals(Constants.MENTIONS_TAG)) {
                GAEvent.Miniblog_CreatePostMention.send();
                showMentionList();
            } else if (typed.equals(Constants.HASH_TAG)) {
                GAEvent.Miniblog_CreatePostHashtag.send();
                showHotTopicsList();
            } else {
                checkUrlPattern();
            }
        } else {
            checkUrlPattern();
        }
    }

    @Override
    public void onAttachmentItemClick(Object data, int attachmentType) {
        BaseEmoticon baseEmoticon = (BaseEmoticon) data;
        if (attachmentType == AttachmentType.EMOTICON.value) {
            mShareEditText.insertEmoticon(baseEmoticon.getMainHotkey());
        }
    }
    
    @Override
    public void onSoftKeyboardShown() {
        isKeyboardDisplayed = true;
        hideEmoticonDrawer();
    }

    @Override
    public void onSoftKeyboardHidden() {
        isKeyboardDisplayed = false;
        
        if (showEmoticonsOnKeyboardHidden) {
            showEmoticonDrawer();
            showEmoticonsOnKeyboardHidden = false;
        }
    }

    public void onPhotoSendPhoto(Bitmap photo) {
        if (photo != null) {
            mPhoto = photo;
            restoreAfterPhotoFetchToggle(photo);
        }
    }

    public void onPhotoSendPhoto(byte[] photo) {
        if (photo != null) {
            mPhoto = BitmapFactory.decodeByteArray(photo, 0, photo.length);
            restoreAfterPhotoFetchToggle(mPhoto);
        }
    }

    public void removeAttachedPhoto() {
        if (mPhoto != null) {
            restoreAfterPhotoFetchToggle(null);
        }
    }


    private void showRemovePhotoDialog() {
        String message = I18n.tr("Sure you want to remove this photo?");
        String title = I18n.tr("Remove photo");
        AlertHandler.getInstance().showCustomConfirmationDialog(getActivity(), title, message, new AlertListener() {

            @Override
            public void onDismiss() {
                // TODO Auto-generated method stub
            }

            @Override
            public void onConfirm() {
                removeAttachedPhoto();

            }
        });
    }

    private void resetPrivacyDisplay() {
        if (postPrivacy == PostPrivacyEnum.AUTHOR_ONLY) {
        	mPrivacyButton.setImageResource(R.drawable.ad_private_grey);
        } else if (postPrivacy == PostPrivacyEnum.FRIENDS) {
        	mPrivacyButton.setImageResource(R.drawable.ad_userppl_grey);
        } else {
        	mPrivacyButton.setImageResource(R.drawable.ad_public_grey);
        }
        
        mPopupMenu.setMarkerOn(replyPermission == ReplyPermissionEnum.EVERYONE);
    }

    public ArrayList<ContextMenuItem> getContextMenuOptions() {
        ArrayList<ContextMenuItem> menuItems = new ArrayList<ContextMenuItem>();
        menuItems.add(new ContextMenuItem(I18n.tr("Camera"), R.id.option_item_camera, null));
        menuItems.add(new ContextMenuItem(I18n.tr("Gallery"), R.id.option_item_gallery, null));
        return menuItems;
    }
    
    private void createPrivacyOptionsPopupMenu() {
        mPopupMenu = new PopupMenu(getActivity());
        mPopupMenu.setMenuOptions(getPrivacyMenuOptions());
        mPopupMenu.setPopupGravity(Gravity.RIGHT | Gravity.BOTTOM);
        mPopupMenu.setPopupMenuListener(this);
        mPopupMenu.setMarker(mPopupMenuMarker);
    }
    
    private List<MenuOption> getPrivacyMenuOptions() {
        List<MenuOption> menuItems = new ArrayList<MenuOption>();
        menuItems.add(new MenuOption(I18n.tr("Public"), 
                R.drawable.ad_public_grey, R.id.action_privacy_public_clicked, MenuOptionType.SELECTABLE,
                (postPrivacy.value() == PostPrivacyEnum.EVERYONE.value()), false));
        menuItems.add(new MenuOption(I18n.tr("Friends"), 
                R.drawable.ad_userppl_grey, R.id.action_privacy_friends_clicked, MenuOptionType.SELECTABLE,
                (postPrivacy.value() == PostPrivacyEnum.FRIENDS.value()), false));
        menuItems.add(new MenuOption(I18n.tr("Private"), 
                R.drawable.ad_private_grey, R.id.action_privacy_private_clicked, MenuOptionType.SELECTABLE,
                (postPrivacy.value() == PostPrivacyEnum.AUTHOR_ONLY.value()), false));
        
        MenuOption allowRepliesOption = new MenuOption(I18n.tr("Allow replies"), R.drawable.ad_reply_white, new MenuAction() {

            @Override
            public void onAction(MenuOption option, boolean isSelected) {
                replyPermission = isSelected ? ReplyPermissionEnum.EVERYONE : ReplyPermissionEnum.NONE;
                resetPrivacyDisplay();
            }
            
        });
        allowRepliesOption.setMenuOptionType(MenuOptionType.CHECKABLE);
        allowRepliesOption.setChecked(replyPermission.value() == ReplyPermissionEnum.EVERYONE.value());
        allowRepliesOption.setDismissPopupOnClick(false);
        menuItems.add(allowRepliesOption);

        return menuItems;
    }
    
    private void showPrivacyOptionsPopupMenu() {        
        mPopupMenu.setPopupAnchor(mPopupMenuMarker);
        mPopupMenu.showAtLocation(0,
                Config.getInstance().getScreenHeight() - mPopupMenu.mAnchorRect.top, true);
    }
    
    @Override
    public void onMenuOptionClicked(MenuOption menuOption) {
        switch(menuOption.getActionId()) {
            case R.id.action_privacy_public_clicked:
                GAEvent.Miniblog_CreatePostPrivacySettings.send("public");
                postPrivacy = PostPrivacyEnum.EVERYONE;
                break;
            case R.id.action_privacy_friends_clicked:
                GAEvent.Miniblog_CreatePostPrivacySettings.send("friends");
                postPrivacy = PostPrivacyEnum.FRIENDS;
                break;
            case R.id.action_privacy_private_clicked:
                GAEvent.Miniblog_CreatePostPrivacySettings.send("private");
                postPrivacy = PostPrivacyEnum.AUTHOR_ONLY;
                break;
        }
        
        resetPrivacyDisplay();
    }
    
    @Override
    public void onPopupMenuDismissed() {
        
    }
    
    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (mPopupMenu != null && mPopupMenu.isShown()) {
            final int diff = (oldh - h);
            if (diff != 0) {
                mPopupMenu.dismiss();
                mPopupMenu.showAtLocation(0, mPopupMenu.getYOffset() + diff, false);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == Constants.REQ_PIC_FROM_CAMERA_FOR_POST) {
            if(resultCode == Activity.RESULT_OK) {
                try {
                    Bitmap photo = Tools.loadImageFromCapturedPhotoFile(getActivity());
                    ShareboxFragment.stateData.setTypeWithPhotoPath(PhotoTypeEnum.CAMERA, Tools.getCapturedPhotoFile(getActivity()));
                    Logger.debug.log(LOG_TAG, "photo from camera width:" + photo.getWidth() + " height:" + photo.getHeight());
                    onPhotoSendPhoto(photo);
                } catch (Exception e) {
                    Logger.error.log(LOG_TAG, e);
                }
            } else if(subAction == ShareboxSubActionType.OPEN_CAMERA) {
                getDialog().onBackPressed();
            }
        } else if (requestCode == Constants.REQ_PIC_FROM_GALLERY_FOR_POST) {
            if (intent != null && resultCode == Activity.RESULT_OK) {
                Uri selectedImage = intent.getData();
                try {
                    Bitmap resizedBitmap = Tools.resizeAndRotateImage(getActivity(), selectedImage,
                            Constants.DEFAULT_PHOTO_SIZE, Constants.DEFAULT_PHOTO_SIZE);
                    ShareboxFragment.stateData.setTypeWithPhotoPath(PhotoTypeEnum.GALLERY, selectedImage.toString());
                    onPhotoSendPhoto(resizedBitmap);
                } catch (Exception e) {
                    Logger.error.log(LOG_TAG, e);
                }
            } else if (intent == null || resultCode == Activity.RESULT_CANCELED) {
                //dimiss dialog if not select any photo
                getDialog().onBackPressed();
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.projectgoth.ui.listener.ContextMenuItemListener#onContextMenuItemClick
     * (com.projectgoth.model.ContextMenuItem)
     */
    @Override
    public void onContextMenuItemClick(ContextMenuItem menuItem) {
        int id = menuItem.getId();
        switch (id) {
            case R.id.option_item_camera:
                subAction = ShareboxSubActionType.NONE;
                ActionHandler.getInstance().takePhoto(this, Constants.REQ_PIC_FROM_CAMERA_FOR_POST, false);
                break;
            case R.id.option_item_gallery:
                ActionHandler.getInstance().pickFromGallery(this, Constants.REQ_PIC_FROM_GALLERY_FOR_POST);
                break;
        }
    }

    private void restoreAfterPhotoFetchToggle(Bitmap photo) {
        if (photo != null && !photo.isRecycled()) {
            GAEvent.Miniblog_CreatePostImageSuccess.send();
            mPhotoPreviewAndButton.setImageBitmap(photo);
            mPhotoRemoveButton.setOnClickListener(this);
            mPhotoRemoveButton.setVisibility(View.VISIBLE);
            mPhotoThumbBox.setVisibility(View.VISIBLE);
        } else {
        	mPhotoThumbBox.setVisibility(View.GONE);
            mPhotoPreviewAndButton.setImageBitmap(null);
            mPhotoPreviewAndButton.setImageResource(R.drawable.ic_camera_sharebox);
            mPhotoRemoveButton.setVisibility(View.INVISIBLE);
            mPhotoRemoveButton.setOnClickListener(null);
        }
        setPostButtonState();
    }

    private void getKeyboardBackOnShareBox() {

        //if open emoticon panel from small input field of repost, show keyboard after click edit field
        if (subAction != null && subAction == ShareboxSubActionType.OPEN_EMOTICON_DRAWER) {
            mShareEditText.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mShareEditText.setFocusableInTouchMode(true);
                    mShareEditText.requestFocus();
                    Tools.showVirtualKeyboard(getActivity(), mShareEditText);
                }
            });
        } else {
            mShareEditText.setFocusableInTouchMode(true);
            mShareEditText.requestFocus();
            Tools.showVirtualKeyboard(getActivity(), mShareEditText);
        }
    }

    private void loadShareboxState() {
        if (action == ShareboxActionType.CREATE_NEW_POST) {
            mShareEditText.setText(ShareboxFragment.stateData.getBody());
            PhotoTypeEnum photoType = ShareboxFragment.stateData.getPhotoType();
            String photoPath = ShareboxFragment.stateData.getPhotoPath();
            
            if (photoPath != null) {
                Bitmap photo;
                try {
                    if(photoType == PhotoTypeEnum.GALLERY) {
                        photo = Tools.resizeAndRotateImage(getActivity(), Uri.parse(photoPath),
                                Constants.DEFAULT_PHOTO_SIZE, Constants.DEFAULT_PHOTO_SIZE);
                    } else {
                        photo = Tools.loadImageFromCapturedPhotoFile(this.getActivity(), photoPath);
                    }
                    onPhotoSendPhoto(photo);
                    
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        
        setTwitter(ShareboxFragment.stateData.isPostToTwitter());
        setFacebook(ShareboxFragment.stateData.isPostToFacebook());
        replyPermission = ShareboxFragment.stateData.getReplyPermission();
        postPrivacy = ShareboxFragment.stateData.getPostPrivacy();
        
        
    }
    
    private void saveShareboxState() {
        
        if (action == ShareboxActionType.CREATE_NEW_POST) {
            ShareboxFragment.stateData.setBody(mShareEditText.getText().toString());
        }
        
        if (mPhotoRemoveButton.getVisibility() == View.INVISIBLE) {
            ShareboxFragment.stateData.setTypeWithPhotoPath(null, null);
        }
        
        if (mTwitterButton.getTag() != null) {
            ShareboxFragment.stateData.setPostToTwitter((Boolean) mTwitterButton.getTag());
        } else {
            ShareboxFragment.stateData.setPostToTwitter(false);
        }

        if (mFacebookButton.getTag() != null) {
            ShareboxFragment.stateData.setPostToFacebook((Boolean) mFacebookButton.getTag());
        } else {
            ShareboxFragment.stateData.setPostToFacebook(false);
        }
        
        ShareboxFragment.stateData.setReplyPermission(replyPermission);
        ShareboxFragment.stateData.setPostPrivacy(postPrivacy);
    }
    
    /**
     * @return true if this Sharebox has any user input content (text / photo) and false if not.
     */
    private boolean hasContentChanges() {
        final String shareText = mShareEditText.getText().toString().trim();
        return (!TextUtils.isEmpty(shareText) || mPhotoRemoveButton.getVisibility() == View.VISIBLE);
    }
    
    /**
     * Clears the content portion of the state of this Sharebox. The settings (twitter / facebook / privacy) are preserved.
     */
    public static void clearContentState() {
        ShareboxFragment.stateData.clearContent();
    }

    private void onLocationButtonClicked() {

        final LocationListFragment.EventListener self = this;
        AnimUtils.doClickScaleAnimation(mLocationText, new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                ActionHandler.getInstance().displayLocationList(getActivity(), self, selectedLocationListItem);
            }
        });
    }    
    
    private void onAttachPhotoButtonClicked() {
    	Tools.showContextMenu(I18n.tr("Add photo"), getContextMenuOptions(), this);
    }
    
    private void setLocationText(final String formattedAddress) {
        if (TextUtils.isEmpty(formattedAddress)) {
        	mLocationText.setSelected(false);
        	mLocationText.setText(Constants.BLANKSTR);
        } else {
        	mLocationText.setSelected(true);
        	mLocationText.setText(formattedAddress);
        }
    }
    
    /**
     * @see com.projectgoth.ui.fragment.LocationListFragment.EventListener#onLocationListItemSelected(com.projectgoth.model.LocationListItem)
     */
    @Override
    public void onLocationListItemSelected(LocationListItem item) {
        if (item != null) {
            selectedLocationListItem = item;            
            setLocationText(item.getFormattedLocation());
        }
    }
    
    /**
     * @see com.projectgoth.ui.fragment.LocationListFragment.EventListener#onLocationListItemRemoved()
     */
    @Override
    public void onLocationListItemRemoved() {
        selectedLocationListItem = null;
        setLocationText(null);
    }

    /* (non-Javadoc)
     * @see com.projectgoth.ui.widget.ScrollViewEx.TouchEventsListener#onInterceptTouchEvent(android.view.MotionEvent)
     */
    @Override
    public void onInterceptTouchEvent(MotionEvent event) {
        getKeyboardBackOnShareBox();
    }

    void setupAutoCompleteDropDownPostion() {
        //make the auto complete drop down center vertically
        int offset = ApplicationEx.getDimension(R.dimen.medium_margin);
        int shareboxMargin = ApplicationEx.getDimension(R.dimen.share_box_margin);
        int screenWidth = Config.getInstance().getScreenWidth();
        int width = screenWidth - shareboxMargin * 2 - offset * 2;

        mShareEditText.setDropDownHorizontalOffset(offset);
        mShareEditText.setDropDownWidth(width);
    }

    private void applyMimeDataPreviewView() {
        if (mimeContentContainer == null) {
            return;
        }
        final List<MimeData> mimeDataList = MimeTypeDataModel.parse(ShareManager.mCurrentShareMimeType, ShareManager.mCurrentShareMimeData);
        for (final MimeData mimeData : mimeDataList) {
            final ContentViewHolder<? extends MimeData, ? extends View> contentViewHolder = applyMimeDataToHolder(mimeData);
            if (contentViewHolder != null) {
                final View contentView = contentViewHolder.getContentView();
                mimeContentContainer.removeAllViews();
                mimeContentContainer.addView(contentView);
                mimeContentContainer.setVisibility(View.VISIBLE);
            }
        }
    }

    protected ContentViewHolder<? extends MimeData, ? extends View> applyMimeDataToHolder(final MimeData mimeData) {
        final ContentViewHolder<? extends MimeData, ? extends View> contentViewHolder =
                ContentViewFactory.createContentViewHolder(ApplicationEx.getContext(), mimeData);
        
        setParametersForContentViewHolder(contentViewHolder);
        
        if (contentViewHolder.applyMimeData()) {
            return contentViewHolder;
        } else {
            Logger.error.log(LOG_TAG,
                    "Failed to apply mimeData of type: ", mimeData.getClass(),
                    " to content view holder of type: ", contentViewHolder.getClass());
        }
        return contentViewHolder;
    }
    
    private void setParametersForContentViewHolder(final ContentViewHolder<? extends MimeData, ? extends View> contentViewHolder) {
        contentViewHolder.setParameter(ContentViewHolder.Parameter.SHOW_INLINE_PLAY_BUTTONS, false);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        String content = mShareEditText.getText().toString();
        if (mOnShareBoxDimissListener != null && !TextUtils.isEmpty(content)) {
            mOnShareBoxDimissListener.onDismiss(content);
        }
        super.onDismiss(dialog);
    }
}

