package com.projectgoth.fusion.maintenance;

import com.projectgoth.fusion.data.AccountEntrySourceData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.data.UserProfileData;
import com.projectgoth.fusion.data.UserRegistrationContextData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.User;
import com.projectgoth.fusion.interfaces.UserHome;
import com.projectgoth.fusion.restapi.enums.RegistrationType;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

public class RegisterAccounts {
   public static final String MOBILE_PATH = "mobile";
   public static final String EMAIL_PATH1 = "email1";
   public static final String EMAIL_PATH2 = "email2";

   public static void main(String[] args) {
      try {
         mainInner(args);
      } catch (Exception var2) {
         System.err.println("Exception " + var2);
         var2.printStackTrace();
      }

   }

   private static void mainInner(String[] args) throws Exception {
      System.out.println("Usage: RegisterAccounts <path> <startIndex> <endIndex> <username prefix> <password> <starting mobile number> <ip address> <campaignName> <verified=true/false> <sleepBetweenUsersMillis>");
      System.out.println("where path=mobile/email1/email2");
      if (args.length != 10) {
         System.err.println("Wrong number of args");
         System.exit(1);
      }

      initializeLog4jForConsoleApp();
      int a = 0;
      int var22 = a + 1;
      String path = args[a].toLowerCase();
      int startIndex = Integer.parseInt(args[var22++]);
      int endIndex = Integer.parseInt(args[var22++]);
      String usernamePrefix = args[var22++];
      String password = args[var22++];
      Long startingMobileNumber = Long.parseLong(args[var22++]);
      String regIP = args[var22++];
      String campaignName = args[var22++];
      boolean verified = Boolean.parseBoolean(args[var22++].toLowerCase());
      long sleep = Long.parseLong(args[var22++]);
      RegistrationType type;
      if (path.equals("mobile")) {
         type = RegistrationType.MOBILE_REGISTRATION;
      } else if (path.equals("email1")) {
         type = RegistrationType.EMAIL_REGISTRATION_PATH1;
      } else if (path.equals("email2")) {
         type = RegistrationType.EMAIL_REGISTRATION_PATH2;
      } else {
         type = null;
         System.err.println("Invalid path param");
         System.exit(1);
      }

      User userEJB = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);

      for(int i = startIndex; i <= endIndex; ++i) {
         String username = usernamePrefix + i;
         UserData userData = new UserData();
         userData.type = UserData.TypeEnum.MIG33;
         userData.username = username;
         userData.password = password;
         userData.mobilePhone = "+1" + Long.toString(startingMobileNumber + (long)i);
         userData.displayName = username;
         userData.emailAddress = username + i + "@testing.mig33.com";
         userData.registrationIPAddress = regIP;
         UserProfileData userProfileData = new UserProfileData();
         userProfileData.username = userData.username;
         userProfileData.firstName = userData.username + ".first";
         userProfileData.lastName = userData.username + ".last";
         UserRegistrationContextData regContextData = new UserRegistrationContextData(campaignName, verified, type);
         userEJB.createUser(userData, userProfileData, false, regContextData, new AccountEntrySourceData(RegisterAccounts.class), false, false, false);

         try {
            Thread.sleep(sleep);
         } catch (Exception var21) {
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
