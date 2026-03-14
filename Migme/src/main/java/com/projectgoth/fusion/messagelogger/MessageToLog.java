package com.projectgoth.fusion.messagelogger;

import java.util.Date;

public class MessageToLog {
   public Date dateCreated;
   public MessageToLog.TypeEnum type;
   public Integer sourceCountryID;
   public String source;
   public String destination;
   public int numRecipients;
   public String messageText;

   public MessageToLog(Date dateCreated, int sourceCountryID, MessageToLog.TypeEnum type, String source, String destination, int numRecipients, String messageText) {
      this.dateCreated = dateCreated;
      this.type = type;
      this.sourceCountryID = sourceCountryID;
      this.source = source.toLowerCase();
      this.destination = destination.toLowerCase();
      this.numRecipients = numRecipients;
      this.messageText = this.replaceQuotesAndLineBreaks(messageText);
   }

   private String replaceQuotesAndLineBreaks(String str) {
      if (str.indexOf(10) >= 0) {
         str = str.replaceAll("(\r\n|\n)", " \\\\ ");
      }

      if (str.indexOf(34) >= 0) {
         str = str.replace("\"", "\"\"");
      }

      return str;
   }

   public static enum TypeEnum {
      PRIVATE(1),
      GROUPCHAT(2),
      CHATROOM(3),
      SMS(4),
      MSN_SENT(5),
      MSN_RECEIVED(6),
      YAHOO_SENT(7),
      YAHOO_RECEIVED(8),
      AIM_SENT(9),
      AIM_RECEIVED(10),
      GTALK_SENT(11),
      GTALK_RECEIVED(12),
      FACEBOOK_SENT(13),
      FACEBOOK_RECEIVED(14);

      private int value;

      private TypeEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static MessageToLog.TypeEnum fromValue(int value) {
         MessageToLog.TypeEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            MessageToLog.TypeEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }
}
