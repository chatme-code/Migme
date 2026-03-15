package com.projectgoth.fusion.reputation.file.comparator;

import java.util.Comparator;
import java.util.List;

public class SingleIndexIntegerStringListComparator implements Comparator<List<String>> {
   private int index;

   public SingleIndexIntegerStringListComparator(int index) {
      this.index = index;
   }

   public int compare(List<String> lhs, List<String> rhs) {
      int lhsValue = Integer.parseInt((String)lhs.get(this.index));
      int rhsValue = Integer.parseInt((String)rhs.get(this.index));
      if (lhsValue < rhsValue) {
         return -1;
      } else {
         return lhsValue > rhsValue ? 1 : 0;
      }
   }
}
