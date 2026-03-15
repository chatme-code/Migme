package com.projectgoth.fusion.reputation.file.comparator;

import com.projectgoth.fusion.reputation.file.FileEntry;
import java.util.Comparator;

public class SingleIndexStringFileEntryComparator implements Comparator<FileEntry> {
   private int index;

   public SingleIndexStringFileEntryComparator(int index) {
      this.index = index;
   }

   public int compare(FileEntry lhs, FileEntry rhs) {
      String lhsValue = lhs.getLine()[this.index];
      String rhsValue = rhs.getLine()[this.index];
      if (lhsValue == null && rhsValue != null) {
         return 1;
      } else {
         return lhsValue != null && rhsValue == null ? -1 : lhsValue.compareTo(rhsValue);
      }
   }
}
