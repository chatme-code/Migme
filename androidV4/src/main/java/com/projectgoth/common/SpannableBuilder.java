/**
 * Copyright (c) 2013 Project Goth
 *
 * SpannableBuilder.java.java
 * Created Jun 12, 2013, 6:24:20 PM
 */

package com.projectgoth.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;

import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.Link;
import com.projectgoth.b.data.Variable;
import com.projectgoth.b.data.VariableLabel;
import com.projectgoth.common.migcommand.SupportedMigCommands;
import com.projectgoth.controller.EmoticonsController;
import com.projectgoth.datastore.EmoticonDatastore;
import com.projectgoth.datastore.PostsDatastore;
import com.projectgoth.datastore.UserDatastore;
import com.projectgoth.listener.EmoticonBmpLoadListener;
import com.projectgoth.ui.widget.ClickableSpanEx;
import com.projectgoth.ui.widget.ClickableSpanEx.ClickableSpanExListener;
import com.projectgoth.ui.widget.TextViewEx;
import com.projectgoth.ui.widget.util.TextViewUtil;

/**
 * @author cherryv
 * 
 */
public class SpannableBuilder {

    public static final Pattern URL_PATTERN;
    public static final Pattern HASHTAG_PATTERN;
    public static final Pattern MENTIONS_PATTERN;
    public static final Pattern CHATROOM_LINK_PATTERN;
    public static final Pattern YOUTUBE_URL_PATTERN;

    static {
        URL_PATTERN = Pattern.compile(Constants.URL_REGEX, Pattern.CASE_INSENSITIVE);
        HASHTAG_PATTERN = Pattern.compile(Constants.HASHTAG_REGEX, Pattern.CASE_INSENSITIVE);
        MENTIONS_PATTERN = Pattern.compile(Constants.MENTIONS_REGEX, Pattern.CASE_INSENSITIVE);
        CHATROOM_LINK_PATTERN = Pattern.compile(Constants.CHATROOM_LIST_REGEX, Pattern.CASE_INSENSITIVE);
        YOUTUBE_URL_PATTERN = Pattern.compile(Constants.YOUTUBE_URL_REGEX, Pattern.CASE_INSENSITIVE);
    }

    public static class SpannableStringBuilderEx extends SpannableStringBuilder {

        private boolean mIsComplete = false;
        private boolean mIsEllipsize = false;
        private Context context;
        private String mFullText;
        private ClickableSpanExListener mClickableSpanExListener;
        private boolean isDecodeText;
        private boolean isProcessUrl;
        private boolean isProcessHashtag;
        private boolean isProcessMention;
        private boolean isProcessChatroomLink;
        private boolean isProcessUsername;
        private TextViewEx mTextView;

        public SpannableStringBuilderEx(CharSequence text) {
            super(text);
        }

        public void setParams(Context context , ClickableSpanExListener listener, boolean isDecodeText, boolean isProcessUrl,
                               boolean isProcessHashtag, boolean isProcessMention, boolean isProcessChatroomLink, boolean isProcessUsername,
                               TextViewEx view) {
            this.context = context;
            this.mClickableSpanExListener = listener;
            this.isDecodeText = isDecodeText;
            this.isProcessUrl = isProcessUrl;
            this.isProcessHashtag = isProcessHashtag;
            this.isProcessMention = isProcessMention;
            this.isProcessChatroomLink = isProcessChatroomLink;
            this.isProcessUsername = isProcessUsername;
            this.mTextView = view;
        }

        public void setEllipsize(boolean isEllipsize) {
            this.mIsEllipsize = isEllipsize;
        }

        public boolean isEllipsize() {
            return mIsEllipsize;
        }

        public void setComplete(boolean mIsComplete) {
            this.mIsComplete = mIsComplete;
        }

        public boolean isComplete() {
            return mIsComplete;
        }
    }
    
    public static SpannableStringBuilderEx build(Context context, String text, float textSize,
            ClickableSpanExListener clickableSpanListener, String[] hotkeysFromServer, boolean isDecodeText,
            boolean shouldProcessUrl, boolean shouldProcessHashtag, boolean shouldProcessMention, boolean shouldProcessChatroomLink,
            boolean shouldProcessUsername) {
        
        return build(context, text, textSize, clickableSpanListener, null, hotkeysFromServer, isDecodeText, shouldProcessUrl, shouldProcessHashtag,
                shouldProcessMention, shouldProcessChatroomLink, shouldProcessUsername, null);
        
    }

    public static SpannableStringBuilderEx build(Context context, String text, float textSize,
                                                 ClickableSpanExListener clickableSpanListener, String[] hotkeysFromServer, boolean isDecodeText,
                                                 boolean shouldProcessUrl, boolean shouldProcessHashtag, boolean shouldProcessMention, boolean shouldProcessChatroomLink,
                                                 boolean shouldProcessUsername, TextViewEx textView) {

        return build(context, text, textSize, clickableSpanListener, null, hotkeysFromServer, isDecodeText, shouldProcessUrl, shouldProcessHashtag,
                shouldProcessMention, shouldProcessChatroomLink, shouldProcessUsername, textView);

    }
    
    /**
     * Build <code>SpannableStringBuilder</code> with expected style.
     * 
     * @param text
     * @param onUserNameClickListener 
     * @param anchorTagListener
     *            if not <code>null</code> will highlight the links and use it
     *            as the callback for clicks
     * @return
     */
    public static SpannableStringBuilderEx build(Context context, String text, float textSize,
            ClickableSpanExListener clickableSpanExListener, Variable[] vars, String[] hotkeysFromServer, boolean isDecodeText,
            boolean shouldProcessUrl, boolean shouldProcessHashtag, boolean shouldProcessMention, boolean shouldProcessChatroomLink,
            boolean shouldProcessUsername, TextViewEx textView) {
        
        Logger.debug.log("SpannableStringBuilderEx.setText", "text:" + text);

        SpannableStringBuilderEx spanBuilder = null;
        if(isDecodeText) {
            spanBuilder = new SpannableStringBuilderEx(Html.fromHtml(text));
        } else {
            spanBuilder = new SpannableStringBuilderEx(text);
        }

        if (textView != null) {
            if (textView.getEllipsize() == TextUtils.TruncateAt.END) {
                spanBuilder = TextViewUtil.ellipsizeString(spanBuilder, textView, clickableSpanExListener);
            }
        }

        spanBuilder.setParams(context, clickableSpanExListener, isDecodeText, shouldProcessUrl, shouldProcessHashtag,
                shouldProcessMention, shouldProcessChatroomLink, shouldProcessUsername, textView);
        
        if (clickableSpanExListener != null && shouldProcessUrl) {
            processAnchorTag(spanBuilder, clickableSpanExListener);
            processUrl(spanBuilder, clickableSpanExListener);
        } else {
            processUrlWithUnclickableSpan(spanBuilder);
        }
        
        if (clickableSpanExListener != null && shouldProcessHashtag) {
            processHashTag(spanBuilder, clickableSpanExListener);
        }
        
        if (clickableSpanExListener != null && shouldProcessMention) {
            processMentions(spanBuilder, clickableSpanExListener);
        }
        
        if (clickableSpanExListener != null && shouldProcessChatroomLink) {
            processChatroomLinks(spanBuilder, clickableSpanExListener);
        }
        
        int emoHeight = ApplicationEx.getInlineEmoticonDimension();
        boolean complete = true;
        if (vars != null) {
            complete = complete && processVariables(context, spanBuilder, vars, emoHeight, clickableSpanExListener);
        }
        
        
        String[] hotkeysUsed = null;
        if(hotkeysFromServer == null) {
            final Set<String> allHotkeysSet = EmoticonDatastore.getInstance().getAllOwnEmoticonHotkeys();
            hotkeysUsed = allHotkeysSet.toArray(new String[allHotkeysSet.size()]);
            if (allHotkeysSet.size() == 0) {
                complete = false;
            }
        } else {
            hotkeysUsed = hotkeysFromServer;
        }

        // sort the hotkeys with hotkey length from long to short
        // which can get rid of short hotkey included by long hotkey
        Arrays.sort(hotkeysUsed, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return  o2.length() - o1.length();
            }
        });
        
        complete = complete && processEmoticon(context, spanBuilder, hotkeysUsed, emoHeight);       
        spanBuilder.setComplete(complete);
        
        return spanBuilder;
    }

    /**
     * Shortens URL links. Maximum length for URL is defined in
     * {@link Constants#PROTOCOL_MARK}
     * 
     * @param url
     *            Found URL
     * @return Shortened URL
     */
    private static String ellipsizeUrl(String url) {
        int protocolMarkIndex = url.indexOf(Constants.PROTOCOL_MARK);
        if(protocolMarkIndex != -1) {
            url = url.substring(protocolMarkIndex + Constants.PROTOCOL_MARK.length());
        }

        if (url.length() <= Constants.MAX_LINK_LENGTH) {
            return url;
        }

        int max = Constants.MAX_LINK_LENGTH / 2;
        String prefix = url.substring(0, max);
        String suffix = url.substring(url.length() - max);
        return prefix + "\u2026" + suffix;
    }

    /**
     * Looks for URLs in the text and set them as clickable spans
     * 
     * @param builder
     * @param listener
     */
    private static void processAnchorTag(SpannableStringBuilder builder, ClickableSpanExListener listener) {
        URLSpan[] urlSpans = builder.getSpans(0, builder.length(), URLSpan.class);
        for (URLSpan urlSpan : urlSpans) {
            int start = builder.getSpanStart(urlSpan);
            int end = builder.getSpanEnd(urlSpan);
            String url = urlSpan.getURL();

            builder.removeSpan(urlSpan);
            ClickableSpanEx clickSpan = new ClickableSpanEx(url, listener);
            builder.setSpan(clickSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    /**
     * Looks for links in the text based on the {@link #URL_PATTERN} defined and
     * set them as clickable spans
     * 
     * @param builder
     * @param listener
     */
    private static void processUrl(SpannableStringBuilder builder, ClickableSpanExListener listener) {
        int offset = 0;
        while (true) {
            Matcher m = URL_PATTERN.matcher(builder.toString());
            if (m.find(offset)) {
                String url = m.group(0);
                ClickableSpanEx clickSpan = new ClickableSpanEx(url, listener);
                builder.setSpan(clickSpan, m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                String ellipsizedUrl = ellipsizeUrl(url);
                builder.replace(m.start(), m.end(), ellipsizedUrl);
                offset = m.start()+ellipsizedUrl.length();
            } else {
                break;
            }
        }
    }

    /**
     * Looks for links in the text based on the {@link #URL_PATTERN} defined
     * however we don't display them as clickable spans. See DROID-2427 use
     * case.
     * 
     * @param builder
     */
    private static void processUrlWithUnclickableSpan(SpannableStringBuilder builder) {
        int offset = 0;
        while (true) {
            Matcher m = URL_PATTERN.matcher(builder.toString());
            if (m.find(offset)) {
                String url = m.group(0);
                StyleSpan span = new StyleSpan(Typeface.NORMAL);
                builder.setSpan(span, m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                String ellipsizedUrl = ellipsizeUrl(url);
                builder.replace(m.start(), m.end(), ellipsizedUrl);
                offset = m.start()+ellipsizedUrl.length();
            } else {
                break;
            }
        }
    }

    public static Link[] processUrlFromString(String body) {
        int offset = 0;
        List<Link> links = new ArrayList<Link>();

        while (true) {
            Matcher m = URL_PATTERN.matcher(body);
            if (m.find(offset)) {
                String url = m.group(0);
                Link link = new Link();
                link.setUrl(url);
                links.add(link);
                offset = m.start()+url.length();
            } else {
                break;
            }
        }
        Link[] linkArray = new Link[links.size()];
        return links.toArray(linkArray);
    }

    /**
     * Looks for strings prepended by '#' character
     * 
     * @param builder
     * @param listener
     *            Callback listener for clicking this span
     */
    private static void processHashTag(SpannableStringBuilder builder, ClickableSpanExListener listener) {
        Matcher m = HASHTAG_PATTERN.matcher(builder.toString());
        while (m.find() == true) {
            ClickableSpanEx clickSpan = new ClickableSpanEx(Tools.constructMigUrl(
                    SupportedMigCommands.SEARCH_TOPIC, m.group(0).substring(1)), listener);
            builder.setSpan(clickSpan, m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    /**
     * Looks for strings prepended by '@' character
     * 
     * @param builder
     * @param listener
     */
    private static void processMentions(SpannableStringBuilder builder, ClickableSpanExListener listener) {
        Matcher m = MENTIONS_PATTERN.matcher(builder.toString());
        while (m.find() == true) {
            String username = m.group(0).substring(1);
            ClickableSpanEx clickSpan = new ClickableSpanEx(Tools.constructMigUrl(
                    SupportedMigCommands.SHOW_PROFILE, username), listener);
            builder.setSpan(clickSpan, m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private static boolean processVariables(Context context, SpannableStringBuilder builder, Variable[] vars,
            int emoHeight, ClickableSpanExListener onUsernameClicklistener) {
        boolean complete = true;

        String label;
        String match;
        int start, end;
        for (Variable var : vars) {
            label = null;
            VariableLabel varLabel = var.getLabel();
            if (varLabel != null) {
                label = varLabel.getText();
                if (label == null || label.length() == 0) {
                    label = "";
                }
            }

            match = "%{" + var.getName() + "}";
            while ((start = builder.toString().indexOf(match)) > -1) {
                end = start + match.length();

                // TODO: Look and recognize image variables

                ClickableSpanEx span = new ClickableSpanEx(label, null);
                if (var.getType() == com.projectgoth.b.enums.ObjectTypeEnum.USER) {
                    String username = label;
                    span.setValue(Tools.constructMigUrl(
                            SupportedMigCommands.SHOW_PROFILE, username));
                    span.setListener(onUsernameClicklistener);
                }

                builder.replace(start, end, label);
                if (label.length() > 0) {
                    builder.setSpan(span, start, start + label.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        }

        return complete;
    }
    
    private static boolean processEmoticon(Context context, SpannableStringBuilder builder, String[] hotkeys, int emoHeight) {
        boolean complete = true;

        Object[] spans = builder.getSpans(0, builder.length(), Object.class);
        int hotkeyLength;
        String text = builder.toString().toLowerCase(Locale.US);
        int searchIndex = 0;
        
        for (String hotkey : hotkeys) {
            Bitmap bitmap = null;
            String hotkeyLower = hotkey.toLowerCase(Locale.US);
            hotkeyLength = hotkey.length();
            if (hotkeyLength == 0) continue;
            searchIndex = 0;
            do {
                boolean hasSpan = false;

                int index = text.indexOf(hotkeyLower, searchIndex);
                if (index < 0) {
                    break;
                } else {
                    for (Object span : spans) {
                        if (!(span instanceof ClickableSpanEx) && index >= builder.getSpanStart(span) && index <= builder.getSpanEnd(span)) {
                            hasSpan = true;
                            break;
                        }
                    }
                }
                
                searchIndex = index + hotkeyLength;
                if (hasSpan) {
                    continue;
                }

                EmoticonBmpLoadListener loadListener = null;
                if (bitmap == null) {
                    loadListener = new EmoticonBmpLoadListener();
                    bitmap = EmoticonsController.getInstance().getResizedEmoticonBitmap(hotkey, emoHeight, loadListener);
                }
                ImageSpan span;
                if (bitmap != null && !bitmap.isRecycled()) {
                    span = new ImageSpan(context, bitmap, ImageSpan.ALIGN_BOTTOM);
                } else {
                    span = new ImageSpan(context, GUIConst.BMP_LOADING_EMOTICON, ImageSpan.ALIGN_BOTTOM);
                    complete = false;
                    if (loadListener != null) {
                        loadListener.setBmpLoadedFromCache(false);
                    }
                }
                builder.setSpan(span, index, index + hotkeyLength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } while (true);
            
            // replace the detected emoticons with spaces
            char spaces[] = new char[hotkeyLength];
            Arrays.fill(spaces, ' ');
            text = text.replace(hotkey, new String(spaces));
        }
        
        return complete;
    }
    
    private static void processChatroomLinks(SpannableStringBuilder builder, ClickableSpanExListener listener) {
        Matcher m = CHATROOM_LINK_PATTERN.matcher(builder.toString());
        while (m.find()) {
            String url = m.group(1);
            if (!TextUtils.isEmpty(url)) {
                ClickableSpanEx clickSpan = new ClickableSpanEx(Tools.constructMigUrl(
                        SupportedMigCommands.JOIN_CHATROOM, url), listener);
                builder.setSpan(clickSpan, m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                builder.replace(m.start(), m.end(), url);
            }
        }
    }




}
