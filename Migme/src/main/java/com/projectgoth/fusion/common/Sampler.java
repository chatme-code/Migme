package com.projectgoth.fusion.common;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Sampler {
   private List<Sampler.Sample> samples = new LinkedList();
   private String name;

   public Sampler(String name) {
      this.name = name;
   }

   public void add(long data) {
      synchronized(this.samples) {
         this.samples.add(new Sampler.Sample(data));
      }
   }

   public Sampler.Summary summarize() {
      Sampler.Sample[] tmp;
      synchronized(this.samples) {
         tmp = (Sampler.Sample[])this.samples.toArray(new Sampler.Sample[this.samples.size()]);
         this.samples.clear();
      }

      Sampler.Summary summary = new Sampler.Summary();
      summary.name = this.name;
      summary.samples = new Sampler.Sample[tmp.length];
      if (tmp.length > 0) {
         System.arraycopy(tmp, 0, summary.samples, 0, tmp.length);
         Arrays.sort(tmp);
         summary.count = tmp.length;
         summary.min = tmp[0].data;
         summary.max = tmp[tmp.length - 1].data;
         summary.median = tmp[tmp.length / 2].data;
         int count1Percentile = (int)Math.ceil((double)tmp.length / 100.0D);
         int count5Percentile = (int)Math.ceil((double)tmp.length / 20.0D);
         int pos1Percentile = tmp.length - count1Percentile;
         int pos5Percentile = tmp.length - count5Percentile;
         long sum = 0L;
         long sum1Percentile = 0L;
         long sum5Percentile = 0L;

         for(int i = 0; i < tmp.length; ++i) {
            sum += tmp[i].data;
            if (i >= pos1Percentile) {
               sum1Percentile += tmp[i].data;
            }

            if (i >= pos5Percentile) {
               sum5Percentile += tmp[i].data;
            }
         }

         summary.mean = (double)sum / (double)tmp.length;
         summary.mean1Percentile = (double)sum1Percentile / (double)count1Percentile;
         summary.mean5Percentile = (double)sum5Percentile / (double)count5Percentile;
         long spread = 0L;
         Sampler.Sample[] arr$ = tmp;
         int len$ = tmp.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            Sampler.Sample sample = arr$[i$];
            spread = (long)((double)spread + Math.pow((double)sample.data - summary.mean, 2.0D));
         }

         summary.standardDeviation = Math.sqrt((double)spread / (double)tmp.length);
      }

      return summary;
   }

   public static class Summary {
      public String name;
      public Sampler.Sample[] samples;
      public int count;
      public long min;
      public long median;
      public long max;
      public double mean;
      public double mean1Percentile;
      public double mean5Percentile;
      public double standardDeviation;
   }

   public static class Sample implements Comparable<Sampler.Sample> {
      public long timestamp = System.currentTimeMillis();
      public long data;

      public Sample(long data) {
         this.data = data;
      }

      public int compareTo(Sampler.Sample sample) {
         return (int)(this.data - sample.data);
      }
   }
}
