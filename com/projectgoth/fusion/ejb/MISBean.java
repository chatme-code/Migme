package com.projectgoth.fusion.ejb;

import com.projectgoth.fusion.accesscontrol.AuthenticatedAccessControlData;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.Enums;
import com.projectgoth.fusion.common.LazyLoader;
import com.projectgoth.fusion.common.MemCachedClientWrapper;
import com.projectgoth.fusion.common.MemCachedKeySpaces;
import com.projectgoth.fusion.common.MemCachedKeyUtils;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.AccountEntrySourceData;
import com.projectgoth.fusion.data.AlertMessageData;
import com.projectgoth.fusion.data.ApplicationMenuOptionData;
import com.projectgoth.fusion.data.CashReceiptData;
import com.projectgoth.fusion.data.CountryData;
import com.projectgoth.fusion.data.CurrencyData;
import com.projectgoth.fusion.data.FileData;
import com.projectgoth.fusion.data.HandsetData;
import com.projectgoth.fusion.data.HandsetInstructionsData;
import com.projectgoth.fusion.data.HandsetVendorPrefixesData;
import com.projectgoth.fusion.data.MenuData;
import com.projectgoth.fusion.data.PromotedPostData;
import com.projectgoth.fusion.data.ReputationLevelData;
import com.projectgoth.fusion.data.ResellerData;
import com.projectgoth.fusion.data.ScrapbookData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.ejb.util.LookupUtil;
import com.projectgoth.fusion.emote.EmoteCommandData;
import com.projectgoth.fusion.interfaces.AccountLocal;
import com.projectgoth.fusion.interfaces.AccountLocalHome;
import com.projectgoth.fusion.interfaces.User;
import com.projectgoth.fusion.interfaces.UserHome;
import com.projectgoth.fusion.interfaces.UserLocal;
import com.projectgoth.fusion.interfaces.UserLocalHome;
import com.projectgoth.fusion.rewardsystem.RewardCentre;
import com.projectgoth.fusion.rewardsystem.triggers.PhotoUploadTrigger;
import com.projectgoth.fusion.slice.EmailUserNotification;
import com.projectgoth.hashtag.HashTagAPI;
import com.projectgoth.hashtag.data.HashTagData;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.sql.DataSource;
import org.apache.log4j.Logger;

public class MISBean implements SessionBean {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(MISBean.class));
   private DataSource dataSourceMaster;
   private DataSource dataSourceSlave;
   private static List<CountryData> countries;
   private static List<CurrencyData> currencies;
   private static List<MenuData> menus;
   private static Map<Integer, String> helpTexts;
   private static Map<Integer, String> infoTexts;
   private HashTagAPI hashTagAPI;
   private static long countriesNextUpdate = -1L;
   private static long currenciesNextUpdate = -1L;
   private static long menusNextUpdate = -1L;
   private static long clientTextsNextUpdate = -1L;
   private static final Object countriesLock = new Object();
   private static final Object currenciesLock = new Object();
   private static final Object clientTextsLock = new Object();
   private static final Object menusLock = new Object();
   private SessionContext context;

   public void setSessionContext(SessionContext newContext) throws EJBException {
      this.context = newContext;
   }

   public void ejbRemove() throws EJBException, RemoteException {
   }

   public void ejbActivate() throws EJBException, RemoteException {
   }

   public void ejbPassivate() throws EJBException, RemoteException {
   }

   public void ejbCreate() throws CreateException {
      try {
         this.dataSourceMaster = LookupUtil.getFusionMasterDataSource();
         this.dataSourceSlave = LookupUtil.getFusionSlaveDataSource();
         SystemProperty.ejbInit(this.dataSourceSlave);
         this.hashTagAPI = new HashTagAPI();
      } catch (Exception var2) {
         log.error("Unable to create MIS EJB", var2);
         throw new CreateException("Unable to create MIS EJB: " + var2.getMessage());
      }
   }

   public List<ApplicationMenuOptionData> getApplicationMenuOptions(int menuVersionId) throws EJBException {
      String key = String.valueOf(menuVersionId);
      List<ApplicationMenuOptionData> menuOptionList = (List)MemCachedClientWrapper.get(MemCachedKeySpaces.CommonKeySpace.APPMENU_OPTIONS, key);
      if (menuOptionList == null) {
         Connection connSlave = null;
         PreparedStatement ps = null;
         ResultSet rs = null;

         try {
            menuOptionList = new LinkedList();
            connSlave = this.dataSourceSlave.getConnection();
            String sql = "select * from appmenuoption where id=?";
            ps = connSlave.prepareStatement(sql);
            ps.setInt(1, menuVersionId);
            rs = ps.executeQuery();

            while(rs.next()) {
               ((List)menuOptionList).add(new ApplicationMenuOptionData(rs));
            }

            MemCachedClientWrapper.set(MemCachedKeySpaces.CommonKeySpace.APPMENU_OPTIONS, key, menuOptionList);
         } catch (SQLException var22) {
            throw new EJBException(var22.getMessage());
         } finally {
            try {
               if (rs != null) {
                  rs.close();
               }
            } catch (SQLException var21) {
               rs = null;
            }

            try {
               if (ps != null) {
                  ps.close();
               }
            } catch (SQLException var20) {
               ps = null;
            }

            try {
               if (connSlave != null) {
                  connSlave.close();
               }
            } catch (SQLException var19) {
               connSlave = null;
            }

         }
      }

      return (List)menuOptionList;
   }

   public int getApplicationMenuVersion(int clientType, short clientVersion, String vasTrackingId) throws EJBException {
      if (vasTrackingId == null) {
         vasTrackingId = "";
      }

      String key = MemCachedKeyUtils.getFullKeyFromStrings(String.valueOf(clientType), String.valueOf(clientVersion), vasTrackingId);
      Integer versionId = MemCachedClientWrapper.getInt(MemCachedKeySpaces.CommonKeySpace.APPMENU, key);
      if (versionId == null) {
         Connection connSlave = null;
         PreparedStatement ps = null;
         ResultSet rs = null;

         try {
            connSlave = this.dataSourceSlave.getConnection();
            String sql = "select max(id) as id from appmenu where clientType=? and minVersion<=? and maxVersion>=? and vasTrackingId=? and status=?";
            ps = connSlave.prepareStatement(sql);
            ps.setInt(1, clientType);
            ps.setShort(2, clientVersion);
            ps.setShort(3, clientVersion);
            ps.setString(4, vasTrackingId);
            ps.setInt(5, 1);
            rs = ps.executeQuery();
            if (rs.next()) {
               versionId = rs.getInt("id");
               MemCachedClientWrapper.set(MemCachedKeySpaces.CommonKeySpace.APPMENU, key, versionId);
            } else {
               versionId = 0;
            }
         } catch (SQLException var24) {
            throw new EJBException(var24.getMessage());
         } finally {
            try {
               if (rs != null) {
                  rs.close();
               }
            } catch (SQLException var23) {
               rs = null;
            }

            try {
               if (ps != null) {
                  ps.close();
               }
            } catch (SQLException var22) {
               ps = null;
            }

            try {
               if (connSlave != null) {
                  connSlave.close();
               }
            } catch (SQLException var21) {
               connSlave = null;
            }

         }
      }

      return versionId;
   }

   private void loadClientText() throws EJBException {
      Connection conn = null;
      PreparedStatement stmt = null;
      ResultSet rs = null;
      synchronized(clientTextsLock) {
         if (helpTexts == null || infoTexts == null || helpTexts.isEmpty() || infoTexts.isEmpty() || clientTextsNextUpdate <= System.currentTimeMillis()) {
            if (helpTexts == null) {
               helpTexts = new HashMap();
            }

            if (infoTexts == null) {
               infoTexts = new HashMap();
            }

            infoTexts.clear();
            helpTexts.clear();

            try {
               conn = this.dataSourceSlave.getConnection();
               stmt = conn.prepareStatement("select * from clienttext");
               rs = stmt.executeQuery();

               while(rs.next()) {
                  int type = rs.getInt("Type");
                  if (type == 1) {
                     helpTexts.put(rs.getInt("ID"), rs.getString("Text"));
                  } else if (type == 2) {
                     infoTexts.put(rs.getInt("ID"), rs.getString("Text"));
                  }
               }

               clientTextsNextUpdate = System.currentTimeMillis() + SystemProperty.getLong((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.EJBCacheDuration.MIS_CLIENT_TEXT) * 1000L;
            } catch (SQLException var22) {
               throw new EJBException("Unable to load client text: " + var22.getMessage());
            } finally {
               try {
                  if (rs != null) {
                     rs.close();
                  }
               } catch (SQLException var21) {
                  rs = null;
               }

               try {
                  if (stmt != null) {
                     stmt.close();
                  }
               } catch (SQLException var20) {
                  stmt = null;
               }

               try {
                  if (conn != null) {
                     conn.close();
                  }
               } catch (SQLException var19) {
                  conn = null;
               }

            }
         }

      }
   }

   private List<CountryData> getCountriesList() throws EJBException {
      Connection conn = null;
      PreparedStatement stmt = null;
      ResultSet rs = null;
      synchronized(countriesLock) {
         if (countries == null || countries.isEmpty() || countriesNextUpdate <= System.currentTimeMillis()) {
            if (countries == null) {
               countries = new LinkedList();
            } else {
               countries.clear();
            }

            try {
               conn = this.dataSourceSlave.getConnection();
               stmt = conn.prepareStatement("select * from country order by name");
               rs = stmt.executeQuery();

               while(rs.next()) {
                  countries.add(new CountryData(rs));
               }

               if (countries.size() == 0) {
                  throw new EJBException("Unable to load country details. No records found");
               }

               countriesNextUpdate = System.currentTimeMillis() + SystemProperty.getLong((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.EJBCacheDuration.MIS_COUNTRIES) * 1000L;
            } catch (SQLException var22) {
               throw new EJBException("Unable to load country details: " + var22.getMessage());
            } finally {
               try {
                  if (rs != null) {
                     rs.close();
                  }
               } catch (SQLException var21) {
                  rs = null;
               }

               try {
                  if (stmt != null) {
                     stmt.close();
                  }
               } catch (SQLException var20) {
                  stmt = null;
               }

               try {
                  if (conn != null) {
                     conn.close();
                  }
               } catch (SQLException var19) {
                  conn = null;
               }

            }
         }
      }

      return countries;
   }

   private List<CurrencyData> getCurrencyList() throws EJBException {
      Connection conn = null;
      PreparedStatement stmt = null;
      ResultSet rs = null;
      synchronized(currenciesLock) {
         if (currencies == null || currencies.isEmpty() || currenciesNextUpdate <= System.currentTimeMillis()) {
            if (currencies == null) {
               currencies = new LinkedList();
            } else {
               currencies.clear();
            }

            try {
               conn = this.dataSourceSlave.getConnection();
               stmt = conn.prepareStatement("select * from currency order by Code");
               rs = stmt.executeQuery();

               while(rs.next()) {
                  currencies.add(new CurrencyData(rs));
               }

               if (currencies.size() == 0) {
                  throw new EJBException("Unable to load currency details. No records found");
               }

               currenciesNextUpdate = System.currentTimeMillis() + SystemProperty.getLong((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.EJBCacheDuration.MIS_CURRENCIES) * 1000L;
            } catch (SQLException var22) {
               throw new EJBException("Unable to load currency details: " + var22.getMessage());
            } finally {
               try {
                  if (rs != null) {
                     rs.close();
                  }
               } catch (SQLException var21) {
                  rs = null;
               }

               try {
                  if (stmt != null) {
                     stmt.close();
                  }
               } catch (SQLException var20) {
                  stmt = null;
               }

               try {
                  if (conn != null) {
                     conn.close();
                  }
               } catch (SQLException var19) {
                  conn = null;
               }

            }
         }
      }

      return currencies;
   }

   private List<MenuData> getMenuList() throws EJBException {
      Connection conn = null;
      PreparedStatement stmt = null;
      ResultSet rs = null;
      synchronized(menusLock) {
         if (menus == null || menus.isEmpty() || menusNextUpdate <= System.currentTimeMillis()) {
            if (menus == null) {
               menus = new LinkedList();
            } else {
               menus.clear();
            }

            try {
               conn = this.dataSourceSlave.getConnection();
               stmt = conn.prepareStatement("select * from menu");
               rs = stmt.executeQuery();

               while(rs.next()) {
                  menus.add(new MenuData(rs));
               }

               if (menus.size() == 0) {
                  throw new EJBException("Unable to load menu items. No records found");
               }

               menusNextUpdate = System.currentTimeMillis() + SystemProperty.getLong((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.EJBCacheDuration.MIS_MIDLET_MENU_ITEMS) * 1000L;
            } catch (SQLException var22) {
               throw new EJBException("Unable to load menus: " + var22.getMessage());
            } finally {
               try {
                  if (rs != null) {
                     rs.close();
                  }
               } catch (SQLException var21) {
                  rs = null;
               }

               try {
                  if (stmt != null) {
                     stmt.close();
                  }
               } catch (SQLException var20) {
                  stmt = null;
               }

               try {
                  if (conn != null) {
                     conn.close();
                  }
               } catch (SQLException var19) {
                  conn = null;
               }

            }
         }
      }

      return menus;
   }

   public void resetCachedSystemProperties() {
      SystemProperty.resetCachedProperties();
   }

   public Map<String, String> getSystemProperties() throws EJBException {
      Connection connSlave = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      Object var5;
      try {
         Map<String, String> properties = (Map)MemCachedClientWrapper.get(MemCachedKeySpaces.CommonKeySpace.SYSTEM_PROPERTY, "");
         if (properties == null) {
            properties = new HashMap();
            connSlave = this.dataSourceSlave.getConnection();
            ps = connSlave.prepareStatement("select * from system");
            rs = ps.executeQuery();

            while(rs.next()) {
               ((Map)properties).put(rs.getString("propertyname"), rs.getString("propertyvalue"));
            }

            MemCachedClientWrapper.set(MemCachedKeySpaces.CommonKeySpace.SYSTEM_PROPERTY, "", properties);
         }

         var5 = properties;
      } catch (SQLException var20) {
         throw new EJBException("Unable to load system properties: " + var20.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var19) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var18) {
            ps = null;
         }

         try {
            if (connSlave != null) {
               connSlave.close();
            }
         } catch (SQLException var17) {
            connSlave = null;
         }

      }

      return (Map)var5;
   }

   public void updateSystemProperty(String propertyName, String value) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      if (StringUtil.isBlank(propertyName)) {
         throw new EJBException("Null system property provided");
      } else if (value == null) {
         throw new EJBException("Null system property value provided");
      } else {
         propertyName = propertyName.trim();
         value = value.trim();

         try {
            conn = this.dataSourceMaster.getConnection();
            ps = conn.prepareStatement("insert into system (propertyname, propertyvalue) VALUES(?,?) on duplicate key update propertyvalue=?");
            ps.setString(1, propertyName);
            ps.setString(2, value);
            ps.setString(3, value);
            if (ps.executeUpdate() < 1) {
               throw new EJBException("Unable to update system property");
            }
         } catch (SQLException var21) {
            throw new EJBException(var21.getMessage());
         } finally {
            try {
               if (rs != null) {
                  ((ResultSet)rs).close();
               }
            } catch (SQLException var20) {
               rs = null;
            }

            try {
               if (ps != null) {
                  ps.close();
               }
            } catch (SQLException var19) {
               ps = null;
            }

            try {
               if (conn != null) {
                  conn.close();
               }
            } catch (SQLException var18) {
               conn = null;
            }

         }

      }
   }

   public String getHelpText(int helpID) throws EJBException {
      this.loadClientText();
      return (String)helpTexts.get(helpID);
   }

   public String getInfoText(int infoID) throws EJBException {
      this.loadClientText();
      return (String)infoTexts.get(infoID);
   }

   public List<CountryData> getCountries() throws EJBException {
      return this.getCountriesList();
   }

   private static List<CountryData> getCountriesSupportedHashtag(DataSource dataSource) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      ArrayList result = new ArrayList();

      try {
         conn = dataSource.getConnection();
         ps = conn.prepareStatement("select * from country c inner join countrysupportedhashtag csh on csh.countryid=c.id where enabled=1");
         rs = ps.executeQuery();

         while(rs.next()) {
            CountryData countryData = new CountryData(rs);
            result.add(countryData);
         }

         ArrayList var22 = result;
         return var22;
      } catch (Exception var20) {
         throw new EJBException(var20.getMessage(), var20);
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var19) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var18) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var17) {
            conn = null;
         }

      }
   }

   public List<CountryData> getCountriesSupportedHashtag() throws EJBException {
      return (List)MISBean.SingletonHolder.COUNTRIES_SUPPORTED_HASHTAG_LOADER.getValue();
   }

   public List<CurrencyData> getCurrencies() throws EJBException {
      return this.getCurrencyList();
   }

   public List<MenuData> getMenus(int countryID, short clientVersion, int clientType) throws EJBException {
      List<MenuData> list = new ArrayList();
      Iterator i$ = this.getMenuList().iterator();

      while(true) {
         MenuData menu;
         do {
            do {
               do {
                  do {
                     if (!i$.hasNext()) {
                        return list;
                     }

                     menu = (MenuData)i$.next();
                  } while(clientType != menu.clientType);
               } while(clientVersion < menu.minVersion);
            } while(clientVersion > menu.maxVersion);
         } while(menu.countryID != null && menu.countryID != countryID);

         list.add(menu);
      }
   }

   public MenuData getMenu(int menuId) throws EJBException {
      Iterator i$ = this.getMenuList().iterator();

      MenuData menu;
      do {
         if (!i$.hasNext()) {
            return null;
         }

         menu = (MenuData)i$.next();
      } while(menu.id != menuId);

      return menu;
   }

   public CountryData getCountry(int countryID) throws EJBException {
      Iterator i$ = this.getCountriesList().iterator();

      CountryData country;
      do {
         if (!i$.hasNext()) {
            return null;
         }

         country = (CountryData)i$.next();
      } while(country.id != countryID);

      return country;
   }

   public CountryData getCountryByISOCode(String isoCountryCode) throws EJBException {
      if (!StringUtil.isBlank(isoCountryCode)) {
         Iterator i$ = this.getCountriesList().iterator();

         while(i$.hasNext()) {
            CountryData country = (CountryData)i$.next();
            if (isoCountryCode.equalsIgnoreCase(country.isoCountryCode)) {
               return country;
            }
         }
      }

      return null;
   }

   public CountryData getCountryByLocation(int locationID) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      CountryData countryData;
      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("select * from country c left join location l on l.countryid=c.id where l.id=? ");
         ps.setInt(1, locationID);
         rs = ps.executeQuery();
         if (rs.next()) {
            countryData = new CountryData(rs);
            CountryData var6 = countryData;
            return var6;
         }

         countryData = null;
      } catch (Exception var24) {
         throw new EJBException(var24.getMessage(), var24);
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var23) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var22) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var21) {
            conn = null;
         }

      }

      return countryData;
   }

   public CountryData getCountryByIDDCode(int iddCode, String mobilePhone) throws EJBException {
      if (iddCode == 1) {
         return (mobilePhone.length() < 4 || "1204;1226;1250;1289;1306;1403;1416;1418;1438;1450;1506;1514;1519;1604;1613;1647;1705;1709;1778;1780;1807;1819;1867;1902;1905".indexOf(mobilePhone.substring(0, 4)) < 0) && (mobilePhone.length() < 7 || "1204131;1226131;1250131;1289131;1306131;1403131;1416131;1418131;1438131;1450131;1506131;1514131;1519131;1604131;1613131;1647131;1705131;1709131;1778131;1780131;1807131;1819131;1867131;1902131;1905131".indexOf(mobilePhone.substring(0, 7)) < 0) ? this.getCountry(231) : this.getCountry(40);
      } else if (iddCode != 7) {
         if (iddCode == 39) {
            return this.getCountry(112);
         } else if (iddCode == 269) {
            return this.getCountry(51);
         } else {
            Iterator i$ = this.getCountriesList().iterator();

            CountryData country;
            do {
               if (!i$.hasNext()) {
                  return null;
               }

               country = (CountryData)i$.next();
            } while(country.iddCode != iddCode);

            return country;
         }
      } else {
         return mobilePhone.length() >= 3 && "731;732".indexOf(mobilePhone.substring(0, 3)) >= 0 || mobilePhone.length() >= 4 && "7333;7570;7571;7573;7700;7701;7702;7705;7707;7777;7336".indexOf(mobilePhone.substring(0, 4)) >= 0 || mobilePhone.length() >= 5 && "73272;73172;73212".indexOf(mobilePhone.substring(0, 5)) >= 0 ? this.getCountry(116) : this.getCountry(185);
      }
   }

   public CountryData getCountryFromIPNumber(Double ipNumber) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      String isoCountryCode;
      Iterator i$;
      label174: {
         try {
            conn = this.dataSourceSlave.getConnection();
            ps = conn.prepareStatement("select isocountrycode from countrytoip where beginIPNum <= ? and endIPNum >= ?");
            ps.setDouble(1, ipNumber);
            ps.setDouble(2, ipNumber);
            rs = ps.executeQuery();
            if (rs.next()) {
               isoCountryCode = rs.getString(1);
               break label174;
            }

            i$ = null;
         } catch (SQLException var24) {
            throw new EJBException(var24.getMessage());
         } finally {
            try {
               if (rs != null) {
                  rs.close();
               }
            } catch (SQLException var23) {
               rs = null;
            }

            try {
               if (ps != null) {
                  ps.close();
               }
            } catch (SQLException var22) {
               ps = null;
            }

            try {
               if (conn != null) {
                  conn.close();
               }
            } catch (SQLException var21) {
               conn = null;
            }

         }

         return i$;
      }

      i$ = this.getCountriesList().iterator();

      CountryData country;
      do {
         if (!i$.hasNext()) {
            return null;
         }

         country = (CountryData)i$.next();
      } while(country.isoCountryCode == null || !country.isoCountryCode.equalsIgnoreCase(isoCountryCode));

      return country;
   }

   public double getVoiceRate(int countryIDFrom, int typeFrom, int countryIDTo, int typeTo) throws EJBException, Exception {
      double rate = 0.0D;
      CountryData countryFrom = this.getCountry(countryIDFrom);
      CountryData countryTo = this.getCountry(countryIDTo);

      try {
         if (countryFrom != null && countryTo != null) {
            if (typeFrom == 1 && countryFrom.callRate != null) {
               rate += countryFrom.callRate;
            } else {
               if (typeFrom != 2 || countryFrom.mobileRate == null) {
                  throw new Exception("There is currently no call route from your source number.");
               }

               rate += countryFrom.mobileRate;
            }

            if (typeTo == 1 && countryTo.callRate != null) {
               rate += countryTo.callRate;
            } else {
               if (typeTo != 2 || countryTo.mobileRate == null) {
                  throw new Exception("There is currently no call route to your destination number.");
               }

               rate += countryTo.mobileRate;
            }

            return rate;
         } else {
            throw new Exception("Country selected not recognised.");
         }
      } catch (SQLException var10) {
         throw new EJBException(var10.getMessage());
      } catch (Exception var11) {
         throw new EJBException(var11.getMessage());
      }
   }

   public CurrencyData getCurrency(String code) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      CurrencyData var6;
      try {
         CurrencyData currencyData = (CurrencyData)MemCachedClientWrapper.get(MemCachedKeySpaces.CommonKeySpace.CURRENCY, code);
         if (currencyData == null) {
            conn = this.dataSourceMaster.getConnection();
            ps = conn.prepareStatement("select * from currency where code = ?");
            ps.setString(1, code);
            rs = ps.executeQuery();
            if (!rs.next()) {
               throw new EJBException("Unable to find the currency code " + code);
            }

            currencyData = new CurrencyData(rs);
            MemCachedClientWrapper.set(MemCachedKeySpaces.CommonKeySpace.CURRENCY, code, currencyData);
         }

         var6 = currencyData;
      } catch (SQLException var21) {
         throw new EJBException(var21.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var20) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var19) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var18) {
            conn = null;
         }

      }

      return var6;
   }

   public void setCurrency(CurrencyData currencyData) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         currencyData.lastUpdated = new Date();
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("select * from currency where code = ?");
         ps.setString(1, currencyData.code);
         rs = ps.executeQuery();
         if (rs.next()) {
            rs.close();
            ps.close();
            ps = conn.prepareStatement("update currency set name = ?, exchangerate = ?, lastupdated = ? where code = ?");
            ps.setString(1, currencyData.name);
            ps.setObject(2, currencyData.exchangeRate);
            ps.setTimestamp(3, new Timestamp(currencyData.lastUpdated.getTime()));
            ps.setString(4, currencyData.code);
            ps.executeUpdate();
         } else {
            rs.close();
            ps.close();
            ps = conn.prepareStatement("insert into currency (code, name, exchangerate, lastupdated) values (?,?,?,?)");
            ps.setString(1, currencyData.code);
            ps.setString(2, currencyData.name);
            ps.setObject(3, currencyData.exchangeRate);
            ps.setTimestamp(4, new Timestamp(currencyData.lastUpdated.getTime()));
            ps.executeUpdate();
         }

         rs.close();
         ps.close();
         ps = conn.prepareStatement("select * from exchangeratehistory where code = ? and datecreated = ?");
         ps.setString(1, currencyData.code);
         ps.setDate(2, new java.sql.Date(currencyData.lastUpdated.getTime()));
         rs = ps.executeQuery();
         if (!rs.next()) {
            rs.close();
            ps.close();
            ps = conn.prepareStatement("insert into exchangeratehistory (code, datecreated, exchangerate) values (?,?,?)");
            ps.setString(1, currencyData.code);
            ps.setTimestamp(2, new Timestamp(currencyData.lastUpdated.getTime()));
            ps.setDouble(3, currencyData.exchangeRate);
            ps.executeUpdate();
         }

         MemCachedClientWrapper.delete(MemCachedKeySpaces.CommonKeySpace.CURRENCY, currencyData.code);
      } catch (SQLException var20) {
         throw new EJBException("Unable to set currency: " + var20.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var19) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var18) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var17) {
            conn = null;
         }

      }

   }

   public double getSMSRate(String currency) throws EJBException, Exception {
      try {
         if (currency == null) {
            throw new Exception("Please select a currency.");
         } else {
            Iterator i$ = this.getCountriesList().iterator();

            CountryData country;
            do {
               if (!i$.hasNext()) {
                  throw new Exception("There is no SMS rate for the chosen currency.");
               }

               country = (CountryData)i$.next();
            } while(!currency.equals(country.currency));

            return country.smsCost;
         }
      } catch (SQLException var4) {
         throw new EJBException(var4.getMessage());
      } catch (Exception var5) {
         throw new EJBException(var5.getMessage());
      }
   }

   public double getPremiumCost(int country) throws EJBException, Exception {
      double cost = 0.0D;
      CountryData countryData = this.getCountry(country);

      try {
         if (countryData == null) {
            throw new Exception("Country selected not recognised.");
         } else if (countryData.premiumSMSAmount != null) {
            cost = countryData.premiumSMSAmount;
            return cost;
         } else {
            throw new Exception("There is currently no premium SMS recharge option in the country specified.");
         }
      } catch (SQLException var6) {
         throw new EJBException(var6.getMessage());
      } catch (Exception var7) {
         throw new EJBException(var7.getMessage());
      }
   }

   public double getPremiumFee(int country) throws EJBException, Exception {
      double cost = 0.0D;
      CountryData countryData = this.getCountry(country);

      try {
         if (countryData == null) {
            throw new Exception("Country selected not recognised.");
         } else if (countryData.premiumSMSFee != null) {
            cost = countryData.premiumSMSFee;
            return cost;
         } else {
            throw new Exception("There is currently no premium SMS recharge option in the country specified.");
         }
      } catch (SQLException var6) {
         throw new EJBException(var6.getMessage());
      } catch (Exception var7) {
         throw new EJBException(var7.getMessage());
      }
   }

   public String getCountryCurrency(int country) throws EJBException, Exception {
      String currency = "";
      CountryData countryData = this.getCountry(country);

      try {
         if (countryData == null) {
            throw new Exception("Country selected not recognised.");
         } else if (countryData.currency != null) {
            currency = countryData.currency;
            return currency;
         } else {
            throw new Exception("There is currently no currency for this country.");
         }
      } catch (SQLException var5) {
         throw new EJBException(var5.getMessage());
      } catch (Exception var6) {
         throw new EJBException(var6.getMessage());
      }
   }

   public void addToMailList(String email) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;

      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("insert into mailinglist (email) values (?)");
         ps.setString(1, email);
         if (ps.executeUpdate() != 1) {
            throw new EJBException("Failed to insert email to mailing list for " + email);
         }
      } catch (SQLException var16) {
         throw new EJBException(var16.getMessage());
      } finally {
         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var15) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var14) {
            conn = null;
         }

      }

   }

   public boolean isVASBuild(String userAgent) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      boolean var6;
      try {
         Integer isVASBuild = MemCachedClientWrapper.getInt(MemCachedKeySpaces.CommonKeySpace.VAS_BUILD, userAgent);
         if (isVASBuild == null) {
            conn = this.dataSourceSlave.getConnection();
            ps = conn.prepareStatement("select pb.id from partnerbuild pb, partneragreementbuild pab, partneragreement pa where pb.id = pab.partnerbuildid and pab.partneragreementid = pa.id and now() >= pa.startdate and now() <= pa.enddate and pb.useragent = ?");
            ps.setString(1, userAgent);
            rs = ps.executeQuery();
            isVASBuild = rs.next() ? 1 : 0;
            MemCachedClientWrapper.set(MemCachedKeySpaces.CommonKeySpace.VAS_BUILD, userAgent, isVASBuild);
         }

         var6 = isVASBuild == 1;
      } catch (SQLException var21) {
         throw new EJBException(var21.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var20) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var19) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var18) {
            conn = null;
         }

      }

      return var6;
   }

   public CashReceiptData getCashReceiptData(int id) {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      CashReceiptData var23;
      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("SELECT * FROM cashreceipt where id = ?");
         ps.setInt(1, id);
         rs = ps.executeQuery();
         if (!rs.next()) {
            throw new SQLException("Failed to find cashreceipt id:" + id);
         }

         CashReceiptData cashreceiptData = new CashReceiptData(rs);
         var23 = cashreceiptData;
      } catch (Exception var21) {
         UUID errorID = UUID.randomUUID();
         log.error("[" + errorID + "] Unable to get cash receipt  from table: " + var21.getMessage());
         throw new EJBException("[" + errorID + "] Unable to get cash receipt  from table", var21);
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var20) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var19) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var18) {
            conn = null;
         }

      }

      return var23;
   }

   public CashReceiptData createCashReceipt(CashReceiptData cashReceiptData, AccountEntrySourceData accountEntrySourceData) throws EJBException {
      return SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.CashReceipt.ENABLE_CASH_RECEIPT_ID_WITH_NEW_ID_RETRIEVAL) ? this.createCashReceipt_NewIDRetrieval(cashReceiptData, accountEntrySourceData) : this.createCashReceipt_OldIDRetrieval(cashReceiptData, accountEntrySourceData);
   }

   /** @deprecated */
   private CashReceiptData createCashReceipt_OldIDRetrieval(CashReceiptData cashReceiptData, AccountEntrySourceData accountEntrySourceData) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      log.info("createCashReceipt for data:[" + cashReceiptData + "]");

      CashReceiptData var7;
      try {
         conn = this.dataSourceMaster.getConnection();
         cashReceiptData.dateCreated = new Date();
         ps = conn.prepareStatement("select providerTransactionID from cashReceipt where providerTransactionID = ? AND status NOT IN (2,3) ");
         ps.setString(1, cashReceiptData.providerTransactionID);
         rs = ps.executeQuery();
         if (rs.next()) {
            throw new EJBException("Failed to add new cash receipt: That Transaction Reference No already exists!");
         }

         ps.close();
         String statement = "insert into cashreceipt ";
         statement = statement + "(datecreated, EnteredBy, datereceived, amountSent, amountReceived, amountCredited, type, MatchedBy, datematched, status, providerTransactionID, senderUsername, comments, paymentDetails,mobilePhone,referenceCashReceiptID,bonus) ";
         statement = statement + "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
         ps = conn.prepareStatement(statement);
         ps.setTimestamp(1, new Timestamp(cashReceiptData.dateCreated.getTime()));
         ps.setString(2, cashReceiptData.enteredBy);
         ps.setTimestamp(3, new Timestamp(cashReceiptData.dateReceived.getTime()));
         ps.setDouble(4, cashReceiptData.amountSent);
         ps.setDouble(5, cashReceiptData.amountReceived);
         ps.setObject(6, cashReceiptData.amountCredited);
         ps.setObject(7, cashReceiptData.type.value());
         ps.setString(8, (String)null);
         ps.setTimestamp(9, (Timestamp)null);
         ps.setInt(10, 0);
         ps.setString(11, cashReceiptData.providerTransactionID);
         ps.setString(12, cashReceiptData.senderUsername);
         ps.setString(13, cashReceiptData.comments);
         ps.setString(14, cashReceiptData.paymentDetails);
         ps.setString(15, cashReceiptData.mobilePhone);
         ps.setObject(16, cashReceiptData.referenceCashReceiptId);
         ps.setObject(17, cashReceiptData.bonus);
         System.out.println(ps.toString());
         if (ps.executeUpdate() != 1) {
            throw new EJBException("Failed to add a new cash receipt: Database error.");
         }

         ps.close();
         statement = "select id from cashreceipt ";
         statement = statement + "where providerTransactionID = ?";
         ps = conn.prepareStatement(statement);
         ps.setString(1, cashReceiptData.providerTransactionID);
         rs = ps.executeQuery();
         rs.next();
         cashReceiptData.id = rs.getInt("ID");
         if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.CashReceipt.MATCH_UPON_CREATION_ENABLED) && cashReceiptData.senderUsername != null) {
            try {
               cashReceiptData.matchedBy = cashReceiptData.enteredBy;
               this.matchCashReceipt(cashReceiptData, accountEntrySourceData);
            } catch (Exception var23) {
               throw new EJBException(var23.getMessage(), var23);
            }
         }

         var7 = cashReceiptData;
      } catch (SQLException var24) {
         throw new EJBException("Failed to add a new cash receipt: Most likely either the user entered does not exist or you have entered a duplicate reference ID!" + var24, var24);
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var22) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var21) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var20) {
            conn = null;
         }

      }

      return var7;
   }

   private CashReceiptData createCashReceipt_NewIDRetrieval(CashReceiptData cashReceiptData, AccountEntrySourceData accountEntrySourceData) throws EJBException {
      log.info("createCashReceipt for data:[" + cashReceiptData + "]");

      try {
         Connection conn = this.dataSourceMaster.getConnection();

         try {
            Timestamp now = new Timestamp(System.currentTimeMillis());
            cashReceiptData.dateCreated = now;
            cashReceiptData.status = CashReceiptData.StatusEnum.UNMATCHED;
            cashReceiptData.matchedBy = null;
            cashReceiptData.dateMatched = null;
            PreparedStatement ps = conn.prepareStatement("select providerTransactionID from cashReceipt where providerTransactionID = ? AND status NOT IN (2,3) ");

            try {
               ps.setString(1, cashReceiptData.providerTransactionID);
               ResultSet rs = ps.executeQuery();

               try {
                  if (rs.next()) {
                     throw new EJBException("Failed to add new cash receipt: That Transaction Reference No already exists!");
                  }
               } finally {
                  rs.close();
               }
            } finally {
               ps.close();
            }

            String var59 = "insert into cashreceipt (datecreated, EnteredBy, datereceived, amountSent, amountReceived, amountCredited, type, MatchedBy, datematched, status, providerTransactionID, senderUsername, comments, paymentDetails,mobilePhone,referenceCashReceiptID,bonus) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            PreparedStatement ps = conn.prepareStatement("insert into cashreceipt (datecreated, EnteredBy, datereceived, amountSent, amountReceived, amountCredited, type, MatchedBy, datematched, status, providerTransactionID, senderUsername, comments, paymentDetails,mobilePhone,referenceCashReceiptID,bonus) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", 1);

            try {
               ps.setTimestamp(1, now);
               ps.setString(2, cashReceiptData.enteredBy);
               ps.setTimestamp(3, new Timestamp(cashReceiptData.dateReceived.getTime()));
               ps.setDouble(4, cashReceiptData.amountSent);
               ps.setDouble(5, cashReceiptData.amountReceived);
               ps.setObject(6, cashReceiptData.amountCredited);
               ps.setObject(7, cashReceiptData.type.value());
               ps.setString(8, (String)null);
               ps.setTimestamp(9, (Timestamp)null);
               ps.setInt(10, cashReceiptData.status.value());
               ps.setString(11, cashReceiptData.providerTransactionID);
               ps.setString(12, cashReceiptData.senderUsername);
               ps.setString(13, cashReceiptData.comments);
               ps.setString(14, cashReceiptData.paymentDetails);
               ps.setString(15, cashReceiptData.mobilePhone);
               ps.setObject(16, cashReceiptData.referenceCashReceiptId);
               ps.setObject(17, cashReceiptData.bonus);
               if (log.isDebugEnabled()) {
                  log.debug(ps.toString());
               }

               if (ps.executeUpdate() != 1) {
                  throw new EJBException("Failed to add a new cash receipt: Database error.");
               }

               ResultSet rs = ps.getGeneratedKeys();

               try {
                  if (!rs.next()) {
                     throw new EJBException("Failed to retrieve new cashreceipt id. Database error.");
                  }

                  cashReceiptData.id = rs.getInt(1);
               } finally {
                  rs.close();
               }
            } finally {
               ps.close();
            }
         } finally {
            conn.close();
         }

         if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.CashReceipt.MATCH_UPON_CREATION_ENABLED) && cashReceiptData.senderUsername != null) {
            try {
               cashReceiptData.matchedBy = cashReceiptData.enteredBy;
               cashReceiptData.dateMatched = new Timestamp(System.currentTimeMillis());
               this.matchCashReceipt(cashReceiptData, accountEntrySourceData);
            } catch (Exception var52) {
               throw new EJBException(var52.getMessage(), var52);
            }
         }

         return cashReceiptData;
      } catch (SQLException var58) {
         throw new EJBException("Failed to add a new cash receipt: Most likely either the user entered does not exist or you have entered a duplicate reference ID!" + var58, var58);
      }
   }

   public void matchCashReceipt(CashReceiptData cashReceiptData, AccountEntrySourceData accountEntrySourceData) throws EJBException {
      log.info("matchCashReceipt for data:[" + cashReceiptData + "]");

      try {
         Connection conn = this.dataSourceMaster.getConnection();

         try {
            long now = System.currentTimeMillis();
            cashReceiptData.dateMatched = new Timestamp(now);
            cashReceiptData.status = CashReceiptData.StatusEnum.MATCHED;
            boolean validateCashReceiptStatusBeforeMatch = SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.CashReceipt.VALIDATE_CASH_RECEIPT_STATUS_BEFORE_MATCH);
            int updatedCount;
            String amountSent;
            PreparedStatement ps;
            if (validateCashReceiptStatusBeforeMatch) {
               amountSent = "update cashreceipt set  MatchedBy = ?, datematched = ?, status = ?,  comments = ?  where ID = ? and status = ?";
               ps = conn.prepareStatement("update cashreceipt set  MatchedBy = ?, datematched = ?, status = ?,  comments = ?  where ID = ? and status = ?");

               try {
                  ps.setString(1, cashReceiptData.matchedBy);
                  ps.setTimestamp(2, new Timestamp(now));
                  ps.setInt(3, cashReceiptData.status.value());
                  ps.setString(4, cashReceiptData.comments);
                  ps.setInt(5, cashReceiptData.id);
                  ps.setInt(6, CashReceiptData.StatusEnum.UNMATCHED.value());
                  updatedCount = ps.executeUpdate();
               } finally {
                  ps.close();
               }
            } else {
               amountSent = "update cashreceipt set  MatchedBy = ?, datematched = ?, status = ?,  comments = ?  where ID = ?";
               ps = conn.prepareStatement("update cashreceipt set  MatchedBy = ?, datematched = ?, status = ?,  comments = ?  where ID = ?");

               try {
                  ps.setString(1, cashReceiptData.matchedBy);
                  ps.setTimestamp(2, new Timestamp(now));
                  ps.setInt(3, cashReceiptData.status.value());
                  ps.setString(4, cashReceiptData.comments);
                  ps.setInt(5, cashReceiptData.id);
                  updatedCount = ps.executeUpdate();
               } finally {
                  ps.close();
               }
            }

            if (updatedCount == 0) {
               throw new EJBException("Failed to update cash receipt: No rows matched the criteria for update. Cash receiptId:[" + cashReceiptData.id + "].validateCashReceiptStatusBeforeMatch=[" + validateCashReceiptStatusBeforeMatch + "]");
            }

            if (updatedCount > 1) {
               throw new EJBException("Too many rows to update.Cash receiptId:[" + cashReceiptData.id + "]");
            }

            PreparedStatement ps = conn.prepareStatement("select senderusername,amountsent,amountcredited from cashreceipt where id=?");

            double amountCredited;
            String senderusername;
            try {
               ps.setInt(1, cashReceiptData.id);
               ResultSet rs = ps.executeQuery();

               try {
                  if (!rs.next()) {
                     throw new EJBException("Cash receipt id [" + cashReceiptData.id + "] does not exist");
                  }

                  amountSent = rs.getDouble("amountsent");
                  amountCredited = rs.getDouble("amountcredited");
                  senderusername = rs.getString("senderusername");
               } finally {
                  rs.close();
               }
            } finally {
               ps.close();
            }

            AccountLocal var69 = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
            var69.creditAndNotifyUser(senderusername, (double)amountSent, amountCredited, Integer.toString(cashReceiptData.id), accountEntrySourceData, true);
         } finally {
            conn.close();
         }

      } catch (RuntimeException var66) {
         throw var66;
      } catch (SQLException var67) {
         throw new EJBException("Failed to match cash receipt: Database error! Cash Receipt Id:[" + cashReceiptData.id + "]");
      } catch (Exception var68) {
         throw new EJBException("Failed to match cash receipt: Cash Receipt Id:[" + cashReceiptData.id + "] Internal error:[" + var68 + "]", var68);
      }
   }

   public void deleteCashReceipt(String providerTransactionID) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      Object rs = null;

      try {
         conn = this.dataSourceMaster.getConnection();
         String statement = "update cashreceipt set ";
         statement = statement + "status = ? ";
         statement = statement + "where providerTransactionID = ?";
         ps = conn.prepareStatement(statement);
         ps.setObject(1, CashReceiptData.StatusEnum.DELETED.value());
         ps.setString(2, providerTransactionID);
         System.out.println("AND THE CASH RECEIPT OBJECT READS: " + CashReceiptData.StatusEnum.DELETED.value());
         if (ps.executeUpdate() != 1) {
            throw new EJBException("Failed to delete cash receipt: Database error.");
         }
      } catch (SQLException var20) {
         throw new EJBException(var20.getMessage());
      } finally {
         try {
            if (rs != null) {
               ((ResultSet)rs).close();
            }
         } catch (SQLException var19) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var18) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var17) {
            conn = null;
         }

      }

   }

   public void logMISLogin(String misUsername, String ipAddress, boolean loginSuccessFul) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      Object rs = null;

      try {
         conn = this.dataSourceMaster.getConnection();
         if (misUsername.length() < 1) {
            throw new EJBException("You must specify the MIS Username");
         }

         if (ipAddress.length() < 1) {
            throw new EJBException("You must specify an IP address");
         }

         ps = conn.prepareStatement("insert into stafflogin (staffusername, ipaddress, datecreated, loginsuccessful) VALUES (?,?,?,?)");
         ps.setString(1, misUsername);
         ps.setString(2, ipAddress);
         ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
         ps.setObject(4, loginSuccessFul);
         if (ps.executeUpdate() < 1) {
            throw new EJBException("Failed to log MIS user login");
         }
      } catch (SQLException var22) {
         throw new EJBException(var22.getMessage());
      } finally {
         try {
            if (rs != null) {
               ((ResultSet)rs).close();
            }
         } catch (SQLException var21) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var20) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var19) {
            conn = null;
         }

      }

   }

   public double calculateBonusCredit(double amount) throws EJBException {
      return 0.0D;
   }

   public void allowUserToReRegister(String username) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      Object rs = null;

      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("update user set mobilephone=?, mobileverified=?, status=? where username=?");
         ps.setString(1, (String)null);
         ps.setInt(2, 0);
         ps.setInt(3, 0);
         ps.setString(4, username);
         if (ps.executeUpdate() != 1) {
            throw new EJBException("Failed to remove mobile phone and deactivate account for " + username);
         }
      } catch (SQLException var20) {
         throw new EJBException(var20.getMessage());
      } finally {
         try {
            if (rs != null) {
               ((ResultSet)rs).close();
            }
         } catch (SQLException var19) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var18) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var17) {
            conn = null;
         }

      }

   }

   public AlertMessageData getAlertMessageData(String id) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      if (id.length() < 1) {
         throw new EJBException("You must specify an ID");
      } else {
         AlertMessageData var5;
         try {
            conn = this.dataSourceMaster.getConnection();
            ps = conn.prepareStatement("select * from alertmessage where id = ?");
            ps.setString(1, id);
            rs = ps.executeQuery();
            if (!rs.next()) {
               throw new EJBException("Invalid alert message ID " + id);
            }

            var5 = new AlertMessageData(rs);
         } catch (SQLException var20) {
            throw new EJBException(var20.getMessage());
         } finally {
            try {
               if (rs != null) {
                  rs.close();
               }
            } catch (SQLException var19) {
               rs = null;
            }

            try {
               if (ps != null) {
                  ps.close();
               }
            } catch (SQLException var18) {
               ps = null;
            }

            try {
               if (conn != null) {
                  conn.close();
               }
            } catch (SQLException var17) {
               conn = null;
            }

         }

         return var5;
      }
   }

   public void createAlertMessage(AlertMessageData amd) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      Object rs = null;

      try {
         conn = this.dataSourceMaster.getConnection();
         if (amd.content.length() < 1) {
            throw new EJBException("The alert message text cannot be empty");
         }

         ps = conn.prepareStatement("insert into alertmessage (countryid, datecreated, startdate, expirydate, type, onceonly, weighting, minmidletversion, maxmidletversion, contenttype, content, url, status) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)");
         ps.setObject(1, amd.countryID);
         ps.setDate(2, new java.sql.Date(System.currentTimeMillis()));
         ps.setTimestamp(3, new Timestamp(amd.startDate.getTime()));
         ps.setTimestamp(4, new Timestamp(amd.expiryDate.getTime()));
         ps.setInt(5, amd.type.value());
         if (amd.onceOnly) {
            ps.setInt(6, 1);
         } else {
            ps.setInt(6, 0);
         }

         ps.setDouble(7, amd.weighting);
         ps.setInt(8, amd.minMidletVersion);
         ps.setInt(9, amd.maxMidletVersion);
         ps.setInt(10, amd.contentType.value());
         ps.setString(11, amd.content);
         ps.setString(12, amd.url);
         ps.setInt(13, amd.status.value());
         if (ps.executeUpdate() < 1) {
            throw new EJBException("Could not create the new alert message");
         }
      } catch (SQLException var20) {
         throw new EJBException(var20.getMessage());
      } finally {
         try {
            if (rs != null) {
               ((ResultSet)rs).close();
            }
         } catch (SQLException var19) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var18) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var17) {
            conn = null;
         }

      }

   }

   public void updateAlertMessage(AlertMessageData amd) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      Object rs = null;

      try {
         conn = this.dataSourceMaster.getConnection();
         if (amd.content.length() < 1) {
            throw new EJBException("The alert message text cannot be empty");
         }

         if (amd.id < 1) {
            throw new EJBException("You must specify an ID to update");
         }

         ps = conn.prepareStatement("update alertmessage set countryid = ?, datecreated = ?, startdate = ?, expirydate = ?, type = ?, onceonly = ?, weighting = ?, minmidletversion = ?, maxmidletversion = ?, contenttype = ?, content = ?, url = ?, status = ? WHERE id = ? ");
         ps.setObject(1, amd.countryID);
         ps.setDate(2, new java.sql.Date(System.currentTimeMillis()));
         ps.setTimestamp(3, new Timestamp(amd.startDate.getTime()));
         ps.setTimestamp(4, new Timestamp(amd.expiryDate.getTime()));
         ps.setInt(5, amd.type.value());
         if (amd.onceOnly) {
            ps.setInt(6, 1);
         } else {
            ps.setInt(6, 0);
         }

         ps.setDouble(7, amd.weighting);
         ps.setInt(8, amd.minMidletVersion);
         ps.setInt(9, amd.maxMidletVersion);
         ps.setInt(10, amd.contentType.value());
         ps.setString(11, amd.content);
         ps.setString(12, amd.url);
         ps.setInt(13, amd.status.value());
         ps.setInt(14, amd.id);
         if (ps.executeUpdate() < 1) {
            throw new EJBException("Could not update the alert message");
         }
      } catch (SQLException var20) {
         throw new EJBException(var20.getMessage());
      } finally {
         try {
            if (rs != null) {
               ((ResultSet)rs).close();
            }
         } catch (SQLException var19) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var18) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var17) {
            conn = null;
         }

      }

   }

   public ResellerData getResellerData(String id) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      if (id.length() < 1) {
         throw new EJBException("You must specify an ID");
      } else {
         ResellerData rd = new ResellerData();

         try {
            conn = this.dataSourceMaster.getConnection();
            ps = conn.prepareStatement("select * from reseller where id = ?");
            ps.setString(1, id);
            rs = ps.executeQuery();
            if (rs.next()) {
               rd.id = rs.getInt("id");
               rd.countryID = rs.getInt("countryid");
               rd.state = rs.getString("state");
               rd.city = rs.getString("city");
               rd.name = rs.getString("name");
               rd.address = rs.getString("address");
               rd.phoneNumber = rs.getString("phonenumber");
               rd.phoneNumberToDisplay = rs.getString("phonenumbertodisplay");
               rd.phoneNumber2 = rs.getString("phonenumber2");
               rd.phoneNumber2ToDisplay = rs.getString("phonenumber2todisplay");
               rd.status = ResellerData.StatusEnum.fromValue(rs.getInt("status"));
            }
         } catch (SQLException var21) {
            throw new EJBException(var21.getMessage());
         } finally {
            try {
               if (rs != null) {
                  rs.close();
               }
            } catch (SQLException var20) {
               rs = null;
            }

            try {
               if (ps != null) {
                  ps.close();
               }
            } catch (SQLException var19) {
               ps = null;
            }

            try {
               if (conn != null) {
                  conn.close();
               }
            } catch (SQLException var18) {
               conn = null;
            }

         }

         return rd;
      }
   }

   public void createReseller(ResellerData rd) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      Object rs = null;

      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("insert into reseller (countryid, state, city, name, address, phonenumber, phonenumbertodisplay, phonenumber2, phonenumber2todisplay, status) VALUES (?,?,?,?,?,?,?,?,?,?)");
         ps.setObject(1, rd.countryID);
         ps.setObject(2, rd.state);
         ps.setObject(3, rd.city);
         ps.setObject(4, rd.name);
         ps.setObject(5, rd.address);
         ps.setObject(6, rd.phoneNumber);
         ps.setObject(7, rd.phoneNumberToDisplay);
         ps.setObject(8, rd.phoneNumber2);
         ps.setObject(9, rd.phoneNumber2ToDisplay);
         ps.setInt(10, rd.status.value());
         if (ps.executeUpdate() < 1) {
            throw new EJBException("Could not create the new reseller");
         }
      } catch (SQLException var20) {
         throw new EJBException(var20.getMessage());
      } finally {
         try {
            if (rs != null) {
               ((ResultSet)rs).close();
            }
         } catch (SQLException var19) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var18) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var17) {
            conn = null;
         }

      }

   }

   public void updateReseller(ResellerData rd) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      Object rs = null;

      try {
         conn = this.dataSourceMaster.getConnection();
         if (rd.id != null && rd.id >= 1) {
            ps = conn.prepareStatement("update reseller set countryid=?, state=?, city=?, name=?, address=?, phonenumber=?, phonenumbertodisplay=?, phonenumber2=?, phonenumber2todisplay=?, status=? WHERE id=? ");
            ps.setObject(1, rd.countryID);
            ps.setObject(2, rd.state);
            ps.setObject(3, rd.city);
            ps.setObject(4, rd.name);
            ps.setObject(5, rd.address);
            ps.setObject(6, rd.phoneNumber);
            ps.setObject(7, rd.phoneNumberToDisplay);
            ps.setObject(8, rd.phoneNumber2);
            ps.setObject(9, rd.phoneNumber2ToDisplay);
            ps.setInt(10, rd.status.value());
            ps.setInt(11, rd.id);
            if (ps.executeUpdate() < 1) {
               throw new EJBException("Could not update the reseller");
            }
         } else {
            throw new EJBException("You must specify an ID to update");
         }
      } catch (SQLException var20) {
         throw new EJBException(var20.getMessage());
      } finally {
         try {
            if (rs != null) {
               ((ResultSet)rs).close();
            }
         } catch (SQLException var19) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var18) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var17) {
            conn = null;
         }

      }
   }

   public void updateCountry(CountryData cd) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      Object rs = null;

      try {
         if (cd.id < 1) {
            throw new EJBException("You must specify the country ID to update");
         }

         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("update country set activationcredit = ?, referralcredit = ?, callrate = ?, mobilerate = ?, callthroughrate = ?, CallSignallingFee = ?, MobileSignallingFee = ?, CallThroughSignallingFee = ?, Population = ?, AllowCreditCard = ?, AllowBankTransfer = ?, AllowWesternUnion = ?, allowZeroAfterIDDCode = ?, allowEmail = ?, allowPhoneCall = ?, allowUserTransferToOtherCountry = ?, lowASRDestination = ?, callRetries = ?, userBonusProgramID = ?, merchantBonusProgramID = ? WHERE id = ?");
         ps.setDouble(1, cd.activationCredit);
         ps.setDouble(2, cd.referralCredit);
         ps.setDouble(3, cd.callRate);
         ps.setDouble(4, cd.mobileRate);
         ps.setDouble(5, cd.callThroughRate);
         ps.setDouble(6, cd.callSignallingFee);
         ps.setDouble(7, cd.mobileSignallingFee);
         ps.setDouble(8, cd.callThroughSignallingFee);
         ps.setInt(9, cd.population);
         ps.setInt(10, cd.allowCreditCard.value());
         ps.setInt(11, cd.allowBankTransfer);
         ps.setInt(12, cd.allowWesternUnion);
         ps.setObject(13, cd.allowZeroAfterIddCode == null ? null : cd.allowZeroAfterIddCode ? 1 : 0);
         ps.setObject(14, cd.allowEmail == null ? null : cd.allowEmail ? 1 : 0);
         ps.setObject(15, cd.allowPhoneCall == null ? null : cd.allowPhoneCall ? 1 : 0);
         ps.setObject(16, cd.allowUserTransferToOtherCountry == null ? null : cd.allowUserTransferToOtherCountry ? 1 : 0);
         ps.setObject(17, cd.lowASRDestination == null ? null : cd.lowASRDestination ? 1 : 0);
         ps.setInt(18, cd.callRetries);
         ps.setObject(19, cd.userBonusProgramID);
         ps.setObject(20, cd.merchantBonusProgramID);
         ps.setInt(21, cd.id);
         if (ps.executeUpdate() < 1) {
            throw new EJBException("Could not update the country data");
         }
      } catch (SQLException var20) {
         throw new EJBException(var20.getMessage());
      } finally {
         try {
            if (rs != null) {
               ((ResultSet)rs).close();
            }
         } catch (SQLException var19) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var18) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var17) {
            conn = null;
         }

      }

   }

   public void changeStaffPassword(String staffUsername, String oldPassword, String newPassword) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         conn = this.dataSourceMaster.getConnection();
         if (oldPassword.length() >= 1 && newPassword.length() >= 1) {
            ps = conn.prepareStatement("select password from staff where username = ?");
            ps.setString(1, staffUsername);
            rs = ps.executeQuery();
            if (rs.next()) {
               String currentPassword = rs.getString("password");
               if (!oldPassword.equals(currentPassword)) {
                  throw new EJBException("Old password is not correct");
               } else {
                  ps = conn.prepareStatement("update staff set password = ? where username = ?");
                  ps.setString(1, newPassword);
                  ps.setString(2, staffUsername);
                  if (ps.executeUpdate() < 1) {
                     throw new EJBException("Could not set the new password");
                  }
               }
            } else {
               throw new EJBException("Staff user does not exist");
            }
         } else {
            throw new EJBException("Passwords cannot be blank");
         }
      } catch (SQLException var22) {
         throw new EJBException(var22.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var21) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var20) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var19) {
            conn = null;
         }

      }
   }

   public String newFileID() {
      return UUID.randomUUID().toString().replaceAll("-", "");
   }

   public FileData getFile(String fileID) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      FileData var6;
      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("select * from file where id = ?");
         ps.setString(1, fileID);
         rs = ps.executeQuery();
         if (!rs.next()) {
            throw new EJBException("Invalid file ID");
         }

         FileData fileData = new FileData();
         fileData.id = rs.getString("id");
         fileData.dateCreated = rs.getTimestamp("dateCreated");
         fileData.mimeType = rs.getString("mimeType");
         fileData.size = (Integer)rs.getObject("size");
         fileData.width = (Integer)rs.getObject("width");
         fileData.height = (Integer)rs.getObject("height");
         fileData.length = (Integer)rs.getObject("length");
         fileData.uploadedBy = rs.getString("uploadedBy");
         var6 = fileData;
      } catch (SQLException var21) {
         throw new EJBException(var21.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var20) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var19) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var18) {
            conn = null;
         }

      }

      return var6;
   }

   public ScrapbookData saveFile(FileData fileData, String description) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      ScrapbookData var28;
      try {
         description = StringUtil.stripHTML(description);
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("insert into file (id, datecreated, mimetype, size, width, height, length, uploadedby) values (?,?,?,?,?,?,?,?)");
         ps.setString(1, fileData.id);
         ps.setTimestamp(2, new Timestamp(fileData.dateCreated.getTime()));
         ps.setString(3, fileData.mimeType);
         ps.setInt(4, fileData.size);
         ps.setObject(5, fileData.width);
         ps.setObject(6, fileData.height);
         ps.setObject(7, fileData.length);
         ps.setString(8, fileData.uploadedBy);
         if (ps.executeUpdate() != 1) {
            throw new EJBException("Failed to save file summary to database");
         }

         ps.close();
         ScrapbookData scrapbookData = new ScrapbookData();
         scrapbookData.username = fileData.uploadedBy;
         scrapbookData.fileID = fileData.id;
         scrapbookData.dateCreated = new Date();
         scrapbookData.receivedFrom = fileData.uploadedBy;
         scrapbookData.description = description;
         scrapbookData.status = ScrapbookData.StatusEnum.PRIVATE;
         ps = conn.prepareStatement("insert into scrapbook (username, fileid, datecreated, receivedfrom, description, status) values (?,?,?,?,?,?)", 1);
         ps.setString(1, scrapbookData.username);
         ps.setString(2, scrapbookData.fileID);
         ps.setTimestamp(3, new Timestamp(scrapbookData.dateCreated.getTime()));
         ps.setString(4, scrapbookData.receivedFrom);
         ps.setString(5, scrapbookData.description);
         ps.setInt(6, scrapbookData.status.value());
         if (ps.executeUpdate() != 1) {
            throw new EJBException("Failed to save file to sender's scrapbook");
         }

         rs = ps.getGeneratedKeys();
         if (!rs.next()) {
            throw new EJBException("Failed to create scrapbook entry");
         }

         scrapbookData.id = rs.getInt(1);
         MemCachedClientWrapper.delete(MemCachedKeySpaces.CommonKeySpace.SCRAPBOOK, scrapbookData.username);

         try {
            UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
            UserData uploaderUserData = userEJB.loadUser(scrapbookData.username, false, false);
            PhotoUploadTrigger trigger = new PhotoUploadTrigger(uploaderUserData);
            trigger.amountDelta = 0.0D;
            trigger.quantityDelta = 1;
            RewardCentre.getInstance().sendTrigger(trigger);
         } catch (Exception var25) {
            log.error("Unable to send PhotoUploadTrigger for user [" + scrapbookData.username + "] :" + var25.getMessage(), var25);
         }

         var28 = scrapbookData;
      } catch (SQLException var26) {
         throw new EJBException(var26.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var24) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var23) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var22) {
            conn = null;
         }

      }

      return var28;
   }

   public void saveFileToScrapbooks(String sender, String[] destinations, String fileID, String description) throws EJBException {
      Connection conn = null;
      PreparedStatement psFileExists = null;
      PreparedStatement psAddToScrapbook = null;
      ResultSet rs = null;

      try {
         description = StringUtil.stripHTML(description);
         conn = this.dataSourceMaster.getConnection();
         psFileExists = conn.prepareStatement("select id from scrapbook where username = ? and fileid = ? and receivedfrom = ? and status <> ?");
         psFileExists.setString(2, fileID);
         psFileExists.setString(3, sender);
         psFileExists.setInt(4, ScrapbookData.StatusEnum.INACTIVE.value());
         psAddToScrapbook = conn.prepareStatement("insert into scrapbook (username, fileid, datecreated, receivedfrom, description, status) values (?,?,?,?,?,?)");
         psAddToScrapbook.setString(2, fileID);
         psAddToScrapbook.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
         psAddToScrapbook.setString(4, sender);
         psAddToScrapbook.setString(5, description != null && description.length() != 0 ? description : null);
         if (destinations.length == 1 && "wall200712041".equalsIgnoreCase(destinations[0])) {
            psAddToScrapbook.setInt(6, ScrapbookData.StatusEnum.PUBLIC.value());
         } else {
            psAddToScrapbook.setInt(6, ScrapbookData.StatusEnum.PRIVATE.value());
         }

         String[] arr$ = destinations;
         int len$ = destinations.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            String destination = arr$[i$];
            if (!destination.equals(sender)) {
               psFileExists.setString(1, destination);
               rs = psFileExists.executeQuery();
               if (!rs.next()) {
                  psAddToScrapbook.setString(1, destination);
                  if (psAddToScrapbook.executeUpdate() != 1) {
                     throw new EJBException("Failed to save file to " + destination + "'s scrapbook");
                  }

                  MemCachedClientWrapper.delete(MemCachedKeySpaces.CommonKeySpace.SCRAPBOOK, destination);
               }

               rs.close();
            }
         }
      } catch (SQLException var30) {
         throw new EJBException(var30.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var29) {
            rs = null;
         }

         try {
            if (psFileExists != null) {
               psFileExists.close();
            }
         } catch (SQLException var28) {
            psFileExists = null;
         }

         try {
            if (psAddToScrapbook != null) {
               psAddToScrapbook.close();
            }
         } catch (SQLException var27) {
            psAddToScrapbook = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var26) {
            conn = null;
         }

      }

   }

   public void updateFileFromScrapbook(ScrapbookData scrapbookData) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;

      try {
         if (scrapbookData.status == ScrapbookData.StatusEnum.PUBLIC) {
            UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
            ReputationLevelData levelData = userEJB.getReputationLevel(scrapbookData.username);
            if (levelData.publishPhoto == null || !levelData.publishPhoto) {
               throw new EJBException("You current mig level is not high enough to make a photo public");
            }
         }

         scrapbookData.description = StringUtil.stripHTML(scrapbookData.description);
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("update scrapbook set description = ?, status = ? where id = ?");
         ps.setString(1, scrapbookData.description);
         ps.setInt(2, scrapbookData.status.value());
         ps.setInt(3, scrapbookData.id);
         if (ps.executeUpdate() != 1) {
            throw new EJBException("Unable to update file " + scrapbookData.id + " in scrapbook");
         }

         MemCachedClientWrapper.delete(MemCachedKeySpaces.CommonKeySpace.SCRAPBOOK, scrapbookData.id.toString());
         MemCachedClientWrapper.delete(MemCachedKeySpaces.CommonKeySpace.SCRAPBOOK, scrapbookData.username);
      } catch (CreateException var18) {
         throw new EJBException(var18.getMessage());
      } catch (SQLException var19) {
         throw new EJBException(var19.getMessage());
      } finally {
         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var17) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var16) {
            conn = null;
         }

      }

   }

   private void onPublishedScrapbookFile(String username, int id, String description) {
   }

   public void publishFileFromScrapbook(String username, int id, String description, boolean contactsOnly) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;

      try {
         description = StringUtil.stripHTML(description);
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("update scrapbook set description = ?, status = ? where id = ?");
         ps.setString(1, description);
         ps.setInt(2, contactsOnly ? ScrapbookData.StatusEnum.CONTACTS_ONLY.value() : ScrapbookData.StatusEnum.PUBLIC.value());
         ps.setInt(3, id);
         this.onPublishedScrapbookFile(username, id, description);
         if (ps.executeUpdate() != 1) {
            throw new EJBException("Unable to publish " + id + " from scrapbook");
         }

         MemCachedClientWrapper.delete(MemCachedKeySpaces.CommonKeySpace.SCRAPBOOK, String.valueOf(id));
         MemCachedClientWrapper.delete(MemCachedKeySpaces.CommonKeySpace.SCRAPBOOK, username);
      } catch (SQLException var19) {
         throw new EJBException(var19.getMessage());
      } finally {
         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var18) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var17) {
            conn = null;
         }

      }

   }

   public void unpublishFileFromScrapbook(String username, int id) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;

      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("update scrapbook set status = ? where id = ?");
         ps.setInt(1, ScrapbookData.StatusEnum.PRIVATE.value());
         ps.setInt(2, id);
         if (ps.executeUpdate() != 1) {
            throw new EJBException("Unable to unpublish " + id + " from scrapbook");
         }

         MemCachedClientWrapper.delete(MemCachedKeySpaces.CommonKeySpace.SCRAPBOOK, String.valueOf(id));
         MemCachedClientWrapper.delete(MemCachedKeySpaces.CommonKeySpace.SCRAPBOOK, username);
      } catch (SQLException var17) {
         throw new EJBException(var17.getMessage());
      } finally {
         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var16) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var15) {
            conn = null;
         }

      }

   }

   public void deleteFileFromScrapbook(String username, int id) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;

      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("update scrapbook set status = ? where id = ?");
         ps.setInt(1, ScrapbookData.StatusEnum.INACTIVE.value());
         ps.setInt(2, id);
         if (ps.executeUpdate() != 1) {
            throw new EJBException("Unable to remove " + id + " from scrapbook");
         }

         MemCachedClientWrapper.delete(MemCachedKeySpaces.CommonKeySpace.SCRAPBOOK, String.valueOf(id));
         MemCachedClientWrapper.delete(MemCachedKeySpaces.CommonKeySpace.SCRAPBOOK, username);
      } catch (SQLException var17) {
         throw new EJBException(var17.getMessage());
      } finally {
         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var16) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var15) {
            conn = null;
         }

      }

   }

   public void setFileReportedFromScrapbook(String username, int id) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;

      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("update scrapbook set status = ? where id = ?");
         ps.setInt(1, ScrapbookData.StatusEnum.REPORTED.value());
         ps.setInt(2, id);
         if (ps.executeUpdate() != 1) {
            throw new EJBException("Unable to set " + id + " from scrapbook to reported");
         }

         MemCachedClientWrapper.delete(MemCachedKeySpaces.CommonKeySpace.SCRAPBOOK, String.valueOf(id));
         MemCachedClientWrapper.delete(MemCachedKeySpaces.CommonKeySpace.SCRAPBOOK, username);
      } catch (SQLException var17) {
         throw new EJBException(var17.getMessage());
      } finally {
         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var16) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var15) {
            conn = null;
         }

      }

   }

   public void setFilePrivateFromScrapbook(String fileID) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      PreparedStatement psSetFilePrivate = null;
      ResultSet rs = null;

      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("select id, username from scrapbook where fileid = ? and status != ?");
         ps.setString(1, fileID);
         ps.setInt(2, ScrapbookData.StatusEnum.PRIVATE.value());
         psSetFilePrivate = conn.prepareStatement("update scrapbook set Status = ? where id = ?");
         psSetFilePrivate.setInt(1, ScrapbookData.StatusEnum.PRIVATE.value());
         rs = ps.executeQuery();

         while(rs.next()) {
            psSetFilePrivate.setInt(2, rs.getInt("id"));
            if (psSetFilePrivate.executeUpdate() > 0) {
               MemCachedClientWrapper.delete(MemCachedKeySpaces.CommonKeySpace.SCRAPBOOK, String.valueOf(rs.getInt("id")));
               MemCachedClientWrapper.delete(MemCachedKeySpaces.CommonKeySpace.SCRAPBOOK, rs.getString("username"));
            }
         }
      } catch (SQLException var24) {
         throw new EJBException(var24.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var23) {
            rs = null;
         }

         try {
            if (psSetFilePrivate != null) {
               psSetFilePrivate.close();
            }
         } catch (SQLException var22) {
            psSetFilePrivate = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var21) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var20) {
            conn = null;
         }

      }

   }

   public void removeAllInstancesOfFile(String fileID) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      PreparedStatement psRemoveFile = null;
      ResultSet rs = null;

      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("select id, username from scrapbook where fileid = ? and status != ?");
         ps.setString(1, fileID);
         ps.setInt(2, ScrapbookData.StatusEnum.INACTIVE.value());
         psRemoveFile = conn.prepareStatement("update scrapbook set status = ? where id = ?");
         psRemoveFile.setInt(1, ScrapbookData.StatusEnum.INACTIVE.value());
         rs = ps.executeQuery();

         while(rs.next()) {
            psRemoveFile.setInt(2, rs.getInt("id"));
            if (psRemoveFile.executeUpdate() > 0) {
               MemCachedClientWrapper.delete(MemCachedKeySpaces.CommonKeySpace.SCRAPBOOK, String.valueOf(rs.getInt("id")));
               MemCachedClientWrapper.delete(MemCachedKeySpaces.CommonKeySpace.SCRAPBOOK, rs.getString("username"));
            }
         }

         rs.close();
         psRemoveFile.close();
         ps.close();
         ps = conn.prepareStatement("select username from user where displaypicture = ?");
         ps.setString(1, fileID);
         rs = ps.executeQuery();
         if (rs.next()) {
            UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);

            do {
               userEJB.updateDisplayPicture(rs.getString("username"), (String)null);
            } while(rs.next());
         }
      } catch (SQLException var25) {
         throw new EJBException(var25.getMessage());
      } catch (CreateException var26) {
         throw new EJBException(var26.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var24) {
            rs = null;
         }

         try {
            if (psRemoveFile != null) {
               psRemoveFile.close();
            }
         } catch (SQLException var23) {
            psRemoveFile = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var22) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var21) {
            conn = null;
         }

      }

   }

   public ScrapbookData getFileFromScrapbook(int id) throws EJBException {
      List<ScrapbookData> scrapbook = this.getScrapbook(id, (String)null, (ScrapbookData.StatusEnum[])null);
      if (scrapbook.size() != 1) {
         throw new EJBException("Invalid scrapbook ID " + id);
      } else {
         return (ScrapbookData)scrapbook.get(0);
      }
   }

   public List<ScrapbookData> getScrapbook(String username) throws EJBException {
      return this.getScrapbook((Integer)null, username, new ScrapbookData.StatusEnum[]{ScrapbookData.StatusEnum.PRIVATE, ScrapbookData.StatusEnum.CONTACTS_ONLY, ScrapbookData.StatusEnum.PUBLIC});
   }

   public List<ScrapbookData> getScrapbook(String username, boolean publishedOnly) throws EJBException {
      return publishedOnly ? this.getScrapbook((Integer)null, username, new ScrapbookData.StatusEnum[]{ScrapbookData.StatusEnum.CONTACTS_ONLY, ScrapbookData.StatusEnum.PUBLIC}) : this.getScrapbook((Integer)null, username, new ScrapbookData.StatusEnum[]{ScrapbookData.StatusEnum.PRIVATE, ScrapbookData.StatusEnum.CONTACTS_ONLY, ScrapbookData.StatusEnum.PUBLIC});
   }

   public List<ScrapbookData> getGallery(String requestingUsername, String targetUsername) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      List var6;
      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("select id from contact where username = ? and fusionusername = ?");
         ps.setString(1, targetUsername);
         ps.setString(2, requestingUsername);
         rs = ps.executeQuery();
         if (rs.next()) {
            ps.setString(1, requestingUsername);
            ps.setString(2, targetUsername);
            rs.close();
            rs = ps.executeQuery();
            if (rs.next()) {
               var6 = this.getScrapbook((Integer)null, targetUsername, new ScrapbookData.StatusEnum[]{ScrapbookData.StatusEnum.CONTACTS_ONLY, ScrapbookData.StatusEnum.PUBLIC, ScrapbookData.StatusEnum.REPORTED});
               return var6;
            }
         }

         var6 = this.getScrapbook((Integer)null, targetUsername, new ScrapbookData.StatusEnum[]{ScrapbookData.StatusEnum.PUBLIC, ScrapbookData.StatusEnum.REPORTED});
      } catch (SQLException var24) {
         throw new EJBException(var24.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var23) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var22) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var21) {
            conn = null;
         }

      }

      return var6;
   }

   private List<ScrapbookData> getScrapbook(Integer id, String username, ScrapbookData.StatusEnum[] statusArray) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      Object var32;
      try {
         String key = id == null ? username : id.toString();
         List<ScrapbookData> scrapbook = (List)MemCachedClientWrapper.get(MemCachedKeySpaces.CommonKeySpace.SCRAPBOOK, key);
         if (scrapbook == null) {
            conn = this.dataSourceSlave.getConnection();
            String sql = "select scrapbook.id, scrapbook.username, scrapbook.fileid, scrapbook.datecreated as scrapbookdatecreated, scrapbook.receivedfrom, scrapbook.status, file.size, file.datecreated as filedatecreated, file.mimetype, file.width, file.height, file.length, file.uploadedby, scrapbook.description from scrapbook, file where scrapbook.fileid = file.id ";
            if (id != null) {
               sql = sql + "and scrapbook.id = ? ";
            }

            if (username != null) {
               sql = sql + "and scrapbook.username = ? ";
            }

            sql = sql + "order by scrapbook.id desc";
            ps = conn.prepareStatement(sql);
            int i = 0;
            if (id != null) {
               ++i;
               ps.setInt(i, id);
            }

            if (username != null) {
               ++i;
               ps.setString(i, username);
            }

            rs = ps.executeQuery();

            ScrapbookData scrapbookData;
            for(scrapbook = new ArrayList(); rs.next(); ((List)scrapbook).add(scrapbookData)) {
               FileData fileData = new FileData();
               fileData.id = rs.getString("fileID");
               fileData.dateCreated = rs.getTimestamp("fileDateCreated");
               fileData.mimeType = rs.getString("mimeType");
               fileData.size = (Integer)rs.getObject("size");
               fileData.width = (Integer)rs.getObject("width");
               fileData.height = (Integer)rs.getObject("height");
               fileData.length = rs.getInt("length");
               fileData.uploadedBy = rs.getString("uploadedBy");
               scrapbookData = new ScrapbookData();
               scrapbookData.id = rs.getInt("id");
               scrapbookData.username = rs.getString("username");
               scrapbookData.fileID = rs.getString("fileID");
               scrapbookData.receivedFrom = rs.getString("receivedFrom");
               scrapbookData.dateCreated = rs.getTimestamp("scrapbookDateCreated");
               scrapbookData.file = fileData;
               scrapbookData.description = StringUtil.stripHTML(rs.getString("description"));
               Integer intVal = (Integer)rs.getObject("status");
               if (intVal != null) {
                  scrapbookData.status = ScrapbookData.StatusEnum.fromValue(intVal);
               }
            }

            MemCachedClientWrapper.set(MemCachedKeySpaces.CommonKeySpace.SCRAPBOOK, key, scrapbook);
         }

         if (statusArray != null) {
            List<ScrapbookData.StatusEnum> statusList = Arrays.asList(statusArray);
            Iterator i = ((List)scrapbook).iterator();

            while(i.hasNext()) {
               ScrapbookData scrapbookData = (ScrapbookData)i.next();
               if (!statusList.contains(scrapbookData.status)) {
                  i.remove();
               }
            }
         }

         var32 = scrapbook;
      } catch (SQLException var28) {
         throw new EJBException(var28.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var27) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var26) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var25) {
            conn = null;
         }

      }

      return (List)var32;
   }

   public Double getScrapbookSize(String username) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      Double size = null;

      Double var7;
      try {
         conn = this.dataSourceSlave.getConnection();
         String sql = "select sum(file.size) totalsize from scrapbook, file where scrapbook.fileid = file.id and scrapbook.username = ? and scrapbook.status IN (?,?)";
         ps = conn.prepareStatement(sql);
         ps.setString(1, username);
         ps.setInt(2, ScrapbookData.StatusEnum.PRIVATE.value());
         ps.setInt(3, ScrapbookData.StatusEnum.PUBLIC.value());
         rs = ps.executeQuery();
         if (rs.next()) {
            size = rs.getDouble("totalsize");
         }

         var7 = size;
      } catch (SQLException var22) {
         throw new EJBException(var22.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var21) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var20) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var19) {
            conn = null;
         }

      }

      return var7;
   }

   public List<String> getHandsetVendors() throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("select distinct vendor from handsetvendorprefixes");
         rs = ps.executeQuery();
         ArrayList handsetVendors = new ArrayList();

         while(rs.next()) {
            handsetVendors.add(rs.getString("vendor"));
         }

         ArrayList var5 = handsetVendors;
         return var5;
      } catch (SQLException var20) {
         throw new EJBException(var20.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var19) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var18) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var17) {
            conn = null;
         }

      }
   }

   public List<HandsetInstructionsData> getHandsetInstructions() throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("select * from handsetinstructions");
         rs = ps.executeQuery();
         ArrayList handsetInstructions = new ArrayList();

         while(rs.next()) {
            HandsetInstructionsData handsetInstruction = new HandsetInstructionsData();
            handsetInstruction.id = rs.getInt("id");
            handsetInstruction.instructionText = rs.getString("instructiontext");
            handsetInstruction.description = rs.getString("description");
            handsetInstructions.add(handsetInstruction);
         }

         ArrayList var22 = handsetInstructions;
         return var22;
      } catch (SQLException var20) {
         throw new EJBException(var20.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var19) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var18) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var17) {
            conn = null;
         }

      }
   }

   public HandsetInstructionsData getHandsetInstruction(int id) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      HandsetInstructionsData var6;
      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("select * from handsetinstructions where id = ?");
         ps.setDouble(1, (double)id);
         rs = ps.executeQuery();
         HandsetInstructionsData handsetInstruction = new HandsetInstructionsData();
         if (rs.next()) {
            handsetInstruction.id = rs.getInt("id");
            handsetInstruction.instructionText = rs.getString("instructiontext");
            handsetInstruction.description = rs.getString("description");
         }

         var6 = handsetInstruction;
      } catch (SQLException var21) {
         throw new EJBException(var21.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var20) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var19) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var18) {
            conn = null;
         }

      }

      return var6;
   }

   public List<HandsetVendorPrefixesData> getHandsetVendorPrefixes() throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("select * from handsetvendorprefixes");
         rs = ps.executeQuery();
         ArrayList handsetVendorPrefixes = new ArrayList();

         while(rs.next()) {
            HandsetVendorPrefixesData handsetVendorPrefixesData = new HandsetVendorPrefixesData();
            handsetVendorPrefixesData.vendor = rs.getString("vendor");
            handsetVendorPrefixesData.prefix = rs.getString("prefix");
            handsetVendorPrefixes.add(handsetVendorPrefixesData);
         }

         ArrayList var22 = handsetVendorPrefixes;
         return var22;
      } catch (SQLException var20) {
         throw new EJBException(var20.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var19) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var18) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var17) {
            conn = null;
         }

      }
   }

   public List<HandsetData> getHandsetDetails(String vendor) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("select * from handsets left join handsetinstructions on handsets.instructionId = handsetinstructions.id where handsets.vendor = ? and phonemodel is not null");
         ps.setString(1, vendor);
         rs = ps.executeQuery();
         ArrayList handsetDetails = new ArrayList();

         while(rs.next()) {
            handsetDetails.add(new HandsetData(rs));
         }

         ArrayList var6 = handsetDetails;
         return var6;
      } catch (SQLException var21) {
         throw new EJBException(var21.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var20) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var19) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var18) {
            conn = null;
         }

      }
   }

   public List<HandsetData> getDefaultHandsetDetails() throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("select * from handsets left join handsetinstructions on handsets.instructionId = handsetinstructions.id where handsets.phonemodel is null");
         rs = ps.executeQuery();
         ArrayList handsetDetails = new ArrayList();

         while(rs.next()) {
            handsetDetails.add(new HandsetData(rs));
         }

         ArrayList var5 = handsetDetails;
         return var5;
      } catch (SQLException var20) {
         throw new EJBException(var20.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var19) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var18) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var17) {
            conn = null;
         }

      }
   }

   public HandsetData getHandsetDetail(String vendor, String model) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      HandsetData var6;
      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("select * from handsets left join handsetinstructions on handsets.instructionId = handsetinstructions.id where handsets.vendor = ? and handsets.phonemodel = ?");
         ps.setString(1, vendor);
         ps.setString(2, model);
         rs = ps.executeQuery();
         if (!rs.next()) {
            throw new EJBException("That model of phone/vendor does not exist");
         }

         var6 = new HandsetData(rs);
      } catch (SQLException var21) {
         throw new EJBException(var21.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var20) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var19) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var18) {
            conn = null;
         }

      }

      return var6;
   }

   public HandsetData getHandsetDetail(Integer id) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      HandsetData var5;
      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("select * from handsets left join handsetinstructions on handsets.instructionId = handsetinstructions.id where handsets.id = ?");
         ps.setDouble(1, (double)id);
         rs = ps.executeQuery();
         if (!rs.next()) {
            throw new EJBException("That handset id does not exist");
         }

         var5 = new HandsetData(rs);
      } catch (SQLException var20) {
         throw new EJBException(var20.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var19) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var18) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var17) {
            conn = null;
         }

      }

      return var5;
   }

   public void createHandset(HandsetData handset) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      Object rs = null;

      try {
         conn = this.dataSourceMaster.getConnection();
         if (handset.vendor.length() < 1) {
            throw new EJBException("You must specify a Vendor");
         }

         if (handset.phoneModel == null) {
            throw new EJBException("You must specify a Phone Model");
         }

         ps = conn.prepareStatement("insert into handsets (vendor, phonemodel, instructionid, midletversion, midletaccepttype, midp, cldc,camerasupport, filesystemsupport, pngsupport, gifsupport, jpegsupport, signedmidletsupport, screenwidth, screenheight, comments) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
         if (handset.vendor != null) {
            ps.setString(1, handset.vendor);
         } else {
            ps.setObject(1, (Object)null);
         }

         if (handset.phoneModel != null) {
            ps.setString(2, handset.phoneModel);
         } else {
            ps.setObject(2, (Object)null);
         }

         if (handset.instructionId != null) {
            ps.setDouble(3, (double)handset.instructionId);
         } else {
            ps.setObject(3, (Object)null);
         }

         ps.setString(4, handset.midletVersion);
         ps.setInt(5, handset.midletAcceptType.value());
         ps.setString(6, handset.midp);
         ps.setString(7, handset.cldc);
         if (handset.cameraSupport) {
            ps.setInt(8, 1);
         } else {
            ps.setInt(8, 0);
         }

         if (handset.fileSystemSupport) {
            ps.setInt(9, 1);
         } else {
            ps.setInt(9, 0);
         }

         if (handset.pngSupport) {
            ps.setInt(10, 1);
         } else {
            ps.setInt(10, 0);
         }

         if (handset.gifSupport) {
            ps.setInt(11, 1);
         } else {
            ps.setInt(11, 0);
         }

         if (handset.jpegSupport) {
            ps.setInt(12, 1);
         } else {
            ps.setInt(12, 0);
         }

         if (handset.signedMidletSupport) {
            ps.setInt(13, 1);
         } else {
            ps.setInt(13, 0);
         }

         if (handset.screenWidth != null) {
            ps.setDouble(14, (double)handset.screenWidth);
         } else {
            ps.setObject(14, (Object)null);
         }

         if (handset.screenHeight != null) {
            ps.setDouble(15, (double)handset.screenHeight);
         } else {
            ps.setObject(15, (Object)null);
         }

         if (handset.comments != null) {
            ps.setString(16, handset.comments);
         } else {
            ps.setObject(16, (Object)null);
         }

         if (ps.executeUpdate() < 1) {
            throw new EJBException("Could not create the new Handset");
         }
      } catch (SQLException var20) {
         throw new EJBException(var20.getMessage());
      } finally {
         try {
            if (rs != null) {
               ((ResultSet)rs).close();
            }
         } catch (SQLException var19) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var18) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var17) {
            conn = null;
         }

      }

   }

   public void updateHandsetDetail(HandsetData handset) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      Object rs = null;

      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("update handsets set vendor = ?, phonemodel = ?, instructionid = ?, midletversion = ?, midletaccepttype = ?, midp = ?, cldc = ?,camerasupport = ?, filesystemsupport = ?, pngsupport = ?, gifsupport = ?, jpegsupport = ?, signedmidletsupport = ?, screenwidth = ?, screenheight = ?, comments = ? where id = ?");
         if (handset.vendor != null) {
            ps.setString(1, handset.vendor);
         } else {
            ps.setObject(1, (Object)null);
         }

         if (handset.phoneModel != null) {
            ps.setString(2, handset.phoneModel);
         } else {
            ps.setObject(2, (Object)null);
         }

         if (handset.instructionId != null) {
            ps.setDouble(3, (double)handset.instructionId);
         } else {
            ps.setObject(3, (Object)null);
         }

         ps.setString(4, handset.midletVersion);
         ps.setInt(5, handset.midletAcceptType.value());
         ps.setString(6, handset.midp);
         ps.setString(7, handset.cldc);
         if (handset.cameraSupport) {
            ps.setInt(8, 1);
         } else {
            ps.setInt(8, 0);
         }

         if (handset.fileSystemSupport) {
            ps.setInt(9, 1);
         } else {
            ps.setInt(9, 0);
         }

         if (handset.pngSupport) {
            ps.setInt(10, 1);
         } else {
            ps.setInt(10, 0);
         }

         if (handset.gifSupport) {
            ps.setInt(11, 1);
         } else {
            ps.setInt(11, 0);
         }

         if (handset.jpegSupport) {
            ps.setInt(12, 1);
         } else {
            ps.setInt(12, 0);
         }

         if (handset.signedMidletSupport) {
            ps.setInt(13, 1);
         } else {
            ps.setInt(13, 0);
         }

         if (handset.screenWidth != null) {
            ps.setDouble(14, (double)handset.screenWidth);
         } else {
            ps.setObject(14, (Object)null);
         }

         if (handset.screenHeight != null) {
            ps.setDouble(15, (double)handset.screenHeight);
         } else {
            ps.setObject(15, (Object)null);
         }

         if (handset.comments != null) {
            ps.setString(16, handset.comments);
         } else {
            ps.setObject(16, (Object)null);
         }

         ps.setDouble(17, (double)handset.id);
         if (ps.executeUpdate() < 1) {
            throw new EJBException("Could not update the Handset Details");
         }
      } catch (SQLException var20) {
         throw new EJBException(var20.getMessage());
      } finally {
         try {
            if (rs != null) {
               ((ResultSet)rs).close();
            }
         } catch (SQLException var19) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var18) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var17) {
            conn = null;
         }

      }

   }

   public void createHandsetInstructions(HandsetInstructionsData handsetInstructions) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      Object rs = null;

      try {
         conn = this.dataSourceMaster.getConnection();
         if (handsetInstructions.description.length() < 1 || handsetInstructions.description == null) {
            throw new EJBException("You must provide a Description");
         } else if (handsetInstructions.instructionText.length() >= 1 && handsetInstructions.instructionText != null) {
            ps = conn.prepareStatement("insert into handsetinstructions (description, instructiontext) VALUES (?,?)");
            ps.setString(1, handsetInstructions.description);
            ps.setString(2, handsetInstructions.instructionText);
            if (ps.executeUpdate() < 1) {
               throw new EJBException("Could not create the instructions");
            }
         } else {
            throw new EJBException("You must provide some Instruction Text");
         }
      } catch (SQLException var20) {
         throw new EJBException(var20.getMessage());
      } finally {
         try {
            if (rs != null) {
               ((ResultSet)rs).close();
            }
         } catch (SQLException var19) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var18) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var17) {
            conn = null;
         }

      }
   }

   public void updateHandsetInstructions(HandsetInstructionsData handsetInstructions) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      Object rs = null;

      try {
         conn = this.dataSourceMaster.getConnection();
         if (handsetInstructions.description.length() < 1) {
            throw new EJBException("You must provide a Description");
         }

         if (handsetInstructions.instructionText.length() < 1) {
            throw new EJBException("You must provide some Instruction Text");
         }

         ps = conn.prepareStatement("update handsetinstructions set description = ?, instructiontext = ? where id = ?");
         ps.setString(1, handsetInstructions.description);
         ps.setString(2, handsetInstructions.instructionText);
         ps.setDouble(3, (double)handsetInstructions.id);
         if (ps.executeUpdate() < 1) {
            throw new EJBException("Could not create the instructions");
         }
      } catch (SQLException var20) {
         throw new EJBException(var20.getMessage());
      } finally {
         try {
            if (rs != null) {
               ((ResultSet)rs).close();
            }
         } catch (SQLException var19) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var18) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var17) {
            conn = null;
         }

      }

   }

   public void removeMerchantPinAuthentication(int userId) {
      Connection conn = null;
      PreparedStatement ps = null;
      Object rs = null;

      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("delete from merchantpin where userid = ?");
         ps.setInt(1, userId);
         ps.execute();
      } catch (Exception var20) {
         throw new EJBException(var20.getMessage());
      } finally {
         try {
            if (rs != null) {
               ((ResultSet)rs).close();
            }
         } catch (SQLException var19) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var18) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var17) {
            conn = null;
         }

      }

   }

   public String getMerchantPinEmail(int userId) {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      String var5;
      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("select email from merchantpin where userid = ?");
         ps.setInt(1, userId);
         rs = ps.executeQuery();
         if (rs.next()) {
            var5 = rs.getString("email");
            return var5;
         }

         var5 = "";
      } catch (Exception var23) {
         throw new EJBException(var23.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var22) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var21) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var20) {
            conn = null;
         }

      }

      return var5;
   }

   public void sendMerchantResetPinNotification(int userId) {
      String email = this.getMerchantPinEmail(userId);
      if (email.length() != 0) {
         try {
            User userBean = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
            UserData userData = userBean.loadUserFromID(userId);
            EmailUserNotification note = new EmailUserNotification();
            note.emailAddress = email;
            note.subject = "Merchant PIN reset";
            note.message = "Hi " + userData.username + ",\n\n" + "We have just resetted your merchant PIN.  If you wish you can reuse the Merchant PIN creation wizard to get a new PIN.\n" + "Thanks,\n" + "migme Team";
            EJBIcePrxFinder.getUserNotificationServiceProxy().notifyUserViaEmail(note);
         } catch (Exception var6) {
            throw new EJBException(var6.getMessage());
         }
      }
   }

   public List<EmoteCommandData> getEmoteCommands() throws EJBException {
      Connection connSlave = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         List<EmoteCommandData> commands = new ArrayList();
         connSlave = this.dataSourceSlave.getConnection();
         ps = connSlave.prepareStatement("select * from emotecommand where status=1 order by id asc");
         rs = ps.executeQuery();

         while(rs.next()) {
            EmoteCommandData ecd = new EmoteCommandData(rs);
            commands.add(ecd);
         }

         ArrayList var22 = commands;
         return var22;
      } catch (SQLException var20) {
         throw new EJBException("Unable to load emote command: " + var20.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var19) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var18) {
            ps = null;
         }

         try {
            if (connSlave != null) {
               connSlave.close();
            }
         } catch (SQLException var17) {
            connSlave = null;
         }

      }
   }

   public List<AuthenticatedAccessControlData> getAuthenticatedAccessControlData() throws EJBException {
      Connection connSlave = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         List<AuthenticatedAccessControlData> dataList = new ArrayList();
         connSlave = this.dataSourceSlave.getConnection();
         ps = connSlave.prepareStatement("select * from authenticatedaccesscontrol where status=1");
         rs = ps.executeQuery();

         while(rs.next()) {
            AuthenticatedAccessControlData ecd = new AuthenticatedAccessControlData(rs);
            dataList.add(ecd);
         }

         ArrayList var22 = dataList;
         return var22;
      } catch (SQLException var20) {
         throw new EJBException("Unable to load authenticated access control: " + var20.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var19) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var18) {
            ps = null;
         }

         try {
            if (connSlave != null) {
               connSlave.close();
            }
         } catch (SQLException var17) {
            connSlave = null;
         }

      }
   }

   private String generateHashTagDataCacheKey(String hashtag, int countryid) {
      return String.format("%s:%d", hashtag, countryid);
   }

   public HashTagData getHashTagData(String hashtag, int countryId) throws EJBException {
      try {
         return this.hashTagAPI.getHashTagData(this.dataSourceSlave.getConnection(), hashtag, countryId);
      } catch (Exception var4) {
         throw new EJBException(var4);
      }
   }

   public void updateHashTagData(String hashtag, int countryId, String description) throws EJBException {
      try {
         this.hashTagAPI.updateHashTagData(this.dataSourceMaster.getConnection(), hashtag, countryId, description);
      } catch (Exception var5) {
         throw new EJBException(var5);
      }
   }

   public void updatePromotedPost(List<PromotedPostData> promotedPostDataList) throws EJBException {
      Connection connMaster = null;
      PreparedStatement ps = null;
      Object rs = null;

      try {
         String sql = "replace into promotedpost  values(?,?,?,?,?,?)";
         connMaster = this.dataSourceMaster.getConnection();
         ps = connMaster.prepareStatement(sql);
         Iterator i$ = promotedPostDataList.iterator();

         while(i$.hasNext()) {
            PromotedPostData promotedPostData = (PromotedPostData)i$.next();
            ps.setString(1, promotedPostData.getUrl());
            ps.setInt(2, promotedPostData.getSlot());
            ps.setBoolean(3, promotedPostData.isStatus());
            ps.setTimestamp(4, new Timestamp(promotedPostData.getStartDate().getTime()));
            ps.setTimestamp(5, new Timestamp(promotedPostData.getEndDate().getTime()));
            ps.setInt(6, promotedPostData.getCountryID());
            ps.addBatch();
         }

         ps.executeBatch();
      } catch (Exception var22) {
         throw new EJBException(var22.getMessage(), var22);
      } finally {
         try {
            if (rs != null) {
               ((ResultSet)rs).close();
            }
         } catch (SQLException var21) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var20) {
            ps = null;
         }

         try {
            if (connMaster != null) {
               connMaster.close();
            }
         } catch (SQLException var19) {
            connMaster = null;
         }

      }
   }

   public List<PromotedPostData> getPromotedPost(int limit, boolean archived, int slot) throws EJBException {
      Connection connSlave = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         String sql = "select * from promotedpost where status=? ";
         if (slot != -1) {
            sql = sql + " and slot=?";
         }

         sql = sql + " order by startdate limit ?";
         connSlave = this.dataSourceSlave.getConnection();
         ps = connSlave.prepareStatement(sql);
         int i = 0;
         int i = i + 1;
         ps.setInt(i, archived ? 0 : 1);
         if (slot != -1) {
            ++i;
            ps.setInt(i, slot);
         }

         ++i;
         ps.setInt(i, limit);
         rs = ps.executeQuery();
         ArrayList promotedPostDatas = new ArrayList();

         while(rs.next()) {
            PromotedPostData promotedPostData = new PromotedPostData(rs);
            promotedPostDatas.add(promotedPostData);
         }

         ArrayList var28 = promotedPostDatas;
         return var28;
      } catch (Exception var25) {
         throw new EJBException(var25.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var24) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var23) {
            ps = null;
         }

         try {
            if (connSlave != null) {
               connSlave.close();
            }
         } catch (SQLException var22) {
            connSlave = null;
         }

      }
   }

   public ReputationLevelData getReputationLevelDataForLevel(int level) throws EJBException {
      Connection connSlave = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      ReputationLevelData repuData = null;

      ReputationLevelData var7;
      try {
         String sql = "select * from reputationscoretolevel where level = ?";
         connSlave = this.dataSourceSlave.getConnection();
         ps = connSlave.prepareStatement(sql);
         ps.setInt(1, level);
         rs = ps.executeQuery();
         if (!rs.next()) {
            throw new EJBException("Unable to retrieve ReputationLevelData for level " + level);
         }

         repuData = new ReputationLevelData(rs);
         MemCachedClientWrapper.set(MemCachedKeySpaces.CommonKeySpace.REPUTATION_LEVEL_DATA, "" + level, repuData);
         var7 = repuData;
      } catch (Exception var22) {
         throw new EJBException(var22.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var21) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var20) {
            ps = null;
         }

         try {
            if (connSlave != null) {
               connSlave.close();
            }
         } catch (SQLException var19) {
            connSlave = null;
         }

      }

      return var7;
   }

   public int getMaximumSystemMigLevel() throws EJBException {
      Connection connSlave = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      Integer maxLevel = MemCachedClientWrapper.getInt(MemCachedKeySpaces.CommonKeySpace.MAX_SYSTEM_MIGLEVEL, "");

      int var22;
      try {
         if (maxLevel == null) {
            String sql = "select max(level) as maxlevel from reputationscoretolevel";
            connSlave = this.dataSourceSlave.getConnection();
            ps = connSlave.prepareStatement(sql);
            rs = ps.executeQuery();
            if (!rs.next()) {
               throw new EJBException("Unable to retrieve maximum level ");
            }

            maxLevel = rs.getInt("maxlevel");
            MemCachedClientWrapper.set(MemCachedKeySpaces.CommonKeySpace.MAX_SYSTEM_MIGLEVEL, "", maxLevel);
         }

         var22 = maxLevel;
      } catch (Exception var20) {
         throw new EJBException(var20.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var19) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var18) {
            ps = null;
         }

         try {
            if (connSlave != null) {
               connSlave.close();
            }
         } catch (SQLException var17) {
            connSlave = null;
         }

      }

      return var22;
   }

   public boolean logMig33UserAction(int objectID, Enums.Mig33UserActionMisLogEnum logEnum, String desc) throws Exception {
      Connection connMaster = null;
      PreparedStatement ps = null;

      try {
         String sql = "INSERT INTO mislog(datecreated, objectID, description, action, section, comment) VALUES(NOW(), ?, ?, ?, ?, 'User initiated action.')";
         connMaster = this.dataSourceMaster.getConnection();
         ps = connMaster.prepareStatement(sql);
         ps.setInt(1, objectID);
         ps.setString(2, desc);
         ps.setString(3, logEnum.getAction());
         ps.setString(4, logEnum.getSection());
         if (ps.executeUpdate() > 0) {
            boolean var7 = true;
            return var7;
         }
      } catch (SQLException var22) {
         log.error(String.format("unable to insert mislog entry (%d, %s, %s) due to sql exception", objectID, logEnum.name(), desc), var22);
         throw new FusionEJBException(var22.getMessage());
      } catch (Exception var23) {
         throw new FusionEJBException(var23.getMessage());
      } finally {
         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var21) {
            ps = null;
         }

         try {
            if (connMaster != null) {
               connMaster.close();
            }
         } catch (SQLException var20) {
            connMaster = null;
         }

      }

      return false;
   }

   public static class SingletonHolder {
      private static LazyLoader<List<CountryData>> COUNTRIES_SUPPORTED_HASHTAG_LOADER = new LazyLoader<List<CountryData>>(300000L) {
         protected List<CountryData> fetchValue() throws Exception {
            DataSource dataSourceSlave = LookupUtil.getFusionSlaveDataSource();
            return MISBean.getCountriesSupportedHashtag(dataSourceSlave);
         }
      };

      public static LazyLoader<List<CountryData>> getCountriesSupportedHashtagLoader() {
         return COUNTRIES_SUPPORTED_HASHTAG_LOADER;
      }
   }
}
