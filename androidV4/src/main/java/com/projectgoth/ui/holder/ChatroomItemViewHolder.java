/**
 * Copyright (c) 2013 Project Goth
 *
 * ChatroomItemViewHolder.java
 * Created Jun 6, 2013, 10:35:45 AM
 */

package com.projectgoth.ui.holder;

import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.TextView;

import com.projectgoth.R;
import com.projectgoth.nemesis.model.ChatRoomItem;

/**
 * @author mapet
 * 
 */
public class ChatroomItemViewHolder extends BaseViewHolder<ChatRoomItem> {

    private final TextView  mark;
    private final TextView  title;
    private final TextView  counter;

    public ChatroomItemViewHolder(View view) {
        super(view);

        mark = (TextView) view.findViewById(R.id.mark);
        title = (TextView) view.findViewById(R.id.title);
        counter = (TextView) view.findViewById(R.id.counter);
    }

    @Override
    public void setData(ChatRoomItem chatroomData) {
        super.setData(chatroomData);

        //first character of the name
        mark.setText(String.valueOf(Character.toUpperCase(chatroomData.getName().charAt(0))));
        
        title.setText(chatroomData.getName());
        counter.setText(chatroomData.getCurrentParticipantsCount() + "/" + chatroomData.getMaxParticipantsCount());

        //set background
        int color = chatroomData.getColor();
        GradientDrawable bg = (GradientDrawable) mark.getBackground();
        bg.setColor(color);
    }

}
