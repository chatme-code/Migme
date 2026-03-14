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

public final class _CallMakerDelM extends _ObjectDelM implements _CallMakerDel {
   public CallDataIce requestCallback(CallDataIce call, int maxDuration, int retries, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("requestCallback", OperationMode.Normal, __ctx);

      CallDataIce var9;
      try {
         try {
            BasicStream __os = __og.os();
            call.__write(__os);
            __os.writeInt(maxDuration);
            __os.writeInt(retries);
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
            CallDataIce __ret = new CallDataIce();
            __ret.__read(__is);
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
}
