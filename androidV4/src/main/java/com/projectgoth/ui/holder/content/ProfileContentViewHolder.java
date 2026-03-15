/**
 * Copyright (c) 2013 Project Goth
 *
 * ProfileContentViewHolder.java
 * Created Mar 4, 2015, 10:30:12 AM
 */

package com.projectgoth.ui.holder.content;

import android.content.Context;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.projectgoth.R;
import com.projectgoth.b.data.Profile;
import com.projectgoth.b.data.mime.ProfileMimeData;
import com.projectgoth.common.TextUtils;
import com.projectgoth.datastore.UserDatastore;
import com.projectgoth.i18n.I18n;
import com.projectgoth.imagefetcher.ImageHandler;
import com.projectgoth.ui.widget.UsernameWithLabelsView;


/**
 * @author shiyukun
 *
 */
public class ProfileContentViewHolder extends ContentViewHolder<ProfileMimeData, RelativeLayout>{
    
    private ImageView               avatar;
    private UsernameWithLabelsView  userDetails;
    private TextView                migLevel;
    private TextView                aboutMe;
    private TextView                aboutMeContent;
    private TextView                seeMore;
    
    /**
     * @param ctx
     * @param mimeData
     */
    public ProfileContentViewHolder(Context ctx, ProfileMimeData mimeData) {
        super(ctx, mimeData);
    }

    @Override
    protected void initializeView() {
        avatar = (ImageView) view.findViewById(R.id.avatar);
        userDetails = (UsernameWithLabelsView) view.findViewById(R.id.name_details);
        migLevel = (TextView) view.findViewById(R.id.mig_level);
        aboutMe = (TextView) view.findViewById(R.id.about_me);
        aboutMeContent = (TextView) view.findViewById(R.id.about_me_content);
        seeMore = (TextView) view.findViewById(R.id.see_more);
    }

    @Override
    public int getLayoutId() {
        return R.layout.content_view_profile;
    }

    @Override
    public boolean applyMimeData() {
        if (mimeData != null) {
            String username = mimeData.getUsername();
            Profile profile = UserDatastore.getInstance().getProfileWithUsername(username, false);
            if (profile != null) {
//                String avatarUrl = ImageHandler.getInstance().getDisplayPictureUrl(username, profile.getDisplayPictureType(), 90);
                ImageHandler.getInstance().loadDisplayPictureOfUser(avatar, username, profile.getDisplayPictureType(), 
                        90, true);
                
                userDetails.setUsername(username);
                userDetails.setLabels(profile.getLabels());
                migLevel.setText(I18n.tr("Level") + " " + profile.getMigLevel());
                aboutMe.setText(I18n.tr("About Me"));
                String aboutMeStr = profile.getAboutMe();
                if(!TextUtils.isEmpty(aboutMeStr)){
                    aboutMeContent.setText(aboutMeStr);
                }

                if (profile.getLabels() == null) {
                    //if profile not null abut Labels null , it could be happen when the profile is returned by
                    //some requests like fetch "mention_autocomplete_list" , then we need to fetch the complete profile
                    UserDatastore.getInstance().getProfileWithUsername(username, true);
                }
            }  
            
            seeMore.setText(I18n.tr("See more"));
            return true;
        }
        
        return false;
    }

}
