package com.projectgoth.fusion.dao;

import com.projectgoth.fusion.domain.CountryLogins;
import java.util.Date;
import java.util.List;

public interface SessionSummaryDAO {
   void updateTotalSession(Date var1, int var2, int var3, int var4);

   void updateUniqueTotals(Date var1, int var2, boolean var3, List<CountryLogins> var4);

   void createDailyRows(Date var1);

   boolean rowsExist(Date var1);

   boolean yesterdaysTotalsExists(Date var1);
}
