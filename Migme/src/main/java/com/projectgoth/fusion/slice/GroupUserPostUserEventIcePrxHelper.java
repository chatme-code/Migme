package com.projectgoth.fusion.slice;

import Ice.FacetNotExistException;
import Ice.ObjectPrx;
import Ice.ObjectPrxHelperBase;
import Ice._ObjectDelD;
import Ice._ObjectDelM;
import IceInternal.BasicStream;
import java.util.Map;

public final class GroupUserPostUserEventIcePrxHelper extends ObjectPrxHelperBase implements GroupUserPostUserEventIcePrx {
   public static GroupUserPostUserEventIcePrx checkedCast(ObjectPrx __obj) {
      GroupUserPostUserEventIcePrx __d = null;
      if (__obj != null) {
         try {
            __d = (GroupUserPostUserEventIcePrx)__obj;
         } catch (ClassCastException var4) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::GroupUserPostUserEventIce")) {
               GroupUserPostUserEventIcePrxHelper __h = new GroupUserPostUserEventIcePrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (GroupUserPostUserEventIcePrx)__d;
   }

   public static GroupUserPostUserEventIcePrx checkedCast(ObjectPrx __obj, Map<String, String> __ctx) {
      GroupUserPostUserEventIcePrx __d = null;
      if (__obj != null) {
         try {
            __d = (GroupUserPostUserEventIcePrx)__obj;
         } catch (ClassCastException var5) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::GroupUserPostUserEventIce", __ctx)) {
               GroupUserPostUserEventIcePrxHelper __h = new GroupUserPostUserEventIcePrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (GroupUserPostUserEventIcePrx)__d;
   }

   public static GroupUserPostUserEventIcePrx checkedCast(ObjectPrx __obj, String __facet) {
      GroupUserPostUserEventIcePrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::GroupUserPostUserEventIce")) {
               GroupUserPostUserEventIcePrxHelper __h = new GroupUserPostUserEventIcePrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var5) {
         }
      }

      return __d;
   }

   public static GroupUserPostUserEventIcePrx checkedCast(ObjectPrx __obj, String __facet, Map<String, String> __ctx) {
      GroupUserPostUserEventIcePrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::GroupUserPostUserEventIce", __ctx)) {
               GroupUserPostUserEventIcePrxHelper __h = new GroupUserPostUserEventIcePrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var6) {
         }
      }

      return __d;
   }

   public static GroupUserPostUserEventIcePrx uncheckedCast(ObjectPrx __obj) {
      GroupUserPostUserEventIcePrx __d = null;
      if (__obj != null) {
         try {
            __d = (GroupUserPostUserEventIcePrx)__obj;
         } catch (ClassCastException var4) {
            GroupUserPostUserEventIcePrxHelper __h = new GroupUserPostUserEventIcePrxHelper();
            __h.__copyFrom(__obj);
            __d = __h;
         }
      }

      return (GroupUserPostUserEventIcePrx)__d;
   }

   public static GroupUserPostUserEventIcePrx uncheckedCast(ObjectPrx __obj, String __facet) {
      GroupUserPostUserEventIcePrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);
         GroupUserPostUserEventIcePrxHelper __h = new GroupUserPostUserEventIcePrxHelper();
         __h.__copyFrom(__bb);
         __d = __h;
      }

      return __d;
   }

   protected _ObjectDelM __createDelegateM() {
      return new _GroupUserPostUserEventIceDelM();
   }

   protected _ObjectDelD __createDelegateD() {
      return new _GroupUserPostUserEventIceDelD();
   }

   public static void __write(BasicStream __os, GroupUserPostUserEventIcePrx v) {
      __os.writeProxy(v);
   }

   public static GroupUserPostUserEventIcePrx __read(BasicStream __is) {
      ObjectPrx proxy = __is.readProxy();
      if (proxy != null) {
         GroupUserPostUserEventIcePrxHelper result = new GroupUserPostUserEventIcePrxHelper();
         result.__copyFrom(proxy);
         return result;
      } else {
         return null;
      }
   }
}
