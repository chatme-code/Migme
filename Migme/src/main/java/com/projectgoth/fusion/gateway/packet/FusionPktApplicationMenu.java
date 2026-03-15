package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.data.ApplicationMenuOptionData;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktApplicationMenu extends FusionPacket {
   public FusionPktApplicationMenu() {
      super((short)933);
   }

   public FusionPktApplicationMenu(short transactionId) {
      super((short)933, transactionId);
   }

   public FusionPktApplicationMenu(ApplicationMenuOptionData menuData) {
      super((short)933);
      this.setMenuVersionId(menuData.menuVersionId);
      this.setPosition(menuData.position);
      this.setTextId(menuData.textId);
      this.setActionURL(menuData.actionURL);
      this.setIconURL(menuData.iconURL);
   }

   public int getMenuVersionId() {
      return this.getIntField((short)1);
   }

   public void setMenuVersionId(int menuVersionId) {
      this.setField((short)1, menuVersionId);
   }

   public int getPosition() {
      return this.getIntField((short)2);
   }

   public void setPosition(int position) {
      this.setField((short)2, position);
   }

   public int getTextId() {
      return this.getIntField((short)3);
   }

   public void setTextId(int textId) {
      this.setField((short)3, textId);
   }

   public String getActionURL() {
      return this.getStringField((short)4);
   }

   public void setActionURL(String actionURL) {
      this.setField((short)4, actionURL);
   }

   public String getIconURL() {
      return this.getStringField((short)5);
   }

   public void setIconURL(String iconURL) {
      this.setField((short)5, iconURL);
   }
}
