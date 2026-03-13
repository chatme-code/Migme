package com.projectgoth.fusion.data;

import com.danga.MemCached.MemCachedClient;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.MemCachedClientWrapper;
import com.projectgoth.fusion.common.MemCachedUtils;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import org.apache.log4j.Logger;

public class DisplayPictureAndStatusMessage implements Serializable {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(DisplayPictureAndStatusMessage.class));
   private String displayPicture;
   private String statusMessage;
   private Date statusTimestamp;

   public DisplayPictureAndStatusMessage() {
   }

   public DisplayPictureAndStatusMessage(ContactData contact) {
      this.displayPicture = contact.displayPicture;
      this.statusMessage = contact.statusMessage;
      this.statusTimestamp = contact.statusTimeStamp;
   }

   public String getDisplayPicture() {
      return this.displayPicture;
   }

   public void setDisplayPicture(String displayPicture) {
      this.displayPicture = displayPicture;
   }

   public String getStatusMessage() {
      return this.statusMessage;
   }

   public void setStatusMessage(String statusMessage) {
      this.statusMessage = statusMessage;
   }

   public Date getStatusTimestamp() {
      return this.statusTimestamp;
   }

   public void setStatusTimestamp(Date statusTimestamp) {
      this.statusTimestamp = statusTimestamp;
   }

   public static String getKey(String username) {
      return username;
   }

   public static DisplayPictureAndStatusMessage getDisplayPictureAndStatusMessage(MemCachedClient instance, String username) {
      try {
         Object obj = instance.get(getKey(username));
         return obj instanceof DisplayPictureAndStatusMessage ? (DisplayPictureAndStatusMessage)obj : null;
      } catch (Exception var3) {
         if (log.isDebugEnabled()) {
            log.debug("Failed to get object since serialVersionUID is diff, ignoring and return null instead", var3);
         }

         return null;
      }
   }

   public static void setDisplayPictureAndStatusMessage(MemCachedClient instance, String username, DisplayPictureAndStatusMessage displayPictureAndStatusMessage) {
      instance.set(getKey(username), displayPictureAndStatusMessage);
   }

   public static void setDisplayPictureAndStatusMessage(MemCachedClient instance, ContactData contact) {
      DisplayPictureAndStatusMessage avatar = new DisplayPictureAndStatusMessage(contact);
      setDisplayPictureAndStatusMessage(instance, contact.fusionUsername, avatar);
   }

   public static void setMultiDisplayPictureAndStatusMessage(MemCachedClient instance, Collection<ContactData> contacts) {
      if (instance == null) {
         instance = MemCachedClientWrapper.getMemCachedClient(MemCachedUtils.Instance.userDisplayPictureAndStatus);
      }

      Iterator i$ = contacts.iterator();

      while(i$.hasNext()) {
         ContactData contact = (ContactData)i$.next();
         setDisplayPictureAndStatusMessage(instance, contact);
      }

   }

   public static boolean deleteDisplayPictureAndStatusMessage(MemCachedClient instance, String username) {
      return instance.delete(username);
   }
}
