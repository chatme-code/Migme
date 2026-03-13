package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.data.ContactGroupData;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktGroup extends FusionPacket {
   public FusionPktGroup() {
      super((short)401);
   }

   public FusionPktGroup(short transactionId) {
      super((short)401, transactionId);
   }

   public FusionPktGroup(FusionPacket packet) {
      super(packet);
   }

   public FusionPktGroup(short transactionId, ContactGroupData groupData) {
      super((short)401, transactionId);
      if (groupData.id != null) {
         this.setGroupId(groupData.id);
      }

      if (groupData.name != null) {
         this.setGroupName(groupData.name);
      }

   }

   public FusionPktGroup(short transactionId, Integer id, String name) {
      super((short)401, transactionId);
      if (id != null) {
         this.setGroupId(id);
      }

      if (name != null) {
         this.setGroupName(name);
      }

   }

   public Integer getGroupId() {
      return this.getIntField((short)1);
   }

   public void setGroupId(int groupId) {
      this.setField((short)1, groupId);
   }

   public String getGroupName() {
      return this.getStringField((short)2);
   }

   public void setGroupName(String groupName) {
      this.setField((short)2, groupName);
   }
}
