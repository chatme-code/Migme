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

public final class _RegistryNodeDelM extends _ObjectDelM implements _RegistryNodeDel {
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

   public void registerMessageSwitchboard(String hostName, MessageSwitchboardPrx cacheProxy, MessageSwitchboardAdminPrx adminProxy, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("registerMessageSwitchboard", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(hostName);
            MessageSwitchboardPrxHelper.__write(__os, cacheProxy);
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

   public String registerNewNode(RegistryNodePrx newNodeProxy, String hostName, boolean replicate, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("registerNewNode", OperationMode.Normal, __ctx);

      String var9;
      try {
         try {
            BasicStream __os = __og.os();
            RegistryNodePrxHelper.__write(__os, newNodeProxy);
            __os.writeString(hostName);
            __os.writeBool(replicate);
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
            String __ret = __is.readString();
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
}
