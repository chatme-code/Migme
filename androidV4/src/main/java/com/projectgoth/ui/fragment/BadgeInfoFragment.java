/**
 * Copyright (c) 2013 Project Goth
 *
 * BadgeInfoFragment.java
 * Created Aug 23, 2013, 3:16:57 PM
 */

package com.projectgoth.ui.fragment;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import com.projectgoth.R;
import com.projectgoth.b.data.Badge;
import com.projectgoth.common.Constants;
import com.projectgoth.common.Tools;
import com.projectgoth.datastore.UserDatastore;
import com.projectgoth.i18n.I18n;
import com.projectgoth.imagefetcher.ImageHandler;

import java.util.List;

/**
 * @author dangui
 * 
 */
public class BadgeInfoFragment extends BaseDialogFragment implements OnClickListener {

    public static final String PARAM_USERNAME = "USERNAME";
    public static final String PARAM_BADGE_ID = "BADGE_ID";

    private ImageView          badgeIcon;
    private TextView           badgeState;
    private TextView           badgeName;
    private TextView           badgeDescription;
    private TextView           badgeValidityContent;
    private ImageView          closeIcon;

    private Badge              badge;

    /**
     * @see com.projectgoth.ui.fragment.BaseFragment#readBundleArguments(android.os.Bundle)
     */
    @Override
    protected void readBundleArguments(Bundle args) {
        super.readBundleArguments(args);

        String username = args.getString(PARAM_USERNAME);
        Integer badgeId = args.getInt(PARAM_BADGE_ID);
        List<Badge> badgeList = UserDatastore.getInstance().getBadgesForUserWithName(username, false);
        if (badgeList != null) {
            for (Badge badge : badgeList) {
                if (badge.getId().equals(badgeId)) {
                    this.badge = badge;
                }
            }
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_badge_info;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        badgeIcon = (ImageView) view.findViewById(R.id.badge_icon);
        badgeState = (TextView) view.findViewById(R.id.badge_state);
        badgeName = (TextView) view.findViewById(R.id.badge_name);
        badgeDescription = (TextView) view.findViewById(R.id.badge_description);
        badgeValidityContent = (TextView) view.findViewById(R.id.badge_validity_content);
        closeIcon = (ImageView) view.findViewById(R.id.close_button);

        closeIcon.setOnClickListener(this);

        updateData();
    }

    private final void updateData() {
        if (badge != null) {

            // badge state
            if (badge.getUnlockedTimestamp() != null) {
                badgeState.setText(I18n.tr("Unlocked"));
                // badge icon
                int size = Constants.BADGES_SIZE_LARGE;
                String url = Tools.constructBadgeUrl(badge.getImageName(), size, false);
                ImageHandler.getInstance().loadImageFromUrl(badgeIcon, url, false, R.drawable.ad_loadstatic_grey);
            } else {
                badgeState.setText(I18n.tr("Locked"));
                badgeIcon.setImageResource(R.drawable.ad_badgelock);
            }

            badgeName.setText(badge.getName());
            badgeDescription.setText(badge.getDescription());
            badgeValidityContent.setText(String.format(I18n.tr("Validity: %s"), badge.getValidity()));
        }
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        
        if (viewId == R.id.close_button) {
            closeFragment();
        }
    }

}
