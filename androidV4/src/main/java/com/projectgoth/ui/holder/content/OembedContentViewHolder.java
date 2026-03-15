package com.projectgoth.ui.holder.content;

import android.content.Context;
import android.net.Uri;

import com.projectgoth.R;
import com.projectgoth.b.data.mime.OembedMimeData;
import com.projectgoth.common.Constants;
import com.projectgoth.common.TextUtils;
import com.projectgoth.datastore.MimeDatastore;
import com.projectgoth.imagefetcher.ImageHandler;
import com.projectgoth.ui.widget.ImageViewEx;

/**
 * Created by houdangui on 9/2/15.
 */
public class OembedContentViewHolder extends BaseImageContentViewHolder<OembedMimeData> {

    /**
     * Constructor.
     * @param ctx       The {@link android.content.Context} to be used for inflation.
     * @param mimeData  The {@link com.projectgoth.b.data.mime.OembedMimeData} to be used as data for this holder.
     */
    public OembedContentViewHolder(Context ctx, OembedMimeData mimeData) {
        super(ctx, mimeData, ImageViewEx.IconOverlay.PLAY);
    }

    @Override
    public boolean applyMimeData() {
        if (mimeData != null) {
            String thumbnailURL = Constants.BLANKSTR;

            if (TextUtils.isEmpty(mimeData.getThumbnailUrl())) {
                String url = mimeData.getUrl();

                //for pin message from web
                if (url.contains("clip_id")) {
                    Uri uri = Uri.parse(url);
                    String clipId = uri.getQueryParameter("clip_id");
                    url = String.format("https://vimeo.com/channels/staffpicks/%s", clipId);
                }

                OembedMimeData oembedMimeData = (OembedMimeData) MimeDatastore.getInstance().getOembedMimeData(url, mimeData.getMimeType());
                if (oembedMimeData != null) {
                    thumbnailURL = oembedMimeData.getThumbnailUrl();
                }
            } else {
                thumbnailURL = mimeData.getThumbnailUrl();
            }

            int placeHolderResId = R.drawable.ad_gallery_grey;
            view.setTag(R.id.overlay_icon, ImageViewEx.IconOverlay.PLAY);
            ImageHandler.getInstance().loadImage(thumbnailURL, view, placeHolderResId);
            return true;
        }
        return false;
    }

    @Override
    protected boolean isPlayableThumbnail() {
        return true;
    }

}