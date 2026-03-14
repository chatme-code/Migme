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

public final class _BotChannelDelD extends _ObjectDelD implements _BotChannelDel {
   public void botKilled(final String botInstanceID, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "botKilled", OperationMode.Normal, __ctx);
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               BotChannel __servant = null;

               try {
                  __servant = (BotChannel)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __servant.botKilled(botInstanceID, __current);
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
      } catch (FusionException var13) {
         throw var13;
      } catch (SystemException var14) {
         throw var14;
      } catch (Throwable var15) {
         LocalExceptionWrapper.throwWrapper(var15);
      }

   }

   public String[] getParticipants(final String requestingUsername, Map<String, String> __ctx) throws LocalExceptionWrapper {
      final Current __current = new Current();
      this.__initCurrent(__current, "getParticipants", OperationMode.Normal, __ctx);
      final StringArrayHolder __result = new StringArrayHolder();
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               BotChannel __servant = null;

               try {
                  __servant = (BotChannel)__obj;
               } catch (ClassCastException var4) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               __result.value = __servant.getParticipants(requestingUsername, __current);
               return DispatchStatus.DispatchOK;
            }
         };

         String[] var7;
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
      } catch (SystemException var14) {
         throw var14;
      } catch (Throwable var15) {
         LocalExceptionWrapper.throwWrapper(var15);
         return __result.value;
      }
   }

   public boolean isParticipant(final String username, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "isParticipant", OperationMode.Normal, __ctx);
      final BooleanHolder __result = new BooleanHolder();
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               BotChannel __servant = null;

               try {
                  __servant = (BotChannel)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __result.value = __servant.isParticipant(username, __current);
                  return DispatchStatus.DispatchOK;
               } catch (UserException var4) {
                  this.setUserException(var4);
                  return DispatchStatus.DispatchUserException;
               }
            }
         };

         boolean var7;
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
      } catch (FusionException var15) {
         throw var15;
      } catch (SystemException var16) {
         throw var16;
      } catch (Throwable var17) {
         LocalExceptionWrapper.throwWrapper(var17);
         return __result.value;
      }
   }

   public void putBotMessage(final String botInstanceID, final String username, final String message, final String[] emoticonHotKeys, final boolean displayPopUp, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "putBotMessage", OperationMode.Normal, __ctx);
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               BotChannel __servant = null;

               try {
                  __servant = (BotChannel)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __servant.putBotMessage(botInstanceID, username, message, emoticonHotKeys, displayPopUp, __current);
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

   public void putBotMessageToAllUsers(final String botInstanceID, final String message, final String[] emoticonHotKeys, final boolean displayPopUp, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "putBotMessageToAllUsers", OperationMode.Normal, __ctx);
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               BotChannel __servant = null;

               try {
                  __servant = (BotChannel)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __servant.putBotMessageToAllUsers(botInstanceID, message, emoticonHotKeys, displayPopUp, __current);
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

   public void putBotMessageToUsers(final String botInstanceID, final String[] usernames, final String message, final String[] emoticonHotKeys, final boolean displayPopUp, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "putBotMessageToUsers", OperationMode.Normal, __ctx);
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               BotChannel __servant = null;

               try {
                  __servant = (BotChannel)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __servant.putBotMessageToUsers(botInstanceID, usernames, message, emoticonHotKeys, displayPopUp, __current);
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

   public void sendGamesHelpToUser(final String username, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "sendGamesHelpToUser", OperationMode.Normal, __ctx);
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               BotChannel __servant = null;

               try {
                  __servant = (BotChannel)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __servant.sendGamesHelpToUser(username, __current);
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
      } catch (FusionException var13) {
         throw var13;
      } catch (SystemException var14) {
         throw var14;
      } catch (Throwable var15) {
         LocalExceptionWrapper.throwWrapper(var15);
      }

   }

   public void sendMessageToBots(final String username, final String message, final long receivedTimestamp, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "sendMessageToBots", OperationMode.Normal, __ctx);
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               BotChannel __servant = null;

               try {
                  __servant = (BotChannel)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __servant.sendMessageToBots(username, message, receivedTimestamp, __current);
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

   public void startBot(final String username, final String botCommandName, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "startBot", OperationMode.Normal, __ctx);
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               BotChannel __servant = null;

               try {
                  __servant = (BotChannel)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __servant.startBot(username, botCommandName, __current);
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

   public void stopAllBots(final String username, final int timeout, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "stopAllBots", OperationMode.Normal, __ctx);
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               BotChannel __servant = null;

               try {
                  __servant = (BotChannel)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __servant.stopAllBots(username, timeout, __current);
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

   public void stopBot(final String username, final String botCommandName, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "stopBot", OperationMode.Normal, __ctx);
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               BotChannel __servant = null;

               try {
                  __servant = (BotChannel)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __servant.stopBot(username, botCommandName, __current);
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
}
