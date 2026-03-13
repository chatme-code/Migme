package com.projectgoth.fusion.bothunter;

import Ice.Application;
import Ice.Properties;
import com.projectgoth.fusion.common.ConfigUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class BotKickerIce extends Application {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(KickThread.class));
   private static final String ONE = "1";
   private static final String CONFIG_FILE = "BotKicker.cfg";

   public static void main(String[] args) {
      BotKickerIce mbi = new BotKickerIce();
      String configFile = args.length > 0 ? args[0] : "BotKicker.cfg";
      int status = mbi.main(mbi.getClass().getName(), args, configFile);
      System.exit(status);
   }

   public int run(String[] args) {
      try {
         log.error("Starting Bot Kicker as an ice app");
         log.error("");
         this.getSettings(args);
         log.error("BotKickerIce starting with logging level " + log.getLevel());
         log.error("and bot hunter proxy=" + Params.BOT_HUNTER_PROXY);
         KickThread.getInstance(false).run();
         log.error("BotKicker ice app terminated");
         return 0;
      } catch (Exception var3) {
         log.error("Exception occured. BotKicker ice app terminated. ", var3);
         return -1;
      }
   }

   private void getSettings(String[] args) {
      Properties p = communicator().getProperties();
      Params.BOT_HUNTER_PROXY = p.getProperty("BotHunterProxy");
      if (this.booleanIceProperty(p, "AutoKick")) {
         Params.AUTOKICK = true;
         Params.REGISTRY_PROXY = p.getProperty("RegistryProxy");
      }

      if (this.booleanIceProperty(p, "Verbose") || this.booleanIceProperty(p, "VerboseKick")) {
         log.setLevel(Level.DEBUG);
      }

   }

   private boolean booleanIceProperty(Properties p, String propName) {
      return p.getProperty(propName).equals("1");
   }
}
