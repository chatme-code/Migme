package com.projectgoth.fusion.registry;

import Ice.Communicator;
import Ice.ObjectAdapter;
import Ice.Properties;
import com.projectgoth.fusion.common.FusionApplicationContext;
import com.projectgoth.fusion.slice.RegistryNodePrx;
import java.lang.ref.WeakReference;

public class RegistryContextBuilder extends FusionApplicationContext implements RegistryContext {
   private WeakReference<Properties> properties;
   private WeakReference<Communicator> communicator;
   private WeakReference<RegistryNodeI> registryNode;
   private WeakReference<RegistryI> registry;
   private WeakReference<ObjectAdapter> registryAdapter;
   private String hostName;
   private WeakReference<RegistryNodePrx> thisNodePrx;

   public Properties getProperties() {
      return (Properties)this.extractProperty(this.properties);
   }

   public RegistryContextBuilder setProperties(Properties properties) {
      this.properties = new WeakReference(properties);
      return this;
   }

   public Communicator getCommunicator() {
      return (Communicator)this.extractProperty(this.communicator);
   }

   public RegistryContextBuilder setCommunicator(Communicator communicator) {
      this.communicator = new WeakReference(communicator);
      return this;
   }

   public RegistryNodeI getRegistryNode() {
      return (RegistryNodeI)this.extractProperty(this.registryNode);
   }

   public RegistryContextBuilder setRegistryNode(RegistryNodeI registryNode) {
      this.registryNode = new WeakReference(registryNode);
      return this;
   }

   public RegistryI getRegistry() {
      return (RegistryI)this.extractProperty(this.registry);
   }

   public RegistryContextBuilder setRegistry(RegistryI registry) {
      this.registry = new WeakReference(registry);
      return this;
   }

   public ObjectAdapter getRegistryAdapter() {
      return (ObjectAdapter)this.extractProperty(this.registryAdapter);
   }

   public RegistryContextBuilder setRegistryAdapter(ObjectAdapter registryAdapter) {
      this.registryAdapter = new WeakReference(registryAdapter);
      return this;
   }

   public String getHostName() {
      return this.hostName;
   }

   public RegistryContextBuilder setHostName(String hostName) {
      this.hostName = hostName;
      return this;
   }

   public RegistryNodePrx getThisNodePrx() {
      return (RegistryNodePrx)this.extractProperty(this.thisNodePrx);
   }

   public RegistryContextBuilder setThisNodePrx(RegistryNodePrx thisNodePrx) {
      this.thisNodePrx = new WeakReference(thisNodePrx);
      return this;
   }
}
