package com.projectgoth.fusion.reputation.util;

public class DirectoryHolder {
   public static final String SCORE_FILE_NAME_PREFIX = "scored.merged.sessionarchive.accountentry.virtualgift.phonecall.";
   private String dataDirectory;
   private String scratchDirectory;
   private String dumpDirectory;

   public DirectoryHolder(String dataDirectory, String scratchDirectory, String dumpDirectory) {
      this.dataDirectory = dataDirectory;
      this.scratchDirectory = scratchDirectory;
      this.dumpDirectory = dumpDirectory;
   }

   public String getDataDirectory() {
      return this.dataDirectory;
   }

   public String getScratchDirectory() {
      return this.scratchDirectory;
   }

   public String getDumpDirectory() {
      return this.dumpDirectory;
   }

   public String getFinalMergedResultsFilename(String runDateString) {
      return "merged.sessionarchive.accountentry.virtualgift.phonecall." + runDateString;
   }

   public String getMergedVirtualGiftsFilename(String runDateString) {
      return "merged.virtualgift." + runDateString;
   }

   public String getMergedSessionArchiveAccountEntryFilename(String runDateString) {
      return "merged.sessionarchive.accountentry." + runDateString;
   }

   public String getMergedSessionArchiveAccountEntryVirtualGiftFilename(String runDateString) {
      return "merged.sessionarchive.accountentry.virtualgift." + runDateString;
   }

   public String getScoreFilename(String runDateString) {
      return "scored.merged.sessionarchive.accountentry.virtualgift.phonecall." + runDateString;
   }

   public String getPhoneCallSummaryFilename(String runDateString) {
      return "phonecall." + runDateString + ".csv.sorted.processed";
   }

   public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append("Directory Holder, data [").append(this.dataDirectory).append("] scratch [").append(this.scratchDirectory).append("] dump [").append(this.dumpDirectory).append("]");
      return builder.toString();
   }
}
