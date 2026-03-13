package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.common.RMIExceptionHelper;
import com.projectgoth.fusion.data.ContactGroupData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.fdl.packets.FusionPktDataAddGroup;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.interfaces.Contact;
import com.projectgoth.fusion.interfaces.ContactHome;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.rmi.RemoteException;
import javax.ejb.CreateException;

public class FusionPktAddGroup extends FusionPktDataAddGroup {
   public FusionPktAddGroup(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktAddGroup(FusionPacket packet) {
      super(packet);
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      try {
         ContactGroupData groupData = new ContactGroupData();
         groupData.username = connection.getUsername();
         groupData.name = this.getGroupName();
         Contact contactEJB = (Contact)EJBHomeCache.getObject("ejb/Contact", ContactHome.class);
         groupData = contactEJB.addGroup(connection.getUserID(), groupData, true);
         return new FusionPacket[]{new FusionPktGroup(this.transactionId, groupData)};
      } catch (CreateException var4) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to create ContactEJB")).toArray();
      } catch (RemoteException var5) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Add group failed - " + RMIExceptionHelper.getRootMessage(var5))).toArray();
      }
   }
}
