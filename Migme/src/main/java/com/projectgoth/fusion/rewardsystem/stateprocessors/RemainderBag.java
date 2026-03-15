package com.projectgoth.fusion.rewardsystem.stateprocessors;

public class RemainderBag<T> extends Bag<T> {
   private final boolean consumed;

   public RemainderBag(boolean consumed) {
      this.consumed = consumed;
   }

   public RemainderBag(int capacity, boolean consumed) {
      super(capacity);
      this.consumed = consumed;
   }

   public boolean isConsumed() {
      return this.consumed;
   }
}
