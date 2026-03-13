package com.projectgoth.fusion.dao;

import com.projectgoth.fusion.domain.CountryLogins;
import com.projectgoth.fusion.userevent.system.domain.AllowListEntry;
import java.util.Date;
import java.util.List;

public interface UserDAO {
   List<CountryLogins> findRecentLoginsGroupedByCountry(Date var1, Date var2, int var3);

   List<AllowListEntry> getAllowListForUser(String var1);

   String getMobileNumberForUser(String var1);

   boolean isVerfiedEmailAddress(String var1);

   boolean isBounceEmailAddress(String var1);
}
