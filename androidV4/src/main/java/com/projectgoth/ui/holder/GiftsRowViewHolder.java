/**
 * Copyright (c) 2013 Project Goth
 *
 * GiftsRowViewHolder.java
 * Created 9 May, 2014, 2:17:13 pm
 */

package com.projectgoth.ui.holder;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.StoreItem;


public class GiftsRowViewHolder extends BaseViewHolder<StoreItem[]> {

    private final LinearLayout container;
    private final int columnNum;
    BaseViewListener<StoreItem> giftViewListener;
    
    public GiftsRowViewHolder(View rootView, int columnNum) {
        super(rootView);
        this.columnNum = columnNum;
        container = (LinearLayout) rootView.findViewById(R.id.gifts_container);
        addChildViews();
    }

    private void addChildViews() {
        LayoutInflater inflater = LayoutInflater.from(ApplicationEx.getContext());
        for (int i = 0; i < columnNum; i++) {
            View giftItemView = inflater.inflate(R.layout.holder_gift_item, null);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0,
                    LayoutParams.WRAP_CONTENT, 1);
            container.addView(giftItemView, params);
        }
    }

    @Override
    public void setData(StoreItem[] data) {
        super.setData(data);
        for (int i = 0; i < columnNum; i++) {
            View giftItemView = container.getChildAt(i);
            
            StoreItem item = null;
            if (data != null && i < data.length) {
                item = data[i];
            }
            
            if (giftItemView == null) {
              return;
            } 
            
            if (item != null) {
                GiftViewHolder viewHolder = new GiftViewHolder(giftItemView);
                viewHolder.setGiftingInChat(true);
                viewHolder.setBaseViewListener(giftViewListener);
                viewHolder.setData(item);
                giftItemView.setVisibility(View.VISIBLE);
            } else {
                giftItemView.setVisibility(View.INVISIBLE);
            }
        }
    }

    /**
     * @param giftViewListener
     */
    public void setGiftViewListener(BaseViewListener<StoreItem> giftViewListener) {
        this.giftViewListener = giftViewListener;
    }
    
}
