package com.projectgoth.fusion.uns;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.data.AccountEntrySourceData;
import com.projectgoth.fusion.data.SystemSMSData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.Message;
import com.projectgoth.fusion.interfaces.MessageHome;
import com.projectgoth.fusion.uns.domain.SMSNote;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicLong;
import javax.ejb.CreateException;
import org.apache.log4j.Logger;

public class SMSQueueWorkerThread extends Thread {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(SMSQueueWorkerThread.class));
   private BlockingQueue<SMSNote> queue;
   private boolean dryRun;
   private String twoWaySMSNumber;
   private Message messageBean;
   private AtomicLong smsSent;

   public SMSQueueWorkerThread(BlockingQueue<SMSNote> queue, boolean dryRun, String twoWaySMSNumber, AtomicLong smsSent) throws CreateException {
      this.queue = queue;
      this.dryRun = dryRun;
      this.twoWaySMSNumber = twoWaySMSNumber;
      this.smsSent = smsSent;
      this.messageBean = (Message)EJBHomeCache.getObject("ejb/Message", MessageHome.class);
   }

   public void run() {
      AccountEntrySourceData accountEntrySource = new AccountEntrySourceData(UserNotificationService.class);

      while(true) {
         while(true) {
            try {
               SMSNote note = (SMSNote)this.queue.take();
               SystemSMSData message = new SystemSMSData();
               message.type = SystemSMSData.TypeEnum.STANDARD;
               message.subType = note.getSubType();
               message.source = this.twoWaySMSNumber;
               message.messageText = note.getText();
               message.username = note.getUsername();
               message.destination = note.getPhoneNumber();
               if (log.isDebugEnabled()) {
                  log.debug("sending sms [" + message.messageText + "] to [" + message.destination + "]");
               }

               if (!this.dryRun) {
                  this.messageBean.sendSystemSMS(message, accountEntrySource);
                  this.smsSent.incrementAndGet();
               }
            } catch (Exception var4) {
               log.error("failed to send sms ", var4);
            }
         }
      }
   }
}
