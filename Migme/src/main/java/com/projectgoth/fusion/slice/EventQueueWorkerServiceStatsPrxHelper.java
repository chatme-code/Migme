package com.projectgoth.fusion.slice;

import Ice.FacetNotExistException;
import Ice.ObjectPrx;
import Ice.ObjectPrxHelperBase;
import Ice._ObjectDelD;
import Ice._ObjectDelM;
import IceInternal.BasicStream;
import java.util.Map;

public final class EventQueueWorkerServiceStatsPrxHelper extends ObjectPrxHelperBase implements EventQueueWorkerServiceStatsPrx {
   public static EventQueueWorkerServiceStatsPrx checkedCast(ObjectPrx __obj) {
      EventQueueWorkerServiceStatsPrx __d = null;
      if (__obj != null) {
         try {
            __d = (EventQueueWorkerServiceStatsPrx)__obj;
         } catch (ClassCastException var4) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::EventQueueWorkerServiceStats")) {
               EventQueueWorkerServiceStatsPrxHelper __h = new EventQueueWorkerServiceStatsPrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (EventQueueWorkerServiceStatsPrx)__d;
   }

   public static EventQueueWorkerServiceStatsPrx checkedCast(ObjectPrx __obj, Map<String, String> __ctx) {
      EventQueueWorkerServiceStatsPrx __d = null;
      if (__obj != null) {
         try {
            __d = (EventQueueWorkerServiceStatsPrx)__obj;
         } catch (ClassCastException var5) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::EventQueueWorkerServiceStats", __ctx)) {
               EventQueueWorkerServiceStatsPrxHelper __h = new EventQueueWorkerServiceStatsPrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (EventQueueWorkerServiceStatsPrx)__d;
   }

   public static EventQueueWorkerServiceStatsPrx checkedCast(ObjectPrx __obj, String __facet) {
      EventQueueWorkerServiceStatsPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::EventQueueWorkerServiceStats")) {
               EventQueueWorkerServiceStatsPrxHelper __h = new EventQueueWorkerServiceStatsPrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var5) {
         }
      }

      return __d;
   }

   public static EventQueueWorkerServiceStatsPrx checkedCast(ObjectPrx __obj, String __facet, Map<String, String> __ctx) {
      EventQueueWorkerServiceStatsPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::EventQueueWorkerServiceStats", __ctx)) {
               EventQueueWorkerServiceStatsPrxHelper __h = new EventQueueWorkerServiceStatsPrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var6) {
         }
      }

      return __d;
   }

   public static EventQueueWorkerServiceStatsPrx uncheckedCast(ObjectPrx __obj) {
      EventQueueWorkerServiceStatsPrx __d = null;
      if (__obj != null) {
         try {
            __d = (EventQueueWorkerServiceStatsPrx)__obj;
         } catch (ClassCastException var4) {
            EventQueueWorkerServiceStatsPrxHelper __h = new EventQueueWorkerServiceStatsPrxHelper();
            __h.__copyFrom(__obj);
            __d = __h;
         }
      }

      return (EventQueueWorkerServiceStatsPrx)__d;
   }

   public static EventQueueWorkerServiceStatsPrx uncheckedCast(ObjectPrx __obj, String __facet) {
      EventQueueWorkerServiceStatsPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);
         EventQueueWorkerServiceStatsPrxHelper __h = new EventQueueWorkerServiceStatsPrxHelper();
         __h.__copyFrom(__bb);
         __d = __h;
      }

      return __d;
   }

   protected _ObjectDelM __createDelegateM() {
      return new _EventQueueWorkerServiceStatsDelM();
   }

   protected _ObjectDelD __createDelegateD() {
      return new _EventQueueWorkerServiceStatsDelD();
   }

   public static void __write(BasicStream __os, EventQueueWorkerServiceStatsPrx v) {
      __os.writeProxy(v);
   }

   public static EventQueueWorkerServiceStatsPrx __read(BasicStream __is) {
      ObjectPrx proxy = __is.readProxy();
      if (proxy != null) {
         EventQueueWorkerServiceStatsPrxHelper result = new EventQueueWorkerServiceStatsPrxHelper();
         result.__copyFrom(proxy);
         return result;
      } else {
         return null;
      }
   }
}
