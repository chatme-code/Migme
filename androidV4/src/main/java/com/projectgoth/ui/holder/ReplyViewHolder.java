/**
 * Copyright (c) 2013 Project Goth
 *
 * ReplyViewHolder.java
 * Created Jul 25, 2013, 6:05:21 PM
 */

package com.projectgoth.ui.holder;

import android.support.v4.app.FragmentActivity;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.Post;
import com.projectgoth.b.data.mime.MimeData;
import com.projectgoth.b.enums.PostOriginalityEnum;
import com.projectgoth.common.Config;
import com.projectgoth.common.Theme;
import com.projectgoth.common.ThemeValues;
import com.projectgoth.common.TimeAgo;
import com.projectgoth.imagefetcher.ImageHandler;
import com.projectgoth.ui.holder.content.ContentViewHolder;
import com.projectgoth.ui.holder.content.action.ContentViewAction;
import com.projectgoth.ui.widget.ClickableSpanEx.ClickableSpanExListener;
import com.projectgoth.ui.widget.ImageViewEx;
import com.projectgoth.ui.widget.UsernameWithLabelsView;
import com.projectgoth.util.PostUtils;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author dangui
 * 
 */
public class ReplyViewHolder extends BaseViewHolder<Post> {

    protected FragmentActivity              mActivity;
    
    private final ImageView                 mUserPicture;
    private final UsernameWithLabelsView    mAuthorDetails;
    private final ViewGroup                 mContentViewsContainer;
    private final TextView                  mTimeAgo;
    private final View                      mSeparator;

    private boolean                         mShowTimeAgo             = true;
    private boolean                         mShowSeperator           = true;
    private boolean                         mReplyOrReshareToOwnPost = false;

    private final ConcurrentHashMap<String, SpannableStringBuilder> spannableCache;

    public ReplyViewHolder(final FragmentActivity activity, View view, ConcurrentHashMap<String, SpannableStringBuilder> spannableCache,
            ClickableSpanExListener spanListener) {
        super(view);
        this.mActivity = activity;

        this.spannableCache = spannableCache;

        mUserPicture = (ImageView) view.findViewById(R.id.reply_author_pic);
        mAuthorDetails = (UsernameWithLabelsView) view.findViewById(R.id.reply_author_details);
        mContentViewsContainer = (ViewGroup) view.findViewById(R.id.content_views_container);
        mTimeAgo = (TextView) view.findViewById(R.id.reply_timestamp);
        mSeparator = view.findViewById(R.id.reply_separator);
        mSeparator.setBackgroundColor(Theme.getColor(ThemeValues.SPP_REPLY_SEPARATOR_COLOR));

        mUserPicture.setOnClickListener(this);
        mAuthorDetails.setOnClickListener(this);
        mTimeAgo.setOnClickListener(this);
    }

    @Override
    public void setData(Post data) {
        super.setData(data);

        ImageHandler.getInstance().loadDisplayPictureOfUser(mUserPicture, data.getAuthor().getUsername(), data.getAuthor()
                .getDisplayPictureType(), Config.getInstance().getDisplayPicSizeSmall(), true);

        mAuthorDetails.setUsername(data.getAuthor().getUsername());
        mAuthorDetails.setLabels(data.getAuthor().getLabels());

        populateWithContentViews();

        if (mShowTimeAgo) {
            String timeStamp = TimeAgo.format(data.getTimestamp());
            mTimeAgo.setText(timeStamp);
            mTimeAgo.setVisibility(View.VISIBLE);
        } else {
            mTimeAgo.setVisibility(View.GONE);
        }

        if (mShowSeperator) {
            mSeparator.setVisibility(View.VISIBLE);
        } else {
            mSeparator.setVisibility(View.GONE);
        }
    }

    /**
     * @return the replyOrReshareToOwnPost
     */
    public boolean isReplyOrReshareToOwnPost() {
        return mReplyOrReshareToOwnPost;
    }

    /**
     * @param replyOrReshareToOwnPost the replyOrReshareToOwnPost to set
     */
    public void setReplyOrReshareToOwnPost(boolean replyOrReshareToOwnPost) {
        this.mReplyOrReshareToOwnPost = replyOrReshareToOwnPost;
    }

    public void setShowTimeAgo(boolean showTimeAgo) {
        this.mShowTimeAgo = showTimeAgo;
    }

    public void setShowSeperator(boolean showSeperator) {
        this.mShowSeperator = showSeperator;
    }

    private void populateWithContentViews() {

        // Remove all views from the contentViewsContainer.
        mContentViewsContainer.removeAllViews();

        final List<MimeData> mimeDataList = data.getMimeDataList();
        for (final MimeData mimeData : mimeDataList) {
            final ContentViewHolder<? extends MimeData, ? extends View> contentViewHolder = applyMimeDataToHolder(mActivity, mimeData);
            if (contentViewHolder != null) {
                final View contentView = contentViewHolder.getContentView();
                mContentViewsContainer.addView(contentView);
            }
        }
    }

    @Override
    protected void setParametersForContentViewHolder(final ContentViewHolder<? extends MimeData, ? extends View> contentViewHolder) {
        contentViewHolder.setParameter(ContentViewHolder.Parameter.SPANNABLE_CACHE, spannableCache);
        contentViewHolder.setParameter(ContentViewHolder.Parameter.DECODE_HTML_TEXT, new Boolean(true));
        contentViewHolder.setParameter(ContentViewHolder.Parameter.IMAGE_TYPE, ImageViewEx.ImageType.BLOG_IMAGE);
        //text size
        int textSize = ApplicationEx.getDimension(R.dimen.text_size_medium);
        contentViewHolder.setParameter(ContentViewHolder.Parameter.TEXT_SIZE, textSize);
        //padding
        int[] paddings = new int[]{0, ApplicationEx.getDimension(R.dimen.xsmall_padding),
                0, ApplicationEx.getDimension(R.dimen.xsmall_padding)};
        contentViewHolder.setParameter(ContentViewHolder.Parameter.PADDING, paddings);

        //set the post originality for setting a prefix of the post body
        PostOriginalityEnum originality = data.getOriginality();
        contentViewHolder.setParameter(ContentViewHolder.Parameter.POST_ORIGINALITY, originality);
        //no need reply body prefix here
        if (originality != PostOriginalityEnum.REPLY) {
            contentViewHolder.setParameter(ContentViewHolder.Parameter.NEED_POST_BODY_REFIX, true);
        }

        //we don't display thumbnail for post photo
        contentViewHolder.setParameter(ContentViewHolder.Parameter.DISPLAY_THUMBNAIL, false);
    }

    @Override
    protected void setParametersForContentViewAction(final ContentViewAction<? extends ContentViewHolder<? extends MimeData, ? extends View>> contentViewAction) {
        contentViewAction.setParameter(ContentViewAction.Parameter.ACTIVITY, mActivity);

        contentViewAction.setParameter(ContentViewAction.Parameter.SENDER, PostUtils.getPostAuthorUsername(data));
    }
}
