package com.projectgoth.fusion.chat.external.facebook;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import javax.security.sasl.Sasl;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.sasl.SASLMechanism;
import org.jivesoftware.smack.util.Base64;

public class FacebookConnectSASLMechanism extends SASLMechanism {
   private String accessToken = "";
   private String apiKey = "";

   public FacebookConnectSASLMechanism(SASLAuthentication saslAuthentication) {
      super(saslAuthentication);
   }

   protected void authenticate() throws IOException, XMPPException {
      StringBuilder stanza = new StringBuilder();
      stanza.append("<auth mechanism=\"").append(this.getName());
      stanza.append("\" xmlns=\"urn:ietf:params:xml:ns:xmpp-sasl\">");
      stanza.append("</auth>");
      this.getSASLAuthentication().send(stanza.toString());
   }

   public void authenticate(String apiKey, String host, String accessToken) throws IOException, XMPPException {
      if (accessToken != null && apiKey != null) {
         this.accessToken = accessToken;
         this.apiKey = apiKey;
         this.hostname = host;
         String[] mechanisms = new String[]{"DIGEST-MD5"};
         Map<String, String> props = new HashMap();
         this.sc = Sasl.createSaslClient(mechanisms, (String)null, "xmpp", host, props, this);
         this.authenticate();
      } else {
         throw new IllegalStateException("Invalid parameters!");
      }
   }

   protected String getName() {
      return "X-FACEBOOK-PLATFORM";
   }

   public void challengeReceived(String challenge) throws IOException {
      StringBuilder stanza = new StringBuilder();
      byte[] response = null;
      String authenticationText;
      if (challenge != null) {
         authenticationText = new String(Base64.decode(challenge));
         Map<String, String> parameters = this.getQueryMap(authenticationText);
         String version = (String)parameters.get("version");
         String nonce = (String)parameters.get("nonce");
         String method = (String)parameters.get("method");
         Long callId = (new GregorianCalendar()).getTimeInMillis() / 1000L;
         String composedResponse = "api_key=" + URLEncoder.encode(this.apiKey, "utf-8") + "&" + "&call_id=" + callId + "&method=" + URLEncoder.encode(method, "utf-8") + "&nonce=" + URLEncoder.encode(nonce, "utf-8") + "&access_token=" + URLEncoder.encode(this.accessToken, "utf-8") + "&v=" + URLEncoder.encode(version, "utf-8");
         response = composedResponse.getBytes("utf-8");
      }

      authenticationText = "";
      if (response != null) {
         authenticationText = Base64.encodeBytes(response, 8);
      }

      stanza.append("<response xmlns=\"urn:ietf:params:xml:ns:xmpp-sasl\">");
      stanza.append(authenticationText);
      stanza.append("</response>");
      this.getSASLAuthentication().send(stanza.toString());
   }

   private Map<String, String> getQueryMap(String query) {
      Map<String, String> map = new HashMap();
      String[] params = query.split("\\&");
      String[] arr$ = params;
      int len$ = params.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         String param = arr$[i$];
         String[] fields = param.split("=", 2);
         map.put(fields[0], fields.length > 1 ? fields[1] : null);
      }

      return map;
   }
}
