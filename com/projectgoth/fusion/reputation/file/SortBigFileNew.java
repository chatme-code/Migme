package com.projectgoth.fusion.reputation.file;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.reputation.util.DirectoryHolder;
import com.projectgoth.fusion.reputation.util.FileLocation;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;

public class SortBigFileNew {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(SortBigFileNew.class));
   public static final int LINES_PER_FILE = 25000;
   public static final int THREAD_COUNT = 4;
   public static final String CHUNK_POSTFIX = "chunk_";
   private DirectoryHolder directoryHolder;
   private int expectedFieldCount;
   private char delimeter = '|';
   private boolean checkExpectedFieldCount;
   private long start;
   private long readFromFile;

   public SortBigFileNew(DirectoryHolder directoryHolder) {
      this.directoryHolder = directoryHolder;
      log.info(directoryHolder);
   }

   private boolean addFileEntryFromReader(List<FileEntry> topEntries, Comparator<FileEntry> comparator, BufferedReader reader) throws IOException {
      this.start = System.currentTimeMillis();
      String line = reader.readLine();
      this.readFromFile += System.currentTimeMillis() - this.start;
      if (line == null) {
         return false;
      } else {
         FileEntry fileEntry = new FileEntry(reader, StringUtil.splitIntoArray(line, this.delimeter));
         int index = Collections.binarySearch(topEntries, fileEntry, comparator);
         if (index < 0) {
            index = -1 * index - 1;
         }

         topEntries.add(index, fileEntry);
         return true;
      }
   }

   private void sortAndDumpChunks(FileLocation outLocation, List<List<String>> lines, Comparator<List<String>> comparator, SortBigFileNew.ChunkResult chunkResult) throws IOException {
      log.debug("received " + lines.size() + " chunks to sort and dump");
      int processedChunks = 0;
      long start = System.currentTimeMillis();
      Thread[] threads = new Thread[4];

      int i;
      for(i = 0; i < 4; ++i) {
         if (!((List)lines.get(i)).isEmpty()) {
            log.debug("chunk " + i + " is not empty and will be sorted and dumped");
            BufferedWriter writer = new BufferedWriter(new FileWriter(this.directoryHolder.getScratchDirectory() + outLocation.getFilename() + "." + "chunk_" + (chunkResult.getChunkCount() + i + 1)));
            threads[i] = new SortAndDumpChunkThread((List)lines.get(i), this.expectedFieldCount, comparator, writer, this.delimeter, this.checkExpectedFieldCount);
            threads[i].start();
            ++processedChunks;
         }
      }

      for(i = 0; i < 4; ++i) {
         try {
            if (threads[i] != null) {
               threads[i].join();
               chunkResult.setLastLine((String)((List)lines.get(i)).get(((List)lines.get(i)).size() - 1));
               ((List)lines.get(i)).clear();
            }
         } catch (InterruptedException var11) {
         }
      }

      log.debug("sortAndDumpChunks took " + (double)(System.currentTimeMillis() - start) / 1000.0D + " secs in wall time and wrote " + processedChunks + " chunks");
      chunkResult.addChunkCount(processedChunks);
   }

   private SortBigFileNew.ChunkResult chunkFileNewConcurrent(FileLocation inLocation, FileLocation outLocation, Comparator<List<String>> comparator) throws IOException {
      log.info("starting chunking [" + inLocation.getDirectory() + inLocation.getFilename() + "]");
      long start = System.currentTimeMillis();
      List<List<String>> lines = new ArrayList();

      int linesRead;
      for(linesRead = 0; linesRead < 4; ++linesRead) {
         lines.add(new ArrayList());
      }

      linesRead = 0;
      SortBigFileNew.ChunkResult chunkResult = new SortBigFileNew.ChunkResult();
      int linesIndex = 0;
      BufferedReader reader = new BufferedReader(new FileReader(inLocation.getDirectory() + inLocation.getFilename()));

      String line;
      while((line = reader.readLine()) != null) {
         if (StringUtils.hasLength(line)) {
            ++linesRead;
            if (linesRead % 25000 == 0) {
               ++linesIndex;
            }

            if (linesRead < 100000) {
               ((List)lines.get(linesIndex)).add(line);
            } else {
               ((List)lines.get(linesIndex - 1)).add(line);
               this.sortAndDumpChunks(outLocation, lines, comparator, chunkResult);
               linesRead = 0;
               linesIndex = 0;
            }
         }
      }

      log.debug("chunks done after main while loop " + chunkResult.chunkCount);
      if (!lines.isEmpty()) {
         log.debug("we have some remainde chunks: " + lines.size());
         this.sortAndDumpChunks(outLocation, lines, comparator, chunkResult);
         log.debug("chunks done after remainder has been done " + chunkResult.chunkCount);
      }

      long end = System.currentTimeMillis();
      log.info("done chunking " + inLocation.getDirectory() + inLocation.getFilename() + ", took " + (double)(end - start) / 1000.0D + " seconds");
      return chunkResult;
   }

   private void mergeChunks(FileLocation outLocation, int chunkCount, Comparator<FileEntry> comparator) throws IOException {
      log.debug("parsing chunks");
      BufferedWriter finalWriter = new BufferedWriter(new FileWriter(outLocation.getDirectory() + outLocation.getFilename() + ".sorted"));
      List<BufferedReader> readers = new ArrayList(chunkCount);
      LinkedList<FileEntry> topEntries = new LinkedList();

      for(int i = 1; i <= chunkCount; ++i) {
         BufferedReader localReader = new BufferedReader(new FileReader(this.directoryHolder.getScratchDirectory() + outLocation.getFilename() + "." + "chunk_" + i));
         readers.add(localReader);
         this.addFileEntryFromReader(topEntries, comparator, localReader);
      }

      if (topEntries.size() != chunkCount) {
         log.error("entries after readers has been setup: " + topEntries.size() + " (this should have equaled chunk count [" + chunkCount + "])");
         throw new IOException("Read chunks does not equal chunk count?");
      } else {
         long removeFirst = 0L;
         long writeToFile = 0L;

         long start;
         long readFromFileAndAdd;
         int i;
         for(readFromFileAndAdd = 0L; !readers.isEmpty(); readFromFileAndAdd += System.currentTimeMillis() - start) {
            start = System.currentTimeMillis();
            FileEntry smallestEntry = (FileEntry)topEntries.removeFirst();
            removeFirst += System.currentTimeMillis() - start;
            start = System.currentTimeMillis();

            for(i = 0; i < smallestEntry.getLine().length; ++i) {
               finalWriter.write(smallestEntry.getLine()[i]);
               if (i < smallestEntry.getLine().length - 1) {
                  finalWriter.write(this.delimeter);
               }
            }

            finalWriter.newLine();
            writeToFile += System.currentTimeMillis() - start;
            start = System.currentTimeMillis();
            if (!this.addFileEntryFromReader(topEntries, comparator, smallestEntry.getReader())) {
               readers.remove(smallestEntry.getReader());
            }
         }

         log.debug("all the readers are empty...");
         log.debug(topEntries.size() + " entries left in topEntries");
         log.debug("removeFirst: " + removeFirst);
         log.debug("writeToFile: " + writeToFile);
         log.debug("readFromFile: " + this.readFromFile);
         log.debug("readFromFileAndAdd: " + readFromFileAndAdd);
         Iterator i$ = topEntries.iterator();

         while(i$.hasNext()) {
            FileEntry entry = (FileEntry)i$.next();
            finalWriter.write(StringUtil.asStringWithoutQuotes(entry.getLine()));
            finalWriter.newLine();
         }

         for(i = 0; i < readers.size(); ++i) {
            ((BufferedReader)readers.get(i)).close();
         }

         finalWriter.close();
      }
   }

   public String go(FileLocation inLocation, FileLocation outLocation, Comparator<FileEntry> fileEntryComparator, Comparator<List<String>> linePartsComparator, int expectedFieldCount, char delimeter) throws IOException {
      return this.go(inLocation, outLocation, fileEntryComparator, linePartsComparator, expectedFieldCount, delimeter, false);
   }

   public String go(FileLocation inLocation, FileLocation outLocation, Comparator<FileEntry> fileEntryComparator, Comparator<List<String>> linePartsComparator, int expectedFieldCount, char delimeter, boolean checkExpectedFieldCount) throws IOException {
      this.expectedFieldCount = expectedFieldCount;
      this.delimeter = delimeter;
      this.checkExpectedFieldCount = checkExpectedFieldCount;
      log.info("sorting [" + inLocation.getDirectory() + inLocation.getFilename() + "]");
      long start = System.currentTimeMillis();
      SortBigFileNew.ChunkResult chunkResult = this.chunkFileNewConcurrent(inLocation, outLocation, linePartsComparator);
      log.info("we have " + chunkResult.getChunkCount() + " chunks to merge");
      this.mergeChunks(outLocation, chunkResult.getChunkCount(), fileEntryComparator);
      log.info("done sorting [" + inLocation.getDirectory() + inLocation.getFilename() + "], took " + (System.currentTimeMillis() - start) / 1000L + " seconds");
      return chunkResult.getLastLine();
   }

   private class ChunkResult {
      private int chunkCount;
      private String lastLine;

      private ChunkResult() {
         this.chunkCount = 0;
      }

      public int getChunkCount() {
         return this.chunkCount;
      }

      public void addChunkCount(int chunkCount) {
         this.chunkCount += chunkCount;
      }

      public String getLastLine() {
         return this.lastLine;
      }

      public void setLastLine(String lastLine) {
         this.lastLine = lastLine;
      }

      // $FF: synthetic method
      ChunkResult(Object x1) {
         this();
      }
   }
}
