package com.projectgoth.fusion.slice;

import Ice.FacetNotExistException;
import Ice.ObjectPrx;
import Ice.ObjectPrxHelperBase;
import Ice._ObjectDelD;
import Ice._ObjectDelM;
import IceInternal.BasicStream;
import java.util.Map;

public final class GroupJoinedUserEventIcePrxHelper extends ObjectPrxHelperBase implements GroupJoinedUserEventIcePrx {
   public static GroupJoinedUserEventIcePrx checkedCast(ObjectPrx __obj) {
      GroupJoinedUserEventIcePrx __d = null;
      if (__obj != null) {
         try {
            __d = (GroupJoinedUserEventIcePrx)__obj;
         } catch (ClassCastException var4) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::GroupJoinedUserEventIce")) {
               GroupJoinedUserEventIcePrxHelper __h = new GroupJoinedUserEventIcePrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (GroupJoinedUserEventIcePrx)__d;
   }

   public static GroupJoinedUserEventIcePrx checkedCast(ObjectPrx __obj, Map<String, String> __ctx) {
      GroupJoinedUserEventIcePrx __d = null;
      if (__obj != null) {
         try {
            __d = (GroupJoinedUserEventIcePrx)__obj;
         } catch (ClassCastException var5) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::GroupJoinedUserEventIce", __ctx)) {
               GroupJoinedUserEventIcePrxHelper __h = new GroupJoinedUserEventIcePrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (GroupJoinedUserEventIcePrx)__d;
   }

   public static GroupJoinedUserEventIcePrx checkedCast(ObjectPrx __obj, String __facet) {
      GroupJoinedUserEventIcePrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::GroupJoinedUserEventIce")) {
               GroupJoinedUserEventIcePrxHelper __h = new GroupJoinedUserEventIcePrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var5) {
         }
      }

      return __d;
   }

   public static GroupJoinedUserEventIcePrx checkedCast(ObjectPrx __obj, String __facet, Map<String, String> __ctx) {
      GroupJoinedUserEventIcePrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::GroupJoinedUserEventIce", __ctx)) {
               GroupJoinedUserEventIcePrxHelper __h = new GroupJoinedUserEventIcePrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var6) {
         }
      }

      return __d;
   }

   public static GroupJoinedUserEventIcePrx uncheckedCast(ObjectPrx __obj) {
      GroupJoinedUserEventIcePrx __d = null;
      if (__obj != null) {
         try {
            __d = (GroupJoinedUserEventIcePrx)__obj;
         } catch (ClassCastException var4) {
            GroupJoinedUserEventIcePrxHelper __h = new GroupJoinedUserEventIcePrxHelper();
            __h.__copyFrom(__obj);
            __d = __h;
         }
      }

      return (GroupJoinedUserEventIcePrx)__d;
   }

   public static GroupJoinedUserEventIcePrx uncheckedCast(ObjectPrx __obj, String __facet) {
      GroupJoinedUserEventIcePrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);
         GroupJoinedUserEventIcePrxHelper __h = new GroupJoinedUserEventIcePrxHelper();
         __h.__copyFrom(__bb);
         __d = __h;
      }

      return __d;
   }

   protected _ObjectDelM __createDelegateM() {
      return new _GroupJoinedUserEventIceDelM();
   }

   protected _ObjectDelD __createDelegateD() {
      return new _GroupJoinedUserEventIceDelD();
   }

   public static void __write(BasicStream __os, GroupJoinedUserEventIcePrx v) {
      __os.writeProxy(v);
   }

   public static GroupJoinedUserEventIcePrx __read(BasicStream __is) {
      ObjectPrx proxy = __is.readProxy();
      if (proxy != null) {
         GroupJoinedUserEventIcePrxHelper result = new GroupJoinedUserEventIcePrxHelper();
         result.__copyFrom(proxy);
         return result;
      } else {
         return null;
      }
   }
}
