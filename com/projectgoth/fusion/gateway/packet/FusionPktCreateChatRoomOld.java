package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.common.ChatRoomUtils;
import com.projectgoth.fusion.common.ChatRoomValidationException;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.RMIExceptionHelper;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.ChatRoomData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.interfaces.Message;
import com.projectgoth.fusion.interfaces.MessageHome;
import com.projectgoth.fusion.packet.FusionPacket;
import java.rmi.RemoteException;
import javax.ejb.CreateException;
import org.apache.log4j.Logger;

public class FusionPktCreateChatRoomOld extends FusionRequest {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(FusionPktCreateChatRoomOld.class));

   public FusionPktCreateChatRoomOld() {
      super((short)705);
   }

   public FusionPktCreateChatRoomOld(short transactionId) {
      super((short)705, transactionId);
   }

   public FusionPktCreateChatRoomOld(FusionPacket packet) {
      super(packet);
   }

   public String getChatRoomName() {
      return this.getStringField((short)1);
   }

   public void setChatRoomName(String chatRoomName) {
      this.setField((short)1, chatRoomName);
   }

   public String getDescription() {
      return this.getStringField((short)2);
   }

   public void setDescription(String description) {
      this.setField((short)2, description);
   }

   public String getKeywords() {
      return this.getStringField((short)3);
   }

   public void setKeywords(String keywords) {
      this.setField((short)3, keywords);
   }

   public String getLanguage() {
      return this.getStringField((short)4);
   }

   public void setLanguage(String language) {
      this.setField((short)4, language);
   }

   public Byte getAllowKicking() {
      return this.getByteField((short)5);
   }

   public void setAllowKicking(byte allowKicking) {
      this.setField((short)5, allowKicking);
   }

   public boolean sessionRequired() {
      return true;
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      try {
         String chatRoomName = this.getChatRoomName();
         chatRoomName = ChatRoomUtils.validateChatRoomNameForCreation(chatRoomName);
         Message messageEJB = (Message)EJBHomeCache.getObject("ejb/Message", MessageHome.class);
         ChatRoomData chatRoom = new ChatRoomData();
         chatRoom.creator = connection.getUsername();
         chatRoom.name = chatRoomName;
         chatRoom.userOwned = true;
         chatRoom.allowBots = true;
         String keywords = null;
         if ((Boolean)SystemPropertyEntities.Temp.Cache.se524CreateChatRoomPktAdditionalDataSupportEnabled.getValue()) {
            chatRoom.description = this.getDescription();
            keywords = this.getKeywords();
            String language = this.getLanguage();
            if (language == null) {
               language = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.DEFAULT_LANGUAGE);
            }

            chatRoom.language = language;
            Byte allowKicking = this.getAllowKicking();
            if (allowKicking != null) {
               chatRoom.allowKicking = allowKicking != 0;
            }
         }

         messageEJB.createChatRoom(chatRoom, keywords);
         FusionPktJoinChatRoomOld join = new FusionPktJoinChatRoomOld(this.transactionId);
         join.setChatRoomName(chatRoomName);
         return join.process(connection);
      } catch (ChatRoomValidationException var8) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to create chat room - " + var8.getMessage())).toArray();
      } catch (CreateException var9) {
         log.error("Caught Exception while trying to create chat room.", var9);
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to create chat room - Internal Error")).toArray();
      } catch (RemoteException var10) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to create chat room - " + RMIExceptionHelper.getRootMessage(var10))).toArray();
      } catch (Exception var11) {
         log.error("Caught Exception while trying to create chat room.", var11);
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to create chat room - Internal Error")).toArray();
      }
   }
}
