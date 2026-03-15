package com.projectgoth.fusion.common;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class MultiSampler {
   private Map<String, Sampler> samplers = new ConcurrentHashMap();

   public void add(String category, long duration) {
      Sampler sampler = (Sampler)this.samplers.get(category);
      if (sampler == null && !this.samplers.containsKey(category)) {
         sampler = new Sampler(category);
         this.samplers.put(category, sampler);
      }

      sampler.add(duration);
   }

   public List<Sampler.Summary> summarize() {
      List<Sampler.Summary> summaries = new LinkedList();
      Iterator i$ = this.samplers.entrySet().iterator();

      while(i$.hasNext()) {
         Entry<String, Sampler> e = (Entry)i$.next();
         Sampler.Summary summary = ((Sampler)e.getValue()).summarize();
         if (summary.count > 0) {
            summaries.add(summary);
         }
      }

      return summaries;
   }
}
