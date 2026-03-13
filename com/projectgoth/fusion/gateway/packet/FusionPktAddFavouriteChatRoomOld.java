package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.common.ChatRoomUtils;
import com.projectgoth.fusion.common.ChatRoomValidationException;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.RMIExceptionHelper;
import com.projectgoth.fusion.data.ChatRoomData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.interfaces.Message;
import com.projectgoth.fusion.interfaces.MessageHome;
import com.projectgoth.fusion.packet.FusionPacket;
import com.projectgoth.fusion.slice.ChatRoomPrx;
import com.projectgoth.fusion.slice.ObjectNotFoundException;
import java.rmi.RemoteException;
import javax.ejb.CreateException;
import org.apache.log4j.Logger;

public class FusionPktAddFavouriteChatRoomOld extends FusionRequest {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(FusionPktAddFavouriteChatRoomOld.class));

   public FusionPktAddFavouriteChatRoomOld() {
      super((short)711);
   }

   public FusionPktAddFavouriteChatRoomOld(short transactionId) {
      super((short)711, transactionId);
   }

   public FusionPktAddFavouriteChatRoomOld(FusionPacket packet) {
      super(packet);
   }

   public String getChatRoomName() {
      return this.getStringField((short)1);
   }

   public void setChatRoomName(String chatRoomName) {
      this.setField((short)1, chatRoomName);
   }

   public boolean sessionRequired() {
      return true;
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      try {
         String chatRoomName = this.getChatRoomName();
         chatRoomName = ChatRoomUtils.validateChatRoomNameForAccess(chatRoomName);
         ChatRoomPrx chatRoomPrx = connection.findRegistry().findChatRoomObject(chatRoomName);
         Message messageEJB = (Message)EJBHomeCache.getObject("ejb/Message", MessageHome.class);
         messageEJB.addFavouriteChatRoom(connection.getUsername(), chatRoomName);
         ChatRoomData roomData = null;
         if (chatRoomPrx == null) {
            roomData = messageEJB.getChatRoom(chatRoomName);
         } else {
            roomData = new ChatRoomData(chatRoomPrx.getRoomData());
         }

         FusionPktChatRoomOld pkt = new FusionPktChatRoomOld(this.transactionId, roomData);
         pkt.setCategory((short)1);
         return pkt.toArray();
      } catch (ChatRoomValidationException var7) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to add favorite chat room - " + var7.getMessage())).toArray();
      } catch (ObjectNotFoundException var8) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to add favorite chat room - Invalid chat room " + this.getChatRoomName())).toArray();
      } catch (CreateException var9) {
         log.error("Caught Exception while trying to add favorite chat room.", var9);
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to add favorite chat room - Internal Error")).toArray();
      } catch (RemoteException var10) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to add favorite chat room - " + RMIExceptionHelper.getRootMessage(var10))).toArray();
      } catch (Exception var11) {
         log.error("Caught Exception while trying to add favorite chat room.", var11);
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to add favorite chat room - Internal Error")).toArray();
      }
   }
}
