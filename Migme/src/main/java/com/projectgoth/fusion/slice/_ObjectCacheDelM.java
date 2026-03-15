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

public final class _ObjectCacheDelM extends _ObjectDelM implements _ObjectCacheDel {
   public ChatRoomPrx createChatRoomObject(String name, Map<String, String> __ctx) throws LocalExceptionWrapper, ObjectExistsException, FusionException {
      Outgoing __og = this.__handler.getOutgoing("createChatRoomObject", OperationMode.Normal, __ctx);

      ChatRoomPrx var7;
      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(name);
         } catch (LocalException var19) {
            __og.abort(var19);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (ObjectExistsException var16) {
                  throw var16;
               } catch (FusionException var17) {
                  throw var17;
               } catch (UserException var18) {
                  throw new UnknownUserException(var18.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            ChatRoomPrx __ret = ChatRoomPrxHelper.__read(__is);
            __is.endReadEncaps();
            var7 = __ret;
         } catch (LocalException var20) {
            throw new LocalExceptionWrapper(var20, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var7;
   }

   public GroupChatPrx createGroupChatObject(String id, String creator, String privateChatPartner, String[] otherPartyList, Map<String, String> __ctx) throws LocalExceptionWrapper, ObjectExistsException, FusionException {
      Outgoing __og = this.__handler.getOutgoing("createGroupChatObject", OperationMode.Normal, __ctx);

      GroupChatPrx var10;
      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(id);
            __os.writeString(creator);
            __os.writeString(privateChatPartner);
            StringArrayHelper.write(__os, otherPartyList);
         } catch (LocalException var22) {
            __og.abort(var22);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (ObjectExistsException var19) {
                  throw var19;
               } catch (FusionException var20) {
                  throw var20;
               } catch (UserException var21) {
                  throw new UnknownUserException(var21.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            GroupChatPrx __ret = GroupChatPrxHelper.__read(__is);
            __is.endReadEncaps();
            var10 = __ret;
         } catch (LocalException var23) {
            throw new LocalExceptionWrapper(var23, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var10;
   }

   public UserPrx createUserObject(String username, Map<String, String> __ctx) throws LocalExceptionWrapper, ObjectExistsException, FusionException {
      Outgoing __og = this.__handler.getOutgoing("createUserObject", OperationMode.Normal, __ctx);

      UserPrx var7;
      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(username);
         } catch (LocalException var19) {
            __og.abort(var19);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (ObjectExistsException var16) {
                  throw var16;
               } catch (FusionException var17) {
                  throw var17;
               } catch (UserException var18) {
                  throw new UnknownUserException(var18.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            UserPrx __ret = UserPrxHelper.__read(__is);
            __is.endReadEncaps();
            var7 = __ret;
         } catch (LocalException var20) {
            throw new LocalExceptionWrapper(var20, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var7;
   }

   public UserPrx createUserObjectNonAsync(String username, Map<String, String> __ctx) throws LocalExceptionWrapper, ObjectExistsException, FusionException {
      Outgoing __og = this.__handler.getOutgoing("createUserObjectNonAsync", OperationMode.Normal, __ctx);

      UserPrx var7;
      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(username);
         } catch (LocalException var19) {
            __og.abort(var19);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (ObjectExistsException var16) {
                  throw var16;
               } catch (FusionException var17) {
                  throw var17;
               } catch (UserException var18) {
                  throw new UnknownUserException(var18.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            UserPrx __ret = UserPrxHelper.__read(__is);
            __is.endReadEncaps();
            var7 = __ret;
         } catch (LocalException var20) {
            throw new LocalExceptionWrapper(var20, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var7;
   }

   public GroupChatPrx[] getAllGroupChats(Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("getAllGroupChats", OperationMode.Normal, __ctx);

      GroupChatPrx[] var6;
      try {
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

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            GroupChatPrx[] __ret = GroupChatArrayHelper.read(__is);
            __is.endReadEncaps();
            var6 = __ret;
         } catch (LocalException var15) {
            throw new LocalExceptionWrapper(var15, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var6;
   }

   public MessageSwitchboardPrx getMessageSwitchboard(Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("getMessageSwitchboard", OperationMode.Normal, __ctx);

      MessageSwitchboardPrx var6;
      try {
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

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            MessageSwitchboardPrx __ret = MessageSwitchboardPrxHelper.__read(__is);
            __is.endReadEncaps();
            var6 = __ret;
         } catch (LocalException var15) {
            throw new LocalExceptionWrapper(var15, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var6;
   }

   public void purgeGroupChatObject(String id, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("purgeGroupChatObject", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(id);
         } catch (LocalException var14) {
            __og.abort(var14);
         }

         boolean __ok = __og.invoke();
         if (!__og.is().isEmpty()) {
            try {
               if (!__ok) {
                  try {
                     __og.throwUserException();
                  } catch (UserException var12) {
                     throw new UnknownUserException(var12.ice_name());
                  }
               }

               __og.is().skipEmptyEncaps();
            } catch (LocalException var13) {
               throw new LocalExceptionWrapper(var13, false);
            }
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void purgeUserObject(String username, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("purgeUserObject", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(username);
         } catch (LocalException var14) {
            __og.abort(var14);
         }

         boolean __ok = __og.invoke();
         if (!__og.is().isEmpty()) {
            try {
               if (!__ok) {
                  try {
                     __og.throwUserException();
                  } catch (UserException var12) {
                     throw new UnknownUserException(var12.ice_name());
                  }
               }

               __og.is().skipEmptyEncaps();
            } catch (LocalException var13) {
               throw new LocalExceptionWrapper(var13, false);
            }
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void sendAlertMessageToAllUsers(String message, String title, short timeout, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("sendAlertMessageToAllUsers", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(message);
            __os.writeString(title);
            __os.writeShort(timeout);
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
}
