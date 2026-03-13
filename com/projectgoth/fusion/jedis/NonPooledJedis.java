package com.projectgoth.fusion.jedis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisShardInfo;

public class NonPooledJedis extends Jedis {
   public NonPooledJedis(String host) {
      super(host);
   }

   public NonPooledJedis(JedisShardInfo shardInfo) {
      super(shardInfo);
   }

   public NonPooledJedis(String host, int port) {
      super(host, port);
   }

   public NonPooledJedis(String host, int port, int timeout) {
      super(host, port, timeout);
   }
}
