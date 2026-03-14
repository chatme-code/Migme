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

public final class RecommendationGenerationServicePrxHelper extends ObjectPrxHelperBase implements RecommendationGenerationServicePrx {
   public void runTransformation(int transformationID) {
      this.runTransformation(transformationID, (Map)null, false);
   }

   public void runTransformation(int transformationID, Map<String, String> __ctx) {
      this.runTransformation(transformationID, __ctx, true);
   }

   private void runTransformation(int transformationID, Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            __delBase = this.__getDelegate(false);
            _RecommendationGenerationServiceDel __del = (_RecommendationGenerationServiceDel)__delBase;
            __del.runTransformation(transformationID, __ctx);
            return;
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public static RecommendationGenerationServicePrx checkedCast(ObjectPrx __obj) {
      RecommendationGenerationServicePrx __d = null;
      if (__obj != null) {
         try {
            __d = (RecommendationGenerationServicePrx)__obj;
         } catch (ClassCastException var4) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::RecommendationGenerationService")) {
               RecommendationGenerationServicePrxHelper __h = new RecommendationGenerationServicePrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (RecommendationGenerationServicePrx)__d;
   }

   public static RecommendationGenerationServicePrx checkedCast(ObjectPrx __obj, Map<String, String> __ctx) {
      RecommendationGenerationServicePrx __d = null;
      if (__obj != null) {
         try {
            __d = (RecommendationGenerationServicePrx)__obj;
         } catch (ClassCastException var5) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::RecommendationGenerationService", __ctx)) {
               RecommendationGenerationServicePrxHelper __h = new RecommendationGenerationServicePrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (RecommendationGenerationServicePrx)__d;
   }

   public static RecommendationGenerationServicePrx checkedCast(ObjectPrx __obj, String __facet) {
      RecommendationGenerationServicePrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::RecommendationGenerationService")) {
               RecommendationGenerationServicePrxHelper __h = new RecommendationGenerationServicePrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var5) {
         }
      }

      return __d;
   }

   public static RecommendationGenerationServicePrx checkedCast(ObjectPrx __obj, String __facet, Map<String, String> __ctx) {
      RecommendationGenerationServicePrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::RecommendationGenerationService", __ctx)) {
               RecommendationGenerationServicePrxHelper __h = new RecommendationGenerationServicePrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var6) {
         }
      }

      return __d;
   }

   public static RecommendationGenerationServicePrx uncheckedCast(ObjectPrx __obj) {
      RecommendationGenerationServicePrx __d = null;
      if (__obj != null) {
         try {
            __d = (RecommendationGenerationServicePrx)__obj;
         } catch (ClassCastException var4) {
            RecommendationGenerationServicePrxHelper __h = new RecommendationGenerationServicePrxHelper();
            __h.__copyFrom(__obj);
            __d = __h;
         }
      }

      return (RecommendationGenerationServicePrx)__d;
   }

   public static RecommendationGenerationServicePrx uncheckedCast(ObjectPrx __obj, String __facet) {
      RecommendationGenerationServicePrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);
         RecommendationGenerationServicePrxHelper __h = new RecommendationGenerationServicePrxHelper();
         __h.__copyFrom(__bb);
         __d = __h;
      }

      return __d;
   }

   protected _ObjectDelM __createDelegateM() {
      return new _RecommendationGenerationServiceDelM();
   }

   protected _ObjectDelD __createDelegateD() {
      return new _RecommendationGenerationServiceDelD();
   }

   public static void __write(BasicStream __os, RecommendationGenerationServicePrx v) {
      __os.writeProxy(v);
   }

   public static RecommendationGenerationServicePrx __read(BasicStream __is) {
      ObjectPrx proxy = __is.readProxy();
      if (proxy != null) {
         RecommendationGenerationServicePrxHelper result = new RecommendationGenerationServicePrxHelper();
         result.__copyFrom(proxy);
         return result;
      } else {
         return null;
      }
   }
}
