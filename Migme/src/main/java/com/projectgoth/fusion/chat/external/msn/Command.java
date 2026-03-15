package com.projectgoth.fusion.chat.external.msn;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

public class Command {
   private Command.Type type;
   private Integer transactionId;
   private int errorCode;
   private List<String> paramList = new ArrayList();
   private byte[] payload;
   private int payloadSize;
   private Command reply;

   public Command(Command.Type type) {
      this.type = type;
   }

   public Command(String rawCommand) throws IOException {
      StringTokenizer tokens = new StringTokenizer(rawCommand);
      if (!tokens.hasMoreTokens()) {
         throw new IOException("Empty command");
      } else {
         String token = tokens.nextToken();

         try {
            this.type = Command.Type.valueOf(token);
         } catch (Exception var8) {
            try {
               this.type = Command.Type.ERROR;
               this.errorCode = Integer.parseInt(token);
            } catch (NumberFormatException var7) {
               throw new IOException("Unknown command type " + token);
            }
         }

         if (this.containTransactionId()) {
            try {
               this.transactionId = Integer.parseInt(tokens.nextToken());
            } catch (Exception var6) {
               throw new IOException("Command contains no transaction ID");
            }
         }

         while(tokens.hasMoreTokens()) {
            this.addParam(tokens.nextToken());
         }

         if (this.isPayloadCommand() && this.paramList.size() > 0) {
            this.payloadSize = Integer.parseInt((String)this.paramList.remove(this.paramList.size() - 1));
         }

      }
   }

   public Command.Type getType() {
      return this.type;
   }

   public int getErrorCode() {
      return this.errorCode;
   }

   public Integer getTransactionId() {
      return this.transactionId;
   }

   public void setTransactionId(Integer transactionId) {
      this.transactionId = transactionId;
   }

   public byte[] getPayload() {
      return this.payload;
   }

   public Command setPayload(byte[] payload) {
      this.payload = payload;
      this.payloadSize = payload == null ? 0 : payload.length;
      return this;
   }

   public int getPayloadSize() {
      return this.payloadSize;
   }

   public void setReply(Command reply) {
      this.reply = reply;
   }

   public Command getReply() {
      return this.reply;
   }

   public Command addParam(String param) {
      this.paramList.add(param);
      return this;
   }

   public String getParam(int index) {
      return (String)this.paramList.get(index);
   }

   public List<String> getParamList() {
      return this.paramList;
   }

   public boolean isPayloadCommand() {
      switch(this.type) {
      case QRY:
      case MSG:
      case NOT:
      case UUX:
      case UBX:
      case GCF:
         return true;
      default:
         return false;
      }
   }

   public boolean containTransactionId() {
      switch(this.type) {
      case QRY:
      case UUX:
      case GCF:
      case ERROR:
      case VER:
      case CVR:
      case USR:
      case XFR:
      case ILN:
      case CHG:
      case SYN:
      case ADD:
      case REM:
      case CAL:
      case ACK:
      case NAK:
      case ANS:
      case ADC:
      case SBP:
      case LKP:
         return true;
      case MSG:
      case NOT:
      case UBX:
      default:
         return false;
      }
   }

   public String getCommandString() {
      StringBuilder builder = new StringBuilder(this.type.toString());
      if (this.transactionId != null) {
         builder.append(" ").append(this.transactionId.toString());
      }

      Iterator i$ = this.paramList.iterator();

      while(i$.hasNext()) {
         String param = (String)i$.next();
         builder.append(" ").append(param);
      }

      if (this.isPayloadCommand()) {
         builder.append(" ").append(this.payloadSize);
      }

      return builder.append("\r\n").toString();
   }

   public byte[] getBytes(String charset) throws UnsupportedEncodingException {
      if (this.isPayloadCommand() && this.payloadSize > 0) {
         byte[] ba = this.getCommandString().getBytes(charset);
         byte[] retval = new byte[ba.length + this.payload.length];
         System.arraycopy(ba, 0, retval, 0, ba.length);
         System.arraycopy(this.payload, 0, retval, ba.length, this.payload.length);
         return retval;
      } else {
         return this.getCommandString().getBytes(charset);
      }
   }

   public String toString() {
      if (this.isPayloadCommand() && this.payloadSize > 0) {
         StringBuilder builder = new StringBuilder(this.getCommandString());
         builder.append(new String(this.payload));
         return builder.toString();
      } else {
         return this.getCommandString();
      }
   }

   public static enum Type {
      ERROR,
      ACK,
      ADD,
      ADG,
      ANS,
      BLP,
      BPR,
      BYE,
      CAL,
      CHG,
      CHL,
      FLN,
      GTC,
      ILN,
      TWN,
      IRO,
      JOI,
      LSG,
      LST,
      MSG,
      NAK,
      NLN,
      OUT,
      PRP,
      QRY,
      REA,
      REG,
      REM,
      RMG,
      RNG,
      SYN,
      USR,
      VER,
      XFR,
      CVR,
      SDC,
      NOT,
      UUX,
      UBX,
      SBS,
      ADC,
      SBP,
      GCF,
      LKP;
   }
}
