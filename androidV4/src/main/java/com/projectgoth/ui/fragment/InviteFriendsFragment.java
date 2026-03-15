/**
 * Copyright (c) 2013 Project Goth
 *
 * InviteFriendsFragment.java
 * Created May 6, 2014, 10:36:35 AM
 */

package com.projectgoth.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import com.projectgoth.R;
import com.projectgoth.common.Constants;
import com.projectgoth.common.WebURL;
import com.projectgoth.events.GAEvent;
import com.projectgoth.i18n.I18n;
import com.projectgoth.imagefetcher.ImageHandler;
import com.projectgoth.ui.activity.ActionHandler;
import com.projectgoth.ui.activity.CustomActionBarConfig;
import com.projectgoth.ui.activity.CustomActionBarConfig.NavigationButtonState;
import com.projectgoth.ui.activity.FragmentHandler;
import com.projectgoth.ui.fragment.GlobalSearchFragment.SearchType;

/**
 * @author angelorohit
 */
public class InviteFriendsFragment extends BaseFragment implements OnClickListener {

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final TextView searchByUsername = (TextView) view.findViewById(R.id.search_by_contacts);
        searchByUsername.setText(I18n.tr("Contacts"));
        searchByUsername.setOnClickListener(this);
        
        final TextView inviteViaFacebook = (TextView) view.findViewById(R.id.invite_via_facebook);
        inviteViaFacebook.setText(I18n.tr("Facebook"));
        inviteViaFacebook.setOnClickListener(this);

        final TextView inviteViaEMail = (TextView) view.findViewById(R.id.invite_via_email);
        inviteViaEMail.setText(I18n.tr("Email"));
        inviteViaEMail.setOnClickListener(this);
        ImageHandler.tintDrawable(inviteViaEMail.getCompoundDrawables()[0], R.color.tint_lightgray);

        final TextView recommendedPeople = (TextView) view.findViewById(R.id.recommended_people);
        recommendedPeople.setText(I18n.tr("Recommended people"));

        initRecommendedFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_invite_friends;
    }

    protected void initRecommendedFragment() {
        ProfileListFragment fragment = FragmentHandler.getInstance().getRecommendedUsersLiteFragment();
        fragment.setShouldUpdateActionBarOnAttach(false);
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction =
                fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.ad_recommended_fragment, fragment);
        fragmentTransaction.commit();
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.view.View.OnClickListener#onClick(android.view.View)
     */
    @Override
    public void onClick(View v) {
        int viewId = v.getId();

        switch (viewId) {
            case R.id.search_by_contacts:
                ActionHandler.getInstance().displayRecommendedContacts(getActivity());
                break;
            case R.id.invite_via_facebook:
                GAEvent.Chat_ClickFacebook.send();
                ActionHandler.getInstance().displayBrowserAsDialog(getActivity(), WebURL.URL_INVITE_VIA_FACEBOOK, 
                        I18n.tr("Facebook"), 
                        getResources().getDrawable(R.drawable.ad_facebook_blue));
                break;
            case R.id.invite_via_email:
                GAEvent.Chat_ClickEmail.send();
                ActionHandler.getInstance().displayBrowserAsDialog(getActivity(), WebURL.URL_INVITE_VIA_EMAIL,
                        I18n.tr("Invite friends"),
                        getResources().getDrawable(R.drawable.ad_mail_grey));
                break;
        }
    }
    
    @Override
    public CustomActionBarConfig getActionBarConfig() {
        CustomActionBarConfig config = new CustomActionBarConfig();
        config.setNavigationButtonState(NavigationButtonState.BACK);
        config.setShowSearchButton(true);
        return config;
    }
    
    @Override
    protected String getTitle() {
        return I18n.tr("Add friends");
    }
    
    @Override
    protected int getTitleIcon() {
        return R.drawable.ad_useradd_white;
    }

    @Override
    public void onSearchButtonPressed() {
        GAEvent.Chat_ClickSearchUsername.send();
        ActionHandler.getInstance().displayGlobalSearch(getActivity(), SearchType.PEOPLE, Constants.BLANKSTR);
    }
}
