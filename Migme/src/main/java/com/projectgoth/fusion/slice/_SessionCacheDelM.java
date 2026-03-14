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

public final class _SessionCacheDelM extends _ObjectDelM implements _SessionCacheDel {
   public void logSession(SessionIce session, SessionMetricsIce sessionMetrics, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("logSession", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            session.__write(__os);
            sessionMetrics.__write(__os);
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
}
