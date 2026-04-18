import { useCallback, useEffect, useState } from 'react';
import {
  ActivityIndicator,
  FlatList,
  Image,
  Modal,
  Platform,
  RefreshControl,
  ScrollView,
  StyleSheet,
  Text,
  TouchableOpacity,
  View,
} from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { Ionicons } from '@expo/vector-icons';
import { getSession } from '../services/storage';
import { API_BASE } from '../services/auth';
import { useAppTheme } from '../services/themeContext';

const GOLD   = '#FFD700';
const SILVER = '#9E9E9E';
const BRONZE = '#CD7F32';
const MEDAL_COLORS = [GOLD, SILVER, BRONZE];
const MEDAL_NAMES  = ['🥇', '🥈', '🥉'];
const PODIUM_SIZES = [72, 60, 56];

interface LBEntry {
  username: string;
  score:    number;
  position: number;
  displayName?: string;
  displayPicture?: string | null;
}

async function buildHeaders(): Promise<HeadersInit> {
  const h: Record<string, string> = { 'Content-Type': 'application/json' };
  if (Platform.OS !== 'web') {
    const cookie = await getSession();
    if (cookie) h['Cookie'] = cookie;
  }
  return h;
}
const fetchOpts = (): RequestInit =>
  Platform.OS === 'web' ? { credentials: 'include' } : {};

async function apiGet(path: string) {
  const headers = await buildHeaders();
  const res = await fetch(`${API_BASE}${path}`, { headers, ...fetchOpts() });
  if (!res.ok) throw new Error(`${res.status}`);
  return res.json();
}

type Category = { key: string; label: string; icon: string; lbType: string; unit: string; };

const CATEGORIES: Category[] = [
  { key: 'gift_sent',  label: 'Gift Senders',   icon: 'gift-outline',             lbType: 'LB:GiftSent:',       unit: 'gifts' },
  { key: 'gift_recv',  label: 'Gift Receivers',  icon: 'heart-outline',            lbType: 'LB:GiftReceived:',   unit: 'gifts' },
  { key: 'mig_level',  label: 'Level',           icon: 'star-outline',             lbType: 'LB:MigLevel:',       unit: 'xp'    },
  { key: 'games',      label: 'Games Won',       icon: 'game-controller-outline',  lbType: 'LB:MostWins:Total:', unit: 'wins'  },
  { key: 'one_wins',   label: 'One Wins',        icon: 'layers-outline',           lbType: 'LB:MostWins:One:',   unit: 'wins'  },
  { key: 'one_played', label: 'One Played',      icon: 'card-outline',             lbType: 'LB:GamesPlayed:One:',unit: 'games' },
  { key: 'paintwars',  label: 'Paint Wars',      icon: 'color-palette-outline',    lbType: 'LB:PaintPoints:',    unit: 'pts'   },
];

type Period = { key: string; label: string };
const PERIODS: Period[] = [
  { key: 'DAILY',    label: 'Today' },
  { key: 'WEEKLY',   label: 'This Week' },
  { key: 'MONTHLY',  label: 'This Month' },
  { key: 'ALL_TIME', label: 'All Time' },
];

function AvatarCircle({
  name, size = 40, color, displayPicture,
}: {
  name: string; size?: number; color?: string; displayPicture?: string | null;
}) {
  const theme = useAppTheme();
  const bg = color ?? theme.accent;
  const uri = displayPicture
    ? (displayPicture.startsWith('http') ? displayPicture : `${API_BASE}${displayPicture}`)
    : null;
  if (uri) {
    return (
      <Image
        source={{ uri }}
        style={{ width: size, height: size, borderRadius: size / 2, backgroundColor: bg }}
        resizeMode="cover"
      />
    );
  }
  return (
    <View style={{ width: size, height: size, borderRadius: size / 2, backgroundColor: bg, alignItems: 'center', justifyContent: 'center' }}>
      <Text style={{ color: '#FFFFFF', fontSize: size * 0.36, fontWeight: '700' }}>
        {(name || '?').slice(0, 2).toUpperCase()}
      </Text>
    </View>
  );
}

function PodiumCard({ entry, rank, theme }: { entry: LBEntry; rank: 1 | 2 | 3; theme: ReturnType<typeof useAppTheme> }) {
  const color   = MEDAL_COLORS[rank - 1];
  const size    = PODIUM_SIZES[rank - 1];
  const isFirst = rank === 1;
  return (
    <View style={[podiumStyles.card, isFirst && podiumStyles.cardFirst]}>
      <View style={[podiumStyles.medalBadge, { backgroundColor: color }]}>
        <Text style={podiumStyles.medalText}>{MEDAL_NAMES[rank - 1]}</Text>
      </View>
      <AvatarCircle name={entry.username} size={size} color={color + 'CC'} displayPicture={entry.displayPicture} />
      <Text style={[podiumStyles.name, isFirst && podiumStyles.nameFirst, { color: theme.textPrimary }]} numberOfLines={1}>
        {entry.username}
      </Text>
      <Text style={[podiumStyles.score, { color }]}>{entry.score.toLocaleString()}</Text>
    </View>
  );
}

const podiumStyles = StyleSheet.create({
  card:       { flex: 1, alignItems: 'center', gap: 6, paddingBottom: 8 },
  cardFirst:  { flex: 1.2 },
  medalBadge: { width: 28, height: 28, borderRadius: 14, alignItems: 'center', justifyContent: 'center', marginBottom: 2 },
  medalText:  { fontSize: 16 },
  name:       { fontSize: 12, fontWeight: '700', textAlign: 'center', maxWidth: 80 },
  nameFirst:  { fontSize: 13 },
  score:      { fontSize: 12, fontWeight: '800' },
});

function RankRow({ entry, unit, theme }: { entry: LBEntry; unit: string; theme: ReturnType<typeof useAppTheme> }) {
  const rankColor = entry.position === 1 ? GOLD : entry.position === 2 ? SILVER : entry.position === 3 ? BRONZE : theme.textSecondary;
  return (
    <View style={{ flexDirection: 'row', alignItems: 'center', paddingVertical: 12, paddingHorizontal: 16, gap: 10 }} testID={`rank-row-${entry.username}`}>
      <View style={{ width: 36, height: 36, borderRadius: 8, backgroundColor: rankColor + '22', alignItems: 'center', justifyContent: 'center' }}>
        <Text style={{ fontSize: 12, fontWeight: '800', color: rankColor }}>#{entry.position}</Text>
      </View>
      <AvatarCircle name={entry.username} size={38} displayPicture={entry.displayPicture} />
      <View style={{ flex: 1, marginLeft: 10 }}>
        <Text style={{ fontSize: 14, fontWeight: '600', color: theme.textPrimary }} numberOfLines={1}>{entry.username}</Text>
      </View>
      <Text style={{ fontSize: 13, fontWeight: '700', color: theme.textPrimary }}>
        {entry.score.toLocaleString()}{' '}
        <Text style={{ fontSize: 11, color: theme.textSecondary, fontWeight: '400' }}>{unit}</Text>
      </Text>
    </View>
  );
}

interface Props { visible: boolean; onClose: () => void; }

export default function LeaderboardModal({ visible, onClose }: Props) {
  const insets = useSafeAreaInsets();
  const theme  = useAppTheme();

  const [catIdx,     setCatIdx]     = useState(0);
  const [periodIdx,  setPeriodIdx]  = useState(1);
  const [entries,    setEntries]    = useState<LBEntry[]>([]);
  const [loading,    setLoading]    = useState(false);
  const [refreshing, setRefreshing] = useState(false);

  const cat    = CATEGORIES[catIdx];
  const period = PERIODS[periodIdx];

  const load = useCallback(async (refresh = false) => {
    if (refresh) setRefreshing(true); else setLoading(true);
    try {
      const enc  = encodeURIComponent(cat.lbType);
      const data = await apiGet(`/api/leaderboard/${enc}/${period.key}?limit=20`);
      setEntries(data.entries ?? []);
    } catch {
      setEntries([]);
    }
    if (refresh) setRefreshing(false); else setLoading(false);
  }, [cat.lbType, period.key]);

  useEffect(() => {
    if (visible) load();
  }, [visible, catIdx, periodIdx]);

  const top3  = entries.slice(0, 3);
  const rest  = entries.slice(3);
  const first  = top3.find(e => e.position === 1);
  const second = top3.find(e => e.position === 2);
  const third  = top3.find(e => e.position === 3);

  const s = makeStyles(theme);

  return (
    <Modal
      visible={visible}
      animationType="slide"
      transparent={false}
      onRequestClose={onClose}
      statusBarTranslucent
    >
      <View style={[s.root, { paddingTop: insets.top }]}>

        {/* ── Header ── */}
        <View style={s.header}>
          <TouchableOpacity onPress={onClose} testID="button-lb-back">
            <Ionicons name="arrow-back" size={24} color={theme.textOnAccent} />
          </TouchableOpacity>
          <Image
            source={require('../assets/icons/ad_solidbadge.png')}
            style={s.headerIcon}
            resizeMode="contain"
          />
          <Text style={s.headerTitle}>Leaderboards</Text>
        </View>

        {/* ── Period selector ── */}
        <View style={s.periodBar}>
          <ScrollView horizontal showsHorizontalScrollIndicator={false} contentContainerStyle={{ paddingHorizontal: 12, gap: 8 }}>
            {PERIODS.map((p, i) => (
              <TouchableOpacity
                key={p.key}
                style={[s.periodChip, periodIdx === i && s.periodChipActive]}
                onPress={() => setPeriodIdx(i)}
                testID={`period-chip-${p.key}`}
              >
                <Text style={[s.periodChipText, periodIdx === i && s.periodChipTextActive]}>
                  {p.label}
                </Text>
              </TouchableOpacity>
            ))}
          </ScrollView>
        </View>

        {/* ── Category tabs ── */}
        <View style={s.catTabBar}>
          <ScrollView horizontal showsHorizontalScrollIndicator={false} contentContainerStyle={{ paddingHorizontal: 12, gap: 0 }}>
            {CATEGORIES.map((c, i) => (
              <TouchableOpacity
                key={c.key}
                style={[s.catTab, catIdx === i && s.catTabActive]}
                onPress={() => setCatIdx(i)}
                testID={`cat-tab-${c.key}`}
              >
                <Ionicons
                  name={c.icon as any}
                  size={14}
                  color={catIdx === i ? theme.accent : theme.textSecondary}
                  style={{ marginRight: 4 }}
                />
                <Text style={[s.catTabText, catIdx === i && s.catTabTextActive]}>
                  {c.label}
                </Text>
              </TouchableOpacity>
            ))}
          </ScrollView>
        </View>

        {/* ── Content ── */}
        {loading ? (
          <ActivityIndicator color={theme.accent} style={{ marginTop: 60 }} size="large" />
        ) : entries.length === 0 ? (
          <View style={s.emptyWrap}>
            <Image
              source={require('../assets/icons/ad_colbadge.png')}
              style={s.emptyIcon}
              resizeMode="contain"
            />
            <Text style={s.emptyTitle}>No entries yet</Text>
            <Text style={s.emptySub}>Be the first to top the {cat.label} leaderboard!</Text>
          </View>
        ) : (
          <ScrollView
            contentContainerStyle={[s.scrollContent, { paddingBottom: insets.bottom + 24 }]}
            showsVerticalScrollIndicator={false}
            refreshControl={
              <RefreshControl
                refreshing={refreshing}
                onRefresh={() => load(true)}
                tintColor={theme.accent}
                colors={[theme.accent]}
              />
            }
          >
            {/* ── Podium: top 3 ── */}
            {top3.length > 0 && (
              <View style={s.podiumWrap}>
                <View style={s.podiumRow}>
                  {second ? <PodiumCard entry={second} rank={2} theme={theme} /> : <View style={{ flex: 1 }} />}
                  {first  ? <PodiumCard entry={first}  rank={1} theme={theme} /> : <View style={{ flex: 1.2 }} />}
                  {third  ? <PodiumCard entry={third}  rank={3} theme={theme} /> : <View style={{ flex: 1 }} />}
                </View>
                <View style={s.podiumSteps}>
                  <View style={[s.podiumStep, { height: 44, backgroundColor: SILVER + '40' }]}>
                    <Text style={[s.podiumStepNum, { color: SILVER }]}>2</Text>
                  </View>
                  <View style={[s.podiumStep, { height: 66, backgroundColor: GOLD + '40' }]}>
                    <Text style={[s.podiumStepNum, { color: GOLD }]}>1</Text>
                  </View>
                  <View style={[s.podiumStep, { height: 32, backgroundColor: BRONZE + '40' }]}>
                    <Text style={[s.podiumStepNum, { color: BRONZE }]}>3</Text>
                  </View>
                </View>
              </View>
            )}

            {/* ── Rank list 4+ ── */}
            {rest.length > 0 && (
              <View style={s.section}>
                <View style={s.sectionHeader}>
                  <Image source={require('../assets/icons/ad_solidbadge.png')} style={s.sectionIcon} resizeMode="contain" />
                  <Text style={s.sectionTitle}>Rankings</Text>
                  <Text style={s.sectionCount}>{entries.length} players</Text>
                </View>
                <View style={s.card}>
                  {rest.map((entry, i) => (
                    <View key={entry.username}>
                      <RankRow entry={entry} unit={cat.unit} theme={theme} />
                      {i < rest.length - 1 && <View style={s.rowDivider} />}
                    </View>
                  ))}
                </View>
              </View>
            )}

            {/* ── Top 3 fallback ── */}
            {top3.length > 0 && rest.length === 0 && (
              <View style={s.section}>
                <View style={s.card}>
                  {top3.map((entry, i) => (
                    <View key={entry.username}>
                      <RankRow entry={entry} unit={cat.unit} theme={theme} />
                      {i < top3.length - 1 && <View style={s.rowDivider} />}
                    </View>
                  ))}
                </View>
              </View>
            )}

            {/* ── Legend strip ── */}
            <View style={s.legendStrip}>
              <Image source={require('../assets/icons/ad_badge_white.png')} style={[s.legendIcon, { tintColor: theme.accent }]} resizeMode="contain" />
              <Text style={s.legendText}>{cat.label} · {period.label} · Top {entries.length}</Text>
            </View>
          </ScrollView>
        )}
      </View>
    </Modal>
  );
}

function makeStyles(theme: ReturnType<typeof useAppTheme>) {
  return StyleSheet.create({
    root: { flex: 1, backgroundColor: theme.screenBg },

    header: {
      backgroundColor: theme.headerBg,
      flexDirection: 'row',
      alignItems: 'center',
      paddingHorizontal: 16,
      paddingVertical: 14,
      gap: 10,
    },
    headerIcon:  { width: 22, height: 22, tintColor: theme.textOnAccent },
    headerTitle: { flex: 1, color: theme.textOnAccent, fontSize: 18, fontWeight: '700', letterSpacing: 0.5 },

    periodBar: {
      backgroundColor: theme.headerBg,
      paddingBottom: 12,
    },
    periodChip: {
      borderRadius: 20,
      paddingHorizontal: 14,
      paddingVertical: 6,
      borderWidth: 1,
      borderColor: 'rgba(255,255,255,0.3)',
    },
    periodChipActive:     { backgroundColor: theme.accent, borderColor: theme.accent },
    periodChipText:       { fontSize: 13, color: 'rgba(255,255,255,0.7)', fontWeight: '600' },
    periodChipTextActive: { color: '#FFFFFF' },

    catTabBar: {
      backgroundColor: theme.cardBg,
      borderBottomWidth: 1,
      borderBottomColor: theme.divider,
      paddingVertical: 2,
    },
    catTab: {
      flexDirection: 'row',
      alignItems: 'center',
      paddingVertical: 10,
      paddingHorizontal: 14,
      borderBottomWidth: 2,
      borderBottomColor: 'transparent',
    },
    catTabActive:     { borderBottomColor: theme.accent },
    catTabText:       { fontSize: 13, color: theme.textSecondary, fontWeight: '600' },
    catTabTextActive: { color: theme.accent },

    scrollContent: { paddingHorizontal: 16, paddingTop: 16 },

    podiumWrap: {
      backgroundColor: theme.cardBg,
      borderRadius: 16,
      overflow: 'hidden',
      marginBottom: 20,
      paddingTop: 20,
      paddingHorizontal: 8,
      borderWidth: 1,
      borderColor: theme.border,
    },
    podiumRow: {
      flexDirection: 'row',
      justifyContent: 'center',
      alignItems: 'flex-end',
      gap: 8,
      paddingHorizontal: 8,
    },
    podiumSteps: { flexDirection: 'row', alignItems: 'flex-end', marginTop: 4 },
    podiumStep: { flex: 1, alignItems: 'center', justifyContent: 'center', borderTopLeftRadius: 4, borderTopRightRadius: 4 },
    podiumStepNum: { fontSize: 18, fontWeight: '900' },

    section:      { marginBottom: 16 },
    sectionHeader: { flexDirection: 'row', alignItems: 'center', marginBottom: 10 },
    sectionIcon:  { width: 16, height: 16, marginRight: 6, tintColor: theme.textSecondary },
    sectionTitle: { flex: 1, fontSize: 13, fontWeight: '700', color: theme.textPrimary, textTransform: 'uppercase', letterSpacing: 0.5 },
    sectionCount: {
      fontSize: 12,
      color: theme.textSecondary,
      backgroundColor: theme.inputBg,
      borderRadius: 10,
      paddingHorizontal: 8,
      paddingVertical: 2,
      overflow: 'hidden',
    },

    card: {
      backgroundColor: theme.cardBg,
      borderRadius: 12,
      borderWidth: 1,
      borderColor: theme.border,
      overflow: 'hidden',
    },
    rowDivider: { height: 1, backgroundColor: theme.divider, marginHorizontal: 16 },

    legendStrip: {
      flexDirection: 'row',
      alignItems: 'center',
      gap: 8,
      backgroundColor: theme.cardBg,
      borderRadius: 10,
      padding: 12,
      borderWidth: 1,
      borderColor: theme.border,
      marginBottom: 8,
    },
    legendIcon: { width: 18, height: 18 },
    legendText: { fontSize: 12, color: theme.textSecondary },

    emptyWrap: { flex: 1, alignItems: 'center', justifyContent: 'center', paddingHorizontal: 32, gap: 12 },
    emptyIcon: { width: 80, height: 80, tintColor: theme.divider, marginBottom: 8 },
    emptyTitle: { fontSize: 18, fontWeight: '700', color: theme.textSecondary },
    emptySub:   { fontSize: 14, color: theme.textSecondary, textAlign: 'center', lineHeight: 20 },
  });
}
