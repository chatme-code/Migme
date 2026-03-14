package com.projectgoth.fusion.uns.task;

import com.projectgoth.fusion.dao.GroupMembershipDAO;

public abstract class GroupTask extends Task {
   protected int groupId;
   protected GroupMembershipDAO groupMembershipDAO;

   public GroupTask(int groupId, GroupMembershipDAO groupMembershipDAO) {
      this.groupId = groupId;
      this.groupMembershipDAO = groupMembershipDAO;
   }
}
