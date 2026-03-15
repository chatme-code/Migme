package com.projectgoth.fusion.common;

import java.lang.ref.WeakReference;
import java.util.concurrent.Semaphore;

public class FusionApplicationContext {
   private Semaphore semaphore = new Semaphore(1);

   protected FusionApplicationContext() {
      this.semaphore.acquireUninterruptibly();
   }

   protected <T> T extractProperty(WeakReference<T> reference) {
      if (null == reference) {
         try {
            this.semaphore.acquire();
         } catch (InterruptedException var3) {
            return null;
         }
      }

      return reference.get();
   }

   public void build() {
      this.semaphore.release();
   }
}
