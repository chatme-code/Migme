package com.projectgoth.fusion.slice.tests;

import Ice.LocalException;
import Ice.OperationMode;
import Ice.UnknownUserException;
import Ice.UserException;
import Ice._ObjectDelM;
import IceInternal.BasicStream;
import IceInternal.LocalExceptionWrapper;
import IceInternal.Outgoing;
import java.util.Map;

public final class _PrinterDelM extends _ObjectDelM implements _PrinterDel {
   public void circular(String s, int level, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("circular", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(s);
            __os.writeInt(level);
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

   public void printString(String s, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("printString", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(s);
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
