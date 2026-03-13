package com.projectgoth.fusion.userevent.domain;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.slice.AddingFriendUserEventIce;
import com.projectgoth.fusion.slice.AddingMultipleFriendsUserEventIce;
import com.projectgoth.fusion.slice.AddingTwoFriendsUserEventIce;
import com.projectgoth.fusion.slice.CreatedChatroomUserEventIce;
import com.projectgoth.fusion.slice.EventPrivacySettingIce;
import com.projectgoth.fusion.slice.GenericApplicationUserEventIce;
import com.projectgoth.fusion.slice.GiftShowerUserEventIce;
import com.projectgoth.fusion.slice.GroupUserEventIce;
import com.projectgoth.fusion.slice.PhotoUploadedUserEventIce;
import com.projectgoth.fusion.slice.ProfileUpdatedUserEventIce;
import com.projectgoth.fusion.slice.PurchasedVirtualGoodsUserEventIce;
import com.projectgoth.fusion.slice.ShortTextStatusUserEventIce;
import com.projectgoth.fusion.slice.UserEventIce;
import com.projectgoth.fusion.slice.UserWallPostUserEventIce;
import com.projectgoth.fusion.slice.VirtualGiftUserEventIce;
import com.sleepycat.persist.model.Persistent;
import org.apache.log4j.Logger;

@Persistent(
   version = 1
)
public class EventPrivacySetting {
   private static final transient Logger log = Logger.getLogger(ConfigUtils.getLoggerName(EventPrivacySetting.class));
   private boolean statusUpdates = true;
   private boolean profileChanges = true;
   private boolean addFriends = false;
   private boolean photosPublished = true;
   private boolean contentPurchased = true;
   private boolean chatroomCreation = true;
   private boolean virtualGifting = true;

   public EventPrivacySetting() {
   }

   public EventPrivacySetting(boolean statusUpdates, boolean profileChanges, boolean addFriends, boolean photosPublished, boolean contentPurchased, boolean chatroomCreation, boolean virtualGifting) {
      this.statusUpdates = statusUpdates;
      this.profileChanges = profileChanges;
      this.addFriends = addFriends;
      this.photosPublished = photosPublished;
      this.contentPurchased = contentPurchased;
      this.chatroomCreation = chatroomCreation;
      this.virtualGifting = virtualGifting;
   }

   public void setStatusUpdates(boolean statusUpdates) {
      this.statusUpdates = statusUpdates;
   }

   public void setProfileChanges(boolean profileChanges) {
      this.profileChanges = profileChanges;
   }

   public void setAddFriends(boolean addFriends) {
      this.addFriends = addFriends;
   }

   public void setPhotosPublished(boolean photosPublished) {
      this.photosPublished = photosPublished;
   }

   public void setContentPurchased(boolean contentPurchased) {
      this.contentPurchased = contentPurchased;
   }

   public void setChatroomCreation(boolean chatroomCreation) {
      this.chatroomCreation = chatroomCreation;
   }

   public void setVirtualGifting(boolean virtualGifting) {
      this.virtualGifting = virtualGifting;
   }

   public boolean applyMask(UserEvent event) {
      if (event instanceof AddingFriendUserEvent) {
         return this.addFriends;
      } else if (event instanceof AddingTwoFriendsUserEvent) {
         return this.addFriends;
      } else if (event instanceof AddingMultipleFriendsUserEvent) {
         return this.addFriends;
      } else if (event instanceof PurchasedVirtualGoodsUserEvent) {
         return this.contentPurchased;
      } else if (event instanceof ShortTextStatusUserEvent) {
         return this.statusUpdates;
      } else if (event instanceof ProfileUpdatedUserEvent) {
         return this.profileChanges;
      } else if (event instanceof PhotoUploadedUserEvent) {
         return this.photosPublished;
      } else if (event instanceof CreatedChatroomUserEvent) {
         return this.chatroomCreation;
      } else if (event instanceof VirtualGiftUserEvent) {
         return this.virtualGifting;
      } else if (event instanceof UserWallPostUserEvent) {
         return true;
      } else if (event instanceof GroupUserEvent) {
         return true;
      } else if (event instanceof GenericApplicationUserEvent) {
         return true;
      } else if (event instanceof GiftShowerUserEvent) {
         return true;
      } else {
         log.warn("unknown event type [" + event + "]");
         return false;
      }
   }

   public boolean applyMask(UserEventIce event) {
      if (event instanceof AddingFriendUserEventIce) {
         return this.addFriends;
      } else if (event instanceof AddingTwoFriendsUserEventIce) {
         return this.addFriends;
      } else if (event instanceof AddingMultipleFriendsUserEventIce) {
         return this.addFriends;
      } else if (event instanceof PurchasedVirtualGoodsUserEventIce) {
         return this.contentPurchased;
      } else if (event instanceof ShortTextStatusUserEventIce) {
         return this.statusUpdates;
      } else if (event instanceof ProfileUpdatedUserEventIce) {
         return this.profileChanges;
      } else if (event instanceof PhotoUploadedUserEventIce) {
         return this.photosPublished;
      } else if (event instanceof CreatedChatroomUserEventIce) {
         return this.chatroomCreation;
      } else if (event instanceof VirtualGiftUserEventIce) {
         return this.virtualGifting;
      } else if (event instanceof UserWallPostUserEventIce) {
         return true;
      } else if (event instanceof GroupUserEventIce) {
         return true;
      } else if (event instanceof GenericApplicationUserEventIce) {
         return true;
      } else if (event instanceof GiftShowerUserEventIce) {
         return true;
      } else {
         log.warn("unknown event type [" + event + "]");
         return false;
      }
   }

   public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append("addFriends [").append(this.addFriends).append("] chatroomCreation [").append(this.chatroomCreation).append("] contentpurchased [").append(this.contentPurchased).append("] photosPublished [").append(this.photosPublished).append("] profileChanges [").append(this.profileChanges).append("] statusUpdates [").append(this.statusUpdates).append("]").append("] virtual gifts [").append(this.virtualGifting).append("]");
      return builder.toString();
   }

   public EventPrivacySettingIce toEventPrivacySettingIce() {
      return new EventPrivacySettingIce(this.statusUpdates, this.profileChanges, this.addFriends, this.photosPublished, this.contentPurchased, this.chatroomCreation, this.virtualGifting);
   }

   public static EventPrivacySetting fromEventPrivacySettingIce(EventPrivacySettingIce mask) {
      return new EventPrivacySetting(mask.statusUpdates, mask.profileChanges, mask.addFriends, mask.photosPublished, mask.contentPurchased, mask.chatroomCreation, mask.virtualGifting);
   }
}
