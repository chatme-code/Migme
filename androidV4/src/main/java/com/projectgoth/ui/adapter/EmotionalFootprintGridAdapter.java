/**
 * Copyright (c) 2013 Project Goth
 *
 * EmotionalFootprintGridAdapter.java
 * Created Aug 28, 2013, 11:01:12 AM
 */

package com.projectgoth.ui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.Tag;
import com.projectgoth.imagefetcher.ImageHandler;

import java.util.ArrayList;
import java.util.List;
/**
 * @author dangui
 * 
 */
public class EmotionalFootprintGridAdapter extends BaseAdapter {

    private List<EmotionalTag>      footprintTagList = new ArrayList<EmotionalTag>();
    private LayoutInflater          mInflater;

    private int[]                   mEmotionalIconImageResource = {R.drawable.bot_happy, R.drawable.bot_cheeky,
            R.drawable.bot_gosh, R.drawable.bot_love,
            R.drawable.bot_angry, R.drawable.bot_sad};
    private int[]                   mEmotionalIconId = {2, 3, 4, 5, 6, 7};
    private String[]                mEmotionalIconLabel = {"Happy", "Cheeky", "Gosh", "Love", "Angry", "Sad"};
    private Context                 mContext;

    /**
     * 
     */
    public EmotionalFootprintGridAdapter(Context context) {
        super();
        mInflater = LayoutInflater.from(ApplicationEx.getContext());
        mContext = context;
        initEmotionalIconList();
    }

    private void initEmotionalIconList() {
        for(int i=0; i<mEmotionalIconImageResource.length; i++) {
            EmotionalTag tag = new EmotionalTag();
            tag.id = mEmotionalIconId[i];
            tag.imageResource = mEmotionalIconImageResource[i];
            tag.lable = mEmotionalIconLabel[i];
            footprintTagList.add(tag);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.widget.Adapter#getCount()
     */
    @Override
    public int getCount() {
        if (footprintTagList != null) {
            return footprintTagList.size();
        }
        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.widget.Adapter#getItem(int)
     */
    @Override
    public Object getItem(int pos) {
        if (footprintTagList != null && pos < getCount()) {
            return footprintTagList.get(pos);
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.widget.Adapter#getItemId(int)
     */
    @Override
    public long getItemId(int pos) {
        return pos;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.widget.Adapter#getView(int, android.view.View,
     * android.view.ViewGroup)
     */
    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.emotional_footprint_icon, null);
        }

        ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
        Bitmap bitmap = BitmapFactory.decodeStream(mContext.getResources().openRawResource(footprintTagList.get(pos).imageResource));
        icon.setImageBitmap(bitmap);
        convertView.setTag(footprintTagList.get(pos).id);
        return convertView;
    }


    private class EmotionalTag {
        public int imageResource;
        public String lable;
        public int id;
    }
}
