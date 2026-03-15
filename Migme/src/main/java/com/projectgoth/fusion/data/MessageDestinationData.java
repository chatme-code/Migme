package com.projectgoth.fusion.data;

import com.projectgoth.fusion.slice.MessageDestinationDataIce;
import java.io.Serializable;
import java.util.Date;

public class MessageDestinationData implements Serializable {
   public Integer id;
   public Integer messageID;
   public Integer contactID;
   public MessageDestinationData.TypeEnum type;
   public String destination;
   public Integer IDDCode;
   public Double cost;
   public Integer gateway;
   public Date dateDispatched;
   public String providerTransactionID;
   public MessageDestinationData.StatusEnum status;

   public MessageDestinationData() {
   }

   public MessageDestinationData(MessageDestinationDataIce messageDestIce) {
      this.id = messageDestIce.id == Integer.MIN_VALUE ? null : messageDestIce.id;
      this.messageID = messageDestIce.messageID == Integer.MIN_VALUE ? null : messageDestIce.messageID;
      this.contactID = messageDestIce.contactID == Integer.MIN_VALUE ? null : messageDestIce.contactID;
      this.type = messageDestIce.type == Integer.MIN_VALUE ? null : MessageDestinationData.TypeEnum.fromValue(messageDestIce.type);
      this.destination = messageDestIce.destination.equals("\u0000") ? null : messageDestIce.destination;
      this.IDDCode = messageDestIce.IDDCode == Integer.MIN_VALUE ? null : messageDestIce.IDDCode;
      this.cost = messageDestIce.cost == Double.MIN_VALUE ? null : messageDestIce.cost;
      this.gateway = messageDestIce.gateway == Integer.MIN_VALUE ? null : messageDestIce.gateway;
      this.dateDispatched = messageDestIce.dateDispatched == Long.MIN_VALUE ? null : new Date(messageDestIce.dateDispatched);
      this.providerTransactionID = messageDestIce.providerTransactionID.equals("\u0000") ? null : messageDestIce.providerTransactionID;
      this.status = messageDestIce.status == Integer.MIN_VALUE ? null : MessageDestinationData.StatusEnum.fromValue(messageDestIce.status);
   }

   public MessageDestinationDataIce toIceObject() {
      MessageDestinationDataIce messageDestIce = new MessageDestinationDataIce();
      messageDestIce.id = this.id == null ? Integer.MIN_VALUE : this.id;
      messageDestIce.messageID = this.messageID == null ? Integer.MIN_VALUE : this.messageID;
      messageDestIce.contactID = this.contactID == null ? Integer.MIN_VALUE : this.contactID;
      messageDestIce.type = this.type == null ? Integer.MIN_VALUE : this.type.value();
      messageDestIce.destination = this.destination == null ? "\u0000" : this.destination;
      messageDestIce.IDDCode = this.IDDCode == null ? Integer.MIN_VALUE : this.IDDCode;
      messageDestIce.cost = this.cost == null ? Double.MIN_VALUE : this.cost;
      messageDestIce.gateway = this.gateway == null ? Integer.MIN_VALUE : this.gateway;
      messageDestIce.dateDispatched = this.dateDispatched == null ? Long.MIN_VALUE : this.dateDispatched.getTime();
      messageDestIce.providerTransactionID = this.providerTransactionID == null ? "\u0000" : this.providerTransactionID;
      messageDestIce.status = this.status == null ? Integer.MIN_VALUE : this.status.value();
      return messageDestIce;
   }

   public static String toString(MessageDestinationDataIce i) {
      return (new MessageDestinationData(i)).toString();
   }

   public String toString() {
      return String.format("MsgDest:id=%d, messageId=%d, contactID=%d, type=%s, destination=%s, IDDCode=%d, cost=%f, gateway=%d, dateDispatched=%s, providerTransactionID=%s, status=%s", this.id, this.messageID, this.contactID, this.type == null ? "null" : this.type.toString(), this.destination, this.IDDCode, this.cost, this.gateway, this.dateDispatched, this.providerTransactionID, this.status == null ? "null" : this.status.toString());
   }

   public static enum TypeEnum {
      INDIVIDUAL(1),
      GROUP(2),
      CHAT_ROOM(3),
      DISTRIBUTION_LIST(4);

      private int value;

      private TypeEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static MessageDestinationData.TypeEnum fromValue(int value) {
         MessageDestinationData.TypeEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            MessageDestinationData.TypeEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }

   public static enum StatusEnum {
      PENDING(0),
      SENT(1),
      FAILED(2);

      private int value;

      private StatusEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static MessageDestinationData.StatusEnum fromValue(int value) {
         MessageDestinationData.StatusEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            MessageDestinationData.StatusEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }
}
