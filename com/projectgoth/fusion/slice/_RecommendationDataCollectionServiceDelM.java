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

public final class _RecommendationDataCollectionServiceDelM extends _ObjectDelM implements _RecommendationDataCollectionServiceDel {
   public void logData(CollectedDataIce dataIce, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionExceptionWithRefCode {
      Outgoing __og = this.__handler.getOutgoing("logData", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeObject(dataIce);
            __os.writePendingObjects();
         } catch (LocalException var15) {
            __og.abort(var15);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionExceptionWithRefCode var13) {
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
