package com.projectgoth.fusion.slice;

import Ice.FacetNotExistException;
import Ice.LocalException;
import Ice.ObjectPrx;
import Ice.ObjectPrxHelperBase;
import Ice._ObjectDel;
import Ice._ObjectDelD;
import Ice._ObjectDelM;
import IceInternal.BasicStream;
import IceInternal.LocalExceptionWrapper;
import IceInternal.OutgoingAsync;
import java.util.Map;

public final class EventStoreAdminPrxHelper extends ObjectPrxHelperBase implements EventStoreAdminPrx {
   public EventStoreStats getStats() throws FusionException {
      return this.getStats((Map)null, false);
   }

   public EventStoreStats getStats(Map<String, String> __ctx) throws FusionException {
      return this.getStats(__ctx, true);
   }

   private EventStoreStats getStats(Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("getStats");
            __delBase = this.__getDelegate(false);
            _EventStoreAdminDel __del = (_EventStoreAdminDel)__delBase;
            return __del.getStats(__ctx);
         } catch (LocalExceptionWrapper var6) {
            this.__handleExceptionWrapper(__delBase, var6, (OutgoingAsync)null);
         } catch (LocalException var7) {
            __cnt = this.__handleException(__delBase, var7, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public static EventStoreAdminPrx checkedCast(ObjectPrx __obj) {
      EventStoreAdminPrx __d = null;
      if (__obj != null) {
         try {
            __d = (EventStoreAdminPrx)__obj;
         } catch (ClassCastException var4) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::EventStoreAdmin")) {
               EventStoreAdminPrxHelper __h = new EventStoreAdminPrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (EventStoreAdminPrx)__d;
   }

   public static EventStoreAdminPrx checkedCast(ObjectPrx __obj, Map<String, String> __ctx) {
      EventStoreAdminPrx __d = null;
      if (__obj != null) {
         try {
            __d = (EventStoreAdminPrx)__obj;
         } catch (ClassCastException var5) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::EventStoreAdmin", __ctx)) {
               EventStoreAdminPrxHelper __h = new EventStoreAdminPrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (EventStoreAdminPrx)__d;
   }

   public static EventStoreAdminPrx checkedCast(ObjectPrx __obj, String __facet) {
      EventStoreAdminPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::EventStoreAdmin")) {
               EventStoreAdminPrxHelper __h = new EventStoreAdminPrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var5) {
         }
      }

      return __d;
   }

   public static EventStoreAdminPrx checkedCast(ObjectPrx __obj, String __facet, Map<String, String> __ctx) {
      EventStoreAdminPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::EventStoreAdmin", __ctx)) {
               EventStoreAdminPrxHelper __h = new EventStoreAdminPrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var6) {
         }
      }

      return __d;
   }

   public static EventStoreAdminPrx uncheckedCast(ObjectPrx __obj) {
      EventStoreAdminPrx __d = null;
      if (__obj != null) {
         try {
            __d = (EventStoreAdminPrx)__obj;
         } catch (ClassCastException var4) {
            EventStoreAdminPrxHelper __h = new EventStoreAdminPrxHelper();
            __h.__copyFrom(__obj);
            __d = __h;
         }
      }

      return (EventStoreAdminPrx)__d;
   }

   public static EventStoreAdminPrx uncheckedCast(ObjectPrx __obj, String __facet) {
      EventStoreAdminPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);
         EventStoreAdminPrxHelper __h = new EventStoreAdminPrxHelper();
         __h.__copyFrom(__bb);
         __d = __h;
      }

      return __d;
   }

   protected _ObjectDelM __createDelegateM() {
      return new _EventStoreAdminDelM();
   }

   protected _ObjectDelD __createDelegateD() {
      return new _EventStoreAdminDelD();
   }

   public static void __write(BasicStream __os, EventStoreAdminPrx v) {
      __os.writeProxy(v);
   }

   public static EventStoreAdminPrx __read(BasicStream __is) {
      ObjectPrx proxy = __is.readProxy();
      if (proxy != null) {
         EventStoreAdminPrxHelper result = new EventStoreAdminPrxHelper();
         result.__copyFrom(proxy);
         return result;
      } else {
         return null;
      }
   }
}
