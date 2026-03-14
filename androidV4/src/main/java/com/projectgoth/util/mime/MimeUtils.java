/**
 * Copyright (c) 2013 Project Goth
 *
 * MimeParseUtils.java
 * Created Nov 12, 2014, 11:16:27 AM
 */

package com.projectgoth.util.mime;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.migme.commonlib.enums.ImageFileType;
import com.projectgoth.b.data.Link;
import com.projectgoth.b.data.mime.FlickrMimeData;
import com.projectgoth.b.data.mime.GiftMimeData;
import com.projectgoth.b.data.mime.ImageMimeData;
import com.projectgoth.b.data.mime.MigmeLinkMimeData;
import com.projectgoth.b.data.mime.MimeData;
import com.projectgoth.b.data.mime.MimeType;
import com.projectgoth.b.data.mime.MimeTypeDataModel;
import com.projectgoth.b.data.mime.OembedMimeData;
import com.projectgoth.b.data.mime.TextRichMimeData;
import com.projectgoth.b.data.mime.YoutubeMimeData;
import com.projectgoth.common.Constants;
import com.projectgoth.common.SpannableBuilder;
import com.projectgoth.common.YoutubeUri;
import com.projectgoth.datastore.MimeDatastore;
import com.projectgoth.datastore.Session;

/**
 * Utilities related to construction and parsing of Mime types and data.
 *
 * @author angelorohit
 */
public abstract class MimeUtils {

    /**
     * Checks whether a gift in {@link GiftMimeData} is incoming or outgoing.
     *
     * @param giftMimeData
     *            The {@link GiftMimeData} to be checked.
     * @return true if the sender of the gift is not the same as the currently
     *         logged in user and false otherwise.
     */
    public static boolean isIncomingGift(GiftMimeData giftMimeData) {
        return (giftMimeData != null && !Session.getInstance().isSelfByUsername(giftMimeData.getSender()));
    }

    /**
     * Generates mime data from a given String text. Currently only generates
     * {@link ImageMimeData} and {@link YoutubeMimeData}.
     *
     * @param text
     *            The String text from which {@link MimeData} is to be
     *            generated.
     * @return A {@link List} of {@link MimeData} that was generated and an
     *         empty {@link List} if no mime types were recognized in the text.
     */
    public static List<MimeData> generateMimeData(final String text, MimeTypeDataModel dataModel, List<MimeData> mimeDataList) {
        final List<MimeData> result = new ArrayList<MimeData>();

        final List<String> urls = extractUrls(text);
        for (final String url : urls) {
            MimeData mimeData = null;

            // Attempt to generate ImageMimeData from url.
            if (ImageFileType.isImageUrl(url)) {
                mimeData = ImageMimeData.createFromUrl(url, url);
            } else {
                // Attempt to generate YoutubeMimeData from url.
                try {
                    YoutubeUri uri = YoutubeUri.parse(url);
                    if (uri != null) {
                        String thumbnailUrl = uri.getThumbnailUrl(Constants.DEFAULT_YOUTUBE_QUALITY);
                        mimeData = YoutubeMimeData.createFromUrl(uri.getUrl(), thumbnailUrl);
                    } else if (FlickrMimeData.isFlickrUrl(url)) {
                        mimeData = MimeDatastore.getInstance().getFlickrMimeData(url);

                        if (mimeData == null) {
                            mimeData = FlickrMimeData.createFromUrl(url, Constants.BLANKSTR);
                        }
                    } else if (OembedMimeData.isSoundcloudUrl(url)) {
                        mimeData = getOembedMimeData(url, MimeType.SOUNDCLOUD);
                    } else if (OembedMimeData.isVimeoUrl(url)) {
                        mimeData = getOembedMimeData(url, MimeType.VIMEO);
                    }
                } catch (Exception ex) {
                    // Nothing to do here
                }
            }

            if (mimeData != null) {
                result.add(mimeData);

                //remove the orginal link if the mime data extracted
                if (text.equals(url)) {
                   removeLink(mimeDataList, url);  
                }

            }
        }

        // server detects these mime data links already, we don't need check from client side
        Link[] links = dataModel.getMimeDataLinks();
        if (links != null) {
            for (Link link : links) {
                MimeData mimeData = null;
                String siteName = link.getSiteName();

                if (siteName == null) {
                    continue;
                }

                if (MigmeLinkMimeData.isMigmeLink(siteName)) {
                    mimeData = MigmeLinkMimeData.createFromUrl(link.getUrl(), link.getImage(), link.getTitle(),
                            link.getDescription());
                }

                if (mimeData != null) {
                    result.add(mimeData);
                }
            }
        }

        return result;
    }

    private static void removeLink(List<MimeData> mimeDataList, String url) {
        if (mimeDataList != null) {
            for (MimeData mimeData : mimeDataList) {
                if(mimeData.getMimeType() == MimeType.TEXT_RICH) {
                    TextRichMimeData data = (TextRichMimeData) mimeData;
                    if (data.getText()!= null && data.getText().equals(url)) {
                        mimeDataList.remove(mimeData);
                        break;
                    }
                }
            }
        }
    }

    private static MimeData getOembedMimeData(String url, MimeType mimeType) {
        MimeData mimeData = MimeDatastore.getInstance().getOembedMimeData(url, mimeType);
        if (mimeData == null) {
            mimeData = OembedMimeData.createFromUrl(url, Constants.BLANKSTR, mimeType);
        }
        return mimeData;
    }

    private static List<String> extractUrls(String text) {
        int offset = 0;
        final Matcher m = SpannableBuilder.URL_PATTERN.matcher(text);
        List<String> result = new ArrayList<String>();
        while (m.find(offset)) {
            final String url = m.group(0);
            offset = m.start() + url.length();
            result.add(url);
        }

        return result;
    }

    public static String convertMimeDataToJson(List<MimeData> mimeDataList) {
        if (mimeDataList == null || mimeDataList.size() == 0) {
            return Constants.BLANKSTR;
        }

        String json;

        JSONArray jsonArray = new JSONArray();
        for (MimeData mimeData : mimeDataList) {
            JSONObject object = null;
            try {
                object = new JSONObject();
                String key = mimeData.getMimeType().getValue();
                String value = mimeData.toJson();
                object.put(key, value);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            jsonArray.put(object);

        }

        json = jsonArray.toString();

        return json;
    }

    /**
     * we save the json string of the mime data which we can parse the MimeData
     * from so we skip serialize the MimeData object
     */

    static public class MessageExclusionStrategy implements ExclusionStrategy {

        static private String fieldNameToSkip = "mimeDataList";

        public boolean shouldSkipClass(Class<?> clazz) {
            return false;
        }

        public boolean shouldSkipField(FieldAttributes f) {
            return f.getName().equals(fieldNameToSkip);
        }
    }

}