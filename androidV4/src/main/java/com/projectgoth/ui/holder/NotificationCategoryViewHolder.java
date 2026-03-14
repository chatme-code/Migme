/**
 * Copyright (c) 2013 Project Goth
 *
 * ChatroomCategoryViewHolder.java
 * Created Sep 16, 2014, 2:05:10 PM
 */

package com.projectgoth.ui.holder;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.projectgoth.R;
import com.projectgoth.b.enums.NotificationTypeEnum;
import com.projectgoth.model.NotificationCategory;

public class NotificationCategoryViewHolder extends BaseViewHolder<NotificationCategory> {

    private ImageView                    categoryIcon;
    private TextView                     categoryName;
    private TextView                     notificationCounter;
    private ViewGroup                    notificationGroupLayout;

    private NotificationCategoryListener mListener;

    public interface NotificationCategoryListener {

        public void onTitleClicked(NotificationCategory notificationCategory);

    }

    public NotificationCategoryViewHolder(View rootView) {
        super(rootView, false);
        categoryIcon = (ImageView) rootView.findViewById(R.id.category_icon);
        categoryName = (TextView) rootView.findViewById(R.id.category_name);
        notificationCounter = (TextView) rootView.findViewById(R.id.notification_counter);
        notificationGroupLayout = (ViewGroup) rootView.findViewById(R.id.notification_group_layout);
    }

    public void setData(NotificationCategory groupItem, boolean isExpanded) {
        super.setData(groupItem);

        if (groupItem.isChatMessageNotification()) {
            categoryIcon.setImageResource(R.drawable.ad_chat_grey);
        } else {
            int resourceId = groupItem.getCategoryIcon();
            categoryIcon.setImageResource(resourceId);
        }

        if (groupItem.isChatMessageNotification() || groupItem.getNotificationTypeEnum() == NotificationTypeEnum.NEW_MENTIONS) {
            notificationGroupLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mListener != null) {
                        mListener.onTitleClicked(data);
                    }
                }
            });
        }

        notificationGroupLayout.setTag(groupItem);
        if (groupItem.getUnreadNotificationCount() > 0
                && groupItem.getNotificationTypeEnum() != NotificationTypeEnum.ALREADY_READ) {
            notificationCounter.setVisibility(View.VISIBLE);
            notificationCounter.setText(String.valueOf(groupItem.getUnreadNotificationCount()));
        } else {
            notificationCounter.setVisibility(View.GONE);
        }

        // notification title
        categoryName.setText(groupItem.getTitle());
    }

    public void setListener(NotificationCategoryListener listener) {
        this.mListener = listener;
    }

}
