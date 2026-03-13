package com.projectgoth.fusion.bothunter;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.projectgoth.fusion.common.ConfigUtils;
import java.io.OutputStream;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import jpcap.packet.TCPPacket;
import org.apache.log4j.Logger;

public class InvalidPackets {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(InvalidPackets.class));
   private final Cache<String, AtomicInteger> data;
   private final ConcurrentMap<String, AtomicInteger> map;

   private InvalidPackets() {
      this.data = CacheBuilder.newBuilder().concurrencyLevel(4).maximumSize(10000L).expireAfterWrite(10L, TimeUnit.MINUTES).build();
      this.map = this.data.asMap();
   }

   public static InvalidPackets getInstance() {
      return InvalidPackets.SingletonHolder.INSTANCE;
   }

   public void hit(TCPPacket packet) {
      String key = this.makeClientKey(packet);
      if (this.map.get(key) == null) {
         this.map.put(key, new AtomicInteger(0));
      }

      int hits = ((AtomicInteger)this.map.get(key)).incrementAndGet();
      log.info("hits=" + hits + " from source=" + key + " threshold=" + Params.INVALID_FUSION_PACKET_THRESHOLD);
      if (hits > Params.INVALID_FUSION_PACKET_THRESHOLD) {
         int usersAtIP = UsernameCountPerIP.getInstance().get("" + packet.src_ip);
         log.info("...users at " + packet.src_ip + "=" + usersAtIP);
         if (usersAtIP <= Params.INVALID_FUSION_PACKET_LIMIT_USERS) {
            this.onThresholdReached(key, usersAtIP);
         }
      }

   }

   private void onThresholdReached(String clientAddress, int userCountAtIP) {
      try {
         log.info("Client " + clientAddress + " exceeded threshold for invalid fusion packets: running invalid fusion packet shell command");
         log.info(Params.INVALID_FUSION_PACKET_SHELL_COMMAND);
         ProcessBuilder pb = new ProcessBuilder(new String[]{Params.INVALID_FUSION_PACKET_SHELL_COMMAND, clientAddress, "" + userCountAtIP});
         pb.redirectErrorStream(true);
         final Process process = pb.start();
         Thread t = new Thread(new Runnable() {
            public void run() {
               try {
                  process.waitFor();
                  OutputStream os = process.getOutputStream();
                  os.flush();
               } catch (Exception var2) {
                  InvalidPackets.log.error("Exception waiting for process to complete: e=" + var2);
               }

            }
         });
         t.start();
      } catch (Exception var6) {
         log.error("Failed to run invalid fusion packet shell command: e=" + var6, var6);
      }

   }

   private String makeClientKey(TCPPacket packet) {
      return packet.src_ip.toString() + ":" + packet.src_port;
   }

   // $FF: synthetic method
   InvalidPackets(Object x0) {
      this();
   }

   private static class SingletonHolder {
      public static final InvalidPackets INSTANCE = new InvalidPackets();
   }
}
