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

public final class _RegistryDelM extends _ObjectDelM implements _RegistryDel {
   public void deregisterBotService(String hostName, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("deregisterBotService", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(hostName);
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

   public void deregisterChatRoomObject(String name, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("deregisterChatRoomObject", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
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

   public void deregisterConnectionObject(String sessionID, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("deregisterConnectionObject", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(sessionID);
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

   public void deregisterGroupChatObject(String id, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("deregisterGroupChatObject", OperationMode.Normal, __ctx);

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

   public void deregisterMessageSwitchboard(String hostName, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("deregisterMessageSwitchboard", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(hostName);
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

   public void deregisterObjectCache(String hostName, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("deregisterObjectCache", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(hostName);
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

   public void deregisterUserObject(String username, String objectCacheHostname, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("deregisterUserObject", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(username);
            __os.writeString(objectCacheHostname);
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

   public ChatRoomPrx findChatRoomObject(String name, Map<String, String> __ctx) throws LocalExceptionWrapper, ObjectNotFoundException {
      Outgoing __og = this.__handler.getOutgoing("findChatRoomObject", OperationMode.Normal, __ctx);

      ChatRoomPrx var7;
      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(name);
         } catch (LocalException var17) {
            __og.abort(var17);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (ObjectNotFoundException var15) {
                  throw var15;
               } catch (UserException var16) {
                  throw new UnknownUserException(var16.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            ChatRoomPrx __ret = ChatRoomPrxHelper.__read(__is);
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

   public ChatRoomPrx[] findChatRoomObjects(String[] chatRoomNames, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("findChatRoomObjects", OperationMode.Normal, __ctx);

      ChatRoomPrx[] var7;
      try {
         try {
            BasicStream __os = __og.os();
            StringArrayHelper.write(__os, chatRoomNames);
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
            ChatRoomPrx[] __ret = ChatRoomProxyArrayHelper.read(__is);
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

   public ConnectionPrx findConnectionObject(String sessionID, Map<String, String> __ctx) throws LocalExceptionWrapper, ObjectNotFoundException {
      Outgoing __og = this.__handler.getOutgoing("findConnectionObject", OperationMode.Normal, __ctx);

      ConnectionPrx var7;
      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(sessionID);
         } catch (LocalException var17) {
            __og.abort(var17);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (ObjectNotFoundException var15) {
                  throw var15;
               } catch (UserException var16) {
                  throw new UnknownUserException(var16.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            ConnectionPrx __ret = ConnectionPrxHelper.__read(__is);
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

   public GroupChatPrx findGroupChatObject(String id, Map<String, String> __ctx) throws LocalExceptionWrapper, ObjectNotFoundException {
      Outgoing __og = this.__handler.getOutgoing("findGroupChatObject", OperationMode.Normal, __ctx);

      GroupChatPrx var7;
      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(id);
         } catch (LocalException var17) {
            __og.abort(var17);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (ObjectNotFoundException var15) {
                  throw var15;
               } catch (UserException var16) {
                  throw new UnknownUserException(var16.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            GroupChatPrx __ret = GroupChatPrxHelper.__read(__is);
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

   public UserPrx findUserObject(String username, Map<String, String> __ctx) throws LocalExceptionWrapper, ObjectNotFoundException {
      Outgoing __og = this.__handler.getOutgoing("findUserObject", OperationMode.Normal, __ctx);

      UserPrx var7;
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
               } catch (ObjectNotFoundException var15) {
                  throw var15;
               } catch (UserException var16) {
                  throw new UnknownUserException(var16.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            UserPrx __ret = UserPrxHelper.__read(__is);
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

   public UserPrx[] findUserObjects(String[] usernames, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("findUserObjects", OperationMode.Normal, __ctx);

      UserPrx[] var7;
      try {
         try {
            BasicStream __os = __og.os();
            StringArrayHelper.write(__os, usernames);
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
            UserPrx[] __ret = UserProxyArrayHelper.read(__is);
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

   public Map<String, UserPrx> findUserObjectsMap(String[] usernames, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("findUserObjectsMap", OperationMode.Normal, __ctx);

      Map var7;
      try {
         try {
            BasicStream __os = __og.os();
            StringArrayHelper.write(__os, usernames);
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
            Map<String, UserPrx> __ret = UsernameToProxyMapHelper.read(__is);
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

   public BotServicePrx getLowestLoadedBotService(Map<String, String> __ctx) throws LocalExceptionWrapper, ObjectNotFoundException {
      Outgoing __og = this.__handler.getOutgoing("getLowestLoadedBotService", OperationMode.Normal, __ctx);

      BotServicePrx var6;
      try {
         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (ObjectNotFoundException var13) {
                  throw var13;
               } catch (UserException var14) {
                  throw new UnknownUserException(var14.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            BotServicePrx __ret = BotServicePrxHelper.__read(__is);
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

   public ObjectCachePrx getLowestLoadedObjectCache(Map<String, String> __ctx) throws LocalExceptionWrapper, ObjectNotFoundException {
      Outgoing __og = this.__handler.getOutgoing("getLowestLoadedObjectCache", OperationMode.Normal, __ctx);

      ObjectCachePrx var6;
      try {
         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (ObjectNotFoundException var13) {
                  throw var13;
               } catch (UserException var14) {
                  throw new UnknownUserException(var14.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            ObjectCachePrx __ret = ObjectCachePrxHelper.__read(__is);
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

   public int getUserCount(Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("getUserCount", OperationMode.Normal, __ctx);

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

   public int newGatewayID(Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("newGatewayID", OperationMode.Normal, __ctx);

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

   public void registerBotService(String hostName, int load, BotServicePrx serviceProxy, BotServiceAdminPrx adminProxy, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("registerBotService", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(hostName);
            __os.writeInt(load);
            BotServicePrxHelper.__write(__os, serviceProxy);
            BotServiceAdminPrxHelper.__write(__os, adminProxy);
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

   public void registerChatRoomObject(String name, ChatRoomPrx chatRoomProxy, Map<String, String> __ctx) throws LocalExceptionWrapper, ObjectExistsException {
      Outgoing __og = this.__handler.getOutgoing("registerChatRoomObject", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(name);
            ChatRoomPrxHelper.__write(__os, chatRoomProxy);
         } catch (LocalException var16) {
            __og.abort(var16);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (ObjectExistsException var14) {
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

   public void registerConnectionObject(String sessionID, ConnectionPrx connectionProxy, Map<String, String> __ctx) throws LocalExceptionWrapper, ObjectExistsException {
      Outgoing __og = this.__handler.getOutgoing("registerConnectionObject", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(sessionID);
            ConnectionPrxHelper.__write(__os, connectionProxy);
         } catch (LocalException var16) {
            __og.abort(var16);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (ObjectExistsException var14) {
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

   public void registerGroupChatObject(String id, GroupChatPrx groupChatProxy, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("registerGroupChatObject", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(id);
            GroupChatPrxHelper.__write(__os, groupChatProxy);
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

   public void registerMessageSwitchboard(String hostName, MessageSwitchboardPrx msbProxy, MessageSwitchboardAdminPrx adminProxy, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("registerMessageSwitchboard", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(hostName);
            MessageSwitchboardPrxHelper.__write(__os, msbProxy);
            MessageSwitchboardAdminPrxHelper.__write(__os, adminProxy);
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

   public void registerObjectCache(String hostName, ObjectCachePrx cacheProxy, ObjectCacheAdminPrx adminProxy, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("registerObjectCache", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(hostName);
            ObjectCachePrxHelper.__write(__os, cacheProxy);
            ObjectCacheAdminPrxHelper.__write(__os, adminProxy);
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

   public void registerObjectCacheStats(String objectCacheHostName, ObjectCacheStats stats, Map<String, String> __ctx) throws LocalExceptionWrapper, ObjectNotFoundException {
      Outgoing __og = this.__handler.getOutgoing("registerObjectCacheStats", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(objectCacheHostName);
            __os.writeObject(stats);
            __os.writePendingObjects();
         } catch (LocalException var16) {
            __og.abort(var16);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (ObjectNotFoundException var14) {
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

   public void registerUserObject(String username, UserPrx userProxy, String objectCacheHostname, Map<String, String> __ctx) throws LocalExceptionWrapper, ObjectExistsException {
      Outgoing __og = this.__handler.getOutgoing("registerUserObject", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(username);
            UserPrxHelper.__write(__os, userProxy);
            __os.writeString(objectCacheHostname);
         } catch (LocalException var17) {
            __og.abort(var17);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (ObjectExistsException var15) {
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
