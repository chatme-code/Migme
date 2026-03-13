package com.projectgoth.fusion.chat.external.gtalk;

import com.projectgoth.fusion.common.ByteBufferHelper;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import sun.misc.BASE64Encoder;

public class Avatar {
   private String base64EncodedValue = "";
   private String sha1Hash = "";
   private byte[] buffer;

   public Avatar(String fileName) throws IOException, NoSuchAlgorithmException {
      if (fileName != null && fileName.length() > 0) {
         File file = new File(fileName);
         this.buffer = new byte[(int)file.length()];
         FileInputStream in = new FileInputStream(file);
         in.read(this.buffer);
         this.base64EncodedValue = (new BASE64Encoder()).encode(this.buffer);
         MessageDigest messageDigest = MessageDigest.getInstance("SHA1");
         this.sha1Hash = ByteBufferHelper.toHexString(messageDigest.digest(this.buffer));
      }

   }

   public byte[] getBytes() {
      return this.buffer;
   }

   public String getBase64EncodedValue() {
      return this.base64EncodedValue;
   }

   public String getSHA1Hash() {
      return this.sha1Hash;
   }

   public String getUploadXML() {
      return "<vCard xmlns='vcard-temp'><PHOTO><BINVAL>" + this.base64EncodedValue + "</BINVAL></PHOTO></vCard>";
   }

   public String getPresenceXML() {
      return "<x xmlns='vcard-temp:x:update'><photo>" + this.sha1Hash + "</photo></x>";
   }
}
