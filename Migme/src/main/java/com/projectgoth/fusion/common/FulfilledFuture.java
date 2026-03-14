package com.projectgoth.fusion.common;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class FulfilledFuture<T> implements Future<T> {
   private final T value;

   public FulfilledFuture(T value) {
      this.value = value;
   }

   public boolean cancel(boolean mayInterruptIfRunning) {
      return false;
   }

   public T get() throws InterruptedException, ExecutionException {
      return this.value;
   }

   public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
      return this.get();
   }

   public boolean isCancelled() {
      return false;
   }

   public boolean isDone() {
      return true;
   }
}
