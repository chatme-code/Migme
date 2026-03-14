package com.projectgoth.fusion.slice;

import Ice.BooleanHolder;
import Ice.Current;
import Ice.DispatchStatus;
import Ice.IntHolder;
import Ice.Object;
import Ice.OperationMode;
import Ice.OperationNotExistException;
import Ice.SystemException;
import Ice.UserException;
import Ice._ObjectDelD;
import IceInternal.Direct;
import IceInternal.LocalExceptionWrapper;
import java.util.Map;

public final class _ChatRoomDelD extends _ObjectDelD implements _ChatRoomDel {
   public void botKilled(final String botInstanceID, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "botKilled", OperationMode.Normal, __ctx);
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               ChatRoom __servant = null;

               try {
                  __servant = (ChatRoom)__obj;
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
               ChatRoom __servant = null;

               try {
                  __servant = (ChatRoom)__obj;
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
               ChatRoom __servant = null;

               try {
                  __servant = (ChatRoom)__obj;
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
               ChatRoom __servant = null;

               try {
                  __servant = (ChatRoom)__obj;
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
               ChatRoom __servant = null;

               try {
                  __servant = (ChatRoom)__obj;
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
               ChatRoom __servant = null;

               try {
                  __servant = (ChatRoom)__obj;
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
               ChatRoom __servant = null;

               try {
                  __servant = (ChatRoom)__obj;
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
               ChatRoom __servant = null;

               try {
                  __servant = (ChatRoom)__obj;
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
               ChatRoom __servant = null;

               try {
                  __servant = (ChatRoom)__obj;
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
               ChatRoom __servant = null;

               try {
                  __servant = (ChatRoom)__obj;
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
               ChatRoom __servant = null;

               try {
                  __servant = (ChatRoom)__obj;
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

   public void addGroupModerator(final String instigator, final String targetUser, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "addGroupModerator", OperationMode.Normal, __ctx);
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               ChatRoom __servant = null;

               try {
                  __servant = (ChatRoom)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __servant.addGroupModerator(instigator, targetUser, __current);
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

   public void addModerator(final String username, Map<String, String> __ctx) throws LocalExceptionWrapper {
      final Current __current = new Current();
      this.__initCurrent(__current, "addModerator", OperationMode.Normal, __ctx);
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               ChatRoom __servant = null;

               try {
                  __servant = (ChatRoom)__obj;
               } catch (ClassCastException var4) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               __servant.addModerator(username, __current);
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

   public void addParticipant(final UserPrx userProxy, final UserDataIce userData, final SessionPrx sessionProxy, final String sessionID, final String ipAddress, final String mobileDevice, final String userAgent, final short clientVersion, final int deviceType, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "addParticipant", OperationMode.Normal, __ctx);
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               ChatRoom __servant = null;

               try {
                  __servant = (ChatRoom)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __servant.addParticipant(userProxy, userData, sessionProxy, sessionID, ipAddress, mobileDevice, userAgent, clientVersion, deviceType, __current);
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

   public void addParticipantOld(final UserPrx userProxy, final UserDataIce userData, final SessionPrx sessionProxy, final String sessionID, final String ipAddress, final String mobileDevice, final String userAgent, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "addParticipantOld", OperationMode.Normal, __ctx);
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               ChatRoom __servant = null;

               try {
                  __servant = (ChatRoom)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __servant.addParticipantOld(userProxy, userData, sessionProxy, sessionID, ipAddress, mobileDevice, userAgent, __current);
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

   public void adminAnnounce(final String announceMessage, final int waitTime, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "adminAnnounce", OperationMode.Normal, __ctx);
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               ChatRoom __servant = null;

               try {
                  __servant = (ChatRoom)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __servant.adminAnnounce(announceMessage, waitTime, __current);
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

   public void announceOff(final String announcer, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "announceOff", OperationMode.Normal, __ctx);
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               ChatRoom __servant = null;

               try {
                  __servant = (ChatRoom)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __servant.announceOff(announcer, __current);
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

   public void announceOn(final String announcer, final String announceMessage, final int waitTime, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "announceOn", OperationMode.Normal, __ctx);
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               ChatRoom __servant = null;

               try {
                  __servant = (ChatRoom)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __servant.announceOn(announcer, announceMessage, waitTime, __current);
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

   public void banGroupMembers(final String[] banList, final String instigator, final int reasonCode, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "banGroupMembers", OperationMode.Normal, __ctx);
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               ChatRoom __servant = null;

               try {
                  __servant = (ChatRoom)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __servant.banGroupMembers(banList, instigator, reasonCode, __current);
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

   public void banIndexes(final int[] indexes, final String bannedBy, final int reasonCode, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "banIndexes", OperationMode.Normal, __ctx);
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               ChatRoom __servant = null;

               try {
                  __servant = (ChatRoom)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __servant.banIndexes(indexes, bannedBy, reasonCode, __current);
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

   public void banMultiIds(final String username, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "banMultiIds", OperationMode.Normal, __ctx);
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               ChatRoom __servant = null;

               try {
                  __servant = (ChatRoom)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __servant.banMultiIds(username, __current);
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

   public void banUser(final String username, Map<String, String> __ctx) throws LocalExceptionWrapper {
      final Current __current = new Current();
      this.__initCurrent(__current, "banUser", OperationMode.Normal, __ctx);
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               ChatRoom __servant = null;

               try {
                  __servant = (ChatRoom)__obj;
               } catch (ClassCastException var4) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               __servant.banUser(username, __current);
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

   public void broadcastMessage(final String instigator, final String message, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "broadcastMessage", OperationMode.Normal, __ctx);
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               ChatRoom __servant = null;

               try {
                  __servant = (ChatRoom)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __servant.broadcastMessage(instigator, message, __current);
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

   public void bumpUser(final String instigator, final String target, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "bumpUser", OperationMode.Normal, __ctx);
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               ChatRoom __servant = null;

               try {
                  __servant = (ChatRoom)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __servant.bumpUser(instigator, target, __current);
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

   public void changeOwner(final String oldOwnerUsername, final String newOwnerUsername, Map<String, String> __ctx) throws LocalExceptionWrapper {
      final Current __current = new Current();
      this.__initCurrent(__current, "changeOwner", OperationMode.Normal, __ctx);
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               ChatRoom __servant = null;

               try {
                  __servant = (ChatRoom)__obj;
               } catch (ClassCastException var4) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               __servant.changeOwner(oldOwnerUsername, newOwnerUsername, __current);
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
      } catch (SystemException var13) {
         throw var13;
      } catch (Throwable var14) {
         LocalExceptionWrapper.throwWrapper(var14);
      }

   }

   public void clearUserKick(final String instigator, final String target, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "clearUserKick", OperationMode.Normal, __ctx);
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               ChatRoom __servant = null;

               try {
                  __servant = (ChatRoom)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __servant.clearUserKick(instigator, target, __current);
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

   public void convertIntoGroupChatRoom(final int groupID, final String groupName, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "convertIntoGroupChatRoom", OperationMode.Normal, __ctx);
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               ChatRoom __servant = null;

               try {
                  __servant = (ChatRoom)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __servant.convertIntoGroupChatRoom(groupID, groupName, __current);
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

   public void convertIntoUserOwnedChatRoom(Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "convertIntoUserOwnedChatRoom", OperationMode.Normal, __ctx);
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               ChatRoom __servant = null;

               try {
                  __servant = (ChatRoom)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __servant.convertIntoUserOwnedChatRoom(__current);
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
      } catch (FusionException var12) {
         throw var12;
      } catch (SystemException var13) {
         throw var13;
      } catch (Throwable var14) {
         LocalExceptionWrapper.throwWrapper(var14);
      }

   }

   public int executeEmoteCommandWithState(final String emoteCommand, final MessageDataIce message, final SessionPrx sessionProxy, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "executeEmoteCommandWithState", OperationMode.Normal, __ctx);
      final IntHolder __result = new IntHolder();
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               ChatRoom __servant = null;

               try {
                  __servant = (ChatRoom)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __result.value = __servant.executeEmoteCommandWithState(emoteCommand, message, sessionProxy, __current);
                  return DispatchStatus.DispatchOK;
               } catch (UserException var4) {
                  this.setUserException(var4);
                  return DispatchStatus.DispatchUserException;
               }
            }
         };

         int var9;
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

   public String[] getAdministrators(final String requestingUsername, Map<String, String> __ctx) throws LocalExceptionWrapper {
      final Current __current = new Current();
      this.__initCurrent(__current, "getAdministrators", OperationMode.Normal, __ctx);
      final StringArrayHolder __result = new StringArrayHolder();
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               ChatRoom __servant = null;

               try {
                  __servant = (ChatRoom)__obj;
               } catch (ClassCastException var4) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               __result.value = __servant.getAdministrators(requestingUsername, __current);
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

   public String[] getAllParticipants(final String requestingUsername, Map<String, String> __ctx) throws LocalExceptionWrapper {
      final Current __current = new Current();
      this.__initCurrent(__current, "getAllParticipants", OperationMode.Normal, __ctx);
      final StringArrayHolder __result = new StringArrayHolder();
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               ChatRoom __servant = null;

               try {
                  __servant = (ChatRoom)__obj;
               } catch (ClassCastException var4) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               __result.value = __servant.getAllParticipants(requestingUsername, __current);
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

   public String[] getGroupModerators(final String instigator, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "getGroupModerators", OperationMode.Normal, __ctx);
      final StringArrayHolder __result = new StringArrayHolder();
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               ChatRoom __servant = null;

               try {
                  __servant = (ChatRoom)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __result.value = __servant.getGroupModerators(instigator, __current);
                  return DispatchStatus.DispatchOK;
               } catch (UserException var4) {
                  this.setUserException(var4);
                  return DispatchStatus.DispatchUserException;
               }
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
      } catch (FusionException var15) {
         throw var15;
      } catch (SystemException var16) {
         throw var16;
      } catch (Throwable var17) {
         LocalExceptionWrapper.throwWrapper(var17);
         return __result.value;
      }
   }

   public int getMaximumMessageLength(final String sender, Map<String, String> __ctx) throws LocalExceptionWrapper {
      final Current __current = new Current();
      this.__initCurrent(__current, "getMaximumMessageLength", OperationMode.Normal, __ctx);
      final IntHolder __result = new IntHolder();
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               ChatRoom __servant = null;

               try {
                  __servant = (ChatRoom)__obj;
               } catch (ClassCastException var4) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               __result.value = __servant.getMaximumMessageLength(sender, __current);
               return DispatchStatus.DispatchOK;
            }
         };

         int var7;
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

   public int getNumParticipants(Map<String, String> __ctx) throws LocalExceptionWrapper {
      final Current __current = new Current();
      this.__initCurrent(__current, "getNumParticipants", OperationMode.Normal, __ctx);
      final IntHolder __result = new IntHolder();
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               ChatRoom __servant = null;

               try {
                  __servant = (ChatRoom)__obj;
               } catch (ClassCastException var4) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               __result.value = __servant.getNumParticipants(__current);
               return DispatchStatus.DispatchOK;
            }
         };

         int var6;
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
      } catch (SystemException var13) {
         throw var13;
      } catch (Throwable var14) {
         LocalExceptionWrapper.throwWrapper(var14);
         return __result.value;
      }
   }

   public ChatRoomDataIce getRoomData(Map<String, String> __ctx) throws LocalExceptionWrapper {
      final Current __current = new Current();
      this.__initCurrent(__current, "getRoomData", OperationMode.Normal, __ctx);
      final ChatRoomDataIceHolder __result = new ChatRoomDataIceHolder();
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               ChatRoom __servant = null;

               try {
                  __servant = (ChatRoom)__obj;
               } catch (ClassCastException var4) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               __result.value = __servant.getRoomData(__current);
               return DispatchStatus.DispatchOK;
            }
         };

         ChatRoomDataIce var6;
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
      } catch (SystemException var13) {
         throw var13;
      } catch (Throwable var14) {
         LocalExceptionWrapper.throwWrapper(var14);
         return __result.value;
      }
   }

   public Map<String, String> getTheme(Map<String, String> __ctx) throws LocalExceptionWrapper {
      final Current __current = new Current();
      this.__initCurrent(__current, "getTheme", OperationMode.Normal, __ctx);
      final ParamMapHolder __result = new ParamMapHolder();
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               ChatRoom __servant = null;

               try {
                  __servant = (ChatRoom)__obj;
               } catch (ClassCastException var4) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               __result.value = __servant.getTheme(__current);
               return DispatchStatus.DispatchOK;
            }
         };

         Map var6;
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
      } catch (SystemException var13) {
         throw var13;
      } catch (Throwable var14) {
         LocalExceptionWrapper.throwWrapper(var14);
         return __result.value;
      }
   }

   public void inviteUserToGroup(final String invitee, final String inviter, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "inviteUserToGroup", OperationMode.Normal, __ctx);
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               ChatRoom __servant = null;

               try {
                  __servant = (ChatRoom)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __servant.inviteUserToGroup(invitee, inviter, __current);
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

   public boolean isLocked(Map<String, String> __ctx) throws LocalExceptionWrapper {
      final Current __current = new Current();
      this.__initCurrent(__current, "isLocked", OperationMode.Normal, __ctx);
      final BooleanHolder __result = new BooleanHolder();
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               ChatRoom __servant = null;

               try {
                  __servant = (ChatRoom)__obj;
               } catch (ClassCastException var4) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               __result.value = __servant.isLocked(__current);
               return DispatchStatus.DispatchOK;
            }
         };

         boolean var6;
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
      } catch (SystemException var13) {
         throw var13;
      } catch (Throwable var14) {
         LocalExceptionWrapper.throwWrapper(var14);
         return __result.value;
      }
   }

   public boolean isVisibleParticipant(final String username, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "isVisibleParticipant", OperationMode.Normal, __ctx);
      final BooleanHolder __result = new BooleanHolder();
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               ChatRoom __servant = null;

               try {
                  __servant = (ChatRoom)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __result.value = __servant.isVisibleParticipant(username, __current);
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

   public void kickIndexes(final int[] indexes, final String bannedBy, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "kickIndexes", OperationMode.Normal, __ctx);
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               ChatRoom __servant = null;

               try {
                  __servant = (ChatRoom)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __servant.kickIndexes(indexes, bannedBy, __current);
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

   public void listParticipants(final String requestingUsername, final int size, final int startIndex, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "listParticipants", OperationMode.Normal, __ctx);
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               ChatRoom __servant = null;

               try {
                  __servant = (ChatRoom)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __servant.listParticipants(requestingUsername, size, startIndex, __current);
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

   public void lock(final String locker, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "lock", OperationMode.Normal, __ctx);
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               ChatRoom __servant = null;

               try {
                  __servant = (ChatRoom)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __servant.lock(locker, __current);
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

   public void mute(final String username, final String target, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "mute", OperationMode.Normal, __ctx);
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               ChatRoom __servant = null;

               try {
                  __servant = (ChatRoom)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __servant.mute(username, target, __current);
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

   public void putMessage(final MessageDataIce message, final String sessionID, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "putMessage", OperationMode.Normal, __ctx);
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               ChatRoom __servant = null;

               try {
                  __servant = (ChatRoom)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __servant.putMessage(message, sessionID, __current);
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

   public void putSystemMessage(final String messageText, final String[] emoticonKeys, Map<String, String> __ctx) throws LocalExceptionWrapper {
      final Current __current = new Current();
      this.__initCurrent(__current, "putSystemMessage", OperationMode.Normal, __ctx);
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               ChatRoom __servant = null;

               try {
                  __servant = (ChatRoom)__obj;
               } catch (ClassCastException var4) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               __servant.putSystemMessage(messageText, emoticonKeys, __current);
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
      } catch (SystemException var13) {
         throw var13;
      } catch (Throwable var14) {
         LocalExceptionWrapper.throwWrapper(var14);
      }

   }

   public void putSystemMessageWithColour(final String messageText, final String[] emoticonKeys, final int messageColour, Map<String, String> __ctx) throws LocalExceptionWrapper {
      final Current __current = new Current();
      this.__initCurrent(__current, "putSystemMessageWithColour", OperationMode.Normal, __ctx);
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               ChatRoom __servant = null;

               try {
                  __servant = (ChatRoom)__obj;
               } catch (ClassCastException var4) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               __servant.putSystemMessageWithColour(messageText, emoticonKeys, messageColour, __current);
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
      } catch (SystemException var14) {
         throw var14;
      } catch (Throwable var15) {
         LocalExceptionWrapper.throwWrapper(var15);
      }

   }

   public void removeGroupModerator(final String instigator, final String targetUser, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "removeGroupModerator", OperationMode.Normal, __ctx);
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               ChatRoom __servant = null;

               try {
                  __servant = (ChatRoom)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __servant.removeGroupModerator(instigator, targetUser, __current);
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

   public void removeModerator(final String username, Map<String, String> __ctx) throws LocalExceptionWrapper {
      final Current __current = new Current();
      this.__initCurrent(__current, "removeModerator", OperationMode.Normal, __ctx);
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               ChatRoom __servant = null;

               try {
                  __servant = (ChatRoom)__obj;
               } catch (ClassCastException var4) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               __servant.removeModerator(username, __current);
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

   public void removeParticipant(final String username, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "removeParticipant", OperationMode.Normal, __ctx);
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               ChatRoom __servant = null;

               try {
                  __servant = (ChatRoom)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __servant.removeParticipant(username, __current);
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

   public void removeParticipantOneWay(final String username, final boolean removeFromUsersChatRoomList, Map<String, String> __ctx) throws LocalExceptionWrapper {
      final Current __current = new Current();
      this.__initCurrent(__current, "removeParticipantOneWay", OperationMode.Normal, __ctx);
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               ChatRoom __servant = null;

               try {
                  __servant = (ChatRoom)__obj;
               } catch (ClassCastException var4) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               __servant.removeParticipantOneWay(username, removeFromUsersChatRoomList, __current);
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
      } catch (SystemException var13) {
         throw var13;
      } catch (Throwable var14) {
         LocalExceptionWrapper.throwWrapper(var14);
      }

   }

   public void setAdultOnly(final boolean adultOnly, Map<String, String> __ctx) throws LocalExceptionWrapper {
      final Current __current = new Current();
      this.__initCurrent(__current, "setAdultOnly", OperationMode.Normal, __ctx);
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               ChatRoom __servant = null;

               try {
                  __servant = (ChatRoom)__obj;
               } catch (ClassCastException var4) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               __servant.setAdultOnly(adultOnly, __current);
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

   public void setAllowKicking(final boolean allowKicking, Map<String, String> __ctx) throws LocalExceptionWrapper {
      final Current __current = new Current();
      this.__initCurrent(__current, "setAllowKicking", OperationMode.Normal, __ctx);
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               ChatRoom __servant = null;

               try {
                  __servant = (ChatRoom)__obj;
               } catch (ClassCastException var4) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               __servant.setAllowKicking(allowKicking, __current);
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

   public void setDescription(final String description, Map<String, String> __ctx) throws LocalExceptionWrapper {
      final Current __current = new Current();
      this.__initCurrent(__current, "setDescription", OperationMode.Normal, __ctx);
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               ChatRoom __servant = null;

               try {
                  __servant = (ChatRoom)__obj;
               } catch (ClassCastException var4) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               __servant.setDescription(description, __current);
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

   public void setMaximumSize(final int maximumSize, Map<String, String> __ctx) throws LocalExceptionWrapper {
      final Current __current = new Current();
      this.__initCurrent(__current, "setMaximumSize", OperationMode.Normal, __ctx);
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               ChatRoom __servant = null;

               try {
                  __servant = (ChatRoom)__obj;
               } catch (ClassCastException var4) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               __servant.setMaximumSize(maximumSize, __current);
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

   public void setNumberOfFakeParticipants(final String username, final int number, Map<String, String> __ctx) throws LocalExceptionWrapper {
      final Current __current = new Current();
      this.__initCurrent(__current, "setNumberOfFakeParticipants", OperationMode.Normal, __ctx);
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               ChatRoom __servant = null;

               try {
                  __servant = (ChatRoom)__obj;
               } catch (ClassCastException var4) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               __servant.setNumberOfFakeParticipants(username, number, __current);
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
      } catch (SystemException var13) {
         throw var13;
      } catch (Throwable var14) {
         LocalExceptionWrapper.throwWrapper(var14);
      }

   }

   public void silence(final String username, final int timeout, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "silence", OperationMode.Normal, __ctx);
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               ChatRoom __servant = null;

               try {
                  __servant = (ChatRoom)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __servant.silence(username, timeout, __current);
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

   public void silenceUser(final String instigator, final String target, final int timeout, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "silenceUser", OperationMode.Normal, __ctx);
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               ChatRoom __servant = null;

               try {
                  __servant = (ChatRoom)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __servant.silenceUser(instigator, target, timeout, __current);
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

   public void submitGiftAllTask(final int giftId, final String giftMessage, final MessageDataIce message, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "submitGiftAllTask", OperationMode.Normal, __ctx);
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               ChatRoom __servant = null;

               try {
                  __servant = (ChatRoom)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __servant.submitGiftAllTask(giftId, giftMessage, message, __current);
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

   public void unbanGroupMember(final String target, final String instigator, final int reasonCode, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "unbanGroupMember", OperationMode.Normal, __ctx);
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               ChatRoom __servant = null;

               try {
                  __servant = (ChatRoom)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __servant.unbanGroupMember(target, instigator, reasonCode, __current);
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

   public void unbanUser(final String username, Map<String, String> __ctx) throws LocalExceptionWrapper {
      final Current __current = new Current();
      this.__initCurrent(__current, "unbanUser", OperationMode.Normal, __ctx);
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               ChatRoom __servant = null;

               try {
                  __servant = (ChatRoom)__obj;
               } catch (ClassCastException var4) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               __servant.unbanUser(username, __current);
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

   public void unlock(final String unlocker, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "unlock", OperationMode.Normal, __ctx);
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               ChatRoom __servant = null;

               try {
                  __servant = (ChatRoom)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __servant.unlock(unlocker, __current);
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

   public void unmute(final String username, final String target, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "unmute", OperationMode.Normal, __ctx);
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               ChatRoom __servant = null;

               try {
                  __servant = (ChatRoom)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __servant.unmute(username, target, __current);
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

   public void unsilence(final String username, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "unsilence", OperationMode.Normal, __ctx);
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               ChatRoom __servant = null;

               try {
                  __servant = (ChatRoom)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __servant.unsilence(username, __current);
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

   public void unsilenceUser(final String instigator, final String target, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "unsilenceUser", OperationMode.Normal, __ctx);
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               ChatRoom __servant = null;

               try {
                  __servant = (ChatRoom)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __servant.unsilenceUser(instigator, target, __current);
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

   public void updateDescription(final String instigator, final String description, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "updateDescription", OperationMode.Normal, __ctx);
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               ChatRoom __servant = null;

               try {
                  __servant = (ChatRoom)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __servant.updateDescription(instigator, description, __current);
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

   public void updateExtraData(final ChatRoomDataIce data, Map<String, String> __ctx) throws LocalExceptionWrapper {
      final Current __current = new Current();
      this.__initCurrent(__current, "updateExtraData", OperationMode.Normal, __ctx);
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               ChatRoom __servant = null;

               try {
                  __servant = (ChatRoom)__obj;
               } catch (ClassCastException var4) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               __servant.updateExtraData(data, __current);
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

   public void updateGroupModeratorStatus(final String username, final boolean promote, Map<String, String> __ctx) throws LocalExceptionWrapper {
      final Current __current = new Current();
      this.__initCurrent(__current, "updateGroupModeratorStatus", OperationMode.Normal, __ctx);
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               ChatRoom __servant = null;

               try {
                  __servant = (ChatRoom)__obj;
               } catch (ClassCastException var4) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               __servant.updateGroupModeratorStatus(username, promote, __current);
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
      } catch (SystemException var13) {
         throw var13;
      } catch (Throwable var14) {
         LocalExceptionWrapper.throwWrapper(var14);
      }

   }

   public void voteToKickUser(final String voter, final String target, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "voteToKickUser", OperationMode.Normal, __ctx);
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               ChatRoom __servant = null;

               try {
                  __servant = (ChatRoom)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __servant.voteToKickUser(voter, target, __current);
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

   public void warnUser(final String instigator, final String target, final String message, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "warnUser", OperationMode.Normal, __ctx);
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               ChatRoom __servant = null;

               try {
                  __servant = (ChatRoom)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __servant.warnUser(instigator, target, message, __current);
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
