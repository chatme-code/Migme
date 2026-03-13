package com.projectgoth.fusion.chat.external.gtalk;

import com.projectgoth.fusion.chat.external.ChatConnectionInterface;
import com.projectgoth.fusion.chat.external.ChatConnectionListenerInterface;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.fdl.enums.ImType;
import com.projectgoth.fusion.fdl.enums.PresenceType;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.Roster.SubscriptionMode;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.packet.VCard;

public class GTalkSession implements ChatConnectionInterface {
   private static final Logger log = Logger.getLogger(GTalkSession.class);
   private static String connectURL = "talk.google.com";
   private static int port = 5222;
   private ChatConnectionListenerInterface listener;
   private GTalkListener gtalkListener;
   private Roster roster;
   private ChatManager chatManager;
   private XMPPConnection xmppConnection;
   private String avatarLocation;
   private Avatar avatar;

   public GTalkSession(ChatConnectionListenerInterface listener, String avatarLocation) throws GTalkException {
      if (!SystemProperty.getBool("GTalkEnabled", true)) {
         throw new GTalkException("GTalk is disabled for a while...");
      } else if (listener == null) {
         throw new IllegalArgumentException("Listener cannot be null");
      } else {
         this.listener = listener;
         this.avatarLocation = avatarLocation;
         this.gtalkListener = new GTalkListener(this, listener);
      }
   }

   private void connectToServer() throws GTalkException {
      ConnectionConfiguration config = new ConnectionConfiguration(connectURL, port, "gmail.com");
      config.setSASLAuthenticationEnabled(true);
      config.setCompressionEnabled(true);
      config.setReconnectionAllowed(false);
      config.setRosterLoadedAtLogin(true);
      if (this.xmppConnection == null) {
         this.xmppConnection = new XMPPConnection(config);
      }

      try {
         this.xmppConnection.connect();
      } catch (Exception var3) {
         log.error("Connecting to gtalk server failed: " + var3.getMessage());
         this.listener.onSignInFailed(this, var3.getMessage());
         throw new GTalkException("Connection to google server failed. Please try again later.");
      }
   }

   public void signIn(String username, String password) throws GTalkException {
      if (this.xmppConnection == null || !this.xmppConnection.isConnected()) {
         this.connectToServer();
      }

      if (!StringUtil.isBlank(username)) {
         if (username.indexOf("@") == -1) {
            username = username + "@gmail.com";
         }

         if (!this.xmppConnection.isAuthenticated()) {
            try {
               this.xmppConnection.login(username, password, "mig33");
            } catch (Exception var5) {
               log.warn("GTalk login failed for user[" + username + "]: " + var5.getMessage());
               this.listener.onSignInFailed(this, var5.getMessage());
               throw new GTalkException("GTalk login failed. Please try again later.");
            }
         }

         this.listener.onSignInSuccess(this);

         try {
            this.roster = this.xmppConnection.getRoster();
            this.propagateContacts(this.roster);
            this.roster.addRosterListener(this.gtalkListener);
            this.roster.setSubscriptionMode(SubscriptionMode.accept_all);
            this.chatManager = this.xmppConnection.getChatManager();
            this.chatManager.addChatListener(this.gtalkListener);
            this.xmppConnection.addConnectionListener(this.gtalkListener);
            this.setStatus(GTalkStatus.AVAILABLE, "I'm using migme (http://mig.me)...");
            this.setAvatar(this.avatarLocation);
         } catch (Exception var4) {
            log.error(var4.getMessage());
         }

      } else {
         this.xmppConnection.disconnect();
         throw new GTalkException("Username is empty");
      }
   }

   public void signOut() {
      log.debug("Stopping gtalk session...");

      try {
         if (this.xmppConnection == null) {
            this.listener.onDisconnected(this, "Signed out off gtalk");
            return;
         }

         if (this.roster != null) {
            this.roster.removeRosterListener(this.gtalkListener);
         }

         if (this.chatManager != null) {
            this.chatManager.removeChatListener(this.gtalkListener);
            this.chatManager = null;
         }

         this.xmppConnection.removeConnectionListener(this.gtalkListener);
         this.xmppConnection.disconnect();
         this.xmppConnection = null;
         this.listener.onDisconnected(this, "Signed out off gtalk");
      } catch (Exception var2) {
         log.error("Failed to signout from gtalk: " + var2.getMessage());
      }

   }

   public void propagateContacts(Roster roster) {
      String displayName;
      String userName;
      for(Iterator i$ = roster.getEntries().iterator(); i$.hasNext(); this.listener.onContactDetail(this, userName, displayName)) {
         RosterEntry entry = (RosterEntry)i$.next();
         displayName = entry.getName();
         userName = entry.getUser();
         if (StringUtil.isBlank(displayName)) {
            if (userName.indexOf("@") == -1) {
               displayName = userName;
            } else {
               displayName = userName.substring(0, userName.indexOf("@"));
            }
         }
      }

   }

   public void sendMessage(String id, String messageContent) throws GTalkException {
      try {
         this.chatManager.createChat(id, this.gtalkListener).sendMessage(messageContent);
      } catch (XMPPException var4) {
         this.listener.onMessageFailed(this, (String)null, id, messageContent, (String)null);
         throw new GTalkException(var4);
      } catch (Exception var5) {
         this.signOut();
         throw new GTalkException(var5);
      }
   }

   public void addContact(String contact) throws GTalkException {
      if (this.roster != null) {
         RosterEntry toAdd = this.roster.getEntry(contact);
         if (toAdd != null) {
            throw new GTalkException(contact + " already exists in your friends list");
         }

         try {
            this.roster.createEntry(contact, contact, (String[])null);
         } catch (Exception var4) {
            throw new GTalkException(var4);
         }
      }

   }

   public void removeContact(String contact) throws GTalkException {
      if (this.roster != null) {
         RosterEntry toRemove = this.roster.getEntry(contact);
         if (toRemove == null) {
            throw new GTalkException(contact + " is not on your friends list");
         }

         try {
            this.roster.removeEntry(toRemove);
         } catch (Exception var4) {
            throw new GTalkException(var4);
         }
      }

   }

   public boolean isConnected() {
      return this.xmppConnection != null && this.xmppConnection.isConnected();
   }

   public boolean isSignedIn() {
      return this.xmppConnection != null && this.xmppConnection.isConnected() && this.xmppConnection.isAuthenticated();
   }

   public String getUsername() {
      return this.isSignedIn() ? this.xmppConnection.getUser() : "";
   }

   public String getUserId() {
      return this.isSignedIn() ? this.xmppConnection.getUser() : "";
   }

   public void setStatus(GTalkStatus status, String personalMessage) throws GTalkException {
      if (this.xmppConnection != null && this.xmppConnection.isConnected() && this.xmppConnection.isAuthenticated()) {
         Presence packet = status.toXMPPPresence();
         packet.setStatus(personalMessage);

         try {
            this.xmppConnection.sendPacket(packet);
         } catch (Exception var5) {
            this.signOut();
            throw new GTalkException(var5);
         }
      } else {
         throw new GTalkException("User is not logged in");
      }
   }

   public void setAvatar(String avatarLocation) throws GTalkException {
      try {
         this.avatarLocation = avatarLocation;
         this.avatar = new Avatar(avatarLocation);
         if (this.isSignedIn()) {
            VCard vcard = new VCard();
            vcard.load(this.xmppConnection, this.xmppConnection.getUser());
            vcard.setAvatar(this.avatar.getBytes());
            vcard.save(this.xmppConnection);
         }

      } catch (XMPPException var3) {
         throw new GTalkException(var3.getMessage());
      } catch (NoSuchAlgorithmException var4) {
         throw new GTalkException(var4.getMessage());
      } catch (Exception var5) {
         throw new GTalkException(var5.getMessage());
      }
   }

   public ImType getImType() {
      return ImType.GTALK;
   }

   public void setStatus(PresenceType status, String message) throws Exception {
      this.setStatus(GTalkStatus.fromFusionPresence(status), message);
   }

   public String inviteToConference(String conferenceID, String username) throws Exception {
      throw new UnsupportedOperationException("GTalk chat does not support conferences");
   }

   public void leaveConference(String conferenceID) throws Exception {
      throw new UnsupportedOperationException("GTalk chat does not support conferences");
   }

   public List<String> getConferenceParticipants(String conferenceID) throws Exception {
      throw new UnsupportedOperationException("GTalk chat does not support conferences");
   }

   static {
      SmackConfiguration.setKeepAliveInterval(-1);
      log.info("Connecting to GTalk using smack version " + SmackConfiguration.getVersion());
   }
}
