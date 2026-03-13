package com.projectgoth.fusion.maintenance;

import java.security.SecureRandom;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class StressTest implements Runnable {
   private static SecureRandom securedRandom = new SecureRandom();
   private LoginBot target;
   private LoginBot friend;

   public StressTest(LoginBot target, LoginBot friend) {
      this.target = target;
      this.friend = friend;
   }

   public void run() {
      synchronized(this.target) {
         if (this.target.isOnline()) {
            if (securedRandom.nextDouble() < 0.8D) {
               try {
                  this.target.sendMessage(this.friend.getUsername(), "Message from " + this.target.getUsername() + " to " + this.friend.getUsername());
               } catch (Exception var5) {
               }
            } else {
               this.target.logout();
            }
         } else {
            try {
               this.target.login();
            } catch (Exception var4) {
               System.out.println("Login failed - " + var4.getMessage());
            }
         }

      }
   }

   public static void main(String[] args) {
      if (args.length != 8) {
         System.out.println("StressTest duration server port isTCP usernamePrefix password firstUserIndex lastUserIndex");
      } else {
         long duration = Long.parseLong(args[0]) * 60000L;
         String server = args[1];
         int port = Integer.parseInt(args[2]);
         boolean isTCP = Integer.parseInt(args[3]) == 1;
         String userPrefix = args[4];
         String password = args[5];
         int lBound = Integer.parseInt(args[6]);
         int uBound = Integer.parseInt(args[7]);
         int size = uBound - lBound + 1;
         System.out.println("Start load testing. Duration is " + duration / 60000L + " minute" + (duration > 1L ? "s" : "") + ". Please sit back and relax");
         ThreadPoolExecutor pool = (ThreadPoolExecutor)Executors.newFixedThreadPool(20);
         LoginBot[] bots = new LoginBot[size];

         for(int i = 0; i < size; ++i) {
            bots[i] = new LoginBot(server, port, isTCP, userPrefix + (i + lBound), password);
         }

         long startTime = System.currentTimeMillis();
         long jobs = 0L;

         while(System.currentTimeMillis() - startTime < duration) {
            int r1 = securedRandom.nextInt(size);
            int r2 = securedRandom.nextInt(size);
            pool.execute(new StressTest(bots[r1], bots[r2]));
            ++jobs;

            try {
               Thread.sleep(5L);
            } catch (Exception var20) {
            }
         }

         pool.shutdown();
         System.out.println("End of load testing. " + jobs + " jobs dispatched");
         System.exit(0);
      }
   }
}
