package com.projectgoth.fusion.maintenance;

import com.projectgoth.fusion.fdl.enums.ClientType;
import com.projectgoth.fusion.fdl.enums.PresenceType;
import com.projectgoth.fusion.fdl.enums.ServiceType;
import com.projectgoth.fusion.gateway.HTTPRequest;
import com.projectgoth.fusion.gateway.HTTPResponse;
import com.projectgoth.fusion.gateway.packet.FusionPktLogin;
import com.projectgoth.fusion.gateway.packet.FusionPktLoginResponse;
import com.projectgoth.fusion.gateway.packet.FusionPktLogout;
import com.projectgoth.fusion.gateway.packet.FusionPktMessage;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class LoginBot {
   private Socket socket;
   private String server;
   private int port;
   private boolean isTCP;
   private String username;
   private String password;
   private String sessionId;
   private short transactionId;

   public LoginBot(String server, int port, boolean isTCP, String username, String password) {
      this.username = username;
      this.password = password;
      this.server = server;
      this.port = port;
      this.isTCP = isTCP;
   }

   public String getUsername() {
      return this.username;
   }

   public String getSessionId() {
      return this.sessionId;
   }

   protected synchronized FusionPacket sendFusionPkt(String sessionId, FusionPacket pkt) throws IOException {
      return this.sendFusionPkt(sessionId, pkt, true);
   }

   protected synchronized FusionPacket sendFusionPkt(String sessionId, FusionPacket pkt, boolean getReply) throws IOException {
      try {
         if (this.socket == null || !this.isTCP) {
            this.socket = new Socket();
            this.socket.connect(new InetSocketAddress(this.server, this.port), 20000);
            this.socket.setSoTimeout(30000);
         }

         if (++this.transactionId == 32767) {
            this.transactionId = 1;
         }

         pkt.setTransactionId(this.transactionId);
         if (!this.isTCP) {
            HTTPRequest r = new HTTPRequest(HTTPRequest.RequestMethod.POST, sessionId, "image/gif", pkt.toByteArray());
            r.setProperty("Transfer-encoding", "binary");
            r.write(this.socket.getOutputStream());
            HTTPResponse x = new HTTPResponse();
            x.read(this.socket.getInputStream());
            byte[] ba = x.getContent();
            FusionPacket[] packets;
            if (ba == null) {
               packets = null;
               return packets;
            } else {
               packets = FusionPacket.parse(ba);
               if (packets != null) {
                  for(int i = 0; i < packets.length; ++i) {
                     if (packets[i].getTransactionId() == pkt.getTransactionId()) {
                        FusionPacket var9 = packets[i];
                        return var9;
                     }
                  }
               }

               Object var24 = null;
               return (FusionPacket)var24;
            }
         } else {
            pkt.write(this.socket.getOutputStream());
            FusionPacket reply;
            if (!getReply) {
               reply = null;
               return reply;
            } else {
               reply = new FusionPacket();

               do {
                  reply.read(this.socket.getInputStream());
               } while(reply.getTransactionId() != pkt.getTransactionId());

               FusionPacket var5 = reply;
               return var5;
            }
         }
      } finally {
         if (!this.isTCP) {
            try {
               this.socket.close();
            } catch (Exception var20) {
            }
         }

      }
   }

   public void login() throws IOException {
      FusionPktLogin login = new FusionPktLogin();
      login.setProtocolVersion((short)1);
      login.setClientVersion((short)300);
      login.setServiceType(ServiceType.X_TXT);
      login.setUsername(this.username);
      login.setEmailAddress("");
      login.setUserAgent("");
      login.setInitialPresence(PresenceType.AVAILABLE);
      login.setClientType(ClientType.MIDP1);
      FusionPacket reply = this.sendFusionPkt("", login);
      if (reply.getType() == 201) {
         String challenge = reply.getStringField((short)1);
         String session = reply.getStringField((short)2);
         FusionPktLoginResponse loginResp = new FusionPktLoginResponse();
         loginResp.setPasswordHash((challenge + this.password).hashCode());
         reply = this.sendFusionPkt(session, loginResp);
         if (reply.getType() != 203) {
            throw new IOException(reply.getStringField((short)2));
         } else {
            this.sessionId = session;
         }
      } else {
         throw new IOException(reply.getStringField((short)2));
      }
   }

   public void logout() {
      try {
         this.sendFusionPkt(this.sessionId, new FusionPktLogout());
      } catch (Exception var41) {
      } finally {
         this.sessionId = null;

         try {
            this.socket.close();
         } catch (Exception var39) {
         } finally {
            this.socket = null;
         }

      }

   }

   public boolean isOnline() {
      if (this.isTCP && (this.socket == null || !this.socket.isConnected())) {
         return false;
      } else {
         return this.sessionId != null && this.sessionId.length() > 0;
      }
   }

   public void sendMessage(String destination, String message) throws IOException {
      FusionPktMessage msg = new FusionPktMessage();
      msg.setMessageType((byte)1);
      msg.setSource(this.username);
      msg.setDestinationType((byte)1);
      msg.setDestination(destination);
      msg.setContentType((short)1);
      msg.setContentAsString(message);
      FusionPacket reply = this.sendFusionPkt(this.sessionId, msg);
      if (reply.getType() != 1) {
         throw new IOException(reply.getStringField((short)2));
      }
   }

   public static void main(String[] args) {
      if (args.length < 7) {
         System.out.println("Usage: LoginBot host gateway port tcp username password interval");
      } else {
         String host = args[0];
         boolean isTCP = Integer.parseInt(args[3]) == 1;
         long interval = Long.parseLong(args[6]);
         int port = Integer.parseInt(args[2]);
         LoginBot bot = new LoginBot(args[1], port, isTCP, args[4], args[5]);

         while(true) {
            try {
               bot.login();
               bot.logout();
               System.out.println(host + "\tGateway " + port + "\t0\tOK");
            } catch (Exception var8) {
               System.out.println(host + "\tGateway " + port + "\t2\tCRITICAL - " + var8.getMessage());
            }

            if (interval <= 0L) {
               return;
            }

            try {
               Thread.sleep(interval);
            } catch (Exception var9) {
            }
         }
      }
   }
}
