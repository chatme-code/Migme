/**
 * Copyright (c) 2013 Project Goth
 *
 * AttachmentTabAdapter.java
 * Created Jul 11, 2013, 2:30:09 PM
 */

package com.projectgoth.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.nemesis.model.BaseEmoticonPack;
import com.projectgoth.ui.holder.AttachmentTabViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * @author mapet
 * 
 */
public class AttachmentTabAdapter extends BaseAdapter {

    private List<BaseEmoticonPack> mAttachmentTabList = new ArrayList<BaseEmoticonPack>();
    private LayoutInflater         mInflater;
    private int                    mSelected;
    private int                    mPadding = ApplicationEx.getDimension(R.dimen.normal_padding);
    
    public AttachmentTabAdapter() {
        super();
        mInflater = LayoutInflater.from(ApplicationEx.getContext());
    }

    @Override
    public int getCount() {
        return mAttachmentTabList.size();
    }

    @Override
    public Object getItem(int position) {
        return mAttachmentTabList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AttachmentTabViewHolder attachmentTabViewHolder = null;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.holder_attachment_tab, null);
            attachmentTabViewHolder = new AttachmentTabViewHolder(convertView);

            if (position == mSelected) {
                attachmentTabViewHolder.setIsSelected(true);
            } else {
                attachmentTabViewHolder.setIsSelected(false);
            }

            convertView.setTag(R.id.holder, attachmentTabViewHolder);
        } else {
            attachmentTabViewHolder = (AttachmentTabViewHolder) convertView.getTag(R.id.holder);
        }
        
        attachmentTabViewHolder.setImagePadding(mPadding);
        BaseEmoticonPack tabData = mAttachmentTabList.get(position);
        attachmentTabViewHolder.setData(tabData);

        return convertView;
    }

    public void setAttachmentTabList(List<BaseEmoticonPack> attachmentTabListData) {
        mAttachmentTabList = attachmentTabListData;
    }

    public void setSelected(int selected) {
        mSelected = selected;
    }

    /**
     * @param padding the padding to set
     */
    public void setPadding(int padding) {
        this.mPadding = padding;
    }

}
