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

public final class _SessionDelM extends _ObjectDelM implements _SessionDel {
   public void chatroomJoined(ChatRoomPrx roomProxy, String name, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("chatroomJoined", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            ChatRoomPrxHelper.__write(__os, roomProxy);
            __os.writeString(name);
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

   public void endSession(Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("endSession", OperationMode.Normal, __ctx);

      try {
         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var10) {
                  throw var10;
               } catch (UserException var11) {
                  throw new UnknownUserException(var11.ice_name());
               }
            }

            __og.is().skipEmptyEncaps();
         } catch (LocalException var12) {
            throw new LocalExceptionWrapper(var12, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void endSessionOneWay(Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("endSessionOneWay", OperationMode.Normal, __ctx);

      try {
         boolean __ok = __og.invoke();
         if (!__og.is().isEmpty()) {
            try {
               if (!__ok) {
                  try {
                     __og.throwUserException();
                  } catch (UserException var9) {
                     throw new UnknownUserException(var9.ice_name());
                  }
               }

               __og.is().skipEmptyEncaps();
            } catch (LocalException var10) {
               throw new LocalExceptionWrapper(var10, false);
            }
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public GroupChatPrx findGroupChatObject(String groupChatID, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("findGroupChatObject", OperationMode.Normal, __ctx);

      GroupChatPrx var7;
      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(groupChatID);
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

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            GroupChatPrx __ret = GroupChatPrxHelper.__read(__is);
            __is.endReadEncaps();
            var7 = __ret;
         } catch (LocalException var17) {
            throw new LocalExceptionWrapper(var17, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var7;
   }

   public void friendInvitedByPhoneNumber(Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("friendInvitedByPhoneNumber", OperationMode.Normal, __ctx);

      try {
         boolean __ok = __og.invoke();
         if (!__og.is().isEmpty()) {
            try {
               if (!__ok) {
                  try {
                     __og.throwUserException();
                  } catch (UserException var9) {
                     throw new UnknownUserException(var9.ice_name());
                  }
               }

               __og.is().skipEmptyEncaps();
            } catch (LocalException var10) {
               throw new LocalExceptionWrapper(var10, false);
            }
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void friendInvitedByUsername(Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("friendInvitedByUsername", OperationMode.Normal, __ctx);

      try {
         boolean __ok = __og.invoke();
         if (!__og.is().isEmpty()) {
            try {
               if (!__ok) {
                  try {
                     __og.throwUserException();
                  } catch (UserException var9) {
                     throw new UnknownUserException(var9.ice_name());
                  }
               }

               __og.is().skipEmptyEncaps();
            } catch (LocalException var10) {
               throw new LocalExceptionWrapper(var10, false);
            }
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public int getChatListVersion(Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("getChatListVersion", OperationMode.Normal, __ctx);

      int var6;
      try {
         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var12) {
                  throw var12;
               } catch (UserException var13) {
                  throw new UnknownUserException(var13.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            int __ret = __is.readInt();
            __is.endReadEncaps();
            var6 = __ret;
         } catch (LocalException var14) {
            throw new LocalExceptionWrapper(var14, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var6;
   }

   public short getClientVersionIce(Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("getClientVersionIce", OperationMode.Normal, __ctx);

      short var6;
      try {
         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (UserException var11) {
                  throw new UnknownUserException(var11.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            short __ret = __is.readShort();
            __is.endReadEncaps();
            var6 = __ret;
         } catch (LocalException var12) {
            throw new LocalExceptionWrapper(var12, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var6;
   }

   public int getDeviceTypeAsInt(Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("getDeviceTypeAsInt", OperationMode.Normal, __ctx);

      int var6;
      try {
         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (UserException var11) {
                  throw new UnknownUserException(var11.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            int __ret = __is.readInt();
            __is.endReadEncaps();
            var6 = __ret;
         } catch (LocalException var12) {
            throw new LocalExceptionWrapper(var12, false);
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
               } catch (FusionException var12) {
                  throw var12;
               } catch (UserException var13) {
                  throw new UnknownUserException(var13.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            MessageSwitchboardPrx __ret = MessageSwitchboardPrxHelper.__read(__is);
            __is.endReadEncaps();
            var6 = __ret;
         } catch (LocalException var14) {
            throw new LocalExceptionWrapper(var14, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var6;
   }

   public String getMobileDeviceIce(Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("getMobileDeviceIce", OperationMode.Normal, __ctx);

      String var6;
      try {
         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (UserException var11) {
                  throw new UnknownUserException(var11.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            String __ret = __is.readString();
            __is.endReadEncaps();
            var6 = __ret;
         } catch (LocalException var12) {
            throw new LocalExceptionWrapper(var12, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var6;
   }

   public String getParentUsername(Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("getParentUsername", OperationMode.Normal, __ctx);

      String var6;
      try {
         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var12) {
                  throw var12;
               } catch (UserException var13) {
                  throw new UnknownUserException(var13.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            String __ret = __is.readString();
            __is.endReadEncaps();
            var6 = __ret;
         } catch (LocalException var14) {
            throw new LocalExceptionWrapper(var14, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var6;
   }

   public String getRemoteIPAddress(Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("getRemoteIPAddress", OperationMode.Normal, __ctx);

      String var6;
      try {
         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (UserException var11) {
                  throw new UnknownUserException(var11.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            String __ret = __is.readString();
            __is.endReadEncaps();
            var6 = __ret;
         } catch (LocalException var12) {
            throw new LocalExceptionWrapper(var12, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var6;
   }

   public String getSessionID(Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("getSessionID", OperationMode.Normal, __ctx);

      String var6;
      try {
         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (UserException var11) {
                  throw new UnknownUserException(var11.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            String __ret = __is.readString();
            __is.endReadEncaps();
            var6 = __ret;
         } catch (LocalException var12) {
            throw new LocalExceptionWrapper(var12, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var6;
   }

   public SessionMetricsIce getSessionMetrics(Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("getSessionMetrics", OperationMode.Normal, __ctx);

      SessionMetricsIce var6;
      try {
         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (UserException var11) {
                  throw new UnknownUserException(var11.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            SessionMetricsIce __ret = new SessionMetricsIce();
            __ret.__read(__is);
            __is.endReadEncaps();
            var6 = __ret;
         } catch (LocalException var12) {
            throw new LocalExceptionWrapper(var12, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var6;
   }

   public String getUserAgentIce(Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("getUserAgentIce", OperationMode.Normal, __ctx);

      String var6;
      try {
         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (UserException var11) {
                  throw new UnknownUserException(var11.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            String __ret = __is.readString();
            __is.endReadEncaps();
            var6 = __ret;
         } catch (LocalException var12) {
            throw new LocalExceptionWrapper(var12, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var6;
   }

   public UserPrx getUserProxy(String username, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("getUserProxy", OperationMode.Normal, __ctx);

      UserPrx var7;
      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(username);
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

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            UserPrx __ret = UserPrxHelper.__read(__is);
            __is.endReadEncaps();
            var7 = __ret;
         } catch (LocalException var17) {
            throw new LocalExceptionWrapper(var17, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var7;
   }

   public void groupChatJoined(String id, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("groupChatJoined", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(id);
         } catch (LocalException var13) {
            __og.abort(var13);
         }

         boolean __ok = __og.invoke();
         if (!__og.is().isEmpty()) {
            try {
               if (!__ok) {
                  try {
                     __og.throwUserException();
                  } catch (UserException var11) {
                     throw new UnknownUserException(var11.ice_name());
                  }
               }

               __og.is().skipEmptyEncaps();
            } catch (LocalException var12) {
               throw new LocalExceptionWrapper(var12, false);
            }
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void groupChatJoinedMultiple(String id, int increment, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("groupChatJoinedMultiple", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(id);
            __os.writeInt(increment);
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

   public void notifyUserJoinedChatRoomOneWay(String chatroomname, String username, boolean isAdministrator, boolean isMuted, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("notifyUserJoinedChatRoomOneWay", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(chatroomname);
            __os.writeString(username);
            __os.writeBool(isAdministrator);
            __os.writeBool(isMuted);
         } catch (LocalException var16) {
            __og.abort(var16);
         }

         boolean __ok = __og.invoke();
         if (!__og.is().isEmpty()) {
            try {
               if (!__ok) {
                  try {
                     __og.throwUserException();
                  } catch (UserException var14) {
                     throw new UnknownUserException(var14.ice_name());
                  }
               }

               __og.is().skipEmptyEncaps();
            } catch (LocalException var15) {
               throw new LocalExceptionWrapper(var15, false);
            }
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void notifyUserJoinedGroupChat(String groupChatId, String username, boolean isMuted, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("notifyUserJoinedGroupChat", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(groupChatId);
            __os.writeString(username);
            __os.writeBool(isMuted);
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

   public void notifyUserLeftChatRoomOneWay(String chatroomname, String username, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("notifyUserLeftChatRoomOneWay", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(chatroomname);
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

   public void notifyUserLeftGroupChat(String groupChatId, String username, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("notifyUserLeftGroupChat", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(groupChatId);
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

   public void photoUploaded(Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("photoUploaded", OperationMode.Normal, __ctx);

      try {
         boolean __ok = __og.invoke();
         if (!__og.is().isEmpty()) {
            try {
               if (!__ok) {
                  try {
                     __og.throwUserException();
                  } catch (UserException var9) {
                     throw new UnknownUserException(var9.ice_name());
                  }
               }

               __og.is().skipEmptyEncaps();
            } catch (LocalException var10) {
               throw new LocalExceptionWrapper(var10, false);
            }
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public boolean privateChattedWith(String username, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("privateChattedWith", OperationMode.Normal, __ctx);

      boolean var7;
      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(username);
         } catch (LocalException var14) {
            __og.abort(var14);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (UserException var13) {
                  throw new UnknownUserException(var13.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            boolean __ret = __is.readBool();
            __is.endReadEncaps();
            var7 = __ret;
         } catch (LocalException var15) {
            throw new LocalExceptionWrapper(var15, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var7;
   }

   public void profileEdited(Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("profileEdited", OperationMode.Normal, __ctx);

      try {
         boolean __ok = __og.invoke();
         if (!__og.is().isEmpty()) {
            try {
               if (!__ok) {
                  try {
                     __og.throwUserException();
                  } catch (UserException var9) {
                     throw new UnknownUserException(var9.ice_name());
                  }
               }

               __og.is().skipEmptyEncaps();
            } catch (LocalException var10) {
               throw new LocalExceptionWrapper(var10, false);
            }
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void putAlertMessage(String message, String title, short timeout, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("putAlertMessage", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(message);
            __os.writeString(title);
            __os.writeShort(timeout);
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

   public void putAlertMessageOneWay(String message, String title, short timeout, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("putAlertMessageOneWay", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(message);
            __os.writeString(title);
            __os.writeShort(timeout);
         } catch (LocalException var15) {
            __og.abort(var15);
         }

         boolean __ok = __og.invoke();
         if (!__og.is().isEmpty()) {
            try {
               if (!__ok) {
                  try {
                     __og.throwUserException();
                  } catch (UserException var13) {
                     throw new UnknownUserException(var13.ice_name());
                  }
               }

               __og.is().skipEmptyEncaps();
            } catch (LocalException var14) {
               throw new LocalExceptionWrapper(var14, false);
            }
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
         } catch (LocalException var14) {
            __og.abort(var14);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var12) {
                  throw var12;
               } catch (UserException var13) {
                  throw new UnknownUserException(var13.ice_name());
               }
            }

            __og.is().skipEmptyEncaps();
         } catch (LocalException var15) {
            throw new LocalExceptionWrapper(var15, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void putMessageOneWay(MessageDataIce message, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("putMessageOneWay", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            message.__write(__os);
         } catch (LocalException var13) {
            __og.abort(var13);
         }

         boolean __ok = __og.invoke();
         if (!__og.is().isEmpty()) {
            try {
               if (!__ok) {
                  try {
                     __og.throwUserException();
                  } catch (UserException var11) {
                     throw new UnknownUserException(var11.ice_name());
                  }
               }

               __og.is().skipEmptyEncaps();
            } catch (LocalException var12) {
               throw new LocalExceptionWrapper(var12, false);
            }
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void putSerializedPacket(byte[] packet, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("putSerializedPacket", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            ByteArrayHelper.write(__os, packet);
         } catch (LocalException var14) {
            __og.abort(var14);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var12) {
                  throw var12;
               } catch (UserException var13) {
                  throw new UnknownUserException(var13.ice_name());
               }
            }

            __og.is().skipEmptyEncaps();
         } catch (LocalException var15) {
            throw new LocalExceptionWrapper(var15, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void putSerializedPacketOneWay(byte[] packet, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("putSerializedPacketOneWay", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            ByteArrayHelper.write(__os, packet);
         } catch (LocalException var13) {
            __og.abort(var13);
         }

         boolean __ok = __og.invoke();
         if (!__og.is().isEmpty()) {
            try {
               if (!__ok) {
                  try {
                     __og.throwUserException();
                  } catch (UserException var11) {
                     throw new UnknownUserException(var11.ice_name());
                  }
               }

               __og.is().skipEmptyEncaps();
            } catch (LocalException var12) {
               throw new LocalExceptionWrapper(var12, false);
            }
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void sendGroupChatParticipantArrays(String groupChatId, byte imType, String[] participants, String[] mutedParticipants, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("sendGroupChatParticipantArrays", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(groupChatId);
            __os.writeByte(imType);
            StringArrayHelper.write(__os, participants);
            StringArrayHelper.write(__os, mutedParticipants);
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

   public void sendGroupChatParticipants(String groupChatId, byte imType, String participants, String mutedParticipants, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("sendGroupChatParticipants", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(groupChatId);
            __os.writeByte(imType);
            __os.writeString(participants);
            __os.writeString(mutedParticipants);
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

   public void sendMessage(MessageDataIce message, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("sendMessage", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            message.__write(__os);
         } catch (LocalException var14) {
            __og.abort(var14);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var12) {
                  throw var12;
               } catch (UserException var13) {
                  throw new UnknownUserException(var13.ice_name());
               }
            }

            __og.is().skipEmptyEncaps();
         } catch (LocalException var15) {
            throw new LocalExceptionWrapper(var15, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void sendMessageBackToUserAsEmote(MessageDataIce message, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("sendMessageBackToUserAsEmote", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            message.__write(__os);
         } catch (LocalException var14) {
            __og.abort(var14);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var12) {
                  throw var12;
               } catch (UserException var13) {
                  throw new UnknownUserException(var13.ice_name());
               }
            }

            __og.is().skipEmptyEncaps();
         } catch (LocalException var15) {
            throw new LocalExceptionWrapper(var15, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void setChatListVersion(int version, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("setChatListVersion", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeInt(version);
         } catch (LocalException var14) {
            __og.abort(var14);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var12) {
                  throw var12;
               } catch (UserException var13) {
                  throw new UnknownUserException(var13.ice_name());
               }
            }

            __og.is().skipEmptyEncaps();
         } catch (LocalException var15) {
            throw new LocalExceptionWrapper(var15, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void setCurrentChatListGroupChatSubset(ChatListIce ccl, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("setCurrentChatListGroupChatSubset", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            ccl.__write(__os);
         } catch (LocalException var13) {
            __og.abort(var13);
         }

         boolean __ok = __og.invoke();
         if (!__og.is().isEmpty()) {
            try {
               if (!__ok) {
                  try {
                     __og.throwUserException();
                  } catch (UserException var11) {
                     throw new UnknownUserException(var11.ice_name());
                  }
               }

               __og.is().skipEmptyEncaps();
            } catch (LocalException var12) {
               throw new LocalExceptionWrapper(var12, false);
            }
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void setLanguage(String language, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("setLanguage", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(language);
         } catch (LocalException var13) {
            __og.abort(var13);
         }

         boolean __ok = __og.invoke();
         if (!__og.is().isEmpty()) {
            try {
               if (!__ok) {
                  try {
                     __og.throwUserException();
                  } catch (UserException var11) {
                     throw new UnknownUserException(var11.ice_name());
                  }
               }

               __og.is().skipEmptyEncaps();
            } catch (LocalException var12) {
               throw new LocalExceptionWrapper(var12, false);
            }
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void setPresence(int presence, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("setPresence", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeInt(presence);
         } catch (LocalException var14) {
            __og.abort(var14);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var12) {
                  throw var12;
               } catch (UserException var13) {
                  throw new UnknownUserException(var13.ice_name());
               }
            }

            __og.is().skipEmptyEncaps();
         } catch (LocalException var15) {
            throw new LocalExceptionWrapper(var15, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void silentlyDropIncomingPackets(Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("silentlyDropIncomingPackets", OperationMode.Normal, __ctx);

      try {
         boolean __ok = __og.invoke();
         if (!__og.is().isEmpty()) {
            try {
               if (!__ok) {
                  try {
                     __og.throwUserException();
                  } catch (UserException var9) {
                     throw new UnknownUserException(var9.ice_name());
                  }
               }

               __og.is().skipEmptyEncaps();
            } catch (LocalException var10) {
               throw new LocalExceptionWrapper(var10, false);
            }
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void statusMessageSet(Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("statusMessageSet", OperationMode.Normal, __ctx);

      try {
         boolean __ok = __og.invoke();
         if (!__og.is().isEmpty()) {
            try {
               if (!__ok) {
                  try {
                     __og.throwUserException();
                  } catch (UserException var9) {
                     throw new UnknownUserException(var9.ice_name());
                  }
               }

               __og.is().skipEmptyEncaps();
            } catch (LocalException var10) {
               throw new LocalExceptionWrapper(var10, false);
            }
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void themeUpdated(Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("themeUpdated", OperationMode.Normal, __ctx);

      try {
         boolean __ok = __og.invoke();
         if (!__og.is().isEmpty()) {
            try {
               if (!__ok) {
                  try {
                     __og.throwUserException();
                  } catch (UserException var9) {
                     throw new UnknownUserException(var9.ice_name());
                  }
               }

               __og.is().skipEmptyEncaps();
            } catch (LocalException var10) {
               throw new LocalExceptionWrapper(var10, false);
            }
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void touch(Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("touch", OperationMode.Normal, __ctx);

      try {
         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var10) {
                  throw var10;
               } catch (UserException var11) {
                  throw new UnknownUserException(var11.ice_name());
               }
            }

            __og.is().skipEmptyEncaps();
         } catch (LocalException var12) {
            throw new LocalExceptionWrapper(var12, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }
}
