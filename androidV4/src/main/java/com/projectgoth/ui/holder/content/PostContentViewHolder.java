package com.projectgoth.ui.holder.content;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.deezer.sdk.model.AImageOwner;
import com.projectgoth.R;
import com.projectgoth.b.data.Author;
import com.projectgoth.b.data.Post;
import com.projectgoth.b.data.mime.DeezerMimeData;
import com.projectgoth.b.data.mime.DeezerMimeData.DeezerDataType;
import com.projectgoth.b.data.mime.ImageMimeData;
import com.projectgoth.b.data.mime.MimeData;
import com.projectgoth.b.data.mime.PostMimeData;
import com.projectgoth.b.data.mime.TextRichMimeData;
import com.projectgoth.common.Config;
import com.projectgoth.common.Constants;
import com.projectgoth.common.SpannableBuilder;
import com.projectgoth.common.Tools;
import com.projectgoth.datastore.DeezerDatastore;
import com.projectgoth.datastore.PostsDatastore;
import com.projectgoth.i18n.I18n;
import com.projectgoth.imagefetcher.ImageHandler;
import com.projectgoth.music.deezer.DeezerRadio;
import com.projectgoth.ui.widget.TextViewEx;

/**
 * Created by houdangui on 2/3/15.
 */
public class PostContentViewHolder extends ContentViewHolder<PostMimeData, RelativeLayout> {

    private ImageView photo;
    private ImageView authorPic;
    private TextView authorName;
    private TextView timestamp;
    private TextViewEx postTextBody;
    private TextView seeMore;

    /**
     * A cache that can be used to fetch or put any {@link SpannableStringBuilder} when setting text.
     */
    private ConcurrentHashMap<String, SpannableStringBuilder> spannableCache = null;

    public PostContentViewHolder(Context ctx, PostMimeData mimeData) {
        super(ctx, mimeData);
    }

    @Override
    protected void initializeView() {
        photo = (ImageView) view.findViewById(R.id.post_photo);
        authorPic = (ImageView) view.findViewById(R.id.author_picture);
        authorName = (TextView) view.findViewById(R.id.author_name);
        timestamp = (TextView) view.findViewById(R.id.timestamp);
        postTextBody = (TextViewEx) view.findViewById(R.id.post_text_body);
        seeMore = (TextView) view.findViewById(R.id.see_more);
    }

    @Override
    public int getLayoutId() {
        return R.layout.content_view_post;
    }

    @Override
    public boolean applyMimeData() {
        if (mimeData != null) {
            String postId = mimeData.getId();
            Post post = PostsDatastore.getInstance().getPost(postId, false);

            if (post != null) {
                Author postAuthor = post.getAuthor();
                if (postAuthor != null) {
                    authorName.setText(postAuthor.getUsername());
                    ImageHandler.getInstance().loadDisplayPictureOfUser(authorPic, postAuthor.getUsername(),
                            postAuthor.getDisplayPictureType(), Config.getInstance().getDisplayPicSizeNormal(), true);
                }
                timestamp.setText(Tools.formatTimestampVia(post.getTimestamp(), post.getApplication()));

                final List<MimeData> mimeDataList = post.getMimeDataList();
                //get the first text mime data
                for (MimeData data : mimeDataList) {
                    if (data instanceof TextRichMimeData) {
                        TextRichMimeData textRichMimeData = (TextRichMimeData) data;
                        setupProfileTextBody(textRichMimeData.getText());
                        break;
                    } else if (data instanceof DeezerMimeData) {
                        DeezerMimeData deezerMimeData = (DeezerMimeData) data;
                        DeezerMimeData.DeezerDataType dataType = ((DeezerMimeData) data).getDataType();
                        
                        if (dataType == DeezerDataType.RADIO) {
                            long id = deezerMimeData.getLongId();
                            DeezerRadio mRadio = DeezerDatastore.getInstance().getRadio(id, false);
                            
                            if (mRadio != null) {
                                postTextBody.setText(mRadio.getTitle());
                            }
                        }

                    }
                }

                photo.setVisibility(View.GONE);
                //get the first mime data with image or thumbnail to display
                loadThumbnailFromPost(photo, mimeDataList);

                //get thumbnail from root post
                if (photo.getVisibility() == View.GONE && post.getRootPost() != null) {
                    loadThumbnailFromPost(photo, post.getRootPost().getMimeDataList());
                }

                seeMore.setText(I18n.tr("See more"));
            }

            return true;
        }

        return false;
    }

    private void loadThumbnailFromPost(ImageView photo, List<MimeData> mimeDataList) {
        for (MimeData data : mimeDataList) {
            if (data instanceof ImageMimeData) {
                ImageMimeData imageMimeData = (ImageMimeData) data;
                String thumbnailUrl = imageMimeData.getThumbnailUrl();
                if (!TextUtils.isEmpty(thumbnailUrl)) {
                    photo.setVisibility(View.VISIBLE);
                    ImageHandler.getInstance().loadImage(thumbnailUrl, photo, R.drawable.ad_loadstatic_grey);
                    break;
                }
            } else if (data instanceof DeezerMimeData) {
                DeezerMimeData deezerMimeData = (DeezerMimeData) data;
                DeezerMimeData.DeezerDataType dataType = ((DeezerMimeData) data).getDataType();
                
                if (dataType == DeezerDataType.RADIO) {
                    long id = deezerMimeData.getLongId();
                    DeezerRadio mRadio = DeezerDatastore.getInstance().getRadio(id, false);
                    if (mRadio != null) {
                        String thumbnailUrl = mRadio.getImageUrl(AImageOwner.ImageSize.big);
                        photo.setVisibility(View.VISIBLE);
                        ImageHandler.getInstance().loadImage(thumbnailUrl, photo, R.drawable.ad_loadstatic_grey);
                    }
                }
                
            }
        }
    }

    private void setSpannableCache(final ConcurrentHashMap<String, SpannableStringBuilder> spannableCache) {
        this.spannableCache = spannableCache;
    }

    @Override
    public void setParameter(final Parameter parameter, final Object value) {
        super.setParameter(parameter, value);

        switch (parameter) {
            case SPANNABLE_CACHE:
                setSpannableCache((ConcurrentHashMap<String, SpannableStringBuilder>) value);
                break;
        }
    }

    private void setupProfileTextBody(String profileTextBody) {
        SpannableStringBuilder bodySpan = null;
        postTextBody.setFullText(profileTextBody);
        if (spannableCache != null) {
            bodySpan = spannableCache.get(profileTextBody);
        }

        if (bodySpan == null) {
            if (TextUtils.isEmpty(profileTextBody)) {
                postTextBody.setText(Constants.BLANKSTR);
            } else {
                SpannableBuilder.SpannableStringBuilderEx span = postTextBody.setText(profileTextBody);
                if (spannableCache != null && span.isComplete()) {
                    spannableCache.put(profileTextBody, span);
                }
            }
        } else {
            postTextBody.setText(bodySpan);
        }
    }
}
