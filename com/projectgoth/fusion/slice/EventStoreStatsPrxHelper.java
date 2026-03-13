package com.projectgoth.fusion.slice;

import Ice.FacetNotExistException;
import Ice.ObjectPrx;
import Ice.ObjectPrxHelperBase;
import Ice._ObjectDelD;
import Ice._ObjectDelM;
import IceInternal.BasicStream;
import java.util.Map;

public final class EventStoreStatsPrxHelper extends ObjectPrxHelperBase implements EventStoreStatsPrx {
   public static EventStoreStatsPrx checkedCast(ObjectPrx __obj) {
      EventStoreStatsPrx __d = null;
      if (__obj != null) {
         try {
            __d = (EventStoreStatsPrx)__obj;
         } catch (ClassCastException var4) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::EventStoreStats")) {
               EventStoreStatsPrxHelper __h = new EventStoreStatsPrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (EventStoreStatsPrx)__d;
   }

   public static EventStoreStatsPrx checkedCast(ObjectPrx __obj, Map<String, String> __ctx) {
      EventStoreStatsPrx __d = null;
      if (__obj != null) {
         try {
            __d = (EventStoreStatsPrx)__obj;
         } catch (ClassCastException var5) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::EventStoreStats", __ctx)) {
               EventStoreStatsPrxHelper __h = new EventStoreStatsPrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (EventStoreStatsPrx)__d;
   }

   public static EventStoreStatsPrx checkedCast(ObjectPrx __obj, String __facet) {
      EventStoreStatsPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::EventStoreStats")) {
               EventStoreStatsPrxHelper __h = new EventStoreStatsPrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var5) {
         }
      }

      return __d;
   }

   public static EventStoreStatsPrx checkedCast(ObjectPrx __obj, String __facet, Map<String, String> __ctx) {
      EventStoreStatsPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::EventStoreStats", __ctx)) {
               EventStoreStatsPrxHelper __h = new EventStoreStatsPrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var6) {
         }
      }

      return __d;
   }

   public static EventStoreStatsPrx uncheckedCast(ObjectPrx __obj) {
      EventStoreStatsPrx __d = null;
      if (__obj != null) {
         try {
            __d = (EventStoreStatsPrx)__obj;
         } catch (ClassCastException var4) {
            EventStoreStatsPrxHelper __h = new EventStoreStatsPrxHelper();
            __h.__copyFrom(__obj);
            __d = __h;
         }
      }

      return (EventStoreStatsPrx)__d;
   }

   public static EventStoreStatsPrx uncheckedCast(ObjectPrx __obj, String __facet) {
      EventStoreStatsPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);
         EventStoreStatsPrxHelper __h = new EventStoreStatsPrxHelper();
         __h.__copyFrom(__bb);
         __d = __h;
      }

      return __d;
   }

   protected _ObjectDelM __createDelegateM() {
      return new _EventStoreStatsDelM();
   }

   protected _ObjectDelD __createDelegateD() {
      return new _EventStoreStatsDelD();
   }

   public static void __write(BasicStream __os, EventStoreStatsPrx v) {
      __os.writeProxy(v);
   }

   public static EventStoreStatsPrx __read(BasicStream __is) {
      ObjectPrx proxy = __is.readProxy();
      if (proxy != null) {
         EventStoreStatsPrxHelper result = new EventStoreStatsPrxHelper();
         result.__copyFrom(proxy);
         return result;
      } else {
         return null;
      }
   }
}
