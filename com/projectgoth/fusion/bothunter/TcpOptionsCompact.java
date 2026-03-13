package com.projectgoth.fusion.bothunter;

import com.projectgoth.fusion.common.ConfigUtils;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.apache.log4j.Logger;

public class TcpOptionsCompact {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(MonitorThread.class));
   private static final int KIND_END = 0;
   private static final int KIND_NOP = 1;
   private static final int KIND_MSS = 2;
   private static final int KIND_WINSCALE = 3;
   private static final int KIND_SACK_PERMITTED = 4;
   private static final int KIND_SACK = 5;
   private static final int KIND_TS = 8;
   private static final int KIND_NOP_LENGTH = 1;
   private static final int KIND_MSS_LENGTH = 4;
   private static final int KIND_WINSCALE_LENGTH = 3;
   private static final int KIND_SACK_PERMITTED_LENGTH = 2;
   private static final int KIND_TS_LENGTH = 10;
   private static final int OPTION_HEADER_LENGTH = 2;
   private static final int TCP_HEADER_LENGTH = 20;
   private static final String BAD_FORMAT = "bad tcp option format";
   private boolean debugEnabled;
   private int offset = 0;
   private boolean hasMss = false;
   private boolean hasWinScale = false;
   private boolean hasSack = false;
   private boolean hasTs = false;
   private int mss = 0;
   private Byte winScale = null;
   private boolean sack = false;
   private int tsVal = 0;
   private int tsEcr = 0;
   private ByteBuffer bbInt;

   public TcpOptionsCompact() {
      this.debugEnabled = log.isDebugEnabled();
      this.bbInt = ByteBuffer.allocate(4);
      this.bbInt.order(ByteOrder.BIG_ENDIAN);
   }

   public void reset() {
      this.offset = 0;
      this.hasMss = false;
      this.hasWinScale = false;
      this.hasSack = false;
      this.hasTs = false;
      this.mss = 0;
      this.winScale = null;
      this.sack = false;
      this.tsVal = 0;
      this.tsEcr = 0;
   }

   public void parse(boolean syn, byte[] optionsBytes) throws Exception {
      this.reset();

      while(this.offset < optionsBytes.length) {
         switch(optionsBytes[this.offset]) {
         case 0:
            if (this.debugEnabled) {
               log.debug("Parsing KIND_END with syn=" + syn);
            }

            return;
         case 1:
            if (this.debugEnabled) {
               log.debug("Parsing KIND_NOP with syn=" + syn);
            }

            ++this.offset;
            break;
         case 2:
            if (this.debugEnabled) {
               log.debug("Parsing KIND_MSS with syn=" + syn);
            }

            if (4 != optionsBytes[this.offset + 1]) {
               throw new Exception("bad tcp option format at offset=" + this.offset + " kind=KIND_MSS");
            }

            this.hasMss = true;
            this.mss = this.getShort(optionsBytes, this.offset + 2);
            this.offset += 4;
            break;
         case 3:
            if (this.debugEnabled) {
               log.debug("Parsing KIND_WINSCALE with syn=" + syn);
            }

            if (!syn) {
               throw new Exception("Unexpected winscale option in non-SYN packet");
            }

            if (3 != optionsBytes[this.offset + 1]) {
               throw new Exception("bad tcp option format at offset=" + this.offset + " kind=KIND_WINSCALE");
            }

            this.hasWinScale = true;
            this.winScale = optionsBytes[this.offset + 2];
            if (this.debugEnabled) {
               log.debug("READ WINSCALE=" + this.winScale);
            }

            this.offset += 3;
            break;
         case 4:
            if (this.debugEnabled) {
               log.debug("Parsing KIND_SACK_PERMITTED with syn=" + syn);
            }

            if (2 != optionsBytes[this.offset + 1]) {
               throw new Exception("bad tcp option format at offset=" + this.offset + " kind=KIND_SACK_PERMITTED");
            }

            this.hasSack = true;
            this.sack = true;
            this.offset += 2;
            break;
         case 5:
            if (this.debugEnabled) {
               log.debug("Parsing KIND_SACK with syn=" + syn);
            }

            byte length = optionsBytes[this.offset + 1];
            if (this.debugEnabled) {
               log.debug("SACK length=" + length);
            }

            this.offset += length;
            break;
         case 6:
         case 7:
         default:
            if (this.debugEnabled) {
               log.debug("Default KIND with syn=" + syn + " - returning");
            }

            return;
         case 8:
            if (this.debugEnabled) {
               log.debug("Parsing KIND_TS with syn=" + syn);
            }

            if (10 != optionsBytes[this.offset + 1]) {
               throw new Exception("bad tcp option format at offset=" + this.offset + " kind=KIND_TS");
            }

            this.hasTs = true;
            this.tsVal = this.getInt(optionsBytes, this.offset + 2);
            this.tsEcr = this.getInt(optionsBytes, this.offset + 4);
            this.offset += 10;
         }
      }

   }

   private int getInt(byte[] optionsBytes, int offset) {
      this.bbInt.position(0);
      this.bbInt.put(optionsBytes, offset, 4);
      this.bbInt.position(0);
      return this.bbInt.getInt();
   }

   public short getShort(byte[] optionsBytes, int offset) {
      ByteBuffer bb = ByteBuffer.allocate(2);
      bb.order(ByteOrder.BIG_ENDIAN);
      bb.put(optionsBytes, offset, 2);
      bb.position(0);
      return bb.getShort();
   }

   public int getOptionsLength() {
      return this.offset;
   }

   public boolean hasMss() {
      return this.hasMss;
   }

   public boolean hasWinScale() {
      return this.hasWinScale;
   }

   public boolean hasSack() {
      return this.hasSack;
   }

   public boolean hasTs() {
      return this.hasTs;
   }

   public int getMss() {
      return this.mss;
   }

   public Byte getWinScale() {
      return this.winScale;
   }

   public boolean getSack() {
      return this.sack;
   }

   public int getTsVal() {
      return this.tsVal;
   }

   public int getTsEcr() {
      return this.tsEcr;
   }
}
