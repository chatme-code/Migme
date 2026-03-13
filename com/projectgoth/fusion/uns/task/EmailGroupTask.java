package com.projectgoth.fusion.uns.task;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.dao.GroupMembershipDAO;
import com.projectgoth.fusion.slice.EmailUserNotification;
import com.projectgoth.fusion.uns.domain.EmailNote;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import org.apache.log4j.Logger;

public abstract class EmailGroupTask extends GroupTask {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(EmailGroupTask.class));
   private EmailUserNotification notificationNote;
   private BlockingQueue<EmailNote> queue;
   private String defaultMigEmailDomain;
   private int groupEmailBlockSize = 100;

   public EmailGroupTask(int groupId, GroupMembershipDAO groupMembershipDAO, EmailUserNotification notificationNote, BlockingQueue<EmailNote> queue, String defaultMigEmailDomain, int groupEmailBlockSize) {
      super(groupId, groupMembershipDAO);
      this.notificationNote = notificationNote;
      this.queue = queue;
      this.defaultMigEmailDomain = defaultMigEmailDomain;
      this.groupEmailBlockSize = groupEmailBlockSize;
   }

   public abstract List<String> getGroupMembersToNotify();

   public void run() {
      List<String> groupMembers = this.getGroupMembersToNotify();
      log.info("found " + groupMembers.size() + " members in group " + this.groupId + " to notify via email, groupEmailBlockSize = " + this.groupEmailBlockSize);
      EmailNote note = new EmailNote(this.notificationNote.message, this.notificationNote.subject);
      int count = 1;
      Iterator i$ = groupMembers.iterator();

      while(i$.hasNext()) {
         String member = (String)i$.next();
         note.addRecipient(member + this.defaultMigEmailDomain);
         if (count++ % this.groupEmailBlockSize == 0) {
            this.queue.add(note);
            note = new EmailNote(this.notificationNote.message, this.notificationNote.subject);
         }
      }

      --count;
      if (note.hasRecipients()) {
         this.queue.add(note);
      }

      log.info("created " + count + " emails for group " + this.groupId + " to queue");
   }
}
