package com.projectgoth.fusion.externalfeed.source;

import com.projectgoth.fusion.data.CurrencyData;
import java.util.Map;
import org.w3c.dom.Document;

public interface ExchangeRateFeedDataSource {
   Map<String, CurrencyData> getCurrencies(Document var1);

   String getSourceURL();

   String getFilePrefix();
}
