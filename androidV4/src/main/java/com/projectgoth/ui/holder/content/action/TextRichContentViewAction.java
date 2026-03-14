/**
 * Copyright (c) 2013 Project Goth
 *
 * TextContentViewAction.java
 * Created Dec 4, 2014, 10:27:41 PM
 */

package com.projectgoth.ui.holder.content.action;

import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.projectgoth.common.Constants;
import com.projectgoth.i18n.I18n;
import com.projectgoth.ui.UrlHandler;
import com.projectgoth.ui.activity.ActionHandler;
import com.projectgoth.ui.holder.content.TextContentViewHolder;
import com.projectgoth.ui.holder.content.TextRichContentViewHolder;
import com.projectgoth.ui.widget.ClickableSpanEx;
import com.projectgoth.ui.widget.ClickableSpanEx.ClickableSpanExListener;
import com.projectgoth.ui.widget.TextViewEx;


/**
 * Represents a class that handles actions performed on the view of a {@link TextContentViewHolder}.
 * @author angelorohit
 *
 */
public class TextRichContentViewAction extends ContentViewAction<TextRichContentViewHolder> implements ClickableSpanExListener {

    private final String mSinglePostLinkPrefix = "https://migxchat.net/share/post/";
    private FragmentActivity mActivity;
    private boolean mNoAction = false;

    public TextRichContentViewAction(TextRichContentViewHolder contentViewHolder) {
        super(contentViewHolder);
    }

    @Override
    public void applyToView() {
        contentViewHolder.getContentView().setClickableSpanExListener(this);
        contentViewHolder.getContentView().setOnClickListener(mNoAction ? null : externalActionListener);
    }

    @Override
    public void onClick(View view, ClickableSpanEx span, String value) {
        //the SpanClickListener is cached in the spannable cache so cannot call contentViewHolder.getContentView().setLinkIsClicked
        // here, which returns the view could be different than the view clicked

        if (view instanceof TextViewEx) {
            TextViewEx tv = (TextViewEx) view;
            tv.setLinkIsClicked(true);

            if (value != null && value.equals(I18n.tr(Constants.ELLIPSIS_MORE))) {
                TextViewEx textView = (TextViewEx) view;
                textView.resetFullText();
                return;
            }
        }

        //redirect url of web post to android client
        if (value.startsWith(mSinglePostLinkPrefix)) {
            int chatIdStartIndex = value.indexOf("/", mSinglePostLinkPrefix.length()) + 1;
            int chatIdEndIndex = value.indexOf("?");
            String chatId = value.substring(chatIdStartIndex, chatIdEndIndex);
            ActionHandler.getInstance().displaySinglePostPage(mActivity, chatId, false, false);
        } else {
            UrlHandler.displayUrl(mActivity, value);
        }
    }

    @Override
    public void setParameter(final Parameter parameter, final Object value) {
        super.setParameter(parameter, value);
        
        switch (parameter) {
            case ACTIVITY:
                this.mActivity = (FragmentActivity) value;
                break;
            case NO_ACTION:
                mNoAction = (Boolean)value;
                break;
            default:
                break;
        }
    }
}
