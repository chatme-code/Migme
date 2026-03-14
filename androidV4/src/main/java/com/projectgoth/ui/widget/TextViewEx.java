/**
 * Copyright (c) 2013 Project Goth
 *
 * TextViewEx.java.java
 * Created Jun 18, 2013, 12:14:26 PM
 */

package com.projectgoth.ui.widget;

import java.util.concurrent.ConcurrentHashMap;

import android.content.Context;
import android.text.*;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.projectgoth.common.Constants;
import com.projectgoth.common.SpannableBuilder;
import com.projectgoth.common.SpannableBuilder.SpannableStringBuilderEx;
import com.projectgoth.ui.widget.ClickableSpanEx.ClickableSpanExListener;

/**
 * @author cherryv
 * 
 */
public class TextViewEx extends TextView implements OnClickListener, OnLongClickListener {

    static public final String[]    NO_HOTKEYS            = new String[] {};

    private boolean                 isUrlEnabled          = true;
    private boolean                 isAnchorTagEnabled    = true;
    private boolean                 isMentionEnabled      = true;
    private boolean                 isHashtagEnabled      = true;
    private boolean                 isEmoticonEnabled     = true;
    private boolean                 isChatroomLinkEnabled = false;
    private boolean                 isUsernameEnabled     = false;

    private ClickableSpanExListener clickableSpanListener;
    private OnClickListener         clickListener;
    private boolean                 linkIsClicked         = false;
    private OnLongClickListener     mOnLongClickListener;
    private boolean                 longClicked;

    private String[]                hotkeysFromServer     = null;
    private boolean                 isDecodeText          = false;
    private boolean                 isProcessMore     = false;
    private int                     mMaxLines;
    private SpannableStringBuilderEx mSpann;
    private String                  mFullText;

    public TextViewEx(Context context) {
        this(context, null, 0);
    }

    public TextViewEx(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TextViewEx(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        super.setOnClickListener(this);

        setMovementMethod(LinkMovementMethod.getInstance());
    }

    public boolean isUrlEnabled() {
        return isUrlEnabled;
    }

    public void setUrlEnabled(boolean isUrlEnabled) {
        this.isUrlEnabled = isUrlEnabled;
    }

    public boolean isAnchorTagEnabled() {
        return isAnchorTagEnabled;
    }

    public void setAnchorTagEnabled(boolean isAnchorTagEnabled) {
        this.isAnchorTagEnabled = isAnchorTagEnabled;
    }

    public boolean isMentionEnabled() {
        return isMentionEnabled;
    }

    public void setMentionEnabled(boolean isMentionEnabled) {
        this.isMentionEnabled = isMentionEnabled;
    }

    public boolean isHashtagEnabled() {
        return isHashtagEnabled;
    }

    public void setHashtagEnabled(boolean isHashtagEnabled) {
        this.isHashtagEnabled = isHashtagEnabled;
    }

    public boolean isEmoticonEnabled() {
        return isEmoticonEnabled;
    }

    public void setEmoticonEnabled(boolean isEmoticonEnabled) {
        this.isEmoticonEnabled = isEmoticonEnabled;
    }
    
    public boolean isChatroomLinkEnabled() {
        return isChatroomLinkEnabled;
    }

    public void setChatroomLinkEnabled(boolean isChatroomLinkEnabled) {
        this.isChatroomLinkEnabled = isChatroomLinkEnabled;
    }
    
    public boolean isUsernameEnabled() {
        return isUsernameEnabled;
    }

    public void setUsernameEnabled(boolean isUsernameEnabled) {
        this.isUsernameEnabled = isUsernameEnabled;
    }

    public void setHotkeysFromServer(String[] hotkeys) {
        hotkeysFromServer = hotkeys;
    }

    public boolean isDecodeText() {
        return isDecodeText;
    }

    public void setDecodeText(boolean isDecodeText) {
        this.isDecodeText = isDecodeText;
    }

    public boolean isProcessMore() {
        return isProcessMore;
    }

    public void setProcessMore(boolean isProcessMore) {
        this.isProcessMore = isProcessMore;
    }

    public void setFullText(String text) {
        this.mFullText = text;
    }

    public String getFullText() {
        return mFullText;
    }

    public SpannableStringBuilderEx setText(String text) {
        if(isProcessMore()) {
            setEllipsize(TextUtils.TruncateAt.END);
        }
        SpannableStringBuilderEx spanBuilder = SpannableBuilder.build(getContext(), text, getTextSize(),
                clickableSpanListener, null, hotkeysFromServer, isDecodeText, isUrlEnabled, isHashtagEnabled,
                isMentionEnabled, isChatroomLinkEnabled, isUsernameEnabled, this);

        setText(spanBuilder);

        return spanBuilder;
    }

    public void resetFullText() {
        if (mFullText != null) {
            SpannableStringBuilderEx spanBuilder = SpannableBuilder.build(getContext(), mFullText, getTextSize(),
                    clickableSpanListener, null, hotkeysFromServer, isDecodeText, isUrlEnabled, isHashtagEnabled,
                    isMentionEnabled, isChatroomLinkEnabled, isUsernameEnabled, null);
            setText(spanBuilder);
        }
    }

    public void setText(SpannableStringBuilderEx text) {
        super.setText(text);
        this.mSpann = text;
    }

    public SpannableStringBuilderEx getSpannableStringBuilder() {
        return this.mSpann;
    }

    /**
     *  Note that the clickableSpanListener will be cached, different TextViewEx objects that contain the same text use the
     *  same listener. so be careful when using the member variables of the listener object in its onClick callback
     */
    public void setClickableSpanExListener(ClickableSpanExListener listener) {
        clickableSpanListener = listener;
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        mOnLongClickListener =  l;
        super.setOnLongClickListener(this);
    }

    @Override
    public boolean onLongClick(View view) {
        boolean ret = mOnLongClickListener.onLongClick(view);
        if (ret) {
            longClicked = true;
        }
        return ret;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // AD-1258 ignore long press case
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (longClicked) {
                longClicked = false;
                return false;
            }
        }

        return super.onTouchEvent(event);
    }

    @Override
    public void onClick(View v) {
        if (!linkIsClicked) {
            if (clickListener != null) {
                clickListener.onClick(v);
            }
        }
        linkIsClicked = false;
    }

    /**
     *  the idea is we always set the TextViewEx itself as the real listener , so that we can check if the span is
     *  clicked in the onClick here instead of checking it in all different listeners including PostViewListener,
     *  SinglePostViewListener, ChatFragment
     *
     * */
    @Override
    public void setOnClickListener(OnClickListener l) {
        clickListener = l;
        super.setOnClickListener(this);
    }

    /**
     * @return the linkIsClicked
     */
    public boolean isLinkIsClicked() {
        return linkIsClicked;
    }

    
    /**
     * @param linkIsClicked the linkIsClicked to set
     */
    public void setLinkIsClicked(boolean linkIsClicked) {
        this.linkIsClicked = linkIsClicked;
    }


    public void setText(String text, ConcurrentHashMap<String, SpannableStringBuilder> spannableCache) {
        SpannableStringBuilder bodySpan = null;

        bodySpan = spannableCache.get(text);

        if (bodySpan == null) {
            if (android.text.TextUtils.isEmpty(text)) {
                super.setText(Constants.BLANKSTR);
            } else {
                SpannableStringBuilderEx span = this.setText(text);

                if (spannableCache != null && span.isComplete()) {
                    spannableCache.put(text, span);
                }
            }
        } else {
            super.setText(bodySpan);
        }

    }

    @Override
    public void setMaxLines(int maxlines) {
        super.setMaxLines(maxlines);
        mMaxLines = maxlines;
        ViewTreeObserver observer = getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int maxLines = (int) TextViewEx.this.getHeight()
                        / TextViewEx.this.getLineHeight();
                TextViewEx.this.setMaxLines(maxLines);
                TextViewEx.this.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });
    }

    public int getMaxLines() {
        return mMaxLines;
    }

}
