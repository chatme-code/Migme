package com.projectgoth.fusion.objectcache;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.data.EmoteData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.Content;
import com.projectgoth.fusion.interfaces.ContentHome;
import com.projectgoth.fusion.slice.ChatRoomPrx;
import com.projectgoth.fusion.slice.GroupChatPrx;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.log4j.Logger;

public class Emote {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(Emote.class));
   public static final String MAGIC_CHAR = "/";
   public static final String[] EIGHT_BALL_TEXTS = new String[]{"Yep", "OK", "Maybe", "No", "Don't Bother"};
   private static Map<String, List<EmoteData>> emoteMap = new ConcurrentHashMap();
   private static SecureRandom secureRandom = new SecureRandom();
   private final long LOCAL_CACHE_TIME;
   private long emoteMapNextUpdate;
   private String target;
   private String finalString;

   public Emote(String sender, String msg) throws Exception {
      this.LOCAL_CACHE_TIME = 300L;
      this.emoteMapNextUpdate = -1L;
      this.target = "";
      this.finalString = "";
      if (msg != null && msg.length() != 0 && msg.startsWith("/")) {
         String command = "";
         String[] tokens = msg.split(" ");
         if (tokens.length > 0) {
            command = tokens[0];
         }

         if ("/me".equalsIgnoreCase(command)) {
            this.finalString = msg.replaceFirst("/[Mm][Ee]", sender);
         } else {
            if (emoteMap.isEmpty() || this.emoteMapNextUpdate <= System.currentTimeMillis()) {
               this.loadEmotes();
            }

            List<EmoteData> emoteDataList = (List)emoteMap.get(command.toLowerCase());
            if (emoteDataList == null) {
               throw new Exception("'" + command + "' is not a valid command");
            } else {
               EmoteData emoteData;
               if (emoteDataList.size() == 1) {
                  emoteData = (EmoteData)emoteDataList.get(0);
               } else {
                  emoteData = (EmoteData)emoteDataList.get(secureRandom.nextInt(emoteDataList.size()));
               }

               for(int i = 1; i < tokens.length; ++i) {
                  if (tokens[i].length() > 0) {
                     this.target = tokens[i];
                     break;
                  }
               }

               if (this.target.length() > 0) {
                  this.finalString = emoteData.actionWithTarget.replaceAll("%s", sender).replaceAll("%t", this.target);
               } else {
                  this.finalString = emoteData.action.replaceAll("%s", sender);
               }

               if ("/roll".equalsIgnoreCase(command)) {
                  this.finalString = this.finalString.replaceAll("%r", String.valueOf(secureRandom.nextInt(100) + 1));
               } else if ("/8ball".equalsIgnoreCase(command)) {
                  this.finalString = this.finalString.replaceAll("%r", EIGHT_BALL_TEXTS[secureRandom.nextInt(EIGHT_BALL_TEXTS.length)]);
               }

            }
         }
      } else {
         throw new Exception("Emotes must start with '/'");
      }
   }

   public Emote(String sender, String msg, String validTarget) throws Exception {
      this(sender, msg);
      if (this.requiresTarget() && !this.target.equals(validTarget)) {
         throw new Exception(this.target + " is not a valid target");
      }
   }

   public Emote(String sender, String msg, ChatRoomPrx chatRoomPrx) throws Exception {
      this(sender, msg);
      if (this.requiresTarget()) {
         String[] validTargets = chatRoomPrx.getParticipants((String)null);
         Arrays.sort(validTargets);
         if (Arrays.binarySearch(validTargets, this.target) < 0) {
            throw new Exception(this.target + " is not a valid target");
         }
      }

   }

   public Emote(String sender, String msg, GroupChatPrx groupChatPrx) throws Exception {
      this(sender, msg);
      if (this.requiresTarget()) {
         String[] validTargets = groupChatPrx.getParticipants((String)null);
         Arrays.sort(validTargets);
         if (Arrays.binarySearch(validTargets, this.target) < 0) {
            throw new Exception(this.target + " is not a valid target");
         }
      }

   }

   private synchronized void loadEmotes() {
      try {
         Content contentEJB = (Content)EJBHomeCache.getObject("ejb/Content", ContentHome.class);
         List<EmoteData> emoteDataList = contentEJB.getEmotes();
         emoteMap.clear();

         EmoteData emoteData;
         String key;
         for(Iterator i$ = emoteDataList.iterator(); i$.hasNext(); ((List)emoteMap.get(key)).add(emoteData)) {
            emoteData = (EmoteData)i$.next();
            key = "/" + emoteData.command.toLowerCase();
            if (!emoteMap.containsKey(key)) {
               emoteMap.put(key, new ArrayList());
            }
         }
      } catch (Exception var10) {
         log.warn("Unable to load emotes - " + var10.getClass().getName() + ":" + var10.getMessage());
      } finally {
         this.emoteMapNextUpdate = System.currentTimeMillis() + 1000L * SystemProperty.getLong("EmotesReloadIntervalInSeconds", 300L);
      }

   }

   public boolean requiresTarget() {
      return this.target.length() > 0;
   }

   public String toString() {
      return this.finalString;
   }

   public static boolean isEmote(String msg) {
      return !StringUtil.isBlank(msg) && msg.startsWith("/");
   }
}
