/**
 * Copyright (c) 2013 Project Goth
 *
 * GameCentreFragment.java
 * Created Jan 19, 2015, 4:00:13 PM
 */

package com.projectgoth.ui.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import com.projectgoth.R;
import com.projectgoth.b.data.Banner;
import com.projectgoth.b.data.GameItem;
import com.projectgoth.common.WebURL;
import com.projectgoth.datastore.GamesDatastore;
import com.projectgoth.events.AppEvents;
import com.projectgoth.events.GAEvent;
import com.projectgoth.i18n.I18n;
import com.projectgoth.imagefetcher.UIUtils;
import com.projectgoth.listener.GameBannerOnPageChangeListener;
import com.projectgoth.ui.activity.ActionHandler;
import com.projectgoth.ui.activity.CustomActionBarConfig;
import com.projectgoth.ui.activity.CustomActionBarConfig.NavigationButtonState;
import com.projectgoth.ui.activity.FragmentHandler;
import com.projectgoth.ui.adapter.GameListAdapter;
import com.projectgoth.ui.adapter.ImagePagerAdapter;
import com.projectgoth.ui.holder.BaseViewHolder.BaseViewListener;
import com.projectgoth.ui.widget.AutoScrollViewPager;
import com.projectgoth.ui.widget.ExpandableHeightListView;
import com.projectgoth.datastore.Session;

/**
 * @author shiyukun
 * 
 */
// TODO
public class GameCentreFragment extends BaseFragment implements BaseViewListener<GameItem> {

    public static final String                    PLAYED_GAME_COUNT = "PLAYED_GAME_COUNT";

    private AutoScrollViewPager                   viewPager; 

    private List<Banner>                          bannerList;
    private HashMap<Integer, ArrayList<GameItem>> gameMap;

    private ExpandableHeightListView              singleGameListView;
    private ExpandableHeightListView              multiGameListView;

    private GameListAdapter                       mSingleGamesAdapter;
    private GameListAdapter                       mMultiGamesAdapter;
    private RelativeLayout                        chatRoomLayout;
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewPager = (AutoScrollViewPager) getActivity().findViewById(R.id.view_pager);
        bannerList = new ArrayList<Banner>();
        gameMap = new HashMap<Integer, ArrayList<GameItem>>();
        
        chatRoomLayout = (RelativeLayout) getActivity().findViewById(R.id.chatroom_layout);
        chatRoomLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                goToChatRoomGame();
            }
        });

        singleGameListView = (ExpandableHeightListView) getActivity().findViewById(R.id.single_game_list);
        multiGameListView = (ExpandableHeightListView) getActivity().findViewById(R.id.multi_game_list);

        mSingleGamesAdapter = new GameListAdapter();
        mMultiGamesAdapter = new GameListAdapter();

        mSingleGamesAdapter.setGameClickListener(this);
        mMultiGamesAdapter.setGameClickListener(this);

        singleGameListView.setAdapter(mSingleGamesAdapter);
        singleGameListView.setExpanded(true);

        multiGameListView.setAdapter(mMultiGamesAdapter);
        multiGameListView.setExpanded(true);

    }

    protected void onShowFragment() {
        super.onShowFragment();
        bannerList = GamesDatastore.getInstance().getBannerList(true);
        updateBanners();
        gameMap = GamesDatastore.getInstance().getGameList(true);
        updateGames();
    }
    
    @Override
    protected int getLayoutId() {
        return R.layout.fragment_games;
    }

    @Override
    protected void readBundleArguments(Bundle args) {
        super.readBundleArguments(args);
    }

    @Override
    protected String getTitle() {
        return I18n.tr("Games");
    }

    @Override
    protected int getTitleIcon() {
        return R.drawable.ad_play_white;
    }

    @Override
    public CustomActionBarConfig getActionBarConfig() {
        CustomActionBarConfig config = super.getActionBarConfig();
        config.setNavigationButtonState(NavigationButtonState.BACK);
        return config;
    }

    @Override
    public void onPause() {
        super.onPause();
        // stop auto scroll when onPause
        viewPager.stopAutoScroll();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    protected void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (action.equals(AppEvents.Games.FETCH_BANNER_COMPLETED)) {
            bannerList = GamesDatastore.getInstance().getBannerList(false);
            updateBanners();
        } else if (action.equals(AppEvents.Games.FETCH_GAMES_COMPLETED)) {
            gameMap = GamesDatastore.getInstance().getGameList(false);
            updateGames();
        }
    }

    @Override
    protected void registerReceivers() {
        registerEvent(AppEvents.Games.FETCH_BANNER_COMPLETED);
        registerEvent(AppEvents.Games.FETCH_GAMES_COMPLETED);
        registerEvent(AppEvents.Games.FETCH_GAME_COMPLETED);
    }

    private void updateBanners() {
        if(bannerList == null || bannerList.size() == 0 || !UIUtils.hasHoneycomb()) {
            viewPager.setVisibility(View.GONE);
        }else{
            viewPager.setVisibility(View.VISIBLE);
        }
        viewPager.setAdapter(new ImagePagerAdapter(getActivity(), bannerList)
                .setInfiniteLoop(true));
        viewPager.setOnPageChangeListener(new GameBannerOnPageChangeListener());
        viewPager.setInterval(4000);
        viewPager.startAutoScroll();
    }

    private void updateGames() {
        if(gameMap == null || gameMap.size() == 0) return;
        mSingleGamesAdapter.setGameList(gameMap.get(GameItem.GAME_SINGLE));
        mMultiGamesAdapter.setGameList(gameMap.get(GameItem.GAME_MULTIPLY));
        singleGameListView.setAdapter(mSingleGamesAdapter);
        multiGameListView.setAdapter(mMultiGamesAdapter);
    }

    private void goToChatRoomGame() {
        GAEvent.GameCenter_ClickChatRoomGame.send();
        ActionHandler.getInstance().displayBrowser(getActivity(),
            String.format(WebURL.URL_GAMES_PLAYED, Session.getInstance().getUsername()),
            String.format("Games"), R.drawable.ad_play_white);
    }

    private void goToGameDetail(GameItem data) {
        GAEvent.GameCenter_ClickGameDetail.send();
        ActionHandler.getInstance().displayGameDetailPageFragment(getActivity(),
            FragmentHandler.GameDetailPageFragmentType.Main, data.getGameId());
    }

    @Override
    public void onItemClick(View v, GameItem data) {
        goToGameDetail(data);
    }

    @Override
    public void onItemLongClick(View v, GameItem data) {
        // do nothing
    }

}
