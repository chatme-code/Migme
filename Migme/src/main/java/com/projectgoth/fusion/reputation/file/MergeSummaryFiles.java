package com.projectgoth.fusion.reputation.file;

import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.reputation.util.CSVUtils;
import com.projectgoth.fusion.reputation.util.DirectoryHolder;
import com.projectgoth.fusion.reputation.util.DirectoryUtils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import org.springframework.util.StringUtils;

public class MergeSummaryFiles {
   public static final char DELIMETER = ',';
   private DirectoryHolder directoryHolder;
   private String outFilename;

   public MergeSummaryFiles(DirectoryHolder directoryHolder) {
      this.directoryHolder = directoryHolder;
   }

   private String zeroColumns(int columns) {
      StringBuilder builder = new StringBuilder();

      for(int i = 0; i < columns; ++i) {
         builder.append(0);
         if (i < columns - 1) {
            builder.append(",");
         }
      }

      return builder.toString();
   }

   private void dumpTuple(BufferedWriter finalWriter, String lhsLine, String lhsEmptyColumns, String rhsLine, String rhsEmptyColumns, String joinColumn) throws IOException {
      if (lhsLine != null) {
         finalWriter.write(lhsLine);
      } else {
         finalWriter.write(joinColumn);
         finalWriter.write(44);
         finalWriter.write(lhsEmptyColumns);
      }

      finalWriter.write(",");
      if (rhsLine != null) {
         finalWriter.write(rhsLine.substring(rhsLine.indexOf(44) + 1));
      } else {
         finalWriter.write(rhsEmptyColumns);
      }

      finalWriter.newLine();
   }

   public void mergeJoinFiles(String outFilename, String lhsFilename, int lhsJoinIndex, int lhsColumns, String rhsFilename, int rhsJoinIndex, int rhsColumns) throws IOException {
      if (StringUtils.hasLength(lhsFilename) && StringUtils.hasLength(rhsFilename)) {
         this.outFilename = outFilename;
         if (!StringUtils.hasLength(this.outFilename)) {
            this.outFilename = "merged." + lhsFilename + "." + rhsFilename;
         }

         BufferedReader lhsReader = new BufferedReader(new FileReader(this.directoryHolder.getDataDirectory() + lhsFilename));
         BufferedReader rhsReader = new BufferedReader(new FileReader(this.directoryHolder.getDataDirectory() + rhsFilename));
         BufferedWriter finalWriter = new BufferedWriter(new FileWriter(this.directoryHolder.getDataDirectory() + this.outFilename));
         boolean lhsEmpty = false;
         boolean rhsEmpty = false;
         String lhsEmptyColumns = null;
         String rhsEmptyColumns = null;
         String lhsLine = lhsReader.readLine();
         if (lhsLine == null) {
            lhsEmpty = true;
         } else if (lhsColumns == 0) {
            lhsColumns = StringUtil.split(lhsLine, ',').size() - 1;
         }

         lhsEmptyColumns = this.zeroColumns(lhsColumns);
         String rhsLine = rhsReader.readLine();
         if (rhsLine == null) {
            rhsEmpty = true;
         } else if (rhsColumns == 0) {
            rhsColumns = StringUtil.split(rhsLine, ',').size() - 1;
         }

         rhsEmptyColumns = this.zeroColumns(rhsColumns);

         String lhsJoinColumn;
         String rhsJoinColumn;
         while(!lhsEmpty && !rhsEmpty) {
            lhsJoinColumn = CSVUtils.getColumnFromLine(lhsLine, lhsJoinIndex, ',');
            rhsJoinColumn = CSVUtils.getColumnFromLine(rhsLine, rhsJoinIndex, ',');
            if (lhsJoinColumn.equals(rhsJoinColumn)) {
               this.dumpTuple(finalWriter, lhsLine, lhsEmptyColumns, rhsLine, rhsEmptyColumns, lhsJoinColumn);
               lhsLine = lhsReader.readLine();
               rhsLine = rhsReader.readLine();
            } else if (lhsJoinColumn.compareTo(rhsJoinColumn) < 0) {
               this.dumpTuple(finalWriter, lhsLine, lhsEmptyColumns, (String)null, rhsEmptyColumns, lhsJoinColumn);
               lhsLine = lhsReader.readLine();
            } else {
               this.dumpTuple(finalWriter, (String)null, lhsEmptyColumns, rhsLine, rhsEmptyColumns, rhsJoinColumn);
               rhsLine = rhsReader.readLine();
            }

            if (lhsLine == null) {
               lhsEmpty = true;
            }

            if (rhsLine == null) {
               rhsEmpty = true;
            }
         }

         while(lhsLine != null) {
            lhsJoinColumn = CSVUtils.getColumnFromLine(lhsLine, lhsJoinIndex, ',');
            this.dumpTuple(finalWriter, lhsLine, lhsEmptyColumns, (String)null, rhsEmptyColumns, lhsJoinColumn);
            lhsLine = lhsReader.readLine();
         }

         while(rhsLine != null) {
            rhsJoinColumn = CSVUtils.getColumnFromLine(rhsLine, rhsJoinIndex, ',');
            this.dumpTuple(finalWriter, (String)null, lhsEmptyColumns, rhsLine, rhsEmptyColumns, rhsJoinColumn);
            rhsLine = rhsReader.readLine();
         }

         finalWriter.close();
      }
   }

   public static void main(String[] args) throws IOException {
      try {
         MergeSummaryFiles merger = new MergeSummaryFiles(DirectoryUtils.getDirectoryHolder());
         merger.mergeJoinFiles((String)null, "sessionarchive.csv.sorted.processed", 0, 0, "accountentry.csv.sorted.processed", 0, 0);
      } catch (Throwable var2) {
         var2.printStackTrace();
      }

   }
}
