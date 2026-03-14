/**
 * Copyright (c) 2013 Project Goth
 *
 * ChatroomListAdapter.java
 * Created Jun 6, 2013, 10:41:46 AM
 */

package com.projectgoth.ui.adapter;

import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;

import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.Alert;
import com.projectgoth.b.enums.NotificationTypeEnum;
import com.projectgoth.common.Logger;
import com.projectgoth.util.AndroidLogger;
import com.projectgoth.i18n.I18n;
import com.projectgoth.model.NotificationCategory;
import com.projectgoth.nemesis.model.ChatRoomCategory;
import com.projectgoth.ui.holder.BaseViewHolder.BaseViewListener;
import com.projectgoth.ui.holder.BasicListCategoryFooterViewHolder;
import com.projectgoth.ui.holder.NotificationCategoryViewHolder;
import com.projectgoth.ui.holder.NotificationCategoryViewHolder.NotificationCategoryListener;
import com.projectgoth.ui.holder.NotificationChildViewHolder;
import com.projectgoth.ui.widget.ClickableSpanEx;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class NotificationListAdapter extends BaseExpandableListAdapter {

    private static final String    LOG_TAG                                = AndroidLogger.makeLogTag(NotificationListAdapter.class);
    private ArrayList<NotificationCategory>    fullNotificationCategories = new ArrayList<NotificationCategory>();
    private ArrayList<NotificationCategory> displayNotificationCategories = new ArrayList<NotificationCategory>();

    private LayoutInflater                 mInflater;

    private BaseViewListener<Alert>        alertBaseViewListener;
    private NotificationCategoryListener   notificationCategoryListener;

    private GroupFooterClickListener       groupFooterListener;

    private static final Set<Short> CATEGORIES_TO_HIDE_WHEN_EMPTY =
            new HashSet<Short>(Arrays.asList(new Short[] {ChatRoomCategory.CATEGORY_ID_RECENT}));

    private ConcurrentHashMap<String, SpannableStringBuilder> spannableCache = new ConcurrentHashMap<String, SpannableStringBuilder>();
    private ClickableSpanEx.ClickableSpanExListener onUsernameClickListener;

    private final int ALREADY_READ_NOTIFICATION_CATEGORY_GROUP_TYPE_NUMBER = 0;
    private final int OTHER_NOTIFICATION_CATEGORY_GROUP_TYPE_NUMBER        = 1;

    //@formatter:off
    private BaseViewListener<NotificationCategory> footerHolderClickListener =  new BaseViewListener<NotificationCategory>() {

        @Override
        public void onItemClick(View v, NotificationCategory data) {
            if (groupFooterListener != null) {
                groupFooterListener.onGroupFooterClick(v, data);
            }
        }

        @Override
        public void onItemLongClick(View v, NotificationCategory data) {
            // DO NOTHING
        }
    };
    //@formatter:on

    public interface GroupFooterClickListener {
        public void onGroupFooterClick(View v, NotificationCategory data);
    };

    public NotificationListAdapter() {
        super();
        mInflater = LayoutInflater.from(ApplicationEx.getContext());
    }


    public void setFullNotificationCategories(List<NotificationCategory> notificationCategories) {
        if (notificationCategories != null) {
            this.fullNotificationCategories = new ArrayList<NotificationCategory>(notificationCategories);
            updateDisplayNotificationCategories();
            notifyDataSetChanged();
        }
    }
    
    private void updateDisplayNotificationCategories() {

        this.displayNotificationCategories = new ArrayList<NotificationCategory>(fullNotificationCategories.size());
        for (NotificationCategory category : fullNotificationCategories) {
            if (CATEGORIES_TO_HIDE_WHEN_EMPTY.contains(category.getID())
                  && category.getAlertItemsSize() == 0) {
                continue;
            }
            this.displayNotificationCategories.add(category);
        }
    }

    @Override
    public NotificationCategory getGroup(int groupPosition) {
        
        // Added a groupPosition < size() check to avoid Samsung specific crash.
        if (displayNotificationCategories != null && groupPosition < displayNotificationCategories.size()) {
            NotificationCategory category = displayNotificationCategories.get(groupPosition);
            Logger.debug.log(LOG_TAG, "notification name:", category.getName(), " id:", category.getID());
            return category;
        }
        return null;
    }

    @Override
    public Alert getChild(int groupPosition, int childPosition) {
        NotificationCategory notificationCategory = getGroup(groupPosition);
        if (notificationCategory != null) {
            List<Alert> items = notificationCategory.getAlertList();
            if (items != null && childPosition >= 0 && childPosition < items.size()) {
                return items.get(childPosition);
            }
        }
        return null;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView,
            ViewGroup parent) {
        NotificationCategory notificationCategory = getGroup(groupPosition);
        if (isLastChild && notificationCategory.showAchieveOption()) {
            convertView = getGroupFooterView(groupPosition, convertView, parent);
        } else {
            NotificationChildViewHolder notificationChildViewHolder = null;
            if (convertView == null || convertView.getTag(R.id.holder) == null) {
                convertView = mInflater.inflate(R.layout.holder_notification_child, parent, false);
                notificationChildViewHolder = new NotificationChildViewHolder(convertView, spannableCache, onUsernameClickListener);
                convertView.setTag(R.id.holder, notificationChildViewHolder);
            } else {
                notificationChildViewHolder = (NotificationChildViewHolder) convertView.getTag(R.id.holder);
            }

            Alert alert = getChild(groupPosition, childPosition);
            if (alert != null) {
                notificationChildViewHolder.setData(alert);
                notificationChildViewHolder.setBaseViewListener(alertBaseViewListener);
            }
        }

        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        final NotificationCategory notificationCategory = getGroup(groupPosition);
        if (notificationCategory != null) {
            int size = notificationCategory.getAlertItemsSize();
            if (notificationCategory.showAchieveOption()) {
                size += 1;
            }
            return size;
        }
        return 0;
    }

    @Override
    public int getGroupCount() {
        if (displayNotificationCategories != null) {
            return displayNotificationCategories.size();
        }
        return 0;
    }

    @Override
    public long getGroupId(int groupPosition) {
        NotificationCategory notificationCategory = getGroup(groupPosition);
        if (notificationCategory != null) {
            return notificationCategory.getID();
        }
        return 0;
    }

    @Override
    public int getGroupType(int groupPosition) {
        NotificationCategory notificationCategory = getGroup(groupPosition);
        if (notificationCategory != null && notificationCategory.getNotificationTypeEnum() == NotificationTypeEnum.ALREADY_READ) {
            return ALREADY_READ_NOTIFICATION_CATEGORY_GROUP_TYPE_NUMBER;
        } else {
            return OTHER_NOTIFICATION_CATEGORY_GROUP_TYPE_NUMBER;
        }
    }

    @Override
    public int getGroupTypeCount() {
        return 2;
    }


    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

        NotificationCategoryViewHolder categoryView = null;
        NotificationCategory notificationCategory = getGroup(groupPosition);
        if (convertView == null || convertView.getTag() == null) {
            if (getGroupType(groupPosition) == 0) {
                convertView = mInflater.inflate(R.layout.blank_layout, parent, false);
//                convertView = mInflater.inflate(R.layout.holder_notification_group, parent, false);
            } else {
                convertView = mInflater.inflate(R.layout.holder_notification_group, parent, false);
            }
            categoryView = new NotificationCategoryViewHolder(convertView);
            categoryView.setListener(notificationCategoryListener);
            convertView.setTag(categoryView);
        } else {
            categoryView = (NotificationCategoryViewHolder) convertView.getTag();
        }
        
        if (notificationCategory != null && categoryView != null) {
            categoryView.setData(notificationCategory, true);
        }        
        
        return convertView;
    }

    @SuppressWarnings("unchecked")  // Needed to avoid warning for categoryView assignment
    private View getGroupFooterView(int groupPosition, View convertView, ViewGroup parent) {
        BasicListCategoryFooterViewHolder<NotificationCategory> categoryView = null;
        
        if (convertView == null || convertView.getTag(R.id.holder_footer) == null) {
            convertView = mInflater.inflate(R.layout.holder_list_footer, parent, false);
            categoryView = new BasicListCategoryFooterViewHolder<NotificationCategory>(convertView);
            convertView.setTag(R.id.holder_footer, categoryView);
        } else {
            categoryView = (BasicListCategoryFooterViewHolder<NotificationCategory>) convertView.getTag(R.id.holder_footer);
        }

        NotificationCategory notificationCategory = getGroup(groupPosition);
        categoryView.setData(notificationCategory);
        // instead of using chatRoomCategory.getCategoryFooter() which is sent
        // from the server, we are using "View More" based on the mockup
        categoryView.setLabel(I18n.tr(notificationCategory.getCategoryFooter()));
        categoryView.setBaseViewListener(footerHolderClickListener);

        return convertView;
    }
    
    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }


    public void setChatItemListener(BaseViewListener<Alert> alertBaseViewListener) {
        this.alertBaseViewListener = alertBaseViewListener;
    }
    
    public void setNotificationCategoryListener(NotificationCategoryListener listener) {
        this.notificationCategoryListener = listener;
    }

    /**
     * @param groupFooterListener the groupFooterListener to set
     */
    public void setGroupFooterListener(GroupFooterClickListener groupFooterListener) {
        this.groupFooterListener = groupFooterListener;
    }

    public void setOnUsernameClickListener(ClickableSpanEx.ClickableSpanExListener onUsernameClickListener) {
        this.onUsernameClickListener = onUsernameClickListener;
    }
}
