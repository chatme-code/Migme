package com.projectgoth.fusion.slice;

import Ice.FacetNotExistException;
import Ice.ObjectPrx;
import Ice.ObjectPrxHelperBase;
import Ice._ObjectDelD;
import Ice._ObjectDelM;
import IceInternal.BasicStream;
import java.util.Map;

public final class GroupEventPrxHelper extends ObjectPrxHelperBase implements GroupEventPrx {
   public static GroupEventPrx checkedCast(ObjectPrx __obj) {
      GroupEventPrx __d = null;
      if (__obj != null) {
         try {
            __d = (GroupEventPrx)__obj;
         } catch (ClassCastException var4) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::GroupEvent")) {
               GroupEventPrxHelper __h = new GroupEventPrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (GroupEventPrx)__d;
   }

   public static GroupEventPrx checkedCast(ObjectPrx __obj, Map<String, String> __ctx) {
      GroupEventPrx __d = null;
      if (__obj != null) {
         try {
            __d = (GroupEventPrx)__obj;
         } catch (ClassCastException var5) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::GroupEvent", __ctx)) {
               GroupEventPrxHelper __h = new GroupEventPrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (GroupEventPrx)__d;
   }

   public static GroupEventPrx checkedCast(ObjectPrx __obj, String __facet) {
      GroupEventPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::GroupEvent")) {
               GroupEventPrxHelper __h = new GroupEventPrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var5) {
         }
      }

      return __d;
   }

   public static GroupEventPrx checkedCast(ObjectPrx __obj, String __facet, Map<String, String> __ctx) {
      GroupEventPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::GroupEvent", __ctx)) {
               GroupEventPrxHelper __h = new GroupEventPrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var6) {
         }
      }

      return __d;
   }

   public static GroupEventPrx uncheckedCast(ObjectPrx __obj) {
      GroupEventPrx __d = null;
      if (__obj != null) {
         try {
            __d = (GroupEventPrx)__obj;
         } catch (ClassCastException var4) {
            GroupEventPrxHelper __h = new GroupEventPrxHelper();
            __h.__copyFrom(__obj);
            __d = __h;
         }
      }

      return (GroupEventPrx)__d;
   }

   public static GroupEventPrx uncheckedCast(ObjectPrx __obj, String __facet) {
      GroupEventPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);
         GroupEventPrxHelper __h = new GroupEventPrxHelper();
         __h.__copyFrom(__bb);
         __d = __h;
      }

      return __d;
   }

   protected _ObjectDelM __createDelegateM() {
      return new _GroupEventDelM();
   }

   protected _ObjectDelD __createDelegateD() {
      return new _GroupEventDelD();
   }

   public static void __write(BasicStream __os, GroupEventPrx v) {
      __os.writeProxy(v);
   }

   public static GroupEventPrx __read(BasicStream __is) {
      ObjectPrx proxy = __is.readProxy();
      if (proxy != null) {
         GroupEventPrxHelper result = new GroupEventPrxHelper();
         result.__copyFrom(proxy);
         return result;
      } else {
         return null;
      }
   }
}
