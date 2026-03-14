package com.projectgoth.fusion.recommendation.collector;

public class CollectorSinkException extends RuntimeException {
   public CollectorSinkException() {
   }

   public CollectorSinkException(String msg, Throwable t) {
      super(msg, t);
   }

   public CollectorSinkException(String msg) {
      super(msg);
   }

   public CollectorSinkException(Throwable t) {
      super(t);
   }
}
