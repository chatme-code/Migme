package com.projectgoth.fusion.dao.impl;

import com.projectgoth.fusion.dao.SystemDAO;
import java.util.Map;

public class SystemDAOJDBC extends MigJdbcDaoSupport implements SystemDAO {
   public Map<String, String> getSystemProperties() {
      return this.getJdbcTemplate().queryForMap(this.getExternalizedQuery("SystemDAO.getSystemProperties"));
   }

   public String getSystemProperty(String key) {
      return (String)this.getJdbcTemplate().queryForObject(this.getExternalizedQuery("SystemDAO.getSystemProperty"), new Object[]{key}, String.class);
   }
}
