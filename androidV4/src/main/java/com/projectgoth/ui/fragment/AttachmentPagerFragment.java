/**
 * Copyright (c) 2013 Project Goth
 *
 * AttachmentFragment.java
 * Created Jul 24, 2013, 2:12:34 PM
 */

package com.projectgoth.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.View;

import com.mig33.diggle.events.Events;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.common.Logger;
import com.projectgoth.common.Theme;
import com.projectgoth.common.ThemeValues;
import com.projectgoth.datastore.EmoticonDatastore;
import com.projectgoth.enums.AttachmentType;
import com.projectgoth.ui.adapter.AttachmentPagerAdapter;
import com.viewpagerindicator.CirclePageIndicator;

import java.util.List;

/**
 * Fragment that handles the display of emoticons, gifts and stickers
 * 
 * @author mapet
 * @author dan
 * 
 */
public class AttachmentPagerFragment extends BaseViewPagerFragment {

    private AttachmentPagerAdapter         mPagerAdapter;
    private BaseAttachmentFragmentListener mAttachmentListener;
    
    private int                            mAttachmentType;
    private int                            mBaseEmoticonPackId;

    public static final String             PARAM_ATTACHMENT_TYPE = "ATTACHMENT_TYPE";
    public static final String             PARAM_PACK_ID         = "PACK_ID";
    public static final String             PARAM_PAGER_COLUMNS   = "PAGER_COLUMNS";

    /**
     * @see com.projectgoth.ui.fragment.BaseFragment#readBundleArguments(android.os.Bundle)
     */
    @Override
    protected void readBundleArguments(Bundle bundleArgs) {
        super.readBundleArguments(bundleArgs);
        
        mAttachmentType = bundleArgs.getInt(PARAM_ATTACHMENT_TYPE);
        mBaseEmoticonPackId = bundleArgs.getInt(PARAM_PACK_ID);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_attachment_pager;
    }
    
    @Override
    public void onAttach(Activity activity) {
        setShouldUpdateActionBarOnAttach(false);
        super.onAttach(activity);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        CirclePageIndicator indicator = (CirclePageIndicator) view.findViewById(R.id.indicator);
        View recentTabEmptyView = view.findViewById(R.id.recent_sticker_gift_empty);

        if (mAttachmentType == AttachmentType.RECENT_STICKER_GIFT.value
                && EmoticonDatastore.getInstance().isRecentStickersAndGiftsEmpty()) {
            viewPager.setVisibility(View.GONE);
            indicator.setVisibility(View.GONE);
            recentTabEmptyView.setVisibility(View.VISIBLE);
        } else {
            viewPager.setVisibility(View.VISIBLE);
            indicator.setVisibility(View.VISIBLE);
            recentTabEmptyView.setVisibility(View.GONE);

            indicator.setViewPager(viewPager);
            indicator.setSnap(true);

            indicator.setRadius(ApplicationEx.getDimension(R.dimen.attachment_circle_page_indicator_radius));
            indicator.setPageColor(Theme.getColor(ThemeValues.CIRCLE_PAGE_INDICATOR_COLOR));
            indicator.setFillColor(Theme.getColor(ThemeValues.CIRCLE_PAGE_INDICATOR_HIGHLIGHT_COLOR));
            indicator.setStrokeColor(Theme.getColor(ThemeValues.CIRCLE_PAGE_INDICATOR_HIGHLIGHT_COLOR));
            indicator.setStrokeWidth(1);
        }

        refreshData();
    }

    /**
     * @return
     */
    private int getPackId() {
        int packId;
        
        if (AttachmentType.fromValue(mAttachmentType) != null) {
            packId = mAttachmentType;
        } else {
            packId = mBaseEmoticonPackId;
        }
        
        return packId;
    }

    private final void refreshData() {
        
        List<? extends Object> list = null;

        AttachmentType type = AttachmentType.fromValue(mAttachmentType);

        if(type != null) {
            switch (type) {
                case EMOTICON:
                    list = EmoticonDatastore.getInstance().getAllOwnEmoticons();
                    break;
                case RECENT_EMOTICON:
                    list = EmoticonDatastore.getInstance().getRecentlyUsedEmoticons();
                    break;
                case RECENT_STICKER_GIFT:
                    list = EmoticonDatastore.getInstance().getRecentlyUsedChatItems();
                    break;
                default:
                    //data not applicable for other types
                    break;
            }
        } else {
            // do default
            list = EmoticonDatastore.getInstance().getAllStickersInPack(mBaseEmoticonPackId);
        }
        
        if (list != null) {
            Logger.debug.log("AttachmentPager", "pagerAdapter set list size: " + list.size());
            mPagerAdapter.setData(list);
        }
        
        mPagerAdapter.setAttachmentListener(mAttachmentListener);
        
        mPagerAdapter.notifyDataSetChanged();
    }

    public interface BaseAttachmentFragmentListener {

        public void onAttachmentItemClick(Object data, int attachmentType);
    }

    public BaseAttachmentFragmentListener getAttachmentListener() {
        return mAttachmentListener;
    }

    public void setAttachmentListener(BaseAttachmentFragmentListener mAttachmentListener) {
        this.mAttachmentListener = mAttachmentListener;
    }

    @Override
    protected void registerReceivers() {
        if (mAttachmentType == AttachmentType.EMOTICON.value ||
            mAttachmentType > AttachmentType.STORE_STICKER.value) {
            registerEvent(Events.Emoticon.RECEIVED);
        }
    }

    @Override
    protected void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (action.equals(Events.Emoticon.RECEIVED)) {
            refreshData();
        }
    }

    @Override
    protected FragmentStatePagerAdapter createAdapter(FragmentManager fragmentManager) {
        int packId = getPackId();
        mPagerAdapter = new AttachmentPagerAdapter(getChildFragmentManager(), getActivity(), packId);
        return mPagerAdapter;
    }
}
