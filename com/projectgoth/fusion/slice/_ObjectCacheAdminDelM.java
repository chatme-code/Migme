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

public final class _ObjectCacheAdminDelM extends _ObjectDelM implements _ObjectCacheAdminDel {
   public int getLoadWeightage(Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("getLoadWeightage", OperationMode.Normal, __ctx);

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

   public ObjectCacheStats getStats(Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("getStats", OperationMode.Normal, __ctx);

      ObjectCacheStats var6;
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
            ObjectCacheStatsHolder __ret = new ObjectCacheStatsHolder();
            __is.readObject(__ret.getPatcher());
            __is.readPendingObjects();
            __is.endReadEncaps();
            var6 = __ret.value;
         } catch (LocalException var15) {
            throw new LocalExceptionWrapper(var15, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var6;
   }

   public String[] getUsernames(Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("getUsernames", OperationMode.Normal, __ctx);

      String[] var6;
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
            String[] __ret = StringArrayHelper.read(__is);
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

   public int ping(Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("ping", OperationMode.Normal, __ctx);

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

   public void reloadEmotes(Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("reloadEmotes", OperationMode.Normal, __ctx);

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

   public void setLoadWeightage(int weightage, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("setLoadWeightage", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeInt(weightage);
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
}
