package com.projectgoth.fusion.externalfeed.source;

import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.data.CurrencyData;
import java.util.HashMap;
import java.util.Map;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XEDataSource implements ExchangeRateFeedDataSource {
   private static String ELEMENT_TAG = "currency";
   private static String FILE_PREFIX = "xe";

   public Map<String, CurrencyData> getCurrencies(Document xmlDocument) {
      Map<String, CurrencyData> currencies = new HashMap();
      NodeList nodeList = xmlDocument.getElementsByTagName(ELEMENT_TAG);
      if (nodeList != null) {
         for(Node node = nodeList.item(0); node != null; node = node.getNextSibling()) {
            if (node.getNodeType() == 1) {
               CurrencyData currencyData = new CurrencyData();

               for(Node childNode = node.getFirstChild(); childNode != null; childNode = childNode.getNextSibling()) {
                  String name = childNode.getNodeName();
                  String content = childNode.getTextContent();
                  if ("csymbol".equals(name)) {
                     currencyData.code = content;
                  } else if ("cname".equals(name)) {
                     currencyData.name = content;
                  } else if ("crate".equals(name)) {
                     currencyData.exchangeRate = Double.valueOf(content);
                  }
               }

               if (currencyData.code != null) {
                  currencies.put(currencyData.code, currencyData);
               }
            }
         }
      }

      return currencies;
   }

   public String getSourceURL() {
      return SystemProperty.get("XeDataFeedURL", "http://www.xe.com/dfs/sample-usd.xml");
   }

   public String getFilePrefix() {
      return FILE_PREFIX;
   }
}
