package com.projectgoth.fusion.eventqueue;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.Enums;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.interfaces.User;
import com.projectgoth.fusion.interfaces.UserHome;
import java.sql.Connection;
import java.util.Hashtable;
import java.util.concurrent.atomic.AtomicInteger;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import org.apache.log4j.Logger;

public class DummyEvent extends Event {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(DummyEvent.class));
   private static AtomicInteger processed = new AtomicInteger();

   public DummyEvent() {
      super(Enums.EventTypeEnum.TEST_EVENT);
   }

   public DummyEvent(String username) {
      super(username, Enums.EventTypeEnum.TEST_EVENT);
   }

   public static void resetProcessedCounter() {
      processed.set(0);
   }

   public static int getProcessedCounter() {
      return processed.get();
   }

   public boolean executeInternal() throws Exception {
      try {
         String initialContextFactory = System.getProperty("java.naming.factory.initial");
         String providerUrl = System.getProperty("java.naming.provider.url");
         if (StringUtil.isBlank(initialContextFactory)) {
            initialContextFactory = "org.jnp.interfaces.NamingContextFactory";
         }

         if (StringUtil.isBlank(providerUrl)) {
            providerUrl = "localhost:1099";
         }

         Hashtable<String, String> environment = new Hashtable();
         environment.put("java.naming.factory.initial", initialContextFactory);
         environment.put("java.naming.factory.url.pkgs", "org.jboss.naming:org.jnp.interfaces");
         environment.put("java.naming.provider.url", providerUrl);
         InitialContext context = new InitialContext(environment);
         Object obj = context.lookup("ejb/User");
         UserHome home = (UserHome)PortableRemoteObject.narrow(obj, UserHome.class);
         User userEJB = home.create();
         int userid = userEJB.getUserID(this.eventSubject, (Connection)null, false);
         if (userid == -1) {
            log.error(String.format("Unable to execute event: UserID not found for [%s]", this.eventSubject));
            return false;
         } else {
            log.info(String.format("Found userid [%d] for username[%s]", userid, this.eventSubject));
            processed.addAndGet(1);
            return true;
         }
      } catch (Exception var9) {
         log.error("Exception caught while executing DummyEvent ", var9);
         return false;
      }
   }
}
