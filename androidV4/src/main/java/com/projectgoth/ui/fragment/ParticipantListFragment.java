/**
 * Copyright (c) 2013 Project Goth
 *
 * ParticipantListFragment.java
 * Created Aug 2, 2013, 3:54:06 PM
 */

package com.projectgoth.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import com.mig33.diggle.events.Events;
import com.projectgoth.R;
import com.projectgoth.b.data.Profile;
import com.projectgoth.b.data.Relationship;
import com.projectgoth.common.Tools;
import com.projectgoth.common.WebURL;
import com.projectgoth.datastore.ChatDatastore;
import com.projectgoth.datastore.UserDatastore;
import com.projectgoth.events.GAEvent;
import com.projectgoth.i18n.I18n;
import com.projectgoth.model.ContextMenuItem;
import com.projectgoth.nemesis.enums.ActivitySourceEnum;
import com.projectgoth.nemesis.model.ChatParticipant;
import com.projectgoth.ui.activity.ActionHandler;
import com.projectgoth.ui.adapter.ParticipantListAdapter;
import com.projectgoth.ui.holder.BaseViewHolder.BaseViewListener;
import com.projectgoth.ui.listener.ContextMenuItemListener;
import com.projectgoth.ui.widget.PinnedHeaderExpandableListView;

import java.util.ArrayList;
import java.util.List;

/**
 * @author sarmadsangi
 * 
 */
public class ParticipantListFragment extends BaseDialogFragment implements ContextMenuItemListener,
        AbsListView.OnScrollListener,
        BaseViewListener<ChatParticipant> {

    private String                 conversationId;
    private ParticipantListAdapter mListAdapter;
    private PinnedHeaderExpandableListView participantListView;

    private boolean                mIsChatRoom;

    private List<ChatParticipant>  participantList;

    private static final Object    ParticipantList_Lock = new Object();

    @Override
    protected void readBundleArguments(Bundle args) {
        super.readBundleArguments(args);
        conversationId = args.getString(ChatFragment.PARAM_CONVERSATION_ID);
        mIsChatRoom = args.getBoolean(ChatFragment.PARAM_CHATROOM);
    }

    @Override
    protected final int getLayoutId() {
        return R.layout.fragment_participant_list;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        participantListView = (PinnedHeaderExpandableListView) view.findViewById(R.id.participant_list);
        mListAdapter = new ParticipantListAdapter(mIsChatRoom);
        View headerView = mListAdapter.getGroupView(0, true, null, (ViewGroup) view);
        participantListView.setPinnedHeaderView(headerView);
        participantListView.setAdapter(mListAdapter);
        mListAdapter.setParticipantClickListener(this);

        participantListView.setOnScrollListener(this);

        updateParticipantsData(true);
    }

    @Override
    protected void registerReceivers() {
        registerEvent(Events.ChatParticipant.FETCH_ALL_COMPLETED);
        registerEvent(Events.ChatParticipant.ChatRoom.FETCH_ALL_ERROR);
        registerEvent(Events.ChatParticipant.GroupChat.FETCH_ALL_ERROR);
        registerEvent(Events.User.FOLLOWED);
        registerEvent(Events.User.ALREADY_FOLLOWING);
        registerEvent(Events.User.PENDING_APPROVAL);
        registerEvent(Events.User.FOLLOW_ERROR);
        registerEvent(Events.Profile.RECEIVED);
    }

    @Override
    protected void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(Events.ChatParticipant.FETCH_ALL_COMPLETED)) {
            updateParticipantsData(true);
        } else if (action.equals(Events.ChatParticipant.ChatRoom.FETCH_ALL_ERROR)
                || action.equals(Events.ChatParticipant.GroupChat.FETCH_ALL_ERROR)) {
            if (participantList == null || participantList.isEmpty()) {
                Tools.showToastForIntent(context, intent);
            }
        } else if (action.equals(Events.User.FOLLOWED) || action.equals(Events.User.ALREADY_FOLLOWING)
                || action.equals(Events.User.PENDING_APPROVAL) || action.equals(Events.User.FOLLOW_ERROR)) {
            Tools.showToastForIntent(context, intent);
        } else if (action.equals(Events.Profile.RECEIVED)) {
            updateParticipantsData(false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateParticipantsData(true);
    }

    public void updateParticipantsData(final boolean shouldForceFetch) {
        synchronized (ParticipantList_Lock) {
            participantList = ChatDatastore.getInstance().getParticipantsForChatConversationWithId(conversationId, shouldForceFetch);
            mListAdapter.setParticipantsList(participantList);
            participantListView.expandGroup(0);
        }
    }

    @Override
    public void onItemClick(View v, ChatParticipant data) {
        GAEvent.Chat_SwipeLeftOpenMiniprofile.send();
        displayProfile(data.getUsername());
    }

    @Override
    public void onItemLongClick(View v, ChatParticipant data) {
        GAEvent.Chat_SwipeLeftLongPress.send();
        ArrayList<ContextMenuItem> menuItemList = getContextMenuOptions(data);
        Tools.showContextMenu(data.getUsername(), menuItemList, this);
    }

    public ArrayList<ContextMenuItem> getContextMenuOptions(Object data) {
        ArrayList<ContextMenuItem> menuItems = new ArrayList<ContextMenuItem>();
        
        String username = ((ChatParticipant) data).getUsername();
        Profile profile = UserDatastore.getInstance().getProfileWithUsername(username, true);
        
        if (profile != null) {
            Relationship relationship = profile.getRelationship();
            if (relationship != null && !relationship.isFriend() && !relationship.isFollower()
                    && !relationship.isFollowerPendingApproval()) {
                menuItems.add(new ContextMenuItem(I18n.tr("Add as fan"), R.id.option_item_follow, data));
            }
        } 
        
        menuItems.add(new ContextMenuItem(I18n.tr("Private chat"), R.id.option_item_chat, data));
        menuItems.add(new ContextMenuItem(I18n.tr("View profile"), R.id.option_item_view_profile, data));
        menuItems.add(new ContextMenuItem(I18n.tr("Send gift"), R.id.option_item_send_gift, data));
        menuItems.add(new ContextMenuItem(I18n.tr("Block"), R.id.option_item_block, data));
        // Kicking is only allowed in chatrooms and not group chats.
        if (mIsChatRoom) {
            menuItems.add(new ContextMenuItem(I18n.tr("Kick"), R.id.option_item_kick, data));
        }
        menuItems.add(new ContextMenuItem(I18n.tr("Report abuse"), R.id.option_item_report_abuse, data));
        return menuItems;
    }

    @Override
    public void onContextMenuItemClick(ContextMenuItem menuItem) {
        ChatParticipant participant = (ChatParticipant) menuItem.getData();

        int id = menuItem.getId();
        switch (id) {
            case R.id.option_item_follow:
                GAEvent.Chat_SwipeLeftLongPressFollow.send();
                ActionHandler.getInstance().followOrUnfollowUser(participant.getUsername(), ActivitySourceEnum.UNKNOWN);
                break;
            case R.id.option_item_chat:
                GAEvent.Chat_SwipeLeftLongPressPrivateChat.send();
                ActionHandler.getInstance().displayPrivateChat(getActivity(), participant.getUsername());
                closeFragment();
                break;
            case R.id.option_item_block:
                GAEvent.Chat_SwipeLeftLongPressBlockPeople.send();
                ActionHandler.getInstance().blockFriend(getActivity(), null, participant.getUsername(),
                        participant.getUsername());
                break;
            case R.id.option_item_send_gift:
                GAEvent.Chat_SwipeLeftLongPressSendGift.send();
                ActionHandler.getInstance().displayStore(getActivity(), participant.getUsername());
                closeFragment();
                break;
            case R.id.option_item_report_abuse:
                GAEvent.Chat_SwipeLeftLongPressReport.send();
                String urlReport = String.format(WebURL.URL_REPORT_USER, participant.getUsername());
                ActionHandler.getInstance().displayBrowser(getActivity(), urlReport);
                closeFragment();
                break;
            case R.id.option_item_kick:
                GAEvent.Chat_SwipeLeftLongPressKick.send();
                participant = (ChatParticipant) menuItem.getData();
                ActionHandler.getInstance().kickUser(conversationId, participant.getUsername());
                closeFragment();
                break;
            case R.id.option_item_view_profile:
                GAEvent.Chat_SwipeLeftLongPressViewProfile.send();
                participant = (ChatParticipant) menuItem.getData();
                displayProfile(participant.getUsername());
                break;
        }
    }

    private void displayProfile(String username) {
        ActionHandler.getInstance().displayProfile(getActivity(), username);
        closeFragment();
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        super.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        if (view instanceof PinnedHeaderExpandableListView) {
            ((PinnedHeaderExpandableListView) view).configureHeaderView(firstVisibleItem);
        }
    }

}
