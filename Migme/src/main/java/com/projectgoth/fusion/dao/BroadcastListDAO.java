package com.projectgoth.fusion.dao;

import java.util.Set;

public interface BroadcastListDAO {
   Set<String> getBroadcastListForUser(String var1);

   Set<String> getBroadcastListForGroup(int var1);
}
