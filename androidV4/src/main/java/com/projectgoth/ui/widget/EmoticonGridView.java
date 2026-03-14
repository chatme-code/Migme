/**
 * Copyright (c) 2013 Project Goth
 *
 * EmoticonGridView.java
 * Created Oct 8, 2014, 12:08:32 AM
 */

package com.projectgoth.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.TagEntity;
import com.projectgoth.b.data.UserTagId;
import com.projectgoth.b.enums.TaggingCriteriaTypeEnum;
import com.projectgoth.ui.holder.BaseViewHolder.BaseViewListener;
import com.projectgoth.ui.holder.EmoteViewHolder;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author warrenbalcos
 * 
 */
public class EmoticonGridView extends LinearLayout {

    private static final int     NUMBER_OF_ROWS = 5;

    private TagEntity            data;

    private ArrayList<UserTagId> list           = new ArrayList<UserTagId>();

    private OnItemClickListener  listener;

    public interface OnItemClickListener {

        public void onItemClick(UserTagId data);
    }

    /**
     * @param context
     */
    public EmoticonGridView(Context context) {
        super(context);
    }

    /**
     * @param context
     * @param attrs
     */
    public EmoticonGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * @param data
     *            the data to set
     */
    public void setData(TagEntity data) {
        this.data = data;
        if (data != null) {
            list = new ArrayList<UserTagId>(Arrays.asList(data.getUsernames()));
        } else {
            list = new ArrayList<UserTagId>();
        }
        resetData();
    }

    private void resetData() {

        removeAllViews();

        Integer criteriaId = (data != null) ? data.getCriteriaId() : TaggingCriteriaTypeEnum.DEFAULT_CRITERIA.value();

        LinearLayout.LayoutParams params = null;

        int lenght = list.size();

        for (int i = 0; i < lenght; i += NUMBER_OF_ROWS) {

            params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            int margin = ApplicationEx.getDimension(R.dimen.medium_margin);
            params.setMargins(margin, margin, margin, margin);

            LinearLayout row = new LinearLayout(getContext());
            row.setLayoutParams(params);
            row.setGravity(Gravity.CENTER_HORIZONTAL);

            params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            params.weight = 1;
            for (int x = 0; x < NUMBER_OF_ROWS; x++) {
                UserTagId tagId = null;
                int tagIndex = x + i;
                if (tagIndex < lenght) {
                    tagId = list.get(tagIndex);
                }
                View view = getEmoticonView(tagId, criteriaId);
                if (tagId == null) {
                    view.setVisibility(View.INVISIBLE);
                }
                view.setLayoutParams(params);
                view.setFocusable(true);
                row.addView(view);
            }
            addView(row);
        }
    }

    private View getEmoticonView(final UserTagId userTag, Integer criteriaId) {

        LayoutInflater inflater = LayoutInflater.from(ApplicationEx.getContext());
        View rootView = (RelativeLayout) inflater.inflate(R.layout.emotional_footprint_item, null);

        EmoteViewHolder holder = new EmoteViewHolder(rootView, (criteriaId == null) ? 0 : criteriaId);
        holder.setData(userTag);

        holder.setBaseViewListener(new BaseViewListener<UserTagId>() {

            @Override
            public void onItemClick(View v, UserTagId data) {
                if (listener != null) {
                    listener.onItemClick(data);
                }
            }

            @Override
            public void onItemLongClick(View v, UserTagId data) {
                // Ignore
            }
        });

        return rootView;
    }

    /**
     * @return the listener
     */
    public OnItemClickListener getListener() {
        return listener;
    }

    /**
     * @param listener
     *            the listener to set
     */
    public void setListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

}
