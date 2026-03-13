package com.projectgoth.fusion.chatnewsfeed.util;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.DateTimeUtils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class FileUtil {
   private static final Logger logger = Logger.getLogger(ConfigUtils.getLoggerName(FileUtil.class));

   public static void downloadFile(String resourceURL, File file) {
      try {
         URL url = new URL(resourceURL);
         BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
         BufferedWriter writer = new BufferedWriter(new FileWriter(file));

         String line;
         while((line = reader.readLine()) != null) {
            writer.write(line);
         }

         writer.close();
         reader.close();
         logger.info("Successfully downloaded " + file.getName());
      } catch (MalformedURLException var6) {
         logger.error("Invalid URL :" + resourceURL + "\n" + var6.getMessage());
      } catch (IOException var7) {
         logger.error("Error downloading file \n" + var7.getMessage());
      }

   }

   public static Document getDocument(DocumentBuilderFactory xmlFactory, File xmlSourceFile) {
      Document xmlDocument = null;

      try {
         xmlDocument = xmlFactory.newDocumentBuilder().parse(xmlSourceFile);
      } catch (SAXException var4) {
         logger.error("Parsing error on file " + xmlSourceFile, var4);
      } catch (IOException var5) {
         logger.error("Parsing error on file " + xmlSourceFile, var5);
      } catch (ParserConfigurationException var6) {
         logger.error("Parsing error on file " + xmlSourceFile, var6);
      }

      if (logger.isDebugEnabled()) {
         logger.debug("Successfully retrieved XML from " + xmlSourceFile);
      }

      return xmlDocument;
   }

   public static void deleteOldLocalFiles(String directory, List<String> fileTypes, List<String> filesToExclude) {
      logger.info("Starting local directory cleanup... ");
      Date midnightYesterday = DateTimeUtils.midnightYesterday();
      ExtensionFilter filter = new ExtensionFilter(fileTypes, filesToExclude);
      File dir = new File(directory);
      String[] files = dir.list(filter);
      if (files != null) {
         if (logger.isInfoEnabled()) {
            logger.info("Files matched at: " + directory + " for extensions: " + fileTypes);
            listFiles(files);
         }

         for(int i = 0; i < files.length; ++i) {
            File file = new File(directory, files[i]);
            String fileName = file.getName();
            Date lastModifiedDate = new Date(file.lastModified());
            if (lastModifiedDate.before(midnightYesterday)) {
               if (logger.isInfoEnabled()) {
                  logger.info("'" + fileName + "' - dated " + DateTimeUtils.dateToString(lastModifiedDate, "MM/dd/yyyy HH:mm:ss"));
               }

               boolean deleted = false;
               deleted = file.delete();
               if (deleted) {
                  logger.info("Deleted: " + fileName);
               } else {
                  logger.error("Could not delete '" + fileName + "'");
               }
            }
         }
      } else {
         logger.info("No matches found in local directory");
      }

      if (logger.isInfoEnabled()) {
         drawLine();
         logger.info("Local directory after cleanup:");
         listFiles(dir.listFiles());
      }

   }

   public static void deleteOldRemoteFiles(String ftpServer, String serverTimeZone, String path, String user, String password, List<String> fileTypes, List<String> filesToExclude) {
      FTPClient client = new FTPClient();
      boolean isConnected = false;
      logger.info("Starting remote directory cleanup. Remote server timezone is: " + serverTimeZone + "...");

      try {
         isConnected = configureFTPConnection(ftpServer, user, password, client);
      } catch (Exception var27) {
         logger.error("Error connecting to FTP server " + ftpServer);
      }

      if (isConnected) {
         try {
            FTPFile[] files = client.listFiles(path);
            Date midnightYesterday = DateTimeUtils.midnightYesterday();
            if (files != null) {
               if (logger.isInfoEnabled()) {
                  logger.info("Remote directory before cleanup: " + ftpServer + path);
                  listFiles(files);
                  logger.info("Checking files...");
               }

               for(int i = 0; i < files.length; ++i) {
                  if (files[i] != null && files[i].isFile()) {
                     String fileName = files[i].getName();
                     String fileExtension = fileName.substring(fileName.lastIndexOf("."));
                     Calendar lastModifiedDate = files[i].getTimestamp();
                     Date lastModifiedDateAdjusted = DateTimeUtils.getDateCurrentTimeZone(lastModifiedDate, serverTimeZone.trim());
                     boolean isDeletable = isValidFileExtension(fileTypes, fileExtension) && filesToExclude != null && !filesToExclude.isEmpty() && !filesToExclude.contains(fileName.trim()) && lastModifiedDateAdjusted.before(midnightYesterday);
                     if (isDeletable) {
                        deleteRemoteFile(client, path.substring(path.indexOf("/") + 1), fileName, lastModifiedDate, lastModifiedDateAdjusted);
                     }
                  }
               }
            }

            if (logger.isInfoEnabled()) {
               drawLine();
               logger.info("Remote directory after cleanup: ");
               listFiles(client.listFiles(path));
            }

            client.logout();
         } catch (IOException var28) {
            var28.printStackTrace();
         } finally {
            try {
               client.disconnect();
            } catch (IOException var26) {
               var26.printStackTrace();
            }

         }
      }

   }

   private static void drawLine() {
      logger.info("-------------------------------------------------------");
   }

   private static boolean configureFTPConnection(String ftpServer, String user, String password, FTPClient client) throws SocketException, IOException {
      FTPClientConfig conf = new FTPClientConfig("UNIX");
      conf.setLenientFutureDates(true);
      client.configure(conf);
      client.setConnectTimeout(30000);
      client.connect(ftpServer);
      logger.info("Connected to " + ftpServer);
      client.login(user, password);
      int reply = client.getReplyCode();
      if (!FTPReply.isPositiveCompletion(reply)) {
         client.disconnect();
         logger.error("Remote FTP server " + ftpServer + " refused connection for user: " + user);
         return false;
      } else {
         logger.info("Logged in to " + ftpServer);
         return true;
      }
   }

   private static void deleteRemoteFile(FTPClient client, String path, String fileName, Calendar lastModifiedDate, Date lastModifiedDateAdjusted) {
      if (logger.isInfoEnabled()) {
         logger.info("Attempt to delete: '" + fileName + "' - dated " + DateTimeUtils.dateToString(lastModifiedDateAdjusted, "MM/dd/yyyy HH:mm:ss,") + "(Server timestamp: " + DateTimeUtils.dateToString(lastModifiedDate.getTime(), "MM/dd/yyyy HH:mm:ss") + ")");
      }

      try {
         boolean deleted = client.deleteFile(path + "/" + fileName);
         if (deleted) {
            logger.info("Deleted '" + fileName + "'");
         } else {
            logger.info("Could not delete '" + path + "/" + fileName + "'");
         }
      } catch (Exception var6) {
         logger.error("Could not delete '" + path + "/" + fileName + "'", var6);
      }

   }

   private static boolean isValidFileExtension(List<String> fileTypes, String type) {
      boolean isValidExtension = false;
      if (fileTypes != null) {
         Iterator i$ = fileTypes.iterator();

         while(i$.hasNext()) {
            String fileType = (String)i$.next();
            if (fileType.equals(type)) {
               isValidExtension = true;
               break;
            }
         }
      }

      return isValidExtension;
   }

   private static void listFiles(File[] files) {
      if (files != null) {
         File[] arr$ = files;
         int len$ = files.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            File file = arr$[i$];
            logger.info(file.getName());
         }
      }

      drawLine();
   }

   private static void listFiles(String[] files) {
      if (files != null) {
         String[] arr$ = files;
         int len$ = files.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            String fileName = arr$[i$];
            logger.info(fileName);
         }
      }

      drawLine();
   }

   private static void listFiles(FTPFile[] files) {
      if (files != null) {
         FTPFile[] arr$ = files;
         int len$ = files.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            FTPFile file = arr$[i$];
            logger.info(file.getName());
         }
      }

      drawLine();
   }

   public static void main(String[] args) {
      String server = "kundulu.com";
      String path = "/feeds-test";
      String user = "sportzinteractive";
      String password = "W57pTD.a";
      List<String> fileTypes = new ArrayList();
      fileTypes.add(".xml");
      List<String> filesToExclude = new ArrayList();
      filesToExclude.add("calendar_new.xml");
      server = "ftp.sportzinteractive.feed.mig33.com";
      path = "/feeds/cricket";
      user = "sportz";
      password = "steer78_pact";

      for(int i = 0; i < 20; ++i) {
         logger.info("Running iteration :" + i);
         deleteOldRemoteFiles(server, "Canada/Central", path, user, password, fileTypes, filesToExclude);
      }

   }
}
