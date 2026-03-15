package com.projectgoth.fusion.app.dao.config;

import com.projectgoth.configuration.BooleanConfigurationValue;

public class FusionBooleanConfigurationValue extends BooleanConfigurationValue {
   public FusionBooleanConfigurationValue(FusionConfigEnum namespace, String name, boolean defaultValue) {
      super(namespace.getIdentifier(), name, defaultValue);
   }

   public FusionBooleanConfigurationValue(FusionConfigEnum namespace, String name, BooleanConfigurationValue defaultValue) {
      super(namespace.getIdentifier(), name, defaultValue);
   }
}
