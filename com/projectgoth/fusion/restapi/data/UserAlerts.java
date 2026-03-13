package com.projectgoth.fusion.restapi.data;

import com.projectgoth.fusion.common.StringUtil;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(
   name = "alerts"
)
public class UserAlerts {
   @XmlElement
   public int unread;
   @XmlElement
   public Map<Integer, Map<String, Map<String, String>>> alerts;
   @XmlElement
   public int offset;
   @XmlElement
   public int total;

   public UserAlerts() {
      this.alerts = new HashMap();
   }

   public UserAlerts(UserAlerts a) {
      this.unread = a.unread;
      this.alerts = a.alerts;
      this.offset = a.offset;
      this.total = a.total;
   }

   public UserAlerts(int unread, Map<Integer, Map<String, Map<String, String>>> alerts) {
      this.unread = unread;
      this.alerts = alerts;
   }

   public UserAlerts retrievePage(int offset, int limit) {
      UserAlerts page = new UserAlerts(this);
      if (this.alerts == null) {
         return page;
      } else if (this.alerts.size() == 0) {
         return page;
      } else {
         List<Alert> allAlerts = this.retrieveAlertsAsList();
         if (offset > allAlerts.size()) {
            return new UserAlerts();
         } else {
            if (offset + limit > allAlerts.size()) {
               limit = allAlerts.size() - offset;
            }

            List<Alert> sublist = allAlerts.subList(offset, offset + limit);
            Map<Integer, Map<String, Map<String, String>>> finalAlerts = new HashMap();
            Iterator i$ = sublist.iterator();

            while(i$.hasNext()) {
               Alert a = (Alert)i$.next();
               Integer alertType = a.alertType;
               if (!finalAlerts.containsKey(alertType)) {
                  finalAlerts.put(alertType, new HashMap());
               }

               Map<String, Map<String, String>> map = (Map)finalAlerts.get(alertType);
               map.put(a.alertKey, a.alertData);
            }

            page.offset = offset;
            page.total = allAlerts.size();
            page.alerts = finalAlerts;
            return page;
         }
      }
   }

   public List<Alert> retrieveAlertsAsList() {
      List<Alert> allAlerts = new LinkedList();
      Iterator i$ = this.alerts.entrySet().iterator();

      while(i$.hasNext()) {
         Entry<Integer, Map<String, Map<String, String>>> e = (Entry)i$.next();
         Integer alertType = (Integer)e.getKey();
         Iterator i$ = ((Map)e.getValue()).entrySet().iterator();

         while(i$.hasNext()) {
            Entry<String, Map<String, String>> e2 = (Entry)i$.next();
            String alertKey = (String)e2.getKey();
            String timestamp = (String)((Map)e2.getValue()).get("timestamp");
            if (StringUtil.toLongOrDefault(timestamp, -1L) > 0L) {
               Alert alert = new Alert(timestamp, alertType, alertKey, (Map)e2.getValue());
               allAlerts.add(alert);
            }
         }
      }

      Collections.sort(allAlerts);
      return allAlerts;
   }
}
