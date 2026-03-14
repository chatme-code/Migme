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

public final class _BotHunterDelD extends _ObjectDelD implements _BotHunterDel {
   public SuspectGroupIce[] getLatestSuspects(Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "getLatestSuspects", OperationMode.Normal, __ctx);
      final SuspectGroupIceArrayHolder __result = new SuspectGroupIceArrayHolder();
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               BotHunter __servant = null;

               try {
                  __servant = (BotHunter)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __result.value = __servant.getLatestSuspects(__current);
                  return DispatchStatus.DispatchOK;
               } catch (UserException var4) {
                  this.setUserException(var4);
                  return DispatchStatus.DispatchUserException;
               }
            }
         };

         SuspectGroupIce[] var6;
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
      } catch (FusionException var14) {
         throw var14;
      } catch (SystemException var15) {
         throw var15;
      } catch (Throwable var16) {
         LocalExceptionWrapper.throwWrapper(var16);
         return __result.value;
      }
   }
}
