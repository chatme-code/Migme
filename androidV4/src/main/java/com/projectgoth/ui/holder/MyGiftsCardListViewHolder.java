package com.projectgoth.ui.holder;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.Profile;
import com.projectgoth.b.data.StoreItem;
import com.projectgoth.b.data.mime.GiftMimeData;
import com.projectgoth.common.Config;
import com.projectgoth.common.TextUtils;
import com.projectgoth.common.Tools;
import com.projectgoth.controller.EmoticonsController;
import com.projectgoth.controller.StoreController;
import com.projectgoth.datastore.GiftsDatastore;
import com.projectgoth.datastore.UserDatastore;
import com.projectgoth.i18n.I18n;
import com.projectgoth.imagefetcher.ImageHandler;
import com.projectgoth.imagefetcher.UIUtils;
import com.projectgoth.ui.widget.UsernameWithLabelsView;

/**
 * Created by lopenny on 1/23/15.
 */
public class MyGiftsCardListViewHolder extends BaseViewHolder<GiftMimeData> {

    private TextView mUserName;
    private TextView mSentInfo;
    private TextView mSentDate;
    private TextView mSentMessage;

    private ImageView mProfileImage;
    private ImageView mGiftImage;
    private ImageView mGiftLabel;
    private ImageView mFavoriteIcon;
    private ImageView mShareIcon;
    private ImageView mGiftBackIcon;

    private LinearLayout mContainer;
    private LinearLayout mMessageContainer;
    private LinearLayout mActionButtonsContainer;
    private UsernameWithLabelsView mLabelsView;

    private boolean mShowActionButtons;

    public MyGiftsCardListViewHolder(View view) {
        super(view);

        mContainer = (LinearLayout) view.findViewById(R.id.container);
        mMessageContainer = (LinearLayout) view.findViewById(R.id.message_container);
        mUserName = (TextView) view.findViewById(R.id.username);
        mSentInfo = (TextView) view.findViewById(R.id.sent_info);
        mSentDate = (TextView) view.findViewById(R.id.sent_date);
        mSentMessage = (TextView) view.findViewById(R.id.sent_message);

        mLabelsView = (UsernameWithLabelsView) view.findViewById(R.id.labels);
        mProfileImage = (ImageView) view.findViewById(R.id.profile_image);
        mGiftImage = (ImageView) view.findViewById(R.id.gift_image);
        mGiftLabel = (ImageView) view.findViewById(R.id.gift_label);

        mActionButtonsContainer = (LinearLayout) view.findViewById(R.id.icon_container);
        mFavoriteIcon = (ImageView) view.findViewById(R.id.favorite_icon);
        mShareIcon = (ImageView) view.findViewById(R.id.share_icon);
        mGiftBackIcon = (ImageView) view.findViewById(R.id.giftback_icon);

        mFavoriteIcon.setOnClickListener(this);
        mShareIcon.setOnClickListener(this);
        mGiftBackIcon.setOnClickListener(this);
    }

    @Override
    public void setData(GiftMimeData data) {
        super.setData(data);

        mActionButtonsContainer.setVisibility(View.GONE);
        mUserName.setText(data.getSender());
        mSentInfo.setText(String.format(I18n.tr("sent %s"), data.getName()));

        if (!TextUtils.isEmpty(data.getMessage())) {
            mMessageContainer.setVisibility(View.VISIBLE);
            mSentMessage.setText(data.getMessage());
        } else {
            mMessageContainer.setVisibility(View.GONE);
        }

        mSentDate.setText(Tools.getTimeAgoDate(data.getReceivedTimeStamp()));

        mLabelsView.setVisibility(View.GONE);
        Profile profile = UserDatastore.getInstance().getProfileWithUsername(data.getSender(), false);
        if (profile != null) {
            mLabelsView.setLabels(profile.getLabels());
            mLabelsView.setVisibility(View.VISIBLE);
        }

        mContainer.setBackgroundColor(ApplicationEx.getColor(R.color.white));
        mGiftLabel.setVisibility(View.GONE);
        StoreItem gift = StoreController.getInstance().getStoreItem(String.valueOf(data.getStoreItemId()));
        if (gift != null) {
            if (gift.isPremium()) {
                mGiftLabel.setImageResource(R.drawable.ad_store_pgift);
                mGiftLabel.setVisibility(View.VISIBLE);
            } else if (gift.isGroupOnly()) {
                mGiftLabel.setImageResource(R.drawable.ad_store_ggroup);
                mGiftLabel.setVisibility(View.VISIBLE);
            }
        }

        if (mShowActionButtons) {
            mActionButtonsContainer.setVisibility(View.VISIBLE);
        } else {
            mActionButtonsContainer.setVisibility(View.GONE);
        }

        if (GiftsDatastore.getInstance().isFavoriteGift(data)) {
            mFavoriteIcon.setImageResource(R.drawable.ad_favourite_pink);
        } else {
            mFavoriteIcon.setImageResource(R.drawable.ad_favourite_grey);
        }

        if (profile != null) {
            ImageHandler.getInstance().loadDisplayPictureOfUser(mProfileImage, data.getSender(),
                    profile.getDisplayPictureType(), Config.getInstance().getDisplayPicSizeNormal(), true);
        }

        // loading animation start
        if (mGiftImage.getAnimation() == null) {
            Bitmap loadBitmap = UIUtils.getBitmapFromDrawableResource(mGiftImage.getContext(), R.drawable.ad_loadstaticchat_grey);
            mGiftImage.setImageBitmap(loadBitmap);
            ImageHandler.imageRotationAnimationStart(mGiftImage);
        }

        final String hotkey = data.getHotkey();
        mGiftImage.setTag(hotkey);
        if (!TextUtils.isEmpty(hotkey)) {
            int size = ApplicationEx.getDimension(R.dimen.vg_request_size);
            EmoticonsController.getInstance().loadResizedBaseEmoticonImage(null, hotkey, size,
                    R.drawable.ad_loadstaticchat_grey, new ImageHandler.ImageLoadListener() {
                        @Override
                        public void onImageLoaded(Bitmap bitmap) {
                            mGiftImage.clearAnimation();
                            if (mGiftImage.getTag().equals(hotkey)) {
                                mGiftImage.setImageBitmap(bitmap);
                            }
                        }

                        @Override
                        public void onImageFailed(ImageView imageView) {
                            //TODO: handle error case
                        }
                    });
        }
        mGiftImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
    }

    public void setShowActionButtons(boolean showActionButtons) {
        mShowActionButtons = showActionButtons;
    }
}
