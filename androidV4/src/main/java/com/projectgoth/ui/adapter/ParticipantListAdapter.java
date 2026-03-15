/**
 * Copyright (c) 2013 Project Goth
 *
 * ParticipantListAdapter.java
 * Created Aug 5, 2013, 2:00:52 PM
 */

package com.projectgoth.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.nemesis.model.ChatParticipant;
import com.projectgoth.ui.holder.BaseViewHolder.BaseViewListener;
import com.projectgoth.ui.holder.ParticipantCategoryViewHolder;
import com.projectgoth.ui.holder.ParticipantViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * @author sarmadsangi
 * 
 */
public class ParticipantListAdapter extends BaseExpandableListAdapter {

    private List<ChatParticipant>             participantListData  = new ArrayList<ChatParticipant>();
    private LayoutInflater                    mInflater;
    private BaseViewListener<ChatParticipant> participantClickListener;
    private final boolean                     isChatRoom;

    public ParticipantListAdapter(boolean isChatRoom) {
        this.isChatRoom = isChatRoom;
        mInflater = LayoutInflater.from(ApplicationEx.getContext());
    }

    // Book keeping functions
    @Override public boolean hasStableIds()                     { return false;     }
    @Override public int  getGroupCount()                       { return 1;         }
    @Override public int  getGroupType(int group)               { return 0;         }
    @Override public int  getGroupTypeCount()                   { return 1;         }
    @Override public long getGroupId(int i)                     { return 0;         }
    @Override public int  getChildType(int group, int child)    { return 0;         }
    @Override public int  getChildTypeCount()                   { return 1;         }
    @Override public long getChildId(int group, int child)      { return child;     }
    @Override public boolean isChildSelectable(int i, int i2)   { return false;     }

    @Override
    public Object getGroup(int groupPosition) {
        return participantListData.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return participantListData.size();
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        if (participantListData != null) {
            return participantListData.get(childPosition);
        }
        return null;
    }

    public void setParticipantsList(List<ChatParticipant> participantListData) {
        this.participantListData = participantListData != null ? participantListData : new ArrayList<ChatParticipant>();
        notifyDataSetChanged();

    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        ParticipantCategoryViewHolder holder;
        if(convertView == null) {
            convertView = mInflater.inflate(R.layout.holder_participant_list_category, parent, false);
            holder = new ParticipantCategoryViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ParticipantCategoryViewHolder) convertView.getTag();
        }

        holder.setData((Integer) getGroup(groupPosition));
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                      View convertView, ViewGroup parent) {
        ParticipantViewHolder participantViewHolder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.holder_participant_list_item, parent, false);
            participantViewHolder = new ParticipantViewHolder(convertView, isChatRoom);
            convertView.setTag(participantViewHolder);
        } else {
            participantViewHolder = (ParticipantViewHolder) convertView.getTag();
        }

        ChatParticipant chatParticipant = (ChatParticipant) getChild(groupPosition, childPosition);
        if(chatParticipant != null) {
            participantViewHolder.setData(chatParticipant);
            participantViewHolder.setBaseViewListener(participantClickListener);
        }

        return convertView;
    }

    public void setParticipantClickListener(BaseViewListener<ChatParticipant> listener) {
        this.participantClickListener = listener;
    }

}
