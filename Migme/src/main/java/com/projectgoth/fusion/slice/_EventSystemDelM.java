package com.projectgoth.fusion.slice;

import Ice.LocalException;
import Ice.OperationMode;
import Ice.UnknownUserException;
import Ice.UserException;
import Ice._ObjectDelM;
import IceInternal.BasicStream;
import IceInternal.LocalExceptionWrapper;
import IceInternal.Outgoing;
import java.util.Map;

public final class _EventSystemDelM extends _ObjectDelM implements _EventSystemDel {
   public void addedFriend(String username, String friend, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("addedFriend", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(username);
            __os.writeString(friend);
         } catch (LocalException var16) {
            __og.abort(var16);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var14) {
                  throw var14;
               } catch (UserException var15) {
                  throw new UnknownUserException(var15.ice_name());
               }
            }

            __og.is().skipEmptyEncaps();
         } catch (LocalException var17) {
            throw new LocalExceptionWrapper(var17, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void createdPublicChatroom(String username, String chatroomName, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("createdPublicChatroom", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(username);
            __os.writeString(chatroomName);
         } catch (LocalException var16) {
            __og.abort(var16);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var14) {
                  throw var14;
               } catch (UserException var15) {
                  throw new UnknownUserException(var15.ice_name());
               }
            }

            __og.is().skipEmptyEncaps();
         } catch (LocalException var17) {
            throw new LocalExceptionWrapper(var17, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void deleteUserEvents(String username, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("deleteUserEvents", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(username);
         } catch (LocalException var15) {
            __og.abort(var15);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var13) {
                  throw var13;
               } catch (UserException var14) {
                  throw new UnknownUserException(var14.ice_name());
               }
            }

            __og.is().skipEmptyEncaps();
         } catch (LocalException var16) {
            throw new LocalExceptionWrapper(var16, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void genericApplicationEvent(String username, String appId, String text, Map<String, String> customDeviceURL, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("genericApplicationEvent", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(username);
            __os.writeString(appId);
            __os.writeString(text);
            ParamMapHelper.write(__os, customDeviceURL);
         } catch (LocalException var18) {
            __og.abort(var18);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var16) {
                  throw var16;
               } catch (UserException var17) {
                  throw new UnknownUserException(var17.ice_name());
               }
            }

            __og.is().skipEmptyEncaps();
         } catch (LocalException var19) {
            throw new LocalExceptionWrapper(var19, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public EventPrivacySettingIce getPublishingPrivacyMask(String username, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("getPublishingPrivacyMask", OperationMode.Normal, __ctx);

      EventPrivacySettingIce var7;
      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(username);
         } catch (LocalException var17) {
            __og.abort(var17);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var15) {
                  throw var15;
               } catch (UserException var16) {
                  throw new UnknownUserException(var16.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            EventPrivacySettingIce __ret = new EventPrivacySettingIce();
            __ret.__read(__is);
            __is.endReadEncaps();
            var7 = __ret;
         } catch (LocalException var18) {
            throw new LocalExceptionWrapper(var18, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var7;
   }

   public EventPrivacySettingIce getReceivingPrivacyMask(String username, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("getReceivingPrivacyMask", OperationMode.Normal, __ctx);

      EventPrivacySettingIce var7;
      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(username);
         } catch (LocalException var17) {
            __og.abort(var17);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var15) {
                  throw var15;
               } catch (UserException var16) {
                  throw new UnknownUserException(var16.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            EventPrivacySettingIce __ret = new EventPrivacySettingIce();
            __ret.__read(__is);
            __is.endReadEncaps();
            var7 = __ret;
         } catch (LocalException var18) {
            throw new LocalExceptionWrapper(var18, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var7;
   }

   public UserEventIce[] getUserEventsForUser(String username, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("getUserEventsForUser", OperationMode.Normal, __ctx);

      UserEventIce[] var7;
      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(username);
         } catch (LocalException var17) {
            __og.abort(var17);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var15) {
                  throw var15;
               } catch (UserException var16) {
                  throw new UnknownUserException(var16.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            UserEventIce[] __ret = UserEventIceArrayHelper.read(__is);
            __is.readPendingObjects();
            __is.endReadEncaps();
            var7 = __ret;
         } catch (LocalException var18) {
            throw new LocalExceptionWrapper(var18, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var7;
   }

   public UserEventIce[] getUserEventsGeneratedByUser(String username, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("getUserEventsGeneratedByUser", OperationMode.Normal, __ctx);

      UserEventIce[] var7;
      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(username);
         } catch (LocalException var17) {
            __og.abort(var17);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var15) {
                  throw var15;
               } catch (UserException var16) {
                  throw new UnknownUserException(var16.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            UserEventIce[] __ret = UserEventIceArrayHelper.read(__is);
            __is.readPendingObjects();
            __is.endReadEncaps();
            var7 = __ret;
         } catch (LocalException var18) {
            throw new LocalExceptionWrapper(var18, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var7;
   }

   public void giftShowerEvent(String username, String recipient, String giftName, int virtualGiftReceivedId, int totalRecipients, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("giftShowerEvent", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(username);
            __os.writeString(recipient);
            __os.writeString(giftName);
            __os.writeInt(virtualGiftReceivedId);
            __os.writeInt(totalRecipients);
         } catch (LocalException var18) {
            __og.abort(var18);
         }

         boolean __ok = __og.invoke();
         if (!__og.is().isEmpty()) {
            try {
               if (!__ok) {
                  try {
                     __og.throwUserException();
                  } catch (UserException var16) {
                     throw new UnknownUserException(var16.ice_name());
                  }
               }

               __og.is().skipEmptyEncaps();
            } catch (LocalException var17) {
               throw new LocalExceptionWrapper(var17, false);
            }
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void groupAnnouncement(String username, int groupId, int groupAnnoucementId, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("groupAnnouncement", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(username);
            __os.writeInt(groupId);
            __os.writeInt(groupAnnoucementId);
         } catch (LocalException var17) {
            __og.abort(var17);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var15) {
                  throw var15;
               } catch (UserException var16) {
                  throw new UnknownUserException(var16.ice_name());
               }
            }

            __og.is().skipEmptyEncaps();
         } catch (LocalException var18) {
            throw new LocalExceptionWrapper(var18, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void groupDonation(String username, int groupId, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("groupDonation", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(username);
            __os.writeInt(groupId);
         } catch (LocalException var16) {
            __og.abort(var16);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var14) {
                  throw var14;
               } catch (UserException var15) {
                  throw new UnknownUserException(var15.ice_name());
               }
            }

            __og.is().skipEmptyEncaps();
         } catch (LocalException var17) {
            throw new LocalExceptionWrapper(var17, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void groupJoined(String username, int groupId, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("groupJoined", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(username);
            __os.writeInt(groupId);
         } catch (LocalException var16) {
            __og.abort(var16);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var14) {
                  throw var14;
               } catch (UserException var15) {
                  throw new UnknownUserException(var15.ice_name());
               }
            }

            __og.is().skipEmptyEncaps();
         } catch (LocalException var17) {
            throw new LocalExceptionWrapper(var17, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void madeGroupUserPost(String username, int userPostId, int groupId, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("madeGroupUserPost", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(username);
            __os.writeInt(userPostId);
            __os.writeInt(groupId);
         } catch (LocalException var17) {
            __og.abort(var17);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var15) {
                  throw var15;
               } catch (UserException var16) {
                  throw new UnknownUserException(var16.ice_name());
               }
            }

            __og.is().skipEmptyEncaps();
         } catch (LocalException var18) {
            throw new LocalExceptionWrapper(var18, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void madePhotoPublic(String username, int scrapbookid, String title, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("madePhotoPublic", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(username);
            __os.writeInt(scrapbookid);
            __os.writeString(title);
         } catch (LocalException var17) {
            __og.abort(var17);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var15) {
                  throw var15;
               } catch (UserException var16) {
                  throw new UnknownUserException(var16.ice_name());
               }
            }

            __og.is().skipEmptyEncaps();
         } catch (LocalException var18) {
            throw new LocalExceptionWrapper(var18, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void purchasedVirtualGoods(String username, byte itemType, int itemid, String itemName, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("purchasedVirtualGoods", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(username);
            __os.writeByte(itemType);
            __os.writeInt(itemid);
            __os.writeString(itemName);
         } catch (LocalException var18) {
            __og.abort(var18);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var16) {
                  throw var16;
               } catch (UserException var17) {
                  throw new UnknownUserException(var17.ice_name());
               }
            }

            __og.is().skipEmptyEncaps();
         } catch (LocalException var19) {
            throw new LocalExceptionWrapper(var19, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void setProfileStatus(String username, String status, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("setProfileStatus", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(username);
            __os.writeString(status);
         } catch (LocalException var16) {
            __og.abort(var16);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var14) {
                  throw var14;
               } catch (UserException var15) {
                  throw new UnknownUserException(var15.ice_name());
               }
            }

            __og.is().skipEmptyEncaps();
         } catch (LocalException var17) {
            throw new LocalExceptionWrapper(var17, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void setPublishingPrivacyMask(String username, EventPrivacySettingIce mask, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("setPublishingPrivacyMask", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(username);
            mask.__write(__os);
         } catch (LocalException var16) {
            __og.abort(var16);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var14) {
                  throw var14;
               } catch (UserException var15) {
                  throw new UnknownUserException(var15.ice_name());
               }
            }

            __og.is().skipEmptyEncaps();
         } catch (LocalException var17) {
            throw new LocalExceptionWrapper(var17, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void setReceivingPrivacyMask(String username, EventPrivacySettingIce mask, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("setReceivingPrivacyMask", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(username);
            mask.__write(__os);
         } catch (LocalException var16) {
            __og.abort(var16);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var14) {
                  throw var14;
               } catch (UserException var15) {
                  throw new UnknownUserException(var15.ice_name());
               }
            }

            __og.is().skipEmptyEncaps();
         } catch (LocalException var17) {
            throw new LocalExceptionWrapper(var17, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void streamEventsToLoggingInUser(String username, ConnectionPrx connectionProxy, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("streamEventsToLoggingInUser", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(username);
            ConnectionPrxHelper.__write(__os, connectionProxy);
         } catch (LocalException var16) {
            __og.abort(var16);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var14) {
                  throw var14;
               } catch (UserException var15) {
                  throw new UnknownUserException(var15.ice_name());
               }
            }

            __og.is().skipEmptyEncaps();
         } catch (LocalException var17) {
            throw new LocalExceptionWrapper(var17, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void updateAllowList(String username, String[] watchers, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("updateAllowList", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(username);
            StringArrayHelper.write(__os, watchers);
         } catch (LocalException var16) {
            __og.abort(var16);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var14) {
                  throw var14;
               } catch (UserException var15) {
                  throw new UnknownUserException(var15.ice_name());
               }
            }

            __og.is().skipEmptyEncaps();
         } catch (LocalException var17) {
            throw new LocalExceptionWrapper(var17, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void updatedProfile(String username, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("updatedProfile", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(username);
         } catch (LocalException var15) {
            __og.abort(var15);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var13) {
                  throw var13;
               } catch (UserException var14) {
                  throw new UnknownUserException(var14.ice_name());
               }
            }

            __og.is().skipEmptyEncaps();
         } catch (LocalException var16) {
            throw new LocalExceptionWrapper(var16, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void userWallPost(String username, String wallOwnerUsername, String postContent, int userWallPostId, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("userWallPost", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(username);
            __os.writeString(wallOwnerUsername);
            __os.writeString(postContent);
            __os.writeInt(userWallPostId);
         } catch (LocalException var18) {
            __og.abort(var18);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var16) {
                  throw var16;
               } catch (UserException var17) {
                  throw new UnknownUserException(var17.ice_name());
               }
            }

            __og.is().skipEmptyEncaps();
         } catch (LocalException var19) {
            throw new LocalExceptionWrapper(var19, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void virtualGift(String username, String recipient, String giftName, int virtualGiftReceivedId, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("virtualGift", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(username);
            __os.writeString(recipient);
            __os.writeString(giftName);
            __os.writeInt(virtualGiftReceivedId);
         } catch (LocalException var17) {
            __og.abort(var17);
         }

         boolean __ok = __og.invoke();
         if (!__og.is().isEmpty()) {
            try {
               if (!__ok) {
                  try {
                     __og.throwUserException();
                  } catch (UserException var15) {
                     throw new UnknownUserException(var15.ice_name());
                  }
               }

               __og.is().skipEmptyEncaps();
            } catch (LocalException var16) {
               throw new LocalExceptionWrapper(var16, false);
            }
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }
}
