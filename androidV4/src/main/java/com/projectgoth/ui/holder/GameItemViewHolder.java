/**
 * Copyright (c) 2013 Project Goth
 *
 * GameItemViewHolder.java
 * Created Jan 21, 2015, 3:39:55 PM
 */

package com.projectgoth.ui.holder;

import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mig33.diggle.common.StringUtils;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.GameItem;
import com.projectgoth.imagefetcher.ImageHandler;


/**
 * @author shiyukun
 *
 */
public class GameItemViewHolder extends BaseViewHolder<GameItem> {
    
//    private RelativeLayout          container;
    private ImageView               thumbnailView;
    private TextView                IMChatIcon;
    private TextView                name;
    private TextView                description;
    private static final int        desLength = 100;

    /**
     * @param rootView
     */
    public GameItemViewHolder(View view) {
        super(view);
        
//        container = (RelativeLayout) view.findViewById(R.id.container);
        thumbnailView = (ImageView) view.findViewById(R.id.thumbnail);
        IMChatIcon = (TextView) view.findViewById(R.id.IM_chat_icon);
        name = (TextView) view.findViewById(R.id.name);
        description = (TextView) view.findViewById(R.id.description);
        
    }
    
    public void setData(GameItem game){
        super.setData(game);
        if(game.getType() != GameItem.GAME_CHATROOM){
            thumbnailView.setVisibility(View.VISIBLE);
            IMChatIcon.setVisibility(View.INVISIBLE);
        }else{
            //set first character
            IMChatIcon.setText(String.valueOf(Character.toUpperCase(game.getName().charAt(0))));
            //set color
            int color = ApplicationEx.getColor(R.color.IM_contact_icon_selectunable);
            GradientDrawable bg = (GradientDrawable) IMChatIcon.getBackground();
            bg.setColor(color);
            thumbnailView.setVisibility(View.INVISIBLE);
            IMChatIcon.setVisibility(View.VISIBLE);
        }
        // set image
        ImageHandler.getInstance().loadImageFromUrl(thumbnailView, game.getThumbnail(), true, R.drawable.ad_avatar_grey);
        
        // set display name
        name.setText(game.getName());
        // set description
        String descriptionInfo = game.getDescriptionInfo();
        if(!StringUtils.isEmpty(descriptionInfo) && descriptionInfo.length() > desLength){
            description.setText(game.getDescriptionInfo().substring(0, desLength) + "...");
        }else{
            description.setText(descriptionInfo);
        }
    }

}
