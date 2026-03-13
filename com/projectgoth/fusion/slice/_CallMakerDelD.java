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

public final class _CallMakerDelD extends _ObjectDelD implements _CallMakerDel {
   public CallDataIce requestCallback(final CallDataIce call, final int maxDuration, final int retries, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "requestCallback", OperationMode.Normal, __ctx);
      final CallDataIceHolder __result = new CallDataIceHolder();
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               CallMaker __servant = null;

               try {
                  __servant = (CallMaker)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __result.value = __servant.requestCallback(call, maxDuration, retries, __current);
                  return DispatchStatus.DispatchOK;
               } catch (UserException var4) {
                  this.setUserException(var4);
                  return DispatchStatus.DispatchUserException;
               }
            }
         };

         CallDataIce var9;
         try {
            DispatchStatus __status = __direct.servant().__collocDispatch(__direct);
            if (__status == DispatchStatus.DispatchUserException) {
               __direct.throwUserException();
            }

            assert __status == DispatchStatus.DispatchOK;

            var9 = __result.value;
         } finally {
            __direct.destroy();
         }

         return var9;
      } catch (FusionException var17) {
         throw var17;
      } catch (SystemException var18) {
         throw var18;
      } catch (Throwable var19) {
         LocalExceptionWrapper.throwWrapper(var19);
         return __result.value;
      }
   }
}
