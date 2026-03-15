/**
 * Copyright (c) 2013 Project Goth
 *
 * ContactGroupViewHolder.java
 * Created Sep 25, 2014, 9:37:49 AM
 */

package com.projectgoth.ui.holder;

import android.text.SpannableStringBuilder;
import android.text.style.AbsoluteSizeSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.nemesis.model.ContactGroup;


/**
 * @author houdangui
 *
 */
public class ContactGroupViewHolder extends BaseViewHolder<ContactGroup> {

    private View container;
    private TextView mGroupTitle;
    private ImageView mGroupIcon;
    private ContactGroupClickListener mListener;
    private int mGroupPosition;
    private boolean showGroupIcon;

    public interface ContactGroupClickListener {
        
        public void onContactGroupClickListener(int groupPosition);        
        
        public void onContactGroupLongPress(int groupPosition);
        
    }
    
    public ContactGroupViewHolder(View rootView) {
        super(rootView, false);
        
        container = rootView;
        mGroupIcon = (ImageView) rootView.findViewById(R.id.category_icon);
        mGroupTitle = (TextView) rootView.findViewById(R.id.category_name);
    }
    
    public void setGroupTitle(String groupName, int count) {
        if (count > 0) {
            // chat participants count has a smaller text size
            String countStr = " (" + count + ")";
            String title = groupName + countStr; 
            SpannableStringBuilder builder = new SpannableStringBuilder(title);
            AbsoluteSizeSpan span = new AbsoluteSizeSpan((int) ApplicationEx.getDimension(R.dimen.text_size_medium));
            builder.setSpan(span, title.length()-countStr.length(), title.length(), SpannableStringBuilder.SPAN_INCLUSIVE_INCLUSIVE);
            mGroupTitle.setText(builder);
        } else {
            mGroupTitle.setText(groupName);
        }
    }
    
    public void setData(ContactGroup group) {
        super.setData(group);

        if (group.isSelectable()) {
            mGroupTitle.setTextColor(ApplicationEx.getColor(R.color.friend_category_title_color));
        } else {
            mGroupTitle.setTextColor(ApplicationEx.getColor(R.color.friend_category_title_unselectable));
        }

        if (showGroupIcon) {
            mGroupIcon.setVisibility(View.VISIBLE);
        } else {
            mGroupIcon.setVisibility(View.INVISIBLE);
        }

    }
    
    public void setListener(ContactGroupClickListener listener) {
        mListener = listener;
        
        container.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onContactGroupClickListener(mGroupPosition);
                }
            }
        });
        
        container.setOnLongClickListener(new OnLongClickListener() {
            
            @Override
            public boolean onLongClick(View v) {
                if (mListener == null) {
                    return false;
                } else {
                    mListener.onContactGroupLongPress(mGroupPosition);
                    return true;
                }
            }
        });
    }

    /**
     * @param groupPosition
     */
    public void setGroupPosition(int groupPosition) {
        mGroupPosition = groupPosition;
    }

    public boolean isShowGroupIcon() {
        return showGroupIcon;
    }

    public void setShowGroupIcon(boolean showGroupIcon) {
        this.showGroupIcon = showGroupIcon;
    }
}
