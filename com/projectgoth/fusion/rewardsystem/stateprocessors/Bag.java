package com.projectgoth.fusion.rewardsystem.stateprocessors;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;

public class Bag<TItem> {
   private final HashMap<TItem, Bag.Counter> itemToCountMap;

   public Bag(int mapCapacity) {
      this.itemToCountMap = new HashMap(mapCapacity);
   }

   public Bag() {
      this(10);
   }

   public int add(TItem item) {
      Bag.Counter counter = (Bag.Counter)this.itemToCountMap.get(item);
      if (counter == null) {
         counter = new Bag.Counter();
         this.itemToCountMap.put(item, counter);
      }

      counter.inc();
      return counter.intValue();
   }

   public Bag<TItem> addAll(Collection<TItem> items) {
      Iterator i$ = items.iterator();

      while(i$.hasNext()) {
         TItem item = i$.next();
         this.add(item);
      }

      return this;
   }

   public Bag<TItem> setCount(TItem item, int count) {
      if (count > 0) {
         this.itemToCountMap.put(item, new Bag.Counter(count));
      } else {
         this.itemToCountMap.remove(item);
      }

      return this;
   }

   public int uniqueItemCount() {
      return this.itemToCountMap.size();
   }

   private Set<Entry<TItem, Bag.Counter>> entries() {
      return this.itemToCountMap.entrySet();
   }

   public Set<Entry<TItem, Integer>> entrySet() {
      return new AbstractSet<Entry<TItem, Integer>>() {
         public Iterator<Entry<TItem, Integer>> iterator() {
            return new Iterator<Entry<TItem, Integer>>() {
               private final Iterator<Entry<TItem, Bag.Counter>> baseIterator = Bag.this.entries().iterator();

               public boolean hasNext() {
                  return this.baseIterator.hasNext();
               }

               public Entry<TItem, Integer> next() {
                  final Entry<TItem, Bag.Counter> baseEntry = (Entry)this.baseIterator.next();
                  return new Entry<TItem, Integer>() {
                     public TItem getKey() {
                        return baseEntry.getKey();
                     }

                     public Integer getValue() {
                        return ((Bag.Counter)baseEntry.getValue()).intValue();
                     }

                     public Integer setValue(Integer value) {
                        throw new UnsupportedOperationException("setValue not supported");
                     }
                  };
               }

               public void remove() {
                  this.baseIterator.remove();
               }
            };
         }

         public int size() {
            return Bag.this.uniqueItemCount();
         }
      };
   }

   public int getCount(TItem item) {
      Bag.Counter count = (Bag.Counter)this.itemToCountMap.get(item);
      return count != null && count.intValue() > 0 ? count.intValue() : 0;
   }

   public void clear() {
      this.itemToCountMap.clear();
   }

   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      } else {
         return !(obj instanceof Bag) ? false : this.itemToCountMap.equals(((Bag)obj).itemToCountMap);
      }
   }

   public RemainderBag<TItem> matchAndFilter(Bag<TItem> itemsToRemove) {
      RemainderBag<TItem> nextConsumedBag = new RemainderBag(itemsToRemove.uniqueItemCount(), true);
      RemainderBag<TItem> nextNonConsumedBag = new RemainderBag(itemsToRemove.uniqueItemCount(), false);
      boolean foundNonConsumable = false;
      Iterator i$ = itemsToRemove.entries().iterator();

      while(true) {
         while(i$.hasNext()) {
            Entry<TItem, Bag.Counter> entriesToRemove = (Entry)i$.next();
            TItem itemToRemove = entriesToRemove.getKey();
            int currentCount = this.getCount(itemToRemove);
            nextNonConsumedBag.setCount(itemToRemove, currentCount);
            if (!foundNonConsumable && currentCount > 0) {
               Bag.Counter expectedCount = (Bag.Counter)entriesToRemove.getValue();
               int remainder = currentCount - expectedCount.intValue();
               if (remainder >= 0) {
                  nextConsumedBag.setCount(itemToRemove, remainder);
               } else {
                  foundNonConsumable = true;
                  nextConsumedBag.clear();
               }
            } else {
               foundNonConsumable = true;
               nextConsumedBag.clear();
            }
         }

         if (foundNonConsumable) {
            return nextNonConsumedBag;
         }

         return nextConsumedBag;
      }
   }

   public String toString() {
      return this.itemToCountMap.toString();
   }

   private static class Counter extends Number implements Comparable<Bag.Counter> {
      private int value;

      public Counter() {
         this(0);
      }

      public Counter(int val) {
         this.value = val;
      }

      void inc() {
         ++this.value;
      }

      public int intValue() {
         return this.value;
      }

      public boolean equals(Object obj) {
         if (obj == null) {
            return false;
         } else if (obj instanceof Bag.Counter) {
            return this.value == ((Bag.Counter)obj).value;
         } else {
            return false;
         }
      }

      public int hashCode() {
         return this.value;
      }

      public String toString() {
         return String.valueOf(this.value);
      }

      public int compareTo(Bag.Counter that) {
         return this.minus(that);
      }

      public int minus(Bag.Counter that) {
         return this.value - that.value;
      }

      public double doubleValue() {
         return (double)this.value;
      }

      public float floatValue() {
         return (float)this.value;
      }

      public long longValue() {
         return (long)this.value;
      }
   }
}
