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

public final class SessionCachePrxHelper extends ObjectPrxHelperBase implements SessionCachePrx {
   public void logSession(SessionIce session, SessionMetricsIce sessionMetrics) {
      this.logSession(session, sessionMetrics, (Map)null, false);
   }

   public void logSession(SessionIce session, SessionMetricsIce sessionMetrics, Map<String, String> __ctx) {
      this.logSession(session, sessionMetrics, __ctx, true);
   }

   private void logSession(SessionIce session, SessionMetricsIce sessionMetrics, Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            __delBase = this.__getDelegate(false);
            _SessionCacheDel __del = (_SessionCacheDel)__delBase;
            __del.logSession(session, sessionMetrics, __ctx);
            return;
         } catch (LocalExceptionWrapper var8) {
            this.__handleExceptionWrapper(__delBase, var8, (OutgoingAsync)null);
         } catch (LocalException var9) {
            __cnt = this.__handleException(__delBase, var9, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public static SessionCachePrx checkedCast(ObjectPrx __obj) {
      SessionCachePrx __d = null;
      if (__obj != null) {
         try {
            __d = (SessionCachePrx)__obj;
         } catch (ClassCastException var4) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::SessionCache")) {
               SessionCachePrxHelper __h = new SessionCachePrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (SessionCachePrx)__d;
   }

   public static SessionCachePrx checkedCast(ObjectPrx __obj, Map<String, String> __ctx) {
      SessionCachePrx __d = null;
      if (__obj != null) {
         try {
            __d = (SessionCachePrx)__obj;
         } catch (ClassCastException var5) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::SessionCache", __ctx)) {
               SessionCachePrxHelper __h = new SessionCachePrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (SessionCachePrx)__d;
   }

   public static SessionCachePrx checkedCast(ObjectPrx __obj, String __facet) {
      SessionCachePrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::SessionCache")) {
               SessionCachePrxHelper __h = new SessionCachePrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var5) {
         }
      }

      return __d;
   }

   public static SessionCachePrx checkedCast(ObjectPrx __obj, String __facet, Map<String, String> __ctx) {
      SessionCachePrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::SessionCache", __ctx)) {
               SessionCachePrxHelper __h = new SessionCachePrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var6) {
         }
      }

      return __d;
   }

   public static SessionCachePrx uncheckedCast(ObjectPrx __obj) {
      SessionCachePrx __d = null;
      if (__obj != null) {
         try {
            __d = (SessionCachePrx)__obj;
         } catch (ClassCastException var4) {
            SessionCachePrxHelper __h = new SessionCachePrxHelper();
            __h.__copyFrom(__obj);
            __d = __h;
         }
      }

      return (SessionCachePrx)__d;
   }

   public static SessionCachePrx uncheckedCast(ObjectPrx __obj, String __facet) {
      SessionCachePrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);
         SessionCachePrxHelper __h = new SessionCachePrxHelper();
         __h.__copyFrom(__bb);
         __d = __h;
      }

      return __d;
   }

   protected _ObjectDelM __createDelegateM() {
      return new _SessionCacheDelM();
   }

   protected _ObjectDelD __createDelegateD() {
      return new _SessionCacheDelD();
   }

   public static void __write(BasicStream __os, SessionCachePrx v) {
      __os.writeProxy(v);
   }

   public static SessionCachePrx __read(BasicStream __is) {
      ObjectPrx proxy = __is.readProxy();
      if (proxy != null) {
         SessionCachePrxHelper result = new SessionCachePrxHelper();
         result.__copyFrom(proxy);
         return result;
      } else {
         return null;
      }
   }
}
