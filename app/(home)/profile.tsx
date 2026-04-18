import { useRouter } from 'expo-router';
import { useCallback, useEffect, useRef, useState } from 'react';
import {
  ActivityIndicator,
  Alert,
  Image,
  Modal,
  Platform,
  ScrollView,
  StyleSheet,
  Text,
  TouchableOpacity,
  TouchableWithoutFeedback,
  View,
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { getMe, logout, type AuthUser, API_BASE } from '../../services/auth';
import { getSession } from '../../services/storage';
import {
  getCreditBalance,
  formatCredit,
  type CreditBalance,
} from '../../services/credit';
import SettingsModal from '../../components/SettingsModal';
import { useAppTheme } from '../../services/themeContext';

// ─── Colour palette — matches Android colors.xml ─────────────────────────────
const C = {
  headerBg:   '#09454A',
  green:      '#64B9A0',
  greenDark:  '#2ECC71',
  white:      '#FFFFFF',
  text:       '#212121',
  sub:        '#757575',
  bg:         '#F7F7F7',
  card:       '#FFFFFF',
  sep:        '#EEEEEE',
  coverBg:    '#1A6B72',
  coverAlt:   '#0D4F55',
  orange:     '#F07421',
  gold:       '#F5A623',
  goldBg:     '#FFF8EC',
  goldBorder: '#F5A62355',
  level:      'rgba(0,0,0,0.45)',
  menuBg:     '#2C2C2C',
  menuText:   '#FFFFFF',
  menuDiv:    '#3C3C3C',
  verified:   '#2ECC71',
};

// ─── Stat column ──────────────────────────────────────────────────────────────
function StatCol({
  label,
  value,
  onPress,
}: {
  label: string;
  value: string | number;
  onPress?: () => void;
}) {
  const theme = useAppTheme();
  return (
    <TouchableOpacity
      style={ss.statCol}
      onPress={onPress}
      activeOpacity={onPress ? 0.7 : 1}
      testID={`stat-${label.toLowerCase()}`}
    >
      <Text style={[ss.statLabel, { color: theme.textSecondary }]}>{label}</Text>
      <Text style={[ss.statValue, { color: theme.textPrimary }]}>{value}</Text>
    </TouchableOpacity>
  );
}

// ─── Credit card ──────────────────────────────────────────────────────────────
function CreditCard({ credit }: { credit: CreditBalance | null }) {
  if (!credit) return null;
  const currency = credit.currency;
  return (
    <View style={ss.creditCard} testID="card-credit">
      <View style={ss.creditHeader}>
        <Text style={ss.creditTitle}>
          {'Kredit'}
        </Text>
        <Text style={ss.creditBadge}>{currency}</Text>
      </View>
      <Text style={ss.creditAmount} testID="text-credit-main">{credit.formatted}</Text>
      <View style={ss.creditRow}>
        <View style={ss.creditItem}>
          <Text style={ss.creditItemLabel}>{currency}</Text>
          <Text style={ss.creditItemValue} testID="text-credit-balance">
            {formatCredit(credit.balance, currency)}
          </Text>
        </View>
        <View style={ss.creditDiv} />
        <View style={ss.creditItem}>
          <Text style={ss.creditItemLabel}>Terisi</Text>
          <Text style={ss.creditItemValue} testID="text-credit-funded">
            {formatCredit(credit.fundedBalance, currency)}
          </Text>
        </View>
      </View>
    </View>
  );
}

// ─── PrivilegeRow (one row in the level card) ─────────────────────────────────
// Mirrors Java's ReputationLevelData boolean flags displayed in Android settings
function PrivilegeRow({
  label, granted, detail,
}: { label: string; granted: boolean; detail?: string }) {
  return (
    <View style={pss.privRow}>
      <Ionicons
        name={granted ? 'checkmark-circle' : 'lock-closed-outline'}
        size={16}
        color={granted ? '#27AE60' : '#BDBDBD'}
        style={{ marginRight: 8 }}
      />
      <View style={{ flex: 1 }}>
        <Text style={[pss.privLabel, !granted && pss.privLabelLocked]}>{label}</Text>
        {detail && <Text style={pss.privDetail}>{detail}</Text>}
      </View>
    </View>
  );
}

const pss = StyleSheet.create({
  privRow:        { flexDirection: 'row', alignItems: 'flex-start', paddingVertical: 6 },
  privLabel:      { color: '#212121', fontSize: 13, fontWeight: '500' },
  privLabelLocked:{ color: '#BDBDBD' },
  privDetail:     { color: '#9E9E9E', fontSize: 11, marginTop: 1 },
});

// ─── Reputation Level (from /api/reputation/:username/level) ─────────────────
// Mirrors ReputationLevelData.java + LevelTable.java floor lookup
interface ReputationLevel {
  level: number;
  levelName: string;
  score: number;
  nextLevelAt: number | null;
  progressPct: number;
  privileges: {
    publishPhoto: boolean;
    postCommentLikeUserWall: boolean;
    addToPhotoWall: boolean;
    createChatRoom: boolean;
    createGroup: boolean;
    enterPot: boolean;
    chatRoomSize: number | null;
    groupSize: number | null;
  } | null;
}

interface FeedPost {
  id: string;
  authorUsername: string;
  comment: string;
  numLikes: number;
  numComments: number;
  createdAt: string;
}

interface ProfileData {
  user: { id: string; username: string; displayName: string };
  profile: {
    gender?: string | null;
    country?: string | null;
    city?: string | null;
    aboutMe?: string | null;
    displayPicture?: string | null;
    migLevel?: number;
  } | null;
  counts?: {
    posts?: number;
    followers?: number;
    following?: number;
    giftsReceived?: number;
    badges?: number;
  };
}

export default function ProfileScreen({ onClose }: { onClose?: () => void } = {}) {
  const theme = useAppTheme();
  const router = useRouter();
  const insets = useSafeAreaInsets();

  const [user, setUser]         = useState<AuthUser | null>(null);
  const [credit, setCredit]     = useState<CreditBalance | null>(null);
  const [profile, setProfile]   = useState<ProfileData | null>(null);
  const [repLevel, setRepLevel] = useState<ReputationLevel | null>(null);
  const [loading, setLoading]   = useState(true);
  const [showMenu, setShowMenu] = useState(false);
  const [showLevelCard, setShowLevelCard] = useState(false);
  const [settingsOpen, setSettingsOpen]   = useState(false);
  const [settingsPage, setSettingsPage]   = useState<'editProfile' | 'changeAvatar' | 'main'>('main');
  const [userPosts, setUserPosts]         = useState<FeedPost[]>([]);

  const buildHeaders = useCallback(async () => {
    const h: Record<string, string> = {};
    if (Platform.OS !== 'web') {
      const cookie = await getSession();
      if (cookie) h['Cookie'] = cookie;
    }
    return h;
  }, []);

  const fetchData = useCallback(async () => {
    setLoading(true);
    try {
      const me = await getMe();
      setUser(me);
      if (me?.username) {
        const [bal, headers] = await Promise.all([
          getCreditBalance(me.username),
          buildHeaders(),
        ]);
        setCredit(bal);
        const [profileRes, feedRes, repRes] = await Promise.all([
          fetch(`${API_BASE}/api/profile/me`, { credentials: 'include', headers }),
          fetch(`${API_BASE}/api/feed`, { credentials: 'include' }),
          fetch(`${API_BASE}/api/reputation/${me.username}`, { credentials: 'include' }),
        ]);
        if (profileRes.ok) {
          const data: ProfileData = await profileRes.json();
          setProfile(data);
        }
        if (feedRes.ok) {
          const feedData = await feedRes.json();
          const posts: FeedPost[] = Array.isArray(feedData) ? feedData : (feedData.posts ?? []);
          setUserPosts(posts.filter(p => p.authorUsername === me.username));
        }
        if (repRes.ok) {
          const repData = await repRes.json();
          setRepLevel({
            level:       repData.level,
            levelName:   repData.levelName,
            score:       repData.score,
            nextLevelAt: repData.nextLevelAt ?? null,
            progressPct: repData.progressPct ?? 0,
            privileges:  repData.levelPrivileges ?? null,
          });
        }
      }
    } catch {}
    setLoading(false);
  }, [buildHeaders]);

  useEffect(() => {
    fetchData();
  }, []);

  // Refresh profile data when settings modal closes (after edit)
  const prevSettingsOpen = useRef(settingsOpen);
  useEffect(() => {
    if (prevSettingsOpen.current && !settingsOpen) {
      fetchData();
    }
    prevSettingsOpen.current = settingsOpen;
  }, [settingsOpen, fetchData]);

  const handleLogout = () => {
    Alert.alert('Log Out', 'Yakin ingin keluar?', [
      { text: 'Batal', style: 'cancel' },
      {
        text: 'Log Out',
        style: 'destructive',
        onPress: async () => {
          await logout();
          router.replace('/');
        },
      },
    ]);
  };

  const openEditProfile = () => {
    setShowMenu(false);
    setSettingsPage('editProfile');
    setSettingsOpen(true);
  };

  const openChangeAvatar = () => {
    setShowMenu(false);
    setSettingsPage('changeAvatar');
    setSettingsOpen(true);
  };

  const openSettings = () => {
    setShowMenu(false);
    setSettingsPage('main');
    setSettingsOpen(true);
  };

  if (loading) {
    return (
      <View style={[ss.container, ss.center, { backgroundColor: theme.screenBg }]}>
        <ActivityIndicator color={theme.accent} size="large" />
      </View>
    );
  }

  const displayName = profile?.user?.displayName || user?.displayName || user?.username || 'Guest';
  const username    = user?.username ?? '';
  const migLevel    = profile?.profile?.migLevel ?? 1;
  const country     = profile?.profile?.country;
  const city        = profile?.profile?.city;
  const aboutMe     = profile?.profile?.aboutMe;
  const avatarUrl   = profile?.profile?.displayPicture;

  const locationLine = [country, city].filter(Boolean).join(', ');

  const counts    = profile?.counts;
  const posts     = counts?.posts     ?? 0;
  const badges    = counts?.badges    ?? 0;
  const gifts     = counts?.giftsReceived ?? 0;
  const fans      = counts?.followers ?? 0;
  const fanOf     = counts?.following ?? 0;

  const formatCount = (n: number) => {
    if (n >= 1000000) return `${(n / 1000000).toFixed(1)}M`;
    if (n >= 1000) return `${(n / 1000).toFixed(1)}K`;
    return String(n);
  };

  const timeAgo = (dateStr: string) => {
    const diff = Math.floor((Date.now() - new Date(dateStr).getTime()) / 1000);
    if (diff < 60) return 'just now';
    if (diff < 3600) return `${Math.floor(diff / 60)}m ago`;
    if (diff < 86400) return `${Math.floor(diff / 3600)}h ago`;
    return `${Math.floor(diff / 86400)}d ago`;
  };

  const menuItems = [
    { id: 'edit',     label: 'Edit profile',   icon: 'create-outline' as const },
    { id: 'avatar',   label: 'Change avatar',  icon: 'camera-outline' as const },
    { id: 'share',    label: 'Share profile',  icon: 'share-social-outline' as const },
    { id: 'settings', label: 'Settings',       icon: 'settings-outline' as const },
  ];

  const handleMenuSelect = (id: string) => {
    if (id === 'edit')     openEditProfile();
    else if (id === 'avatar')   openChangeAvatar();
    else if (id === 'settings') openSettings();
    else { setShowMenu(false); Alert.alert('Share', `migxchat.net/profile/${username}`); }
  };

  return (
    <View style={[ss.container, { backgroundColor: theme.screenBg }]}>
      <ScrollView
        style={ss.scroll}
        showsVerticalScrollIndicator={false}
      >
        {/* ── Cover photo area — mirrors @id/main_container in header_profile.xml ── */}
        <View style={[ss.cover, { backgroundColor: theme.headerBg, paddingTop: insets.top + 8 }]}>
          <View style={ss.coverDecor} />

          {/* back button when opened as modal */}
          {onClose && (
            <TouchableOpacity
              style={ss.backBtn}
              onPress={onClose}
              testID="button-profile-back"
            >
              <Ionicons name="arrow-back" size={22} color={C.white} />
            </TouchableOpacity>
          )}

          {/* @username label in cover */}
          <View style={ss.coverUsernameWrap}>
            <Text style={ss.coverUsername}>@{username}</Text>
          </View>

          {/* ⋮ more button */}
          <TouchableOpacity
            style={ss.moreBtn}
            onPress={() => setShowMenu(v => !v)}
            testID="button-profile-more"
          >
            <Ionicons name="ellipsis-vertical" size={20} color={C.white} />
          </TouchableOpacity>
        </View>

        {/* ── Profile info section — mirrors user_info_container in header_profile.xml ── */}
        <View style={[ss.infoSection, { backgroundColor: theme.cardBg, borderBottomColor: theme.divider }]}>
          {/* Avatar — overlapping the cover */}
          <View style={ss.avatarWrap}>
            {avatarUrl ? (
              <Image source={{ uri: avatarUrl }} style={ss.avatar} />
            ) : (
              <View style={[ss.avatar, ss.avatarDefault]}>
                <Text style={ss.avatarText}>{displayName.slice(0, 2).toUpperCase()}</Text>
              </View>
            )}
          </View>

          {/* Username row with verified checkmark */}
          <View style={ss.nameRow}>
            <View style={ss.nameBlock}>
              <View style={{ flexDirection: 'row', alignItems: 'center', gap: 4 }}>
                <Text style={[ss.displayName, { color: theme.textPrimary }]} testID="text-profile-displayname">
                  {displayName}
                </Text>
                <Ionicons name="checkmark-circle" size={16} color={theme.accent} />
              </View>
              <Text style={[ss.subLine, { color: theme.textSecondary }]} testID="text-profile-location">
                {[locationLine, `Level ${migLevel}`].filter(Boolean).join(', ')}
              </Text>
            </View>

            {/* >>> level progress — mirrors LevelProgressArrows — tap to show privileges */}
            <TouchableOpacity
              style={ss.levelArrows}
              onPress={() => setShowLevelCard(v => !v)}
              testID="button-level-progress"
            >
              <Text style={ss.levelArrowText}>{'>'.repeat(3)}</Text>
            </TouchableOpacity>
          </View>
        </View>

        {/* ── Stats bar — mirrors profile_main_container.xml ── */}
        <View style={[ss.statsBar, { backgroundColor: theme.cardBg, borderBottomColor: theme.divider }]}>
          <StatCol label="Posts"  value={formatCount(posts)}  />
          <View style={ss.statSep} />
          <StatCol label="Badges" value={formatCount(badges)} />
          <View style={ss.statSep} />
          <StatCol label="Gifts"  value={formatCount(gifts)}  />
          <View style={ss.statSep} />
          <StatCol label="Fans"   value={formatCount(fans)}   />
          <View style={ss.statSep} />
          <StatCol label="Fan of" value={formatCount(fanOf)}  />
        </View>

        {/* ── Level Privileges Card — tap >>> to toggle — mirrors ReputationLevelData ── */}
        {showLevelCard && repLevel && (
          <View style={ss.levelCard} testID="card-level-privileges">
            {/* Header row */}
            <View style={ss.levelCardHeader}>
              <View>
                <Text style={ss.levelCardTitle}>Level {repLevel.level} · {repLevel.levelName}</Text>
                <Text style={ss.levelCardScore}>{repLevel.score.toLocaleString()} XP</Text>
              </View>
              <TouchableOpacity onPress={() => setShowLevelCard(false)} testID="button-close-level-card">
                <Ionicons name="close" size={18} color={C.sub} />
              </TouchableOpacity>
            </View>

            {/* Progress bar to next level */}
            {repLevel.nextLevelAt !== null && (
              <View style={ss.levelProgressRow}>
                <View style={ss.levelProgressBg}>
                  <View style={[ss.levelProgressFill, { width: `${repLevel.progressPct}%` as any }]} />
                </View>
                <Text style={ss.levelProgressLabel}>
                  {repLevel.progressPct}% → Level {(repLevel.level) + 1} ({repLevel.nextLevelAt.toLocaleString()} XP)
                </Text>
              </View>
            )}

            {/* Privilege rows — mirrors ReputationLevelData boolean flags */}
            {repLevel.privileges && (
              <View style={ss.privilegeList}>
                <PrivilegeRow
                  label="Post, komentar & suka"
                  granted={repLevel.privileges.postCommentLikeUserWall}
                  detail="Berinteraksi di feed"
                />
                <PrivilegeRow
                  label="Upload foto ke postingan"
                  granted={repLevel.privileges.publishPhoto}
                  detail="Lampirkan foto ke post"
                />
                <PrivilegeRow
                  label="Tambah ke photo wall"
                  granted={repLevel.privileges.addToPhotoWall}
                  detail="Publikasi di photo wall"
                />
                <PrivilegeRow
                  label="Buat chatroom"
                  granted={repLevel.privileges.createChatRoom}
                  detail={repLevel.privileges.chatRoomSize ? `Maks. ${repLevel.privileges.chatRoomSize} anggota` : undefined}
                />
                <PrivilegeRow
                  label="Buat grup"
                  granted={repLevel.privileges.createGroup}
                  detail={repLevel.privileges.groupSize ? `Maks. ${repLevel.privileges.groupSize} anggota` : undefined}
                />
                <PrivilegeRow
                  label="Ikut pot berhadiah"
                  granted={repLevel.privileges.enterPot}
                />
              </View>
            )}
          </View>
        )}

        {/* ── About Me — mirrors ProfileProperty aboutMe ── */}
        {aboutMe ? (
          <View style={[ss.propSection, { backgroundColor: theme.cardBg, borderColor: theme.divider }]}>
            <Text style={[ss.propLabel, { color: theme.accent }]}>About me</Text>
            <Text style={[ss.propValue, { color: theme.textPrimary }]} testID="text-profile-about">{aboutMe}</Text>
          </View>
        ) : (
          <TouchableOpacity
            style={[ss.propSection, { backgroundColor: theme.cardBg, borderColor: theme.divider }]}
            onPress={openEditProfile}
            testID="button-add-bio"
          >
            <Text style={[ss.propLabel, { color: theme.accent }]}>About me</Text>
            <Text style={[ss.propValue, { color: theme.textSecondary, fontStyle: 'italic' }]}>
              Tambahkan bio...
            </Text>
          </TouchableOpacity>
        )}

        {/* ── Email ── */}
        <View style={[ss.propSection, { backgroundColor: theme.cardBg, borderColor: theme.divider }]}>
          <Text style={[ss.propLabel, { color: theme.accent }]}>Email</Text>
          <Text style={[ss.propValue, { color: theme.textPrimary }]} testID="text-profile-email">{user?.email ?? '—'}</Text>
        </View>

        {/* ── Posts feed ── */}
        {userPosts.length > 0 && (
          <View style={[ss.feedSection, { backgroundColor: theme.cardBg, borderColor: theme.divider }]}>
            {userPosts.map((post, idx) => (
              <View
                key={post.id}
                style={[ss.feedCard, idx < userPosts.length - 1 && ss.feedCardBorder]}
                testID={`card-post-${post.id}`}
              >
                {/* Author row */}
                <View style={ss.feedAuthorRow}>
                  <View style={ss.feedAvatar}>
                    {avatarUrl ? (
                      <Image source={{ uri: avatarUrl }} style={{ width: 36, height: 36, borderRadius: 18 }} resizeMode="cover" />
                    ) : (
                      <Text style={ss.feedAvatarText}>{displayName.slice(0, 2).toUpperCase()}</Text>
                    )}
                  </View>
                  <View style={{ flex: 1 }}>
                    <View style={{ flexDirection: 'row', alignItems: 'center', gap: 4 }}>
                      <Text style={ss.feedAuthorName}>{displayName}</Text>
                      <Ionicons name="checkmark-circle" size={13} color={C.verified} />
                    </View>
                    <Text style={ss.feedTime}>{timeAgo(post.createdAt)}</Text>
                  </View>
                </View>
                {/* Post text */}
                <Text style={ss.feedText} testID={`text-post-${post.id}`}>{post.comment}</Text>
                {/* Actions row */}
                <View style={ss.feedActions}>
                  <View style={ss.feedAction}>
                    <Ionicons name="heart-outline" size={15} color={C.sub} />
                    <Text style={ss.feedActionText}>{post.numLikes}</Text>
                  </View>
                  <View style={ss.feedAction}>
                    <Ionicons name="chatbubble-outline" size={15} color={C.sub} />
                    <Text style={ss.feedActionText}>{post.numComments}</Text>
                  </View>
                </View>
              </View>
            ))}
          </View>
        )}

        <View style={{ height: 40 }} />
      </ScrollView>

      {/* ── 3-dot dropdown menu ── */}
      {showMenu && (
        <TouchableWithoutFeedback onPress={() => setShowMenu(false)} testID="button-menu-overlay">
          <View style={ss.menuOverlay}>
            <View style={ss.menuSheet}>
              {menuItems.map((item, idx) => (
                <TouchableOpacity
                  key={item.id}
                  style={[ss.menuItem, idx < menuItems.length - 1 && ss.menuItemBorder]}
                  onPress={() => handleMenuSelect(item.id)}
                  testID={`button-menu-${item.id}`}
                >
                  <Ionicons name={item.icon} size={18} color={C.menuText} style={{ marginRight: 10 }} />
                  <Text style={ss.menuLabel}>{item.label}</Text>
                </TouchableOpacity>
              ))}
            </View>
          </View>
        </TouchableWithoutFeedback>
      )}

      {/* ── Settings / Edit Profile modal ── */}
      <SettingsModal
        visible={settingsOpen}
        onClose={() => setSettingsOpen(false)}
        onLogout={async () => {
          setSettingsOpen(false);
          handleLogout();
        }}
        onAvatarChange={fetchData}
        username={username || null}
        initialPage={settingsPage}
      />
    </View>
  );
}

const COVER_HEIGHT = 160;
const AVATAR_SIZE  = 76;

const ss = StyleSheet.create({
  container: { flex: 1, backgroundColor: C.bg },
  center:    { alignItems: 'center', justifyContent: 'center' },
  scroll:    { flex: 1 },

  // ── Cover photo ──────────────────────────────────────────────────────────
  cover: {
    height: COVER_HEIGHT,
    backgroundColor: C.coverBg,
    justifyContent: 'flex-end',
    overflow: 'hidden',
  },
  coverDecor: {
    ...StyleSheet.absoluteFillObject,
    backgroundColor: C.coverBg,
    // subtle two-tone stripe
    borderBottomWidth: 30,
    borderBottomColor: C.coverAlt,
  },
  coverUsernameWrap: {
    position: 'absolute',
    top: 40,
    right: 52,
    backgroundColor: 'rgba(0,0,0,0.35)',
    borderRadius: 6,
    paddingHorizontal: 10,
    paddingVertical: 4,
  },
  coverUsername: {
    color: C.white,
    fontSize: 13,
    fontWeight: '700',
  },
  moreBtn: {
    position: 'absolute',
    top: 36,
    right: 12,
    width: 36,
    height: 36,
    borderRadius: 6,
    borderWidth: 1.5,
    borderColor: 'rgba(255,255,255,0.6)',
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: 'rgba(255,255,255,0.1)',
  },
  backBtn: {
    position: 'absolute',
    top: 36,
    left: 12,
    width: 36,
    height: 36,
    borderRadius: 6,
    alignItems: 'center',
    justifyContent: 'center',
  },

  // ── Dropdown menu ─────────────────────────────────────────────────────────
  menuOverlay: {
    position: 'absolute',
    top: 0,
    left: 0,
    right: 0,
    bottom: 0,
    zIndex: 100,
  },
  menuSheet: {
    position: 'absolute',
    top: COVER_HEIGHT - 10,
    right: 12,
    backgroundColor: C.menuBg,
    borderRadius: 8,
    overflow: 'hidden',
    minWidth: 180,
    shadowColor: '#000',
    shadowOpacity: 0.25,
    shadowRadius: 8,
    shadowOffset: { width: 0, height: 4 },
    elevation: 8,
    zIndex: 101,
  },
  menuItem: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 16,
    paddingVertical: 13,
  },
  menuItemBorder: {
    borderBottomWidth: 1,
    borderBottomColor: C.menuDiv,
  },
  menuLabel: {
    color: C.menuText,
    fontSize: 14,
    fontWeight: '500',
  },

  // ── Profile info section ─────────────────────────────────────────────────
  infoSection: {
    backgroundColor: C.white,
    paddingBottom: 0,
    borderBottomWidth: 1,
    borderBottomColor: C.sep,
  },
  avatarWrap: {
    marginTop: -(AVATAR_SIZE / 2),
    marginLeft: 16,
    width: AVATAR_SIZE,
    height: AVATAR_SIZE,
    borderRadius: AVATAR_SIZE / 2,
    borderWidth: 3,
    borderColor: C.white,
    shadowColor: '#000',
    shadowOpacity: 0.15,
    shadowRadius: 6,
    shadowOffset: { width: 0, height: 2 },
    elevation: 4,
    overflow: 'hidden',
  },
  avatar: {
    width: '100%',
    height: '100%',
    borderRadius: AVATAR_SIZE / 2,
  },
  avatarDefault: {
    backgroundColor: C.coverBg,
    alignItems: 'center',
    justifyContent: 'center',
  },
  avatarText: {
    color: C.white,
    fontSize: 26,
    fontWeight: 'bold',
  },
  nameRow: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 16,
    paddingTop: 8,
    paddingBottom: 14,
  },
  nameBlock: { flex: 1 },
  displayName: {
    color: C.text,
    fontSize: 17,
    fontWeight: 'bold',
  },
  subLine: {
    color: C.sub,
    fontSize: 12,
    marginTop: 2,
  },
  levelArrows: {
    paddingHorizontal: 10,
    paddingVertical: 6,
  },
  levelArrowText: {
    color: C.green,
    fontSize: 14,
    fontWeight: '700',
    letterSpacing: 1,
  },

  // ── Stats bar — mirrors profile_main_container.xml ───────────────────────
  statsBar: {
    flexDirection: 'row',
    backgroundColor: C.white,
    borderBottomWidth: 1,
    borderBottomColor: C.sep,
    paddingVertical: 14,
  },
  statCol: {
    flex: 1,
    alignItems: 'center',
  },
  statLabel: {
    color: C.green,
    fontSize: 11,
    fontWeight: '600',
    marginBottom: 2,
  },
  statValue: {
    color: C.text,
    fontSize: 17,
    fontWeight: 'bold',
  },
  statSep: {
    width: 1,
    backgroundColor: C.sep,
    marginVertical: 4,
  },

  // ── Credit card ──────────────────────────────────────────────────────────
  creditCard: {
    backgroundColor: C.goldBg,
    borderRadius: 10,
    borderWidth: 1,
    borderColor: C.goldBorder,
    padding: 14,
  },
  creditHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 6,
  },
  creditTitle: { color: C.text, fontSize: 12, fontWeight: '700', textTransform: 'uppercase', letterSpacing: 0.5 },
  creditBadge: {
    backgroundColor: '#27AE60',
    color: C.white,
    fontSize: 10,
    fontWeight: 'bold',
    paddingHorizontal: 7,
    paddingVertical: 2,
    borderRadius: 6,
    overflow: 'hidden',
  },
  creditAmount: { color: C.gold, fontSize: 28, fontWeight: 'bold', marginBottom: 8 },
  creditRow: {
    flexDirection: 'row',
    backgroundColor: '#FFF3DC',
    borderRadius: 6,
    padding: 8,
  },
  creditItem: { flex: 1, alignItems: 'center' },
  creditItemLabel: { color: C.sub, fontSize: 10, fontWeight: '700', textTransform: 'uppercase', marginBottom: 2 },
  creditItemValue: { color: C.text, fontSize: 13, fontWeight: 'bold' },
  creditDiv: { width: 1, backgroundColor: C.goldBorder, marginHorizontal: 6 },

  // ── Property sections — mirrors ProfileProperty widget ───────────────────
  propSection: {
    backgroundColor: C.white,
    paddingHorizontal: 16,
    paddingVertical: 12,
    marginTop: 8,
    borderTopWidth: 1,
    borderBottomWidth: 1,
    borderColor: C.sep,
  },
  propLabel: {
    color: C.green,
    fontSize: 10,
    fontWeight: '700',
    textTransform: 'uppercase',
    letterSpacing: 0.5,
    marginBottom: 4,
  },
  propValue: { color: C.text, fontSize: 14, lineHeight: 20 },

  // ── Level Privileges Card (mirrors ReputationLevelData display) ─────────────
  levelCard: {
    backgroundColor: C.card, margin: 12, marginTop: 0, borderRadius: 10,
    padding: 14, borderWidth: 1, borderColor: C.sep,
    shadowColor: '#000', shadowOpacity: 0.06, shadowRadius: 4,
    shadowOffset: { width: 0, height: 2 }, elevation: 2,
  },
  levelCardHeader: {
    flexDirection: 'row', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 10,
  },
  levelCardTitle:      { color: C.text, fontSize: 15, fontWeight: '700' },
  levelCardScore:      { color: C.green, fontSize: 12, marginTop: 2 },
  levelProgressRow:    { marginBottom: 10 },
  levelProgressBg:     { height: 6, backgroundColor: C.sep, borderRadius: 3, overflow: 'hidden', marginBottom: 4 },
  levelProgressFill:   { height: 6, backgroundColor: C.green, borderRadius: 3 },
  levelProgressLabel:  { color: C.sub, fontSize: 11 },
  privilegeList:       { borderTopWidth: 1, borderTopColor: C.sep, paddingTop: 8 },

  // ── Feed section ─────────────────────────────────────────────────────────
  feedSection: {
    marginTop: 8,
    backgroundColor: C.white,
    borderTopWidth: 1,
    borderBottomWidth: 1,
    borderColor: C.sep,
  },
  feedCard: {
    paddingHorizontal: 16,
    paddingVertical: 12,
  },
  feedCardBorder: {
    borderBottomWidth: 1,
    borderBottomColor: C.sep,
  },
  feedAuthorRow: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 8,
    gap: 10,
  },
  feedAvatar: {
    width: 38,
    height: 38,
    borderRadius: 19,
    backgroundColor: C.headerBg,
    alignItems: 'center',
    justifyContent: 'center',
  },
  feedAvatarText: {
    color: C.white,
    fontSize: 13,
    fontWeight: 'bold',
  },
  feedAuthorName: {
    color: C.text,
    fontSize: 13,
    fontWeight: '700',
  },
  feedTime: {
    color: C.sub,
    fontSize: 11,
    marginTop: 1,
  },
  feedText: {
    color: C.text,
    fontSize: 14,
    lineHeight: 20,
    marginBottom: 10,
  },
  feedActions: {
    flexDirection: 'row',
    gap: 16,
  },
  feedAction: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 4,
  },
  feedActionText: {
    color: C.sub,
    fontSize: 12,
  },
});
