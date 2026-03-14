/**
 * Copyright (c) 2013 Project Goth
 *
 * MimeDataGenerator.java
 * Created Dec 11, 2014, 4:57:29 PM
 */

package com.projectgoth.common;

import java.util.ArrayList;
import java.util.List;

import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.Photo;
import com.projectgoth.b.data.Post;
import com.projectgoth.b.data.mime.EmoteMimeData;
import com.projectgoth.b.data.mime.ImageMimeData;
import com.projectgoth.b.data.mime.MimeData;
import com.projectgoth.b.data.mime.MimeDataGenerator;
import com.projectgoth.b.data.mime.MimeTypeDataModel;
import com.projectgoth.b.data.mime.StickerMimeData;
import com.projectgoth.b.data.mime.TextPlainMimeData;
import com.projectgoth.b.data.mime.TextRichMimeData;
import com.projectgoth.b.enums.PostOriginalityEnum;
import com.projectgoth.i18n.I18n;
import com.projectgoth.imagefetcher.ImageHandler;
import com.projectgoth.imagefetcher.UIUtils;
import com.projectgoth.model.Message;
import com.projectgoth.util.ArrayUtils;
import com.projectgoth.util.PostUtils;
import com.projectgoth.util.mime.MimeUtils;


/**
 * A concrete implementation of {@link MimeDataGenerator}.
 * This class will be used to generate {@link MimeData} for {@link MimeTypeDataModel}.
 * @author angelorohit
 *
 */
public class MimeDataGeneratorImpl implements MimeDataGenerator {

    @Override
    public List<MimeData> generateForMessage(MimeTypeDataModel model) {
        Message message = (Message) model;
        boolean didGenerateTextContentMimeData = false;
        
        List<MimeData> mimeDataList = new ArrayList<MimeData>();
        switch(message.getContentType()) {
            case TEXT:
                if (!TextUtils.isEmpty(message.getMessage())) {
                    if (message.isInfoMessage()) {
                        mimeDataList.add(TextPlainMimeData.createFromText(I18n.tr(message.getMessage()), message.getMessageColorStr()));
                        didGenerateTextContentMimeData = true;
                    } else {
                        mimeDataList.add(TextRichMimeData.createFromText(message.getMessage(), message.getMessageColorStr(), message.getHotkeys()));
                    }
                    
                    mimeDataList.addAll(MimeUtils.generateMimeData(message.getMessage(), message, mimeDataList));
                }
                break;
            case IMAGE:
                final String url = Tools.constructProperImageUrl(message.getFilename()); 
                final String thumbnailUrl = ImageHandler.constructFullImageLinkForChatThumbnail(message.getFilename(),
                                Tools.getPixels(Constants.DEFAULT_CHAT_THUMB_SIZE));
                if (!TextUtils.isEmpty(url) && !TextUtils.isEmpty(thumbnailUrl)) {
                    mimeDataList.add(ImageMimeData.createFromUrl(url, thumbnailUrl));
                }
                break;
            case EMOTE:
                switch(message.getEmoteContentType()) {
                    case PLAIN:
                        if (!TextUtils.isEmpty(message.getMessage())) {
                            mimeDataList.add(EmoteMimeData.createFromText(message.getMessage(), message.getMessageColorStr()));
                        }
                        break;
                    case STICKERS:
                        if (!ArrayUtils.<String>isEmpty(message.getHotkeys())) {
                            mimeDataList.add(StickerMimeData.createFromHotkey(message.getHotkeys()[0]));
                        }
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }
        
        if (!didGenerateTextContentMimeData && message.isInfoMessage()) {
            if (!TextUtils.isEmpty(message.getMessage())) {
                mimeDataList.add(TextPlainMimeData.createFromText(I18n.tr(message.getMessage()), message.getMessageColorStr()));
            }
        }
        
        return mimeDataList;
    }

    @Override
    public List<MimeData> generateForPost(MimeTypeDataModel model) {
        Post post = (Post) model;

        ArrayList<MimeData> mimeDataList = new ArrayList<MimeData>();

        //extract mime data from the text body, e.g. youtube mime data
        mimeDataList.addAll(MimeUtils.generateMimeData(post.getBody(), post, mimeDataList));

        //convert photo to mime data
        Photo postPhoto = post.getPhoto();
        if (postPhoto != null) {
            int photoDisplayLimit = Config.getInstance().getScreenWidth();
            final String url = UIUtils.getPhotoUrl(postPhoto, null, photoDisplayLimit);
            final String thumbnailUrl = postPhoto.getNearestAvailableUrl(UIUtils.PhotoSize._480X.getSize());
            if (!TextUtils.isEmpty(url)) {
                mimeDataList.add(ImageMimeData.createFromUrl(url, thumbnailUrl));
            } else if (postPhoto.getBitMapByte() != null) {
                ImageMimeData imageMimeData = ImageMimeData.createFromBmpBytes(postPhoto.getBitMapByte());
                mimeDataList.add(imageMimeData);
            }
        }

        //convert text body to mime data
        String bodyText = PostUtils.getFormattedPostBody(post);

        // if the post is a repost with no text, we add a text mime data with empty text and with a prefix Repost later
        if (!TextUtils.isEmpty(bodyText) || post.getOriginality() == PostOriginalityEnum.RESHARE) {
            mimeDataList.add(TextRichMimeData.createFromText(PostUtils.getFormattedPostBody(post),
                    UIUtils.getRRGGBBString(ApplicationEx.getColor(R.color.default_text)),
                    null));
        }

        return mimeDataList;
    }
}
