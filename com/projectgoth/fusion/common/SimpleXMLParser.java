package com.projectgoth.fusion.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class SimpleXMLParser {
   private static final String DEFAULT_CHATSET = "UTF-8";
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(SimpleXMLParser.class));
   private Document document;

   public SimpleXMLParser(InputStream in) throws SAXException, IOException, ParserConfigurationException {
      this.document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
   }

   public SimpleXMLParser(String server, String request) throws SAXException, IOException, ParserConfigurationException {
      URL url = new URL(server);
      HttpURLConnection httpConn = (HttpURLConnection)url.openConnection();
      this.processRequest(httpConn, request);
   }

   public SimpleXMLParser(String server, String request, boolean strictSSLCheck) throws SAXException, IOException, ParserConfigurationException, Exception {
      if (strictSSLCheck) {
         URL url = new URL(server);
         HttpURLConnection httpConn = (HttpURLConnection)url.openConnection();
         this.processRequest(httpConn, request);
      } else {
         this.nonStrictXMLPost(server, request);
      }

   }

   private void nonStrictXMLPost(String serverName, String postData) throws Exception {
      DefaultHttpClient client = new DefaultHttpClient();
      TrustModifier.relaxHostChecking(client);
      HttpPost httppost = new HttpPost(serverName);
      httppost.addHeader("Content-Type", "application/xml");
      httppost.setEntity(new StringEntity(postData, "UTF-8"));
      HttpResponse response = null;

      try {
         response = client.execute(httppost);
      } catch (Exception var9) {
         log.error(var9.getMessage());
      }

      if (null != response) {
         HttpEntity entity = response.getEntity();
         String xmlStr = EntityUtils.toString(entity);
         InputSource is = new InputSource(new StringReader(xmlStr));
         this.document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
      }

   }

   private void processRequest(HttpURLConnection httpConn, String request) throws IOException, ParserConfigurationException, SAXException {
      httpConn.setDoOutput(true);
      httpConn.setUseCaches(false);
      httpConn.getOutputStream().write(request.getBytes("UTF-8"));
      if (httpConn.getResponseCode() != 200) {
         throw new IOException("HTTP " + httpConn.getResponseCode() + " " + httpConn.getResponseMessage());
      } else {
         this.document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(httpConn.getInputStream());
      }
   }

   public boolean containsTag(String tagName) {
      NodeList nodes = this.document.getElementsByTagName(tagName);
      return nodes != null && nodes.getLength() > 0;
   }

   public String getTagValue(String tagName) {
      NodeList nodes = this.document.getElementsByTagName(tagName);
      if (nodes != null && nodes.getLength() != 0) {
         Node node = nodes.item(0).getFirstChild();
         return node == null ? null : node.getNodeValue();
      } else {
         return null;
      }
   }

   public String getTagValue(String parentTagName, String tagName) {
      NodeList nodes = this.document.getElementsByTagName(parentTagName);
      if (nodes != null && nodes.getLength() != 0) {
         for(Node node = nodes.item(0).getFirstChild(); node != null; node = node.getNextSibling()) {
            if (node.getNodeName().equalsIgnoreCase(tagName)) {
               return node.getFirstChild().getNodeValue();
            }
         }

         return null;
      } else {
         return null;
      }
   }

   public String toString() {
      try {
         StringWriter writer = new StringWriter();
         Transformer t = TransformerFactory.newInstance().newTransformer();
         t.setOutputProperty("indent", "yes");
         t.transform(new DOMSource(this.document), new StreamResult(writer));
         return writer.toString();
      } catch (Exception var3) {
         return this.document.toString();
      }
   }
}
