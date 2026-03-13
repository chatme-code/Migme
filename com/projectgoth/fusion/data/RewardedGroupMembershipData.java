package com.projectgoth.fusion.data;

import java.io.Serializable;

public class RewardedGroupMembershipData implements Serializable {
   private final int groupId;

   public RewardedGroupMembershipData(int groupId) {
      this.groupId = groupId;
   }

   public int getGroupId() {
      return this.groupId;
   }

   public String toString() {
      return "group#" + this.groupId;
   }
}
