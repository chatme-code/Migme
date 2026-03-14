package com.projectgoth.ui.widget;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.Post;
import com.projectgoth.common.Config;
import com.projectgoth.common.Constants;
import com.projectgoth.common.Tools;
import com.projectgoth.datastore.EmoticonDatastore;
import com.projectgoth.datastore.PostsDatastore;
import com.projectgoth.enums.AttachmentType;
import com.projectgoth.enums.UsedChatItemType;
import com.projectgoth.i18n.I18n;
import com.projectgoth.model.UsedChatItem;
import com.projectgoth.nemesis.model.BaseEmoticon;
import com.projectgoth.nemesis.model.BaseEmoticonPack;
import com.projectgoth.nemesis.model.Sticker;
import com.projectgoth.ui.activity.ActionHandler;
import com.projectgoth.ui.activity.BaseCustomFragmentActivity;
import com.projectgoth.ui.activity.CustomPopupActivity;
import com.projectgoth.ui.fragment.AttachmentPagerFragment;
import com.projectgoth.ui.fragment.AttachmentPhotoFragment;
import com.projectgoth.ui.fragment.BaseFragment;
import com.projectgoth.ui.fragment.EmoticonDrawerFragment;
import com.projectgoth.ui.fragment.ShareboxFragment;
import com.projectgoth.ui.fragment.StickerDrawerFragment;
import com.projectgoth.ui.listener.KeyboardListener;
import com.projectgoth.util.PostUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by houdangui on 23/10/14.
 */
public class MessageInputPanel extends BaseFragment implements View.OnClickListener, TextWatcher, KeyboardListener,
        AttachmentPagerFragment.BaseAttachmentFragmentListener, AttachmentPhotoFragment.PhotoEventListener {

    private HeightAdjustableFrameLayout   chatInputContainer;
    private ImageView   mEmoticonButton;
    private ImageView   mReplyEmoticonButton;
    private EditTextEx  mChatField;
    private ImageView   mSendButton;
    private ImageView   mStickerButton;
    private ImageView   mCameraButton;
    private ImageView   mGalleryButton;
    private ImageView   mGiftButton;
    private FrameLayout drawer;
    private TextView    mCharCount;

    private String initialRecipient = Constants.BLANKSTR;
    private List<BaseEmoticonPack> emoticonDrawerTabData;
    private MessageInputPanelListener listener;
    private boolean isKeyboardDisplayed;
    private String mPostId;
    private boolean mIsFromSinglePostFragment = false;

    private static final int       MIN_RECENT_EMOTICONS          = 6;
    private int                    defaultSelectedTab;
    private static final int       MAX_CONTENT_CHAR_LENGTH       = 300;
    private ShareboxFragment.OnShareBoxDimissListener mShareBoxDismissListener;

    public interface MessageInputPanelListener {

        public void onSendMessageButtonClick();

        public void onGiftIconClick();

        public void onStickerSelect(Sticker sticker);

        public void onPhotoClick(byte[] photo);

        public void onEmotionSelectionShown();

        public void onEmotionSelectionHidden();

        public void onStickerSelectionShown();

        public void onStickerSelectionHidden();

        public void onKeyboardShown();

        public void onKeyboardHidden();

    }

    @Override
    protected int getLayoutId() {
        return R.layout.msg_input_panel;
    }

    @Override
    public void onAttach(Activity activity) {
        setShouldUpdateActionBarOnAttach(false);
        super.onAttach(activity);
    }



    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        chatInputContainer = (HeightAdjustableFrameLayout) view.findViewById(R.id.chat_input_container);
        mChatField = (EditTextEx) view.findViewById(R.id.textfield_chat);
        mEmoticonButton = (ImageView) view.findViewById(R.id.icon_emoticon);
        mStickerButton = (ImageView) view.findViewById(R.id.icon_sticker);
        mCameraButton = (ImageView) view.findViewById(R.id.icon_camera);
        mGalleryButton = (ImageView) view.findViewById(R.id.icon_gallery);
        mGiftButton = (ImageView) view.findViewById(R.id.icon_gift);
        drawer = (FrameLayout) view.findViewById(R.id.drawer);
        mSendButton = (ImageView) view.findViewById(R.id.button_send);
        mReplyEmoticonButton = (ImageView) view.findViewById(R.id.icon_reply_emoticon);
        mCharCount = (TextView) view.findViewById(R.id.char_count_container);

        bindOnClickListener(this, R.id.icon_gift,
                R.id.icon_sticker,
                R.id.icon_camera,
                R.id.icon_gallery,
                R.id.button_send,
                R.id.icon_emoticon,
                R.id.icon_reply_emoticon,
                R.id.textfield_chat);

        mChatField.addTextChangedListener(this);

        setSendButtonState();

        setupOnBackPressListener();

        mShareBoxDismissListener = new ShareboxFragment.OnShareBoxDimissListener() {
            @Override
            public void onDismiss(String body) {
                mChatField.setText("");
            }
        };

    }


    protected void bindOnClickListener(View.OnClickListener listener, int... viewIds) {
        for (int id : viewIds) {
            View view = getView().findViewById(id);
            if(view != null) view.setOnClickListener(listener);
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.icon_gift: {
                listener.onGiftIconClick();
                break;
            }
            case R.id.icon_sticker: {
                if(!mStickerButton.isSelected()) {
                    if (mEmoticonButton.isSelected() || mReplyEmoticonButton.isSelected()) {
                        hideEmoticonDrawer();
                    }

                    if (isKeyboardDisplayed) {
                        //just hide the keyboard here and show the sticker in onSoftKeyboardHidden.
                        // in this way to make it smooth and prevent it from jumping
                        mStickerButton.setSelected(true);
                        Tools.hideVirtualKeyboard(getActivity());
                    } else {
                        showStickerDrawer();
                    }
                } else {
                    hideStickerDrawer();
                }
                break;
            }
            case R.id.icon_camera:
            {
                if(!isAllowReply()) {
                    return;
                }

                if (mIsFromSinglePostFragment) {
                    ShareboxFragment fragment = (ShareboxFragment)ActionHandler.getInstance().displaySharebox(getActivity(), ShareboxFragment.ShareboxActionType.REPLY_POST,
                            mPostId, getTextMessage(), null, true, ShareboxFragment.ShareboxSubActionType.OPEN_CAMERA);
                    fragment.setOnShareBoxDismissListener(mShareBoxDismissListener);
                    fragment.setIsFromSinglePostFragment(true);
                } else {
                    setupPhotoEventListener();
                    ActionHandler.getInstance().takePhoto(getActivity(), Constants.REQ_PIC_FROM_CAMERA_FOR_CHAT_MSG, false);
                }
                break;
            }
            case R.id.icon_gallery:
            {
                if(!isAllowReply()) {
                    return;
                }

                if (mIsFromSinglePostFragment) {
                    ShareboxFragment fragment = (ShareboxFragment)ActionHandler.getInstance().displaySharebox(getActivity(), ShareboxFragment.ShareboxActionType.REPLY_POST,
                            mPostId, getTextMessage(), null, true, ShareboxFragment.ShareboxSubActionType.OPEN_GALLERY);
                    fragment.setOnShareBoxDismissListener(mShareBoxDismissListener);
                    fragment.setIsFromSinglePostFragment(true);
                } else {
                    setupPhotoEventListener();
                    ActionHandler.getInstance().pickFromGallery(getActivity(), Constants.REQ_PIC_FROM_GALLERY_FOR_CHAT_MSG);
                }

                break;
            }
            case R.id.icon_emoticon: {
                if (!mEmoticonButton.isSelected()) {

                    if(mStickerButton.isSelected()) {
                        hideStickerDrawer();
                    }

                    if (isKeyboardDisplayed) {
                        //just hide the keyboard here and show the sticker in onSoftKeyboardHidden.
                        // in this way to make it smooth and prevent it from jumping
                        mEmoticonButton.setSelected(true);
                        Tools.hideVirtualKeyboard(getActivity());
                    } else {
                        showEmoticonDrawer();
                    }

                } else {
                    hideEmoticonDrawer();
                }
                break;
            }
            case R.id.button_send: {
                listener.onSendMessageButtonClick();
                break;
            }
            case R.id.icon_reply_emoticon:
                if (!mReplyEmoticonButton.isSelected()) {
                    mChatField.requestFocus();
                    if (isKeyboardDisplayed) {
                        //just hide the keyboard here and show the sticker in onSoftKeyboardHidden.
                        // in this way to make it smooth and prevent it from jumping
                        mEmoticonButton.setSelected(true);
                        Tools.hideVirtualKeyboard(getActivity());
                    } else {
                        showEmoticonDrawer();
                    }
                } else {
                    hideEmoticonDrawer();
                }

                break;

        }

    }

    private boolean isAllowReply() {
        Post post = PostsDatastore.getInstance().getPost(mPostId, false);
        if (PostUtils.isPostLocked(post)) {
            Tools.showToast(getActivity(), I18n.tr("Post locked."));
            return false;
        } else {
            return true;
        }
    }

    public void setListener(MessageInputPanelListener listener) {
        this.listener = listener;
    }

    public String getTextMessage() {
        return  mChatField.getText().toString();
    }

    private void showEmoticonDrawer() {
        mEmoticonButton.setSelected(true);
        mReplyEmoticonButton.setSelected(true);
        Tools.hideVirtualKeyboard(getActivity());

        EmoticonDrawerFragment emoticonDrawerFragment = new EmoticonDrawerFragment();

        int drawerHeight = getDrawerHeight();
        emoticonDrawerFragment.setDrawerHeight(drawerHeight);
        setChatInputDrawerHeight(drawerHeight);

        emoticonDrawerFragment.setDrawerTabData(getEmoticonTabData());
        emoticonDrawerFragment.setAttachmentListener(this);

        // display all emoticons if the number of recently used emoticons is
        // less than minRecentEmoticons
        int size = EmoticonDatastore.getInstance().getRecentlyUsedEmoticons().size();
        if (size < MIN_RECENT_EMOTICONS) {
            defaultSelectedTab = 1;
            emoticonDrawerFragment.setSelectedTab(defaultSelectedTab);
        }

        addChildFragment(R.id.drawer, emoticonDrawerFragment);
        drawer.setVisibility(View.VISIBLE);

        listener.onEmotionSelectionShown();
    }

    public int getDrawerHeight() {
        int defaultDrawerHeight = ApplicationEx.getDimension(R.dimen.attachment_drawer_height);
        int softkeyboardHeight = Config.getInstance().getSoftKeyboardHeight();
        return Math.max(defaultDrawerHeight, softkeyboardHeight);
    }

    private void setChatInputDrawerHeight(int softKeyboardHeight) {
        ViewGroup.LayoutParams params = drawer.getLayoutParams();
        params.height = softKeyboardHeight;
    }

    private List<BaseEmoticonPack> getEmoticonTabData() {
        if (emoticonDrawerTabData == null) {
            emoticonDrawerTabData = new ArrayList<BaseEmoticonPack>();
            BaseEmoticonPack tabData;

            tabData = new BaseEmoticonPack(AttachmentType.RECENT_EMOTICON.value);
            tabData.setIconUrl(Tools.getDrawableUri("chat_input_tab_recent"));
            emoticonDrawerTabData.add(tabData);

            tabData = new BaseEmoticonPack(AttachmentType.EMOTICON.value);
            tabData.setIconUrl(Tools.getDrawableUri("chat_input_tab_emoticon"));
            emoticonDrawerTabData.add(tabData);

            tabData = new BaseEmoticonPack(AttachmentType.STORE_EMOTICON.value);
            tabData.setIconUrl(Tools.getDrawableUri("chat_input_tab_store"));
            emoticonDrawerTabData.add(tabData);
        }

        return emoticonDrawerTabData;
    }

    public void hideEmoticonDrawer() {
        mEmoticonButton.setSelected(false);
        mReplyEmoticonButton.setSelected(false);
        drawer.setVisibility(View.GONE);
        drawer.removeAllViews();

        listener.onEmotionSelectionHidden();
    }

    @Override
    public void onAttachmentItemClick(Object data, int attachmentType) {
        if (attachmentType == AttachmentType.EMOTICON.value || attachmentType == AttachmentType.RECENT_EMOTICON.value) {
            BaseEmoticon baseEmoticon = (BaseEmoticon) data;
            mChatField.insertEmoticon(baseEmoticon.getMainHotkey());
            EmoticonDatastore.getInstance().addEmoticonHotkeyToRecentlyUsedCache(baseEmoticon.getMainHotkey());
        } else {
            BaseEmoticon baseEmoticon = (BaseEmoticon) data;
            listener.onStickerSelect((Sticker) baseEmoticon);

            UsedChatItem usedChatItem = new UsedChatItem(UsedChatItemType.STICKER, baseEmoticon.getMainHotkey());
            EmoticonDatastore.getInstance().addUsedChatItemToUsedCache(usedChatItem);
        }
    }

    private void setupPhotoEventListener() {
        Activity activity = getActivity();
        if (activity instanceof BaseCustomFragmentActivity) {
            BaseCustomFragmentActivity customPopupActivity = (BaseCustomFragmentActivity) activity;
            customPopupActivity.setPhotoEventListener(this);
        }
    }

    @Override
    public void onPhotoSendPhoto(Bitmap photo) {
        onPhotoSendPhoto(Tools.getBitmapDataForUpload(photo, false));
    }

    @Override
    public void onPhotoSendPhoto(byte[] photo) {
        listener.onPhotoClick(photo);
    }

    private void showStickerDrawer() {
        Tools.hideVirtualKeyboard(getActivity());

        mStickerButton.setSelected(true);

        StickerDrawerFragment stickerDrawerFragment = new StickerDrawerFragment();

        int drawerHeight = getDrawerHeight();
        stickerDrawerFragment.setDrawerHeight(drawerHeight);
        setChatInputDrawerHeight(drawerHeight);

        stickerDrawerFragment.setAttachmentListener(this);
        stickerDrawerFragment.setInitialRecipient(initialRecipient);

        addChildFragment(R.id.drawer, stickerDrawerFragment);
        drawer.setVisibility(View.VISIBLE);

        listener.onStickerSelectionShown();

    }

    public void hideStickerDrawer() {
        mStickerButton.setSelected(false);
        drawer.setVisibility(View.GONE);
        drawer.removeAllViews();

        listener.onStickerSelectionHidden();
    }

    public void setSendButtonState() {
        if (!TextUtils.isEmpty(mChatField.getText().toString().trim())) {
            mSendButton.setEnabled(true);
        } else {
            mSendButton.setEnabled(false);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        setSendButtonState();
        if (mIsFromSinglePostFragment) {
            int availableInputCounts = MAX_CONTENT_CHAR_LENGTH - mChatField.getEditableText().length();
            mCharCount.setText(String.valueOf(availableInputCounts));
            mCharCount.setTextColor(getResources().getColor(R.color.text_light_gray));
            if (availableInputCounts < 0) {
                mSendButton.setEnabled(false);
                mCharCount.setTextColor(getResources().getColor(R.color.red));
            }
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }

    public void onSoftKeyboardShown() {
        isKeyboardDisplayed = true;

        // Logger.debug.logWithTrace(LOG_TAG, getClass());

        if (mEmoticonButton.isSelected() || mReplyEmoticonButton.isSelected()) {
            hideEmoticonDrawer();
        }

        if (mStickerButton.isSelected()) {
            hideStickerDrawer();
        }

        listener.onKeyboardShown();
    }

    /**
     * android has no api to detect if soft keyboard shown or hidden, this is a
     * workaround a bug now is if the message list refreshed, for example, a
     * message received, the this method also called. if the keyboard is shown,
     * the isKeyboardDisplayed will be set to false incorrectly, the consequence
     * now is just a minor UI issue though. Don't know how to fix it yet.
     *
     * the workaround is not perfect. This method not only called when soft
     * keyboard is hidden but also chat input drawer is hidden and message list
     * refreshed, so cannot confirm soft keyboard hidden here
     */

    @Override
    public void onSoftKeyboardHidden() {
        isKeyboardDisplayed = false;

        // Logger.debug.logWithTrace(LOG_TAG, getClass());

        if (mEmoticonButton.isSelected()) {
            showEmoticonDrawer();
        } else if(mStickerButton.isSelected()) {
            showStickerDrawer();
        }

        listener.onKeyboardHidden();
    }

    public void enablePhotoSending() {
        mGalleryButton.clearColorFilter();
        mCameraButton.clearColorFilter();

        mGalleryButton.setClickable(true);
        mCameraButton.setClickable(true);
    }

    public void disablePhotoSending() {
        Tools.setTranslucentFilter(mGalleryButton);
        Tools.setTranslucentFilter(mCameraButton);

        mGalleryButton.setClickable(false);
        mCameraButton.setClickable(false);
    }
    
    public void onlyEnableTextSending(){
        Tools.setTranslucentFilter(mGiftButton);
        Tools.setTranslucentFilter(mStickerButton);

        mGiftButton.setClickable(false);
        mStickerButton.setClickable(false);

        disablePhotoSending();
    }

    public void showEnabledSendButtons() {
        mGiftButton.clearColorFilter();
        mStickerButton.clearColorFilter();
        mSendButton.clearColorFilter();

        mGiftButton.setClickable(true);
        mStickerButton.setClickable(true);
        mSendButton.setClickable(true);

        enablePhotoSending();
    }
    
    public void showDisabledSendButtons() {
        Tools.setTranslucentFilter(mGiftButton);
        Tools.setTranslucentFilter(mStickerButton);
        Tools.setTranslucentFilter(mSendButton);

        mGiftButton.setClickable(false);
        mStickerButton.setClickable(false);
        mSendButton.setClickable(false);

        disablePhotoSending();
    }

    public boolean isKeyboardDisplayed() {
        return isKeyboardDisplayed;
    }

    public boolean isStickerDrawerShown() {
        return mStickerButton.isSelected();
    }

    public boolean isEmoticonDrawerShown() {
        return mEmoticonButton.isSelected();
    }

    public EditTextEx getEditText() {
        return mChatField;
    }

    public ImageView getGiftIcon() {
        return this.mGiftButton;
    }

    public HeightAdjustableFrameLayout getChatInputContainer() {
       return chatInputContainer;
    }

    public void setInitialRecipient(String initialRecipient) {
        this.initialRecipient = initialRecipient;
    }

    private void setupOnBackPressListener() {

        Activity activity = getActivity();
        if (activity instanceof CustomPopupActivity) {
            ((CustomPopupActivity)activity).setOnBackPresslistener(new CustomPopupActivity.OnBackPressListener() {
                @Override
                public boolean onBackPress() {
                    if (isEmoticonDrawerShown()) {
                        hideEmoticonDrawer();
                        return true;
                    }

                    if (isStickerDrawerShown()) {
                        hideStickerDrawer();
                        return true;
                    }

                    return false;
                }
            });
        }

    }

    public void setFromSinglePostFragment(boolean fromSinglePostFragment) {
        mIsFromSinglePostFragment = fromSinglePostFragment;
    }

    public void setPostId(String postId) {
        mPostId = postId;
    }


    @Override
    protected void onShowFragment() {
        super.onShowFragment();
        if (mIsFromSinglePostFragment) {
            mReplyEmoticonButton.setVisibility(View.VISIBLE);
            mGiftButton.setVisibility(View.VISIBLE);
            mStickerButton.setVisibility(View.GONE);
            mCharCount.setVisibility(View.VISIBLE);
            mCharCount.setText(String.valueOf(MAX_CONTENT_CHAR_LENGTH));
        } else {
            mEmoticonButton.setVisibility(View.VISIBLE);
        }
    }
}
