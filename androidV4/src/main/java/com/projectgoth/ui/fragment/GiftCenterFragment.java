/**
 * Copyright (c) 2013 Project Goth
 *
 * GiftCenterFragment.java
 * Created 7 May, 2014, 1:59:21 pm
 */

package com.projectgoth.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import com.projectgoth.R;
import com.projectgoth.common.Theme;
import com.projectgoth.common.ThemeValues;
import com.projectgoth.datastore.ChatDatastore;
import com.projectgoth.i18n.I18n;
import com.projectgoth.model.ChatConversation;
import com.projectgoth.ui.activity.ActionHandler;
import com.projectgoth.ui.activity.FragmentHandler;
import com.projectgoth.ui.adapter.GiftCenterPagerAdapter;
import com.projectgoth.ui.widget.PagerSlidingTabStrip;

import java.util.ArrayList;

/**
 * @author dan
 * 
 */
public class GiftCenterFragment extends BaseDialogFragment implements OnClickListener, OnPageChangeListener {

    private PagerSlidingTabStrip   mTabs;
    private ImageView              storeButton;
    private ViewPager              viewPager;
    private GiftCenterPagerAdapter mAdapter;
    private String                 mConversationId;
    private String                 mInitialRecipient;
    //for start chat with a gift message, the conversation is not created yet
    private ArrayList<String>      mSelectedUsers;

    private ImageView              mCloseBtn;
    private ChatConversation       mConversation;
    private boolean                mIsFromSinglePost;

    public static final String     PARAM_CONVERSATION_ID   = "PARAM_CONVERSATION_ID";
    public static final String     PARAM_INITIAL_RECIPIENT = "PARAM_INITIAL_RECIPIENT";
    public static final String     PARAM_SELECTED_USERS    = "PARAM_SELECTED_USERS";

    @Override
    protected void readBundleArguments(Bundle args) {
        super.readBundleArguments(args);
        mConversationId = args.getString(PARAM_CONVERSATION_ID);
        mInitialRecipient = args.getString(PARAM_INITIAL_RECIPIENT);
        mSelectedUsers = args.getStringArrayList(PARAM_SELECTED_USERS);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_gift_center;
    }
    
    @Override
    public void onAttach(Activity activity) {
        setShouldUpdateActionBarOnAttach(false);
        super.onAttach(activity);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mConversation = ChatDatastore.getInstance().getChatConversationWithId(mConversationId);

        viewPager = (ViewPager) view.findViewById(R.id.pager);
        mAdapter = new GiftCenterPagerAdapter(getChildFragmentManager(), getActivity(), mInitialRecipient);
        mAdapter.setConversationId(mConversationId);
        mAdapter.setSelectedUsers(mSelectedUsers);
        viewPager.setAdapter(mAdapter);

        mTabs = (PagerSlidingTabStrip) view.findViewById(R.id.tabs);
        mTabs.setViewPager(viewPager);
        mTabs.setBackgroundColor(Theme.getColor(ThemeValues.PAGER_TAB_STRIP_BG_COLOR));

        viewPager.setBackgroundColor(Theme.getColor(ThemeValues.LIGHT_BACKGROUND_COLOR));
        viewPager.setOnPageChangeListener(this);

        storeButton = (ImageView) view.findViewById(R.id.store_button);
        storeButton.setBackgroundColor(Theme.getColor(ThemeValues.ORANGE_NORMAL_BG));
        storeButton.setOnClickListener(this);

        ImageView headerIcon = (ImageView) view.findViewById(R.id.icon);
        TextView headerTitle = (TextView) view.findViewById(R.id.title);
        TextView headerStep = (TextView) view.findViewById(R.id.step_count);

        headerIcon.setImageResource(R.drawable.ad_gift_grey);
        headerTitle.setText(I18n.tr("Select gift"));

        if (mConversation != null) {
            if (mConversation.isMigPrivateChat()) {
                headerStep.setText(String.format(I18n.tr("%d of %d"), 1, 2));
            } else if (mConversation.isMigGroupChat() || mConversation.isChatroom()) {
                headerStep.setText(String.format(I18n.tr("%d of %d"), 1, 3));
            }
        } else if (mSelectedUsers != null && !mSelectedUsers.isEmpty()) {
            if (mSelectedUsers.size() == 1) {
                headerStep.setText(String.format(I18n.tr("%d of %d"), 1, 2));
            } else if (mSelectedUsers.size() > 1) {
                headerStep.setText(String.format(I18n.tr("%d of %d"), 1, 3));
            }
        }

        mCloseBtn = (ImageView) view.findViewById(R.id.close_button);
        mCloseBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.store_button:
                ActionHandler.getInstance().displayStoreFromChat(getActivity(), mInitialRecipient);
                FragmentHandler.getInstance().clearBackStack();
                break;
            case R.id.close_button:
                FragmentHandler.getInstance().clearBackStack();
                break;
            default:
                break;
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        mAdapter.setCurrentPos(position);
        mTabs.notifyDataSetChanged();
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

}
