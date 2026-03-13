package com.projectgoth.fusion.chat.external.facebook;

import com.projectgoth.fusion.chat.external.ChatConnectionInterface;
import com.projectgoth.fusion.chat.external.ChatConnectionListenerInterface;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.MemCachedRateLimiter;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.fdl.enums.ImType;
import java.util.Collection;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;

public class FacebookListener implements ChatManagerListener, ConnectionListener, MessageListener, PacketListener, RosterListener {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(FacebookListener.class));
   private ChatConnectionInterface source;
   private ChatConnectionListenerInterface listener;

   public FacebookListener(ChatConnectionInterface source, ChatConnectionListenerInterface listener) {
      if (source == null) {
         throw new IllegalArgumentException("Please provide a source");
      } else if (listener == null) {
         throw new IllegalArgumentException("Please provide a listener");
      } else if (source.getImType() != ImType.FACEBOOK) {
         throw new IllegalArgumentException("Please provide a source of IM type Facebook");
      } else {
         this.source = source;
         this.listener = listener;
      }
   }

   public void chatCreated(Chat chat, boolean local) {
      if (!local) {
         chat.addMessageListener(this);
      }
   }

   public void connectionClosed() {
      this.listener.onDisconnected(this.source, "Connection to facebook server closed");
   }

   public void connectionClosedOnError(Exception excep) {
      this.listener.onDisconnected(this.source, excep.getMessage());
   }

   public void reconnectingIn(int arg0) {
   }

   public void reconnectionFailed(Exception excep) {
   }

   public void reconnectionSuccessful() {
   }

   public void processMessage(Chat chat, Message msg) {
      if (msg.getBody() != null) {
         boolean discardMessageAboveThresholdEnabled = SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.IMSettings.DISCARD_MESSAGE_ABOVE_THRESHOLD_ENABLED.forIM(ImType.FACEBOOK));
         String messageReceiveRateLimit = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.IMSettings.MAX_MESSAGES_RECEIVE_RATE_LIMIT.forIM(ImType.FACEBOOK));

         try {
            MemCachedRateLimiter.hit(MemCachedRateLimiter.NameSpace.FACEBOOK_MESSAGE_RECEIVE_PER_USER.toString(), msg.getFrom(), messageReceiveRateLimit);
            this.listener.onMessageReceived(this.source, (String)null, chat.getParticipant(), msg.getBody());
         } catch (MemCachedRateLimiter.LimitExceeded var6) {
            log.warn("Undelivered message due to rate limit [" + messageReceiveRateLimit + "]: from [" + msg.getFrom() + "] to [" + msg.getTo() + "] " + msg.getBody());
            if (!discardMessageAboveThresholdEnabled) {
               this.listener.onMessageReceived(this.source, (String)null, chat.getParticipant(), msg.getBody());
            }
         } catch (MemCachedRateLimiter.FormatError var7) {
            log.warn("Undelivered message due to invalid rate limit format error [" + messageReceiveRateLimit + "]: from [" + msg.getFrom() + "] to [" + msg.getTo() + "] " + msg.getBody(), var7);
         }

      }
   }

   public void processPacket(Packet pkt) {
   }

   public void entriesAdded(Collection<String> entries) {
   }

   public void entriesDeleted(Collection<String> arg0) {
   }

   public void entriesUpdated(Collection<String> arg0) {
   }

   public void presenceChanged(Presence presence) {
      this.listener.onContactStatusChanged(this.source, presence.getFrom(), FacebookStatus.fromXMPPPresence(presence).toFusionPresence());
   }
}
