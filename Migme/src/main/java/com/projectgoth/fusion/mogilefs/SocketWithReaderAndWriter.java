package com.projectgoth.fusion.mogilefs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;

class SocketWithReaderAndWriter {
   private Socket socket;
   private BufferedReader reader;
   private Writer writer;

   public SocketWithReaderAndWriter(Socket socket) throws IOException {
      this.socket = socket;
      this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      this.writer = new OutputStreamWriter(socket.getOutputStream());
   }

   public BufferedReader getReader() {
      return this.reader;
   }

   public Socket getSocket() {
      return this.socket;
   }

   public Writer getWriter() {
      return this.writer;
   }

   public String getTracker() {
      return this.socket.getInetAddress().getHostName();
   }
}
