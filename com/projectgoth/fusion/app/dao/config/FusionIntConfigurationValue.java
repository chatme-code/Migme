package com.projectgoth.fusion.app.dao.config;

import com.projectgoth.configuration.ConfigurationValue;
import com.projectgoth.configuration.IntConfigurationValue;

public class FusionIntConfigurationValue extends IntConfigurationValue {
   public FusionIntConfigurationValue(FusionConfigEnum namespace, String name, int defaultValue) {
      super(namespace.getIdentifier(), name, defaultValue);
   }

   public FusionIntConfigurationValue(FusionConfigEnum namespace, String name, ConfigurationValue defaultValue) {
      super(namespace.getIdentifier(), name, defaultValue);
   }
}
