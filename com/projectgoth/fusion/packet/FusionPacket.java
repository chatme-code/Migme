package com.projectgoth.fusion.packet;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.gateway.packet.FusionPacketFactory;
import com.projectgoth.fusion.slice.FusionException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class FusionPacket implements Serializable {
   private static final int PACKET_HEADER_LENGTH = 9;
   private static final int PACKET_TYPE_POS = 1;
   private static final int PACKET_CONTENT_LENGTH_POS = 5;
   public static final short ERROR = 0;
   public static final short OK = 1;
   public static final short PING = 2;
   public static final short PING_REPLY = 3;
   public static final short ALERT = 5;
   public static final short HELP_TEXT = 6;
   public static final short GET_HELP_TEXT = 7;
   public static final short SERVER_QUESTION = 8;
   public static final short SERVER_QUESTION_REPLY = 9;
   public static final short REPORT = 10;
   public static final short MIDLET_PROPERTY = 11;
   public static final short GET_MIDLET_PROPERTY = 12;
   public static final short HTTP_POLL = 13;
   public static final short NOTIFICATION = 14;
   public static final short RESERVED = 15;
   public static final short CAPTCHA = 16;
   public static final short CAPTCHA_RESPONSE = 17;
   public static final short GET_SERVER_QUESTION = 18;
   public static final short NEW_REGISTRATION = 100;
   public static final short REGISTRATION = 101;
   public static final short REGISTRATION_CHALLENGE = 102;
   public static final short REGISTRATION_RESPONSE = 103;
   public static final short REGISTRATION_ERROR = 104;
   public static final short LOGIN = 200;
   public static final short LOGIN_CHALLENGE = 201;
   public static final short LOGIN_RESPONSE = 202;
   public static final short LOGIN_OK = 203;
   public static final short ACTIVATE_ACCOUNT = 204;
   public static final short RESEND_VERIFICATION_CODE = 205;
   public static final short IM_LOGIN = 206;
   public static final short IM_SESSION_STATUS = 207;
   public static final short IM_AVAILABLE = 208;
   public static final short SLIM_LOGIN = 209;
   public static final short SLIM_LOGIN_OK = 210;
   public static final short CREATE_SESSION = 211;
   public static final short SLIM_LOGIN_CHALLENGE = 212;
   public static final short LOGOUT = 300;
   public static final short SESSION_TERMINATED = 301;
   public static final short IM_LOGOUT = 302;
   public static final short GET_CONTACTS = 400;
   public static final short GROUP = 401;
   public static final short CONTACT = 402;
   public static final short GET_CONTACTS_COMPLETE = 403;
   public static final short PRESENCE = 404;
   public static final short ADD_CONTACT = 405;
   public static final short REMOVE_CONTACT = 406;
   public static final short UPDATE_CONTACT = 407;
   public static final short ADD_GROUP = 408;
   public static final short REMOVE_GROUP = 409;
   public static final short UPDATE_GROUP = 410;
   public static final short MOVE_CONTACT = 411;
   public static final short CONTACT_REQUEST = 412;
   public static final short ACCEPT_CONTACT_REQUEST = 413;
   public static final short REJECT_CONTACT_REQUEST = 414;
   public static final short GET_PERMISSION_LIST = 415;
   public static final short PERMISSION_LIST = 416;
   public static final short SET_PERMISSION = 417;
   public static final short VOICE_CAPABILITY = 418;
   public static final short HAVE_LATEST_CONTACT_LIST = 419;
   public static final short CONTACT_LIST_VERSION = 420;
   public static final short STATUS_MESSAGE = 421;
   public static final short USER_EVENT = 422;
   public static final short DISPLAY_PICTURE = 423;
   public static final short GET_USER_INIT_DETAILS = 424;
   public static final short GET_CONTACT_REQUESTS = 425;
   public static final short MESSAGE = 500;
   public static final short WAP_PUSH = 501;
   public static final short FILE_RECEIVED = 502;
   public static final short MAIL_INFO = 503;
   public static final short UPLOAD_FILE = 504;
   public static final short MESSAGE_STATUS_EVENT = 505;
   public static final short MESSAGE_STATUS_EVENTS = 506;
   public static final short LEAVE_PRIVATE_CHAT = 507;
   public static final short GET_MESSAGES = 550;
   public static final short GET_CHATS = 551;
   public static final short HAVE_LATEST_CHAT_LIST = 552;
   public static final short CHAT = 560;
   public static final short CHAT_LIST_VERSION = 561;
   public static final short END_MESSAGES = 562;
   public static final short LATEST_MESSAGES_DIGEST = 563;
   public static final short SET_CHAT_NAME = 564;
   public static final short GET_MESSAGE_STATUS_EVENTS = 565;
   public static final short SET_PRESENCE = 600;
   public static final short SET_STATUS_MESSAGE = 601;
   public static final short SET_DISPLAY_PICTURE = 602;
   public static final short AVATAR = 603;
   public static final short DEVICE_MODE = 604;
   public static final short GET_CHATROOMS = 700;
   public static final short CHATROOM = 701;
   public static final short GET_CHATROOMS_COMPLETE = 702;
   public static final short JOIN_CHATROOM = 703;
   public static final short LEAVE_CHATROOM = 704;
   public static final short CREATE_CHATROOM = 705;
   public static final short KICK_CHATROOM_PARTICIPANT = 706;
   public static final short GET_CHATROOM_PARTICIPANTS = 707;
   public static final short CHATROOM_PARTICIPANTS = 708;
   public static final short MUTE_CHATROOM_PARTICIPANT = 709;
   public static final short UNMUTE_CHATROOM_PARTICIPANT = 710;
   public static final short ADD_FAVOURITE_CHATROOM = 711;
   public static final short REMOVE_FAVOURITE_CHATROOM = 712;
   public static final short GET_CHATROOM_CATEGORIES = 713;
   public static final short CHATROOM_CATEGORY = 714;
   public static final short GET_CHATROOM_CATEGORIES_COMPLETE = 715;
   public static final short GET_CATEGORIZED_CHATROOMS = 716;
   public static final short GET_CATEGORIZED_CHATROOMS_COMPLETE = 717;
   public static final short CHATROOM_NOTIFICATION = 718;
   public static final short CHATROOM_THEME = 719;
   public static final short CHATROOM_USER_STATUS = 720;
   public static final short GROUP_CHAT = 750;
   public static final short CREATE_GROUP_CHAT = 751;
   public static final short GROUP_CHAT_INVITE = 752;
   public static final short LEAVE_GROUP_CHAT = 753;
   public static final short GET_GROUP_CHAT_PARTICIPANTS = 754;
   public static final short GROUP_CHAT_PARTICIPANTS = 755;
   public static final short GROUP_CHAT_USER_STATUS = 756;
   public static final short PHONE_CALL = 800;
   public static final short WEBCALL = 801;
   public static final short WEBCALL_NOTIFICATION = 802;
   public static final short WEBCALL_RESPONSE = 803;
   public static final short DIAL = 804;
   public static final short COMPARE_CALL_RATE = 805;
   public static final short ANONYMOUS_CALL_NOTIFICATION = 806;
   public static final short ANONYMOUS_CALL_RESPONSE = 807;
   public static final short INVITE_FRIEND = 900;
   public static final short GET_ACCOUNT_BALANCE = 901;
   public static final short ACCOUNT_BALANCE = 902;
   public static final short RECHARGE = 903;
   public static final short FORGOT_PASSWORD = 904;
   public static final short GET_USER_PROFILE = 905;
   public static final short USER_PROFILE = 906;
   public static final short UPDATE_USER_PROFILE = 907;
   public static final short CHANGE_PASSWORD = 908;
   public static final short TRANSFER_CREDIT = 909;
   public static final short GET_URL = 910;
   public static final short DYNAMIC_MENU = 911;
   public static final short WALLPAPER = 912;
   public static final short GET_EMOTICON = 913;
   public static final short EMOTICON = 914;
   public static final short CLEAR_EMOTICON = 915;
   public static final short EMOTICON_HOTKEYS = 916;
   public static final short MIDLET_TAB = 918;
   public static final short MIDLET_ACTION = 920;
   public static final short CHANGE_USER_EVENT_SETTING = 921;
   public static final short OPEN_URL = 922;
   public static final short OPEN_URL_RESPONSE = 923;
   public static final short TEXT_COLOUR = 924;
   public static final short LANGUAGE = 925;
   public static final short GET_IM_ICONS = 926;
   public static final short IM_ICONS = 927;
   public static final short GET_EMOTICONS_COMPLETE = 928;
   public static final short GET_DYNAMIC_MENU_ICON = 929;
   public static final short DYNAMIC_MENU_ICON = 930;
   public static final short GET_EMOTICON_HOTKEYS = 937;
   public static final short GET_PRELOGIN_MARKETING_MSG = 931;
   public static final short PRELOGIN_MARKETING_MSG = 932;
   public static final short APPLICATION_MENU = 933;
   public static final short APPLICATION_MENU_COMPLETE = 934;
   public static final short GET_GIFT = 935;
   public static final short GIFT_HOTKEYS = 936;
   public static final short GET_STICKER_PACK_LIST = 938;
   public static final short STICKER_PACK_LIST = 939;
   public static final short GET_STICKER_PACK = 940;
   public static final short STICKER_PACK = 941;
   public static final short END_STICKER_PACK = 942;
   public static final short GET_UPLOAD_DATA_TICKET = 959;
   public static final short UPLOAD_ADDRESS_BOOK_CONTACTS = 960;
   public static final int VERSION_FIELDID_AND_VALUE_AS_FIELDS = 1;
   public static final int VERSION_FIELDID_AS_KEY = 2;
   protected static final byte PACKETID = 2;
   protected static final int MAX_CONTENT_LENGTH = 358400;
   protected static final int MAX_FIELD_LENGTH = 358400;
   protected short type;
   protected short transactionId;
   protected Map<Short, FusionField> fields = new HashMap();
   protected long timeReceived;
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(FusionPacket.class));

   public FusionPacket() {
      this.timeReceived = System.currentTimeMillis();
   }

   public FusionPacket(short type) {
      this.type = type;
      this.timeReceived = System.currentTimeMillis();
   }

   public FusionPacket(PacketType type) {
      this.type = type.value();
      this.timeReceived = System.currentTimeMillis();
   }

   public FusionPacket(PacketType type, short transactionId) {
      this.type = type.value();
      this.transactionId = transactionId;
      this.timeReceived = System.currentTimeMillis();
   }

   public FusionPacket(short type, short transactionId) {
      this.type = type;
      this.transactionId = transactionId;
      this.timeReceived = System.currentTimeMillis();
   }

   public FusionPacket(FusionPacket packet) {
      this.type = packet.type;
      this.transactionId = packet.transactionId;
      this.fields = packet.fields;
      this.timeReceived = packet.timeReceived;
   }

   public FusionPacket(ByteBuffer byteBuffer) throws IOException {
      this.read(byteBuffer);
   }

   public FusionPacket(JSONObject jsonPacket, int version) throws FusionException {
      try {
         this.timeReceived = System.currentTimeMillis();
         this.type = (short)jsonPacket.getInt("T");
         if (jsonPacket.has("I")) {
            this.setTransactionId((short)jsonPacket.getInt("I"));
         }

         if (jsonPacket.has("F")) {
            JSONArray arr;
            long[] lngArray;
            int i;
            String[] strArray;
            switch(version) {
            case 2:
               JSONObject jsonField = jsonPacket.getJSONObject("F");
               Iterator iter = jsonField.keys();

               while(true) {
                  while(true) {
                     while(iter != null && iter.hasNext()) {
                        String key = (String)iter.next();
                        arr = jsonField.optJSONArray(key);
                        if (arr != null) {
                           if (arr.length() == 0) {
                              this.setField(Short.parseShort(key), new String[0]);
                           } else if (arr.get(0) instanceof String) {
                              strArray = new String[arr.length()];

                              for(i = 0; i < arr.length(); ++i) {
                                 strArray[i] = arr.getString(i);
                              }

                              this.setField(Short.parseShort(key), strArray);
                           } else {
                              lngArray = new long[arr.length()];

                              for(i = 0; i < arr.length(); ++i) {
                                 lngArray[i] = arr.getLong(i);
                              }

                              this.setField(Short.parseShort(key), lngArray);
                           }
                        } else {
                           this.setJsonStringField(Short.parseShort(key), jsonField.getString(key));
                        }
                     }

                     return;
                  }
               }
            default:
               log.warn("JSON version " + version + " request received");
               JSONArray jsonFieldsArray = jsonPacket.getJSONArray("F");

               for(int j = 0; j < jsonFieldsArray.length(); ++j) {
                  JSONObject jsonField = jsonFieldsArray.getJSONObject(j);
                  arr = jsonField.optJSONArray("V");
                  if (arr == null) {
                     this.setJsonStringField((short)jsonField.getInt("N"), jsonField.getString("V"));
                  } else if (arr.length() == 0) {
                     this.setField((short)jsonField.getInt("N"), new String[0]);
                  } else if (arr.get(0) instanceof String) {
                     strArray = new String[arr.length()];

                     for(i = 0; i < arr.length(); ++i) {
                        strArray[i] = arr.getString(i);
                     }

                     this.setField((short)jsonField.getInt("N"), strArray);
                  } else {
                     lngArray = new long[arr.length()];

                     for(i = 0; i < arr.length(); ++i) {
                        lngArray[i] = arr.getLong(i);
                     }

                     this.setField((short)jsonField.getInt("N"), lngArray);
                  }
               }
            }
         }

      } catch (JSONException var9) {
         log.warn("JSONException in FusionPacket(JSONObject,int): " + var9.getMessage());
         throw new FusionException(var9.getMessage());
      } catch (Exception var10) {
         log.warn("Exception in FusionPacket(JSONObject,int): " + var10.getMessage());
         throw new FusionException(var10.getMessage());
      }
   }

   public short getType() {
      return this.type;
   }

   public void setType(short type) {
      this.type = type;
   }

   public short getTransactionId() {
      return this.transactionId;
   }

   public void setTransactionId(short transactionId) {
      this.transactionId = transactionId;
   }

   public Collection getFields() {
      return this.fields.values();
   }

   public FusionField getField(short index) {
      return (FusionField)this.fields.get(index);
   }

   public void setField(short index, boolean val) {
      this.fields.put(index, new FusionField(val));
   }

   public void setField(short index, byte val) {
      this.fields.put(index, new FusionField(val));
   }

   public void setField(short index, int val) {
      this.fields.put(index, new FusionField(val));
   }

   public void setField(short index, short val) {
      this.fields.put(index, new FusionField(val));
   }

   public void setField(short index, long val) {
      this.fields.put(index, new FusionField(val));
   }

   public void setField(short index, char val) {
      this.fields.put(index, new FusionField(val));
   }

   public void setField(short index, String val) {
      this.fields.put(index, new FusionField(val));
   }

   public void setField(short index, boolean[] val) {
      byte[] byteArray = new byte[val.length];

      for(int i = 0; i < val.length; ++i) {
         byteArray[i] = (byte)(val[i] ? 1 : 0);
      }

      this.fields.put(index, new FusionField(byteArray));
   }

   public void setField(short index, byte[] val) {
      this.fields.put(index, new FusionField(val));
   }

   public void setByteEnumArrayField(short index, ByteValueEnum[] val) {
      this.fields.put(index, FusionField.createByteEnumArrayField(val));
   }

   public void setField(short index, String[] val) {
      this.fields.put(index, new FusionField(val));
   }

   public void setField(short index, String[] val, char separator) {
      StringBuilder builder = new StringBuilder();
      if (val != null && val.length > 0) {
         builder.append(val[0]);

         for(int i = 1; i < val.length; ++i) {
            builder.append(separator).append(val[i]);
         }
      }

      this.fields.put(index, new FusionField(builder.toString()));
   }

   public void setFieldWithTrailingSeparator(short index, String[] val, char separator) {
      StringBuilder builder = new StringBuilder();
      if (val != null) {
         String[] arr$ = val;
         int len$ = val.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            String s = arr$[i$];
            builder.append(s).append(separator);
         }
      }

      this.fields.put(index, new FusionField(builder.toString()));
   }

   public void setField(short index, long[] val) {
      this.fields.put(index, new FusionField(val));
   }

   public void setField(short index, FusionField field) {
      this.fields.put(index, field);
   }

   public void setJsonStringField(short index, String val) {
      this.fields.put(index, FusionField.createJsonStringField(val));
   }

   public Boolean getBooleanField(short index) {
      FusionField f = this.getField(index);
      if (f == null) {
         return null;
      } else {
         Byte value = f.getByteVal();
         return value == null ? null : value != 0;
      }
   }

   public Byte getByteField(short index) {
      FusionField f = this.getField(index);
      return f == null ? null : f.getByteVal();
   }

   public Short getShortField(short index) {
      FusionField f = this.getField(index);
      return f == null ? null : f.getShortVal();
   }

   public Integer getIntField(short index) {
      FusionField f = this.getField(index);
      return f == null ? null : f.getIntVal();
   }

   public Long getLongField(short index) {
      FusionField f = this.getField(index);
      return f == null ? null : f.getLongVal();
   }

   public Character getCharField(short index) {
      FusionField f = this.getField(index);
      return f == null ? null : f.getCharVal();
   }

   public String getStringField(short index) {
      FusionField f = this.getField(index);
      return f == null ? null : f.getStringVal();
   }

   public boolean[] getBooleanArrayField(short index) {
      FusionField f = this.getField(index);
      return f == null ? null : f.getBooleanArrayVal();
   }

   public byte[] getByteArrayField(short index) {
      FusionField f = this.getField(index);
      return f == null ? null : f.getByteArrayVal();
   }

   public String[] getStringArrayField(short index) {
      FusionField f = this.getField(index);
      return f == null ? null : f.getStringArrayVal();
   }

   public String[] getStringArrayField(short index, char separator) {
      FusionField f = this.getField(index);
      if (f == null) {
         return null;
      } else {
         String s = f.getStringVal();
         if (s == null) {
            return null;
         } else {
            int length = getStringArrayLength(s, separator);
            String[] result = new String[length];
            int begin = 0;

            for(int i = 0; i < length; ++i) {
               int end = s.indexOf(separator, begin);
               result[i] = s.substring(begin, end != -1 ? end : s.length());
               begin = end + 1;
            }

            return result;
         }
      }
   }

   public String[] getStringArrayWithTrailingSeparatorField(short index, char separator) {
      FusionField f = this.getField(index);
      if (f == null) {
         return null;
      } else {
         String s = f.getStringVal();
         if (s == null) {
            return null;
         } else {
            int length = getStringArrayLength(s, separator);
            if (length > 0 && s.charAt(s.length() - 1) == separator) {
               --length;
            }

            String[] result = new String[length];
            int begin = 0;

            for(int i = 0; i < length; ++i) {
               int end = s.indexOf(separator, begin);
               result[i] = s.substring(begin, end != -1 ? end : s.length());
               begin = end + 1;
            }

            return result;
         }
      }
   }

   private static int getStringArrayLength(String s, char separator) {
      if (s.length() == 0) {
         return 0;
      } else {
         int counter = 0;
         int offset = -1;

         do {
            offset = s.indexOf(separator, offset + 1);
            ++counter;
         } while(offset != -1);

         return counter;
      }
   }

   public long[] getLongArrayField(short index) {
      FusionField f = this.getField(index);
      if (f == null) {
         return null;
      } else {
         long[] numbers = f.getNumericArrayVal();
         return numbers == null ? null : numbers;
      }
   }

   public int length() {
      int len = 0;

      FusionField f;
      for(Iterator i$ = this.fields.values().iterator(); i$.hasNext(); len += f.totalLength()) {
         f = (FusionField)i$.next();
      }

      return len;
   }

   public static boolean haveFusionPacket(ByteBuffer readBuffer) {
      if (readBuffer.remaining() < 9) {
         return false;
      } else {
         int contentLength = readBuffer.getInt(readBuffer.position() + 5) + 9;
         return contentLength <= readBuffer.remaining();
      }
   }

   public static short getPacketType(ByteBuffer readBuffer) {
      return readBuffer.getShort(readBuffer.position() + 1);
   }

   public int totalLength() {
      return this.length() + 9;
   }

   public void read(InputStream in) throws IOException {
      DataInputStream reader = new DataInputStream(in);
      if (reader.readByte() != 2) {
         throw new IOException("Invalid packet id. Packet id must be 2");
      } else {
         this.type = reader.readShort();
         this.transactionId = reader.readShort();
         this.fields.clear();
         int contentLen = reader.readInt();
         if (contentLen > 358400) {
            throw new IOException("Content length exceeded");
         } else {
            while(contentLen > 0) {
               short fieldNumber = reader.readShort();
               int fieldLen = reader.readInt();
               if (fieldLen > 358400) {
                  throw new IOException("Field length exceeded " + fieldLen);
               }

               byte[] fieldVal = new byte[fieldLen];
               reader.readFully(fieldVal);
               this.setField(fieldNumber, fieldVal);
               contentLen -= fieldLen + 6;
            }

         }
      }
   }

   public void read(ByteBuffer buffer) throws BufferUnderflowException, IOException {
      if (buffer.get() != 2) {
         throw new IOException("Invalid packet id. Packet id must be 2");
      } else {
         this.type = buffer.getShort();
         this.transactionId = buffer.getShort();
         this.fields.clear();
         int contentLen = buffer.getInt();
         if (contentLen > 358400) {
            throw new IOException("Content length exceeded");
         } else {
            while(contentLen > 0) {
               short fieldNumber = buffer.getShort();
               int fieldLen = buffer.getInt();
               if (fieldLen > 358400) {
                  throw new IOException("Field length exceeded " + fieldLen);
               }

               byte[] fieldVal = new byte[fieldLen];
               buffer.get(fieldVal);
               this.setField(fieldNumber, fieldVal);
               contentLen -= fieldLen + 6;
            }

         }
      }
   }

   public void write(OutputStream out) throws IOException {
      out.write(this.toByteArray());
      out.flush();
   }

   public byte[] toByteArray() {
      ByteBuffer buffer = ByteBuffer.allocate(this.totalLength());
      buffer.put((byte)2);
      buffer.putShort(this.type);
      buffer.putShort(this.transactionId);
      buffer.putInt(this.length());
      Iterator i$ = this.fields.entrySet().iterator();

      while(i$.hasNext()) {
         Entry<Short, FusionField> e = (Entry)i$.next();
         buffer.putShort((Short)e.getKey());
         buffer.putInt(((FusionField)e.getValue()).length());
         buffer.put(((FusionField)e.getValue()).getByteArrayVal());
      }

      return buffer.array();
   }

   public String toXML() {
      StringBuilder b = new StringBuilder();
      b.append("<P T=\"").append(this.type).append("\" I=\"").append(this.transactionId).append("\">");
      Iterator i$ = this.fields.entrySet().iterator();

      while(i$.hasNext()) {
         Entry<Short, FusionField> e = (Entry)i$.next();
         b.append("<F N=\"").append(e.getKey()).append("\">");
         b.append(((FusionField)e.getValue()).getStringVal().replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll("'", "&apos;").replaceAll("\"", "&quot;").replaceAll(">", "&gt;"));
         b.append("</F>");
      }

      return b.append("</P>").toString();
   }

   public String toJSON(int version) throws JSONException {
      JSONStringer jsonStringer = new JSONStringer();
      jsonStringer.object().key("T").value((long)this.type).key("I").value((long)this.transactionId);
      if (!this.fields.isEmpty()) {
         if (2 == version) {
            JSONObject obj = new JSONObject();
            Iterator i$ = this.fields.entrySet().iterator();

            while(i$.hasNext()) {
               Entry<Short, FusionField> e = (Entry)i$.next();
               ((FusionField)e.getValue()).setValueInJsonObject(obj, e.getKey() + "");
            }

            jsonStringer.key("F").value(obj);
         } else {
            log.warn("JSON version " + version + " response sent out");
            jsonStringer.key("F");
            jsonStringer.array();
            Iterator i$ = this.fields.entrySet().iterator();

            while(i$.hasNext()) {
               Entry<Short, FusionField> e = (Entry)i$.next();
               JSONObject obj = new JSONObject();
               obj.put("N", e.getKey());
               ((FusionField)e.getValue()).setValueInJsonObject(obj, "V");
               jsonStringer.value(obj);
            }

            jsonStringer.endArray();
         }
      }

      jsonStringer.endObject();
      if (log.isDebugEnabled()) {
         log.debug("Converted FusionPacket to JSON. FusionPacket:\n[\n" + this.toString() + "\n]\nJSON:\n" + jsonStringer.toString());
      }

      return jsonStringer.toString();
   }

   public static String toJSON(FusionPacket[] packets, int version) throws JSONException {
      StringBuilder b = new StringBuilder();
      b.append('[');

      for(int i = 0; i < packets.length; ++i) {
         if (i > 0) {
            b.append(',');
         }

         b.append(packets[i].toJSON(version));
      }

      b.append(']');
      return b.toString();
   }

   public static FusionPacket parseJSON(String json, int version) throws FusionException {
      if (log.isDebugEnabled()) {
         log.debug("Parsing JSON packet:\n" + json);
      }

      try {
         JSONObject jsonPacket = new JSONObject(json);
         return new FusionPacket(jsonPacket, version);
      } catch (JSONException var3) {
         log.warn("JSONException in parseJSON(): " + var3.getMessage());
         throw new FusionException(var3.getMessage());
      }
   }

   public static FusionPacket[] parseJSONArrayNonSpecific(String json, int version) throws FusionException {
      if (log.isDebugEnabled()) {
         log.debug("Parsing JSON packet:\n" + json);
      }

      ArrayList packets;
      try {
         JSONArray jsonPackets = new JSONArray(json);
         packets = new ArrayList(jsonPackets.length());

         for(int i = 0; i < jsonPackets.length(); ++i) {
            JSONObject jsonPacket = jsonPackets.getJSONObject(i);
            FusionPacket packet = new FusionPacket(jsonPacket, version);
            packets.add(packet);
         }
      } catch (JSONException var7) {
         log.warn("JSONException in parseJSON(): " + var7.getMessage());
         throw new FusionException(var7.getMessage());
      }

      int size = packets.size();
      return size == 0 ? null : (FusionPacket[])packets.toArray(new FusionPacket[size]);
   }

   public static FusionPacket[] parseJSONArray(String json, int version) throws FusionException {
      FusionPacket[] packets = parseJSONArrayNonSpecific(json, version);
      if (packets == null) {
         return null;
      } else {
         for(int i = 0; i < packets.length; ++i) {
            packets[i] = FusionPacketFactory.getSpecificPacket(packets[i]);
         }

         return packets;
      }
   }

   public static byte[] toByteArray(FusionPacket[] packets) {
      if (packets == null) {
         return null;
      } else {
         int len = 0;

         for(int i = 0; i < packets.length; ++i) {
            len += packets[i].totalLength();
         }

         ByteBuffer buffer = ByteBuffer.allocate(len);

         for(int i = 0; i < packets.length; ++i) {
            buffer.put(packets[i].toByteArray());
         }

         return buffer.array();
      }
   }

   public static String toXML(FusionPacket[] packets) {
      StringBuilder b = new StringBuilder();
      b.append("<A>");
      FusionPacket[] arr$ = packets;
      int len$ = packets.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         FusionPacket p = arr$[i$];
         b.append(p.toXML());
      }

      b.append("</A>");
      return b.toString();
   }

   public static FusionPacket[] parse(byte[] byteStream) throws IOException {
      ByteArrayInputStream in = new ByteArrayInputStream(byteStream);
      ArrayList packets = new ArrayList();

      while(in.available() > 0) {
         FusionPacket p = new FusionPacket();
         p.read((InputStream)in);
         packets.add(p);
      }

      int size = packets.size();
      return size == 0 ? null : (FusionPacket[])packets.toArray(new FusionPacket[size]);
   }

   public static FusionPacket[] parseXML(String xml) throws IOException, SAXException, ParserConfigurationException {
      DocumentBuilderFactory xmlFactory = DocumentBuilderFactory.newInstance();
      Document xmlDocument = xmlFactory.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
      NodeList nodeList = xmlDocument.getElementsByTagName("P");
      if (nodeList == null) {
         return null;
      } else {
         ArrayList<FusionPacket> packets = new ArrayList();

         for(Node packetNode = nodeList.item(0); packetNode != null; packetNode = packetNode.getNextSibling()) {
            Node attri = packetNode.getAttributes().getNamedItem("T");
            if (attri != null) {
               FusionPacket packet = new FusionPacket(Short.parseShort(attri.getNodeValue()));
               attri = packetNode.getAttributes().getNamedItem("I");
               if (attri != null) {
                  packet.setTransactionId(Short.parseShort(attri.getNodeValue()));
               }

               for(Node fieldNode = packetNode.getFirstChild(); fieldNode != null; fieldNode = fieldNode.getNextSibling()) {
                  attri = fieldNode.getAttributes().getNamedItem("N");
                  if (attri != null) {
                     Node data = fieldNode.getFirstChild();
                     if (data != null) {
                        if (data.getNodeType() == 3) {
                           packet.setField(Short.parseShort(attri.getNodeValue()), ((Text)data).getData());
                        }
                     } else {
                        packet.setField(Short.parseShort(attri.getNodeValue()), "");
                     }
                  }
               }

               packets.add(packet);
            }
         }

         int size = packets.size();
         if (size == 0) {
            return null;
         } else {
            return (FusionPacket[])packets.toArray(new FusionPacket[size]);
         }
      }
   }

   public FusionPacket[] toArray() {
      return new FusionPacket[]{this};
   }

   public String toString() {
      StringBuffer b = new StringBuffer();
      b.append("Packet ");
      b.append(this.type);
      b.append("\nLength ");
      b.append(this.length());
      b.append("\nTransaction ID ");
      b.append(this.transactionId);
      Iterator i$ = this.fields.entrySet().iterator();

      while(i$.hasNext()) {
         Entry<Short, FusionField> e = (Entry)i$.next();
         b.append('\n');
         b.append("Field ").append(e.getKey()).append(" ");
         b.append(((FusionField)e.getValue()).toString());
      }

      return b.toString();
   }

   public byte[] toSerializedBytes() throws FusionException {
      try {
         ByteArrayOutputStream b = new ByteArrayOutputStream();
         ObjectOutputStream o = new ObjectOutputStream(b);
         o.writeObject(this);
         return b.toByteArray();
      } catch (Exception var3) {
         log.error(var3);
         throw new FusionException(var3.getMessage());
      }
   }
}
