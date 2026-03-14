package com.projectgoth.fusion.mogilefs;

import java.io.IOException;

public interface DFSManager {
   void storeFile(String var1, String var2, byte[] var3) throws IOException, NoTrackersException, TrackerCommunicationException, StorageCommunicationException;
}
