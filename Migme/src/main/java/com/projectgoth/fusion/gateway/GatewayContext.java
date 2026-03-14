package com.projectgoth.fusion.gateway;

import Ice.Communicator;
import Ice.ObjectAdapter;
import Ice.Properties;
import com.projectgoth.fusion.common.CaptchaService;
import com.projectgoth.fusion.common.IcePrxFinder;
import com.projectgoth.fusion.mogilefs.DFSManager;
import com.projectgoth.fusion.slice.RegistryPrx;
import java.nio.channels.Selector;
import java.util.Map;

public interface GatewayContext {
   Properties getProperties();

   Communicator getCommunicator();

   long getStartTime();

   Map<Gateway.ThreadPoolName, InstrumentedThreadPool> getGatewayThreadPool();

   Selector getSelector();

   PurgeConnectionTask getPurger();

   ObjectAdapter getConnectionAdapter();

   DFSManager getMogileFSManager();

   IcePrxFinder getIcePrxFinder();

   SamplingTask getSamplingTask();

   CaptchaService getCaptchaService();

   RegistryPrx getRegistryPrx();
}
