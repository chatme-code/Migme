package com.projectgoth.fusion.bothunter;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.gateway.packet.FusionPktLogin;
import com.projectgoth.fusion.packet.FusionField;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import jpcap.JpcapCaptor;
import jpcap.NetworkInterface;
import jpcap.packet.Packet;
import jpcap.packet.TCPPacket;
import org.apache.log4j.Logger;

public class JpcapPacketSource implements PacketSource {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(MonitorThread.class));
   private static final int FUSION_PACKET_HEADER_SIZE = 9;
   private static final byte FUSION_PACKET_TYPE = 2;
   private static final byte LOGIN_PACKET_TYPE_MSB = 0;
   private static final byte LOGIN_PACKET_TYPE_LSB = -56;
   private static final byte MSG_PACKET_TYPE_MSB = 1;
   private static final byte MSG_PACKET_TYPE_LSB = -12;
   private static final int LOGIN_PACKET_USERNAME_FIELD_INDEX = 5;
   private boolean debugEnabled;
   private NetworkInterface nic;
   private JpcapCaptor captor;
   private TcpOptionsCompact tcpOptions;
   private ByteBuffer bbInt;
   private ByteBuffer bbShort;
   private static final int READ_BUFFER_SIZE = 1024;
   private static final int MAX_READ_BUFFER_SIZE = 358400;
   private ByteBuffer readBuffer = ByteBuffer.allocate(1024);

   public JpcapPacketSource(String nicName, String pcapFilter) throws Exception {
      this.debugEnabled = log.isDebugEnabled();
      NetworkInterface[] devices = JpcapCaptor.getDeviceList();
      NetworkInterface[] arr$ = devices;
      int len$ = devices.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         NetworkInterface d = arr$[i$];
         if (d.name.equals(nicName)) {
            this.nic = d;
            break;
         }
      }

      this.captor = JpcapCaptor.openDevice(this.nic, 256000, false, Integer.MAX_VALUE);
      if (pcapFilter == null) {
         this.captor.setFilter("tcp and dst 192.168.2.220", true);
      } else {
         this.captor.setFilter(pcapFilter, true);
      }

      this.captor.setNonBlockingMode(true);
      this.tcpOptions = new TcpOptionsCompact();
      this.bbInt = ByteBuffer.allocate(4);
      this.bbInt.order(ByteOrder.BIG_ENDIAN);
      this.bbShort = ByteBuffer.allocate(2);
      this.bbShort.order(ByteOrder.BIG_ENDIAN);
   }

   public PacketDetails nextPacket() throws Exception {
      Packet p = this.captor.getPacket();
      if (p == null) {
         return null;
      } else if (!(p instanceof TCPPacket)) {
         return null;
      } else {
         TCPPacket tp = (TCPPacket)p;
         if (tp.option == null) {
            return null;
         } else {
            try {
               this.tcpOptions.parse(tp.syn, tp.option);
            } catch (Exception var5) {
               log.error("Exception in JpcapPacketSource.nextPacket(): ", var5);
               return null;
            }

            if (log.isDebugEnabled() && tp.syn) {
               log.debug("SYN packet" + (this.tcpOptions.hasWinScale() ? " with winscale=" + this.tcpOptions.getWinScale() : ""));
            }

            boolean hasWinscale = tp.syn && this.tcpOptions.hasWinScale() && this.tcpOptions.getWinScale() != 1;
            if (p.data.length == 0 && !hasWinscale) {
               return null;
            } else {
               PacketDetails pd = new PacketDetails(System.currentTimeMillis(), tp.src_ip.toString(), tp.src_port, (long)this.tcpOptions.getTsVal(), this.tcpOptions.getWinScale());
               if (hasWinscale) {
                  return pd;
               } else {
                  if (this.debugEnabled) {
                     log.debug("Packet with src_ip=" + tp.src_ip.toString() + " src_port=" + tp.src_port + " dst_ip=" + tp.dst_ip.toString() + " dst_port=" + tp.dst_port + " timestamp=" + pd.getTcpTimestamp() + " winscale=" + pd.getTcpWinscale() + " tp.data.length=" + tp.data.length);
                  }

                  if (tp.data.length != 0 && tp.data[0] == 2) {
                     this.onFusionPacket(tp, pd);
                  }

                  return pd;
               }
            }
         }
      }
   }

   private void onFusionPacket(TCPPacket tp, PacketDetails pd) throws Exception {
      FusionPacket fp = null;

      try {
         fp = this.readFusionPacket(tp, true);
      } catch (Exception var5) {
         if (this.debugEnabled) {
            log.debug("Invalid fusion packet!");
         }
      }

      if (fp != null) {
         this.sniffUsername(tp, fp, pd);
      }

      if (Params.MODE.equals(Params.Mode.INVALID_FUSION_PACKET_TYPE_DETECTION)) {
         log.debug("Invalid type detection, checking...");
         this.onFusionPacketInvalidPacketTypeDetectionMode(tp, pd);
      } else if (Params.MODE.equals(Params.Mode.INVALID_FUSION_PACKET_DETECTION)) {
         if (fp == null) {
            this.onInvalidPacket(tp);
         }
      } else if (!Params.MODE.equals(Params.Mode.BOT_DETECTION)) {
         throw new Exception("Invalid mode parameter");
      }

   }

   private void onFusionPacketInvalidPacketTypeDetectionMode(TCPPacket tp, PacketDetails pd) throws Exception {
      Short packetType = this.getPacketType(tp);
      if (packetType == null) {
         log.info("Invalid fusion packet type=" + packetType);
         this.onInvalidPacket(tp);
      } else if (!ValidFusionPackets.getInstance().contains(packetType)) {
         log.info("Invalid fusion packet type=" + packetType);
         this.onInvalidPacket(tp);
      }

   }

   private void onInvalidPacket(TCPPacket tp) throws Exception {
      InvalidPackets.getInstance().hit(tp);
   }

   private void sniffUsername(TCPPacket tp, FusionPacket fp, PacketDetails pd) throws Exception {
      if (tp.data.length >= 3) {
         if (fp.getType() == 200) {
            this.extractUsername(tp, fp, (short)5);
            if (this.debugEnabled) {
               log.debug("Getting claimed client type...");
            }

            FusionPktLogin login = new FusionPktLogin(fp);
            pd.setClaimedClientType(login.getClientType().value());
            if (this.debugEnabled) {
               log.debug("Got claimed client type=" + login.getClientType());
            }
         } else if (fp.getType() == 500) {
            this.extractUsername(tp, fp, (short)2);
         }

         if (this.debugEnabled) {
            String username = ClientAddressToUsername.getInstance().getUsername(tp);
            log.debug("Fusion packet (doesnt contain username): username=" + username + " host=" + tp.src_ip.toString() + " port=" + tp.src_port + " timestamp=" + pd.getTcpTimestamp() + "  packet as string=" + tp.toString());
         }

      }
   }

   private Short getPacketType(TCPPacket tp) throws Exception {
      return tp.data.length < 3 ? null : (short)(tp.data[1] << 8 | tp.data[2] & 255);
   }

   private FusionPacket readFusionPacket(TCPPacket tp, boolean tolerateEOF) throws Exception {
      FusionPacket fp = new FusionPacket();
      ByteArrayInputStream in = new ByteArrayInputStream(tp.data);

      try {
         fp.read((InputStream)in);
      } catch (EOFException var7) {
         if (!tolerateEOF) {
            throw var7;
         }

         if (this.debugEnabled) {
         }
      }

      if (this.debugEnabled) {
         for(short i = 0; i < fp.getFields().size(); ++i) {
            FusionField field = fp.getField(i);
            log.debug(i + " = " + field);
         }
      }

      return fp;
   }

   private void extractUsername(TCPPacket tp, FusionPacket fp, short usernameFieldIndex) throws Exception {
      if (this.debugEnabled) {
         log.debug("Extracting username from FusionPacket field " + usernameFieldIndex);
      }

      String username = fp.getStringField(usernameFieldIndex);
      if (username != null && username.length() != 0) {
         if (this.debugEnabled) {
            log.debug("User " + username + " logging in " + "with host=" + tp.src_ip.toString() + " port=" + tp.src_port);
         }

         ClientAddressToUsername.getInstance().putUsername(tp, username);
      }

   }

   private void handrolledExtractUsername_NotUsed(TCPPacket tp) throws Exception {
      int fieldOffset = 9;

      int stringDataLength;
      for(stringDataLength = 1; stringDataLength < 5; ++stringDataLength) {
         log.debug("Field " + stringDataLength + " is at offset " + fieldOffset);
         this.bbShort.position(0);
         this.bbShort.put(tp.data, fieldOffset, 2);
         this.bbShort.position(0);
         short fieldType = this.bbShort.getShort();
         log.debug("...is of type " + fieldType);
         this.bbInt.position(0);
         this.bbInt.put(tp.data, fieldOffset + 2, 4);
         this.bbInt.position(0);
         int dataLength = this.bbInt.getInt();
         log.debug("and has length 6+" + dataLength);
         fieldOffset += 6 + dataLength;
      }

      this.bbInt.position(0);
      this.bbInt.put(tp.data, fieldOffset + 2, 4);
      this.bbInt.position(0);
      stringDataLength = this.bbInt.getInt();
      log.debug("Username is of length " + stringDataLength);
      byte[] utf = new byte[stringDataLength];
      System.arraycopy(tp.data, fieldOffset + 2 + 4, utf, 0, stringDataLength);
      new String(utf, "UTF-8");
   }

   public boolean finished() throws Exception {
      return false;
   }

   public void close() {
   }

   public String getSourceIP() {
      return null;
   }

   public String getDestinationIP() {
      return null;
   }

   public long getTcpTimestamp() {
      return 0L;
   }

   public long getTimeReceived() {
      return 0L;
   }
}
