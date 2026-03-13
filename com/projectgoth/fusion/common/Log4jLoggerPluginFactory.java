package com.projectgoth.fusion.common;

import Ice.Communicator;
import Ice.Logger;
import Ice.LoggerPlugin;
import Ice.Plugin;
import Ice.PluginFactory;

public class Log4jLoggerPluginFactory implements PluginFactory {
   public Plugin create(Communicator communicator, String name, String[] args) {
      Logger logger = new IceLog4jLogger();
      return new LoggerPlugin(communicator, logger);
   }
}
