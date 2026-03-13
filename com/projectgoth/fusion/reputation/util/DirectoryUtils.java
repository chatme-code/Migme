package com.projectgoth.fusion.reputation.util;

import org.springframework.util.StringUtils;

public abstract class DirectoryUtils {
   public static String getValidDirectory(String directory) {
      if (!StringUtils.hasLength(directory)) {
         return directory;
      } else {
         return !directory.endsWith("/") ? directory + "/" : directory;
      }
   }

   public static String getDataDirectory() {
      String dataDirectory = System.getProperty("rep.data.dir");
      if (!StringUtils.hasLength(dataDirectory)) {
         dataDirectory = "/reputation/";
      }

      return getValidDirectory(dataDirectory);
   }

   public static String getScratchDirectory() {
      String scratchDirectory = System.getProperty("rep.scratch.dir");
      if (!StringUtils.hasLength(scratchDirectory)) {
         scratchDirectory = "/reputation/scratch/";
      }

      return getValidDirectory(scratchDirectory);
   }

   public static String getDumpDirectory() {
      String dumpDirectory = System.getProperty("rep.dump.dir");
      if (!StringUtils.hasLength(dumpDirectory)) {
         dumpDirectory = "/reputation/dump/";
      }

      return getValidDirectory(dumpDirectory);
   }

   public static DirectoryHolder getDirectoryHolder() {
      DirectoryHolder directoryHolder = new DirectoryHolder(getDataDirectory(), getScratchDirectory(), getDumpDirectory());
      return directoryHolder;
   }
}
