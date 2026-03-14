package com.projectgoth.ui.holder;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.Profile;
import com.projectgoth.b.data.mime.GiftMimeData;
import com.projectgoth.common.Config;
import com.projectgoth.controller.EmoticonsController;
import com.projectgoth.datastore.UserDatastore;
import com.projectgoth.imagefetcher.ImageHandler;
import com.projectgoth.imagefetcher.UIUtils;

/**
 * Created by lopenny on 1/23/15.
 */
public class MyGiftsFavoriteListViewHolder extends BaseViewHolder<GiftMimeData> {

    private ImageView mGiftImage;
    private ImageView mProfileImage;

    public MyGiftsFavoriteListViewHolder(View view) {
        super(view);

        mGiftImage = (ImageView) view.findViewById(R.id.gift_image);
        mProfileImage = (ImageView) view.findViewById(R.id.profile_image);
    }

    @Override
    public void setData(GiftMimeData data) {
        super.setData(data);

        Profile profile = UserDatastore.getInstance().getProfileWithUsername(data.getSender(), false);
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
            EmoticonsController.getInstance().loadResizedBaseEmoticonImage(mGiftImage, hotkey, size,
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
}
