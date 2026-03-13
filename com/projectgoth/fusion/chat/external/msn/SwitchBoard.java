package com.projectgoth.fusion.chat.external.msn;

import com.projectgoth.fusion.chat.external.ChatConnectionInterface;
import com.projectgoth.fusion.chat.external.ChatConnectionListenerInterface;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SwitchBoard extends Connection {
   private ChatConnectionInterface source;
   private ChatConnectionListenerInterface listener;
   private Map<String, SwitchBoard> switchBoards;
   private Set<String> users = new HashSet();
   private List<P2PSession> p2pSessions = new ArrayList();
   private String conferenceID;
   private Object waitingForUser = new Object();
   protected MSNObject displayPicture;

   public SwitchBoard(Map<String, SwitchBoard> switchBoards, ChatConnectionInterface source, ChatConnectionListenerInterface listener, String server, int port, MSNObject displayPicture, int timeout) throws MSNException {
      this.listener = listener;
      this.source = source;
      this.switchBoards = switchBoards;
      this.displayPicture = displayPicture;
      this.connectionTimeout = timeout;
      this.connect(server, port);
   }

   public SwitchBoard(Map<String, SwitchBoard> switchBoards, ChatConnectionInterface source, ChatConnectionListenerInterface listener, MSNObject displayPicture, int timeout) throws MSNException {
      this.listener = listener;
      this.source = source;
      this.switchBoards = switchBoards;
      this.displayPicture = displayPicture;
      this.connectionTimeout = timeout;
   }

   public void signIn(String username, String ticket) throws MSNException {
      try {
         this.sendCommand((new Command(Command.Type.USR)).addParam(username).addParam(ticket));
      } catch (MSNException var4) {
         this.disconnect("Failed to signin to switchboard - " + var4.getMessage());
         throw var4;
      }
   }

   public void join(String username, String sessionId, String ticket) throws MSNException {
      try {
         this.sendCommand((new Command(Command.Type.ANS)).addParam(username).addParam(ticket).addParam(sessionId));
      } catch (MSNException var5) {
         this.disconnect("Failed to join switchboard - " + var5.getMessage());
         throw var5;
      }
   }

   public void sendMessage(String username, String message) throws MSNException {
      if (!username.equals(this.conferenceID)) {
         this.waitForUser(username);
      }

      StringBuilder builder = new StringBuilder("MIME-Version: 1.0\r\nContent-Type: text/plain; charset=");
      builder.append("UTF-8").append("\r\n\r\n").append(message);

      try {
         byte[] payload = builder.toString().getBytes("UTF-8");
         this.sendCommand((new Command(Command.Type.MSG)).addParam("A").setPayload(payload));
      } catch (UnsupportedEncodingException var5) {
         throw new MSNException(var5.getMessage());
      }
   }

   public void updateSwitchBoards(String username) {
      SwitchBoard oldSwitchBoard;
      synchronized(this.switchBoards) {
         oldSwitchBoard = (SwitchBoard)this.switchBoards.put(username, this);
      }

      if (oldSwitchBoard != null && oldSwitchBoard != this) {
         oldSwitchBoard.disconnect("");
      }

   }

   public void convertToConference(String creator) {
      synchronized(this.users) {
         if (this.conferenceID == null) {
            Iterator i$ = this.users.iterator();

            while(i$.hasNext()) {
               String username = (String)i$.next();
               synchronized(this.switchBoards) {
                  if (this == this.switchBoards.get(username)) {
                     this.switchBoards.remove(username);
                  }
               }
            }

            synchronized(this.switchBoards) {
               long maxKey = 0L;
               Iterator i$ = this.switchBoards.keySet().iterator();

               while(i$.hasNext()) {
                  String key = (String)i$.next();

                  try {
                     long thisKey = Long.parseLong(key);
                     if (thisKey > maxKey) {
                        maxKey = thisKey;
                     }
                  } catch (NumberFormatException var12) {
                  }
               }

               this.conferenceID = String.valueOf(maxKey + 1L);
               this.updateSwitchBoards(this.conferenceID);
            }

            if (this.listener != null) {
               this.listener.onConferenceCreated(this.source, this.conferenceID, creator);
            }
         }

      }
   }

   public String inviteToConference(String username) throws MSNException {
      synchronized(this.users) {
         if (!this.users.contains(username)) {
            this.sendCommand((new Command(Command.Type.CAL)).addParam(username));
         }

         return this.conferenceID;
      }
   }

   public String getConferenceID() {
      return this.conferenceID;
   }

   public List<String> getUsers() {
      synchronized(this.users) {
         return new LinkedList(this.users);
      }
   }

   private void waitForUser(String username) throws MSNException {
      synchronized(this.waitingForUser) {
         synchronized(this.users) {
            if (!this.users.contains(username)) {
               try {
                  this.sendCommand((new Command(Command.Type.CAL)).addParam(username));

                  try {
                     this.users.wait(20000L);
                  } catch (Exception var7) {
                  }

                  if (!this.users.contains(username)) {
                     throw new MSNException("Timeout while waiting for user " + username);
                  }
               } catch (MSNException var8) {
                  if (var8.getMSNErrorCode() != 215) {
                     throw var8;
                  }
               }
            }
         }

      }
   }

   private void addUser(String username) {
      synchronized(this.users) {
         if (this.users.size() > 0) {
            this.convertToConference((String)this.users.iterator().next());
         }

         if (this.isConnected() && this.conferenceID == null) {
            this.updateSwitchBoards(username);
         }

         this.users.add(username);
         if (this.conferenceID != null && this.listener != null) {
            this.listener.onUserJoinedConference(this.source, this.conferenceID, username);
         }

         this.users.notifyAll();
      }
   }

   private void removeUser(String username) {
      synchronized(this.users) {
         synchronized(this.switchBoards) {
            if (this == this.switchBoards.get(username)) {
               this.switchBoards.remove(username);
            }
         }

         this.users.remove(username);
         if (this.conferenceID != null && this.listener != null) {
            this.listener.onUserLeftConference(this.source, this.conferenceID, username);
         }

         if (this.users.size() == 0) {
            this.disconnect("");
         }

      }
   }

   protected void onDisconnect(String reason) {
      this.p2pSessions.clear();
      this.users.clear();
      this.listener = null;
      this.switchBoards = null;
      this.displayPicture = null;
      this.conferenceID = null;
   }

   protected void onIncomingCommand(Command incomingCommand, Command originalCommand) {
      switch(incomingCommand.getType()) {
      case IRO:
         this.addUser(incomingCommand.getParam(3));
         break;
      case JOI:
         this.addUser(incomingCommand.getParam(0));
         break;
      case BYE:
         this.removeUser(incomingCommand.getParam(0));
         break;
      case MSG:
         this.onMSG(incomingCommand);
      }

   }

   private void onMSG(Command msg) {
      try {
         byte[] payload = msg.getPayload();
         if (payload != null) {
            String content = new String(payload, "UTF-8");
            Pattern plainTextPattern = Pattern.compile("Content-Type: text/plain(.*)\\r\\n\\r\\n(.+)", 32);
            Matcher matcher = plainTextPattern.matcher(content);
            if (matcher.find()) {
               if (this.listener != null) {
                  this.listener.onMessageReceived(this.source, this.conferenceID, msg.getParam(0), matcher.group(2));
               }
            } else if (content.contains("Content-Type: application/x-msnmsgrp2p")) {
               this.onP2PMessage(msg.getParam(0), new P2PMessage(new String(payload, "ISO8859_1")));
            }
         }
      } catch (Exception var6) {
         var6.printStackTrace();
      }

   }

   private void onP2PMessage(String source, P2PMessage message) {
      MSNSLPMessage msnslp = message.getMSNSLP();
      P2PSession session;
      if (msnslp != null && msnslp.getType() == MSNSLPMessage.Type.INVITE) {
         MSNSLPMessage.Content msnslpContent = msnslp.getContent();
         if (msnslpContent != null && "{A4268EEC-FEC5-49E5-95C3-F126696BDBF6}".equals(msnslpContent.getString("EUF-GUID")) && this.displayPicture != null) {
            session = new P2PSession(this, source, message);
            this.p2pSessions.add(session);
         }

      } else {
         Iterator i$ = this.p2pSessions.iterator();

         while(i$.hasNext()) {
            session = (P2PSession)i$.next();
            if (session.belongsToMe(message)) {
               session.onP2PMessage(source, message);
               break;
            }
         }

      }
   }
}
