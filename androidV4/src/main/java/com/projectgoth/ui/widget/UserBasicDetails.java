
package com.projectgoth.ui.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.projectgoth.R;
import com.projectgoth.b.data.Labels;
import com.projectgoth.b.data.Profile;
import com.projectgoth.blackhole.enums.ImType;
import com.projectgoth.blackhole.enums.PresenceType;
import com.projectgoth.common.Constants;

public class UserBasicDetails extends LinearLayout {

    private final UserImageView          mUserImageView;

    private final UsernameWithLabelsView mUsernameWithLabelsView;

    private final TextView               migLevel;

    private final ImageView              closeIcon;

    public UserBasicDetails(Context context) {
        this(context, null);
    }

    public UserBasicDetails(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater.from(context).inflate(R.layout.user_basic_details, this, true);

        mUserImageView = (UserImageView) findViewById(R.id.user_image);
        mUsernameWithLabelsView = (UsernameWithLabelsView) findViewById(R.id.username_with_labels);
        migLevel = (TextView) findViewById(R.id.miglevel_num);
        closeIcon = (ImageView) findViewById(R.id.close_button);
    }

    public void setSelfImage(final Profile profile) {
        mUserImageView.setSelfImage(profile);
    }

    public void setUserImage(final Profile profile) {
        mUserImageView.setUserImage(profile);
    }

    public void showMainIcon() {
        mUserImageView.setVisibility(View.VISIBLE);
    }

    public void hideMainIcon() {
        mUserImageView.setVisibility(View.GONE);
    }

    public void setPresenceIcon(PresenceType presence) {
        mUserImageView.setVisibility(View.VISIBLE);
        mUserImageView.setPresenceImage(presence);
    }

    public void setIMPresenceIcon(ImType imType, PresenceType presence) {
        mUserImageView.setVisibility(View.VISIBLE);
        mUserImageView.setPresenceImage(imType, presence);
    }

    public void hideUsername() {
        mUsernameWithLabelsView.setUsername(Constants.BLANKSTR);
    }

    public void setUsername(String username) {
        mUsernameWithLabelsView.setUsername(username);
    }

    public void setUsernameColor(int color) {
        mUsernameWithLabelsView.setTextColor(color);
    }

    public void setLabels(Labels labels) {
        setLabels(labels, true);
    }
    
    public void setLabels(Labels labels, boolean shouldSetUsernameColorFromLabels) {
        mUsernameWithLabelsView.setLabels(labels, shouldSetUsernameColorFromLabels);
    }

    public void setMigLevel(String migLevel) {
        if (!TextUtils.isEmpty(migLevel)) {
            this.migLevel.setText(migLevel);
            this.migLevel.setVisibility(View.VISIBLE);
        } else {
            hideMigLevel();
        }
    }

    public void hideMigLevel() {
        migLevel.setVisibility(View.GONE);
    }

    public void showCloseIcon() {
        closeIcon.setVisibility(View.VISIBLE);
    }

    public void hideCloseIcon() {
        closeIcon.setVisibility(View.GONE);
    }

    public boolean isCloseIconVisible() {
        if (closeIcon.getVisibility() == View.VISIBLE) {
            return true;
        }
        return false;
    }
}
