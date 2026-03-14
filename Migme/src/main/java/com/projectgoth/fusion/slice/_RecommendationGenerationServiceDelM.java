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

public final class _RecommendationGenerationServiceDelM extends _ObjectDelM implements _RecommendationGenerationServiceDel {
   public void runTransformation(int transformationID, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("runTransformation", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeInt(transformationID);
         } catch (LocalException var14) {
            __og.abort(var14);
         }

         boolean __ok = __og.invoke();
         if (!__og.is().isEmpty()) {
            try {
               if (!__ok) {
                  try {
                     __og.throwUserException();
                  } catch (UserException var12) {
                     throw new UnknownUserException(var12.ice_name());
                  }
               }

               __og.is().skipEmptyEncaps();
            } catch (LocalException var13) {
               throw new LocalExceptionWrapper(var13, false);
            }
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }
}
