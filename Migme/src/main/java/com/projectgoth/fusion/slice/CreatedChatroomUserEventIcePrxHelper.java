package com.projectgoth.fusion.slice;

import Ice.FacetNotExistException;
import Ice.ObjectPrx;
import Ice.ObjectPrxHelperBase;
import Ice._ObjectDelD;
import Ice._ObjectDelM;
import IceInternal.BasicStream;
import java.util.Map;

public final class CreatedChatroomUserEventIcePrxHelper extends ObjectPrxHelperBase implements CreatedChatroomUserEventIcePrx {
   public static CreatedChatroomUserEventIcePrx checkedCast(ObjectPrx __obj) {
      CreatedChatroomUserEventIcePrx __d = null;
      if (__obj != null) {
         try {
            __d = (CreatedChatroomUserEventIcePrx)__obj;
         } catch (ClassCastException var4) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::CreatedChatroomUserEventIce")) {
               CreatedChatroomUserEventIcePrxHelper __h = new CreatedChatroomUserEventIcePrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (CreatedChatroomUserEventIcePrx)__d;
   }

   public static CreatedChatroomUserEventIcePrx checkedCast(ObjectPrx __obj, Map<String, String> __ctx) {
      CreatedChatroomUserEventIcePrx __d = null;
      if (__obj != null) {
         try {
            __d = (CreatedChatroomUserEventIcePrx)__obj;
         } catch (ClassCastException var5) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::CreatedChatroomUserEventIce", __ctx)) {
               CreatedChatroomUserEventIcePrxHelper __h = new CreatedChatroomUserEventIcePrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (CreatedChatroomUserEventIcePrx)__d;
   }

   public static CreatedChatroomUserEventIcePrx checkedCast(ObjectPrx __obj, String __facet) {
      CreatedChatroomUserEventIcePrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::CreatedChatroomUserEventIce")) {
               CreatedChatroomUserEventIcePrxHelper __h = new CreatedChatroomUserEventIcePrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var5) {
         }
      }

      return __d;
   }

   public static CreatedChatroomUserEventIcePrx checkedCast(ObjectPrx __obj, String __facet, Map<String, String> __ctx) {
      CreatedChatroomUserEventIcePrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::CreatedChatroomUserEventIce", __ctx)) {
               CreatedChatroomUserEventIcePrxHelper __h = new CreatedChatroomUserEventIcePrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var6) {
         }
      }

      return __d;
   }

   public static CreatedChatroomUserEventIcePrx uncheckedCast(ObjectPrx __obj) {
      CreatedChatroomUserEventIcePrx __d = null;
      if (__obj != null) {
         try {
            __d = (CreatedChatroomUserEventIcePrx)__obj;
         } catch (ClassCastException var4) {
            CreatedChatroomUserEventIcePrxHelper __h = new CreatedChatroomUserEventIcePrxHelper();
            __h.__copyFrom(__obj);
            __d = __h;
         }
      }

      return (CreatedChatroomUserEventIcePrx)__d;
   }

   public static CreatedChatroomUserEventIcePrx uncheckedCast(ObjectPrx __obj, String __facet) {
      CreatedChatroomUserEventIcePrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);
         CreatedChatroomUserEventIcePrxHelper __h = new CreatedChatroomUserEventIcePrxHelper();
         __h.__copyFrom(__bb);
         __d = __h;
      }

      return __d;
   }

   protected _ObjectDelM __createDelegateM() {
      return new _CreatedChatroomUserEventIceDelM();
   }

   protected _ObjectDelD __createDelegateD() {
      return new _CreatedChatroomUserEventIceDelD();
   }

   public static void __write(BasicStream __os, CreatedChatroomUserEventIcePrx v) {
      __os.writeProxy(v);
   }

   public static CreatedChatroomUserEventIcePrx __read(BasicStream __is) {
      ObjectPrx proxy = __is.readProxy();
      if (proxy != null) {
         CreatedChatroomUserEventIcePrxHelper result = new CreatedChatroomUserEventIcePrxHelper();
         result.__copyFrom(proxy);
         return result;
      } else {
         return null;
      }
   }
}
