package com.projectgoth.ui.holder;

import android.view.View;
import android.widget.TextView;

import com.projectgoth.R;
import com.projectgoth.b.data.mime.GiftMimeData;
import com.projectgoth.common.Tools;
import com.projectgoth.i18n.I18n;

/**
 * Created by lopenny on 1/23/15.
 */
public class MyGiftsAllListViewHolder extends BaseViewHolder<GiftMimeData> {

    private TextView mUserName;
    private TextView mSentInfo;
    private TextView mSentDate;

    public MyGiftsAllListViewHolder(View view) {
        super(view);

        mUserName = (TextView) view.findViewById(R.id.username);
        mSentInfo = (TextView) view.findViewById(R.id.sent_info);
        mSentDate = (TextView) view.findViewById(R.id.sent_date);
    }

    @Override
    public void setData(GiftMimeData data) {
        super.setData(data);

        mUserName.setText(data.getSender());
        mSentInfo.setText(String.format(I18n.tr("sent %s"), data.getName()));
        mSentDate.setText(Tools.getTimeAgoDate(data.getReceivedTimeStamp()));
    }
}
