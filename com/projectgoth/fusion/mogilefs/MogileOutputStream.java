package com.projectgoth.fusion.mogilefs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;

public class MogileOutputStream extends OutputStream {
   private static Logger log = Logger.getLogger(MogileOutputStream.class);
   public static final int SOCKET_TIMEOUT = 60000;
   private Backend backend;
   private String domain;
   private String fid;
   private String path;
   private String devid;
   private String key;
   private long totalBytes;
   private Socket socket;
   private OutputStream out;
   private BufferedReader reader;
   private int count;

   public MogileOutputStream(Backend backend, String domain, String fid, String path, String devid, String key, long totalBytes) throws MalformedURLException, StorageCommunicationException {
      this.backend = backend;
      this.domain = domain;
      this.fid = fid;
      this.path = path;
      this.devid = devid;
      this.key = key;
      this.totalBytes = totalBytes;
      this.count = 0;

      try {
         this.socket = new Socket();
         this.socket.setSoTimeout(60000);
         URL parsedPath = new URL(path);
         this.socket.connect(new InetSocketAddress(parsedPath.getHost(), parsedPath.getPort()));
         this.out = this.socket.getOutputStream();
         this.reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
         Writer writer = new OutputStreamWriter(this.out);
         writer.write("PUT ");
         writer.write(parsedPath.getPath());
         writer.write(" HTTP/1.0\r\nContent-length: ");
         writer.write(Long.toString(totalBytes));
         writer.write("\r\n\r\n");
         writer.flush();
      } catch (IOException var11) {
         throw new StorageCommunicationException("problem initiating communication with storage server before storing " + path + ": " + var11.getMessage(), var11);
      }
   }

   public void close() throws IOException {
      if (this.out != null && this.socket != null) {
         this.out.flush();
         String response = this.reader.readLine();
         if (response == null) {
            throw new IOException("no response after putting file to " + this.path.toString());
         } else {
            Pattern validResponse = Pattern.compile("^HTTP/\\d+\\.\\d+\\s+(\\d+)");
            Matcher matcher = validResponse.matcher(response);
            if (!matcher.find()) {
               throw new IOException("response from put to " + this.path.toString() + " not understood: " + response);
            } else {
               int responseCode = Integer.parseInt(matcher.group(1));
               if (responseCode >= 200 && responseCode <= 299) {
                  this.out.close();
                  this.out = null;
                  this.reader.close();
                  this.reader = null;
                  this.socket.close();
                  this.socket = null;

                  try {
                     Map closeResponse = this.backend.doRequest("create_close", new String[]{"fid", this.fid, "devid", this.devid, "domain", this.domain, "size", Long.toString(this.totalBytes), "key", this.key, "path", this.path});
                     if (closeResponse == null) {
                        throw new IOException(this.backend.getLastErrStr());
                     }
                  } catch (NoTrackersException var6) {
                     throw new IOException(var6.getMessage());
                  } catch (TrackerCommunicationException var7) {
                     throw new IOException(var7.getMessage());
                  }
               } else {
                  StringBuffer fullResponse = new StringBuffer();
                  fullResponse.append("Problem storing to ");
                  fullResponse.append(this.path.toString());
                  fullResponse.append("\n\n");
                  fullResponse.append(response);
                  fullResponse.append("\n");

                  while((response = this.reader.readLine()) != null) {
                     fullResponse.append(response);
                     fullResponse.append("\n");
                  }

                  throw new IOException(fullResponse.toString());
               }
            }
         }
      } else {
         throw new IOException("socket has been closed already");
      }
   }

   public void flush() throws IOException {
      if (this.out != null && this.socket != null) {
         this.out.flush();
      } else {
         throw new IOException("socket has been closed already");
      }
   }

   public void write(int b) throws IOException {
      if (this.out != null && this.socket != null) {
         try {
            ++this.count;
            this.out.write(b);
         } catch (IOException var3) {
            log.error("wrote at most " + this.count + "/" + this.totalBytes + " of stream to storage node " + this.socket.getInetAddress().getHostName());
            throw var3;
         }
      } else {
         throw new IOException("socket has been closed already");
      }
   }

   public void write(byte[] b, int off, int len) throws IOException {
      if (this.out != null && this.socket != null) {
         try {
            this.count += len;
            this.out.write(b, off, len);
         } catch (IOException var5) {
            log.error("wrote at most " + this.count + "/" + this.totalBytes + " of stream to storage node " + this.socket.getInetAddress().getHostName());
            throw var5;
         }
      } else {
         throw new IOException("socket has been closed already");
      }
   }

   public void write(byte[] b) throws IOException {
      if (this.out != null && this.socket != null) {
         try {
            this.count += b.length;
            this.out.write(b);
         } catch (IOException var3) {
            log.error("wrote at most " + this.count + "/" + this.totalBytes + " of stream to storage node " + this.socket.getInetAddress().getHostName());
            throw var3;
         }
      } else {
         throw new IOException("socket has been closed already");
      }
   }
}
