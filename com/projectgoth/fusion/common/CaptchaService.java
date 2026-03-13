package com.projectgoth.fusion.common;

import com.danga.MemCached.MemCachedClient;
import java.awt.image.BufferedImage;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class CaptchaService {
   private CaptchaImageGenerator imageGenerator = new CaptchaImageGenerator();
   private Map<String, CaptchaService.CaptchaAnswer> localcache;
   private MemCachedClient memcache;
   private String characterSet = "abcdefghjkmnpqrstuvwxyz23456789";
   private long captchaValidPeriod = 300000L;
   public static final int CAPTCHA_DEFAULT_IMAGE_WIDTH = 80;
   public static final int CAPTCHA_DEFAULT_IMAGE_HEIGHT = 25;
   public static final int CAPTCHA_DEFAULT_WORD_LENGTH = 3;
   public static final String CAPTCHA_DEFAULT_IMAGE_FORMAT = "png";
   public static final String CAPTCHA_DEFAULT_DISPLAY_TEXT_INITIAL = "Please enter the letters as shown in the image below.";
   public static final String CAPTCHA_DEFAULT_DISPLAY_TEXT_TRY_AGAIN = "The letters you entered were incorrect. Please try again.";

   public CaptchaService() {
      this.localcache = new LinkedHashMap();
   }

   public CaptchaService(MemCachedClient memcache, boolean useLocalCache) {
      if (useLocalCache) {
         this.localcache = new LinkedHashMap();
      }

      this.memcache = memcache;
   }

   public String getCharacterSet() {
      return this.characterSet;
   }

   public void setCharacterSet(String characterSet) {
      this.characterSet = characterSet;
   }

   public long getCaptchaValidPeriod() {
      return this.captchaValidPeriod;
   }

   public void setCaptchaValidPeriod(long captchaValidPeriod) {
      this.captchaValidPeriod = captchaValidPeriod;
   }

   public CaptchaImageGenerator getImageGenerator() {
      return this.imageGenerator;
   }

   public Captcha nextCaptcha() {
      return this.nextCaptcha(80, 25, 3);
   }

   public Captcha nextCaptcha(int wordLength) {
      return this.nextCaptcha(80, 25, wordLength);
   }

   public Captcha nextCaptcha(int imageWidth, int imageHeight, int wordLength) {
      String answer = StringUtil.generateRandomWord(this.characterSet, wordLength);
      BufferedImage image = this.imageGenerator.newImage(imageWidth, imageHeight, answer);
      Captcha captcha = new Captcha(UUID.randomUUID().toString(), answer, image);
      Date expiry = new Date(System.currentTimeMillis() + this.captchaValidPeriod);
      if (this.localcache != null) {
         this.addToLocalCache(captcha.getId(), answer, expiry);
      }

      if (this.memcache != null) {
         this.memcache.set(captcha.getId(), answer, expiry);
      }

      return captcha;
   }

   public boolean validateResponse(String captchaId, String response) {
      if (captchaId != null && response != null) {
         String answer = null;
         if (this.localcache != null) {
            answer = this.getFromLocalCache(captchaId);
         }

         if (this.memcache != null) {
            if (answer == null) {
               answer = (String)this.memcache.get(captchaId);
            }

            this.memcache.delete(captchaId);
         }

         return response.equalsIgnoreCase(answer);
      } else {
         return false;
      }
   }

   private void addToLocalCache(String captchaId, String answer, Date expiry) {
      synchronized(this.localcache) {
         Iterator i = this.localcache.values().iterator();

         while(i.hasNext() && ((CaptchaService.CaptchaAnswer)i.next()).expiry < System.currentTimeMillis()) {
            i.remove();
         }

         this.localcache.put(captchaId, new CaptchaService.CaptchaAnswer(answer, expiry.getTime()));
      }
   }

   private String getFromLocalCache(String captchaId) {
      synchronized(this.localcache) {
         CaptchaService.CaptchaAnswer captchaAnswer = (CaptchaService.CaptchaAnswer)this.localcache.remove(captchaId);
         if (captchaAnswer == null) {
            return null;
         } else {
            return captchaAnswer.expiry < System.currentTimeMillis() ? null : captchaAnswer.answer;
         }
      }
   }

   public static void main(String[] args) {
      try {
         long start = System.currentTimeMillis();
         CaptchaService service = new CaptchaService();
         Captcha c = service.nextCaptcha(80, 25, 1);
         int i = 0;

         while(service.validateResponse(c.getId(), c.getAnswer())) {
            ++i;
            System.out.println(i);
         }

         System.out.println(System.currentTimeMillis() - start);
      } catch (Exception var6) {
         var6.printStackTrace();
      }

   }

   private static class CaptchaAnswer {
      private final String answer;
      private final long expiry;

      public CaptchaAnswer(String answer, long expiry) {
         this.answer = answer;
         this.expiry = expiry;
      }
   }
}
