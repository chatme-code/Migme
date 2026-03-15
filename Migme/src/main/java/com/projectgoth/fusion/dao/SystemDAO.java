package com.projectgoth.fusion.dao;

import java.util.Map;

public interface SystemDAO {
   Map<String, String> getSystemProperties();

   String getSystemProperty(String var1);
}
