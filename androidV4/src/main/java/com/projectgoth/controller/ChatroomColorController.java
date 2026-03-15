package com.projectgoth.controller;

import com.projectgoth.datastore.ChatDatastore;
import com.projectgoth.imagefetcher.UIUtils;
import com.projectgoth.nemesis.model.ChatRoomItem;

import java.util.HashMap;

/**
 * Created by houdangui on 24/4/15.
 *
 * chatroom color is decided by client, it could appear in different places , chat list, chatroom list,
 * chatroom mime content view, the color should be consistent for same chatroom.  the basic logic is
 * use the color of the chatroom in chatroom list if exists, otherwise create a random color
 *
 */
public class ChatroomColorController {

    private final static ChatroomColorController INSTANCE = new ChatroomColorController();

    HashMap<String, Integer> colorMap = new HashMap<String, Integer>();

    /**
     * Constructor
     */
    private ChatroomColorController() {

    }

    public static synchronized ChatroomColorController getInstance(){
        return INSTANCE;
    }

    public void addChatroomColor(String chatroomName, int color) {
        colorMap.put(chatroomName, new Integer(color));
    }

    public int getChatroomColor(String chatroomName) {
        //get color from chatroom item in the chatroom list frist
        ChatRoomItem chatRoomItem = getChatroomItemInChatroomList(chatroomName);
        if (chatRoomItem != null) {
            return chatRoomItem.getColor();
        }
        
        //get it from color map
        Integer color = colorMap.get(chatroomName);
        if (color == null) {
            //if it is not there, create one
            addChatroomColor(chatroomName, UIUtils.getRandomChatroomColor());
        }

        return colorMap.get(chatroomName).intValue();
    }

    public ChatRoomItem getChatroomItemInChatroomList(String chatroomName) {
        ChatRoomItem item = ChatDatastore.getInstance().getChatRoomItem(chatroomName);
        if (item != null) {
            return item;
        }
        return null;
    }
}
