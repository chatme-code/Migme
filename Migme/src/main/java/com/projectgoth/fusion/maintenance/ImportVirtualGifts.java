package com.projectgoth.fusion.maintenance;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ImportVirtualGifts {
   private static Set<String> giftNames = new HashSet();

   public static void main(String[] args) {
      File startingDirectory = new File("/Users/dave/Documents/mig33/Nov 2009 Emoticons and Gifts/07-31 December 2009_Release");
      getGiftNames(startingDirectory);
      createInsertStatements();
      System.out.println("\n" + giftNames.size() + " gifts");
   }

   private static void getGiftNames(File dir) {
      File[] filesAndDirs = dir.listFiles();
      File[] arr$ = filesAndDirs;
      int len$ = filesAndDirs.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         File file = arr$[i$];
         if (file.isDirectory()) {
            getGiftNames(file);
         } else if (file.isFile() && (file.getName().endsWith("gif") || file.getName().endsWith("png"))) {
            giftNames.add(file.getName().substring(0, file.getName().length() - 7));

            try {
               copy(file.getPath(), "/Users/dave/Documents/mig33/Nov 2009 Emoticons and Gifts/All Gift Images/" + file.getName());
            } catch (IOException var7) {
               System.err.println("Unable to copy " + file.getName() + ": " + var7.getMessage());
            }
         }
      }

   }

   private static void createInsertStatements() {
      System.out.println("INSERT INTO virtualgift (name, hotkey, price, currency, numsold, groupid, groupviponly, location12x12gif, location12x12png, location14x14gif, location14x14png, location16x16gif, location16x16png, location64x64png, status) VALUES ");
      Iterator i$ = giftNames.iterator();

      while(i$.hasNext()) {
         String s = (String)i$.next();
         System.out.println("('" + s + "', '(vg_" + s + ")', $, 'USD', 0, NULL, NULL, " + "'/usr/fusion/emoticons/virtualgifts/" + s + "_12.gif', " + "'/usr/fusion/emoticons/virtualgifts/" + s + "_12.png', " + "'/usr/fusion/emoticons/virtualgifts/" + s + "_14.gif', " + "'/usr/fusion/emoticons/virtualgifts/" + s + "_14.png', " + "'/usr/fusion/emoticons/virtualgifts/" + s + "_16.gif', " + "'/usr/fusion/emoticons/virtualgifts/" + s + "_16.png', " + "'/usr/fusion/emoticons/virtualgifts/" + s + "_64.png', 0),");
      }

   }

   public static void copy(String fromFileName, String toFileName) throws IOException {
      File fromFile = new File(fromFileName);
      File toFile = new File(toFileName);
      if (!fromFile.exists()) {
         throw new IOException("FileCopy: no such source file: " + fromFileName);
      } else if (!fromFile.isFile()) {
         throw new IOException("FileCopy: can't copy directory: " + fromFileName);
      } else if (!fromFile.canRead()) {
         throw new IOException("FileCopy: source file is unreadable: " + fromFileName);
      } else {
         if (toFile.isDirectory()) {
            toFile = new File(toFile, fromFile.getName());
         }

         if (toFile.exists()) {
            if (!toFile.canWrite()) {
               throw new IOException("FileCopy: destination file is unwriteable: " + toFileName);
            }

            System.out.print("Overwrite existing file " + toFile.getName() + "? (Y/N): ");
            System.out.flush();
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            String response = in.readLine();
            if (!response.equals("Y") && !response.equals("y")) {
               throw new IOException("FileCopy: existing file was not overwritten.");
            }
         } else {
            String parent = toFile.getParent();
            if (parent == null) {
               parent = System.getProperty("user.dir");
            }

            File dir = new File(parent);
            if (!dir.exists()) {
               throw new IOException("FileCopy: destination directory doesn't exist: " + parent);
            }

            if (dir.isFile()) {
               throw new IOException("FileCopy: destination is not a directory: " + parent);
            }

            if (!dir.canWrite()) {
               throw new IOException("FileCopy: destination directory is unwriteable: " + parent);
            }
         }

         FileInputStream from = null;
         FileOutputStream to = null;

         try {
            from = new FileInputStream(fromFile);
            to = new FileOutputStream(toFile);
            byte[] buffer = new byte[4096];

            int bytesRead;
            while((bytesRead = from.read(buffer)) != -1) {
               to.write(buffer, 0, bytesRead);
            }
         } finally {
            if (from != null) {
               try {
                  from.close();
               } catch (IOException var17) {
               }
            }

            if (to != null) {
               try {
                  to.close();
               } catch (IOException var16) {
               }
            }

         }

      }
   }
}
