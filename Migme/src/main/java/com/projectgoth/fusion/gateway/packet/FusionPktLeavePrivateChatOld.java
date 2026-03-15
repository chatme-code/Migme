package com.projectgoth.fusion.gateway.packet;

import Ice.LocalException;
import com.projectgoth.fusion.fdl.enums.ClientType;
import com.projectgoth.fusion.fdl.enums.ImType;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.messageswitchboard.MessageSwitchboardDispatcher;
import com.projectgoth.fusion.packet.FusionPacket;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.RegistryPrx;

public class FusionPktLeavePrivateChatOld extends FusionRequest {
   public FusionPktLeavePrivateChatOld() {
      super((short)507);
   }

   public FusionPktLeavePrivateChatOld(short transactionId) {
      super((short)507, transactionId);
   }

   public FusionPktLeavePrivateChatOld(FusionPacket packet) {
      super(packet);
   }

   public String getDestinationUsername() {
      return this.getStringField((short)1);
   }

   public void setDestinationUsername(String destUsername) {
      this.setField((short)1, destUsername);
   }

   public Byte getIMType() {
      return this.getByteField((short)2);
   }

   public void setIMType(byte imType) {
      this.setField((short)2, imType);
   }

   public boolean sessionRequired() {
      return true;
   }

   private FusionPacket[] leaveFusionPrivateChat(ConnectionI connection) throws Exception {
      RegistryPrx registryPrx = connection.findRegistry();
      if (registryPrx == null) {
         throw new Exception("Unable to locate registry");
      } else {
         String dest = this.getDestinationUsername();
         if (dest == null) {
            throw new Exception("You must specify the username of the person you were private chatting with");
         } else {
            ClientType dt = ClientType.fromValue(connection.getDeviceTypeAsInt());
            MessageSwitchboardDispatcher.getInstance().onLeavePrivateChat(connection, connection.getUserID(), connection.getUsername(), dest, dt, connection.getClientVersion());
            return (new FusionPktOk(this.transactionId)).toArray();
         }
      }
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      try {
         ImType imType = ImType.FUSION;
         Byte byteVal = this.getIMType();
         if (byteVal != null) {
            imType = ImType.fromValue(byteVal);
            if (imType == null) {
               throw new Exception("Invalid IM type " + byteVal);
            }
         }

         switch(imType) {
         case FUSION:
            return this.leaveFusionPrivateChat(connection);
         default:
            throw new Exception("Group chat is not supported for IM type " + imType);
         }
      } catch (FusionException var4) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to leave private chat - " + var4.message)).toArray();
      } catch (LocalException var5) {
         return (new FusionPktInternalServerError(this.transactionId, var5, "Failed to leave private chat")).toArray();
      } catch (Exception var6) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to leave private chat - " + var6.getMessage())).toArray();
      }
   }
}
