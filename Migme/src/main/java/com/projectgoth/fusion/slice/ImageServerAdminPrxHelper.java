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

public final class ImageServerAdminPrxHelper extends ObjectPrxHelperBase implements ImageServerAdminPrx {
   public ImageServerStats getStats() throws FusionException {
      return this.getStats((Map)null, false);
   }

   public ImageServerStats getStats(Map<String, String> __ctx) throws FusionException {
      return this.getStats(__ctx, true);
   }

   private ImageServerStats getStats(Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("getStats");
            __delBase = this.__getDelegate(false);
            _ImageServerAdminDel __del = (_ImageServerAdminDel)__delBase;
            return __del.getStats(__ctx);
         } catch (LocalExceptionWrapper var6) {
            this.__handleExceptionWrapper(__delBase, var6, (OutgoingAsync)null);
         } catch (LocalException var7) {
            __cnt = this.__handleException(__delBase, var7, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public static ImageServerAdminPrx checkedCast(ObjectPrx __obj) {
      ImageServerAdminPrx __d = null;
      if (__obj != null) {
         try {
            __d = (ImageServerAdminPrx)__obj;
         } catch (ClassCastException var4) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::ImageServerAdmin")) {
               ImageServerAdminPrxHelper __h = new ImageServerAdminPrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (ImageServerAdminPrx)__d;
   }

   public static ImageServerAdminPrx checkedCast(ObjectPrx __obj, Map<String, String> __ctx) {
      ImageServerAdminPrx __d = null;
      if (__obj != null) {
         try {
            __d = (ImageServerAdminPrx)__obj;
         } catch (ClassCastException var5) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::ImageServerAdmin", __ctx)) {
               ImageServerAdminPrxHelper __h = new ImageServerAdminPrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (ImageServerAdminPrx)__d;
   }

   public static ImageServerAdminPrx checkedCast(ObjectPrx __obj, String __facet) {
      ImageServerAdminPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::ImageServerAdmin")) {
               ImageServerAdminPrxHelper __h = new ImageServerAdminPrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var5) {
         }
      }

      return __d;
   }

   public static ImageServerAdminPrx checkedCast(ObjectPrx __obj, String __facet, Map<String, String> __ctx) {
      ImageServerAdminPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::ImageServerAdmin", __ctx)) {
               ImageServerAdminPrxHelper __h = new ImageServerAdminPrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var6) {
         }
      }

      return __d;
   }

   public static ImageServerAdminPrx uncheckedCast(ObjectPrx __obj) {
      ImageServerAdminPrx __d = null;
      if (__obj != null) {
         try {
            __d = (ImageServerAdminPrx)__obj;
         } catch (ClassCastException var4) {
            ImageServerAdminPrxHelper __h = new ImageServerAdminPrxHelper();
            __h.__copyFrom(__obj);
            __d = __h;
         }
      }

      return (ImageServerAdminPrx)__d;
   }

   public static ImageServerAdminPrx uncheckedCast(ObjectPrx __obj, String __facet) {
      ImageServerAdminPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);
         ImageServerAdminPrxHelper __h = new ImageServerAdminPrxHelper();
         __h.__copyFrom(__bb);
         __d = __h;
      }

      return __d;
   }

   protected _ObjectDelM __createDelegateM() {
      return new _ImageServerAdminDelM();
   }

   protected _ObjectDelD __createDelegateD() {
      return new _ImageServerAdminDelD();
   }

   public static void __write(BasicStream __os, ImageServerAdminPrx v) {
      __os.writeProxy(v);
   }

   public static ImageServerAdminPrx __read(BasicStream __is) {
      ObjectPrx proxy = __is.readProxy();
      if (proxy != null) {
         ImageServerAdminPrxHelper result = new ImageServerAdminPrxHelper();
         result.__copyFrom(proxy);
         return result;
      } else {
         return null;
      }
   }
}
