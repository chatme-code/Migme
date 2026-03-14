package com.projectgoth.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.nemesis.model.Friend;


/**
 * Created by houdangui on 16/10/14.
 */
public class SelectedItemLabel extends LinearLayout {

    private TextView mDisplayName;
    private Friend mFriend;

    public SelectedItemLabel(Context context) {
        this(context, null);
    }

    public SelectedItemLabel(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater.from(context).inflate(R.layout.selected_user_label, this, true);

        mDisplayName = (TextView) findViewById(R.id.display_name);

        int smallPadding = ApplicationEx.getDimension(R.dimen.small_padding);
        setPadding(smallPadding, smallPadding, smallPadding, smallPadding);
        setBackgroundResource(R.drawable.rounded_green_background);
    }

    public Friend getFriend() {
        return mFriend;
    }

    public void setFriend(Friend friend) {
        mFriend = friend;
        mDisplayName.setText(friend.getDisplayName());
    }

    public void setDisplayName(String displayName) {
        mDisplayName.setText(displayName);
    }

    public String getDisplayName() {
        return mDisplayName.getText().toString();
    }
}
