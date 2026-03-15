package com.projectgoth.fusion.slice;

import Ice.LocalException;
import Ice.ObjectPrx;
import Ice.ObjectPrxHelperBase;
import Ice.OperationMode;
import Ice.UnknownUserException;
import Ice.UserException;
import IceInternal.OutgoingAsync;
import java.util.Map;

public abstract class AMI_Connection_putMessageAsync extends OutgoingAsync {
   public abstract void ice_response();

   public abstract void ice_exception(LocalException var1);

   public abstract void ice_exception(UserException var1);

   public final boolean __invoke(ObjectPrx __prx, AMI_Connection_putMessageAsync __cb, MessageDataIce message, Map<String, String> __ctx) {
      this.__acquireCallback(__prx);

      try {
         ((ObjectPrxHelperBase)__prx).__checkTwowayOnly("putMessageAsync");
         this.__prepare(__prx, "putMessageAsync", OperationMode.Normal, __ctx);
         message.__write(this.__os);
         this.__os.endWriteEncaps();
         return this.__send();
      } catch (LocalException var6) {
         this.__releaseCallback(var6);
         return false;
      }
   }

   protected final void __response(boolean __ok) {
      try {
         if (!__ok) {
            try {
               this.__throwUserException();
            } catch (FusionException var13) {
               throw var13;
            } catch (UserException var14) {
               throw new UnknownUserException(var14.ice_name());
            }
         }

         this.__is.skipEmptyEncaps();
      } catch (UserException var15) {
         UserException __ex = var15;

         try {
            this.ice_exception(__ex);
         } catch (Exception var11) {
            this.__warning(var11);
         } finally {
            this.__releaseCallback();
         }

         return;
      } catch (LocalException var16) {
         this.__finished(var16);
         return;
      }

      this.ice_response();
      this.__releaseCallback();
   }
}
