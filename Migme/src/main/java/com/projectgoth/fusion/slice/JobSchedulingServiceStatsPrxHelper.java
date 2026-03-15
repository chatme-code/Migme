package com.projectgoth.fusion.slice;

import Ice.FacetNotExistException;
import Ice.ObjectPrx;
import Ice.ObjectPrxHelperBase;
import Ice._ObjectDelD;
import Ice._ObjectDelM;
import IceInternal.BasicStream;
import java.util.Map;

public final class JobSchedulingServiceStatsPrxHelper extends ObjectPrxHelperBase implements JobSchedulingServiceStatsPrx {
   public static JobSchedulingServiceStatsPrx checkedCast(ObjectPrx __obj) {
      JobSchedulingServiceStatsPrx __d = null;
      if (__obj != null) {
         try {
            __d = (JobSchedulingServiceStatsPrx)__obj;
         } catch (ClassCastException var4) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::JobSchedulingServiceStats")) {
               JobSchedulingServiceStatsPrxHelper __h = new JobSchedulingServiceStatsPrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (JobSchedulingServiceStatsPrx)__d;
   }

   public static JobSchedulingServiceStatsPrx checkedCast(ObjectPrx __obj, Map<String, String> __ctx) {
      JobSchedulingServiceStatsPrx __d = null;
      if (__obj != null) {
         try {
            __d = (JobSchedulingServiceStatsPrx)__obj;
         } catch (ClassCastException var5) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::JobSchedulingServiceStats", __ctx)) {
               JobSchedulingServiceStatsPrxHelper __h = new JobSchedulingServiceStatsPrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (JobSchedulingServiceStatsPrx)__d;
   }

   public static JobSchedulingServiceStatsPrx checkedCast(ObjectPrx __obj, String __facet) {
      JobSchedulingServiceStatsPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::JobSchedulingServiceStats")) {
               JobSchedulingServiceStatsPrxHelper __h = new JobSchedulingServiceStatsPrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var5) {
         }
      }

      return __d;
   }

   public static JobSchedulingServiceStatsPrx checkedCast(ObjectPrx __obj, String __facet, Map<String, String> __ctx) {
      JobSchedulingServiceStatsPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::JobSchedulingServiceStats", __ctx)) {
               JobSchedulingServiceStatsPrxHelper __h = new JobSchedulingServiceStatsPrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var6) {
         }
      }

      return __d;
   }

   public static JobSchedulingServiceStatsPrx uncheckedCast(ObjectPrx __obj) {
      JobSchedulingServiceStatsPrx __d = null;
      if (__obj != null) {
         try {
            __d = (JobSchedulingServiceStatsPrx)__obj;
         } catch (ClassCastException var4) {
            JobSchedulingServiceStatsPrxHelper __h = new JobSchedulingServiceStatsPrxHelper();
            __h.__copyFrom(__obj);
            __d = __h;
         }
      }

      return (JobSchedulingServiceStatsPrx)__d;
   }

   public static JobSchedulingServiceStatsPrx uncheckedCast(ObjectPrx __obj, String __facet) {
      JobSchedulingServiceStatsPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);
         JobSchedulingServiceStatsPrxHelper __h = new JobSchedulingServiceStatsPrxHelper();
         __h.__copyFrom(__bb);
         __d = __h;
      }

      return __d;
   }

   protected _ObjectDelM __createDelegateM() {
      return new _JobSchedulingServiceStatsDelM();
   }

   protected _ObjectDelD __createDelegateD() {
      return new _JobSchedulingServiceStatsDelD();
   }

   public static void __write(BasicStream __os, JobSchedulingServiceStatsPrx v) {
      __os.writeProxy(v);
   }

   public static JobSchedulingServiceStatsPrx __read(BasicStream __is) {
      ObjectPrx proxy = __is.readProxy();
      if (proxy != null) {
         JobSchedulingServiceStatsPrxHelper result = new JobSchedulingServiceStatsPrxHelper();
         result.__copyFrom(proxy);
         return result;
      } else {
         return null;
      }
   }
}
