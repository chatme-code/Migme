package com.projectgoth.fusion.app.dao.config;

import com.projectgoth.configuration.StringConfigurationValue;

public class FusionStringConfigurationValue extends StringConfigurationValue {
   public FusionStringConfigurationValue(FusionConfigEnum namespace) {
      super(namespace.getIdentifier());
   }

   public FusionStringConfigurationValue(FusionConfigEnum namespace, String name, String defaultValue) {
      super(namespace.getIdentifier(), name, defaultValue);
   }

   public FusionStringConfigurationValue(FusionConfigEnum namespace, String name) {
      super(namespace.getIdentifier(), name, "");
   }
}
