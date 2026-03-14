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

public final class _MessageSwitchboardDelM extends _ObjectDelM implements _MessageSwitchboardDel {
   public GroupChatPrx ensureGroupChatExists(SessionPrx currentSession, String groupChatID, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("ensureGroupChatExists", OperationMode.Normal, __ctx);

      GroupChatPrx var8;
      try {
         try {
            BasicStream __os = __og.os();
            SessionPrxHelper.__write(__os, currentSession);
            __os.writeString(groupChatID);
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

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            GroupChatPrx __ret = GroupChatPrxHelper.__read(__is);
            __is.endReadEncaps();
            var8 = __ret;
         } catch (LocalException var19) {
            throw new LocalExceptionWrapper(var19, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var8;
   }

   public void getAndPushMessages(String username, byte chatType, String suppliedChatID, long oldestMessageTimestamp, long newestMessageTimestamp, int limit, ConnectionPrx cxn, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("getAndPushMessages", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(username);
            __os.writeByte(chatType);
            __os.writeString(suppliedChatID);
            __os.writeLong(oldestMessageTimestamp);
            __os.writeLong(newestMessageTimestamp);
            __os.writeInt(limit);
            ConnectionPrxHelper.__write(__os, cxn);
         } catch (LocalException var23) {
            __og.abort(var23);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var21) {
                  throw var21;
               } catch (UserException var22) {
                  throw new UnknownUserException(var22.ice_name());
               }
            }

            __og.is().skipEmptyEncaps();
         } catch (LocalException var24) {
            throw new LocalExceptionWrapper(var24, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void getAndPushMessages2(String username, byte chatType, String suppliedChatID, long oldestMessageTimestamp, long newestMessageTimestamp, int limit, ConnectionPrx cxn, int deviceType, short clientVersion, short fusionPktTransactionId, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("getAndPushMessages2", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(username);
            __os.writeByte(chatType);
            __os.writeString(suppliedChatID);
            __os.writeLong(oldestMessageTimestamp);
            __os.writeLong(newestMessageTimestamp);
            __os.writeInt(limit);
            ConnectionPrxHelper.__write(__os, cxn);
            __os.writeInt(deviceType);
            __os.writeShort(clientVersion);
            __os.writeShort(fusionPktTransactionId);
         } catch (LocalException var26) {
            __og.abort(var26);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var24) {
                  throw var24;
               } catch (UserException var25) {
                  throw new UnknownUserException(var25.ice_name());
               }
            }

            __og.is().skipEmptyEncaps();
         } catch (LocalException var27) {
            throw new LocalExceptionWrapper(var27, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public ChatDefinitionIce[] getChats(int userID, int chatListVersion, int limit, byte chatType, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("getChats", OperationMode.Normal, __ctx);

      ChatDefinitionIce[] var10;
      try {
         try {
            BasicStream __os = __og.os();
            __os.writeInt(userID);
            __os.writeInt(chatListVersion);
            __os.writeInt(limit);
            __os.writeByte(chatType);
         } catch (LocalException var20) {
            __og.abort(var20);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var18) {
                  throw var18;
               } catch (UserException var19) {
                  throw new UnknownUserException(var19.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            ChatDefinitionIce[] __ret = ChatDefinitionIceArrayHelper.read(__is);
            __is.endReadEncaps();
            var10 = __ret;
         } catch (LocalException var21) {
            throw new LocalExceptionWrapper(var21, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var10;
   }

   public ChatDefinitionIce[] getChats2(int userID, int chatListVersion, int limit, byte chatType, ConnectionPrx cxn, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("getChats2", OperationMode.Normal, __ctx);

      ChatDefinitionIce[] var11;
      try {
         try {
            BasicStream __os = __og.os();
            __os.writeInt(userID);
            __os.writeInt(chatListVersion);
            __os.writeInt(limit);
            __os.writeByte(chatType);
            ConnectionPrxHelper.__write(__os, cxn);
         } catch (LocalException var21) {
            __og.abort(var21);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var19) {
                  throw var19;
               } catch (UserException var20) {
                  throw new UnknownUserException(var20.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            ChatDefinitionIce[] __ret = ChatDefinitionIceArrayHelper.read(__is);
            __is.endReadEncaps();
            var11 = __ret;
         } catch (LocalException var22) {
            throw new LocalExceptionWrapper(var22, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var11;
   }

   public boolean isUserChatSyncEnabled(ConnectionPrx cxn, String username, int userID, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("isUserChatSyncEnabled", OperationMode.Normal, __ctx);

      boolean var9;
      try {
         try {
            BasicStream __os = __og.os();
            ConnectionPrxHelper.__write(__os, cxn);
            __os.writeString(username);
            __os.writeInt(userID);
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
            boolean __ret = __is.readBool();
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

   public void onCreateGroupChat(ChatDefinitionIce storedGroupChat, String creatorUsername, String privateChatPartnerUsername, GroupChatPrx groupChatRemote, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("onCreateGroupChat", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            storedGroupChat.__write(__os);
            __os.writeString(creatorUsername);
            __os.writeString(privateChatPartnerUsername);
            GroupChatPrxHelper.__write(__os, groupChatRemote);
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

   public void onCreatePrivateChat(int userID, String username, String otherUser, int deviceType, short clientVersion, UserDataIce senderUserData, String recipientDisplayPicture, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("onCreatePrivateChat", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeInt(userID);
            __os.writeString(username);
            __os.writeString(otherUser);
            __os.writeInt(deviceType);
            __os.writeShort(clientVersion);
            senderUserData.__write(__os);
            __os.writeString(recipientDisplayPicture);
         } catch (LocalException var21) {
            __og.abort(var21);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var19) {
                  throw var19;
               } catch (UserException var20) {
                  throw new UnknownUserException(var20.ice_name());
               }
            }

            __og.is().skipEmptyEncaps();
         } catch (LocalException var22) {
            throw new LocalExceptionWrapper(var22, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void onGetChats(ConnectionPrx cxn, int userID, int chatListVersion, int limit, byte chatType, short transactionId, String parentUsername, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("onGetChats", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            ConnectionPrxHelper.__write(__os, cxn);
            __os.writeInt(userID);
            __os.writeInt(chatListVersion);
            __os.writeInt(limit);
            __os.writeByte(chatType);
            __os.writeShort(transactionId);
            __os.writeString(parentUsername);
         } catch (LocalException var21) {
            __og.abort(var21);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var19) {
                  throw var19;
               } catch (UserException var20) {
                  throw new UnknownUserException(var20.ice_name());
               }
            }

            __og.is().skipEmptyEncaps();
         } catch (LocalException var22) {
            throw new LocalExceptionWrapper(var22, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void onJoinChatRoom(String username, int userID, String chatRoomName, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("onJoinChatRoom", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(username);
            __os.writeInt(userID);
            __os.writeString(chatRoomName);
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

   public void onJoinGroupChat(String username, int userID, String groupChatGUID, boolean debug, UserPrx userProxy, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("onJoinGroupChat", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(username);
            __os.writeInt(userID);
            __os.writeString(groupChatGUID);
            __os.writeBool(debug);
            UserPrxHelper.__write(__os, userProxy);
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

   public void onLeaveChatRoom(String username, int userID, String chatRoomName, UserPrx userProxy, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("onLeaveChatRoom", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(username);
            __os.writeInt(userID);
            __os.writeString(chatRoomName);
            UserPrxHelper.__write(__os, userProxy);
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

   public void onLeaveGroupChat(String username, int userID, String groupChatGUID, UserPrx userProxy, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("onLeaveGroupChat", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(username);
            __os.writeInt(userID);
            __os.writeString(groupChatGUID);
            UserPrxHelper.__write(__os, userProxy);
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

   public void onLeavePrivateChat(int userID, String username, String otherUser, int deviceType, short clientVersion, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("onLeavePrivateChat", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeInt(userID);
            __os.writeString(username);
            __os.writeString(otherUser);
            __os.writeInt(deviceType);
            __os.writeShort(clientVersion);
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

   public void onLogon(int userID, SessionPrx sess, short transactionID, String parentUsername, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("onLogon", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeInt(userID);
            SessionPrxHelper.__write(__os, sess);
            __os.writeShort(transactionID);
            __os.writeString(parentUsername);
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

   public void onSendFusionMessageToChatRoom(SessionPrx currentSession, UserPrx parentUser, MessageDataIce messageData, String chatRoomName, int deviceType, short clientVersion, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("onSendFusionMessageToChatRoom", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            SessionPrxHelper.__write(__os, currentSession);
            UserPrxHelper.__write(__os, parentUser);
            messageData.__write(__os);
            __os.writeString(chatRoomName);
            __os.writeInt(deviceType);
            __os.writeShort(clientVersion);
         } catch (LocalException var20) {
            __og.abort(var20);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var18) {
                  throw var18;
               } catch (UserException var19) {
                  throw new UnknownUserException(var19.ice_name());
               }
            }

            __og.is().skipEmptyEncaps();
         } catch (LocalException var21) {
            throw new LocalExceptionWrapper(var21, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void onSendFusionMessageToGroupChat(SessionPrx currentSession, UserPrx parentUser, MessageDataIce messageData, String groupChatID, int deviceType, short clientVersion, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("onSendFusionMessageToGroupChat", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            SessionPrxHelper.__write(__os, currentSession);
            UserPrxHelper.__write(__os, parentUser);
            messageData.__write(__os);
            __os.writeString(groupChatID);
            __os.writeInt(deviceType);
            __os.writeShort(clientVersion);
         } catch (LocalException var20) {
            __og.abort(var20);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var18) {
                  throw var18;
               } catch (UserException var19) {
                  throw new UnknownUserException(var19.ice_name());
               }
            }

            __og.is().skipEmptyEncaps();
         } catch (LocalException var21) {
            throw new LocalExceptionWrapper(var21, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public boolean onSendFusionMessageToIndividual(SessionPrx currentSession, UserPrx parentUser, MessageDataIce messageData, String destinationUsername, String[] uniqueUsersPrivateChattedWith, int deviceType, short clientVersion, UserDataIce senderUserData, String recipientDisplayPicture, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("onSendFusionMessageToIndividual", OperationMode.Normal, __ctx);

      boolean var15;
      try {
         try {
            BasicStream __os = __og.os();
            SessionPrxHelper.__write(__os, currentSession);
            UserPrxHelper.__write(__os, parentUser);
            messageData.__write(__os);
            __os.writeString(destinationUsername);
            StringArrayHelper.write(__os, uniqueUsersPrivateChattedWith);
            __os.writeInt(deviceType);
            __os.writeShort(clientVersion);
            senderUserData.__write(__os);
            __os.writeString(recipientDisplayPicture);
         } catch (LocalException var25) {
            __og.abort(var25);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var23) {
                  throw var23;
               } catch (UserException var24) {
                  throw new UnknownUserException(var24.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            boolean __ret = __is.readBool();
            __is.endReadEncaps();
            var15 = __ret;
         } catch (LocalException var26) {
            throw new LocalExceptionWrapper(var26, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var15;
   }

   public boolean onSendMessageToAllUsersInChat(SessionPrx currentSession, UserPrx parentUser, MessageDataIce messageData, UserDataIce senderUserData, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("onSendMessageToAllUsersInChat", OperationMode.Normal, __ctx);

      boolean var10;
      try {
         try {
            BasicStream __os = __og.os();
            SessionPrxHelper.__write(__os, currentSession);
            UserPrxHelper.__write(__os, parentUser);
            messageData.__write(__os);
            senderUserData.__write(__os);
         } catch (LocalException var20) {
            __og.abort(var20);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var18) {
                  throw var18;
               } catch (UserException var19) {
                  throw new UnknownUserException(var19.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            boolean __ret = __is.readBool();
            __is.endReadEncaps();
            var10 = __ret;
         } catch (LocalException var21) {
            throw new LocalExceptionWrapper(var21, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var10;
   }

   public void setChatName(String parentUsername, String suppliedChatID, byte chatType, String chatName, RegistryPrx regy, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("setChatName", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(parentUsername);
            __os.writeString(suppliedChatID);
            __os.writeByte(chatType);
            __os.writeString(chatName);
            RegistryPrxHelper.__write(__os, regy);
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
}
