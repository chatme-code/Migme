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

public final class _MessageLoggerDelM extends _ObjectDelM implements _MessageLoggerDel {
   public void logMessage(int type, int sourceCountryID, String source, String destination, int numRecipients, String messageText, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("logMessage", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeInt(type);
            __os.writeInt(sourceCountryID);
            __os.writeString(source);
            __os.writeString(destination);
            __os.writeInt(numRecipients);
            __os.writeString(messageText);
         } catch (LocalException var19) {
            __og.abort(var19);
         }

         boolean __ok = __og.invoke();
         if (!__og.is().isEmpty()) {
            try {
               if (!__ok) {
                  try {
                     __og.throwUserException();
                  } catch (UserException var17) {
                     throw new UnknownUserException(var17.ice_name());
                  }
               }

               __og.is().skipEmptyEncaps();
            } catch (LocalException var18) {
               throw new LocalExceptionWrapper(var18, false);
            }
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }
}
