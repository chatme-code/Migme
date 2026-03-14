package com.projectgoth.fusion.app.dao;

import com.projectgoth.fusion.app.dao.base.EmoAndStickerDAOChain;
import com.projectgoth.fusion.common.LazyLoader;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.EmoticonData;
import com.projectgoth.fusion.data.EmoticonPackData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

public class EmoAndStickerDAO {
   private EmoAndStickerDAOChain readChain;
   private EmoAndStickerDAOChain writeChain;
   private final LazyLoader<Map<Integer, EmoticonPackData>> local_emoticon_packs;
   private final LazyLoader<Map<Integer, EmoticonData>> local_emoticons;
   private final LazyLoader<SortedSet<Integer>> local_emoticon_heights;

   public EmoAndStickerDAO(EmoAndStickerDAOChain readChain, EmoAndStickerDAOChain writeChain) {
      this.local_emoticon_packs = new LazyLoader<Map<Integer, EmoticonPackData>>("LOCAL_EMOTICON_PACKS", SystemProperty.getLong((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.DAOSettings.EMOTICON_PACKS_REFRESH_PERIOD_IN_MS)) {
         protected Map<Integer, EmoticonPackData> fetchValue() throws DAOException {
            if (EmoAndStickerDAO.this.readChain != null) {
               return EmoAndStickerDAO.this.readChain.loadEmoticonPacks();
            } else {
               throw new DAOException("Unable to loadEmoticonPacks");
            }
         }
      };
      this.local_emoticons = new LazyLoader<Map<Integer, EmoticonData>>("LOCAL_EMOTICONS", SystemProperty.getLong((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.DAOSettings.EMOTICONS_REFRESH_PERIOD_IN_MS)) {
         protected Map<Integer, EmoticonData> fetchValue() throws DAOException {
            if (EmoAndStickerDAO.this.readChain != null) {
               return EmoAndStickerDAO.this.readChain.loadEmoticons();
            } else {
               throw new DAOException("Unable to loadEmoticons");
            }
         }
      };
      this.local_emoticon_heights = new LazyLoader<SortedSet<Integer>>("LOCAL_EMOTICON_HEIGHTS", Long.MAX_VALUE) {
         protected SortedSet<Integer> fetchValue() throws DAOException {
            if (EmoAndStickerDAO.this.readChain != null) {
               return EmoAndStickerDAO.this.readChain.loadEmoticonHeights();
            } else {
               throw new DAOException("Unable to loadEmoticonHeights");
            }
         }
      };
      this.readChain = readChain;
      this.writeChain = writeChain;
   }

   public SortedSet<Integer> loadEmoticonHeights() throws DAOException {
      return (SortedSet)this.local_emoticon_heights.getValue();
   }

   public Map<Integer, EmoticonPackData> loadEmoticonPacks() throws DAOException {
      return (Map)this.local_emoticon_packs.getValue();
   }

   public Map<Integer, EmoticonData> loadEmoticons() throws DAOException {
      return (Map)this.local_emoticons.getValue();
   }

   public List<EmoticonData> getEmoticonPack(int emoticonPackId) throws DAOException {
      EmoticonPackData emoticonPackData = (EmoticonPackData)this.loadEmoticonPacks().get(emoticonPackId);
      if (emoticonPackData != null) {
         List<Integer> emoticonIDs = emoticonPackData.getEmoticonIDs();
         return this.getEmoticons(emoticonIDs);
      } else {
         return Collections.emptyList();
      }
   }

   private List<EmoticonData> getEmoticons(List<Integer> emoticonIDList) throws DAOException {
      List<EmoticonData> emoticonDatas = new ArrayList(emoticonIDList.size());
      Map<Integer, EmoticonData> emoMap = this.loadEmoticons();
      Iterator i$ = emoticonIDList.iterator();

      while(i$.hasNext()) {
         Integer emoticonID = (Integer)i$.next();
         EmoticonData emoticonData = (EmoticonData)emoMap.get(emoticonID);
         if (emoticonData != null) {
            emoticonDatas.add(emoticonData);
         }
      }

      return emoticonDatas;
   }

   public int getOptimalEmoticonHeight(int fontHeight) throws DAOException {
      try {
         Integer previous = 0;

         Integer height;
         for(Iterator i$ = this.loadEmoticonHeights().iterator(); i$.hasNext(); previous = height) {
            height = (Integer)i$.next();
            if (height > fontHeight) {
               return previous == 0 ? (Integer)this.loadEmoticonHeights().first() : previous;
            }
         }

         return previous;
      } catch (DAOException var5) {
         return this.readChain.getOptimalEmoticonHeight(fontHeight);
      }
   }
}
