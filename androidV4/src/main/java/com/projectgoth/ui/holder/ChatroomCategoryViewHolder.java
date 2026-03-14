/**
 * Copyright (c) 2013 Project Goth
 *
 * ChatroomCategoryViewHolder.java
 * Created Sep 16, 2014, 2:05:10 PM
 */

package com.projectgoth.ui.holder;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.enums.ChatroomCategoryEnum;
import com.projectgoth.i18n.I18n;
import com.projectgoth.nemesis.model.ChatRoomCategory;


/**
 * @author houdangui
 *
 */
public class ChatroomCategoryViewHolder extends BaseViewHolder<ChatRoomCategory> {

    private ImageView categoryIcon;
    private TextView categoryName;
    private ImageView refreshBtn;
    
    private boolean mIsCategorySizeVisible;
    
    private ChatroomCategoryListener mListener;
    
    public interface ChatroomCategoryListener {
     
        public void onRefreshIconClicked(ChatRoomCategory chatRoomCategory);
        
    }
    
    public ChatroomCategoryViewHolder(View rootView) {
        super(rootView , false);
        
        categoryIcon = (ImageView) rootView.findViewById(R.id.category_icon);
        categoryName = (TextView) rootView.findViewById(R.id.category_name);
        refreshBtn = (ImageView) rootView.findViewById(R.id.refresh_btn);
    }
    
    @Override
    public void setData(ChatRoomCategory data) {
        super.setData(data);
        //chatroom category icon
        int resId = getChatroomCategoryIconResId(data.getID());
        if (resId != -1) {
            categoryIcon.setImageResource(resId);
            categoryIcon.setVisibility(View.VISIBLE);
        } else {
            categoryIcon.setVisibility(View.INVISIBLE);
        }
        
        //chatroom category name
        if (mIsCategorySizeVisible) {
            categoryName.setText(I18n.tr(data.getName()) + " (" + data.getChatroomItemsSize() + ")");
        } else {
            categoryName.setText(I18n.tr(data.getName()));
        }
    }
    
    /**
     * @param id
     * @return
     */
    private int getChatroomCategoryIconResId(short id) {
        ChatroomCategoryEnum categoryEnum = ChatroomCategoryEnum.fromValue(id);
        
        if (categoryEnum == null) {
            return -1;
        }
        
        switch (categoryEnum) {
            case RECENT:
                return R.drawable.ad_recent_grey;
            case FAVORITES:
                return R.drawable.ad_favourite_grey;
            case GAMES:
                return R.drawable.ad_grp_play_grey;
            case RECOMMENDED:
                return R.drawable.ad_recommended_grey;
            case FRIEND_FINDER:
                return R.drawable.ad_grp_usersearch;
            case GAME_ZONE:
                return R.drawable.ad_grp_play_grey;
            case HELP:
                return R.drawable.ad_grp_question;
            default:
                return R.drawable.ad_recommended_grey;
        }  
    }

    public void showRefreshButton(boolean isToShow) {
        if (isToShow) {
            refreshBtn.setVisibility(View.VISIBLE);
        } else {
            refreshBtn.setVisibility(View.INVISIBLE);
        }
    }
    
    /**
     * Animates the refresh icon if it is present in the group.
     * 
     * @param shouldAnimate
     *            Whether animation should be started or stopped.
     */
    public void animateRefreshIcon(final boolean shouldAnimate) {
        if (refreshBtn != null && refreshBtn.getVisibility() == View.VISIBLE) {
            Animation rotateAnimation = refreshBtn.getAnimation();
            if (rotateAnimation == null) {
                rotateAnimation = AnimationUtils.loadAnimation(ApplicationEx.getContext(),
                        R.anim.rotate_around_center_point);
            }

            if (shouldAnimate) {
                rotateAnimation.setRepeatCount(Animation.INFINITE);

                if (!rotateAnimation.hasStarted()) {
                    refreshBtn.startAnimation(rotateAnimation);
                }
            } else if (rotateAnimation.hasStarted()) {
                // Let the rotate animation play out for one more time.
                // This is so that it aligns back to its start position.
                rotateAnimation.setRepeatCount(1);
            }
        }
    }
    
    public void setListener(ChatroomCategoryListener listener) {
        this.mListener = listener;
        
        refreshBtn.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onRefreshIconClicked(data);    
                }   
            }
        });
    }

    public void setCategorySizeVisible(boolean visible) {
        mIsCategorySizeVisible = visible;
    }
    
}
