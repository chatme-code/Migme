package com.projectgoth.fusion.app.dao.config;

import com.projectgoth.configuration.StringListConfigurationValue;

public class FusionStringListConfigurationValue extends StringListConfigurationValue {
   public FusionStringListConfigurationValue(FusionConfigEnum namespace, String name, String defaultValue) {
      super(namespace.getIdentifier(), name, defaultValue);
   }

   public FusionStringListConfigurationValue(FusionConfigEnum namespace, String name) {
      super(namespace.getIdentifier(), name, "");
   }
}
