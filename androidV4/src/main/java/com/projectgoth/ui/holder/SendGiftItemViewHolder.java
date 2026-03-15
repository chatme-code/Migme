package com.projectgoth.ui.holder;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.StoreItem;
import com.projectgoth.common.Constants;
import com.projectgoth.controller.EmoticonsController;
import com.projectgoth.imagefetcher.ImageHandler;
import com.projectgoth.imagefetcher.UIUtils;

/**
 * Created by houdangui on 22/6/15.
 */
public class SendGiftItemViewHolder extends BaseViewHolder<StoreItem> {

    private final RelativeLayout        mGiftContainer;
    private final ImageView             mGiftImage;
    private final TextView              mGiftName;
    private final TextView              mGiftPrice;
    private boolean                     mIsSelected;

    public SendGiftItemViewHolder(View view) {
        super(view);

        mGiftContainer = (RelativeLayout) view.findViewById(R.id.gift_container);
        mGiftImage = (ImageView) view.findViewById(R.id.gift_image);
        mGiftName = (TextView) view.findViewById(R.id.gift_name);
        mGiftPrice = (TextView) view.findViewById(R.id.gift_price);

        mGiftContainer.setOnClickListener(this);
        mGiftImage.setOnClickListener(this);
        mGiftName.setOnClickListener(this);
        mGiftPrice.setOnClickListener(this);
    }

    @Override
    public void setData(StoreItem data) {
        super.setData(data);
        //gift name
        mGiftName.setText(data.getName());
        mGiftName.setTextColor(ApplicationEx.getInstance().getResources().getColor(R.color.gift_balance_black));

        //gift price
        final float roundedPrice = data.getRoundedPrice();
        mGiftPrice.setText(roundedPrice + Constants.SPACESTR + data.getLocalCurrency());
        mGiftPrice.setTextColor(ApplicationEx.getInstance().getResources().getColor(R.color.gift_balance_black));

        //start loading
        if (mGiftImage.getAnimation() == null) {
            Bitmap loadBitmap = UIUtils.getBitmapFromDrawableResource(mGiftImage.getContext(), R.drawable.ad_loadstaticchat_grey);
            mGiftImage.setImageBitmap(loadBitmap);
            ImageHandler.imageRotationAnimationStart(mGiftImage);
        }

        //load gift image
        final String hotkey = data.getGiftHotkey();
        mGiftImage.setTag(hotkey);
        if (!TextUtils.isEmpty(hotkey)) {
            int size = ApplicationEx.getDimension(R.dimen.vg_request_size);
            EmoticonsController.getInstance().loadResizedBaseEmoticonImage(null, hotkey, size,
                    R.drawable.ad_loadstaticchat_grey, new ImageHandler.ImageLoadListener() {
                        @Override
                        public void onImageLoaded(Bitmap bitmap) {
                            if (mGiftImage != null) {
                                mGiftImage.clearAnimation();
                                if (mGiftImage.getTag().equals(hotkey) && bitmap != null) {
                                    mGiftImage.setImageBitmap(bitmap);
                                }
                            }
                        }

                        @Override
                        public void onImageFailed(ImageView imageView) {
                            //TODO: handle error case
                        }
                    });
        }
    }

    public boolean isSelected() {
        return mIsSelected;
    }

    public void setIsSelected(boolean isSelected) {
        this.mIsSelected = isSelected;
        UIUtils.setBackground(mGiftContainer, getBgDrawable(isSelected));

    }

    public Drawable getBgDrawable(boolean isSelected) {
        Drawable drawable = null;
        if (isSelected) {
            GradientDrawable gradientDrawable = new GradientDrawable();
            gradientDrawable.setColor(ApplicationEx.getColor(R.color.gift_selected_bg));
            gradientDrawable.setStroke(ApplicationEx.getDimension(R.dimen.gift_selected_bg_stroke_w),
                    ApplicationEx.getColor(R.color.gift_selected_bg_stroke_c));
            gradientDrawable.setCornerRadius(ApplicationEx.getDimension(R.dimen.gift_selected_bg_stroke_r));
            drawable = gradientDrawable;
        } else {
            ColorDrawable colorDrawable = new ColorDrawable(Color.TRANSPARENT);
            drawable = colorDrawable;
        }
        return drawable;

    }
}
