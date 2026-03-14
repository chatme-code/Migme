package com.projectgoth.ui.adapter;

import java.util.ArrayList;

import android.content.Context;
import com.projectgoth.b.data.GameItem;
import com.projectgoth.ui.activity.FragmentHandler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

/**
 * Created by danielchen on 15/1/23.
 */
public class GameDetailPagerAdapter extends BasePagerAdapter {
    private final int mTotalFragments = 1;
    private GameItem gameItem;


    public GameDetailPagerAdapter(FragmentManager fm, Context context, GameItem gameItem) {
        super(fm, context);
        this.gameItem = gameItem;
    }

    @Override
    protected ArrayList createItemList() {
        return null;
    }

    @Override
    public void onPositionChanged(int newPosition) {

    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = FragmentHandler.getInstance().getGameDetailPageFragment(
                FragmentHandler.GameDetailPageFragmentType.Information, gameItem.getGameId());
        return fragment;
    }

    @Override
    public int getCount() {
        return mTotalFragments;
    }
}
