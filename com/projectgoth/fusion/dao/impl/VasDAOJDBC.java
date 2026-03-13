package com.projectgoth.fusion.dao.impl;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.dao.VasDAO;
import org.apache.log4j.Logger;

public class VasDAOJDBC extends MigJdbcDaoSupport implements VasDAO {
   private Logger log = Logger.getLogger(ConfigUtils.getLoggerName(VasDAOJDBC.class));

   public void init() {
      this.log.info("Initialize data generation for VAS Partners");
      String query = "REPLACE INTO partnerstat (PartnerBuildID, DateCreated) SELECT DISTINCT(pab.PartnerBuildID), date_sub(curdate(), interval 1 day) FROM partneragreementbuild pab, partneragreement pa WHERE pab.PartnerAgreementID = pa.ID AND date_sub(curdate(), interval 1 day) between DATE(pa.StartDate) and DATE(pa.endDate)";
      this.log.debug("[QUERY] " + query);
      this.getJdbcTemplate().execute(query);
      this.log.info("[DONE] Initialize data generation for VAS Partners");
   }

   public void generateUniqueUsers() {
      this.log.info("Generating UniqueUsers for VAS Partners");
      String query = "UPDATE (SELECT COUNT(*) as Total, rd.UserAgent as UserAgent FROM user u, registrationdevice rd, userid uid, partnerbuild pb WHERE DATE(u.LastLoginDate) = date_sub(curdate(), interval 1 day) AND uid.username = u.username AND uid.id = rd.userid AND pb.useragent = rd.useragent GROUP BY rd.UserAgent) t, partnerstat ps, partnerbuild pb SET ps.UniqueUsers = t.Total WHERE ps.PartnerBuildID = pb.ID AND DATE(ps.DateCreated) = date_sub(curdate(), interval 1 day) AND pb.UserAgent = t.UserAgent";
      this.log.debug("[QUERY] " + query);
      this.getJdbcTemplate().execute(query);
      this.log.info("[DONE] Generating UniqueUsers for VAS Partners");
   }

   public void generateRegistration() {
      this.log.info("Generating Registration for VAS Partners");
      String query = "UPDATE (SELECT COUNT(*) as Total, rd.UserAgent as UserAgent FROM user u, userid uid, registrationdevice rd, partnerbuild pb WHERE u.username = uid.username AND uid.id = rd.userid AND pb.useragent = rd.useragent AND DATE(u.dateregistered) = date_sub(curdate(), interval 1 day) GROUP BY rd.UserAgent) t, partnerstat ps, partnerbuild pb SET ps.Registration = t.Total WHERE ps.PartnerBuildID = pb.ID AND DATE(ps.DateCreated) = date_sub(curdate(), interval 1 day) AND pb.UserAgent = t.UserAgent";
      this.log.debug("[QUERY] " + query);
      this.getJdbcTemplate().execute(query);
      this.log.info("[DONE] Generating Registration for VAS Partners");
   }

   public void generateAuthentication() {
      this.log.info("Generating Authentication for VAS Partners");
      String query = "UPDATE (SELECT COUNT(*) as Total, rd.UserAgent as UserAgent FROM user u, activation a, userid uid, registrationdevice rd, partnerbuild pb WHERE DATE(a.DateCreated) = date_sub(curdate(), interval 1 day) AND a.username = uid.username AND u.username = uid.username AND a.mobilephone = u.mobilephone AND uid.id = rd.userid AND pb.useragent = rd.useragent GROUP BY rd.UserAgent) t, partnerstat ps, partnerbuild pb SET ps.Authentication = t.Total WHERE ps.PartnerBuildID = pb.ID AND DATE(ps.DateCreated) = date_sub(curdate(), interval 1 day) AND pb.UserAgent = t.UserAgent";
      this.log.debug("[QUERY] " + query);
      this.getJdbcTemplate().execute(query);
      this.log.info("[DONE] Generating Authentication for VAS Partners");
   }

   public void generateActiveUsers() {
      this.log.info("Generating ActiveUsers for VAS Partners");
      String query1 = "INSERT INTO partneractiveuser (userid, datecreated) SELECT uid.id, date_sub(curdate(), interval 1 day) FROM partnerbuild pb INNER JOIN registrationdevice rd ON (pb.useragent = rd.useragent) INNER JOIN userid uid ON (rd.userid = uid.id) INNER JOIN user u ON (uid.username = u.username) LEFT OUTER JOIN partneractiveuser pau ON (uid.id = pau.userid) WHERE u.mobileverified = 1 AND datediff(u.lastlogindate, u.dateregistered) >= 30 AND datediff(u.lastlogindate, u.dateregistered) <= 60 AND pau.id is null";
      this.log.debug("[QUERY] " + query1);
      this.getJdbcTemplate().execute(query1);
      String query2 = "UPDATE (SELECT COUNT(*) as Total, rd.UserAgent as UserAgent FROM registrationdevice rd, partneractiveuser pau WHERE rd.userid = pau.userid AND DATE(pau.datecreated) = date_sub(curdate(), interval 1 day) GROUP BY rd.UserAgent) t, partnerstat ps, partnerbuild pb SET ps.ActiveUsers = t.Total WHERE ps.PartnerBuildID = pb.ID AND DATE(ps.DateCreated) = date_sub(curdate(), interval 1 day) AND pb.UserAgent = t.UserAgent";
      this.log.debug("[QUERY] " + query2);
      this.getJdbcTemplate().execute(query2);
      this.log.info("[DONE] Generating ActiveUsers for VAS Partners");
   }

   public void generateCreditSpending() {
      this.log.info("Generating Credit Spending for VAS Partners");
      String query = "UPDATE (SELECT ABS(SUM(ae.fundedamount/ae.exchangerate)) as Total, rd.UserAgent as UserAgent FROM registrationdevice rd, partneractiveuser pau, partnerbuild pb, userid uid, accountentry ae WHERE rd.userid = pau.userid   AND uid.id = pau.userid   AND pb.useragent = rd.useragent  AND uid.username = ae.username   AND (ae.type = 41 OR ae.type = 32)   AND DATE(pau.datecreated) = date_sub(curdate(), interval 1 day) GROUP BY rd.UserAgent) t, partnerstat ps, partnerbuild pb SET ps.CreditSpending = t.Total WHERE ps.PartnerBuildID = pb.ID AND DATE(ps.DateCreated) = date_sub(curdate(), interval 1 day) AND pb.UserAgent = t.UserAgent";
      this.log.debug("[QUERY] " + query);
      this.getJdbcTemplate().execute(query);
      this.log.info("[DONE] Generating Credit Spending for VAS Partners");
   }
}
