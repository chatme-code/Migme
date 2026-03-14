
package com.projectgoth.ui.holder;

import android.graphics.drawable.GradientDrawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.datastore.UserDatastore;
import com.projectgoth.imagefetcher.UIUtils;
import com.projectgoth.nemesis.model.Friend;
import com.projectgoth.nemesis.model.User;
import com.projectgoth.ui.fragment.FriendListFragment.FriendListItemActionType;
import com.projectgoth.ui.widget.UserImageView;

/**
 * @author dangui
 * 
 */
public class FriendViewHolder extends BaseViewHolder<Friend> {

    private RelativeLayout container;
    private UserImageView  userImageView;
    private TextView       IMChatIcon;
    private TextView       username;
    private TextView       subTitle;
    private ImageView      check;

    private String         filterKeyword;

    public FriendViewHolder(View view, FriendListItemActionType actionType) {
        super(view);

        container = (RelativeLayout) view.findViewById(R.id.container);
        userImageView = (UserImageView) view.findViewById(R.id.user_image);
        IMChatIcon = (TextView) view.findViewById(R.id.IM_chat_icon);
        username = (TextView) view.findViewById(R.id.username);
        check = (ImageView) view.findViewById(R.id.check);
        subTitle = (TextView) view.findViewById(R.id.subtitle);
        // for clicking the icon to open the profile page
        userImageView.setOnClickListener(this);

        // set the sub view to be unClickable so that the onClick of rootview
        // can be called
        username.setClickable(false);
        username.setLongClickable(false);

    }

    @Override
    public void setData(Friend friend) {
        super.setData(friend);

        if (!friend.isIMContact()) {
            userImageView.setUser(friend);
            userImageView.setVisibility(View.VISIBLE);
            IMChatIcon.setVisibility(View.INVISIBLE);
        } else {
            // first character
            IMChatIcon.setText(String.valueOf(Character.toUpperCase(friend.getDisplayName().charAt(0))));
            // set color
            int color = ApplicationEx.getColor(friend.isSelectable() ? R.color.IM_contact_icon_selectable
                    : R.color.IM_contact_icon_selectunable);
            GradientDrawable bg = (GradientDrawable) IMChatIcon.getBackground();
            bg.setColor(color);
            userImageView.setVisibility(View.INVISIBLE);
            IMChatIcon.setVisibility(View.VISIBLE);
        }

        // set display name
        setDisplayName(friend.getDisplayName(), filterKeyword);

        if (!TextUtils.isEmpty(friend.getStatusMessage())) {
            subTitle.setVisibility(View.VISIBLE);
            subTitle.setSelected(true);
            subTitle.setText(friend.getStatusMessage());
        } else {
            subTitle.setVisibility(View.GONE);
        }

        if (friend.isSelectable()) {
            final User user = UserDatastore.getInstance().getUserWithUsername(friend.getUsername(), false);
            if (user != null && user.getProfile() != null) {
                username.setTextColor(UIUtils.getUsernameColorFromLabels(user.getProfile().getLabels(), false));
            } else {
                //default color before the Profile loaded
                username.setTextColor(ApplicationEx.getColor(R.color.friend_name_selectable));
            }
        } else {
            username.setTextColor(ApplicationEx.getColor(R.color.friend_name_unselectable));
        }

        // selected status
        if (friend.isFriendSelected()) {
            container.setBackgroundResource(R.drawable.friend_item_selected_divider);
            check.setVisibility(View.VISIBLE);
        } else {
            container.setBackgroundResource(R.drawable.friend_item_unselected_divider);
            check.setVisibility(View.GONE);
        }

    }

    public void setFilterKeyword(String filterKeyword) {
        this.filterKeyword = filterKeyword;
    }

    private void setDisplayName(String displayName, String filterKeyword) {
        username.setText(null);
        if (TextUtils.isEmpty(filterKeyword)) {
            username.setText(displayName);
        } else {
            Spannable spannable = new SpannableString(displayName);
            BackgroundColorSpan span = new BackgroundColorSpan(
                    ApplicationEx.getColor(R.color.filter_keyword_highlight_bg));

            int start = displayName.indexOf(filterKeyword);
            if (start != -1) {
                spannable.setSpan(span, start, start + filterKeyword.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                username.setText(spannable);
            } else {
                username.setText(displayName);
            }
        }

    }

}
