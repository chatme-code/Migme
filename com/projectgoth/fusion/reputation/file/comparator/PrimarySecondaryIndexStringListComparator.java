package com.projectgoth.fusion.reputation.file.comparator;

import java.util.Comparator;
import java.util.List;

public class PrimarySecondaryIndexStringListComparator implements Comparator<List<String>> {
   private int primaryIndex;
   private int secondaryIndex;

   public PrimarySecondaryIndexStringListComparator(int primaryIndex, int secondaryIndex) {
      this.primaryIndex = primaryIndex;
      this.secondaryIndex = secondaryIndex;
   }

   public int compare(List<String> lhs, List<String> rhs) {
      int result = ((String)lhs.get(this.primaryIndex)).compareTo((String)rhs.get(this.primaryIndex));
      return result == 0 ? ((String)lhs.get(this.secondaryIndex)).compareTo((String)rhs.get(this.secondaryIndex)) : result;
   }
}
