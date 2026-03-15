package com.projectgoth.listener;

import com.projectgoth.b.data.mime.MimeData;
import com.projectgoth.model.Message;

/**
 * Created by houdangui on 7/4/15.
 */
public interface MimeContentViewListener {

    void onContentViewLongClick(Message msg, MimeData mimeData);
}
