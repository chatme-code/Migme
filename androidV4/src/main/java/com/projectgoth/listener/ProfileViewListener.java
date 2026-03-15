/**
 * Copyright (c) 2013 Project Goth
 *
 * ProfileViewListener.java
 * Created Oct 16, 2014, 4:20:40 PM
 */

package com.projectgoth.listener;

import android.support.v4.app.FragmentActivity;
import android.view.View;
import com.projectgoth.R;
import com.projectgoth.b.data.Profile;
import com.projectgoth.b.data.Relationship;
import com.projectgoth.common.Tools;
import com.projectgoth.common.WebURL;
import com.projectgoth.events.GAEvent;
import com.projectgoth.i18n.I18n;
import com.projectgoth.model.ContextMenuItem;
import com.projectgoth.nemesis.enums.ActivitySourceEnum;
import com.projectgoth.nemesis.model.Friend;
import com.projectgoth.nemesis.model.User;
import com.projectgoth.ui.activity.ActionHandler;
import com.projectgoth.ui.fragment.ProfileListFragment;
import com.projectgoth.ui.holder.BaseViewHolder.BaseViewListener;
import com.projectgoth.ui.listener.ContextMenuItemListener;

import java.util.ArrayList;
import java.util.List;

/**
 * @author angelorohit
 *
 */
public class ProfileViewListener implements BaseViewListener<User>, ContextMenuItemListener {
    
    private FragmentActivity activity;
    private ProfileListFragment.ProfileListType listType;
    
    public ProfileViewListener(final FragmentActivity activity, ProfileListFragment.ProfileListType listType) {
        this.activity = activity;
        this.listType = listType;
    }

    /* (non-Javadoc)
     * @see com.projectgoth.ui.holder.BaseViewHolder.BaseViewListener#onItemClick(android.view.View, java.lang.Object)
     */
    @Override
    public void onItemClick(View v, User data) {
        if (Tools.hideVirtualKeyboard(this.activity)) {
            //If software keyboard showing just hide it and do nothing
            return;
        }
        final int viewId = v.getId();
        final Profile profile = data.getProfile();
        Relationship relationship = null;

        if (profile != null) {
            relationship = profile.getRelationship();
        }

        switch (viewId) {
            case R.id.title:
            case R.id.icon_main:
                if(listType == ProfileListFragment.ProfileListType.SEARCH_RESULTS) {
                    GAEvent.Miniblog_ClickPeopleResult.send();
                }
                ActionHandler.getInstance().displayProfile(activity, data.getUsername());
                break;
            case R.id.chat_button:
                if (relationship != null && relationship.isFriend()) {
                    if(listType == ProfileListFragment.ProfileListType.FOLLOWERS) {
                        GAEvent.Profile_FanListChat.send();
                    } else if(listType == ProfileListFragment.ProfileListType.FOLLOWING) {
                        GAEvent.Profile_FanOfListChat.send();
                    }

                    ActionHandler.getInstance().displayPrivateChat(activity, data.getUsername());
                } else {
                    if(listType == ProfileListFragment.ProfileListType.FOLLOWERS) {
                        GAEvent.Profile_FanListFollow.send();
                    } else if(listType == ProfileListFragment.ProfileListType.FOLLOWING) {
                        GAEvent.Profile_FanOfListFollow.send();
                    }

                    ActionHandler.getInstance().displayRequestFollow(activity, data.getUsername());
                }
                break;
            case R.id.send_gift_button:
                if(listType == ProfileListFragment.ProfileListType.FOLLOWERS) {
                    GAEvent.Profile_FanListSendGift.send();
                } else if(listType == ProfileListFragment.ProfileListType.FOLLOWING) {
                    GAEvent.Profile_FanOfListSendGift.send();
                }
                if (relationship != null && relationship.isFriend()) {
                    ActionHandler.getInstance().displayStore(activity, data.getUsername());
                } else {
                    ActionHandler.getInstance().displayRequestFollow(activity, data.getUsername());
                }
                break;
            case R.id.option_button:
                if (relationship != null) {
                    if (relationship.isFriend()) {
                        if(listType == ProfileListFragment.ProfileListType.FOLLOWERS) {
                            GAEvent.Profile_FanListChat.send();
                        } else if(listType == ProfileListFragment.ProfileListType.FOLLOWING) {
                            GAEvent.Profile_FanOfListChat.send();
                        }

                        ActionHandler.getInstance().displayPrivateChat(activity, data.getUsername());
                    } else if (!relationship.isFriend() && relationship.isFollower()) {
                        if(listType == ProfileListFragment.ProfileListType.FOLLOWERS) {
                            GAEvent.Profile_FanListFollow.send();
                        } else if(listType == ProfileListFragment.ProfileListType.FOLLOWING) {
                            GAEvent.Profile_FanOfListFollow.send();
                        } else if(listType == ProfileListFragment.ProfileListType.SEARCH_RESULTS) {
                            GAEvent.Chat_SearchAddUsers.send();
                        } else if(listType == ProfileListFragment.ProfileListType.RECOMMENDED_PEOPLE) {
                            GAEvent.Chat_RecommendedUserAdd.send();
                        }

                        ActionHandler.getInstance().displayRequestFollow(activity, data.getUsername());
                    } else {
                        ActionHandler.getInstance()
                                .followOrUnfollowUser(data.getUsername(), ActivitySourceEnum.UNKNOWN);
                    }
                } else {
                    if(listType == ProfileListFragment.ProfileListType.FOLLOWERS) {
                        GAEvent.Profile_FanListFollow.send();
                    } else if(listType == ProfileListFragment.ProfileListType.FOLLOWING) {
                        GAEvent.Profile_FanOfListFollow.send();
                    } else if(listType == ProfileListFragment.ProfileListType.SEARCH_RESULTS) {
                        GAEvent.Chat_SearchAddUsers.send();
                    } else if(listType == ProfileListFragment.ProfileListType.RECOMMENDED_PEOPLE) {
                        GAEvent.Chat_RecommendedUserAdd.send();
                    }

                    ActionHandler.getInstance().followOrUnfollowUser(data.getUsername(), ActivitySourceEnum.UNKNOWN);
                }
                break;
            default:
                ActionHandler.getInstance().displayProfile(activity, data.getUsername());
                break;
        }
    }

    /* (non-Javadoc)
     * @see com.projectgoth.ui.holder.BaseViewHolder.BaseViewListener#onItemLongClick(android.view.View, java.lang.Object)
     */
    @Override
    public void onItemLongClick(View v, User data) {
        final Friend friend = data.getFriend();
        final Profile profile = data.getProfile();
        Relationship relationship = null;

        if (profile != null) {
            relationship = profile.getRelationship();
        }

        if (friend != null && relationship != null && relationship.isFriend()) {
            final List<ContextMenuItem> menuItemList = getContextMenuOptions(friend);
            Tools.showContextMenu(friend.getDisplayName(), menuItemList, this);
        }
    }
    
    private List<ContextMenuItem> getContextMenuOptions(Object data) {
        List<ContextMenuItem> menuItems = new ArrayList<ContextMenuItem>();
        menuItems.add(new ContextMenuItem(I18n.tr("View profile"), R.id.option_item_view_profile, data));
        menuItems.add(new ContextMenuItem(I18n.tr("Chat"), R.id.option_item_chat, data));
        menuItems.add(new ContextMenuItem(I18n.tr("Send gift"), R.id.option_item_send_gift, data));
        menuItems.add(new ContextMenuItem(I18n.tr("Report abuse"), R.id.option_item_report_abuse, data));
        menuItems.add(new ContextMenuItem(I18n.tr("Block/Mute"), R.id.option_item_block, data));
        menuItems.add(new ContextMenuItem(I18n.tr("Unfriend"), R.id.option_item_remove_friend, data));
        return menuItems;
    }
    
    @Override
    public void onContextMenuItemClick(ContextMenuItem menuItem) {
        Friend friend = (Friend) menuItem.getData();

        int id = menuItem.getId();
        switch (id) {
            case R.id.option_item_view_profile:
                ActionHandler.getInstance().displayProfile(activity, friend.getUsername());
                break;
            case R.id.option_item_chat:
                if(listType == ProfileListFragment.ProfileListType.FOLLOWERS) {
                    GAEvent.Profile_FanListChat.send();
                } else if(listType == ProfileListFragment.ProfileListType.FOLLOWING) {
                    GAEvent.Profile_FanOfListChat.send();
                }
                ActionHandler.getInstance().displayPrivateChat(activity, friend.getUsername());
                break;
            case R.id.option_item_send_gift:
                if(listType == ProfileListFragment.ProfileListType.FOLLOWERS) {
                    GAEvent.Profile_FanListSendGift.send();
                } else if(listType == ProfileListFragment.ProfileListType.FOLLOWING) {
                    GAEvent.Profile_FanOfListSendGift.send();
                }
                ActionHandler.getInstance().displayStore(activity, friend.getUsername());
                break;
            case R.id.option_item_report_abuse:
                final String urlReport = String.format(WebURL.URL_REPORT_USER, friend.getUsername());
                ActionHandler.getInstance().displayBrowser(activity, urlReport);
                break;
            case R.id.option_item_block:
                ActionHandler.getInstance().blockFriend(activity, null, friend.getDisplayName(),
                        friend.getUsername());
                break;
            case R.id.option_item_remove_friend:
                ActionHandler.getInstance().removeFriend(activity, friend.getDisplayName(), friend.getContactID());
                break;
            default:
                break;
        }
    }

}
