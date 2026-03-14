/**
 * Copyright (c) 2013 Project Goth
 *
 * AttachmentPagerFragment.java
 * Created 21 Mar, 2014, 11:01:23 am
 */

package com.projectgoth.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;

import com.mig33.diggle.events.Events;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.common.Config;
import com.projectgoth.common.Tools;
import com.projectgoth.controller.AttachementGridViewCalculator;
import com.projectgoth.controller.AttachmentPagerCalculator;
import com.projectgoth.enums.AttachmentType;
import com.projectgoth.i18n.I18n;
import com.projectgoth.nemesis.model.Sticker;
import com.projectgoth.ui.adapter.AttachmentAdapter;
import com.projectgoth.ui.fragment.AttachmentPagerFragment.BaseAttachmentFragmentListener;
import com.projectgoth.ui.holder.BaseViewHolder.BaseViewListener;
import com.projectgoth.util.CrashlyticsLog;

import java.util.List;

/**
 * @author dan
 * 
 */
//TODO: since the data of the AttachmentFragment can be UsedChatItem now, not a BaseEmoticon, 
//it has been changed to Object from BaseEmoticon now. Should be refactor to something better 
public class AttachmentFragment extends BaseFragment implements BaseViewListener<Object> {

    private GridView          gridView;
    private AttachmentAdapter mAttachmentAdapter;
    private int               packId;
    private List<? extends Object> list;
    private int numOfColumn;
    private BaseAttachmentFragmentListener mAttachmentListener;

    /**
     * @see com.projectgoth.ui.fragment.BaseFragment#readBundleArguments(android.os.Bundle)
     */
    @Override
    protected void readBundleArguments(Bundle args) {
        super.readBundleArguments(args);
        packId = args.getInt(AttachmentPagerFragment.PARAM_PACK_ID);
        numOfColumn = args.getInt(AttachmentPagerFragment.PARAM_PAGER_COLUMNS);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_attachment;
    }
    
    @Override
    public void onAttach(Activity activity) {
        setShouldUpdateActionBarOnAttach(false);
        super.onAttach(activity);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        gridView = (GridView) view.findViewById(R.id.grid);
        gridView.setNumColumns(numOfColumn);

        int paddingLeftRight = ApplicationEx.getDimension(R.dimen.small_padding);

        int gridViewHeight = getGridViewHeight();
        int rowNum = getRowNum();
        int rowHeight = getRowHeight();
        int paddingTopBottom = AttachementGridViewCalculator.getGridViewPaddingTopBottom(gridViewHeight, rowNum, rowHeight);
        gridView.setPadding(paddingLeftRight, paddingTopBottom, paddingLeftRight, paddingTopBottom);

        int verticalSpacing = AttachementGridViewCalculator.getGridViewVerticalSpacing(gridViewHeight, rowNum, rowHeight);

        mAttachmentAdapter = new AttachmentAdapter(packId);
        //set vertical spacing by setting paddingTop and paddingDown in view of item instead of doing gridView.setVerticalSpacing
        // for a bigger clicking area
        mAttachmentAdapter.setVerticalSpacing(verticalSpacing);

        refreshData();
    }
    
    @Override
    protected void registerReceivers() {
        registerEvent(Events.Emoticon.RECEIVED);
        registerEvent(Events.Emoticon.FETCH_ALL_COMPLETED);
    }
    
    @Override
    protected void onReceive(Context context, Intent intent) {
        refreshData();
    }

    /**
     * 
     */
    private final void refreshData() {
        if (list != null) {
            mAttachmentAdapter.setGridList(list);
            mAttachmentAdapter.setGridItemClickListener(this);
            gridView.setAdapter(mAttachmentAdapter);
        }
    }

    /**
     * @return
     */
    private int getRowHeight() {
        int height;
        if (packId == AttachmentType.EMOTICON.value || packId == AttachmentType.RECENT_EMOTICON.value) {
            height = ApplicationEx.getDimension(R.dimen.gift_gridHeight);
        } else {
            height = ApplicationEx.getDimension(R.dimen.sticker_thumb_height);
        }
        return height;
    }

    /**
     * @return
     */
    private int getRowNum() {
        int num;
        if (packId == AttachmentType.EMOTICON.value || packId == AttachmentType.RECENT_EMOTICON.value) {
            num = AttachmentPagerCalculator.EMOTICON_ROWS;
        } else {
            num = AttachmentPagerCalculator.STICKER_ROWS;
        }
        return num;
    }

    /**
     * @return
     */
    private int getGridViewHeight() {
        int defaultDrawerHeight = ApplicationEx.getDimension(R.dimen.attachment_drawer_height);
        int softkeyboardHeight = Config.getInstance().getSoftKeyboardHeight();
        if (softkeyboardHeight > defaultDrawerHeight) {
            defaultDrawerHeight = softkeyboardHeight;
        }
        
        int pageIndicatorHeight = ApplicationEx.getDimension(R.dimen.attachment_circle_page_indicator_radius) * 2 ;
        int normalPaddingBottom = ApplicationEx.getDimension(R.dimen.normal_padding);
        int gridViewHeight = defaultDrawerHeight - ApplicationEx.getDimension(R.dimen.sticker_pack_icon_size)
                - pageIndicatorHeight - normalPaddingBottom * 2;
        
        return gridViewHeight;
    }

    /**
     * @param subList
     */
    public void setData(List<? extends Object> subList) {
        list = subList;
    }

    /* (non-Javadoc)
     * @see com.projectgoth.ui.holder.BaseViewHolder.BaseViewListener#onItemClick(android.view.View, java.lang.Object)
     */
    @Override
    public void onItemClick(View v, Object data) {
        if (mAttachmentListener != null && data != null) {
            mAttachmentListener.onAttachmentItemClick(data, packId);
        } else {
            createCrashlyticsLog(data);
            Tools.showToast(Tools.ensureContext(getActivity()), I18n.DEFAULT_ERROR_MESSAGE);
        }
    }

    /* (non-Javadoc)
     * @see com.projectgoth.ui.holder.BaseViewHolder.BaseViewListener#onItemLongClick(android.view.View, java.lang.Object)
     */
    @Override
    public void onItemLongClick(View v, Object data) {
        // TODO Auto-generated method stub
        
    }

    public void setAttachmentListener(BaseAttachmentFragmentListener attachmentListener) {
        this.mAttachmentListener = attachmentListener;
    }

    private void createCrashlyticsLog(Object data) {
        String dataMessage;
        if (data instanceof Sticker) {
            Sticker sticker = (Sticker) data;
            dataMessage = sticker.toString();
        } else {
            dataMessage = data.toString();
        }

        CrashlyticsLog.log(new NullPointerException(), dataMessage);
    }

}
