package com.projectgoth.fusion.reputation.file;

import java.io.BufferedReader;

public class FileEntry {
   private BufferedReader reader;
   private String[] line;

   public FileEntry(BufferedReader reader, String[] line) {
      this.reader = reader;
      this.line = line;
   }

   public BufferedReader getReader() {
      return this.reader;
   }

   public String[] getLine() {
      return this.line;
   }
}
