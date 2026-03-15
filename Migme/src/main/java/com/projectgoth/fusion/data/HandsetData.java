package com.projectgoth.fusion.data;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

public class HandsetData implements Serializable {
   public Integer id;
   public String vendor;
   public String phoneModel;
   public Integer instructionId;
   public String instructionText;
   public String midletVersion;
   public HandsetData.MidletAcceptTypeEnum midletAcceptType;
   public String midp;
   public String cldc;
   public Boolean cameraSupport;
   public Boolean fileSystemSupport;
   public Boolean pngSupport;
   public Boolean gifSupport;
   public Boolean jpegSupport;
   public Boolean signedMidletSupport;
   public Integer screenWidth;
   public Integer screenHeight;
   public Integer applicationIconSize;
   public String comments;

   public HandsetData() {
   }

   public HandsetData(ResultSet rs) throws SQLException {
      this.id = (Integer)rs.getObject("id");
      this.vendor = rs.getString("vendor");
      this.phoneModel = rs.getString("phonemodel");
      this.instructionId = (Integer)rs.getObject("instructionId");
      this.instructionText = rs.getString("instructiontext");
      this.midletVersion = rs.getString("midletversion");
      this.midp = rs.getString("midp");
      this.cldc = rs.getString("cldc");
      Integer intVal = (Integer)rs.getObject("camerasupport");
      if (intVal != null) {
         this.cameraSupport = intVal != 0;
      }

      intVal = (Integer)rs.getObject("filesystemsupport");
      if (intVal != null) {
         this.fileSystemSupport = intVal != 0;
      }

      intVal = (Integer)rs.getObject("pngsupport");
      if (intVal != null) {
         this.pngSupport = intVal != 0;
      }

      intVal = (Integer)rs.getObject("gifsupport");
      if (intVal != null) {
         this.gifSupport = intVal != 0;
      }

      intVal = (Integer)rs.getObject("jpegsupport");
      if (intVal != null) {
         this.jpegSupport = intVal != 0;
      }

      intVal = (Integer)rs.getObject("signedmidletsupport");
      if (intVal != null) {
         this.signedMidletSupport = intVal != 0;
      }

      intVal = (Integer)rs.getObject("midletaccepttype");
      if (intVal != null) {
         this.midletAcceptType = HandsetData.MidletAcceptTypeEnum.fromValue(intVal);
      }

      this.screenWidth = (Integer)rs.getObject("screenwidth");
      this.screenHeight = (Integer)rs.getObject("screenheight");
      this.applicationIconSize = (Integer)rs.getObject("applicationIconSize");
      this.comments = rs.getString("comments");
   }

   public static enum MidletAcceptTypeEnum {
      JAR(1),
      JAD(2),
      NOT_SURE(3);

      private int value;

      private MidletAcceptTypeEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static HandsetData.MidletAcceptTypeEnum fromValue(int value) {
         HandsetData.MidletAcceptTypeEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            HandsetData.MidletAcceptTypeEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }
}
