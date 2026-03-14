/**
 * Copyright (c) 2013 Project Goth
 *
 * AttachmentPhotoFragment.java
 * Created Jul 19, 2013, 10:15:02 AM
 */

package com.projectgoth.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import com.projectgoth.R;
import com.projectgoth.common.Constants;
import com.projectgoth.common.Tools;
import com.projectgoth.model.GridItem;
import com.projectgoth.ui.activity.ActionHandler;
import com.projectgoth.ui.adapter.AttachmentPhotoAdapter;
import com.projectgoth.ui.holder.BaseViewHolder.BaseViewListener;

/**
 * @author mapet
 * 
 */
public class AttachmentPhotoFragment extends BaseFragment implements BaseViewListener<GridItem> {

    private GridView               mAttachmentList;
    private AttachmentPhotoAdapter mAttachmentPhotoAdapter;

    private PhotoEventListener     photoEventListener;
    private int                    backgroundResource;

    private int                    PHOTO_COLUMNS = 2;

    public interface PhotoEventListener {

        public void onPhotoSendPhoto(Bitmap photo);

        public void onPhotoSendPhoto(byte[] photo);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_attachment_photo;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAttachmentList = (GridView) view.findViewById(R.id.menu);
        if (this.backgroundResource != 0) {
            mAttachmentList.setBackgroundColor(this.backgroundResource);
        }
        mAttachmentList.setNumColumns(PHOTO_COLUMNS);
        mAttachmentPhotoAdapter = new AttachmentPhotoAdapter();
        mAttachmentPhotoAdapter.setGridItemClickListener(this);
        mAttachmentList.setAdapter(mAttachmentPhotoAdapter);
    }

    @Override
    public void onItemClick(View v, GridItem data) {
        switch (data.getId()) {
            case 0:
                ActionHandler.getInstance().takePhoto(getActivity(), Constants.REQ_PIC_FROM_CAMERA_FOR_CHAT_MSG, false);
                break;
            case 1:
                ActionHandler.getInstance().pickFromGallery(getActivity(), Constants.REQ_PIC_FROM_GALLERY_FOR_CHAT_MSG);
                break;
        }
    }

    @Override
    public void onItemLongClick(View v, GridItem data) {
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == Constants.REQ_PIC_FROM_CAMERA_FOR_CHAT_MSG && resultCode == Activity.RESULT_OK) {
            try {
                Bitmap photo = Tools.loadImageFromCapturedPhotoFile(getActivity());
                if (photoEventListener != null) {
                    photoEventListener.onPhotoSendPhoto(photo);
                }
            } catch (Exception e) {
            }

        } else if (requestCode == Constants.REQ_PIC_FROM_GALLERY_FOR_CHAT_MSG && resultCode == Activity.RESULT_OK) {
            if (intent != null) {
                Uri selectedImage = intent.getData();

                try {
                    Bitmap resizedBitmap = Tools.resizeAndRotateImage(getActivity(), selectedImage,
                            Constants.DEFAULT_PHOTO_SIZE, Constants.DEFAULT_PHOTO_SIZE);

                    if (photoEventListener != null) {
                        photoEventListener.onPhotoSendPhoto(resizedBitmap);
                    }

                } catch (Exception e) {
                }
            }
        }
    }

    /**
     * @return the photoEventListener
     */
    public PhotoEventListener getPhotoEventListener() {
        return photoEventListener;
    }

    /**
     * @param photoEventListener
     *            the photoEventListener to set
     */
    public void setPhotoEventListener(PhotoEventListener photoEventListener) {
        this.photoEventListener = photoEventListener;
    }

    public void setGridBackground(int background) {
        this.backgroundResource = background;
    }
}
