package com.projectgoth.ui.holder;

import android.view.View;
import android.widget.TextView;
import com.projectgoth.R;
import com.projectgoth.model.StoreSearchCategory;

/**
 * Created by houdangui on 9/12/14.
 */
public class StoreSearchGroupViewHolder extends BaseViewHolder<StoreSearchCategory> {

    private TextView searchCategoryName;
    private TextView searchResultNum;

    public StoreSearchGroupViewHolder(View rootView) {
        super(rootView, false);

        searchCategoryName = (TextView) rootView.findViewById(R.id.store_category_name);
        searchResultNum = (TextView) rootView.findViewById(R.id.store_category_num);
    }

    @Override
    public void setData(StoreSearchCategory data) {
        super.setData(data);

        searchCategoryName.setText(data.getLabel());

        int count = data.getTotalNum();
        if (count > 0 || data.hasNoResult()) {
            searchResultNum.setText("(" + count + ")");
            searchResultNum.setVisibility(View.VISIBLE);
        } else {
            searchResultNum.setVisibility(View.GONE);
        }
    }

}
