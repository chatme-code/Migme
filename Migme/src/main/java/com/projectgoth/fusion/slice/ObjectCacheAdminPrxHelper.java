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

public final class ObjectCacheAdminPrxHelper extends ObjectPrxHelperBase implements ObjectCacheAdminPrx {
   public int getLoadWeightage() {
      return this.getLoadWeightage((Map)null, false);
   }

   public int getLoadWeightage(Map<String, String> __ctx) {
      return this.getLoadWeightage(__ctx, true);
   }

   private int getLoadWeightage(Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("getLoadWeightage");
            __delBase = this.__getDelegate(false);
            _ObjectCacheAdminDel __del = (_ObjectCacheAdminDel)__delBase;
            return __del.getLoadWeightage(__ctx);
         } catch (LocalExceptionWrapper var6) {
            this.__handleExceptionWrapper(__delBase, var6, (OutgoingAsync)null);
         } catch (LocalException var7) {
            __cnt = this.__handleException(__delBase, var7, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public ObjectCacheStats getStats() throws FusionException {
      return this.getStats((Map)null, false);
   }

   public ObjectCacheStats getStats(Map<String, String> __ctx) throws FusionException {
      return this.getStats(__ctx, true);
   }

   private ObjectCacheStats getStats(Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("getStats");
            __delBase = this.__getDelegate(false);
            _ObjectCacheAdminDel __del = (_ObjectCacheAdminDel)__delBase;
            return __del.getStats(__ctx);
         } catch (LocalExceptionWrapper var6) {
            this.__handleExceptionWrapper(__delBase, var6, (OutgoingAsync)null);
         } catch (LocalException var7) {
            __cnt = this.__handleException(__delBase, var7, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public String[] getUsernames() {
      return this.getUsernames((Map)null, false);
   }

   public String[] getUsernames(Map<String, String> __ctx) {
      return this.getUsernames(__ctx, true);
   }

   private String[] getUsernames(Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("getUsernames");
            __delBase = this.__getDelegate(false);
            _ObjectCacheAdminDel __del = (_ObjectCacheAdminDel)__delBase;
            return __del.getUsernames(__ctx);
         } catch (LocalExceptionWrapper var6) {
            this.__handleExceptionWrapper(__delBase, var6, (OutgoingAsync)null);
         } catch (LocalException var7) {
            __cnt = this.__handleException(__delBase, var7, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public int ping() {
      return this.ping((Map)null, false);
   }

   public int ping(Map<String, String> __ctx) {
      return this.ping(__ctx, true);
   }

   private int ping(Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("ping");
            __delBase = this.__getDelegate(false);
            _ObjectCacheAdminDel __del = (_ObjectCacheAdminDel)__delBase;
            return __del.ping(__ctx);
         } catch (LocalExceptionWrapper var6) {
            this.__handleExceptionWrapper(__delBase, var6, (OutgoingAsync)null);
         } catch (LocalException var7) {
            __cnt = this.__handleException(__delBase, var7, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void reloadEmotes() {
      this.reloadEmotes((Map)null, false);
   }

   public void reloadEmotes(Map<String, String> __ctx) {
      this.reloadEmotes(__ctx, true);
   }

   private void reloadEmotes(Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            __delBase = this.__getDelegate(false);
            _ObjectCacheAdminDel __del = (_ObjectCacheAdminDel)__delBase;
            __del.reloadEmotes(__ctx);
            return;
         } catch (LocalExceptionWrapper var6) {
            this.__handleExceptionWrapper(__delBase, var6, (OutgoingAsync)null);
         } catch (LocalException var7) {
            __cnt = this.__handleException(__delBase, var7, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void setLoadWeightage(int weightage) {
      this.setLoadWeightage(weightage, (Map)null, false);
   }

   public void setLoadWeightage(int weightage, Map<String, String> __ctx) {
      this.setLoadWeightage(weightage, __ctx, true);
   }

   private void setLoadWeightage(int weightage, Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            __delBase = this.__getDelegate(false);
            _ObjectCacheAdminDel __del = (_ObjectCacheAdminDel)__delBase;
            __del.setLoadWeightage(weightage, __ctx);
            return;
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public static ObjectCacheAdminPrx checkedCast(ObjectPrx __obj) {
      ObjectCacheAdminPrx __d = null;
      if (__obj != null) {
         try {
            __d = (ObjectCacheAdminPrx)__obj;
         } catch (ClassCastException var4) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::ObjectCacheAdmin")) {
               ObjectCacheAdminPrxHelper __h = new ObjectCacheAdminPrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (ObjectCacheAdminPrx)__d;
   }

   public static ObjectCacheAdminPrx checkedCast(ObjectPrx __obj, Map<String, String> __ctx) {
      ObjectCacheAdminPrx __d = null;
      if (__obj != null) {
         try {
            __d = (ObjectCacheAdminPrx)__obj;
         } catch (ClassCastException var5) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::ObjectCacheAdmin", __ctx)) {
               ObjectCacheAdminPrxHelper __h = new ObjectCacheAdminPrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (ObjectCacheAdminPrx)__d;
   }

   public static ObjectCacheAdminPrx checkedCast(ObjectPrx __obj, String __facet) {
      ObjectCacheAdminPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::ObjectCacheAdmin")) {
               ObjectCacheAdminPrxHelper __h = new ObjectCacheAdminPrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var5) {
         }
      }

      return __d;
   }

   public static ObjectCacheAdminPrx checkedCast(ObjectPrx __obj, String __facet, Map<String, String> __ctx) {
      ObjectCacheAdminPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::ObjectCacheAdmin", __ctx)) {
               ObjectCacheAdminPrxHelper __h = new ObjectCacheAdminPrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var6) {
         }
      }

      return __d;
   }

   public static ObjectCacheAdminPrx uncheckedCast(ObjectPrx __obj) {
      ObjectCacheAdminPrx __d = null;
      if (__obj != null) {
         try {
            __d = (ObjectCacheAdminPrx)__obj;
         } catch (ClassCastException var4) {
            ObjectCacheAdminPrxHelper __h = new ObjectCacheAdminPrxHelper();
            __h.__copyFrom(__obj);
            __d = __h;
         }
      }

      return (ObjectCacheAdminPrx)__d;
   }

   public static ObjectCacheAdminPrx uncheckedCast(ObjectPrx __obj, String __facet) {
      ObjectCacheAdminPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);
         ObjectCacheAdminPrxHelper __h = new ObjectCacheAdminPrxHelper();
         __h.__copyFrom(__bb);
         __d = __h;
      }

      return __d;
   }

   protected _ObjectDelM __createDelegateM() {
      return new _ObjectCacheAdminDelM();
   }

   protected _ObjectDelD __createDelegateD() {
      return new _ObjectCacheAdminDelD();
   }

   public static void __write(BasicStream __os, ObjectCacheAdminPrx v) {
      __os.writeProxy(v);
   }

   public static ObjectCacheAdminPrx __read(BasicStream __is) {
      ObjectPrx proxy = __is.readProxy();
      if (proxy != null) {
         ObjectCacheAdminPrxHelper result = new ObjectCacheAdminPrxHelper();
         result.__copyFrom(proxy);
         return result;
      } else {
         return null;
      }
   }
}
