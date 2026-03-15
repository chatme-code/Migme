package com.projectgoth.ui.holder.content;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.projectgoth.R;
import com.projectgoth.b.data.MusicItem;
import com.projectgoth.datastore.MusicDatastore;
import com.projectgoth.datastore.MusicDatastore.MusicProviderType;
import com.projectgoth.imagefetcher.ImageHandler;
import com.projectgoth.ui.holder.BaseViewHolder;

/**
 * Created by mapet on 17/4/15.
 */
public class NewMusicViewHolder extends BaseViewHolder<MusicItem> {

    private TextView mChannelLabel;
    private ImageView mChannelImage;
    private ImageView mChannelSource;
    private ImageView mFavorite;
    private boolean mShouldShowSource;


    public NewMusicViewHolder(View rootView, boolean shouldShowSource) {
        super(rootView);

        mShouldShowSource = shouldShowSource;
        mChannelLabel = (TextView) rootView.findViewById(R.id.channel_label);
        mChannelImage = (ImageView) rootView.findViewById(R.id.channel_image);
        mChannelSource = (ImageView) rootView.findViewById(R.id.channel_source_image);
        mFavorite = (ImageView) rootView.findViewById(R.id.channel_favorite);
    }

    @Override
    public void setData(MusicItem data) {
        super.setData(data);

        mFavorite.setVisibility(View.GONE);
        mChannelLabel.setText(data.getTitle());
        ImageHandler.getInstance().loadImageFromUrl(mChannelImage, data.getPicture(), true, R.drawable.ic_default_cover_loading);

        if (mShouldShowSource) {
            mChannelSource.setVisibility(View.VISIBLE);
        } else {
            mChannelSource.setVisibility(View.GONE);
        }

        if (MusicDatastore.getInstance().isFavoriteMusicChannel(MusicProviderType.deezer, data)) {
            mFavorite.setVisibility(View.VISIBLE);
        }

    }

}
