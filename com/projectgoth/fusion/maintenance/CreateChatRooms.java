package com.projectgoth.fusion.maintenance;

import com.projectgoth.fusion.data.ChatRoomData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.Message;
import com.projectgoth.fusion.interfaces.MessageHome;
import com.projectgoth.fusion.interfaces.User;
import com.projectgoth.fusion.interfaces.UserHome;
import java.sql.Connection;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

public class CreateChatRooms {
   private static final Logger log = Logger.getLogger(CreateChatRooms.class);

   public static void main(String[] args) {
      try {
         mainInner(args);
      } catch (Exception var2) {
         System.err.println("Exception " + var2);
         var2.printStackTrace();
      }

   }

   private static void mainInner(String[] args) throws Exception {
      System.out.println("Usage: CreateChatRooms <creatorUsername> <creatorRepuScore> <startIndex> <endIndex> <chatroom prefix> <maxChatRoomSize> <sleepMillis>");
      System.out.println("Note: owner's reputation score will be permanently updated to the supplied value  (owner will not be rewarded)");
      if (args.length != 7) {
         System.err.println("Wrong number of args");
         System.exit(1);
      }

      initializeLog4jForConsoleApp();
      int a = 0;
      int var18 = a + 1;
      String creatorUsername = args[a];
      int creatorRepuScore = Integer.parseInt(args[var18++]);
      int startIndex = Integer.parseInt(args[var18++]);
      int endIndex = Integer.parseInt(args[var18++]);
      String chatRoomPrefix = args[var18++];
      int maxChatRoomSize = Integer.parseInt(args[var18++]);
      long sleep = Long.parseLong(args[var18++]);
      User userEJB = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
      Message messageEJB = (Message)EJBHomeCache.getObject("ejb/Message", MessageHome.class);
      int creatorId = userEJB.getUserID(creatorUsername, (Connection)null);
      userEJB.updateReputationScore(creatorId, creatorRepuScore, true);

      for(int i = startIndex; i <= endIndex; ++i) {
         String roomName = chatRoomPrefix + i;
         log.info("Creating chatroom " + roomName);
         ChatRoomData chatRoom = new ChatRoomData();
         chatRoom.creator = creatorUsername;
         chatRoom.name = roomName;
         chatRoom.maximumSize = maxChatRoomSize;
         messageEJB.createChatRoom(chatRoom, (String)null);

         try {
            Thread.sleep(sleep);
         } catch (Exception var17) {
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
