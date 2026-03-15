/**
 * Copyright (c) 2013 Project Goth
 *
 * EmoteViewHolder.java
 * Created Oct 7, 2014, 11:33:16 PM
 */

package com.projectgoth.ui.holder;

import android.view.View;
import android.widget.ImageView;
import com.projectgoth.R;
import com.projectgoth.b.data.Tag;
import com.projectgoth.b.data.UserTagId;
import com.projectgoth.b.enums.UserProfileDisplayPictureChoiceEnum;
import com.projectgoth.common.Config;
import com.projectgoth.datastore.PostsDatastore;
import com.projectgoth.imagefetcher.ImageHandler;

/**
 * @author warrenbalcos
 * 
 */
public class EmoteViewHolder extends BaseViewHolder<UserTagId> {

    private int       criteriaId;

    private ImageView avatar;
    private ImageView tagView;

    /**
     * @param rootView
     */
    public EmoteViewHolder(View rootView, int criteriaId) {
        super(rootView);

        this.criteriaId = criteriaId;

        avatar = (ImageView) rootView.findViewById(R.id.avatar);
        tagView = (ImageView) rootView.findViewById(R.id.tag);
    }

    @Override
    public void setData(UserTagId data) {
        super.setData(data);

        if (data == null) {
            return;
        }

        ImageHandler.getInstance().loadDisplayPictureOfUser(avatar, data.getUsername(),
                UserProfileDisplayPictureChoiceEnum.PROFILE_PICTURE, Config.getInstance().getDisplayPicSizeSmall(),
                true);

        Tag tag = PostsDatastore.getInstance().getEmotionalFootprintTag(criteriaId, data.getTagId());
        if (tag != null) {
            ImageHandler.getInstance().loadImageFromUrl(tagView, tag.getImage(), false, R.drawable.ad_loadstaticchat_grey);
        }

    }

}
