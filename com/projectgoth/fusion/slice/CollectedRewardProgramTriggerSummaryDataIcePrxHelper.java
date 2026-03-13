package com.projectgoth.fusion.slice;

import Ice.FacetNotExistException;
import Ice.ObjectPrx;
import Ice.ObjectPrxHelperBase;
import Ice._ObjectDelD;
import Ice._ObjectDelM;
import IceInternal.BasicStream;
import java.util.Map;

public final class CollectedRewardProgramTriggerSummaryDataIcePrxHelper extends ObjectPrxHelperBase implements CollectedRewardProgramTriggerSummaryDataIcePrx {
   public static CollectedRewardProgramTriggerSummaryDataIcePrx checkedCast(ObjectPrx __obj) {
      CollectedRewardProgramTriggerSummaryDataIcePrx __d = null;
      if (__obj != null) {
         try {
            __d = (CollectedRewardProgramTriggerSummaryDataIcePrx)__obj;
         } catch (ClassCastException var4) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::CollectedRewardProgramTriggerSummaryDataIce")) {
               CollectedRewardProgramTriggerSummaryDataIcePrxHelper __h = new CollectedRewardProgramTriggerSummaryDataIcePrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (CollectedRewardProgramTriggerSummaryDataIcePrx)__d;
   }

   public static CollectedRewardProgramTriggerSummaryDataIcePrx checkedCast(ObjectPrx __obj, Map<String, String> __ctx) {
      CollectedRewardProgramTriggerSummaryDataIcePrx __d = null;
      if (__obj != null) {
         try {
            __d = (CollectedRewardProgramTriggerSummaryDataIcePrx)__obj;
         } catch (ClassCastException var5) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::CollectedRewardProgramTriggerSummaryDataIce", __ctx)) {
               CollectedRewardProgramTriggerSummaryDataIcePrxHelper __h = new CollectedRewardProgramTriggerSummaryDataIcePrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (CollectedRewardProgramTriggerSummaryDataIcePrx)__d;
   }

   public static CollectedRewardProgramTriggerSummaryDataIcePrx checkedCast(ObjectPrx __obj, String __facet) {
      CollectedRewardProgramTriggerSummaryDataIcePrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::CollectedRewardProgramTriggerSummaryDataIce")) {
               CollectedRewardProgramTriggerSummaryDataIcePrxHelper __h = new CollectedRewardProgramTriggerSummaryDataIcePrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var5) {
         }
      }

      return __d;
   }

   public static CollectedRewardProgramTriggerSummaryDataIcePrx checkedCast(ObjectPrx __obj, String __facet, Map<String, String> __ctx) {
      CollectedRewardProgramTriggerSummaryDataIcePrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::CollectedRewardProgramTriggerSummaryDataIce", __ctx)) {
               CollectedRewardProgramTriggerSummaryDataIcePrxHelper __h = new CollectedRewardProgramTriggerSummaryDataIcePrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var6) {
         }
      }

      return __d;
   }

   public static CollectedRewardProgramTriggerSummaryDataIcePrx uncheckedCast(ObjectPrx __obj) {
      CollectedRewardProgramTriggerSummaryDataIcePrx __d = null;
      if (__obj != null) {
         try {
            __d = (CollectedRewardProgramTriggerSummaryDataIcePrx)__obj;
         } catch (ClassCastException var4) {
            CollectedRewardProgramTriggerSummaryDataIcePrxHelper __h = new CollectedRewardProgramTriggerSummaryDataIcePrxHelper();
            __h.__copyFrom(__obj);
            __d = __h;
         }
      }

      return (CollectedRewardProgramTriggerSummaryDataIcePrx)__d;
   }

   public static CollectedRewardProgramTriggerSummaryDataIcePrx uncheckedCast(ObjectPrx __obj, String __facet) {
      CollectedRewardProgramTriggerSummaryDataIcePrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);
         CollectedRewardProgramTriggerSummaryDataIcePrxHelper __h = new CollectedRewardProgramTriggerSummaryDataIcePrxHelper();
         __h.__copyFrom(__bb);
         __d = __h;
      }

      return __d;
   }

   protected _ObjectDelM __createDelegateM() {
      return new _CollectedRewardProgramTriggerSummaryDataIceDelM();
   }

   protected _ObjectDelD __createDelegateD() {
      return new _CollectedRewardProgramTriggerSummaryDataIceDelD();
   }

   public static void __write(BasicStream __os, CollectedRewardProgramTriggerSummaryDataIcePrx v) {
      __os.writeProxy(v);
   }

   public static CollectedRewardProgramTriggerSummaryDataIcePrx __read(BasicStream __is) {
      ObjectPrx proxy = __is.readProxy();
      if (proxy != null) {
         CollectedRewardProgramTriggerSummaryDataIcePrxHelper result = new CollectedRewardProgramTriggerSummaryDataIcePrxHelper();
         result.__copyFrom(proxy);
         return result;
      } else {
         return null;
      }
   }
}
