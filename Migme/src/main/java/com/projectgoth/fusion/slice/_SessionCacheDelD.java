package com.projectgoth.fusion.slice;

import Ice.Current;
import Ice.DispatchStatus;
import Ice.Object;
import Ice.OperationMode;
import Ice.OperationNotExistException;
import Ice.SystemException;
import Ice._ObjectDelD;
import IceInternal.Direct;
import IceInternal.LocalExceptionWrapper;
import java.util.Map;

public final class _SessionCacheDelD extends _ObjectDelD implements _SessionCacheDel {
   public void logSession(final SessionIce session, final SessionMetricsIce sessionMetrics, Map<String, String> __ctx) throws LocalExceptionWrapper {
      final Current __current = new Current();
      this.__initCurrent(__current, "logSession", OperationMode.Normal, __ctx);
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               SessionCache __servant = null;

               try {
                  __servant = (SessionCache)__obj;
               } catch (ClassCastException var4) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               __servant.logSession(session, sessionMetrics, __current);
               return DispatchStatus.DispatchOK;
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
      } catch (SystemException var13) {
         throw var13;
      } catch (Throwable var14) {
         LocalExceptionWrapper.throwWrapper(var14);
      }

   }
}
