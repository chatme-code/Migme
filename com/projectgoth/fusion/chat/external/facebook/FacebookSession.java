package com.projectgoth.fusion.chat.external.facebook;

import com.projectgoth.fusion.chat.external.ChatConnectionInterface;
import com.projectgoth.fusion.chat.external.ChatConnectionListenerInterface;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.fdl.enums.ImType;
import com.projectgoth.fusion.fdl.enums.PresenceType;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.Roster.SubscriptionMode;
import org.jivesoftware.smack.packet.Presence;

public class FacebookSession implements ChatConnectionInterface {
   private static final Logger log = Logger.getLogger(FacebookSession.class);
   private ChatConnectionListenerInterface listener;
   private FacebookListener fbListener;
   private Roster roster;
   private ChatManager chatManager;
   private static String connectURL = "chat.facebook.com";
   private static int port = 5222;
   private String username;
   private static String apiKey = SystemProperty.get("FacebookApiKey", "161865877194414");
   private XMPPConnection xmppConnection;
   private static AtomicInteger concurrentConnectionsCount = new AtomicInteger();

   public FacebookSession(ChatConnectionListenerInterface listener) throws FacebookException {
      if (!SystemProperty.getBool("FacebookEnabled", true)) {
         throw new FacebookException("Facebook is disabled for a while...");
      } else if (listener == null) {
         throw new IllegalArgumentException("Listener cannot be null");
      } else {
         this.listener = listener;
         this.fbListener = new FacebookListener(this, listener);
      }
   }

   private void connectToServer() throws FacebookException {
      ConnectionConfiguration config = new ConnectionConfiguration(connectURL, port, "chat.facebook.com");
      config.setSASLAuthenticationEnabled(true);
      config.setCompressionEnabled(true);
      config.setReconnectionAllowed(false);
      config.setRosterLoadedAtLogin(true);
      if (this.xmppConnection == null) {
         this.xmppConnection = new XMPPConnection(config);
      }

      try {
         if (!this.xmppConnection.isConnected()) {
            this.xmppConnection.connect();
         }

      } catch (Exception var3) {
         log.error("Connecting to chat.facebook.com failed: " + var3.getMessage(), var3);
         this.listener.onSignInFailed(this, var3.getMessage());
         throw new FacebookException("Connection to facebook server failed. Please try again later.");
      }
   }

   public void signIn(String login, String accessToken) throws FacebookException {
      this.username = login;
      int maxConcurrentConnections = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.IMSettings.MAX_CONCURRENT_CONNECTIONS.forIM(ImType.FACEBOOK));
      if (concurrentConnectionsCount.incrementAndGet() > maxConcurrentConnections) {
         concurrentConnectionsCount.decrementAndGet();
         log.info("Maximum number of concurrent sessions exceeded. [" + concurrentConnectionsCount.get() + "] max: " + maxConcurrentConnections);
         throw new FacebookException("Unable to connect to facebook at the moment. Please try to connect at a later time.");
      } else {
         try {
            if (this.xmppConnection == null || !this.xmppConnection.isConnected()) {
               this.connectToServer();
            }

            if (!this.xmppConnection.isAuthenticated()) {
               this.xmppConnection.login(apiKey, accessToken);
            }

            String userId = this.getUserId();
            if (userId != null && userId != "" && !userId.startsWith("-0@")) {
               this.listener.onSignInSuccess(this);
               this.roster = this.xmppConnection.getRoster();
               this.roster.addRosterListener(this.fbListener);
               this.roster.setSubscriptionMode(SubscriptionMode.manual);
               this.propagateContacts(this.roster);
               this.chatManager = this.xmppConnection.getChatManager();
               this.chatManager.addChatListener(this.fbListener);
               this.xmppConnection.addConnectionListener(this.fbListener);
               this.setStatus(FacebookStatus.AVAILABLE, SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.IMSettings.DEFAULT_ONLINE_MESSAGE));
            } else {
               throw new Exception("invalid access token");
            }
         } catch (Exception var7) {
            Exception e = var7;
            log.warn("Facebook login failed for user:" + this.username + " error: ", var7);

            try {
               this.listener.onSignInFailed(this, e.getMessage());
               this.stopSession();
            } catch (Exception var6) {
               log.error("Error occurred in cleaning up session fo facebook after failed login.", var6);
            }

            throw new FacebookException("Facebook login failed. Please try re-authenticating your account with facebook again.");
         }
      }
   }

   public void propagateContacts(Roster roster) {
      Iterator i$ = roster.getEntries().iterator();

      while(true) {
         RosterEntry entry;
         Presence presence;
         do {
            if (!i$.hasNext()) {
               return;
            }

            entry = (RosterEntry)i$.next();
            this.listener.onContactDetail(this, entry.getUser(), entry.getName());
            presence = roster.getPresence(entry.getUser());
         } while(!presence.isAvailable() && !presence.isAway());

         this.listener.onContactStatusChanged(this, entry.getUser(), FacebookStatus.AVAILABLE.toFusionPresence());
      }
   }

   public void stopSession() {
      log.debug("Stopping facebook session...");

      try {
         if (this.xmppConnection != null) {
            if (this.roster != null) {
               this.roster.removeRosterListener(this.fbListener);
            }

            if (this.chatManager != null) {
               this.chatManager.removeChatListener(this.fbListener);
               this.chatManager = null;
            }

            this.xmppConnection.removeConnectionListener(this.fbListener);
            this.xmppConnection.disconnect();
            this.listener.onDisconnected(this, "Signed out off facebook");
            return;
         }

         this.listener.onDisconnected(this, "Signed out off facebook");
      } catch (Exception var6) {
         log.warn("Exception occurred in signing off from Facebook.", var6);
         return;
      } finally {
         this.xmppConnection = null;
         concurrentConnectionsCount.decrementAndGet();
      }

   }

   public void sendMessage(String userId, String messageContent) throws FacebookException {
      try {
         this.chatManager.createChat(userId, this.fbListener).sendMessage(messageContent);
      } catch (XMPPException var4) {
         log.error("Unable to send facebook message from:" + this.getUserId() + " to:" + userId + " error: ", var4);
         this.listener.onMessageFailed(this, (String)null, userId, messageContent, (String)null);
         throw new FacebookException(var4);
      } catch (Exception var5) {
         log.error("Unable to send facebook message from:" + this.getUserId() + " to:" + userId + " error: ", var5);
         this.stopSession();
         throw new FacebookException(var5);
      }
   }

   public void addContact(String contact) throws FacebookException {
      if (this.roster != null) {
         this.roster.setSubscriptionMode(SubscriptionMode.accept_all);
         RosterEntry toAdd = this.roster.getEntry(contact);
         if (toAdd != null) {
            throw new FacebookException(contact + " already exists in your friends list");
         } else {
            try {
               this.roster.createEntry(contact, contact, (String[])null);
            } catch (XMPPException var4) {
               log.error("Unable to add facebook contact", var4);
               throw new FacebookException(var4);
            } catch (Exception var5) {
               log.error("Unable to add facebook contact", var5);
               this.stopSession();
               throw new FacebookException(var5);
            }
         }
      }
   }

   public void removeContact(String contact) throws FacebookException {
      if (this.roster != null) {
         RosterEntry toRemove = this.roster.getEntry(contact);
         if (toRemove == null) {
            throw new FacebookException(contact + " is not on your friends list");
         } else {
            try {
               this.roster.removeEntry(toRemove);
            } catch (XMPPException var4) {
               log.error("Unable to remove facebook contact", var4);
               throw new FacebookException(var4);
            } catch (Exception var5) {
               log.error("Unable to remove facebook contact", var5);
               this.stopSession();
               throw new FacebookException(var5);
            }
         }
      }
   }

   public boolean isConnected() {
      return this.xmppConnection != null && this.xmppConnection.isConnected();
   }

   public boolean isSignedIn() {
      return this.xmppConnection != null && this.xmppConnection.isConnected() && this.xmppConnection.isAuthenticated() && !this.xmppConnection.getUser().startsWith("-0@");
   }

   public String getUsername() {
      return this.isSignedIn() ? this.xmppConnection.getUser() : "";
   }

   public String getUserId() {
      return this.isSignedIn() ? this.xmppConnection.getUser() : "";
   }

   public void setStatus(FacebookStatus status, String personalMessage) throws FacebookException {
      if (this.xmppConnection == null || !this.xmppConnection.isConnected() || !this.xmppConnection.isAuthenticated()) {
         throw new FacebookException("User is not logged in");
      } else {
         Presence packet = status.toXMPPPresence();
         packet.setStatus(personalMessage);

         try {
            this.xmppConnection.sendPacket(packet);
         } catch (Exception var5) {
            log.error("Unable to set facebook status", var5);
            this.stopSession();
            throw new FacebookException(var5);
         }
      }
   }

   public ImType getImType() {
      return ImType.FACEBOOK;
   }

   public void signOut() {
      this.stopSession();
   }

   public void setAvatar(String fileLocation) throws Exception {
      throw new UnsupportedOperationException("Facebook chat does not support changing profile pictures");
   }

   public void setStatus(PresenceType status, String message) throws Exception {
      this.setStatus(FacebookStatus.fromFusionPresence(status), message);
   }

   public String inviteToConference(String conferenceID, String username) throws Exception {
      throw new UnsupportedOperationException("Facebook chat does not support conferences");
   }

   public void leaveConference(String conferenceID) throws Exception {
      throw new UnsupportedOperationException("Facebook chat does not support conferences");
   }

   public List<String> getConferenceParticipants(String conferenceID) throws Exception {
      throw new UnsupportedOperationException("Facebook chat does not support conferences");
   }

   static {
      SmackConfiguration.setPacketReplyTimeout(5000);
      SASLAuthentication.registerSASLMechanism("X-FACEBOOK-PLATFORM", FacebookConnectSASLMechanism.class);
      SASLAuthentication.supportSASLMechanism("X-FACEBOOK-PLATFORM", 0);
      SmackConfiguration.setKeepAliveInterval(-1);
      log.info("Connecting to Facebook using smack version " + SmackConfiguration.getVersion());
   }
}
