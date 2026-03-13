package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.common.RMIExceptionHelper;
import com.projectgoth.fusion.data.ContactGroupData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.interfaces.Contact;
import com.projectgoth.fusion.interfaces.ContactHome;
import com.projectgoth.fusion.packet.FusionPacket;
import java.rmi.RemoteException;
import javax.ejb.CreateException;

public class FusionPktUpdateGroupOld extends FusionRequest {
   public FusionPktUpdateGroupOld() {
      super((short)410);
   }

   public FusionPktUpdateGroupOld(short transactionId) {
      super((short)410, transactionId);
   }

   public FusionPktUpdateGroupOld(FusionPacket packet) {
      super(packet);
   }

   public Integer getGroupID() {
      return this.getIntField((short)1);
   }

   public void setGroupID(int groupID) {
      this.setField((short)1, groupID);
   }

   public String getGroupName() {
      return this.getStringField((short)2);
   }

   public void setGroupName(String groupName) {
      this.setField((short)2, groupName);
   }

   public boolean sessionRequired() {
      return true;
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      try {
         ContactGroupData groupData = new ContactGroupData();
         groupData.id = this.getGroupID();
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
