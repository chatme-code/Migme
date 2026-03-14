package com.projectgoth.fusion.slice;

import Ice.LocalException;
import IceInternal.BasicStream;
import IceInternal.Incoming;
import IceInternal.IncomingAsync;

final class _AMD_User_createSession extends IncomingAsync implements AMD_User_createSession {
   public _AMD_User_createSession(Incoming in) {
      super(in);
   }

   public void ice_response(SessionPrx __ret) {
      if (this.__validateResponse(true)) {
         try {
            BasicStream __os = this.__os();
            SessionPrxHelper.__write(__os, __ret);
         } catch (LocalException var3) {
            this.ice_exception(var3);
         }

         this.__response(true);
      }

   }

   public void ice_exception(Exception ex) {
      try {
         throw ex;
      } catch (FusionException var3) {
         if (this.__validateResponse(false)) {
            this.__os().writeUserException(var3);
            this.__response(false);
         }
      } catch (Exception var4) {
         if (this.__validateException(var4)) {
            this.__exception(var4);
         }
      }

   }
}
