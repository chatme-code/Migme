package com.projectgoth.fusion.maintenance;

import com.projectgoth.fusion.data.ContactData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.Contact;
import com.projectgoth.fusion.interfaces.ContactHome;
import com.projectgoth.fusion.interfaces.User;
import com.projectgoth.fusion.interfaces.UserHome;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.util.Random;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

public class FriendUsers {
   private static final Logger log = Logger.getLogger(FriendUsers.class);
   private static final Random ran = new Random();

   public static void main(String[] args) {
      try {
         mainInner(args);
      } catch (Exception var2) {
         System.err.println("Exception " + var2);
         var2.printStackTrace();
      }

   }

   private static void mainInner(String[] args) throws Exception {
      System.out.println("Usage: FriendUsers <startIndex> <endIndex> <username prefix> <friendsPerUser> <sleepMillis>");
      if (args.length != 5) {
         System.err.println("Wrong number of args");
         System.exit(1);
      }

      initializeLog4jForConsoleApp();
      int a = 0;
      int var21 = a + 1;
      int startIndex = Integer.parseInt(args[a]);
      int endIndex = Integer.parseInt(args[var21++]);
      String usernamePrefix = args[var21++];
      int friendsPerUser = Integer.parseInt(args[var21++]);
      long sleep = Long.parseLong(args[var21++]);
      Contact contactEJB = (Contact)EJBHomeCache.getObject("ejb/Contact", ContactHome.class);
      User userEJB = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);

      for(int i = startIndex; i <= endIndex; ++i) {
         String usernameA = usernamePrefix + i;
         log.info("Making friends for " + usernameA);

         for(int j = 0; j < friendsPerUser; ++j) {
            int friendIndex = startIndex + ran.nextInt(endIndex - startIndex);
            String usernameB = usernamePrefix + friendIndex;
            log.info("Friending " + usernameA + " and " + usernameB);
            int userA_id = userEJB.getUserID(usernameA, (Connection)null);
            int userB_id = userEJB.getUserID(usernameB, (Connection)null);

            try {
               ContactData friendContact = new ContactData();
               friendContact.username = usernameA;
               friendContact.fusionUsername = usernameB;
               friendContact.displayOnPhone = true;
               contactEJB.addPendingFusionContact(userA_id, friendContact);
               ContactData acceptContact = new ContactData();
               acceptContact.username = usernameB;
               acceptContact.fusionUsername = usernameA;
               acceptContact.displayOnPhone = true;
               contactEJB.acceptContactRequest(userB_id, acceptContact, true);
            } catch (RemoteException var20) {
               log.error("Failed to friend users: " + var20);
            }

            try {
               Thread.sleep(sleep);
            } catch (Exception var19) {
            }
         }
      }

   }

   private static void initializeLog4jForConsoleApp() {
      ConsoleAppender console = new ConsoleAppender();
      String PATTERN = "%d [%p|%C{1}] %m%n";
      console.setLayout(new PatternLayout(PATTERN));
      console.setThreshold(Level.DEBUG);
      console.activateOptions();
      Logger.getRootLogger().addAppender(console);
      Logger.getRootLogger().setLevel(Level.INFO);
   }
}
