package com.projectgoth.fusion.common;

public class SystemPropertyEntryWithParent implements SystemPropertyEntities.SystemPropertyEntryInterfaceWithNamespace {
   private final String currentNamespace;
   private final String fqName;
   private final SystemPropertyEntities.SystemPropertyEntryInterfaceWithNamespace parentProperty;
   private final SystemPropertyEntities.SystemPropertyEntry<?> entry;
   private final SystemPropertyEntryFallbackCreator systemPropertyEntryFallbackCreator;

   protected SystemPropertyEntryWithParent(SystemPropertyEntryFallbackCreator sysPropEntryFallbackCreator, String subNameSpace, SystemPropertyEntities.SystemPropertyEntryInterfaceWithNamespace parentProperty) {
      this.parentProperty = parentProperty;
      this.currentNamespace = SystemPropertyEntities.getNameWithNamespace(parentProperty.getNamespace(), subNameSpace);
      this.fqName = StringUtil.isBlank(subNameSpace) ? parentProperty.getNamespace() : SystemPropertyEntities.getNameWithNamespace(this.currentNamespace, parentProperty.getEntry().getName());
      this.entry = sysPropEntryFallbackCreator.create(parentProperty);
      this.systemPropertyEntryFallbackCreator = sysPropEntryFallbackCreator;
   }

   public SystemPropertyEntities.SystemPropertyEntry<?> getEntry() {
      return this.entry;
   }

   public String getName() {
      return this.fqName;
   }

   public String getNamespace() {
      return this.currentNamespace;
   }

   public SystemPropertyEntities.SystemPropertyEntryInterfaceWithNamespace getParentProperty() {
      return this.parentProperty;
   }

   protected SystemPropertyEntryFallbackCreator getSystemPropertyEntryFallbackCreator() {
      return this.systemPropertyEntryFallbackCreator;
   }
}
