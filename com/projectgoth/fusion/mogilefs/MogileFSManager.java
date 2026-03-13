package com.projectgoth.fusion.mogilefs;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

public class MogileFSManager implements DFSManager {
   private MogileFS[] mogileFS;
   private int numberOfTrackers;
   private int lastTrackerUsed = -1;

   public MogileFSManager(String domain, String[] trackers, int connectionsPerTracker, Logger logger) throws NoTrackersException, BadHostFormatException {
      List<MogileFS> l = new ArrayList();

      for(int i = 0; i < connectionsPerTracker; ++i) {
         for(int j = 0; j < trackers.length; ++j) {
            try {
               l.add(new MogileFS(domain, new String[]{trackers[j]}, true, logger));
            } catch (Exception var9) {
               logger.warn("Failed to initialize MogileFS tracker " + trackers[j] + ". Exception: " + var9.toString());
            }
         }
      }

      this.numberOfTrackers = l.size();
      this.mogileFS = (MogileFS[])l.toArray(new MogileFS[0]);
   }

   private synchronized int getLastTrackerUsed() {
      if (this.numberOfTrackers == 0) {
         return 0;
      } else {
         this.lastTrackerUsed = ++this.lastTrackerUsed % this.numberOfTrackers;
         return this.lastTrackerUsed;
      }
   }

   public void storeFile(String key, String storageClass, byte[] content) throws IOException, NoTrackersException, TrackerCommunicationException, StorageCommunicationException {
      int tracker = this.getLastTrackerUsed();

      for(int i = 0; i < this.numberOfTrackers; ++i) {
         try {
            ++tracker;
            OutputStream out = this.mogileFS[tracker % this.numberOfTrackers].newFile(key, storageClass, (long)content.length);
            out.write(content);
            out.close();
            return;
         } catch (NoTrackersException var7) {
            if (i == this.numberOfTrackers - 1) {
               throw var7;
            }
         } catch (TrackerCommunicationException var8) {
            if (i == this.numberOfTrackers - 1) {
               throw var8;
            }
         } catch (StorageCommunicationException var9) {
            if (i == this.numberOfTrackers - 1) {
               throw var9;
            }
         }
      }

      throw new NoTrackersException();
   }

   public byte[] getFile(String key) throws NoTrackersException, TrackerCommunicationException, StorageCommunicationException, IOException, FileNotFoundException {
      int tracker = this.getLastTrackerUsed();

      for(int i = 0; i < this.numberOfTrackers; ++i) {
         try {
            ++tracker;
            byte[] content = this.mogileFS[tracker % this.numberOfTrackers].getFileBytes(key);
            if (content == null) {
               throw new FileNotFoundException();
            }

            return content;
         } catch (NoTrackersException var5) {
            if (i == this.numberOfTrackers - 1) {
               throw var5;
            }
         } catch (TrackerCommunicationException var6) {
            if (i == this.numberOfTrackers - 1) {
               throw var6;
            }
         } catch (StorageCommunicationException var7) {
            if (i == this.numberOfTrackers - 1) {
               throw var7;
            }
         } catch (FileNotFoundException var8) {
            throw var8;
         } catch (IOException var9) {
            if (i == this.numberOfTrackers - 1) {
               throw var9;
            }
         }
      }

      throw new NoTrackersException();
   }
}
