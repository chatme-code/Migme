package com.projectgoth.fusion.dao;

import com.projectgoth.fusion.data.BotData;
import java.util.List;
import java.util.Map;

public interface BotDAO {
   List<BotData> getBots();

   Map<String, String> getBotConfig(long var1);

   Map<String, String> getBotCommands(long var1, String var3);

   Map<String, String> getBotMessages(long var1, String var3);
}
