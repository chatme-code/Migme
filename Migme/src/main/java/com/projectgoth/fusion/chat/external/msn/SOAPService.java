package com.projectgoth.fusion.chat.external.msn;

import com.projectgoth.fusion.common.SimpleXMLParser;
import com.projectgoth.fusion.common.StringUtil;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

public class SOAPService {
   private static final String PASSPORT_SERVER = "https://loginnet.passport.com/RST.srf";

   public static String getTweenerKey(String username, String password, String challenge) throws MSNException {
      try {
         String url = "https://loginnet.passport.com/RST.srf";
         String requestXML = getTweenerXML(username, password, challenge);

         do {
            SimpleXMLParser parser = new SimpleXMLParser(url, requestXML);
            String key = parser.getTagValue("wsse:BinarySecurityToken");
            if (key != null) {
               return key;
            }

            url = parser.getTagValue("psf:redirectUrl");
         } while(url != null);

         throw new MSNException("Incorrect MSN username or password");
      } catch (SAXException var7) {
         throw new MSNException("Unable to retrieve authentication key - SAXException");
      } catch (ParserConfigurationException var8) {
         throw new MSNException("Unable to retrieve authentication key - ParserConfigurationException");
      } catch (IOException var9) {
         throw new MSNException("Unable to retrieve authentication key - " + var9.getMessage());
      }
   }

   private static String getTweenerXML(String username, String password, String challenge) throws UnsupportedEncodingException {
      if (!StringUtil.isBlank(challenge)) {
         challenge = URLDecoder.decode(challenge, "UTF-8");
         challenge = challenge.replaceAll(",", "&");
         challenge = challenge.replaceAll("&", "&amp;");
      }

      StringBuilder builder = new StringBuilder();
      builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
      builder.append("<Envelope xmlns=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:wsse=\"http://schemas.xmlsoap.org/ws/2003/06/secext\" xmlns:saml=\"urn:oasis:names:tc:SAML:1.0:assertion\" xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2002/12/policy\" xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\" xmlns:wsa=\"http://schemas.xmlsoap.org/ws/2004/03/addressing\" xmlns:wssc=\"http://schemas.xmlsoap.org/ws/2004/04/sc\" xmlns:wst=\"http://schemas.xmlsoap.org/ws/2004/04/trust\">");
      builder.append("<Header>");
      builder.append("<wsse:Security>");
      builder.append("<wsse:UsernameToken Id=\"user\">");
      builder.append("<wsse:Username>").append(username).append("</wsse:Username>");
      builder.append("<wsse:Password>").append(password).append("</wsse:Password>");
      builder.append("</wsse:UsernameToken>");
      builder.append("</wsse:Security>");
      builder.append("</Header>");
      builder.append("<Body>");
      builder.append("<ps:RequestMultipleSecurityTokens xmlns:ps=\"http://schemas.microsoft.com/Passport/SoapServices/PPCRL\" Id=\"RSTS\">");
      builder.append("<wst:RequestSecurityToken Id=\"RST1\">");
      builder.append("<wst:RequestType>http://schemas.xmlsoap.org/ws/2004/04/security/trust/Issue</wst:RequestType>");
      builder.append("<wsp:AppliesTo>");
      builder.append("<wsa:EndpointReference>");
      builder.append("<wsa:Address>messenger.msn.com</wsa:Address>");
      builder.append("</wsa:EndpointReference>");
      builder.append("</wsp:AppliesTo>");
      builder.append("<wsse:PolicyReference URI=\"?").append(challenge).append("\"></wsse:PolicyReference>");
      builder.append("</wst:RequestSecurityToken>");
      builder.append("</ps:RequestMultipleSecurityTokens>");
      builder.append("</Body>");
      builder.append("</Envelope>");
      return builder.toString();
   }
}
