/**
 * Copyright (c) 2013 Project Goth
 *
 * GiftContentViewHolder.java
 * Created Dec 5, 2014, 9:57:53 PM
 */

package com.projectgoth.ui.holder.content;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.mime.GiftMimeData;
import com.projectgoth.b.data.mime.GiftMimeData.GiftType;
import com.projectgoth.common.Tools;
import com.projectgoth.controller.EmoticonsController;
import com.projectgoth.i18n.I18n;
import com.projectgoth.imagefetcher.UIUtils;

/**
 * Represents a content view holder for {@link GiftMimeData}.
 * @author angelorohit
 *
 */
public class GiftContentViewHolder extends ContentViewHolder<GiftMimeData, LinearLayout> {

    private LinearLayout   contentViewContainer;
    private LinearLayout   giftShowerImageContainer;
    private ImageView      giftShowerBanner;
    private ImageView      giftImage;
    private TextView       giftMessage;
    private TextView       giftRecipient;
    private TextView       giftSender;

    private boolean        isPinned;

    /**
     * Constructor.
     * @param ctx       The {@link Context} to be used for inflation. 
     * @param mimeData  The {@link GiftMimeData} to be used as data for this holder.
     */
    public GiftContentViewHolder(Context ctx, GiftMimeData mimeData) {
        super(ctx, mimeData);
    }

    @Override
    public int getLayoutId() {
        return R.layout.content_view_gift;
    }

    @Override
    public boolean applyMimeData() {
        if (mimeData != null) {
            final String hotkey = mimeData.getHotkey();
            if (!TextUtils.isEmpty(hotkey)) {
                EmoticonsController.getInstance().loadGiftImageInList(giftImage, hotkey,
                        R.drawable.ad_gallery_grey);
            }
    
            // Since the loading icon is small, set it center inside so that it is not stretched.
            if (isDisplayingLoadingIcon()) {
                giftImage.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                giftImage.setImageResource(R.drawable.ad_gallery_grey);
            } else {
                giftImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
            }
            
            final String giftMsg = mimeData.getMessage();
            if (!TextUtils.isEmpty(giftMsg)) {
                giftMessage.setText(giftMsg);
                giftMessage.setVisibility(View.VISIBLE);
            } else {
                giftMessage.setVisibility(View.GONE);
            }
            
            final String senderText = String.format(I18n.tr("Love, %s"), mimeData.getSender());
            final String senderName = mimeData.getSender();
            SpannableString spannableSenderStr = new SpannableString(senderText);        
            setGiftMessageInfoSpan(spannableSenderStr, senderText, senderName, ApplicationEx.getColor(R.color.dim_gray));
            giftSender.setText(spannableSenderStr);      
            
            if (mimeData.getType() == GiftType.GIFT_SHOWER) {
                giftShowerBanner.setVisibility(View.VISIBLE);
                giftRecipient.setVisibility(View.GONE);
                
                giftShowerImageContainer.setBackgroundResource(R.drawable.gift_msg_shower_bg);
                Tools.makeBackgroundTiled(giftShowerImageContainer);
                
                contentViewContainer.setBackgroundColor(ApplicationEx.getColor(R.color.dark_beige));
                
            } else {
                giftShowerBanner.setVisibility(View.GONE);
                
                final String recipientText = String.format(I18n.tr("Gift for %s"), mimeData.getRecipient());
                final String recipientName = mimeData.getRecipient();
                SpannableString spannableRecipientStr = new SpannableString(recipientText);
                setGiftMessageInfoSpan(spannableRecipientStr, recipientText, recipientName, ApplicationEx.getColor(R.color.dim_gray));
                giftRecipient.setText(spannableRecipientStr);
                giftRecipient.setVisibility(View.VISIBLE);
               
                UIUtils.setBackground(giftShowerImageContainer, null);
                contentViewContainer.setBackgroundColor(ApplicationEx.getColor(R.color.light_beige));
            }

            if (isPinned) {
               resetLayoutWhenPinned();
            }
            
            return true;
        }
        
        return false;
    }

    private void resetLayoutWhenPinned() {
        //make everything smaller

        giftShowerImageContainer.setPadding(0,0,0,0);

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) giftImage.getLayoutParams();
        params.width = ApplicationEx.getDimension(R.dimen.emoticon_gridSize);
        params.height = ApplicationEx.getDimension(R.dimen.emoticon_gridSize);
        params.bottomMargin = 0;

        giftRecipient.setTextSize(TypedValue.COMPLEX_UNIT_PX, ApplicationEx.getDimension(R.dimen.text_size_medium));
        giftMessage.setTextSize(TypedValue.COMPLEX_UNIT_PX, ApplicationEx.getDimension(R.dimen.text_size_medium));
        giftSender.setTextSize(TypedValue.COMPLEX_UNIT_PX, ApplicationEx.getDimension(R.dimen.text_size_small));

        int padding = ApplicationEx.getDimension(R.dimen.small_padding);
        giftSender.setPadding(padding, padding, padding, padding);
    }

    private static void setGiftMessageInfoSpan(SpannableString spannable, final String fullText, 
            final String highlightedText, final int color) {
        if (spannable == null || fullText == null || highlightedText == null) {
            return;
        }
        
        final ForegroundColorSpan colorSpan = new ForegroundColorSpan(color);
        final int start = fullText.indexOf(highlightedText);
        final int end = start + highlightedText.length();
        
        spannable.setSpan(colorSpan, start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);        
    }

    private boolean isDisplayingLoadingIcon() {
        final Object tag = giftImage.getTag(R.id.image_loading);
        return (tag == null) ? false : ((Boolean) tag).booleanValue();
    }

    @Override
    protected void initializeView() {
        contentViewContainer = (LinearLayout) view.findViewById(R.id.content_view);
        giftShowerImageContainer = (LinearLayout) view.findViewById(R.id.gift_shower_image_container);
        giftShowerBanner = (ImageView) view.findViewById(R.id.gift_shower_banner);
        giftImage = (ImageView) view.findViewById(R.id.gift_image);
        giftRecipient = (TextView) view.findViewById(R.id.gift_recipient);
        giftMessage = (TextView) view.findViewById(R.id.gift_message);
        giftSender = (TextView) view.findViewById(R.id.gift_sender);
        
        final int giftMsgColor = ApplicationEx.getColor(R.color.brown);
        giftRecipient.setTextColor(giftMsgColor);
        giftMessage.setTextColor(giftMsgColor);
        giftSender.setTextColor(giftMsgColor);
    }

    @Override
    public void setParameter(final Parameter parameter, final Object value) {
        super.setParameter(parameter, value);

        switch (parameter) {

            case IS_PINNED:
                isPinned = (Boolean)value;
                break;
        }
    }
}
