package com.projectgoth.fusion.slice;

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

public final class _RecommendationDataCollectionServiceDelD extends _ObjectDelD implements _RecommendationDataCollectionServiceDel {
   public void logData(final CollectedDataIce dataIce, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionExceptionWithRefCode {
      final Current __current = new Current();
      this.__initCurrent(__current, "logData", OperationMode.Normal, __ctx);
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               RecommendationDataCollectionService __servant = null;

               try {
                  __servant = (RecommendationDataCollectionService)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __servant.logData(dataIce, __current);
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
      } catch (FusionExceptionWithRefCode var12) {
         throw var12;
      } catch (SystemException var13) {
         throw var13;
      } catch (Throwable var14) {
         LocalExceptionWrapper.throwWrapper(var14);
      }

   }
}
