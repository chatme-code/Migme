/**
 * Copyright (c) 2013 Project Goth
 *
 * ParticipantViewHolder.java
 * Created Aug 5, 2013, 2:20:25 PM
 */

package com.projectgoth.ui.holder;

import android.view.View;
import com.projectgoth.R;
import com.projectgoth.blackhole.enums.ChatParticipantType;
import com.projectgoth.blackhole.enums.PresenceType;
import com.projectgoth.datastore.UserDatastore;
import com.projectgoth.imagefetcher.UIUtils;
import com.projectgoth.nemesis.model.ChatParticipant;
import com.projectgoth.nemesis.model.User;
import com.projectgoth.ui.widget.UserImageView;
import com.projectgoth.ui.widget.UsernameWithLabelsView;

/**
 * @author sarmadsangi
 * 
 */
public class ParticipantViewHolder extends BaseViewHolder<ChatParticipant> {

    private final UserImageView          userImageView;
    private final UsernameWithLabelsView title;
    private final boolean                isChatRoom;

    public ParticipantViewHolder(View view, boolean isChatRoom) {
        super(view);

        this.isChatRoom = isChatRoom;

        userImageView = (UserImageView) view.findViewById(R.id.user_image);
        title = (UsernameWithLabelsView) view.findViewById(R.id.title);
    }

    @Override
    public void setData(ChatParticipant participant) {
        super.setData(participant);

        if(isChatRoom) {
            userImageView.setUserImage(participant.getUsername());
            userImageView.setPresenceImage(PresenceType.AVAILABLE);
        } else {
            userImageView.setUser(participant.getUsername());
        }
        title.setUsername(participant.getUsername());
        final User user = UserDatastore.getInstance().getUserWithUsername(participant.getUsername(), false);
        if (user != null && user.getProfile() != null) {
            title.setLabels(user.getProfile().getLabels(), false);
            title.setTextColor(UIUtils.getUsernameColorFromLabels(user.getProfile().getLabels(), 
                    participant.getType().equals(ChatParticipantType.ADMINISTRATOR)));
        }        
    }
}
