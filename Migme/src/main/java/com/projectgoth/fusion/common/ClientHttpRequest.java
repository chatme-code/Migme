package com.projectgoth.fusion.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

public class ClientHttpRequest {
   URLConnection connection;
   OutputStream os;
   Map cookies;
   private static Random random = new Random();
   String boundary;

   protected void connect() throws IOException {
      if (this.os == null) {
         this.os = this.connection.getOutputStream();
      }

   }

   protected void write(char c) throws IOException {
      this.connect();
      this.os.write(c);
   }

   protected void write(String s) throws IOException {
      this.connect();
      this.os.write(s.getBytes());
   }

   protected void newline() throws IOException {
      this.connect();
      this.write("\r\n");
   }

   protected void writeln(String s) throws IOException {
      this.connect();
      this.write(s);
      this.newline();
   }

   protected static String randomString() {
      return Long.toString(random.nextLong(), 36);
   }

   private void boundary() throws IOException {
      this.write("--");
      this.write(this.boundary);
   }

   public ClientHttpRequest(URLConnection connection) throws IOException {
      this.os = null;
      this.cookies = new HashMap();
      this.boundary = "---------------------------" + randomString() + randomString() + randomString();
      this.connection = connection;
      connection.setDoOutput(true);
      connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + this.boundary);
   }

   public ClientHttpRequest(URL url) throws IOException {
      this(url.openConnection());
   }

   public ClientHttpRequest(String urlString) throws IOException {
      this(new URL(urlString));
   }

   public void setCookie(String name, String value) throws IOException {
      this.cookies.put(name, value);
   }

   public void setCookies(Map cookies) throws IOException {
      if (cookies != null) {
         this.cookies.putAll(cookies);
      }
   }

   public void setCookies(String[] cookies) throws IOException {
      if (cookies != null) {
         for(int i = 0; i < cookies.length - 1; i += 2) {
            this.setCookie(cookies[i], cookies[i + 1]);
         }

      }
   }

   private void writeName(String name) throws IOException {
      this.newline();
      this.write("Content-Disposition: form-data; name=\"");
      this.write(name);
      this.write('"');
   }

   public void setParameter(String name, String value) throws IOException {
      this.boundary();
      this.writeName(name);
      this.newline();
      this.newline();
      this.writeln(value);
   }

   private static void pipe(InputStream in, OutputStream out) throws IOException {
      byte[] buf = new byte[500000];
      int total = 0;
      int nread;
      synchronized(in) {
         while((nread = in.read(buf, 0, buf.length)) >= 0) {
            out.write(buf, 0, nread);
            total += nread;
         }
      }

      out.flush();
      byte[] buf = null;
   }

   public void setParameter(String name, String filename, InputStream is) throws IOException {
      this.boundary();
      this.writeName(name);
      this.write("; filename=\"");
      this.write(filename);
      this.write('"');
      this.newline();
      this.write("Content-Type: ");
      String type = URLConnection.guessContentTypeFromName(filename);
      if (type == null) {
         type = "application/octet-stream";
      }

      this.writeln(type);
      this.newline();
      pipe(is, this.os);
      this.newline();
   }

   public void setParameter(String name, File file) throws IOException {
      this.setParameter(name, file.getPath(), new FileInputStream(file));
   }

   public void setParameter(String name, Object object) throws IOException {
      if (object instanceof File) {
         this.setParameter(name, (File)object);
      } else {
         this.setParameter(name, object.toString());
      }

   }

   public void setParameters(Map parameters) throws IOException {
      if (parameters != null) {
         Iterator i = parameters.entrySet().iterator();

         while(i.hasNext()) {
            Entry entry = (Entry)i.next();
            this.setParameter(entry.getKey().toString(), entry.getValue());
         }

      }
   }

   public void setParameters(Object[] parameters) throws IOException {
      if (parameters != null) {
         for(int i = 0; i < parameters.length - 1; i += 2) {
            this.setParameter(parameters[i].toString(), parameters[i + 1]);
         }

      }
   }

   public InputStream post() throws IOException {
      this.boundary();
      this.writeln("--");
      this.os.close();
      return this.connection.getInputStream();
   }

   public InputStream post(Map parameters) throws IOException {
      this.setParameters(parameters);
      return this.post();
   }

   public InputStream post(Object[] parameters) throws IOException {
      this.setParameters(parameters);
      return this.post();
   }

   public InputStream post(Map cookies, Map parameters) throws IOException {
      this.setCookies(cookies);
      this.setParameters(parameters);
      return this.post();
   }

   public InputStream post(String[] cookies, Object[] parameters) throws IOException {
      this.setCookies(cookies);
      this.setParameters(parameters);
      return this.post();
   }

   public InputStream post(String name, Object value) throws IOException {
      this.setParameter(name, value);
      return this.post();
   }

   public InputStream post(String name1, Object value1, String name2, Object value2) throws IOException {
      this.setParameter(name1, value1);
      return this.post(name2, value2);
   }

   public InputStream post(String name1, Object value1, String name2, Object value2, String name3, Object value3) throws IOException {
      this.setParameter(name1, value1);
      return this.post(name2, value2, name3, value3);
   }

   public InputStream post(String name1, Object value1, String name2, Object value2, String name3, Object value3, String name4, Object value4) throws IOException {
      this.setParameter(name1, value1);
      return this.post(name2, value2, name3, value3, name4, value4);
   }

   public static InputStream post(URL url, Map parameters) throws IOException {
      return (new ClientHttpRequest(url)).post(parameters);
   }

   public static InputStream post(URL url, Object[] parameters) throws IOException {
      return (new ClientHttpRequest(url)).post(parameters);
   }

   public static InputStream post(URL url, Map cookies, Map parameters) throws IOException {
      return (new ClientHttpRequest(url)).post(cookies, parameters);
   }

   public static InputStream post(URL url, String[] cookies, Object[] parameters) throws IOException {
      return (new ClientHttpRequest(url)).post(cookies, parameters);
   }

   public static InputStream post(URL url, String name1, Object value1) throws IOException {
      return (new ClientHttpRequest(url)).post(name1, value1);
   }

   public static InputStream post(URL url, String name1, Object value1, String name2, Object value2) throws IOException {
      return (new ClientHttpRequest(url)).post(name1, value1, name2, value2);
   }

   public static InputStream post(URL url, String name1, Object value1, String name2, Object value2, String name3, Object value3) throws IOException {
      return (new ClientHttpRequest(url)).post(name1, value1, name2, value2, name3, value3);
   }

   public static InputStream post(URL url, String name1, Object value1, String name2, Object value2, String name3, Object value3, String name4, Object value4) throws IOException {
      return (new ClientHttpRequest(url)).post(name1, value1, name2, value2, name3, value3, name4, value4);
   }
}
