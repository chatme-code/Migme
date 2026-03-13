package com.projectgoth.fusion.chat.external.yahoo;

import com.google.gson.annotations.Expose;
import com.projectgoth.fusion.chat.external.ChatConnectionInterface;
import com.projectgoth.fusion.chat.external.ChatConnectionListenerInterface;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.fdl.enums.ImType;
import com.projectgoth.fusion.fdl.enums.PresenceType;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.log4j.Logger;

public class YahooConnection extends Connection implements ChatConnectionInterface {
   private static final String YAHOO_SERVER = "scsa.msg.yahoo.com";
   private static final int YAHOO_PORT = 5050;
   private static final int SIGNIN_TIMEOUT = 30000;
   private static final String CLIENT_VERSION = "9.0.0.2162";
   private static final String DEFAULT_GROUP = "migme";
   private static final String DEFAULT_MESSAGE = "http://mig.me";
   private static final boolean ALLOW_INVISIBLE = false;
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(YahooConnection.class));
   private static ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
   private ChatConnectionInterface source;
   private ChatConnectionListenerInterface listener;
   @Expose
   private String loginUser;
   private String loginPassword;
   private int sessionId;
   @Expose
   private AtomicBoolean isSignedIn = new AtomicBoolean(false);
   @Expose
   private AtomicBoolean isSigningIn = new AtomicBoolean(false);
   private Map<String, String> contactGroup = new HashMap();
   private Map<String, YahooConference> conferences = new HashMap();
   private AtomicInteger nextConferenceID = new AtomicInteger();
   @Expose
   private AtomicBoolean isInvisible = new AtomicBoolean(false);

   public YahooConnection(ChatConnectionListenerInterface listener, int maxConcurrentConnections, int connectionTimeout) {
      this.listener = listener;
      this.source = this;
      this.maxConcurrentConnections = maxConcurrentConnections;
      this.connectionTimeout = connectionTimeout;
   }

   public synchronized void signIn(String username, String password) throws YahooException {
      if (!SystemProperty.getBool("YahooEnabled", true)) {
         throw new YahooException("Yahoo is disabled for a while...");
      } else {
         try {
            if (!this.isSignedIn() && this.isSigningIn.compareAndSet(false, true)) {
               this.connect("scsa.msg.yahoo.com", 5050);
               this.loginUser = username.toLowerCase().split("@yahoo.co")[0];
               this.loginPassword = password;
               YMSGPacket auth = new YMSGPacket(YahooService.AUTH, YahooStatus.DEFAULT, 0);
               auth.setField(1, this.loginUser);
               this.sendAsyncPacket(auth);
               scheduler.schedule(new Runnable() {
                  public void run() {
                     if (YahooConnection.this.isSigningIn.compareAndSet(true, false)) {
                        YahooConnection.this.disconnect("");
                        YahooConnection.this.listener.onSignInFailed(YahooConnection.this.source, "Sign in time out");
                     }

                  }
               }, 30000L, TimeUnit.MILLISECONDS);
            }

         } catch (Exception var4) {
            this.isSigningIn.set(false);
            this.disconnect(var4.getMessage());
            throw new YahooException(var4.getMessage());
         }
      }
   }

   public boolean isSignedIn() {
      return this.isSignedIn.get() && this.isConnected();
   }

   public void signOut() {
      if (this.isSignedIn()) {
         YMSGPacket logoff = new YMSGPacket(YahooService.LOGOFF, YahooStatus.DEFAULT, this.sessionId);
         this.sendAsyncPacket(logoff);
      }

      this.disconnect("");
   }

   public void setStatus(YahooStatus status, String personalMessage) {
      switch(status) {
      case INVISIBLE:
         this.setStatus(YahooStatus.IDLE, personalMessage);
         this.isInvisible.set(true);
         break;
      case VISIBLE:
         this.setStatus(YahooStatus.AVAILABLE, personalMessage);
         this.isInvisible.set(false);
         break;
      case TYPING:
      case NONE:
      case DEFAULT:
      case AVAILABLE:
         status = YahooStatus.AVAILABLE;
      case IDLE:
      case BRB:
      case BUSY:
      case NOTATHOME:
      case NOTATDESK:
      case NOTINOFFICE:
      case ONPHONE:
      case ONVACATION:
      case OUTTOLUNCH:
      case STEPPEDOUT:
      case CUSTOM:
         if (this.isInvisible.get()) {
            this.isInvisible.set(false);
         }

         YMSGPacket statusPkt = new YMSGPacket(YahooService.Y6STATUS, YahooStatus.DEFAULT, this.sessionId);
         statusPkt.setField(10, String.valueOf(YahooStatus.CUSTOM.getValue()));
         if (personalMessage != null && personalMessage.length() > 0) {
            statusPkt.setField(19, personalMessage);
         } else {
            statusPkt.setField(19, "http://mig.me");
         }

         statusPkt.setField(97, "1");
         if (status != YahooStatus.AVAILABLE && status != YahooStatus.TYPING && status != YahooStatus.CUSTOM) {
            statusPkt.setField(47, "1");
         } else {
            statusPkt.setField(47, "0");
         }

         statusPkt.setField(187, "0");
         this.sendAsyncPacket(statusPkt);
         break;
      case OFFLINE:
         this.setStatus(YahooStatus.INVISIBLE, personalMessage);
      }

   }

   public void addContact(String username) {
      YMSGPacket addContact = new YMSGPacket(YahooService.ADDBUDDY, YahooStatus.DEFAULT, this.sessionId);
      addContact.setField(1, this.loginUser);
      addContact.setField(7, username);
      addContact.setField(65, "migme");
      addContact.setField(97, "1");
      addContact.setField(300, "319");
      this.sendAsyncPacket(addContact);
   }

   public void removeContact(String username) {
      YMSGPacket removeContact = new YMSGPacket(YahooService.REMBUDDY, YahooStatus.DEFAULT, this.sessionId);
      removeContact.setField(1, this.loginUser);
      removeContact.setField(7, username);
      removeContact.setField(65, (String)this.contactGroup.get(username));
      removeContact.setField(97, "1");
      removeContact.setField(300, "319");
      this.sendAsyncPacket(removeContact);
   }

   public void sendMessage(String username, String message) {
      YMSGPacket msg;
      if (this.conferences.containsKey(username)) {
         msg = new YMSGPacket(YahooService.CONFMSG, YahooStatus.DEFAULT, this.sessionId);
         msg.setField(1, this.loginUser);
         msg.setField(14, message);
         msg.setField(57, username);
         msg.setField(97, "1");
         YahooConference conference = (YahooConference)this.conferences.get(username);
         if (conference != null) {
            Iterator i$ = conference.getParticipants().iterator();

            while(i$.hasNext()) {
               String participant = (String)i$.next();
               msg.setField(53, participant);
            }
         }
      } else {
         msg = new YMSGPacket(YahooService.MESSAGE, YahooStatus.DEFAULT, this.sessionId);
         msg.setField(0, this.loginUser);
         msg.setField(1, this.loginUser);
         msg.setField(5, username);
         msg.setField(14, message);
      }

      this.sendAsyncPacket(msg);
   }

   public String inviteToConference(String conferenceID, String username) throws YahooException {
      String creator = null;
      if (!this.conferences.containsKey(conferenceID)) {
         creator = conferenceID;
         conferenceID = this.loginUser + "-" + this.nextConferenceID.incrementAndGet();
         this.conferences.put(conferenceID, new YahooConference());
         this.listener.onConferenceCreated(this, conferenceID, creator);
      }

      YMSGPacket conferenceInvite = new YMSGPacket(YahooService.CONFINVITE, YahooStatus.DEFAULT, this.sessionId);
      conferenceInvite.setField(1, this.loginUser);
      conferenceInvite.setField(13, "0");
      conferenceInvite.setField(50, this.loginUser);
      conferenceInvite.setField(52, username);
      conferenceInvite.setField(57, conferenceID);
      conferenceInvite.setField(58, "Join my conference");
      conferenceInvite.setField(97, "1");
      if (creator != null) {
         conferenceInvite.setField(52, creator);
      }

      this.sendAsyncPacket(conferenceInvite);
      return conferenceID;
   }

   public void leaveConference(String conferenceID) {
      YMSGPacket leaveConference = new YMSGPacket(YahooService.CONFLOGOFF, YahooStatus.DEFAULT, this.sessionId);
      leaveConference.setField(1, this.loginUser);
      leaveConference.setField(57, conferenceID);
      YahooConference conference = (YahooConference)this.conferences.remove(conferenceID);
      if (conference != null) {
         Iterator i$ = conference.getParticipants().iterator();

         while(i$.hasNext()) {
            String participant = (String)i$.next();
            leaveConference.setField(3, participant);
         }
      }

      this.sendAsyncPacket(leaveConference);
   }

   public List<String> getConferenceParticipants(String conferenceID) {
      YahooConference conference = (YahooConference)this.conferences.get(conferenceID);
      return conference == null ? Collections.EMPTY_LIST : conference.getParticipants();
   }

   public ImType getImType() {
      return ImType.YAHOO;
   }

   public String getUsername() {
      return this.loginUser;
   }

   public void setAvatar(String fileLocation) throws Exception {
      throw new UnsupportedOperationException("Yahoo chat does not support changing profile pictures");
   }

   public void setStatus(PresenceType status, String message) throws Exception {
      this.setStatus(YahooStatus.fromFusionPresence(status), message);
   }

   public void ping() {
      YMSGPacket keepAlive = new YMSGPacket(YahooService.KEEPALIVE, YahooStatus.DEFAULT, this.sessionId);
      if (this.loginUser != null) {
         keepAlive.setField(0, this.loginUser);
      }

      this.sendAsyncPacket(keepAlive);
   }

   protected void onDisconnect(String reason) {
      if (this.isSignedIn.compareAndSet(true, false)) {
         this.listener.onDisconnected(this, reason);
      }

   }

   protected synchronized void onIncomingPacket(YMSGPacket packet) {
      YahooService service = YahooService.valueOf(packet.getService());
      if (log.isDebugEnabled()) {
         log.debug("Yahoo packet: " + service + " user:" + this.getUsername());
      }

      if (service != null) {
         switch(service) {
         case AUTH:
            this.onAuth(packet);
            break;
         case AUTHRESP:
            this.onAuthResp(packet);
            break;
         case LIST:
            this.onLogon(packet);
            break;
         case LOGON:
            this.onOtherLogon(packet);
            break;
         case LOGOFF:
            this.onOtherLogoff(packet);
            break;
         case LISTV15:
            this.onListAll(packet);
            break;
         case STATUSV15:
            this.onStatusAll(packet);
            break;
         case Y6STATUS:
            this.onStatus(packet, (YahooStatus)null);
            break;
         case MESSAGE:
         case CONFMSG:
            this.onMessage(packet);
            break;
         case ADDBUDDY:
            this.onAddBuddy(packet);
            break;
         case NEWCONTACT:
            this.onNewContact(packet);
            break;
         case CONFINVITE:
         case CONFADDINVITE:
            this.onConferenceInvite(packet);
            break;
         case CONFDECLINE:
            this.onConferenceDecline(packet);
            break;
         case CONFLOGON:
            this.onConferenceLogon(packet);
            break;
         case CONFLOGOFF:
            this.onConferenceLogoff(packet);
         }
      }

   }

   private void onAuth(YMSGPacket packet) {
      this.sessionId = packet.getSessionId();

      try {
         String chal = packet.getField(94);
         String[] cResponse = null;
         if (chal != null) {
            cResponse = ChallengeResponseV15.getStrings(this.loginUser, this.loginPassword, chal);
         }

         if (cResponse != null && cResponse[0] != null && cResponse[1] != null && cResponse[2] != null) {
            YMSGPacket login = new YMSGPacket(YahooService.AUTHRESP, YahooStatus.DEFAULT, 0);
            login.setField(0, this.loginUser);
            login.setField(1, this.loginUser);
            login.setField(2, this.loginUser);
            login.setField(2, "1");
            login.setField(135, "9.0.0.2162");
            login.setField(244, "4194239");
            login.setField(277, cResponse[1]);
            login.setField(278, cResponse[2]);
            login.setField(307, cResponse[0]);
            login.setField(98, "us");
            this.sendAsyncPacket(login);
         } else if (this.isSigningIn.compareAndSet(true, false)) {
            this.isSignedIn.set(false);
            this.listener.onSignInFailed(this, "Incorrect ID or password");
         }
      } catch (Exception var5) {
         log.warn("Yahoo login failed for " + this.loginUser, var5);
      }

   }

   private void onAuthResp(YMSGPacket packet) {
      if (this.isSigningIn.compareAndSet(true, false)) {
         this.isSignedIn.set(false);
         String reasonCode = packet.getField(66);
         if (reasonCode == null) {
            log.warn("Yahoo login failed for " + this.loginUser + " on onAuthResp() with no reason code\n" + packet.toString());
            this.listener.onSignInFailed(this, "Yahoo service temporary unavailable");
         } else {
            int code = Integer.parseInt(reasonCode);
            if (code != 3 && code != 13) {
               if (code == 14) {
                  this.listener.onSignInFailed(this, "Locked ID");
               } else {
                  log.warn("Yahoo login failed for " + this.loginUser + " on onAuthResp() with reason code " + reasonCode + "\n" + packet.toString());
                  this.listener.onSignInFailed(this, "Yahoo service temporary unavailable (" + reasonCode + ")");
               }
            } else {
               this.listener.onSignInFailed(this, "Incorrect ID or password");
            }
         }
      }

   }

   private void onLogon(YMSGPacket packet) {
      if (this.isSigningIn.compareAndSet(true, false)) {
         this.isSignedIn.set(true);
         this.onStatus(packet, YahooStatus.AVAILABLE);
         this.listener.onSignInSuccess(this);
      }

   }

   private void onOtherLogon(YMSGPacket packet) {
      this.onStatus(packet, YahooStatus.AVAILABLE);
   }

   private void onOtherLogoff(YMSGPacket packet) {
      this.onStatus(packet, YahooStatus.OFFLINE);
   }

   private void onListAll(YMSGPacket packet) {
      int i = 0;
      int u = 0;
      int g = 0;
      String group = "";

      while(true) {
         String type = packet.getField(300, i++);
         if (type == null) {
            break;
         }

         if (type.equals("318")) {
            group = packet.getField(65, g++);
         } else if (type.equals("319")) {
            String username = packet.getField(7, u++);
            if (username == null) {
               break;
            }

            if (group != null) {
               this.contactGroup.put(username, group);
            }

            this.listener.onContactDetail(this, username, username);
            this.listener.onContactStatusChanged(this, username, YahooStatus.OFFLINE.toFusionPresence());
         }
      }

   }

   private void onStatusAll(YMSGPacket packet) {
      int i = 0;

      while(true) {
         String username;
         String s;
         String f;
         do {
            username = packet.getField(7, i);
            if (username == null) {
               return;
            }

            s = packet.getField(10, i);
            f = packet.getField(47, i++);
         } while(s == null);

         YahooStatus status = YahooStatus.valueOf(Integer.parseInt(s));
         if (status == YahooStatus.CUSTOM) {
            if (f != null && !f.equals("0")) {
               if (f.equals("1")) {
                  status = YahooStatus.BUSY;
               } else {
                  status = YahooStatus.OFFLINE;
               }
            } else {
               status = YahooStatus.AVAILABLE;
            }
         }

         if (status != null) {
            this.listener.onContactStatusChanged(this, username, status.toFusionPresence());
         }
      }
   }

   private void onStatus(YMSGPacket packet, YahooStatus status) {
      String username = packet.getField(7);
      if (username != null) {
         if (status == null) {
            this.onStatusAll(packet);
         } else {
            this.listener.onContactStatusChanged(this, username, status.toFusionPresence());
         }
      }

   }

   private void onMessage(YMSGPacket packet) {
      int i = 0;
      int status = packet.getStatus();
      String conferenceID = packet.getField(57);
      String sender = packet.getField(conferenceID == null ? 4 : 3, i);

      for(String message = packet.getField(14, i); sender != null && message != null; message = packet.getField(14, i)) {
         message = message.replaceAll("(<|</)(FONT|font)[^>]*>", "").replaceAll(".\\[[^m]*m", "").replaceAll("(<|</)(FADE|fade)[^>]*>", "").replaceAll("(<|</)(ALT|alt)[^>]*>", "");
         if (status != 1 && status != 5) {
            this.listener.onMessageFailed(this, conferenceID, sender, message, (String)null);
         } else if (!sender.equals(this.loginUser)) {
            this.listener.onMessageReceived(this, conferenceID, sender, message);
         }

         ++i;
         conferenceID = packet.getField(57, i);
         sender = packet.getField(conferenceID == null ? 4 : 3, i);
      }

   }

   private void onAddBuddy(YMSGPacket packet) {
      if ("0".equals(packet.getField(66))) {
         String username = packet.getField(7);
         if (username != null) {
            this.contactGroup.put(username, packet.getField(65));
         }

         this.listener.onContactDetail(this, username, username);
      }

   }

   private void onNewContact(YMSGPacket packet) {
      String username = packet.getField(7);
      if (username != null) {
         String s = packet.getField(10);
         if (s != null) {
            YahooStatus status = YahooStatus.valueOf(Integer.parseInt(s));
            if (status != null) {
               this.listener.onContactStatusChanged(this, username, status.toFusionPresence());
            }
         }
      }

   }

   private void onConferenceInvite(YMSGPacket packet) {
      String conferenceID = packet.getField(57);
      String creator = packet.getField(50);
      if (conferenceID != null && creator != null) {
         YahooConference conference = (YahooConference)this.conferences.get(conferenceID);
         if (conference == null) {
            conference = new YahooConference();
            this.conferences.put(conferenceID, conference);
            this.listener.onConferenceCreated(this, conferenceID, creator);
         }

         conference.addParticipant(creator);
         YMSGPacket conferenceLogon = new YMSGPacket(YahooService.CONFLOGON, YahooStatus.DEFAULT, this.sessionId);
         conferenceLogon.setField(1, this.loginUser);
         conferenceLogon.setField(3, creator);
         conferenceLogon.setField(57, conferenceID);
         int i = 0;

         String participant;
         for(participant = packet.getField(52, i); participant != null; participant = packet.getField(52, i)) {
            if (!participant.equals(this.loginUser)) {
               conferenceLogon.setField(3, participant);
            }

            ++i;
         }

         i = 0;

         for(participant = packet.getField(53, i); participant != null; participant = packet.getField(53, i)) {
            if (!participant.equals(this.loginUser)) {
               conference.addParticipant(participant);
               conferenceLogon.setField(3, participant);
               this.listener.onUserJoinedConference(this, conferenceID, participant);
            }

            ++i;
         }

         this.sendAsyncPacket(conferenceLogon);
      }

   }

   private void onConferenceDecline(YMSGPacket packet) {
      String conferenceID = packet.getField(57);
      String username = packet.getField(54);
      if (conferenceID != null && username != null) {
         String reason = packet.getField(14);
         if (reason != null && reason.length() != 0) {
            reason = username + " replied [" + reason + "]";
         } else {
            reason = username + " declined your conference invitation";
         }

         this.listener.onConferenceInvitationFailed(this, conferenceID, username, reason);
      }

   }

   private void onConferenceLogon(YMSGPacket packet) {
      String conferenceID = packet.getField(57);
      if (conferenceID != null) {
         YahooConference conference = (YahooConference)this.conferences.get(conferenceID);
         if (conference == null) {
            conference = new YahooConference();
            this.conferences.put(conferenceID, conference);
         }

         int i = 0;

         for(String participant = packet.getField(53, i); participant != null; participant = packet.getField(53, i)) {
            if (!participant.equals(this.loginUser)) {
               conference.addParticipant(participant);
               this.listener.onUserJoinedConference(this, conferenceID, participant);
            }

            ++i;
         }
      }

   }

   private void onConferenceLogoff(YMSGPacket packet) {
      String conferenceID = packet.getField(57);
      if (conferenceID != null) {
         YahooConference conference = (YahooConference)this.conferences.get(conferenceID);
         if (conference != null) {
            int i = 0;

            for(String participant = packet.getField(56, i); participant != null; participant = packet.getField(56, i)) {
               conference.removeParticipant(participant);
               this.listener.onUserLeftConference(this, conferenceID, participant);
               ++i;
            }
         }
      }

   }
}
