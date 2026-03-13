package com.projectgoth.fusion.gateway.packet;

import Ice.LocalException;
import com.projectgoth.fusion.fdl.enums.ClientType;
import com.projectgoth.fusion.fdl.enums.ImType;
import com.projectgoth.fusion.fdl.packets.FusionPktDataLeavePrivateChat;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.messageswitchboard.MessageSwitchboardDispatcher;
import com.projectgoth.fusion.packet.FusionPacket;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.RegistryPrx;
import java.io.IOException;
import java.nio.ByteBuffer;

public class FusionPktLeavePrivateChat extends FusionPktDataLeavePrivateChat {
   public FusionPktLeavePrivateChat(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktLeavePrivateChat(FusionPacket packet) {
      super(packet);
   }

   private FusionPacket[] leaveFusionPrivateChat(ConnectionI connection) throws Exception {
      RegistryPrx registryPrx = connection.findRegistry();
      if (registryPrx == null) {
         throw new Exception("Unable to locate registry");
      } else {
         String dest = this.getChatId();
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
         ImType imType = this.getImType();
         switch(imType != null ? imType : ImType.FUSION) {
         case FUSION:
            return this.leaveFusionPrivateChat(connection);
         default:
            throw new Exception("Group chat is not supported for IM type " + imType);
         }
      } catch (FusionException var3) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to leave private chat - " + var3.message)).toArray();
      } catch (LocalException var4) {
         return (new FusionPktInternalServerError(this.transactionId, var4, "Failed to leave private chat")).toArray();
      } catch (Exception var5) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to leave private chat - " + var5.getMessage())).toArray();
      }
   }
}
