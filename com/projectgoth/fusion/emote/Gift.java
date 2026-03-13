package com.projectgoth.fusion.emote;

import com.projectgoth.fusion.cache.MemCacheOrEJB;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.DateTimeUtils;
import com.projectgoth.fusion.common.IcePrxFinder;
import com.projectgoth.fusion.common.MemCachedKeySpaces;
import com.projectgoth.fusion.common.MemCachedKeyUtils;
import com.projectgoth.fusion.common.MemCachedRateLimiter;
import com.projectgoth.fusion.common.RMIExceptionHelper;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.AccountEntrySourceData;
import com.projectgoth.fusion.data.ChatRoomData;
import com.projectgoth.fusion.data.ChatRoomEmoteLogData;
import com.projectgoth.fusion.data.MessageData;
import com.projectgoth.fusion.data.MessageDestinationData;
import com.projectgoth.fusion.data.StoreCategoryData;
import com.projectgoth.fusion.data.StoreItemData;
import com.projectgoth.fusion.data.StoreItemInventorySummaryData;
import com.projectgoth.fusion.data.StoreRatingSummaryData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.data.UserProfileData;
import com.projectgoth.fusion.data.VirtualGiftData;
import com.projectgoth.fusion.data.VirtualGiftReceivedData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.ejb.FusionEJBException;
import com.projectgoth.fusion.emote.gift.GiftAsync;
import com.projectgoth.fusion.gateway.packet.FusionPktMidletTab;
import com.projectgoth.fusion.interfaces.Content;
import com.projectgoth.fusion.interfaces.ContentHome;
import com.projectgoth.fusion.interfaces.User;
import com.projectgoth.fusion.interfaces.UserHome;
import com.projectgoth.fusion.slice.ChatRoomDataIce;
import com.projectgoth.fusion.slice.ChatRoomPrx;
import com.projectgoth.fusion.slice.ConnectionPrx;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.GroupChatPrx;
import java.rmi.RemoteException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ejb.CreateException;
import org.apache.log4j.Logger;

public class Gift extends EmoteCommand {
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(Gift.class));
   private static final DecimalFormat TWO_DECIMAL_POINT_FORMAT = new DecimalFormat("0.00");
   private static final DecimalFormat ONE_DECIMAL_POINT_FORMAT = new DecimalFormat("0.0");
   private Pattern giftHelpPattern = null;
   private Pattern giftInventoryPattern = null;
   private Pattern giftListPattern = null;
   private Pattern giftSearchPattern = null;
   private Pattern giftReceivedPattern = null;
   private Pattern giftAllPattern = null;
   private Pattern giftCategoryPattern = null;
   private Pattern giftRatePattern = null;
   private Pattern giftToUserPattern = null;
   private Pattern giftInventoryToUserPattern = null;
   private Pattern giftInventoryNoUserPattern = null;

   public Gift(EmoteCommandData emoteCommandData) {
      super(emoteCommandData);
      this.giftHelpPattern = Pattern.compile("^/g(ift)?( help)?$", 2);
      this.giftInventoryPattern = Pattern.compile("^/g(?:ift)? (unlocked|ul)?$", 2);
      this.giftListPattern = Pattern.compile("^/g(?:ift)? (list|featured|popular|new|recent)(?: (\\d+))?$", 2);
      this.giftSearchPattern = Pattern.compile("^/g(?:ift)? (search|display) (.+)$", 2);
      this.giftReceivedPattern = Pattern.compile("^/g(?:ift)? gifts(?: ([a-z0-9_.-]+))?(?: (\\d+))?$", 2);
      this.giftAllPattern = Pattern.compile("^/g(?:ift)? all(?: (.+?))?(?: -m (\\S.+?))?$", 2);
      this.giftCategoryPattern = Pattern.compile("^/g(?:ift)? category(?: (.+?)(?: (\\d+))?)?$", 2);
      this.giftRatePattern = Pattern.compile("^/g(?:ift)? rate (.+?) (\\d+([.,]\\d*)?)$", 2);
      this.giftToUserPattern = Pattern.compile("^/g(?:ift)? ([a-z0-9._-]+) (.+?)(?: -m (\\S.+?))?$", 2);
      this.giftInventoryToUserPattern = Pattern.compile("^/g(?:ift)? -u ([a-z0-9._-]+) (.+?)(?: -m (\\S.+?))?$", 2);
      this.giftInventoryNoUserPattern = Pattern.compile("^/g(?:ift)? -u?$", 2);
   }

   protected EmoteCommand.ResultType execute(MessageData messageData, ChatSource chatSource) throws FusionException {
      try {
         boolean result = false;
         switch(chatSource.getChatType()) {
         case PRIVATE_CHAT:
            result = this.handleVirtualGiftCommand(messageData, chatSource, VirtualGiftReceivedData.PurchaseLocationEnum.PRIVATE_CHAT_COMMAND);
            break;
         case GROUP_CHAT:
            result = this.handleVirtualGiftCommand(messageData, chatSource, VirtualGiftReceivedData.PurchaseLocationEnum.GROUP_CHAT_COMMAND);
            break;
         case CHATROOM_CHAT:
            result = this.handleVirtualGiftCommand(messageData, chatSource, VirtualGiftReceivedData.PurchaseLocationEnum.CHATROOM_COMMAND);
         }

         return !result ? EmoteCommand.ResultType.HANDLED_AND_STOP : EmoteCommand.ResultType.HANDLED_AND_CONTINUE;
      } catch (Exception var4) {
         throw new FusionException(var4.getMessage());
      }
   }

   public EmoteCommandState createDefaultState(ChatSource.ChatType chatType) {
      return null;
   }

   private boolean handleGiftHelpEmote(short clientVersion, ConnectionPrx connectionProxy, MessageData messageData, ChatSource chatSource) throws NoSuchFieldException, FusionException {
      if (clientVersion >= 400) {
         FusionPktMidletTab pktMidletTab = new FusionPktMidletTab();
         pktMidletTab.setURL(SystemProperty.get("WebServerURL") + SystemProperty.get("VirtualGiftEmoteHelpPath", "/sites/midlet/migworld/get_page?page_id=13995"));
         pktMidletTab.setFocus((byte)1);
         connectionProxy.putSerializedPacket(pktMidletTab.toSerializedBytes());
      } else {
         String[] giftCommands = new String[]{"/gift", "/gift ul", "/gift unlocked", "/gift -u [username] [gift name]", "/gift list", "/gift new", "/gift featured", "/gift popular", "/gift recent", "/gift [username] [gift name]", "/gift [username] [gift name] -m [message]", "/gift all [gift name] -m [message]", "/gift search [text]", "/gift display [gift name]", "/gift category", "/gift category [category name]", "/gift rate [gift name] [rating]", "/gift gifts", "/gift gifts [username]"};
         messageData.messageText = "List of allowed gift commands: \n (" + StringUtil.join((Object[])giftCommands, "), \n(") + ")";
         this.emoteCommandData.updateMessageData(messageData);
         chatSource.sendMessageToSender(messageData);
      }

      return false;
   }

   private boolean handleGiftInventoryEmote(int userid, Matcher giftListPatternMatcher, Content contentBean, MessageData messageData, ChatSource chatSource) throws FusionException, RemoteException {
      java.util.List<StoreItemInventorySummaryData> siisd = contentBean.getStoreItemsInventoryByType(userid, StoreItemData.TypeEnum.VIRTUAL_GIFT);
      if (siisd != null && !siisd.isEmpty()) {
         this.createVirtualGiftInventoryListMessage(messageData, siisd);
      } else {
         messageData.messageText = "Sorry, there is no unlock gift available for your migLevel at the moment.";
      }

      this.emoteCommandData.updateMessageData(messageData, false);
      chatSource.sendMessageToSender(messageData);
      return false;
   }

   private boolean handleGiftListEmote(String parentUsername, Matcher giftListPatternMatcher, Content contentBean, MessageData messageData, ChatSource chatSource) throws FusionException, RemoteException {
      String listType = giftListPatternMatcher.group(1);
      int limit = 20;
      if (giftListPatternMatcher.group(2) != null) {
         int input_limit = Integer.parseInt(giftListPatternMatcher.group(2));
         if (input_limit != 5 && input_limit != 10 && input_limit != 20) {
            messageData.messageText = "Limit for [/gift " + listType + "] can only be 5 or 10";
            this.emoteCommandData.updateMessageData(messageData);
            chatSource.sendMessageToSender(messageData);
         } else {
            limit = input_limit;
         }
      }

      java.util.List<VirtualGiftData> gifts = null;
      if (SystemProperty.getBool("GiftEmoteCustomListsEnabled", true)) {
         if (listType.equalsIgnoreCase("featured")) {
            gifts = contentBean.getFeaturedVirtualGifts(parentUsername, 0, limit);
         } else if (listType.equalsIgnoreCase("popular")) {
            gifts = contentBean.getPopularVirtualGifts(parentUsername, 0, limit);
         } else if (listType.equalsIgnoreCase("new")) {
            gifts = contentBean.getNewVirtualGifts(parentUsername, 0, limit);
         } else if (listType.equalsIgnoreCase("recent")) {
            gifts = contentBean.getRecentGiftsSentBy(parentUsername, parentUsername, 0, limit);
         }
      }

      if (gifts == null) {
         gifts = contentBean.getFeaturedPopularNewVirtualGifts(parentUsername, 0, limit);
      }

      if (gifts != null && !gifts.isEmpty()) {
         this.createVirtualGiftListMessage(messageData, gifts);
      } else {
         messageData.messageText = "Sorry, there is no gift available for your migLevel at the moment.";
      }

      this.emoteCommandData.updateMessageData(messageData, false);
      chatSource.sendMessageToSender(messageData);
      return false;
   }

   private boolean handleGiftSearchEmote(String parentUsername, Matcher giftSearchPatternMatcher, Content contentBean, MessageData messageData, ChatSource chatSource) throws RemoteException, FusionException {
      boolean isSearch = giftSearchPatternMatcher.group(1).equalsIgnoreCase("search");
      String search = giftSearchPatternMatcher.group(2);
      int searchMinLength = 3;
      if (isSearch && search.length() < searchMinLength) {
         messageData.messageText = "Search string is to short [" + search + "]. Use at least " + searchMinLength + " characters.";
      } else {
         java.util.List<VirtualGiftData> gifts = contentBean.searchVirtualGifts(parentUsername, 0, search, isSearch ? 20 : 1, isSearch);
         if (gifts.size() <= 0) {
            messageData.messageText = "No gift found for [" + search + "]";
         } else if (gifts.size() == 1) {
            VirtualGiftData gift = (VirtualGiftData)gifts.get(0);
            messageData.messageText = gift.getName() + " " + gift.getHotKey() + " (" + TWO_DECIMAL_POINT_FORMAT.format(gift.getRoundedPrice()) + " " + gift.getCurrency() + ")";
            gift = contentBean.getVirtualGiftDetails(gift);
            StoreCategoryData cat = gift.getStoreCategory();
            if (cat != null) {
               messageData.messageText = messageData.messageText + " -- category: " + cat.name;
            }

            StoreRatingSummaryData ratingSummary = gift.getStoreRatingSummary();
            if (ratingSummary != null) {
               messageData.messageText = messageData.messageText + " -- rating: " + ONE_DECIMAL_POINT_FORMAT.format(ratingSummary.average);
            }

            messageData.emoticonKeys = new LinkedList();
            messageData.emoticonKeys.add(gift.getHotKey());
         } else {
            this.createVirtualGiftListMessage(messageData, gifts);
         }
      }

      this.emoteCommandData.updateMessageData(messageData, false);
      chatSource.sendMessageToSender(messageData);
      return false;
   }

   private boolean handleGiftReceivedEmote(short clientVersion, String parentUsername, Matcher giftReceivedPatternMatcher, ConnectionPrx connectionProxy, User userBean, Content contentBean, MessageData messageData, ChatSource chatSource) throws Exception {
      String username;
      if (giftReceivedPatternMatcher.group(1) == null && clientVersion >= 400) {
         username = SystemProperty.get("WebServerURL") + "/sites/index.php?c=profile&a=gifts_received&v=midlet";
         FusionPktMidletTab pktMidletTab = new FusionPktMidletTab();
         pktMidletTab.setURL(username);
         pktMidletTab.setFocus((byte)1);
         connectionProxy.putSerializedPacket(pktMidletTab.toSerializedBytes());
         return false;
      } else {
         if (giftReceivedPatternMatcher.group(1) == null) {
            username = parentUsername;
         } else {
            username = giftReceivedPatternMatcher.group(1).toLowerCase();
            UserData targetUser = userBean.loadUser(username, true, false);
            if (targetUser == null) {
               throw new Exception(username + " is not a user");
            }

            UserProfileData.StatusEnum profileType = userBean.getUserProfileStatus(username);
            if (profileType.equals(UserProfileData.StatusEnum.PRIVATE) || profileType.equals(UserProfileData.StatusEnum.CONTACTS_ONLY) && !targetUser.broadcastList.contains(parentUsername)) {
               throw new Exception("You are not allowed to see " + username + "'s received gifts");
            }
         }

         int limit = 20;
         if (giftReceivedPatternMatcher.group(2) != null) {
            int input_limit = Integer.parseInt(giftReceivedPatternMatcher.group(2));
            if (input_limit != 5 && input_limit != 10 && input_limit != 20) {
               messageData.messageText = "Limit for [/gift gifts] can only be 5 or 10";
               this.emoteCommandData.updateMessageData(messageData);
               chatSource.sendMessageToSender(messageData);
            } else {
               limit = input_limit;
            }
         }

         java.util.List<VirtualGiftData> gifts = contentBean.getRecentGiftsReceivedBy(username, parentUsername, 0, limit);
         this.createVirtualGiftListMessage(messageData, gifts);
         messageData.messageText = "Recent gifts received by " + username + ": \n" + messageData.messageText;
         this.emoteCommandData.updateMessageData(messageData, false);
         chatSource.sendMessageToSender(messageData);
         return false;
      }
   }

   private boolean handleUnlockedDefaultMessage(MessageData messageData, ChatSource chatSource) throws FusionException {
      messageData.messageText = "You can use the command: /gift -u <username> <gift name> to send a free unlocked gift to anyone in migme. Type /gift unlocked to see all the free unlocked gifts that you have.";
      this.emoteCommandData.updateMessageData(messageData);
      chatSource.sendMessageToSender(messageData);
      String defaultMessage = SystemProperty.get("GiftEmoteUnlockedDefaultMessage", "");
      if (!defaultMessage.equals("")) {
         messageData.messageText = defaultMessage;
         this.emoteCommandData.updateMessageData(messageData);
         chatSource.sendMessageToSender(messageData);
      }

      return false;
   }

   private boolean handleGiftAllEmote(String parentUsername, Matcher giftAllPatternMatcher, VirtualGiftReceivedData.PurchaseLocationEnum purchaseLocation, User userBean, Content contentBean, MessageData messageData, ChatSource chatSource) throws Exception {
      if (giftAllPatternMatcher.group(1) != null) {
         String giftName = giftAllPatternMatcher.group(1);
         String giftMessage = giftAllPatternMatcher.group(2);
         int systemWideRateLimit = SystemProperty.getInt((String)"SystemWideGiftAllRateLimit", 50);
         if (!MemCachedRateLimiter.hit(MemCachedRateLimiter.NameSpace.VIRTUALGIFT.toString(), MemCachedKeyUtils.getFullKeyForKeySpace(MemCachedKeySpaces.RateLimitKeySpace.VIRTUAL_GIFT_RATE_LIMIT, "systemwidegiftall"), (long)systemWideRateLimit, 1000L)) {
            log.warn("/giftall rate limit[" + systemWideRateLimit + "/sec] exceeded. Unable to process request for user: " + messageData.username);
            throw new Exception("Sorry, /giftall is not available right now. Please try again later.");
         } else {
            VirtualGiftData gift = null;

            try {
               gift = this.findVirtualGiftByName(giftName, parentUsername, messageData);
            } catch (Gift.VirtualGiftNotFoundException var21) {
               chatSource.sendMessageToSender(messageData);
               return false;
            }

            int rateLimitSeconds = SystemProperty.getInt((String)"GiftAllRateLimitInSeconds", 60);
            if (!MemCachedRateLimiter.hit(MemCachedRateLimiter.NameSpace.VIRTUALGIFT.toString(), MemCachedKeyUtils.getFullKeyForKeySpace(MemCachedKeySpaces.RateLimitKeySpace.VIRTUAL_GIFT_RATE_LIMIT, parentUsername, "giftall"), 1L, (long)(rateLimitSeconds * 1000))) {
               throw new Exception(String.format("You can only use /gift all every %s. Try again later.", DateTimeUtils.timeInvertalInSecondsToPrettyString(rateLimitSeconds)));
            } else if (!MemCachedRateLimiter.hit(MemCachedRateLimiter.NameSpace.VIRTUALGIFT.toString(), MemCachedKeyUtils.getFullKeyForKeySpace(MemCachedKeySpaces.RateLimitKeySpace.VIRTUAL_GIFT_RATE_LIMIT, parentUsername, gift.getId().toString()), 1L, (long)(rateLimitSeconds * 1000))) {
               throw new Exception(String.format("You can only send the same gift every %s. Try sending a different gift.", DateTimeUtils.timeInvertalInSecondsToPrettyString(rateLimitSeconds)));
            } else {
               double lowPriceThreshold = contentBean.isGiftLowPrice(gift, parentUsername);
               boolean isLowPriceGift = lowPriceThreshold >= 0.0D;
               if (isLowPriceGift && giftMessage != null) {
                  throw new Exception(String.format("Gifts priced %s %s and less can't have custom message. Please try another gift instead if you'd like to attach a message to your gift.", TWO_DECIMAL_POINT_FORMAT.format(lowPriceThreshold), gift.getCurrency()));
               } else {
                  Object allRecipients;
                  if (((MessageDestinationData)messageData.messageDestinations.get(0)).type == MessageDestinationData.TypeEnum.CHAT_ROOM) {
                     allRecipients = Arrays.asList(chatSource.getAllUsernamesInChat(false));
                     if (((java.util.List)allRecipients).size() == 0) {
                        messageData.messageText = "There are no other users in the room.";
                        this.emoteCommandData.updateMessageData(messageData);
                        chatSource.sendMessageToSender(messageData);
                        return false;
                     }
                  } else if (((MessageDestinationData)messageData.messageDestinations.get(0)).type == MessageDestinationData.TypeEnum.GROUP) {
                     try {
                        GroupChatPrx groupChatPrx = chatSource.getSessionI().findGroupChatPrx(((MessageDestinationData)messageData.messageDestinations.get(0)).destination);
                        allRecipients = Arrays.asList(groupChatPrx.getParticipants(parentUsername));
                     } catch (FusionException var20) {
                        log.error("Failed to find group chat '" + ((MessageDestinationData)messageData.messageDestinations.get(0)).destination + "'", var20);
                        messageData.messageText = "Unable to find group chat.";
                        this.emoteCommandData.updateMessageData(messageData);
                        chatSource.sendMessageToSender(messageData);
                        return false;
                     }

                     if (((java.util.List)allRecipients).size() == 0) {
                        messageData.messageText = "There are no other users in this group chat.";
                        this.emoteCommandData.updateMessageData(messageData);
                        chatSource.sendMessageToSender(messageData);
                        return false;
                     }
                  } else {
                     if (((MessageDestinationData)messageData.messageDestinations.get(0)).type != MessageDestinationData.TypeEnum.INDIVIDUAL) {
                        log.debug("Unsupported message destination '" + ((MessageDestinationData)messageData.messageDestinations.get(0)).type.toString() + "' for /gift all");
                        throw new Exception("Unsupported message destination '" + ((MessageDestinationData)messageData.messageDestinations.get(0)).type.toString() + "' for /gift all");
                     }

                     allRecipients = new ArrayList(1);
                     ((java.util.List)allRecipients).add(((MessageDestinationData)messageData.messageDestinations.get(0)).destination);
                  }

                  if (isLowPriceGift && ((java.util.List)allRecipients).size() < 2) {
                     throw new Exception(String.format("Gifts priced %s %s and less are only available via /gift all for more than 1 recipient. Try other gifts instead?", TWO_DECIMAL_POINT_FORMAT.format(lowPriceThreshold), gift.getCurrency()));
                  } else if (chatSource.getSessionI().getBalance() < gift.getPrice() * (double)((java.util.List)allRecipients).size()) {
                     log.warn(chatSource.getSessionI().getUsername() + " doesn't have enough credit[" + chatSource.getSessionI().getBalance() + "] to gift shower[ " + gift.getName() + "] priced [" + gift.getPrice() + " " + gift.getCurrency() + " X " + ((java.util.List)allRecipients).size() + "]");
                     throw new FusionEJBException("You do not have enough credit to purchase the gift");
                  } else {
                     if (chatSource.chatType == ChatSource.ChatType.CHATROOM_CHAT) {
                        try {
                           ChatRoomPrx chatRoomPrx = chatSource.getSessionI().findChatRoomPrx(((MessageDestinationData)messageData.messageDestinations.get(0)).destination);
                           if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.ASYNC_GIFT_ALL_ENABLED)) {
                              chatRoomPrx.submitGiftAllTask(gift.getId(), giftMessage, messageData.toIceObject());
                           } else {
                              giftAll(new ChatRoomData(chatRoomPrx.getRoomData()), giftMessage, chatRoomPrx.getMaximumMessageLength(parentUsername), isLowPriceGift, purchaseLocation, (java.util.List)allRecipients, gift, messageData, this.emoteCommandData, chatSource, chatSource.getSessionI().getIcePrxFinder());
                           }
                        } catch (FusionException var19) {
                           log.error("Failed to find chat room '" + ((MessageDestinationData)messageData.messageDestinations.get(0)).destination + "'", var19);
                           messageData.messageText = "Unable to find chat room " + ((MessageDestinationData)messageData.messageDestinations.get(0)).destination;
                           this.emoteCommandData.updateMessageData(messageData);
                           chatSource.sendMessageToSender(messageData);
                           return false;
                        }
                     } else {
                        giftAll((ChatRoomData)null, giftMessage, -1, isLowPriceGift, purchaseLocation, (java.util.List)allRecipients, gift, messageData, this.emoteCommandData, chatSource, chatSource.getSessionI().getIcePrxFinder());
                     }

                     return false;
                  }
               }
            }
         }
      } else {
         messageData.messageText = "To buy a gift for all users in this room, type \"/gift all <gift name>\". Type \"/gift list\" to see available gifts.";
         this.emoteCommandData.updateMessageData(messageData);
         chatSource.sendMessageToSender(messageData);
         return false;
      }
   }

   public static boolean giftAll(ChatRoomData chatroomData, String giftMessage, int maxMessageLength, boolean lowPriceGift, VirtualGiftReceivedData.PurchaseLocationEnum purchaseLocation, java.util.List<String> allRecipients, VirtualGiftData gift, MessageData messageData, EmoteCommandData emoteCommandData, ChatSource chatSource, IcePrxFinder ipf) throws FusionException {
      return GiftAsync.giftAll(chatroomData, giftMessage, maxMessageLength, lowPriceGift, purchaseLocation, allRecipients, gift, messageData, emoteCommandData, chatSource, ipf);
   }

   private boolean handleGiftCategoryEmote(String parentUsername, Matcher giftCategoryPatternMatcher, Content contentBean, MessageData messageData, ChatSource chatSource) throws Exception {
      if (giftCategoryPatternMatcher.group(1) != null) {
         String categoryName = giftCategoryPatternMatcher.group(1);
         int limit = 20;
         if (giftCategoryPatternMatcher.group(2) != null) {
            int input_limit = Integer.parseInt(giftCategoryPatternMatcher.group(2));
            if (input_limit != 5 && input_limit != 10 && input_limit != 20) {
               messageData.messageText = "Limit for [/gift category] can only be 5 or 10";
               this.emoteCommandData.updateMessageData(messageData);
               chatSource.sendMessageToSender(messageData);
            } else {
               limit = input_limit;
            }
         }

         java.util.List<VirtualGiftData> gifts = contentBean.getVirtualGiftForCategory(parentUsername, categoryName, 0, limit);
         if (gifts.size() <= 0) {
            messageData.messageText = "No items in category [" + categoryName + "]";
         } else {
            this.createVirtualGiftListMessage(messageData, gifts);
         }
      } else {
         try {
            ArrayList<String> categories = contentBean.getVirtualGiftCategoryNames();
            messageData.messageText = StringUtil.join((Collection)categories, "; ");
         } catch (Exception var9) {
            messageData.messageText = "Sorry, unable to get virtualgift categories: " + var9.getMessage();
         }
      }

      this.emoteCommandData.updateMessageData(messageData, false);
      chatSource.sendMessageToSender(messageData);
      return false;
   }

   private boolean handleGiftRateEmote(Matcher giftRatePatternMatcher, Content contentBean, MessageData messageData, ChatSource chatSource) throws Exception {
      String giftName = giftRatePatternMatcher.group(1);
      int rating;
      if (giftRatePatternMatcher.group(3) == null && (rating = Integer.parseInt(giftRatePatternMatcher.group(2))) <= 5 && rating != 0) {
         try {
            boolean rated = contentBean.rateVirtualGift(chatSource.getSessionI().getUserID(), giftName, rating);
            if (rated) {
               messageData.messageText = "Thanks, your rating of " + rating + " for [" + giftName + "] was registered";
            } else {
               messageData.messageText = "No gift found for [" + giftName + "]";
            }
         } catch (Exception var8) {
            messageData.messageText = "Sorry, we couldn't register your rating";
         }

         this.emoteCommandData.updateMessageData(messageData);
         chatSource.sendMessageToSender(messageData);
         return false;
      } else {
         messageData.messageText = "Rating value must be 1, 2, 3, 4, or 5";
         this.emoteCommandData.updateMessageData(messageData);
         chatSource.sendMessageToSender(messageData);
         return false;
      }
   }

   private boolean handleGiftInventoryToUserEmote(String parentUsername, Matcher giftToUserPatternMatcher, Content contentBean, VirtualGiftReceivedData.PurchaseLocationEnum purchaseLocation, MessageData messageData, ChatSource chatSource) throws Exception {
      String recipientUsername = giftToUserPatternMatcher.group(1).toLowerCase();
      String giftName = giftToUserPatternMatcher.group(2);
      String giftMessage = giftToUserPatternMatcher.group(3);
      VirtualGiftData gift = null;

      try {
         gift = this.findVirtualGiftByName(giftName, parentUsername, messageData);
      } catch (Gift.VirtualGiftNotFoundException var20) {
         chatSource.sendMessageToSender(messageData);
         return false;
      }

      int rateLimitSeconds = SystemProperty.getInt((String)"GiftSingleRateLimitInSeconds", 60);
      if (!MemCachedRateLimiter.hit(MemCachedRateLimiter.NameSpace.VIRTUALGIFT.toString(), MemCachedKeyUtils.getFullKeyForKeySpace(MemCachedKeySpaces.RateLimitKeySpace.VIRTUAL_GIFT_RATE_LIMIT, parentUsername, recipientUsername, gift.getId().toString()), 1L, (long)(rateLimitSeconds * 1000))) {
         throw new Exception(String.format("You can only send the same gift to %s every %s. Try sending a different gift.", recipientUsername, DateTimeUtils.timeInvertalInSecondsToPrettyString(rateLimitSeconds)));
      } else {
         java.util.List<String> recipientUsernames = new ArrayList();
         recipientUsernames.add(recipientUsername);
         StoreItemData storeItemData = new StoreItemData();
         storeItemData.id = gift.getStoreitemId();
         storeItemData.referenceData = gift;
         contentBean.giveVirtualGiftForMultipleUsers(chatSource.getSessionI().getUsername(), recipientUsernames, storeItemData, purchaseLocation.value(), false, giftMessage);
         messageData.contentType = MessageData.ContentTypeEnum.EMOTE;
         GiftSentToUserMessageData giftSentToUserMessageData = new GiftSentToUserMessageData(parentUsername, recipientUsername, giftMessage, gift);
         giftSentToUserMessageData.source = messageData.source;
         giftSentToUserMessageData.sourceType = messageData.sourceType;
         giftSentToUserMessageData.sendReceive = messageData.sendReceive;
         giftSentToUserMessageData.type = messageData.type;
         giftSentToUserMessageData.username = messageData.username;
         giftSentToUserMessageData.messageDestinations = messageData.messageDestinations;
         if (SystemProperty.getBool("logSingleGiftEmote", false)) {
            int chatRoomID = -1;
            int chatRoomGroupID = -1;
            if (((MessageDestinationData)messageData.messageDestinations.get(0)).type == MessageDestinationData.TypeEnum.CHAT_ROOM) {
               ChatRoomPrx chatRoomPrx;
               try {
                  chatRoomPrx = chatSource.getSessionI().findChatRoomPrx(((MessageDestinationData)messageData.messageDestinations.get(0)).destination);
               } catch (FusionException var19) {
                  log.error("Failed to find chat room '" + ((MessageDestinationData)messageData.messageDestinations.get(0)).destination + "'", var19);
                  giftSentToUserMessageData.messageText = "Unable to find chat room " + ((MessageDestinationData)messageData.messageDestinations.get(0)).destination;
                  this.emoteCommandData.updateMessageData(giftSentToUserMessageData);
                  chatSource.sendMessageToSender(giftSentToUserMessageData);
                  return false;
               }

               ChatRoomDataIce roomData = chatRoomPrx.getRoomData();
               chatRoomID = roomData.id;
               chatRoomGroupID = roomData.groupID;
            }

            ChatRoomEmoteLogData logData = new ChatRoomEmoteLogData(messageData.source, "", "/gift", chatRoomID, chatRoomGroupID, -1, gift.getId() + ";1");
            chatSource.getSessionI().logEmoteData(logData);
         }

         this.emoteCommandData.updateMessageData(giftSentToUserMessageData, false);
         messageData.setMimeTypeAndData(gift, parentUsername, recipientUsernames, VirtualGiftData.GiftingType.GIFT, giftMessage);
         chatSource.sendMessageToAllUsersInChat(giftSentToUserMessageData);
         return false;
      }
   }

   private boolean handleGiftToUserEmote(String parentUsername, Matcher giftToUserPatternMatcher, Content contentBean, VirtualGiftReceivedData.PurchaseLocationEnum purchaseLocation, MessageData messageData, ChatSource chatSource) throws Exception {
      String recipientUsername = giftToUserPatternMatcher.group(1).toLowerCase();
      String giftName = giftToUserPatternMatcher.group(2);
      String giftMessage = giftToUserPatternMatcher.group(3);
      VirtualGiftData gift = null;

      try {
         gift = this.findVirtualGiftByName(giftName, parentUsername, messageData);
      } catch (Gift.VirtualGiftNotFoundException var18) {
         chatSource.sendMessageToSender(messageData);
         return false;
      }

      int rateLimitSeconds = SystemProperty.getInt((String)"GiftSingleRateLimitInSeconds", 60);
      if (!MemCachedRateLimiter.hit(MemCachedRateLimiter.NameSpace.VIRTUALGIFT.toString(), MemCachedKeyUtils.getFullKeyForKeySpace(MemCachedKeySpaces.RateLimitKeySpace.VIRTUAL_GIFT_RATE_LIMIT, parentUsername, recipientUsername, gift.getId().toString()), 1L, (long)(rateLimitSeconds * 1000))) {
         throw new Exception(String.format("You can only send the same gift to %s every %s. Try sending a different gift.", recipientUsername, DateTimeUtils.timeInvertalInSecondsToPrettyString(rateLimitSeconds)));
      } else if (chatSource.getSessionI().getBalance() < gift.getPrice()) {
         log.warn(parentUsername + " doesn't have enough credit[" + chatSource.getSessionI().getBalance() + "] to purchase [" + gift.getName() + "] priced [" + gift.getPrice() + " " + gift.getCurrency() + "]");
         throw new FusionEJBException("You do not have enough credit to purchase the gift");
      } else {
         if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Temp.SE425_ENABLED)) {
            AccountEntrySourceData accountEntrySourceData;
            if (chatSource.getSessionI() != null) {
               accountEntrySourceData = new AccountEntrySourceData(chatSource.getSessionI());
            } else if (chatSource.getSessionPrx() != null) {
               accountEntrySourceData = new AccountEntrySourceData(chatSource.getSessionPrx());
            } else {
               accountEntrySourceData = new AccountEntrySourceData(Gift.class);
            }

            contentBean.buyVirtualGift(chatSource.getSessionI().getUsername(), recipientUsername, gift.getId(), purchaseLocation.value(), false, giftMessage, (String)null, accountEntrySourceData);
         } else {
            contentBean.buyVirtualGift(chatSource.getSessionI().getUsername(), recipientUsername, gift.getId(), purchaseLocation.value(), false, giftMessage, (String)null, new AccountEntrySourceData(chatSource.getSessionI().getClass()));
         }

         messageData.contentType = MessageData.ContentTypeEnum.EMOTE;
         String returnMsg = formatUserNameWithLevel(parentUsername) + " gives ";
         if (StringUtil.startsWithaVowel(gift.getName())) {
            returnMsg = returnMsg + "an ";
         } else {
            returnMsg = returnMsg + "a ";
         }

         returnMsg = returnMsg + gift.getName() + " " + gift.getHotKey() + " to " + formatUserNameWithLevel(recipientUsername) + "!";
         if (giftMessage != null) {
            returnMsg = returnMsg + " -- " + giftMessage;
         }

         messageData.messageText = "<< " + returnMsg + " >>";
         messageData.emoticonKeys = new LinkedList();
         messageData.emoticonKeys.add(gift.getHotKey());
         if (SystemProperty.getBool("logSingleGiftEmote", false)) {
            int chatRoomID = -1;
            int chatRoomGroupID = -1;
            if (((MessageDestinationData)messageData.messageDestinations.get(0)).type == MessageDestinationData.TypeEnum.CHAT_ROOM) {
               ChatRoomPrx chatRoomPrx;
               try {
                  chatRoomPrx = chatSource.getSessionI().findChatRoomPrx(((MessageDestinationData)messageData.messageDestinations.get(0)).destination);
               } catch (FusionException var17) {
                  log.error("Failed to find chat room '" + ((MessageDestinationData)messageData.messageDestinations.get(0)).destination + "'", var17);
                  messageData.messageText = "Unable to find chat room " + ((MessageDestinationData)messageData.messageDestinations.get(0)).destination;
                  this.emoteCommandData.updateMessageData(messageData);
                  chatSource.sendMessageToSender(messageData);
                  return false;
               }

               ChatRoomDataIce roomData = chatRoomPrx.getRoomData();
               chatRoomID = roomData.id;
               chatRoomGroupID = roomData.groupID;
            }

            ChatRoomEmoteLogData logData = new ChatRoomEmoteLogData(messageData.source, "", "/gift", chatRoomID, chatRoomGroupID, -1, gift.getId() + ";1");
            chatSource.getSessionI().logEmoteData(logData);
         }

         this.emoteCommandData.updateMessageData(messageData, false);
         messageData.setMimeTypeAndData(gift, parentUsername, Arrays.asList(recipientUsername), VirtualGiftData.GiftingType.GIFT, giftMessage);
         chatSource.sendMessageToAllUsersInChat(messageData);
         return false;
      }
   }

   private boolean handleVirtualGiftCommand(MessageData messageData, ChatSource chatSource, VirtualGiftReceivedData.PurchaseLocationEnum purchaseLocation) throws Exception {
      try {
         Content contentBean = (Content)EJBHomeCache.getObject("ejb/Content", ContentHome.class);
         User userBean = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
         short clientVersion = chatSource.getSessionI().getClientVersion();
         String parentUsername = chatSource.getParentUsername();
         ConnectionPrx connectionProxy = chatSource.getSessionI().getConnectionProxy();
         String messageText = messageData.messageText.trim().replace("\\s+", " ");
         if (this.giftHelpPattern.matcher(messageText).matches()) {
            return this.handleGiftHelpEmote(clientVersion, connectionProxy, messageData, chatSource);
         } else {
            Matcher giftListPatternMatcher = this.giftListPattern.matcher(messageText);
            if (giftListPatternMatcher.matches()) {
               return this.handleGiftListEmote(parentUsername, giftListPatternMatcher, contentBean, messageData, chatSource);
            } else {
               Matcher giftToUserPatternMatcher;
               if (SystemProperty.getBool("GiftEmoteSearchEnabled", true)) {
                  giftToUserPatternMatcher = this.giftSearchPattern.matcher(messageText);
                  if (giftToUserPatternMatcher.matches()) {
                     return this.handleGiftSearchEmote(parentUsername, giftToUserPatternMatcher, contentBean, messageData, chatSource);
                  }
               }

               if (SystemProperty.getBool("GiftEmoteGiftsEnabled", true)) {
                  giftToUserPatternMatcher = this.giftReceivedPattern.matcher(messageText);
                  if (giftToUserPatternMatcher.matches()) {
                     return this.handleGiftReceivedEmote(clientVersion, parentUsername, giftToUserPatternMatcher, connectionProxy, userBean, contentBean, messageData, chatSource);
                  }
               }

               if (SystemProperty.getBool("GiftEmoteAllEnabled", true)) {
                  giftToUserPatternMatcher = this.giftAllPattern.matcher(messageText);
                  if (giftToUserPatternMatcher.matches()) {
                     return this.handleGiftAllEmote(parentUsername, giftToUserPatternMatcher, purchaseLocation, userBean, contentBean, messageData, chatSource);
                  }
               }

               if (SystemProperty.getBool("GiftEmoteCategoryEnabled", true)) {
                  giftToUserPatternMatcher = this.giftCategoryPattern.matcher(messageText);
                  if (giftToUserPatternMatcher.matches()) {
                     return this.handleGiftCategoryEmote(parentUsername, giftToUserPatternMatcher, contentBean, messageData, chatSource);
                  }
               }

               if (SystemProperty.getBool("GiftEmoteRateEnabled", true)) {
                  giftToUserPatternMatcher = this.giftRatePattern.matcher(messageText);
                  if (giftToUserPatternMatcher.matches()) {
                     return this.handleGiftRateEmote(giftToUserPatternMatcher, contentBean, messageData, chatSource);
                  }
               }

               if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Emote.GIFT_INVENTORY_ENABLED)) {
                  giftToUserPatternMatcher = this.giftInventoryPattern.matcher(messageText);
                  if (giftToUserPatternMatcher.matches()) {
                     return this.handleGiftInventoryEmote(chatSource.getSessionI().getUserID(), giftToUserPatternMatcher, contentBean, messageData, chatSource);
                  }

                  Matcher giftInventoryToUserPatternMatcher = this.giftInventoryToUserPattern.matcher(messageText);
                  if (giftInventoryToUserPatternMatcher.matches()) {
                     return this.handleGiftInventoryToUserEmote(parentUsername, giftInventoryToUserPatternMatcher, contentBean, purchaseLocation, messageData, chatSource);
                  }

                  Matcher giftInventoryNoUserPatternMatcher = this.giftInventoryNoUserPattern.matcher(messageText);
                  if (giftInventoryNoUserPatternMatcher.matches()) {
                     return this.handleUnlockedDefaultMessage(messageData, chatSource);
                  }
               }

               giftToUserPatternMatcher = this.giftToUserPattern.matcher(messageText);
               if (giftToUserPatternMatcher.matches()) {
                  return this.handleGiftToUserEmote(parentUsername, giftToUserPatternMatcher, contentBean, purchaseLocation, messageData, chatSource);
               } else {
                  messageData.messageText = "To buy a gift for another user type \"/gift <username> <gift name>\". Type \"/gift list\" to see available gifts.";
                  this.emoteCommandData.updateMessageData(messageData);
                  chatSource.sendMessageToSender(messageData);
                  String defaultMessage = SystemProperty.get("GiftEmoteDefaultMessage", "");
                  if (!defaultMessage.equals("")) {
                     messageData.messageText = defaultMessage;
                     this.emoteCommandData.updateMessageData(messageData);
                     chatSource.sendMessageToSender(messageData);
                  }

                  return false;
               }
            }
         }
      } catch (RemoteException var14) {
         throw new Exception(RMIExceptionHelper.getRootMessage(var14));
      }
   }

   private void createVirtualGiftInventoryListMessage(MessageData messageData, java.util.List<StoreItemInventorySummaryData> siisdList) {
      String msg = "";
      boolean doneOne = false;
      messageData.emoticonKeys = new LinkedList();
      messageData.messageColour = 3965073;
      long totalGift = 0L;
      Iterator i$ = siisdList.iterator();

      while(i$.hasNext()) {
         StoreItemInventorySummaryData siisd = (StoreItemInventorySummaryData)i$.next();
         VirtualGiftData gift = (VirtualGiftData)siisd.getStoreItemData().referenceData;
         totalGift += (long)siisd.getCount();
         if (doneOne) {
            msg = msg + "\n";
         }

         msg = msg + gift.getHotKey() + " " + gift.getName() + " (" + TWO_DECIMAL_POINT_FORMAT.format(gift.getRoundedPrice()) + " " + gift.getCurrency() + "):" + siisd.getCount();
         doneOne = true;
         messageData.emoticonKeys.add(gift.getHotKey());
      }

      messageData.messageText = "You have " + totalGift + " unlocked gift.\n" + msg;
   }

   private void createVirtualGiftListMessage(MessageData messageData, java.util.List<VirtualGiftData> gifts) {
      String msg = "";
      boolean doneOne = false;
      messageData.emoticonKeys = new LinkedList();
      Iterator i$ = gifts.iterator();

      while(i$.hasNext()) {
         VirtualGiftData gift = (VirtualGiftData)i$.next();
         if (doneOne) {
            msg = msg + "; ";
         }

         msg = msg + gift.getName() + " " + gift.getHotKey() + " (" + TWO_DECIMAL_POINT_FORMAT.format(gift.getRoundedPrice()) + " " + gift.getCurrency() + ")";
         doneOne = true;
         messageData.emoticonKeys.add(gift.getHotKey());
      }

      messageData.messageText = msg;
   }

   private VirtualGiftData findVirtualGiftByName(String giftName, String userName, MessageData messageData) throws Gift.VirtualGiftNotFoundException, RemoteException, CreateException, FusionEJBException {
      Content contentBean = (Content)EJBHomeCache.getObject("ejb/Content", ContentHome.class);
      VirtualGiftData gift = contentBean.getVirtualGift((Integer)null, giftName, userName);
      if (gift == null) {
         Object gifts;
         if (SystemProperty.getBool("GiftEmoteGiftSearchEnabled", true)) {
            gifts = contentBean.searchVirtualGifts(userName, 0, giftName, 5, true);
         } else {
            gifts = new LinkedList();
         }

         if (((java.util.List)gifts).size() <= 0) {
            messageData.messageText = "Sorry, there is no gift matching [" + giftName + "]";
            throw new Gift.VirtualGiftNotFoundException("No gift matching", messageData);
         }

         if (((java.util.List)gifts).size() != 1) {
            this.createVirtualGiftListMessage(messageData, (java.util.List)gifts);
            messageData.messageText = "Sorry, there is no gift matching [" + giftName + "], " + "here are some suggestions: " + messageData.messageText;
            throw new Gift.VirtualGiftNotFoundException("Multiple gifts matching", messageData);
         }

         gift = (VirtualGiftData)((java.util.List)gifts).get(0);
      }

      return gift;
   }

   public static String formatUserNameWithLevel(String username) {
      try {
         int userReputationLevel = MemCacheOrEJB.getUserReputationLevel(username);
         return username + " [" + userReputationLevel + "]";
      } catch (Exception var2) {
         return username;
      }
   }

   private class VirtualGiftNotFoundException extends Exception {
      private static final long serialVersionUID = 1L;
      private MessageData messageData;

      public VirtualGiftNotFoundException(String errMsg, MessageData messageData) {
         super(errMsg);
         this.messageData = messageData;
      }
   }
}
