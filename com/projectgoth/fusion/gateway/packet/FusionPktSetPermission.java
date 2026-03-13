package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.common.RMIExceptionHelper;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.fdl.enums.UserPermissionType;
import com.projectgoth.fusion.fdl.packets.FusionPktDataSetPermission;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.interfaces.Contact;
import com.projectgoth.fusion.interfaces.ContactHome;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.rmi.RemoteException;
import javax.ejb.CreateException;

public class FusionPktSetPermission extends FusionPktDataSetPermission {
   public FusionPktSetPermission(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktSetPermission(FusionPacket packet) {
      super(packet);
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      try {
         String username = this.getUsername();
         if (username == null) {
            throw new Exception("You must specify an username");
         } else {
            UserPermissionType permission = this.getPermission();
            if (permission == null) {
               throw new Exception("You must specify permission");
            } else {
               Contact contactEJB;
               switch(permission) {
               case ALLOW:
                  contactEJB = (Contact)EJBHomeCache.getObject("ejb/Contact", ContactHome.class);
                  contactEJB.unblockContact(connection.getUsername(), username, false);
                  break;
               case BLOCK:
                  contactEJB = (Contact)EJBHomeCache.getObject("ejb/Contact", ContactHome.class);
                  contactEJB.blockContact(connection.getUserID(), connection.getUsername(), username);
                  break;
               default:
                  throw new Exception("Invalid permission type " + permission);
               }

               return (new FusionPktOk(this.transactionId)).toArray();
            }
         }
      } catch (CreateException var5) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to set permission - Failed to create ContactEJB")).toArray();
      } catch (RemoteException var6) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to set permission - " + RMIExceptionHelper.getRootMessage(var6))).toArray();
      } catch (Exception var7) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to set permission - " + var7.getMessage())).toArray();
      }
   }
}
