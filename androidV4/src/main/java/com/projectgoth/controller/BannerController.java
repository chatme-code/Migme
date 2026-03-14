/**
 * Copyright (c) 2013 Project Goth
 *
 * BannerController.java
 * Created Jul 11, 2014, 3:14:36 PM
 */

package com.projectgoth.controller;

import android.util.DisplayMetrics;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.Banner;
import com.projectgoth.b.enums.ViewTypeEnum;
import com.projectgoth.common.Logger;
import com.projectgoth.common.migcommand.MigCommandAction;
import com.projectgoth.datastore.DataCache;
import com.projectgoth.events.BroadcastHandler;
import com.projectgoth.imagefetcher.ImageHandler;
import com.projectgoth.nemesis.RequestManager;
import com.projectgoth.nemesis.listeners.GetBannerListener;
import com.projectgoth.nemesis.model.MigError;
import com.projectgoth.util.scheduler.JobScheduler;
import com.projectgoth.util.scheduler.JobScheduler.ScheduleListener;

/**
 * @author warrenbalcos
 * 
 */
public class BannerController {

    private static final String TAG           = "BannerController";

    private static final int    BANNER_EXPIRY       = 60 * 60;

    private final static int    BANNER_SWITCH_DELAY = 10 * 1000;
    
    private int                 bannerLoopJobId = -1;

    public enum Placement {
        HOMEFEEDS("homefeed"), SIDEBAR("sidebar");

        private final String title;

        private Placement(String title) {
            this.title = title;
        }

        public String getTitle() {
            return title;
        }
    }

    private DataCache<Banner[]>           bannerCache;

    private static final BannerController INSTANCE = new BannerController();

    private BannerController() {
        bannerCache = new DataCache<Banner[]>(Placement.values().length, BANNER_EXPIRY);

        bannerLoopJobId = JobScheduler.getInstance().createJob(bannerLoopJobId, new ScheduleListener() {

            @Override
            public void processJob() {
                BroadcastHandler.Banner.sendSwitchBanner();
            }
        }, BANNER_SWITCH_DELAY, false);
    }

    public static BannerController getInstance() {
        return INSTANCE;
    }

    public synchronized Banner getBanner(Placement placement) {
        Banner[] bannersData = getBanners(placement);
        Banner banner = null;
        if (bannersData != null && bannersData.length > 0) {
            int counter = (int) (System.currentTimeMillis() / BANNER_SWITCH_DELAY % bannersData.length);
            banner = bannersData[counter];
            if (bannersData.length > 1) {
                JobScheduler.getInstance().restartJob(bannerLoopJobId);
            }
        }
        if (banner == null) {
            banner = getDefaultBanner(placement);
        }
        return banner;
    }

    /**
     * Get the {@link Banner}'s from cache or server
     * 
     * @param placement
     *            - the placement type see {@link Placement}
     * @return
     */
    private synchronized Banner[] getBanners(Placement placement) {
        if (placement == null) {
            throw new IllegalArgumentException("placement must not be null");
        }
        String key = placement.getTitle();

        Logger.info.log(TAG, "getting Banner: ", key);

        Banner[] banners = bannerCache.getData(key);
        if (banners == null || bannerCache.isExpired(key)) {
            fetchBanners(placement);
        }
        return banners;
    }

    /**
     * Fetch {@link Banner}'s from the server
     * 
     * @param placement
     */
    private void fetchBanners(Placement placement) {

        if (placement == null) {
            throw new IllegalArgumentException("placement must not be null");
        }

        final String placementStr = placement.getTitle();
        String platform = ViewTypeEnum.TOUCH.value();

        Logger.info.log(TAG, "fetching from server banner: ", placement);
        final ApplicationEx appEx = ApplicationEx.getInstance();
        if (appEx != null) {
            final RequestManager requestManager = appEx.getRequestManager();
            if (requestManager != null) {
                requestManager.sendGetBanner(new GetBannerListener() {

                    @Override
                    public void onBannerReceived(Banner[] banners) {
                        Logger.info.log(TAG, "received banner: ", placementStr, " data: ", new Gson().toJson(banners));
                        synchronized(BannerController.this) {
                            bannerCache.cacheData(placementStr, banners);
                        }
                        BroadcastHandler.Banner.sendFetchCompleted(placementStr);
                    }

                    @Override
                    public void onError(MigError error) {
                        super.onError(error);
                        Logger.info.log(TAG, "error fetching banner: ", placementStr, " -- ", error.getErrorMsg());
                        BroadcastHandler.Banner.sendFetchError(error);
                    }
                }, platform, placementStr, detectBannerSize());
            }
        }
    }

    public static void setBannerIntoImage(Banner banner, ImageView imgView) {
        if (banner != null && imgView != null) {
            int resId = banner.getImageRes();
            String url = banner.getImageUrl();
            if (resId > 0) {
                imgView.setImageResource(resId);
            } else if (url != null) {
                ImageHandler.getInstance().loadImage(url, imgView, R.drawable.no_picture_banner);
            }
        }
    }
    
    private static String detectBannerSize() {
        DisplayMetrics metrics = ApplicationEx.getInstance().getDisplayMetrics();
        if (metrics != null) {
            switch (metrics.densityDpi) {
                case DisplayMetrics.DENSITY_LOW:
                    return "S";
                case DisplayMetrics.DENSITY_MEDIUM:
                    return "M";
                case DisplayMetrics.DENSITY_HIGH:
                    return "L";
                case DisplayMetrics.DENSITY_XHIGH:
                case DisplayMetrics.DENSITY_XXHIGH:
                    return "XL";
            }
        }
        return "M";
    }
    
    private static Banner getDefaultBanner(Placement placement) {
        Banner banner = new Banner();
        switch (placement) {
            case HOMEFEEDS:
                banner.setImageRes(R.drawable.banner_feed);
                break;
            case SIDEBAR:
                banner.setImageRes(R.drawable.banner_left_panel);
                break;
        }
        banner.setPlacement(placement.getTitle());
        banner.setUrl(MigCommandAction.ShowInviteFriends.getActionUrl());
        return banner;
    }
    
    
}
