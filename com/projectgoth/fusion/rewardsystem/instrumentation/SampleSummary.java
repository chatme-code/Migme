package com.projectgoth.fusion.rewardsystem.instrumentation;

import com.projectgoth.fusion.common.ConfigUtils;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import org.apache.log4j.Logger;

public class SampleSummary implements Serializable {
   public static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(SampleSummary.class));
   private long receivedCount = 0L;
   private long droppedCount = 0L;
   private long successfulCount = 0L;
   private long failedCount = 0L;
   private long minReceivedTimestamp = -1L;
   private long maxReceivedTimestamp = -1L;
   private final IncrementalStats timeSpentInQueue = new IncrementalStats();
   private final IncrementalStats timeSpentProcessingAfterDequeue = new IncrementalStats();
   private final SampleCategory sampleCategory;
   private final String summaryId;

   public SampleSummary(String summaryId, SampleCategory sampleCategory) {
      this.sampleCategory = sampleCategory;
      this.summaryId = summaryId;
   }

   public SampleCategory getSampleCategory() {
      return this.sampleCategory;
   }

   public String getSummaryId() {
      return this.summaryId;
   }

   private void preprocessReceived(Sample samplePoint) {
      long receivedTimestamp = samplePoint.getReceivedTimestamp();
      if (this.minReceivedTimestamp == -1L) {
         this.minReceivedTimestamp = receivedTimestamp;
      } else if (receivedTimestamp < this.minReceivedTimestamp) {
         this.minReceivedTimestamp = receivedTimestamp;
      }

      if (this.maxReceivedTimestamp == -1L) {
         this.maxReceivedTimestamp = receivedTimestamp;
      } else if (receivedTimestamp > this.maxReceivedTimestamp) {
         this.maxReceivedTimestamp = receivedTimestamp;
      }

      ++this.receivedCount;
   }

   public void append(Sample samplePoint) {
      this.preprocessReceived(samplePoint);
      ProcessingResultEnum processingResult = samplePoint.getProcessingResult();
      if (processingResult == ProcessingResultEnum.DROPPED) {
         ++this.droppedCount;
      } else {
         long timeInQueue = samplePoint.getDequeuedTimestamp() - samplePoint.getReceivedTimestamp();
         if (timeInQueue >= 0L) {
            this.timeSpentInQueue.append(timeInQueue);
         } else {
            log.warn("Negative time spent in queue");
            this.timeSpentInQueue.append(0L);
         }

         long processingTime = samplePoint.getEndProcessTimestamp() - samplePoint.getDequeuedTimestamp();
         if (processingTime >= 0L) {
            this.timeSpentProcessingAfterDequeue.append(processingTime);
         } else {
            log.warn("Negative time spent for processing after dequeue");
            this.timeSpentProcessingAfterDequeue.append(0L);
         }

         switch(processingResult) {
         case FAILED:
            ++this.failedCount;
            break;
         case SUCCESSFUL:
            ++this.successfulCount;
         }
      }

   }

   public long getReceivedCount() {
      return this.receivedCount;
   }

   public long getDroppedCount() {
      return this.droppedCount;
   }

   public long getFailedCount() {
      return this.failedCount;
   }

   public long getSuccessfulCount() {
      return this.successfulCount;
   }

   public long getCountTimeSpentInQueue() {
      return this.timeSpentInQueue.getSampleCount();
   }

   public long getMinTimeSpentInQueue() {
      return this.timeSpentInQueue.getMinSample();
   }

   public long getMaxTimeSpentInQueue() {
      return this.timeSpentInQueue.getMaxSample();
   }

   public double getVarianceTimeSpentInQueue() {
      return this.timeSpentInQueue.getSampleVariance();
   }

   public double getMeanTimeSpentInQueue() {
      return this.timeSpentInQueue.getSampleMean();
   }

   public long getCountTimeSpentProcessingAfterDequeue() {
      return this.timeSpentProcessingAfterDequeue.getSampleCount();
   }

   public long getMinTimeSpentProcessingAfterDequeue() {
      return this.timeSpentProcessingAfterDequeue.getMinSample();
   }

   public long getMaxTimeSpentProcessingAfterDequeue() {
      return this.timeSpentProcessingAfterDequeue.getMaxSample();
   }

   public double getVarianceTimeSpentProcessingAfterDequeue() {
      return this.timeSpentProcessingAfterDequeue.getSampleVariance();
   }

   public double getMeanTimeSpentProcessingAfterDequeue() {
      return this.timeSpentProcessingAfterDequeue.getSampleMean();
   }

   public long getMaxReceivedTimestamp() {
      return this.maxReceivedTimestamp;
   }

   public long getMinReceivedTimestamp() {
      return this.minReceivedTimestamp;
   }

   public byte[] getBytes() throws IOException {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();

      try {
         ObjectOutputStream oos = new ObjectOutputStream(baos);

         try {
            oos.writeObject(this);
         } finally {
            oos.close();
         }
      } finally {
         baos.close();
      }

      return baos.toByteArray();
   }

   public static SampleSummary createFromBytes(byte[] data) throws IOException, ClassNotFoundException {
      ByteArrayInputStream bais = new ByteArrayInputStream(data);

      SampleSummary var3;
      try {
         ObjectInputStream ois = new ObjectInputStream(bais);

         try {
            var3 = (SampleSummary)ois.readObject();
         } finally {
            ois.close();
         }
      } finally {
         bais.close();
      }

      return var3;
   }

   public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append("SampleSummary [receivedCount=");
      builder.append(this.receivedCount);
      builder.append(", droppedCount=");
      builder.append(this.droppedCount);
      builder.append(", successfulCount=");
      builder.append(this.successfulCount);
      builder.append(", failedCount=");
      builder.append(this.failedCount);
      builder.append(", minReceivedTimestamp=");
      builder.append(this.minReceivedTimestamp);
      builder.append(", maxReceivedTimestamp=");
      builder.append(this.maxReceivedTimestamp);
      builder.append(", sampleCategory=");
      builder.append(this.sampleCategory);
      builder.append(", summaryId=");
      builder.append(this.summaryId);
      builder.append(", getReceivedCount()=");
      builder.append(this.getReceivedCount());
      builder.append(", getDroppedCount()=");
      builder.append(this.getDroppedCount());
      builder.append(", getFailedCount()=");
      builder.append(this.getFailedCount());
      builder.append(", getSuccessfulCount()=");
      builder.append(this.getSuccessfulCount());
      builder.append(", getCountTimeSpentInQueue()=");
      builder.append(this.getCountTimeSpentInQueue());
      builder.append(", getMinTimeSpentInQueue()=");
      builder.append(this.getMinTimeSpentInQueue());
      builder.append(", getMaxTimeSpentInQueue()=");
      builder.append(this.getMaxTimeSpentInQueue());
      builder.append(", getVarianceTimeSpentInQueue()=");
      builder.append(this.getVarianceTimeSpentInQueue());
      builder.append(", getMeanTimeSpentInQueue()=");
      builder.append(this.getMeanTimeSpentInQueue());
      builder.append(", getCountTimeSpentProcessingAfterDequeue()=");
      builder.append(this.getCountTimeSpentProcessingAfterDequeue());
      builder.append(", getMinTimeSpentProcessingAfterDequeue()=");
      builder.append(this.getMinTimeSpentProcessingAfterDequeue());
      builder.append(", getMaxTimeSpentProcessingAfterDequeue()=");
      builder.append(this.getMaxTimeSpentProcessingAfterDequeue());
      builder.append(", getVarianceTimeSpentProcessingAfterDequeue()=");
      builder.append(this.getVarianceTimeSpentProcessingAfterDequeue());
      builder.append(", getMeanTimeSpentProcessingAfterDequeue()=");
      builder.append(this.getMeanTimeSpentProcessingAfterDequeue());
      builder.append(", getMaxReceivedTimestamp()=");
      builder.append(this.getMaxReceivedTimestamp());
      builder.append(", getMinReceivedTimestamp()=");
      builder.append(this.getMinReceivedTimestamp());
      builder.append("]");
      return builder.toString();
   }
}
