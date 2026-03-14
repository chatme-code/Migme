/**
 * Copyright (c) 2013 Project Goth
 *
 * ProfileFragment.java
 * Created Aug 12, 2013, 2:53:57 PM
 */

package com.projectgoth.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.mig33.diggle.events.Events;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.Profile;
import com.projectgoth.b.enums.UserProfileGenderEnum;
import com.projectgoth.b.enums.UserProfileRelationshipEnum;
import com.projectgoth.common.Constants;
import com.projectgoth.common.Tools;
import com.projectgoth.common.WebURL;
import com.projectgoth.datastore.Session;
import com.projectgoth.datastore.UserDatastore;
import com.projectgoth.i18n.I18n;
import com.projectgoth.imagefetcher.ImageHandler;
import com.projectgoth.ui.activity.ActionHandler;
import com.projectgoth.ui.activity.CustomActionBarConfig;
import com.projectgoth.ui.activity.CustomActionBarConfig.NavigationButtonState;
import com.projectgoth.ui.widget.ProfileProperty;
import com.projectgoth.util.StringUtils;

/**
 * @author houdangui
 * 
 */
public class FullProfileFragment extends BaseFragment implements OnClickListener {

    public static final String PARAM_USERNAME   = "username";

    private Profile            profile;
    private String             username;
    private boolean            isSelf;

    private ImageView          fullBodyAvatar;
    private ProfileProperty    joinDate;
    private ProfileProperty    country;
    private ProfileProperty    gender;
    private ProfileProperty    relationship;
    private ProfileProperty    birthdate;
    private ProfileProperty    aboutMe;
    private ProfileProperty    interests;
    private ProfileProperty    realName;
    private ProfileProperty    emailAddress;
    private ProfileProperty    school;
    private ProfileProperty    company;

    private boolean     shouldForceFetch = false;

    @Override
    protected void readBundleArguments(Bundle bundle) {
        super.readBundleArguments(bundle);
        username = bundle.getString(PARAM_USERNAME);
        isSelf = Session.getInstance().getUsername().equals(username) ? true : false;
        profile = UserDatastore.getInstance().getProfileWithUsername(username, shouldForceFetch);
        shouldForceFetch = false;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_full_profile;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fullBodyAvatar = (ImageView) view.findViewById(R.id.full_body_avatar);
        if (isSelf) {
            fullBodyAvatar.setOnClickListener(this);
        }

        joinDate = (ProfileProperty) view.findViewById(R.id.joined_date);
        country = (ProfileProperty) view.findViewById(R.id.country);
        gender = (ProfileProperty) view.findViewById(R.id.gender);
        relationship = (ProfileProperty) view.findViewById(R.id.relationship);
        birthdate = (ProfileProperty) view.findViewById(R.id.birthdate);
        aboutMe = (ProfileProperty) view.findViewById(R.id.about_me);
        interests = (ProfileProperty) view.findViewById(R.id.interests);
        realName = (ProfileProperty) view.findViewById(R.id.real_name);
        emailAddress = (ProfileProperty) view.findViewById(R.id.email_address);
        school = (ProfileProperty) view.findViewById(R.id.school);
        company = (ProfileProperty) view.findViewById(R.id.company);

        if (isSelf) {
            RelativeLayout editContainer = (RelativeLayout) view.findViewById(R.id.edit_profile);
            TextView editTextview = (TextView) view.findViewById(R.id.edit_text);
            View upSeparator = view.findViewById(R.id.separator_up);
            View downSeparator = view.findViewById(R.id.separator_down);
            ImageView editIcon = (ImageView) view.findViewById(R.id.edit_icon);

            editIcon.setOnClickListener(this);
            editTextview.setText(I18n.tr("Edit"));
            
            editContainer.setVisibility(View.VISIBLE);
            upSeparator.setVisibility(View.VISIBLE);
            downSeparator.setVisibility(View.VISIBLE);
        }

        updateProfileData();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateProfileData();
    }

    @Override
    protected void registerReceivers() {
        registerEvent(Events.Profile.RECEIVED);
    }

    @Override
    protected void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (action.equals(Events.Profile.RECEIVED)) {
            Bundle data = intent.getExtras();
            String username = data.getString(Events.User.Extra.USERNAME);
            if (this.username.equalsIgnoreCase(username)) {
                updateProfileData();
            }
        }
    }

    private void updateProfileData() {
        profile = UserDatastore.getInstance().getProfileWithUsername(username, shouldForceFetch);
        if (profile != null) {
            // full body avatar
            int width = Math.min(ApplicationEx.getDimension(R.dimen.full_avatar_width),
                    Constants.FULL_BODY_AVATAR_WIDTH);
            int height = Math.min(ApplicationEx.getDimension(R.dimen.full_avatar_height),
                    Constants.FULL_BODY_AVATAR_HEIGHT);

            String fullAvatarUrl = ImageHandler
                    .constructFullImageLink(profile.getAvatarBodyUuid(), width, height, 1, 1);
            ImageHandler.getInstance().loadImageFromUrl(fullBodyAvatar, fullAvatarUrl, false, R.drawable.full_body_avatar_loading);

            // joined date
            if (!TextUtils.isEmpty(profile.getDateRegistered())) {
                joinDate.setName(I18n.tr("Joined mig on"));
                joinDate.setContent(Tools.formatDateProfile(profile.getDateRegistered()));
            }

            // gender
            if (profile.getGender() != null) {
                gender.setName(I18n.tr("Gender"));
                String genderString = Constants.BLANKSTR;
                if (profile.getGender() == UserProfileGenderEnum.MALE) {
                    genderString = I18n.tr("Male");
                } else if (profile.getGender() == UserProfileGenderEnum.FEMALE) {
                    genderString = I18n.tr("Female");
                }
                gender.setContent(genderString);
            }

            // country
            if (!TextUtils.isEmpty(profile.getCountry())) {
                country.setName(I18n.tr("Country"));
                country.setContent(profile.getCountry());
            }

            // relationship
            if (profile.getApplication() != null) {
                relationship.setName(I18n.tr("Relationship status"));
                UserProfileRelationshipEnum userRelationshipEnum = profile.getApplication();
                String strRelationship = Tools.getUserRelationshipEnumString(userRelationshipEnum);
                relationship.setContent(TextUtils.isEmpty(strRelationship) ? Constants.BLANKSTR : strRelationship);
            }

            // real name
            String strRealName = Constants.BLANKSTR;
            String firstName = profile.getFirstName();
            String lastName = profile.getLastName();

            if (!TextUtils.isEmpty(firstName)) {
                strRealName += firstName;
            }
            if (!TextUtils.isEmpty(lastName)) {
                if (!TextUtils.isEmpty(strRealName)) {
                    strRealName += Constants.SPACESTR;
                }
                strRealName += lastName;
            }

            if (!TextUtils.isEmpty(strRealName)) {
                realName.setName(I18n.tr("Real name"));
                realName.setContent(strRealName);
            }

            // about me
            if (!TextUtils.isEmpty(profile.getAboutMe())) {
                aboutMe.setName(I18n.tr("About me"));
                String strAboutMe = StringUtils.decodeHtml(profile.getAboutMe());
                aboutMe.setContent(TextUtils.isEmpty(strAboutMe) ? Constants.BLANKSTR : strAboutMe);
            }

            // interests
            if (!TextUtils.isEmpty(profile.getInterests())) {
                interests.setName(I18n.tr("Interests"));
                String strInterests = profile.getInterests();
                interests.setContent(TextUtils.isEmpty(strInterests) ? Constants.BLANKSTR : strInterests);
            }

            // email
            if (!TextUtils.isEmpty(profile.getExternalEmail())) {
                emailAddress.setName(I18n.tr("Email address"));
                emailAddress.setContent(profile.getExternalEmail());
            }

            // school
            if (!TextUtils.isEmpty(profile.getSchools())) {
                school.setName(I18n.tr("Studied at"));
                school.setContent(profile.getSchools());
            }

            // company
            if (!TextUtils.isEmpty(profile.getCompanies())) {
                company.setName(I18n.tr("Worked at"));
                company.setContent(profile.getCompanies());
            }

            // birthdate
            if (!TextUtils.isEmpty(profile.getDateOfBirth())) {
                birthdate.setName(I18n.tr("Date of birth"));
                birthdate.setContent(Tools.formatDateProfile(profile.getDateOfBirth()));
            }
        }
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        switch (viewId) {
            case R.id.full_body_avatar:
                shouldForceFetch = true;
                ActionHandler.getInstance().displayBrowser(getActivity(), WebURL.URL_MY_AVATAR, I18n.tr("Edit avatar"),
                        R.drawable.ad_user_white);
                break;
            case R.id.edit_icon:
                shouldForceFetch = true;
                ActionHandler.getInstance().displayBrowser(getActivity(), WebURL.URL_EDITPROFILE,
                        I18n.tr("Edit profile"), R.drawable.ad_user_white);
                break;
        }
    }

    @Override
    protected String getTitle() {
        return I18n.tr("Full profile");
    }

    @Override
    protected int getTitleIcon() {
        return R.drawable.ad_user_white;
    }

    @Override
    public CustomActionBarConfig getActionBarConfig() {
        CustomActionBarConfig config = new CustomActionBarConfig();
        config.setNavigationButtonState(NavigationButtonState.BACK);
        return config;
    }

}
