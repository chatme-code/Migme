package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktNotification extends FusionPacket {
   public FusionPktNotification() {
      super((short)14);
   }

   public FusionPktNotification(short transactionId) {
      super((short)14, transactionId);
   }

   public FusionPktNotification(FusionPacket packet) {
      super(packet);
   }

   public FusionPktNotification(String text, String url) {
      super((short)14);
      this.setText(text);
      this.setURL(url);
   }

   public String getText() {
      return this.getStringField((short)1);
   }

   public void setText(String text) {
      this.setField((short)1, text);
   }

   public String getURL() {
      return this.getStringField((short)2);
   }

   public void setURL(String url) {
      this.setField((short)2, url);
   }

   public Integer getNotificationType() {
      return this.getIntField((short)3);
   }

   public void setNotificationType(Integer notificationType) {
      this.setField((short)3, notificationType);
   }

   public Integer getPendingNotificationsTotal() {
      return this.getIntField((short)4);
   }

   public void setPendingNotificationsTotal(Integer notifications) {
      this.setField((short)4, notifications);
   }
}
