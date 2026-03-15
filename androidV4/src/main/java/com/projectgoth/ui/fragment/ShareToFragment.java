/**
 * Copyright (c) 2013 Project Goth
 * ShareToFragment.java
 * Created Feb 27, 2015, 11:27:52 AM
 */

package com.projectgoth.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.projectgoth.R;
import com.projectgoth.common.Config;
import com.projectgoth.common.ShareManager;
import com.projectgoth.i18n.I18n;
import com.projectgoth.model.ShareToItem;
import com.projectgoth.ui.adapter.ShareToListAdapter;
import com.projectgoth.ui.holder.BaseViewHolder.BaseViewListener;

import java.util.ArrayList;
import java.util.List;

/**
 * @author shiyukun
 */
public class ShareToFragment extends BaseDialogFragment implements BaseViewListener<ShareToItem> {

    private ListView mList;
    private ShareToListAdapter mShareToListAdapter;
    private ShareItemListener mListener;

    public interface ShareItemListener {

        public abstract void onShareItemSelected(int type);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_share_to_list;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mList = (ListView) view.findViewById(R.id.list_view);
        mList.addHeaderView(createHeader());
        mList.setDivider(null);
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mList.getLayoutParams();
        lp.height = (int) (Config.getInstance().getScreenHeight() * 0.6f);
        mList.setLayoutParams(lp);
        mShareToListAdapter = new ShareToListAdapter();
        final Bundle args = getArguments();
        if (args.getBoolean(BaseDialogFragment.FROM_DEEZER)) {
            setListData(true);
        } else {
            setListData(false);
        }
    }

    // prepare share to list for view
    private void setListData(boolean fromDeezer) {
        List<ShareToItem> dataList = new ArrayList<ShareToItem>();
        if (fromDeezer) {
            dataList.add(new ShareToItem(ShareManager.ShareType.SHARE_TO_FEED.value(), R.drawable.ad_feed_grey, I18n.tr("Feed")));
        }
        dataList.add(new ShareToItem(ShareManager.ShareType.SHARE_TO_CHAT.value(), R.drawable.ad_chat_grey, I18n.tr("Chat")));
        dataList.add(new ShareToItem(ShareManager.ShareType.SHARE_TO_EMAIL.value(), R.drawable.ad_email_grey, I18n.tr("Email")));
        dataList.add(new ShareToItem(ShareManager.ShareType.SHARE_TO_FACEBOOK.value(), R.drawable.ad_facebook_outline, I18n.tr("Facebook")));
        dataList.add(new ShareToItem(ShareManager.ShareType.SHARE_TO_TWITTER.value(), R.drawable.ad_twitter_outline, I18n.tr("Twitter")));
        dataList.add(new ShareToItem(ShareManager.ShareType.SHARE_TO_OTHER.value(), R.drawable.ad_share_dark_grey, I18n.tr("Other")));

        mShareToListAdapter.setList(dataList);
        mShareToListAdapter.setListener(this);
        mList.setAdapter(mShareToListAdapter);
        mShareToListAdapter.notifyDataSetChanged();
    }

    private View createHeader() {
        View header = LayoutInflater.from(getActivity()).inflate(R.layout.header_share_to, null);
        TextView headerText = (TextView) header.findViewById(R.id.label);
        headerText.setText(I18n.tr("Share"));
        return header;
    }

    public void setShareItemListener(ShareItemListener listener) {
        this.mListener = listener;
    }

    @Override
    public void onItemClick(View v, ShareToItem data) {
        if (mListener != null) {
            int type = data.getType();
            mListener.onShareItemSelected(type);
            closeFragment();
        }
    }

    @Override
    public void onItemLongClick(View v, ShareToItem data) {
        onItemClick(v, data);
    }

}
