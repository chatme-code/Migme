package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.data.MenuData;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktDynamicMenu extends FusionPacket {
   public FusionPktDynamicMenu() {
      super((short)911);
   }

   public FusionPktDynamicMenu(short transactionId) {
      super((short)911, transactionId);
   }

   public FusionPktDynamicMenu(FusionPacket packet) {
      super(packet);
   }

   public FusionPktDynamicMenu(short transactionId, MenuData menuData) {
      super((short)911, transactionId);
      if (menuData.type != null) {
         this.setMenuType(menuData.type);
      }

      if (menuData.position != null) {
         this.setPosition(menuData.position);
      }

      if (menuData.title != null) {
         this.setTitle(menuData.title);
      }

      if (menuData.url != null) {
         this.setURL(menuData.url);
      }

      if (menuData.location != null && menuData.location.length() > 0) {
         this.setIconId(menuData.id);
      }

   }

   public Integer getMenuType() {
      return this.getIntField((short)1);
   }

   public void setMenuType(int menuType) {
      this.setField((short)1, menuType);
   }

   public Integer getPosition() {
      return this.getIntField((short)2);
   }

   public void setPosition(int position) {
      this.setField((short)2, position);
   }

   public String getTitle() {
      return this.getStringField((short)3);
   }

   public void setTitle(String title) {
      this.setField((short)3, title);
   }

   public String getURL() {
      return this.getStringField((short)4);
   }

   public void setURL(String url) {
      this.setField((short)4, url);
   }

   public Integer getIconId() {
      return this.getIntField((short)5);
   }

   public void setIconId(int iconId) {
      this.setField((short)5, iconId);
   }
}
