
package com.projectgoth.ui.holder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.projectgoth.R;
import com.projectgoth.controller.StoreController;
import com.projectgoth.i18n.I18n;
import com.projectgoth.model.StoreFilterItem;

public class FilterListViewHolder extends BaseViewHolder<StoreFilterItem> {

    private TextView  filterName;
    private ImageView filterCheck;
    private ImageView filterRibbon;

    public FilterListViewHolder(View view) {
        super(view);
        filterName = (TextView) view.findViewById(R.id.name);
        filterCheck = (ImageView) view.findViewById(R.id.check);
        filterRibbon = (ImageView) view.findViewById(R.id.filter_ribbon);
    }

    @Override
    public void setData(StoreFilterItem data) {
        super.setData(data);

        filterName.setText(I18n.tr(data.getName()));
        if (data.isSelected()) {
            filterName.setTextColor(filterName.getResources().getColor(R.color.default_green));
            filterCheck.setVisibility(View.VISIBLE);
        } else {
            filterName.setTextColor(filterName.getResources().getColor(R.color.light_text_color));
            filterCheck.setVisibility(View.GONE);
        }

        if (data.getId() == null) {
            filterRibbon.setImageResource(android.R.color.transparent);
        } else {
            if (data.getId().intValue() == StoreController.featuredCategoryId) {
                filterRibbon.setImageResource(R.drawable.ad_featured_ribbon);
            } else if (data.getId().intValue() == StoreController.newCategoryId) {
                filterRibbon.setImageResource(R.drawable.ad_new_ribbon);
            } else {
                filterRibbon.setImageResource(android.R.color.transparent);
            }
        }
    }

}
