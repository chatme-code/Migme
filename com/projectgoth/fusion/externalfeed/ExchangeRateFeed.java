package com.projectgoth.fusion.externalfeed;

import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.data.CurrencyData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.externalfeed.source.ExchangeRateFeedDataSource;
import com.projectgoth.fusion.externalfeed.source.ExchangeRateFeedDataSourceFactory;
import com.projectgoth.fusion.interfaces.MIS;
import com.projectgoth.fusion.interfaces.MISHome;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class ExchangeRateFeed extends TimerTask {
   private static final String APP_NAME = "ExchangeRateFeed";
   public static Logger logger = Logger.getLogger("ExchangeRateFeed");
   private DocumentBuilderFactory xmlFactory = DocumentBuilderFactory.newInstance();
   private Map<String, CurrencyData> currencies = new HashMap();
   public static String FILE_DIRECTORY = "feeds";
   private String localCurrency;
   private Calendar rollOverTime;
   private ExchangeRateFeedDataSource dataSource;
   private boolean simulationMode;

   public ExchangeRateFeed(String localCurrency, Calendar rollOverTime, ExchangeRateFeedDataSource dataSource, boolean simulationMode) {
      this.localCurrency = localCurrency.toUpperCase();
      this.rollOverTime = rollOverTime;
      this.dataSource = dataSource;
      this.simulationMode = simulationMode;
   }

   public Map<String, CurrencyData> getCurrencies() {
      return this.currencies;
   }

   public void run() {
      try {
         this.currencies.clear();
         Document xmlDocument = this.getDocument();
         if (xmlDocument != null) {
            this.currencies = this.dataSource.getCurrencies(xmlDocument);
         }

         if (!this.currencies.containsKey(this.localCurrency)) {
            throw new Exception("Invalid local currency " + this.localCurrency);
         }

         Double localRate = ((CurrencyData)this.currencies.get(this.localCurrency)).exchangeRate;
         if (localRate == null || localRate == 0.0D) {
            throw new Exception("Invalid exchange rate for local currency " + this.localCurrency + " - " + localRate);
         }

         if (!this.simulationMode) {
            MIS misEJB = (MIS)EJBHomeCache.getObject("ejb/MIS", MISHome.class);
            Iterator i$ = this.currencies.values().iterator();

            while(i$.hasNext()) {
               CurrencyData currencyData = (CurrencyData)i$.next();

               try {
                  currencyData.exchangeRate = currencyData.exchangeRate / localRate;
                  logger.debug(currencyData.code + " " + currencyData.name + " " + currencyData.exchangeRate);
                  misEJB.setCurrency(currencyData);
               } catch (Exception var7) {
                  logger.error("Unable to update currency " + currencyData.name, var7);
               }
            }
         }
      } catch (Exception var8) {
         logger.error("Exception occured in run() method", var8);
      }

   }

   public Document getDocument() throws SAXException, IOException, ParserConfigurationException, TransformerFactoryConfigurationError, TransformerException {
      Calendar now = Calendar.getInstance();
      if (now.before(this.rollOverTime)) {
         now = (Calendar)this.rollOverTime.clone();
         now.add(10, -24);
      } else {
         this.rollOverTime.add(10, 24);
      }

      File dir = new File(FILE_DIRECTORY);
      if (!dir.exists()) {
         dir.mkdir();
      }

      String fileURL = FILE_DIRECTORY + File.separator + this.dataSource.getFilePrefix() + (new SimpleDateFormat("ddMMyyyy")).format(now.getTime()) + ".xml";

      Document xmlDocument;
      try {
         xmlDocument = this.xmlFactory.newDocumentBuilder().parse(fileURL);
         logger.info("Successfully retrieved XML from " + fileURL);
      } catch (FileNotFoundException var8) {
         xmlDocument = this.xmlFactory.newDocumentBuilder().parse(this.dataSource.getSourceURL());
         logger.info("Successfully retrieved XML from " + this.dataSource.getSourceURL());
         Source source = new DOMSource(xmlDocument);
         Result result = new StreamResult(new File(fileURL));
         TransformerFactory.newInstance().newTransformer().transform(source, result);
      }

      return xmlDocument;
   }

   public static void main(String[] args) throws Exception {
      logger.info("ExchangeRateFeed version @version@");
      logger.info("Copyright (c) 2005-2008 Project Goth Inc. All rights reserved");
      if (args.length < 1) {
         logger.fatal("Usage: ExchangeRateFeed localCurrency [time] [interval] [optional: simulation] [optional: datasource, only available if simulation is enabled]");
      }

      String localCurrency = args[0];
      Integer time = null;
      Integer interval = null;
      boolean simulationMode = false;
      String dataSourceName = null;

      for(int i = 1; i < args.length; ++i) {
         if (i == 1) {
            try {
               time = Integer.parseInt(args[1]);
               if (time < 0 || time > 2359) {
                  logger.fatal("Time must be between 0 and 2359");
                  System.exit(0);
               }
            } catch (NumberFormatException var11) {
               logger.fatal("time argument must be an integer");
               System.exit(0);
            }
         } else if (i == 2) {
            try {
               interval = Integer.parseInt(args[2]);
            } catch (NumberFormatException var10) {
               logger.fatal("interval argument must be an integer");
               System.exit(0);
            }
         } else if (i == 3) {
            simulationMode = "simulation".equals(args[3]);
         } else if (i == 4) {
            dataSourceName = args[4];
         }
      }

      Calendar rollOverTime = Calendar.getInstance();
      rollOverTime.set(11, time == null ? 0 : time / 100);
      rollOverTime.set(12, time == null ? 0 : time % 100);
      rollOverTime.set(13, 0);
      if (rollOverTime.before(Calendar.getInstance())) {
         rollOverTime.add(10, 24);
      }

      ExchangeRateFeedDataSource dataSource;
      if (StringUtil.isBlank(dataSourceName)) {
         dataSource = ExchangeRateFeedDataSourceFactory.getDataSource();
      } else {
         dataSource = ExchangeRateFeedDataSourceFactory.getDataSource(ExchangeRateFeedDataSourceFactory.DataSourceType.fromName(dataSourceName));
         if (dataSource == null) {
            throw new Exception("Invalid datasource specified  [" + dataSourceName + "]");
         }
      }

      ExchangeRateFeed feed = new ExchangeRateFeed(localCurrency, rollOverTime, dataSource, simulationMode);
      feed.run();
      if (time != null && !simulationMode) {
         SimpleDateFormat df = new SimpleDateFormat("EEEE d MMM yyyy h:mm a");
         if (interval == null) {
            logger.info("Next update on " + df.format(rollOverTime.getTime()));
            (new Timer()).schedule(feed, rollOverTime.getTime());
         } else {
            logger.info("Next update on " + df.format(rollOverTime.getTime()) + " (every " + interval + " minute(s))");
            (new Timer()).scheduleAtFixedRate(feed, rollOverTime.getTime(), (long)(interval * '\uea60'));
         }
      }

   }
}
