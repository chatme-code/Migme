package com.projectgoth.fusion.reputation.file;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.StringUtil;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;

public class SortAndDumpChunkThread extends Thread {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(SortAndDumpChunkThread.class));
   private List<String> lines;
   private int expectedFieldCount;
   private Comparator<List<String>> comparator;
   private BufferedWriter writer;
   private char delimeter;
   private boolean checkExpectedFieldCount;

   public SortAndDumpChunkThread(List<String> lines, int expectedFieldCount, Comparator<List<String>> comparator, BufferedWriter writer, char delimeter, boolean checkExpectedFieldCount) {
      this.lines = lines;
      this.expectedFieldCount = expectedFieldCount;
      this.comparator = comparator;
      this.writer = writer;
      this.delimeter = delimeter;
      this.checkExpectedFieldCount = checkExpectedFieldCount;
   }

   public void run() {
      List<List<String>> linesParts = new ArrayList();

      Iterator i$;
      List list;
      for(i$ = this.lines.iterator(); i$.hasNext(); linesParts.add(list)) {
         String line = (String)i$.next();
         list = StringUtil.split(line, this.delimeter);
         if (this.checkExpectedFieldCount && list.size() != this.expectedFieldCount) {
            log.warn("found more or less parts [" + list.size() + "] in this line [" + list + "]");
         }
      }

      Collections.sort(linesParts, this.comparator);

      try {
         i$ = linesParts.iterator();

         while(i$.hasNext()) {
            List<String> line = (List)i$.next();

            for(int i = 0; i < line.size(); ++i) {
               this.writer.write((String)line.get(i));
               if (i < line.size() - 1) {
                  this.writer.write(this.delimeter);
               }
            }

            this.writer.newLine();
         }

         this.writer.flush();
         this.writer.close();
      } catch (IOException var5) {
         log.error("failed to write chunk!", var5);
      }

   }
}
