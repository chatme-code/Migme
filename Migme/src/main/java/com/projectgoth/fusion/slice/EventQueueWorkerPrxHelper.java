package com.projectgoth.fusion.slice;

import Ice.FacetNotExistException;
import Ice.ObjectPrx;
import Ice.ObjectPrxHelperBase;
import Ice._ObjectDelD;
import Ice._ObjectDelM;
import IceInternal.BasicStream;
import java.util.Map;

public final class EventQueueWorkerPrxHelper extends ObjectPrxHelperBase implements EventQueueWorkerPrx {
   public static EventQueueWorkerPrx checkedCast(ObjectPrx __obj) {
      EventQueueWorkerPrx __d = null;
      if (__obj != null) {
         try {
            __d = (EventQueueWorkerPrx)__obj;
         } catch (ClassCastException var4) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::EventQueueWorker")) {
               EventQueueWorkerPrxHelper __h = new EventQueueWorkerPrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (EventQueueWorkerPrx)__d;
   }

   public static EventQueueWorkerPrx checkedCast(ObjectPrx __obj, Map<String, String> __ctx) {
      EventQueueWorkerPrx __d = null;
      if (__obj != null) {
         try {
            __d = (EventQueueWorkerPrx)__obj;
         } catch (ClassCastException var5) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::EventQueueWorker", __ctx)) {
               EventQueueWorkerPrxHelper __h = new EventQueueWorkerPrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (EventQueueWorkerPrx)__d;
   }

   public static EventQueueWorkerPrx checkedCast(ObjectPrx __obj, String __facet) {
      EventQueueWorkerPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::EventQueueWorker")) {
               EventQueueWorkerPrxHelper __h = new EventQueueWorkerPrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var5) {
         }
      }

      return __d;
   }

   public static EventQueueWorkerPrx checkedCast(ObjectPrx __obj, String __facet, Map<String, String> __ctx) {
      EventQueueWorkerPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::EventQueueWorker", __ctx)) {
               EventQueueWorkerPrxHelper __h = new EventQueueWorkerPrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var6) {
         }
      }

      return __d;
   }

   public static EventQueueWorkerPrx uncheckedCast(ObjectPrx __obj) {
      EventQueueWorkerPrx __d = null;
      if (__obj != null) {
         try {
            __d = (EventQueueWorkerPrx)__obj;
         } catch (ClassCastException var4) {
            EventQueueWorkerPrxHelper __h = new EventQueueWorkerPrxHelper();
            __h.__copyFrom(__obj);
            __d = __h;
         }
      }

      return (EventQueueWorkerPrx)__d;
   }

   public static EventQueueWorkerPrx uncheckedCast(ObjectPrx __obj, String __facet) {
      EventQueueWorkerPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);
         EventQueueWorkerPrxHelper __h = new EventQueueWorkerPrxHelper();
         __h.__copyFrom(__bb);
         __d = __h;
      }

      return __d;
   }

   protected _ObjectDelM __createDelegateM() {
      return new _EventQueueWorkerDelM();
   }

   protected _ObjectDelD __createDelegateD() {
      return new _EventQueueWorkerDelD();
   }

   public static void __write(BasicStream __os, EventQueueWorkerPrx v) {
      __os.writeProxy(v);
   }

   public static EventQueueWorkerPrx __read(BasicStream __is) {
      ObjectPrx proxy = __is.readProxy();
      if (proxy != null) {
         EventQueueWorkerPrxHelper result = new EventQueueWorkerPrxHelper();
         result.__copyFrom(proxy);
         return result;
      } else {
         return null;
      }
   }
}
