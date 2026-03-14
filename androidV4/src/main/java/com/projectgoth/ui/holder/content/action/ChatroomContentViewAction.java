package com.projectgoth.ui.holder.content.action;

import android.support.v4.app.FragmentActivity;
import android.view.View;
import com.projectgoth.ui.UrlHandler;
import com.projectgoth.ui.activity.ActionHandler;
import com.projectgoth.ui.holder.content.ChatroomContentViewHolder;
import com.projectgoth.ui.holder.content.MigmeLinkContentViewHolder;

/**
 * Created by houdangui on 19/3/15.
 */
public class ChatroomContentViewAction extends ContentViewAction<ChatroomContentViewHolder> {

    private FragmentActivity activity;

    public ChatroomContentViewAction(ChatroomContentViewHolder contentViewHolder) {
        super(contentViewHolder);
    }

    @Override
    public void onClick(View v) {
        String chatroomName = contentViewHolder.getMimeData().getName();
        ActionHandler.getInstance().displayPublicChat(activity, chatroomName, 0);
    }

    @Override
    public void applyToView() {
        contentViewHolder.getContentView().setOnClickListener(this);
    }

    @Override
    public void setParameter(final Parameter parameter, final Object value) {
        super.setParameter(parameter, value);

        switch (parameter) {
            case ACTIVITY:
                this.activity = (FragmentActivity) value;
                break;
            default:
                break;
        }
    }

}
