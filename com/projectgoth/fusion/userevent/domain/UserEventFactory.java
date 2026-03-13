package com.projectgoth.fusion.userevent.domain;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.slice.AddingFriendUserEventIce;
import com.projectgoth.fusion.slice.AddingMultipleFriendsUserEventIce;
import com.projectgoth.fusion.slice.AddingTwoFriendsUserEventIce;
import com.projectgoth.fusion.slice.CreatedChatroomUserEventIce;
import com.projectgoth.fusion.slice.GenericApplicationUserEventIce;
import com.projectgoth.fusion.slice.GiftShowerUserEventIce;
import com.projectgoth.fusion.slice.GroupAnnouncementUserEventIce;
import com.projectgoth.fusion.slice.GroupDonationUserEventIce;
import com.projectgoth.fusion.slice.GroupJoinedUserEventIce;
import com.projectgoth.fusion.slice.GroupUserPostUserEventIce;
import com.projectgoth.fusion.slice.PhotoUploadedUserEventIce;
import com.projectgoth.fusion.slice.ProfileUpdatedUserEventIce;
import com.projectgoth.fusion.slice.PurchasedVirtualGoodsUserEventIce;
import com.projectgoth.fusion.slice.ShortTextStatusUserEventIce;
import com.projectgoth.fusion.slice.UserEventIce;
import com.projectgoth.fusion.slice.UserWallPostUserEventIce;
import com.projectgoth.fusion.slice.VirtualGiftUserEventIce;
import org.apache.log4j.Logger;

public abstract class UserEventFactory {
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(UserEventFactory.class));

   public static UserEvent getUserEvent(UserEventIce event) {
      if (event instanceof AddingFriendUserEventIce) {
         return new AddingFriendUserEvent((AddingFriendUserEventIce)event);
      } else if (event instanceof AddingTwoFriendsUserEventIce) {
         return new AddingTwoFriendsUserEvent((AddingTwoFriendsUserEventIce)event);
      } else if (event instanceof AddingMultipleFriendsUserEventIce) {
         return new AddingMultipleFriendsUserEvent((AddingMultipleFriendsUserEventIce)event);
      } else if (event instanceof PurchasedVirtualGoodsUserEventIce) {
         return new PurchasedVirtualGoodsUserEvent((PurchasedVirtualGoodsUserEventIce)event);
      } else if (event instanceof ShortTextStatusUserEventIce) {
         return new ShortTextStatusUserEvent((ShortTextStatusUserEventIce)event);
      } else if (event instanceof ProfileUpdatedUserEventIce) {
         return new ProfileUpdatedUserEvent((ProfileUpdatedUserEventIce)event);
      } else if (event instanceof PhotoUploadedUserEventIce) {
         return new PhotoUploadedUserEvent((PhotoUploadedUserEventIce)event);
      } else if (event instanceof CreatedChatroomUserEventIce) {
         return new CreatedChatroomUserEvent((CreatedChatroomUserEventIce)event);
      } else if (event instanceof VirtualGiftUserEventIce) {
         return new VirtualGiftUserEvent((VirtualGiftUserEventIce)event);
      } else if (event instanceof UserWallPostUserEventIce) {
         return new UserWallPostUserEvent((UserWallPostUserEventIce)event);
      } else if (event instanceof GroupDonationUserEventIce) {
         return new GroupDonationUserEvent((GroupDonationUserEventIce)event);
      } else if (event instanceof GroupJoinedUserEventIce) {
         return new GroupJoinedUserEvent((GroupJoinedUserEventIce)event);
      } else if (event instanceof GroupAnnouncementUserEventIce) {
         return new GroupAnnouncementUserEvent((GroupAnnouncementUserEventIce)event);
      } else if (event instanceof GroupUserPostUserEventIce) {
         return new GroupUserPostUserEvent((GroupUserPostUserEventIce)event);
      } else if (event instanceof GenericApplicationUserEventIce) {
         return new GenericApplicationUserEvent((GenericApplicationUserEventIce)event);
      } else if (event instanceof GiftShowerUserEventIce) {
         return new GiftShowerUserEvent((GiftShowerUserEventIce)event);
      } else {
         log.warn("unknown event type " + event.ice_id());
         return new UserEvent(event);
      }
   }
}
