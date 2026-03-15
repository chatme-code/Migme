/**
 * Copyright (c) 2013 Project Goth
 *
 * EditTextEx.java.java
 * Created Jun 19, 2013, 2:28:02 PM
 */

package com.projectgoth.ui.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionWrapper;
import android.widget.EditText;

import com.projectgoth.app.ApplicationEx;
import com.projectgoth.common.Theme;
import com.projectgoth.common.ThemeValues;
import com.projectgoth.controller.EmoticonsController;
import com.projectgoth.imagefetcher.UIUtils;
import com.projectgoth.ui.WidgetUtils;

/**
 * @author cherryv
 * 
 */
public class EditTextEx extends EditText {

    private Drawable background = null;

    public EditTextEx(Context context) {
        this(context, null, 0);
    }

    public EditTextEx(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EditTextEx(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setFocusable(true);
        initStyle();
    }

    private void initStyle() {
        ColorStateList textColor = WidgetUtils.createColorStates(Theme.getColor(ThemeValues.TEXTFIELD_TEXT_NORMAL),
                Theme.getColor(ThemeValues.TEXTFIELD_TEXT_HIGHLIGHT),
                Theme.getColor(ThemeValues.TEXTFIELD_TEXT_HIGHLIGHT),
                Theme.getColor(ThemeValues.TEXTFIELD_TEXT_HIGHLIGHT));
        setTextColor(textColor);
    }

    public void setCustomBackground(Drawable background) {
        this.background = background;
        UIUtils.setBackground(this, this.background);
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

    // Modified from http://stackoverflow.com/questions/4886858/android-edittext-deletebackspace-key-event
    private class EditTextExInputConnection extends InputConnectionWrapper {
        public EditTextExInputConnection(InputConnection target, boolean mutable) {
            super(target, mutable);
        }

        @Override
        public boolean sendKeyEvent(KeyEvent event) {
            if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
                if(handleBackspace()) return false;
            }
            return super.sendKeyEvent(event);
        }

        @Override
        public boolean deleteSurroundingText(int beforeLength, int afterLength) {
            // magic: in latest Android, deleteSurroundingText(1, 0) will be called for backspace
            if (beforeLength == 1 && afterLength == 0) {
                // backspace
                return sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
                        && sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL));
            }

            return super.deleteSurroundingText(beforeLength, afterLength);
        }
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        InputConnection conn = super.onCreateInputConnection(outAttrs);
        outAttrs.imeOptions &= ~EditorInfo.IME_FLAG_NO_ENTER_ACTION;
        return new EditTextExInputConnection(conn, true);
    }

    // Modified from https://ballardhack.wordpress.com/2011/07/25/customizing-the-android-edittext-behavior-with-spans/
    private boolean handleBackspace() {
        Editable buffer = getText();

        int start = Selection.getSelectionStart(buffer);
        int end = Selection.getSelectionEnd(buffer);
        if (start == end) {
            ImageSpan[] link = buffer.getSpans(start, end, ImageSpan.class);
            if (link.length > 0) {
                buffer.replace(buffer.getSpanStart(link[0]), buffer.getSpanEnd(link[0]), "");
                buffer.removeSpan(link[0]);
                return true;
            }
        }
        return false;
    }
}
