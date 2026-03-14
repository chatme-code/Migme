
package com.projectgoth.ui.holder;

import android.content.Context;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.Author;
import com.projectgoth.b.data.Location;
import com.projectgoth.b.data.Post;
import com.projectgoth.b.data.TagEntity;
import com.projectgoth.b.data.mime.MimeData;
import com.projectgoth.b.enums.PostOriginalityEnum;
import com.projectgoth.common.Config;
import com.projectgoth.common.Logger;
import com.projectgoth.common.Tools;
import com.projectgoth.i18n.I18n;
import com.projectgoth.imagefetcher.ImageHandler;
import com.projectgoth.ui.holder.content.ContentViewFactory;
import com.projectgoth.ui.holder.content.ContentViewHolder;
import com.projectgoth.ui.holder.content.TextRichContentViewHolder;
import com.projectgoth.ui.holder.content.action.ContentViewAction;
import com.projectgoth.ui.widget.IconCounterView;
import com.projectgoth.ui.widget.IconTextView;
import com.projectgoth.ui.widget.ImageViewEx;
import com.projectgoth.ui.widget.UsernameWithLabelsView;
import com.projectgoth.util.AndroidLogger;
import com.projectgoth.util.PostUtils;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PostViewHolder.java
 *
 * @author dangui
 */

public class PostViewHolder extends BaseViewHolder<Post> {

    private static final String                               LOG_TAG = AndroidLogger.makeLogTag(PostViewHolder.class);

    protected FragmentActivity                                activity;

    private final View                                        mPostItemView;
    private final View                                        container;
    private final ImageView                                   authorPicture;
    private final UsernameWithLabelsView                      authorDetails;
    private final TextView                                    timestamp;
    private final ViewGroup                                   contentViewsContainer;
    private final ViewGroup                                   locationContainer;
    private final LinearLayout                                feedbackButtonsContainer;
    private final IconTextView                                feedbackView;
    private final IconCounterView                             replyView;
    private final IconCounterView                             reshareView;
    private final RelativeLayout                              rootContainer;
    private final ViewGroup                                   rootContentViewsContainer;
    private final ImageView                                   rootAuthorPicture;
    private final UsernameWithLabelsView                      rootUserDetails;
    private final TextView                                    rootTimestamp;
    private final ViewGroup                                   rootLocationContainer;
    private final LinearLayout                                rootReplyCounterContainer;
    private final TextView                                    rootReplyCounter;
    private final TextView                                    rootReshareCounter;
    private final ImageView                                   dot;
    private final ImageView                                   watchedPostIcon;
    private final LinearLayout                                postUploadStatus;
    private final LinearLayout                                retryRemoveAction;
    private final ImageView                                   retryAction;
    private final TextView                                    retryRemoveTitle;
    private final ImageView                                   removeAction;
    private final ImageView                                   shareButton;

    // for SPP - Single Post Page
    private final ViewGroup                                   sppLocationContainer;
    private final LinearLayout                                myEmotionalFootprintContainer;
    private final ImageView                                   myEmotionalFootprint;
    private final TextView                                    footprintCounterSpp;
    private final ImageView                                   footprintArrowSpp;

    private boolean                                           isWatchable;
    private boolean                                           isForSinglePostPage     = false;

    private final ConcurrentHashMap<String, SpannableStringBuilder> spannableCache;

    private PostViewMarker                                    promotedMarker;

    private int                                               getLayoutHeightDelay = 50;

    public PostViewHolder(final FragmentActivity activity, View view, ConcurrentHashMap<String, SpannableStringBuilder> spannableCache) {

        super(view);
        mPostItemView = view;
        this.activity = activity;
        this.spannableCache = spannableCache;

        container = view.findViewById(R.id.post_container);
        container.setBackgroundResource(R.drawable.post_container_background);

        authorPicture = (ImageView) view.findViewById(R.id.author_picture);
        authorDetails = (UsernameWithLabelsView) view.findViewById(R.id.author_details);

        timestamp = (TextView) view.findViewById(R.id.timestamp);
        locationContainer = (ViewGroup) view.findViewById(R.id.location_container);

        contentViewsContainer = (ViewGroup) view.findViewById(R.id.content_views_container);

        feedbackButtonsContainer = (LinearLayout) view.findViewById(R.id.feedback_buttons_container);

        feedbackView = (IconTextView) view.findViewById(R.id.feedback);
        replyView = (IconCounterView) view.findViewById(R.id.replies);
        reshareView = (IconCounterView) view.findViewById(R.id.reshares);

        watchedPostIcon = (ImageView) view.findViewById(R.id.watched_post_icon);

        rootContainer = (RelativeLayout) view.findViewById(R.id.root_post_container);

        rootContentViewsContainer = (ViewGroup) view.findViewById(R.id.root_content_views_container);

        rootAuthorPicture = (ImageView) view.findViewById(R.id.root_author_picture);

        rootUserDetails = (UsernameWithLabelsView) view.findViewById(R.id.root_author_details);

        rootTimestamp = (TextView) view.findViewById(R.id.root_timestamp);
        rootLocationContainer = (ViewGroup) view.findViewById(R.id.root_location_container);

        rootReplyCounterContainer = (LinearLayout) view.findViewById(R.id.root_reply_counter_container);
        rootReplyCounter = (TextView) view.findViewById(R.id.root_reply_counter);
        rootReshareCounter = (TextView) view.findViewById(R.id.root_reshare_counter);
        dot = (ImageView) view.findViewById(R.id.dot);

        postUploadStatus = (LinearLayout) view.findViewById(R.id.post_upload_status);
        retryRemoveAction = (LinearLayout) view.findViewById(R.id.retry_remove_action);
        retryAction = (ImageView) view.findViewById(R.id.retry_action);
        retryRemoveTitle = (TextView)  view.findViewById(R.id.retry_remove_name);
        removeAction = (ImageView) view.findViewById(R.id.remove_action);

        sppLocationContainer = (ViewGroup) view.findViewById(R.id.spp_location_container);

        myEmotionalFootprintContainer = (LinearLayout) view.findViewById(R.id.my_emote_footprint_container);

        myEmotionalFootprint = (ImageView) view.findViewById(R.id.my_emote_footprint);

        footprintCounterSpp = (TextView) view.findViewById(R.id.footprint_counter_spp);
        footprintArrowSpp = (ImageView) view.findViewById(R.id.set_footprint_arrow);

        promotedMarker = new PostViewMarker(view);
        promotedMarker.setTitle(I18n.tr("PROMOTED"));

        shareButton = (ImageView) view.findViewById(R.id.share);

        setViewOnClickListener(this);
    }

    private void setViewOnClickListener(View.OnClickListener onClickListener){
        container.setOnClickListener(onClickListener);
        authorPicture.setOnClickListener(onClickListener);
        authorDetails.setOnClickListener(onClickListener);
        watchedPostIcon.setOnClickListener(onClickListener);
        rootContainer.setOnClickListener(onClickListener);
        rootUserDetails.setOnClickListener(onClickListener);
        feedbackView.setOnClickListener(onClickListener);
        reshareView.setOnClickListener(onClickListener);
        replyView.setOnClickListener(onClickListener);
        myEmotionalFootprintContainer.setOnClickListener(onClickListener);
        shareButton.setOnClickListener(onClickListener);
    }

    /**
     * Set the authors details
     *
     * @param postAuthor
     */
    private void setAuthorDetails(Author postAuthor) {
        if (postAuthor != null) {
            ImageHandler.getInstance().loadDisplayPictureOfUser(authorPicture, postAuthor.getUsername(),
                    postAuthor.getDisplayPictureType(), Config.getInstance().getDisplayPicSizeNormal(), true);

            // set username
            authorDetails.setUsername(postAuthor.getUsername());

            if (postAuthor.getLabels() != null) {
                authorDetails.setLabels(postAuthor.getLabels());
            }
        }
    }

    @Override
    public void setData(final Post post) {
        super.setData(post);

        if (post == null) {
            Logger.error.logWithTrace(LOG_TAG, getClass(), "post object cannot be null");
            return;
        }

        setAuthorDetails(post.getAuthor());

        // set time stamp
        timestamp.setText(Tools.formatTimestampVia(post.getTimestamp(), post.getApplication()));

        if (!isForSinglePostPage) {
            setLocationForUI(post, locationContainer, contentViewsContainer);
        } else {
            setLocationForUI(post, sppLocationContainer, myEmotionalFootprintContainer);
        }

        populateWithContentViews(activity);

        // set footprint counter, reply counter and reshare counter
        if (!isForSinglePostPage) {
            replyView.setCounter(PostUtils.getRepliesCounter(post));
            reshareView.setCounter(PostUtils.getResharesCounter(post));
            feedbackView.setCounter(PostUtils.getFootprintCounter(post));

            // set the watch button
            if (isWatchable) {
                watchedPostIcon.setSelected(post.getIsWatching());
                watchedPostIcon.setVisibility(View.VISIBLE);
            } else {
                watchedPostIcon.setVisibility(View.GONE);
            }
        }

        TagEntity emotionalFootprints = post.getTagEntity();
        if (isForSinglePostPage) {
            // footprint counter on SPP
            footprintCounterSpp.setText(PostUtils.getFormattedFootprintCounter(post));

            // my emotional footprint if SPP.
            PostUtils.setEmotionalFootprintOnImage(emotionalFootprints, myEmotionalFootprint);
        } else {
            // Set my emotional footprint if not SPP.
            feedbackView.loadEmotionalFootprintOnPostList(
                    PostUtils.getEmotionalFootprintUrl(emotionalFootprints), R.drawable.ad_emotibot_grey);
        }

        // set the root post
        rootContainer.setVisibility(View.GONE);

        PostOriginalityEnum originality = post.getOriginality();

        if (originality == PostOriginalityEnum.REPLY || originality == PostOriginalityEnum.RESHARE) {
            final Post rootPost = post.getRootPost();
            if (rootPost != null) {

                // String rootId = rootPost.getId();
                Author rootAuthor = rootPost.getAuthor();

                if (rootAuthor != null) {
                    if (isForSinglePostPage) {
                        ImageHandler.getInstance()
                                .loadDisplayPictureOfUser(rootAuthorPicture, rootAuthor.getUsername(), rootAuthor
                                        .getDisplayPictureType(), Config.getInstance().getDisplayPicSizeSmall(), true);
                    }

                    rootUserDetails.setUsername(rootAuthor.getUsername());
                    if (rootAuthor.getLabels() != null) {
                        rootUserDetails.setLabels(rootAuthor.getLabels());
                    }

                    String strPostedTimeStampVia = Tools.formatTimestampVia(rootPost.getTimestamp(),
                            rootPost.getApplication());
                    rootTimestamp.setText(strPostedTimeStampVia);

                    setLocationForUI(rootPost, rootLocationContainer, rootContentViewsContainer);

                    populateWithContentViews(activity, true);

                    int replyCounter = PostUtils.getRepliesCounter(rootPost);
                    int reshareCounter = PostUtils.getResharesCounter(rootPost);

                    if (!isForSinglePostPage || (replyCounter == 0 && reshareCounter == 0)) {
                        rootReplyCounterContainer.setVisibility(View.GONE);
                    } else {
                        if (replyCounter == 0) {
                            rootReplyCounter.setVisibility(View.GONE);
                        } else if (replyCounter == 1) {
                            rootReplyCounter.setText(String.format(I18n.tr("%s reply"),
                                    PostUtils.getFormattedRepliesCounter(rootPost)));
                            rootReplyCounter.setVisibility(View.VISIBLE);
                        } else {
                            rootReplyCounter.setText(String.format(I18n.tr("%s replies"),
                                    PostUtils.getFormattedRepliesCounter(rootPost)));
                            rootReplyCounter.setVisibility(View.VISIBLE);
                        }

                        if (reshareCounter == 0) {
                            rootReshareCounter.setVisibility(View.GONE);
                        } else if (reshareCounter == 1) {
                            rootReshareCounter.setText(String.format(I18n.tr("%s repost"),
                                    PostUtils.getFormattedResharesCounter(rootPost)));
                            rootReshareCounter.setVisibility(View.VISIBLE);
                        } else {
                            rootReshareCounter.setText(String.format(I18n.tr("%s reposts"),
                                    PostUtils.getFormattedResharesCounter(rootPost)));
                            rootReshareCounter.setVisibility(View.VISIBLE);
                        }

                        if (rootReshareCounter.getVisibility() == View.VISIBLE
                                && rootReplyCounter.getVisibility() == View.VISIBLE) {
                            dot.setVisibility(View.VISIBLE);
                        } else {
                            dot.setVisibility(View.GONE);
                        }

                        rootReplyCounterContainer.setVisibility(View.VISIBLE);
                    }

                    rootContainer.setVisibility(View.VISIBLE);
                }
            }
        }

        if (post.isPromoted() && !isForSinglePostPage) {
            promotedMarker.show();
        } else {
            promotedMarker.hide();
        }

        if (post.isDraft()) {
            retryRemoveAction.setVisibility(View.VISIBLE);
            retryRemoveTitle.setText(I18n.tr("Try posting again"));
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    int postContainerHeight = container.getHeight();

                    postUploadStatus.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, postContainerHeight));
                }
            }, getLayoutHeightDelay);

            postUploadStatus.setVisibility(View.VISIBLE);
            setViewOnClickListener(null);
            retryAction.setOnClickListener(this);
            retryAction.setTag(retryRemoveTitle);
            removeAction.setOnClickListener(this);

        } else {
            retryRemoveAction.setVisibility(View.GONE);
            postUploadStatus.setLayoutParams(new RelativeLayout.LayoutParams(0, 0));
            postUploadStatus.setVisibility(View.GONE);
            setViewOnClickListener(this);
        }
    }

    private static void setLocationForUI(final Post post, final ViewGroup container, final View belowView) {
        if (post != null && container != null && Config.getInstance().isLocationInPostEnabled()) {
            container.setVisibility(View.GONE);
            final Location location = post.getLocation();
            if (location != null) {
                final String displayName = location.getDisplayName();
                if (!TextUtils.isEmpty(displayName)) {
                    final TextView txtLocation = (TextView) container.findViewById(R.id.txt_location);
                    if (txtLocation != null) {
                        txtLocation.setText(displayName);
                        container.setVisibility(View.VISIBLE);
                        if (belowView != null) {
                            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) belowView.getLayoutParams();
                            if (params != null) {
                                params.addRule(RelativeLayout.BELOW, container.getId());
                                belowView.setLayoutParams(params);
                            }
                        }
                    }
                }
            }
        }
    }

    public void setWatchable(boolean watchable) {
        isWatchable = watchable;
    }

    public void setForSinglePostPage() {
        this.isForSinglePostPage = true;
        
        container.setBackgroundColor(ApplicationEx.getColor(R.color.white));

        container.setPadding(container.getPaddingLeft(), container.getPaddingTop(), container.getPaddingRight(),
                ApplicationEx.getDimension(R.dimen.medium_padding));

        feedbackButtonsContainer.setVisibility(View.GONE);
        myEmotionalFootprintContainer.setVisibility(View.GONE);

        rootAuthorPicture.setVisibility(View.VISIBLE);
        
    }

    public void setEmotionalFootprintsSelected(boolean selected) {
        myEmotionalFootprint.setSelected(selected);
        footprintCounterSpp.setSelected(selected);
        footprintArrowSpp.setSelected(selected);
    }

    private void populateWithContentViews(final Context ctx) {
        populateWithContentViews(ctx, false);
    }

    private void populateWithContentViews(final Context ctx, boolean isRootPost) {

        ViewGroup viewsContainer = isRootPost ? rootContentViewsContainer : contentViewsContainer;
        Post post = isRootPost ? data.getRootPost() : data;

        // Remove all views from the contentViewsContainer.
        viewsContainer.removeAllViews();

        final List<MimeData> mimeDataList = post.getMimeDataList();
        for (final MimeData mimeData : mimeDataList) {
            final ContentViewHolder<? extends MimeData, ? extends View> contentViewHolder = applyMimeDataToHolder(ctx, mimeData, isRootPost);
            if (contentViewHolder != null) {
                final View contentView = contentViewHolder.getContentView();
                viewsContainer.addView(contentView);

                //For gift recipient list, need to know where the content view holder is.
                if (isForSinglePostPage && contentViewHolder instanceof TextRichContentViewHolder) {
                    if (isRootPost) {
                        mPostItemView.setTag(R.id.root_content_views_container, viewsContainer);
                    } else {
                        mPostItemView.setTag(R.id.content_views_container, viewsContainer);
                    }
                }

            }
        }
    }

    protected ContentViewHolder<? extends MimeData, ? extends View> applyMimeDataToHolder(final Context ctx, final MimeData mimeData, boolean isRootPost) {
        final ContentViewHolder<? extends MimeData, ? extends View> contentViewHolder =
                ContentViewFactory.createContentViewHolder(ctx, mimeData);
        if (contentViewHolder != null) {
            final ContentViewAction<? extends ContentViewHolder<? extends MimeData, ? extends View>> contentViewAction =
                    ContentViewFactory.createContentViewAction(contentViewHolder);
            if (contentViewAction != null) {
                setParametersForContentViewAction(contentViewAction, isRootPost);
                contentViewAction.setExternalActionListener(this);
                contentViewAction.applyToView();
            }

            setParametersForContentViewHolder(contentViewHolder, isRootPost);

            if (contentViewHolder.applyMimeData()) {
                /*final View contentView = contentViewHolder.getContentView();
                if (contentViewAction == null) {
                    contentView.setOnClickListener(this);
                }*/
                return contentViewHolder;
            } else {
                Logger.error.log(LOG_TAG,
                        "Failed to apply mimeData of type: ", mimeData.getClass(),
                        " to content view holder of type: ", contentViewHolder.getClass());
            }
        }

        return null;
    }


    protected void setParametersForContentViewHolder(final ContentViewHolder<? extends MimeData, ? extends View> contentViewHolder
        , boolean isRootPost) {
        contentViewHolder.setParameter(ContentViewHolder.Parameter.SPANNABLE_CACHE, spannableCache);
        contentViewHolder.setParameter(ContentViewHolder.Parameter.DECODE_HTML_TEXT, new Boolean(true));
        contentViewHolder.setParameter(ContentViewHolder.Parameter.IS_ROOT_DATA_CONTENT, new Boolean(isRootPost));
        contentViewHolder.setParameter(ContentViewHolder.Parameter.IMAGE_TYPE, ImageViewEx.ImageType.BLOG_IMAGE);
        //set padding
        int[] paddings = new int[] {0, ApplicationEx.getDimension(R.dimen.small_padding),
                0, ApplicationEx.getDimension(R.dimen.small_padding)};
        contentViewHolder.setParameter(ContentViewHolder.Parameter.PADDING, paddings);
        //text size
        int textSize = getPostBodyTextSize(isRootPost, isForSinglePostPage);
        contentViewHolder.setParameter(ContentViewHolder.Parameter.TEXT_SIZE, textSize);

        //set the post originality for setting a prefix of the post body
        Post post = isRootPost ? data.getRootPost() : data;
        PostOriginalityEnum originality = post.getOriginality();
        contentViewHolder.setParameter(ContentViewHolder.Parameter.POST_ORIGINALITY, originality);
        contentViewHolder.setParameter(ContentViewHolder.Parameter.NEED_POST_BODY_REFIX, true);

        //we don't display thumbnail for post photo
        contentViewHolder.setParameter(ContentViewHolder.Parameter.DISPLAY_THUMBNAIL, false);

        contentViewHolder.setParameter(ContentViewHolder.Parameter.TRUNCATE_LONG_POST, !isForSinglePostPage);

    }

    private int getPostBodyTextSize(boolean isRootPost, boolean isForSinglePostPage) {
        int textSize;
        if (isRootPost) {
            textSize = isForSinglePostPage ? ApplicationEx.getDimension(R.dimen.text_size_large)
                    : ApplicationEx.getDimension(R.dimen.text_size_medium);
        } else {
            textSize = isForSinglePostPage ? ApplicationEx.getDimension(R.dimen.text_size_xlarge)
                    : ApplicationEx.getDimension(R.dimen.text_size_large);
        }
        return textSize;
    }

    protected void setParametersForContentViewAction(final ContentViewAction<? extends ContentViewHolder<? extends MimeData, ? extends View>> contentViewAction,
                                                     boolean isRootPost) {
        contentViewAction.setParameter(ContentViewAction.Parameter.ACTIVITY, activity);

        Post post = isRootPost ? data.getRootPost() : data;
        contentViewAction.setParameter(ContentViewAction.Parameter.SENDER, PostUtils.getPostAuthorUsername(post));

        if (post.isDraft()) {
            contentViewAction.setParameter(ContentViewAction.Parameter.NO_ACTION, true);
        }
    }

}
