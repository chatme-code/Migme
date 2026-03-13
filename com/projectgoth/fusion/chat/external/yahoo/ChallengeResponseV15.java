package com.projectgoth.fusion.chat.external.yahoo;

import com.projectgoth.fusion.common.ConfigUtils;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.jboss.security.Base64Encoder;

public class ChallengeResponseV15 {
   private static final String DEFAULT_CHARSET = "UTF-8";
   private static final int CONNECTION_TIMEOUT = 5000;
   private static final int READ_TIMEOUT = 10000;
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(ChallengeResponseV15.class));

   public static String[] getStrings(String username, String password, String challenge) throws IOException, NoSuchAlgorithmException {
      Properties properties = httpPost("https://login.yahoo.com/config/pwtoken_get?src=ymsgr&login=" + urlEncode(username) + "&passwd=" + urlEncode(password) + "&chal=" + urlEncode(challenge));
      String ymsgr = properties.getProperty("ymsgr");
      if (ymsgr == null) {
         if (!properties.containsKey("1212") && !properties.containsKey("1213") && !properties.containsKey("1235")) {
            log.warn("Yahoo login failed for [" + username + ", " + password + ", " + challenge + "] when getting ymsgr");
         }

         return null;
      } else {
         properties = httpPost("https://login.yahoo.com/config/pwtoken_login?src=ymsgr&token=" + urlEncode(ymsgr));
         String crumb = properties.getProperty("crumb");
         String y = properties.getProperty("Y");
         String t = properties.getProperty("T");
         if (crumb != null && y != null && t != null) {
            String[] tokens = new String[]{mac64(md5(crumb + challenge)), y, t};
            return tokens;
         } else {
            log.warn("Yahoo login failed for [" + username + ", " + password + ", " + challenge + "] when getting crumb");
            return null;
         }
      }
   }

   private static Properties httpPost(String urlString) throws IOException {
      URL url = new URL(urlString);
      HttpURLConnection httpConn = (HttpURLConnection)url.openConnection();
      httpConn.setConnectTimeout(5000);
      httpConn.setReadTimeout(10000);
      httpConn.setDoOutput(true);
      httpConn.setUseCaches(false);
      int responseCode = httpConn.getResponseCode();
      if (responseCode == 200) {
         Properties properties = new Properties();
         properties.load(httpConn.getInputStream());
         return properties;
      } else {
         throw new IOException("HTTP response code " + responseCode + " when opening " + urlString);
      }
   }

   private static String urlEncode(String s) throws UnsupportedEncodingException {
      return URLEncoder.encode(s, "UTF-8");
   }

   private static byte[] md5(String s) throws NoSuchAlgorithmException {
      return MessageDigest.getInstance("MD5").digest(s.getBytes());
   }

   private static String mac64(byte[] ba) throws IOException {
      return Base64Encoder.encode(ba).replaceAll("=", "-").replaceAll("/", "_").replaceAll("\\+", ".");
   }
}
