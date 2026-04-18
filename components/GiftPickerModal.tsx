import { useEffect, useState } from 'react';
import {
  ActivityIndicator,
  Dimensions,
  FlatList,
  Image,
  Modal,
  ScrollView,
  StyleSheet,
  Text,
  TouchableOpacity,
  View,
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { EMOTICONS, STICKER_PACKS } from '../constants/emoticons';
import { API_BASE } from '../services/auth';
import { useAppTheme } from '../services/themeContext';
import { updateGiftCache } from '../services/giftCache';

const { width: SCREEN_W } = Dimensions.get('window');

const GIFT_CATEGORIES = ['Semua', 'Populer', 'Cinta', 'Spesial', 'Lucu'];

// Flat rate: 1 USD = 10,000 IDR (migchat fixed rate)
// 1 MIG coin = 100 IDR = 0.01 USD
const MIG_TO_USD  = 0.01;   // 1 MIG = 0.01 USD
const USD_TO_IDR  = 10_000; // 1 USD = 10,000 IDR

function priceToUSD(amount: number, currency: string): string {
  let usd: number;
  if (currency === 'USD') {
    usd = amount;
  } else if (currency === 'IDR') {
    usd = amount / USD_TO_IDR;
  } else {
    // MIG or default: 1 MIG = 0.01 USD
    usd = amount * MIG_TO_USD;
  }
  // Use up to 4 decimal places for very small values, 2 for normal
  const formatted = usd < 0.01 ? usd.toFixed(4).replace(/\.?0+$/, '') : usd.toFixed(2);
  return `USD ${formatted}`;
}

function priceToIDR(amount: number, currency: string): string {
  let idr: number;
  if (currency === 'USD') {
    idr = Math.round(amount * USD_TO_IDR);
  } else if (currency === 'IDR') {
    idr = Math.round(amount);
  } else {
    // MIG or default: 1 MIG = 100 IDR
    idr = Math.round(amount * MIG_TO_USD * USD_TO_IDR);
  }
  return `IDR ${idr.toLocaleString('id-ID')}`;
}

export interface GiftItem {
  id: string;
  name: string;
  emoji: string;
  imageUrl?: string;
  coins: number;
  currency: string;
  category: string;
}

export const GIFTS: GiftItem[] = [
  { id: 'g1',  name: 'Bunga',       emoji: '🌸', coins: 10,  currency: 'MIG', category: 'Populer' },
  { id: 'g2',  name: 'Cake',        emoji: '🎂', coins: 25,  currency: 'MIG', category: 'Populer' },
  { id: 'g3',  name: 'Balon',       emoji: '🎈', coins: 15,  currency: 'MIG', category: 'Populer' },
  { id: 'g4',  name: 'Hadiah',      emoji: '🎁', coins: 30,  currency: 'MIG', category: 'Populer' },
  { id: 'g5',  name: 'Hati',        emoji: '❤️',  coins: 5,   currency: 'MIG', category: 'Cinta' },
  { id: 'g6',  name: 'Hati Merah',  emoji: '💖', coins: 10,  currency: 'MIG', category: 'Cinta' },
  { id: 'g7',  name: 'Mawar',       emoji: '🌹', imageUrl: '/gifts/rose.png', coins: 15, currency: 'MIG', category: 'Cinta' },
  { id: 'g8',  name: 'Cupid',       emoji: '💘', coins: 20,  currency: 'MIG', category: 'Cinta' },
  { id: 'g9',  name: 'Berlian',     emoji: '💎', coins: 100, currency: 'MIG', category: 'Spesial' },
  { id: 'g10', name: 'Mahkota',     emoji: '👑', coins: 80,  currency: 'MIG', category: 'Spesial' },
  { id: 'g11', name: 'Trofi',       emoji: '🏆', coins: 50,  currency: 'MIG', category: 'Spesial' },
  { id: 'g12', name: 'Bintang',     emoji: '⭐', coins: 20,  currency: 'MIG', category: 'Spesial' },
  { id: 'g13', name: 'Teddy',       emoji: '🧸', coins: 35,  currency: 'MIG', category: 'Lucu' },
  { id: 'g14', name: 'Unicorn',     emoji: '🦄', coins: 45,  currency: 'MIG', category: 'Lucu' },
  { id: 'g15', name: 'Anjing',      emoji: '🐶', coins: 20,  currency: 'MIG', category: 'Lucu' },
  { id: 'g16', name: 'Kucing',      emoji: '🐱', coins: 20,  currency: 'MIG', category: 'Lucu' },
  { id: 'g17', name: 'Pizza',       emoji: '🍕', coins: 15,  currency: 'MIG', category: 'Lucu' },
  { id: 'g18', name: 'Es Krim',     emoji: '🍦', coins: 10,  currency: 'MIG', category: 'Lucu' },
  { id: 'g19', name: 'Alien',       emoji: '👾', coins: 40,  currency: 'MIG', category: 'Spesial' },
  { id: 'g20', name: 'Roket',       emoji: '🚀', coins: 60,  currency: 'MIG', category: 'Spesial' },
  { id: 'g21', name: 'Matahari',    emoji: '🌟', coins: 35,  currency: 'MIG', category: 'Populer' },
  { id: 'g22', name: 'Pelangi',     emoji: '🌈', coins: 40,  currency: 'MIG', category: 'Populer' },
  { id: 'g23', name: 'Cincin',      emoji: '💍', coins: 90,  currency: 'MIG', category: 'Cinta' },
  { id: 'g24', name: 'Coklat',      emoji: '🍫', coins: 12,  currency: 'MIG', category: 'Cinta' },
];

type MainTab = 'gifts' | 'emoticons' | 'stickers';

interface Props {
  visible: boolean;
  onClose: () => void;
  onSelectEmoticon: (unicode: string) => void;
  onSelectGift: (gift: GiftItem) => void;
  onSelectSticker: (stickerKey: string, label: string) => void;
  creditAmount: number;
  currency: string;
  recipientName?: string;
}

const GIFT_COL = 4;
const GIFT_ITEM_W = (SCREEN_W - 24) / GIFT_COL;
const EMOTICON_COL = 5;
const EMOTICON_ITEM_W = (SCREEN_W - 16) / EMOTICON_COL;
const STICKER_COL = 2;
const STICKER_ITEM_W = (SCREEN_W - 24) / STICKER_COL;

export default function GiftPickerModal({ visible, onClose, onSelectEmoticon, onSelectGift, onSelectSticker, creditAmount, currency, recipientName }: Props) {
  const insets = useSafeAreaInsets();
  const theme = useAppTheme();
  const [mainTab, setMainTab] = useState<MainTab>('gifts');
  const [giftCat, setGiftCat] = useState('Semua');
  const [stickerPack, setStickerPack] = useState(0);
  const [selectedGift, setSelectedGift] = useState<GiftItem | null>(null);
  const [liveGifts, setLiveGifts] = useState<GiftItem[]>(GIFTS);
  const [giftsLoading, setGiftsLoading] = useState(false);

  // Fetch gifts from API so ImageKit images show up
  useEffect(() => {
    if (!visible) return;
    setGiftsLoading(true);
    fetch(`${API_BASE}/api/store/gifts`)
      .then(r => r.json())
      .then(data => {
        const apiGifts: any[] = data.gifts ?? [];
        if (apiGifts.length > 0) {
          const mapped: GiftItem[] = apiGifts.map((g: any) => {
            const staticMatch = GIFTS.find(s => s.name.toLowerCase() === (g.name ?? '').toLowerCase());
            return {
              id: String(g.id),
              name: g.name ?? '',
              emoji: g.hotKey ?? staticMatch?.emoji ?? '🎁',
              imageUrl: g.location64x64Png ?? staticMatch?.imageUrl,
              coins: g.price ?? staticMatch?.coins ?? 0,
              currency: g.currency ?? staticMatch?.currency ?? 'MIG',
              category: staticMatch?.category ?? 'Populer',
            };
          });
          setLiveGifts(mapped);
          updateGiftCache(mapped.map(g => ({ name: g.name, emoji: g.emoji, imageUrl: g.imageUrl })));
        }
      })
      .catch(() => {})
      .finally(() => setGiftsLoading(false));
  }, [visible]);

  const filteredGifts = giftCat === 'Semua'
    ? liveGifts
    : liveGifts.filter(g => g.category === giftCat);

  const currentPack = STICKER_PACKS[stickerPack];

  const balanceStr = currency === 'USD'
    ? `USD ${creditAmount.toFixed(2)}`
    : `IDR ${Math.round(creditAmount).toLocaleString('id-ID')}`;

  return (
    <Modal
      visible={visible}
      animationType="slide"
      transparent
      onRequestClose={onClose}
    >
      <View style={styles.overlay}>
        <TouchableOpacity style={styles.backdrop} activeOpacity={1} onPress={onClose} />

        <View style={[styles.sheet, { backgroundColor: theme.cardBg, paddingBottom: insets.bottom || 8 }]}>

          {/* ── Main Tab Bar ── */}
          <View style={[styles.mainTabBar, { backgroundColor: theme.cardBg }]}>
            <TouchableOpacity
              style={[styles.mainTabBtn, mainTab === 'gifts' && { backgroundColor: theme.accentSoft }]}
              onPress={() => { setMainTab('gifts'); setSelectedGift(null); }}
              testID="tab-gifts"
            >
              <Ionicons name="gift-outline" size={20} color={mainTab === 'gifts' ? theme.accent : theme.textSecondary} />
              <Text style={[styles.mainTabTxt, { color: mainTab === 'gifts' ? theme.accent : theme.textSecondary }, mainTab === 'gifts' && styles.mainTabTxtActive]}>Gift</Text>
            </TouchableOpacity>

            <TouchableOpacity
              style={[styles.mainTabBtn, mainTab === 'emoticons' && { backgroundColor: theme.accentSoft }]}
              onPress={() => { setMainTab('emoticons'); setSelectedGift(null); }}
              testID="tab-emoticons"
            >
              <Ionicons name="happy-outline" size={20} color={mainTab === 'emoticons' ? theme.accent : theme.textSecondary} />
              <Text style={[styles.mainTabTxt, { color: mainTab === 'emoticons' ? theme.accent : theme.textSecondary }, mainTab === 'emoticons' && styles.mainTabTxtActive]}>Emoticon</Text>
            </TouchableOpacity>

            <TouchableOpacity
              style={[styles.mainTabBtn, mainTab === 'stickers' && { backgroundColor: theme.accentSoft }]}
              onPress={() => { setMainTab('stickers'); setSelectedGift(null); }}
              testID="tab-stickers"
            >
              <Ionicons name="images-outline" size={20} color={mainTab === 'stickers' ? theme.accent : theme.textSecondary} />
              <Text style={[styles.mainTabTxt, { color: mainTab === 'stickers' ? theme.accent : theme.textSecondary }, mainTab === 'stickers' && styles.mainTabTxtActive]}>Sticker</Text>
            </TouchableOpacity>

            <View style={{ flex: 1 }} />

            <TouchableOpacity onPress={onClose} style={styles.closeBtn} testID="button-close-picker">
              <Ionicons name="close" size={22} color={theme.textSecondary} />
            </TouchableOpacity>
          </View>

          <View style={[styles.divider, { backgroundColor: theme.divider }]} />

          {/* ── GIFTS TAB ── */}
          {mainTab === 'gifts' && (
            <View style={styles.tabContent}>
              {/* Credit balance */}
              <View style={styles.balanceRow}>
                <Ionicons name="wallet-outline" size={14} color="#27AE60" />
                <Text style={styles.balanceUSD} testID="text-gift-balance">{balanceStr}</Text>
              </View>

              {/* Category tabs */}
              <ScrollView horizontal showsHorizontalScrollIndicator={false} style={styles.catScroll} contentContainerStyle={{ paddingHorizontal: 8, gap: 6 }}>
                {GIFT_CATEGORIES.map(cat => (
                  <TouchableOpacity
                    key={cat}
                    style={[
                      styles.catBtn,
                      { backgroundColor: theme.inputBg, borderColor: 'transparent' },
                      giftCat === cat && { backgroundColor: theme.accentSoft, borderColor: theme.accent },
                    ]}
                    onPress={() => { setGiftCat(cat); setSelectedGift(null); }}
                    testID={`gift-cat-${cat}`}
                  >
                    <Text style={[styles.catTxt, { color: giftCat === cat ? theme.accent : theme.textSecondary }, giftCat === cat && styles.catTxtActive]}>{cat}</Text>
                  </TouchableOpacity>
                ))}
              </ScrollView>

              {/* Gift grid */}
              {giftsLoading && (
                <View style={{ paddingVertical: 16, alignItems: 'center' }}>
                  <ActivityIndicator size="small" color={theme.accent} />
                </View>
              )}
              <FlatList
                data={filteredGifts}
                keyExtractor={g => g.id}
                numColumns={GIFT_COL}
                style={styles.giftGrid}
                contentContainerStyle={{ paddingHorizontal: 6, paddingBottom: 8 }}
                renderItem={({ item }) => {
                  const isSelected = selectedGift?.id === item.id;
                  const resolvedImageUrl = item.imageUrl
                    ? (item.imageUrl.startsWith('http') ? item.imageUrl : `${API_BASE}${item.imageUrl}`)
                    : null;
                  return (
                    <TouchableOpacity
                      style={[
                        styles.giftCell,
                        isSelected && { backgroundColor: theme.accentSoft, borderWidth: 1.5, borderColor: theme.accent },
                      ]}
                      onPress={() => setSelectedGift(isSelected ? null : item)}
                      testID={`gift-item-${item.id}`}
                    >
                      {resolvedImageUrl ? (
                        <Image
                          source={{ uri: resolvedImageUrl }}
                          style={styles.giftImage}
                          resizeMode="contain"
                        />
                      ) : (
                        <Text style={styles.giftEmoji}>{item.emoji}</Text>
                      )}
                      <Text style={[styles.giftName, { color: theme.textPrimary }]} numberOfLines={1}>{item.name}</Text>
                      <Text style={styles.giftPriceUSD}>{priceToUSD(item.coins, item.currency)}</Text>
                      <Text style={[styles.giftPriceIDR, { color: theme.textSecondary }]}>{priceToIDR(item.coins, item.currency)}</Text>
                    </TouchableOpacity>
                  );
                }}
              />

              {/* Send button */}
              {selectedGift && (
                <View style={[styles.sendGiftBar, { backgroundColor: theme.drawerBg, borderTopColor: theme.divider }]}>
                  <Text style={[styles.sendGiftName, { color: theme.textPrimary }]}>
                    {selectedGift.imageUrl ? '' : selectedGift.emoji + ' '}{selectedGift.name}
                  </Text>
                  <View style={styles.sendGiftPriceCol}>
                    <Text style={styles.sendGiftPriceUSD} testID="text-send-price-usd">{priceToUSD(selectedGift.coins, selectedGift.currency)}</Text>
                    <Text style={[styles.sendGiftPriceIDR, { color: theme.textSecondary }]} testID="text-send-price-idr">{priceToIDR(selectedGift.coins, selectedGift.currency)}</Text>
                  </View>
                  <TouchableOpacity
                    style={[styles.sendGiftBtn, { backgroundColor: theme.accent }, !recipientName && styles.sendGiftBtnAll]}
                    onPress={() => { onSelectGift(selectedGift); setSelectedGift(null); }}
                    testID="button-send-gift"
                  >
                    <Text style={[styles.sendGiftBtnTxt, { color: theme.textOnAccent }]}>
                      {recipientName ? `Kirim ke ${recipientName}` : '🎊 Gift ke Semua'}
                    </Text>
                  </TouchableOpacity>
                </View>
              )}
            </View>
          )}

          {/* ── EMOTICONS TAB ── */}
          {mainTab === 'emoticons' && (
            <View style={styles.tabContent}>
              <FlatList
                data={EMOTICONS}
                keyExtractor={e => e.key}
                numColumns={EMOTICON_COL}
                contentContainerStyle={{ paddingHorizontal: 8, paddingBottom: 8 }}
                renderItem={({ item }) => (
                  <TouchableOpacity
                    style={[styles.emoticonCell, { width: EMOTICON_ITEM_W }]}
                    onPress={() => onSelectEmoticon(item.unicode)}
                    testID={`emoticon-${item.key}`}
                  >
                    <Image source={item.image as any} style={styles.emoticonImg} resizeMode="contain" />
                    <Text style={[styles.emoticonLabel, { color: theme.textSecondary }]} numberOfLines={1}>{item.label}</Text>
                  </TouchableOpacity>
                )}
              />
            </View>
          )}

          {/* ── STICKERS TAB ── */}
          {mainTab === 'stickers' && (
            <View style={styles.tabContent}>
              {/* Pack selector */}
              <ScrollView horizontal showsHorizontalScrollIndicator={false} style={styles.packScroll} contentContainerStyle={{ paddingHorizontal: 8, gap: 8 }}>
                {STICKER_PACKS.map((pack, idx) => (
                  <TouchableOpacity
                    key={pack.id}
                    style={[
                      styles.packThumb,
                      { borderColor: 'transparent' },
                      stickerPack === idx && { backgroundColor: theme.accentSoft, borderColor: theme.accent },
                    ]}
                    onPress={() => setStickerPack(idx)}
                    testID={`sticker-pack-${pack.id}`}
                  >
                    <Image source={pack.coverImage as any} style={styles.packThumbImg} resizeMode="contain" />
                    <Text style={[styles.packThumbTxt, { color: stickerPack === idx ? theme.accent : theme.textSecondary }, stickerPack === idx && styles.packThumbTxtActive]} numberOfLines={1}>
                      {pack.name}
                    </Text>
                  </TouchableOpacity>
                ))}
              </ScrollView>

              <View style={[styles.divider, { backgroundColor: theme.divider }]} />

              {/* Sticker grid */}
              <FlatList
                key={currentPack.id}
                data={currentPack.stickers}
                keyExtractor={s => s.key}
                numColumns={STICKER_COL}
                contentContainerStyle={{ paddingHorizontal: 6, paddingBottom: 8 }}
                renderItem={({ item }) => (
                  <TouchableOpacity
                    style={[styles.stickerCell, { width: STICKER_ITEM_W, backgroundColor: theme.inputBg }]}
                    onPress={() => onSelectSticker(item.key, item.label)}
                    testID={`sticker-${item.key}`}
                  >
                    <Image source={item.image as any} style={styles.stickerImg} resizeMode="contain" />
                    <Text style={[styles.stickerLabel, { color: theme.textSecondary }]} numberOfLines={1}>{item.label}</Text>
                  </TouchableOpacity>
                )}
              />
            </View>
          )}
        </View>
      </View>
    </Modal>
  );
}

const styles = StyleSheet.create({
  overlay: {
    flex: 1,
    justifyContent: 'flex-end',
  },
  backdrop: {
    ...StyleSheet.absoluteFillObject,
    backgroundColor: 'rgba(0,0,0,0.35)',
  },
  sheet: {
    borderTopLeftRadius: 16,
    borderTopRightRadius: 16,
    height: '75%',
    overflow: 'hidden',
    elevation: 24,
    shadowColor: '#000',
    shadowOpacity: 0.25,
    shadowRadius: 12,
    shadowOffset: { width: 0, height: -4 },
  },

  /* ── Main Tab Bar ── */
  mainTabBar: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 8,
    paddingTop: 10,
    paddingBottom: 4,
  },
  mainTabBtn: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 20,
    gap: 4,
    marginRight: 4,
  },
  mainTabTxt: {
    fontSize: 13,
  },
  mainTabTxtActive: {
    fontWeight: '700',
  },
  closeBtn: {
    padding: 6,
  },
  divider: {
    height: 1,
  },

  tabContent: {
    flex: 1,
  },

  /* ── Balance row (USD + IDR) ── */
  balanceRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 4,
    paddingHorizontal: 12,
    paddingTop: 6,
    paddingBottom: 2,
  },
  balanceUSD: {
    fontSize: 13,
    color: '#27AE60',
    fontWeight: '700',
  },

  /* ── Gift Category Tabs ── */
  catScroll: {
    flexGrow: 0,
    paddingVertical: 6,
  },
  catBtn: {
    paddingHorizontal: 14,
    paddingVertical: 5,
    borderRadius: 16,
    borderWidth: 1,
  },
  catTxt: {
    fontSize: 12,
  },
  catTxtActive: {
    fontWeight: '700',
  },

  /* ── Gift Grid ── */
  giftGrid: {
    flex: 1,
  },
  giftCell: {
    width: GIFT_ITEM_W,
    alignItems: 'center',
    paddingVertical: 8,
    paddingHorizontal: 4,
    borderRadius: 10,
    margin: 2,
  },
  giftEmoji: {
    fontSize: 34,
    lineHeight: 42,
  },
  giftImage: {
    width: 52,
    height: 52,
    borderRadius: 4,
  },
  giftName: {
    fontSize: 10,
    fontWeight: '700',
    marginTop: 2,
    textAlign: 'center',
  },
  giftPriceUSD: {
    fontSize: 10,
    color: '#27AE60',
    fontWeight: '600',
    marginTop: 2,
  },
  giftPriceIDR: {
    fontSize: 9,
    marginTop: 1,
  },

  /* ── Send Gift Bar ── */
  sendGiftBar: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 14,
    paddingVertical: 10,
    borderTopWidth: 1,
    gap: 8,
  },
  sendGiftName: {
    flex: 1,
    fontSize: 13,
    fontWeight: '700',
  },
  sendGiftPriceCol: {
    alignItems: 'flex-end',
  },
  sendGiftPriceUSD: {
    fontSize: 13,
    color: '#27AE60',
    fontWeight: '700',
  },
  sendGiftPriceIDR: {
    fontSize: 10,
    marginTop: 1,
  },
  sendGiftBtn: {
    borderRadius: 20,
    paddingHorizontal: 20,
    paddingVertical: 8,
  },
  sendGiftBtnAll: {
    backgroundColor: '#B45309',
  },
  sendGiftBtnTxt: {
    fontSize: 13,
    fontWeight: '700',
  },

  /* ── Emoticons ── */
  emoticonCell: {
    alignItems: 'center',
    paddingVertical: 8,
    paddingHorizontal: 2,
    borderRadius: 8,
    margin: 1,
  },
  emoticonImg: {
    width: 44,
    height: 44,
  },
  emoticonLabel: {
    fontSize: 9,
    marginTop: 2,
    textAlign: 'center',
  },

  /* ── Sticker Pack Selector ── */
  packScroll: {
    flexGrow: 0,
    paddingVertical: 8,
  },
  packThumb: {
    alignItems: 'center',
    width: 72,
    borderRadius: 8,
    paddingVertical: 4,
    paddingHorizontal: 2,
    borderWidth: 1.5,
  },
  packThumbImg: {
    width: 60,
    height: 36,
  },
  packThumbTxt: {
    fontSize: 10,
    marginTop: 2,
    textAlign: 'center',
  },
  packThumbTxtActive: {
    fontWeight: '700',
  },

  /* ── Sticker Grid ── */
  stickerCell: {
    alignItems: 'center',
    padding: 6,
    margin: 3,
    borderRadius: 10,
  },
  stickerImg: {
    width: STICKER_ITEM_W - 24,
    height: (STICKER_ITEM_W - 24) * 0.6,
  },
  stickerLabel: {
    fontSize: 10,
    marginTop: 4,
    textAlign: 'center',
  },
});
