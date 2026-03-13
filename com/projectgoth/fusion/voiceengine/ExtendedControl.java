package com.projectgoth.fusion.voiceengine;

import com.projectgoth.fusion.common.ConfigUtils;
import java.util.Stack;
import org.apache.log4j.Logger;

public class ExtendedControl {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(ExtendedControl.class));
   public static final int DIALOPT_DEFAULTS = 0;
   public static final int PLAYOPT_DEFAULTS = 0;
   public static final int PLAYOPT_DROPDIGIT = 1;
   public static final int PLAYOPT_NOQUEUE = 2;
   public static final int PLAYOPT_FORCE = 4;
   public static final int READOPT_DEFAULTS = 0;
   public static final int READOPT_NEEDPOUND = 1;
   public static final int READOPT_NOPOUND = 2;
   public static final int READOPT_FORCE = 4;
   public static final int RECDOPT_DEFAULTS = 0;
   public static final int DIALSTAT_UNKNOWN = 0;
   public static final int DIALSTAT_CHANUNAVAIL = 1;
   public static final int DIALSTAT_CONGESTION = 2;
   public static final int DIALSTAT_NOANSWER = 3;
   public static final int DIALSTAT_BUSY = 4;
   public static final int DIALSTAT_ANSWER = 5;
   public static final int DIALSTAT_CANCEL = 6;
   public static final int DIALSTAT_DONTCALL = 7;
   public static final int DIALSTAT_TORTURE = 8;
   public static final int DIALSTAT_INVALIDARGS = 9;
   protected FastAGIChannel channel = null;
   protected Stack<Integer> digitQueue = new Stack();
   protected boolean connected = false;

   public ExtendedControl(FastAGIChannel channel) {
      this.channel = channel;
      this.connected = true;
   }

   public void setChannel(FastAGIChannel channel) {
      this.channel = channel;
   }

   public FastAGIChannel getChannel() {
      return this.channel;
   }

   protected boolean hasOption(int options, int option) {
      boolean has = (options & option) == option;
      return has;
   }

   public void clearQueue() {
      this.digitQueue.clear();
   }

   public void setQueue(Stack<Integer> digitQueue) {
      this.digitQueue = digitQueue;
   }

   public Stack<Integer> getQueue() {
      return this.digitQueue;
   }

   public int getQueueCount() {
      return this.digitQueue != null ? this.digitQueue.size() : 0;
   }

   public boolean isConnected() {
      ChannelResponse response = null;
      if (!this.connected) {
         return false;
      } else {
         response = this.channel.execChannelStatus();
         if (response != null) {
            return response.getReturnValue() == 6;
         } else {
            return false;
         }
      }
   }

   public String getAsteriskCallerId() {
      String num = this.getVariable("CALLERID(num)");
      if (num == null || num.length() < 1) {
         String number = this.getVariable("CALLERID(number)");
         if (number != null && num.length() > 0) {
            num = number;
         } else {
            num = null;
         }
      }

      return num;
   }

   public void setAsteriskCallerId(String callerId) {
      if (callerId != null && callerId.length() > 0) {
         this.setVariable("CALLERID(num)", callerId);
         this.setAsteriskCallerIdName(callerId);
      } else {
         this.setVariable("CALLERID(num)", "");
         this.setAsteriskCallerIdName("");
      }

   }

   public String getAsteriskCallerIdName() {
      String name = this.getVariable("CALLERID(name)");
      if (name == null || name.length() < 1) {
         name = null;
      }

      return name;
   }

   public void setAsteriskCallerIdName(String name) {
      if (name != null && name.length() > 0) {
         this.setVariable("CALLERID(name)", name);
      } else {
         this.setVariable("CALLERID(name)", "");
      }

   }

   public String getAsteriskCallerDid() {
      String num = this.getParameter("did");
      if (num == null || num.length() < 1) {
         num = null;
      }

      return num;
   }

   public void setAsteriskCallerDid(String did) {
      if (did != null && did.length() > 0) {
         this.setParameter("did", did);
      } else {
         this.setParameter("did", "");
      }

   }

   public String getAsteriskLanguage() {
      String lang = this.getVariable("LANGUAGE()");
      if (lang == null || lang.length() < 1) {
         lang = null;
      }

      return lang;
   }

   public void setAsteriskLanguage(String langCode) {
      if (langCode != null && langCode.length() > 0) {
         this.setVariable("LANGUAGE()", langCode);
      } else {
         this.setVariable("LANGUAGE()", "");
      }

   }

   public String getAsteriskCurrency() {
      String curr = this.getVariable("CURRENCY");
      if (curr == null || curr.length() < 1) {
         curr = null;
      }

      return curr;
   }

   public void setAsteriskCurrency(String currCode) {
      if (currCode != null && currCode.length() > 0) {
         this.setVariable("CURRENCY", currCode);
      } else {
         this.setVariable("CURRENCY", "");
      }

   }

   public void setVariable(String name, String value) {
      if (name != null && name.length() >= 1) {
         if (value != null && value.length() > 0) {
            this.channel.execSetVariable(name, value);
         } else {
            this.channel.execSetVariable(name, "");
         }

      }
   }

   public String getVariable(String name) {
      ChannelResponse response = null;
      String value = null;
      if (name != null && name.length() >= 1) {
         response = this.channel.execGetVariable(name);
         if (response != null) {
            value = response.getReturnData();
            if (value == null || value.length() < 1) {
               value = null;
            }
         }

         return value;
      } else {
         return null;
      }
   }

   public void setParameter(String name, String value) {
      if (name != null && name.length() >= 1) {
         if (value != null && value.length() > 0) {
            this.channel.setParameter(name, value);
         } else {
            this.channel.setParameter(name, "");
         }

      }
   }

   public String getParameter(String name) {
      ChannelResponse response = null;
      String value = null;
      if (name != null && name.length() >= 1) {
         response = this.channel.getParameter(name);
         if (response != null) {
            value = response.getReturnData();
            if (value == null || value.length() < 1) {
               value = null;
            }
         }

         return value;
      } else {
         return null;
      }
   }

   public void answer() {
      this.channel.execAppAnswer();
   }

   public void wait(int seconds) {
      this.channel.execAppWait(seconds);
   }

   public void hangup() {
      this.digitQueue.clear();
      this.channel.execAppHangup();
      this.connected = false;
   }

   public int dial(String dialString, int seconds) {
      return this.dial(dialString, seconds, 0);
   }

   public int dial(String dialString, int seconds, int options) {
      ChannelResponse response = null;
      int status = 0;
      this.digitQueue.clear();
      response = this.channel.execAppDial(dialString, seconds, "");
      if (response != null) {
         response = this.channel.execGetVariable("DIALSTATUS");
         if (response != null && response.getReturnData() != null) {
            String dstatus = response.getReturnData();
            if (dstatus.equalsIgnoreCase("CHANUNAVAIL")) {
               status = 1;
            } else if (dstatus.equalsIgnoreCase("CONGESTION")) {
               status = 2;
            } else if (dstatus.equalsIgnoreCase("NOANSWER")) {
               status = 3;
            } else if (dstatus.equalsIgnoreCase("BUSY")) {
               status = 4;
            } else if (dstatus.equalsIgnoreCase("ANSWER")) {
               status = 5;
            } else if (dstatus.equalsIgnoreCase("ANSWERED")) {
               status = 5;
            } else if (dstatus.equalsIgnoreCase("CANCEL")) {
               status = 6;
            } else if (dstatus.equalsIgnoreCase("DONTCALL")) {
               status = 7;
            } else if (dstatus.equalsIgnoreCase("TORTURE")) {
               status = 8;
            } else if (dstatus.equalsIgnoreCase("INVALIDARGS")) {
               status = 9;
            } else {
               status = 0;
            }
         }
      }

      return status;
   }

   public int dialOptions(String dialString, int seconds, String dialOptions) {
      return this.dialOptions(dialString, seconds, dialOptions, 0);
   }

   public int dialOptions(String dialString, int seconds, String dialOptions, int options) {
      ChannelResponse response = null;
      int status = 0;
      this.digitQueue.clear();
      response = this.channel.execAppDial(dialString, seconds, dialOptions);
      if (response != null) {
         response = this.channel.execGetVariable("DIALSTATUS");
         if (response != null && response.getReturnData() != null) {
            String dstatus = response.getReturnData();
            log.info("Received dial status = '" + dstatus + "'");
            if (dstatus.equalsIgnoreCase("CHANUNAVAIL")) {
               status = 1;
            } else if (dstatus.equalsIgnoreCase("CONGESTION")) {
               status = 2;
            } else if (dstatus.equalsIgnoreCase("NOANSWER")) {
               status = 3;
            } else if (dstatus.equalsIgnoreCase("BUSY")) {
               status = 4;
            } else if (dstatus.equalsIgnoreCase("ANSWER")) {
               status = 5;
            } else if (dstatus.equalsIgnoreCase("ANSWERED")) {
               status = 5;
            } else if (dstatus.equalsIgnoreCase("CANCEL")) {
               status = 6;
            } else if (dstatus.equalsIgnoreCase("DONTCALL")) {
               status = 7;
            } else if (dstatus.equalsIgnoreCase("TORTURE")) {
               status = 8;
            } else if (dstatus.equalsIgnoreCase("INVALIDARGS")) {
               status = 9;
            } else {
               status = 0;
            }
         }
      }

      return status;
   }

   public int playback(String filename) {
      return this.playback(filename, 0);
   }

   public int playback(String filename, int options) {
      ChannelResponse response = null;
      if (filename != null && filename.length() >= 1) {
         if (this.hasOption(options, 4)) {
            response = this.channel.execAppPlayback(filename, "");
         } else if (this.digitQueue.size() < 1) {
            response = this.channel.execAppBackground(filename, "");
         }

         if (response != null) {
            if (response.getReturnValue() > 0) {
               this.digitQueue.push(response.getReturnValue());
            } else if (response.getReturnValue() < 0) {
               this.connected = false;
            }

            return response.getReturnValue();
         } else {
            return 0;
         }
      } else {
         return 0;
      }
   }

   public String readInputExit(String filename, String exitKeys, int maxDigits, int maxSeconds) {
      return this.readInputExit(filename, exitKeys, maxDigits, maxSeconds, 0);
   }

   public String readInputExit(String filename, String exitKeys, int maxDigits, int maxSeconds, int options) {
      ChannelResponse response = null;
      String newFilename = "";
      String data = "";
      if (this.hasOption(options, 4) || this.digitQueue.size() < 1) {
         newFilename = filename;
      }

      if (this.digitQueue.size() < 1 && exitKeys != null && exitKeys.length() > 0) {
         response = this.channel.execAppBackground(newFilename, "");
         newFilename = "";
         if (response == null || response.getReturnValue() < 1) {
            response = this.channel.execGetDigit(maxSeconds * 1000);
         }

         if (response == null || response.getReturnValue() <= 0) {
            return null;
         }

         if ((char)response.getReturnValue() == '#') {
            return data;
         }

         this.digitQueue.push(response.getReturnValue());
      }

      if (this.digitQueue.size() > 0) {
         for(int i = 0; i < this.digitQueue.size() && maxDigits > 0; ++i) {
            Integer digit = (Integer)this.digitQueue.pop();
            data = (char)digit.byteValue() + data;
            --maxDigits;
            if (i == 0 && exitKeys != null && exitKeys.length() > 0 && exitKeys.indexOf((char)digit.byteValue()) >= 0) {
               return data;
            }
         }
      }

      if (maxDigits > 0) {
         this.channel.execAppRead("READINPUT", newFilename, maxDigits, "", 1, maxSeconds);
         response = this.channel.execGetVariable("READINPUT");
         if (response != null) {
            data = data + response.getReturnData();
         }
      }

      return data != null && data.length() > 0 ? data : null;
   }

   public String readInput(String filename, int maxDigits, int maxSeconds) {
      return this.readInput(filename, maxDigits, maxSeconds, 0);
   }

   public String readInput(String filename, int maxDigits, int maxSeconds, int options) {
      ChannelResponse response = null;
      String newFilename = "";
      String data = "";
      if (this.hasOption(options, 4) || this.digitQueue.size() < 1) {
         newFilename = filename;
      }

      if (this.digitQueue.size() > 0) {
         for(int i = 0; i < this.digitQueue.size() && maxDigits > 0; ++i) {
            Integer digit = (Integer)this.digitQueue.pop();
            data = (char)digit.byteValue() + data;
            --maxDigits;
         }
      }

      if (maxDigits > 0) {
         this.channel.execAppRead("READINPUT", newFilename, maxDigits, "", 1, maxSeconds);
         response = this.channel.execGetVariable("READINPUT");
         if (response != null) {
            data = data + response.getReturnData();
         }
      }

      return data != null && data.length() > 0 ? data : null;
   }

   public int recordAudio(String filename, String format, int maxSilenceSeconds, int maxSeconds) {
      return this.recordAudio(filename, format, maxSilenceSeconds, maxSeconds, 0);
   }

   public int recordAudio(String filename, String format, int maxSilenceSeconds, int maxSeconds, int options) {
      ChannelResponse response = null;
      String newFilename = "";
      if (filename != null && filename.length() >= 1) {
         if (this.hasOption(options, 4) || this.digitQueue.size() < 1) {
            newFilename = filename;
         }

         response = this.channel.execAppRecord(newFilename, format, maxSilenceSeconds, maxSeconds, "");
         this.digitQueue.clear();
         return response != null ? response.getReturnValue() : 0;
      } else {
         return 0;
      }
   }

   public int sayDigits(String number) {
      return this.sayDigits(number, 0);
   }

   public int sayDigits(String number, int options) {
      int res = 0;
      long realnumber = 0L;
      if (this.hasOption(options, 4) || this.digitQueue.size() < 1) {
         try {
            realnumber = Long.parseLong(number);
         } catch (Exception var7) {
         }

         if (realnumber < 0L) {
            res = this.playback("digits/minus", options);
            realnumber *= -1L;
            if (res == 0) {
               return this.sayDigits("" + realnumber, options);
            }

            return res;
         }

         while(number.length() > 0 && res == 0) {
            String digit = number.substring(0, 1);
            res = this.playback("digits/" + digit, options);
            if (number.length() > 1) {
               number = number.substring(1);
            } else {
               number = "";
            }
         }
      }

      return res;
   }

   public int sayNumber(String number) {
      return this.sayNumber(number, 0);
   }

   public int sayNumber(String number, int options) {
      int res = 0;
      long realnumber = 0L;
      if (this.hasOption(options, 4) || this.digitQueue.size() < 1) {
         try {
            realnumber = Long.parseLong(number);
         } catch (Exception var16) {
         }

         if (realnumber < 0L) {
            res = this.playback("digits/minus", options);
            realnumber *= -1L;
            if (res == 0) {
               return this.sayNumber("" + realnumber, options);
            }

            return res;
         }

         if (realnumber == 0L) {
            res = this.playback("digits/0", options);
            return res;
         }

         long million = realnumber / 1000000L;
         long thousand = realnumber % 1000000L / 1000L;
         long hundred = realnumber % 1000L / 100L;
         long ten = realnumber % 100L / 10L;
         long small = realnumber % 10L;
         if (million > 0L && res == 0) {
            res = this.playback("digits/" + million, options);
            if (res == 0) {
               res = this.playback("digits/million", options);
            }
         }

         if (thousand > 0L && res == 0) {
            res = this.sayNumber("" + thousand, options);
            if (res == 0) {
               res = this.playback("digits/thousand", options);
            }
         }

         if (hundred > 0L && res == 0) {
            res = this.playback("digits/" + hundred, options);
            if (res == 0) {
               res = this.playback("digits/hundred", options);
            }
         }

         if (ten > 1L && res == 0) {
            res = this.playback("digits/" + ten + "0", options);
            if (small > 0L && res == 0) {
               res = this.playback("digits/" + small, options);
            }
         } else if (ten == 1L && small == 0L && res == 0) {
            res = this.playback("digits/10", options);
         } else if (small > 0L && res == 0) {
            res = this.playback("digits/" + (ten * 10L + small), options);
         }
      }

      return res;
   }

   public int playCongestion(int seconds) {
      ChannelResponse response = null;
      response = this.channel.execAppPlayTones("congestion");
      if (response != null && response.getReturnValue() < 0) {
         return response.getReturnValue();
      } else {
         response = this.channel.execAppWait(seconds);
         if (response != null && response.getReturnValue() < 0) {
            return response.getReturnValue();
         } else {
            response = this.channel.execAppStopPlayTones();
            return response != null && response.getReturnValue() < 0 ? response.getReturnValue() : 0;
         }
      }
   }

   public boolean sayLimit(long seconds) {
      log.info("Playing limit of " + seconds / 60L + " min, " + seconds % 60L + " secs (" + seconds / 60L + "." + seconds % 60L * 100L / 60L + " min)");
      this.playback("ct0027");
      if (seconds / 60L > 0L || seconds == 0L) {
         this.sayNumber("" + seconds / 60L);
      }

      if (seconds % 60L > 0L) {
         this.playback("ct0013");
         if (seconds % 60L < 10L) {
            this.sayDigits("0");
         }

         this.sayDigits("" + seconds % 60L * 100L / 60L);
      }

      this.playback("ct0028");
      return true;
   }

   public boolean sayBalance(double balance, String currency) {
      long cents = (long)(balance * 100.0D);
      log.info("Playing balance of " + cents / 100L + "." + cents % 100L + " dollars (" + currency + ")");
      this.playback("ct0021");
      if (cents / 100L > 0L || cents == 0L) {
         this.sayNumber("" + cents / 100L);
      }

      if (cents % 100L > 0L) {
         this.playback("ct0013");
         if (cents % 100L < 10L) {
            this.sayDigits("0");
         }

         this.sayDigits("" + cents % 100L);
      }

      if (currency != null && currency.toLowerCase().equals("usd")) {
         this.playback("ct0002");
      } else if (currency != null && currency.toLowerCase().equals("aud")) {
         this.playback("ct0001");
      } else if (currency != null && currency.toLowerCase().equals("cny")) {
         this.playback("ct0035");
      } else if (currency != null && currency.toLowerCase().equals("myr")) {
         this.playback("ct0036");
      } else if (currency != null && currency.toLowerCase().equals("zar")) {
         this.playback("ct0037");
      } else {
         log.warn("Unknown currency type (" + currency + "), so defaulting to USD");
         this.playback("ct0002");
      }

      return true;
   }

   public void setCDRUserField(String value) {
      this.channel.execAppSetCDRUserField(value);
   }

   public void addSIPHeader(String value) {
      this.channel.execAppSIPAddHeader(value);
   }

   public void resetCDR() {
      this.channel.execAppResetCDR();
   }
}
