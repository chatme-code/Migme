package com.projectgoth.fusion.gateway;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.MultiSampler;
import com.projectgoth.fusion.common.Sampler;
import com.projectgoth.fusion.common.SystemProperty;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;

public class SamplingTask implements Runnable {
   private final GatewayContext gatewayContext;
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(Gateway.class));
   private static final String SEPERATOR = ",";
   private static final String NEW_LINE = "\r\n";
   MultiSampler sampler = new MultiSampler();
   private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
   private SimpleDateFormat fileDateFormat = new SimpleDateFormat("yyyy-MM-dd");
   private DecimalFormat decimalFormat = new DecimalFormat("0.00");
   private String samepleFile;
   private String summaryFile;

   public SamplingTask(GatewayContext gatewayContext) {
      this.samepleFile = System.getProperty("log.dir") + File.separator + System.getProperty("log.filename") + ".inst.sample.";
      this.summaryFile = System.getProperty("log.dir") + File.separator + System.getProperty("log.filename") + ".inst.summary.";
      this.gatewayContext = gatewayContext;
   }

   public void run() {
      boolean var12 = false;

      long samplingInterval;
      label110: {
         try {
            var12 = true;
            List<Sampler.Summary> summaries = this.sampler.summarize();
            boolean var2 = SystemProperty.getBool("LogInstrumentationSample", true);
            if (var2) {
               FileOutputStream out = new FileOutputStream(this.samepleFile + this.fileDateFormat.format(new Date()), true);
               Iterator i$ = summaries.iterator();

               while(i$.hasNext()) {
                  Sampler.Summary summary = (Sampler.Summary)i$.next();
                  this.logSamples(summary.name, summary.samples, out);
               }

               out.close();
            }

            boolean logSummary = SystemProperty.getBool("LogInstrumentationSummary", true);
            if (!logSummary) {
               var12 = false;
            } else {
               FileOutputStream out = new FileOutputStream(this.summaryFile + this.fileDateFormat.format(new Date()), true);
               Iterator i$ = summaries.iterator();

               while(i$.hasNext()) {
                  Sampler.Summary summary = (Sampler.Summary)i$.next();
                  this.logSummary(summary, out);
               }

               out.close();
               var12 = false;
            }
            break label110;
         } catch (Exception var13) {
            log.error("Error in summarizing instrumentation samples", var13);
            var12 = false;
         } finally {
            if (var12) {
               long samplingInterval = SystemProperty.getLong("SamplingInverval", 60L) * 1000L;
               ((InstrumentedThreadPool)this.gatewayContext.getGatewayThreadPool().get(Gateway.ThreadPoolName.PRIMARY)).schedule(this, samplingInterval, TimeUnit.MILLISECONDS);
            }
         }

         samplingInterval = SystemProperty.getLong("SamplingInverval", 60L) * 1000L;
         ((InstrumentedThreadPool)this.gatewayContext.getGatewayThreadPool().get(Gateway.ThreadPoolName.PRIMARY)).schedule(this, samplingInterval, TimeUnit.MILLISECONDS);
         return;
      }

      samplingInterval = SystemProperty.getLong("SamplingInverval", 60L) * 1000L;
      ((InstrumentedThreadPool)this.gatewayContext.getGatewayThreadPool().get(Gateway.ThreadPoolName.PRIMARY)).schedule(this, samplingInterval, TimeUnit.MILLISECONDS);
   }

   private void logSamples(String name, Sampler.Sample[] samples, OutputStream out) throws IOException {
      Sampler.Sample[] arr$ = samples;
      int len$ = samples.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         Sampler.Sample sample = arr$[i$];
         StringBuilder builder = new StringBuilder();
         builder.append(this.dateFormat.format(sample.timestamp)).append(",").append(name).append(",").append(1).append(",").append(sample.data).append(",").append(sample.data).append("\r\n");
         out.write(builder.toString().getBytes());
      }

   }

   private void logSummary(Sampler.Summary summary, OutputStream out) throws IOException {
      StringBuilder builder = new StringBuilder();
      builder.append(this.dateFormat.format(new Date())).append(",").append(summary.name).append(",").append(summary.count).append(",").append(summary.min).append(",").append(summary.median).append(",").append(summary.max).append(",").append(this.decimalFormat.format(summary.mean)).append(",").append(this.decimalFormat.format(summary.mean1Percentile)).append(",").append(this.decimalFormat.format(summary.mean5Percentile)).append(",").append(this.decimalFormat.format(summary.standardDeviation)).append("\r\n");
      out.write(builder.toString().getBytes());
   }
}
