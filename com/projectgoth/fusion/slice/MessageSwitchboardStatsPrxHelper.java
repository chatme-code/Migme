package com.projectgoth.fusion.slice;

import Ice.FacetNotExistException;
import Ice.ObjectPrx;
import Ice.ObjectPrxHelperBase;
import Ice._ObjectDelD;
import Ice._ObjectDelM;
import IceInternal.BasicStream;
import java.util.Map;

public final class MessageSwitchboardStatsPrxHelper extends ObjectPrxHelperBase implements MessageSwitchboardStatsPrx {
   public static MessageSwitchboardStatsPrx checkedCast(ObjectPrx __obj) {
      MessageSwitchboardStatsPrx __d = null;
      if (__obj != null) {
         try {
            __d = (MessageSwitchboardStatsPrx)__obj;
         } catch (ClassCastException var4) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::MessageSwitchboardStats")) {
               MessageSwitchboardStatsPrxHelper __h = new MessageSwitchboardStatsPrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (MessageSwitchboardStatsPrx)__d;
   }

   public static MessageSwitchboardStatsPrx checkedCast(ObjectPrx __obj, Map<String, String> __ctx) {
      MessageSwitchboardStatsPrx __d = null;
      if (__obj != null) {
         try {
            __d = (MessageSwitchboardStatsPrx)__obj;
         } catch (ClassCastException var5) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::MessageSwitchboardStats", __ctx)) {
               MessageSwitchboardStatsPrxHelper __h = new MessageSwitchboardStatsPrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (MessageSwitchboardStatsPrx)__d;
   }

   public static MessageSwitchboardStatsPrx checkedCast(ObjectPrx __obj, String __facet) {
      MessageSwitchboardStatsPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::MessageSwitchboardStats")) {
               MessageSwitchboardStatsPrxHelper __h = new MessageSwitchboardStatsPrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var5) {
         }
      }

      return __d;
   }

   public static MessageSwitchboardStatsPrx checkedCast(ObjectPrx __obj, String __facet, Map<String, String> __ctx) {
      MessageSwitchboardStatsPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::MessageSwitchboardStats", __ctx)) {
               MessageSwitchboardStatsPrxHelper __h = new MessageSwitchboardStatsPrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var6) {
         }
      }

      return __d;
   }

   public static MessageSwitchboardStatsPrx uncheckedCast(ObjectPrx __obj) {
      MessageSwitchboardStatsPrx __d = null;
      if (__obj != null) {
         try {
            __d = (MessageSwitchboardStatsPrx)__obj;
         } catch (ClassCastException var4) {
            MessageSwitchboardStatsPrxHelper __h = new MessageSwitchboardStatsPrxHelper();
            __h.__copyFrom(__obj);
            __d = __h;
         }
      }

      return (MessageSwitchboardStatsPrx)__d;
   }

   public static MessageSwitchboardStatsPrx uncheckedCast(ObjectPrx __obj, String __facet) {
      MessageSwitchboardStatsPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);
         MessageSwitchboardStatsPrxHelper __h = new MessageSwitchboardStatsPrxHelper();
         __h.__copyFrom(__bb);
         __d = __h;
      }

      return __d;
   }

   protected _ObjectDelM __createDelegateM() {
      return new _MessageSwitchboardStatsDelM();
   }

   protected _ObjectDelD __createDelegateD() {
      return new _MessageSwitchboardStatsDelD();
   }

   public static void __write(BasicStream __os, MessageSwitchboardStatsPrx v) {
      __os.writeProxy(v);
   }

   public static MessageSwitchboardStatsPrx __read(BasicStream __is) {
      ObjectPrx proxy = __is.readProxy();
      if (proxy != null) {
         MessageSwitchboardStatsPrxHelper result = new MessageSwitchboardStatsPrxHelper();
         result.__copyFrom(proxy);
         return result;
      } else {
         return null;
      }
   }
}
