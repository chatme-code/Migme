package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.common.RMIExceptionHelper;
import com.projectgoth.fusion.data.ContactGroupData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.fdl.packets.FusionPktDataUpdateGroup;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.interfaces.Contact;
import com.projectgoth.fusion.interfaces.ContactHome;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.rmi.RemoteException;
import javax.ejb.CreateException;

public class FusionPktUpdateGroup extends FusionPktDataUpdateGroup {
   public FusionPktUpdateGroup(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktUpdateGroup(FusionPacket packet) {
      super(packet);
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      try {
         ContactGroupData groupData = new ContactGroupData();
         groupData.id = this.getGroupId();
         groupData.username = connection.getUsername();
         groupData.name = this.getGroupName();
         Contact contactEJB = (Contact)EJBHomeCache.getObject("ejb/Contact", ContactHome.class);
         contactEJB.updateGroupDetail(connection.getUserID(), groupData);
         return (new FusionPktOk(this.transactionId)).toArray();
      } catch (CreateException var4) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to create ContactEJB")).toArray();
      } catch (RemoteException var5) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to update group detail - " + RMIExceptionHelper.getRootMessage(var5))).toArray();
      }
   }
}
