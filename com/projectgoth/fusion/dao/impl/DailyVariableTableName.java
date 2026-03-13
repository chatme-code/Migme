package com.projectgoth.fusion.dao.impl;

import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import java.util.Calendar;
import javax.sql.DataSource;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

public class DailyVariableTableName extends VariableTableName {
   private static final Logger log = Logger.getLogger(DailyVariableTableName.class);
   private final String baseTablename;
   private final JdbcTemplate jdbcTemplate;
   private String tableCreateDDL;

   public DailyVariableTableName(DataSource dataSource, String baseTablename) {
      this.baseTablename = baseTablename;
      this.jdbcTemplate = new JdbcTemplate(dataSource);
   }

   public synchronized void setTableCreateDDL(String tableCreateDDL) {
      this.tableCreateDDL = tableCreateDDL;
   }

   public synchronized void updateTableName() {
      Calendar today = Calendar.getInstance();
      int year = today.get(1);
      int month = today.get(2) + 1;
      int day = today.get(5);
      String monthString = month < 10 ? "0" + month : "" + month;
      String dayString = day < 10 ? "0" + day : "" + day;
      String newTableName = this.baseTablename + year + monthString + dayString;
      this.setTablename(newTableName);
      this.setLastUpdated(today.getTime());
      this.jdbcTemplate.execute("create table if not exists " + newTableName + this.tableCreateDDL);
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Temp.ER68_ENABLED)) {
         today.add(5, 1);
         year = today.get(1);
         month = today.get(2) + 1;
         day = today.get(5);
         monthString = month < 10 ? "0" + month : "" + month;
         dayString = day < 10 ? "0" + day : "" + day;
         newTableName = this.baseTablename + year + monthString + dayString;
         this.jdbcTemplate.execute("create table if not exists " + newTableName + this.tableCreateDDL);
      }

      if (log.isDebugEnabled()) {
         log.debug("table name was updated, now " + newTableName);
      }

   }
}
