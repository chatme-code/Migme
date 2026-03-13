package com.projectgoth.fusion.recommendation.collector;

public class CollectorTransformationException extends RuntimeException {
   public CollectorTransformationException() {
   }

   public CollectorTransformationException(String msg, Throwable t) {
      super(msg, t);
   }

   public CollectorTransformationException(String msg) {
      super(msg);
   }

   public CollectorTransformationException(Throwable msg) {
      super(msg);
   }
}
