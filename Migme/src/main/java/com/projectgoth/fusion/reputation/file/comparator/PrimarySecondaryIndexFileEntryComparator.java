package com.projectgoth.fusion.reputation.file.comparator;

import com.projectgoth.fusion.reputation.file.FileEntry;
import java.util.Comparator;

public class PrimarySecondaryIndexFileEntryComparator implements Comparator<FileEntry> {
   private int primaryIndex;
   private int secondaryIndex;

   public PrimarySecondaryIndexFileEntryComparator(int primaryIndex, int secondaryIndex) {
      this.primaryIndex = primaryIndex;
      this.secondaryIndex = secondaryIndex;
   }

   public int compare(FileEntry lhs, FileEntry rhs) {
      int result = lhs.getLine()[this.primaryIndex].compareTo(rhs.getLine()[this.primaryIndex]);
      return result == 0 ? lhs.getLine()[this.secondaryIndex].compareTo(rhs.getLine()[this.secondaryIndex]) : result;
   }
}
