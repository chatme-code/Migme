package com.projectgoth.fusion.chat.external.msn;

import com.google.gson.annotations.Expose;
import com.projectgoth.fusion.chat.external.ChatConnectionInterface;
import com.projectgoth.fusion.chat.external.ChatConnectionListenerInterface;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.fdl.enums.ImType;
import com.projectgoth.fusion.fdl.enums.PresenceType;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import org.apache.log4j.Logger;

public class MSNConnection extends Connection implements ChatConnectionInterface {
   private static final String DISPATCH_SERVER = "messenger.hotmail.com";
   private static final int DISPATCH_SERVER_PORT = 1863;
   private static final String PROTOCOL_VERSION = "MSNP11";
   private static final String CVR_PARAM = "0x0409 winnt 5.1 i386 MSNMSGR 7.0.0813 MSMSGS";
   private static final int CLIENT_ID = 805306401;
   private static final String PRODUCT_ID = "PROD0101{0RM?UBW";
   private static final String PRODUCT_KEY = "CFHUR$52U_{VIX5T";
   private static Logger log = Logger.getLogger(MSNConnection.class);
   private static ExecutorService pool = Executors.newFixedThreadPool(20);
   private ChatConnectionInterface source;
   private ChatConnectionListenerInterface listener;
   private Map<String, SwitchBoard> switchBoards = new ConcurrentHashMap();
   @Expose
   private String currentUser;
   @Expose
   private boolean signedIn;
   private String displayPictureLocation;
   private MSNObject displayPicture;
   @Expose
   private Presence presence;
   private Map<String, String> contactGuids;
   private Semaphore signInLock;
   private Object disconnectLock;
   private String username;

   public MSNConnection(ChatConnectionListenerInterface listener, String displayPictureLocation, int timeout) {
      this.presence = Presence.FLN;
      this.contactGuids = new ConcurrentHashMap();
      this.signInLock = new Semaphore(1);
      this.disconnectLock = new Object();
      this.listener = listener;
      this.displayPictureLocation = displayPictureLocation;
      this.connectionTimeout = timeout;
      this.source = this;
   }

   public MSNConnection(ChatConnectionListenerInterface listener, String username, String password, int contactListVersion, String displayPictureLocation) {
      this.presence = Presence.FLN;
      this.contactGuids = new ConcurrentHashMap();
      this.signInLock = new Semaphore(1);
      this.disconnectLock = new Object();
      this.listener = listener;
      this.displayPictureLocation = displayPictureLocation;
      this.source = this;
      pool.execute(new MSNConnection.LoginTask(username, password, contactListVersion));
   }

   public void signIn(String username, String password, int contactListVersion) throws MSNException {
      if (!SystemProperty.getBool("MSNEnabled", true)) {
         throw new MSNException("MSN is disabled for a while...");
      } else {
         this.username = username;
         pool.execute(new MSNConnection.LoginTask(username, password, contactListVersion));
      }
   }

   public String getUsername() {
      return this.username;
   }

   public boolean isSignedIn() {
      return this.signedIn && this.isConnected();
   }

   public void signOut() {
      if (this.isSignedIn()) {
         this.sendAsyncCommand(new Command(Command.Type.OUT));
      }

      this.disconnect("");
   }

   public void setPresence(Presence presence, String personalMessage) throws MSNException {
      try {
         Command chg = (new Command(Command.Type.CHG)).addParam(presence.toString()).addParam(String.valueOf(805306401));
         if (this.displayPicture != null) {
            chg.addParam(URLEncoder.encode(this.displayPicture.toString(), "UTF-8").replace("+", "%20"));
         }

         this.sendAsyncCommand(chg);
         this.presence = presence;
         if (personalMessage != null) {
            this.sendAsyncCommand((new Command(Command.Type.UUX)).setPayload(("<Data><PSM>" + personalMessage + "</PSM><CurrentMedia></CurrentMedia></Data>").getBytes("UTF-8")));
         }

      } catch (Exception var4) {
         throw new MSNException(var4.getMessage());
      }
   }

   public void setDisplayPicture(String fileLocation) throws MSNException {
      try {
         this.displayPictureLocation = fileLocation;
         this.displayPicture = new MSNObject(this.currentUser, "AAA=", MSNObject.Type.AVATAR, fileLocation);
         if (this.isSignedIn()) {
            this.setPresence(Presence.NLN, (String)null);
         }

      } catch (Exception var3) {
         throw new MSNException(var3.getMessage());
      }
   }

   public void addContact(String username, String displayname, int groupId) throws MSNException {
      this.sendAsyncCommand((new Command(Command.Type.ADC)).addParam("FL").addParam("N=" + username).addParam("F=" + displayname));
      this.sendAsyncCommand((new Command(Command.Type.ADC)).addParam("AL").addParam("N=" + username));
   }

   public void removeContact(String username) throws MSNException {
      String guid = (String)this.contactGuids.get(username);
      if (guid == null) {
         throw new MSNException("Unable to find GUID for " + username);
      } else {
         this.sendAsyncCommand((new Command(Command.Type.REM)).addParam("FL").addParam(guid));
      }
   }

   public void sendMessage(String username, String message) throws MSNException {
      if (this.presence == Presence.HDN) {
         SwitchBoard switchBoard = (SwitchBoard)this.switchBoards.get(username);
         if (switchBoard == null || !switchBoard.isConnected()) {
            throw new MSNException("You cannot initiate a MSN chat while appear offline");
         }
      }

      pool.execute(new MSNConnection.MessageTask(username, message));
   }

   public String inviteToConference(String conferenceID, String username) throws MSNException {
      SwitchBoard switchBoard = (SwitchBoard)this.switchBoards.get(conferenceID);
      if (switchBoard == null || !switchBoard.isConnected()) {
         switchBoard = new SwitchBoard(this.switchBoards, this.source, this.listener, this.displayPicture, this.connectionTimeout);
      }

      switchBoard.convertToConference(this.currentUser);
      if (this.isValidUsernameFormat(conferenceID)) {
         pool.execute(new MSNConnection.ConferenceInvitationTask(switchBoard, new String[]{conferenceID, username}));
      } else {
         pool.execute(new MSNConnection.ConferenceInvitationTask(switchBoard, new String[]{username}));
      }

      return switchBoard.getConferenceID();
   }

   public void leaveConference(String conferenceID) {
      SwitchBoard switchBoard = (SwitchBoard)this.switchBoards.get(conferenceID);
      if (switchBoard != null && switchBoard.isConnected()) {
         switchBoard.disconnect("");
      }

   }

   public List<String> getConferenceParticipants(String conferenceID) {
      SwitchBoard switchBoard = (SwitchBoard)this.switchBoards.get(conferenceID);
      return switchBoard != null && switchBoard.isConnected() ? switchBoard.getUsers() : Collections.EMPTY_LIST;
   }

   private boolean isValidUsernameFormat(String username) {
      return username.matches(".*@.*\\..*");
   }

   private void signIn(String server, int port, String username, String password, int contactListVersion) throws MSNException {
      if (!this.isValidUsernameFormat(username)) {
         throw new MSNException("Incorrect MSN username");
      } else {
         this.connect(server, port);
         this.sendCommand((new Command(Command.Type.VER)).addParam("MSNP11").addParam("CVR0"));
         this.sendCommand((new Command(Command.Type.CVR)).addParam("0x0409 winnt 5.1 i386 MSNMSGR 7.0.0813 MSMSGS").addParam(username));
         Command reply = this.sendCommand((new Command(Command.Type.USR)).addParam("TWN").addParam("I").addParam(username));
         Command.Type type = reply.getType();
         if (type == Command.Type.XFR) {
            String destination = reply.getParam(0);
            if ("NS".equals(destination)) {
               this.disconnect("");
               String redirection = reply.getParam(1);
               if (redirection != null) {
                  String[] address = redirection.split(":");
                  this.signIn(address[0], Integer.parseInt(address[1]), username, password, contactListVersion);
               }
            }
         } else if (type == Command.Type.USR) {
            this.sendCommand((new Command(Command.Type.USR)).addParam("TWN").addParam("S").addParam(SOAPService.getTweenerKey(username, password, reply.getParam(2))));
            this.sendCommand((new Command(Command.Type.SYN)).addParam("0").addParam("0"));
            this.setPresence(Presence.NLN, (String)null);
            this.signedIn = true;
            this.listener.onSignInSuccess(this.source);
         }

      }
   }

   protected void onDisconnect(String reason) {
      synchronized(this.disconnectLock) {
         if (this.signedIn) {
            this.signedIn = false;
            this.currentUser = null;
            Iterator i$ = this.switchBoards.values().iterator();

            while(i$.hasNext()) {
               SwitchBoard switchBoard = (SwitchBoard)i$.next();
               switchBoard.disconnect("");
            }

            this.switchBoards.clear();
            this.listener.onDisconnected(this.source, reason);
         }

      }
   }

   protected void onIncomingCommand(Command incomingCommand, Command originalCommand) {
      switch(incomingCommand.getType()) {
      case CHL:
         this.onCHL(incomingCommand, originalCommand);
         break;
      case RNG:
         this.onRNG(incomingCommand, originalCommand);
         break;
      case ILN:
      case NLN:
         this.listener.onContactStatusChanged(this.source, incomingCommand.getParam(1), Presence.valueOf(incomingCommand.getParam(0)).toFusionPresence());
         break;
      case FLN:
         this.listener.onContactStatusChanged(this.source, incomingCommand.getParam(0), Presence.FLN.toFusionPresence());
         break;
      case LST:
         this.onLST(incomingCommand, originalCommand);
         break;
      case ADC:
         this.onADC(incomingCommand, originalCommand);
         break;
      case OUT:
         if ("OTH".equals(incomingCommand.getParam(0))) {
            this.disconnect("You have signed in to MSN on another device");
         }
      }

   }

   private void onCHL(Command chl, Command originalCommand) {
      try {
         String response = MSNChallenge.getResponse("PROD0101{0RM?UBW", "CFHUR$52U_{VIX5T", chl.getParam(1));
         String secondOpinion = MSNChallenge.getResponseYourPaste(chl.getParam(1));
         if (!response.equals(secondOpinion)) {
            log.warn("Mismatching challenge response generated for " + this.currentUser + " - " + chl.getParam(1));
         }

         this.sendAsyncCommand((new Command(Command.Type.QRY)).addParam("PROD0101{0RM?UBW").setPayload(response.getBytes("UTF-8")));
      } catch (Exception var5) {
      }

   }

   private void onLST(Command lst, Command originalCommand) {
      try {
         String username = null;
         String displayName = null;
         Iterator i$ = lst.getParamList().iterator();

         while(i$.hasNext()) {
            String param = (String)i$.next();
            if (param.startsWith("N=")) {
               username = param.substring(2);
            } else if (param.startsWith("F=")) {
               displayName = URLDecoder.decode(param.substring(2), "UTF-8");
            } else {
               if (!param.startsWith("C=")) {
                  int list = Integer.parseInt(param);
                  boolean onFL = (list & 1) == 1;
                  boolean onAL = (list & 2) == 2;
                  boolean onRL = (list & 8) == 8;
                  boolean onPL = (list & 16) == 16;
                  if (onFL && onAL && !onRL && onPL) {
                     this.sendAsyncCommand((new Command(Command.Type.ADC)).addParam("RL").addParam("N=" + username));
                     this.sendAsyncCommand((new Command(Command.Type.REM)).addParam("PL").addParam(username));
                     this.listener.onContactDetail(this.source, username, displayName);
                  } else if (onFL) {
                     this.listener.onContactDetail(this.source, username, displayName);
                  } else if (onPL) {
                     this.listener.onContactRequest(this.source, username, displayName);
                  }
                  break;
               }

               this.contactGuids.put(username, param.substring(2));
            }
         }
      } catch (Exception var12) {
      }

   }

   private void onADC(Command adc, Command originalCommand) {
      try {
         if ("RL".equals(adc.getParam(0))) {
            if (adc.getTransactionId() == 0) {
               this.listener.onContactDetail(this.source, adc.getParam(1).substring(2), URLDecoder.decode(adc.getParam(2).substring(2), "UTF-8"));
            }
         } else if ("FL".equals(adc.getParam(0)) && adc.getTransactionId() > 0) {
            String username = adc.getParam(1).substring(2);
            this.listener.onContactDetail(this.source, username, adc.getParam(2).substring(2));
            this.contactGuids.put(username, adc.getParam(3).substring(2));
         }
      } catch (Exception var4) {
      }

   }

   private void onRNG(Command rng, Command originalCommand) {
      try {
         if (this.isSignedIn()) {
            String address = rng.getParam(1);
            String[] addressTokens = address.split(":");
            SwitchBoard switchBoard = new SwitchBoard(this.switchBoards, this.source, this.listener, addressTokens[0], Integer.parseInt(addressTokens[1]), this.displayPicture, this.connectionTimeout);
            switchBoard.updateSwitchBoards(rng.getParam(4));
            switchBoard.join(this.currentUser, rng.getParam(0), rng.getParam(3));
         }
      } catch (Exception var6) {
      }

   }

   public ImType getImType() {
      return ImType.MSN;
   }

   public void signIn(String username, String password) throws Exception {
      this.signIn(username, password, 0);
   }

   public void addContact(String username) throws Exception {
      this.addContact(username, username, 0);
   }

   public void setAvatar(String fileLocation) throws Exception {
      this.setDisplayPicture(fileLocation);
   }

   public void setStatus(PresenceType status, String message) throws Exception {
      this.setPresence(Presence.fromFusionPresence(status), message);
   }

   private class ConferenceInvitationTask implements Runnable {
      private String conferenceID;
      private SwitchBoard switchBoard;
      private String[] usersToInvite;

      public ConferenceInvitationTask(SwitchBoard switchBoard, String[] usersToInvite) {
         this.conferenceID = switchBoard.getConferenceID();
         this.switchBoard = switchBoard;
         this.usersToInvite = usersToInvite;
      }

      public void run() {
         int i$;
         if (!this.switchBoard.isConnected()) {
            try {
               Command xfr = MSNConnection.this.sendCommand((new Command(Command.Type.XFR)).addParam("SB"));
               String address = xfr.getParam(1);
               if ("SB".equals(xfr.getParam(0)) && address != null) {
                  String[] addressTokens = address.split(":");
                  this.switchBoard.connect(addressTokens[0], Integer.parseInt(addressTokens[1]));
                  this.switchBoard.signIn(MSNConnection.this.currentUser, xfr.getParam(3));
                  synchronized(MSNConnection.this.disconnectLock) {
                     if (!MSNConnection.this.isSignedIn()) {
                        this.switchBoard.disconnect("");
                        throw new MSNException("Not connected to server");
                     }
                  }
               }
            } catch (MSNException var8) {
               MSNException e = var8;
               String[] arr$ = this.usersToInvite;
               i$ = arr$.length;

               for(int i$x = 0; i$x < i$; ++i$x) {
                  String usernamex = arr$[i$x];
                  MSNConnection.this.listener.onConferenceInvitationFailed(MSNConnection.this.source, this.conferenceID, usernamex, e.getMessage());
               }

               return;
            }
         }

         String[] arr$x = this.usersToInvite;
         int len$ = arr$x.length;

         for(i$ = 0; i$ < len$; ++i$) {
            String username = arr$x[i$];

            try {
               this.switchBoard.inviteToConference(username);
            } catch (MSNException var6) {
               MSNConnection.this.listener.onConferenceInvitationFailed(MSNConnection.this.source, this.conferenceID, username, var6.getMessage());
            }
         }

      }
   }

   private class MessageTask implements Runnable {
      private String username;
      private String message;

      public MessageTask(String username, String message) {
         this.username = username;
         this.message = message;
      }

      public void run() {
         SwitchBoard switchBoard = null;

         String address;
         try {
            switchBoard = (SwitchBoard)MSNConnection.this.switchBoards.get(this.username);
            if (switchBoard == null || !switchBoard.isConnected()) {
               Command xfr = MSNConnection.this.sendCommand((new Command(Command.Type.XFR)).addParam("SB"));
               address = xfr.getParam(1);
               if ("SB".equals(xfr.getParam(0)) && address != null) {
                  String[] addressTokens = address.split(":");
                  switchBoard = new SwitchBoard(MSNConnection.this.switchBoards, MSNConnection.this.source, MSNConnection.this.listener, addressTokens[0], Integer.parseInt(addressTokens[1]), MSNConnection.this.displayPicture, MSNConnection.this.connectionTimeout);
                  switchBoard.signIn(MSNConnection.this.currentUser, xfr.getParam(3));
                  synchronized(MSNConnection.this.disconnectLock) {
                     if (!MSNConnection.this.isSignedIn()) {
                        switchBoard.disconnect("");
                        throw new MSNException("Not connected to server");
                     }

                     switchBoard.updateSwitchBoards(this.username);
                  }
               }
            }

            switchBoard.sendMessage(this.username, this.message);
         } catch (MSNException var8) {
            address = switchBoard == null ? null : switchBoard.getConferenceID();
            MSNConnection.this.listener.onMessageFailed(MSNConnection.this.source, address, this.username, this.message, var8.getMessage());
         }

      }
   }

   private class LoginTask implements Runnable {
      private String username;
      private String password;
      private int contactListVersion;

      public LoginTask(String username, String password, int contactListVersion) {
         this.username = username;
         this.password = password;
         this.contactListVersion = contactListVersion;
      }

      public void run() {
         if (MSNConnection.this.signInLock.tryAcquire()) {
            try {
               if (!MSNConnection.this.isSignedIn()) {
                  MSNConnection.this.currentUser = this.username;
                  if (MSNConnection.this.displayPicture == null && MSNConnection.this.displayPictureLocation != null) {
                     MSNConnection.this.setDisplayPicture(MSNConnection.this.displayPictureLocation);
                  }

                  MSNConnection.this.signIn("messenger.hotmail.com", 1863, this.username, this.password, this.contactListVersion);
               }
            } catch (MSNException var7) {
               MSNConnection.this.disconnect("Sign in failed");
               MSNConnection.this.listener.onSignInFailed(MSNConnection.this.source, var7.getMessage());
            } catch (Exception var8) {
               MSNConnection.log.error("Unexpected exception caught during MSN login '" + this.username + "', '" + this.password + "'", var8);
               MSNConnection.this.disconnect("Sign in failed");
               MSNConnection.this.listener.onSignInFailed(MSNConnection.this.source, var8.getMessage());
            } finally {
               MSNConnection.this.signInLock.release();
            }

         }
      }
   }
}
