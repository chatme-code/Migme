package com.projectgoth.ui.holder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.projectgoth.R;
import com.projectgoth.b.data.GiftCategoryItem;
import com.projectgoth.datastore.GiftsDatastore;

/**
 * Created by lopenny on 1/27/15.
 */
public class MyGiftsCategoryListViewHolder extends BaseViewHolder<GiftCategoryItem> {

    private TextView mCategoryTitle;
    private ImageView mCheckIcon;

    public MyGiftsCategoryListViewHolder(View view) {
        super(view);

        mCategoryTitle = (TextView) view.findViewById(R.id.name);
        mCheckIcon = (ImageView) view.findViewById(R.id.check);
    }

    public void setData(int position, GiftCategoryItem data, boolean selected) {
        super.setData(position, data);

        mCategoryTitle.setText(GiftsDatastore.Category.fromType(data.getTitle()));

        if (selected) {
            mCheckIcon.setVisibility(View.VISIBLE);
        } else {
            mCheckIcon.setVisibility(View.INVISIBLE);
        }
    }
}
