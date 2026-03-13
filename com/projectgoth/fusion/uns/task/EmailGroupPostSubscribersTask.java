package com.projectgoth.fusion.uns.task;

import com.projectgoth.fusion.dao.GroupMembershipDAO;
import com.projectgoth.fusion.slice.EmailUserNotification;
import com.projectgoth.fusion.uns.domain.EmailNote;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class EmailGroupPostSubscribersTask extends EmailGroupTask {
   private int userPostId;

   public EmailGroupPostSubscribersTask(int userPostId, GroupMembershipDAO groupMembershipDAO, EmailUserNotification notificationNote, BlockingQueue<EmailNote> queue, String defaultMigEmailDomain, int groupEmailBlockSize) {
      super(0, groupMembershipDAO, notificationNote, queue, defaultMigEmailDomain, groupEmailBlockSize);
      this.userPostId = userPostId;
   }

   public List<String> getGroupMembersToNotify() {
      return this.groupMembershipDAO.getGroupMemberUsernamesForNewGroupUserPostNotificationViaEmail(this.userPostId);
   }
}
