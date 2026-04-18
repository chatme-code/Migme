import { useCallback, useEffect, useRef, useState } from 'react';
import PagerView from 'react-native-pager-view';
import { useRouter } from 'expo-router';
import {
  Animated,
  Image,
  ImageSourcePropType,
  Modal,
  Platform,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  TouchableOpacity,
  useWindowDimensions,
  View,
} from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { Ionicons } from '@expo/vector-icons';
import HomeScreen from './home';
import FeedScreen from './feed';
import ChatScreen from './chat';
import RoomListScreen from './roomlist';
import { API_BASE, getMe, logout } from '../../services/auth';
import { getSession } from '../../services/storage';
import { getCreditBalance } from '../../services/credit';
import ProfileScreen from './profile';
import StoreModal from '../../components/StoreModal';
import CreditsModal from '../../components/CreditsModal';
import MerchantsModal from '../../components/MerchantsModal';
import DiscoverModal from '../../components/DiscoverModal';
import LeaderboardModal from '../../components/LeaderboardModal';
import SettingsModal from '../../components/SettingsModal';
import NotificationsModal from '../../components/NotificationsModal';
import SearchFriendModal from '../../components/SearchFriendModal';
import { useAppTheme, type AppTheme } from '../../services/themeContext';

type RouteKey = 'home' | 'feed' | 'chat' | 'roomlist';

const ROUTES: { key: RouteKey; title: string }[] = [
  { key: 'home',     title: 'Home' },
  { key: 'feed',     title: 'Feed' },
  { key: 'chat',     title: 'Chat' },
  { key: 'roomlist', title: 'Room List' },
];

const TAB_ICONS: Record<RouteKey, ImageSourcePropType> = {
  home:     require('../../assets/icons/ad_userppl_grey.png'),
  feed:     require('../../assets/icons/ad_feed_grey.png'),
  chat:     require('../../assets/icons/ad_chat_grey.png'),
  roomlist: require('../../assets/icons/ad_chatroom_grey.png'),
};

const SCREENS = [HomeScreen, FeedScreen, ChatScreen, RoomListScreen];

function CustomTabBar({
  index,
  onTabPress,
}: {
  index: number;
  onTabPress: (i: number) => void;
}) {
  const theme = useAppTheme();
  return (
    <View style={[tabBarStyles.container, { backgroundColor: theme.tabBg, borderBottomColor: theme.tabBorder }]}>
      {ROUTES.map((route, i) => {
        const focused = index === i;
        const color   = focused ? theme.tabActiveColor : theme.tabInactiveColor;
        return (
          <TouchableOpacity
            key={route.key}
            style={tabBarStyles.tab}
            onPress={() => onTabPress(i)}
            activeOpacity={0.7}
            testID={`tab-${route.key}`}
          >
            <Image
              source={TAB_ICONS[route.key]}
              style={[tabBarStyles.icon, { tintColor: color }]}
              resizeMode="contain"
            />
            <Text style={[tabBarStyles.label, { color }]}>
              {route.title}
            </Text>
            {focused && <View style={[tabBarStyles.indicator, { backgroundColor: theme.tabActiveColor }]} />}
          </TouchableOpacity>
        );
      })}
    </View>
  );
}

type PresenceStatus = 'online' | 'away' | 'busy' | 'offline';

interface DrawerUser {
  username: string;
  displayName: string | null;
  creditFormatted: string;
  level: number;
  displayPicture?: string | null;
}

const PRESENCE_COLORS: Record<PresenceStatus, string> = {
  online:  '#4CAF50',
  away:    '#FFC107',
  busy:    '#F44336',
  offline: '#BDBDBD',
};

const PRESENCE_LABELS: Record<PresenceStatus, string> = {
  online:  'Online',
  away:    'Away',
  busy:    'Busy',
  offline: 'Offline',
};

async function buildAuthHeaders(json = false): Promise<Record<string, string>> {
  const h: Record<string, string> = {};
  if (json) h['Content-Type'] = 'application/json';
  if (Platform.OS !== 'web') {
    const cookie = await getSession();
    if (cookie) h['Cookie'] = cookie;
  }
  return h;
}

function Drawer({
  visible,
  onClose,
  onLogout,
  onOpenProfile,
  onOpenStore,
  onOpenCredits,
  onOpenMerchants,
  onOpenDiscover,
  onOpenLeaderboard,
  onOpenSettings,
  user,
}: {
  visible: boolean;
  onClose: () => void;
  onLogout: () => void;
  onOpenProfile: () => void;
  onOpenStore: () => void;
  onOpenCredits: () => void;
  onOpenMerchants: () => void;
  onOpenDiscover: () => void;
  onOpenLeaderboard: () => void;
  onOpenSettings: () => void;
  user: DrawerUser | null;
}) {
  const theme = useAppTheme();
  const { width } = useWindowDimensions();
  const drawerWidth = Math.min(width * 0.78, 320);
  const slideAnim = useRef(new Animated.Value(-drawerWidth)).current;
  const fadeAnim  = useRef(new Animated.Value(0)).current;
  const [statusMsg, setStatusMsg]       = useState('');
  const [presence, setPresence]         = useState<PresenceStatus>('online');
  const [showPicker, setShowPicker]     = useState(false);
  const [saving, setSaving]             = useState(false);

  useEffect(() => {
    if (visible) {
      Animated.parallel([
        Animated.timing(slideAnim, { toValue: 0,          duration: 260, useNativeDriver: true }),
        Animated.timing(fadeAnim,  { toValue: 1,          duration: 260, useNativeDriver: true }),
      ]).start();
      loadMyStatus();
    } else {
      Animated.parallel([
        Animated.timing(slideAnim, { toValue: -drawerWidth, duration: 220, useNativeDriver: true }),
        Animated.timing(fadeAnim,  { toValue: 0,            duration: 220, useNativeDriver: true }),
      ]).start();
    }
  }, [visible]);

  const loadMyStatus = async () => {
    try {
      const headers = await buildAuthHeaders();
      const opts: RequestInit = Platform.OS === 'web' ? { credentials: 'include' } : {};
      const res = await fetch(`${API_BASE}/api/me/status`, { headers, ...opts });
      if (res.ok) {
        const data = await res.json();
        if (data.statusMessage !== undefined) setStatusMsg(data.statusMessage);
        if (data.presence) setPresence(data.presence as PresenceStatus);
      }
    } catch {}
  };

  const saveStatus = async (msg: string, pres?: PresenceStatus) => {
    if (saving) return;
    setSaving(true);
    try {
      const headers = await buildAuthHeaders(true);
      const opts: RequestInit = Platform.OS === 'web' ? { credentials: 'include' } : {};
      await fetch(`${API_BASE}/api/me/status`, {
        method: 'POST',
        headers,
        body: JSON.stringify({ message: msg, presence: pres ?? presence }),
        ...opts,
      });
    } catch {}
    setSaving(false);
  };

  const handleStatusMsgSubmit = () => {
    saveStatus(statusMsg);
  };

  const handlePresenceSelect = (p: PresenceStatus) => {
    setPresence(p);
    setShowPicker(false);
    saveStatus(statusMsg, p);
  };

  const initials = user
    ? (user.displayName || user.username).slice(0, 2).toUpperCase()
    : 'ME';

  type MenuItem = {
    img: ImageSourcePropType;
    label: string;
    right?: string;
    rightSub?: string;
    action?: () => void;
  };

  const MENU_ITEMS: MenuItem[] = [
    {
      img: require('../../assets/icons/ad_avatar_white.png'),
      label: 'Profile',
      action: () => { onClose(); onOpenProfile(); },
    },
    {
      img: require('../../assets/icons/ic_menu_store.png'),
      label: 'Store',
      action: () => { onClose(); onOpenStore(); },
    },
    {
      img: require('../../assets/icons/ad_solidcredit.png'),
      label: 'Credits',
      right: user?.creditFormatted ?? 'IDR 0',
      action: () => { onClose(); onOpenCredits(); },
    },
    {
      img: require('../../assets/icons/ad_solidmerch.png'),
      label: 'Merchants',
      action: () => { onClose(); onOpenMerchants(); },
    },
    {
      img: require('../../assets/icons/ad_explore_orange.png'),
      label: 'Discover',
      action: () => { onClose(); onOpenDiscover(); },
    },
    {
      img: require('../../assets/icons/ad_solidbadge.png'),
      label: 'Leaderboards',
      action: () => { onClose(); onOpenLeaderboard(); },
    },
    {
      img: require('../../assets/icons/ad_setting_green.png'),
      label: 'Settings',
      action: () => { onClose(); onOpenSettings(); },
    },
    {
      img: require('../../assets/icons/ad_chatleave_grey.png'),
      label: 'Logout',
      action: () => { onClose(); onLogout(); },
    },
  ];

  const ds = makeDrawerStyles(theme);

  return (
    <Modal
      visible={visible}
      transparent
      animationType="none"
      onRequestClose={onClose}
      statusBarTranslucent
    >
      <View style={StyleSheet.absoluteFill}>
        <Animated.View
          style={[StyleSheet.absoluteFill, ds.backdrop, { opacity: fadeAnim }]}
        >
          <Pressable style={StyleSheet.absoluteFill} onPress={onClose} />
        </Animated.View>

        <Animated.View
          style={[ds.panel, { width: drawerWidth, transform: [{ translateX: slideAnim }] }]}
        >
          <ScrollView showsVerticalScrollIndicator={false} bounces={false}>
            <View style={ds.profile}>
              <View style={ds.avatarCircle}>
                {user?.displayPicture ? (
                  <Image
                    source={{ uri: user.displayPicture }}
                    style={{ width: '100%', height: '100%', borderRadius: 999 }}
                    resizeMode="cover"
                  />
                ) : (
                  <Text style={ds.avatarText}>{initials}</Text>
                )}
              </View>
              <Text style={ds.profileName}>{user?.displayName || user?.username || 'User'}</Text>
              <Text style={ds.profileLevel}>Level {user?.level ?? 1}</Text>

              <View style={ds.presenceRow}>
                <TouchableOpacity
                  onPress={() => setShowPicker(v => !v)}
                  style={ds.presenceBtn}
                  testID="button-presence-picker"
                  activeOpacity={0.7}
                >
                  <View style={[ds.onlineDot, { backgroundColor: PRESENCE_COLORS[presence] }]} />
                  <Ionicons name="chevron-down" size={14} color={theme.textSecondary} />
                </TouchableOpacity>
                <TextInput
                  style={ds.statusInput}
                  placeholder="What's on your mind?"
                  placeholderTextColor={theme.textSecondary}
                  value={statusMsg}
                  onChangeText={setStatusMsg}
                  onSubmitEditing={handleStatusMsgSubmit}
                  onBlur={handleStatusMsgSubmit}
                  returnKeyType="done"
                  testID="input-status-message"
                />
              </View>

              {showPicker && (
                <View style={ds.pickerPanel} testID="panel-status-picker">
                  {(['online', 'away', 'busy', 'offline'] as PresenceStatus[]).map(p => (
                    <TouchableOpacity
                      key={p}
                      style={[ds.pickerItem, presence === p && ds.pickerItemActive]}
                      onPress={() => handlePresenceSelect(p)}
                      testID={`button-status-${p}`}
                      activeOpacity={0.7}
                    >
                      <View style={[ds.pickerDot, { backgroundColor: PRESENCE_COLORS[p] }]} />
                      <Text style={[ds.pickerLabel, presence === p && ds.pickerLabelActive]}>
                        {PRESENCE_LABELS[p]}
                      </Text>
                    </TouchableOpacity>
                  ))}
                </View>
              )}
            </View>

            <View style={ds.menuList}>
              {MENU_ITEMS.map((item, idx) => (
                <View key={item.label}>
                  <TouchableOpacity
                    style={ds.menuRow}
                    activeOpacity={0.6}
                    onPress={item.action}
                    testID={`drawer-menu-${item.label.toLowerCase()}`}
                  >
                    <Image
                      source={item.img}
                      style={[ds.menuIconImg, { tintColor: theme.accent }]}
                      resizeMode="contain"
                    />
                    <Text style={[ds.menuLabel, { color: theme.accent }]}>{item.label}</Text>
                    {item.right !== undefined && (
                      <View style={ds.menuRightCol}>
                        <Text style={[ds.menuRight, { color: theme.textSecondary }]} testID="text-credit-idr-drawer">{item.right}</Text>
                        {item.rightSub !== undefined && (
                          <Text style={ds.menuRightSub} testID="text-credit-usd-drawer">{item.rightSub}</Text>
                        )}
                      </View>
                    )}
                  </TouchableOpacity>
                  {idx < MENU_ITEMS.length - 1 && <View style={[ds.menuDivider, { backgroundColor: theme.divider }]} />}
                </View>
              ))}
            </View>
          </ScrollView>
        </Animated.View>
      </View>
    </Modal>
  );
}

export default function HomeLayout() {
  const theme  = useAppTheme();
  const insets    = useSafeAreaInsets();
  const router    = useRouter();
  const pagerRef  = useRef<PagerView>(null);
  const [index, setIndex]           = useState(0);
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [drawerUser, setDrawerUser] = useState<DrawerUser | null>(null);
  const [profileOpen, setProfileOpen] = useState(false);
  const [storeOpen, setStoreOpen]     = useState(false);
  const [creditsOpen, setCreditsOpen]       = useState(false);
  const [merchantsOpen, setMerchantsOpen]   = useState(false);
  const [discoverOpen, setDiscoverOpen]         = useState(false);
  const [leaderboardOpen, setLeaderboardOpen]   = useState(false);
  const [settingsOpen, setSettingsOpen]         = useState(false);
  const [notificationsOpen, setNotificationsOpen] = useState(false);
  const [searchFriendOpen, setSearchFriendOpen]   = useState(false);
  const [unreadCount, setUnreadCount]             = useState(0);

  const fetchUnreadCount = useCallback(async () => {
    try {
      const headers: Record<string, string> = { 'Content-Type': 'application/json' };
      if (Platform.OS !== 'web') {
        const cookie = await getSession();
        if (cookie) headers['Cookie'] = cookie;
      }
      const opts: RequestInit = Platform.OS === 'web' ? { credentials: 'include' as RequestCredentials } : {};
      const res = await fetch(`${API_BASE}/api/uns/notifications/me/count`, { headers, ...opts });
      if (res.ok) {
        const data = await res.json();
        setUnreadCount(data.count ?? 0);
      }
    } catch {}
  }, []);

  const handleLogout = async () => {
    await logout();
    router.replace('/');
  };

  const fetchDrawerUser = async () => {
    try {
      const me = await getMe();
      if (!me) return;
      const [credit, headers] = await Promise.all([
        getCreditBalance(me.username),
        (async () => {
          const h: Record<string, string> = {};
          if (Platform.OS !== 'web') {
            const cookie = await getSession();
            if (cookie) h['Cookie'] = cookie;
          }
          return h;
        })(),
      ]);
      const profileRes = await fetch(`${API_BASE}/api/profile/me`, {
        headers,
        ...(Platform.OS === 'web' ? { credentials: 'include' as RequestCredentials } : {}),
      });
      const profileData = profileRes.ok ? await profileRes.json() : null;
      const displayPicture = profileData?.profile?.displayPicture ?? null;
      const migLevel = profileData?.profile?.migLevel ?? 1;
      setDrawerUser({
        username:        me.username,
        displayName:     me.displayName,
        creditFormatted: credit?.formatted ?? 'IDR 0',
        level:           migLevel,
        displayPicture,
      });
    } catch {}
  };

  useEffect(() => {
    fetchDrawerUser();
    fetchUnreadCount();
    const interval = setInterval(fetchUnreadCount, 30000);
    return () => clearInterval(interval);
  }, []);

  const prevSettingsOpen = useRef(settingsOpen);
  useEffect(() => {
    if (prevSettingsOpen.current && !settingsOpen) {
      fetchDrawerUser();
    }
    prevSettingsOpen.current = settingsOpen;
  }, [settingsOpen]);

  const s = makeHeaderStyles(theme);

  return (
    <View style={[s.root]}>
      <View style={[s.header, { paddingTop: insets.top + 10 }]}>
        <View style={s.headerLeft}>
          <TouchableOpacity
            onPress={() => { setDrawerOpen(true); fetchDrawerUser(); }}
            style={s.hamburger}
            testID="button-menu"
          >
            <View style={[s.hamburgerLine, { backgroundColor: theme.textOnAccent }]} />
            <View style={[s.hamburgerLine, { width: 16, backgroundColor: theme.textOnAccent }]} />
            <View style={[s.hamburgerLine, { backgroundColor: theme.textOnAccent }]} />
          </TouchableOpacity>
          <Image
            source={require('../../assets/logo_new.png')}
            style={s.headerLogo}
            resizeMode="contain"
          />
        </View>

        <View style={s.headerIcons}>
          <TouchableOpacity
            testID="button-search-friend"
            onPress={() => setSearchFriendOpen(true)}
          >
            <Image
              source={require('../../assets/icons/ad_usersearch_white.png')}
              style={[s.headerIcon, { tintColor: '#FFFFFF' }]}
              resizeMode="contain"
            />
          </TouchableOpacity>
          <TouchableOpacity
            style={{ marginLeft: 16 }}
            testID="button-notifications"
            onPress={() => {
              setNotificationsOpen(true);
              fetchUnreadCount();
            }}
          >
            <View style={{ position: 'relative' }}>
              <Image
                source={require('../../assets/icons/ad_alert_white.png')}
                style={s.headerIcon}
                resizeMode="contain"
              />
              {unreadCount > 0 && (
                <View style={[s.bellBadge, { borderColor: theme.headerBg }]} testID="badge-bell-count">
                  <Text style={s.bellBadgeText}>
                    {unreadCount > 99 ? '99+' : unreadCount}
                  </Text>
                </View>
              )}
            </View>
          </TouchableOpacity>
        </View>
      </View>

      <CustomTabBar
        index={index}
        onTabPress={(i) => {
          setIndex(i);
          pagerRef.current?.setPage(i);
        }}
      />

      <PagerView
        ref={pagerRef}
        style={{ flex: 1 }}
        initialPage={0}
        onPageSelected={(e) => setIndex(e.nativeEvent.position)}
        scrollEnabled
      >
        {SCREENS.map((Screen, i) => (
          <View key={i} style={{ flex: 1 }}>
            <Screen />
          </View>
        ))}
      </PagerView>

      <Drawer
        visible={drawerOpen}
        onClose={() => setDrawerOpen(false)}
        onLogout={handleLogout}
        onOpenProfile={() => { setDrawerOpen(false); setProfileOpen(true); }}
        onOpenStore={() => { setDrawerOpen(false); setStoreOpen(true); }}
        onOpenCredits={() => { setDrawerOpen(false); setCreditsOpen(true); }}
        onOpenMerchants={() => { setDrawerOpen(false); setMerchantsOpen(true); }}
        onOpenDiscover={() => { setDrawerOpen(false); setDiscoverOpen(true); }}
        onOpenLeaderboard={() => { setDrawerOpen(false); setLeaderboardOpen(true); }}
        onOpenSettings={() => { setDrawerOpen(false); setSettingsOpen(true); }}
        user={drawerUser}
      />

      <Modal
        visible={profileOpen}
        animationType="slide"
        onRequestClose={() => setProfileOpen(false)}
        statusBarTranslucent
      >
        <ProfileScreen onClose={() => setProfileOpen(false)} />
      </Modal>

      <StoreModal
        visible={storeOpen}
        onClose={() => setStoreOpen(false)}
        username={drawerUser?.username ?? null}
      />

      <CreditsModal
        visible={creditsOpen}
        onClose={() => setCreditsOpen(false)}
        username={drawerUser?.username ?? null}
      />

      <MerchantsModal
        visible={merchantsOpen}
        onClose={() => setMerchantsOpen(false)}
      />

      <DiscoverModal
        visible={discoverOpen}
        onClose={() => setDiscoverOpen(false)}
      />

      <LeaderboardModal
        visible={leaderboardOpen}
        onClose={() => setLeaderboardOpen(false)}
      />

      <SettingsModal
        visible={settingsOpen}
        onClose={() => setSettingsOpen(false)}
        onLogout={handleLogout}
        onAvatarChange={fetchDrawerUser}
        username={drawerUser?.username ?? null}
      />

      <NotificationsModal
        visible={notificationsOpen}
        onClose={() => {
          setNotificationsOpen(false);
          fetchUnreadCount();
        }}
      />

      <SearchFriendModal
        visible={searchFriendOpen}
        onClose={() => setSearchFriendOpen(false)}
      />
    </View>
  );
}

// ─── Dynamic style factories ──────────────────────────────────────────────────

function makeHeaderStyles(t: AppTheme) {
  return StyleSheet.create({
    root: { flex: 1, backgroundColor: t.headerBg },
    header: {
      backgroundColor: t.headerBg,
      paddingBottom: 12,
      paddingHorizontal: 16,
      flexDirection: 'row',
      alignItems: 'center',
      justifyContent: 'space-between',
    },
    headerLeft:  { flexDirection: 'row', alignItems: 'center' },
    hamburger:   { marginRight: 12, justifyContent: 'center', gap: 5 },
    hamburgerLine: { width: 20, height: 2, backgroundColor: t.textOnAccent, borderRadius: 1 },
    headerLogo:  { width: 110, height: 44, marginLeft: -4 },
    headerIcons: { flexDirection: 'row', alignItems: 'center' },
    headerIcon:  { width: 22, height: 22, tintColor: t.textOnAccent },
    bellBadge: {
      position: 'absolute',
      top: -5,
      right: -6,
      backgroundColor: '#E53935',
      borderRadius: 8,
      minWidth: 16,
      height: 16,
      alignItems: 'center',
      justifyContent: 'center',
      paddingHorizontal: 3,
      borderWidth: 1,
      borderColor: t.headerBg,
    },
    bellBadgeText: { color: '#FFFFFF', fontSize: 9, fontWeight: '700' },
  });
}

const tabBarStyles = StyleSheet.create({
  container: {
    flexDirection: 'row',
    borderBottomWidth: 1,
  },
  tab: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: 10,
    position: 'relative',
  },
  icon:  { width: 20, height: 20, marginBottom: 3 },
  label: { fontSize: 10, fontWeight: '500' },
  indicator: {
    position: 'absolute',
    bottom: 0,
    left: 0,
    right: 0,
    height: 2,
  },
});

function makeDrawerStyles(t: AppTheme) {
  return StyleSheet.create({
    backdrop: { backgroundColor: 'rgba(0,0,0,0.45)' },
    panel: {
      position: 'absolute',
      top: 0,
      left: 0,
      bottom: 0,
      backgroundColor: t.drawerBg,
      shadowColor: '#000',
      shadowOpacity: 0.25,
      shadowRadius: 8,
      shadowOffset: { width: 2, height: 0 },
      elevation: 8,
    },
    profile: {
      backgroundColor: t.drawerBg,
      paddingHorizontal: 20,
      paddingTop: 48,
      paddingBottom: 20,
    },
    avatarCircle: {
      width: 64,
      height: 64,
      borderRadius: 32,
      backgroundColor: t.accent,
      alignItems: 'center',
      justifyContent: 'center',
      marginBottom: 12,
    },
    avatarText:   { color: '#FFFFFF', fontSize: 24, fontWeight: 'bold' },
    profileName:  { fontSize: 18, fontWeight: '700', color: t.textPrimary, marginBottom: 2 },
    profileLevel: { fontSize: 13, color: t.textSecondary, marginBottom: 14 },

    presenceRow: { flexDirection: 'row', alignItems: 'center' },
    presenceBtn: {
      flexDirection: 'row',
      alignItems: 'center',
      marginRight: 8,
      paddingVertical: 4,
      paddingHorizontal: 2,
    },
    onlineDot: { width: 12, height: 12, borderRadius: 6, marginRight: 4 },
    statusInput: {
      flex: 1,
      height: 36,
      backgroundColor: t.inputBg,
      borderRadius: 4,
      paddingHorizontal: 10,
      fontSize: 13,
      color: t.textPrimary,
      borderWidth: 1,
      borderColor: t.border,
    },
    pickerPanel: {
      marginTop: 8,
      backgroundColor: t.cardBg,
      borderRadius: 8,
      borderWidth: 1,
      borderColor: t.border,
      overflow: 'hidden',
    },
    pickerItem:       { flexDirection: 'row', alignItems: 'center', paddingVertical: 10, paddingHorizontal: 14 },
    pickerItemActive: { backgroundColor: t.accentSoft },
    pickerDot:        { width: 10, height: 10, borderRadius: 5, marginRight: 10 },
    pickerLabel:      { fontSize: 14, color: t.textPrimary },
    pickerLabelActive:{ fontWeight: '700', color: t.accent },

    menuList: { backgroundColor: t.cardBg, marginTop: 8 },
    menuRow:  { flexDirection: 'row', alignItems: 'center', paddingVertical: 15, paddingHorizontal: 20 },
    menuIconImg:   { width: 24, height: 24, marginRight: 16 },
    menuLabel:     { flex: 1, fontSize: 15 },
    menuRightCol:  { alignItems: 'flex-end' },
    menuRight:     { fontSize: 13, fontWeight: '600' },
    menuRightSub:  { fontSize: 11, color: '#27AE60', marginTop: 1 },
    menuDivider:   { height: 1, marginLeft: 60 },
  });
}
