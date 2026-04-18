import { forwardRef, useCallback, useEffect, useImperativeHandle, useMemo, useRef, useState } from 'react';
import {
  ActivityIndicator,
  Alert,
  AppState,
  FlatList,
  Image,
  Keyboard,
  KeyboardAvoidingView,
  Modal,
  Platform,
  Pressable,
  RefreshControl,
  SafeAreaView,
  ScrollView,
  Share,
  StyleSheet,
  Text,
  TextInput,
  TouchableOpacity,
  TouchableWithoutFeedback,
  View,
} from 'react-native';
import { Ionicons, MaterialIcons } from '@expo/vector-icons';
import * as Clipboard from 'expo-clipboard';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { API_BASE, getMe, buildHeaders as buildHeadersFromAuth } from '../services/auth';
import { WS_URL } from '../config/connection';
import { registerWS, unregisterWS } from '../services/wsManager';
import { getUser, getAuthToken } from '../services/storage';
import { getCreditBalance } from '../services/credit';
import { notificationService } from '../services/notificationService';
import { messageQueue } from '../services/messageQueue';
import GiftPickerModal, { type GiftItem } from './GiftPickerModal';
import ViewProfileModal from './ViewProfileModal';
import CreditsModal from './CreditsModal';
import ChatroomInputBar from './ChatroomInputBar';
import { STICKER_PACKS, UNICODE_TO_EMOTICON, parseMessageWithEmoticons } from '../constants/emoticons';
import { type ChatroomTheme, getThemeById } from '../constants/chatThemes';
import { useAppTheme, type AppTheme } from '../services/themeContext';

/**
 * Module-level message cache keyed by room ID.
 * Persists across component unmount/remount cycles (e.g. when the parent Modal
 * hides/shows due to Android back navigation).  Entries are cleared only when
 * the user explicitly closes a tab (onLeaveTab).
 */
const roomMessageCache = new Map<string, Message[]>();

/* ─── UNO card image assets ─── */
const UNO_CARDS: { key: string; color: string; image: ReturnType<typeof require> }[] = [
  { key: 'uno:red',    color: '#E53935', image: require('../assets/card/one/uno/uno_red.png') },
  { key: 'uno:yellow', color: '#FDD835', image: require('../assets/card/one/uno/uno_yellow.png') },
  { key: 'uno:green',  color: '#43A047', image: require('../assets/card/one/uno/uno_green.png') },
  { key: 'uno:blue',   color: '#1E88E5', image: require('../assets/card/one/uno/uno_blue.png') },
];
const CARD_IMAGE_MAP: Record<string, ReturnType<typeof require>> = {};
for (const card of UNO_CARDS) { CARD_IMAGE_MAP[card.key] = card.image; }

/* ─── Card encoding helpers ─── */
const CARD_PREFIX = '[[card:';
const CARD_SUFFIX = ']]';
function encodeCardText(key: string): string { return `${CARD_PREFIX}${key}${CARD_SUFFIX}`; }
function parseCardText(text: string): string | null {
  if (!text.startsWith(CARD_PREFIX) || !text.endsWith(CARD_SUFFIX)) return null;
  return text.slice(CARD_PREFIX.length, -CARD_SUFFIX.length);
}

/* ─── Sticker lookup map: key → local image asset ─── */
// Mirrors ChatController.java: sticker messages are stored by alias/key
// and displayed as images on the client side (not text).
const STICKER_IMAGE_MAP: Record<string, ReturnType<typeof require>> = {};
for (const pack of STICKER_PACKS) {
  for (const sticker of pack.stickers) {
    STICKER_IMAGE_MAP[sticker.key] = sticker.image;
  }
}

/* ─── Sticker encoding helpers ─── */
// Mirrors ChatController.java STICKER_COMMAND = "/sticker %s" where %s is the alias.
// We encode as [[sticker:KEY:LABEL]] in the text field so the server stores/forwards
// the key and both sender + receivers can decode and render as image.
const STICKER_PREFIX = '[[sticker:';
const STICKER_SUFFIX = ']]';
function encodeStickerText(key: string, label: string): string {
  return `${STICKER_PREFIX}${key}:${label}${STICKER_SUFFIX}`;
}
function parseStickerText(text: string): { key: string; label: string } | null {
  if (!text.startsWith(STICKER_PREFIX) || !text.endsWith(STICKER_SUFFIX)) return null;
  const inner = text.slice(STICKER_PREFIX.length, -STICKER_SUFFIX.length);
  const colonIdx = inner.indexOf(':');
  if (colonIdx === -1) return null;
  return { key: inner.slice(0, colonIdx), label: inner.slice(colonIdx + 1) };
}

function makePalette(appTheme: AppTheme) {
  return {
    headerBg:    appTheme.headerBg,
    iconCircle:  'rgba(255,255,255,0.15)',
    white:       '#FFFFFF',
    msgBg:       appTheme.screenBg,
    text:        appTheme.textPrimary,
    ts:          appTheme.textSecondary,
    inputBg:     appTheme.inputBg,
    inputBorder: appTheme.border,
    dropBg:      appTheme.cardBg,
    dropBorder:  appTheme.divider,
    divider:     appTheme.divider,
    menuBg:      appTheme.drawerBg,
    menuItem:    appTheme.textPrimary,
    menuIcon:    appTheme.textPrimary,
    danger:      '#FF4444',
    gold:        '#B45309',
    pinned:      appTheme.isDark ? 'rgba(245,158,11,0.15)' : '#FFF8E1',
    pinnedBorder:'#F59E0B',
    failedBg:    appTheme.isDark ? 'rgba(255,68,68,0.15)' : '#FFEBEE',
    failedBorder:'#FF4444',
    emoteText:        '#46AAAF',
    emoteTextBg:      'rgba(70,170,175,0.08)',
    emoteTextBorder:  '#46AAAF',
    giftBrown:        '#5A3C28',
    giftLightBeige:   '#F5F2E5',
    giftDarkBeige:    '#EEE9CC',
    giftDimGray:      '#645A55',
  };
}
type Palette = ReturnType<typeof makePalette>;

interface Message {
  id: string;
  senderId: string | null;
  senderUsername: string;
  senderColor: string;
  text: string;
  isSystem: boolean;
  isRoomInfo?: boolean;
  isWelcomeEmote?: boolean;
  isPinned?: boolean;
  failed?: boolean;
  isGift?: boolean;
  giftEmoji?: string;
  giftImageUrl?: string;
  giftName?: string;
  roomName?: string;
  roomColor?: string;
  createdAt: string;
}

interface Participant {
  id: string;
  username: string;
  displayName: string;
  color: string;
  isOwner?: boolean;
  isMod?: boolean;
  displayPicture?: string | null;
}

interface Chatroom {
  id: string;
  name: string;
  description: string | null;
  color: string;
  currentParticipants: number;
  maxParticipants: number;
  /** Username of the room owner — mirrors ChatRoom.java getCreator(). Null for system rooms. */
  creatorUsername?: string | null;
}

/** Methods exposed to parent (MultiRoomChatModal) via forwardRef */
export interface RoomChatHandle {
  toggleParticipants: () => void;
  toggleOverflow: () => void;
  getParticipantCount: () => number;
}

interface Props {
  visible: boolean;
  room: Chatroom | null;
  currentUserId: string | null;
  onClose: () => void;
  /** When true, render as a plain View (no Modal wrapper) — used inside MultiRoomChatModal */
  isEmbedded?: boolean;
  /** When true, hide the room header — the multi-tab parent renders its own header */
  hideHeader?: boolean;
  /** Called in embedded mode when the user wants to leave/close this specific tab */
  onLeaveTab?: () => void;
  /** Called when user picks "Private chat" from a participant's context menu */
  onOpenPrivateChat?: (username: string, displayName: string) => void;
  /** Called when a CHAT_MESSAGE arrives for a private conversation (so parent can open a tab) */
  onIncomingPrivateMessage?: (convId: string, peerUsername: string, peerDisplayName: string) => void;
  /** Called when the current user is kicked from this room (so parent can show cooldown in room list) */
  onKicked?: (roomId: string, roomName: string) => void;
}

const ROOM_NAME_COLOR = '#F59E0B';

/* ─── ChatRoomWelcomeMessageData constants (mirrors Java class) ─── */
const CHATROOM_WELCOME_MESSAGE_EMOTE_HOTKEY = '(chatwelcomemessage)';
const CHATROOM_WELCOME_MESSAGE_COLOUR = '#FF8800';

/**
 * Normalises a raw senderColor value from the server into a valid React Native
 * colour string.  The server stores colours without the '#' prefix (e.g. "2196F3"),
 * but React Native requires the '#' prefix for hex colours.
 * Already-prefixed strings (from older messages or local system messages) are
 * returned as-is.
 */
function toDisplayColor(raw: string | undefined | null, fallback = '#2196F3'): string {
  if (!raw) return fallback;
  if (raw.startsWith('#')) return raw;
  // Validate: 3 or 6 hex chars
  if (/^[0-9A-Fa-f]{3}$/.test(raw) || /^[0-9A-Fa-f]{6}$/.test(raw)) return `#${raw}`;
  return fallback;
}

function toMediaUrl(raw: string | undefined | null): string | null {
  if (!raw) return null;
  if (/^https?:\/\//i.test(raw)) return raw;
  return raw.startsWith('/') ? `${API_BASE}${raw}` : `${API_BASE}/${raw}`;
}

/* ─── Overflow / Context menu item type ─── */
interface MenuItem {
  id: string;
  label: string;
  icon: React.ReactNode;
  isDanger?: boolean;
}

// Mirrors ChatRoomWelcomeMessageData.java:
// Prepends CHATROOM_WELCOME_MESSAGE_EMOTE_HOTKEY to each message text,
// uses CHATROOM_WELCOME_MESSAGE_COLOUR (#FF8800) as the message color,
// and marks messages as welcome emote (ContentTypeEnum.EMOTE).
function buildWelcomeMessages(room: Chatroom): Message[] {
  const lines: string[] = [];
  if (room.description) lines.push(room.description);
  // Mirrors ChatRoom.java: "This room is managed by " + chatRoomData.getCreator()
  // creatorUsername is the owner's username; fall back to room name for system rooms
  const managedBy = room.creatorUsername ?? room.name;
  lines.push(`This room is managed by ${managedBy}`);
  return lines.map((text, i) => ({
    id: `__welcome_${i}`,
    senderId: null,
    senderUsername: room.name,
    senderColor: CHATROOM_WELCOME_MESSAGE_COLOUR,
    text: `${CHATROOM_WELCOME_MESSAGE_EMOTE_HOTKEY} ${text}`,
    isSystem: false,
    isRoomInfo: true,
    isWelcomeEmote: true,
    roomName: room.name,
    roomColor: CHATROOM_WELCOME_MESSAGE_COLOUR,
    createdAt: new Date().toISOString(),
  }));
}

/* ─── Message Row ─── */
function MessageRow({
  msg, isOwn, roomName, onLongPress, chatTheme,
}: {
  msg: Message;
  isOwn: boolean;
  roomName: string;
  onLongPress: (msg: Message) => void;
  chatTheme?: ChatroomTheme;
}) {
  const appTheme     = useAppTheme();
  const C            = useMemo(() => makePalette(appTheme), [appTheme]);
  const styles       = useMemo(() => makeStyles(C), [C]);
  const theme        = chatTheme ?? getThemeById(1);
  const msgBodyColor = `#${isOwn ? theme.sender_message_color : theme.recp_message_color}`;
  const serverColor  = `#${theme.server_message_color}`;
  const emoteColor   = `#${theme.emote_message_color}`;
  const errorColor   = `#${theme.error_message_color}`;
  if (msg.isRoomInfo && msg.isWelcomeEmote) {
    const displayText = msg.text.startsWith(CHATROOM_WELCOME_MESSAGE_EMOTE_HOTKEY)
      ? msg.text.slice(CHATROOM_WELCOME_MESSAGE_EMOTE_HOTKEY.length).trim()
      : msg.text;
    return (
      <View style={styles.msgRow}>
        <Text style={styles.msgLine}>
          <Text style={[styles.msgSenderName, { color: CHATROOM_WELCOME_MESSAGE_COLOUR }]}>
            {msg.roomName}:{' '}
          </Text>
          <Text style={[styles.msgBody, { color: serverColor }]}>{displayText}</Text>
        </Text>
      </View>
    );
  }

  if (msg.isRoomInfo) {
    return (
      <View style={styles.msgRow}>
        <Text style={styles.msgLine}>
          <Text style={[styles.msgSenderName, { color: msg.roomColor }]}>{msg.roomName}: </Text>
          <Text style={[styles.msgBody, { color: serverColor }]}>{msg.text}</Text>
        </Text>
      </View>
    );
  }

  if (msg.isSystem) {
    // "has entered" / "has left" messages already embed the full label in the
    // text as "{RoomName}::{username} has entered" — show text only, no prefix.
    const isEnterLeave = /::.*\s(has entered|has left)$/.test(msg.text ?? '');
    if (isEnterLeave) {
      const sepIdx = (msg.text ?? '').indexOf('::');
      const roomPart = sepIdx >= 0 ? (msg.text ?? '').slice(0, sepIdx) : '';
      const restPart = sepIdx >= 0 ? (msg.text ?? '').slice(sepIdx) : (msg.text ?? '');
      return (
        <View style={styles.msgRow}>
          <Text style={styles.msgLine}>
            <Text style={[styles.msgSenderName, { color: ROOM_NAME_COLOR }]}>{roomPart}</Text>
            <Text style={[styles.msgBody, { color: serverColor }]}>{restPart}</Text>
          </Text>
        </View>
      );
    }
    // If the message has a specific sender name (e.g. "DiceBot"), use that.
    // Otherwise fall back to the room name for generic system messages.
    const hasBotSender = msg.senderUsername && msg.senderUsername !== 'System' && msg.senderUsername.trim() !== '';
    const systemLabel  = hasBotSender ? msg.senderUsername : roomName;
    const systemColor  = hasBotSender ? toDisplayColor(msg.senderColor) : ROOM_NAME_COLOR;
    const bodyColor    = hasBotSender ? '#4b92c9' : serverColor;
    const systemSegments = parseMessageWithEmoticons(msg.text ?? '');
    const hasSystemEmotes = systemSegments.some(s => s.type === 'emote');
    if (!hasSystemEmotes) {
      return (
        <View style={styles.msgRow}>
          <Text style={styles.msgLine}>
            <Text style={[styles.msgSenderName, { color: systemColor }]}>{systemLabel}: </Text>
            <Text style={[styles.msgBody, { color: bodyColor }]}>{msg.text}</Text>
          </Text>
        </View>
      );
    }
    return (
      <View style={styles.msgRow}>
        <Text>
          <Text style={[styles.msgSenderName, { color: systemColor }]}>{systemLabel}: </Text>
          {systemSegments.map((seg, i) =>
            seg.type === 'text' ? (
              <Text key={i} style={[styles.msgBody, { color: bodyColor }]}>{seg.content}</Text>
            ) : (
              <Image key={i} source={seg.image as any} style={styles.inlineEmote} resizeMode="contain" />
            )
          )}
        </Text>
      </View>
    );
  }

  // ── Emote message — /me, /roll, /slap, /hug, etc. ──────────────────────
  // Emote messages are saved with senderUsername='' (empty) and isSystem=false.
  // They are rendered as plain action text in dark maroon with no "username:" prefix.
  // This mirrors classic Mig33 emote display behavior.
  if (!msg.isSystem && !msg.senderUsername) {
    return (
      <Pressable
        onLongPress={() => onLongPress(msg)}
        delayLongPress={350}
        style={({ pressed }) => [styles.msgRow, pressed && { opacity: 0.75 }]}
        testID={`msg-emote-${msg.id}`}
      >
        <Text style={[styles.msgLine, { color: emoteColor, fontStyle: 'italic' }]}>
          {msg.text}
        </Text>
      </Pressable>
    );
  }

  // ── UNO card message — encoded as [[card:uno:COLOR]] ──
  const cardKey = parseCardText(msg.text ?? '');
  if (cardKey !== null) {
    const cardImg = CARD_IMAGE_MAP[cardKey];
    const cardColor = UNO_CARDS.find(c => c.key === cardKey)?.color ?? '#888';
    return (
      <Pressable
        onLongPress={() => onLongPress(msg)}
        delayLongPress={350}
        style={({ pressed }) => [styles.msgRow, pressed && { opacity: 0.75 }]}
        testID={`msg-card-${msg.id}`}
      >
        <Text style={[styles.msgSenderName, { color: toDisplayColor(msg.senderColor) }]}>
          {msg.senderUsername}:{' '}
        </Text>
        {cardImg ? (
          <View style={[styles.cardMsgWrap, { borderColor: cardColor }]}>
            <Image source={cardImg} style={styles.cardMsgImg} resizeMode="contain" />
          </View>
        ) : (
          <Text style={[styles.msgBody, { color: msgBodyColor }]}>🃏 {cardKey}</Text>
        )}
      </Pressable>
    );
  }

  // ── Sticker message — mirrors ChatController.java StickerMimeData rendering ──
  // Java stores sticker by alias and renders as image (not text).
  // Expo encodes as [[sticker:KEY:LABEL]] so both sender and all receivers
  // can look up the local asset from STICKER_IMAGE_MAP and render as image.
  const stickerData = parseStickerText(msg.text ?? '');
  if (stickerData) {
    const stickerImg = STICKER_IMAGE_MAP[stickerData.key];
    return (
      <Pressable
        onLongPress={() => onLongPress(msg)}
        delayLongPress={350}
        style={({ pressed }) => [styles.msgRow, pressed && { opacity: 0.75 }]}
        testID={`msg-sticker-${msg.id}`}
      >
        <Text style={[styles.msgSenderName, { color: toDisplayColor(msg.senderColor) }]}>
          {msg.senderUsername}:{' '}
        </Text>
        {stickerImg ? (
          <View style={styles.stickerMsgWrap}>
            <Image
              source={stickerImg}
              style={styles.stickerMsgImg}
              resizeMode="contain"
            />
            <Text style={styles.stickerMsgLabel}>{stickerData.label}</Text>
          </View>
        ) : (
          <Text style={[styles.msgBody, { color: msgBodyColor }]}>✨ {stickerData.label}</Text>
        )}
      </Pressable>
    );
  }

  const isGiftMsg = msg.isGift || (msg.text?.startsWith('<< ') && msg.text?.endsWith(' >>'));

  if (isGiftMsg) {
    const giftImgUrl = toMediaUrl(msg.giftImageUrl);
    const giftNameInText = msg.giftName ?? null;

    let textBefore = msg.text ?? '';
    let textAfter  = '';

    if (giftImgUrl && giftNameInText) {
      const lower   = textBefore.toLowerCase();
      const nameIdx = lower.indexOf(giftNameInText.toLowerCase());
      if (nameIdx !== -1) {
        const endIdx = nameIdx + giftNameInText.length;
        textAfter  = textBefore.substring(endIdx);
        textBefore = textBefore.substring(0, endIdx);
      }
    }

    return (
      <Pressable
        onLongPress={() => onLongPress(msg)}
        delayLongPress={350}
        style={({ pressed }) => [styles.giftMsgRow, pressed && { opacity: 0.75 }]}
        testID={`msg-gift-${msg.id}`}
      >
        {giftImgUrl && giftNameInText ? (
          <View style={styles.giftMsgInlineRow}>
            {textBefore ? (
              <Text style={styles.giftMsgText}>{textBefore}</Text>
            ) : null}
            <Image
              source={{ uri: giftImgUrl }}
              style={styles.giftMsgInlineImg}
              resizeMode="contain"
            />
            {textAfter ? (
              <Text style={styles.giftMsgText}>{textAfter}</Text>
            ) : null}
          </View>
        ) : (
          <Text style={styles.giftMsgText} numberOfLines={4}>
            {msg.text}
          </Text>
        )}
      </Pressable>
    );
  }

  const containerStyle = [
    styles.msgRow,
    msg.isPinned && styles.msgRowPinned,
    msg.failed  && styles.msgRowFailed,
  ];

  const bodySegments = parseMessageWithEmoticons(msg.text ?? '');

  return (
    <Pressable
      onLongPress={() => onLongPress(msg)}
      delayLongPress={350}
      style={({ pressed }) => [containerStyle, pressed && { opacity: 0.75 }]}
    >
      <Text>
        <Text style={[styles.msgSenderName, { color: toDisplayColor(msg.senderColor) }]}>
          {msg.senderUsername}:{' '}
        </Text>
        {bodySegments.map((seg, i) =>
          seg.type === 'text' ? (
            <Text key={i} style={[styles.msgBody, { color: msgBodyColor }]}>{seg.content}</Text>
          ) : (
            <Image key={i} source={seg.image as any} style={styles.inlineEmote} resizeMode="contain" />
          )
        )}
        {msg.failed && <Text style={[styles.failedTag, { color: errorColor }]}> ✗ gagal</Text>}
        {msg.isPinned && <Text style={styles.pinnedTag}> 📌</Text>}
      </Text>
    </Pressable>
  );
}

/* ─── Participant Row ─── */
function ParticipantRow({
  p,
  isSelf,
  onMenu,
}: {
  p: Participant;
  isSelf: boolean;
  onMenu: (p: Participant) => void;
}) {
  const appTheme = useAppTheme();
  const C = useMemo(() => makePalette(appTheme), [appTheme]);
  const initial = p.username.charAt(0).toUpperCase();
  const avatarUri = p.displayPicture
    ? (p.displayPicture.startsWith('http') ? p.displayPicture : `${API_BASE}${p.displayPicture}`)
    : null;
  return (
    <View style={{ flexDirection: 'row', alignItems: 'center', paddingHorizontal: 14, paddingVertical: 9, borderBottomWidth: 1, borderBottomColor: C.dropBorder }}>
      {avatarUri ? (
        <Image
          source={{ uri: avatarUri }}
          style={[{ width: 32, height: 32, borderRadius: 16, alignItems: 'center', justifyContent: 'center', marginRight: 10 }, { backgroundColor: toDisplayColor(p.color) }]}
          resizeMode="cover"
        />
      ) : (
        <View style={{ width: 32, height: 32, borderRadius: 16, alignItems: 'center', justifyContent: 'center', marginRight: 10, backgroundColor: toDisplayColor(p.color) }}>
          <Text style={{ color: '#FFFFFF', fontFamily: 'Roboto_700Bold', fontSize: 13 }}>{initial}</Text>
        </View>
      )}
      <View style={{ flex: 1 }}>
        <Text style={{ fontSize: 13, color: toDisplayColor(p.color), fontFamily: 'Roboto_400Regular' }}>{p.username}</Text>
        {(p.isOwner || p.isMod) && (
          <Text style={{ fontSize: 10, color: C.gold, fontFamily: 'Roboto_700Bold', marginTop: 1 }}>{p.isOwner ? 'Owner' : 'Mod'}</Text>
        )}
      </View>
      {!isSelf && (
        <Pressable
          style={{ padding: 6, marginLeft: 4 }}
          onPress={() => onMenu(p)}
          hitSlop={{ top: 8, bottom: 8, left: 8, right: 8 }}
          testID={`button-participant-menu-${p.id}`}
        >
          <Ionicons name="ellipsis-vertical" size={18} color={C.ts} />
        </Pressable>
      )}
    </View>
  );
}

/* ─── Room Info Sheet ─── */
function RoomInfoSheet({
  room,
  onClose,
  buildHeaders,
}: {
  room: Chatroom;
  onClose: () => void;
  buildHeaders: (json?: boolean) => Promise<Record<string, string>>;
}) {
  const appTheme = useAppTheme();
  const C = useMemo(() => makePalette(appTheme), [appTheme]);
  const [mods, setMods]     = useState<{ userId: string; username: string }[]>([]);
  const [banned, setBanned] = useState<{ userId: string; username: string }[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    (async () => {
      try {
        const h = await buildHeaders();
        const res = await fetch(`${API_BASE}/api/chatrooms/${room.id}/info`, { headers: h });
        if (res.ok) {
          const data = await res.json();
          setMods(data.moderators ?? []);
          setBanned(data.bannedUsers ?? []);
        }
      } catch {}
      setLoading(false);
    })();
  }, [room.id]);

  return (
    <TouchableWithoutFeedback onPress={onClose}>
      <View style={{ position: 'absolute', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.5)', justifyContent: 'flex-end', zIndex: 100 }}>
        <TouchableWithoutFeedback>
          <View style={{ backgroundColor: C.dropBg, minHeight: '65%' as any, maxHeight: '90%' as any, overflow: 'hidden' }}>
            {/* ── Header ── */}
            <View style={{ flexDirection: 'row', alignItems: 'center', paddingHorizontal: 16, paddingVertical: 14, gap: 10, backgroundColor: C.headerBg }}>
              <View style={{ flex: 1 }}>
                <Text style={{ fontSize: 17, fontFamily: 'Roboto_700Bold', color: '#FFFFFF', marginBottom: 2 }} numberOfLines={1}>{room.name}</Text>
                <Text style={{ fontSize: 12, fontFamily: 'Roboto_400Regular', color: 'rgba(255,255,255,0.8)' }}>
                  {room.currentParticipants}/{room.maxParticipants} pengguna
                  {room.creatorUsername ? `  ·  Owner: ${room.creatorUsername}` : ''}
                </Text>
              </View>
              <Pressable onPress={onClose} hitSlop={{ top: 8, bottom: 8, left: 8, right: 8 }} testID="button-info-close">
                <Ionicons name="close" size={22} color="#FFFFFF" />
              </Pressable>
            </View>

            <ScrollView showsVerticalScrollIndicator={false} bounces={false}>
              {/* ── Description ── */}
              <View style={{ paddingHorizontal: 16, paddingVertical: 12, borderBottomWidth: 1, borderBottomColor: C.divider }}>
                <Text style={{ fontSize: 11, fontFamily: 'Roboto_700Bold', color: C.ts, textTransform: 'uppercase', letterSpacing: 0.6, marginBottom: 8 }}>Deskripsi</Text>
                <Text style={{ fontSize: 14, fontFamily: 'Roboto_400Regular', color: C.text, lineHeight: 20 }}>
                  {room.description || 'Tidak ada deskripsi'}
                </Text>
              </View>

              {/* ── Moderators ── */}
              <View style={{ paddingHorizontal: 16, paddingVertical: 12, borderBottomWidth: 1, borderBottomColor: C.divider }}>
                <Text style={{ fontSize: 11, fontFamily: 'Roboto_700Bold', color: C.ts, textTransform: 'uppercase', letterSpacing: 0.6, marginBottom: 8 }}>Moderator ({loading ? '...' : mods.length})</Text>
                {loading ? (
                  <ActivityIndicator size="small" color={C.ts} style={{ marginVertical: 8 }} />
                ) : mods.length === 0 ? (
                  <Text style={{ fontSize: 13, color: C.ts, fontFamily: 'Roboto_400Regular', fontStyle: 'italic' }}>Belum ada moderator</Text>
                ) : (
                  mods.map((m) => (
                    <View key={m.userId} style={{ flexDirection: 'row', alignItems: 'center', paddingVertical: 8, gap: 10, borderBottomWidth: StyleSheet.hairlineWidth, borderBottomColor: C.divider }}>
                      <View style={{ width: 34, height: 34, borderRadius: 17, alignItems: 'center', justifyContent: 'center', backgroundColor: C.headerBg }}>
                        <Text style={{ color: '#FFFFFF', fontFamily: 'Roboto_700Bold', fontSize: 14 }}>{m.username.charAt(0).toUpperCase()}</Text>
                      </View>
                      <Text style={{ flex: 1, fontSize: 14, fontFamily: 'Roboto_400Regular', color: C.text }}>{m.username}</Text>
                      <View style={{ backgroundColor: C.headerBg, paddingHorizontal: 7, paddingVertical: 2, borderRadius: 4 }}>
                        <Text style={{ color: '#FFFFFF', fontSize: 10, fontFamily: 'Roboto_700Bold' }}>MOD</Text>
                      </View>
                    </View>
                  ))
                )}
              </View>

              {/* ── Banned users (only shown to owner/mod via API) ── */}
              {!loading && banned.length > 0 && (
                <View style={{ paddingHorizontal: 16, paddingVertical: 12, borderBottomWidth: 1, borderBottomColor: C.divider }}>
                  <Text style={{ fontSize: 11, fontFamily: 'Roboto_700Bold', color: C.ts, textTransform: 'uppercase', letterSpacing: 0.6, marginBottom: 8 }}>Banned ({banned.length})</Text>
                  {banned.map((b) => (
                    <View key={b.userId} style={{ flexDirection: 'row', alignItems: 'center', paddingVertical: 8, gap: 10, borderBottomWidth: StyleSheet.hairlineWidth, borderBottomColor: C.divider }}>
                      <View style={{ width: 34, height: 34, borderRadius: 17, alignItems: 'center', justifyContent: 'center', backgroundColor: '#E53935' }}>
                        <Text style={{ color: '#FFFFFF', fontFamily: 'Roboto_700Bold', fontSize: 14 }}>{b.username.charAt(0).toUpperCase()}</Text>
                      </View>
                      <Text style={{ flex: 1, fontSize: 14, fontFamily: 'Roboto_400Regular', color: C.text }}>{b.username}</Text>
                      <Ionicons name="ban-outline" size={16} color={C.danger} />
                    </View>
                  ))}
                </View>
              )}
              <View style={{ height: 24 }} />
            </ScrollView>
          </View>
        </TouchableWithoutFeedback>
      </View>
    </TouchableWithoutFeedback>
  );
}

/* ─── Generic Bottom-Sheet Menu ─── */
function BottomSheetMenu({
  visible,
  title,
  items,
  onSelect,
  onClose,
  styles,
}: {
  visible: boolean;
  title?: string;
  items: MenuItem[];
  onSelect: (id: string) => void;
  onClose: () => void;
  styles: ReturnType<typeof makeStyles>;
}) {
  if (!visible) return null;
  return (
    <TouchableWithoutFeedback onPress={onClose}>
      <View style={styles.menuOverlay}>
        <TouchableWithoutFeedback>
          <View style={styles.menuSheet}>
            {title ? (
              <View style={styles.menuTitleRow}>
                <Text style={styles.menuTitle} numberOfLines={1}>{title}</Text>
              </View>
            ) : null}
            {items.map((item, idx) => (
              <TouchableOpacity
                key={item.id}
                style={[
                  styles.menuItem,
                  idx < items.length - 1 && styles.menuItemBorder,
                ]}
                onPress={() => onSelect(item.id)}
                testID={`button-menu-${item.id}`}
              >
                <View style={styles.menuIconWrap}>{item.icon}</View>
                <Text style={[styles.menuLabel, item.isDanger && styles.menuLabelDanger]}>
                  {item.label}
                </Text>
              </TouchableOpacity>
            ))}
          </View>
        </TouchableWithoutFeedback>
      </View>
    </TouchableWithoutFeedback>
  );
}

/* ═══════════════════════════════════════════════════════
   Main Component
   ═══════════════════════════════════════════════════════ */
const RoomChatModal = forwardRef<RoomChatHandle, Props>(function RoomChatModal(
  { visible, room, currentUserId, onClose, isEmbedded, hideHeader, onLeaveTab, onOpenPrivateChat, onIncomingPrivateMessage, onKicked }: Props,
  ref,
) {
  const insets   = useSafeAreaInsets();
  const appTheme = useAppTheme();
  const C        = useMemo(() => makePalette(appTheme), [appTheme]);
  const styles   = useMemo(() => makeStyles(C), [C]);
  const [messages, setMessages]                 = useState<Message[]>([]);
  const [inputText, setInputText]               = useState('');
  // Ref mirrors inputText so sendMessage/onPressIn always read the latest value
  // even when called from a stale closure (e.g. onPressIn during Android Modal touch)
  const inputTextRef = useRef('');
  const [loading, setLoading]                   = useState(true);
  const [participants, setParticipants]         = useState<Participant[]>([]);
  const [participantCount, setParticipantCount] = useState(0);
  const [showUsers, setShowUsers]               = useState(false);
  const [showPicker, setShowPicker]             = useState(false);
  const [showOverflow, setShowOverflow]         = useState(false);
  const [showRoomInfo, setShowRoomInfo]         = useState(false);
  const [isMuted, setIsMuted]                   = useState(false);
  const [isFavorite, setIsFavorite]             = useState(false);
  const [isChatRoomAdmin, setIsChatRoomAdmin]   = useState(false);
  const [contextMessage, setContextMessage]     = useState<Message | null>(null);
  const [participantMenuTarget, setParticipantMenuTarget] = useState<Participant | null>(null);
  const [followedUsers, setFollowedUsers]       = useState<Set<string>>(new Set());
  const [blockedUsers, setBlockedUsers]         = useState<Set<string>>(new Set());
  const [showGiftForUser, setShowGiftForUser]   = useState<Participant | null>(null);
  const [showCardPicker, setShowCardPicker]     = useState(false);
  const [viewProfileTarget, setViewProfileTarget] = useState<Participant | null>(null);
  const [transferCreditTarget, setTransferCreditTarget] = useState<string | null>(null);
  const [creditAmount, setCreditAmount]         = useState(0);
  const [creditCurrency, setCreditCurrency]     = useState('IDR');
  const [headerHeight, setHeaderHeight]         = useState(0);
  const [loadingHistory, setLoadingHistory]     = useState(false);
  const [hasMoreHistory, setHasMoreHistory]     = useState(true);
  // Active announcement banner — mirrors Announce.java announceOn/Off state
  const [announceText, setAnnounceText]         = useState<string | null>(null);
  // Keyboard height for Android — KeyboardAvoidingView doesn't work inside Modal on Android
  const [keyboardHeight, setKeyboardHeight]     = useState(0);
  // Chat room theme — derived from user's saved preference via ThemeContext
  const chatTheme = appTheme.chatTheme;
  const wsRef            = useRef<WebSocket | null>(null);
  const flatListRef      = useRef<FlatList>(null);
  const inputRef         = useRef<TextInput>(null);
  const suppressKbHide   = useRef(false);
  const kbHideTimer      = useRef<ReturnType<typeof setTimeout> | null>(null);
  const myUsernameRef    = useRef<string>('me');
  const wasKickedRef     = useRef<boolean>(false);
  const myMigLevelRef    = useRef<number>(1);
  const myRoleColorRef   = useRef<string>('2196F3');  // resolved by server on SUBSCRIBE
  // Tracks optimistic temp message IDs waiting to be replaced by WS broadcast
  const pendingTempRef   = useRef<Set<string>>(new Set());
  // Always-current ref for currentUserId (avoids stale closure in WS handler)
  const currentUserIdRef = useRef<string | null>(currentUserId);
  useEffect(() => { currentUserIdRef.current = currentUserId; }, [currentUserId]);
  // Always-current ref for blockedUsers (avoids stale closure in WS message handler)
  const blockedUsersRef = useRef<Set<string>>(new Set());
  useEffect(() => { blockedUsersRef.current = blockedUsers; }, [blockedUsers]);

  // Keyboard listener — KAV doesn't work inside Android Modal with edgeToEdge enabled.
  // Strategy: when keyboard hides, wait 350ms before collapsing layout. If the keyboard
  // comes back up within that window (e.g. because the send button called focus()),
  // cancel the collapse — the layout never shifts and the send button stays in place.
  useEffect(() => {
    if (Platform.OS !== 'android') return;
    const showSub = Keyboard.addListener('keyboardDidShow', (e) => {
      suppressKbHide.current = false;
      if (kbHideTimer.current) {
        clearTimeout(kbHideTimer.current);
        kbHideTimer.current = null;
      }
      setKeyboardHeight(e.endCoordinates.height);
    });
    const hideSub = Keyboard.addListener('keyboardDidHide', () => {
      if (suppressKbHide.current) {
        suppressKbHide.current = false;
        setTimeout(() => inputRef.current?.focus(), 10);
        return;
      }
      // Delay layout collapse — if keyboard re-appears quickly (send btn tap),
      // the timer is cancelled by keyboardDidShow and layout never changes.
      kbHideTimer.current = setTimeout(() => {
        kbHideTimer.current = null;
        setKeyboardHeight(0);
      }, 350);
    });
    return () => {
      showSub.remove();
      hideSub.remove();
      if (kbHideTimer.current) clearTimeout(kbHideTimer.current);
    };
  }, []);

  // Rate limit for /whois: 1 per 5 seconds — mirrors Java WhoisRateLimitExpr "1/5S"
  const whoisLastCallRef = useRef<number>(0);

  // ── Connection resilience — mirrors FusionService reconnect/ping logic ──────
  // isActiveRef: true while modal is open; false when closed (stops all reconnects)
  const isActiveRef          = useRef(false);
  const reconnectAttemptsRef = useRef(0);
  const reconnectTimerRef    = useRef<ReturnType<typeof setTimeout> | null>(null);
  const pingTimerRef         = useRef<ReturnType<typeof setInterval> | null>(null);
  const connectWSRef         = useRef<(() => void) | undefined>(undefined);   // holds latest connectWS to break circular dep
  const onIncomingPrivateMessageRef = useRef(onIncomingPrivateMessage);
  useEffect(() => { onIncomingPrivateMessageRef.current = onIncomingPrivateMessage; }, [onIncomingPrivateMessage]);
  const [wsStatus, setWsStatus] = useState<'connecting' | 'connected' | 'reconnecting' | 'disconnected'>('disconnected');

  const PING_INTERVAL_MS      = 25_000;   // send PING every 25s (FusionService: pingFrequency)
  const BASE_RECONNECT_MS     = 1_000;    // 1s initial delay (FusionService: reconnectionDelay)
  const MAX_RECONNECT_MS      = 60_000;   // 60s max backoff delay (was 30s)
  // 20 attempts ≈ 1+2+4+8+16+32+60+60+60+60+60+60+60+60+60+60+60+60+60+60 (~14 min total)
  // This keeps the client trying to reconnect well beyond the server's 120s grace period,
  // matching the Java NetworkService behaviour of reconnecting indefinitely.
  const MAX_RECONNECT_ATTEMPTS = 20;

  const clearWsTimers = useCallback(() => {
    if (pingTimerRef.current) {
      clearInterval(pingTimerRef.current);
      pingTimerRef.current = null;
    }
    if (reconnectTimerRef.current) {
      clearTimeout(reconnectTimerRef.current);
      reconnectTimerRef.current = null;
    }
  }, []);

  const buildHeaders = useCallback(async (json = false) => {
    const extra: Record<string, string> = {};
    if (json) extra['Content-Type'] = 'application/json';
    // Use the shared buildHeaders from auth.ts which adds Authorization: Bearer JWT token.
    // This replaces the old cookie-only approach that caused 401 on React Native.
    const h = await buildHeadersFromAuth(extra);
    return h as Record<string, string>;
  }, []);

  useEffect(() => {
    (async () => {
      try {
        const me = await getMe();
        if (!me) return;
        const credit = await getCreditBalance(me.username);
        if (credit) {
          setCreditAmount(credit.balance);
          setCreditCurrency(credit.currency);
        }
      } catch {}
    })();
  }, []);

  // Fetch favourite status from server when room is opened
  useEffect(() => {
    if (!room) return;
    (async () => {
      try {
        const headers = await buildHeaders();
        const res = await fetch(`${API_BASE}/api/chatrooms/${room.id}/favourite`, {
          credentials: 'include', headers,
        });
        if (res.ok) {
          const data = await res.json();
          setIsFavorite(!!data.isFavourite);
        }
      } catch {}
    })();
  }, [room?.id]);

  // Matches Java FusionPktJoinChatRoomOld (703) behaviour: on room entry the
  // client only sees the room description header lines ("welcome messages").
  // No old message history is sent — real-time messages accumulate from
  // WS events.  History/backlog is fetched via GET_MESSAGES with a timestamp
  // cursor only when needed (e.g. pull-to-refresh or explicit load-more).
  const loadMessages = useCallback(() => {
    if (!room) return;
    setMessages(buildWelcomeMessages(room));
    setLoading(false);
  }, [room]);

  // Ref to always access latest messages without stale closure in fetchHistory
  const messagesRef = useRef<Message[]>([]);
  useEffect(() => { messagesRef.current = messages; }, [messages]);

  // Persist messages to the module-level cache whenever they change so they
  // survive parent-Modal unmount/remount (e.g. Android back + re-open).
  useEffect(() => {
    if (room?.id && messages.length > 0) {
      roomMessageCache.set(room.id, messages);
    }
  }, [messages, room?.id]);

  // Pull-to-refresh: request older messages via GET_MESSAGES with a `before`
  // cursor (oldest real message currently in view).  Server returns HISTORY
  // packet which the client prepends at the top of the list.
  // Matches FusionPktGetMessages timestamp cursor behaviour.
  const fetchHistory = useCallback(() => {
    if (!room || !hasMoreHistory || loadingHistory) return;
    const ws = wsRef.current;
    if (!ws || ws.readyState !== WebSocket.OPEN) return;
    const oldest = messagesRef.current.find(m => !m.id.startsWith('__welcome_'));
    setLoadingHistory(true);
    ws.send(JSON.stringify({
      type: 'GET_MESSAGES',
      roomId: room.id,
      before: oldest?.createdAt ?? new Date().toISOString(),
      limit: 50,
    }));
  }, [room, hasMoreHistory, loadingHistory]);

  const loadParticipants = useCallback(async () => {
    if (!room) return;
    try {
      const headers = await buildHeaders();
      const res  = await fetch(`${API_BASE}/api/chatrooms/${room.id}/participants`, {
        credentials: 'include', headers,
      });
      const data = await res.json();
      const list: Participant[] = data.participants ?? [];
      setParticipants(list);
      setParticipantCount(list.length);
      if (currentUserId) {
        const me = list.find(p => p.id === currentUserId);
        setIsChatRoomAdmin(!!(me?.isOwner || me?.isMod));
      }
    } catch {}
  }, [room, buildHeaders, currentUserId]);

  // Expose participant/overflow toggle methods to MultiRoomChatModal header buttons
  useImperativeHandle(ref, () => ({
    toggleParticipants: () => {
      setShowUsers(v => {
        if (!v) loadParticipants();
        return !v;
      });
      setShowOverflow(false);
    },
    toggleOverflow: () => {
      setShowOverflow(v => !v);
      setShowUsers(false);
    },
    getParticipantCount: () => participantCount,
  }), [participantCount, loadParticipants]);

  // HTTP join/leave removed: WS SUBSCRIBE/UNSUBSCRIBE and server grace period
  // are the sole owners of "has entered"/"has left" broadcasts, preventing
  // double-enter and double-leave from concurrent HTTP + WS paths.

  // ── WebSocket Gateway — resilient connection with auto-reconnect ─────────────
  // Protocol: WELCOME → AUTH → AUTH_OK → SUBSCRIBE
  // Reconnect: exponential backoff, mirrors FusionService.scheduleStartService()
  // Heartbeat: PING every 25s, mirrors FusionService.scheduleNextPingTimerTask()
  // AppState: reconnects when app returns from background
  const connectWS = useCallback(async () => {
    if (!room || !isActiveRef.current) return;

    // Close any existing connection cleanly before opening new one
    if (wsRef.current) {
      unregisterWS(wsRef.current);
      wsRef.current.onclose = null;
      wsRef.current.onerror = null;
      wsRef.current.close();
      wsRef.current = null;
    }
    clearWsTimers();

    let storedUser: Awaited<ReturnType<typeof getUser>>;
    let authToken: string | null = null;
    try {
      storedUser = await getUser();
      authToken  = await getAuthToken();
    } catch {
      storedUser = null;
    }
    if (!storedUser || !isActiveRef.current) return;

    const roomId = room.id;
    // Keep 'reconnecting' label if this is a retry attempt, not the initial connect
    setWsStatus(reconnectAttemptsRef.current > 0 ? 'reconnecting' : 'connecting');

    console.log(`[WS] Connecting to ${WS_URL} (room=${roomId} attempt=${reconnectAttemptsRef.current})`);
    const ws = new WebSocket(WS_URL);
    wsRef.current = ws;

    // Helper: schedule reconnect with exponential backoff
    // Mirrors FusionService.scheduleStartService()
    const scheduleReconnect = () => {
      if (!isActiveRef.current) return;
      const attempts = reconnectAttemptsRef.current;
      if (attempts >= MAX_RECONNECT_ATTEMPTS) {
        console.log(`[WS] Max reconnect attempts (${MAX_RECONNECT_ATTEMPTS}) reached — giving up`);
        setWsStatus('disconnected');
        return;
      }
      const delay = Math.min(BASE_RECONNECT_MS * Math.pow(2, attempts), MAX_RECONNECT_MS);
      console.log(`[WS] Scheduling reconnect in ${delay}ms (attempt ${attempts + 1})`);
      setWsStatus('reconnecting');
      reconnectTimerRef.current = setTimeout(() => {
        if (isActiveRef.current) {
          reconnectAttemptsRef.current += 1;
          connectWSRef.current?.();
        }
      }, delay);
    };

    ws.onopen = () => {
      console.log(`[WS] Connected — waiting for WELCOME`);
    };

    ws.onmessage = (e) => {
      if (!isActiveRef.current) return;
      try {
        const payload = JSON.parse(e.data);

        // Step 1: Server → WELCOME; Client → AUTH with JWT
        if (payload.type === 'WELCOME') {
          console.log(`[WS] WELCOME received — sending AUTH (JWT=${!!authToken})`);
          if (authToken) {
            ws.send(JSON.stringify({ type: 'AUTH', token: authToken }));
          } else {
            ws.send(JSON.stringify({
              type: 'AUTH',
              sessionUserId: storedUser!.id,
              username: storedUser!.username,
            }));
          }
          return;
        }

        // Step 2: Server → AUTH_OK; Client → JOIN_ROOM + start heartbeat
        if (payload.type === 'AUTH_OK') {
          reconnectAttemptsRef.current = 0;
          if (payload.username)  myUsernameRef.current  = payload.username;
          if (payload.migLevel)  myMigLevelRef.current  = payload.migLevel;
          console.log(`[WS] AUTH_OK — joining room ${roomId}`);
          setWsStatus('connected');
          // Register this WS so the logout handler can send LOGOUT signal
          // directly through this socket (bypasses the 15s grace period).
          registerWS(ws);
          // Send JOIN_ROOM (preferred) — server also accepts SUBSCRIBE for web compat
          ws.send(JSON.stringify({ type: 'JOIN_ROOM', roomId }));
          // Heartbeat PING — mirrors FusionService.scheduleNextPingTimerTask()
          pingTimerRef.current = setInterval(() => {
            if (ws.readyState === WebSocket.OPEN) {
              ws.send(JSON.stringify({ type: 'PING' }));
            }
          }, PING_INTERVAL_MS);
          return;
        }

        // PONG — server acknowledged PING, connection is alive
        if (payload.type === 'PONG') return;

        // SUBSCRIBED — server confirmed room join and tells us our resolved role color
        // (owner/mod = FCC504, merchant = 990099, regular = chatColor).
        // Store it so optimistic message bubbles use the correct color immediately.
        if (payload.type === 'SUBSCRIBED' && payload.roomId === roomId) {
          console.log(`[WS] SUBSCRIBED to room ${roomId} — connection fully established`);
          if (payload.userColor) myRoleColorRef.current = payload.userColor;
          return;
        }

        // AUTH_FAIL — fatal, do not reconnect (bad credentials / invalid JWT)
        if (payload.type === 'AUTH_FAIL') {
          console.log(`[WS] AUTH_FAIL — stopping reconnects`);
          isActiveRef.current = false;
          setWsStatus('disconnected');
          clearWsTimers();
          return;
        }

        // JOIN_FAIL — fatal join error (banned, room missing, email not verified)
        // Do not reconnect — this will not resolve on retry.
        if (payload.type === 'JOIN_FAIL') {
          console.log(`[WS] JOIN_FAIL code=${payload.code} — ${payload.message}`);
          isActiveRef.current = false;
          setWsStatus('disconnected');
          clearWsTimers();
          if (wasKickedRef.current) {
            wasKickedRef.current = false;
            onKicked?.(roomId, room?.name ?? '');
          } else if (payload.code === 'KICK_COOLDOWN') {
            onKicked?.(roomId, room?.name ?? '');
          } else if (payload.code === 'EMAIL_NOT_VERIFIED') {
            Alert.alert(
              'Email Belum Terverifikasi',
              'Kamu perlu memverifikasi email terlebih dahulu untuk masuk ke chatroom.',
              [{ text: 'OK' }],
            );
          } else {
            Alert.alert('Tidak Bisa Masuk Room', payload.message ?? 'Terjadi kesalahan.');
          }
          return;
        }

        // Regular chatroom events
        if (payload.type === 'MESSAGE' && payload.roomId === roomId) {
          // Drop messages from blocked users (except our own optimistic echoes)
          const incomingSender = (payload.message?.senderUsername ?? '').toLowerCase();
          const isOwnMsg = payload.message?.senderId === currentUserIdRef.current;
          if (!isOwnMsg && incomingSender && blockedUsersRef.current.has(incomingSender)) {
            return; // silently discard
          }
          setMessages(prev => {
            const exists = prev.some(m => m.id === payload.message?.id);
            if (exists) return prev;
            // If this message is from the current user and there's a pending optimistic,
            // replace the oldest pending temp in-place (no flash/gap)
            if (payload.message?.senderId === currentUserIdRef.current && pendingTempRef.current.size > 0) {
              const [oldestTemp] = pendingTempRef.current;
              pendingTempRef.current.delete(oldestTemp);
              const idx = prev.findIndex(m => m.id === oldestTemp);
              if (idx !== -1) {
                const next = [...prev];
                next[idx] = payload.message;
                return next;
              }
            }
            return [...prev, payload.message];
          });
          // Mirrors NetworkBroadcastReceiver UPDATE_AVAILABLE → showStatusNotification()
          // Show push notification for messages from OTHER users when app is in background
          if (payload.message?.senderId !== currentUserIdRef.current && !payload.message?.isSystem) {
            notificationService.showMessageNotification({
              senderName:     payload.message?.senderUsername ?? 'New Message',
              text:           payload.message?.text ?? '',
              conversationId: roomId,
              isRoom:         true,
            }).catch(() => {});
          }
          setTimeout(() => flatListRef.current?.scrollToEnd({ animated: true }), 100);
        }

        if (payload.type === 'MESSAGES' && payload.roomId === roomId) {
          const wsMessages: Message[] = payload.messages ?? [];
          setMessages(prev => {
            const ids = new Set(prev.map((m: Message) => m.id));
            const blocked = blockedUsersRef.current;
            const fresh = wsMessages.filter(m =>
              !ids.has(m.id) && !blocked.has((m.senderUsername ?? '').toLowerCase())
            );
            return [...prev, ...fresh];
          });
          setTimeout(() => flatListRef.current?.scrollToEnd({ animated: true }), 100);
        }

        // HISTORY — response to GET_MESSAGES (explicit pull-to-refresh).
        // Messages are OLDER than what's currently shown so they are prepended
        // at the top.  hasMore tells the client whether to allow further loads.
        if (payload.type === 'HISTORY' && payload.roomId === roomId) {
          const historyMsgs: Message[] = payload.messages ?? [];
          setLoadingHistory(false);
          setHasMoreHistory(payload.hasMore ?? false);
          if (historyMsgs.length > 0) {
            setMessages(prev => {
              const ids = new Set(prev.map((m: Message) => m.id));
              const blocked = blockedUsersRef.current;
              const fresh = historyMsgs.filter(m =>
                !ids.has(m.id) && !blocked.has((m.senderUsername ?? '').toLowerCase())
              );
              return [...fresh, ...prev];
            });
          }
        }

        // PARTICIPANTS — mirrors FusionPktChatRoomParticipantsOld (packet 708).
        // Android ChatRoom.java builds "Currently in the room: joinedUser, user1, user2, ..."
        // and sends it as a queueAdminMessage with MIMETYPE_PARTICIPANTS.
        // Here we replicate that: put the joining user first, then all others, and
        // inject the line into the chat as a welcome-emote system message.
        if (payload.type === 'PARTICIPANTS' && payload.roomId === roomId) {
          const admins:  string[] = payload.administrators  ?? [];
          const regular: string[] = payload.participants    ?? [];
          const muted:   string[] = payload.mutedParticipants ?? [];
          const allUsers = [...admins, ...regular, ...muted];
          setParticipantCount(allUsers.length);
          // Refresh full participant objects (avatar colors, roles, etc.)
          loadParticipants();
          // Build "Currently in the room: myUser, user1, user2, ..." mirroring Android logic
          const myUsername = myUsernameRef.current;
          const others = allUsers.filter(u => u !== myUsername);
          const participantsString =
            others.length === 0
              ? myUsername
              : `${myUsername}, ${others.join(', ')}`;
          const participantsText = `Currently in the room: ${participantsString}`;
          const currentRoomName = room?.name ?? '';
          const inRoomMsg: Message = {
            id: '__participants_in_room',
            senderId: null,
            senderUsername: currentRoomName,
            senderColor: CHATROOM_WELCOME_MESSAGE_COLOUR,
            text: `${CHATROOM_WELCOME_MESSAGE_EMOTE_HOTKEY} ${participantsText}`,
            isSystem: false,
            isRoomInfo: true,
            isWelcomeEmote: true,
            roomName: currentRoomName,
            roomColor: CHATROOM_WELCOME_MESSAGE_COLOUR,
            createdAt: new Date().toISOString(),
          };
          setMessages(prev => {
            // Replace if already injected (e.g. on reconnect), otherwise append
            const idx = prev.findIndex(m => m.id === '__participants_in_room');
            if (idx !== -1) {
              const next = [...prev];
              next[idx] = inRoomMsg;
              return next;
            }
            return [...prev, inRoomMsg];
          });
        }

        // GIFT event — broadcast gift received from another user or yourself
        if (payload.type === 'GIFT' && payload.roomId === roomId) {
          if (payload.message) {
            const giftMsg: Message = {
              ...payload.message,
              isGift: true,
              giftEmoji: payload.giftEmoji,
              giftImageUrl: payload.giftImageUrl,
              giftName: payload.giftName,
            };
            setMessages(prev => {
              const idx = prev.findIndex(m => m.id === giftMsg.id);
              if (idx === -1) return [...prev, giftMsg];
              const next = [...prev];
              next[idx] = {
                ...next[idx],
                isGift: true,
                giftEmoji: payload.giftEmoji,
                giftImageUrl: payload.giftImageUrl,
                giftName: payload.giftName,
              };
              return next;
            });
            setTimeout(() => flatListRef.current?.scrollToEnd({ animated: true }), 100);
          }
        }

        // ERROR — server-side errors (balance, rate limit, validation, muted, etc.)
        // If there is a pending optimistic message waiting for WS confirmation,
        // remove it so the failed message doesn't linger.
        if (payload.type === 'ERROR') {
          console.log(`[WS] ERROR code=${payload.code} message=${payload.message}`);
          if (pendingTempRef.current.size > 0) {
            const [oldestTemp] = pendingTempRef.current;
            pendingTempRef.current.delete(oldestTemp);
            setMessages(prev => prev.filter(m => m.id !== oldestTemp));
          }
          // Fatal errors that should stop reconnect (older server compat without JOIN_FAIL)
          const FATAL_CODES = ['EMAIL_NOT_VERIFIED', 'BANNED', 'INCORRECT_CREDENTIAL'];
          const isFatal = FATAL_CODES.includes(payload.code) || payload.message?.includes('di-ban');
          if (isFatal) {
            isActiveRef.current = false;
            clearWsTimers();
          }
          if (payload.code === 'EMAIL_NOT_VERIFIED') {
            Alert.alert(
              'Email Belum Terverifikasi',
              'Kamu perlu memverifikasi email terlebih dahulu untuk masuk ke chatroom. Cek inbox email kamu.',
              [{ text: 'OK' }],
            );
          } else if (isFatal) {
            Alert.alert('Tidak Bisa Masuk Room', payload.message ?? 'Terjadi kesalahan.');
          } else {
            Alert.alert('Gagal', payload.message ?? 'Terjadi kesalahan.');
          }
        }

        // GIFT_BILLING — sent only to sender after /gift all
        // Matches GiftAllBillingMessageData.java: billing confirmation + remaining balance
        if (payload.type === 'GIFT_BILLING') {
          Alert.alert('🎉 Gift Shower Berhasil!', payload.message ?? 'Gift shower terkirim ke semua pengguna di room.');
        }

        // COLOR_LIST — available colors from FusionPktDataTextColor (packet 924)
        if (payload.type === 'COLOR_LIST') {
          // Available for future use
        }

        // THEME — server theme event (user preference from ThemeContext takes priority)
        // setChatTheme intentionally omitted; theme is driven by user's AsyncStorage preference

        // COLOR_CHANGED — update participant color in list
        if (payload.type === 'COLOR_CHANGED' && payload.roomId === roomId) {
          setParticipants(prev =>
            prev.map(p => p.username === payload.username ? { ...p, color: `#${payload.color}` } : p)
          );
        }

        // WARNED — mirrors Warn.java chatRoomPrx.warnUser()
        // Server broadcasts WARNED to the whole room; only show the Alert
        // to the target user (matches chatSource.sendMessageToSender behaviour).
        if (payload.type === 'WARNED' && payload.roomId === roomId) {
          if (payload.username === myUsernameRef.current) {
            const warnDetail = payload.message ? `\n"${payload.message}"` : '';
            Alert.alert(
              '⚠️ Peringatan dari Moderator',
              `Kamu mendapat peringatan di room ini.${warnDetail}`,
              [{ text: 'OK' }],
            );
          }
        }

        // ANNOUNCEMENT — mirrors Announce.java chatRoomPrx.announceOn()
        // Shows a sticky banner at top of chat with the announcement text.
        // Repeating announcements (waitTime > 0) keep updating the banner.
        if (payload.type === 'ANNOUNCEMENT' && payload.roomId === roomId) {
          setAnnounceText(payload.message ?? null);
        }

        // ANNOUNCEMENT_OFF — mirrors Announce.java chatRoomPrx.announceOff()
        if (payload.type === 'ANNOUNCEMENT_OFF' && payload.roomId === roomId) {
          setAnnounceText(null);
        }

        // GET_MY_LUCK — mirrors GetMyLuck.java sendMessageToAllUsersInChat
        // Server caches values per user per day in Redis; broadcast to all in room.
        // Render as a compact luck bar bubble: Love / Career / Health / Luck (⭐ 1-5).
        if (payload.type === 'GET_MY_LUCK' && payload.roomId === roomId) {
          const stars = (n: number) => '⭐'.repeat(n);
          const gmlBubble: Message = {
            id: `__getmyluck_${Date.now()}`,
            senderId: null, senderUsername: '', senderColor: 'FF9800',
            text: `🔮 Luck of ${payload.username} hari ini — ` +
                  `Cinta: ${stars(payload.love)} | Karir: ${stars(payload.career)} | ` +
                  `Kesehatan: ${stars(payload.health)} | Keberuntungan: ${stars(payload.luck)}`,
            isSystem: true, createdAt: new Date().toISOString(),
          };
          setMessages(prev => [...prev, gmlBubble]);
          setTimeout(() => flatListRef.current?.scrollToEnd({ animated: true }), 100);
        }

        // FOLLOW_OK — mirrors Follow.java sendMessageToSender "You are now following…"
        // Only the caller (sender) receives this — not broadcast to room.
        // Update followedUsers state so participant menu reflects the new relationship.
        if (payload.type === 'FOLLOW_OK') {
          setFollowedUsers(prev => { const s = new Set(prev); s.add(payload.username); return s; });
          const foMsg: Message = {
            id: `__follow_ok_${Date.now()}`,
            senderId: null, senderUsername: '', senderColor: '4CAF50',
            text: `➕ Kamu sekarang mengikuti ${payload.username}`,
            isSystem: true, createdAt: new Date().toISOString(),
          };
          setMessages(prev => [...prev, foMsg]);
          setTimeout(() => flatListRef.current?.scrollToEnd({ animated: true }), 100);
        }

        // UNFOLLOW_OK — companion to Follow.java; only caller sees confirmation.
        if (payload.type === 'UNFOLLOW_OK') {
          setFollowedUsers(prev => { const s = new Set(prev); s.delete(payload.username); return s; });
          const ufMsg: Message = {
            id: `__unfollow_ok_${Date.now()}`,
            senderId: null, senderUsername: '', senderColor: '9E9E9E',
            text: `➖ Kamu berhenti mengikuti ${payload.username}`,
            isSystem: true, createdAt: new Date().toISOString(),
          };
          setMessages(prev => [...prev, ufMsg]);
          setTimeout(() => flatListRef.current?.scrollToEnd({ animated: true }), 100);
        }

        // FLAMES — mirrors Flames.java sendMessageToAllUsersInChat
        // score > 0 → letter + label result (F/L/A/M/E/S); score == 0 → no match
        if (payload.type === 'FLAMES' && payload.roomId === roomId) {
          const flBubble: Message = {
            id: `__flames_${Date.now()}`,
            senderId: null, senderUsername: '', senderColor: 'FF5722',
            text: `🔥 ${payload.user1} dan ${payload.user2}: ${payload.emoji} ${payload.letter} — ${payload.label}!`,
            isSystem: true, createdAt: new Date().toISOString(),
          };
          setMessages(prev => [...prev, flBubble]);
          setTimeout(() => flatListRef.current?.scrollToEnd({ animated: true }), 100);
        }

        // FLAMES_NO_MATCH — mirrors Flames.java DEFAULT_NO_MATCH_MESSAGE (score == 0)
        if (payload.type === 'FLAMES_NO_MATCH' && payload.roomId === roomId) {
          const nmBubble: Message = {
            id: `__flames_nm_${Date.now()}`,
            senderId: null, senderUsername: '', senderColor: '9E9E9E',
            text: `😔 Sayang sekali, ${payload.user1} dan ${payload.user2} tidak cocok.`,
            isSystem: true, createdAt: new Date().toISOString(),
          };
          setMessages(prev => [...prev, nmBubble]);
          setTimeout(() => flatListRef.current?.scrollToEnd({ animated: true }), 100);
        }

        // LOVE_MATCH — mirrors LoveMatch.java sendMessageToAllUsersInChat
        // Received by every user in room; append as system message bubble with heart colour.
        if (payload.type === 'LOVE_MATCH' && payload.roomId === roomId) {
          const lmBubble: Message = {
            id: `__lovematch_${Date.now()}`,
            senderId: null, senderUsername: '', senderColor: 'E91E63',
            text: `💕 ${payload.user1} dan ${payload.user2} memiliki love match score: ${payload.score}%`,
            isSystem: true, createdAt: new Date().toISOString(),
          };
          setMessages(prev => [...prev, lmBubble]);
          setTimeout(() => flatListRef.current?.scrollToEnd({ animated: true }), 100);
        }

        // FIND_MY_MATCH — mirrors FindMyMatch.java sendMessageToAllUsersInChat
        if (payload.type === 'FIND_MY_MATCH' && payload.roomId === roomId) {
          const fmmBubble: Message = {
            id: `__findmymatch_${Date.now()}`,
            senderId: null, senderUsername: '', senderColor: 'E91E63',
            text: `💕 Match terbaik ${payload.seeker} adalah ${payload.match} dengan score: ${payload.score}%`,
            isSystem: true, createdAt: new Date().toISOString(),
          };
          setMessages(prev => [...prev, fmmBubble]);
          setTimeout(() => flatListRef.current?.scrollToEnd({ animated: true }), 100);
        }

        // BUMPED — mirrors Bump.java chatRoomPrx.bumpUser()
        // bumpUser = force-disconnect target from the room (soft kick, can rejoin).
        // If the current user is the one bumped, close the modal (they are disconnected).
        // All other clients remove the bumped user from the participant list.
        if (payload.type === 'BUMPED' && payload.roomId === roomId) {
          if (payload.username === myUsernameRef.current) {
            Alert.alert(
              '🔌 Disconnected',
              'Kamu telah di-disconnect dari room ini oleh moderator. Kamu bisa join kembali.',
              [{ text: 'OK', onPress: () => isActiveRef.current && onClose() }],
            );
          } else {
            setParticipants(prev => prev.filter(p => p.username !== payload.username));
            setParticipantCount(prev => Math.max(0, prev - 1));
          }
        }

        // MOD — user was promoted to mod; refresh participants + update own color if self
        if (payload.type === 'MOD' && payload.roomId === roomId) {
          loadParticipants();
          if (payload.username === myUsernameRef.current) {
            myRoleColorRef.current = 'FCC504';
          }
        }

        // UNMOD — user was demoted from mod; refresh participants + reset own color if self
        if (payload.type === 'UNMOD' && payload.roomId === roomId) {
          loadParticipants();
          if (payload.username === myUsernameRef.current) {
            myRoleColorRef.current = '2196F3';
          }
        }

        // KICKED — remove kicked user from participant list
        if (payload.type === 'KICKED' && payload.roomId === roomId) {
          setParticipants(prev => prev.filter(p => p.username !== payload.username));
          setParticipantCount(prev => Math.max(0, prev - 1));
          if (payload.username === myUsernameRef.current) {
            wasKickedRef.current = true;
          }
        }

        // CHAT_MESSAGE — incoming private chat message broadcast to this WS connection.
        // broadcastToUser() sends to ALL of the user's WS connections, including this room WS.
        // If no PrivateChatTab is open for this conversation, notify parent to open a tab.
        if (payload.type === 'CHAT_MESSAGE' && payload.conversationId) {
          const msg = payload.message as { senderId?: string; senderUsername?: string };
          if (msg?.senderId && msg.senderId !== currentUserIdRef.current) {
            onIncomingPrivateMessageRef.current?.(
              payload.conversationId,
              msg.senderUsername ?? '',
              msg.senderUsername ?? '',
            );
          }
        }
      } catch {}
    };

    ws.onerror = (e) => {
      console.log(`[WS] Error — scheduling reconnect`);
      clearWsTimers();
      scheduleReconnect();
    };

    ws.onclose = (e) => {
      console.log(`[WS] Disconnected code=${e.code} reason=${e.reason} — scheduling reconnect`);
      clearWsTimers();
      if (isActiveRef.current) {
        scheduleReconnect();
      }
    };
  }, [room, clearWsTimers]);

  // Keep connectWSRef in sync so scheduleReconnect can call the latest version
  connectWSRef.current = connectWS;

  // ── Effect 1: UI state — runs when visible or room changes ─────────────────
  // Only restores cached messages and resets UI. Does NOT touch the WebSocket
  // so that minimise → resume cycles never trigger a reconnect or message reload.
  useEffect(() => {
    if (!visible || !room) return;

    // Restore cached messages if returning to a room we've visited before,
    // otherwise reset to welcome messages so the room feels fresh on first entry.
    const cached = roomMessageCache.get(room.id);
    const isReturn = cached && cached.length > 0;

    setLoading(!isReturn);
    setMessages(isReturn ? cached : []);
    setInputText('');
    setShowUsers(false);
    setShowPicker(false);
    setShowOverflow(false);
    setShowRoomInfo(false);
    setContextMessage(null);
    setLoadingHistory(false);
    setHasMoreHistory(true);
    setParticipantCount(room.currentParticipants);
    if (!isReturn) loadMessages();

    // Load current user's contact list (following list) from server so the
    // participant menu correctly shows "Unfollow" vs "Add as fan" for users
    // the viewer already follows — mirrors FusionPktDataGetContacts (Android).
    (async () => {
      try {
        const headers = await buildHeaders();
        const res = await fetch(`${API_BASE}/api/me/following`, {
          credentials: 'include', headers,
        });
        if (res.ok) {
          const data = await res.json();
          const list: string[] = data.following ?? [];
          setFollowedUsers(new Set(list));
        }
      } catch {}
    })();
    // Load block list so incoming messages from blocked users are hidden
    (async () => {
      try {
        const headers = await buildHeaders();
        const res = await fetch(`${API_BASE}/api/chatrooms/${room.id}/cmd/blocklist`, {
          credentials: 'include', headers,
        });
        if (res.ok) {
          const data = await res.json();
          const list: string[] = (data.blockedUsers ?? []).map((u: string) => u.toLowerCase());
          setBlockedUsers(new Set(list));
        }
      } catch {}
    })();
  }, [visible, room, loadMessages]);

  // ── Effect 2: WebSocket lifecycle — only depends on room ID ─────────────────
  // Connects WS when a room is opened, keeps it alive across minimise/resume,
  // and only disconnects when the room itself changes or the component unmounts.
  // This prevents the "all messages reload on resume" issue caused by reconnecting
  // on every visible toggle.
  useEffect(() => {
    if (!room) return;

    isActiveRef.current = true;
    reconnectAttemptsRef.current = 0;
    setWsStatus('connecting');
    // Note: HTTP joinRoom removed — WS SUBSCRIBE is the canonical join
    // that broadcasts "has entered" (matches FusionPktJoinChatRoomOld).
    connectWSRef.current?.();

    // ── AppState listener ────────────────────────────────────────────────
    // Mirrors Android's network-availability check in FusionService.
    // When app returns to foreground, reconnect WS if it dropped.
    const appStateSub = AppState.addEventListener('change', (nextState) => {
      if (nextState === 'active' && isActiveRef.current) {
        const ws = wsRef.current;
        const needsReconnect = !ws || ws.readyState === WebSocket.CLOSED || ws.readyState === WebSocket.CLOSING;
        if (needsReconnect) {
          clearWsTimers();
          reconnectAttemptsRef.current = 0;
          connectWSRef.current?.();
        }
      }
    });

    return () => {
      // Deactivate — stop all reconnect attempts
      isActiveRef.current = false;
      clearWsTimers();

      // Close WS silently (null handlers so no client-side reconnect fires).
      // The server sees the close event and starts a 15s grace period before
      // broadcasting "has left", so quick back→reopen cycles are seamless.
      if (wsRef.current) {
        unregisterWS(wsRef.current);
        wsRef.current.onclose = null;
        wsRef.current.onerror = null;
        wsRef.current.close();
        wsRef.current = null;
      }

      appStateSub.remove();
      // No HTTP leaveRoom — server grace period handles "has left" broadcast.
      setWsStatus('disconnected');
      // Clear any active announcement banner when leaving the room
      setAnnounceText(null);
    };
  }, [room?.id, clearWsTimers]);

  // ── /whois command handler — mirrors Whois.java EmoteCommand ──────────────
  // Fetches target user profile and displays result only to the sender
  // (equivalent to chatSource.sendMessageToSender in Java).
  // Rate limit: 1 per 5 seconds per source (WhoisRateLimitExpr "1/5S").
  const handleWhoisCommand = useCallback(async (targetUsername: string) => {
    const now = Date.now();
    const WHOIS_RATE_LIMIT_MS = 5_000;
    if (now - whoisLastCallRef.current < WHOIS_RATE_LIMIT_MS) {
      const remaining = Math.ceil((WHOIS_RATE_LIMIT_MS - (now - whoisLastCallRef.current)) / 1000);
      const rateLimitMsg: Message = {
        id: `__whois_rl_${now}`,
        senderId: null,
        senderUsername: '',
        senderColor: ROOM_NAME_COLOR,
        text: `** Terlalu cepat. Tunggu ${remaining} detik sebelum menggunakan /whois lagi. **`,
        isSystem: true,
        createdAt: new Date().toISOString(),
      };
      setMessages(prev => [...prev, rateLimitMsg]);
      setTimeout(() => flatListRef.current?.scrollToEnd({ animated: true }), 100);
      return;
    }
    whoisLastCallRef.current = now;

    try {
      const headers = await buildHeaders();
      const res = await fetch(`${API_BASE}/api/profile/${encodeURIComponent(targetUsername)}`, {
        credentials: 'include', headers,
      });
      const data = await res.json();

      let resultText: string;
      if (!res.ok || !data.user) {
        // User not found — mirrors: messageData.messageText + " Not Found."
        resultText = `** ${targetUsername} : Not Found. **`;
      } else {
        const profile = data.profile;
        const gender   = profile?.gender   ?? 'Unknown';
        const migLevel = profile?.migLevel ?? 1;
        const location = profile?.country  ?? 'Unknown';
        // Mirrors: String.format(" Gender: %s, migLevel: %d, Location: %s.", gender, migLevel, country)
        resultText = `** ${targetUsername} : Gender: ${gender}, migLevel: ${migLevel}, Location: ${location}. **`;
      }

      const whoisMsg: Message = {
        id: `__whois_${now}`,
        senderId: null,
        senderUsername: '',
        senderColor: ROOM_NAME_COLOR,
        text: resultText,
        isSystem: true,
        createdAt: new Date().toISOString(),
      };
      setMessages(prev => [...prev, whoisMsg]);
      setTimeout(() => flatListRef.current?.scrollToEnd({ animated: true }), 100);
    } catch {
      const errMsg: Message = {
        id: `__whois_err_${Date.now()}`,
        senderId: null,
        senderUsername: '',
        senderColor: ROOM_NAME_COLOR,
        text: `** ${targetUsername} : Not Found. **`,
        isSystem: true,
        createdAt: new Date().toISOString(),
      };
      setMessages(prev => [...prev, errMsg]);
      setTimeout(() => flatListRef.current?.scrollToEnd({ animated: true }), 100);
    }
  }, [buildHeaders]);

  /* ─── Send message (with failure tracking) ─── */
  const sendMessage = useCallback(async (text?: string, failedMsgId?: string) => {
    const msg = (text ?? inputTextRef.current).trim();
    if (!msg || !room) return;
    if (!text) { setInputText(''); inputTextRef.current = ''; }

    // ── Intercept /announce command (mirrors Announce.java EmoteCommand) ─────────
    // Usage: /announce [pesan] [waktu]  or  /announce off
    // Rules (from Announce.java):
    //   - args.length < 2 → error
    //   - "off"  → send CMD announce_off via WS (chatRoomPrx.announceOff)
    //   - waitTime must be 3-4 digit integer, range 120-3600 (matches Announce.java check)
    //   - max message length 320 chars
    //   - waitTime -1 (absent) = one-shot; >0 = repeat every N seconds
    if (/^\/announce(\s|$)/i.test(msg)) {
      const rest = msg.replace(/^\/announce\s*/i, '').trim();
      if (!rest) {
        const usageMsg: Message = {
          id: `__announce_usage_${Date.now()}`,
          senderId: null, senderUsername: '', senderColor: ROOM_NAME_COLOR,
          text: '** Usage: /announce [pesan] [waktu] atau /announce off **',
          isSystem: true, createdAt: new Date().toISOString(),
        };
        setMessages(prev => [...prev, usageMsg]);
        setTimeout(() => flatListRef.current?.scrollToEnd({ animated: true }), 100);
        return;
      }
      const ws = wsRef.current;
      // /announce off → mirrors Announce.java args[1] === "off" → announceOff()
      if (rest.toLowerCase() === 'off') {
        if (ws && ws.readyState === WebSocket.OPEN) {
          ws.send(JSON.stringify({ type: 'CMD', cmd: 'announce_off', roomId: room.id }));
        } else { Alert.alert('Gagal', 'Koneksi terputus. Coba lagi.'); }
        return;
      }
      // Parse trailing waitTime — mirrors Announce.java Pattern "^(.*) ([0-9]+)$"
      let announceMsg = rest;
      let waitTime = -1;
      const trailMatch = rest.match(/^(.*)\s+([0-9]+)$/);
      if (trailMatch) {
        const s = trailMatch[2];
        const parsed = parseInt(s, 10);
        if (s.length >= 3 && s.length <= 4 && parsed >= 120 && parsed <= 3600) {
          announceMsg = trailMatch[1].trim();
          waitTime = parsed;
        } else if (s.length > 0) {
          // Number present but invalid range — mirrors Announce.java error
          const errMsg: Message = {
            id: `__announce_err_${Date.now()}`,
            senderId: null, senderUsername: '', senderColor: C.danger,
            text: '** Waktu tidak valid. Harus antara 120 sampai 3600 detik. **',
            isSystem: true, createdAt: new Date().toISOString(),
          };
          setMessages(prev => [...prev, errMsg]);
          setTimeout(() => flatListRef.current?.scrollToEnd({ animated: true }), 100);
          return;
        }
      }
      // Max 320 chars — matches Announce.java hardcoded limit
      if (announceMsg.length > 320) {
        const errMsg: Message = {
          id: `__announce_long_${Date.now()}`,
          senderId: null, senderUsername: '', senderColor: C.danger,
          text: '** Pesan tidak boleh lebih dari 320 karakter. **',
          isSystem: true, createdAt: new Date().toISOString(),
        };
        setMessages(prev => [...prev, errMsg]);
        setTimeout(() => flatListRef.current?.scrollToEnd({ animated: true }), 100);
        return;
      }
      if (ws && ws.readyState === WebSocket.OPEN) {
        ws.send(JSON.stringify({
          type: 'CMD', cmd: 'announce', roomId: room.id,
          message: announceMsg,
          ...(waitTime > 0 ? { waitTime } : {}),
        }));
      } else { Alert.alert('Gagal', 'Koneksi terputus. Coba lagi.'); }
      return;
    }

    // ── Intercept /getmyluck command (mirrors GetMyLuck.java EmoteCommand) ──────────
    // Usage: /getmyluck  (no args — mirrors Java args.length != 2 → error if any)
    // Server generates 4 values (1-5), caches in Redis 24h, broadcasts GET_MY_LUCK to room.
    if (/^\/getmyluck(\s|$)/i.test(msg)) {
      const ws = wsRef.current;
      if (ws && ws.readyState === WebSocket.OPEN) {
        ws.send(JSON.stringify({ type: 'CMD', cmd: 'getmyluck', roomId: room.id }));
      } else { Alert.alert('Gagal', 'Koneksi terputus. Coba lagi.'); }
      return;
    }

    // ── Intercept /follow [username] command (mirrors Follow.java EmoteCommand) ───────
    // Usage: /follow [username] or /f [username]
    // Mirrors Follow.java: args.length != 2 → error; sendMessageToSender only.
    // The FOLLOW_OK WS event updates followedUsers state so participant menu stays in sync.
    if (/^\/follow(\s|$)/i.test(msg) || /^\/f(\s|$)/.test(msg)) {
      const rest = msg.replace(/^\/f(?:ollow)?\s*/i, '').trim();
      if (!rest) {
        const usageMsg: Message = {
          id: `__fo_usage_${Date.now()}`,
          senderId: null, senderUsername: '', senderColor: '4CAF50',
          text: '** Usage: /follow [username] **',
          isSystem: true, createdAt: new Date().toISOString(),
        };
        setMessages(prev => [...prev, usageMsg]);
        setTimeout(() => flatListRef.current?.scrollToEnd({ animated: true }), 100);
        return;
      }
      const ws = wsRef.current;
      if (ws && ws.readyState === WebSocket.OPEN) {
        ws.send(JSON.stringify({ type: 'CMD', cmd: 'follow', roomId: room.id, message: rest }));
      } else { Alert.alert('Gagal', 'Koneksi terputus. Coba lagi.'); }
      return;
    }

    // ── Intercept /unfollow [username] command ────────────────────────────────────
    // Usage: /unfollow [username]
    // sendMessageToSender only — companion to Follow.java.
    if (/^\/unfollow(\s|$)/i.test(msg)) {
      const rest = msg.replace(/^\/unfollow\s*/i, '').trim();
      if (!rest) {
        const usageMsg: Message = {
          id: `__ufo_usage_${Date.now()}`,
          senderId: null, senderUsername: '', senderColor: '9E9E9E',
          text: '** Usage: /unfollow [username] **',
          isSystem: true, createdAt: new Date().toISOString(),
        };
        setMessages(prev => [...prev, usageMsg]);
        setTimeout(() => flatListRef.current?.scrollToEnd({ animated: true }), 100);
        return;
      }
      const ws = wsRef.current;
      if (ws && ws.readyState === WebSocket.OPEN) {
        ws.send(JSON.stringify({ type: 'CMD', cmd: 'unfollow', roomId: room.id, message: rest }));
      } else { Alert.alert('Gagal', 'Koneksi terputus. Coba lagi.'); }
      return;
    }

    // ── Intercept /flames [user1] [user2] command (mirrors Flames.java) ─────────────
    // Usage: /flames [user1] [user2]
    // Available to all users — matches Java EmoteCommand (no FilteringEmoteCommand).
    // Server computes getFlamesScore → maps score % 6 → FLAMES_VALUES → broadcasts to room.
    // score == 0 → "Too bad, not a match" (mirrors Flames.java DEFAULT_NO_MATCH_MESSAGE).
    if (/^\/flames(\s|$)/i.test(msg)) {
      const rest = msg.replace(/^\/flames\s*/i, '').trim();
      const parts = rest.split(/\s+/).filter(Boolean);
      if (parts.length < 2) {
        const usageMsg: Message = {
          id: `__fl_usage_${Date.now()}`,
          senderId: null, senderUsername: '', senderColor: 'FF5722',
          text: '** Usage: /flames [user1] [user2] **',
          isSystem: true, createdAt: new Date().toISOString(),
        };
        setMessages(prev => [...prev, usageMsg]);
        setTimeout(() => flatListRef.current?.scrollToEnd({ animated: true }), 100);
        return;
      }
      const ws = wsRef.current;
      if (ws && ws.readyState === WebSocket.OPEN) {
        ws.send(JSON.stringify({
          type: 'CMD', cmd: 'flames', roomId: room.id,
          message: `${parts[0]} ${parts[1]}`,
        }));
      } else { Alert.alert('Gagal', 'Koneksi terputus. Coba lagi.'); }
      return;
    }

    // ── Intercept /lovematch [user1] [user2] command (mirrors LoveMatch.java) ──────
    // Usage: /lovematch [user1] [user2]
    // Available to all users (not admin-only) — matches Java EmoteCommand (no FilteringEmoteCommand).
    // Server calculates score and broadcasts LOVE_MATCH to everyone in room.
    if (/^\/lovematch(\s|$)/i.test(msg)) {
      const rest = msg.replace(/^\/lovematch\s*/i, '').trim();
      const parts = rest.split(/\s+/).filter(Boolean);
      if (parts.length < 2) {
        const usageMsg: Message = {
          id: `__lm_usage_${Date.now()}`,
          senderId: null, senderUsername: '', senderColor: 'E91E63',
          text: '** Usage: /lovematch [user1] [user2] **',
          isSystem: true, createdAt: new Date().toISOString(),
        };
        setMessages(prev => [...prev, usageMsg]);
        setTimeout(() => flatListRef.current?.scrollToEnd({ animated: true }), 100);
        return;
      }
      const ws = wsRef.current;
      if (ws && ws.readyState === WebSocket.OPEN) {
        ws.send(JSON.stringify({
          type: 'CMD', cmd: 'lovematch', roomId: room.id,
          message: `${parts[0]} ${parts[1]}`,
        }));
      } else { Alert.alert('Gagal', 'Koneksi terputus. Coba lagi.'); }
      return;
    }

    // ── Intercept /findmymatch command (mirrors FindMyMatch.java) ────────────────
    // Usage: /findmymatch
    // Server scans all users in room, computes scores, finds best match,
    // broadcasts FIND_MY_MATCH to everyone — mirrors sendMessageToAllUsersInChat.
    if (/^\/findmymatch(\s|$)/i.test(msg)) {
      const ws = wsRef.current;
      if (ws && ws.readyState === WebSocket.OPEN) {
        ws.send(JSON.stringify({ type: 'CMD', cmd: 'findmymatch', roomId: room.id }));
      } else { Alert.alert('Gagal', 'Koneksi terputus. Coba lagi.'); }
      return;
    }

    // ── Intercept /bump [username] command (mirrors Bump.java FilteringEmoteCommand) ──
    // Usage: /bump [username]
    // Syntax: exactly 2 args — mirrors Bump.java checkSyntax: cmdArgs.length != 2 → error
    // Action: force-disconnect (soft kick) target user from room; they can rejoin freely.
    // Rate limit & admin/mod check enforced server-side.
    if (/^\/bump(\s|$)/i.test(msg)) {
      const parts = msg.trim().split(/\s+/);
      if (parts.length !== 2 || !parts[1]) {
        const usageMsg: Message = {
          id: `__bump_usage_${Date.now()}`,
          senderId: null,
          senderUsername: '',
          senderColor: ROOM_NAME_COLOR,
          text: `** Usage: /bump [username] **`,
          isSystem: true,
          createdAt: new Date().toISOString(),
        };
        setMessages(prev => [...prev, usageMsg]);
        setTimeout(() => flatListRef.current?.scrollToEnd({ animated: true }), 100);
        return;
      }
      const bumpTarget = parts[1];
      try {
        const headers = await buildHeaders(true);
        const res = await fetch(
          `${API_BASE}/api/chatrooms/${room.id}/cmd/bump/${encodeURIComponent(bumpTarget)}`,
          { method: 'POST', headers, credentials: 'include' },
        );
        const data = await res.json();
        if (!res.ok) {
          const errMsg: Message = {
            id: `__bump_err_${Date.now()}`,
            senderId: null,
            senderUsername: '',
            senderColor: C.danger,
            text: `** ${data.message ?? 'Gagal bump user.'} **`,
            isSystem: true,
            createdAt: new Date().toISOString(),
          };
          setMessages(prev => [...prev, errMsg]);
          setTimeout(() => flatListRef.current?.scrollToEnd({ animated: true }), 100);
        }
      } catch {
        Alert.alert('Gagal', 'Tidak dapat menghubungi server.');
      }
      return;
    }

    // ── Intercept /warn command (mirrors Warn.java FilteringEmoteCommand) ──────
    // Usage: /warn [username] -m [message]
    // Syntax: args.length < 2 OR args.length == 3 → error (Java Warn.checkSyntax)
    // Sends CMD packet via WS; server broadcasts WARNED + system message to room.
    // Rate limit handled server-side (Warn.getRateLimitThreshold).
    if (/^\/warn(\s|$)/i.test(msg)) {
      const parts = msg.trim().split(/\s+/);
      // parts[0]=cmd, parts[1]=username, parts[2]="-m", parts[3..]=message
      const WARN_MESSAGE_MAX_LENGTH = 200;
      if (parts.length < 2 || !parts[1]) {
        const usageMsg: Message = {
          id: `__warn_usage_${Date.now()}`,
          senderId: null,
          senderUsername: '',
          senderColor: ROOM_NAME_COLOR,
          text: '** Usage: /warn [username] -m [message] **',
          isSystem: true,
          createdAt: new Date().toISOString(),
        };
        setMessages(prev => [...prev, usageMsg]);
        setTimeout(() => flatListRef.current?.scrollToEnd({ animated: true }), 100);
        return;
      }
      // args.length == 3 means /warn user -m with no message (Java: invalid)
      if (parts.length === 3) {
        const usageMsg: Message = {
          id: `__warn_usage_${Date.now()}`,
          senderId: null,
          senderUsername: '',
          senderColor: ROOM_NAME_COLOR,
          text: '** Usage: /warn [username] -m [message] **',
          isSystem: true,
          createdAt: new Date().toISOString(),
        };
        setMessages(prev => [...prev, usageMsg]);
        setTimeout(() => flatListRef.current?.scrollToEnd({ animated: true }), 100);
        return;
      }
      const warnTarget = parts[1];
      // Build message from args[3..] (words after -m), or empty if -m not provided
      const hasDashM = parts[2]?.toLowerCase() === '-m';
      const warnMessageWords = hasDashM ? parts.slice(3) : [];
      const warnMessage = warnMessageWords.join(' ');
      if (warnMessage.length > WARN_MESSAGE_MAX_LENGTH) {
        const errMsg: Message = {
          id: `__warn_toolong_${Date.now()}`,
          senderId: null,
          senderUsername: '',
          senderColor: ROOM_NAME_COLOR,
          text: `** Pesan peringatan terlalu panjang (maks ${WARN_MESSAGE_MAX_LENGTH} karakter). **`,
          isSystem: true,
          createdAt: new Date().toISOString(),
        };
        setMessages(prev => [...prev, errMsg]);
        setTimeout(() => flatListRef.current?.scrollToEnd({ animated: true }), 100);
        return;
      }
      const ws = wsRef.current;
      if (ws && ws.readyState === WebSocket.OPEN) {
        ws.send(JSON.stringify({
          type: 'CMD',
          cmd: 'warn',
          target: warnTarget,
          message: warnMessage || undefined,
          roomId: room.id,
        }));
      } else {
        Alert.alert('Gagal', 'Koneksi terputus. Coba lagi.');
      }
      return;
    }

    // ── Intercept /whois command (mirrors Whois.java EmoteCommand) ──────────
    // Usage: /whois [username]
    // Result shown only to sender, not broadcast to the room.
    if (/^\/whois(\s|$)/i.test(msg)) {
      const parts = msg.trim().split(/\s+/);
      if (parts.length < 2 || !parts[1]) {
        const usageMsg: Message = {
          id: `__whois_usage_${Date.now()}`,
          senderId: null,
          senderUsername: '',
          senderColor: ROOM_NAME_COLOR,
          text: '** Usage: /whois [username] **',
          isSystem: true,
          createdAt: new Date().toISOString(),
        };
        setMessages(prev => [...prev, usageMsg]);
        setTimeout(() => flatListRef.current?.scrollToEnd({ animated: true }), 100);
        return;
      }
      await handleWhoisCommand(parts[1]);
      return;
    }

    // ── Intercept /mod [username] command (mirrors Mod.java FilteringEmoteCommand) ──
    // Usage: /mod [username]
    // Owner-only — server enforces; sends CMD "mod" via WS → broadcasts MOD event + sysMsg.
    if (/^\/mod(\s|$)/i.test(msg)) {
      const parts = msg.trim().split(/\s+/);
      if (parts.length < 2 || !parts[1]) {
        const usageMsg: Message = {
          id: `__mod_usage_${Date.now()}`,
          senderId: null, senderUsername: '', senderColor: ROOM_NAME_COLOR,
          text: '** Usage: /mod [username] **',
          isSystem: true, createdAt: new Date().toISOString(),
        };
        setMessages(prev => [...prev, usageMsg]);
        setTimeout(() => flatListRef.current?.scrollToEnd({ animated: true }), 100);
        return;
      }
      const ws = wsRef.current;
      if (ws && ws.readyState === WebSocket.OPEN) {
        ws.send(JSON.stringify({ type: 'CMD', cmd: 'mod', target: parts[1], roomId: room.id }));
      } else { Alert.alert('Gagal', 'Koneksi terputus. Coba lagi.'); }
      return;
    }

    // ── Intercept /unmod [username] command (mirrors Unmod.java FilteringEmoteCommand) ──
    // Usage: /unmod [username]
    // Owner-only — server enforces; sends CMD "unmod" via WS → broadcasts UNMOD event + sysMsg.
    if (/^\/unmod(\s|$)/i.test(msg)) {
      const parts = msg.trim().split(/\s+/);
      if (parts.length < 2 || !parts[1]) {
        const usageMsg: Message = {
          id: `__unmod_usage_${Date.now()}`,
          senderId: null, senderUsername: '', senderColor: ROOM_NAME_COLOR,
          text: '** Usage: /unmod [username] **',
          isSystem: true, createdAt: new Date().toISOString(),
        };
        setMessages(prev => [...prev, usageMsg]);
        setTimeout(() => flatListRef.current?.scrollToEnd({ animated: true }), 100);
        return;
      }
      const ws = wsRef.current;
      if (ws && ws.readyState === WebSocket.OPEN) {
        ws.send(JSON.stringify({ type: 'CMD', cmd: 'unmod', target: parts[1], roomId: room.id }));
      } else { Alert.alert('Gagal', 'Koneksi terputus. Coba lagi.'); }
      return;
    }

    // ── Intercept /bot, /botstop, /games commands ─────────────────────────────
    // Mirrors Bot.java / BotStop.java / SendGamesHelpToUser.java slash commands.
    // These are handled server-side inside the SEND_MESSAGE handler — the server
    // intercepts and does NOT echo the text back, so we must NOT show an optimistic
    // bubble here (otherwise the command text stays visible in the chat forever).
    if (/^\/bot(\s|$)/i.test(msg) || /^\/botstop(\s|$)/i.test(msg) || /^\/games(\s|$)/i.test(msg)) {
      const ws = wsRef.current;
      if (ws && ws.readyState === WebSocket.OPEN) {
        ws.send(JSON.stringify({ type: 'SEND_MESSAGE', roomId: room.id, text: msg }));
      } else {
        Alert.alert('Gagal', 'Koneksi terputus. Coba lagi.');
      }
      return;
    }

    const tempId = failedMsgId ?? `__temp_${Date.now()}`;
    const ws = wsRef.current;

    // WebSocket must be open — SEND_MESSAGE goes directly through the real-time socket.
    // This mirrors the Java TCP packet flow: client.write(packet) → server.onData → broadcast.
    if (!ws || ws.readyState !== WebSocket.OPEN) {
      // Mirrors ChatController.resendAllFailedMessages(): queue message for resend on reconnect
      messageQueue.enqueue({
        conversationId: room.id,
        text:           msg,
        senderUsername: myUsernameRef.current,
      }).catch(() => {});
      // Show as failed optimistic bubble
      const failedBubble: Message = {
        id: tempId,
        senderId: currentUserId,
        senderUsername: myUsernameRef.current,
        senderColor: '2196F3',
        text: msg,
        isSystem: false,
        createdAt: new Date().toISOString(),
        failed: true,
      };
      setMessages(prev => [...prev, failedBubble]);
      setTimeout(() => flatListRef.current?.scrollToEnd({ animated: true }), 80);
      Alert.alert('Koneksi terputus', 'Pesan akan dikirim ulang saat terhubung kembali.');
      return;
    }

    // Skip optimistic render for bot !commands — the command itself should
    // never appear in chat; only the bot's response (a real WS MESSAGE) should.
    // Mirrors Java ChatSession: sendMessageToBots() suppresses the raw text.
    const isBotCommand = msg.startsWith('!');

    if (!failedMsgId && !isBotCommand) {
      const optimistic: Message = {
        id: tempId,
        senderId: currentUserId,
        senderUsername: myUsernameRef.current,
        senderColor: myRoleColorRef.current,  // use role color so owner sees yellow immediately
        text: msg,
        isSystem: false,
        createdAt: new Date().toISOString(),
      };
      pendingTempRef.current.add(tempId);
      setMessages(prev => [...prev, optimistic]);
      setTimeout(() => flatListRef.current?.scrollToEnd({ animated: true }), 80);
    }

    // Send directly via WebSocket — server saves to DB and broadcasts MESSAGE to all
    // subscribers (including sender). The WS MESSAGE handler replaces this optimistic
    // in-place when the broadcast arrives (no flash, no gap, no HTTP round-trip).
    ws.send(JSON.stringify({ type: 'SEND_MESSAGE', roomId: room.id, text: msg }));

    // Safety fallback: if WS broadcast never arrives within 5s (e.g. server error without
    // sending ERROR event), clean up the optimistic so it doesn't linger.
    setTimeout(() => {
      if (pendingTempRef.current.has(tempId)) {
        pendingTempRef.current.delete(tempId);
        setMessages(prev => prev.filter(m => m.id !== tempId));
      }
    }, 5000);
  }, [room, currentUserId, handleWhoisCommand]);

  // Back button (icon or Android hardware) — close the modal UI only.
  // We do NOT send HTTP leave or WS UNSUBSCRIBE here: the server starts a
  // 15s grace period on WS close.  If the user reopens the room within that
  // window no "has left"/"has entered" messages are emitted (seamless rejoin).
  // In embedded (multi-tab) mode, calls onLeaveTab instead — the parent removes
  // this room from the tabs array, which unmounts the component and cleans up WS.
  const handleClose = useCallback(() => {
    // Stop reconnect attempts — isActiveRef gates all timers
    isActiveRef.current = false;
    clearWsTimers();
    // Null out WS handlers before closing so client reconnect logic never fires
    if (wsRef.current) {
      unregisterWS(wsRef.current);
      wsRef.current.onclose = null;
      wsRef.current.onerror = null;
      wsRef.current.close();
      wsRef.current = null;
    }
    setWsStatus('disconnected');
    setShowUsers(false);
    setShowPicker(false);
    setShowOverflow(false);
    setShowRoomInfo(false);
    setContextMessage(null);
    if (isEmbedded && onLeaveTab) {
      // Clear cache so next open of this room starts fresh (user explicitly left)
      if (room?.id) roomMessageCache.delete(room.id);
      onLeaveTab();
    } else {
      onClose();
    }
  }, [onClose, clearWsTimers, isEmbedded, onLeaveTab, room?.id]);

  // Explicit leave: send UNSUBSCRIBE first so the server immediately broadcasts
  // "has left" to all participants — no 15-second grace period delay.
  const handleLeave = useCallback(() => {
    if (room && wsRef.current && wsRef.current.readyState === WebSocket.OPEN) {
      wsRef.current.send(JSON.stringify({ type: 'UNSUBSCRIBE', roomId: room.id }));
    }
    handleClose();
  }, [room, handleClose]);

  const toggleUsers = useCallback(() => {
    setShowOverflow(false);
    if (!showUsers) loadParticipants();
    setShowUsers(v => !v);
  }, [showUsers, loadParticipants]);

  /* ─── Context menu: long-press message ─── */
  const handleLongPressMessage = useCallback((msg: Message) => {
    setContextMessage(msg);
  }, []);

  const contextMenuItems: MenuItem[] = contextMessage ? [
    ...(!contextMessage.isSystem && !contextMessage.isRoomInfo ? [
      {
        id: 'copy',
        label: 'Copy',
        icon: <Ionicons name="copy-outline" size={20} color={C.menuIcon} />,
      },
      {
        id: 'share',
        label: 'Share',
        icon: <Ionicons name="share-outline" size={20} color={C.menuIcon} />,
      },
    ] : []),
    ...(isChatRoomAdmin && !contextMessage.isSystem && !contextMessage.isRoomInfo && !contextMessage.failed ? [
      {
        id: 'pin',
        label: contextMessage.isPinned ? 'Unpin' : 'Pin',
        icon: <MaterialIcons name="push-pin" size={20} color={C.menuIcon} />,
      },
    ] : []),
    ...(contextMessage.failed ? [
      {
        id: 'retry',
        label: 'Try again',
        icon: <Ionicons name="refresh" size={20} color="#4CAF50" />,
      },
      {
        id: 'delete_failed',
        label: 'Hapus pesan',
        icon: <Ionicons name="trash-outline" size={20} color={C.danger} />,
        isDanger: true,
      },
    ] : []),
  ] : [];

  const handleContextMenuSelect = useCallback((id: string) => {
    const msg = contextMessage;
    setContextMessage(null);
    if (!msg) return;

    switch (id) {
      case 'copy':
        Clipboard.setStringAsync(msg.text).then(() => {
          Alert.alert('Disalin', 'Pesan telah disalin ke clipboard.');
        });
        break;

      case 'share':
        Share.share({ message: msg.text });
        break;

      case 'pin':
        if (msg.isPinned) {
          setMessages(prev => prev.map(m =>
            m.id === msg.id ? { ...m, isPinned: false } : m,
          ));
          Alert.alert('Unpin', 'Pesan telah di-unpin.');
        } else {
          setMessages(prev => prev.map(m => ({
            ...m,
            isPinned: m.id === msg.id ? true : false,
          })));
          Alert.alert('Pinned', 'Pesan telah di-pin.');
        }
        break;

      case 'retry':
        sendMessage(msg.text, msg.id);
        break;

      case 'delete_failed':
        setMessages(prev => prev.filter(m => m.id !== msg.id));
        break;
    }
  }, [contextMessage, sendMessage]);

  /* ─── Overflow menu items ─── */
  const overflowItems: MenuItem[] = room ? [
    {
      id: 'share',
      label: 'Share chatroom',
      icon: <Ionicons name="share-social-outline" size={20} color="#444444" />,
    },
    {
      id: 'participants',
      label: 'View participants',
      icon: <Ionicons name="people-outline" size={20} color="#444444" />,
    },
    {
      id: 'invite',
      label: 'Invite people',
      icon: <Ionicons name="person-add-outline" size={20} color="#444444" />,
    },
    {
      id: 'favorite',
      label: isFavorite ? 'Remove from favorites' : 'Add to favorites',
      icon: <Ionicons
        name={isFavorite ? 'star' : 'star-outline'}
        size={20}
        color={isFavorite ? C.gold : '#444444'}
      />,
    },
    {
      id: 'info',
      label: 'Room info',
      icon: <Ionicons name="information-circle-outline" size={20} color="#444444" />,
    },
    {
      id: 'report',
      label: 'Report abuse',
      icon: <MaterialIcons name="report-gmailerrorred" size={20} color="#444444" />,
    },
    {
      id: 'mute',
      label: isMuted ? 'Unmute' : 'Mute',
      icon: <Ionicons
        name={isMuted ? 'volume-mute' : 'volume-high-outline'}
        size={20}
        color="#444444"
      />,
    },
    {
      id: 'leave',
      label: 'Leave chat',
      icon: <MaterialIcons name="exit-to-app" size={20} color={C.danger} />,
      isDanger: true,
    },
  ] : [];

  const handleOverflowSelect = useCallback((id: string) => {
    setShowOverflow(false);
    if (!room) return;

    switch (id) {
      case 'share':
        Share.share({
          message: `Join me in the "${room.name}" chatroom on migchat!`,
          title: room.name,
        });
        break;
      case 'participants':
        loadParticipants();
        setShowUsers(true);
        break;
      case 'invite':
        Share.share({
          message: `Hey! Come join the "${room.name}" chatroom on migchat!`,
          title: `Invite to ${room.name}`,
        });
        break;
      case 'favorite':
        (async () => {
          const next = !isFavorite;
          const method = next ? 'POST' : 'DELETE';
          try {
            const headers = await buildHeaders();
            const res = await fetch(`${API_BASE}/api/chatrooms/${room.id}/favourite`, {
              method,
              credentials: 'include',
              headers,
            });
            if (res.ok) {
              setIsFavorite(next);
              Alert.alert(
                next ? 'Ditambahkan ke favorit' : 'Dihapus dari favorit',
                next
                  ? `"${room.name}" telah ditambahkan ke favorit.`
                  : `"${room.name}" telah dihapus dari favorit.`,
                [{ text: 'OK' }],
              );
            } else {
              Alert.alert('Gagal', 'Tidak dapat mengubah status favorit. Coba lagi.');
            }
          } catch {
            Alert.alert('Gagal', 'Tidak dapat terhubung ke server.');
          }
        })();
        break;
      case 'info':
        setShowRoomInfo(true);
        break;
      case 'report':
        Alert.alert(
          'Report Abuse',
          `Laporkan room "${room.name}" karena pelanggaran?`,
          [
            { text: 'Batal', style: 'cancel' },
            {
              text: 'Laporkan',
              style: 'destructive',
              onPress: () => Alert.alert('Terima kasih', 'Laporan kamu telah dikirim.'),
            },
          ],
        );
        break;
      case 'mute':
        setIsMuted(prev => {
          const next = !prev;
          Alert.alert(
            next ? 'Room di-mute' : 'Room di-unmute',
            next
              ? `Notifikasi dari "${room.name}" dimatikan.`
              : `Notifikasi dari "${room.name}" dinyalakan kembali.`,
            [{ text: 'OK' }],
          );
          return next;
        });
        break;
      case 'leave':
        Alert.alert(
          'Keluar dari room?',
          `Kamu akan keluar dari "${room.name}".`,
          [
            { text: 'Batal', style: 'cancel' },
            { text: 'Keluar', style: 'destructive', onPress: () => handleLeave() },
          ],
        );
        break;
    }
  }, [room, loadParticipants, handleClose, handleLeave]);

  /* ─── Participant menu ─── */
  const handleOpenParticipantMenu = useCallback((p: Participant) => {
    setShowUsers(false);
    setParticipantMenuTarget(p);
  }, []);

  const participantMenuItems: MenuItem[] = participantMenuTarget ? [
    ...(!followedUsers.has(participantMenuTarget.username) ? [{
      id: 'follow',
      label: 'Add as fan',
      icon: <Ionicons name="person-add-outline" size={20} color={C.menuIcon} />,
    }] : [{
      id: 'unfollow',
      label: 'Unfollow',
      icon: <Ionicons name="person-remove-outline" size={20} color={C.menuIcon} />,
    }]),
    {
      id: 'private_chat',
      label: 'Private chat',
      icon: <Ionicons name="chatbubble-outline" size={20} color={C.menuIcon} />,
    },
    {
      id: 'view_profile',
      label: 'View profile',
      icon: <Ionicons name="person-circle-outline" size={20} color={C.menuIcon} />,
    },
    {
      id: 'send_gift',
      label: 'Send gift',
      icon: <Ionicons name="gift-outline" size={20} color={C.menuIcon} />,
    },
    {
      id: 'block',
      label: blockedUsers.has(participantMenuTarget.username) ? 'Unblock' : 'Block',
      icon: <MaterialIcons name="block" size={20} color={C.danger} />,
      isDanger: !blockedUsers.has(participantMenuTarget.username),
    },
    ...(isChatRoomAdmin ? [{
      id: 'bump',
      label: 'Bump (Disconnect)',
      icon: <MaterialIcons name="power-off" size={20} color="#FF8C00" />,
      isDanger: false,
    }, {
      id: 'warn',
      label: 'Warn',
      icon: <MaterialIcons name="warning" size={20} color="#FF8C00" />,
      isDanger: false,
    }] : []),
    {
      id: 'kick',
      label: 'Kick',
      icon: <MaterialIcons name="sports-kabaddi" size={20} color={C.danger} />,
      isDanger: true,
    },
    {
      id: 'report',
      label: 'Report abuse',
      icon: <MaterialIcons name="report-gmailerrorred" size={20} color={C.menuIcon} />,
    },
  ] : [];

  const handleParticipantMenuSelect = useCallback(async (id: string) => {
    const target = participantMenuTarget;
    setParticipantMenuTarget(null);
    if (!target || !room) return;

    const name = target.displayName || target.username;

    switch (id) {
      case 'follow': {
        // Mirrors Follow.java: contactBean.addFusionUserAsContact + sendMessageToSender
        // Sends CMD follow via WS so the server persists to contacts table and
        // returns FOLLOW_OK which shows "➕ Kamu sekarang mengikuti X" in chat.
        const ws = wsRef.current;
        if (ws && ws.readyState === WebSocket.OPEN) {
          ws.send(JSON.stringify({ type: 'CMD', cmd: 'follow', roomId: room.id, message: target.username }));
        } else {
          // Fallback to HTTP when WS is unavailable
          try {
            const headers = await buildHeaders(true);
            await fetch(`${API_BASE}/api/users/${target.username}/follow`, {
              method: 'POST', credentials: 'include', headers,
            });
            setFollowedUsers(prev => { const s = new Set(prev); s.add(target.username); return s; });
            Alert.alert('Add as fan', `Kamu sekarang mengikuti ${name}.`);
          } catch {
            Alert.alert('Gagal', 'Tidak dapat follow user saat ini.');
          }
        }
        break;
      }

      case 'unfollow': {
        // Mirrors Follow.java companion: removes from contacts table + sendMessageToSender
        const ws = wsRef.current;
        if (ws && ws.readyState === WebSocket.OPEN) {
          ws.send(JSON.stringify({ type: 'CMD', cmd: 'unfollow', roomId: room.id, message: target.username }));
        } else {
          try {
            const headers = await buildHeaders(true);
            await fetch(`${API_BASE}/api/users/${target.username}/follow`, {
              method: 'DELETE', credentials: 'include', headers,
            });
            setFollowedUsers(prev => { const s = new Set(prev); s.delete(target.username); return s; });
            Alert.alert('Unfollow', `Kamu tidak lagi mengikuti ${name}.`);
          } catch {
            Alert.alert('Gagal', 'Tidak dapat unfollow user saat ini.');
          }
        }
        break;
      }

      case 'private_chat':
        setShowUsers(false);
        if (onOpenPrivateChat) {
          onOpenPrivateChat(target.username, target.displayName || target.username);
        } else {
          Alert.alert('Private chat', `Membuka private chat dengan ${name}...`);
        }
        break;

      case 'view_profile':
        setViewProfileTarget(target);
        break;

      case 'send_gift':
        setShowGiftForUser(target);
        setShowPicker(true);
        break;

      case 'block': {
        const alreadyBlocked = blockedUsers.has(target.username);
        if (alreadyBlocked) {
          try {
            const headers = await buildHeaders(true);
            await fetch(`${API_BASE}/api/users/${target.username}/block`, {
              method: 'DELETE', credentials: 'include', headers,
            });
            setBlockedUsers(prev => { const s = new Set(prev); s.delete(target.username); return s; });
            Alert.alert('Unblock', `${name} sudah di-unblock.`);
          } catch {
            Alert.alert('Gagal', 'Tidak dapat unblock user saat ini.');
          }
        } else {
          Alert.alert(
            'Block pengguna?',
            `${name} tidak akan bisa mengirimmu pesan.`,
            [
              { text: 'Batal', style: 'cancel' },
              {
                text: 'Block',
                style: 'destructive',
                onPress: async () => {
                  try {
                    const headers = await buildHeaders(true);
                    await fetch(`${API_BASE}/api/users/${target.username}/block`, {
                      method: 'POST', credentials: 'include', headers,
                    });
                    const blockedLower = target.username.toLowerCase();
                    setBlockedUsers(prev => { const s = new Set(prev); s.add(blockedLower); return s; });
                    // Remove all existing messages from the blocked user
                    setMessages(prev => prev.filter(
                      m => (m.senderUsername ?? '').toLowerCase() !== blockedLower
                    ));
                  } catch {
                    Alert.alert('Gagal', 'Tidak dapat memblokir user saat ini.');
                  }
                },
              },
            ],
          );
        }
        break;
      }

      case 'bump': {
        // Mirrors Bump.java: chatRoomPrx.bumpUser(source, target)
        // Force-disconnect (soft kick) target from room — can rejoin freely.
        // Syntax: /bump [username] — exactly 2 args (matches Bump.java checkSyntax)
        Alert.alert(
          'Bump (Disconnect)?',
          `${name} akan di-disconnect dari room ini. Mereka bisa join kembali.`,
          [
            { text: 'Batal', style: 'cancel' },
            {
              text: 'Bump',
              style: 'default',
              onPress: async () => {
                try {
                  const headers = await buildHeaders(true);
                  const res = await fetch(
                    `${API_BASE}/api/chatrooms/${room!.id}/cmd/bump/${encodeURIComponent(target.username)}`,
                    { method: 'POST', headers, credentials: 'include' },
                  );
                  const data = await res.json();
                  if (!res.ok) {
                    Alert.alert('Gagal', data.message ?? 'Tidak dapat bump user saat ini.');
                  }
                } catch {
                  Alert.alert('Gagal', 'Tidak dapat menghubungi server.');
                }
              },
            },
          ],
        );
        break;
      }

      case 'warn': {
        // Mirrors Warn.java: chatRoomPrx.warnUser(source, target, message)
        // Sends CMD warn via WS — admin/mod only (enforced by server)
        Alert.alert(
          'Warn pengguna?',
          `Kirim peringatan ke ${name}?\n\nUntuk menambahkan pesan, ketik /warn ${target.username} -m [pesan] di chat.`,
          [
            { text: 'Batal', style: 'cancel' },
            {
              text: 'Warn',
              style: 'default',
              onPress: () => {
                const ws = wsRef.current;
                if (ws && ws.readyState === WebSocket.OPEN) {
                  ws.send(JSON.stringify({
                    type: 'CMD',
                    cmd: 'warn',
                    target: target.username,
                    roomId: room!.id,
                  }));
                } else {
                  Alert.alert('Gagal', 'Koneksi terputus. Coba lagi.');
                }
              },
            },
          ],
        );
        break;
      }

      case 'kick': {
        Alert.alert(
          'Kick pengguna?',
          `${name} akan dikeluarkan dari room ini.`,
          [
            { text: 'Batal', style: 'cancel' },
            {
              text: 'Kick',
              style: 'destructive',
              onPress: async () => {
                try {
                  const headers = await buildHeaders(true);
                  const res = await fetch(
                    `${API_BASE}/api/chatrooms/${room.id}/kick/${target.username}`,
                    { method: 'POST', credentials: 'include', headers },
                  );
                  if (res.ok) {
                    setParticipants(prev => prev.filter(p => p.id !== target.id));
                    setParticipantCount(prev => Math.max(0, prev - 1));
                    Alert.alert('Kicked', `${name} telah dikeluarkan dari room.`);
                  } else {
                    const data = await res.json();
                    Alert.alert('Gagal', data.message ?? 'Tidak dapat kick user.');
                  }
                } catch {
                  Alert.alert('Gagal', 'Tidak dapat kick user saat ini.');
                }
              },
            },
          ],
        );
        break;
      }

      case 'report': {
        Alert.alert(
          'Report abuse',
          `Laporkan ${name} karena pelanggaran?`,
          [
            { text: 'Batal', style: 'cancel' },
            {
              text: 'Laporkan',
              style: 'destructive',
              onPress: async () => {
                try {
                  const headers = await buildHeaders(true);
                  await fetch(`${API_BASE}/api/users/${target.username}/report`, {
                    method: 'POST', credentials: 'include', headers,
                    body: JSON.stringify({ reason: 'Reported from chatroom participant list' }),
                  });
                  Alert.alert('Terima kasih', 'Laporan kamu telah dikirim.');
                } catch {
                  Alert.alert('Gagal', 'Tidak dapat mengirim laporan saat ini.');
                }
              },
            },
          ],
        );
        break;
      }
    }
  }, [participantMenuTarget, room, buildHeaders, followedUsers, blockedUsers]);

  const handleSelectEmoticon = useCallback((unicode: string) => {
    const emoticon = UNICODE_TO_EMOTICON[unicode];
    const token = emoticon ? `:${emoticon.key}:` : unicode;
    setInputText(prev => {
      const next = prev + token;
      inputTextRef.current = next;
      return next;
    });
    setShowPicker(false);
  }, []);

  // ── Gift: send via WebSocket SEND_GIFT (matches /gift [recipient|all] giftName format)
  // recipient === 'all' → shower format + GIFT_BILLING billing (matches GiftAsync.java)
  // Falls back to HTTP /gift command if WS not connected
  const handleSelectGift = useCallback(async (gift: GiftItem) => {
    setShowPicker(false);
    if (!room) return;
    const recipientUser = showGiftForUser;
    const recipient     = recipientUser ? recipientUser.username : 'all';

    const ws = wsRef.current;
    if (ws && ws.readyState === 1 /* OPEN */) {
      // Send via WebSocket SEND_GIFT — gateway formats shower << ... >> and sends GIFT_BILLING to sender
      ws.send(JSON.stringify({
        type: 'SEND_GIFT',
        roomId: room.id,
        recipient,
        giftName: gift.name,
        giftEmoji: gift.emoji,
        price: gift.coins,
      }));
    } else {
      // Fallback: HTTP path via /gift command — server returns billing info in response
      try {
        const headers = await buildHeaders(true);
        const cmdText = `/gift ${recipient} ${gift.name}`;
        const res = await fetch(`${API_BASE}/api/chatrooms/${room.id}/messages`, {
          method: 'POST', credentials: 'include', headers,
          body: JSON.stringify({ text: cmdText }),
        });
        if (res.ok) {
          const body = await res.json();
          if (body.billing?.text) {
            Alert.alert('🎉 Gift Shower Berhasil!', body.billing.text);
          }
        } else {
          const body = await res.json().catch(() => ({}));
          Alert.alert('Tidak bisa kirim gift', body.message ?? `Error ${res.status}`);
        }
      } catch {
        Alert.alert('Gagal', 'Tidak dapat mengirim gift saat ini.');
      }
    }
    setShowGiftForUser(null);
  }, [room, buildHeaders, showGiftForUser]);

  // Mirrors ChatController.java sendSticker():
  // Java sends "/sticker {alias}" as text but stores/renders as StickerMimeData image.
  // Expo encodes sticker key+label into the text field so both sender and all receivers
  // can decode and render the local sticker image (via STICKER_IMAGE_MAP).
  const handleSelectSticker = useCallback((key: string, label: string) => {
    setShowPicker(false);
    sendMessage(encodeStickerText(key, label));
  }, [sendMessage]);

  const handleSelectCard = useCallback((key: string) => {
    setShowCardPicker(false);
    sendMessage(encodeCardText(key));
  }, [sendMessage]);

  if (!room) return null;

  /* ─── Pinned message banner ─── */
  const pinnedMsg = messages.find(m => m.isPinned);

  /* ─── Inner content (shared between Modal and embedded mode) ─── */
  const chatContent = (
    <>
      {/* ── Header (hidden in embedded/multi-tab mode — parent renders its own) ── */}
      {!hideHeader && (
        <View
          style={[styles.header, { paddingTop: Platform.OS === 'android' ? insets.top + 8 : 8 }]}
          onLayout={(e) => setHeaderHeight(e.nativeEvent.layout.height)}
        >
          <TouchableOpacity onPress={handleClose} style={styles.backBtn} testID="button-back-room">
            <Ionicons name="arrow-back" size={22} color={C.white} />
          </TouchableOpacity>

          <View style={styles.headerInfo}>
            <Text style={styles.headerTitle} numberOfLines={1}>{room.name}</Text>
            {room.description ? (
              <Text style={styles.headerSub} numberOfLines={1}>{room.description}</Text>
            ) : null}
          </View>

          <View style={styles.headerIcons}>
            {isMuted && (
              <Ionicons name="volume-mute" size={14} color="rgba(255,255,255,0.5)" />
            )}
            {isFavorite && (
              <Ionicons name="star" size={14} color={C.gold} />
            )}
            <TouchableOpacity
              style={[styles.iconCircle, showUsers && styles.iconCircleActive]}
              onPress={toggleUsers}
              testID="button-participants"
            >
              <Ionicons name="person-outline" size={18} color={C.white} />
            </TouchableOpacity>
            <TouchableOpacity
              style={[styles.iconCircle, showOverflow && styles.iconCircleActive]}
              onPress={() => {
                setShowUsers(false);
                setShowOverflow(v => !v);
              }}
              testID="button-room-menu"
            >
              <Ionicons name="ellipsis-vertical" size={18} color={C.white} />
            </TouchableOpacity>
          </View>
        </View>
      )}

      {/* ── Connection status bar ── */}
      {(wsStatus === 'connecting' || wsStatus === 'reconnecting') && (
        <View style={styles.connBar} testID="status-ws-reconnecting">
          <ActivityIndicator size="small" color={C.white} />
        </View>
      )}
      {wsStatus === 'disconnected' && (
        <View style={[styles.connBar, styles.connBarOff]} testID="status-ws-disconnected">
          <Ionicons name="wifi-outline" size={13} color={C.white} style={{ marginRight: 4 }} />
          <Text style={styles.connBarText}>Tidak terhubung</Text>
        </View>
      )}

      {/* ── Announcement banner — mirrors Announce.java announceOn sticky display ── */}
      {announceText && (
        <View style={styles.announceBanner}>
          <MaterialIcons name="campaign" size={15} color="#FFFFFF" style={{ marginRight: 6 }} />
          <Text style={styles.announceBannerText} numberOfLines={2}>{announceText}</Text>
          {isChatRoomAdmin && (
            <TouchableOpacity
              onPress={() => {
                const ws = wsRef.current;
                if (ws && ws.readyState === WebSocket.OPEN) {
                  ws.send(JSON.stringify({ type: 'CMD', cmd: 'announce_off', roomId: room.id }));
                }
              }}
              hitSlop={{ top: 6, bottom: 6, left: 6, right: 6 }}
              testID="button-announce-off"
            >
              <MaterialIcons name="close" size={15} color="#FFFFFF" />
            </TouchableOpacity>
          )}
        </View>
      )}

      {/* ── Pinned message banner ── */}
      {pinnedMsg && (
        <View style={styles.pinnedBanner}>
          <MaterialIcons name="push-pin" size={13} color={C.gold} />
          <Text style={styles.pinnedBannerText} numberOfLines={1}>
            {pinnedMsg.senderUsername}: {pinnedMsg.text}
          </Text>
        </View>
      )}

      {/* ── User dropdown ── */}
      {showUsers && (
        <TouchableWithoutFeedback onPress={() => setShowUsers(false)}>
          <View style={styles.dropOverlay}>
            <TouchableWithoutFeedback>
              <View style={[styles.dropPanel, { top: headerHeight }]}>
                <View style={styles.dropHeader}>
                  <Text style={styles.dropTitle}>Pengguna di room</Text>
                  <Text style={styles.dropCount}>{participantCount}/{room.maxParticipants}</Text>
                </View>
                <View style={styles.dropDivider} />
                {participants.length === 0 ? (
                  <Text style={styles.dropEmpty}>Belum ada pengguna</Text>
                ) : (
                  <ScrollView style={styles.dropList} showsVerticalScrollIndicator={false}>
                    {participants.map((p) => (
                      <ParticipantRow
                        key={p.id}
                        p={p}
                        isSelf={p.id === currentUserId}
                        onMenu={handleOpenParticipantMenu}
                      />
                    ))}
                  </ScrollView>
                )}
              </View>
            </TouchableWithoutFeedback>
          </View>
        </TouchableWithoutFeedback>
      )}

      {/* ── Overflow dropdown (3-dot menu) ── */}
      {showOverflow && room && (
        <TouchableWithoutFeedback onPress={() => setShowOverflow(false)}>
          <View style={styles.dropOverlay}>
            <TouchableWithoutFeedback>
              <View style={[styles.overflowDropPanel, { top: headerHeight }]}>
                <ScrollView showsVerticalScrollIndicator={false} bounces={false}>
                  {overflowItems.map((item, idx) => (
                    <TouchableOpacity
                      key={item.id}
                      style={[
                        styles.overflowDropItem,
                        idx < overflowItems.length - 1 && styles.overflowDropItemBorder,
                      ]}
                      onPress={() => {
                        setShowOverflow(false);
                        handleOverflowSelect(item.id);
                      }}
                      testID={`button-overflow-${item.id}`}
                    >
                      <View style={styles.overflowDropIconWrap}>{item.icon}</View>
                      <Text style={[styles.overflowDropLabel, item.isDanger && styles.overflowDropLabelDanger]}>
                        {item.label}
                      </Text>
                    </TouchableOpacity>
                  ))}
                </ScrollView>
              </View>
            </TouchableWithoutFeedback>
          </View>
        </TouchableWithoutFeedback>
      )}

      {/* ── Message list ── */}
      <KeyboardAvoidingView
        style={[styles.flex, { backgroundColor: `#${chatTheme?.background_color ?? '1A1A2E'}` }]}
        behavior={undefined}
        keyboardVerticalOffset={0}
      >
        {loading ? (
          <View style={styles.center}>
            <ActivityIndicator color={room.color} size="large" />
          </View>
        ) : (
          <FlatList
            ref={flatListRef}
            data={messages}
            keyExtractor={item => item.id}
            style={styles.msgList}
            contentContainerStyle={styles.msgContent}
            keyboardShouldPersistTaps="always"
            keyboardDismissMode="none"
            renderItem={({ item }) => (
              <MessageRow
                msg={item}
                isOwn={!!currentUserId && item.senderId === currentUserId}
                roomName={room.name}
                onLongPress={handleLongPressMessage}
                chatTheme={chatTheme}
              />
            )}
            onContentSizeChange={() => flatListRef.current?.scrollToEnd({ animated: false })}
            showsVerticalScrollIndicator={false}
            refreshControl={
              <RefreshControl
                refreshing={false}
                onRefresh={() => {}}
                enabled={false}
                testID="refresh-history"
              />
            }
          />
        )}

        <ChatroomInputBar
          inputRef={inputRef}
          inputText={inputText}
          inputTextRef={inputTextRef}
          onChangeInputText={setInputText}
          onOpenPicker={() => setShowPicker(true)}
          onSendMessage={() => sendMessage()}
        />
      </KeyboardAvoidingView>
    </>
  );

  /* ─── Overlays (menus/pickers) — used in both modes ─── */
  const overlays = (
    <>
      {/* ── UNO card picker ── */}
      {showCardPicker && (
        <TouchableWithoutFeedback onPress={() => setShowCardPicker(false)}>
          <View style={styles.cardPickerOverlay}>
            <TouchableWithoutFeedback>
              <View style={styles.cardPickerSheet}>
                <Text style={styles.cardPickerTitle}>Pilih kartu UNO</Text>
                <View style={styles.cardPickerGrid}>
                  {UNO_CARDS.map((card) => (
                    <TouchableOpacity
                      key={card.key}
                      style={[styles.cardPickerItem, { borderColor: card.color }]}
                      onPress={() => handleSelectCard(card.key)}
                      testID={`card-pick-${card.key}`}
                    >
                      <Image source={card.image as any} style={styles.cardPickerImg} resizeMode="contain" />
                    </TouchableOpacity>
                  ))}
                </View>
              </View>
            </TouchableWithoutFeedback>
          </View>
        </TouchableWithoutFeedback>
      )}

      {/* ── Context menu (long-press message) ── */}
      <BottomSheetMenu
        visible={!!contextMessage && contextMenuItems.length > 0}
        title={contextMessage?.text}
        items={contextMenuItems}
        onSelect={handleContextMenuSelect}
        onClose={() => setContextMessage(null)}
        styles={styles}
      />

      {/* ── Participant action menu (3-dot) ── */}
      <BottomSheetMenu
        visible={!!participantMenuTarget}
        title={participantMenuTarget ? (participantMenuTarget.displayName || participantMenuTarget.username) : undefined}
        items={participantMenuItems}
        onSelect={handleParticipantMenuSelect}
        onClose={() => setParticipantMenuTarget(null)}
        styles={styles}
      />

      {/* ── Room Info Sheet ── */}
      {showRoomInfo && (
        <RoomInfoSheet
          room={room}
          onClose={() => setShowRoomInfo(false)}
          buildHeaders={buildHeaders}
        />
      )}

      {/* ── View Profile Modal — mirrors MiniProfilePopupFragment.java ── */}
      {viewProfileTarget && (
        <ViewProfileModal
          visible={!!viewProfileTarget}
          username={viewProfileTarget.username}
          displayName={viewProfileTarget.displayName || viewProfileTarget.username}
          avatarColor={viewProfileTarget.color}
          currentUserId={currentUserId}
          isFollowing={followedUsers.has(viewProfileTarget.username)}
          isBlocked={blockedUsers.has(viewProfileTarget.username)}
          onClose={() => setViewProfileTarget(null)}
          onSendGift={(uname) => {
            const p = participants.find(x => x.username === uname);
            if (p) { setShowGiftForUser(p); setShowPicker(true); }
          }}
          onPrivateChat={(uname, dname) => {
            setViewProfileTarget(null);
            if (onOpenPrivateChat) onOpenPrivateChat(uname, dname);
          }}
          onFollow={(uname) => setFollowedUsers(prev => { const s = new Set(prev); s.add(uname); return s; })}
          onUnfollow={(uname) => setFollowedUsers(prev => { const s = new Set(prev); s.delete(uname); return s; })}
          onBlock={(uname) => setBlockedUsers(prev => { const s = new Set(prev); s.add(uname); return s; })}
          onUnblock={(uname) => setBlockedUsers(prev => { const s = new Set(prev); s.delete(uname); return s; })}
          onTransferCredit={(uname) => setTransferCreditTarget(uname)}
        />
      )}

      {/* ── Transfer Credits Modal ── */}
      <CreditsModal
        visible={!!transferCreditTarget}
        onClose={() => setTransferCreditTarget(null)}
        username={myUsernameRef.current}
        initialTab="transfer"
        initialToUsername={transferCreditTarget ?? undefined}
      />

      {/* ── Gift / Emoticon / Sticker Picker ── */}
      <GiftPickerModal
        visible={showPicker}
        onClose={() => { setShowPicker(false); setShowGiftForUser(null); }}
        onSelectEmoticon={handleSelectEmoticon}
        onSelectGift={handleSelectGift}
        onSelectSticker={handleSelectSticker}
        creditAmount={creditAmount}
        currency={creditCurrency}
        recipientName={showGiftForUser ? (showGiftForUser.displayName || showGiftForUser.username) : undefined}
      />
    </>
  );

  /* ─── Embedded mode: render as plain View inside a parent container ─── */
  if (isEmbedded) {
    return (
      <View style={{ flex: 1, backgroundColor: `#${chatTheme?.background_color ?? '1A1A2E'}` }}>
        {chatContent}
        {overlays}
      </View>
    );
  }

  /* ─── Standard mode: full-screen Modal ─── */
  return (
    <Modal
      visible={visible}
      animationType="slide"
      statusBarTranslucent
      hardwareAccelerated
      onRequestClose={handleClose}
    >
      <SafeAreaView style={styles.root}>
        {chatContent}
      </SafeAreaView>
      {overlays}
    </Modal>
  );
});

export default RoomChatModal;

function makeStyles(C: Palette) {
  return StyleSheet.create({
  root: { flex: 1, backgroundColor: C.headerBg },
  flex: { flex: 1, backgroundColor: C.msgBg },

  /* ── Header ── */
  header: {
    backgroundColor: C.headerBg,
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 10,
    paddingBottom: 10,
  },
  backBtn:    { padding: 6, marginRight: 6 },
  headerInfo: { flex: 1 },
  headerTitle:{ color: C.white, fontSize: 17, fontFamily: 'Roboto_700Bold' },
  headerSub:  { color: 'rgba(255,255,255,0.65)', fontSize: 11, marginTop: 1, fontFamily: 'Roboto_400Regular' },
  headerIcons:      { flexDirection: 'row', alignItems: 'center', gap: 6, marginLeft: 8 },
  iconCircle:       {
    width: 36, height: 36, borderRadius: 18,
    backgroundColor: C.iconCircle,
    alignItems: 'center', justifyContent: 'center',
  },
  iconCircleActive: { backgroundColor: 'rgba(255,255,255,0.30)' },

  /* ── Connection status bar ── */
  connBar: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: 'rgba(0,0,0,0.45)',
    paddingVertical: 4,
    paddingHorizontal: 12,
  },
  connBarOff: { backgroundColor: 'rgba(180,30,30,0.7)' },
  connBarText: { color: C.white, fontSize: 11, fontWeight: '500' },

  /* ── Pinned banner ── */
  pinnedBanner: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: C.pinned,
    borderBottomWidth: 1,
    borderBottomColor: C.pinnedBorder,
    paddingHorizontal: 12,
    paddingVertical: 6,
    gap: 6,
  },
  pinnedBannerText: {
    flex: 1,
    fontSize: 12,
    color: C.text,
    fontFamily: 'Roboto_400Regular',
  },

  /* ── Announcement banner — mirrors Announce.java announceOn sticky display ── */
  announceBanner: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#1565C0',
    paddingHorizontal: 12,
    paddingVertical: 7,
    gap: 4,
  },
  announceBannerText: {
    flex: 1,
    fontSize: 12,
    color: '#FFFFFF',
    fontFamily: 'Roboto_400Regular',
    lineHeight: 17,
  },

  /* ── User dropdown ── */
  dropOverlay: { position: 'absolute', top: 0, left: 0, right: 0, bottom: 0, zIndex: 10 },
  dropPanel: {
    position: 'absolute', right: 0,
    width: 270, maxHeight: 320,
    backgroundColor: C.dropBg,
    borderBottomLeftRadius: 10,
    shadowColor: '#000', shadowOpacity: 0.18, shadowRadius: 8,
    shadowOffset: { width: -2, height: 4 }, elevation: 8,
  },
  dropHeader: {
    flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between',
    paddingHorizontal: 14, paddingVertical: 10,
  },
  dropTitle:  { fontSize: 13, fontFamily: 'Roboto_700Bold', color: C.text },
  dropCount:  { fontSize: 12, color: C.ts, fontFamily: 'Roboto_400Regular' },
  dropDivider:{ height: 1, backgroundColor: C.dropBorder },
  dropList:   { maxHeight: 260 },
  dropEmpty:  { padding: 16, color: C.ts, fontSize: 13, textAlign: 'center', fontFamily: 'Roboto_400Regular' },

  /* ── Overflow dropdown (3-dot menu) ── */
  overflowDropPanel: {
    position: 'absolute', right: 0,
    width: 230, maxHeight: 460,
    backgroundColor: C.dropBg,
    borderBottomLeftRadius: 12,
    shadowColor: '#000', shadowOpacity: 0.18, shadowRadius: 10,
    shadowOffset: { width: -2, height: 4 }, elevation: 10,
  },
  overflowDropItem: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 16,
    paddingVertical: 13,
  },
  overflowDropItemBorder: {
    borderBottomWidth: StyleSheet.hairlineWidth,
    borderBottomColor: C.divider,
  },
  overflowDropIconWrap: {
    width: 28,
    alignItems: 'center',
  },
  overflowDropLabel: {
    fontSize: 14,
    color: C.text,
    fontFamily: 'Roboto_400Regular',
    marginLeft: 12,
    flex: 1,
  },
  overflowDropLabelDanger: {
    color: '#FF4444',
  },
  pRow: {
    flexDirection: 'row', alignItems: 'center',
    paddingHorizontal: 14, paddingVertical: 9,
    borderBottomWidth: 1, borderBottomColor: C.dropBorder,
  },
  pAvatar: {
    width: 32, height: 32, borderRadius: 16,
    alignItems: 'center', justifyContent: 'center', marginRight: 10,
  },
  pAvatarText: { color: C.white, fontFamily: 'Roboto_700Bold', fontSize: 13 },
  pName:       { fontSize: 13, color: C.text, fontFamily: 'Roboto_400Regular' },
  pRole:       { fontSize: 10, color: C.gold, fontFamily: 'Roboto_700Bold', marginTop: 1 },
  pMenuBtn:    { padding: 6, marginLeft: 4 },

  /* ── Messages ── */
  center:     { flex: 1, alignItems: 'center', justifyContent: 'center' },
  msgList:    { flex: 1 },
  msgContent: { paddingHorizontal: 12, paddingTop: 6, paddingBottom: 4 },
  msgRow:     { paddingVertical: 1, paddingHorizontal: 4, borderRadius: 4 },
  msgRowPinned: {
    backgroundColor: C.pinned,
    borderLeftWidth: 2,
    borderLeftColor: C.pinnedBorder,
    paddingLeft: 8,
    marginVertical: 1,
  },
  msgRowFailed: {
    backgroundColor: C.failedBg,
    borderLeftWidth: 2,
    borderLeftColor: C.failedBorder,
    paddingLeft: 8,
    marginVertical: 1,
  },
  msgLine:       { flexDirection: 'row', flexWrap: 'wrap', alignItems: 'center' },
  inlineEmote:   { width: 18, height: 18, marginHorizontal: 1 },
  msgSenderName: { fontFamily: 'Roboto_700Bold' },
  msgBody:       { color: C.text, fontFamily: 'Roboto_400Regular' },
  failedTag:     { color: C.danger, fontSize: 11, fontFamily: 'Roboto_400Regular' },
  pinnedTag:     { fontSize: 12 },

  /* ── Gift/Emote message — warna dari Android R.color.chat_emote_text (#46AAAF) ── */
  giftMsgRow: {
    backgroundColor: C.emoteTextBg,
    borderLeftWidth: 3,
    borderLeftColor: C.emoteTextBorder,
    borderRadius: 6,
    paddingHorizontal: 10,
    paddingVertical: 8,
    marginVertical: 3,
    marginHorizontal: 2,
  },
  giftMsgIcon: {
    fontSize: 20,
  },
  giftMsgInlineRow: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    alignItems: 'center',
  },
  giftMsgInlineImg: {
    width: 52,
    height: 52,
    marginHorizontal: 4,
  },
  giftMsgText: {
    color: C.emoteText,
    fontFamily: 'Roboto_700Bold',
    fontSize: 13,
    lineHeight: 22,
    flexShrink: 1,
  },

  /* ── Sticker message — mirrors StickerMimeData rendering in ChatController.java ── */
  stickerMsgWrap: {
    alignItems: 'flex-start',
    marginTop: 4,
  },
  stickerMsgImg: {
    width: 160,
    height: 96,
    borderRadius: 8,
    backgroundColor: '#f5f5f5',
  },
  stickerMsgLabel: {
    fontSize: 10,
    color: C.ts,
    marginTop: 2,
  },

  /* ── Bottom-sheet menu (shared: overflow + context) ── */
  menuOverlay: {
    position: 'absolute', top: 0, left: 0, right: 0, bottom: 0,
    backgroundColor: 'rgba(0,0,0,0.45)',
    justifyContent: 'flex-end',
    zIndex: 100,
  },
  menuSheet: {
    backgroundColor: C.menuBg,
    borderTopLeftRadius: 14,
    borderTopRightRadius: 14,
    paddingBottom: 16,
    paddingTop: 4,
  },
  menuTitleRow: {
    paddingHorizontal: 20,
    paddingVertical: 12,
    borderBottomWidth: StyleSheet.hairlineWidth,
    borderBottomColor: C.divider,
    marginBottom: 4,
  },
  menuTitle: {
    fontSize: 12,
    color: C.ts,
    fontFamily: 'Roboto_400Regular',
  },
  menuItem: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 20,
    paddingVertical: 15,
  },
  menuItemBorder: {
    borderBottomWidth: StyleSheet.hairlineWidth,
    borderBottomColor: C.divider,
  },
  menuIconWrap: {
    width: 32,
    alignItems: 'center',
  },
  menuLabel: {
    fontSize: 15,
    color: C.menuItem,
    fontFamily: 'Roboto_400Regular',
    marginLeft: 14,
  },
  menuLabelDanger: {
    color: C.danger,
  },

  /* ── Room Info Sheet (bottom sheet) ── */
  infoOverlay: {
    position: 'absolute', top: 0, left: 0, right: 0, bottom: 0,
    backgroundColor: 'rgba(0,0,0,0.5)',
    justifyContent: 'flex-end',
    zIndex: 100,
  },
  infoSheet: {
    backgroundColor: '#FAFAFA',
    minHeight: '65%',
    maxHeight: '90%',
    overflow: 'hidden',
  },
  infoHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 16,
    paddingVertical: 14,
    gap: 10,
    backgroundColor: C.headerBg,
  },
  infoHeaderTitle: {
    fontSize: 17,
    fontFamily: 'Roboto_700Bold',
    color: '#FFFFFF',
    marginBottom: 2,
  },
  infoHeaderSub: {
    fontSize: 12,
    fontFamily: 'Roboto_400Regular',
    color: 'rgba(255,255,255,0.8)',
  },
  infoScrollView: {},
  infoSection: {
    paddingHorizontal: 16,
    paddingVertical: 12,
    borderBottomWidth: 1,
    borderBottomColor: 'rgba(0,0,0,0.07)',
  },
  infoSectionTitle: {
    fontSize: 11,
    fontFamily: 'Roboto_700Bold',
    color: C.ts,
    textTransform: 'uppercase',
    letterSpacing: 0.6,
    marginBottom: 8,
  },
  infoDesc: {
    fontSize: 14,
    fontFamily: 'Roboto_400Regular',
    color: C.text,
    lineHeight: 20,
  },
  infoUserRow: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: 8,
    gap: 10,
    borderBottomWidth: StyleSheet.hairlineWidth,
    borderBottomColor: 'rgba(0,0,0,0.06)',
  },
  infoUserAvatar: {
    width: 34,
    height: 34,
    borderRadius: 17,
    alignItems: 'center',
    justifyContent: 'center',
  },
  infoUserAvatarText: {
    color: '#FFFFFF',
    fontFamily: 'Roboto_700Bold',
    fontSize: 14,
  },
  infoUserName: {
    flex: 1,
    fontSize: 14,
    fontFamily: 'Roboto_400Regular',
    color: C.text,
  },
  infoModBadge: {
    backgroundColor: C.headerBg,
    paddingHorizontal: 7,
    paddingVertical: 2,
    borderRadius: 4,
  },
  infoModBadgeText: {
    color: '#FFFFFF',
    fontSize: 10,
    fontFamily: 'Roboto_700Bold',
  },
  infoEmptyText: {
    fontSize: 13,
    color: C.ts,
    fontFamily: 'Roboto_400Regular',
    fontStyle: 'italic',
  },

  /* ── UNO card — message bubble ── */
  cardMsgWrap: {
    marginTop: 4,
    borderWidth: 2,
    borderRadius: 10,
    padding: 4,
    backgroundColor: '#FFFFFF',
    alignSelf: 'flex-start',
  },
  cardMsgImg: {
    width: 80,
    height: 110,
  },

  /* ── UNO card — input bar button (inside emojiBtn, same width as inner icons) ── */
  cardBtnEmoji: {
    fontSize: 17,
    lineHeight: 22,
  },

  /* ── UNO card picker overlay ── */
  cardPickerOverlay: {
    position: 'absolute', top: 0, left: 0, right: 0, bottom: 0,
    backgroundColor: 'rgba(0,0,0,0.45)',
    justifyContent: 'flex-end',
    zIndex: 110,
  },
  cardPickerSheet: {
    backgroundColor: C.dropBg,
    borderTopLeftRadius: 18,
    borderTopRightRadius: 18,
    paddingTop: 16,
    paddingBottom: 24,
    paddingHorizontal: 16,
  },
  cardPickerTitle: {
    fontSize: 15,
    fontFamily: 'Roboto_700Bold',
    color: C.text,
    marginBottom: 14,
    textAlign: 'center',
  },
  cardPickerGrid: {
    flexDirection: 'row',
    justifyContent: 'space-around',
  },
  cardPickerItem: {
    borderWidth: 3,
    borderRadius: 10,
    padding: 4,
    backgroundColor: '#FAFAFA',
  },
  cardPickerImg: {
    width: 64,
    height: 88,
  },
  });
}
