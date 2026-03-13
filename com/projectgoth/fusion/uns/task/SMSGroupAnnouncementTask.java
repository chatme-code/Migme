package com.projectgoth.fusion.uns.task;

import com.projectgoth.fusion.dao.GroupMembershipDAO;
import com.projectgoth.fusion.domain.UsernameAndMobileNumber;
import com.projectgoth.fusion.slice.SMSUserNotification;
import com.projectgoth.fusion.uns.domain.SMSNote;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class SMSGroupAnnouncementTask extends SMSGroupTask {
   public SMSGroupAnnouncementTask(int groupId, GroupMembershipDAO groupMembershipDAO, SMSUserNotification notificationNote, BlockingQueue<SMSNote> queue) {
      super(groupId, groupMembershipDAO, notificationNote, queue);
   }

   public List<UsernameAndMobileNumber> getUsersToNotify() {
      return this.groupMembershipDAO.getGroupMemberUsernamesAndMobileNumbersForSMSNotification(this.groupId);
   }
}
