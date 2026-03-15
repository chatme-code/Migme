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

public final class _RecommendationDataCollectionServiceAdminDelD extends _ObjectDelD implements _RecommendationDataCollectionServiceAdminDel {
   public RecommendationDataCollectionServiceStats getStats(Map<String, String> __ctx) throws LocalExceptionWrapper, FusionExceptionWithRefCode {
      final Current __current = new Current();
      this.__initCurrent(__current, "getStats", OperationMode.Normal, __ctx);
      final RecommendationDataCollectionServiceStatsHolder __result = new RecommendationDataCollectionServiceStatsHolder();
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               RecommendationDataCollectionServiceAdmin __servant = null;

               try {
                  __servant = (RecommendationDataCollectionServiceAdmin)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __result.value = __servant.getStats(__current);
                  return DispatchStatus.DispatchOK;
               } catch (UserException var4) {
                  this.setUserException(var4);
                  return DispatchStatus.DispatchUserException;
               }
            }
         };

         RecommendationDataCollectionServiceStats var6;
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
      } catch (FusionExceptionWithRefCode var14) {
         throw var14;
      } catch (SystemException var15) {
         throw var15;
      } catch (Throwable var16) {
         LocalExceptionWrapper.throwWrapper(var16);
         return __result.value;
      }
   }
}
