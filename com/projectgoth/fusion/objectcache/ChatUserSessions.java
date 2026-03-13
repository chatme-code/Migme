package com.projectgoth.fusion.objectcache;

import Ice.TimeoutException;
import com.projectgoth.fusion.chat.external.ChatConnectionInterface;
import com.projectgoth.fusion.chatsync.MessageStatusEvent;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.ContactData;
import com.projectgoth.fusion.data.ContactGroupData;
import com.projectgoth.fusion.fdl.enums.ClientType;
import com.projectgoth.fusion.fdl.enums.ImType;
import com.projectgoth.fusion.fdl.enums.PresenceType;
import com.projectgoth.fusion.slice.CurrencyDataIce;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.Message;
import com.projectgoth.fusion.slice.MessageDataIce;
import com.projectgoth.fusion.slice.UserEventIce;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.log4j.Logger;

public class ChatUserSessions {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(ChatUserSessions.class));
   private static AtomicInteger totalIntermediateFails = new AtomicInteger(0);
   private static AtomicInteger totalFinalFails = new AtomicInteger(0);
   private long sessionIdleTimeout;
   private final ConcurrentMap<String, ChatSession> sessions = new ConcurrentHashMap();
   private final ChatUser chatUser;
   private final ScheduledExecutorService retryService;

   public ChatUserSessions(ChatObjectManagerUser objectManager, ChatUser chatUser) {
      this.retryService = Executors.newScheduledThreadPool(SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatUserSessionsSettings.RETRY_EXECUTOR_CORE_SIZE));
      this.chatUser = chatUser;
      this.sessionIdleTimeout = (long)(objectManager.getProperties().getPropertyAsIntWithDefault("SessionIdleTimeout", 3600) * 1000);
   }

   private boolean immediatePurgingEnabled() {
      return !SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatUserSessionsSettings.IMMEDIATE_PURGING_DISABLED);
   }

   private boolean retriesEnabled() {
      return SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatUserSessionsSettings.RETRIES_ENABLED);
   }

   public boolean removeSession(String sessionID, boolean userIsBeingPurged) {
      ChatSession session = (ChatSession)this.sessions.remove(sessionID);
      if (session == null) {
         return false;
      } else {
         this.chatUser.onSessionRemoved(session);
         return true;
      }
   }

   public void removeSessions(ArrayList<String> sessionsToPurge, boolean userIsBeingPurged) {
      if (sessionsToPurge != null) {
         Iterator i$ = sessionsToPurge.iterator();

         while(i$.hasNext()) {
            String sessionID = (String)i$.next();
            this.removeSession(sessionID, userIsBeingPurged);
         }

      }
   }

   public int getSessionCount() {
      return this.sessions.size();
   }

   public ChatSession getSession(String sessionID) {
      Iterator i$ = this.sessions.values().iterator();

      ChatSession session;
      do {
         if (!i$.hasNext()) {
            return null;
         }

         session = (ChatSession)i$.next();
      } while(!session.getSessionID().equals(sessionID));

      return session;
   }

   public void onSignInSuccess(ChatConnectionInterface source) {
      Iterator i$ = this.sessions.values().iterator();

      while(i$.hasNext()) {
         ChatSession session = (ChatSession)i$.next();

         try {
            session.otherIMLoggedIn(source.getImType().value());
         } catch (Exception var5) {
         }
      }

   }

   public void onSignInFailed(ChatConnectionInterface source, String reason) {
      Iterator i$ = this.sessions.values().iterator();

      while(i$.hasNext()) {
         ChatSession session = (ChatSession)i$.next();

         try {
            session.otherIMLoggedOut(source.getImType().value(), reason);
         } catch (Exception var6) {
         }
      }

   }

   private void logOnSessionException(int iLevel, ChatSession session, String message, Exception e) {
      String err = message + " session=" + (session != null ? session.getSessionID() : session) + " e=" + e;
      if (iLevel == 40000) {
         log.error(err, e);
      } else {
         log.warn(err, e);
      }

   }

   public void contactAdded(final ContactData contact, final int contactListVersion, final boolean guaranteedIsNew) {
      Iterator i$ = this.sessions.values().iterator();

      while(i$.hasNext()) {
         final ChatSession session = (ChatSession)i$.next();
         (new ChatUserSessions.ChatUserSessionsCallable(session, true) {
            public void onCall() throws Exception {
               session.contactAdded(contact, contactListVersion, guaranteedIsNew);
            }

            public void logException(Exception e, int level) {
               ChatUserSessions.this.logOnSessionException(level, session, "Exception in contactAdded, contactData=" + contact, e);
            }
         }).call();
      }

   }

   public void contactChangedPresence(final Integer contactID, final int imType, final PresenceType presence) {
      Iterator i$ = this.sessions.values().iterator();

      while(i$.hasNext()) {
         final ChatSession session = (ChatSession)i$.next();
         (new ChatUserSessions.ChatUserSessionsCallable(session, true) {
            public void onCall() throws Exception {
               session.contactChangedPresence(contactID, imType, presence.value());
            }

            public void logException(Exception e, int level) {
               ChatUserSessions.this.logOnSessionException(level, session, "Exception in contactChangedPresence, contactID=" + contactID + " imType=" + imType + "presence=" + presence + " purgeOnException=" + ChatUserSessions.this.immediatePurgingEnabled(), e);
            }
         }).call();
      }

   }

   public void contactChangedDisplayPicture(final int contactId, final String displayPicture, final long timeStamp) throws FusionException {
      ArrayList<String> sessionsToPurge = null;
      Iterator i$ = this.sessions.values().iterator();

      while(i$.hasNext()) {
         final ChatSession session = (ChatSession)i$.next();
         (new ChatUserSessions.ChatUserSessionsCallable(session, true) {
            public void onCall() throws Exception {
               session.contactChangedDisplayPicture(contactId, displayPicture, timeStamp);
            }

            public void logException(Exception e, int level) {
               ChatUserSessions.this.logOnSessionException(level, session, "Exception in contactChangedDisplayPicture, contactId=" + contactId + " displayPicture=" + displayPicture + " timeStamp=" + timeStamp, e);
            }
         }).call();
      }

   }

   public void contactChangedStatusMessage(final Integer contactId, final String statusMessage, final long timeStamp) {
      Iterator i$ = this.sessions.values().iterator();

      while(i$.hasNext()) {
         final ChatSession session = (ChatSession)i$.next();
         (new ChatUserSessions.ChatUserSessionsCallable(session, true) {
            public void onCall() throws Exception {
               session.contactChangedStatusMessage(contactId, statusMessage, timeStamp);
            }

            public void logException(Exception e, int level) {
               ChatUserSessions.this.logOnSessionException(level, session, "Exception in contactChangedStatusMessage, contactId=" + contactId + " statusMessage=" + statusMessage + " timeStamp=" + timeStamp, e);
            }
         }).call();
      }

   }

   public void otherIMConferenceCreated(final ImType imType, final String conferenceID, final String creator) {
      Iterator i$ = this.sessions.values().iterator();

      while(i$.hasNext()) {
         final ChatSession session = (ChatSession)i$.next();
         (new ChatUserSessions.ChatUserSessionsCallable(session, false) {
            public void onCall() throws Exception {
               session.otherIMConferenceCreated(imType, conferenceID, creator);
            }

            public void logException(Exception e, int level) {
               ChatUserSessions.this.logOnSessionException(level, session, "Exception in otherIMConferenceCreated, imType=" + imType + " conferenceID=" + conferenceID + " creator=" + creator, e);
            }
         }).call();
      }

   }

   public void notifyUserJoinedGroupChat(String groupChatId, String username, boolean isMuted) {
      Iterator i$ = this.sessions.values().iterator();

      while(i$.hasNext()) {
         ChatSession session = (ChatSession)i$.next();

         try {
            session.notifyUserJoinedGroupChat(groupChatId, username, isMuted);
         } catch (Exception var7) {
            log.info("notifyUserJoinedGroupChat error: " + var7.getMessage());
         }
      }

   }

   public void notifyUserLeftGroupChat(String groupChatId, String username) {
      Iterator i$ = this.sessions.values().iterator();

      while(i$.hasNext()) {
         ChatSession session = (ChatSession)i$.next();

         try {
            session.notifyUserLeftGroupChat(groupChatId, username);
         } catch (Exception var6) {
            log.info("notifyUserLeftGroupChat error: " + var6.getMessage());
         }
      }

   }

   public void notifySessionsOfNewContact(final ContactData contact, final int contactListVersion, final boolean guaranteedIsNew) {
      Iterator i$ = this.sessions.values().iterator();

      while(i$.hasNext()) {
         final ChatSession session = (ChatSession)i$.next();
         (new ChatUserSessions.ChatUserSessionsCallable(session, true) {
            public void onCall() throws Exception {
               session.contactAdded(contact, contactListVersion, guaranteedIsNew);
            }

            public void logException(Exception e, int level) {
               ChatUserSessions.this.logOnSessionException(level, session, "Exception in notifySessionsOfNewContact, contact=" + contact + " contactListVersion=" + contactListVersion + " guaranteedIsNew=" + guaranteedIsNew, e);
            }
         }).call();
      }

   }

   public void notifySessionsOfRemovedContact(final int contactID, final int contactListVersion) {
      ArrayList<String> sessionsToPurge = null;
      Iterator i$ = this.sessions.values().iterator();

      while(i$.hasNext()) {
         final ChatSession session = (ChatSession)i$.next();
         (new ChatUserSessions.ChatUserSessionsCallable(session, true) {
            public void onCall() throws Exception {
               if (ChatUserSessions.log.isDebugEnabled()) {
                  ChatUserSessions.log.debug("sending contact removed packet to session [" + session + "]");
               }

               session.contactRemoved(contactID, contactListVersion);
            }

            public void logException(Exception e, int level) {
               ChatUserSessions.this.logOnSessionException(level, session, "Exception in notifySessionsOfRemovedContact, contactID=" + contactID + " contactListVersion=" + contactListVersion, e);
            }
         }).call();
      }

   }

   public void sendGroupChatParticipantArrays(String conferenceID, byte imType, String[] participants, String[] mutedParticipants) {
      Iterator i$ = this.sessions.values().iterator();

      while(i$.hasNext()) {
         ChatSession session = (ChatSession)i$.next();

         try {
            session.sendGroupChatParticipantArrays(conferenceID, imType, participants, mutedParticipants);
         } catch (Exception var8) {
            log.info("sendGroupChatParticipants error: " + var8.getMessage());
         }
      }

   }

   public void sendGroupChatParticipants(String conferenceID, byte imType, String participants, String mutedParticipants) {
      Iterator i$ = this.sessions.values().iterator();

      while(i$.hasNext()) {
         ChatSession session = (ChatSession)i$.next();

         try {
            session.sendGroupChatParticipants(conferenceID, imType, participants, mutedParticipants);
         } catch (Exception var8) {
            log.info("sendGroupChatParticipants error: " + var8.getMessage());
         }
      }

   }

   public void putMessage(final MessageDataIce message) throws FusionException {
      final AtomicBoolean messageForwardedOK = new AtomicBoolean(false);
      if (this.sessions.size() == 0) {
         throw new FusionException("The user is no longer online");
      } else {
         Iterator i$ = this.sessions.values().iterator();

         while(i$.hasNext()) {
            final ChatSession session = (ChatSession)i$.next();
            (new ChatUserSessions.ChatUserSessionsCallable(session, true) {
               public void onCall() throws Exception {
                  session.putMessageLocal(message);
                  messageForwardedOK.set(true);
               }

               public void logException(Exception e, int level) {
                  ChatUserSessions.this.logOnSessionException(level, session, "Exception in putMessage, message=" + message + " purgeOnException=" + ChatUserSessions.this.immediatePurgingEnabled(), e);
               }
            }).call();
         }

         if (!messageForwardedOK.get()) {
            throw new FusionException("The message could not be sent");
         }
      }
   }

   public void emoticonsChanged(final String[] emoticonHotKeys, final String[] emoticonAlternateKeys) {
      Iterator i$ = this.sessions.values().iterator();

      while(i$.hasNext()) {
         final ChatSession session = (ChatSession)i$.next();
         (new ChatUserSessions.ChatUserSessionsCallable(session, false) {
            public void onCall() throws Exception {
               session.emoticonsChanged(emoticonHotKeys, emoticonAlternateKeys);
            }

            public void logException(Exception e, int level) {
               ChatUserSessions.this.logOnSessionException(level, session, "Exception in emoticonsChanged, emoticonHotKeys=" + emoticonHotKeys + " emoticonAlternateKeys=" + emoticonAlternateKeys, e);
            }
         }).call();
      }

   }

   public boolean hasPrivateChattedWith(String username) {
      Iterator i$ = this.sessions.values().iterator();

      ChatSession session;
      do {
         if (!i$.hasNext()) {
            return false;
         }

         session = (ChatSession)i$.next();
      } while(!session.hasPrivateChattedWith(username));

      return true;
   }

   public boolean dispose() {
      String[] sessionsToRemove = null;
      if (this.sessions.size() > 0) {
         sessionsToRemove = (String[])this.sessions.keySet().toArray(new String[this.sessions.size()]);
      }

      if (sessionsToRemove != null) {
         String[] arr$ = sessionsToRemove;
         int len$ = sessionsToRemove.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            String sessionID = arr$[i$];
            this.removeSession(sessionID, true);
         }

         if (sessionsToRemove.length > 0) {
            return true;
         }
      }

      return false;
   }

   public void purgeExpiredSessions() {
      ArrayList<String> sessionsToPurge = new ArrayList();
      synchronized(this) {
         long minimumLastTouchedTime = System.currentTimeMillis() - this.sessionIdleTimeout;
         Iterator i$ = this.sessions.values().iterator();

         while(true) {
            if (!i$.hasNext()) {
               break;
            }

            ChatSession session = (ChatSession)i$.next();
            if (session.getTimeLastTouched() < minimumLastTouchedTime) {
               sessionsToPurge.add(session.getSessionID());
               if (log.isDebugEnabled()) {
                  log.debug("Session expired - user:" + session.getUsername() + " sessionId:" + session.getSessionID());
               }
            }
         }
      }

      Iterator i$ = sessionsToPurge.iterator();

      while(i$.hasNext()) {
         String sessionID = (String)i$.next();
         this.removeSession(sessionID, false);
      }

   }

   public PresenceType getPresence(PresenceType newPresence) {
      Iterator i$ = this.sessions.values().iterator();

      while(i$.hasNext()) {
         ChatSession session = (ChatSession)i$.next();
         if (session.getPresence().value() < newPresence.value()) {
            newPresence = session.getPresence();
         }
      }

      return newPresence;
   }

   public void otherIMLoggedOut(ImType imType, String reason) {
      Iterator i$ = this.sessions.values().iterator();

      while(i$.hasNext()) {
         ChatSession session = (ChatSession)i$.next();

         try {
            session.otherIMLoggedOut(imType.value(), reason);
         } catch (Exception var6) {
            log.warn("Unable to notify user:" + this.chatUser.getUsername() + " session:" + session.getSessionID() + " of IM disconnect from:" + imType.name(), var6);
         }
      }

   }

   public void contactRequestAccepted(final ContactData newContact, final int inviteeContactListVersion, final int outstandingPendingContacts) {
      Iterator i$ = this.sessions.values().iterator();

      while(i$.hasNext()) {
         final ChatSession session = (ChatSession)i$.next();
         (new ChatUserSessions.ChatUserSessionsCallable(session, false) {
            public void onCall() throws Exception {
               session.contactRequestAccepted(newContact, inviteeContactListVersion, outstandingPendingContacts);
            }

            public void logException(Exception e, int level) {
               ChatUserSessions.this.logOnSessionException(level, session, "Exception in contactRequestAccepted, newContact=" + newContact + " inviteeContactListVersion=" + inviteeContactListVersion, e);
            }
         }).call();
      }

   }

   public void contactRequestRejected(final String inviterUsername, final int outstandingPendingContacts) {
      Iterator i$ = this.sessions.values().iterator();

      while(i$.hasNext()) {
         final ChatSession session = (ChatSession)i$.next();
         (new ChatUserSessions.ChatUserSessionsCallable(session, false) {
            public void onCall() throws Exception {
               session.contactRequestRejected(inviterUsername, outstandingPendingContacts);
            }

            public void logException(Exception e, int level) {
               ChatUserSessions.this.logOnSessionException(level, session, "Exception in contactRequestRejected, inviterUsername=" + inviterUsername + " outstandingPendingContacts=" + outstandingPendingContacts, e);
            }
         }).call();
      }

   }

   public void avatarChanged(final String displayPicture, final String statusMessage) {
      Iterator i$ = this.sessions.values().iterator();

      while(i$.hasNext()) {
         final ChatSession session = (ChatSession)i$.next();
         (new ChatUserSessions.ChatUserSessionsCallable(session, false) {
            public void onCall() throws Exception {
               session.avatarChanged(displayPicture, statusMessage);
            }

            public void logException(Exception e, int level) {
               ChatUserSessions.this.logOnSessionException(level, session, "Exception in avatarChanged, displayPicture=" + displayPicture + " statusMessage=" + statusMessage, e);
            }
         }).call();
      }

   }

   public List<ChatSession> disconnect(String reason, String username) {
      List<ChatSession> sessionsToDisconnect = new ArrayList(this.sessions.size());
      Iterator i$ = this.sessions.values().iterator();

      ChatSession session;
      while(i$.hasNext()) {
         session = (ChatSession)i$.next();
         sessionsToDisconnect.add(session);
      }

      i$ = sessionsToDisconnect.iterator();

      while(i$.hasNext()) {
         session = (ChatSession)i$.next();

         try {
            session.disconnect(reason);
         } catch (FusionException var7) {
            log.error("Failed to disconnect session [" + session.getSessionID() + "] for user [" + username + "]", var7);
         }
      }

      return sessionsToDisconnect;
   }

   public boolean isEmpty() {
      return this.sessions.isEmpty();
   }

   public void putFileReceived(final MessageDataIce messageIce) {
      Iterator i$ = this.sessions.values().iterator();

      while(i$.hasNext()) {
         final ChatSession session = (ChatSession)i$.next();
         (new ChatUserSessions.ChatUserSessionsCallable(session, false) {
            public void onCall() throws Exception {
               session.putFileReceived(messageIce);
            }

            public void logException(Exception e, int level) {
               ChatUserSessions.this.logOnSessionException(level, session, "Exception in putFileReceived, messageIce=" + messageIce, e);
            }
         }).call();
      }

   }

   public Set<String> contactRequest(Set<String> userList) {
      List<ChatSession> sessionsToNotify = new LinkedList();
      Set<String> contactRequestSentList = new HashSet();
      sessionsToNotify.addAll(this.sessions.values());
      Iterator i$ = sessionsToNotify.iterator();

      while(i$.hasNext()) {
         ChatSession sessionToNotify = (ChatSession)i$.next();
         Iterator i$ = userList.iterator();

         while(i$.hasNext()) {
            String username = (String)i$.next();

            try {
               sessionToNotify.contactRequest(username, userList.size());
               contactRequestSentList.add(username);
            } catch (Exception var10) {
               int level = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatUserSessionsSettings.FINAL_FAILURE_LOG_LEVEL);
               if (level != Integer.MAX_VALUE) {
                  this.logOnSessionException(level, sessionToNotify, "Exception in contactRequest, userList=" + userList, var10);
               }
            }
         }
      }

      return contactRequestSentList;
   }

   public void pushNotification(final Message message) {
      Iterator i$ = this.sessions.values().iterator();

      while(i$.hasNext()) {
         final ChatSession session = (ChatSession)i$.next();
         (new ChatUserSessions.ChatUserSessionsCallable(session, false) {
            public void onCall() throws Exception {
               session.pushNotification(message);
            }

            public void logException(Exception e, int level) {
               ChatUserSessions.this.logOnSessionException(level, session, "Exception in pushNotification, message=" + message, e);
            }
         }).call();
      }

   }

   public boolean putAnonymousCallNotifiaction(String requestingUsername, String requestingMobilePhone) {
      boolean found = false;
      Iterator i$ = this.sessions.values().iterator();

      while(i$.hasNext()) {
         ChatSession session = (ChatSession)i$.next();

         try {
            session.putAnonymousCallNotification(requestingUsername, requestingMobilePhone);
            found = true;
         } catch (Exception var8) {
            int level = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatUserSessionsSettings.FINAL_FAILURE_LOG_LEVEL);
            if (level != Integer.MAX_VALUE) {
               this.logOnSessionException(level, session, "Exception in putAnonymousCallNotifiaction, requestingUsername=" + requestingUsername + "requestingMobilePhone=" + requestingMobilePhone, var8);
            }
         }
      }

      return found;
   }

   public boolean supportsBinaryMessage() {
      Iterator i$ = this.sessions.values().iterator();

      ClientType device;
      do {
         if (!i$.hasNext()) {
            return false;
         }

         ChatSession session = (ChatSession)i$.next();
         device = session.getDeviceType();
      } while(device != ClientType.AJAX1 && device != ClientType.AJAX2 && device != ClientType.MIDP2 && device != ClientType.WINDOWS_MOBILE && device != ClientType.ANDROID && device != ClientType.BLACKBERRY && device != ClientType.MRE && device != ClientType.IOS);

      return true;
   }

   public void putWebCallNotification(String source, String destination, int gateway, String gatewayName, int protocol) throws FusionException {
      FusionException fe = new FusionException();
      if (this.sessions.size() == 0) {
         fe.message = destination + " is currently offline";
      } else {
         fe.message = destination + " does not have VOIP capability";
      }

      throw fe;
   }

   public void putEvent(final UserEventIce event) {
      Iterator i$ = this.sessions.values().iterator();

      while(i$.hasNext()) {
         final ChatSession session = (ChatSession)i$.next();
         (new ChatUserSessions.ChatUserSessionsCallable(session, false) {
            public void onCall() throws Exception {
               session.putEvent(event);
            }

            public void logException(Exception e, int level) {
               ChatUserSessions.this.logOnSessionException(level, session, "Exception in putEvent, event=" + event, e);
            }
         }).call();
      }

   }

   public void putServerQuestion(final String message, final String url) {
      Iterator i$ = this.sessions.values().iterator();

      while(i$.hasNext()) {
         final ChatSession session = (ChatSession)i$.next();
         (new ChatUserSessions.ChatUserSessionsCallable(session, false) {
            public void onCall() throws Exception {
               session.putServerQuestion(message, url);
            }

            public void logException(Exception e, int level) {
               ChatUserSessions.this.logOnSessionException(level, session, "Exception in putServerQuestion, message=" + message + " url=" + url, e);
            }
         }).call();
      }

   }

   public void putAlertMessage(final String message, final String title, final short timeout) {
      Iterator i$ = this.sessions.values().iterator();

      while(i$.hasNext()) {
         final ChatSession session = (ChatSession)i$.next();
         (new ChatUserSessions.ChatUserSessionsCallable(session, false) {
            public void onCall() throws Exception {
               session.putAlertMessage(message, title, timeout);
            }

            public void logException(Exception e, int level) {
               ChatUserSessions.this.logOnSessionException(level, session, "Exception in putAlertMessage, message=" + message + " title=" + title + " timeout=" + timeout, e);
            }
         }).call();
      }

   }

   public void putPrivateChatNowAGroupChat(final String groupChatID, final String creator) {
      Iterator i$ = this.sessions.values().iterator();

      while(i$.hasNext()) {
         final ChatSession session = (ChatSession)i$.next();
         (new ChatUserSessions.ChatUserSessionsCallable(session, false) {
            public void onCall() throws Exception {
               session.privateChatNowAGroupChat(groupChatID, creator);
            }

            public void logException(Exception e, int level) {
               ChatUserSessions.this.logOnSessionException(level, session, "Exception in putPrivateChatNowAGroupChat, groupChatID=" + groupChatID + " creator=" + creator, e);
            }
         }).call();
      }

   }

   public void accountBalanceChanged(final double balance, final double fundedBalance, final CurrencyDataIce currency) {
      Iterator i$ = this.sessions.values().iterator();

      while(i$.hasNext()) {
         final ChatSession session = (ChatSession)i$.next();
         (new ChatUserSessions.ChatUserSessionsCallable(session, false) {
            public void onCall() throws Exception {
               session.accountBalanceChanged(balance, fundedBalance, currency);
            }

            public void logException(Exception e, int level) {
               ChatUserSessions.this.logOnSessionException(level, session, "Exception in accountBalanceChanged, balance=" + balance + " fundedBalancee=" + fundedBalance + " currency=" + currency, e);
            }
         }).call();
      }

   }

   public void notifySessionsOfNewContactGroup(final ContactGroupData contactGroup, final int contactListVersion) {
      Iterator i$ = this.sessions.values().iterator();

      while(i$.hasNext()) {
         final ChatSession session = (ChatSession)i$.next();
         (new ChatUserSessions.ChatUserSessionsCallable(session, true) {
            public void onCall() throws Exception {
               session.contactGroupAdded(contactGroup, contactListVersion);
            }

            public void logException(Exception e, int level) {
               ChatUserSessions.this.logOnSessionException(level, session, "Exception in notifySessionsOfNewContactGroup, contactGroup=" + contactGroup + " contactListVersion=" + contactListVersion, e);
            }
         }).call();
      }

   }

   public void notifySessionsOfRemovedContactGroup(final int contactGroupID, final int contactListVersion) {
      Iterator i$ = this.sessions.values().iterator();

      while(i$.hasNext()) {
         final ChatSession session = (ChatSession)i$.next();
         (new ChatUserSessions.ChatUserSessionsCallable(session, true) {
            public void onCall() throws Exception {
               session.contactGroupRemoved(contactGroupID, contactListVersion);
            }

            public void logException(Exception e, int level) {
               ChatUserSessions.this.logOnSessionException(level, session, "Exception in notifySessionsOfRemovedContactGroup, contactGroupID=" + contactGroupID + " contactListVersion=" + contactListVersion, e);
            }
         }).call();
      }

   }

   public String[] getSessions() {
      List<String> sessionProxies = new ArrayList(this.sessions.size());
      Iterator i$ = this.sessions.values().iterator();

      while(i$.hasNext()) {
         ChatSession session = (ChatSession)i$.next();
         sessionProxies.add(session.getSessionID());
      }

      return (String[])sessionProxies.toArray(new String[this.sessions.size()]);
   }

   public void addSession(String sessionID, ChatSession session) {
      this.sessions.put(sessionID, session);
   }

   public int verifyCanCreateSession(String sessionID) throws FusionException {
      if (this.sessions.containsKey(sessionID)) {
         throw new FusionException("A session with the ID '" + sessionID + "' already exists");
      } else {
         return this.sessions.size();
      }
   }

   public void emailNotification(final int unreadEmailCount) {
      Iterator i$ = this.sessions.values().iterator();

      while(i$.hasNext()) {
         final ChatSession session = (ChatSession)i$.next();
         (new ChatUserSessions.ChatUserSessionsCallable(session, false) {
            public void onCall() throws Exception {
               session.emailNotification(unreadEmailCount);
            }

            public void logException(Exception e, int level) {
               ChatUserSessions.this.logOnSessionException(level, session, "Exception in emailNotification, unreadEmailCount=" + unreadEmailCount, e);
            }
         }).call();
      }

   }

   public void putSerializedPacket(byte[] packet) throws FusionException {
      Iterator i$ = this.sessions.values().iterator();

      while(i$.hasNext()) {
         ChatSession session = (ChatSession)i$.next();
         session.putSerializedPacket(packet);
      }

   }

   public void themeChanged(final String themeLocation) {
      Iterator i$ = this.sessions.values().iterator();

      while(i$.hasNext()) {
         final ChatSession session = (ChatSession)i$.next();
         (new ChatUserSessions.ChatUserSessionsCallable(session, false) {
            public void onCall() throws Exception {
               session.themeChanged(themeLocation);
            }

            public void logException(Exception e, int level) {
               ChatUserSessions.this.logOnSessionException(level, session, "Exception in themeChanged, themeLocation=" + themeLocation, e);
            }
         }).call();
      }

   }

   public void putMessageStatusEvent(final MessageStatusEvent mse) {
      Iterator i$ = this.sessions.values().iterator();

      while(i$.hasNext()) {
         final ChatSession session = (ChatSession)i$.next();
         if (session.isMessageStatusEventCapable()) {
            (new ChatUserSessions.ChatUserSessionsCallable(session, false) {
               public void onCall() throws Exception {
                  session.putMessageStatusEvent(mse);
               }

               public void logException(Exception e, int level) {
                  ChatUserSessions.this.logOnSessionException(level, session, "Exception in putMessageStatusEvent, event=" + mse + " e=" + e, e);
               }
            }).call();
         }
      }

   }

   public ChatSessionState[] getState() {
      List<ChatSessionState> sessionProxies = new ArrayList(this.sessions.size());
      Iterator i$ = this.sessions.values().iterator();

      while(i$.hasNext()) {
         ChatSession session = (ChatSession)i$.next();
         sessionProxies.add(session.getState());
      }

      return (ChatSessionState[])sessionProxies.toArray(new ChatSessionState[this.sessions.size()]);
   }

   public static int getTotalIntermediateFails() {
      return totalIntermediateFails.get();
   }

   public static int getTotalFinalFails() {
      return totalFinalFails.get();
   }

   public abstract class ChatUserSessionsCallable implements Callable<Boolean> {
      private final ChatSession session;
      private final boolean purgeOnFail;
      private int tries = 0;

      protected abstract void onCall() throws Exception;

      protected abstract void logException(Exception var1, int var2);

      public ChatUserSessionsCallable(ChatSession session, boolean purgeOnFail) {
         this.session = session;
         this.purgeOnFail = purgeOnFail;
      }

      public Boolean call_prePt73110676() {
         try {
            this.onCall();
            return true;
         } catch (Exception var3) {
            int level;
            if (ChatUserSessions.this.retriesEnabled() && ++this.tries < SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatUserSessionsSettings.TRIES_LIMIT)) {
               level = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatUserSessionsSettings.RETRY_INTERVAL_MILLIS) * this.tries;
               ChatUserSessions.this.retryService.schedule(this, (long)level, TimeUnit.MILLISECONDS);
            } else if (this.purgeOnFail && ChatUserSessions.this.immediatePurgingEnabled()) {
               ArrayList<String> sessionsToPurge = new ArrayList();
               sessionsToPurge.add(this.session.getSessionID());
               ChatUserSessions.this.removeSessions(sessionsToPurge, false);
            }

            level = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatUserSessionsSettings.FINAL_FAILURE_LOG_LEVEL);
            if (level != Integer.MAX_VALUE) {
               this.logException(var3, level);
            }

            return false;
         }
      }

      public Boolean call() {
         if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatUserSessionsSettings.PT73110676_CHANGES_ENABLED)) {
            return this.call_prePt73110676();
         } else {
            try {
               this.onCall();
               return true;
            } catch (TimeoutException var4) {
               int triesLimit = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatUserSessionsSettings.TRIES_LIMIT);
               int level;
               if (ChatUserSessions.this.retriesEnabled() && ++this.tries < triesLimit) {
                  level = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatUserSessionsSettings.RETRY_INTERVAL_MILLIS) * this.tries;
                  ChatUserSessions.this.retryService.schedule(this, (long)level, TimeUnit.MILLISECONDS);
               } else if (this.purgeOnFail && ChatUserSessions.this.immediatePurgingEnabled()) {
                  ArrayList<String> sessionsToPurge = new ArrayList();
                  sessionsToPurge.add(this.session.getSessionID());
                  ChatUserSessions.this.removeSessions(sessionsToPurge, false);
               }

               if (ChatUserSessions.this.retriesEnabled() && this.tries < triesLimit) {
                  ChatUserSessions.totalIntermediateFails.incrementAndGet();
                  this.logException(var4, 30000);
               } else {
                  ChatUserSessions.totalFinalFails.incrementAndGet();
                  level = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatUserSessionsSettings.FINAL_FAILURE_LOG_LEVEL);
                  if (level != Integer.MAX_VALUE) {
                     this.logException(var4, level);
                  }
               }

               return false;
            } catch (Exception var5) {
               this.logException(var5, 40000);
               return false;
            }
         }
      }
   }
}
