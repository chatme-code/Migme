package com.projectgoth.fusion.reputation.util;

public class FileLocation {
   private String directory;
   private String filename;

   public FileLocation(String directory, String filename) {
      this.directory = directory;
      this.filename = filename;
   }

   public String getDirectory() {
      return this.directory;
   }

   public String getFilename() {
      return this.filename;
   }
}
