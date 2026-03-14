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

public final class _MessageLoggerDelD extends _ObjectDelD implements _MessageLoggerDel {
   public void logMessage(final int type, final int sourceCountryID, final String source, final String destination, final int numRecipients, final String messageText, Map<String, String> __ctx) throws LocalExceptionWrapper {
      final Current __current = new Current();
      this.__initCurrent(__current, "logMessage", OperationMode.Normal, __ctx);
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               MessageLogger __servant = null;

               try {
                  __servant = (MessageLogger)__obj;
               } catch (ClassCastException var4) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               __servant.logMessage(type, sourceCountryID, source, destination, numRecipients, messageText, __current);
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
      } catch (SystemException var17) {
         throw var17;
      } catch (Throwable var18) {
         LocalExceptionWrapper.throwWrapper(var18);
      }

   }
}
