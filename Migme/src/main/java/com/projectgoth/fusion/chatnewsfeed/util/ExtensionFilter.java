package com.projectgoth.fusion.chatnewsfeed.util;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class ExtensionFilter implements FilenameFilter {
   private List<String> extensions;
   private List<String> exclusions;

   public ExtensionFilter(List<String> extensions, List<String> exclusions) {
      this.extensions = (List)(extensions == null ? new ArrayList() : extensions);
      this.exclusions = (List)(exclusions == null ? new ArrayList() : exclusions);
   }

   public boolean accept(File dir, String name) {
      boolean extensionValid = false;
      Iterator i$ = this.extensions.iterator();

      while(i$.hasNext()) {
         String extension = (String)i$.next();
         if (name.endsWith(extension)) {
            extensionValid = true;
            break;
         }
      }

      boolean exclude = false;
      Iterator i$ = this.exclusions.iterator();

      while(i$.hasNext()) {
         String exclusion = (String)i$.next();
         if (name.equals(exclusion)) {
            exclude = true;
            break;
         }
      }

      return extensionValid && !exclude;
   }
}
