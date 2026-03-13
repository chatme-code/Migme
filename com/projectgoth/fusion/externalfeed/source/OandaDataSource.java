package com.projectgoth.fusion.externalfeed.source;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.data.CurrencyData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.MIS;
import com.projectgoth.fusion.interfaces.MISHome;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class OandaDataSource implements ExchangeRateFeedDataSource {
   private static String PAYLOAD_PATTERN = "?fxmlrequest=<convert><client_id>%s</client_id><expr>%s</expr><exch>%s</exch></convert>";
   private static String BASE_CURRENCY = "AUD";
   private static String SUPPORTED_CURRENCIES = "";
   private static String FILE_PREFIX = "oanda";
   static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(OandaDataSource.class));

   public Map<String, CurrencyData> getCurrencies(Document xmlDocument) {
      Map<String, CurrencyData> currencies = new HashMap();
      HashMap oldCurrencyData = new HashMap();

      try {
         MIS misEJB = (MIS)EJBHomeCache.getObject("ejb/MIS", MISHome.class);
         List<CurrencyData> oldCurrencies = misEJB.getCurrencies();
         Iterator i$ = oldCurrencies.iterator();

         while(i$.hasNext()) {
            CurrencyData c = (CurrencyData)i$.next();
            oldCurrencyData.put(c.code, c);
         }
      } catch (Exception var13) {
         log.error("Unable to obtain MISEJB " + var13.getMessage(), var13);
         return currencies;
      }

      NodeList nodeList = xmlDocument.getElementsByTagName("EXPR");
      SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
      if (nodeList != null) {
         for(Node node = nodeList.item(0); node != null; node = node.getNextSibling()) {
            if (node.getNodeType() == 1) {
               CurrencyData currencyData = new CurrencyData();
               currencyData.code = node.getTextContent();
               log.debug("Found currency [" + currencyData.code + "]");

               while(!"CONVERSION".equals(node.getNodeName())) {
                  node = node.getNextSibling();
                  if ("EXPR".equals(node.getNodeName())) {
                     log.error("Conversion rate for [" + currencyData.code + "] not found. Possible Invalid XML Format. Terminating");
                     return new HashMap();
                  }
               }

               for(Node childNode = node.getFirstChild(); childNode != null; childNode = childNode.getNextSibling()) {
                  if ("DATE".equals(childNode.getNodeName())) {
                     try {
                        currencyData.lastUpdated = dateFormat.parse(childNode.getTextContent());
                        log.debug("Found lastUpdated [" + currencyData.lastUpdated + "]");
                     } catch (Exception var12) {
                        log.warn(" Unable to parse currency lastupated date: [" + childNode.getTextContent() + "] using today's date instead.");
                        currencyData.lastUpdated = new Date();
                     }
                  } else if ("BID".equals(childNode.getNodeName())) {
                     try {
                        currencyData.exchangeRate = Double.valueOf(childNode.getTextContent());
                        log.debug("Found exchange rate [" + currencyData.exchangeRate + "]");
                     } catch (NumberFormatException var11) {
                        log.error("Invalid number format [" + childNode.getTextContent() + "] for currency [" + currencyData.code + "]. Skipping this currency");
                        currencyData.code = null;
                     }
                  }
               }

               if (currencyData.code != null) {
                  currencyData.name = "";
                  CurrencyData oldData = (CurrencyData)oldCurrencyData.get(currencyData.code);
                  if (oldData != null) {
                     currencyData.name = oldData.name;
                  }

                  log.debug("Successfully retrieved data for [" + currencyData.code + "]");
                  currencies.put(currencyData.code, currencyData);
               }
            }
         }
      }

      return currencies;
   }

   public String getSourceURL() {
      String clientId = SystemProperty.get("OandaClientId", "oandatest");
      String SOURCE_URL = SystemProperty.get("OandaFeedURL", "http://web-services.oanda.com/cgi-bin/fxml/fxml");
      return SOURCE_URL + String.format(PAYLOAD_PATTERN, clientId, SUPPORTED_CURRENCIES, BASE_CURRENCY);
   }

   public String getFilePrefix() {
      return FILE_PREFIX;
   }
}
