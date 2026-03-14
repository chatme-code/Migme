/**
 * Copyright (c) 2013 Project Goth
 *
 * GroupFragment.java
 * Created Aug 26, 2013, 2:01:55 PM
 */

package com.projectgoth.ui.fragment;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mig33.diggle.events.Events;
import com.projectgoth.R;
import com.projectgoth.b.data.Group;
import com.projectgoth.common.Constants;
import com.projectgoth.common.Logger;
import com.projectgoth.common.Theme;
import com.projectgoth.common.ThemeValues;
import com.projectgoth.common.Tools;
import com.projectgoth.common.WebURL;
import com.projectgoth.controller.GroupsController;
import com.projectgoth.i18n.I18n;
import com.projectgoth.imagefetcher.ImageHandler;
import com.projectgoth.model.ContextMenuItem;
import com.projectgoth.ui.activity.ActionHandler;
import com.projectgoth.ui.activity.CustomActionBarConfig;
import com.projectgoth.ui.activity.CustomActionBarConfig.NavigationButtonState;
import com.projectgoth.ui.fragment.ShareboxFragment.ShareboxActionType;
import com.projectgoth.ui.listener.ContextMenuItemListener;
import com.projectgoth.util.StringUtils;

/**
 * @author dangui
 * 
 */
public class GroupPageFragment extends PostListFragment implements ContextMenuItemListener {

    private static final String TAG                         = "GroupPageFragment";
    private View                groupPageHeader;
    private ImageView           groupIcon;
    private TextView            groupName;
    private TextView            groupDescription;
    private ImageView           groupOfficialIcon;
    private View                playButtonContainer;
    private View                inviteButtonContainer;
    private View                chatButtonContainer;
    private View                joinButtonContainer;
    private View                pendingApprovalContainer;
    private TextView            playButtonText;
    private TextView            chatButtonText;
    private TextView            inviteButtonText;
    private TextView            joinButtonText;
    // this is the pending approval status message which is displayed
    // after sending a request to join a pending approval group
    private TextView            pendingApprovalText;
    // this is how many pending requests you need to approval
    private View                pendingRequests;
    private TextView            pendingRequestsText;
    private ImageView           arrowButton;

    private Group               group;
    private boolean             isDescriptionExpanded;
    // force refresh for the first time
    private boolean             shouldForceRefreshGroupInfo = true;

    @Override
    protected View createHeaderView() {
        View headerView = null;
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        headerView = inflater.inflate(R.layout.header_group_page, null);

        groupPageHeader = headerView;
        headerView.setBackgroundColor(Theme.getColor(ThemeValues.LIGHT_BACKGROUND_COLOR));

        groupIcon = (ImageView) headerView.findViewById(R.id.group_icon);

        groupName = (TextView) headerView.findViewById(R.id.group_name);
        groupName.setOnClickListener(this);

        groupDescription = (TextView) headerView.findViewById(R.id.group_description);
        groupDescription.setOnClickListener(this);

        groupOfficialIcon = (ImageView) headerView.findViewById(R.id.group_official);

        playButtonContainer = headerView.findViewById(R.id.play_button_container);
        playButtonContainer.setOnClickListener(this);

        inviteButtonContainer = headerView.findViewById(R.id.invite_button_container);
        inviteButtonContainer.setOnClickListener(this);

        chatButtonContainer = headerView.findViewById(R.id.chat_button_container);
        chatButtonContainer.setOnClickListener(this);

        joinButtonContainer = headerView.findViewById(R.id.join_button_container);
        joinButtonContainer.setOnClickListener(this);

        pendingApprovalContainer = headerView.findViewById(R.id.pending_approval_container);

        playButtonText = (TextView) headerView.findViewById(R.id.play_button);
        playButtonText.setText(I18n.tr("Play"));

        chatButtonText = (TextView) headerView.findViewById(R.id.chat_button);
        chatButtonText.setText(I18n.tr("Chat rooms"));

        inviteButtonText = (TextView) headerView.findViewById(R.id.invite_button);
        inviteButtonText.setText(I18n.tr("Invite"));

        joinButtonText = (TextView) headerView.findViewById(R.id.join_button);
        joinButtonText.setText(I18n.tr("Join"));

        pendingApprovalText = (TextView) headerView.findViewById(R.id.pending_approval_text);
        pendingApprovalText.setText(I18n.tr("Request sent, hang on."));

        pendingRequests = headerView.findViewById(R.id.pending_requests);
        pendingRequests.setOnClickListener(this);

        pendingRequestsText = (TextView) headerView.findViewById(R.id.pending_text);

        arrowButton = (ImageView) headerView.findViewById(R.id.arrow_button);
        arrowButton.setOnClickListener(this);

        return headerView;
    }

    @Override
    public void onResume() {
        super.onResume();

        // fetch the group info data
        Group group = GroupsController.getInstance().getGroup(this.groupId, shouldForceRefreshGroupInfo);
        if (shouldForceRefreshGroupInfo) {
            shouldForceRefreshGroupInfo = false;
        }
        if (group != null) {
            this.group = group;
            updateGroupHeader();
        }
    }

    @Override
    public void onRefresh() {
        super.onRefresh();
        // force fetch the group info
        GroupsController.getInstance().getGroup(this.groupId, true);
    }

    @Override
    protected void registerReceivers() {
        super.registerReceivers();

        registerEvent(Events.Group.BEGIN_FETCH_INFO);
        registerEvent(Events.Group.FETCH_INFO_COMPLETED);
        registerEvent(Events.Group.JOINED);
        registerEvent(Events.Post.SENT);
        registerEvent(Events.Group.JOIN_REQUEST_SENT);
        registerEvent(Events.Group.JOIN_ERROR);
        registerEvent(Events.Group.SEND_JOIN_REQUEST_ERROR);
    }

    @Override
    protected void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        final String action = intent.getAction();
        Logger.debug.log("Dangui", "onReceive:" + action);

        if (action.equals(Events.Group.BEGIN_FETCH_INFO)) {
           //show loading info
        } else if (action.equals(Events.Group.FETCH_INFO_COMPLETED)) {
            String groupIdReceived = intent.getStringExtra(Events.Group.Extra.ID);
            if (groupIdReceived.equals(this.groupId)) {
                Group group = GroupsController.getInstance().getGroup(this.groupId, false);
                if (group != null) {
                    this.group = group;
                    updateGroupHeader();
                }
                setRefreshDone();
            }
        } else if (action.equals(Events.Group.JOINED)) {
            Tools.showToastForIntent(context, intent);
            group.setSession_user_member(true);
            updateGroupHeader();
            // force fetch the group info
            GroupsController.getInstance().getGroup(this.groupId, true);
            // force fetch the posts in the group
            refreshData(true);
        } else if (action.equals(Events.Group.JOIN_REQUEST_SENT)) {
            Tools.showToastForIntent(context, intent);
            // force fetch the group info
            GroupsController.getInstance().getGroup(this.groupId, true);
        } else if (action.equals(Events.Post.SENT)) {
            refreshData(true);
        } else if (action.equals(Events.Group.SEND_JOIN_REQUEST_ERROR) || action.equals(Events.Group.JOIN_ERROR)) {
            Tools.showToastForIntent(context, intent);
        }
    }

    private void updateGroupHeader() {
        if (group == null)
            return;

        groupName.setText(StringUtils.decodeHtml(group.getName()));
        // groupName.setText("A very loooong group name. A very loooong group name. A very loooong group name. A very loooong group name.");
        groupDescription.setText(StringUtils.decodeHtml(group.getDescription()));
        // groupDescription.setText("A very loooong group description. A very loooong group description. A very loooong group description. A very loooong group description.");

        Integer official = group.getOfficial();
        if (official != null && official != 0) {
            groupOfficialIcon.setVisibility(View.VISIBLE);
        } else {
            groupOfficialIcon.setVisibility(View.GONE);
        }

        String clientId = group.getClient_id();
        if (clientId != null && clientId.trim().length() > 0) {
            playButtonContainer.setOnClickListener(this);
            playButtonContainer.setVisibility(View.VISIBLE);
        } else {
            playButtonContainer.setVisibility(View.GONE);
        }

        if (group.getSession_user_member()) {
            inviteButtonContainer.setVisibility(View.VISIBLE);
            joinButtonContainer.setVisibility(View.GONE);
            pendingApprovalContainer.setVisibility(View.GONE);
        } else if (group.getRequest_exists()) {
            inviteButtonContainer.setVisibility(View.GONE);
            joinButtonContainer.setVisibility(View.GONE);
            pendingApprovalContainer.setVisibility(View.VISIBLE);
        } else {
            inviteButtonContainer.setVisibility(View.GONE);
            joinButtonContainer.setVisibility(View.VISIBLE);
            pendingApprovalContainer.setVisibility(View.GONE);
        }
        inviteButtonContainer.setOnClickListener(this);
        chatButtonContainer.setOnClickListener(this);
        joinButtonContainer.setOnClickListener(this);

        int size = getResources().getDimensionPixelSize(R.dimen.group_icon_size);
        String displayFileId = group.getDisplay_file_id();
        if (displayFileId != null && displayFileId.trim().length() > 0) {
            String url = ImageHandler.constructFullImageLink(displayFileId, size);
            ImageHandler.getInstance().loadImageFromUrl(groupIcon, url, false, R.drawable.ic_default_group);
        } else {
            groupIcon.setImageResource(R.drawable.ic_default_group);
        }

        View dividerAbove = groupPageHeader.findViewById(R.id.divider4);
        View dividerBelow = groupPageHeader.findViewById(R.id.divider5);

        dividerAbove.setVisibility(View.GONE);
        dividerBelow.setVisibility(View.GONE);
        pendingRequests.setVisibility(View.GONE);

        if (group.getSession_user_admin()) {
            Integer invitations = group.getPending_invitation();
            if (invitations != null && invitations > 0) {
                dividerAbove.setVisibility(View.VISIBLE);
                dividerBelow.setVisibility(View.VISIBLE);
                pendingRequests.setVisibility(View.VISIBLE);
                pendingRequestsText.setText(String.format(I18n.tr("%d pending requests"), invitations));
            }
        }

        arrowButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {

        if (v == groupName || v == groupDescription) {
            toggleGroupNameAndDescription();
            return;
        } else if (v == inviteButtonContainer) {
            showGroupInviteScreen(groupId);
            return;
        } else if (v == chatButtonContainer) {
            showGroupChatroomsScreen(groupId);
            return;
        } else if (v == pendingRequests) {
            shouldForceRefreshGroupInfo = true;
            showPendingRequestsScreen(groupId);
            return;
        } else if (v == joinButtonContainer) {
            joinGroup();
            return;
        } else if (v == playButtonContainer) {
            if (group != null) {
                String clientId = group.getClient_id();
                String url = String.format(WebURL.URL_GROUPS_PLAY, clientId);
                ActionHandler.getInstance().displayBrowser(getActivity(), url);
            }
            return;
        } else if (v == arrowButton) {
            String title = group == null ? null : group.getName();
            ArrayList<ContextMenuItem> menuItemList = generateMenuItems();
            Tools.showContextMenu(title, menuItemList, this);
            return;
        }

        super.onClick(v);
    }

    /**
     * @param group
     * @return
     */
    private ArrayList<ContextMenuItem> generateMenuItems() {
        ArrayList<ContextMenuItem> menuItems = new ArrayList<ContextMenuItem>();

        menuItems.add(new ContextMenuItem(I18n.tr("Post"), R.id.option_item_group_post, group));
        menuItems.add(new ContextMenuItem(I18n.tr("Members"), R.id.option_item_group_member, group));
        if (group.getSession_user_admin()) {
            menuItems.add(new ContextMenuItem(I18n.tr("Setting"), R.id.option_item_group_setting, group));
        }
        menuItems.add(new ContextMenuItem(I18n.tr("Report abuse"), R.id.option_item_report_group, group));
        if (group.getSession_user_member()) {
            menuItems.add(new ContextMenuItem(I18n.tr("Leave group"), R.id.option_item_leave_group, group));
        }

        return menuItems;
    }

    private void joinGroup() {
        if (group != null) {
            if (group.isOpen()) {
                GroupsController.getInstance().joinGroup(groupId);
            } else if (group.isByApproval()) {
                GroupsController.getInstance().requestJoinGroup(groupId);
            }
        }
    }

    private void leaveGroup() {
        GroupsController.getInstance().leaveGroup(groupId);
        closeFragment();
    }

    /**
     * @param groupId
     */
    private void showGroupMemberScreen(String groupId) {
        String url = String.format(WebURL.URL_GROUPS_MEMBERS, groupId);
        ActionHandler.getInstance().displayBrowser(getActivity(), url);
    }

    /**
     * @param groupId
     */
    private void showGroupChatroomsScreen(String groupId) {
        String url = String.format(WebURL.URL_GROUPS_CHATROOMS, groupId);
        ActionHandler.getInstance().displayBrowser(getActivity(), url);
    }

    /**
     * @param groupId
     */
    private void showGroupInviteScreen(String groupId) {
        String url = String.format(WebURL.URL_GROUPS_INVITE, groupId);
        ActionHandler.getInstance().displayBrowser(getActivity(), url);
    }

    /**
     * @param groupId
     */
    private void showGroupSettingsScreen(String groupId) {
        String url = String.format(WebURL.URL_GROUPS_SETTINGS, groupId);
        ActionHandler.getInstance().displayBrowser(getActivity(), url);
    }

    private void showGroupReportAbuseScreen(String groupId, String groupName) {
        String url = String.format(WebURL.URL_GROUPS_REPORT_ABUSE, groupName, groupId);
        ActionHandler.getInstance().displayBrowser(getActivity(), url);
    }

    /**
     * @param groupId
     */
    private void showPendingRequestsScreen(String groupId) {
        String url = String.format(WebURL.URL_GROUPS_JOIN_REQUESTS, groupId);
        ActionHandler.getInstance().displayBrowser(getActivity(), url);
    }

    private void toggleGroupNameAndDescription() {
        if (isDescriptionExpanded) {
            groupName.setMaxLines(2);
            groupDescription.setMaxLines(2);
            isDescriptionExpanded = false;
        } else {
            groupName.setMaxLines(100);
            groupDescription.setMaxLines(100);
            isDescriptionExpanded = true;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.projectgoth.ui.listener.ContextMenuItemListener#onContextMenuItemClick
     * (com.projectgoth.model.ContextMenuItem)
     */
    @Override
    public void onContextMenuItemClick(ContextMenuItem menuItem) {
        int id = menuItem.getId();
        switch (id) {
            case R.id.option_item_group_post:
                ActionHandler.getInstance().displaySharebox(getActivity(), ShareboxActionType.CREATE_NEW_POST_IN_GROUP,
                        null, "!" + groupId + Constants.SPACESTR, groupId, true);
                break;
            case R.id.option_item_group_member:
                showGroupMemberScreen(groupId);
                break;
            case R.id.option_item_group_setting:
                showGroupSettingsScreen(groupId);
                break;
            case R.id.option_item_report_group:
                if (group != null) {
                    try {
                        showGroupReportAbuseScreen(groupId,
                                URLEncoder.encode(group.getName(), Constants.DEFAULT_ENCODING));
                    } catch (UnsupportedEncodingException e) {
                        Logger.error.log(TAG, e);
                    }
                }
                break;
            case R.id.option_item_leave_group:
                leaveGroup();
                break;
        }
    }
    
    @Override
    protected String getTitle() {
        return I18n.tr("Group");
    }
    
    @Override
    protected int getTitleIcon() {
        return R.drawable.ad_usergroup_white;
    }
    
    @Override
    public CustomActionBarConfig getActionBarConfig() {
        CustomActionBarConfig config = new CustomActionBarConfig();
        config.setNavigationButtonState(NavigationButtonState.BACK);
        return config;
    }

}
