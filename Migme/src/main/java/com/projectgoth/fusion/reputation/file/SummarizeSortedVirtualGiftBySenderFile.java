package com.projectgoth.fusion.reputation.file;

import com.projectgoth.fusion.reputation.domain.VirtualGiftMetrics;
import com.projectgoth.fusion.reputation.util.DirectoryHolder;
import com.projectgoth.fusion.reputation.util.DirectoryUtils;

public class SummarizeSortedVirtualGiftBySenderFile extends AbstractSummarizeSortedVirtualGiftFile {
   public SummarizeSortedVirtualGiftBySenderFile(DirectoryHolder directoryHolder) {
      super(directoryHolder);
   }

   protected void updateMetrics(VirtualGiftMetrics metrics) {
      metrics.addVirtualGiftsSent(1);
   }

   protected int usernameIndex() {
      return 5;
   }

   public static void main(String[] args) {
      SummarizeSortedVirtualGiftBySenderFile summarizeFile = new SummarizeSortedVirtualGiftBySenderFile(new DirectoryHolder(DirectoryUtils.getDataDirectory(), DirectoryUtils.getScratchDirectory(), DirectoryUtils.getDumpDirectory()));

      try {
         summarizeFile.go(args[0]);
      } catch (Throwable var3) {
         var3.printStackTrace();
      }

   }
}
