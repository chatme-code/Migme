package com.projectgoth.fusion.slice;

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

public final class _BotServiceDelD extends _ObjectDelD implements _BotServiceDel {
   public BotInstance addBotToChannel(final BotChannelPrx channelProxy, final String botCommandName, final String starterUsername, final boolean purgeIfIdle, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "addBotToChannel", OperationMode.Normal, __ctx);
      final BotInstanceHolder __result = new BotInstanceHolder();
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               BotService __servant = null;

               try {
                  __servant = (BotService)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __result.value = __servant.addBotToChannel(channelProxy, botCommandName, starterUsername, purgeIfIdle, __current);
                  return DispatchStatus.DispatchOK;
               } catch (UserException var4) {
                  this.setUserException(var4);
                  return DispatchStatus.DispatchUserException;
               }
            }
         };

         BotInstance var10;
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

   public void removeBot(final String botInstanceID, final boolean stopEvenIfGameInProgress, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "removeBot", OperationMode.Normal, __ctx);
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               BotService __servant = null;

               try {
                  __servant = (BotService)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __servant.removeBot(botInstanceID, stopEvenIfGameInProgress, __current);
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
      } catch (FusionException var14) {
         throw var14;
      } catch (SystemException var15) {
         throw var15;
      } catch (Throwable var16) {
         LocalExceptionWrapper.throwWrapper(var16);
      }

   }

   public void sendMessageToBot(final String botInstanceID, final String username, final String message, final long receivedTimestamp, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "sendMessageToBot", OperationMode.Normal, __ctx);
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               BotService __servant = null;

               try {
                  __servant = (BotService)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __servant.sendMessageToBot(botInstanceID, username, message, receivedTimestamp, __current);
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

   public void sendMessageToBotsInChannel(final String channelID, final String username, final String message, final long receivedTimestamp, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "sendMessageToBotsInChannel", OperationMode.Normal, __ctx);
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               BotService __servant = null;

               try {
                  __servant = (BotService)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __servant.sendMessageToBotsInChannel(channelID, username, message, receivedTimestamp, __current);
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

   public void sendNotificationToBotsInChannel(final String channelID, final String username, final int notification, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "sendNotificationToBotsInChannel", OperationMode.Normal, __ctx);
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               BotService __servant = null;

               try {
                  __servant = (BotService)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __servant.sendNotificationToBotsInChannel(channelID, username, notification, __current);
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
