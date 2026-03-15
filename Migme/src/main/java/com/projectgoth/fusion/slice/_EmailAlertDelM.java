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

public final class _EmailAlertDelM extends _ObjectDelM implements _EmailAlertDel {
   public void requestUnreadEmailCount(String username, String password, UserPrx userProxy, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("requestUnreadEmailCount", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(username);
            __os.writeString(password);
            UserPrxHelper.__write(__os, userProxy);
         } catch (LocalException var16) {
            __og.abort(var16);
         }

         boolean __ok = __og.invoke();
         if (!__og.is().isEmpty()) {
            try {
               if (!__ok) {
                  try {
                     __og.throwUserException();
                  } catch (UserException var14) {
                     throw new UnknownUserException(var14.ice_name());
                  }
               }

               __og.is().skipEmptyEncaps();
            } catch (LocalException var15) {
               throw new LocalExceptionWrapper(var15, false);
            }
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }
}
