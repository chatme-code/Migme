package com.projectgoth.fusion.common;

import com.projectgoth.fusion.fdl.enums.ClientType;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;

public class URLUtil {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(URLUtil.class));
   private static final Pattern PATTERN_URL_HOSTNAME_PREFIX = Pattern.compile("https?://[^/]+");

   public static String replaceViewTypeToken(String url, ClientType deviceType) {
      if (!StringUtil.isBlank(url)) {
         if (ClientType.ANDROID != deviceType && ClientType.WINDOWS_MOBILE != deviceType && ClientType.BLACKBERRY != deviceType) {
            if (ClientType.MRE == deviceType) {
               url = url.replaceAll("%1", "mre");
            } else if (ClientType.IOS == deviceType) {
               url = url.replaceAll("%1", "ios");
            } else {
               url = url.replaceAll("%1", "midlet");
            }
         } else {
            url = url.replaceAll("%1", "touch");
         }
      }

      return url;
   }

   public static boolean purgeVanishCache(String uri) {
      Socket socket = null;
      String payload = "PURGE " + uri + " HTTP/1.1\r\n\r\n";
      String serverAddress = SystemProperty.get((String)"Varnish01HTTPServer", (String)null);
      if (StringUtil.isBlank(serverAddress)) {
         return false;
      } else {
         boolean var5;
         try {
            socket = new Socket();
            InetSocketAddress endPoint = new InetSocketAddress(serverAddress, 80);
            socket.connect(endPoint, 1000);
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            bufferedWriter.write(payload);
            bufferedWriter.flush();
            bufferedWriter.close();
            return true;
         } catch (Exception var15) {
            log.error(var15.getMessage());
            var5 = false;
         } finally {
            try {
               if (socket != null) {
                  socket.close();
               }
            } catch (IOException var14) {
            }

         }

         return var5;
      }
   }

   public static String replaceUrlHost(String url, String replaceUrl) {
      Matcher m = PATTERN_URL_HOSTNAME_PREFIX.matcher(url);
      url = m.replaceFirst(replaceUrl);
      return url;
   }
}
