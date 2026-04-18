import { useCallback, useEffect, useState } from 'react';
import {
  ActionSheetIOS,
  ActivityIndicator,
  Alert,
  FlatList,
  Image,
  Platform,
  RefreshControl,
  StyleSheet,
  Text,
  TouchableOpacity,
  View,
} from 'react-native';
import MultiRoomChatModal, { type PrivateChat } from '../../components/MultiRoomChatModal';
import { API_BASE, getMe, buildHeaders } from '../../services/auth';
import { useAppTheme } from '../../services/themeContext';

// ─── Colors ───────────────────────────────────────────────────────────────────
const C = {
  green: '#64B9A0',
  white: '#FFFFFF',
  text: '#424242',
  ts: '#999999',
  sep: '#F5F5F5',
  unreadBg: '#F0FAF7',
  badgeBg: '#64B9A0',
  groupBadge: '#9C27B0',
  passivated: '#BDBDBD',
  passivatedBg: '#FAFAFA',
  versionChip: '#E8F5E9',
};

// ─── Types ────────────────────────────────────────────────────────────────────
interface ConversationMember {
  userId: string;
  username: string;
  displayName: string | null;
  displayGUID: string | null;
}

interface Conversation {
  id: string;
  type: 'private' | 'group';
  name: string;
  avatarInitial: string;
  avatarColor: string;
  displayGUID: string | null;
  groupOwner: string | null;
  lastMessageText: string | null;
  lastMessageType: string;
  lastMessageAt: string | null;
  unreadCount: number;
  isClosed: boolean;
  isPassivated: boolean;
  members: ConversationMember[];
}

// Room the user is currently active in
interface ActiveRoom {
  room: {
    id: string;
    name: string;
    description: string | null;
    color: string;
    currentParticipants: number;
    maxParticipants: number;
  };
  participantCount: number;
  totalMessages: number;
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

function formatTimestamp(iso: string | null): string {
  if (!iso) return '';
  const d = new Date(iso);
  const now = new Date();
  const diffDays = Math.floor((now.getTime() - d.getTime()) / 86400000);
  if (diffDays === 0) return d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  if (diffDays === 1) return 'Yesterday';
  if (diffDays < 7) return d.toLocaleDateString([], { weekday: 'short' });
  return d.toLocaleDateString([], { day: '2-digit', month: 'short' });
}

// Mirrors fusion ContentTypeEnum: TEXT=1, IMAGE=2, EMOTE/STICKER=6
function messageTypeLabel(type: string, text: string | null): string {
  switch (type) {
    case 'image':   return '📷 Photo';
    case 'sticker': return '✨ Sticker';
    case 'system':  return text ?? 'System message';
    default:        return text ?? 'Tap to start chatting';
  }
}

function messageTypeIcon(type: string): string | null {
  switch (type) {
    case 'image':   return '📷';
    case 'sticker': return '✨';
    default:        return null;
  }
}

// ─── Avatar ───────────────────────────────────────────────────────────────────
function ConvAvatar({
  displayGUID, avatarColor, avatarInitial, isGroup, isPassivated,
}: {
  displayGUID: string | null;
  avatarColor: string;
  avatarInitial: string;
  isGroup: boolean;
  isPassivated: boolean;
}) {
  const bg = isPassivated ? C.passivated : avatarColor;
  return (
    <View style={[styles.convAvatar, { backgroundColor: bg }]}>
      {displayGUID ? (
        <Image source={{ uri: displayGUID }} style={styles.convAvatarImg} resizeMode="cover" />
      ) : (
        <Text style={styles.convAvatarText}>{avatarInitial}</Text>
      )}
      {isGroup && (
        <View style={styles.groupBadge}>
          <Text style={styles.groupBadgeText}>G</Text>
        </View>
      )}
    </View>
  );
}

// ─── Conversation Row ─────────────────────────────────────────────────────────
function ConversationItem({
  item,
  onPress,
  onLongPress,
}: {
  item: Conversation;
  onPress: (item: Conversation) => void;
  onLongPress: (item: Conversation) => void;
}) {
  const theme = useAppTheme();
  const isUnread = item.unreadCount > 0;
  const isGroup = item.type === 'group';
  const memberCount = item.members.length;
  const titleSuffix = isGroup ? ` (${memberCount})` : '';
  const previewText = messageTypeLabel(item.lastMessageType, item.lastMessageText);
  const typeIcon = messageTypeIcon(item.lastMessageType);

  return (
    <TouchableOpacity
      activeOpacity={0.7}
      onPress={() => onPress(item)}
      onLongPress={() => onLongPress(item)}
      testID={`card-conversation-${item.id}`}
    >
      <View style={[
        styles.convItem,
        { backgroundColor: theme.cardBg },
        isUnread && { backgroundColor: theme.accentSoft },
        item.isPassivated && styles.convItemPassivated,
      ]}>
        <ConvAvatar
          displayGUID={item.displayGUID}
          avatarColor={item.avatarColor}
          avatarInitial={item.avatarInitial}
          isGroup={isGroup}
          isPassivated={item.isPassivated}
        />
        <View style={styles.convBody}>
          {/* Row 1: name + timestamp */}
          <View style={styles.convRow}>
            <Text
              style={[
                styles.convName,
                { color: theme.textPrimary },
                isUnread && styles.convNameUnread,
                item.isPassivated && styles.convNamePassivated,
              ]}
              numberOfLines={1}
            >
              {item.name}{titleSuffix}
            </Text>
            <Text style={[styles.convTs, { color: theme.textSecondary }, item.isPassivated && styles.tsPassivated]}>
              {formatTimestamp(item.lastMessageAt)}
            </Text>
          </View>

          {/* Row 2: last message + unread badge */}
          <View style={styles.convRow}>
            <View style={styles.previewRow}>
              {typeIcon ? <Text style={styles.typeIconText}>{typeIcon} </Text> : null}
              <Text
                style={[styles.convPreview, { color: theme.textSecondary }, item.isPassivated && styles.previewPassivated]}
                numberOfLines={1}
              >
                {previewText}
              </Text>
            </View>
            {isUnread ? (
              <View style={styles.unreadBadge}>
                <Text style={styles.unreadBadgeText}>
                  {item.unreadCount > 99 ? '99+' : item.unreadCount}
                </Text>
              </View>
            ) : null}
          </View>

          {/* Row 3: groupOwner / passivated chip */}
          {(isGroup && item.groupOwner) || item.isPassivated ? (
            <View style={styles.metaRow}>
              {isGroup && item.groupOwner ? (
                <Text style={styles.metaText}>Owner: {item.groupOwner}</Text>
              ) : null}
              {item.isPassivated ? (
                <View style={styles.passivatedChip}>
                  <Text style={styles.passivatedChipText}>Inactive</Text>
                </View>
              ) : null}
            </View>
          ) : null}
        </View>
      </View>
    </TouchableOpacity>
  );
}

// ─── Active Room Row (My Rooms tab) ──────────────────────────────────────────
// Mirrors Android ChatManagerFragment tab CHATROOM_LIST — shows rooms user is inside
function ActiveRoomItem({ item, onPress }: { item: ActiveRoom; onPress: (item: ActiveRoom) => void }) {
  const theme = useAppTheme();
  const { room, participantCount, totalMessages } = item;
  const initials = room.name.slice(0, 2).toUpperCase();
  const fill = Math.min((room.currentParticipants / room.maxParticipants) * 100, 100);
  return (
    <TouchableOpacity
      style={[styles.item, { backgroundColor: theme.cardBg }]}
      activeOpacity={0.75}
      testID={`card-myroom-${room.id}`}
      onPress={() => onPress(item)}
    >
      <View style={[styles.roomAvatar, { backgroundColor: room.color || C.green }]}>
        <Text style={styles.roomAvatarText}>{initials}</Text>
      </View>
      <View style={styles.itemBody}>
        <View style={styles.itemRow}>
          <Text style={[styles.roomName, { color: theme.accent }]} numberOfLines={1}>{room.name}</Text>
          <View style={styles.roomBadgeGroup}>
            <View style={styles.badge}>
              <Image
                source={require('../../assets/icons/ad_userppl_grey.png')}
                style={styles.badgeIcon}
                resizeMode="contain"
              />
              <Text style={styles.badgeText}>{participantCount}</Text>
            </View>
            {totalMessages > 0 && (
              <View style={[styles.badge, styles.chatBadge]}>
                <Image
                  source={require('../../assets/icons/ad_chatlarge_grey.png')}
                  style={[styles.badgeIcon, { tintColor: theme.accent }]}
                  resizeMode="contain"
                />
                <Text style={[styles.badgeText, { color: theme.accent }]}>{totalMessages > 999 ? '999+' : totalMessages}</Text>
              </View>
            )}
          </View>
        </View>
        {room.description ? (
          <Text style={[styles.roomDesc, { color: theme.textSecondary }]} numberOfLines={1}>{room.description}</Text>
        ) : null}
        <View style={styles.progressTrack}>
          <View style={[styles.progressFill, {
            width: `${fill}%` as `${number}%`,
            backgroundColor: room.color || C.green,
          }]} />
        </View>
      </View>
    </TouchableOpacity>
  );
}

const MAX_OPEN_TABS = 5;

// ─── Main Screen ──────────────────────────────────────────────────────────────
export default function ChatScreen() {
  const theme = useAppTheme();
  const [activeTab, setActiveTab] = useState<'chats' | 'myrooms'>('chats');

  // Conversations (private + group chats)
  const [conversations, setConversations] = useState<Conversation[]>([]);
  const [chatListVersion, setChatListVersion] = useState<number>(0);
  const [convsLoading, setConvsLoading] = useState(true);
  const [convsRefreshing, setConvsRefreshing] = useState(false);

  // My Rooms (chatrooms user is currently in)
  const [myRooms, setMyRooms] = useState<ActiveRoom[]>([]);
  const [roomsLoading, setRoomsLoading] = useState(true);
  const [roomsRefreshing, setRoomsRefreshing] = useState(false);

  // Open rooms & private chat modal state
  const [openRooms, setOpenRooms] = useState<ActiveRoom['room'][]>([]);
  const [openPrivateChats, setOpenPrivateChats] = useState<PrivateChat[]>([]);
  const [activeTabId, setActiveTabId] = useState<string | null>(null);
  const [modalVisible, setModalVisible] = useState(false);
  const [currentUserId, setCurrentUserId] = useState<string | null>(null);

  useEffect(() => {
    getMe().then(me => { if (me) setCurrentUserId(me.id); });
  }, []);

  const loadConversations = useCallback(async (refresh = false) => {
    if (refresh) setConvsRefreshing(true);
    try {
      const headers = await buildHeaders();
      const res = await fetch(`${API_BASE}/api/chatsync/conversations`, { credentials: 'include', headers });
      const data = await res.json();
      setConversations(data.conversations ?? []);
      if (typeof data.chatListVersion === 'number') setChatListVersion(data.chatListVersion);
    } catch {
      setConversations([]);
    } finally {
      setConvsLoading(false);
      setConvsRefreshing(false);
    }
  }, []);

  const loadMyRooms = useCallback(async (refresh = false) => {
    if (refresh) setRoomsRefreshing(true);
    try {
      const headers = await buildHeaders();
      const res = await fetch(`${API_BASE}/api/chatrooms/my`, { credentials: 'include', headers });
      const data = await res.json();
      setMyRooms(data.myRooms ?? []);
    } catch {
      setMyRooms([]);
    } finally {
      setRoomsLoading(false);
      setRoomsRefreshing(false);
    }
  }, []);

  useEffect(() => {
    loadConversations();
    loadMyRooms();
  }, [loadConversations, loadMyRooms]);

  // Chat list version polling — mirrors FusionPktChatListVersion.
  // Poll /api/chatsync/version every 30 s; if version increased, refresh inbox.
  // Mirrors RedisChatSyncStore CLV (chatlist:ver:[userId]) incrementing on any
  // chat-list change (new message, group created, member left, conv closed).
  useEffect(() => {
    let active = true;
    const poll = async () => {
      try {
        const headers = await buildHeaders();
        const res = await fetch(`${API_BASE}/api/chatsync/version`, { credentials: 'include', headers });
        if (!res.ok || !active) return;
        const data = await res.json();
        const serverVersion: number = data.chatListVersion ?? 0;
        setChatListVersion(prev => {
          if (serverVersion > prev) {
            loadConversations();
            return serverVersion;
          }
          return prev;
        });
      } catch {}
    };
    const timer = setInterval(poll, 30_000);
    return () => { active = false; clearInterval(timer); };
  }, [loadConversations]);

  const handleLongPress = useCallback((conv: Conversation) => {
    const options = ['Close Chat', 'Cancel'];
    if (Platform.OS === 'ios') {
      ActionSheetIOS.showActionSheetWithOptions(
        { options, destructiveButtonIndex: 0, cancelButtonIndex: 1, title: conv.name },
        async (i) => { if (i === 0) closeConversation(conv.id); },
      );
    } else {
      Alert.alert(conv.name, 'Choose an action', [
        { text: 'Close Chat', style: 'destructive', onPress: () => closeConversation(conv.id) },
        { text: 'Cancel', style: 'cancel' },
      ]);
    }
  }, []);

  const closeConversation = useCallback(async (id: string) => {
    try {
      const headers = await buildHeaders();
      await fetch(`${API_BASE}/api/chatsync/conversations/${id}`, {
        method: 'DELETE',
        credentials: 'include',
        headers,
      });
      loadConversations();
    } catch {
      Alert.alert('Error', 'Could not close conversation.');
    }
  }, [loadConversations]);

  const handleConversationPress = useCallback((conv: Conversation) => {
    const peer = conv.members.find(m => m.userId !== currentUserId) ?? conv.members[0];
    const chatEntry: PrivateChat = {
      id: conv.id,
      peerUsername: peer?.username ?? conv.name,
      peerDisplayName: peer?.displayName ?? conv.name,
      color: conv.avatarColor,
    };
    setOpenPrivateChats(prev => {
      const exists = prev.find(c => c.id === conv.id);
      if (exists) return prev;
      return prev.length >= MAX_OPEN_TABS ? [...prev.slice(1), chatEntry] : [...prev, chatEntry];
    });
    // Langsung hapus dari list saat dibuka
    setConversations(prev => prev.filter(c => c.id !== conv.id));
    setActiveTabId(conv.id);
    setModalVisible(true);
  }, [currentUserId]);

  const handleRemovePrivateChat = useCallback((chatId: string) => {
    setOpenPrivateChats(prev => {
      const next = prev.filter(c => c.id !== chatId);
      if (next.length === 0) {
        setActiveTabId(null);
        setModalVisible(false);
      } else if (activeTabId === chatId) {
        setActiveTabId(next[next.length - 1]?.id ?? null);
      }
      return next;
    });
    // Langsung hapus dari list saat leave/close chat
    setConversations(prev => prev.filter(c => c.id !== chatId));
    closeConversation(chatId);
  }, [activeTabId, closeConversation]);

  const handleRoomPress = useCallback((item: ActiveRoom) => {
    const r = item.room;
    setOpenRooms(prev => {
      const exists = prev.find(x => x.id === r.id);
      if (exists) return prev;
      return prev.length >= MAX_OPEN_TABS ? [...prev.slice(1), r] : [...prev, r];
    });
    setActiveTabId(r.id);
    setModalVisible(true);
  }, []);

  const handleRemoveRoom = useCallback((roomId: string) => {
    setOpenRooms(prev => {
      const next = prev.filter(r => r.id !== roomId);
      if (next.length === 0 && openPrivateChats.length === 0) {
        setActiveTabId(null);
        setModalVisible(false);
      } else if (activeTabId === roomId) {
        const allIds = [...openRooms.filter(r => r.id !== roomId).map(r => r.id), ...openPrivateChats.map(c => c.id)];
        setActiveTabId(allIds[allIds.length - 1] ?? null);
      }
      return next;
    });
  }, [activeTabId, openRooms, openPrivateChats]);

  const handleOpenPrivateChat = useCallback(async (username: string, displayName: string) => {
    try {
      const headers = await buildHeaders({ 'Content-Type': 'application/json' });
      const res = await fetch(`${API_BASE}/api/chatsync/conversations/private`, {
        method: 'POST',
        credentials: 'include',
        headers,
        body: JSON.stringify({ targetUsername: username }),
      });
      if (!res.ok) return;
      const { conversation: conv } = await res.json();
      const chatEntry: PrivateChat = {
        id: conv.id,
        peerUsername: username,
        peerDisplayName: displayName || username,
        color: conv.avatarColor ?? '#4CAF50',
      };
      setOpenPrivateChats(prev => {
        const exists = prev.find(c => c.id === conv.id);
        if (exists) return prev;
        return prev.length >= MAX_OPEN_TABS ? [...prev.slice(1), chatEntry] : [...prev, chatEntry];
      });
      setActiveTabId(conv.id);
      setModalVisible(true);
    } catch {
      Alert.alert('Error', 'Tidak dapat membuka private chat saat ini.');
    }
  }, []);

  return (
    <View style={[styles.container, { backgroundColor: theme.screenBg }]}>
      {/* Tab bar — Chats + My Rooms */}
      <View style={[styles.tabRow, { backgroundColor: theme.tabBg, borderBottomColor: theme.tabBorder }]}>
        {([
          ['chats',   'Chats',    require('../../assets/icons/ad_chatlarge_grey.png')],
          ['myrooms', 'My Rooms', require('../../assets/icons/ad_chatroom_grey.png')],
        ] as const).map(([t, label, icon]) => (
          <TouchableOpacity
            key={t}
            style={[styles.tab, activeTab === t && { borderBottomColor: theme.tabActiveColor }]}
            onPress={() => setActiveTab(t)}
            testID={`tab-${t}`}
          >
            <Image
              source={icon}
              style={[styles.tabIcon, { tintColor: activeTab === t ? theme.tabActiveColor : theme.tabInactiveColor }]}
              resizeMode="contain"
            />
            <Text style={[styles.tabLabel, { color: activeTab === t ? theme.tabActiveColor : theme.tabInactiveColor }]}>{label}</Text>
          </TouchableOpacity>
        ))}
      </View>

      {/* Chat list version bar — mirrors fusion FusionPktChatListVersion */}
      {activeTab === 'chats' && chatListVersion > 0 && (
        <View style={styles.versionBar}>
          <Text style={styles.versionText}>Chat list v{chatListVersion}</Text>
        </View>
      )}

      {/* ── Chats Tab ── */}
      {activeTab === 'chats' && (
        convsLoading ? (
          <View style={styles.center}><ActivityIndicator color={theme.accent} size="large" /></View>
        ) : conversations.length === 0 ? (
          <View style={styles.empty}>
            <Image source={require('../../assets/icons/ad_chatlarge_grey.png')} style={[styles.emptyIcon, { tintColor: theme.textSecondary }]} resizeMode="contain" />
            <Text style={[styles.emptyTitle, { color: theme.textPrimary }]}>No chats yet</Text>
            <Text style={[styles.emptySub, { color: theme.textSecondary }]}>Start a private chat from someone's profile or the People tab.</Text>
          </View>
        ) : (
          <FlatList
            data={conversations}
            keyExtractor={(item) => item.id}
            renderItem={({ item }) => (
              <ConversationItem item={item} onPress={handleConversationPress} onLongPress={handleLongPress} />
            )}
            refreshControl={
              <RefreshControl refreshing={convsRefreshing} onRefresh={() => loadConversations(true)} tintColor={theme.accent} />
            }
            ItemSeparatorComponent={() => <View style={[styles.divider, { backgroundColor: theme.divider }]} />}
          />
        )
      )}

      {/* ── My Rooms Tab — rooms where user is currently inside ── */}
      {activeTab === 'myrooms' && (
        roomsLoading ? (
          <View style={styles.center}><ActivityIndicator color={theme.accent} size="large" /></View>
        ) : myRooms.length === 0 ? (
          <View style={styles.empty}>
            <Image source={require('../../assets/icons/ad_chatroom_grey.png')} style={[styles.emptyIcon, { tintColor: theme.textSecondary }]} resizeMode="contain" />
            <Text style={[styles.emptyTitle, { color: theme.textPrimary }]}>Not in any room</Text>
            <Text style={[styles.emptySub, { color: theme.textSecondary }]}>Rooms you join will appear here. Browse rooms from the Room List tab.</Text>
          </View>
        ) : (
          <FlatList
            data={myRooms}
            keyExtractor={(item) => item.room.id}
            renderItem={({ item }) => <ActiveRoomItem item={item} onPress={handleRoomPress} />}
            refreshControl={
              <RefreshControl refreshing={roomsRefreshing} onRefresh={() => loadMyRooms(true)} tintColor={theme.accent} />
            }
            ItemSeparatorComponent={() => <View style={[styles.divider, { backgroundColor: theme.divider }]} />}
          />
        )
      )}

      {/* Resume pill — shown when modal is minimised */}
      {(openPrivateChats.length > 0 || openRooms.length > 0) && !modalVisible && (
        <TouchableOpacity
          style={[styles.resumePill, { backgroundColor: theme.accent }]}
          onPress={() => setModalVisible(true)}
          activeOpacity={0.85}
          testID="button-resume-chat"
        >
          <Text style={styles.resumePillText}>
            💬  Resume ({openRooms.length + openPrivateChats.length} {openRooms.length + openPrivateChats.length === 1 ? 'tab' : 'tabs'})
          </Text>
        </TouchableOpacity>
      )}

      {/* Multi-tab private chat modal */}
      {(openPrivateChats.length > 0 || openRooms.length > 0) && (
        <MultiRoomChatModal
          visible={modalVisible}
          openRooms={openRooms}
          openPrivateChats={openPrivateChats}
          activeTabId={activeTabId}
          currentUserId={currentUserId}
          onMinimize={() => setModalVisible(false)}
          onRemoveRoom={handleRemoveRoom}
          onRemovePrivateChat={handleRemovePrivateChat}
          onChangeActiveTab={setActiveTabId}
          onOpenPrivateChat={handleOpenPrivateChat}
          onIncomingPrivateChat={(convId, peerUsername, peerDisplayName) => {
            const chatEntry: PrivateChat = {
              id: convId,
              peerUsername,
              peerDisplayName,
              color: '#4CAF50',
            };
            setOpenPrivateChats(prev => {
              const exists = prev.find(c => c.id === convId);
              if (exists) return prev;
              return prev.length >= MAX_OPEN_TABS ? [...prev.slice(1), chatEntry] : [...prev, chatEntry];
            });
            setActiveTabId(convId);
            setModalVisible(true);
          }}
        />
      )}
    </View>
  );
}

// ─── Styles ───────────────────────────────────────────────────────────────────
const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: C.white },

  tabRow: { flexDirection: 'row', borderBottomWidth: 1, borderBottomColor: '#EEE3D2', backgroundColor: C.white },
  tab: { flex: 1, flexDirection: 'row', alignItems: 'center', justifyContent: 'center', paddingVertical: 12, gap: 6 },
  tabActive: { borderBottomWidth: 2, borderBottomColor: C.green },
  tabIcon: { width: 18, height: 18 },
  tabLabel: { color: C.ts, fontSize: 13, fontWeight: '500' },
  tabLabelActive: { color: C.green },

  versionBar: {
    flexDirection: 'row',
    justifyContent: 'flex-end',
    paddingHorizontal: 14,
    paddingVertical: 4,
    backgroundColor: C.versionChip,
    borderBottomWidth: 1,
    borderBottomColor: '#C8E6C9',
  },
  versionText: { color: '#388E3C', fontSize: 10, fontWeight: '600' },

  center: { flex: 1, alignItems: 'center', justifyContent: 'center' },

  // ── Conversation item ────────────────────────────────────────────────────────
  convItem: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 16,
    paddingVertical: 10,
    backgroundColor: C.white,
  },
  convItemUnread: { backgroundColor: C.unreadBg },
  convItemPassivated: { backgroundColor: C.passivatedBg },

  convAvatar: {
    width: 48,
    height: 48,
    borderRadius: 24,
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 12,
    overflow: 'hidden',
  },
  convAvatarImg: { width: 48, height: 48, borderRadius: 24 },
  convAvatarText: { color: C.white, fontWeight: 'bold', fontSize: 18 },

  groupBadge: {
    position: 'absolute',
    bottom: 0,
    right: 0,
    width: 16,
    height: 16,
    borderRadius: 8,
    backgroundColor: C.groupBadge,
    alignItems: 'center',
    justifyContent: 'center',
    borderWidth: 1.5,
    borderColor: C.white,
  },
  groupBadgeText: { color: C.white, fontSize: 8, fontWeight: 'bold' },

  convBody: { flex: 1 },
  convRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: 2,
  },

  convName: { flex: 1, color: C.text, fontWeight: '600', fontSize: 15, marginRight: 8 },
  convNameUnread: { color: '#1A1A1A', fontWeight: '700' },
  convNamePassivated: { color: C.passivated },

  convTs: { color: C.ts, fontSize: 11, flexShrink: 0 },
  tsPassivated: { color: C.passivated },

  previewRow: { flex: 1, flexDirection: 'row', alignItems: 'center', marginRight: 6 },
  typeIconText: { fontSize: 12 },
  convPreview: { flex: 1, color: C.ts, fontSize: 13 },
  previewPassivated: { color: C.passivated },

  unreadBadge: {
    backgroundColor: C.badgeBg,
    borderRadius: 10,
    paddingHorizontal: 6,
    paddingVertical: 2,
    minWidth: 20,
    alignItems: 'center',
    justifyContent: 'center',
  },
  unreadBadgeText: { color: C.white, fontSize: 11, fontWeight: 'bold' },

  metaRow: { flexDirection: 'row', alignItems: 'center', marginTop: 2, gap: 6 },
  metaText: { color: C.ts, fontSize: 10 },
  passivatedChip: {
    backgroundColor: '#EEEEEE',
    borderRadius: 4,
    paddingHorizontal: 5,
    paddingVertical: 1,
  },
  passivatedChipText: { color: C.passivated, fontSize: 9, fontWeight: '600' },

  // ── Room item ────────────────────────────────────────────────────────────────
  item: { flexDirection: 'row', alignItems: 'center', paddingHorizontal: 16, paddingVertical: 12 },
  roomAvatar: { width: 46, height: 46, borderRadius: 23, alignItems: 'center', justifyContent: 'center', marginRight: 12 },
  roomAvatarText: { color: C.white, fontWeight: 'bold', fontSize: 16 },
  itemBody: { flex: 1 },
  itemRow: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', marginBottom: 3 },
  roomName: { color: C.green, fontWeight: '600', fontSize: 15, flex: 1, marginRight: 8 },
  roomBadgeGroup: { flexDirection: 'row', alignItems: 'center', gap: 8 },
  badge: { flexDirection: 'row', alignItems: 'center', gap: 3 },
  chatBadge: { gap: 3 },
  badgeIcon: { width: 13, height: 13, tintColor: C.ts },
  badgeText: { color: C.ts, fontSize: 11 },
  roomDesc: { color: C.text, fontSize: 12, marginBottom: 5 },
  progressTrack: { height: 3, backgroundColor: '#EEEEEE', borderRadius: 2, overflow: 'hidden' },
  progressFill: { height: 3, borderRadius: 2 },

  divider: { height: 1, backgroundColor: C.sep, marginLeft: 76 },

  empty: { flex: 1, alignItems: 'center', justifyContent: 'center', padding: 40 },
  emptyIcon: { width: 64, height: 64, tintColor: C.ts, marginBottom: 16 },
  emptyTitle: { color: C.text, fontSize: 18, fontWeight: 'bold', marginBottom: 6 },
  emptySub: { color: C.ts, fontSize: 13, textAlign: 'center', lineHeight: 18 },

  resumePill: {
    position: 'absolute',
    bottom: 16,
    alignSelf: 'center',
    backgroundColor: C.green,
    borderRadius: 24,
    paddingHorizontal: 20,
    paddingVertical: 10,
    elevation: 4,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.2,
    shadowRadius: 4,
  },
  resumePillText: { color: C.white, fontWeight: '600', fontSize: 14 },
});
