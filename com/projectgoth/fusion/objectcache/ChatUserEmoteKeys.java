package com.projectgoth.fusion.objectcache;

import com.projectgoth.fusion.app.dao.DAOException;
import com.projectgoth.fusion.app.dao.DAOFactory;
import com.projectgoth.fusion.app.dao.data.UserObject;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.EmoticonData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.Content;
import com.projectgoth.fusion.interfaces.ContentHome;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Logger;

public class ChatUserEmoteKeys {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(ChatUserEmoteKeys.class));
   private final Set<String> hotKeys = new LinkedHashSet();
   private final Set<String> alternateKeys = new LinkedHashSet();

   public ChatUserEmoteKeys(String username) throws Exception {
      List<EmoticonData> emoticons = null;
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.DAOSettings.ENABLED_USERDATA_DAO)) {
         emoticons = (new UserObject(username)).getEmoticons();
         if (log.isDebugEnabled()) {
            log.debug(String.format("DAO: get emotions for user:%s, data:%s", username, emoticons));
         }
      } else {
         Content contentEJB = (Content)EJBHomeCache.getObject("ejb/Content", ContentHome.class);
         emoticons = contentEJB.getEmoticons(username);
      }

      if (emoticons != null) {
         synchronized(this.hotKeys) {
            Iterator i$ = emoticons.iterator();

            while(i$.hasNext()) {
               EmoticonData emoticon = (EmoticonData)i$.next();
               this.hotKeys.add(emoticon.hotKey);
               this.alternateKeys.addAll(emoticon.alternateHotKeys);
            }
         }
      }

   }

   public String[] getEmoticonHotKeys() {
      synchronized(this.hotKeys) {
         return (String[])this.hotKeys.toArray(new String[this.hotKeys.size()]);
      }
   }

   public String[] getEmoticonAlternateKeys() {
      synchronized(this.hotKeys) {
         return (String[])this.alternateKeys.toArray(new String[this.alternateKeys.size()]);
      }
   }

   public List<String> getEmoticonKeysInMessage(String msg) {
      List<String> keys = new ArrayList();
      synchronized(this.hotKeys) {
         Iterator i$ = this.hotKeys.iterator();

         String emoticonKey;
         while(i$.hasNext()) {
            emoticonKey = (String)i$.next();
            if (!StringUtil.isBlank(emoticonKey) && msg.contains(emoticonKey)) {
               keys.add(emoticonKey);
            }
         }

         i$ = this.alternateKeys.iterator();

         while(i$.hasNext()) {
            emoticonKey = (String)i$.next();
            if (!StringUtil.isBlank(emoticonKey) && msg.contains(emoticonKey)) {
               keys.add(emoticonKey);
            }
         }

         return keys;
      }
   }

   public void packPurchased(int emoticonPackId) throws Exception {
      List<EmoticonData> emoticons = null;
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.DAOSettings.ENABLED_EMOANDSTICKER_DAO)) {
         try {
            emoticons = DAOFactory.getInstance().getEmoAndStickerDAO().getEmoticonPack(emoticonPackId);
         } catch (DAOException var7) {
            log.error(String.format("DAO: Failed to get EmoticonPack for pack:%s", emoticonPackId), var7);
         }
      } else {
         Content contentEJB = (Content)EJBHomeCache.getObject("ejb/Content", ContentHome.class);
         emoticons = contentEJB.getEmoticonPack(emoticonPackId);
      }

      synchronized(this.hotKeys) {
         if (emoticons != null) {
            Iterator i$ = emoticons.iterator();

            while(i$.hasNext()) {
               EmoticonData emoticon = (EmoticonData)i$.next();
               this.hotKeys.add(emoticon.hotKey);
               this.alternateKeys.addAll(emoticon.alternateHotKeys);
            }
         }

      }
   }
}
