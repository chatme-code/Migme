package com.projectgoth.fusion.recommendation.collector;

import com.projectgoth.fusion.slice.CollectedDataIce;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public final class DataCollectorWriterStrategy<TLoggable> {
   private final List<ICollectorSink<TLoggable>> collectorSinkList = new ArrayList();
   private final ICollectorTransformation<TLoggable> transformation;
   private final String name;

   public DataCollectorWriterStrategy(String name, ICollectorTransformation<TLoggable> transformation) {
      this.transformation = transformation;
      this.name = name;
   }

   public String getName() {
      return this.name;
   }

   public DataCollectorWriterStrategy<TLoggable> addSink(ICollectorSink<TLoggable> sink) {
      this.collectorSinkList.add(sink);
      return this;
   }

   public void write(CollectedDataIce dataIce, DataCollectorInvocationCtx invokeCtx) {
      String strategyName = this.getName();
      invokeCtx.beginStrategy(strategyName);

      try {
         String transformationName = this.transformation.getName();

         Collection loggables;
         try {
            invokeCtx.beforeTransformation(strategyName, transformationName);
            loggables = this.transformation.toLoggables(dataIce);
            invokeCtx.afterTransformation(strategyName, transformationName, (Exception)null);
         } catch (Exception var16) {
            invokeCtx.afterTransformation(strategyName, transformationName, var16);
            return;
         }

         Iterator i$ = this.collectorSinkList.iterator();

         while(i$.hasNext()) {
            ICollectorSink<TLoggable> logSink = (ICollectorSink)i$.next();
            String logSinkName = logSink.getName();

            try {
               invokeCtx.beforeWritingToSink(strategyName, transformationName, logSinkName);
               if (loggables != null && !loggables.isEmpty()) {
                  logSink.write(loggables);
               }

               invokeCtx.afterWritingToSink(strategyName, transformationName, logSinkName, (Exception)null);
            } catch (Exception var15) {
               invokeCtx.afterWritingToSink(strategyName, transformationName, logSinkName, var15);
            }
         }

      } finally {
         invokeCtx.afterStrategy(strategyName);
      }
   }
}
