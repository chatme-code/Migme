package com.projectgoth.fusion.slice;

import Ice.LocalException;
import Ice.ObjectPrx;
import Ice.ObjectPrxHelperBase;
import Ice.OperationMode;
import Ice.UnknownUserException;
import Ice.UserException;
import IceInternal.OutgoingAsync;
import java.util.Map;

public abstract class AMI_ObjectCache_getMessageSwitchboard extends OutgoingAsync {
   public abstract void ice_response(MessageSwitchboardPrx var1);

   public abstract void ice_exception(LocalException var1);

   public abstract void ice_exception(UserException var1);

   public final boolean __invoke(ObjectPrx __prx, AMI_ObjectCache_getMessageSwitchboard __cb, Map<String, String> __ctx) {
      this.__acquireCallback(__prx);

      try {
         ((ObjectPrxHelperBase)__prx).__checkTwowayOnly("getMessageSwitchboard");
         this.__prepare(__prx, "getMessageSwitchboard", OperationMode.Normal, __ctx);
         this.__os.endWriteEncaps();
         return this.__send();
      } catch (LocalException var5) {
         this.__releaseCallback(var5);
         return false;
      }
   }

   protected final void __response(boolean __ok) {
      MessageSwitchboardPrx __ret;
      try {
         if (!__ok) {
            try {
               this.__throwUserException();
            } catch (FusionException var14) {
               throw var14;
            } catch (UserException var15) {
               throw new UnknownUserException(var15.ice_name());
            }
         }

         this.__is.startReadEncaps();
         __ret = MessageSwitchboardPrxHelper.__read(this.__is);
         this.__is.endReadEncaps();
      } catch (UserException var16) {
         UserException __ex = var16;

         try {
            this.ice_exception(__ex);
         } catch (Exception var12) {
            this.__warning(var12);
         } finally {
            this.__releaseCallback();
         }

         return;
      } catch (LocalException var17) {
         this.__finished(var17);
         return;
      }

      this.ice_response(__ret);
      this.__releaseCallback();
   }
}
