package com.projectgoth.fusion.ice;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.slice.FusionException;
import org.apache.log4j.Logger;

public abstract class IceAmdInvoker implements Runnable {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(IceAmdInvoker.class));

   public abstract boolean isAMDEnabled();

   public abstract void schedule(Runnable var1) throws Exception;

   public abstract void payload() throws Exception;

   public abstract void ice_response();

   public abstract void ice_exception(Exception var1);

   public abstract String getLogContext();

   public void invoke() throws FusionException {
      if (this.isAMDEnabled()) {
         if (log.isDebugEnabled()) {
            log.debug("Scheduling (AMD)..." + this.getLogContext());
         }

         try {
            this.schedule(this);
         } catch (Exception var2) {
            log.error("Exception scheduling AMD job: " + var2, var2);
            throw new FusionException("Exception scheduling AMD job: " + var2);
         }
      } else {
         if (log.isDebugEnabled()) {
            log.debug("Executing (non-AMD)..." + this.getLogContext());
         }

         this.run();
      }

   }

   public void run() {
      try {
         if (log.isDebugEnabled()) {
            log.debug("Executing..." + this.getLogContext());
         }

         this.payload();
         this.ice_response();
      } catch (Exception var2) {
         this.ice_exception(var2);
      }

   }
}
