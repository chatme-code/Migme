package com.projectgoth.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.projectgoth.R;
import com.projectgoth.common.Theme;
import com.projectgoth.common.ThemeValues;
import com.projectgoth.i18n.I18n;
import com.projectgoth.ui.activity.FragmentHandler;
import com.projectgoth.ui.adapter.GiftCenterPagerAdapter;
import com.projectgoth.ui.widget.PagerSlidingTabStrip;

import java.util.ArrayList;

/**
 * Created by danielchen on 15/5/21.
 */
public class SinglePostGiftFragment extends BaseDialogFragment implements View.OnClickListener, ViewPager.OnPageChangeListener {
    private PagerSlidingTabStrip   mTabs;
    private ImageView              mStoreButton;
    private ViewPager              mViewPager;
    private GiftCenterPagerAdapter mAdapter;
    private String                 mRootPostId;
    private String                 mParentPostId;

    //for start chat with a gift message, the conversation is not created yet
    private ArrayList<String>      mSelectedUsers;

    private ImageView              mCloseBtn;
    public static final String     PARAM_IS_FROM_SINGLE_POST   = "PARAM_IS_FROM_SINGLE_POST";
    public static final String     PARAM_POST_ROOT_ID          = "PARAM_POST_ROOT_ID";
    public static final String     PARAM_POST_PARENT_ID        = "PARAM_POST_PARENT_ID";


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
    protected void readBundleArguments(Bundle args) {
        super.readBundleArguments(args);
        mRootPostId = args.getString(PARAM_POST_ROOT_ID);
        mParentPostId = args.getString(PARAM_POST_PARENT_ID);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mViewPager = (ViewPager) view.findViewById(R.id.pager);
        mAdapter = new GiftCenterPagerAdapter(getChildFragmentManager(), getActivity(), "", mRootPostId, mParentPostId);
        mAdapter.setSelectedUsers(mSelectedUsers);
        mViewPager.setAdapter(mAdapter);

        mTabs = (PagerSlidingTabStrip) view.findViewById(R.id.tabs);
        mTabs.setViewPager(mViewPager);
        mTabs.setBackgroundColor(Theme.getColor(ThemeValues.PAGER_TAB_STRIP_BG_COLOR));

        mViewPager.setBackgroundColor(Theme.getColor(ThemeValues.LIGHT_BACKGROUND_COLOR));
        mViewPager.setOnPageChangeListener(this);

        mStoreButton = (ImageView) view.findViewById(R.id.store_button);
        mStoreButton.setVisibility(View.GONE);

        ImageView headerIcon = (ImageView) view.findViewById(R.id.icon);
        TextView headerTitle = (TextView) view.findViewById(R.id.title);
        TextView headerStep = (TextView) view.findViewById(R.id.step_count);

        headerIcon.setImageResource(R.drawable.ad_gift_grey);
        headerTitle.setText(I18n.tr("Select gift"));
        headerStep.setText(String.format(I18n.tr("%d of %d"), 1, 2));

        mCloseBtn = (ImageView) view.findViewById(R.id.close_button);
        mCloseBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
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
