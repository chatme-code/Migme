package com.projectgoth.fusion.botservice;

import com.projectgoth.fusion.botservice.bot.Bot;
import com.projectgoth.fusion.dao.BotDAO;
import com.projectgoth.fusion.dao.impl.BotDAOJDBC;
import com.projectgoth.fusion.data.BotData;
import com.projectgoth.fusion.slice.BotChannelPrx;
import java.io.File;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.concurrent.ScheduledExecutorService;
import org.springframework.util.StringUtils;

public class BotLoader {
   public static Bot addBotToChannel(ScheduledExecutorService executor, BotData botData, BotChannelPrx channelProxy, BotDAO botDAO, String starterUsername, String languageCode) throws Exception {
      ClassLoader classLoader = new URLClassLoader(getLibraryURLs(botData.getLibraryPaths()));
      Class botClass = classLoader.loadClass(botData.getExecutableFileName());
      Class[] signature = new Class[]{ScheduledExecutorService.class, BotChannelPrx.class, BotData.class, String.class, String.class, BotDAO.class};
      Object[] parameters = new Object[]{executor, channelProxy, botData, languageCode, starterUsername, botDAO};
      Constructor botConstructor = botClass.getConstructor(signature);
      Bot bot = (Bot)botConstructor.newInstance(parameters);
      return bot;
   }

   private static URL[] getLibraryURLs(String libraryPaths) throws MalformedURLException {
      if (!StringUtils.hasLength(libraryPaths)) {
         return new URL[0];
      } else {
         String[] paths = libraryPaths.split(";");
         URL[] urls = new URL[paths.length];

         for(int i = 0; i < paths.length; ++i) {
            urls[i] = (new File(paths[i])).toURI().toURL();
         }

         return urls;
      }
   }

   public static void main(String[] args) {
      try {
         BotData botData = new BotData();
         botData.setId(1L);
         botData.setLibraryPaths("C:/dev/fusion_botservice/target/artifacts/lib/fusion.jar;C:/dev/common/log4j-1.2.9/log4j-1.2.9.jar");
         botData.setExecutableFileName("com.projectgoth.fusion.botservice.bot.migbot.trivia.Trivia");
         addBotToChannel((ScheduledExecutorService)null, botData, (BotChannelPrx)null, new BotDAOJDBC(), "sashafan", "ENG");
      } catch (Exception var2) {
         var2.printStackTrace();
      }

   }
}
