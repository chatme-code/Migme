/**
 * Copyright (c) 2013 Project Goth
 *
 * ParticipantViewHolder.java
 * Created Aug 5, 2013, 2:20:25 PM
 */

package com.projectgoth.ui.holder;

import android.view.View;
import android.widget.TextView;
import com.projectgoth.R;
import com.projectgoth.i18n.I18n;

/**
 * @author sarmadsangi
 * 
 */
public class ParticipantCategoryViewHolder extends BaseViewHolder<Integer> {

    protected final TextView  participantText;

    public ParticipantCategoryViewHolder(View view) {
        super(view);

        participantText = (TextView) view.findViewById(R.id.participant_text);
    }

    @Override
    public void setData(Integer participantCount) {
        super.setData(participantCount);

        String text = String.format(I18n.tr("Participants (%d)"), participantCount);
        participantText.setText(text);
    }

}
