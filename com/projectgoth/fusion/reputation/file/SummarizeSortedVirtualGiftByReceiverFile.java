package com.projectgoth.fusion.reputation.file;

import com.projectgoth.fusion.reputation.domain.VirtualGiftMetrics;
import com.projectgoth.fusion.reputation.util.DirectoryHolder;
import com.projectgoth.fusion.reputation.util.DirectoryUtils;

public class SummarizeSortedVirtualGiftByReceiverFile extends AbstractSummarizeSortedVirtualGiftFile {
   public SummarizeSortedVirtualGiftByReceiverFile(DirectoryHolder directoryHolder) {
      super(directoryHolder);
   }

   protected void updateMetrics(VirtualGiftMetrics metrics) {
      metrics.addVirtualGiftsReceived(1);
   }

   protected int usernameIndex() {
      return 1;
   }

   public static void main(String[] args) {
      SummarizeSortedVirtualGiftByReceiverFile summarizeFile = new SummarizeSortedVirtualGiftByReceiverFile(new DirectoryHolder(DirectoryUtils.getDataDirectory(), DirectoryUtils.getScratchDirectory(), DirectoryUtils.getDumpDirectory()));

      try {
         summarizeFile.go(args[0]);
      } catch (Throwable var3) {
         var3.printStackTrace();
      }

   }
}
