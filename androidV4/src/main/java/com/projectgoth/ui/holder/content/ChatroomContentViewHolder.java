package com.projectgoth.ui.holder.content;

import android.content.Context;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.projectgoth.R;
import com.projectgoth.b.data.ChatroomInfo;
import com.projectgoth.b.data.mime.ChatroomMimeData;
import com.projectgoth.controller.ChatroomColorController;
import com.projectgoth.datastore.ChatDatastore;
import com.projectgoth.i18n.I18n;
import com.projectgoth.imagefetcher.UIUtils;
import com.projectgoth.nemesis.model.ChatRoomItem;

/**
 * Created by houdangui on 19/3/15.
 */
public class ChatroomContentViewHolder extends ContentViewHolder<ChatroomMimeData, RelativeLayout> {

    private TextView chatroomMark;
    private TextView chatroomName;
    private TextView chatroomCreator;
    private TextView description;
    private TextView joinChat;

    /**
     * Constructor.
     *
     * @param ctx      The {@link android.content.Context} to be used for inflation.
     * @param mimeData The {@link com.projectgoth.b.data.mime.ChatroomMimeData} to be used as data for this holder.
     */
    public ChatroomContentViewHolder(Context ctx, ChatroomMimeData mimeData) {
        super(ctx, mimeData);
    }

    @Override
    protected void initializeView() {
        chatroomMark = (TextView) view.findViewById(R.id.chatroom_mark);
        chatroomName = (TextView) view.findViewById(R.id.chatroom_name);
        chatroomCreator = (TextView) view.findViewById(R.id.chatroom_creator);
        description = (TextView) view.findViewById(R.id.chatroom_description);
        joinChat = (TextView) view.findViewById(R.id.join_chat);
    }

    @Override
    public int getLayoutId() {
        return R.layout.content_view_chatroom;
    }

    @Override
    public boolean applyMimeData() {
        if (mimeData != null) {
            String roomName = mimeData.getName();
            chatroomMark.setText(String.valueOf(Character.toUpperCase(roomName.charAt(0))));
            chatroomName.setText(roomName);

            //set chatroom color
            int chatroomColor = ChatroomColorController.getInstance().getChatroomColor(roomName);
            chatroomMark.setBackgroundColor(chatroomColor);

            ChatroomInfo chatroomInfo = ChatDatastore.getInstance().getChatroomInfo(roomName);
            if (chatroomInfo != null) {
                chatroomCreator.setText(String.format(I18n.tr("Created by %s"), chatroomInfo.getCreator()));
                description.setText(chatroomInfo.getDescription());
            }

            joinChat.setText(I18n.tr("Join chat"));


            return true;
        }
        return false;
    }
}
