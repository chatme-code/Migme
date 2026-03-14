package com.projectgoth.model;

import com.projectgoth.b.data.mime.MimeData;

/**
 * Created by houdangui on 7/4/15.
 *
 * we can pin each mime data contained in a message. is it a simple data structure for
 * creating a new message to pin
 *
 */
public class PinMessageData {

    private Message message;
    private MimeData  mimeData;

    public PinMessageData(Message message, MimeData mimeData) {
        this.message = message;
        this.mimeData = mimeData;
    }

    public Message getMessage() {
        return message;
    }

    public MimeData getMimeData() {
        return mimeData;
    }
}
