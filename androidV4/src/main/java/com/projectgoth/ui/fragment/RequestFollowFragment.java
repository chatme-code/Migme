/**
 * Copyright (c) 2013 Project Goth
 *
 * RequestFollowFragment.java
 * Created Apr 15, 2014, 1:58:14 PM
 */

package com.projectgoth.ui.fragment;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.projectgoth.R;
import com.projectgoth.b.data.Profile;
import com.projectgoth.b.enums.UserProfileDisplayPictureChoiceEnum;
import com.projectgoth.common.Config;
import com.projectgoth.common.Tools;
import com.projectgoth.datastore.UserDatastore;
import com.projectgoth.i18n.I18n;
import com.projectgoth.imagefetcher.ImageHandler;
import com.projectgoth.ui.activity.ActionHandler;
import com.projectgoth.ui.widget.ButtonEx;

/**
 * @author mapet
 * 
 */
public class RequestFollowFragment extends BaseDialogFragment implements OnClickListener {

    private Profile            profile;
    private String             username;

    private ImageView          iconMain;
    private TextView           title;
    private TextView           subTitle;
    private TextView           infoText;
    private TextView           infoSubText;
    private ButtonEx           sendGiftBtn;

    public static final String PARAM_USERNAME   = "PARAM_USERNAME";

    @Override
    protected void readBundleArguments(Bundle args) {
        super.readBundleArguments(args);
        username = args.getString(PARAM_USERNAME);
        
        // TODO: this fragment should cater for null profile, 
        // since there is a change a profile is not available yet
        // -> add receiver for profile received and reload/reinitialize UI
        profile = UserDatastore.getInstance().getProfileWithUsername(username, false);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_request_follow;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        iconMain = (ImageView) view.findViewById(R.id.icon_main);
        title = (TextView) view.findViewById(R.id.title);
        subTitle = (TextView) view.findViewById(R.id.subtitle);
        infoText = (TextView) view.findViewById(R.id.info_text);
        infoSubText = (TextView) view.findViewById(R.id.info_subtext);
        sendGiftBtn = (ButtonEx) view.findViewById(R.id.send_gift_button);

        UserProfileDisplayPictureChoiceEnum type = UserProfileDisplayPictureChoiceEnum.PROFILE_PICTURE;
        if (profile != null) {
            type = profile.getDisplayPictureType();
        }
        ImageHandler.getInstance().loadDisplayPictureOfUser(iconMain, username, type, Config.getInstance().getDisplayPicSizeNormal(),
                true);

        if (profile != null) {
            title.setText(profile.getUsername());
            subTitle.setText(Tools.formatProfileRemarks(profile));
        }
        infoText.setText(String.format(I18n.tr("Want to chat? Be friends with %s first."), username));
        infoSubText.setText(I18n.tr("Try sending a gift to sweeten the invite!"));
        sendGiftBtn.setText(I18n.tr("SEND GIFT"));
        
        sendGiftBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();

        switch (viewId) {
            case R.id.send_gift_button:
                ActionHandler.getInstance().displayStore(getActivity(), username);
                closeFragment();
                break;
        }
    }
}
