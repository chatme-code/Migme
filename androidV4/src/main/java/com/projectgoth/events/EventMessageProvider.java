/**
 * Copyright (c) 2013 Project Goth
 *
 * EventMessages.java
 * Created Feb 19, 2014, 2:19:16 PM
 */

package com.projectgoth.events;

import com.mig33.diggle.events.BroadcastEvent;
import com.mig33.diggle.events.Events;
import com.projectgoth.i18n.I18n;
import com.projectgoth.nemesis.model.MigError;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides formatted event messages to the {@link BroadcastHandler}. A
 * formatted event message is available as extra data
 * {@link AppEvents.Misc.Extra#FORMATTED_MESSAGE} for events that are broadcast.
 * Note that all events may not contain a formatted message.
 * 
 * @author angelorohit
 */
public class EventMessageProvider {

    /**
     * Represents a formatted message for events. Contains a String formatted
     * message and optional parameters for it. 
     * Also contains information whether this formatted message can override
     * the message provided from the server.
     * 
     * @author angelorohit
     */
    private class FormattedEventMessage {

        private String   message;
        private String[] params;
        private boolean  shouldOverrideServerMessage;

        public FormattedEventMessage(final String message, final String... params) {
            this(false, message, params);
        }

        public FormattedEventMessage(final boolean shouldOverrideServerMessage, final String message,
                final String... params) {
            this.message = message;
            this.params = params;
            this.shouldOverrideServerMessage = shouldOverrideServerMessage;
        }

        public String getMessage() {
            return message;
        }

        public String[] getParams() {
            return params;
        }

        public boolean shouldOverrideServerMessage() {
            return shouldOverrideServerMessage;
        }
    }

    /**
     * A map containing {@link FormattedEventMessage} associated with events.
     */
    private Map<String, FormattedEventMessage> eventMessagesMap = null;

    /**
     * A singleton instance of this class.
     */
    private static EventMessageProvider        instance         = null;

    private EventMessageProvider() {
        initialize();
    }

    //@formatter:off
    /**
     * Initializes {@link #eventMessagesMap}. This routine should be called if the language changes.  
     */
    public void initialize() {
        // Initialize all event messages.
        eventMessagesMap = new HashMap<String, FormattedEventMessage>();
        eventMessagesMap.put(Events.Contact.FETCH_ALL_ERROR, new FormattedEventMessage(I18n.tr("We couldn't get your contacts. Try again.")));
        eventMessagesMap.put(Events.ContactGroup.ADD_ERROR, new FormattedEventMessage(I18n.tr("We couldn't add the %s group. Try again."), Events.ContactGroup.Extra.NAME));
        eventMessagesMap.put(Events.ContactGroup.UPDATE_ERROR, new FormattedEventMessage(I18n.tr("We couldn't update %s as the group name. Try again."), Events.ContactGroup.Extra.NAME));
        eventMessagesMap.put(Events.ChatRoomCategory.FETCH_ALL_ERROR, new FormattedEventMessage(I18n.tr("We couldn't get your chat rooms. Try again.")));
        eventMessagesMap.put(Events.ChatRoom.FAVOURITE_ERROR, new FormattedEventMessage(I18n.tr("We couldn't add %s chat room to your favorites. Try again."), Events.ChatRoom.Extra.NAME));
        eventMessagesMap.put(Events.ChatRoom.UNFAVOURITE_ERROR, new FormattedEventMessage(I18n.tr("We couldn't remove %s chat room from your favorites. Try again."), Events.ChatRoom.Extra.NAME));
        eventMessagesMap.put(Events.ChatRoom.FETCH_FOR_CATEGORY_ERROR, new FormattedEventMessage(I18n.tr("We couldn't get %s chat rooms. Try again."), Events.ChatRoomCategory.Extra.NAME));        
        eventMessagesMap.put(Events.ChatConversation.NAME_CHANGE_ERROR, new FormattedEventMessage(I18n.tr("We couldn't set the chat name. Try again.")));
        eventMessagesMap.put(Events.ChatConversation.PrivateChat.LEAVE_ERROR, new FormattedEventMessage(I18n.tr("We couldn't leave the chat. Try again.")));
        eventMessagesMap.put(Events.ChatConversation.GroupChat.LEAVE_ERROR, new FormattedEventMessage(I18n.tr("We couldn't leave the group chat. Try again.")));
        eventMessagesMap.put(Events.ChatConversation.ChatRoom.LEAVE_ERROR, new FormattedEventMessage(I18n.tr("We couldn't leave %s chat room. Try again."), Events.ChatConversation.Extra.CHAT_ID));
        eventMessagesMap.put(Events.ChatParticipant.GroupChat.FETCH_ALL_ERROR, new FormattedEventMessage(I18n.tr("We couldn't get group chat participants. Try again.")));
        eventMessagesMap.put(Events.ChatParticipant.ChatRoom.FETCH_ALL_ERROR, new FormattedEventMessage(I18n.tr("We couldn't get chat room participants. Try again.")));
        eventMessagesMap.put(Events.ChatParticipant.ChatRoom.KICK_ERROR, new FormattedEventMessage(I18n.tr("We couldn't kick %s out. Try again."), Events.ChatParticipant.Extra.USERNAME));
        eventMessagesMap.put(Events.ContactGroup.REMOVE_ERROR, new FormattedEventMessage(I18n.tr("We couldn't remove your %s contact group. Try again."), Events.ContactGroup.Extra.NAME));
        eventMessagesMap.put(Events.Contact.REMOVED, new FormattedEventMessage(I18n.tr("%s is no longer your friend."), Events.Contact.Extra.USERNAME));
        eventMessagesMap.put(Events.Contact.REMOVE_ERROR, new FormattedEventMessage(I18n.tr("We couldn't remove your friend. Try again.")));
        eventMessagesMap.put(Events.User.BLOCKED, new FormattedEventMessage(I18n.tr("%s is now blocked."), Events.User.Extra.USERNAME));
        eventMessagesMap.put(Events.User.UNBLOCKED, new FormattedEventMessage(I18n.tr("%s has been unblocked."), Events.User.Extra.USERNAME));
        eventMessagesMap.put(Events.User.BLOCK_ERROR, new FormattedEventMessage(I18n.tr("We couldn't block %s. Try again."), Events.User.Extra.USERNAME));
        eventMessagesMap.put(Events.User.UNBLOCK_ERROR, new FormattedEventMessage(I18n.tr("We couldn't unblock %s. Try again."), Events.User.Extra.USERNAME));
        eventMessagesMap.put(Events.Profile.FETCH_FOLLOWERS_ERROR, new FormattedEventMessage(I18n.tr("We couldn't get people who are fans of %s. Try again."), Events.User.Extra.USERNAME));
        eventMessagesMap.put(Events.Profile.FETCH_FOLLOWING_ERROR, new FormattedEventMessage(I18n.tr("We couldn't get %s's fans. Try again."), Events.User.Extra.USERNAME));
        eventMessagesMap.put(Events.Profile.FETCH_ERROR, new FormattedEventMessage(I18n.tr("We couldn't get %s's profile. Try again."), Events.User.Extra.USERNAME));
        eventMessagesMap.put(Events.User.FOLLOWED, new FormattedEventMessage(I18n.tr("You're now a fan of %s."), Events.User.Extra.USERNAME));
        eventMessagesMap.put(Events.User.ALREADY_FOLLOWING, new FormattedEventMessage(I18n.tr("You're already a fan of %s."), Events.User.Extra.USERNAME));
        eventMessagesMap.put(Events.User.PENDING_APPROVAL, new FormattedEventMessage(I18n.tr("Waiting for approval from %s."), Events.User.Extra.USERNAME));
        eventMessagesMap.put(Events.User.FOLLOW_ERROR, new FormattedEventMessage(I18n.tr("We couldn't add %s. Try again."), Events.User.Extra.USERNAME));
        eventMessagesMap.put(Events.User.UNFOLLOWED, new FormattedEventMessage(I18n.tr("You're no longer a fan of %s."), Events.User.Extra.USERNAME));
        eventMessagesMap.put(Events.User.REQUESTING_FOLLOWING, new FormattedEventMessage(I18n.tr("Waiting for %s to become your fan."), Events.User.Extra.USERNAME));
        eventMessagesMap.put(Events.User.NOT_FOLLOWING, new FormattedEventMessage(I18n.tr("You're not a fan of %s yet. Add now!"), Events.User.Extra.USERNAME));
        eventMessagesMap.put(Events.User.UNFOLLOW_ERROR, new FormattedEventMessage(I18n.tr("We couldn't remove %s. Try again."), Events.User.Extra.USERNAME));
        eventMessagesMap.put(Events.User.FETCH_BADGES_ERROR, new FormattedEventMessage(I18n.tr("We couldn't get %s's badges. Try again."), Events.User.Extra.USERNAME));
        eventMessagesMap.put(Events.Post.WATCHED, new FormattedEventMessage(I18n.tr("Added to favorites")));
        eventMessagesMap.put(Events.Post.WATCH_ERROR, new FormattedEventMessage(I18n.DEFAULT_ERROR_MESSAGE));
        eventMessagesMap.put(Events.Post.UNWATCH_ERROR, new FormattedEventMessage(I18n.DEFAULT_ERROR_MESSAGE));
        eventMessagesMap.put(Events.Post.LOCK_ERROR, new FormattedEventMessage(I18n.tr("We couldn't lock your post. Try again.")));
        eventMessagesMap.put(Events.Post.UNLOCK_ERROR, new FormattedEventMessage(I18n.tr("We couldn't unlock your post. Try again.")));
        eventMessagesMap.put(Events.Post.DELETED, new FormattedEventMessage(I18n.tr("Post deleted")));
        eventMessagesMap.put(Events.Post.DELETE_ERROR, new FormattedEventMessage(I18n.DEFAULT_ERROR_MESSAGE));
        eventMessagesMap.put(Events.MigAlert.FETCH_ALL_ERROR, new FormattedEventMessage(I18n.tr("We couldn't get your notifications. Try again.")));
        eventMessagesMap.put(Events.MigAlert.ACTION_SUCCESS, new FormattedEventMessage("%s", Events.MigAlert.Extra.ACTION_SUCCESS_MESSAGE));
        eventMessagesMap.put(Events.Post.FETCH_FOR_SEARCH_ERROR, new FormattedEventMessage(true, I18n.tr("Failed to search for posts with \"%s\""), Events.Misc.Extra.SEARCH_QUERY));
        eventMessagesMap.put(Events.Profile.FETCH_SEARCHED_USERS_ERROR, new FormattedEventMessage(true, I18n.tr("Failed to search for users with \"%s\""), Events.Misc.Extra.SEARCH_QUERY));
        eventMessagesMap.put(Events.HotTopic.FETCH_ALL_ERROR, new FormattedEventMessage(I18n.tr("We couldn't get hot topics. Try again.")));
        eventMessagesMap.put(Events.Post.SENT, new FormattedEventMessage(I18n.tr("Post created.")));
        eventMessagesMap.put(Events.Post.SEND_ERROR, new FormattedEventMessage(true, I18n.DEFAULT_ERROR_MESSAGE));
        eventMessagesMap.put(Events.Post.FETCH_FOR_CATEGORY_ERROR, new FormattedEventMessage(true, I18n.DEFAULT_ERROR_MESSAGE));
        eventMessagesMap.put(Events.Group.JOIN_ERROR, new FormattedEventMessage(I18n.tr("We couldn't join the group. Try again.")));
        eventMessagesMap.put(Events.Group.JOIN_REQUEST_SENT, new FormattedEventMessage("%s", Events.Group.Extra.JOIN_SUCCESS_MESSAGE));
        eventMessagesMap.put(Events.Group.SEND_JOIN_REQUEST_ERROR, new FormattedEventMessage(I18n.tr("We couldn't join the group. Try again.")));
        eventMessagesMap.put(Events.Group.LEAVE_ERROR, new FormattedEventMessage(I18n.tr("We couldn't leave the group. Try again.")));
        eventMessagesMap.put(Events.Group.FETCH_ERROR, new FormattedEventMessage(I18n.tr("We couldn't get group info. Try again.")));
        eventMessagesMap.put(Events.User.DISPLAY_PICTURE_SET, new FormattedEventMessage(I18n.tr("Photo set as display.")));
        eventMessagesMap.put(Events.User.SET_DISPLAY_PICTURE_ERROR, new FormattedEventMessage(I18n.tr("We couldn't set your display picture. Try again.")));
        eventMessagesMap.put(Events.User.UPLOADED_TO_PHOTO_ALBUM, new FormattedEventMessage(I18n.tr("Photo uploaded.")));
        eventMessagesMap.put(Events.User.UPLOAD_TO_PHOTO_ALBUM_ERROR, new FormattedEventMessage(I18n.tr("We couldn't upload photo to album. Try again.")));
        eventMessagesMap.put(Events.ChatParticipant.GroupChat.FETCH_ALL_ERROR, new FormattedEventMessage(I18n.tr("We couldn't create your group chat. Try again.")));
        eventMessagesMap.put(AppEvents.Location.FETCH_NEARBY_PLACES_ERROR, new FormattedEventMessage(I18n.tr("We couldn't get your current location. Try again.")));
    }
   //@formatter:on

    /**
     * @return A lazy initialized instance of this class.
     */
    public static synchronized EventMessageProvider getInstance() {
        if (instance == null) {
            instance = new EventMessageProvider();
        }

        return instance;
    }

    /**
     * Gets a {@link FormattedEventMessage} from {@link #eventMessagesMap} that
     * matches the given event name.
     * 
     * @param eventName
     *            The name of the event associated with the
     *            {@link FormattedEventMessage}
     * @return An associated {@link FormattedEventMessage} or null if no match
     *         was found.
     */
    private FormattedEventMessage getFormattedMessageForEventWithName(final String eventName) {
        synchronized (eventMessagesMap) {
            return eventMessagesMap.get(eventName);
        }
    }

    /**
     * Provides a formatted message for the given {@link BroadcastEvent}
     * 
     * @param evt
     *            The {@link BroadcastEvent} for which a formatted message is to
     *            be created.
     * @return A formatted message or null if no message can be created for the
     *         specified {@link BroadcastEvent}
     */
    public String getFormattedMessageForEvent(final BroadcastEvent evt) {
        // Use the error message from the event, if it is available.
        final String errorMessage = evt.getStringExtra(Events.Misc.Extra.ERROR_MESSAGE);
        final FormattedEventMessage formattedEventMessage = getFormattedMessageForEventWithName(evt.getName());

        String formattedMessage = errorMessage;

        if (formattedEventMessage != null) {
            if (formattedEventMessage.shouldOverrideServerMessage() || errorMessage == null) {
                // Get all the parameters (if any) from the broadcast event and
                // put them in the formatted message.
                final int noOfMessageParams = formattedEventMessage.getParams().length;
                Object[] broadcastEventParams = new Object[noOfMessageParams];

                if (noOfMessageParams > 0) {
                    for (int count = 0; count < noOfMessageParams; ++count) {
                        final String param = formattedEventMessage.getParams()[count];
                        broadcastEventParams[count] = evt.getExtra(param);
                    }
                }

                formattedMessage = String.format(formattedEventMessage.getMessage(), broadcastEventParams);
            }
        }

        // Use the default error message if no formatted message was found and
        // this is an error event.
        final int unknownMigErrorType = MigError.Type.UNKNOWN.value();
        if (formattedMessage == null && 
            evt.getIntExtra(Events.Misc.Extra.ERROR_TYPE, unknownMigErrorType) != unknownMigErrorType) {
            formattedMessage = I18n.DEFAULT_ERROR_MESSAGE;
        }

        return formattedMessage;
    }
}
