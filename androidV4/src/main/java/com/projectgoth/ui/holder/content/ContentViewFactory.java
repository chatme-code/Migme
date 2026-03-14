/**
 * Copyright (c) 2013 Project Goth
 *
 * ContentViewHolderFactory.java
 * Created Dec 2, 2014, 1:09:25 PM
 */

package com.projectgoth.ui.holder.content;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.projectgoth.b.data.mime.ChatroomMimeData;
import com.projectgoth.b.data.mime.DeezerMimeData;
import com.projectgoth.b.data.mime.EmoteMimeData;
import com.projectgoth.b.data.mime.FlickrMimeData;
import com.projectgoth.b.data.mime.GiftMimeData;
import com.projectgoth.b.data.mime.ImageMimeData;
import com.projectgoth.b.data.mime.MigmeLinkMimeData;
import com.projectgoth.b.data.mime.MimeData;
import com.projectgoth.b.data.mime.OembedMimeData;
import com.projectgoth.b.data.mime.PostMimeData;
import com.projectgoth.b.data.mime.ProfileMimeData;
import com.projectgoth.b.data.mime.StickerMimeData;
import com.projectgoth.b.data.mime.SystemChatroomParticipantEnterData;
import com.projectgoth.b.data.mime.SystemChatroomParticipantExitData;
import com.projectgoth.b.data.mime.TextPlainMimeData;
import com.projectgoth.b.data.mime.TextRichMimeData;
import com.projectgoth.b.data.mime.YoutubeMimeData;
import com.projectgoth.common.Logger;
import com.projectgoth.ui.holder.content.action.ChatroomContentViewAction;
import com.projectgoth.ui.holder.content.action.ContentViewAction;
import com.projectgoth.ui.holder.content.action.DeezerContentViewAction;
import com.projectgoth.ui.holder.content.action.FlickrContentViewAction;
import com.projectgoth.ui.holder.content.action.ImageContentViewAction;
import com.projectgoth.ui.holder.content.action.MigmeLinkContentViewAction;
import com.projectgoth.ui.holder.content.action.OembedContentViewAction;
import com.projectgoth.ui.holder.content.action.PostContentViewAction;
import com.projectgoth.ui.holder.content.action.ProfileContentViewAction;
import com.projectgoth.ui.holder.content.action.TextRichContentViewAction;
import com.projectgoth.ui.holder.content.action.YoutubeContentViewAction;
import com.projectgoth.ui.widget.ImageViewEx;
import com.projectgoth.ui.widget.TextViewEx;
import com.projectgoth.util.AndroidLogger;

import java.util.HashMap;
import java.util.Map;


/**
 * Represents a factory class for retrieving {@link ContentViewHolder} and associated {@link ContentViewAction}. 
 * @author angelorohit
 */
public abstract class ContentViewFactory {
    private final static String LOG_TAG = AndroidLogger.makeLogTag(ContentViewHolder.class);   
    
    /**
     * A map that associates {@link MimeData} class types with {@link ContentViewData}.
     * The {@link ContentViewData} associates {@link ContentViewHolder} with {@link ContentViewAction} and preserves 
     * type safety.
     * 
     * The {@link ContentViewHolder} type needs to be specified when populating this map.
     * However, the {@link ContentViewAction} is purely optional since there are cases where there are no actions that 
     * can be performed on a {@link ContentViewHolder}.
     */
    //@formatter:off
    @SuppressWarnings("serial")
    private final static Map<Class<? extends MimeData>, 
                             ContentViewData<? extends MimeData, 
                                             ? extends View, 
                                             ? extends ContentViewHolder<? extends MimeData, ? extends View>, 
                                             ? extends ContentViewAction<? extends ContentViewHolder<? extends MimeData, ? extends View>>>> CONTENT_VIEW_HOLDER_TABLE = 
            new HashMap<Class<? extends MimeData>, 
                        ContentViewData<? extends MimeData, 
                                        ? extends View, 
                                        ? extends ContentViewHolder<? extends MimeData, ? extends View>, 
                                        ? extends ContentViewAction<? extends ContentViewHolder<? extends MimeData, ? extends View>>>>() {{
                // Add all content view holders here...
                put(TextPlainMimeData.class, new ContentViewData<TextPlainMimeData, TextView, TextContentViewHolder, ContentViewAction<TextContentViewHolder>>(TextContentViewHolder.class, null));
                put(TextRichMimeData.class, new ContentViewData<TextRichMimeData, TextViewEx, TextRichContentViewHolder, TextRichContentViewAction>(TextRichContentViewHolder.class, TextRichContentViewAction.class));
                put(EmoteMimeData.class, new ContentViewData<EmoteMimeData, TextView, EmoteContentViewHolder, ContentViewAction<EmoteContentViewHolder>>(EmoteContentViewHolder.class, null));
                put(SystemChatroomParticipantEnterData.class, new ContentViewData<SystemChatroomParticipantEnterData, TextView, SystemChatroomParticipantEnterViewHolder, ContentViewAction<SystemChatroomParticipantEnterViewHolder>>(SystemChatroomParticipantEnterViewHolder.class, null));
                put(SystemChatroomParticipantExitData.class, new ContentViewData<SystemChatroomParticipantExitData, TextView, SystemChatroomParticipantExitViewHolder, ContentViewAction<SystemChatroomParticipantExitViewHolder>>(SystemChatroomParticipantExitViewHolder.class, null));
                put(ImageMimeData.class, new ContentViewData<ImageMimeData, ImageViewEx, ImageContentViewHolder, ImageContentViewAction>(ImageContentViewHolder.class, ImageContentViewAction.class));
                put(YoutubeMimeData.class, new ContentViewData<YoutubeMimeData, ImageViewEx, YoutubeContentViewHolder, YoutubeContentViewAction>(YoutubeContentViewHolder.class, YoutubeContentViewAction.class));
                put(StickerMimeData.class, new ContentViewData<StickerMimeData, ImageView, StickerContentViewHolder, ContentViewAction<StickerContentViewHolder>>(StickerContentViewHolder.class, null));
                put(GiftMimeData.class, new ContentViewData<GiftMimeData, LinearLayout, GiftContentViewHolder, ContentViewAction<GiftContentViewHolder>>(GiftContentViewHolder.class, null));
                put(OembedMimeData.class, new ContentViewData<OembedMimeData, ImageViewEx, OembedContentViewHolder, OembedContentViewAction>(OembedContentViewHolder.class, OembedContentViewAction.class));
                put(FlickrMimeData.class, new ContentViewData<FlickrMimeData, ImageViewEx, FlickrContentViewHolder, FlickrContentViewAction>(FlickrContentViewHolder.class, FlickrContentViewAction.class));
                put(MigmeLinkMimeData.class, new ContentViewData<MigmeLinkMimeData, RelativeLayout, MigmeLinkContentViewHolder, MigmeLinkContentViewAction>(MigmeLinkContentViewHolder.class, MigmeLinkContentViewAction.class));
                put(ChatroomMimeData.class, new ContentViewData<ChatroomMimeData, RelativeLayout, ChatroomContentViewHolder, ChatroomContentViewAction>(ChatroomContentViewHolder.class, ChatroomContentViewAction.class));
                put(PostMimeData.class, new ContentViewData<PostMimeData, RelativeLayout, PostContentViewHolder, PostContentViewAction>(PostContentViewHolder.class, PostContentViewAction.class));
                put(ProfileMimeData.class, new ContentViewData<ProfileMimeData, RelativeLayout, ProfileContentViewHolder, ProfileContentViewAction>(ProfileContentViewHolder.class, ProfileContentViewAction.class));
                put(DeezerMimeData.class, new ContentViewData<DeezerMimeData, RelativeLayout, DeezerContentViewHolder, DeezerContentViewAction>(DeezerContentViewHolder.class, DeezerContentViewAction.class));
    }};
    //@formatter:on
    
    /**
     * Creates appropriate {@link ContentViewHolder} given the {@link MimeData}.
     * Note that the {@link ContentViewHolder that is created will not have the
     * {@link MimeData} applied to it.
     * 
     * @param ctx
     *            A context to be used in the creation of
     *            {@link ContentViewHolder}.
     * @param mimeData
     *            The {@link MimeData} corresponding to the
     *            {@link ContentViewHolder} to be retrieved.
     * @return A {@link ContentViewHolder} for the given {@link MimeData}. If no
     *         corresponding value is found then null is returned.
     */
    //@formatter:off
    public static ContentViewHolder<? extends MimeData, ? extends View> createContentViewHolder(final Context ctx, final MimeData mimeData) {
        final Class<? extends MimeData> mimeDataClass = mimeData.getClass();
        final ContentViewData<? extends MimeData, 
                              ? extends View, 
                              ? extends ContentViewHolder<? extends MimeData, ? extends View>, 
                              ? extends ContentViewAction<? extends ContentViewHolder<? extends MimeData, ? extends View>>> contentViewData = 
                CONTENT_VIEW_HOLDER_TABLE.get(mimeDataClass);
        try {
            if (contentViewData != null) {
                // Create a new instance of a content view holder.
                return contentViewData.getContentViewHolderClass().getConstructor(Context.class, mimeDataClass).newInstance(ctx, mimeData);
            } else {
                Logger.error.log(LOG_TAG, 
                        "Failed to find an appropriate content view holder for mime data of type: ", mimeDataClass);
            }
        } catch (Exception ex) {
            Logger.error.log(LOG_TAG, ex);
        }
        
        return null;
    }
    //@formatter:on

    /**
     * Creates appropriate {@link ContentViewAction} given the {@link ContentViewHolder}.
     * Note that the {@link ContentViewAction that is created will not be initialized yet.
     * @param contentViewHolder The {@link ContentViewHolder} corresponding to the {@link ContentViewAction} to be retrieved.
     * @return  A {@link ContentViewAction} for the given {@link ContentViewHolder}. In no corresponding value is found then null is returned.
     */
    //@formatter:off
    public static ContentViewAction<? extends ContentViewHolder<? extends MimeData, ? extends View>> createContentViewAction(
            ContentViewHolder<? extends MimeData, ? extends View> contentViewHolder) {
        final Class<? extends MimeData> mimeDataClass = contentViewHolder.getMimeData().getClass();
        final ContentViewData<? extends MimeData, 
                              ? extends View, 
                              ? extends ContentViewHolder<? extends MimeData, ? extends View>, 
                              ? extends ContentViewAction<? extends ContentViewHolder<? extends MimeData, ? extends View>>> contentViewData = 
                CONTENT_VIEW_HOLDER_TABLE.get(mimeDataClass);
        try {
            if (contentViewData != null) {
                if (contentViewData.getContentViewActionClass() != null) {
                    // Create a new instance of a content view action with the content view holder in it .
                    return contentViewData.getContentViewActionClass().getConstructor(contentViewData.getContentViewHolderClass()).newInstance(contentViewHolder);
                }
            } else {
                Logger.error.log(LOG_TAG, 
                        "Failed to find an appropriate content view action for mime data of type: ", mimeDataClass);
            }
        } catch (Exception ex) {
            Logger.error.log(LOG_TAG, ex);
        }
        
        return null;
    }
    //@formatter:on
}
