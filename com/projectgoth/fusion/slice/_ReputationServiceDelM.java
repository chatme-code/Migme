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

public final class _ReputationServiceDelM extends _ObjectDelM implements _ReputationServiceDel {
   public void gatherAndProcess(Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("gatherAndProcess", OperationMode.Normal, __ctx);

      try {
         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var11) {
                  throw var11;
               } catch (UserException var12) {
                  throw new UnknownUserException(var12.ice_name());
               }
            }

            __og.is().skipEmptyEncaps();
         } catch (LocalException var13) {
            throw new LocalExceptionWrapper(var13, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public int getUserLevel(String username, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("getUserLevel", OperationMode.Normal, __ctx);

      int var7;
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
            int __ret = __is.readInt();
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

   public ScoreAndLevel[] getUserScoreAndLevels(int[] userIDs, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("getUserScoreAndLevels", OperationMode.Normal, __ctx);

      ScoreAndLevel[] var7;
      try {
         try {
            BasicStream __os = __og.os();
            IntArrayHelper.write(__os, userIDs);
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
            ScoreAndLevel[] __ret = ScoreAndLevelSequenceHelper.read(__is);
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

   public void processPreviouslyDumpedData(String runDateString, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("processPreviouslyDumpedData", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(runDateString);
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

   public void processPreviouslySortedData(String runDateString, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("processPreviouslySortedData", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(runDateString);
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

   public void updateLastRunDate(Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("updateLastRunDate", OperationMode.Normal, __ctx);

      try {
         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var11) {
                  throw var11;
               } catch (UserException var12) {
                  throw new UnknownUserException(var12.ice_name());
               }
            }

            __og.is().skipEmptyEncaps();
         } catch (LocalException var13) {
            throw new LocalExceptionWrapper(var13, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void updateScoreFromPreviouslyProcessedData(String runDateString, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("updateScoreFromPreviouslyProcessedData", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(runDateString);
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
}
