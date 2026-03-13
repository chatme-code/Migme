package com.projectgoth.fusion.imageserver;

import com.projectgoth.fusion.common.ImageInfo;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Semaphore;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.MemoryCacheImageOutputStream;

public class ImageItem {
   private static Semaphore imageIOSemaphore = new Semaphore(1);
   private String format;
   private byte[] bytes;

   private BufferedImage readFromImageIO(InputStream input) throws IOException {
      BufferedImage var2;
      try {
         imageIOSemaphore.acquireUninterruptibly();
         var2 = ImageIO.read(input);
      } finally {
         imageIOSemaphore.release();
      }

      return var2;
   }

   private ImageInfo getImageInfo(byte[] rawImage) throws IOException {
      ImageInfo info = new ImageInfo();
      info.setInput((InputStream)(new ByteArrayInputStream(rawImage)));
      if (!info.check()) {
         throw new IOException("Invalid image input");
      } else {
         return info;
      }
   }

   private BufferedImage copy(BufferedImage image, boolean alpha) {
      BufferedImage i = new BufferedImage(image.getWidth(), image.getHeight(), alpha ? 2 : 1);
      Graphics2D g = i.createGraphics();
      g.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), (ImageObserver)null);
      g.dispose();
      return i;
   }

   private BufferedImage scale(BufferedImage image, int width, int height, boolean alpha) {
      BufferedImage scaled = new BufferedImage(width, height, alpha ? 2 : 1);
      Graphics2D g = scaled.createGraphics();
      g.drawImage(image.getScaledInstance(width, height, 4), 0, 0, width, height, (ImageObserver)null);
      g.dispose();
      return scaled;
   }

   private BufferedImage cropAndScale(BufferedImage image, int width, int height, boolean alpha) {
      int imageWidth = image.getWidth();
      int imageHeight = image.getHeight();
      double widthRatio = (double)imageWidth / (double)width;
      double heightRatio = (double)imageHeight / (double)height;
      BufferedImage cropped;
      int cropHeight;
      int cropY;
      if (widthRatio <= heightRatio) {
         cropHeight = (int)((double)height * widthRatio);
         cropY = (imageHeight - cropHeight) / 2;
         cropped = image.getSubimage(0, cropY, imageWidth, cropHeight);
      } else {
         cropHeight = (int)((double)width * heightRatio);
         cropY = (imageWidth - cropHeight) / 2;
         cropped = image.getSubimage(cropY, 0, cropHeight, imageHeight);
      }

      return this.scale(cropped, width, height, alpha);
   }

   private byte[] encode(BufferedImage image, String format, float compressionQuality) throws IOException {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      if ("jpeg".equals(format) && compressionQuality > 0.0F && compressionQuality <= 1.0F) {
         Iterator i = ImageIO.getImageWritersByFormatName(format);
         ImageWriter writer = (ImageWriter)i.next();
         ImageWriteParam iwp = writer.getDefaultWriteParam();
         iwp.setCompressionMode(2);
         iwp.setCompressionQuality(compressionQuality);
         MemoryCacheImageOutputStream output = new MemoryCacheImageOutputStream(out);
         writer.setOutput(output);
         IIOImage iio = new IIOImage(image, (List)null, (IIOMetadata)null);
         writer.write((IIOMetadata)null, iio, iwp);
      } else {
         ImageIO.write(image, this.format, out);
      }

      byte[] ba = out.toByteArray();
      out.close();
      return ba;
   }

   private void load(byte[] rawImage, String format, int width, int height, boolean keepAspectRatio, boolean crop, float compressionQualtiy) throws IOException {
      ImageInfo info = this.getImageInfo(rawImage);
      if (format == null) {
         if (info.getFormat() == 2) {
            this.format = info.getFormatName().toLowerCase();
         } else {
            this.format = "jpeg";
         }
      } else {
         this.format = format;
      }

      boolean convertFormat = !this.format.equalsIgnoreCase(info.getFormatName());
      boolean scaleImage = width > 0 && height > 0 && (width != info.getWidth() || height != info.getHeight());
      boolean alpha = !convertFormat && info.getFormat() == 2;
      if (scaleImage) {
         if (keepAspectRatio) {
            if (crop) {
               this.bytes = this.encode(this.cropAndScale(this.readFromImageIO(new ByteArrayInputStream(rawImage)), width, height, alpha), this.format, compressionQualtiy);
            } else {
               double originalRatio = (double)info.getWidth() / (double)info.getHeight();
               double newRatio = (double)width / (double)height;
               if (originalRatio > newRatio) {
                  height = (int)((double)width / originalRatio);
               } else if (originalRatio < newRatio) {
                  width = (int)((double)height * originalRatio);
               }

               this.bytes = this.encode(this.scale(this.readFromImageIO(new ByteArrayInputStream(rawImage)), width, height, alpha), this.format, compressionQualtiy);
            }
         } else {
            this.bytes = this.encode(this.scale(this.readFromImageIO(new ByteArrayInputStream(rawImage)), width, height, alpha), this.format, compressionQualtiy);
         }
      } else if (convertFormat) {
         this.bytes = this.encode(this.copy(this.readFromImageIO(new ByteArrayInputStream(rawImage)), alpha), this.format, compressionQualtiy);
      } else {
         this.bytes = rawImage;
      }

   }

   public ImageItem(byte[] rawImage) throws IOException {
      this.load(rawImage, (String)null, -1, -1, false, false, 0.0F);
   }

   public ImageItem(byte[] rawImage, String format) throws IOException {
      this.load(rawImage, format, -1, -1, false, false, 0.0F);
   }

   public ImageItem(byte[] rawImage, String format, int width, int height, boolean keepAspectRatio, boolean crop, float compressionQuality) throws IOException {
      this.load(rawImage, format, width, height, keepAspectRatio, crop, compressionQuality);
   }

   public String getFormat() {
      return this.format;
   }

   public byte[] getBytes() {
      return this.bytes;
   }

   public int size() {
      return this.bytes.length;
   }

   public static void setImageIOSemaphore(Semaphore imageIOSemaphore) {
      ImageItem.imageIOSemaphore = imageIOSemaphore;
   }
}
