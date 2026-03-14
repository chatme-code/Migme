/**
 * Copyright (c) 2013 Project Goth
 *
 * AttachmentPhotoAdapter.java
 * Created Jul 19, 2013, 3:25:39 PM
 */

package com.projectgoth.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.i18n.I18n;
import com.projectgoth.model.GridItem;
import com.projectgoth.ui.holder.AttachmentPhotoViewHolder;
import com.projectgoth.ui.holder.BaseViewHolder.BaseViewListener;

/**
 * @author mapet
 * 
 */
public class AttachmentPhotoAdapter extends BaseAdapter {

    private static final String[]      mMenuItems = { 
        I18n.tr("Camera"), 
        I18n.tr("Gallery")
    };

    private static final Integer[]     mIcons     = {
        R.drawable.ic_photo_from_camera, 
        R.drawable.ic_photo_from_gallery, 
    };

    private LayoutInflater             mInflater;
    private BaseViewListener<GridItem> mGridClickListener;

    
    public AttachmentPhotoAdapter() {
        super();
        mInflater = LayoutInflater.from(ApplicationEx.getContext());
    }

    @Override
    public int getCount() {
        return mMenuItems.length;
    }

    @Override
    public Object getItem(int position) {
        return mMenuItems[position];
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AttachmentPhotoViewHolder attachmentPhotoViewHolder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.holder_item_photo, null);
            attachmentPhotoViewHolder = new AttachmentPhotoViewHolder(convertView);
            convertView.setTag(R.id.holder, attachmentPhotoViewHolder);
        } else {
            attachmentPhotoViewHolder = (AttachmentPhotoViewHolder) convertView.getTag(R.id.holder);
        }

        GridItem gi = new GridItem(position, mMenuItems[position], mIcons[position]);
        attachmentPhotoViewHolder.setData(gi);
        attachmentPhotoViewHolder.setBaseViewListener(mGridClickListener);

        return convertView;
    }

    public void setGridItemClickListener(BaseViewListener<GridItem> listener) {
        mGridClickListener = listener;
    }

}
