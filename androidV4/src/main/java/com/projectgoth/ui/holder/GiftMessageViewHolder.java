/**
 * Copyright (c) 2013 Project Goth
 *
 * GiftMessageViewHolder.java
 * Created Aug 4, 2014, 1:47:13 PM
 */

package com.projectgoth.ui.holder;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.mime.GiftMimeData;
import com.projectgoth.b.data.mime.GiftMimeData.GiftType;
import com.projectgoth.b.data.mime.MimeData;
import com.projectgoth.common.Config;
import com.projectgoth.imagefetcher.ImageHandler;
import com.projectgoth.model.Message;
import com.projectgoth.ui.holder.content.ContentViewHolder;
import com.projectgoth.util.mime.MimeUtils;

/**
 * @author angelorohit
 * 
 */
public class GiftMessageViewHolder extends BaseMessageViewHolder {
    
    private ImageView      giftSenderAvatar;
    private ViewGroup      contentViewContainer;
    
    public GiftMessageViewHolder(View view) {
        super(view);
        
        giftSenderAvatar = (ImageView) view.findViewById(R.id.message_avatar);
        contentViewContainer = (ViewGroup) view.findViewById(R.id.content_view_container);
        giftSenderAvatar.setOnClickListener(this);
    }


    @Override
    public void setData(Message message) {
        super.setData(message);
        
        final GiftMimeData giftMimeData = (GiftMimeData) message.getFirstMimeDataAs(GiftMimeData.class);
        if (giftMimeData != null) {
            final ContentViewHolder<? extends MimeData, ? extends View> contentViewHolder = applyMimeDataToHolder(ApplicationEx.getContext(), giftMimeData);
            if (contentViewHolder != null) {
                contentViewContainer.removeAllViews();
                contentViewContainer.addView(contentViewHolder.getContentView());
                
                if (giftMimeData.getType() == GiftType.GIFT_SHOWER) {
                    contentViewContainer.setBackgroundResource(
                                (MimeUtils.isIncomingGift(giftMimeData)) ? 
                                        R.drawable.gift_shower_msg_incoming : R.drawable.gift_shower_msg_outgoing);
                    
                } else {
                    contentViewContainer.setBackgroundResource(
                            (MimeUtils.isIncomingGift(giftMimeData)) ? 
                                    R.drawable.gift_msg_incoming : R.drawable.gift_msg_outgoing);
                }

                contentViewHolder.getContentView().setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        mimeListener.onContentViewLongClick(data, giftMimeData);
                        return true;
                    }
                });

                // Sender avatar
                if (MimeUtils.isIncomingGift(giftMimeData)) {
                    giftSenderAvatar.setVisibility(View.VISIBLE);
                    giftSenderAvatar.setImageResource(R.drawable.icon_default_avatar);
                    
                    // Use the profile image of the user if it is available.
                    // Otherwise simply, use the display picture guid sent in the message.
                    ImageHandler.getInstance().loadDisplayPictureOrGuid(giftSenderAvatar, giftMimeData.getSender(), 
                            Config.getInstance().getDisplayPicSizeSmall(), true, data.getDisplayPictureGuid(), 
                            R.drawable.icon_default_avatar);
                    
                } else {
                    giftSenderAvatar.setVisibility(View.GONE);
                }
            }
        }
    }
}
