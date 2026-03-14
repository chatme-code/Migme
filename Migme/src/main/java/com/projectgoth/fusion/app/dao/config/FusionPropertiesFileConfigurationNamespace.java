package com.projectgoth.fusion.app.dao.config;

import com.projectgoth.configuration.PropertiesFileConfigurationNamespace;

public class FusionPropertiesFileConfigurationNamespace extends PropertiesFileConfigurationNamespace {
   private static final String CONFIG_DIR_PROPNAME = "fusion.config.dir";
   private static final String DEFAULT_CONFIG_DIR = "/usr/fusion/etc";

   public FusionPropertiesFileConfigurationNamespace(String filename) {
      super(filename);
   }

   protected final String getPath() {
      return System.getProperty("fusion.config.dir", "/usr/fusion/etc");
   }
}
