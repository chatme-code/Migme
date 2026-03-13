package com.projectgoth.fusion.slice;

import Ice.CollocationOptimizationException;
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

public final class _ObjectCacheDelD extends _ObjectDelD implements _ObjectCacheDel {
   public ChatRoomPrx createChatRoomObject(final String name, Map<String, String> __ctx) throws LocalExceptionWrapper, ObjectExistsException, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "createChatRoomObject", OperationMode.Normal, __ctx);
      final ChatRoomPrxHolder __result = new ChatRoomPrxHolder();
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               ObjectCache __servant = null;

               try {
                  __servant = (ObjectCache)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __result.value = __servant.createChatRoomObject(name, __current);
                  return DispatchStatus.DispatchOK;
               } catch (UserException var4) {
                  this.setUserException(var4);
                  return DispatchStatus.DispatchUserException;
               }
            }
         };

         ChatRoomPrx var7;
         try {
            DispatchStatus __status = __direct.servant().__collocDispatch(__direct);
            if (__status == DispatchStatus.DispatchUserException) {
               __direct.throwUserException();
            }

            assert __status == DispatchStatus.DispatchOK;

            var7 = __result.value;
         } finally {
            __direct.destroy();
         }

         return var7;
      } catch (ObjectExistsException var16) {
         throw var16;
      } catch (FusionException var17) {
         throw var17;
      } catch (SystemException var18) {
         throw var18;
      } catch (Throwable var19) {
         LocalExceptionWrapper.throwWrapper(var19);
         return __result.value;
      }
   }

   public GroupChatPrx createGroupChatObject(final String id, final String creator, final String privateChatPartner, final String[] otherPartyList, Map<String, String> __ctx) throws LocalExceptionWrapper, ObjectExistsException, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "createGroupChatObject", OperationMode.Normal, __ctx);
      final GroupChatPrxHolder __result = new GroupChatPrxHolder();
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               ObjectCache __servant = null;

               try {
                  __servant = (ObjectCache)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __result.value = __servant.createGroupChatObject(id, creator, privateChatPartner, otherPartyList, __current);
                  return DispatchStatus.DispatchOK;
               } catch (UserException var4) {
                  this.setUserException(var4);
                  return DispatchStatus.DispatchUserException;
               }
            }
         };

         GroupChatPrx var10;
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
      } catch (ObjectExistsException var19) {
         throw var19;
      } catch (FusionException var20) {
         throw var20;
      } catch (SystemException var21) {
         throw var21;
      } catch (Throwable var22) {
         LocalExceptionWrapper.throwWrapper(var22);
         return __result.value;
      }
   }

   public UserPrx createUserObject(String username, Map<String, String> __ctx) throws LocalExceptionWrapper, ObjectExistsException, FusionException {
      throw new CollocationOptimizationException();
   }

   public UserPrx createUserObjectNonAsync(final String username, Map<String, String> __ctx) throws LocalExceptionWrapper, ObjectExistsException, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "createUserObjectNonAsync", OperationMode.Normal, __ctx);
      final UserPrxHolder __result = new UserPrxHolder();
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               ObjectCache __servant = null;

               try {
                  __servant = (ObjectCache)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __result.value = __servant.createUserObjectNonAsync(username, __current);
                  return DispatchStatus.DispatchOK;
               } catch (UserException var4) {
                  this.setUserException(var4);
                  return DispatchStatus.DispatchUserException;
               }
            }
         };

         UserPrx var7;
         try {
            DispatchStatus __status = __direct.servant().__collocDispatch(__direct);
            if (__status == DispatchStatus.DispatchUserException) {
               __direct.throwUserException();
            }

            assert __status == DispatchStatus.DispatchOK;

            var7 = __result.value;
         } finally {
            __direct.destroy();
         }

         return var7;
      } catch (ObjectExistsException var16) {
         throw var16;
      } catch (FusionException var17) {
         throw var17;
      } catch (SystemException var18) {
         throw var18;
      } catch (Throwable var19) {
         LocalExceptionWrapper.throwWrapper(var19);
         return __result.value;
      }
   }

   public GroupChatPrx[] getAllGroupChats(Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "getAllGroupChats", OperationMode.Normal, __ctx);
      final GroupChatArrayHolder __result = new GroupChatArrayHolder();
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               ObjectCache __servant = null;

               try {
                  __servant = (ObjectCache)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __result.value = __servant.getAllGroupChats(__current);
                  return DispatchStatus.DispatchOK;
               } catch (UserException var4) {
                  this.setUserException(var4);
                  return DispatchStatus.DispatchUserException;
               }
            }
         };

         GroupChatPrx[] var6;
         try {
            DispatchStatus __status = __direct.servant().__collocDispatch(__direct);
            if (__status == DispatchStatus.DispatchUserException) {
               __direct.throwUserException();
            }

            assert __status == DispatchStatus.DispatchOK;

            var6 = __result.value;
         } finally {
            __direct.destroy();
         }

         return var6;
      } catch (FusionException var14) {
         throw var14;
      } catch (SystemException var15) {
         throw var15;
      } catch (Throwable var16) {
         LocalExceptionWrapper.throwWrapper(var16);
         return __result.value;
      }
   }

   public MessageSwitchboardPrx getMessageSwitchboard(Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "getMessageSwitchboard", OperationMode.Normal, __ctx);
      final MessageSwitchboardPrxHolder __result = new MessageSwitchboardPrxHolder();
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               ObjectCache __servant = null;

               try {
                  __servant = (ObjectCache)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __result.value = __servant.getMessageSwitchboard(__current);
                  return DispatchStatus.DispatchOK;
               } catch (UserException var4) {
                  this.setUserException(var4);
                  return DispatchStatus.DispatchUserException;
               }
            }
         };

         MessageSwitchboardPrx var6;
         try {
            DispatchStatus __status = __direct.servant().__collocDispatch(__direct);
            if (__status == DispatchStatus.DispatchUserException) {
               __direct.throwUserException();
            }

            assert __status == DispatchStatus.DispatchOK;

            var6 = __result.value;
         } finally {
            __direct.destroy();
         }

         return var6;
      } catch (FusionException var14) {
         throw var14;
      } catch (SystemException var15) {
         throw var15;
      } catch (Throwable var16) {
         LocalExceptionWrapper.throwWrapper(var16);
         return __result.value;
      }
   }

   public void purgeGroupChatObject(final String id, Map<String, String> __ctx) throws LocalExceptionWrapper {
      final Current __current = new Current();
      this.__initCurrent(__current, "purgeGroupChatObject", OperationMode.Normal, __ctx);
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               ObjectCache __servant = null;

               try {
                  __servant = (ObjectCache)__obj;
               } catch (ClassCastException var4) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               __servant.purgeGroupChatObject(id, __current);
               return DispatchStatus.DispatchOK;
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
      } catch (SystemException var12) {
         throw var12;
      } catch (Throwable var13) {
         LocalExceptionWrapper.throwWrapper(var13);
      }

   }

   public void purgeUserObject(final String username, Map<String, String> __ctx) throws LocalExceptionWrapper {
      final Current __current = new Current();
      this.__initCurrent(__current, "purgeUserObject", OperationMode.Normal, __ctx);
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               ObjectCache __servant = null;

               try {
                  __servant = (ObjectCache)__obj;
               } catch (ClassCastException var4) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               __servant.purgeUserObject(username, __current);
               return DispatchStatus.DispatchOK;
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
      } catch (SystemException var12) {
         throw var12;
      } catch (Throwable var13) {
         LocalExceptionWrapper.throwWrapper(var13);
      }

   }

   public void sendAlertMessageToAllUsers(final String message, final String title, final short timeout, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "sendAlertMessageToAllUsers", OperationMode.Normal, __ctx);
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               ObjectCache __servant = null;

               try {
                  __servant = (ObjectCache)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __servant.sendAlertMessageToAllUsers(message, title, timeout, __current);
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
}
