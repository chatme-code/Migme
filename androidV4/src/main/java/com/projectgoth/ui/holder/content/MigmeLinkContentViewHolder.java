package com.projectgoth.ui.holder.content;

import android.content.Context;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.projectgoth.R;
import com.projectgoth.b.data.mime.MigmeLinkMimeData;
import com.projectgoth.imagefetcher.ImageHandler;

/**
 * Created by houdangui on 11/2/15.
 */
public class MigmeLinkContentViewHolder extends ContentViewHolder<MigmeLinkMimeData, RelativeLayout> {

    private ImageView thumbnail;
    private TextView title;
    private TextView link;
    private TextView description;

    public MigmeLinkContentViewHolder(Context ctx, MigmeLinkMimeData mimeData) {
        super(ctx, mimeData);
    }

    @Override
    public int getLayoutId() {
        return R.layout.content_view_migme_link;
    }

    @Override
    protected void initializeView() {
        thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
        title = (TextView) view.findViewById(R.id.title);
        link = (TextView) view.findViewById(R.id.link);
        description = (TextView) view.findViewById(R.id.description);
    }

    @Override
    public boolean applyMimeData() {
        if (mimeData != null) {
            ImageHandler.getInstance().loadImage(mimeData.getImage(), thumbnail, R.drawable.ad_loadstatic_grey);
            title.setText(mimeData.getTitle());
            link.setText(mimeData.getUrl());
            description.setText(mimeData.getDescription());

            return true;
        }
        return false;
    }
}
