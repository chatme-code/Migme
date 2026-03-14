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

public final class _BotHunterDelM extends _ObjectDelM implements _BotHunterDel {
   public SuspectGroupIce[] getLatestSuspects(Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("getLatestSuspects", OperationMode.Normal, __ctx);

      SuspectGroupIce[] var6;
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
            SuspectGroupIce[] __ret = SuspectGroupIceArrayHelper.read(__is);
            __is.endReadEncaps();
            var6 = __ret;
         } catch (LocalException var15) {
            throw new LocalExceptionWrapper(var15, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var6;
   }
}
