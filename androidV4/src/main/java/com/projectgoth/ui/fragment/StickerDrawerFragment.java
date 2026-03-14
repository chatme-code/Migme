/**
 * Copyright (c) 2013 Project Goth
 *
 * DrawerFragment.java
 * Created Jul 16, 2013, 6:13:14 PM
 */

package com.projectgoth.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.common.Theme;
import com.projectgoth.common.ThemeValues;
import com.projectgoth.datastore.EmoticonDatastore;
import com.projectgoth.enums.AttachmentType;
import com.projectgoth.nemesis.model.BaseEmoticonPack;
import com.projectgoth.ui.activity.ActionHandler;
import com.projectgoth.ui.adapter.AttachmentTabAdapter;
import com.projectgoth.ui.fragment.AttachmentPagerFragment.BaseAttachmentFragmentListener;
import com.projectgoth.ui.widget.HorizontalListViewEx;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment that displays the attachment panel which includes the tab scroller
 * and the grid menu
 * 
 * @author mapet
 * @author dan
 * 
 */
public class StickerDrawerFragment extends BaseFragment
        implements HorizontalListViewEx.OnItemClickListener,
                   OnClickListener {

    private LinearLayout                   mStickerTabsContainer;
    private HorizontalListViewEx           mStickerTabs;
    private ImageView                      mStickerStore;
    private AttachmentTabAdapter           mStickerTabsAdapter;

    private List<BaseEmoticonPack>         mStickerTabsData      = new ArrayList<BaseEmoticonPack>();

    private int                            mSelectedStickerTab   = 0;

    private BaseAttachmentFragmentListener mAttachmentListener;

    private int                            mDrawerHeight = -1;
    private String                         mInitialRecipient;

    private static final String            PARAM_ATTACHMENT_TYPE = "ATTACHMENT_TYPE";
    private static final String            PARAM_PACK_ID         = "PACK_ID";

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_sticker_drawer;
    }
    
    @Override
    public void onAttach(Activity activity) {
        setShouldUpdateActionBarOnAttach(false);
        super.onAttach(activity);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (mDrawerHeight > 0) {
            LayoutParams params = view.getLayoutParams();
            params.height = mDrawerHeight;
            view.requestLayout();
        }

        mStickerTabsContainer = (LinearLayout) view.findViewById(R.id.sticker_tabs_container);
        mStickerTabsContainer.setBackgroundColor(Theme.getColor(ThemeValues.CHAT_INPUT_TAB_BG_COLOR));
        mStickerTabs = (HorizontalListViewEx) view.findViewById(R.id.sticker_tabs);
        mStickerTabs.setOnItemClickListener(this);
        mStickerStore = (ImageView) view.findViewById(R.id.sticker_store);
        mStickerStore.setOnClickListener(this);

        mStickerTabsAdapter = new AttachmentTabAdapter();
        mStickerTabsAdapter.setPadding(ApplicationEx.getDimension(R.dimen.sticker_tab_padding));

        refreshTabData();

        if (!mStickerTabsData.isEmpty()) {
            refreshDrawerFragment(mStickerTabsData.get(mSelectedStickerTab).getId());
        }
    }

    private void refreshTabData() {

        // TODO: remove this once push notifications are available. 
        // this is an extra network call which needs to be optimized
        // AD-864: temp fix ticket
        // SE-414: server push notification ticket
        EmoticonDatastore.getInstance().requestStickerPackList();
        
        // get the date of sticker packs
        mStickerTabsData = EmoticonDatastore.getInstance().getMyEnabledStickerPacks();
        mStickerTabsAdapter.setAttachmentTabList(mStickerTabsData);
        mStickerTabsAdapter.setSelected(mSelectedStickerTab);
        // display sticker packs bar
        mStickerTabs.setAdapter(mStickerTabsAdapter);
    }

    private void refreshDrawerFragment(int tabType) {
        AttachmentPagerFragment mDrawerGrid = new AttachmentPagerFragment();
        mDrawerGrid.setAttachmentListener(mAttachmentListener);

        Bundle args = new Bundle();
        args.putInt(PARAM_ATTACHMENT_TYPE, tabType);
        args.putInt(PARAM_PACK_ID, tabType);
        mDrawerGrid.setArguments(args);

        addChildFragment(R.id.grid, mDrawerGrid);
    }

    /*
     * callback of clicking the sticker tabs
     */
    @Override
    public void onItemClicked(HorizontalListViewEx adapterView, View view, int position, long id) {
        mSelectedStickerTab = position;

        if (mSelectedStickerTab >= 0 && mSelectedStickerTab < mStickerTabsData.size()) {
            int tabId = 0;

            try {
                tabId = mStickerTabsData.get(mSelectedStickerTab).getId();

                if (tabId == AttachmentType.STORE_STICKER.value) {
                    showStickerStore();

                } else {
                    mStickerTabsAdapter.setSelected(mSelectedStickerTab);
                    mStickerTabs.setAdapter(mStickerTabsAdapter);
                    refreshDrawerFragment(tabId);
                }
            } catch (ArrayIndexOutOfBoundsException aioobe) {
            }
        }
    }

    public void setAttachmentListener(BaseAttachmentFragmentListener mAttachmentListener) {
        this.mAttachmentListener = mAttachmentListener;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.sticker_store:
                showStickerStore();
                break;
            default:
                break;
        }
    }

    public void setInitialRecipient(String initialRecipient) {
        this.mInitialRecipient = initialRecipient;
    }

    private void showStickerStore() {
        // show the sticker tab
        ActionHandler.getInstance().displayStore(getActivity(), 1, mInitialRecipient);
    }

    public void setDrawerHeight(int drawerHeight) {
        this.mDrawerHeight = drawerHeight;
    }
}
