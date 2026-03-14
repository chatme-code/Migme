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

public final class _GroupChatDelM extends _ObjectDelM implements _GroupChatDel {
   public void botKilled(String botInstanceID, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("botKilled", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(botInstanceID);
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

   public String[] getParticipants(String requestingUsername, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("getParticipants", OperationMode.Normal, __ctx);

      String[] var7;
      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(requestingUsername);
         } catch (LocalException var15) {
            __og.abort(var15);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (UserException var14) {
                  throw new UnknownUserException(var14.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            String[] __ret = StringArrayHelper.read(__is);
            __is.endReadEncaps();
            var7 = __ret;
         } catch (LocalException var16) {
            throw new LocalExceptionWrapper(var16, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var7;
   }

   public boolean isParticipant(String username, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("isParticipant", OperationMode.Normal, __ctx);

      boolean var7;
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
            boolean __ret = __is.readBool();
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

   public void putBotMessage(String botInstanceID, String username, String message, String[] emoticonHotKeys, boolean displayPopUp, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("putBotMessage", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(botInstanceID);
            __os.writeString(username);
            __os.writeString(message);
            StringArrayHelper.write(__os, emoticonHotKeys);
            __os.writeBool(displayPopUp);
         } catch (LocalException var19) {
            __og.abort(var19);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var17) {
                  throw var17;
               } catch (UserException var18) {
                  throw new UnknownUserException(var18.ice_name());
               }
            }

            __og.is().skipEmptyEncaps();
         } catch (LocalException var20) {
            throw new LocalExceptionWrapper(var20, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void putBotMessageToAllUsers(String botInstanceID, String message, String[] emoticonHotKeys, boolean displayPopUp, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("putBotMessageToAllUsers", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(botInstanceID);
            __os.writeString(message);
            StringArrayHelper.write(__os, emoticonHotKeys);
            __os.writeBool(displayPopUp);
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

   public void putBotMessageToUsers(String botInstanceID, String[] usernames, String message, String[] emoticonHotKeys, boolean displayPopUp, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("putBotMessageToUsers", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(botInstanceID);
            StringArrayHelper.write(__os, usernames);
            __os.writeString(message);
            StringArrayHelper.write(__os, emoticonHotKeys);
            __os.writeBool(displayPopUp);
         } catch (LocalException var19) {
            __og.abort(var19);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var17) {
                  throw var17;
               } catch (UserException var18) {
                  throw new UnknownUserException(var18.ice_name());
               }
            }

            __og.is().skipEmptyEncaps();
         } catch (LocalException var20) {
            throw new LocalExceptionWrapper(var20, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void sendGamesHelpToUser(String username, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("sendGamesHelpToUser", OperationMode.Normal, __ctx);

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

   public void sendMessageToBots(String username, String message, long receivedTimestamp, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("sendMessageToBots", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(username);
            __os.writeString(message);
            __os.writeLong(receivedTimestamp);
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

   public void startBot(String username, String botCommandName, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("startBot", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(username);
            __os.writeString(botCommandName);
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

   public void stopAllBots(String username, int timeout, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("stopAllBots", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(username);
            __os.writeInt(timeout);
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

   public void stopBot(String username, String botCommandName, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("stopBot", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(username);
            __os.writeString(botCommandName);
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

   public void addParticipant(String inviterUsername, String inviteeUsername, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("addParticipant", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(inviterUsername);
            __os.writeString(inviteeUsername);
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

   public void addParticipantInner(String inviterUsername, String inviteeUsername, boolean debug, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("addParticipantInner", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(inviterUsername);
            __os.writeString(inviteeUsername);
            __os.writeBool(debug);
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

   public void addParticipants(String inviterUsername, String[] inviteeUsernames, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("addParticipants", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(inviterUsername);
            StringArrayHelper.write(__os, inviteeUsernames);
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

   public void addUserToGroupChatDebug(String participant, boolean b, boolean c, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("addUserToGroupChatDebug", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(participant);
            __os.writeBool(b);
            __os.writeBool(c);
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

   public int executeEmoteCommandWithState(String emoteCommand, MessageDataIce message, SessionPrx sessionProxy, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("executeEmoteCommandWithState", OperationMode.Normal, __ctx);

      int var9;
      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(emoteCommand);
            message.__write(__os);
            SessionPrxHelper.__write(__os, sessionProxy);
         } catch (LocalException var19) {
            __og.abort(var19);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var17) {
                  throw var17;
               } catch (UserException var18) {
                  throw new UnknownUserException(var18.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            int __ret = __is.readInt();
            __is.endReadEncaps();
            var9 = __ret;
         } catch (LocalException var20) {
            throw new LocalExceptionWrapper(var20, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var9;
   }

   public int getCreatorUserID(Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("getCreatorUserID", OperationMode.Normal, __ctx);

      int var6;
      try {
         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (UserException var12) {
                  throw new UnknownUserException(var12.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            int __ret = __is.readInt();
            __is.endReadEncaps();
            var6 = __ret;
         } catch (LocalException var13) {
            throw new LocalExceptionWrapper(var13, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var6;
   }

   public String getCreatorUsername(Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("getCreatorUsername", OperationMode.Normal, __ctx);

      String var6;
      try {
         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (UserException var12) {
                  throw new UnknownUserException(var12.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            String __ret = __is.readString();
            __is.endReadEncaps();
            var6 = __ret;
         } catch (LocalException var13) {
            throw new LocalExceptionWrapper(var13, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var6;
   }

   public String getId(Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("getId", OperationMode.Normal, __ctx);

      String var6;
      try {
         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (UserException var12) {
                  throw new UnknownUserException(var12.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            String __ret = __is.readString();
            __is.endReadEncaps();
            var6 = __ret;
         } catch (LocalException var13) {
            throw new LocalExceptionWrapper(var13, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var6;
   }

   public int getNumParticipants(Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("getNumParticipants", OperationMode.Normal, __ctx);

      int var6;
      try {
         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (UserException var12) {
                  throw new UnknownUserException(var12.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            int __ret = __is.readInt();
            __is.endReadEncaps();
            var6 = __ret;
         } catch (LocalException var13) {
            throw new LocalExceptionWrapper(var13, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var6;
   }

   public int[] getParticipantUserIDs(Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("getParticipantUserIDs", OperationMode.Normal, __ctx);

      int[] var6;
      try {
         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (UserException var12) {
                  throw new UnknownUserException(var12.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            int[] __ret = IntArrayHelper.read(__is);
            __is.endReadEncaps();
            var6 = __ret;
         } catch (LocalException var13) {
            throw new LocalExceptionWrapper(var13, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var6;
   }

   public int getPrivateChatPartnerUserID(Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("getPrivateChatPartnerUserID", OperationMode.Normal, __ctx);

      int var6;
      try {
         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (UserException var12) {
                  throw new UnknownUserException(var12.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            int __ret = __is.readInt();
            __is.endReadEncaps();
            var6 = __ret;
         } catch (LocalException var13) {
            throw new LocalExceptionWrapper(var13, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var6;
   }

   public String listOfParticipants(Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("listOfParticipants", OperationMode.Normal, __ctx);

      String var6;
      try {
         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (UserException var12) {
                  throw new UnknownUserException(var12.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            String __ret = __is.readString();
            __is.endReadEncaps();
            var6 = __ret;
         } catch (LocalException var13) {
            throw new LocalExceptionWrapper(var13, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var6;
   }

   public void putFileReceived(MessageDataIce message, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("putFileReceived", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            message.__write(__os);
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

   public void putMessage(MessageDataIce message, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("putMessage", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            message.__write(__os);
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

   public boolean removeParticipant(String username, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("removeParticipant", OperationMode.Normal, __ctx);

      boolean var7;
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
            boolean __ret = __is.readBool();
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

   public void sendInitialMessages(Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("sendInitialMessages", OperationMode.Normal, __ctx);

      try {
         boolean __ok = __og.invoke();
         if (!__og.is().isEmpty()) {
            try {
               if (!__ok) {
                  try {
                     __og.throwUserException();
                  } catch (UserException var10) {
                     throw new UnknownUserException(var10.ice_name());
                  }
               }

               __og.is().skipEmptyEncaps();
            } catch (LocalException var11) {
               throw new LocalExceptionWrapper(var11, false);
            }
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public boolean supportsBinaryMessage(String usernameToExclude, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("supportsBinaryMessage", OperationMode.Normal, __ctx);

      boolean var7;
      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(usernameToExclude);
         } catch (LocalException var15) {
            __og.abort(var15);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (UserException var14) {
                  throw new UnknownUserException(var14.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            boolean __ret = __is.readBool();
            __is.endReadEncaps();
            var7 = __ret;
         } catch (LocalException var16) {
            throw new LocalExceptionWrapper(var16, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var7;
   }
}
