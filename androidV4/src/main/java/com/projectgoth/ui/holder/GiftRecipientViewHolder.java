/**
 * Copyright (c) 2013 Project Goth
 *
 * GiftRecipientViewHolder.java
 * Created 19 May, 2014, 1:52:23 pm
 */

package com.projectgoth.ui.holder;

import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import com.projectgoth.R;
import com.projectgoth.common.Config;
import com.projectgoth.i18n.I18n;
import com.projectgoth.imagefetcher.ImageHandler;
import com.projectgoth.nemesis.model.ChatParticipant;

/**
 * @author dan
 * 
 */
public class GiftRecipientViewHolder extends BaseViewHolder<ChatParticipant> {

    private final ImageView   displayPic;
    private final TextView    username;
    private final RadioButton radioButton;
    private String            selectedRecipient;

    private final String      RECIPIENT_EVERYONE = I18n.tr("Everyone");

    /**
     * @param rootView
     */
    public GiftRecipientViewHolder(View rootView) {
        super(rootView);
        displayPic = (ImageView) rootView.findViewById(R.id.display_pic);
        username = (TextView) rootView.findViewById(R.id.username);
        radioButton = (RadioButton) rootView.findViewById(R.id.radio_button);
    }

    @Override
    public void setData(ChatParticipant participant) {
        super.setData(participant);

        if (participant.getUsername().equals(RECIPIENT_EVERYONE)) {
            displayPic.setImageResource(R.drawable.ic_default_group);
        } else {
            ImageHandler.getInstance().loadDisplayPictureOfUser(displayPic, participant.getUsername(), Config.getInstance()
                    .getDisplayPicSizeNormal(), true);
        }

        username.setText(participant.getUsername());

        if (participant.getUsername().equals(selectedRecipient)) {
            radioButton.setChecked(true);
        } else {
            radioButton.setChecked(false);
        }
    }

    public void setSelected(String selectedRecipient) {
        this.selectedRecipient = selectedRecipient;
    }

}
