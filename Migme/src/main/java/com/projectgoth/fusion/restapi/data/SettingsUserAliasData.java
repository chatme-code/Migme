package com.projectgoth.fusion.restapi.data;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(
   name = "settings"
)
public class SettingsUserAliasData {
   public String alias;

   public SettingsUserAliasData() {
   }

   public SettingsUserAliasData(String alias) {
      this.alias = alias;
   }
}
