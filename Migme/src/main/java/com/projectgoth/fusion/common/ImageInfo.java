package com.projectgoth.fusion.common;

import java.io.DataInput;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Vector;

public class ImageInfo {
   public static final int FORMAT_JPEG = 0;
   public static final int FORMAT_GIF = 1;
   public static final int FORMAT_PNG = 2;
   public static final int FORMAT_BMP = 3;
   public static final int FORMAT_PCX = 4;
   public static final int FORMAT_IFF = 5;
   public static final int FORMAT_RAS = 6;
   public static final int FORMAT_PBM = 7;
   public static final int FORMAT_PGM = 8;
   public static final int FORMAT_PPM = 9;
   public static final int FORMAT_PSD = 10;
   private static final String[] FORMAT_NAMES = new String[]{"JPEG", "GIF", "PNG", "BMP", "PCX", "IFF", "RAS", "PBM", "PGM", "PPM", "PSD"};
   private static final String[] MIME_TYPE_STRINGS = new String[]{"image/jpeg", "image/gif", "image/png", "image/bmp", "image/pcx", "image/iff", "image/ras", "image/x-portable-bitmap", "image/x-portable-graymap", "image/x-portable-pixmap", "image/psd"};
   private int width;
   private int height;
   private int bitsPerPixel;
   private boolean progressive;
   private int format;
   private InputStream in;
   private DataInput din;
   private boolean collectComments = true;
   private Vector comments;
   private boolean determineNumberOfImages;
   private int numberOfImages;
   private int physicalHeightDpi;
   private int physicalWidthDpi;

   private void addComment(String s) {
      if (this.comments == null) {
         this.comments = new Vector();
      }

      this.comments.addElement(s);
   }

   public boolean check() {
      this.format = -1;
      this.width = -1;
      this.height = -1;
      this.bitsPerPixel = -1;
      this.numberOfImages = 1;
      this.physicalHeightDpi = -1;
      this.physicalWidthDpi = -1;
      this.comments = null;

      try {
         int b1 = this.read() & 255;
         int b2 = this.read() & 255;
         if (b1 == 71 && b2 == 73) {
            return this.checkGif();
         } else if (b1 == 137 && b2 == 80) {
            return this.checkPng();
         } else if (b1 == 255 && b2 == 216) {
            return this.checkJpeg();
         } else if (b1 == 66 && b2 == 77) {
            return this.checkBmp();
         } else if (b1 == 10 && b2 < 6) {
            return this.checkPcx();
         } else if (b1 == 70 && b2 == 79) {
            return this.checkIff();
         } else if (b1 == 89 && b2 == 166) {
            return this.checkRas();
         } else if (b1 == 80 && b2 >= 49 && b2 <= 54) {
            return this.checkPnm(b2 - 48);
         } else {
            return b1 == 56 && b2 == 66 ? this.checkPsd() : false;
         }
      } catch (IOException var3) {
         return false;
      }
   }

   private boolean checkBmp() throws IOException {
      byte[] a = new byte[44];
      if (this.read(a) != a.length) {
         return false;
      } else {
         this.width = getIntLittleEndian(a, 16);
         this.height = getIntLittleEndian(a, 20);
         if (this.width >= 1 && this.height >= 1) {
            this.bitsPerPixel = getShortLittleEndian(a, 26);
            if (this.bitsPerPixel != 1 && this.bitsPerPixel != 4 && this.bitsPerPixel != 8 && this.bitsPerPixel != 16 && this.bitsPerPixel != 24 && this.bitsPerPixel != 32) {
               return false;
            } else {
               int x = (int)((double)getIntLittleEndian(a, 36) * 0.0254D);
               if (x > 0) {
                  this.setPhysicalWidthDpi(x);
               }

               int y = (int)((double)getIntLittleEndian(a, 40) * 0.0254D);
               if (y > 0) {
                  this.setPhysicalHeightDpi(y);
               }

               this.format = 3;
               return true;
            }
         } else {
            return false;
         }
      }
   }

   private boolean checkGif() throws IOException {
      byte[] GIF_MAGIC_87A = new byte[]{70, 56, 55, 97};
      byte[] GIF_MAGIC_89A = new byte[]{70, 56, 57, 97};
      byte[] a = new byte[11];
      if (this.read(a) != 11) {
         return false;
      } else if (!equals(a, 0, GIF_MAGIC_89A, 0, 4) && !equals(a, 0, GIF_MAGIC_87A, 0, 4)) {
         return false;
      } else {
         this.format = 1;
         this.width = getShortLittleEndian(a, 4);
         this.height = getShortLittleEndian(a, 6);
         int flags = a[8] & 255;
         this.bitsPerPixel = (flags >> 4 & 7) + 1;
         if (!this.determineNumberOfImages) {
            return true;
         } else {
            int blockType;
            if ((flags & 128) != 0) {
               blockType = (1 << (flags & 7) + 1) * 3;
               this.skip(blockType);
            }

            this.numberOfImages = 0;

            label113:
            do {
               blockType = this.read();
               int localBitsPerPixel;
               int n;
               switch(blockType) {
               case 33:
                  localBitsPerPixel = this.read();
                  if (this.collectComments && localBitsPerPixel == 254) {
                     StringBuffer sb = new StringBuffer();

                     while(true) {
                        int n = this.read();
                        if (n == -1) {
                           return false;
                        }

                        if (n > 0) {
                           for(int i = 0; i < n; ++i) {
                              int ch = this.read();
                              if (ch == -1) {
                                 return false;
                              }

                              sb.append((char)ch);
                           }
                        }

                        if (n <= 0) {
                           continue label113;
                        }
                     }
                  } else {
                     while(true) {
                        n = this.read();
                        if (n > 0) {
                           this.skip(n);
                        } else if (n == -1) {
                           return false;
                        }

                        if (n <= 0) {
                           continue label113;
                        }
                     }
                  }
               case 44:
                  if (this.read(a, 0, 9) != 9) {
                     return false;
                  }

                  flags = a[8] & 255;
                  this.progressive = (flags & 64) != 0;
                  localBitsPerPixel = (flags & 7) + 1;
                  if (localBitsPerPixel > this.bitsPerPixel) {
                     this.bitsPerPixel = localBitsPerPixel;
                  }

                  if ((flags & 128) != 0) {
                     this.skip((1 << localBitsPerPixel) * 3);
                  }

                  this.skip(1);

                  do {
                     n = this.read();
                     if (n > 0) {
                        this.skip(n);
                     } else if (n == -1) {
                        return false;
                     }
                  } while(n > 0);

                  ++this.numberOfImages;
               case 59:
                  break;
               default:
                  return false;
               }
            } while(blockType != 59);

            return true;
         }
      }
   }

   private boolean checkIff() throws IOException {
      byte[] a = new byte[10];
      if (this.read(a, 0, 10) != 10) {
         return false;
      } else {
         byte[] IFF_RM = new byte[]{82, 77};
         if (!equals(a, 0, IFF_RM, 0, 2)) {
            return false;
         } else {
            int type = getIntBigEndian(a, 6);
            if (type != 1229734477 && type != 1346522400) {
               return false;
            } else {
               while(this.read(a, 0, 8) == 8) {
                  int chunkId = getIntBigEndian(a, 0);
                  int size = getIntBigEndian(a, 4);
                  if ((size & 1) == 1) {
                     ++size;
                  }

                  if (chunkId == 1112361028) {
                     if (this.read(a, 0, 9) != 9) {
                        return false;
                     }

                     this.format = 5;
                     this.width = getShortBigEndian(a, 0);
                     this.height = getShortBigEndian(a, 2);
                     this.bitsPerPixel = a[8] & 255;
                     return this.width > 0 && this.height > 0 && this.bitsPerPixel > 0 && this.bitsPerPixel < 33;
                  }

                  this.skip(size);
               }

               return false;
            }
         }
      }
   }

   private boolean checkJpeg() throws IOException {
      byte[] data = new byte[12];

      while(this.read(data, 0, 4) == 4) {
         int marker = getShortBigEndian(data, 0);
         int size = getShortBigEndian(data, 2);
         if ((marker & '\uff00') != 65280) {
            return false;
         }

         byte[] APP0_ID;
         if (marker == 65504) {
            if (size < 14) {
               this.skip(size - 2);
            } else {
               if (this.read(data, 0, 12) != 12) {
                  return false;
               }

               APP0_ID = new byte[]{74, 70, 73, 70, 0};
               if (equals(APP0_ID, 0, data, 0, 5)) {
                  if (data[7] == 1) {
                     this.setPhysicalWidthDpi(getShortBigEndian(data, 8));
                     this.setPhysicalHeightDpi(getShortBigEndian(data, 10));
                  } else if (data[7] == 2) {
                     int x = getShortBigEndian(data, 8);
                     int y = getShortBigEndian(data, 10);
                     this.setPhysicalWidthDpi((int)((float)x * 2.54F));
                     this.setPhysicalHeightDpi((int)((float)y * 2.54F));
                  }
               }

               this.skip(size - 14);
            }
         } else if (this.collectComments && size > 2 && marker == 65534) {
            size -= 2;
            APP0_ID = new byte[size];
            if (this.read(APP0_ID, 0, size) != size) {
               return false;
            }

            String comment = new String(APP0_ID, "iso-8859-1");
            comment = comment.trim();
            this.addComment(comment);
         } else {
            if (marker >= 65472 && marker <= 65487 && marker != 65476 && marker != 65480) {
               if (this.read(data, 0, 6) != 6) {
                  return false;
               }

               this.format = 0;
               this.bitsPerPixel = (data[0] & 255) * (data[5] & 255);
               this.progressive = marker == 65474 || marker == 65478 || marker == 65482 || marker == 65486;
               this.width = getShortBigEndian(data, 3);
               this.height = getShortBigEndian(data, 1);
               return true;
            }

            this.skip(size - 2);
         }
      }

      return false;
   }

   private boolean checkPcx() throws IOException {
      byte[] a = new byte[64];
      if (this.read(a) != a.length) {
         return false;
      } else if (a[0] != 1) {
         return false;
      } else {
         int x1 = getShortLittleEndian(a, 2);
         int y1 = getShortLittleEndian(a, 4);
         int x2 = getShortLittleEndian(a, 6);
         int y2 = getShortLittleEndian(a, 8);
         if (x1 >= 0 && x2 >= x1 && y1 >= 0 && y2 >= y1) {
            this.width = x2 - x1 + 1;
            this.height = y2 - y1 + 1;
            int bits = a[1];
            int planes = a[63];
            if (planes == 1 && (bits == 1 || bits == 2 || bits == 4 || bits == 8)) {
               this.bitsPerPixel = bits;
            } else {
               if (planes != 3 || bits != 8) {
                  return false;
               }

               this.bitsPerPixel = 24;
            }

            this.setPhysicalWidthDpi(getShortLittleEndian(a, 10));
            this.setPhysicalHeightDpi(getShortLittleEndian(a, 10));
            this.format = 4;
            return true;
         } else {
            return false;
         }
      }
   }

   private boolean checkPng() throws IOException {
      byte[] PNG_MAGIC = new byte[]{78, 71, 13, 10, 26, 10};
      byte[] a = new byte[27];
      if (this.read(a) != 27) {
         return false;
      } else if (!equals(a, 0, PNG_MAGIC, 0, 6)) {
         return false;
      } else {
         this.format = 2;
         this.width = getIntBigEndian(a, 14);
         this.height = getIntBigEndian(a, 18);
         this.bitsPerPixel = a[22] & 255;
         int colorType = a[23] & 255;
         if (colorType == 2 || colorType == 6) {
            this.bitsPerPixel *= 3;
         }

         this.progressive = (a[26] & 255) != 0;
         return true;
      }
   }

   private boolean checkPnm(int id) throws IOException {
      if (id >= 1 && id <= 6) {
         int[] PNM_FORMATS = new int[]{7, 8, 9};
         this.format = PNM_FORMATS[(id - 1) % 3];
         boolean hasPixelResolution = false;

         while(true) {
            while(true) {
               String s;
               do {
                  do {
                     s = this.readLine();
                     if (s != null) {
                        s = s.trim();
                     }
                  } while(s == null);
               } while(s.length() < 1);

               if (s.charAt(0) != '#') {
                  int maxSample;
                  if (hasPixelResolution) {
                     try {
                        maxSample = Integer.parseInt(s);
                     } catch (NumberFormatException var9) {
                        return false;
                     }

                     if (maxSample < 0) {
                        return false;
                     }

                     for(int i = 0; i < 25; ++i) {
                        if (maxSample < 1 << i + 1) {
                           this.bitsPerPixel = i + 1;
                           if (this.format == 9) {
                              this.bitsPerPixel *= 3;
                           }

                           return true;
                        }
                     }

                     return false;
                  }

                  maxSample = s.indexOf(32);
                  if (maxSample == -1) {
                     return false;
                  }

                  String widthString = s.substring(0, maxSample);
                  maxSample = s.lastIndexOf(32);
                  if (maxSample == -1) {
                     return false;
                  }

                  String heightString = s.substring(maxSample + 1);

                  try {
                     this.width = Integer.parseInt(widthString);
                     this.height = Integer.parseInt(heightString);
                  } catch (NumberFormatException var10) {
                     return false;
                  }

                  if (this.width < 1 || this.height < 1) {
                     return false;
                  }

                  if (this.format == 7) {
                     this.bitsPerPixel = 1;
                     return true;
                  }

                  hasPixelResolution = true;
               } else if (this.collectComments && s.length() > 1) {
                  this.addComment(s.substring(1));
               }
            }
         }
      } else {
         return false;
      }
   }

   private boolean checkPsd() throws IOException {
      byte[] a = new byte[24];
      if (this.read(a) != a.length) {
         return false;
      } else {
         byte[] PSD_MAGIC = new byte[]{80, 83};
         if (!equals(a, 0, PSD_MAGIC, 0, 2)) {
            return false;
         } else {
            this.format = 10;
            this.width = getIntBigEndian(a, 16);
            this.height = getIntBigEndian(a, 12);
            int channels = getShortBigEndian(a, 10);
            int depth = getShortBigEndian(a, 20);
            this.bitsPerPixel = channels * depth;
            return this.width > 0 && this.height > 0 && this.bitsPerPixel > 0 && this.bitsPerPixel <= 64;
         }
      }
   }

   private boolean checkRas() throws IOException {
      byte[] a = new byte[14];
      if (this.read(a) != a.length) {
         return false;
      } else {
         byte[] RAS_MAGIC = new byte[]{106, -107};
         if (!equals(a, 0, RAS_MAGIC, 0, 2)) {
            return false;
         } else {
            this.format = 6;
            this.width = getIntBigEndian(a, 2);
            this.height = getIntBigEndian(a, 6);
            this.bitsPerPixel = getIntBigEndian(a, 10);
            return this.width > 0 && this.height > 0 && this.bitsPerPixel > 0 && this.bitsPerPixel <= 24;
         }
      }
   }

   private static boolean determineVerbosity(String[] args) {
      if (args != null && args.length > 0) {
         for(int i = 0; i < args.length; ++i) {
            if ("-c".equals(args[i])) {
               return false;
            }
         }
      }

      return true;
   }

   private static boolean equals(byte[] a1, int offs1, byte[] a2, int offs2, int num) {
      while(true) {
         if (num-- > 0) {
            if (a1[offs1++] == a2[offs2++]) {
               continue;
            }

            return false;
         }

         return true;
      }
   }

   public int getBitsPerPixel() {
      return this.bitsPerPixel;
   }

   public String getComment(int index) {
      if (this.comments != null && index >= 0 && index < this.comments.size()) {
         return (String)this.comments.elementAt(index);
      } else {
         throw new IllegalArgumentException("Not a valid comment index: " + index);
      }
   }

   public int getFormat() {
      return this.format;
   }

   public String getFormatName() {
      return this.format >= 0 && this.format < FORMAT_NAMES.length ? FORMAT_NAMES[this.format] : "?";
   }

   public int getHeight() {
      return this.height;
   }

   private static int getIntBigEndian(byte[] a, int offs) {
      return (a[offs] & 255) << 24 | (a[offs + 1] & 255) << 16 | (a[offs + 2] & 255) << 8 | a[offs + 3] & 255;
   }

   private static int getIntLittleEndian(byte[] a, int offs) {
      return (a[offs + 3] & 255) << 24 | (a[offs + 2] & 255) << 16 | (a[offs + 1] & 255) << 8 | a[offs] & 255;
   }

   public String getMimeType() {
      if (this.format >= 0 && this.format < MIME_TYPE_STRINGS.length) {
         return this.format == 0 && this.progressive ? "image/pjpeg" : MIME_TYPE_STRINGS[this.format];
      } else {
         return null;
      }
   }

   public int getNumberOfComments() {
      return this.comments == null ? 0 : this.comments.size();
   }

   public int getNumberOfImages() {
      return this.numberOfImages;
   }

   public int getPhysicalHeightDpi() {
      return this.physicalHeightDpi;
   }

   public float getPhysicalHeightInch() {
      int h = this.getHeight();
      int ph = this.getPhysicalHeightDpi();
      return h > 0 && ph > 0 ? (float)h / (float)ph : -1.0F;
   }

   public int getPhysicalWidthDpi() {
      return this.physicalWidthDpi;
   }

   public float getPhysicalWidthInch() {
      int w = this.getWidth();
      int pw = this.getPhysicalWidthDpi();
      return w > 0 && pw > 0 ? (float)w / (float)pw : -1.0F;
   }

   private static int getShortBigEndian(byte[] a, int offs) {
      return (a[offs] & 255) << 8 | a[offs + 1] & 255;
   }

   private static int getShortLittleEndian(byte[] a, int offs) {
      return a[offs] & 255 | (a[offs + 1] & 255) << 8;
   }

   public int getWidth() {
      return this.width;
   }

   public boolean isProgressive() {
      return this.progressive;
   }

   public static void main(String[] args) {
      ImageInfo imageInfo = new ImageInfo();
      imageInfo.setDetermineImageNumber(true);
      boolean verbose = determineVerbosity(args);
      if (args.length == 0) {
         run((String)null, System.in, imageInfo, verbose);
      } else {
         int index = 0;

         while(index < args.length) {
            Object in = null;

            try {
               String name = args[index++];
               System.out.print(name + ";");
               if (name.startsWith("http://")) {
                  in = (new URL(name)).openConnection().getInputStream();
               } else {
                  in = new FileInputStream(name);
               }

               run(name, (InputStream)in, imageInfo, verbose);
               ((InputStream)in).close();
            } catch (IOException var8) {
               System.out.println(var8);

               try {
                  if (in != null) {
                     ((InputStream)in).close();
                  }
               } catch (IOException var7) {
               }
            }
         }
      }

   }

   private static void print(String sourceName, ImageInfo ii, boolean verbose) {
      if (verbose) {
         printVerbose(sourceName, ii);
      } else {
         printCompact(sourceName, ii);
      }

   }

   private static void printCompact(String sourceName, ImageInfo imageInfo) {
      String SEP = "\t";
      System.out.println(sourceName + "\t" + imageInfo.getFormatName() + "\t" + imageInfo.getMimeType() + "\t" + imageInfo.getWidth() + "\t" + imageInfo.getHeight() + "\t" + imageInfo.getBitsPerPixel() + "\t" + imageInfo.getNumberOfImages() + "\t" + imageInfo.getPhysicalWidthDpi() + "\t" + imageInfo.getPhysicalHeightDpi() + "\t" + imageInfo.getPhysicalWidthInch() + "\t" + imageInfo.getPhysicalHeightInch() + "\t" + imageInfo.isProgressive());
   }

   private static void printLine(int indentLevels, String text, float value, float minValidValue) {
      if (!(value < minValidValue)) {
         printLine(indentLevels, text, Float.toString(value));
      }
   }

   private static void printLine(int indentLevels, String text, int value, int minValidValue) {
      if (value >= minValidValue) {
         printLine(indentLevels, text, Integer.toString(value));
      }

   }

   private static void printLine(int indentLevels, String text, String value) {
      if (value != null && value.length() != 0) {
         while(indentLevels-- > 0) {
            System.out.print("\t");
         }

         if (text != null && text.length() > 0) {
            System.out.print(text);
            System.out.print(" ");
         }

         System.out.println(value);
      }
   }

   private static void printVerbose(String sourceName, ImageInfo ii) {
      printLine(0, (String)null, sourceName);
      printLine(1, "File format: ", ii.getFormatName());
      printLine(1, "MIME type: ", ii.getMimeType());
      printLine(1, "Width (pixels): ", ii.getWidth(), 1);
      printLine(1, "Height (pixels): ", ii.getHeight(), 1);
      printLine(1, "Bits per pixel: ", ii.getBitsPerPixel(), 1);
      printLine(1, "Progressive: ", ii.isProgressive() ? "yes" : "no");
      printLine(1, "Number of images: ", ii.getNumberOfImages(), 1);
      printLine(1, "Physical width (dpi): ", ii.getPhysicalWidthDpi(), 1);
      printLine(1, "Physical height (dpi): ", ii.getPhysicalHeightDpi(), 1);
      printLine(1, "Physical width (inches): ", ii.getPhysicalWidthInch(), 1.0F);
      printLine(1, "Physical height (inches): ", ii.getPhysicalHeightInch(), 1.0F);
      int numComments = ii.getNumberOfComments();
      printLine(1, "Number of textual comments: ", numComments, 1);
      if (numComments > 0) {
         for(int i = 0; i < numComments; ++i) {
            printLine(2, (String)null, ii.getComment(i));
         }
      }

   }

   private int read() throws IOException {
      return this.in != null ? this.in.read() : this.din.readByte();
   }

   private int read(byte[] a) throws IOException {
      if (this.in != null) {
         return this.in.read(a);
      } else {
         this.din.readFully(a);
         return a.length;
      }
   }

   private int read(byte[] a, int offset, int num) throws IOException {
      if (this.in != null) {
         return this.in.read(a, offset, num);
      } else {
         this.din.readFully(a, offset, num);
         return num;
      }
   }

   private String readLine() throws IOException {
      return this.readLine(new StringBuffer());
   }

   private String readLine(StringBuffer sb) throws IOException {
      boolean finished;
      do {
         int value = this.read();
         finished = value == -1 || value == 10;
         if (!finished) {
            sb.append((char)value);
         }
      } while(!finished);

      return sb.toString();
   }

   private static void run(String sourceName, InputStream in, ImageInfo imageInfo, boolean verbose) {
      imageInfo.setInput(in);
      imageInfo.setDetermineImageNumber(true);
      imageInfo.setCollectComments(verbose);
      if (imageInfo.check()) {
         print(sourceName, imageInfo, verbose);
      }

   }

   public void setCollectComments(boolean newValue) {
      this.collectComments = newValue;
   }

   public void setDetermineImageNumber(boolean newValue) {
      this.determineNumberOfImages = newValue;
   }

   public void setInput(DataInput dataInput) {
      this.din = dataInput;
      this.in = null;
   }

   public void setInput(InputStream inputStream) {
      this.in = inputStream;
      this.din = null;
   }

   private void setPhysicalHeightDpi(int newValue) {
      this.physicalWidthDpi = newValue;
   }

   private void setPhysicalWidthDpi(int newValue) {
      this.physicalHeightDpi = newValue;
   }

   private void skip(int num) throws IOException {
      while(num > 0) {
         long result;
         if (this.in != null) {
            result = this.in.skip((long)num);
         } else {
            result = (long)this.din.skipBytes(num);
         }

         if (result > 0L) {
            num = (int)((long)num - result);
         } else {
            if (this.in != null) {
               result = (long)this.in.read();
            } else {
               result = (long)this.din.readByte();
            }

            if (result == -1L) {
               throw new IOException("Premature end of input.");
            }

            --num;
         }
      }

   }
}
