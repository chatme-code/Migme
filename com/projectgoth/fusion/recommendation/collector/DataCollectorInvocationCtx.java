package com.projectgoth.fusion.recommendation.collector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DataCollectorInvocationCtx {
   private List<DataCollectorInvocationCtx.ErrorData> errorDataList = new ArrayList();

   public void beginStrategy(String strategyName) {
   }

   public void afterStrategy(String strategyName) {
   }

   public void beforeTransformation(String strategyName, String transformationName) {
   }

   public void afterTransformation(String strategyName, String transformationName, Exception exceptionOccured) {
      if (exceptionOccured != null) {
         this.errorDataList.add(new DataCollectorInvocationCtx.TransformationErrorData(exceptionOccured, strategyName, transformationName));
      }

   }

   public void beforeWritingToSink(String strategyName, String lastTransformationName, String sinkName) {
   }

   public void afterWritingToSink(String strategyName, String lastTransformationName, String sinkName, Exception exceptionOccured) {
      if (exceptionOccured != null) {
         this.errorDataList.add(new DataCollectorInvocationCtx.SinkErrorData(exceptionOccured, strategyName, lastTransformationName, sinkName));
      }

   }

   public List<DataCollectorInvocationCtx.ErrorData> getErrorDataList() {
      return Collections.unmodifiableList(this.errorDataList);
   }

   public static class SinkErrorData extends DataCollectorInvocationCtx.ErrorData {
      private String strategyName;
      private String transformationName;
      private String sinkName;

      public String toString() {
         StringBuilder builder = new StringBuilder();
         builder.append("Sink Error [strategyName=");
         builder.append(this.strategyName);
         builder.append(", transformationName=");
         builder.append(this.transformationName);
         builder.append(", sinkName=");
         builder.append(this.sinkName);
         builder.append(", getException()=");
         builder.append(this.getException());
         builder.append("]");
         return builder.toString();
      }

      public SinkErrorData(Exception exception, String strategyName, String transformationName, String sinkName) {
         super(exception);
         this.strategyName = strategyName;
         this.transformationName = transformationName;
         this.sinkName = sinkName;
      }

      public String getStrategyName() {
         return this.strategyName;
      }

      public String getTransformationName() {
         return this.transformationName;
      }

      public String getSinkName() {
         return this.sinkName;
      }
   }

   public static class TransformationErrorData extends DataCollectorInvocationCtx.ErrorData {
      private String strategyName;
      private String transformationName;

      public String toString() {
         StringBuilder builder = new StringBuilder();
         builder.append("Transformation Error [strategyName=");
         builder.append(this.strategyName);
         builder.append(", transformationName=");
         builder.append(this.transformationName);
         builder.append(", getException()=");
         builder.append(this.getException());
         builder.append("]");
         return builder.toString();
      }

      public TransformationErrorData(Exception exception, String strategyName, String transformationName) {
         super(exception);
         this.strategyName = strategyName;
         this.transformationName = transformationName;
      }

      public String getStrategyName() {
         return this.strategyName;
      }

      public String getTransformationName() {
         return this.transformationName;
      }
   }

   public abstract static class ErrorData {
      private Exception exception;

      public ErrorData(Exception exception) {
         this.exception = exception;
      }

      public Exception getException() {
         return this.exception;
      }
   }
}
