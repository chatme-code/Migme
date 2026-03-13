package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.URLUtil;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.fdl.packets.FusionPktDataChatroomNotification;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.interfaces.Message;
import com.projectgoth.fusion.interfaces.MessageHome;
import com.projectgoth.fusion.packet.FusionPacket;
import java.text.DecimalFormat;

public class FusionPktChatroomNotification extends FusionPktDataChatroomNotification {
   public FusionPktChatroomNotification(FusionPacket packet) {
      super(packet);
   }

   public FusionPktChatroomNotification(short transactionId, ConnectionI connection) {
      super(transactionId);

      int numOfUsers;
      try {
         numOfUsers = connection.getGatewayContext().getIcePrxFinder().getRegistry(false).getUserCount();
      } catch (Exception var11) {
         numOfUsers = 0;
      }

      Message messageEJB = null;

      int numOfChatRooms;
      try {
         messageEJB = (Message)EJBHomeCache.getObject("ejb/Message", MessageHome.class);
         numOfChatRooms = messageEJB.getRecentlyAccessedChatRoomCount();
      } catch (Exception var10) {
         numOfChatRooms = 0;
      }

      int numOfGroups = 0;

      try {
         numOfGroups = messageEJB != null ? messageEJB.getActiveGroupsCount() : 0;
      } catch (Exception var9) {
      }

      String url = URLUtil.replaceViewTypeToken(SystemProperty.get("ChatRoomIntroURL", ""), connection.getDeviceType());
      StringBuilder builder = new StringBuilder();
      builder.append(toNiceNumber(numOfUsers)).append(" users. ").append(toNiceNumber(numOfChatRooms)).append(" rooms. ").append(toNiceNumber(numOfGroups)).append(" groups.");
      this.setText(builder.toString());
      this.setUrl(url);
   }

   private static String toNiceNumber(int number) {
      return number < 1000 ? Integer.toString(number) : (new DecimalFormat("0.0k")).format((double)number / 1000.0D);
   }
}
