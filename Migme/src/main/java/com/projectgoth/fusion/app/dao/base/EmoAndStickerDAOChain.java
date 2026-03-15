package com.projectgoth.fusion.app.dao.base;

import com.projectgoth.fusion.app.dao.DAOException;
import com.projectgoth.fusion.data.EmoticonData;
import com.projectgoth.fusion.data.EmoticonPackData;
import java.util.Map;
import java.util.SortedSet;

public class EmoAndStickerDAOChain implements DAOChain {
   private EmoAndStickerDAOChain nextRead;
   private EmoAndStickerDAOChain nextWrite;

   public void setNextRead(DAOChain a) {
      this.nextRead = (EmoAndStickerDAOChain)a;
   }

   public void setNextWrite(DAOChain a) {
      this.nextWrite = (EmoAndStickerDAOChain)a;
   }

   public Map<Integer, EmoticonPackData> loadEmoticonPacks() throws DAOException {
      if (this.nextRead != null) {
         return this.nextRead.loadEmoticonPacks();
      } else {
         throw new DAOException("Unable to loadEmoticonPacks");
      }
   }

   public Map<Integer, EmoticonData> loadEmoticons() throws DAOException {
      if (this.nextRead != null) {
         return this.nextRead.loadEmoticons();
      } else {
         throw new DAOException("Unable to loadEmoticon datas");
      }
   }

   public SortedSet<Integer> loadEmoticonHeights() throws DAOException {
      if (this.nextRead != null) {
         return this.nextRead.loadEmoticonHeights();
      } else {
         throw new DAOException("Unable to load Emoticon heights");
      }
   }

   public int getOptimalEmoticonHeight(int fontHeight) throws DAOException {
      if (this.nextRead != null) {
         return this.nextRead.getOptimalEmoticonHeight(fontHeight);
      } else {
         throw new DAOException(String.format("Unable to load Emoticon heights for fontHeight:%s", fontHeight));
      }
   }
}
