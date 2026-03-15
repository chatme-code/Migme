/**
 * Copyright (c) 2013 Project Goth
 *
 * BaseMessageViewHolder.java
 * Created Dec 6, 2014, 3:06:51 PM
 */

package com.projectgoth.ui.holder;

import android.view.View;
import android.widget.TextView;

import com.projectgoth.R;
import com.projectgoth.common.Tools;
import com.projectgoth.i18n.I18n;
import com.projectgoth.listener.MimeContentViewListener;
import com.projectgoth.model.Message;
import com.projectgoth.ui.widget.DateTextView;
import com.projectgoth.util.AndroidLogger;


/**
 * @author angelorohit
 *
 */
public class BaseMessageViewHolder extends BaseViewHolder<Message> {
    
    private final static String LOG_TAG = AndroidLogger.makeLogTag(BaseMessageViewHolder.class);

    private final DateTextView date;
    private final View         dateSeparator;
    private final View         container;

    private final TextView     newMessageIndicator;

    protected MimeContentViewListener mimeListener;

    public BaseMessageViewHolder(View view) {
        super(view);

        date = (DateTextView) view.findViewById(R.id.message_date);
        dateSeparator = (View) view.findViewById(R.id.date_separator);
        container = view.findViewById(R.id.container);
        newMessageIndicator = (TextView) view.findViewById(R.id.new_message);

        View messageContainer = (View) view.findViewById(R.id.message_container);
        if (messageContainer != null) {
            messageContainer.setOnClickListener(this);
        }
        container.setOnClickListener(this);
    }

    public void setDisplayDateTitleAndNewMessageIndicator(final boolean shouldDisplayDateTitle, 
            final boolean shouldDisplayNewMessageIndicator) {
        if (shouldDisplayNewMessageIndicator) {
            newMessageIndicator.setText(I18n.tr("New messages"));
            newMessageIndicator.setVisibility(View.VISIBLE);
        } else {
            newMessageIndicator.setVisibility(View.GONE);
        }
        
        date.setVisibility(shouldDisplayDateTitle ? View.VISIBLE : View.GONE);
        dateSeparator.setVisibility((shouldDisplayDateTitle || shouldDisplayNewMessageIndicator) ? View.VISIBLE : View.GONE);
    }
    
    @Override
    public void setData(Message message) {
        super.setData(message);
        
        date.setText(Tools.getMessageDisplayDate(data.getLongTimestamp()));
    }

    public void setMimeItemLongClickListener(MimeContentViewListener mimeClickListener) {
        mimeListener = mimeClickListener;
    }

}
