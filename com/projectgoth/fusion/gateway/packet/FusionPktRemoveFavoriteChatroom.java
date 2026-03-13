package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.common.ChatRoomUtils;
import com.projectgoth.fusion.common.ChatRoomValidationException;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.RMIExceptionHelper;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.fdl.packets.FusionPktDataRemoveFavoriteChatroom;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.interfaces.Message;
import com.projectgoth.fusion.interfaces.MessageHome;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.rmi.RemoteException;
import javax.ejb.CreateException;
import org.apache.log4j.Logger;

public class FusionPktRemoveFavoriteChatroom extends FusionPktDataRemoveFavoriteChatroom {
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(FusionPktRemoveFavoriteChatroom.class));

   public FusionPktRemoveFavoriteChatroom(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktRemoveFavoriteChatroom(FusionPacket packet) {
      super(packet);
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      try {
         String chatRoomName = this.getChatroomName();
         chatRoomName = ChatRoomUtils.validateChatRoomNameForAccess(chatRoomName);
         Message messageEJB = (Message)EJBHomeCache.getObject("ejb/Message", MessageHome.class);
         messageEJB.removeFavouriteChatRoom(connection.getUsername(), chatRoomName);
         return (new FusionPktOk(this.transactionId)).toArray();
      } catch (CreateException var4) {
         log.error("Exception while trying to remove favorite chat room.", var4);
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to remove favorite chat room - Internal Error")).toArray();
      } catch (RemoteException var5) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to remove favorite chat room - " + RMIExceptionHelper.getRootMessage(var5))).toArray();
      } catch (ChatRoomValidationException var6) {
         FusionPktError pktError = new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to remove favorite chat room - " + var6.getMessage());
         return new FusionPacket[]{pktError};
      } catch (Exception var7) {
         log.error("Exception while trying to remove favorite chat room.", var7);
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to remove favorite chat room - Internal Error")).toArray();
      }
   }
}
