package com.projectgoth.fusion.imageserver;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.mogilefs.MogileFSManager;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.log4j.Logger;

public class ImageCache {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(ImageCache.class));
   private MogileFSManager mogileFSManager;
   private Map<String, ImageItem> cache = new LinkedHashMap();
   private int cacheSize;
   private int maxCacheSize;
   private boolean cacheOriginalImage;
   private boolean cacheScaledImage;
   private int hits;
   private int misses;
   private Object performanceCounterLock = new Object();

   public ImageCache(MogileFSManager mogileFSManager, int maxCacheSize, boolean cacheOriginalImage, boolean cacheScaledImage) {
      this.mogileFSManager = mogileFSManager;
      this.maxCacheSize = maxCacheSize;
      this.cacheOriginalImage = cacheOriginalImage;
      this.cacheScaledImage = cacheScaledImage;
   }

   public String getPerformanceSummary() {
      synchronized(this.performanceCounterLock) {
         double hitRate;
         if (this.hits + this.misses == 0) {
            hitRate = 0.0D;
         } else {
            hitRate = (double)this.hits / (double)(this.hits + this.misses);
         }

         return this.cache.size() + " files " + (new DecimalFormat("0.0%")).format(hitRate) + " hit rate (" + this.hits + " hits, " + this.misses + " misses)";
      }
   }

   private void hit() {
      synchronized(this.performanceCounterLock) {
         if (++this.hits == Integer.MAX_VALUE) {
            this.hits = 0;
            this.misses = 0;
         }

      }
   }

   private void miss() {
      synchronized(this.performanceCounterLock) {
         if (++this.misses == Integer.MAX_VALUE) {
            this.hits = 0;
            this.misses = 0;
         }

      }
   }

   public ImageItem getImage(String id, String format) throws Exception {
      if (log.isDebugEnabled()) {
         log.debug("Get image id = " + id + ", format = " + format);
      }

      ImageItem imageItem = this.getImageFromCache(id);
      if (imageItem == null) {
         this.miss();
         imageItem = new ImageItem(this.mogileFSManager.getFile(id), format);
         if (this.cacheOriginalImage) {
            this.cacheImage(id, imageItem);
         }
      } else {
         this.hit();
         if (!imageItem.getFormat().equalsIgnoreCase(format)) {
            imageItem = new ImageItem(imageItem.getBytes(), format);
         }
      }

      return imageItem;
   }

   public ImageItem getImage(String id, String format, int width, int height, boolean keepAspectRatio, boolean crop, float compressionQuality) throws Exception {
      if (log.isDebugEnabled()) {
         log.debug("Get image id = " + id + ", format = " + format + ", width = " + width + ", height = " + height + ", keep ratio = " + keepAspectRatio + ", crop = " + crop + ", compression quality = " + compressionQuality);
      }

      String key = id + ";" + format + ";" + width + ";" + height + ";" + keepAspectRatio + ";" + crop + ";" + compressionQuality;
      ImageItem imageItem = this.getImageFromCache(key);
      if (imageItem == null) {
         this.miss();
         imageItem = this.getImageFromCache(id);
         if (imageItem == null) {
            imageItem = new ImageItem(this.mogileFSManager.getFile(id));
            if (this.cacheOriginalImage) {
               this.cacheImage(id, imageItem);
            }
         }

         imageItem = new ImageItem(imageItem.getBytes(), format, width, height, keepAspectRatio, crop, compressionQuality);
         if (this.cacheScaledImage) {
            this.cacheImage(key, imageItem);
         }
      } else {
         this.hit();
      }

      return imageItem;
   }

   private ImageItem getImageFromCache(String key) {
      synchronized(this.cache) {
         return (ImageItem)this.cache.get(key);
      }
   }

   private void cacheImage(String key, ImageItem imageItem) {
      int imageSize = imageItem.size();
      if (this.maxCacheSize > 0 && imageSize < this.maxCacheSize) {
         synchronized(this.cache) {
            if (!this.cache.containsKey(key)) {
               Iterator i = this.cache.values().iterator();

               while(i.hasNext() && imageSize + this.cacheSize > this.maxCacheSize) {
                  this.cacheSize -= ((ImageItem)i.next()).size();
                  i.remove();
               }

               this.cache.put(key, imageItem);
               this.cacheSize += imageSize;
            }
         }
      }

   }
}
