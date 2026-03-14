package com.projectgoth.ui.holder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.projectgoth.R;
import com.projectgoth.b.data.MusicGenreData;
import com.projectgoth.i18n.I18n;

/**
 * Created by mapet on 21/4/15.
 */
public class MusicGenreFilterViewHolder extends BaseViewHolder<MusicGenreData> {

    private TextView mFilterName;
    private ImageView mFilterCheck;
    private int mSelectedGenreId;

    public MusicGenreFilterViewHolder(View view) {
        super(view);
        mFilterName = (TextView) view.findViewById(R.id.name);
        mFilterCheck = (ImageView) view.findViewById(R.id.check);
    }

    @Override
    public void setData(final MusicGenreData data) {
        super.setData(data);

        mFilterName.setText(I18n.tr(data.getTitle()));
        if (data.getId() == mSelectedGenreId) {
            mFilterName.setTextColor(mFilterName.getResources().getColor(R.color.default_green));
            mFilterCheck.setVisibility(View.VISIBLE);
        } else {
            mFilterName.setTextColor(mFilterName.getResources().getColor(R.color.default_text));
            mFilterCheck.setVisibility(View.GONE);
        }
    }

    public void setSelectedGenreId(int selectedGenreId) {
        mSelectedGenreId = selectedGenreId;
    }
}
