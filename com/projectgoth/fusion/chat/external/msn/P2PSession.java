package com.projectgoth.fusion.chat.external.msn;

import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class P2PSession {
   private SwitchBoard switchBoard;
   private String counterParty;
   private DWord sessionId;
   private String callId;
   private DWord baseIdentifier;
   private DWord lastIdentifier;
   private Map<String, P2PMessage> messagesSent = new ConcurrentHashMap();

   private void sendP2PMessage(DWord identifier, P2PMessage message) {
      try {
         message.setDestination(this.counterParty);
         message.setIdentifier(identifier);
         byte[] payload = message.toString().getBytes("ISO8859_1");
         this.messagesSent.put(identifier.toString(), message);
         this.switchBoard.sendAsyncCommand((new Command(Command.Type.MSG)).addParam("D").setPayload(payload));
      } catch (UnsupportedEncodingException var4) {
         var4.printStackTrace();
      }

   }

   private DWord nextIdentifier() {
      long next = this.lastIdentifier.longValue() + 1L;
      if (next == this.baseIdentifier.longValue()) {
         ++next;
      }

      this.lastIdentifier = new DWord(next);
      return this.lastIdentifier;
   }

   public P2PSession(SwitchBoard switchBoard, String source, P2PMessage p2pInvitation) {
      MSNSLPMessage msnslp = p2pInvitation.getMSNSLP();
      MSNSLPMessage.Content msnslpContent = msnslp.getContent();
      this.switchBoard = switchBoard;
      this.counterParty = source;
      this.sessionId = new DWord(Long.parseLong(msnslpContent.getString("SessionID")));
      this.callId = msnslp.getHeader("Call-ID");
      this.baseIdentifier = new DWord((long)Math.abs((new SecureRandom()).nextInt()));
      this.lastIdentifier = new DWord(this.baseIdentifier.longValue() - 4L);
      this.sendP2PMessage(this.baseIdentifier, P2PMessage.createAcknowledgement(2, p2pInvitation));
      this.sendP2PMessage(this.nextIdentifier(), P2PMessage.createOkStatus(this.sessionId, p2pInvitation));
   }

   public boolean belongsToMe(P2PMessage message) {
      DWord ackIdentifier = message.getAckIdentifier();
      if (ackIdentifier.longValue() != 0L && this.messagesSent.containsKey(ackIdentifier.toString())) {
         return true;
      } else {
         MSNSLPMessage msnslp = message.getMSNSLP();
         return msnslp != null && this.callId.equals(msnslp.getHeader("Call-ID"));
      }
   }

   public void onP2PMessage(String source, P2PMessage message) {
      Object content = message.getContent();
      if (content == null) {
         P2PMessage acknowledgedP2P = (P2PMessage)this.messagesSent.get(message.getAckIdentifier().toString());
         if (acknowledgedP2P != null) {
            Object acknowledgedContent = acknowledgedP2P.getContent();
            if (acknowledgedContent != null) {
               if (acknowledgedContent instanceof MSNSLPMessage) {
                  MSNSLPMessage acknowledgedSLP = (MSNSLPMessage)acknowledgedContent;
                  if (acknowledgedSLP.getType() == MSNSLPMessage.Type.STATUS) {
                     this.sendP2PMessage(this.nextIdentifier(), P2PMessage.createDataPreparation(this.sessionId));
                  }
               } else if (acknowledgedContent instanceof byte[]) {
                  byte[] acknowledgedBinaryData = (byte[])((byte[])acknowledgedContent);
                  if (Arrays.equals(acknowledgedBinaryData, new byte[4])) {
                     DWord identifier = this.nextIdentifier();
                     Iterator i$ = P2PMessage.createDataMessages(this.sessionId, this.switchBoard.displayPicture.getContent()).iterator();

                     while(i$.hasNext()) {
                        P2PMessage fragment = (P2PMessage)i$.next();
                        this.sendP2PMessage(identifier, fragment);
                     }
                  }
               }
            }
         }
      } else if (content instanceof MSNSLPMessage) {
         MSNSLPMessage msnslp = (MSNSLPMessage)content;
         if (msnslp.getType() == MSNSLPMessage.Type.BYE) {
            this.sendP2PMessage(this.nextIdentifier(), P2PMessage.createAcknowledgement(64, message));
         }
      } else if (content instanceof byte[]) {
      }

   }
}
