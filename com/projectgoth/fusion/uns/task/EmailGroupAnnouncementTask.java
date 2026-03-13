package com.projectgoth.fusion.uns.task;

import com.projectgoth.fusion.dao.GroupMembershipDAO;
import com.projectgoth.fusion.slice.EmailUserNotification;
import com.projectgoth.fusion.uns.domain.EmailNote;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class EmailGroupAnnouncementTask extends EmailGroupTask {
   public EmailGroupAnnouncementTask(int groupId, GroupMembershipDAO groupMembershipDAO, EmailUserNotification notificationNote, BlockingQueue<EmailNote> queue, String defaultMigEmailDomain, int groupEmailBlockSize) {
      super(groupId, groupMembershipDAO, notificationNote, queue, defaultMigEmailDomain, groupEmailBlockSize);
   }

   public List<String> getGroupMembersToNotify() {
      return this.groupMembershipDAO.getGroupMemberUsernamesForEmailNotification(this.groupId);
   }
}
