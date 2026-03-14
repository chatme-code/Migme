package com.projectgoth.fusion.slice;

import IceInternal.Incoming;
import IceInternal.IncomingAsync;

final class _AMD_User_otherIMLogin extends IncomingAsync implements AMD_User_otherIMLogin {
   public _AMD_User_otherIMLogin(Incoming in) {
      super(in);
   }

   public void ice_response() {
      if (this.__validateResponse(true)) {
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
