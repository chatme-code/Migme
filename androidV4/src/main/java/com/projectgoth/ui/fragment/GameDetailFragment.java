
package com.projectgoth.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mig33.diggle.common.StringUtils;
import com.projectgoth.R;
import com.projectgoth.b.data.Banner;
import com.projectgoth.b.data.GameItem;
import com.projectgoth.datastore.GamesDatastore;
import com.projectgoth.events.AppEvents;
import com.projectgoth.events.GAEvent;
import com.projectgoth.imagefetcher.ImageHandler;
import com.projectgoth.imagefetcher.UIUtils;
import com.projectgoth.listener.GameBannerOnPageChangeListener;
import com.projectgoth.localization.I18n;
import com.projectgoth.ui.activity.CustomActionBarConfig;
import com.projectgoth.ui.activity.CustomActionBarConfig.NavigationButtonState;
import com.projectgoth.ui.adapter.GameDetailPagerAdapter;
import com.projectgoth.ui.adapter.ImagePagerAdapter;
import com.projectgoth.ui.widget.AutoScrollViewPager;
import com.projectgoth.ui.widget.TextButton;

/**
 * Created by danielchen on 15/1/22.
 */
public class GameDetailFragment extends BaseFragment implements View.OnClickListener {

    private static final int   mInfoTabPosition   = 0;
    private static final int   mRateTabPosition   = 1;
    private static final int   mRankTabPosition   = 2;
    private static final int   mFriendTabPosition = 3;
    public static final String KEY_GAME_ITEM      = "KEY_GAME_ITEM";
    
    private static final String MIG_GAME_APP_URL  = "https://play.google.com/store/apps/details?id=com.matchme.migmenemezis";
    private static final String MIG_GAME_APP_PACKAGE = "com.matchme.migmenemezis";
    
    private ImageView          mInfoTab;
    private ImageView          mRateTab;
    private ImageView          mRankTab;
    private ImageView          mFriendTab;
    private TextButton         mPlayButton;
    private ViewPager          mViewPager;
    private LinearLayout       mRatingWrapper;
    private RelativeLayout     mGameBannerRelativeLayout;
    private ImageView          mGameThumbnail;
    
    private GameItem           mGameItem;
    
    private AutoScrollViewPager  bannerViewPager;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_games_detail;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        int ratingStars = 0;
        Bundle bundle = getArguments();
        String gameId = bundle.getString(GameDetailFragment.KEY_GAME_ITEM);
        mGameItem = GamesDatastore.getInstance().getGameItemWithId(gameId);
        TextView gameTitle = (TextView) view.findViewById(R.id.game_title);
        gameTitle.setText(mGameItem.getName());
        mInfoTab = (ImageView) view.findViewById(R.id.game_detail_tab_info);
        mRateTab = (ImageView) view.findViewById(R.id.game_detail_tab_rate);
        mRankTab = (ImageView) view.findViewById(R.id.game_detail_tab_rank);
        mGameThumbnail = (ImageView) view.findViewById(R.id.thumbnail);
        mFriendTab = (ImageView) view.findViewById(R.id.game_detail_tab_friend);
        mPlayButton = (TextButton) view.findViewById(R.id.button_play);
        mViewPager = (ViewPager) view.findViewById(R.id.pager);

        mInfoTab.setOnClickListener(this);
        mRateTab.setOnClickListener(this);
        mRankTab.setOnClickListener(this);
        mFriendTab.setOnClickListener(this);
        mPlayButton.setOnClickListener(this);
//        mRatingWrapper = (LinearLayout) view.findViewById(R.id.rating_wrapper);
        
        mGameBannerRelativeLayout = (RelativeLayout)view.findViewById(R.id.game_banner_div); 
        //load thumbnail
        ImageHandler.getInstance().loadImageFromUrl(mGameThumbnail, mGameItem.getThumbnail(), true, R.drawable.ad_avatar_grey);

        bannerViewPager = (AutoScrollViewPager) getActivity().findViewById(R.id.banner_view_pager);
        updateBanners(mGameItem.getImages());
        

        GameDetailPagerAdapter adapter = new GameDetailPagerAdapter(getChildFragmentManager(), getActivity(),
                mGameItem);
        mViewPager.setAdapter(adapter);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int i, float v, int i1) {
            }

            @Override
            public void onPageSelected(int position) {
                ImageView currentTab = mInfoTab;
                switch (position) {
                    case mInfoTabPosition:
                        currentTab = mInfoTab;
                        break;
                    case mRateTabPosition:
                        currentTab = mRateTab;
                        break;
                    case mRankTabPosition:
                        currentTab = mRankTab;
                        break;
                    case mFriendTabPosition:
                        currentTab = mFriendTab;
                        break;
                }
                setImageTabSelected(currentTab);
            }

            @Override
            public void onPageScrollStateChanged(int i) {
            }
        });

        setImageTabSelected(mInfoTab);
        mViewPager.setCurrentItem(mInfoTabPosition, true);

        if(mGameItem.getType() == GameItem.GAME_MULTIPLY){
        }
//        showRatingStars(ratingStars);
    }
    
    @Override
    protected void registerReceivers() {
        registerEvent(AppEvents.Games.FETCH_GAME_COMPLETED);
    }
    
    protected void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (action.equals(AppEvents.Games.FETCH_GAME_COMPLETED)) {
            updateBanners(mGameItem.getImages());
        }
    }

    private void updateBanners(String[] images) {
        ArrayList<Banner> gameBanners = new ArrayList<Banner>();
        if(images == null || !UIUtils.hasHoneycomb()){
            bannerViewPager.setVisibility(View.GONE);
        }else{
            bannerViewPager.setVisibility(View.VISIBLE);
        }
        for(int i=0 ; i<images.length; i++){
            String bannerUrl = images[i];
            Banner gameBanner = new Banner();
            gameBanner.setImageUrl(bannerUrl);
            gameBanner.setUrl("image");
            gameBanners.add(gameBanner);
        }
        
        if(gameBanners.size() > 0){
            bannerViewPager.setAdapter(new ImagePagerAdapter(getActivity(), gameBanners)
            .setInfiniteLoop(true));
            bannerViewPager.setOnPageChangeListener(new GameBannerOnPageChangeListener());
            bannerViewPager.setInterval(4000);
            bannerViewPager.startAutoScroll();
        }else{
            mGameBannerRelativeLayout.setVisibility(View.GONE);
        }
    }

    private void showRatingStars(int stars) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 10, 0);
        for (int i = 0; i < stars; i++) {
            ImageView star = new ImageView(getActivity());
            star.setImageResource(R.drawable.ad_rating);
            mRatingWrapper.addView(star, params);
        }
    }
    
    @Override
    public void onClick(View view) {
        if (view == mInfoTab) {
            mViewPager.setCurrentItem(mInfoTabPosition, true);
        } else if (view == mRateTab) {
            mViewPager.setCurrentItem(mRateTabPosition, true);
        } else if (view == mRankTab) {
            mViewPager.setCurrentItem(mRankTabPosition, true);
        } else if (view == mFriendTab) {
            mViewPager.setCurrentItem(mFriendTabPosition, true);
        } else if (view == mPlayButton) {

            String gameName;
            if (mGameItem != null && (gameName = mGameItem.getName()) != null) {
                GAEvent.GameDetail_ClickPlayGame.send(gameName);
            } else {
                GAEvent.GameDetail_ClickPlayGame.send();
            }

            String url = mGameItem.getActionUrl();
            if (StringUtils.isEmpty(url)) return ;
            if (mGameItem.getType() == GameItem.GAME_SINGLE) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            } else if (mGameItem.getType() == GameItem.GAME_MULTIPLY) {
                PackageManager manager = getActivity().getPackageManager();
                Intent intent = manager.getLaunchIntentForPackage(MIG_GAME_APP_PACKAGE);
                if (intent == null) {
                    intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(MIG_GAME_APP_URL));
                    startActivity(intent);
                } else {
                    startActivityForResult(intent, 0);
                }
            }
        }
    }

    private void setImageTabSelected(ImageView view) {
        mInfoTab.setSelected(false);
        mRateTab.setSelected(false);
        mRankTab.setSelected(false);
        mFriendTab.setSelected(false);
        view.setSelected(true);
    }

    @Override
    public CustomActionBarConfig getActionBarConfig() {
        CustomActionBarConfig config = super.getActionBarConfig();
        config.setNavigationButtonState(NavigationButtonState.BACK);
        return config;

    }

    @Override
    protected String getTitle() {
        return I18n.tr("Games");
    }

    @Override
    protected int getTitleIcon() {
        return R.drawable.ad_play_white;
    }

}