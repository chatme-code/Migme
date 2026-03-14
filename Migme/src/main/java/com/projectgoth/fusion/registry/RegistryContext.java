package com.projectgoth.fusion.registry;

import Ice.Communicator;
import Ice.ObjectAdapter;
import Ice.Properties;
import com.projectgoth.fusion.slice.RegistryNodePrx;

public interface RegistryContext {
   Properties getProperties();

   Communicator getCommunicator();

   RegistryNodeI getRegistryNode();

   RegistryI getRegistry();

   ObjectAdapter getRegistryAdapter();

   String getHostName();

   RegistryNodePrx getThisNodePrx();
}
