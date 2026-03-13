package com.projectgoth.fusion.slice;

import Ice.BooleanHolder;
import Ice.Current;
import Ice.DispatchStatus;
import Ice.Object;
import Ice.OperationMode;
import Ice.OperationNotExistException;
import Ice.SystemException;
import Ice.UserException;
import Ice._ObjectDelD;
import IceInternal.Direct;
import IceInternal.LocalExceptionWrapper;
import java.util.Map;

public final class _MessageSwitchboardDelD extends _ObjectDelD implements _MessageSwitchboardDel {
   public GroupChatPrx ensureGroupChatExists(final SessionPrx currentSession, final String groupChatID, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "ensureGroupChatExists", OperationMode.Normal, __ctx);
      final GroupChatPrxHolder __result = new GroupChatPrxHolder();
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               MessageSwitchboard __servant = null;

               try {
                  __servant = (MessageSwitchboard)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __result.value = __servant.ensureGroupChatExists(currentSession, groupChatID, __current);
                  return DispatchStatus.DispatchOK;
               } catch (UserException var4) {
                  this.setUserException(var4);
                  return DispatchStatus.DispatchUserException;
               }
            }
         };

         GroupChatPrx var8;
         try {
            DispatchStatus __status = __direct.servant().__collocDispatch(__direct);
            if (__status == DispatchStatus.DispatchUserException) {
               __direct.throwUserException();
            }

            assert __status == DispatchStatus.DispatchOK;

            var8 = __result.value;
         } finally {
            __direct.destroy();
         }

         return var8;
      } catch (FusionException var16) {
         throw var16;
      } catch (SystemException var17) {
         throw var17;
      } catch (Throwable var18) {
         LocalExceptionWrapper.throwWrapper(var18);
         return __result.value;
      }
   }

   public void getAndPushMessages(final String username, final byte chatType, final String suppliedChatID, final long oldestMessageTimestamp, final long newestMessageTimestamp, final int limit, final ConnectionPrx cxn, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "getAndPushMessages", OperationMode.Normal, __ctx);
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               MessageSwitchboard __servant = null;

               try {
                  __servant = (MessageSwitchboard)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __servant.getAndPushMessages(username, chatType, suppliedChatID, oldestMessageTimestamp, newestMessageTimestamp, limit, cxn, __current);
                  return DispatchStatus.DispatchOK;
               } catch (UserException var4) {
                  this.setUserException(var4);
                  return DispatchStatus.DispatchUserException;
               }
            }
         };

         try {
            DispatchStatus __status = __direct.servant().__collocDispatch(__direct);
            if (__status == DispatchStatus.DispatchUserException) {
               __direct.throwUserException();
            }

            assert __status == DispatchStatus.DispatchOK;
         } finally {
            __direct.destroy();
         }
      } catch (FusionException var21) {
         throw var21;
      } catch (SystemException var22) {
         throw var22;
      } catch (Throwable var23) {
         LocalExceptionWrapper.throwWrapper(var23);
      }

   }

   public void getAndPushMessages2(final String username, final byte chatType, final String suppliedChatID, final long oldestMessageTimestamp, final long newestMessageTimestamp, final int limit, final ConnectionPrx cxn, final int deviceType, final short clientVersion, final short fusionPktTransactionId, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "getAndPushMessages2", OperationMode.Normal, __ctx);
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               MessageSwitchboard __servant = null;

               try {
                  __servant = (MessageSwitchboard)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __servant.getAndPushMessages2(username, chatType, suppliedChatID, oldestMessageTimestamp, newestMessageTimestamp, limit, cxn, deviceType, clientVersion, fusionPktTransactionId, __current);
                  return DispatchStatus.DispatchOK;
               } catch (UserException var4) {
                  this.setUserException(var4);
                  return DispatchStatus.DispatchUserException;
               }
            }
         };

         try {
            DispatchStatus __status = __direct.servant().__collocDispatch(__direct);
            if (__status == DispatchStatus.DispatchUserException) {
               __direct.throwUserException();
            }

            assert __status == DispatchStatus.DispatchOK;
         } finally {
            __direct.destroy();
         }
      } catch (FusionException var24) {
         throw var24;
      } catch (SystemException var25) {
         throw var25;
      } catch (Throwable var26) {
         LocalExceptionWrapper.throwWrapper(var26);
      }

   }

   public ChatDefinitionIce[] getChats(final int userID, final int chatListVersion, final int limit, final byte chatType, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "getChats", OperationMode.Normal, __ctx);
      final ChatDefinitionIceArrayHolder __result = new ChatDefinitionIceArrayHolder();
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               MessageSwitchboard __servant = null;

               try {
                  __servant = (MessageSwitchboard)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __result.value = __servant.getChats(userID, chatListVersion, limit, chatType, __current);
                  return DispatchStatus.DispatchOK;
               } catch (UserException var4) {
                  this.setUserException(var4);
                  return DispatchStatus.DispatchUserException;
               }
            }
         };

         ChatDefinitionIce[] var10;
         try {
            DispatchStatus __status = __direct.servant().__collocDispatch(__direct);
            if (__status == DispatchStatus.DispatchUserException) {
               __direct.throwUserException();
            }

            assert __status == DispatchStatus.DispatchOK;

            var10 = __result.value;
         } finally {
            __direct.destroy();
         }

         return var10;
      } catch (FusionException var18) {
         throw var18;
      } catch (SystemException var19) {
         throw var19;
      } catch (Throwable var20) {
         LocalExceptionWrapper.throwWrapper(var20);
         return __result.value;
      }
   }

   public ChatDefinitionIce[] getChats2(final int userID, final int chatListVersion, final int limit, final byte chatType, final ConnectionPrx cxn, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "getChats2", OperationMode.Normal, __ctx);
      final ChatDefinitionIceArrayHolder __result = new ChatDefinitionIceArrayHolder();
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               MessageSwitchboard __servant = null;

               try {
                  __servant = (MessageSwitchboard)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __result.value = __servant.getChats2(userID, chatListVersion, limit, chatType, cxn, __current);
                  return DispatchStatus.DispatchOK;
               } catch (UserException var4) {
                  this.setUserException(var4);
                  return DispatchStatus.DispatchUserException;
               }
            }
         };

         ChatDefinitionIce[] var11;
         try {
            DispatchStatus __status = __direct.servant().__collocDispatch(__direct);
            if (__status == DispatchStatus.DispatchUserException) {
               __direct.throwUserException();
            }

            assert __status == DispatchStatus.DispatchOK;

            var11 = __result.value;
         } finally {
            __direct.destroy();
         }

         return var11;
      } catch (FusionException var19) {
         throw var19;
      } catch (SystemException var20) {
         throw var20;
      } catch (Throwable var21) {
         LocalExceptionWrapper.throwWrapper(var21);
         return __result.value;
      }
   }

   public boolean isUserChatSyncEnabled(final ConnectionPrx cxn, final String username, final int userID, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "isUserChatSyncEnabled", OperationMode.Normal, __ctx);
      final BooleanHolder __result = new BooleanHolder();
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               MessageSwitchboard __servant = null;

               try {
                  __servant = (MessageSwitchboard)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __result.value = __servant.isUserChatSyncEnabled(cxn, username, userID, __current);
                  return DispatchStatus.DispatchOK;
               } catch (UserException var4) {
                  this.setUserException(var4);
                  return DispatchStatus.DispatchUserException;
               }
            }
         };

         boolean var9;
         try {
            DispatchStatus __status = __direct.servant().__collocDispatch(__direct);
            if (__status == DispatchStatus.DispatchUserException) {
               __direct.throwUserException();
            }

            assert __status == DispatchStatus.DispatchOK;

            var9 = __result.value;
         } finally {
            __direct.destroy();
         }

         return var9;
      } catch (FusionException var17) {
         throw var17;
      } catch (SystemException var18) {
         throw var18;
      } catch (Throwable var19) {
         LocalExceptionWrapper.throwWrapper(var19);
         return __result.value;
      }
   }

   public void onCreateGroupChat(final ChatDefinitionIce storedGroupChat, final String creatorUsername, final String privateChatPartnerUsername, final GroupChatPrx groupChatRemote, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "onCreateGroupChat", OperationMode.Normal, __ctx);
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               MessageSwitchboard __servant = null;

               try {
                  __servant = (MessageSwitchboard)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __servant.onCreateGroupChat(storedGroupChat, creatorUsername, privateChatPartnerUsername, groupChatRemote, __current);
                  return DispatchStatus.DispatchOK;
               } catch (UserException var4) {
                  this.setUserException(var4);
                  return DispatchStatus.DispatchUserException;
               }
            }
         };

         try {
            DispatchStatus __status = __direct.servant().__collocDispatch(__direct);
            if (__status == DispatchStatus.DispatchUserException) {
               __direct.throwUserException();
            }

            assert __status == DispatchStatus.DispatchOK;
         } finally {
            __direct.destroy();
         }
      } catch (FusionException var16) {
         throw var16;
      } catch (SystemException var17) {
         throw var17;
      } catch (Throwable var18) {
         LocalExceptionWrapper.throwWrapper(var18);
      }

   }

   public void onCreatePrivateChat(final int userID, final String username, final String otherUser, final int deviceType, final short clientVersion, final UserDataIce senderUserData, final String recipientDisplayPicture, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "onCreatePrivateChat", OperationMode.Normal, __ctx);
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               MessageSwitchboard __servant = null;

               try {
                  __servant = (MessageSwitchboard)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __servant.onCreatePrivateChat(userID, username, otherUser, deviceType, clientVersion, senderUserData, recipientDisplayPicture, __current);
                  return DispatchStatus.DispatchOK;
               } catch (UserException var4) {
                  this.setUserException(var4);
                  return DispatchStatus.DispatchUserException;
               }
            }
         };

         try {
            DispatchStatus __status = __direct.servant().__collocDispatch(__direct);
            if (__status == DispatchStatus.DispatchUserException) {
               __direct.throwUserException();
            }

            assert __status == DispatchStatus.DispatchOK;
         } finally {
            __direct.destroy();
         }
      } catch (FusionException var19) {
         throw var19;
      } catch (SystemException var20) {
         throw var20;
      } catch (Throwable var21) {
         LocalExceptionWrapper.throwWrapper(var21);
      }

   }

   public void onGetChats(final ConnectionPrx cxn, final int userID, final int chatListVersion, final int limit, final byte chatType, final short transactionId, final String parentUsername, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "onGetChats", OperationMode.Normal, __ctx);
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               MessageSwitchboard __servant = null;

               try {
                  __servant = (MessageSwitchboard)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __servant.onGetChats(cxn, userID, chatListVersion, limit, chatType, transactionId, parentUsername, __current);
                  return DispatchStatus.DispatchOK;
               } catch (UserException var4) {
                  this.setUserException(var4);
                  return DispatchStatus.DispatchUserException;
               }
            }
         };

         try {
            DispatchStatus __status = __direct.servant().__collocDispatch(__direct);
            if (__status == DispatchStatus.DispatchUserException) {
               __direct.throwUserException();
            }

            assert __status == DispatchStatus.DispatchOK;
         } finally {
            __direct.destroy();
         }
      } catch (FusionException var19) {
         throw var19;
      } catch (SystemException var20) {
         throw var20;
      } catch (Throwable var21) {
         LocalExceptionWrapper.throwWrapper(var21);
      }

   }

   public void onJoinChatRoom(final String username, final int userID, final String chatRoomName, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "onJoinChatRoom", OperationMode.Normal, __ctx);
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               MessageSwitchboard __servant = null;

               try {
                  __servant = (MessageSwitchboard)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __servant.onJoinChatRoom(username, userID, chatRoomName, __current);
                  return DispatchStatus.DispatchOK;
               } catch (UserException var4) {
                  this.setUserException(var4);
                  return DispatchStatus.DispatchUserException;
               }
            }
         };

         try {
            DispatchStatus __status = __direct.servant().__collocDispatch(__direct);
            if (__status == DispatchStatus.DispatchUserException) {
               __direct.throwUserException();
            }

            assert __status == DispatchStatus.DispatchOK;
         } finally {
            __direct.destroy();
         }
      } catch (FusionException var15) {
         throw var15;
      } catch (SystemException var16) {
         throw var16;
      } catch (Throwable var17) {
         LocalExceptionWrapper.throwWrapper(var17);
      }

   }

   public void onJoinGroupChat(final String username, final int userID, final String groupChatGUID, final boolean debug, final UserPrx userProxy, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "onJoinGroupChat", OperationMode.Normal, __ctx);
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               MessageSwitchboard __servant = null;

               try {
                  __servant = (MessageSwitchboard)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __servant.onJoinGroupChat(username, userID, groupChatGUID, debug, userProxy, __current);
                  return DispatchStatus.DispatchOK;
               } catch (UserException var4) {
                  this.setUserException(var4);
                  return DispatchStatus.DispatchUserException;
               }
            }
         };

         try {
            DispatchStatus __status = __direct.servant().__collocDispatch(__direct);
            if (__status == DispatchStatus.DispatchUserException) {
               __direct.throwUserException();
            }

            assert __status == DispatchStatus.DispatchOK;
         } finally {
            __direct.destroy();
         }
      } catch (FusionException var17) {
         throw var17;
      } catch (SystemException var18) {
         throw var18;
      } catch (Throwable var19) {
         LocalExceptionWrapper.throwWrapper(var19);
      }

   }

   public void onLeaveChatRoom(final String username, final int userID, final String chatRoomName, final UserPrx userProxy, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "onLeaveChatRoom", OperationMode.Normal, __ctx);
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               MessageSwitchboard __servant = null;

               try {
                  __servant = (MessageSwitchboard)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __servant.onLeaveChatRoom(username, userID, chatRoomName, userProxy, __current);
                  return DispatchStatus.DispatchOK;
               } catch (UserException var4) {
                  this.setUserException(var4);
                  return DispatchStatus.DispatchUserException;
               }
            }
         };

         try {
            DispatchStatus __status = __direct.servant().__collocDispatch(__direct);
            if (__status == DispatchStatus.DispatchUserException) {
               __direct.throwUserException();
            }

            assert __status == DispatchStatus.DispatchOK;
         } finally {
            __direct.destroy();
         }
      } catch (FusionException var16) {
         throw var16;
      } catch (SystemException var17) {
         throw var17;
      } catch (Throwable var18) {
         LocalExceptionWrapper.throwWrapper(var18);
      }

   }

   public void onLeaveGroupChat(final String username, final int userID, final String groupChatGUID, final UserPrx userProxy, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "onLeaveGroupChat", OperationMode.Normal, __ctx);
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               MessageSwitchboard __servant = null;

               try {
                  __servant = (MessageSwitchboard)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __servant.onLeaveGroupChat(username, userID, groupChatGUID, userProxy, __current);
                  return DispatchStatus.DispatchOK;
               } catch (UserException var4) {
                  this.setUserException(var4);
                  return DispatchStatus.DispatchUserException;
               }
            }
         };

         try {
            DispatchStatus __status = __direct.servant().__collocDispatch(__direct);
            if (__status == DispatchStatus.DispatchUserException) {
               __direct.throwUserException();
            }

            assert __status == DispatchStatus.DispatchOK;
         } finally {
            __direct.destroy();
         }
      } catch (FusionException var16) {
         throw var16;
      } catch (SystemException var17) {
         throw var17;
      } catch (Throwable var18) {
         LocalExceptionWrapper.throwWrapper(var18);
      }

   }

   public void onLeavePrivateChat(final int userID, final String username, final String otherUser, final int deviceType, final short clientVersion, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "onLeavePrivateChat", OperationMode.Normal, __ctx);
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               MessageSwitchboard __servant = null;

               try {
                  __servant = (MessageSwitchboard)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __servant.onLeavePrivateChat(userID, username, otherUser, deviceType, clientVersion, __current);
                  return DispatchStatus.DispatchOK;
               } catch (UserException var4) {
                  this.setUserException(var4);
                  return DispatchStatus.DispatchUserException;
               }
            }
         };

         try {
            DispatchStatus __status = __direct.servant().__collocDispatch(__direct);
            if (__status == DispatchStatus.DispatchUserException) {
               __direct.throwUserException();
            }

            assert __status == DispatchStatus.DispatchOK;
         } finally {
            __direct.destroy();
         }
      } catch (FusionException var17) {
         throw var17;
      } catch (SystemException var18) {
         throw var18;
      } catch (Throwable var19) {
         LocalExceptionWrapper.throwWrapper(var19);
      }

   }

   public void onLogon(final int userID, final SessionPrx sess, final short transactionID, final String parentUsername, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "onLogon", OperationMode.Normal, __ctx);
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               MessageSwitchboard __servant = null;

               try {
                  __servant = (MessageSwitchboard)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __servant.onLogon(userID, sess, transactionID, parentUsername, __current);
                  return DispatchStatus.DispatchOK;
               } catch (UserException var4) {
                  this.setUserException(var4);
                  return DispatchStatus.DispatchUserException;
               }
            }
         };

         try {
            DispatchStatus __status = __direct.servant().__collocDispatch(__direct);
            if (__status == DispatchStatus.DispatchUserException) {
               __direct.throwUserException();
            }

            assert __status == DispatchStatus.DispatchOK;
         } finally {
            __direct.destroy();
         }
      } catch (FusionException var16) {
         throw var16;
      } catch (SystemException var17) {
         throw var17;
      } catch (Throwable var18) {
         LocalExceptionWrapper.throwWrapper(var18);
      }

   }

   public void onSendFusionMessageToChatRoom(final SessionPrx currentSession, final UserPrx parentUser, final MessageDataIce messageData, final String chatRoomName, final int deviceType, final short clientVersion, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "onSendFusionMessageToChatRoom", OperationMode.Normal, __ctx);
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               MessageSwitchboard __servant = null;

               try {
                  __servant = (MessageSwitchboard)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __servant.onSendFusionMessageToChatRoom(currentSession, parentUser, messageData, chatRoomName, deviceType, clientVersion, __current);
                  return DispatchStatus.DispatchOK;
               } catch (UserException var4) {
                  this.setUserException(var4);
                  return DispatchStatus.DispatchUserException;
               }
            }
         };

         try {
            DispatchStatus __status = __direct.servant().__collocDispatch(__direct);
            if (__status == DispatchStatus.DispatchUserException) {
               __direct.throwUserException();
            }

            assert __status == DispatchStatus.DispatchOK;
         } finally {
            __direct.destroy();
         }
      } catch (FusionException var18) {
         throw var18;
      } catch (SystemException var19) {
         throw var19;
      } catch (Throwable var20) {
         LocalExceptionWrapper.throwWrapper(var20);
      }

   }

   public void onSendFusionMessageToGroupChat(final SessionPrx currentSession, final UserPrx parentUser, final MessageDataIce messageData, final String groupChatID, final int deviceType, final short clientVersion, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "onSendFusionMessageToGroupChat", OperationMode.Normal, __ctx);
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               MessageSwitchboard __servant = null;

               try {
                  __servant = (MessageSwitchboard)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __servant.onSendFusionMessageToGroupChat(currentSession, parentUser, messageData, groupChatID, deviceType, clientVersion, __current);
                  return DispatchStatus.DispatchOK;
               } catch (UserException var4) {
                  this.setUserException(var4);
                  return DispatchStatus.DispatchUserException;
               }
            }
         };

         try {
            DispatchStatus __status = __direct.servant().__collocDispatch(__direct);
            if (__status == DispatchStatus.DispatchUserException) {
               __direct.throwUserException();
            }

            assert __status == DispatchStatus.DispatchOK;
         } finally {
            __direct.destroy();
         }
      } catch (FusionException var18) {
         throw var18;
      } catch (SystemException var19) {
         throw var19;
      } catch (Throwable var20) {
         LocalExceptionWrapper.throwWrapper(var20);
      }

   }

   public boolean onSendFusionMessageToIndividual(final SessionPrx currentSession, final UserPrx parentUser, final MessageDataIce messageData, final String destinationUsername, final String[] uniqueUsersPrivateChattedWith, final int deviceType, final short clientVersion, final UserDataIce senderUserData, final String recipientDisplayPicture, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "onSendFusionMessageToIndividual", OperationMode.Normal, __ctx);
      final BooleanHolder __result = new BooleanHolder();
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               MessageSwitchboard __servant = null;

               try {
                  __servant = (MessageSwitchboard)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __result.value = __servant.onSendFusionMessageToIndividual(currentSession, parentUser, messageData, destinationUsername, uniqueUsersPrivateChattedWith, deviceType, clientVersion, senderUserData, recipientDisplayPicture, __current);
                  return DispatchStatus.DispatchOK;
               } catch (UserException var4) {
                  this.setUserException(var4);
                  return DispatchStatus.DispatchUserException;
               }
            }
         };

         boolean var15;
         try {
            DispatchStatus __status = __direct.servant().__collocDispatch(__direct);
            if (__status == DispatchStatus.DispatchUserException) {
               __direct.throwUserException();
            }

            assert __status == DispatchStatus.DispatchOK;

            var15 = __result.value;
         } finally {
            __direct.destroy();
         }

         return var15;
      } catch (FusionException var23) {
         throw var23;
      } catch (SystemException var24) {
         throw var24;
      } catch (Throwable var25) {
         LocalExceptionWrapper.throwWrapper(var25);
         return __result.value;
      }
   }

   public boolean onSendMessageToAllUsersInChat(final SessionPrx currentSession, final UserPrx parentUser, final MessageDataIce messageData, final UserDataIce senderUserData, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "onSendMessageToAllUsersInChat", OperationMode.Normal, __ctx);
      final BooleanHolder __result = new BooleanHolder();
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               MessageSwitchboard __servant = null;

               try {
                  __servant = (MessageSwitchboard)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __result.value = __servant.onSendMessageToAllUsersInChat(currentSession, parentUser, messageData, senderUserData, __current);
                  return DispatchStatus.DispatchOK;
               } catch (UserException var4) {
                  this.setUserException(var4);
                  return DispatchStatus.DispatchUserException;
               }
            }
         };

         boolean var10;
         try {
            DispatchStatus __status = __direct.servant().__collocDispatch(__direct);
            if (__status == DispatchStatus.DispatchUserException) {
               __direct.throwUserException();
            }

            assert __status == DispatchStatus.DispatchOK;

            var10 = __result.value;
         } finally {
            __direct.destroy();
         }

         return var10;
      } catch (FusionException var18) {
         throw var18;
      } catch (SystemException var19) {
         throw var19;
      } catch (Throwable var20) {
         LocalExceptionWrapper.throwWrapper(var20);
         return __result.value;
      }
   }

   public void setChatName(final String parentUsername, final String suppliedChatID, final byte chatType, final String chatName, final RegistryPrx regy, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "setChatName", OperationMode.Normal, __ctx);
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               MessageSwitchboard __servant = null;

               try {
                  __servant = (MessageSwitchboard)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __servant.setChatName(parentUsername, suppliedChatID, chatType, chatName, regy, __current);
                  return DispatchStatus.DispatchOK;
               } catch (UserException var4) {
                  this.setUserException(var4);
                  return DispatchStatus.DispatchUserException;
               }
            }
         };

         try {
            DispatchStatus __status = __direct.servant().__collocDispatch(__direct);
            if (__status == DispatchStatus.DispatchUserException) {
               __direct.throwUserException();
            }

            assert __status == DispatchStatus.DispatchOK;
         } finally {
            __direct.destroy();
         }
      } catch (FusionException var17) {
         throw var17;
      } catch (SystemException var18) {
         throw var18;
      } catch (Throwable var19) {
         LocalExceptionWrapper.throwWrapper(var19);
      }

   }
}
