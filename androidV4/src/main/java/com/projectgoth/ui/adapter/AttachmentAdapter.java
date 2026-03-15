/**
 * Copyright (c) 2013 Project Goth
 *
 * AttachmentAdapter.java
 * Created Jul 24, 2013, 2:38:44 PM
 */

package com.projectgoth.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.enums.AttachmentType;
import com.projectgoth.model.UsedChatItem;
import com.projectgoth.nemesis.model.Emoticon;
import com.projectgoth.nemesis.model.Sticker;
import com.projectgoth.ui.holder.AttachmentEmoticonViewHolder;
import com.projectgoth.ui.holder.AttachmentStickerViewHolder;
import com.projectgoth.ui.holder.BaseViewHolder.BaseViewListener;
import com.projectgoth.ui.holder.UsedChatItemViewHolder;

import java.util.List;

/**
 * @author mapet
 * 
 */
public class AttachmentAdapter extends BaseAdapter {

    private List<? extends Object>         mAttachmentList;
    private LayoutInflater                 mInflater;
    private int                            mAttachmentType;
    private BaseViewListener<Object> mGridClickListener;
    private int verticalSpacing;

    
    public AttachmentAdapter(int attachmentType) {
        super();
        mInflater = LayoutInflater.from(ApplicationEx.getContext());
        mAttachmentType = attachmentType;
    }

    public void setGridList(List<? extends Object> attachmentList) {
        this.mAttachmentList = attachmentList;
    }

    @Override
    public int getCount() {
        return mAttachmentList.size();
    }

    @Override
    public Object getItem(int position) {
        return mAttachmentList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Object data = mAttachmentList.get(position);

        if (mAttachmentType == AttachmentType.EMOTICON.value || mAttachmentType == AttachmentType.RECENT_EMOTICON.value) {

            AttachmentEmoticonViewHolder attachmentViewHolder;

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.holder_item_emoticon, null);
                attachmentViewHolder = new AttachmentEmoticonViewHolder(convertView);
                attachmentViewHolder.setVerticalSpacing(verticalSpacing);
                convertView.setTag(R.id.holder, attachmentViewHolder);
            } else {
                attachmentViewHolder = (AttachmentEmoticonViewHolder) convertView.getTag(R.id.holder);
            }

            Emoticon e = (Emoticon) data;
            attachmentViewHolder.setData(e);
            attachmentViewHolder.setBaseViewListener(mGridClickListener);

        } else if (mAttachmentType == AttachmentType.RECENT_STICKER_GIFT.value) {
            
            UsedChatItemViewHolder usedChatItemViewHolder;
            
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.holder_item_used_chat_item, null);
                usedChatItemViewHolder = new UsedChatItemViewHolder(convertView);
                usedChatItemViewHolder.setVerticalSpacing(verticalSpacing);
                convertView.setTag(R.id.holder, usedChatItemViewHolder); 
            
            } else {
                usedChatItemViewHolder = (UsedChatItemViewHolder) convertView.getTag(R.id.holder);
            }
            
            UsedChatItem item = (UsedChatItem) data;
            usedChatItemViewHolder.setData(item);
            usedChatItemViewHolder.setBaseViewListener(mGridClickListener);
            
        } else {

            AttachmentStickerViewHolder attachmentViewHolder;

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.holder_item_sticker, null);
                attachmentViewHolder = new AttachmentStickerViewHolder(convertView);
                attachmentViewHolder.setVerticalSpacing(verticalSpacing);
                convertView.setTag(R.id.holder, attachmentViewHolder);

            } else {
                attachmentViewHolder = (AttachmentStickerViewHolder) convertView.getTag(R.id.holder);
            }

            Sticker s = (Sticker) data;
            attachmentViewHolder.setData(s);
            attachmentViewHolder.setBaseViewListener(mGridClickListener);

        }

        return convertView;
    }
    
    public void setGridItemClickListener(BaseViewListener<Object> gridItemClickListener) {
        mGridClickListener = gridItemClickListener;
    }

    public void setVerticalSpacing(int verticalSpacing) {
        this.verticalSpacing = verticalSpacing;
    }


}
