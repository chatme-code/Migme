/**
 * Copyright (c) 2013 Project Goth
 *
 * GameListAdapter.java
 * Created Jan 21, 2015, 3:12:16 PM
 */

package com.projectgoth.ui.adapter;

import java.util.ArrayList;
import java.util.List;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.GameItem;
import com.projectgoth.ui.holder.BaseViewHolder.BaseViewListener;
import com.projectgoth.ui.holder.GameItemViewHolder;

public class GameListAdapter extends BaseAdapter{
    
    List<GameItem>                      mGameListData         = new ArrayList<GameItem>();
    
    private LayoutInflater              mInflater;
    private BaseViewListener<GameItem>  gameClickListener;

    public GameListAdapter(){
        super();
        mInflater = LayoutInflater.from(ApplicationEx.getContext());
        
    }

    @Override
    public int getCount() {
        return mGameListData.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        GameItemViewHolder gameItemViewHolder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.holder_game_item, null);
            gameItemViewHolder = new GameItemViewHolder(convertView);
            convertView.setTag(R.id.holder, gameItemViewHolder);
        } else {
            gameItemViewHolder = (GameItemViewHolder) convertView.getTag(R.id.holder);
        }
        
        GameItem game = mGameListData.get(position);
        gameItemViewHolder.setData(game);
        gameItemViewHolder.setBaseViewListener(gameClickListener);
        
        return convertView;
    }
    
    public void setGameList(List<GameItem> games){
        mGameListData = games;
    }
    
    public void setGameClickListener(BaseViewListener<GameItem> listener) {
        this.gameClickListener = listener;
    }


}
