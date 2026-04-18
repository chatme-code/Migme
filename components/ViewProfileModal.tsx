import { useCallback, useEffect, useState } from 'react';
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
import { Ionicons, MaterialIcons, FontAwesome5 } from '@expo/vector-icons';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { API_BASE } from '../services/auth';
import { getSession } from '../services/storage';
import { useAppTheme } from '../services/themeContext';

// ─── Colour palette — mirrors fragment_profile_popup.xml & colors.xml ───────
const C = {
  headerBg:     '#09454A',
  green:        '#2ECC71',
  defaultText:  '#212121',
  grayBg:       '#F0F0F0',
  white:        '#FFFFFF',
  shadow:       '#CCCCCC',
  ts:           '#999999',
  danger:       '#E53935',
  gold:         '#F59E0B',
  levelBg:      'rgba(0,0,0,0.55)',
  coverDefault: '#09454A',
  menuBg:       '#2C2C2C',
  menuItem:     '#FFFFFF',
  menuDivider:  '#444444',
  private:      '#FF8800',
};

// Relationship status labels — mirrors RelationshipStatus.java
const RELATIONSHIP_LABELS: Record<number, string> = {
  1: 'Single',
  2: 'In a relationship',
  3: 'Engaged',
  4: 'Married',
  5: "It's complicated",
  6: 'Open relationship',
  7: 'Widowed',
};

// Gender labels — mirrors GenderEnum.java
const GENDER_LABELS: Record<string, string> = {
  male:   'Male',
  female: 'Female',
  other:  'Other',
};

interface ProfileData {
  user: {
    id: string;
    username: string;
    displayName: string;
  };
  profile: {
    gender?: string | null;
    dateOfBirth?: string | null;
    country?: string | null;
    city?: string | null;
    aboutMe?: string | null;
    likes?: string | null;
    dislikes?: string | null;
    relationshipStatus?: number | null;
    displayPicture?: string | null;
    migLevel?: number;
    profileStatus?: number;
  } | null;
  isOwner?: boolean;
  isPrivate?: boolean;
  counts?: {
    followers: number;
    giftsReceived: number;
    badges: number;
  };
}

// ─── Merchant tag data (mirrors MerchantTagData.TypeEnum) ───────────────────
interface MerchantTagInfo {
  id: string;
  type: number;     // 1=TOP_MERCHANT_TAG  2=NON_TOP_MERCHANT_TAG
  status: number;   // 1=ACTIVE
  amount: number | null;
  currency: string | null;
  expiresAt: string | null;
  merchantUsername: string;
}

function tagTypeLabel(type: number) {
  return type === 1 ? { label: 'TOP TAG', bg: '#FF6F00', text: '#FFFFFF' }
                    : { label: 'TAG', bg: '#64B9A0', text: '#FFFFFF' };
}

interface Props {
  visible: boolean;
  username: string;
  displayName?: string;
  avatarColor?: string;
  currentUserId?: string | null;
  onClose: () => void;
  onSendGift?: (username: string) => void;
  onPrivateChat?: (username: string, displayName: string) => void;
  isFollowing?: boolean;
  isBlocked?: boolean;
  onFollow?: (username: string) => void;
  onUnfollow?: (username: string) => void;
  onBlock?: (username: string) => void;
  onUnblock?: (username: string) => void;
  onTransferCredit?: (username: string) => void;
}

// ─── Profile property row — mirrors ProfileProperty widget in Java ───────────
function ProfileRow({
  icon,
  label,
  value,
}: {
  icon: React.ReactNode;
  label: string;
  value: string | null | undefined;
}) {
  const theme = useAppTheme();
  if (!value) return null;
  return (
    <View style={[styles.propRow, { borderBottomColor: theme.divider }]}>
      <View style={styles.propIcon}>{icon}</View>
      <View style={styles.propContent}>
        <Text style={[styles.propLabel, { color: theme.textSecondary }]}>{label}</Text>
        <Text style={[styles.propValue, { color: theme.textPrimary }]}>{value}</Text>
      </View>
    </View>
  );
}

// ─── Stat column — mirrors UserMiniDetails (gifts, badges, fans) ─────────────
function StatCol({
  value,
  label,
  icon,
  onPress,
}: {
  value: number;
  label: string;
  icon: React.ReactNode;
  onPress?: () => void;
}) {
  const theme = useAppTheme();
  return (
    <TouchableOpacity
      style={styles.statCol}
      onPress={onPress}
      activeOpacity={onPress ? 0.7 : 1}
      testID={`stat-${label.toLowerCase()}`}
    >
      {icon}
      <Text style={[styles.statValue, { color: theme.textPrimary }]}>{value}</Text>
      <Text style={[styles.statLabel, { color: theme.textSecondary }]}>{label}</Text>
    </TouchableOpacity>
  );
}

export default function ViewProfileModal({
  visible,
  username,
  displayName,
  avatarColor = '#09454A',
  currentUserId,
  onClose,
  onSendGift,
  onPrivateChat,
  isFollowing: initialFollowing = false,
  isBlocked: initialBlocked = false,
  onFollow,
  onUnfollow,
  onBlock,
  onUnblock,
  onTransferCredit,
}: Props) {
  const insets = useSafeAreaInsets();
  const theme = useAppTheme();

  const [data, setData]             = useState<ProfileData | null>(null);
  const [loading, setLoading]       = useState(false);
  const [error, setError]           = useState<string | null>(null);
  const [showMenu, setShowMenu]     = useState(false);
  const [isFollowing, setIsFollowing] = useState(initialFollowing);
  const [isBlocked, setIsBlocked]     = useState(initialBlocked);
  const [merchantTag, setMerchantTag] = useState<MerchantTagInfo | null>(null);

  const buildHeaders = useCallback(async () => {
    const h: Record<string, string> = {};
    if (Platform.OS !== 'web') {
      const cookie = await getSession();
      if (cookie) h['Cookie'] = cookie;
    }
    return h;
  }, []);

  const fetchProfile = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const headers = await buildHeaders();
      const [profileRes, tagRes] = await Promise.all([
        fetch(`${API_BASE}/api/profile/${encodeURIComponent(username)}`, { credentials: 'include', headers }),
        fetch(`${API_BASE}/api/merchant-tags/tag/${encodeURIComponent(username)}`, { credentials: 'include', headers }),
      ]);
      if (!profileRes.ok) throw new Error('Gagal memuat profil');
      const json: ProfileData = await profileRes.json();
      setData(json);
      if (tagRes.ok) {
        const tagJson = await tagRes.json();
        setMerchantTag(tagJson.tag ?? null);
      }
    } catch (e: any) {
      setError(e.message ?? 'Terjadi kesalahan');
    } finally {
      setLoading(false);
    }
  }, [username, buildHeaders]);

  useEffect(() => {
    if (visible && username) {
      setData(null);
      setMerchantTag(null);
      setIsFollowing(initialFollowing);
      setIsBlocked(initialBlocked);
      fetchProfile();
    }
  }, [visible, username]);

  const handleFollow = useCallback(async () => {
    try {
      const headers = await buildHeaders();
      if (isFollowing) {
        await fetch(`${API_BASE}/api/users/${username}/follow`, {
          method: 'DELETE', credentials: 'include', headers,
        });
        setIsFollowing(false);
        onUnfollow?.(username);
      } else {
        await fetch(`${API_BASE}/api/users/${username}/follow`, {
          method: 'POST', credentials: 'include', headers: { ...headers, 'Content-Type': 'application/json' },
        });
        setIsFollowing(true);
        onFollow?.(username);
      }
    } catch {
      Alert.alert('Gagal', 'Tidak dapat memperbarui status follow.');
    }
  }, [isFollowing, username, buildHeaders, onFollow, onUnfollow]);

  const handleBlock = useCallback(async () => {
    const name = data?.user.displayName || username;
    Alert.alert(
      isBlocked ? `Unblock ${name}?` : `Block ${name}?`,
      isBlocked
        ? `${name} akan bisa melihat kamu lagi.`
        : `${name} tidak akan bisa mengirim pesan ke kamu.`,
      [
        { text: 'Batal', style: 'cancel' },
        {
          text: isBlocked ? 'Unblock' : 'Block',
          style: 'destructive',
          onPress: async () => {
            try {
              const headers = await buildHeaders();
              if (isBlocked) {
                await fetch(`${API_BASE}/api/users/${username}/block`, {
                  method: 'DELETE', credentials: 'include', headers,
                });
                setIsBlocked(false);
                onUnblock?.(username);
              } else {
                await fetch(`${API_BASE}/api/users/${username}/block`, {
                  method: 'POST', credentials: 'include',
                  headers: { ...headers, 'Content-Type': 'application/json' },
                });
                setIsBlocked(true);
                onBlock?.(username);
                onClose();
              }
            } catch {
              Alert.alert('Gagal', 'Tidak dapat memperbarui status blokir.');
            }
          },
        },
      ],
    );
  }, [isBlocked, username, data, buildHeaders, onBlock, onUnblock, onClose]);

  const handleReport = useCallback(async () => {
    Alert.alert('Report', `Laporkan ${username}?`, [
      { text: 'Batal', style: 'cancel' },
      {
        text: 'Laporkan',
        style: 'destructive',
        onPress: async () => {
          try {
            const headers = await buildHeaders();
            await fetch(`${API_BASE}/api/users/${username}/report`, {
              method: 'POST', credentials: 'include',
              headers: { ...headers, 'Content-Type': 'application/json' },
              body: JSON.stringify({ reason: 'Reported from chat room' }),
            });
            Alert.alert('Terima kasih', 'Laporan kamu telah diterima.');
          } catch {
            Alert.alert('Gagal', 'Tidak dapat mengirim laporan.');
          }
        },
      },
    ]);
  }, [username, buildHeaders]);

  const profile  = data?.profile;
  const userInfo = data?.user;
  const counts   = data?.counts;
  const isOwner  = data?.isOwner;
  const isPrivate = data?.isPrivate;

  const initial = (displayName || username).charAt(0).toUpperCase();
  const migLevel = profile?.migLevel ?? 1;

  // ── Popup menu items — mirrors MiniProfilePopupFragment.getProfileMenuOptions() ──
  interface MenuOption { id: string; label: string; icon: React.ReactNode; danger?: boolean }
  const menuOptions: MenuOption[] = isOwner
    ? [
        { id: 'edit', label: 'Edit profile', icon: <Ionicons name="create-outline" size={18} color="#FFFFFF" /> },
        { id: 'share', label: 'Share profile', icon: <Ionicons name="share-social-outline" size={18} color="#FFFFFF" /> },
      ]
    : [
        { id: 'send_gift', label: 'Send gift', icon: <Ionicons name="gift-outline" size={18} color="#FFFFFF" /> },
        { id: 'transfer', label: 'Transfer credit', icon: <Ionicons name="swap-horizontal-outline" size={18} color="#FFFFFF" /> },
        { id: 'share', label: 'Share profile', icon: <Ionicons name="share-social-outline" size={18} color="#FFFFFF" /> },
        { id: 'report', label: 'Report abuse', icon: <MaterialIcons name="report" size={18} color="#FFFFFF" /> },
        {
          id: 'block',
          label: isBlocked ? 'Unblock' : 'Block',
          icon: <MaterialIcons name="block" size={18} color={isBlocked ? '#FFFFFF' : C.danger} />,
          danger: !isBlocked,
        },
      ];

  const handleMenuSelect = useCallback((id: string) => {
    setShowMenu(false);
    switch (id) {
      case 'send_gift':
        onSendGift?.(username);
        onClose();
        break;
      case 'report':
        handleReport();
        break;
      case 'block':
        handleBlock();
        break;
      case 'share':
        Alert.alert('Share', `migxchat.net/profile/${username}`);
        break;
      case 'edit':
        Alert.alert('Edit Profile', 'Buka halaman edit profil.');
        break;
      case 'transfer':
        onClose();
        onTransferCredit?.(username);
        break;
    }
  }, [username, onSendGift, onClose, handleReport, handleBlock]);

  return (
    <Modal
      visible={visible}
      animationType="slide"
      transparent
      onRequestClose={onClose}
      statusBarTranslucent
    >
      <TouchableWithoutFeedback onPress={onClose}>
        <View style={styles.overlay}>
          <TouchableWithoutFeedback>
            <View style={[styles.sheet, { backgroundColor: theme.cardBg, paddingBottom: insets.bottom + 8 }]}>
              {/* ── Cover photo area — mirrors @id/main_container in fragment_profile_popup.xml ── */}
              <View style={styles.cover}>
                {profile?.displayPicture ? (
                  <Image
                    source={{ uri: profile.displayPicture }}
                    style={styles.coverImg}
                    resizeMode="cover"
                  />
                ) : (
                  <View style={[styles.coverImg, { backgroundColor: avatarColor }]} />
                )}

                {/* More options button (⋮) — mirrors @id/more_options_button */}
                <TouchableOpacity
                  style={styles.moreBtn}
                  onPress={() => setShowMenu(v => !v)}
                  testID="button-profile-more"
                >
                  <Ionicons name="ellipsis-vertical" size={20} color="#FFFFFF" />
                </TouchableOpacity>

                {/* Close button */}
                <TouchableOpacity
                  style={styles.closeBtn}
                  onPress={onClose}
                  testID="button-profile-close"
                >
                  <Ionicons name="close" size={20} color="#FFFFFF" />
                </TouchableOpacity>

                {/* mig Level badge — mirrors MigLevelView */}
                <View style={styles.levelBadge}>
                  <Text style={styles.levelText}>Lv {migLevel}</Text>
                </View>
              </View>

              {/* Popup menu overlay — mirrors PopupMenu.java */}
              {showMenu && (
                <TouchableWithoutFeedback onPress={() => setShowMenu(false)}>
                  <View style={styles.menuOverlay}>
                    <TouchableWithoutFeedback>
                      <View style={styles.menuSheet}>
                        {menuOptions.map((opt, i) => (
                          <TouchableOpacity
                            key={opt.id}
                            style={[styles.menuItem, i < menuOptions.length - 1 && styles.menuItemBorder]}
                            onPress={() => handleMenuSelect(opt.id)}
                            testID={`button-profile-menu-${opt.id}`}
                          >
                            <View style={styles.menuIconWrap}>{opt.icon}</View>
                            <Text style={[styles.menuLabel, opt.danger && styles.menuLabelDanger]}>
                              {opt.label}
                            </Text>
                          </TouchableOpacity>
                        ))}
                      </View>
                    </TouchableWithoutFeedback>
                  </View>
                </TouchableWithoutFeedback>
              )}

              {/* ── User info row — mirrors @id/user_info_container ── */}
              <View style={[styles.userRow, { backgroundColor: theme.cardBg }]}>
                {/* Avatar — mirrors UserImageView (@id/user_image) */}
                <View style={[styles.avatar, { backgroundColor: avatarColor, borderColor: theme.cardBg }]}>
                  {profile?.displayPicture ? (
                    <Image source={{ uri: profile.displayPicture }} style={styles.avatarImg} />
                  ) : (
                    <Text style={styles.avatarInitial}>{initial}</Text>
                  )}
                </View>

                <View style={styles.userMeta}>
                  {/* Username — mirrors @id/username with textColor=default_green */}
                  <Text style={[styles.username, { color: theme.accent }]} testID="text-profile-username">
                    {userInfo?.username || username}
                  </Text>

                  {/* Display name */}
                  {userInfo?.displayName && userInfo.displayName !== userInfo.username && (
                    <Text style={[styles.displayName, { color: theme.textPrimary }]} testID="text-profile-displayname">
                      {userInfo.displayName}
                    </Text>
                  )}

                  {/* Level + country line — mirrors UserBasicDetails */}
                  <Text style={[styles.userSubtext, { color: theme.textSecondary }]} testID="text-profile-details">
                    {[
                      `Level ${migLevel}`,
                      profile?.country,
                      profile?.city,
                    ].filter(Boolean).join(' · ')}
                  </Text>

                  {/* ── Merchant Tag badge — mirrors MerchantTagData display ── */}
                  {merchantTag && merchantTag.status === 1 && (() => {
                    const meta = tagTypeLabel(merchantTag.type);
                    return (
                      <View style={styles.tagBadgeRow} testID="view-merchant-tag">
                        <View style={[styles.tagPill, { backgroundColor: meta.bg }]}>
                          <Text style={[styles.tagPillText, { color: meta.text }]}>{meta.label}</Text>
                        </View>
                        <Text style={[styles.tagMerchant, { color: theme.textSecondary }]} numberOfLines={1}>
                          @{merchantTag.merchantUsername}
                        </Text>
                        {merchantTag.amount != null && (
                          <Text style={[styles.tagAmount, { color: theme.textPrimary }]}>
                            {merchantTag.currency ?? 'USD'} {Number(merchantTag.amount).toLocaleString(undefined, { minimumFractionDigits: 0 })}
                          </Text>
                        )}
                      </View>
                    );
                  })()}
                </View>
              </View>

              {/* ── Content scroll — mirrors ScrollView fillViewport ── */}
              <ScrollView
                style={styles.scroll}
                contentContainerStyle={styles.scrollContent}
                showsVerticalScrollIndicator={false}
                testID="scroll-profile-content"
              >
                {loading && (
                  <View style={styles.loadingWrap}>
                    <ActivityIndicator size="large" color={theme.accent} />
                    <Text style={[styles.loadingText, { color: theme.textSecondary }]}>Memuat profil…</Text>
                  </View>
                )}

                {error && !loading && (
                  <View style={styles.errorWrap}>
                    <MaterialIcons name="error-outline" size={32} color={theme.textSecondary} />
                    <Text style={[styles.errorText, { color: theme.textSecondary }]}>{error}</Text>
                    <TouchableOpacity onPress={fetchProfile} style={[styles.retryBtn, { backgroundColor: theme.accent }]} testID="button-profile-retry">
                      <Text style={styles.retryText}>Coba lagi</Text>
                    </TouchableOpacity>
                  </View>
                )}

                {isPrivate && !loading && (
                  <View style={styles.privateWrap}>
                    <Ionicons name="lock-closed-outline" size={36} color={C.private} />
                    <Text style={styles.privateText}>Profil ini privat</Text>
                  </View>
                )}

                {!loading && !error && profile && (
                  <>
                    {/* ── Stats row — mirrors UserMiniDetails (gifts, badges, fans) ── */}
                    <View style={[styles.statsRow, { backgroundColor: theme.cardBg }]}>
                      <View style={styles.statsDivider} />
                      <StatCol
                        value={counts?.giftsReceived ?? 0}
                        label="Gifts"
                        icon={<Ionicons name="gift-outline" size={22} color={C.gold} />}
                        onPress={() => Alert.alert('Gifts', `${counts?.giftsReceived ?? 0} gift diterima`)}
                      />
                      <View style={[styles.statSep, { backgroundColor: theme.divider }]} />
                      <StatCol
                        value={counts?.badges ?? 0}
                        label="Badges"
                        icon={<FontAwesome5 name="medal" size={20} color={C.gold} />}
                        onPress={() => Alert.alert('Badges', `${counts?.badges ?? 0} badge`)}
                      />
                      <View style={[styles.statSep, { backgroundColor: theme.divider }]} />
                      <StatCol
                        value={counts?.followers ?? 0}
                        label="Fans"
                        icon={<Ionicons name="people-outline" size={22} color={theme.accent} />}
                        onPress={() => Alert.alert('Fans', `${counts?.followers ?? 0} fans`)}
                      />
                      <View style={styles.statsDivider} />
                    </View>

                    <View style={[styles.shadowLine, { backgroundColor: theme.divider }]} />

                    {/* ── Profile properties — mirrors fragment_full_profile.xml ProfileProperty items ── */}
                    <View style={styles.propSection}>
                      {profile.aboutMe && (
                        <View style={[styles.aboutBox, { backgroundColor: theme.inputBg }]}>
                          <Text style={[styles.aboutLabel, { color: theme.textSecondary }]}>About Me</Text>
                          <Text style={[styles.aboutValue, { color: theme.textPrimary }]} testID="text-profile-about">
                            {profile.aboutMe}
                          </Text>
                        </View>
                      )}

                      <ProfileRow
                        icon={<Ionicons name="person-outline" size={16} color={theme.textSecondary} />}
                        label="Gender"
                        value={profile.gender ? GENDER_LABELS[profile.gender] ?? profile.gender : null}
                      />
                      <ProfileRow
                        icon={<Ionicons name="heart-outline" size={16} color={theme.textSecondary} />}
                        label="Relationship"
                        value={
                          profile.relationshipStatus
                            ? RELATIONSHIP_LABELS[profile.relationshipStatus] ?? null
                            : null
                        }
                      />
                      <ProfileRow
                        icon={<Ionicons name="calendar-outline" size={16} color={theme.textSecondary} />}
                        label="Birthday"
                        value={profile.dateOfBirth ? formatDate(profile.dateOfBirth) : null}
                      />
                      <ProfileRow
                        icon={<Ionicons name="location-outline" size={16} color={theme.textSecondary} />}
                        label="Location"
                        value={[profile.city, profile.country].filter(Boolean).join(', ') || null}
                      />
                      {profile.likes && (
                        <ProfileRow
                          icon={<Ionicons name="thumbs-up-outline" size={16} color={theme.textSecondary} />}
                          label="Likes"
                          value={profile.likes}
                        />
                      )}
                      {profile.dislikes && (
                        <ProfileRow
                          icon={<Ionicons name="thumbs-down-outline" size={16} color={theme.textSecondary} />}
                          label="Dislikes"
                          value={profile.dislikes}
                        />
                      )}
                    </View>
                  </>
                )}
              </ScrollView>

              {/* ── Action buttons — mirrors send_gifts + start_chat in fragment_profile_popup.xml ── */}
              {!loading && !isPrivate && !isOwner && (
                <View style={[styles.actionRow, { backgroundColor: theme.cardBg, borderTopColor: theme.divider }]}>
                  <TouchableOpacity
                    style={styles.actionBtn}
                    onPress={() => {
                      onSendGift?.(username);
                      onClose();
                    }}
                    testID="button-profile-send-gift"
                  >
                    <Ionicons name="gift-outline" size={22} color={C.gold} />
                    <Text style={[styles.actionLabel, { color: theme.textPrimary }]}>Send Gift</Text>
                  </TouchableOpacity>

                  <View style={[styles.actionSep, { backgroundColor: theme.divider }]} />

                  <TouchableOpacity
                    style={styles.actionBtn}
                    onPress={handleFollow}
                    testID="button-profile-follow"
                  >
                    <Ionicons
                      name={isFollowing ? 'person-remove-outline' : 'person-add-outline'}
                      size={22}
                      color={isFollowing ? theme.textSecondary : theme.accent}
                    />
                    <Text style={[styles.actionLabel, { color: isFollowing ? theme.textSecondary : theme.accent }]}>
                      {isFollowing ? 'Unfollow' : 'Add Fan'}
                    </Text>
                  </TouchableOpacity>

                  <View style={[styles.actionSep, { backgroundColor: theme.divider }]} />

                  <TouchableOpacity
                    style={styles.actionBtn}
                    onPress={() => {
                      const name = userInfo?.displayName || username;
                      onPrivateChat?.(username, name);
                      onClose();
                    }}
                    testID="button-profile-chat"
                  >
                    <Ionicons name="chatbubble-outline" size={22} color={theme.accent} />
                    <Text style={[styles.actionLabel, { color: theme.textPrimary }]}>Chat</Text>
                  </TouchableOpacity>
                </View>
              )}
            </View>
          </TouchableWithoutFeedback>
        </View>
      </TouchableWithoutFeedback>
    </Modal>
  );
}

function formatDate(iso: string): string {
  try {
    const d = new Date(iso);
    return d.toLocaleDateString('id-ID', { day: 'numeric', month: 'long', year: 'numeric' });
  } catch {
    return iso;
  }
}

const styles = StyleSheet.create({
  overlay: {
    flex: 1,
    backgroundColor: 'rgba(0,0,0,0.5)',
    justifyContent: 'flex-end',
  },
  sheet: {
    backgroundColor: C.white,
    borderTopLeftRadius: 20,
    borderTopRightRadius: 20,
    overflow: 'hidden',
    maxHeight: '90%',
  },

  // ── Cover ──────────────────────────────────────────────────────────────────
  cover: {
    height: 140,
    position: 'relative',
    overflow: 'hidden',
    backgroundColor: C.coverDefault,
  },
  coverImg: {
    position: 'absolute',
    inset: 0,
    width: '100%',
    height: '100%',
  },
  moreBtn: {
    position: 'absolute',
    top: 12,
    right: 48,
    width: 36,
    height: 36,
    borderRadius: 18,
    backgroundColor: 'rgba(0,0,0,0.45)',
    alignItems: 'center',
    justifyContent: 'center',
  },
  closeBtn: {
    position: 'absolute',
    top: 12,
    right: 8,
    width: 36,
    height: 36,
    borderRadius: 18,
    backgroundColor: 'rgba(0,0,0,0.45)',
    alignItems: 'center',
    justifyContent: 'center',
  },
  levelBadge: {
    position: 'absolute',
    bottom: 10,
    left: 12,
    backgroundColor: C.levelBg,
    borderRadius: 12,
    paddingHorizontal: 8,
    paddingVertical: 2,
  },
  levelText: {
    color: C.white,
    fontSize: 11,
    fontWeight: '700',
  },

  // ── User row ───────────────────────────────────────────────────────────────
  userRow: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 16,
    paddingVertical: 12,
    backgroundColor: C.white,
  },
  avatar: {
    width: 64,
    height: 64,
    borderRadius: 32,
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 12,
    borderWidth: 3,
    borderColor: C.white,
    overflow: 'hidden',
    marginTop: -20,
    elevation: 3,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.2,
    shadowRadius: 4,
  },
  avatarImg: {
    width: '100%',
    height: '100%',
  },
  avatarInitial: {
    color: C.white,
    fontSize: 26,
    fontWeight: '700',
  },
  userMeta: {
    flex: 1,
  },
  username: {
    fontSize: 18,
    fontWeight: '700',
    color: C.green,
  },
  displayName: {
    fontSize: 13,
    color: C.defaultText,
    marginTop: 1,
  },
  userSubtext: {
    fontSize: 12,
    color: C.ts,
    marginTop: 3,
  },

  // ── Scroll ────────────────────────────────────────────────────────────────
  scroll: {
    maxHeight: 360,
  },
  scrollContent: {
    paddingBottom: 8,
  },

  // ── Stats — mirrors UserMiniDetails ───────────────────────────────────────
  statsRow: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: 10,
    backgroundColor: C.white,
  },
  statsDivider: {
    flex: 1,
  },
  statSep: {
    width: 1,
    height: 36,
    backgroundColor: C.shadow,
  },
  statCol: {
    flex: 3,
    alignItems: 'center',
    paddingVertical: 4,
  },
  statValue: {
    fontSize: 16,
    fontWeight: '700',
    color: C.defaultText,
    marginTop: 2,
  },
  statLabel: {
    fontSize: 10,
    color: C.ts,
    marginTop: 1,
  },
  shadowLine: {
    height: 1,
    backgroundColor: C.grayBg,
    marginHorizontal: 0,
  },

  // ── Profile properties — mirrors ProfileProperty widget ───────────────────
  propSection: {
    paddingHorizontal: 16,
    paddingTop: 12,
  },
  aboutBox: {
    backgroundColor: C.grayBg,
    borderRadius: 8,
    padding: 12,
    marginBottom: 12,
  },
  aboutLabel: {
    fontSize: 11,
    fontWeight: '600',
    color: C.ts,
    textTransform: 'uppercase',
    marginBottom: 4,
  },
  aboutValue: {
    fontSize: 14,
    color: C.defaultText,
    lineHeight: 20,
  },
  propRow: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    paddingVertical: 8,
    borderBottomWidth: StyleSheet.hairlineWidth,
    borderBottomColor: C.grayBg,
  },
  propIcon: {
    width: 28,
    alignItems: 'center',
    marginTop: 1,
  },
  propContent: {
    flex: 1,
  },
  propLabel: {
    fontSize: 11,
    color: C.ts,
    textTransform: 'uppercase',
    fontWeight: '600',
  },
  propValue: {
    fontSize: 14,
    color: C.defaultText,
    marginTop: 1,
  },

  // ── Action buttons — mirrors send_gifts + start_chat ──────────────────────
  actionRow: {
    flexDirection: 'row',
    borderTopWidth: 1,
    borderTopColor: C.grayBg,
    backgroundColor: C.white,
    paddingVertical: 10,
  },
  actionBtn: {
    flex: 1,
    alignItems: 'center',
    paddingVertical: 6,
  },
  actionSep: {
    width: 1,
    height: '100%',
    backgroundColor: C.grayBg,
  },
  actionLabel: {
    fontSize: 11,
    color: C.defaultText,
    marginTop: 4,
    fontWeight: '600',
  },

  // ── Loading / Error ───────────────────────────────────────────────────────
  loadingWrap: {
    alignItems: 'center',
    paddingVertical: 32,
  },
  loadingText: {
    color: C.ts,
    marginTop: 8,
    fontSize: 14,
  },
  errorWrap: {
    alignItems: 'center',
    paddingVertical: 24,
    paddingHorizontal: 16,
  },
  errorText: {
    color: C.ts,
    marginTop: 8,
    textAlign: 'center',
  },
  retryBtn: {
    marginTop: 12,
    backgroundColor: C.headerBg,
    borderRadius: 8,
    paddingHorizontal: 20,
    paddingVertical: 8,
  },
  retryText: {
    color: C.white,
    fontWeight: '600',
  },
  privateWrap: {
    alignItems: 'center',
    paddingVertical: 32,
  },
  privateText: {
    color: C.private,
    marginTop: 8,
    fontSize: 15,
    fontWeight: '600',
  },

  // ── Popup menu — mirrors PopupMenu.java ───────────────────────────────────
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
    top: 52,
    right: 8,
    backgroundColor: C.menuBg,
    borderRadius: 10,
    overflow: 'hidden',
    minWidth: 200,
    elevation: 8,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.3,
    shadowRadius: 6,
  },
  menuItem: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 16,
    paddingVertical: 13,
  },
  menuItemBorder: {
    borderBottomWidth: StyleSheet.hairlineWidth,
    borderBottomColor: C.menuDivider,
  },
  menuIconWrap: {
    width: 28,
    alignItems: 'center',
  },
  menuLabel: {
    color: C.menuItem,
    fontSize: 14,
    marginLeft: 8,
  },
  menuLabelDanger: {
    color: C.danger,
  },

  tagBadgeRow: {
    flexDirection: 'row',
    alignItems: 'center',
    marginTop: 4,
    gap: 5,
    flexWrap: 'wrap',
  },
  tagPill: {
    borderRadius: 4,
    paddingHorizontal: 6,
    paddingVertical: 2,
  },
  tagPillText: {
    fontSize: 9,
    fontWeight: '800',
    letterSpacing: 0.5,
  },
  tagMerchant: {
    fontSize: 11,
    color: '#757575',
    fontStyle: 'italic',
  },
  tagAmount: {
    fontSize: 11,
    color: '#212121',
    fontWeight: '700',
  },
});
