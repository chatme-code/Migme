import { useCallback, useEffect, useRef, useState } from 'react';
import {
  ActivityIndicator,
  Animated,
  FlatList,
  Image,
  Modal,
  Platform,
  Pressable,
  RefreshControl,
  StyleSheet,
  Text,
  TextInput,
  TouchableOpacity,
  View,
} from 'react-native';
import { API_BASE, getMe, buildHeaders } from '../../services/auth';
import ViewProfileModal from '../../components/ViewProfileModal';
import CreditsModal from '../../components/CreditsModal';
import MultiRoomChatModal, { type PrivateChat } from '../../components/MultiRoomChatModal';
import { useAppTheme } from '../../services/themeContext';

const C = {
  bg: '#FFFFFF',
  divider: '#E8E8E8',
  text: '#212121',
  sub: '#757575',
  green: '#64B9A0',
  inputBg: '#F2F2F2',
  inputBorder: '#E0E0E0',
  onlineDot: '#4CAF50',
  offlineDot: '#BDBDBD',
  awayDot: '#FFC107',
  busyDot: '#F44336',
  headerBorder: '#E0E0E0',
  menuBg: '#FFFFFF',
  menuShadow: '#00000022',
  menuText: '#212121',
  menuDanger: '#E53935',
  overlay: 'rgba(0,0,0,0.35)',
};

type PresenceStatus = 'online' | 'away' | 'busy' | 'offline';

interface ContactRow {
  id?: string;
  friendUserId?: string;
  friendUsername?: string;
  friendDisplayName?: string | null;
  username?: string;
  displayName?: string | null;
  displayPicture?: string | null;
  aboutMe?: string | null;
  presence?: PresenceStatus;
  statusMessage?: string;
}

const AVATAR_DEFAULT    = require('../../assets/icons/icon_default_avatar.png');
const PRESENCE_ONLINE   = require('../../assets/icons/ic_presence_online.png');
const PRESENCE_OFFLINE  = require('../../assets/icons/ic_presence_offline.png');
const PRESENCE_AWAY     = require('../../assets/icons/ic_presence_away.png');
const PRESENCE_BUSY     = require('../../assets/icons/ic_presence_busy.png');
const ICON_VIEW_PROFILE = require('../../assets/icons/ad_avatar_grey.png');
const ICON_SEND_CREDIT  = require('../../assets/icons/ad_solidcredit.png');
const ICON_PRIVATE_CHAT = require('../../assets/icons/ad_chatlarge_grey.png');

function presenceIcon(status: PresenceStatus) {
  if (status === 'online') return PRESENCE_ONLINE;
  if (status === 'away')   return PRESENCE_AWAY;
  if (status === 'busy')   return PRESENCE_BUSY;
  return PRESENCE_OFFLINE;
}

function ContactItem({
  item,
  onMenuPress,
}: {
  item: ContactRow;
  onMenuPress: (item: ContactRow) => void;
}) {
  const theme = useAppTheme();
  const displayName = item.friendDisplayName || item.displayName || item.friendUsername || item.username || '';
  const username = item.friendUsername || item.username || '';
  const statusMsg = item.statusMessage?.trim();
  const subtitle = statusMsg || `@${username}`;
  const status: PresenceStatus = item.presence ?? 'offline';
  const pIcon = presenceIcon(status);
  const id = item.id || item.friendUserId || username;

  return (
    <View style={[styles.row, { backgroundColor: theme.cardBg }]}>
      <View style={styles.avatarWrap}>
        {item.displayPicture ? (
          <Image
            source={{ uri: item.displayPicture.startsWith('http') ? item.displayPicture : `${API_BASE}${item.displayPicture}` }}
            style={styles.avatar}
            defaultSource={AVATAR_DEFAULT}
          />
        ) : (
          <Image source={AVATAR_DEFAULT} style={styles.avatar} resizeMode="cover" />
        )}
        <Image source={pIcon} style={styles.presenceDot} resizeMode="contain" />
      </View>

      <View style={styles.textArea}>
        <Text style={[styles.name, { color: theme.textPrimary }]} numberOfLines={1}>
          {displayName}
        </Text>
        <Text
          style={[styles.subtitle, { color: theme.textSecondary }, statusMsg ? styles.subtitleStatus : null, statusMsg ? { color: theme.textSecondary } : null]}
          numberOfLines={1}
        >
          {subtitle}
        </Text>
      </View>

      <TouchableOpacity
        style={styles.menuBtn}
        activeOpacity={0.6}
        onPress={() => onMenuPress(item)}
        testID={`button-menu-contact-${id}`}
        hitSlop={{ top: 8, bottom: 8, left: 8, right: 8 }}
      >
        <View style={[styles.dot, { backgroundColor: theme.textSecondary }]} />
        <View style={[styles.dot, { backgroundColor: theme.textSecondary }]} />
        <View style={[styles.dot, { backgroundColor: theme.textSecondary }]} />
      </TouchableOpacity>
    </View>
  );
}

function ContactContextMenu({
  visible,
  contact,
  onClose,
  onViewProfile,
  onSendCredit,
  onPrivateChat,
}: {
  visible: boolean;
  contact: ContactRow | null;
  onClose: () => void;
  onViewProfile: () => void;
  onSendCredit: () => void;
  onPrivateChat: () => void;
}) {
  const theme     = useAppTheme();
  const fadeAnim  = useRef(new Animated.Value(0)).current;
  const slideAnim = useRef(new Animated.Value(60)).current;

  useEffect(() => {
    if (visible) {
      Animated.parallel([
        Animated.timing(fadeAnim,  { toValue: 1, duration: 200, useNativeDriver: true }),
        Animated.timing(slideAnim, { toValue: 0, duration: 220, useNativeDriver: true }),
      ]).start();
    } else {
      Animated.parallel([
        Animated.timing(fadeAnim,  { toValue: 0, duration: 160, useNativeDriver: true }),
        Animated.timing(slideAnim, { toValue: 60, duration: 160, useNativeDriver: true }),
      ]).start();
    }
  }, [visible]);

  if (!contact) return null;

  const displayName = contact.friendDisplayName || contact.displayName || contact.friendUsername || contact.username || '';
  const username    = contact.friendUsername || contact.username || '';

  const MENU_ITEMS = [
    {
      label: 'View Profile',
      img: ICON_VIEW_PROFILE,
      action: () => { onClose(); setTimeout(onViewProfile, 180); },
      testID: 'button-ctx-view-profile',
    },
    {
      label: 'Send Credit',
      img: ICON_SEND_CREDIT,
      action: () => { onClose(); setTimeout(onSendCredit, 180); },
      testID: 'button-ctx-send-credit',
    },
    {
      label: 'Private Chat',
      img: ICON_PRIVATE_CHAT,
      action: () => { onClose(); setTimeout(onPrivateChat, 180); },
      testID: 'button-ctx-private-chat',
    },
  ];

  return (
    <Modal
      visible={visible}
      transparent
      animationType="none"
      onRequestClose={onClose}
      statusBarTranslucent
    >
      <Animated.View style={[styles.ctxOverlay, { opacity: fadeAnim }]}>
        <Pressable style={StyleSheet.absoluteFill} onPress={onClose} />

        <Animated.View style={[styles.ctxSheet, { backgroundColor: theme.cardBg, transform: [{ translateY: slideAnim }] }]}>
          <View style={styles.ctxHeader}>
            <Text style={[styles.ctxName, { color: theme.textPrimary }]} numberOfLines={1}>{displayName}</Text>
            <Text style={[styles.ctxUsername, { color: theme.textSecondary }]} numberOfLines={1}>@{username}</Text>
          </View>
          <View style={[styles.ctxDivider, { backgroundColor: theme.divider }]} />

          {MENU_ITEMS.map((item, idx) => (
            <View key={item.label}>
              <TouchableOpacity
                style={styles.ctxItem}
                activeOpacity={0.65}
                onPress={item.action}
                testID={item.testID}
              >
                <Image source={item.img} style={[styles.ctxItemIcon, { tintColor: theme.accent }]} resizeMode="contain" />
                <Text style={[styles.ctxItemLabel, { color: theme.textPrimary }]}>{item.label}</Text>
              </TouchableOpacity>
              {idx < MENU_ITEMS.length - 1 && <View style={[styles.ctxDivider, { backgroundColor: theme.divider }]} />}
            </View>
          ))}

          <View style={[styles.ctxDivider, { backgroundColor: theme.divider }]} />
          <TouchableOpacity
            style={styles.ctxItem}
            activeOpacity={0.65}
            onPress={onClose}
            testID="button-ctx-cancel"
          >
            <Text style={[styles.ctxItemLabel, { color: theme.textSecondary, textAlign: 'center', flex: 1 }]}>
              Cancel
            </Text>
          </TouchableOpacity>
        </Animated.View>
      </Animated.View>
    </Modal>
  );
}

const MAX_OPEN_PRIVATE_TABS = 5;

export default function HomeScreen() {
  const theme = useAppTheme();
  const [contacts, setContacts]     = useState<ContactRow[]>([]);
  const [query, setQuery]           = useState('');
  const [loading, setLoading]       = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [myUsername, setMyUsername] = useState<string | null>(null);

  const [selectedContact, setSelectedContact] = useState<ContactRow | null>(null);
  const [menuVisible, setMenuVisible]         = useState(false);
  const [viewProfileVisible, setViewProfileVisible] = useState(false);
  const [creditsVisible, setCreditsVisible]   = useState(false);

  const [openPrivateChats, setOpenPrivateChats]   = useState<PrivateChat[]>([]);
  const [activePrivateChatId, setActivePrivateChatId] = useState<string | null>(null);
  const [privateChatModalVisible, setPrivateChatModalVisible] = useState(false);
  const [privateChatLoading, setPrivateChatLoading] = useState(false);

  useEffect(() => {
    getMe().then(me => { if (me) setMyUsername(me.username); });
  }, []);

  const fetchContacts = useCallback(async () => {
    try {
      const headers = await buildHeaders();
      const opts: RequestInit = Platform.OS === 'web'
        ? { credentials: 'include' }
        : { headers: headers as Record<string, string> };
      const res = await fetch(`${API_BASE}/api/contacts`, opts);
      if (!res.ok) return;
      const data = await res.json();
      setContacts(Array.isArray(data) ? data : []);
    } catch {
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, []);

  useEffect(() => {
    fetchContacts();
    const interval = setInterval(fetchContacts, 30000);
    return () => clearInterval(interval);
  }, [fetchContacts]);

  const onRefresh = useCallback(() => {
    setRefreshing(true);
    fetchContacts();
  }, [fetchContacts]);

  const filtered = query.trim()
    ? contacts.filter(c => {
        const q = query.toLowerCase();
        const name = (c.friendDisplayName || c.displayName || c.friendUsername || c.username || '').toLowerCase();
        const msg = (c.statusMessage || '').toLowerCase();
        return name.includes(q) || msg.includes(q);
      })
    : contacts;

  const handleMenuPress = useCallback((item: ContactRow) => {
    setSelectedContact(item);
    setMenuVisible(true);
  }, []);

  const selectedUsername = selectedContact
    ? (selectedContact.friendUsername || selectedContact.username || '')
    : '';

  const handlePrivateChat = useCallback(async () => {
    if (!selectedContact) return;
    const uname = selectedContact.friendUsername || selectedContact.username || '';
    const dname = selectedContact.friendDisplayName || selectedContact.displayName || uname;
    if (!uname) return;
    setPrivateChatLoading(true);
    try {
      const headers = await buildHeaders();
      const res = await fetch(`${API_BASE}/api/chatsync/conversations/private`, {
        method: 'POST',
        headers: { ...(headers as Record<string, string>), 'Content-Type': 'application/json' },
        ...(Platform.OS === 'web' ? { credentials: 'include' as RequestCredentials } : {}),
        body: JSON.stringify({ targetUsername: uname }),
      });
      if (!res.ok) return;
      const { conversation: conv } = await res.json();
      const chatEntry: PrivateChat = {
        id: conv.id,
        peerUsername: uname,
        peerDisplayName: dname,
        color: conv.avatarColor ?? '#4CAF50',
      };
      setOpenPrivateChats(prev => {
        const exists = prev.find(c => c.id === conv.id);
        if (exists) return prev;
        return prev.length >= MAX_OPEN_PRIVATE_TABS
          ? [...prev.slice(1), chatEntry]
          : [...prev, chatEntry];
      });
      setActivePrivateChatId(conv.id);
      setPrivateChatModalVisible(true);
    } catch {
    } finally {
      setPrivateChatLoading(false);
    }
  }, [selectedContact]);

  const handleRemovePrivateChat = useCallback((chatId: string) => {
    setOpenPrivateChats(prev => {
      const next = prev.filter(c => c.id !== chatId);
      if (next.length === 0) {
        setActivePrivateChatId(null);
        setPrivateChatModalVisible(false);
      } else if (activePrivateChatId === chatId) {
        setActivePrivateChatId(next[next.length - 1]?.id ?? null);
      }
      return next;
    });
  }, [activePrivateChatId]);

  return (
    <View style={[styles.container, { backgroundColor: theme.screenBg }]}>
      <View style={[styles.searchBar, { backgroundColor: theme.inputBg, borderColor: theme.border }]}>
        <Image
          source={require('../../assets/icons/ad_search_grey.png')}
          style={[styles.searchIcon, { tintColor: theme.textSecondary }]}
          resizeMode="contain"
        />
        <TextInput
          style={[styles.searchInput, { color: theme.textPrimary }]}
          placeholder="Search contacts..."
          placeholderTextColor={theme.textSecondary}
          value={query}
          onChangeText={setQuery}
          autoCorrect={false}
          autoCapitalize="none"
          testID="input-search-friends"
        />
      </View>

      {loading ? (
        <View style={styles.center}>
          <ActivityIndicator color={theme.accent} size="large" />
        </View>
      ) : (
        <FlatList
          data={filtered}
          keyExtractor={(item, idx) => item.id || item.friendUserId || item.friendUsername || String(idx)}
          renderItem={({ item }) => (
            <ContactItem item={item} onMenuPress={handleMenuPress} />
          )}
          ItemSeparatorComponent={() => <View style={[styles.divider, { backgroundColor: theme.divider }]} />}
          refreshControl={
            <RefreshControl
              refreshing={refreshing}
              onRefresh={onRefresh}
              tintColor={theme.accent}
              colors={[theme.accent]}
            />
          }
          ListEmptyComponent={
            <View style={styles.empty}>
              <Image
                source={require('../../assets/icons/ad_userppl_grey.png')}
                style={[styles.emptyIcon, { tintColor: theme.textSecondary }]}
                resizeMode="contain"
              />
              <Text style={[styles.emptyText, { color: theme.textSecondary }]}>
                {query ? 'No contacts found' : 'No contacts yet'}
              </Text>
              {!query && (
                <Text style={[styles.emptySubText, { color: theme.textSecondary }]}>
                  Add friends from the People tab to see them here.
                </Text>
              )}
            </View>
          }
          contentContainerStyle={filtered.length === 0 ? styles.emptyContainer : undefined}
          showsVerticalScrollIndicator={false}
        />
      )}

      {privateChatLoading && (
        <View style={[styles.loadingOverlay, { backgroundColor: theme.isDark ? 'rgba(0,0,0,0.5)' : 'rgba(255,255,255,0.6)' }]} pointerEvents="none">
          <ActivityIndicator color={theme.accent} size="large" />
        </View>
      )}

      <ContactContextMenu
        visible={menuVisible}
        contact={selectedContact}
        onClose={() => setMenuVisible(false)}
        onViewProfile={() => setViewProfileVisible(true)}
        onSendCredit={() => setCreditsVisible(true)}
        onPrivateChat={handlePrivateChat}
      />

      <ViewProfileModal
        visible={viewProfileVisible}
        username={selectedUsername}
        onClose={() => setViewProfileVisible(false)}
      />

      <CreditsModal
        visible={creditsVisible}
        onClose={() => setCreditsVisible(false)}
        username={myUsername}
        initialTab="transfer"
        initialToUsername={selectedUsername}
      />

      <MultiRoomChatModal
        visible={privateChatModalVisible}
        openPrivateChats={openPrivateChats}
        openRooms={[]}
        activeTabId={activePrivateChatId}
        currentUserId={myUsername}
        onMinimize={() => setPrivateChatModalVisible(false)}
        onRemoveRoom={() => {}}
        onRemovePrivateChat={handleRemovePrivateChat}
        onChangeActiveTab={(id) => setActivePrivateChatId(id)}
        onOpenPrivateChat={() => {}}
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
            return [...prev, chatEntry];
          });
          setActivePrivateChatId(convId);
        }}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: C.bg },

  searchBar: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: C.inputBg,
    marginHorizontal: 12,
    marginVertical: 8,
    borderRadius: 8,
    paddingHorizontal: 10,
    borderWidth: 1,
    borderColor: C.inputBorder,
  },
  searchIcon: { width: 16, height: 16, tintColor: C.sub, marginRight: 8 },
  searchInput: {
    flex: 1,
    height: 38,
    fontSize: 14,
    color: C.text,
    paddingVertical: 0,
  },

  row: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: 10,
    paddingLeft: 16,
    paddingRight: 4,
    backgroundColor: C.bg,
  },

  avatarWrap: {
    width: 48,
    height: 48,
    marginRight: 12,
  },
  avatar: {
    width: 48,
    height: 48,
    borderRadius: 24,
  },
  presenceDot: {
    position: 'absolute',
    bottom: 0,
    right: 0,
    width: 14,
    height: 14,
  },

  textArea: { flex: 1 },
  name: {
    fontSize: 15,
    fontWeight: '600',
    color: C.text,
    marginBottom: 2,
  },
  subtitle: {
    fontSize: 13,
    color: C.sub,
  },
  subtitleStatus: {
    color: '#555555',
    fontStyle: 'italic',
  },

  menuBtn: {
    width: 40,
    height: 48,
    alignItems: 'center',
    justifyContent: 'center',
    gap: 4,
  },
  dot: {
    width: 4,
    height: 4,
    borderRadius: 2,
    backgroundColor: C.sub,
  },

  divider: {
    height: 1,
    backgroundColor: C.divider,
    marginLeft: 76,
  },

  center: { flex: 1, alignItems: 'center', justifyContent: 'center' },

  emptyContainer: { flex: 1 },
  empty: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    paddingTop: 80,
    paddingHorizontal: 32,
  },
  emptyIcon: {
    width: 56,
    height: 56,
    tintColor: C.offlineDot,
    marginBottom: 12,
  },
  emptyText: { fontSize: 14, color: C.sub, fontWeight: '600' },
  emptySubText: { fontSize: 12, color: C.sub, marginTop: 6, textAlign: 'center' },

  loadingOverlay: {
    ...StyleSheet.absoluteFillObject,
    backgroundColor: 'rgba(255,255,255,0.6)',
    alignItems: 'center',
    justifyContent: 'center',
    zIndex: 99,
  },

  ctxOverlay: {
    flex: 1,
    backgroundColor: C.overlay,
    justifyContent: 'flex-end',
  },
  ctxSheet: {
    backgroundColor: C.menuBg,
    paddingBottom: 24,
    shadowColor: '#000',
    shadowOpacity: 0.15,
    shadowRadius: 12,
    elevation: 12,
  },
  ctxHeader: {
    paddingHorizontal: 20,
    paddingTop: 18,
    paddingBottom: 12,
  },
  ctxName: {
    fontSize: 16,
    fontWeight: '700',
    color: C.text,
  },
  ctxUsername: {
    fontSize: 13,
    color: C.sub,
    marginTop: 2,
  },
  ctxDivider: {
    height: 1,
    backgroundColor: C.divider,
  },
  ctxItem: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 20,
    paddingVertical: 16,
  },
  ctxItemIcon: {
    width: 24,
    height: 24,
    marginRight: 14,
    tintColor: C.green,
  },
  ctxItemLabel: {
    fontSize: 15,
    color: C.menuText,
    fontWeight: '500',
  },
});
