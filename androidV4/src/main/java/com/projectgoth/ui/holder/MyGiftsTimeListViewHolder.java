package com.projectgoth.ui.holder;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.UserGiftStat;
import com.projectgoth.i18n.I18n;
import com.projectgoth.imagefetcher.ImageHandler;
import com.projectgoth.imagefetcher.UIUtils;
import com.projectgoth.ui.widget.TimelineView;

/**
 * Created by lopenny on 1/23/15.
 */
public class MyGiftsTimeListViewHolder extends BaseViewHolder<UserGiftStat> {

    private final int ColorArrSize = UIUtils.getChatroomColorArray().length;

    private ImageView timeLabel;
    private TimelineView timeLine;

    private TextView receivedMonth;
    private TextView receivedText;
    private TextView receivedCounter;

    public MyGiftsTimeListViewHolder(View view) {
        super(view);
        timeLabel = (ImageView) view.findViewById(R.id.time_label);
        timeLine = (TimelineView) view.findViewById(R.id.time_line);

        receivedMonth = (TextView) view.findViewById(R.id.received_month);
        receivedText = (TextView) view.findViewById(R.id.received_text);
        receivedCounter = (TextView) view.findViewById(R.id.received_counter);
    }

    public void setData(int groupPosition, int childPosition, TimelineView.Type type, UserGiftStat data) {
        super.setData(groupPosition, data); //groupPosition for get data in which group

        timeLine.setLineType(type);

        Drawable drawable = ApplicationEx.getContext().getResources().getDrawable(R.drawable.ad_month);
        drawable.mutate().setColorFilter(null);
        receivedMonth.setText(data.getTitle());

        if (data.getNoOfReceived() > 0) {
            int color = UIUtils.getChatroomColorArray()[childPosition % ColorArrSize];
            ImageHandler.tintDrawableByColor(drawable, color);
            grayoutText(false);
        } else {
            grayoutText(true);
        }
        timeLabel.setImageDrawable(drawable);
        receivedCounter.setText(String.valueOf(data.getNoOfReceived()));
        receivedText.setText(I18n.tr("Received"));
    }

    private void grayoutText(boolean grayout) {
        int textColor = ApplicationEx.getColor(R.color.dark_text_color);
        if (grayout) {
            textColor = ApplicationEx.getColor(R.color.text_light_gray);
        }
        receivedMonth.setTextColor(textColor);
        receivedText.setTextColor(textColor);
        receivedCounter.setTextColor(textColor);
    }
}
