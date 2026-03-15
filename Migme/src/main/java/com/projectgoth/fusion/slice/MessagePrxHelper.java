package com.projectgoth.fusion.slice;

import Ice.FacetNotExistException;
import Ice.ObjectPrx;
import Ice.ObjectPrxHelperBase;
import Ice._ObjectDelD;
import Ice._ObjectDelM;
import IceInternal.BasicStream;
import java.util.Map;

public final class MessagePrxHelper extends ObjectPrxHelperBase implements MessagePrx {
   public static MessagePrx checkedCast(ObjectPrx __obj) {
      MessagePrx __d = null;
      if (__obj != null) {
         try {
            __d = (MessagePrx)__obj;
         } catch (ClassCastException var4) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::Message")) {
               MessagePrxHelper __h = new MessagePrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (MessagePrx)__d;
   }

   public static MessagePrx checkedCast(ObjectPrx __obj, Map<String, String> __ctx) {
      MessagePrx __d = null;
      if (__obj != null) {
         try {
            __d = (MessagePrx)__obj;
         } catch (ClassCastException var5) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::Message", __ctx)) {
               MessagePrxHelper __h = new MessagePrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (MessagePrx)__d;
   }

   public static MessagePrx checkedCast(ObjectPrx __obj, String __facet) {
      MessagePrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::Message")) {
               MessagePrxHelper __h = new MessagePrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var5) {
         }
      }

      return __d;
   }

   public static MessagePrx checkedCast(ObjectPrx __obj, String __facet, Map<String, String> __ctx) {
      MessagePrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::Message", __ctx)) {
               MessagePrxHelper __h = new MessagePrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var6) {
         }
      }

      return __d;
   }

   public static MessagePrx uncheckedCast(ObjectPrx __obj) {
      MessagePrx __d = null;
      if (__obj != null) {
         try {
            __d = (MessagePrx)__obj;
         } catch (ClassCastException var4) {
            MessagePrxHelper __h = new MessagePrxHelper();
            __h.__copyFrom(__obj);
            __d = __h;
         }
      }

      return (MessagePrx)__d;
   }

   public static MessagePrx uncheckedCast(ObjectPrx __obj, String __facet) {
      MessagePrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);
         MessagePrxHelper __h = new MessagePrxHelper();
         __h.__copyFrom(__bb);
         __d = __h;
      }

      return __d;
   }

   protected _ObjectDelM __createDelegateM() {
      return new _MessageDelM();
   }

   protected _ObjectDelD __createDelegateD() {
      return new _MessageDelD();
   }

   public static void __write(BasicStream __os, MessagePrx v) {
      __os.writeProxy(v);
   }

   public static MessagePrx __read(BasicStream __is) {
      ObjectPrx proxy = __is.readProxy();
      if (proxy != null) {
         MessagePrxHelper result = new MessagePrxHelper();
         result.__copyFrom(proxy);
         return result;
      } else {
         return null;
      }
   }
}
