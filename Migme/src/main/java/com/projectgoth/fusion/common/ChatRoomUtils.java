package com.projectgoth.fusion.common;

import com.projectgoth.fusion.data.ChatRoomData;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatRoomUtils {
   private static final Pattern CHATROOM_NAME_PATTERN = Pattern.compile("^[a-zA-Z]{1}[ \\w.-]{2,}$");
   private static final String BANNED_CHATROOM_NAME_SEP = ";";

   public static String normalizeChatRoomName(String chatRoomInput) {
      return chatRoomInput == null ? null : chatRoomInput.trim().toLowerCase();
   }

   public static void invalidateChatRoomCache(String chatRoomName) {
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatSyncSettings.CHATROOM_SYNC_ENABLED)) {
         ChatRoomData cached = (ChatRoomData)MemCachedClientWrapper.get(MemCachedKeySpaces.CommonKeySpace.CHATROOM, normalizeChatRoomName(chatRoomName));
         if (cached != null) {
            MemCachedClientWrapper.delete(MemCachedKeySpaces.CommonKeySpace.CHATROOM_BY_ID, Integer.toString(cached.id));
         }
      }

      MemCachedClientWrapper.delete(MemCachedKeySpaces.CommonKeySpace.CHATROOM, normalizeChatRoomName(chatRoomName));
   }

   public static String validateChatRoomNameForCreation(String chatRoomName) throws ChatRoomValidationException {
      String trimmedChatRoomName = validateChatRoomNameCharacters(chatRoomName);
      validateChatRoomNameForBannedWords(trimmedChatRoomName);
      return trimmedChatRoomName;
   }

   public static String validateChatRoomNameForAccess(String chatRoomName) throws ChatRoomValidationException {
      String trimmedChatRoomName = validateChatRoomNameCharacters(chatRoomName);
      validateChatRoomNameNotInNegativeCache(trimmedChatRoomName);
      return trimmedChatRoomName;
   }

   private static String validateChatRoomNameCharacters(String chatRoomName) throws ChatRoomValidationException {
      if (chatRoomName == null) {
         throw new ChatRoomValidationException("The chat room name is not specified");
      } else if (chatRoomName.length() == 0) {
         throw new ChatRoomValidationException("The chat room name is blank");
      } else if (chatRoomName.length() > MemCachedClientWrapper.MAX_KEY_LENGTH) {
         throw new ChatRoomValidationException("The chat room name is too long");
      } else {
         String trimmedChatRoomName = chatRoomName.trim();
         Matcher matcher = CHATROOM_NAME_PATTERN.matcher(trimmedChatRoomName);
         if (!matcher.matches()) {
            throw new ChatRoomValidationException("Chat room names must start with a letter and contain any combination of at least 3 letters, numbers, periods (.), hyphens (-), underscores (_), or spaces");
         } else {
            int maxChatRoomNameLength = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Chatroom.MAX_NAME_LENGTH);
            if (trimmedChatRoomName.length() > maxChatRoomNameLength) {
               throw new ChatRoomValidationException("The chat room name must not contain more than " + maxChatRoomNameLength + " characters");
            } else {
               return trimmedChatRoomName;
            }
         }
      }
   }

   private static void validateChatRoomNameNotInNegativeCache(String chatRoomName) throws ChatRoomValidationException {
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Chatroom.NEGATIVE_CACHE_ENABLED) && MemCachedClientWrapper.get(MemCachedKeySpaces.CommonKeySpace.CHATROOM_NEGATIVE_CACHE, normalizeChatRoomName(chatRoomName)) != null) {
         throw new ChatRoomValidationException("Chat room is not available yet.");
      }
   }

   private static void validateChatRoomNameForBannedWords(String trimmedChatRoomName) throws ChatRoomValidationException {
      String sysprop = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Chatroom.BANNED_NAMES);
      if (!StringUtil.isBlank(sysprop)) {
         String normalizedChatRoomName = normalizeChatRoomName(trimmedChatRoomName);
         String[] bannedChatRoomNames = sysprop.split(";");
         String[] arr$ = bannedChatRoomNames;
         int len$ = bannedChatRoomNames.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            String bannedChatRoomName = arr$[i$];
            if (normalizedChatRoomName.indexOf(normalizeChatRoomName(bannedChatRoomName)) != -1) {
               throw new ChatRoomValidationException("You cannot use the word " + bannedChatRoomName + " in a chat room name");
            }
         }

      }
   }
}
