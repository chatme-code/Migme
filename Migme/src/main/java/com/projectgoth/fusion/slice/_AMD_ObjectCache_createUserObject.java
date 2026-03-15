package com.projectgoth.fusion.slice;

import Ice.LocalException;
import IceInternal.BasicStream;
import IceInternal.Incoming;
import IceInternal.IncomingAsync;

final class _AMD_ObjectCache_createUserObject extends IncomingAsync implements AMD_ObjectCache_createUserObject {
   public _AMD_ObjectCache_createUserObject(Incoming in) {
      super(in);
   }

   public void ice_response(UserPrx __ret) {
      if (this.__validateResponse(true)) {
         try {
            BasicStream __os = this.__os();
            UserPrxHelper.__write(__os, __ret);
         } catch (LocalException var3) {
            this.ice_exception(var3);
         }

         this.__response(true);
      }

   }

   public void ice_exception(Exception ex) {
      try {
         throw ex;
      } catch (ObjectExistsException var3) {
         if (this.__validateResponse(false)) {
            this.__os().writeUserException(var3);
            this.__response(false);
         }
      } catch (FusionException var4) {
         if (this.__validateResponse(false)) {
            this.__os().writeUserException(var4);
            this.__response(false);
         }
      } catch (Exception var5) {
         if (this.__validateException(var5)) {
            this.__exception(var5);
         }
      }

   }
}
