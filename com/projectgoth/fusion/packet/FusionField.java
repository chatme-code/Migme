package com.projectgoth.fusion.packet;

import IceUtilInternal.Base64;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Vector;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

public class FusionField implements Serializable {
   private static final Logger log = Logger.getLogger(FusionField.class);
   private static final String CHAR_ENCODING = "UTF-8";
   public static final int FIELD_HEADER_LENGTH = 6;
   private static final byte BYTEARRAY = 1;
   private static final byte BYTE = 2;
   private static final byte INT = 3;
   private static final byte SHORT = 4;
   private static final byte LONG = 5;
   private static final byte CHAR = 6;
   private static final byte STRING = 7;
   private static final byte STRINGARRAY = 8;
   private static final byte JSONSTRING = 9;
   private static final byte NUMERICARRAY = 10;
   private byte dataType;
   private int length;
   private Object data;

   public FusionField(boolean val) {
      this.setByteVal((byte)(val ? 1 : 0));
   }

   public FusionField(byte val) {
      this.setByteVal(val);
   }

   public FusionField(int val) {
      this.setIntVal(val);
   }

   public FusionField(short val) {
      this.setShortVal(val);
   }

   public FusionField(long val) {
      this.setLongVal(val);
   }

   public FusionField(char val) {
      this.setCharVal(val);
   }

   public FusionField(String val) {
      this.setStringVal(val);
   }

   public static FusionField createJsonStringField(String val) {
      FusionField f = new FusionField(val);
      f.dataType = 9;
      return f;
   }

   public FusionField(byte[] val) {
      this.setByteArrayVal(val);
   }

   public static FusionField createByteEnumArrayField(ByteValueEnum[] val) {
      byte[] data = new byte[val.length];

      for(int i = 0; i < val.length; ++i) {
         data[i] = val[i].value();
      }

      return new FusionField(data);
   }

   public FusionField(String[] val) {
      this.setStringArrayVal(val);
   }

   public FusionField(long[] val) {
      this.setNumericArrayVal(val);
   }

   public int length() {
      return this.length;
   }

   public int totalLength() {
      return this.length() + 6;
   }

   public Byte getByteVal() {
      if (this.dataType == 2) {
         return (Byte)this.data;
      } else if (this.dataType == 1) {
         byte[] ba = (byte[])((byte[])this.data);
         return ba[0];
      } else if (this.dataType != 7 && this.dataType != 9) {
         return null;
      } else {
         try {
            return Byte.parseByte((String)this.data);
         } catch (NumberFormatException var2) {
            return null;
         }
      }
   }

   public void setByteVal(byte byteVal) {
      this.dataType = 2;
      this.length = 1;
      this.data = byteVal;
   }

   public Character getCharVal() {
      if (this.dataType == 6) {
         return (Character)this.data;
      } else if (this.dataType == 1) {
         try {
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream((byte[])((byte[])this.data)));
            return dis.readChar();
         } catch (IOException var2) {
            return null;
         }
      } else if (this.dataType != 7 && this.dataType != 9) {
         return null;
      } else {
         String s = (String)this.data;
         return s.length() == 0 ? null : s.charAt(0);
      }
   }

   public void setCharVal(char charVal) {
      this.dataType = 6;
      this.length = 2;
      this.data = charVal;
   }

   public Integer getIntVal() {
      if (this.dataType == 3) {
         return (Integer)this.data;
      } else if (this.dataType == 1) {
         try {
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream((byte[])((byte[])this.data)));
            return dis.readInt();
         } catch (IOException var2) {
            return null;
         }
      } else if (this.dataType != 7 && this.dataType != 9) {
         return null;
      } else {
         try {
            return Integer.parseInt((String)this.data);
         } catch (NumberFormatException var3) {
            return null;
         }
      }
   }

   public void setIntVal(int intVal) {
      this.dataType = 3;
      this.length = 4;
      this.data = intVal;
   }

   public Long getLongVal() {
      if (this.dataType == 5) {
         return (Long)this.data;
      } else if (this.dataType == 1) {
         try {
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream((byte[])((byte[])this.data)));
            return dis.readLong();
         } catch (IOException var2) {
            return null;
         }
      } else if (this.dataType != 7 && this.dataType != 9) {
         return null;
      } else {
         try {
            return Long.parseLong((String)this.data);
         } catch (NumberFormatException var3) {
            return null;
         }
      }
   }

   public void setLongVal(long longVal) {
      this.dataType = 5;
      this.length = 8;
      this.data = longVal;
   }

   public Short getShortVal() {
      if (this.dataType == 4) {
         return (Short)this.data;
      } else if (this.dataType == 1) {
         try {
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream((byte[])((byte[])this.data)));
            return dis.readShort();
         } catch (IOException var2) {
            return null;
         }
      } else if (this.dataType != 7 && this.dataType != 9) {
         return null;
      } else {
         try {
            return Short.parseShort((String)this.data);
         } catch (NumberFormatException var3) {
            return null;
         }
      }
   }

   public void setShortVal(short shortVal) {
      this.dataType = 4;
      this.length = 2;
      this.data = shortVal;
   }

   public String getStringVal() {
      try {
         switch(this.dataType) {
         case 1:
            try {
               return new String((byte[])((byte[])this.data), "UTF-8");
            } catch (Exception var2) {
               return new String((byte[])((byte[])this.data));
            }
         case 2:
            return String.valueOf(this.getByteVal());
         case 3:
            return String.valueOf(this.getIntVal());
         case 4:
            return String.valueOf(this.getShortVal());
         case 5:
            return String.valueOf(this.getLongVal());
         case 6:
            return String.valueOf(this.getCharVal());
         case 7:
         case 9:
            return (String)this.data;
         case 8:
         default:
            return null;
         }
      } catch (Exception var3) {
         return null;
      }
   }

   public void setStringVal(String stringVal) {
      try {
         this.dataType = 7;
         this.data = stringVal;
         this.length = stringVal.getBytes("UTF-8").length;
      } catch (Exception var3) {
         this.length = stringVal.getBytes().length;
      }

   }

   public boolean[] getBooleanArrayVal() {
      byte[] byteArray = this.getByteArrayVal();
      if (byteArray == null) {
         return null;
      } else {
         boolean[] booleanArray = new boolean[byteArray.length];

         for(int i = 0; i < byteArray.length; ++i) {
            booleanArray[i] = byteArray[i] != 0;
         }

         return booleanArray;
      }
   }

   public byte[] getByteArrayVal() {
      ByteBuffer buffer = ByteBuffer.allocate(this.length);

      try {
         switch(this.dataType) {
         case 1:
            buffer.put((byte[])((byte[])this.data));
            break;
         case 2:
            buffer.put(this.getByteVal());
            break;
         case 3:
            buffer.putInt(this.getIntVal());
            break;
         case 4:
            buffer.putShort(this.getShortVal());
            break;
         case 5:
            buffer.putLong(this.getLongVal());
            break;
         case 6:
            buffer.putChar(this.getCharVal());
            break;
         case 7:
            buffer.put(this.getStringVal().getBytes("UTF-8"));
            break;
         case 8:
            String[] stringArray = this.getStringArrayVal();

            for(int i = 0; i < stringArray.length; ++i) {
               try {
                  buffer.put(stringArray[i].getBytes("UTF-8"));
                  if (i < stringArray.length - 1) {
                     buffer.put((byte)0);
                  }
               } catch (Exception var9) {
                  return null;
               }
            }

            return buffer.array();
         case 9:
            return Base64.decode((String)this.data);
         case 10:
            long[] numericArray = (long[])((long[])this.data);
            long[] arr$ = numericArray;
            int len$ = numericArray.length;

            for(int i$ = 0; i$ < len$; ++i$) {
               long num = arr$[i$];
               buffer.putLong(num);
            }
         }
      } catch (IOException var10) {
         return null;
      }

      return buffer.array();
   }

   public void setByteArrayVal(byte[] byteArrayVal) {
      this.dataType = 1;
      byte[] ba = new byte[byteArrayVal.length];
      System.arraycopy(byteArrayVal, 0, ba, 0, byteArrayVal.length);
      this.length = ba.length;
      this.data = ba;
   }

   public long[] getNumericArrayVal() {
      if (this.dataType == 10) {
         return (long[])((long[])this.data);
      } else if (this.dataType != 1) {
         return null;
      } else {
         ByteBuffer buffer = ByteBuffer.wrap((byte[])((byte[])this.data));
         long[] numbers = new long[buffer.limit() / 8];

         for(int var3 = 0; buffer.hasRemaining() && buffer.remaining() % 8 == 0; numbers[var3++] = buffer.getLong()) {
         }

         if (buffer.remaining() != 0) {
            log.warn("Incomplete numeric array field received, remainder=" + buffer.remaining());
         }

         return numbers;
      }
   }

   public String[] getStringArrayVal() {
      if (this.dataType == 8) {
         return (String[])((String[])this.data);
      } else if (this.dataType == 1) {
         try {
            Vector v = new Vector();
            byte[] byteArray = (byte[])((byte[])this.data);
            int offset = 0;

            for(int length = 0; offset + length < byteArray.length; length = 0) {
               if (byteArray[offset] != 0) {
                  for(length = 0; offset + length <= byteArray.length; ++length) {
                     if (byteArray[offset + length] == 0 && length >= 1) {
                        v.add(new String(byteArray, offset, length, "UTF-8"));
                        break;
                     }

                     if (offset + length == byteArray.length - 1) {
                        v.add(new String(byteArray, offset, length + 1, "UTF-8"));
                        break;
                     }
                  }
               }

               offset = offset + length + 1;
            }

            return (String[])((String[])v.toArray(new String[v.size()]));
         } catch (IOException var5) {
            return null;
         }
      } else {
         return null;
      }
   }

   public void setStringArrayVal(String[] stringArrayVal) {
      this.dataType = 8;
      this.length = 0;

      for(int i = 0; i < stringArrayVal.length; ++i) {
         try {
            this.length += stringArrayVal[i].getBytes("UTF-8").length;
         } catch (Exception var4) {
            this.length += stringArrayVal[i].getBytes().length;
         }

         if (i < stringArrayVal.length - 1) {
            ++this.length;
         }
      }

      this.data = stringArrayVal;
   }

   public void setNumericArrayVal(long[] numericArrayVal) {
      this.dataType = 10;
      this.length += 8 * numericArrayVal.length;
      this.data = numericArrayVal;
   }

   public String toString() {
      StringBuffer b = new StringBuffer();
      switch(this.dataType) {
      case 1:
         try {
            byte[] byteArrayVal = this.getByteArrayVal();
            b.append("[ByteArray ").append(this.length()).append("] ").append(this.byteArrayToHexString(byteArrayVal)).append(", ").append(this.translate(byteArrayVal));
         } catch (Exception var5) {
            b.append("EXCEPTION: " + var5.getMessage());
         }
         break;
      case 2:
         b.append("[Byte ").append(this.length()).append("] ").append(this.getByteVal());
         break;
      case 3:
         b.append("[Int ").append(this.length()).append("] ").append(this.getIntVal());
         break;
      case 4:
         b.append("[Short ").append(this.length()).append("] ").append(this.getShortVal());
         break;
      case 5:
         b.append("[Long ").append(this.length()).append("] ").append(this.getLongVal());
         break;
      case 6:
         b.append("[Char ").append(this.length()).append("] ").append(this.getCharVal());
         break;
      case 7:
         b.append("[String ").append(this.length()).append("] ").append(this.getStringVal());
         break;
      case 8:
         String[] stringArrayVal = this.getStringArrayVal();
         b.append("[StringArray ").append(this.length()).append("] [");

         for(int i = 0; i < stringArrayVal.length; ++i) {
            b.append(stringArrayVal[i]);
            if (i < stringArrayVal.length - 1) {
               b.append(", ");
            }
         }

         b.append("]");
         break;
      case 9:
         b.append("[JsonString ").append(this.length()).append("] ").append(this.getStringVal());
         break;
      case 10:
         long[] numericArrayVal = this.getNumericArrayVal();
         b.append("[NumericArray ").append(this.length()).append("] [");

         for(int i = 0; i < numericArrayVal.length; ++i) {
            b.append(numericArrayVal[i]);
            if (i < numericArrayVal.length - 1) {
               b.append(", ");
            }
         }

         b.append("]");
      }

      return b.toString();
   }

   public boolean isNumeric() {
      return this.dataType == 2 || this.dataType == 4 || this.dataType == 3 || this.dataType == 5;
   }

   public byte getDataType() {
      return this.dataType;
   }

   public void setValueInJsonObject(JSONObject obj, String key) {
      try {
         JSONArray arr;
         int len$;
         int i$;
         switch(this.dataType) {
         case 1:
            obj.put(key, Base64.encode((byte[])((byte[])this.data)));
            return;
         case 2:
            obj.put(key, this.getByteVal());
            return;
         case 3:
            obj.put(key, this.getIntVal());
            return;
         case 4:
            obj.put(key, this.getShortVal());
            return;
         case 5:
            obj.put(key, this.getLongVal());
            return;
         case 6:
            char[] buf = new char[]{this.getCharVal()};
            obj.put(key, String.valueOf(buf));
            return;
         case 7:
         case 9:
            obj.put(key, (String)this.data);
            return;
         case 8:
            String[] stringArrayVal = this.getStringArrayVal();
            arr = new JSONArray();
            String[] arr$ = stringArrayVal;
            len$ = stringArrayVal.length;

            for(i$ = 0; i$ < len$; ++i$) {
               String s = arr$[i$];
               arr.put(s);
            }

            obj.put(key, arr);
            return;
         case 10:
            long[] numericArrayVal = this.getNumericArrayVal();
            arr = new JSONArray();
            long[] arr$ = numericArrayVal;
            len$ = numericArrayVal.length;

            for(i$ = 0; i$ < len$; ++i$) {
               long l = arr$[i$];
               arr.put(l);
            }

            obj.put(key, arr);
            return;
         default:
         }
      } catch (Exception var10) {
      }
   }

   private String translate(byte[] in) {
      StringBuffer b = new StringBuffer();

      String s;
      try {
         s = new String(in, "UTF-8");
      } catch (UnsupportedEncodingException var6) {
         s = new String(in);
      }

      for(int i = 0; i < s.length(); ++i) {
         char c = s.charAt(i);
         if (c < ' ' || c > '~') {
            if (b.length() > 0) {
               b.append("...");
            }
            break;
         }

         b.append(c);
      }

      if (b.length() > 0) {
         b.insert(0, "String=\"");
         b.append("\"");
      }

      if (in.length == 1) {
         if (b.length() > 0) {
            b.append(", ");
         }

         b.append("Byte=").append(in[0]);
      } else if (in.length == 2) {
         if (b.length() > 0) {
            b.append(", ");
         }

         b.append("Short=").append(ByteBuffer.wrap(in).getShort());
      } else if (in.length == 4) {
         if (b.length() > 0) {
            b.append(", ");
         }

         b.append("Int=").append(ByteBuffer.wrap(in).getInt());
      } else if (in.length == 8) {
         if (b.length() > 0) {
            b.append(", ");
         }

         b.append("Long=").append(ByteBuffer.wrap(in).getLong());
      }

      return b.toString();
   }

   private String byteArrayToHexString(byte[] in) {
      byte ch = false;
      int i = 0;
      if (in != null && in.length > 0) {
         String[] pseudo = new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};

         StringBuffer out;
         for(out = new StringBuffer(in.length * 2); i < in.length; ++i) {
            if (out.length() > 0) {
               out.append(' ');
            }

            byte ch = (byte)(in[i] & 240);
            ch = (byte)(ch >>> 4);
            ch = (byte)(ch & 15);
            out.append(pseudo[ch]);
            ch = (byte)(in[i] & 15);
            out.append(pseudo[ch]);
         }

         return new String(out);
      } else {
         return null;
      }
   }
}
