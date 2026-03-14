/**
 * Copyright (c) 2013 Project Goth
 * NetworkResponseController.java
 * 
 * Jun 25, 2013 1:28:48 PM
 */

package com.projectgoth.controller;

import android.content.BroadcastReceiver;
import android.text.TextUtils;

import com.projectgoth.app.ApplicationEx;
import com.projectgoth.blackhole.enums.ChatDestinationType;
import com.projectgoth.blackhole.enums.ChatParticipantType;
import com.projectgoth.blackhole.enums.ChatUserStatusType;
import com.projectgoth.blackhole.enums.ImDetailType;
import com.projectgoth.blackhole.enums.MessageType;
import com.projectgoth.blackhole.enums.PacketType;
import com.projectgoth.blackhole.enums.PresenceType;
import com.projectgoth.blackhole.fusion.packet.FusionPacket;
import com.projectgoth.blackhole.fusion.packet.FusionPktAccountBalance;
import com.projectgoth.blackhole.fusion.packet.FusionPktAvatar;
import com.projectgoth.blackhole.fusion.packet.FusionPktCaptcha;
import com.projectgoth.blackhole.fusion.packet.FusionPktChat;
import com.projectgoth.blackhole.fusion.packet.FusionPktChatListVersion;
import com.projectgoth.blackhole.fusion.packet.FusionPktChatroomNotification;
import com.projectgoth.blackhole.fusion.packet.FusionPktChatroomUserStatus;
import com.projectgoth.blackhole.fusion.packet.FusionPktContact;
import com.projectgoth.blackhole.fusion.packet.FusionPktContactListVersion;
import com.projectgoth.blackhole.fusion.packet.FusionPktDisplayPicture;
import com.projectgoth.blackhole.fusion.packet.FusionPktEmoticonHotkeys;
import com.projectgoth.blackhole.fusion.packet.FusionPktGroup;
import com.projectgoth.blackhole.fusion.packet.FusionPktGroupChatParticipants;
import com.projectgoth.blackhole.fusion.packet.FusionPktGroupChatUserStatus;
import com.projectgoth.blackhole.fusion.packet.FusionPktImAvailable;
import com.projectgoth.blackhole.fusion.packet.FusionPktImSessionStatus;
import com.projectgoth.blackhole.fusion.packet.FusionPktLatestMessagesDigest;
import com.projectgoth.blackhole.fusion.packet.FusionPktLoginOk;
import com.projectgoth.blackhole.fusion.packet.FusionPktMessageStatusEvent;
import com.projectgoth.blackhole.fusion.packet.FusionPktPresence;
import com.projectgoth.blackhole.fusion.packet.FusionPktRemoveContact;
import com.projectgoth.blackhole.fusion.packet.FusionPktRemoveGroup;
import com.projectgoth.blackhole.fusion.packet.FusionPktStatusMessage;
import com.projectgoth.blackhole.model.Captcha;
import com.projectgoth.common.Config;
import com.projectgoth.common.Logger;
import com.projectgoth.datastore.AddressBookDatastore;
import com.projectgoth.datastore.AlertsDatastore;
import com.projectgoth.datastore.ChatDatastore;
import com.projectgoth.datastore.ChatDatastore.ChatNotification;
import com.projectgoth.datastore.EmoticonDatastore;
import com.projectgoth.datastore.PostsDatastore;
import com.projectgoth.datastore.Session;
import com.projectgoth.datastore.SystemDatastore;
import com.projectgoth.datastore.UserDatastore;
import com.projectgoth.events.BroadcastHandler;
import com.projectgoth.fusion.packet.UrlHandler;
import com.projectgoth.model.ChatConversation;
import com.projectgoth.model.IMItem;
import com.projectgoth.nemesis.NetworkResponseListener;
import com.projectgoth.nemesis.model.ChatData;
import com.projectgoth.nemesis.model.ChatParticipant;
import com.projectgoth.nemesis.model.ContactGroup;
import com.projectgoth.nemesis.model.Friend;
import com.projectgoth.nemesis.model.GiftCategory;
import com.projectgoth.nemesis.model.MessageStatusEvent;
import com.projectgoth.nemesis.model.MigResponse;
import com.projectgoth.nemesis.utils.FusionPacketUtils;
import com.projectgoth.notification.NotificationHandler;
import com.projectgoth.notification.NotificationType;
import com.projectgoth.service.NetworkService;
import com.projectgoth.util.AndroidLogger;
import com.projectgoth.util.ChatUtils;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * The purpose of this class is to manage general network responses.
 * 
 * @author angelorohit
 */
public class NetworkResponseController {

    protected final static String LOG_TAG                 = AndroidLogger.makeLogTag(NetworkResponseController.class);

    /**
     * The network service that contains this controller. The controller can use
     * this network service to send requests.
     */
    NetworkService                networkService          = null;

    /**
     * This listener acts as a default network response listener. It can send
     * processed responses to other controllers. All generic responses for which
     * no controller is available can be handled by this controller.
     */
    NetworkResponseListener       networkResponseListener = null;

    /**
     * Constructor
     */
    public NetworkResponseController(final NetworkService networkService) {
        this.networkService = networkService;

        // this one is used as default response listener , pushed packet will be handled here since it doesn't have a
        // custom response listener set like when fetching packets
        networkResponseListener = new NetworkResponseListener() {

            @Override
            public void onResponseReceived(MigResponse response) {
                final FusionPacket packetRecvd = response.getPacketReceived();
                final FusionPacket matchedPacketSent = response.getMatchedPacket();
                                
                if (packetRecvd == null) {
                    // SANITY CHECK, IGNORE IF PACKET PUSHED IS NULL
                    Logger.error.log(LOG_TAG, "Received null packet from ", response);
                    return;
                }
                Logger.debug.log(LOG_TAG, "onResponseReceived: ", packetRecvd.toSimpleString());

                PacketType packetType = packetRecvd.getType();
                switch (packetType) {
                    case LOGIN_OK:
                        NetworkResponseController.this.processLoginOkPacket(packetRecvd, matchedPacketSent);
                        break;
                    case EMOTICON_HOTKEYS:
                        NetworkResponseController.this.processEmoticonHotkeysPacket(packetRecvd, matchedPacketSent);
                        break;
                    case PRESENCE:
                        NetworkResponseController.this.processPresencePacket(packetRecvd, matchedPacketSent);
                        break;
                    // The user's status message pushed by the server on
                    // post-login.
                    case STATUS_MESSAGE:
                        NetworkResponseController.this.processStatusMessagePacket(packetRecvd, matchedPacketSent);
                        break;
                    case DISPLAY_PICTURE:
                        NetworkResponseController.this.processDisplayPicturePacket(packetRecvd, matchedPacketSent);
                        break;
                    case CHATROOM_USER_STATUS:
                        NetworkResponseController.this.processChatRoomUserStatusPacket(packetRecvd, matchedPacketSent);
                        break;
                    case MESSAGE:
                        ChatController.getInstance().processChatMessageServerResponse(response, true);
                        Logger.debug.log("Packet", "MESSAGE pushed from server");
                        break;
                    case MESSAGE_STATUS_EVENT:
                        NetworkResponseController.this.processMessageStatusEventPacket(packetRecvd, matchedPacketSent);
                        break;
                    case AVATAR:
                        NetworkResponseController.this.processAvatarPacket(packetRecvd, matchedPacketSent);
                        break;
                    // Contact pushed by the server when you follow someone and
                    // they follow you back or vice-versa.
                    case CONTACT:
                        NetworkResponseController.this.processContactPacket(packetRecvd, matchedPacketSent);
                        break;
                    // Pushed by the server when a request to remove friend is
                    // sent.
                    // This will ensure that all devices have the contact
                    // removed.
                    case REMOVE_CONTACT:
                        NetworkResponseController.this.processRemoveContactPacket(packetRecvd, matchedPacketSent);
                        break;
                    case REMOVE_GROUP:
                        NetworkResponseController.this.processRemoveGroupPacket(packetRecvd, matchedPacketSent);
                        break;
                    case GROUP:
                        NetworkResponseController.this.processGroupPacket(packetRecvd, matchedPacketSent);
                        break;
                    case CONTACT_LIST_VERSION:
                        NetworkResponseController.this.processContactListVersionPacket(packetRecvd, matchedPacketSent);
                        break;
                    case ACCOUNT_BALANCE:
                        NetworkResponseController.this.processAccountBalancePacket(packetRecvd, matchedPacketSent);
                        break;
                    case CHAT:
                        handleChatPacket(new FusionPktChat(packetRecvd));
                        break;
                    case CHAT_LIST_VERSION:
                        handleChatListVersion(new FusionPktChatListVersion(packetRecvd));
                        break;
                    case LATEST_MESSAGES_DIGEST:
                        handleLatestMessageDigest(new FusionPktLatestMessagesDigest(packetRecvd));
                        break;
                    case CAPTCHA:
                        Captcha captcha = FusionPacketUtils.createCaptcha(new FusionPktCaptcha(packetRecvd));
                            SystemController.getInstance().setCaptcha(captcha);
                        break;
                    case GROUP_CHAT_PARTICIPANTS:
                        processGroupChatParticipantsPacket(packetRecvd);
                        break;
                    case GROUP_CHAT_USER_STATUS:
                        processGroupChatUserStatusPacket(packetRecvd);
                        break;
                    case IM_AVAILABLE:
                        handleIMAvailable(new FusionPktImAvailable(packetRecvd));
                        break;
                    case IM_SESSION_STATUS:
                        handleIMSessionStatus(new FusionPktImSessionStatus(packetRecvd));
                        break;
                    case CHATROOM_NOTIFICATION:
                        handleChatRoomNotificationPacket(packetRecvd);
                        break;
                    case NO_PINNED_MESSAGE:
                        break;
                    case NOTIFICATION:
                        AlertsDatastore.getInstance().requestGetUnreadMigAlerts(Session.getInstance().getUserId(),
                                AlertsDatastore.DEFAULT_MIGALERTS_FETCH_LIMIT);
                        break;
                    case SET_CHAT_MUTING:
                        handleMuteChatPacket(packetRecvd);
                        break;
                    default:
                        break;
                }
            }
        };
    }
    
    /**
     * Gets the network response listener.
     * 
     * @return The network response listener.
     */
    public NetworkResponseListener getNetworkResponseListener() {
        return networkResponseListener;
    }
    
    private void handleChatRoomNotificationPacket(FusionPacket packetRecvd) {
        if (packetRecvd != null) {

            final FusionPktChatroomNotification fusionPkt = new FusionPktChatroomNotification(packetRecvd);
            Logger.debug.log(LOG_TAG, "handleChatRoomNotificationPacket: " + fusionPkt.getText() + " url: " + fusionPkt.getUrl());

            ChatDatastore.getInstance().setChatNotification(
                    new ChatNotification(fusionPkt.getText(), fusionPkt.getUrl()));
            BroadcastHandler.ChatRoom.sendChatNotification();
        }
    }

    /**
     * Parses and handles the pushed group chat participants packet. This packet
     * is normally pushed when a user is invited to join a group chat.
     * 
     * @param packetRecvd
     *            The packet to be processed.
     */
    private void processGroupChatParticipantsPacket(FusionPacket packetRecvd) {
        if (packetRecvd != null) {
            FusionPktGroupChatParticipants participants = new FusionPktGroupChatParticipants(packetRecvd);
            final List<ChatParticipant> groupChatParticipantList = FusionPacketUtils
                    .createGroupChatParticipantsListFromPacket(participants);
            ChatDatastore.getInstance().setGroupChatParticipants(participants.getGroupChatId(),
                    groupChatParticipantList);
        }
    }

    /**
     * Processes the packet pushed by the server a participants joins or leaves
     * a group chat
     * 
     * @param packetRecvd
     *            The packet to be processed.
     */
    private void processGroupChatUserStatusPacket(FusionPacket packetRecvd) {
        if (packetRecvd != null) {
            FusionPktGroupChatUserStatus fusionPkt = new FusionPktGroupChatUserStatus(packetRecvd);
            final String chatId = fusionPkt.getGroupChatId();
            final String username = fusionPkt.getUsername();
            final ChatParticipantType type = fusionPkt.getParticipantType();
            final ChatUserStatusType status = fusionPkt.getUserStatus();

            if (chatId != null && username != null && status != null) {
                if (status == ChatUserStatusType.JOINED) {
                    ChatDatastore.getInstance().addGroupChatParticipant(chatId, new ChatParticipant(username, type));
                    BroadcastHandler.ChatParticipant.sendJoined(chatId, username);
                } else if (status == ChatUserStatusType.LEFT) {
                    ChatDatastore.getInstance().removeGroupChatParticipant(chatId, username);
                    BroadcastHandler.ChatParticipant.sendLeft(chatId, username);
                }
            }
        }
    }

    /**
     * Process the packet pushed by the server when a participant joins or
     * leaves a chatroom.
     * 
     * @param packetRecvd
     *            The packet to be processed.
     * @param matchedPacketSent
     *            The packet that may have been sent to the server. Can be null.
     */
    private void processChatRoomUserStatusPacket(FusionPacket packetRecvd, FusionPacket matchedPacketSent) {
        if (packetRecvd != null) {
            FusionPktChatroomUserStatus fusionPkt = new FusionPktChatroomUserStatus(packetRecvd);
            final String chatId = fusionPkt.getChatroomName();
            final String username = fusionPkt.getUsername();
            final ChatParticipantType type = fusionPkt.getParticipantType();
            final ChatUserStatusType status = fusionPkt.getUserStatus();

            if (chatId != null && username != null && status != null) {
                if (status == ChatUserStatusType.JOINED) {
                    ChatDatastore.getInstance().addChatRoomParticipant(chatId, new ChatParticipant(username, type));
                    BroadcastHandler.ChatParticipant.sendJoined(chatId, username);
                } else if(status == ChatUserStatusType.LEFT) {
                    ChatDatastore.getInstance().removeChatRoomParticipant(chatId, username);
                    BroadcastHandler.ChatParticipant.sendLeft(chatId, username);
                }
            }
        }
    }

    /**
     * Process the packet pushed by the server that contains all emoticon
     * hotkeys and altkeys.
     * 
     * @param packetRecvd
     *            The packet to be processed.
     * @param matchedPacketSent
     *            The packet that may have been sent to the server. Can be null.
     */
    private void processEmoticonHotkeysPacket(final FusionPacket packetRecvd, final FusionPacket matchedPacketSent) {
        if (packetRecvd != null) {
            FusionPktEmoticonHotkeys fusionPktHotkeys = new FusionPktEmoticonHotkeys(packetRecvd);

            final String[] hotkeysArr = fusionPktHotkeys.getHotkeyList();
            Logger.debug.log("Emoticons", "processEmoticonHotkeysPacket: hotkeysStr: ", hotkeysArr);

            if (hotkeysArr != null && hotkeysArr.length > 0) {
                EmoticonDatastore.getInstance().addOwnHotkeys(new LinkedHashSet<String>(Arrays.asList(hotkeysArr)),
                        true);
            }
        }
    }

    /**
     * Process the packet pushed by the server when the presence of a friend
     * changes.
     * 
     * @param packetRecvd
     *            The packet to be processed.
     * @param matchedPacketSent
     *            The packet that may have been sent to the server. Can be null.
     */
    private void processPresencePacket(final FusionPacket packetRecvd, final FusionPacket matchedPacketSent) {
        if (packetRecvd != null) {
            final FusionPktPresence fusionPktPresence = new FusionPktPresence(packetRecvd);
            final Integer contactId = fusionPktPresence.getContactId();
            Byte presence = fusionPktPresence.getPresenceByte();

            // TODO: Support multiple IM presence properly!
            PresenceType presenceType = null;
            if (presence != null) {
                presenceType = PresenceType.fromValue(presence);
            } else {
                final Byte imPresence = fusionPktPresence.getImPresenceByte();
                if (imPresence != null) {
                    presenceType = PresenceType.fromValue(imPresence);
                }
            }

            if (presenceType != null) {
                UserDatastore.getInstance().setPresenceForFriendWithContactId(contactId, presenceType);
            }
        }
    }

    /**
     * Process the packet pushed by the server when the status message of a
     * friend changes.
     * 
     * @param packetRecvd
     *            The packet to be processed.
     * @param matchedPacketSent
     *            The packet that may have been sent to the server. Can be null.
     */
    private void processStatusMessagePacket(final FusionPacket packetRecvd, final FusionPacket matchedPacketSent) {
        if (packetRecvd != null) {
            final FusionPktStatusMessage fusionPkt = new FusionPktStatusMessage(packetRecvd);
            final Integer contactId = fusionPkt.getContactId();
            final String statusMessage = fusionPkt.getStatusMessage();

            final Long contactListTimestamp = fusionPkt.getTimestamp();
            Session.getInstance().setContactListTimestamp(contactListTimestamp);

            if (contactId != null && statusMessage != null) {
                UserDatastore.getInstance().setStatusMessageForFriendWithContactId(contactId, statusMessage);
            }
        }
    }

    /**
     * Process the packet pushed by the server when the display picture of a
     * friend changes.
     * 
     * @param packetRecvd
     *            The packet to be processed.
     * @param matchedPacketSent
     *            The packet that may have been sent to the server. Can be null.
     */
    private void processDisplayPicturePacket(FusionPacket packetRecvd, FusionPacket matchedPacketSent) {
        if (packetRecvd != null) {
            final FusionPktDisplayPicture fusionPkt = new FusionPktDisplayPicture(packetRecvd);
            final Integer contactId = fusionPkt.getContactId();
            final String displayPicGuid = fusionPkt.getDisplayPictureGuid();

            final Long contactListTimestamp = fusionPkt.getTimestamp();
            Session.getInstance().setContactListTimestamp(contactListTimestamp);

            if (contactId != null && displayPicGuid != null) {
                UserDatastore.getInstance().setDisplayPicGuidForFriendWithContactId(contactId, displayPicGuid);
            }
        }
    }

    /**
     * Processes the packet pushed by the server for user status on post-login.
     * 
     * @param packetRecvd
     *            The packet to be processed.
     * @param matchedPacketSent
     *            The packet that may have been sent to the server. Can be null.
     */
    private void processAvatarPacket(FusionPacket packetRecvd, FusionPacket matchedPacketSent) {
        if (packetRecvd != null) {
            final FusionPktAvatar fusionPkt = new FusionPktAvatar(packetRecvd);
            String newStatusMessage = fusionPkt.getStatusMessage();

            // This is to make sure that the status message is updated to blank if it was cleared
            // from another device
            if (newStatusMessage == null) {
                newStatusMessage = "";
            }
            Session.getInstance().setStatusMessage(newStatusMessage);
            BroadcastHandler.User.sendStatusMessageSet();

            //I do this because server dependency BE-1442 not fixed,
            //otherwise I cannot make setting display picture feature look as if it works
            //and this is how V3 does
            final String profilePicGuid = fusionPkt.getDisplayPictureGuid();
            final String avatarPicGuid = fusionPkt.getAvatarPictureGuid();
            
            if (!TextUtils.isEmpty(profilePicGuid)) {
                Session.getInstance().setProfilePicGuid(profilePicGuid);
            }
            
            if (!TextUtils.isEmpty(avatarPicGuid)) {
                Session.getInstance().setAvatarPicGuid(avatarPicGuid);
            }
                
            BroadcastHandler.User.sendDisplayPictureSet();
        }
    }

    /**
     * Contact pushed by the server when you follow someone and they follow you
     * back or vice-versa.
     * 
     * @param packetRecvd
     *            The packet to be processed.
     * @param matchedPacketSent
     *            The packet that may have been sent to the server. Can be null.
     */
    private void processContactPacket(FusionPacket packetRecvd, FusionPacket matchedPacketSent) {
        if (packetRecvd != null) {
            FusionPktContact fusionPktContact = new FusionPktContact(packetRecvd);
            Friend friend = FusionPacketUtils.createFriendFromPacket(fusionPktContact);
            if (friend != null) {
                // AD-1115 Re-request get profile to sync friend status
                UserDatastore.getInstance().requestGetProfile(friend.getUsername());
                UserDatastore.getInstance().onContactReceived(friend);
            }
        }
    }

    /**
     * Pushed by the server when a request to remove friend is sent. This will
     * ensure that all devices have the contact removed.
     * 
     * @param packetRecvd
     *            The packet to be processed.
     * @param matchedPacketSent
     *            The packet that may have been sent to the server. Can be null.
     */
    private void processRemoveContactPacket(FusionPacket packetRecvd, FusionPacket matchedPacketSent) {
        if (packetRecvd != null) {
            FusionPktRemoveContact fusionPktRemoveContact = new FusionPktRemoveContact(packetRecvd);
            Integer contactId = fusionPktRemoveContact.getContactId();
            UserDatastore.getInstance().onContactRemoved(contactId);
        }
    }
    
    private void processRemoveGroupPacket(FusionPacket packetRecvd, FusionPacket matchedPacketSent) {
        if (packetRecvd != null) {
            FusionPktRemoveGroup fusionPktRemoveGroup = new FusionPktRemoveGroup(packetRecvd);
            Integer groupId = fusionPktRemoveGroup.getGroupId();
            if (groupId != null) {
                UserDatastore.getInstance().onContactGroupRemoved(groupId);
            }
        }
    }

    /**
     * Pushed by the server for multiple cases. Eg; Updating a group. This will
     * ensure that all devices have the updated ContactGroup.
     * 
     * @param packetRecvd
     *            The packet to be processed.
     * @param matchedPacketSent
     *            The packet that may have been sent to the server. Can be null.
     */
    private void processGroupPacket(FusionPacket packetRecvd, FusionPacket matchedPacketSent) {
        if (packetRecvd != null) {
            FusionPktGroup fusionPktGroup = new FusionPktGroup(packetRecvd);
            ContactGroup contactGroup = FusionPacketUtils.createContactGroupFromPacket(fusionPktGroup);

            if (contactGroup != null) {
                UserDatastore.getInstance().onContactGroupReceived(contactGroup);
            }
        }
    }

    /**
     * CONTACT_LIST_VERSION will be pushed by the server for multiple cases. Eg;
     * when removing a contact.
     * 
     * @param packetRecvd
     *            The packet to be processed.
     * @param matchedPacketSent
     *            The packet that may have been sent to the server. Can be null.
     */
    private void processContactListVersionPacket(FusionPacket packetRecvd, FusionPacket matchedPacketSent) {
        if (packetRecvd != null) {
            FusionPktContactListVersion fusionPktContactListVersion = new FusionPktContactListVersion(packetRecvd);
            Integer contactListVersion = fusionPktContactListVersion.getVersion();
            Long contactListTimestamp = fusionPktContactListVersion.getTimestamp();

            Session.getInstance().setContactListVersion(contactListVersion);
            Session.getInstance().setContactListTimestamp(contactListTimestamp);
        }
    }

    /**
     * The ACCOUNT_BALANCE packet that is pushed by the server on post-login.
     * 
     * @param packetRecvd
     *            The packet to be processed.
     * @param matchedPacketSent
     *            The packet that may have been sent to the server. Can be null.
     */
    private void processAccountBalancePacket(FusionPacket packetRecvd, FusionPacket matchedPacketSent) {
        if (packetRecvd != null) {
            final FusionPktAccountBalance fusionPktAccountBalance = new FusionPktAccountBalance(packetRecvd);
            final String accountBalance = fusionPktAccountBalance.getAccountBalance();

            if (accountBalance != null) {
                Session.getInstance().setAccountBalance(accountBalance);
                BroadcastHandler.User.sendAccountBalanceReceived(accountBalance);
            }
        }
    }

    /**
     * Process the LoginOk packet pushed by the server when login succeeds.
     * 
     * @param packetRecvd
     *            The packet to be processed.
     * @param matchedPacketSent
     *            The packet that may have been sent to the server. Can be null.
     */
    private void processLoginOkPacket(final FusionPacket packetRecvd, final FusionPacket matchedPacketSent) {
        
        if (packetRecvd != null) {
            long currentTime = System.currentTimeMillis();
            
            // Send the login ok packet to the relevant datastores that may
            // need the data contained on login ok.
            final FusionPktLoginOk fusionPktLoginOk = new FusionPktLoginOk(packetRecvd);

            final String lastLoggedInUserId = Session.getInstance().getUserId();

            Session.getInstance().setUserId(fusionPktLoginOk.getUserId());
            Session.getInstance().setMigLevel(fusionPktLoginOk.getReputationLevel());
            Session.getInstance().setMigLevelImageUrl(fusionPktLoginOk.getReputationImagePath());
            
            Long serverTime = fusionPktLoginOk.getServerTime();
            if (serverTime != null) {
                Session.getInstance().setServerTimeOnLogin(serverTime);
                Session.getInstance().setClientTimeOnLogin(currentTime);
            }
            Logger.debug.log("timestamp", "LoginOK: Server time: " + serverTime + ", client time: " + currentTime);

            if (TextUtils.isEmpty(lastLoggedInUserId) || !Session.getInstance().getUserId().equals(lastLoggedInUserId)) {
                onFirstTimeUserLogin();
            }

            UserDatastore.getInstance().onLoginOkReceived(fusionPktLoginOk);

            // Check whether sticker is supported.
            final Boolean supportsStickerByte = fusionPktLoginOk.getSupportsStickers();
            Session.getInstance().setIsStickerSupported(
                    (supportsStickerByte != null) && supportsStickerByte.booleanValue());

            String imageServerUrl = fusionPktLoginOk.getImageServerUrl();
            if (imageServerUrl != null) {
                UrlHandler.getInstance().setImageServerUrl(imageServerUrl);
            }

            String pageletServerUrl = fusionPktLoginOk.getPageletUrl();
            if (null != pageletServerUrl) {
                UrlHandler.getInstance().setPageletServerUrl(pageletServerUrl);
            }

            Boolean chatSyncSupport = fusionPktLoginOk.getSupportsChatSync();
            if (chatSyncSupport != null) {
                Config.getInstance().setChatSyncEnabled(chatSyncSupport);
                Logger.debug.log("ChatSync", "LoginOK packet: Chat sync supported: "
                        + Config.getInstance().isChatSyncEnabled() + " - from packet: " + chatSyncSupport);
            }
            
            String username = fusionPktLoginOk.getUsername();
            if(username != null) {
                Session.getInstance().setUsername(username);
            }

            //clear the existing IM contacts in memory, because after login every time, 
            //the same online IM contact from server has a different id [#66121726]
            UserDatastore.getInstance().removeIMContactsInMemory();
            
            initiatePostLoginRequests();
        }
    }

    /**
     * Initiates all post login requests. This routine should only be called
     * after the login ok packet has been processed.
     * 
     * @see #processLoginOkPacket(FusionPacket, FusionPacket)
     */
    private void initiatePostLoginRequests() {
        // Request for server urls.
        UrlHandler.getInstance().refreshServerUrls(networkService);
       
        // Request for contacts.
        UserDatastore.getInstance().requestContacts(networkService);
        
        // Request for own profile
        UserDatastore.getInstance().requestGetProfile(networkService, Session.getInstance().getUsername());

        // Request for the sticker pack list only if stickers are supported.
        if (Session.getInstance().getIsStickerSupported()) {
            EmoticonDatastore.getInstance().requestStickerPackList(networkService);
        }
        
        EmoticonDatastore.getInstance().requestGiftsForCategory(networkService, GiftCategory.Popular);

        ChatController.getInstance().initializeChats(networkService);

        
        // fetch the chatroom categories if it's empty
        if (ChatDatastore.getInstance().getAllChatRoomCategories(false).isEmpty()) {
            ChatDatastore.getInstance().requestChatRoomCategories(networkService);
        }
        
        // fetch the participants of cached group chat
        ChatController.getInstance().fetchAllGroupChatParticipants(networkService);

        // fetch auto mention complete list
        SystemDatastore.getInstance().getMentions(Session.getInstance().getUserId());

    }

    private void onFirstTimeUserLogin() {
        Session.getInstance().setIsFirstTimeUserLogin(true);
        Session.getInstance().setShowWelcomeScreen(true);
        clearNotificationData();
        clearDatastores();
    }

    private void handleChatListVersion(FusionPktChatListVersion packet) {
        Logger.debug.log(LOG_TAG, "handleChatListVersion: ", packet);
        Integer version = packet.getVersion();
        Long timestamp = packet.getTimestamp();
        if (version != null) {
            ChatDatastore.getInstance().setChatListVersion(version);
        }
        if (timestamp != null) {
            ChatDatastore.getInstance().setChatListTimestamp(timestamp);
        }
    }

    private void handleChatPacket(FusionPktChat chatPacket) {
        Logger.debug.log(LOG_TAG, "handleChatPacket: ", chatPacket);
        if (chatPacket != null) {
            ChatDatastore.getInstance().addChatData(ChatData.fromFusionPacket(chatPacket));

            Integer version = chatPacket.getChatListVersion();
            Long timestamp = chatPacket.getChatListTimestamp();
            if (version != null && timestamp != null) {
                ChatDatastore.getInstance().setChatListTimestamp(timestamp);
                ChatDatastore.getInstance().setChatListVersion(version);
            }
        }
    }

    private void handleMuteChatPacket(FusionPacket mutePacket) {
        String chatId = mutePacket.getStringField((short) 1);
        ChatDestinationType chatType = ChatDestinationType.fromValue(mutePacket.getByteField((short) 2));
        boolean isMuteChat = mutePacket.getBooleanField((short) 3);

        if (chatType == ChatDestinationType.PRIVATE) {
            chatId = chatId.substring(chatId.indexOf(":") + 1);
        }

        ChatConversation chatConversation  = ChatDatastore.getInstance().findChatConversation(chatType, MessageType.FUSION, chatId);
        if (chatConversation != null) {
            chatConversation.setMuted(isMuteChat);
            ChatDatastore.getInstance().saveChatConversationToPersistentStorage(chatConversation);
            BroadcastHandler.ChatConversation.sendMutedReceived();
        } 
    }

    private void handleLatestMessageDigest(FusionPktLatestMessagesDigest digestPkt) {
        Logger.debug.log(LOG_TAG, "handleLatestMessageDigest: ", digestPkt);
        ChatController.getInstance().processLatestMessageDigest(digestPkt);
    }
    
    private void handleIMAvailable(FusionPktImAvailable imAvailPacket) {
        if (imAvailPacket != null) {
            Session.getInstance().setIMItem(imAvailPacket.getImType(), imAvailPacket);
            BroadcastHandler.Contact.sendIMStatusChanged();
        }
    }
    
    private void handleIMSessionStatus(FusionPktImSessionStatus imSessionStatus) {
        if (imSessionStatus != null) {
            IMItem imItem = Session.getInstance().getIMItem(imSessionStatus.getImType());
            
            if(imItem != null) {
                if(imSessionStatus.getStatus() == FusionPktImSessionStatus.StatusType.LOGGED_IN) {
                    imItem.setDetail(ImDetailType.CONNECTED);
                } else {
                    imItem.setDetail(ImDetailType.DISCONNECTED);
                }
            }
            
            BroadcastHandler.Contact.sendIMStatusChanged();
         }
    }

    private void clearDatastores() {
        AddressBookDatastore.getInstance().clearData();
        AlertsDatastore.getInstance().clearData();
        ChatDatastore.getInstance().clearData();
        EmoticonDatastore.getInstance().clearData();
        PostsDatastore.getInstance().clearData();
        SystemDatastore.getInstance().clearData();
        UserDatastore.getInstance().clearData();        
    }
    
    /**
     * @param packetRecvd
     * @param matchedPacketSent
     */
    private void processMessageStatusEventPacket(FusionPacket packetRecvd, FusionPacket matchedPacketSent) {
        FusionPktMessageStatusEvent packet = new FusionPktMessageStatusEvent(packetRecvd);
        
        // find the conversation which contains the message 
        MessageStatusEvent msgStatusEvent = FusionPacketUtils.createMessageStatusEvent(packet);

        String conversationId = ChatUtils.getConversationIdFromMsgStatusEvent(msgStatusEvent);
        
        ChatController.getInstance().processMessageStatusEvent(conversationId, 
                msgStatusEvent.getGuid(), 
                msgStatusEvent.getMessageEventType());
    }
    
    private void clearNotificationData() {
        NotificationHandler notificationHandler = ApplicationEx.getInstance().getNotificationHandler();
        notificationHandler.removeAllNotifications(NotificationType.CHAT_NOTIFICATION, true);
        notificationHandler.removeAllNotifications(NotificationType.MIG_ALERT_NOTIFICATION, true);
        notificationHandler.removeAllNotifications(NotificationType.ACCUMULATED_FOLLOWERS_NOTIFICATION, true);
        notificationHandler.resetUnreadMentionCount();
    }
    
}
