/**
 * Copyright (c) 2013 Project Goth
 *
 * ImagePagerAdapter.java
 * Created Jan 20, 2015, 2:55:04 PM
 */

package com.projectgoth.ui.adapter;

import java.util.List;

import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

import com.mig33.diggle.common.StringUtils;
import com.projectgoth.R;
import com.projectgoth.b.data.Banner;
import com.projectgoth.imagefetcher.ImageHandler;
import com.projectgoth.ui.activity.ActionHandler;
import com.projectgoth.ui.activity.FragmentHandler;

/**
 * @author shiyukun
 *
 */
public class ImagePagerAdapter extends RecyclingPagerAdapter {

    private FragmentActivity            activity;
    private List<Banner>                bannerList;

    private int                         size;
    private boolean                     isInfiniteLoop;
    
    private static final String         BANNER_IMAGE_PROFIX = "image";
    private static final String         BANNER_GAME_PROFIX = "game";

    public ImagePagerAdapter(FragmentActivity activity, List<Banner> bannerList) {
        this.activity = activity;
        this.bannerList = bannerList;
        this.size = bannerList == null ? 0 : bannerList.size();
        isInfiniteLoop = false;
    }

    @Override
    public int getCount() {
        // Infinite loop
        int size = bannerList == null ? 0 : bannerList.size();
        return isInfiniteLoop ? Integer.MAX_VALUE : size;
    }

    /**
     * get really position
     * 
     * @param position
     * @return
     */
    private int getPosition(int position) {
        return isInfiniteLoop ? position % size : position;
    }

    @Override
    public View getView(int position, View view, ViewGroup container) {
        ViewHolder holder;
        final Banner banner = bannerList.get(getPosition(position));
        if (view == null) {
            holder = new ViewHolder();
            view = holder.imageView = new ImageView(activity.getApplicationContext());
            view.setTag(holder);
            ((ImageView)view).setScaleType(ImageView.ScaleType.CENTER_CROP);
            ((ImageView)view).setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            
            view.setOnClickListener(new OnClickListener() {
                
                @Override
                public void onClick(View v) {
                    String actionUrl = banner.getUrl();
                    if(StringUtils.isEmpty(actionUrl)) return;
                    if(actionUrl.startsWith(BANNER_IMAGE_PROFIX)){
                        ActionHandler.getInstance().displayPhotoViewerFragment(activity, banner.getImageUrl(), "", null, false, true);  
                    }else if(actionUrl.startsWith(BANNER_GAME_PROFIX)){
                        String gameId = actionUrl.substring(BANNER_GAME_PROFIX.length() + 1);
                        ActionHandler.getInstance().displayGameDetailPageFragment(activity, FragmentHandler.GameDetailPageFragmentType.Main, gameId);
                    }
                }
            });
        } else {
            holder = (ViewHolder)view.getTag();
        }
        ImageHandler.getInstance().loadImageFromUrl(holder.imageView, banner.getImageUrl(), false, R.drawable.ad_avatar_grey);
        return view;
    }

    private static class ViewHolder {
        ImageView imageView;
    }

    /**
     * @return the isInfiniteLoop
     */
    public boolean isInfiniteLoop() {
        return isInfiniteLoop;
    }

    /**
     * @param isInfiniteLoop the isInfiniteLoop to set
     */
    public ImagePagerAdapter setInfiniteLoop(boolean isInfiniteLoop) {
        this.isInfiniteLoop = isInfiniteLoop;
        return this;
    }

}

