/**
 * Copyright (c) 2013 Project Goth
 *
 * TextRichContentViewHolder.java
 * Created Dec 8, 2014, 8:12:43 PM
 */

package com.projectgoth.ui.holder.content;

import java.util.concurrent.ConcurrentHashMap;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;

import android.util.TypedValue;
import com.projectgoth.R;
import com.projectgoth.b.data.mime.TextRichMimeData;
import com.projectgoth.b.enums.PostOriginalityEnum;
import com.projectgoth.common.Constants;
import com.projectgoth.common.SpannableBuilder.SpannableStringBuilderEx;
import com.projectgoth.ui.widget.TextViewEx;
import com.projectgoth.util.PostUtils;


/**
 * Represents a holder for {@link TextRichMimeData} content.
 * @author angelorohit
 *
 */
public class TextRichContentViewHolder extends BaseTextContentViewHolder<TextRichMimeData, TextViewEx>{
    
    /**
     * A cache that can be used to fetch or put any {@link SpannableStringBuilder} when setting text.
     */
    private ConcurrentHashMap<String, SpannableStringBuilder> spannableCache = null;

    /**
     * one case is that
     */
    private PostOriginalityEnum postOriginality;

    private boolean needPostBody;
    private boolean truncatePostBody;

    /**
     * Constructor.
     * @param ctx       The {@link Context} to be used for inflation. 
     * @param mimeData  The {@link TextRichMimeData} to be used as data for this holder.
     */
    public TextRichContentViewHolder(Context ctx, TextRichMimeData mimeData) {
        super(ctx, mimeData);
    }
    
    /**
     * Provides a cache that can be used to fetch or put any {@link SpannableStringBuilder} when setting text.
     * @param spannableCache The cache.
     */
    private void setSpannableCache(final ConcurrentHashMap<String, SpannableStringBuilder> spannableCache) {
        this.spannableCache = spannableCache;
    }

    @Override
    public int getLayoutId() {
        return R.layout.content_view_text_rich;
    }
    
    @Override
    public boolean getProperty(final Property property) {
        switch (property) {
            case CAN_LONG_CLICK_ON_MESSAGE:
                return true;
            default:
                return super.getProperty(property);
        }
    }

    @Override
    public boolean applyMimeData() {
        if (mimeData != null) {
            SpannableStringBuilder bodySpan = null;
            String text = mimeData.getText();
            if (needPostBody) {
                String prefix = PostUtils.getPostBodyPrefix(postOriginality, TextUtils.isEmpty(text));
                if (!TextUtils.isEmpty(prefix)) {
                    text = prefix + text;
                }
            }
            view.setFullText(text);
            if (spannableCache != null) {
                bodySpan = spannableCache.get(text);
            }

            view.setHotkeysFromServer(mimeData.getHotkeys());
            if (bodySpan == null) {
                if (TextUtils.isEmpty(text)) {
                    view.setText(Constants.BLANKSTR);
                } else {
                    SpannableStringBuilderEx span = view.setText(text);
                    if (spannableCache != null && span.isComplete()) {
                        spannableCache.put(text, span);
                    }
                }
            } else {
                view.setText(bodySpan);
            }
            
            applyTextColor();
            return true;
        }

        return false;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void setParameter(final Parameter parameter, final Object value) {
        super.setParameter(parameter, value);
        
        switch(parameter) {
            case SPANNABLE_CACHE:
                setSpannableCache((ConcurrentHashMap<String, SpannableStringBuilder>) value);
                break;
            case DECODE_HTML_TEXT:
                Boolean decodeText = (Boolean) value;
                view.setDecodeText(decodeText.booleanValue());
                break;
            case TEXT_SIZE:
                Integer textSize = (Integer)value;
                view.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
                break;
            case TEXT_COLOR:
                Integer color = (Integer) value;
                specifiedTextColor = color ;
                break;
            case IS_ROOT_DATA_CONTENT:
                view.setTag(R.id.is_root_data_content, value);
                break;
            case POST_ORIGINALITY:
                postOriginality = (PostOriginalityEnum)value;
                break;
            case NEED_POST_BODY_REFIX:
                needPostBody = (Boolean) value;
                break;
            case TRUNCATE_LONG_POST:
                truncatePostBody = (Boolean) value;
                setupForLongPostBody(truncatePostBody);
                break;
            default:
                break;
        } 
    }

    private void setupForLongPostBody(final boolean truncatePostBody) {
        if (truncatePostBody) {
            view.setProcessMore(true);
        } else {
            view.setProcessMore(false);
        }
    }

    protected int getDefaultTextColorId() {
        return R.color.default_text;
    }

    @Override
    protected void initializeView() {
        view.setChatroomLinkEnabled(true);
    }

}
