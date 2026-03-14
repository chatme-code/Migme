package com.projectgoth.fusion.chat.external.aim;

import com.projectgoth.fusion.common.ByteBufferHelper;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class BuddyIcon extends Connection {
   private static final short CLIENT_ID = 272;
   private static final short CLIENT_VERSION = 1147;
   private Map<Short, Short> snacServices = new HashMap();
   private String name;
   private Short id;
   private byte[] data;
   private byte[] md5Hash;
   private byte[] cookie;

   public BuddyIcon(short id, String name, String fileName) throws IOException, NoSuchAlgorithmException {
      this.id = id;
      this.name = name;
      File file = new File(fileName);
      FileInputStream in = new FileInputStream(file);
      this.data = new byte[(int)file.length()];
      int bytesRead = 0;

      for(int available = in.available(); available > 0; available = in.available()) {
         bytesRead += in.read(this.data, bytesRead, available);
      }

      in.close();
      this.md5Hash = MessageDigest.getInstance("MD5").digest(this.data);
   }

   public void upload(String server, int port, byte[] cookie) throws AIMException {
      this.cookie = cookie;
      this.connect(server, port);
   }

   public byte[] getData() {
      return this.data;
   }

   public Short getID() {
      return this.id;
   }

   public void setID(Short id) {
      this.id = id;
   }

   public byte[] getMd5Hash() {
      return this.md5Hash;
   }

   public String getName() {
      return this.name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public boolean validateMD5Hash(byte[] md5Hash) {
      return this.md5Hash != null && ByteBufferHelper.compare(this.md5Hash, md5Hash) == 0;
   }

   protected void onDisconnect(String reason) {
   }

   protected void onIncomingPacket(FLAP packet) {
      byte channel = packet.getChannel();
      if (channel == 1) {
         this.onServerHello();
      } else if (channel == 2) {
         SNAC snac = new SNAC(ByteBuffer.wrap(packet.getData()));
         short service = snac.getService();
         short subType = snac.getSubType();
         ByteBuffer data = snac.getAdjustedData();
         if (service == 1) {
            if (subType == 3) {
               this.onServiceList(snac, data);
            } else if (subType == 24) {
               this.onServiceVersions(snac, data);
            } else if (subType == 7) {
               this.onRateLimits(snac, data);
            }
         } else if (service == 16 && (subType == 1 || subType == 3)) {
            this.disconnect("");
         }
      } else if (channel == 4) {
         this.disconnect("");
      }

   }

   private void onServerHello() {
      this.sendAsyncPacket((new FLAP((byte)1)).append(1).append(new TLV((short)6, this.cookie)));
   }

   private void onServiceList(SNAC snac, ByteBuffer data) {
      SNAC serviceVersionRequest = new SNAC((short)1, (short)23);

      while(data.hasRemaining()) {
         serviceVersionRequest.append(data.getShort()).append((short)1);
      }

      this.sendAsyncPacket(new FLAP(serviceVersionRequest));
   }

   private void onServiceVersions(SNAC snac, ByteBuffer data) {
      while(data.hasRemaining()) {
         short snacService = data.getShort();
         short version = data.getShort();
         this.snacServices.put(snacService, version);
      }

      this.sendAsyncPacket(new FLAP(new SNAC((short)1, (short)6)));
   }

   private void onRateLimits(SNAC snac, ByteBuffer data) {
      SNAC rateLimitAck = new SNAC((short)1, (short)8);
      short classes = data.getShort();
      data.get(new byte[30 * classes]);

      for(int i = 0; i < classes; ++i) {
         short groupID = data.getShort();
         short pairs = data.getShort();
         data.get(new byte[4 * pairs]);
         rateLimitAck.append(groupID);
      }

      this.sendAsyncPacket(new FLAP(rateLimitAck));
      SNAC ready = new SNAC((short)1, (short)2);
      Iterator i$ = this.snacServices.entrySet().iterator();

      while(i$.hasNext()) {
         Entry<Short, Short> snacService = (Entry)i$.next();
         ready.append((Short)snacService.getKey()).append((Short)snacService.getValue()).append((short)272).append((short)1147);
      }

      this.sendAsyncPacket(new FLAP(ready));
      this.sendAsyncPacket(new FLAP((new SNAC((short)16, (short)2)).append(Short.valueOf(this.name)).append((short)this.data.length).append(this.data)));
   }
}
