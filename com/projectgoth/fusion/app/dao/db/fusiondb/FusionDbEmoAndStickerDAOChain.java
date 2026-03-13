package com.projectgoth.fusion.app.dao.db.fusiondb;

import com.projectgoth.fusion.app.dao.DAOException;
import com.projectgoth.fusion.app.dao.base.EmoAndStickerDAOChain;
import com.projectgoth.fusion.data.EmoticonData;
import com.projectgoth.fusion.data.EmoticonPackData;
import com.projectgoth.fusion.data.StoreItemData;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import org.apache.log4j.Logger;

public class FusionDbEmoAndStickerDAOChain extends EmoAndStickerDAOChain {
   private static final Logger log = Logger.getLogger(FusionDbEmoAndStickerDAOChain.class);

   public Map<Integer, EmoticonPackData> loadEmoticonPacks() throws DAOException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      Map var5;
      try {
         conn = DBUtils.getFusionReadConnection();
         ps = conn.prepareStatement("select ep.*, e.id as emoticonid, e.type as contenttype, s.catalogimage as thumbnailfile,s.catalogimage as catalogimage from emoticonpack ep join emoticon e on ep.id = e.emoticonpackid  left join storeitem s on (s.referenceid = ep.id and s.type = ?)  where ep.status = ?  and e.type in (1,2,3,4) order by ep.id");
         ps.setInt(1, StoreItemData.TypeEnum.STICKER.value());
         ps.setInt(2, EmoticonPackData.StatusEnum.ACTIVE.value());
         rs = ps.executeQuery();
         HashMap emoticonPacks = new HashMap();

         while(rs.next()) {
            EmoticonPackData emoticonPackData = this.readEmoticonPackFromResultSet(rs);
            emoticonPacks.put(emoticonPackData.getId(), emoticonPackData);
            rs.previous();
         }

         HashMap var13 = emoticonPacks;
         return var13;
      } catch (Exception var10) {
         log.error("Unable to loadEmoticonPacks from fusion db", var10);
         var5 = super.loadEmoticonPacks();
      } finally {
         DBUtils.closeResource(rs, ps, conn, log);
      }

      return var5;
   }

   private EmoticonPackData readEmoticonPackFromResultSet(ResultSet rs) throws SQLException {
      EmoticonPackData emoticonPack = new EmoticonPackData();
      emoticonPack.setId(rs.getInt("id"));
      emoticonPack.setType(EmoticonPackData.TypeEnum.fromValue(rs.getInt("type")));
      emoticonPack.setName(rs.getString("name"));
      emoticonPack.setDescription(rs.getString("description"));
      emoticonPack.setPrice(rs.getDouble("price"));
      emoticonPack.setServiceID(rs.getInt("ServiceID"));
      emoticonPack.setGroupID(rs.getInt("GroupID"));
      emoticonPack.setServiceID(rs.getInt("ServiceID"));
      emoticonPack.setGroupVIPOnly(rs.getBoolean("GroupVIPOnly"));
      emoticonPack.setForSale(rs.getBoolean("ForSale"));
      emoticonPack.setSortOrder(rs.getInt("SortOrder"));
      emoticonPack.setStatus(EmoticonPackData.StatusEnum.fromValue(rs.getInt("status")));
      emoticonPack.setVersion(rs.getInt("version"));
      emoticonPack.setThumbnailFile(rs.getString("thumbnailfile"));
      emoticonPack.setCatalogImage(rs.getString("catalogimage"));
      int contentType = rs.getInt("contenttype");
      if (contentType == 5) {
         emoticonPack.setContentType(EmoticonPackData.ContentTypeEnum.STICKER);
      } else {
         emoticonPack.setContentType(EmoticonPackData.ContentTypeEnum.EMOTICON);
      }

      do {
         emoticonPack.addEmoticonID(rs.getInt("emoticonid"));
      } while(rs.next() && emoticonPack.getId().equals(rs.getInt("id")));

      return emoticonPack;
   }

   public Map<Integer, EmoticonData> loadEmoticons() throws DAOException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      Map var5;
      try {
         conn = DBUtils.getFusionReadConnection();
         ps = conn.prepareStatement("select e.*, ehk.type hotkeytype, ehk.hotkey from emoticon e left outer join emoticonhotkey ehk on e.id = ehk.emoticonid order by e.id");
         rs = ps.executeQuery();
         HashMap emoticons = new HashMap();

         while(rs.next()) {
            EmoticonData emoticonData = new EmoticonData(rs);

            do {
               String hotKey = rs.getString("hotKey");
               if (hotKey != null && hotKey.length() > 0) {
                  EmoticonData.HotKeyTypeEnum type = EmoticonData.HotKeyTypeEnum.fromValue(rs.getInt("hotkeytype"));
                  if (type == EmoticonData.HotKeyTypeEnum.PRIMARY) {
                     emoticonData.hotKey = hotKey;
                  } else {
                     emoticonData.alternateHotKeys.add(hotKey);
                  }
               }
            } while(rs.next() && emoticonData.id.equals((Integer)rs.getObject("id")));

            rs.previous();
            emoticons.put(emoticonData.id, emoticonData);
         }

         HashMap var15 = emoticons;
         return var15;
      } catch (Exception var12) {
         log.error("Unable to loadEmoticonPacks from fusion db", var12);
         var5 = super.loadEmoticons();
      } finally {
         DBUtils.closeResource(rs, ps, conn, log);
      }

      return var5;
   }

   public SortedSet<Integer> loadEmoticonHeights() throws DAOException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      SortedSet var5;
      try {
         conn = DBUtils.getFusionReadConnection();
         ps = conn.prepareStatement("select distinct height from emoticon where type in (1,2,3,4) order by height");
         rs = ps.executeQuery();
         TreeSet emoticonHeights = new TreeSet();

         while(rs.next()) {
            emoticonHeights.add(rs.getInt(1));
         }

         TreeSet var12 = emoticonHeights;
         return var12;
      } catch (Exception var10) {
         log.error("Unable to loadEmoticonHeights from fusion db", var10);
         var5 = super.loadEmoticonHeights();
      } finally {
         DBUtils.closeResource(rs, ps, conn, log);
      }

      return var5;
   }
}
