package com.projectgoth.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.projectgoth.R;
import com.projectgoth.b.data.Profile;
import com.projectgoth.datastore.UserDatastore;

public class UserMiniDetails extends LinearLayout {

    private final TextView               menuGifts;
    private final TextView               menuBadges;
    private final TextView               menuFans;

    public UserMiniDetails(Context context) {
        this(context, null);
    }

    public UserMiniDetails(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater.from(context).inflate(R.layout.user_mini_details, this, true);

        menuGifts = (TextView)findViewById(R.id.menu_gifts);
        menuBadges = (TextView)findViewById(R.id.menu_badges);
        menuFans = (TextView)findViewById(R.id.menu_fans);

    }

    @Override
    public void setOnClickListener(OnClickListener listener) {
        menuGifts.setOnClickListener(listener);
        menuBadges.setOnClickListener(listener);
        menuFans.setOnClickListener(listener);
    }

    public void updateMiniDetails(Profile profile) {
        if (profile != null) {
            menuGifts.setText(String.valueOf(profile.getNumOfGiftsReceived()));
            menuBadges.setText(String.valueOf(UserDatastore.getInstance().getUnlockedBadgesCounter(
                    profile.getUsername())));
            menuFans.setText(String.valueOf(profile.getNumOfFollowers()));
        }
    }
}
