package com.projectgoth.fusion.gateway;

import Ice.Communicator;
import Ice.ObjectAdapter;
import Ice.Properties;
import com.projectgoth.fusion.common.CaptchaService;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.FusionApplicationContext;
import com.projectgoth.fusion.common.IcePrxFinder;
import com.projectgoth.fusion.mogilefs.DFSManager;
import com.projectgoth.fusion.slice.RegistryPrx;
import java.lang.ref.WeakReference;
import java.nio.channels.Selector;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.log4j.Logger;

public class GatewayContextBuilder extends FusionApplicationContext implements GatewayContext {
   static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(GatewayContextBuilder.class));
   private Properties properties;
   private Communicator communicator;
   private long startTime;
   private WeakReference<Map<Gateway.ThreadPoolName, InstrumentedThreadPool>> pools;
   private WeakReference<Selector> selector;
   private WeakReference<PurgeConnectionTask> purger;
   private WeakReference<ObjectAdapter> connectionAdapter;
   private WeakReference<DFSManager> mogileFSManager;
   private WeakReference<IcePrxFinder> icePrxFinder;
   private WeakReference<SamplingTask> samplingTask;
   private WeakReference<CaptchaService> captchaService;
   private AtomicReference<RegistryPrx> registryPrx = new AtomicReference();

   public Properties getProperties() {
      return this.properties;
   }

   public GatewayContextBuilder setProperties(Properties properties) {
      this.properties = properties;
      return this;
   }

   public Communicator getCommunicator() {
      return this.communicator;
   }

   public GatewayContextBuilder setCommunicator(Communicator communicator) {
      this.communicator = communicator;
      return this;
   }

   public long getStartTime() {
      return this.startTime;
   }

   public GatewayContextBuilder setStartTime(long startTime) {
      this.startTime = startTime;
      return this;
   }

   public GatewayContextBuilder setGatewayThreadPool(Map<Gateway.ThreadPoolName, InstrumentedThreadPool> pools) {
      this.pools = new WeakReference(pools);
      return this;
   }

   public Map<Gateway.ThreadPoolName, InstrumentedThreadPool> getGatewayThreadPool() {
      return (Map)this.extractProperty(this.pools);
   }

   public GatewayContextBuilder setSelector(Selector selector) {
      this.selector = new WeakReference(selector);
      return this;
   }

   public Selector getSelector() {
      return (Selector)this.extractProperty(this.selector);
   }

   public GatewayContextBuilder setPurger(PurgeConnectionTask purger) {
      this.purger = new WeakReference(purger);
      return this;
   }

   public PurgeConnectionTask getPurger() {
      return (PurgeConnectionTask)this.extractProperty(this.purger);
   }

   public GatewayContextBuilder setConnectionAdapter(ObjectAdapter connAdapter) {
      this.connectionAdapter = new WeakReference(connAdapter);
      return this;
   }

   public ObjectAdapter getConnectionAdapter() {
      return (ObjectAdapter)this.extractProperty(this.connectionAdapter);
   }

   public GatewayContextBuilder setMogileFSManager(DFSManager mogileFSManager) {
      this.mogileFSManager = new WeakReference(mogileFSManager);
      return this;
   }

   public DFSManager getMogileFSManager() {
      return (DFSManager)this.extractProperty(this.mogileFSManager);
   }

   public GatewayContextBuilder setIcePrxFinder(IcePrxFinder icePrxFinder) {
      this.icePrxFinder = new WeakReference(icePrxFinder);
      return this;
   }

   public IcePrxFinder getIcePrxFinder() {
      return (IcePrxFinder)this.extractProperty(this.icePrxFinder);
   }

   public GatewayContextBuilder setSamplingTask(SamplingTask samplingTask) {
      this.samplingTask = new WeakReference(samplingTask);
      return this;
   }

   public SamplingTask getSamplingTask() {
      return (SamplingTask)this.extractProperty(this.samplingTask);
   }

   public GatewayContextBuilder setCaptchaService(CaptchaService captchaService) {
      this.captchaService = new WeakReference(captchaService);
      return this;
   }

   public CaptchaService getCaptchaService() {
      return (CaptchaService)this.extractProperty(this.captchaService);
   }

   public synchronized void setRegistryPrx(RegistryPrx rp) {
      this.registryPrx.set(rp);
   }

   public RegistryPrx getRegistryPrx() {
      return (RegistryPrx)this.registryPrx.get();
   }
}
