package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.URLUtil;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.interfaces.Message;
import com.projectgoth.fusion.interfaces.MessageHome;
import com.projectgoth.fusion.packet.FusionPacket;
import java.text.DecimalFormat;

public class FusionPktChatRoomNotificationOld extends FusionPacket {
   public FusionPktChatRoomNotificationOld() {
      super((short)718);
   }

   public FusionPktChatRoomNotificationOld(short transactionId) {
      super((short)718, transactionId);
   }

   public FusionPktChatRoomNotificationOld(FusionPacket packet) {
      super(packet);
   }

   public FusionPktChatRoomNotificationOld(String text, String url) {
      super((short)718);
      this.setText(text);
      this.setURL(url);
   }

   public FusionPktChatRoomNotificationOld(ConnectionI connection) {
      super((short)718);

      int numOfUsers;
      try {
         numOfUsers = connection.getGatewayContext().getIcePrxFinder().getRegistry(false).getUserCount();
      } catch (Exception var10) {
         numOfUsers = 0;
      }

      Message messageEJB = null;

      int numOfChatRooms;
      try {
         messageEJB = (Message)EJBHomeCache.getObject("ejb/Message", MessageHome.class);
         numOfChatRooms = messageEJB.getRecentlyAccessedChatRoomCount();
      } catch (Exception var9) {
         numOfChatRooms = 0;
      }

      int numOfGroups = 0;

      try {
         numOfGroups = messageEJB != null ? messageEJB.getActiveGroupsCount() : 0;
      } catch (Exception var8) {
      }

      String url = URLUtil.replaceViewTypeToken(SystemProperty.get("ChatRoomIntroURL", ""), connection.getDeviceType());
      StringBuilder builder = new StringBuilder();
      builder.append(this.toNiceNumber(numOfUsers)).append(" users. ").append(this.toNiceNumber(numOfChatRooms)).append(" rooms. ").append(this.toNiceNumber(numOfGroups)).append(" groups.");
      this.setText(builder.toString());
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

   private String toNiceNumber(int number) {
      return number < 1000 ? Integer.toString(number) : (new DecimalFormat("0.0k")).format((double)number / 1000.0D);
   }
}
