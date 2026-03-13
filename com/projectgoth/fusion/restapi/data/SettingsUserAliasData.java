package com.projectgoth.fusion.restapi.data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

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
