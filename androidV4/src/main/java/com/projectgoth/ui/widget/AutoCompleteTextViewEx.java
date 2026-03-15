/**
 * Copyright (c) 2013 Project Goth
 *
 * AutoCompleteTextViewEx.java
 * Created Sep 15, 2014, 5:50:17 PM
 */

package com.projectgoth.ui.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;

import com.projectgoth.app.ApplicationEx;
import com.projectgoth.common.Constants;
import com.projectgoth.controller.EmoticonsController;

/**
 * @author angelorohit
 *
 */
public class AutoCompleteTextViewEx extends AutoCompleteTextView {

	public AutoCompleteTextViewEx(Context context) {
		this(context, null, 0);
	}

	public AutoCompleteTextViewEx(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public AutoCompleteTextViewEx(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
        setFocusable(true);

        disableAutoCompleteFlag();
	}

    /**
     * AutoCompleteTextView changed the default auto correct behavior of EditText
     *  we disable it to keep the default behavior
     */
    private void disableAutoCompleteFlag() {
        int inputType = getInputType();
        inputType &= ~EditorInfo.TYPE_TEXT_FLAG_AUTO_COMPLETE;
        setRawInputType(inputType);
    }

    public void insertEmoticon(String hotkey) {
        if (TextUtils.isEmpty(hotkey)) {
            return;
        }

        Editable editable = getText();
        int start = getSelectionStart();
        int end = getSelectionEnd();
        if (start < 0) {
            start = 0;
        }
        if (end < 0) {
            end = 0;
        }
        start = Math.min(start, end);
        end = Math.max(start, end);

        int emoHeight = ApplicationEx.getInlineEmoticonDimension();
        Bitmap bitmap = EmoticonsController.getInstance().getResizedEmoticonBitmap(hotkey, emoHeight);
        if (bitmap == null) {
            editable.replace(start, end, hotkey);
        } else {
            SpannableString ss = new SpannableString(hotkey);
            ImageSpan span = new ImageSpan(getContext(), bitmap, ImageSpan.ALIGN_BOTTOM);
            ss.setSpan(span, 0, ss.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            editable.replace(start, end, ss);
        }
    }

    /**
     * Perform custom filtering of text by only including the string content from the last hashtag or mention symbol.  
     * @see android.widget.AutoCompleteTextView#performFiltering(java.lang.CharSequence, int)
     */
    @Override
    protected void performFiltering(CharSequence text, int keyCode) {
    	String textToFilter = text.toString();
    	int lastIndex = getLastIndexOfMentionOrHashTagSymbol(textToFilter);
    	if (lastIndex > -1) {
	    	textToFilter = textToFilter.substring(lastIndex);
	    	
	    	if (textToFilter.length() > 0) {
	    		super.performFiltering(textToFilter, keyCode);
	    	}
    	}
    }
    
    /**
     * Gets the last index of the mention or hashtag symbol, whichever is last.
     * @param text	The text within which to search for the mention or hashtag symbol.
     * @return	The last index or -1 if a match was not found.
     */
    private int getLastIndexOfMentionOrHashTagSymbol(final String text) {
    	if (!TextUtils.isEmpty(text)) {
	    	final int lastIndexOfMentionSymbol = text.lastIndexOf(Constants.MENTIONS_TAG);
	    	final int lastIndexOfHashTagSymbol = text.lastIndexOf(Constants.HASH_TAG);
	    	
	    	if (lastIndexOfMentionSymbol == -1) {
	    		return lastIndexOfHashTagSymbol;
	    	}
	    	
	    	return (lastIndexOfMentionSymbol < lastIndexOfHashTagSymbol) ? 
	    			lastIndexOfHashTagSymbol : lastIndexOfMentionSymbol;
    	}
    	
    	return -1;
    }
    
    /**
     * @see android.widget.AutoCompleteTextView#replaceText(CharSequence)
     */
    @Override
    protected void replaceText(CharSequence text) {
        clearComposingText();
        
        String prependedText = getText().toString();
        int index = getLastIndexOfMentionOrHashTagSymbol(prependedText);
        prependedText = prependedText.substring(0, index > 0 ? index : 0);
        setText(prependedText + text + Constants.SPACESTR);
        
        // make sure we keep the caret at the end of the text view
        Editable spannable = getText();
        Selection.setSelection(spannable, spannable.length());
    }
}
