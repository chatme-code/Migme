/**
 * Copyright (c) 2013 Project Goth
 *
 * GiftCategoryViewHolder.java
 * Created 27 May, 2014, 9:42:07 am
 */

package com.projectgoth.ui.holder;

import android.view.View;
import android.widget.TextView;

import com.projectgoth.R;
import com.projectgoth.b.data.StoreCategory;
import com.projectgoth.i18n.I18n;

/**
 * @author Dan
 * 
 */
public class GiftCategoryViewHolder extends BaseViewHolder<StoreCategory> {

    private final TextView categoryName;
    private final TextView giftCategoryNum;

    public GiftCategoryViewHolder(View rootView) {
        super(rootView);

        categoryName = (TextView) rootView.findViewById(R.id.category_name);
        giftCategoryNum = (TextView) rootView.findViewById(R.id.category_gift_number);
    }

    @Override
    public void setData(StoreCategory data) {
        super.setData(data);

        categoryName.setText(I18n.tr(data.getName()));
        String strNum = String.format("(%d)", data.getTotalItems());
        giftCategoryNum.setText(strNum);
    }

}
