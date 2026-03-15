package com.projectgoth.fusion.slice;

import Ice.FacetNotExistException;
import Ice.ObjectPrx;
import Ice.ObjectPrxHelperBase;
import Ice._ObjectDelD;
import Ice._ObjectDelM;
import IceInternal.BasicStream;
import java.util.Map;

public final class SMSUserNotificationPrxHelper extends ObjectPrxHelperBase implements SMSUserNotificationPrx {
   public static SMSUserNotificationPrx checkedCast(ObjectPrx __obj) {
      SMSUserNotificationPrx __d = null;
      if (__obj != null) {
         try {
            __d = (SMSUserNotificationPrx)__obj;
         } catch (ClassCastException var4) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::SMSUserNotification")) {
               SMSUserNotificationPrxHelper __h = new SMSUserNotificationPrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (SMSUserNotificationPrx)__d;
   }

   public static SMSUserNotificationPrx checkedCast(ObjectPrx __obj, Map<String, String> __ctx) {
      SMSUserNotificationPrx __d = null;
      if (__obj != null) {
         try {
            __d = (SMSUserNotificationPrx)__obj;
         } catch (ClassCastException var5) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::SMSUserNotification", __ctx)) {
               SMSUserNotificationPrxHelper __h = new SMSUserNotificationPrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (SMSUserNotificationPrx)__d;
   }

   public static SMSUserNotificationPrx checkedCast(ObjectPrx __obj, String __facet) {
      SMSUserNotificationPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::SMSUserNotification")) {
               SMSUserNotificationPrxHelper __h = new SMSUserNotificationPrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var5) {
         }
      }

      return __d;
   }

   public static SMSUserNotificationPrx checkedCast(ObjectPrx __obj, String __facet, Map<String, String> __ctx) {
      SMSUserNotificationPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::SMSUserNotification", __ctx)) {
               SMSUserNotificationPrxHelper __h = new SMSUserNotificationPrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var6) {
         }
      }

      return __d;
   }

   public static SMSUserNotificationPrx uncheckedCast(ObjectPrx __obj) {
      SMSUserNotificationPrx __d = null;
      if (__obj != null) {
         try {
            __d = (SMSUserNotificationPrx)__obj;
         } catch (ClassCastException var4) {
            SMSUserNotificationPrxHelper __h = new SMSUserNotificationPrxHelper();
            __h.__copyFrom(__obj);
            __d = __h;
         }
      }

      return (SMSUserNotificationPrx)__d;
   }

   public static SMSUserNotificationPrx uncheckedCast(ObjectPrx __obj, String __facet) {
      SMSUserNotificationPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);
         SMSUserNotificationPrxHelper __h = new SMSUserNotificationPrxHelper();
         __h.__copyFrom(__bb);
         __d = __h;
      }

      return __d;
   }

   protected _ObjectDelM __createDelegateM() {
      return new _SMSUserNotificationDelM();
   }

   protected _ObjectDelD __createDelegateD() {
      return new _SMSUserNotificationDelD();
   }

   public static void __write(BasicStream __os, SMSUserNotificationPrx v) {
      __os.writeProxy(v);
   }

   public static SMSUserNotificationPrx __read(BasicStream __is) {
      ObjectPrx proxy = __is.readProxy();
      if (proxy != null) {
         SMSUserNotificationPrxHelper result = new SMSUserNotificationPrxHelper();
         result.__copyFrom(proxy);
         return result;
      } else {
         return null;
      }
   }
}
