import * as FileSystem from 'expo-file-system/legacy';
import * as ImagePicker from 'expo-image-picker';
import { useCallback, useEffect, useRef, useState } from 'react';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import {
  ActivityIndicator,
  Alert,
  Animated,
  Dimensions,
  FlatList,
  Image,
  KeyboardAvoidingView,
  Linking,
  Modal,
  Platform,
  Pressable,
  RefreshControl,
  ScrollView,
  Share,
  StyleSheet,
  Text,
  TextInput,
  TouchableOpacity,
  View,
} from 'react-native';

import { API_BASE, buildHeaders, getMe } from '../../services/auth';
import { useAppTheme } from '../../services/themeContext';

const SCREEN_H = Dimensions.get('window').height;

// ─── Constants (mirroring Android's Constants.MAX_MESSAGE_LENGTH) ─────────────
const MAX_CHARS = 500;

// ─── Theme colours ────────────────────────────────────────────────────────────
const C = {
  green:     '#64B9A0',
  darkGreen: '#09454A',
  white:     '#FFFFFF',
  bg:        '#F2F2F2',
  cardBg:    '#FFFFFF',
  text:      '#212121',
  ts:        '#9E9E9E',
  sep:       '#EEEEEE',
  actionSep: '#E0E0E0',
  grey:      '#757575',
  inputBg:   '#FAFAFA',
  charOk:    '#9E9E9E',
  charWarn:  '#FB8C00',
  charError: '#E53935',
  overlayBg: 'rgba(0,0,0,0.55)',
  fabBg:     '#64B9A0',
  avatarBg:  '#114C54',
  previewBg: '#F5F5F5',
  privacySep:'#E8E8E8',
  twitterBlue: '#1DA1F2',
  facebookBlue: '#1877F2',
};

// ─── Emoji list (mirroring AttachmentPagerFragment emoticon grid) ─────────────
const EMOJIS = [
  '😀','😃','😄','😁','😆','😅','😂','🤣','😊','😇',
  '🙂','🙃','😉','😌','😍','🥰','😘','😗','😙','😚',
  '😋','😛','😜','🤪','😝','🤑','🤗','🤭','🤫','🤔',
  '😐','😑','😶','😏','😒','🙄','😬','🤥','😔','😪',
  '😴','😷','🤒','🤕','🤢','🤧','🥵','🥶','😵','🤯',
  '🤠','🥳','😎','🤓','🧐','😕','😟','🙁','☹️','😮',
  '😯','😲','😳','🥺','😦','😧','😨','😰','😥','😢',
  '😭','😱','😖','😣','😞','😓','😩','😫','🥱','😤',
  '😡','😠','🤬','😈','👿','💀','☠️','💩','🤡','👹',
  '❤️','🧡','💛','💚','💙','💜','🖤','🤍','🤎','💔',
];

// ─── Hot Topics (mirroring PostsDatastore.getHotTopics) ───────────────────────
const HOT_TOPICS = [
  '#migchat', '#trending', '#fun', '#music', '#gaming',
  '#food', '#travel', '#selfie', '#love', '#friends',
];

// ─── Types ────────────────────────────────────────────────────────────────────
interface FeedPost {
  id: string;
  authorUsername: string;
  authorDisplayPicture?: string | null;
  comment: string;
  imageUrl?: string | null;
  numLikes: number;
  numDislikes: number;
  numComments: number;
  createdAt: string;
  type: number;
  repostId?: string | null;
  repostAuthorUsername?: string | null;
  repostComment?: string | null;
}

interface PostComment {
  id: string;
  postId: string;
  authorUsername: string;
  text: string;
  createdAt: string;
}

type PostPrivacy   = 'everyone' | 'friends' | 'private';
type CreateAction  = 'new_post' | 'reply' | 'repost';

// ─── Rich text: colour #hashtags and @mentions (mirrors SpannableBuilder.java) ─
function RichText({ text, style, testID }: { text: string; style?: any; testID?: string }) {
  const theme = useAppTheme();
  const parts = text.split(/(#\w+|@\w+)/g);
  return (
    <Text style={style} testID={testID}>
      {parts.map((part, i) => {
        if (/^#\w+/.test(part)) {
          return (
            <Text key={i} style={{ color: theme.accent, fontWeight: '600' }}>{part}</Text>
          );
        }
        if (/^@\w+/.test(part)) {
          return (
            <Text key={i} style={{ color: theme.isDark ? '#FF7070' : '#8B0000', fontWeight: '600' }}>{part}</Text>
          );
        }
        return <Text key={i}>{part}</Text>;
      })}
    </Text>
  );
}

// ─── Helpers ──────────────────────────────────────────────────────────────────
function timeAgo(dateStr: string): string {
  const diff = Date.now() - new Date(dateStr).getTime();
  const mins = Math.floor(diff / 60000);
  if (mins < 1) return 'just now';
  if (mins < 60) return `${mins}m ago`;
  const hrs = Math.floor(mins / 60);
  if (hrs < 24) return `${hrs}h ago`;
  return `${Math.floor(hrs / 24)}d ago`;
}

function useSlideUp(visible: boolean) {
  const anim = useRef(new Animated.Value(700)).current;
  useEffect(() => {
    if (visible) {
      Animated.spring(anim, { toValue: 0, useNativeDriver: true, damping: 20, stiffness: 150 }).start();
    } else {
      Animated.timing(anim, { toValue: 700, duration: 200, useNativeDriver: true }).start();
    }
  }, [visible]);
  return anim;
}

// ─── Avatar ───────────────────────────────────────────────────────────────────
function Avatar({ username, displayPicture, size = 42 }: { username: string; displayPicture?: string | null; size?: number }) {
  const theme = useAppTheme();
  const [err, setErr] = useState(false);
  if (displayPicture && !err) {
    return (
      <Image
        source={{ uri: displayPicture }}
        style={{ width: size, height: size, borderRadius: size / 2 }}
        resizeMode="cover"
        onError={() => setErr(true)}
      />
    );
  }
  return (
    <View style={[ss.avatarCircle, { width: size, height: size, borderRadius: size / 2, backgroundColor: theme.accent }]}>
      <Text style={[ss.avatarText, { fontSize: size * 0.35 }]}>
        {username.slice(0, 2).toUpperCase()}
      </Text>
    </View>
  );
}

// ─── Privacy Popup (mirroring getPrivacyMenuOptions) ─────────────────────────
interface PrivacyPopupProps {
  visible: boolean;
  privacy: PostPrivacy;
  allowReplies: boolean;
  onSelect: (privacy: PostPrivacy) => void;
  onToggleReplies: () => void;
  onClose: () => void;
}

function PrivacyPopup({ visible, privacy, allowReplies, onSelect, onToggleReplies, onClose }: PrivacyPopupProps) {
  const theme = useAppTheme();
  return (
    <Modal visible={visible} transparent animationType="fade" onRequestClose={onClose} statusBarTranslucent>
      <Pressable style={ss.popupOverlay} onPress={onClose}>
        <Pressable style={[ss.popupBox, { backgroundColor: theme.cardBg }]} onPress={e => e.stopPropagation()}>
          <Text style={[ss.popupTitle, { color: theme.textSecondary }]}>Post visibility</Text>

          {([
            { value: 'everyone', label: 'Public',  icon: require('../../assets/icons/ad_public_grey.png') },
            { value: 'friends',  label: 'Friends', icon: require('../../assets/icons/ad_userppl_grey.png') },
            { value: 'private',  label: 'Private', icon: require('../../assets/icons/ad_private_grey.png') },
          ] as { value: PostPrivacy; label: string; icon: any }[]).map((opt, i) => (
            <View key={opt.value}>
              <TouchableOpacity
                style={ss.popupRow}
                onPress={() => { onSelect(opt.value); onClose(); }}
                testID={`button-privacy-${opt.value}`}
              >
                <Image source={opt.icon} style={ss.popupIcon} resizeMode="contain" />
                <Text style={[ss.popupLabel, { color: theme.textPrimary }, privacy === opt.value && { color: theme.accent, fontWeight: '700' }]}>
                  {opt.label}
                </Text>
                {privacy === opt.value && (
                  <View style={[ss.popupCheck, { backgroundColor: theme.accent }]} />
                )}
              </TouchableOpacity>
              {i < 2 && <View style={[ss.popupSep, { backgroundColor: theme.divider }]} />}
            </View>
          ))}

          <View style={[ss.popupSep, { marginVertical: 4, backgroundColor: theme.divider }]} />

          <TouchableOpacity
            style={ss.popupRow}
            onPress={onToggleReplies}
            testID="button-toggle-replies"
          >
            <Image source={require('../../assets/icons/ad_reply_grey.png')} style={ss.popupIcon} resizeMode="contain" />
            <Text style={[ss.popupLabel, { color: theme.textPrimary }]}>Allow replies</Text>
            <View style={[ss.toggleDot, allowReplies && { backgroundColor: theme.accent, borderColor: theme.accent }]} />
          </TouchableOpacity>
        </Pressable>
      </Pressable>
    </Modal>
  );
}

// ─── Privacy button icon helper (mirroring resetPrivacyDisplay) ───────────────
function privacyIcon(p: PostPrivacy) {
  if (p === 'private') return require('../../assets/icons/ad_private_grey.png');
  if (p === 'friends')  return require('../../assets/icons/ad_userppl_grey.png');
  return require('../../assets/icons/ad_public_grey.png');
}

// ─── SimplePostPreviewHolder (mirroring holder_simplepostpreview.xml) ─────────
function SimplePostPreview({ post }: { post: FeedPost }) {
  const theme = useAppTheme();
  return (
    <View style={[ss.postPreviewBox, { backgroundColor: theme.inputBg, borderLeftColor: theme.accent }]}>
      <Text style={[ss.postPreviewAuthor, { color: theme.accent }]} numberOfLines={1}>{post.authorUsername}</Text>
      <Text style={[ss.postPreviewContent, { color: theme.textSecondary }]} numberOfLines={2}>{post.comment}</Text>
    </View>
  );
}

// ─── Reputation Privileges (from /api/reputation/:username/level) ─────────────
// Mirrors ReputationLevelData.java — fetched at screen mount and cached locally.
interface ReputationPrivileges {
  level: number;
  levelName: string;
  publishPhoto: boolean;           // can attach photos to posts
  postCommentLikeUserWall: boolean; // can post / comment / like
  addToPhotoWall: boolean;          // can add to photo wall
}

const DEFAULT_PRIVILEGES: ReputationPrivileges = {
  level: 1, levelName: 'Newbie',
  publishPhoto: false, postCommentLikeUserWall: false, addToPhotoWall: false,
};

// ─── Create Post Modal (ShareboxFragment equivalent) ─────────────────────────
interface CreatePostModalProps {
  visible: boolean;
  onClose: () => void;
  onPosted: () => void;
  action?: CreateAction;
  originalPost?: FeedPost | null;
  prefix?: string;
  allUsernames?: string[];
  canPhoto?: boolean; // mirrors publishPhoto privilege
}

function CreatePostModal({
  visible,
  onClose,
  onPosted,
  action = 'new_post',
  originalPost = null,
  prefix = '',
  allUsernames = [],
  canPhoto = false,
}: CreatePostModalProps) {
  const [text, setText] = useState('');
  const [photo, setPhoto] = useState<{ uri: string } | null>(null);
  const [location, setLocation] = useState('');
  const [showLocation, setShowLocation] = useState(false);
  const [privacy, setPrivacy] = useState<PostPrivacy>('everyone');
  const [allowReplies, setAllowReplies] = useState(true);
  const [showPrivacy, setShowPrivacy] = useState(false);
  const [postToTwitter, setPostToTwitter] = useState(false);
  const [postToFacebook, setPostToFacebook] = useState(false);
  const [showEmojiPicker, setShowEmojiPicker] = useState(false);
  const [suggestions, setSuggestions] = useState<string[]>([]);
  const [suggestionType, setSuggestionType] = useState<'mention' | 'hashtag' | null>(null);
  const [posting, setPosting] = useState(false);
  const [currentUser, setCurrentUser] = useState('');

  const theme = useAppTheme();
  const inputRef = useRef<TextInput>(null);
  const slideAnim = useSlideUp(visible);

  useEffect(() => {
    getMe().then(me => { if (me) setCurrentUser(me.username); }).catch(() => {});
  }, []);

  useEffect(() => {
    if (visible) {
      setText(prefix || '');
      setPhoto(null);
      setLocation('');
      setShowLocation(false);
      setShowEmojiPicker(false);
      setSuggestions([]);
      setSuggestionType(null);
    }
  }, [visible, prefix]);

  const remaining = MAX_CHARS - text.length;
  const overLimit  = remaining < 0;
  const canPost = (text.trim().length > 0 || photo !== null || action === 'repost') && !overLimit && !posting;

  const charColor = overLimit ? C.charError : remaining <= 50 ? C.charWarn : C.charOk;

  // ── Text change: detect @ and # (mirroring onTextChanged) ─────────────────
  const handleTextChange = (val: string) => {
    setText(val);
    const lastChar = val.slice(-1);
    if (lastChar === '@') {
      setSuggestionType('mention');
      setSuggestions(allUsernames.map(u => `@${u}`));
    } else if (lastChar === '#') {
      setSuggestionType('hashtag');
      setSuggestions(HOT_TOPICS);
    } else {
      setSuggestionType(null);
      setSuggestions([]);
    }
  };

  const insertSuggestion = (s: string) => {
    const base = text.slice(0, -1); // remove the @ or #
    setText(base + s + ' ');
    setSuggestions([]);
    setSuggestionType(null);
  };

  const insertEmoji = (emoji: string) => {
    setText(prev => prev + emoji);
  };

  // ── Camera (mirroring attach_photo_button → takePhoto) ────────────────────
  const handleCamera = async () => {
    if (!canPhoto) {
      Alert.alert('Level terlalu rendah', 'Kamu perlu mencapai Level 2 (Newcomer) untuk bisa melampirkan foto.');
      return;
    }
    const { status } = await ImagePicker.requestCameraPermissionsAsync();
    if (status !== 'granted') return;
    const result = await ImagePicker.launchCameraAsync({
      mediaTypes: ImagePicker.MediaTypeOptions.Images,
      allowsEditing: true,
      quality: 0.8,
    });
    if (!result.canceled && result.assets[0]) {
      setPhoto({ uri: result.assets[0].uri });
      setShowEmojiPicker(false);
    }
  };

  // ── Gallery (mirroring attach_gallery_button → pickFromGallery) ────────────
  const handleGallery = async () => {
    if (!canPhoto) {
      Alert.alert('Level terlalu rendah', 'Kamu perlu mencapai Level 2 (Newcomer) untuk bisa melampirkan foto.');
      return;
    }
    const { status } = await ImagePicker.requestMediaLibraryPermissionsAsync();
    if (status !== 'granted') return;
    const result = await ImagePicker.launchImageLibraryAsync({
      mediaTypes: ImagePicker.MediaTypeOptions.Images,
      allowsEditing: true,
      quality: 0.8,
    });
    if (!result.canceled && result.assets[0]) {
      setPhoto({ uri: result.assets[0].uri });
      setShowEmojiPicker(false);
    }
  };

  // ── Upload photo to ImageKit CDN via imageserver endpoint ─────────────────
  const uploadPhoto = async (photoUri: string): Promise<string> => {
    const me = await getMe();
    if (!me) throw new Error('Sesi tidak valid. Silakan login ulang.');
    const base64 = await FileSystem.readAsStringAsync(photoUri, { encoding: 'base64' });
    const ext = photoUri.toLowerCase().includes('.png') ? 'png' : 'jpeg';
    const mimeType = ext === 'png' ? 'image/png' : 'image/jpeg';
    const imageKey = `feed_${me.username}_${Date.now()}`;
    const headers = await buildHeaders();
    const res = await fetch(`${API_BASE}/api/imageserver/upload`, {
      method: 'POST',
      headers: { ...headers as any, 'Content-Type': 'application/json' },
      body: JSON.stringify({ username: me.username, imageKey, mimeType, base64Data: base64 }),
    });
    const data = await res.json();
    if (!res.ok) {
      throw new Error(data?.error ?? `Upload gagal (${res.status}). Periksa konfigurasi ImageKit di server.`);
    }
    if (!data.url) throw new Error('Server tidak mengembalikan URL gambar.');
    return data.url;
  };

  // ── Post (mirroring handlePost) ────────────────────────────────────────────
  const handlePost = async () => {
    if (!canPost) return;
    setPosting(true);
    try {
      let imageUrl: string | undefined;
      if (photo) {
        try {
          imageUrl = await uploadPhoto(photo.uri);
        } catch (uploadErr: unknown) {
          const msg = uploadErr instanceof Error ? uploadErr.message : 'Upload foto gagal.';
          setPosting(false);
          Alert.alert(
            'Upload Foto Gagal',
            msg + '\n\nKamu bisa posting tanpa foto, atau batal untuk mencoba lagi.',
            [
              { text: 'Batal', style: 'cancel' },
              {
                text: 'Posting Tanpa Foto',
                onPress: async () => {
                  setPosting(true);
                  try {
                    const h = await buildHeaders();
                    const body: any = { comment: text.trim(), type: 1, privacy, allowReplies };
                    if (action === 'repost' && originalPost) body.repostId = originalPost.id;
                    await fetch(`${API_BASE}/api/feed/post`, {
                      method: 'POST',
                      headers: { ...h as any, 'Content-Type': 'application/json' },
                      body: JSON.stringify(body),
                    });
                    onPosted();
                    onClose();
                  } catch {} finally { setPosting(false); }
                },
              },
            ],
          );
          return;
        }
      }
      const body: any = {
        comment: text.trim(),
        type: 1,
        privacy,
        allowReplies,
        postToTwitter,
        postToFacebook,
        location: location.trim() || undefined,
        imageUrl,
      };
      if (action === 'repost' && originalPost) {
        body.repostId = originalPost.id;
      }
      const headers = await buildHeaders();
      await fetch(`${API_BASE}/api/feed/post`, {
        method: 'POST',
        headers: { ...headers as any, 'Content-Type': 'application/json' },
        body: JSON.stringify(body),
      });
      onPosted();
      onClose();
    } catch {
    } finally {
      setPosting(false);
    }
  };

  const titleText =
    action === 'reply'  ? 'Leave your comment' :
    action === 'repost' ? 'Repost'              : 'Share';

  const placeholderText =
    action === 'reply'  ? 'Leave your comment' :
    action === 'repost' ? 'Add your thoughts...' : 'Tell your story!';

  return (
    <>
      <Modal visible={visible} transparent animationType="none" onRequestClose={onClose} statusBarTranslucent>
        <KeyboardAvoidingView style={ss.modalOuter} behavior={Platform.OS === 'ios' ? 'padding' : 'height'}>
          <Pressable style={StyleSheet.absoluteFill} onPress={onClose} />

          <Animated.View style={[ss.createSheet, { backgroundColor: theme.cardBg, transform: [{ translateY: slideAnim }] }]}>
            {/* flex column: Header + Author + [flex content] + Footer */}

            {/* ── Header (mirroring main_sharebox_container top bar) ─────── */}
            <View style={[ss.modalHeader, { borderBottomColor: theme.divider }]}>
              <TouchableOpacity onPress={onClose} style={ss.modalCloseBtn} testID="button-close-create-post">
                <Image source={require('../../assets/icons/ic_cancel.png')} style={[ss.modalCloseIcon, { tintColor: theme.textSecondary }]} resizeMode="contain" />
              </TouchableOpacity>
              <Text style={[ss.modalTitle, { color: theme.textPrimary }]}>{titleText}</Text>
              <TouchableOpacity
                onPress={handlePost}
                disabled={!canPost}
                style={[ss.postSendBtn, { backgroundColor: canPost ? theme.accent : undefined }, !canPost && ss.postSendBtnDisabled]}
                testID="button-submit-post"
              >
                {posting
                  ? <ActivityIndicator color="#fff" size="small" />
                  : <Image source={require('../../assets/icons/ad_send_green.png')} style={ss.sendIcon} resizeMode="contain" />
                }
              </TouchableOpacity>
            </View>

            {/* ── Author row + Privacy button ────────────────────────────── */}
            <View style={ss.authorRow}>
              <Avatar username={currentUser || 'me'} size={38} />
              <View style={ss.authorMeta}>
                <Text style={[ss.authorName, { color: theme.textPrimary }]}>{currentUser || 'You'}</Text>
                {action !== 'reply' && (
                  <TouchableOpacity
                    style={[ss.privacyBtn, { backgroundColor: theme.inputBg }]}
                    onPress={() => setShowPrivacy(true)}
                    testID="button-privacy"
                  >
                    <Image source={privacyIcon(privacy)} style={ss.privacyIcon} resizeMode="contain" />
                    <Text style={[ss.privacyText, { color: theme.textSecondary }]}>
                      {privacy === 'everyone' ? 'Everyone' : privacy === 'friends' ? 'Friends' : 'Only me'}
                    </Text>
                  </TouchableOpacity>
                )}
              </View>
            </View>

            {/* ── Scrollable content area (mirroring ScrollViewEx) ──────── */}
            {/* View with flex:1 ensures ScrollView fills remaining space */}
            <View style={ss.contentArea}>
            <ScrollView style={ss.contentScroll} contentContainerStyle={ss.contentScrollInner} keyboardShouldPersistTaps="handled">

              {/* Autocomplete suggestions (@ mention / # hashtag) ──────── */}
              {suggestions.length > 0 && (
                <View style={[ss.suggestBox, { backgroundColor: theme.cardBg, borderColor: theme.border }]}>
                  {suggestions.map((s, i) => (
                    <TouchableOpacity
                      key={s}
                      style={[ss.suggestRow, i < suggestions.length - 1 && [ss.suggestRowBorder, { borderBottomColor: theme.divider }]]}
                      onPress={() => insertSuggestion(s)}
                      testID={`button-suggest-${s}`}
                    >
                      {suggestionType === 'mention' && (
                        <Image source={require('../../assets/icons/ad_avatar_grey.png')} style={[ss.suggestIcon, { tintColor: theme.textSecondary }]} resizeMode="contain" />
                      )}
                      <Text style={[ss.suggestText, { color: theme.textPrimary }]}>{s}</Text>
                    </TouchableOpacity>
                  ))}
                </View>
              )}

              {/* Main text field (mirroring share_field AutoCompleteTextViewEx) */}
              <TextInput
                ref={inputRef}
                style={[ss.shareField, { color: theme.textPrimary }]}
                placeholder={placeholderText}
                placeholderTextColor={theme.textSecondary}
                multiline
                value={text}
                onChangeText={handleTextChange}
                autoFocus
                textAlignVertical="top"
                testID="input-post-text"
              />

              {/* Photo thumbnail (mirroring thumbnail_box) ───────────────── */}
              {photo && (
                <View style={ss.thumbBox}>
                  <Image source={{ uri: photo.uri }} style={ss.thumbImage} resizeMode="cover" />
                  <TouchableOpacity
                    style={ss.thumbRemove}
                    onPress={() => setPhoto(null)}
                    testID="button-remove-photo"
                  >
                    <Image source={require('../../assets/icons/ic_cancel.png')} style={ss.thumbRemoveIcon} resizeMode="contain" />
                  </TouchableOpacity>
                </View>
              )}

              {/* Repost preview (mirroring SimplePostPreviewHolder for REPOST) */}
              {action === 'repost' && originalPost && (
                <SimplePostPreview post={originalPost} />
              )}

              {/* Location field (mirroring location_text) ───────────────── */}
              {showLocation && (
                <View style={[ss.locationRow, { backgroundColor: theme.inputBg }]}>
                  <Image source={require('../../assets/icons/ad_location_grey.png')} style={[ss.locationIcon, { tintColor: theme.textSecondary }]} resizeMode="contain" />
                  <TextInput
                    style={[ss.locationInput, { color: theme.textPrimary }]}
                    placeholder="Add location"
                    placeholderTextColor={theme.textSecondary}
                    value={location}
                    onChangeText={setLocation}
                    testID="input-location"
                  />
                  {location.length > 0 && (
                    <TouchableOpacity onPress={() => { setLocation(''); setShowLocation(false); }} testID="button-clear-location">
                      <Image source={require('../../assets/icons/ic_cancel.png')} style={{ width: 14, height: 14, tintColor: theme.textSecondary }} resizeMode="contain" />
                    </TouchableOpacity>
                  )}
                </View>
              )}
            </ScrollView>
            </View>{/* end contentArea */}

            {/* ── Char counter (mirroring char_count_container) ────────── */}
            <Text style={[ss.charCount, { color: charColor }]} testID="text-char-count">
              {remaining}
            </Text>

            {/* ── Footer separator (mirroring action_buttons_separator) ── */}
            <View style={[ss.footerSep, { backgroundColor: theme.divider }]} />

            {/* ── Action bar (mirroring action_buttons_container) ─────── */}
            <View style={ss.footerBar}>

              {/* Camera button (ad_camera_grey = attach_photo_button) */}
              {/* Locked (dimmed) when publishPhoto privilege is false (Level < 2) */}
              <TouchableOpacity
                style={[ss.footerBtn, !canPhoto && ss.footerBtnLocked]}
                onPress={handleCamera}
                testID="button-attach-camera"
              >
                <Image
                  source={require('../../assets/icons/ad_camera_grey.png')}
                  style={[ss.footerIcon, !canPhoto && ss.footerIconLocked]}
                  resizeMode="contain"
                />
              </TouchableOpacity>

              {/* Gallery button (ad_gallery_grey = attach_gallery_button) */}
              {/* Locked (dimmed) when publishPhoto privilege is false (Level < 2) */}
              <TouchableOpacity
                style={[ss.footerBtn, !canPhoto && ss.footerBtnLocked]}
                onPress={handleGallery}
                testID="button-attach-gallery"
              >
                <Image
                  source={require('../../assets/icons/ad_gallery_grey.png')}
                  style={[ss.footerIcon, !canPhoto && ss.footerIconLocked]}
                  resizeMode="contain"
                />
              </TouchableOpacity>

              {/* Emoticon button (ad_emoticon_grey = emoticon_button) */}
              <TouchableOpacity
                style={ss.footerBtn}
                onPress={() => { setShowEmojiPicker(p => !p); inputRef.current?.blur(); }}
                testID="button-emoticon"
              >
                <Image
                  source={require('../../assets/icons/ad_emoticon_grey.png')}
                  style={[ss.footerIcon, showEmojiPicker && { tintColor: theme.accent }]}
                  resizeMode="contain"
                />
              </TouchableOpacity>

              {/* Location button (ad_location_grey) */}
              {action !== 'reply' && (
                <TouchableOpacity style={ss.footerBtn} onPress={() => setShowLocation(p => !p)} testID="button-location">
                  <Image
                    source={require('../../assets/icons/ad_location_grey.png')}
                    style={[ss.footerIcon, showLocation && { tintColor: theme.accent }]}
                    resizeMode="contain"
                  />
                </TouchableOpacity>
              )}

              <View style={{ flex: 1 }} />

              {/* Twitter toggle (ad_twitter_grey/blue, for new_post & repost) */}
              {(action === 'new_post' || action === 'repost') && (
                <TouchableOpacity
                  style={ss.footerBtn}
                  onPress={() => setPostToTwitter(p => !p)}
                  testID="button-twitter"
                >
                  <Image
                    source={postToTwitter
                      ? require('../../assets/icons/ad_twitter_blue.png')
                      : require('../../assets/icons/ad_twitter_grey.png')}
                    style={ss.footerIcon}
                    resizeMode="contain"
                  />
                </TouchableOpacity>
              )}

              {/* Facebook toggle (ad_facebook_grey/blue, for new_post & repost) */}
              {(action === 'new_post' || action === 'repost') && (
                <TouchableOpacity
                  style={ss.footerBtn}
                  onPress={() => setPostToFacebook(p => !p)}
                  testID="button-facebook"
                >
                  <Image
                    source={postToFacebook
                      ? require('../../assets/icons/ad_facebook_blue.png')
                      : require('../../assets/icons/ad_facebook_grey.png')}
                    style={ss.footerIcon}
                    resizeMode="contain"
                  />
                </TouchableOpacity>
              )}
            </View>

            {/* ── Emoticon grid (mirroring emoticon_grid FrameLayout) ────── */}
            {showEmojiPicker && (
              <View style={[ss.emojiGrid, { backgroundColor: theme.inputBg }]}>
                <ScrollView horizontal={false} style={{ maxHeight: 180 }}>
                  <View style={ss.emojiWrap}>
                    {EMOJIS.map(emoji => (
                      <TouchableOpacity
                        key={emoji}
                        style={ss.emojiCell}
                        onPress={() => insertEmoji(emoji)}
                        testID={`button-emoji-${emoji}`}
                      >
                        <Text style={ss.emojiChar}>{emoji}</Text>
                      </TouchableOpacity>
                    ))}
                  </View>
                </ScrollView>
              </View>
            )}

            <View style={{ height: Platform.OS === 'ios' ? 30 : 12 }} />
          </Animated.View>
        </KeyboardAvoidingView>
      </Modal>

      {/* Privacy popup (mirroring createPrivacyOptionsPopupMenu) */}
      <PrivacyPopup
        visible={showPrivacy}
        privacy={privacy}
        allowReplies={allowReplies}
        onSelect={setPrivacy}
        onToggleReplies={() => setAllowReplies(p => !p)}
        onClose={() => setShowPrivacy(false)}
      />
    </>
  );
}

// ─── Share Popup (ShareToFragment equivalent) ─────────────────────────────────
const SHARE_ITEMS = [
  { key: 'chat',     label: 'Chat',     icon: require('../../assets/icons/ad_chat_grey.png') },
  { key: 'email',    label: 'Email',    icon: require('../../assets/icons/ad_email_grey.png') },
  { key: 'facebook', label: 'Facebook', icon: require('../../assets/icons/ad_facebook_outline.png') },
  { key: 'twitter',  label: 'Twitter',  icon: require('../../assets/icons/ad_twitter_outline.png') },
  { key: 'other',    label: 'Other',    icon: require('../../assets/icons/ad_share_dark_grey.png') },
];

function SharePopup({ visible, post, onClose }: { visible: boolean; post: FeedPost | null; onClose: () => void }) {
  const theme = useAppTheme();
  const slideAnim = useSlideUp(visible);
  const insets = useSafeAreaInsets();

  const handleShare = async (key: string) => {
    onClose();
    if (!post) return;
    const url = `https://migxchat.net/share/post/${post.authorUsername}/${post.id}`;
    const content = post.comment;
    switch (key) {
      case 'email':
        Linking.openURL(`mailto:?subject=Check this post on Migchat&body=${encodeURIComponent(content + '\n\n' + url)}`);
        break;
      case 'facebook':
        Linking.openURL(`https://www.facebook.com/sharer/sharer.php?u=${encodeURIComponent(url)}`);
        break;
      case 'twitter':
        Linking.openURL(`https://twitter.com/intent/tweet?url=${encodeURIComponent(url)}&text=${encodeURIComponent(content)}`);
        break;
      default:
        try { await Share.share({ message: `${content}\n\n${url}`, url }); } catch {}
    }
  };

  return (
    <Modal visible={visible} transparent animationType="none" onRequestClose={onClose} statusBarTranslucent>
      <KeyboardAvoidingView style={ss.modalOuter} behavior={Platform.OS === 'ios' ? 'padding' : 'height'}>
        <Pressable style={StyleSheet.absoluteFill} onPress={onClose} />
        <Animated.View style={[ss.modalSheet, { backgroundColor: theme.cardBg, transform: [{ translateY: slideAnim }] }]}>
          <View style={[ss.shareHeader, { borderBottomColor: theme.divider }]}>
            <Text style={[ss.shareHeaderText, { color: theme.textPrimary }]}>Share to</Text>
          </View>
          {SHARE_ITEMS.map((item, idx) => (
            <View key={item.key}>
              <TouchableOpacity style={ss.shareRow} onPress={() => handleShare(item.key)} activeOpacity={0.7} testID={`button-share-dest-${item.key}`}>
                <Image source={item.icon} style={ss.shareItemIcon} resizeMode="contain" />
                <Text style={[ss.shareItemLabel, { color: theme.textPrimary }]}>{item.label}</Text>
              </TouchableOpacity>
              {idx < SHARE_ITEMS.length - 1 && <View style={[ss.shareDivider, { backgroundColor: theme.divider }]} />}
            </View>
          ))}
          <View style={{ height: Math.max(insets.bottom, 16) }} />
        </Animated.View>
      </KeyboardAvoidingView>
    </Modal>
  );
}

// ─── Comment Modal (SinglePostFragment REPLY_TAB equivalent) ──────────────────
function CommentModal({
  visible, post, onClose, onCommented, allUsernames,
}: {
  visible: boolean;
  post: FeedPost | null;
  onClose: () => void;
  onCommented: (postId: string) => void;
  allUsernames: string[];
}) {
  const theme = useAppTheme();
  const [comments, setComments] = useState<PostComment[]>([]);
  const [text, setText] = useState('');
  const [loading, setLoading] = useState(false);
  const [sending, setSending] = useState(false);
  const [currentUser, setCurrentUser] = useState('');
  const [showReplyBox, setShowReplyBox] = useState(false);
  const slideAnim = useSlideUp(visible);
  const insets = useSafeAreaInsets();

  useEffect(() => {
    getMe().then(me => { if (me) setCurrentUser(me.username); }).catch(() => {});
  }, []);

  useEffect(() => {
    if (visible && post) { loadComments(post.id); setShowReplyBox(false); }
    else { setComments([]); setText(''); }
  }, [visible, post]);

  const loadComments = async (postId: string) => {
    setLoading(true);
    try {
      const headers = await buildHeaders();
      const res = await fetch(`${API_BASE}/api/feed/post/${postId}/comments`, { headers, credentials: 'include' });
      const data = await res.json();
      setComments(Array.isArray(data.comments) ? data.comments : []);
    } catch { setComments([]); }
    finally { setLoading(false); }
  };

  const handleSend = async () => {
    if (!text.trim() || sending || !post) return;
    setSending(true);
    try {
      const headers = await buildHeaders();
      const res = await fetch(`${API_BASE}/api/feed/post/${post.id}/comment`, {
        method: 'POST', credentials: 'include',
        headers: { ...headers as any, 'Content-Type': 'application/json' },
        body: JSON.stringify({ text: text.trim() }),
      });
      const data = await res.json();
      if (data.comment) {
        setComments(prev => [...prev, data.comment]);
        setText('');
        onCommented(post.id);
        setShowReplyBox(false);
      }
    } catch {} finally { setSending(false); }
  };

  if (!post) return null;

  return (
    <Modal visible={visible} transparent animationType="none" onRequestClose={onClose} statusBarTranslucent>
      <KeyboardAvoidingView style={ss.modalOuter} behavior={Platform.OS === 'ios' ? 'padding' : 'height'}>
        <Pressable style={StyleSheet.absoluteFill} onPress={onClose} />
        <Animated.View style={[ss.commentSheet, { backgroundColor: theme.cardBg, transform: [{ translateY: slideAnim }], paddingBottom: Math.max(insets.bottom, 16) }]}>

          <View style={[ss.modalHeader, { borderBottomColor: theme.divider }]}>
            <TouchableOpacity onPress={onClose} style={ss.modalCloseBtn} testID="button-close-comments">
              <Image source={require('../../assets/icons/ic_cancel.png')} style={[ss.modalCloseIcon, { tintColor: theme.textSecondary }]} resizeMode="contain" />
            </TouchableOpacity>
            <Text style={[ss.modalTitle, { color: theme.textPrimary }]}>Replies</Text>
            <Text style={[ss.commentCount, { color: theme.textSecondary }]}>{post.numComments}</Text>
          </View>

          {/* Original post preview (mirroring SinglePostFragment header) */}
          <View style={[ss.originalPostBox, { backgroundColor: theme.inputBg }]}>
            <Avatar username={post.authorUsername} size={32} />
            <View style={{ flex: 1, marginLeft: 10 }}>
              <Text style={[ss.username, { color: theme.accent }]}>{post.authorUsername}</Text>
              <Text style={[ss.originalPostText, { color: theme.textSecondary }]} numberOfLines={2}>{post.comment}</Text>
            </View>
          </View>

          <View style={[ss.commentListSep, { backgroundColor: theme.divider }]} />

          {loading ? (
            <View style={ss.commentCenter}><ActivityIndicator color={theme.accent} /></View>
          ) : comments.length === 0 ? (
            <View style={ss.commentCenter}>
              <Text style={[ss.noCommentText, { color: theme.textSecondary }]}>No replies yet. Be the first!</Text>
            </View>
          ) : (
            <FlatList
              data={comments}
              keyExtractor={c => c.id}
              style={ss.commentList}
              renderItem={({ item }) => (
                <View style={ss.commentItem} testID={`comment-item-${item.id}`}>
                  <Avatar username={item.authorUsername} size={34} />
                  <View style={[ss.commentBubble, { backgroundColor: theme.inputBg }]}>
                    <Text style={[ss.commentAuthor, { color: theme.accent }]}>{item.authorUsername}</Text>
                    <RichText text={item.text} style={[ss.commentText, { color: theme.textPrimary }]} />
                    <Text style={[ss.commentTs, { color: theme.textSecondary }]}>{timeAgo(item.createdAt)}</Text>
                  </View>
                </View>
              )}
              ItemSeparatorComponent={() => <View style={{ height: 6 }} />}
            />
          )}

          {/* Reply input bar (mirroring ShareboxFragment REPLY_POST mode) */}
          {showReplyBox ? (
            <View style={[ss.replyInputBar, { borderTopColor: theme.divider }]}>
              <Avatar username={currentUser || 'me'} size={32} />
              <TextInput
                style={[ss.replyInput, { backgroundColor: theme.inputBg, color: theme.textPrimary }]}
                placeholder="Write a reply..."
                placeholderTextColor={theme.textSecondary}
                value={text}
                onChangeText={setText}
                multiline
                maxLength={MAX_CHARS}
                autoFocus
                testID="input-comment-text"
              />
              <TouchableOpacity
                onPress={handleSend}
                disabled={!text.trim() || sending}
                style={[ss.replySendBtn, { backgroundColor: text.trim() && !sending ? theme.accent : undefined }, (!text.trim() || sending) && ss.replySendBtnDisabled]}
                testID="button-send-comment"
              >
                {sending
                  ? <ActivityIndicator color="#fff" size="small" />
                  : <Image source={require('../../assets/icons/ad_send_green.png')} style={ss.sendIcon} resizeMode="contain" />
                }
              </TouchableOpacity>
            </View>
          ) : (
            <TouchableOpacity
              style={[ss.replyTrigger, { borderTopColor: theme.divider, backgroundColor: theme.inputBg }]}
              onPress={() => setShowReplyBox(true)}
              testID="button-open-reply"
            >
              <Avatar username={currentUser || 'me'} size={30} />
              <Text style={[ss.replyTriggerText, { color: theme.textSecondary }]}>Write a reply...</Text>
              <Image source={require('../../assets/icons/ad_reply_grey.png')} style={[ss.replyTriggerIcon, { tintColor: theme.textSecondary }]} resizeMode="contain" />
            </TouchableOpacity>
          )}
        </Animated.View>
      </KeyboardAvoidingView>
    </Modal>
  );
}

// ─── PostImage ─────────────────────────────────────────────────────────────────
function PostImage({ uri, postId }: { uri: string; postId: string }) {
  const [loaded, setLoaded] = useState(false);
  const [err, setErr] = useState(false);
  if (err) return null;
  return (
    <View style={ss.postImageBox}>
      {!loaded && <ActivityIndicator color={C.green} style={StyleSheet.absoluteFill} />}
      <Image
        source={{ uri }}
        style={[ss.postImage, !loaded && { opacity: 0 }]}
        resizeMode="cover"
        onLoad={() => setLoaded(true)}
        onError={() => setErr(true)}
        testID={`img-post-${postId}`}
      />
    </View>
  );
}

// ─── PostCard ─────────────────────────────────────────────────────────────────
function PostCard({
  item, onReply, onRepost, onShare, canInteract,
}: {
  item: FeedPost;
  onReply: (post: FeedPost) => void;
  onRepost: (post: FeedPost) => void;
  onShare: (post: FeedPost) => void;
  canInteract: boolean; // mirrors postCommentLikeUserWall privilege
}) {
  const theme = useAppTheme();
  const [reposted, setReposted] = useState(false);
  const [localLikes, setLocalLikes] = useState(item.numLikes);

  useEffect(() => { setLocalLikes(item.numLikes); }, [item.numLikes]);

  const handleRepost = () => {
    if (!canInteract) {
      Alert.alert('Level terlalu rendah', 'Kamu perlu mencapai Level 2 (Newcomer) untuk bisa berinteraksi dengan postingan.');
      return;
    }
    if (reposted) return;
    setReposted(true);
    setLocalLikes(n => n + 1);
    onRepost(item);
  };

  const handleReply = () => {
    if (!canInteract) {
      Alert.alert('Level terlalu rendah', 'Kamu perlu mencapai Level 2 (Newcomer) untuk bisa berkomentar.');
      return;
    }
    onReply(item);
  };

  return (
    <View style={[ss.card, { backgroundColor: theme.cardBg }]} testID={`card-post-${item.id}`}>
      <View style={ss.cardHeader}>
        <Avatar username={item.authorUsername} displayPicture={item.authorDisplayPicture} size={44} />
        <View style={ss.cardMeta}>
          <Text style={[ss.username, { color: theme.textPrimary }]} testID={`text-username-${item.id}`}>{item.authorUsername}</Text>
          <Text style={[ss.timestamp, { color: theme.textSecondary }]} testID={`text-timestamp-${item.id}`}>{timeAgo(item.createdAt)}</Text>
        </View>
      </View>

      {item.comment.trim().length > 0 && (
        <RichText text={item.comment} style={[ss.postBody, { color: theme.textPrimary }]} testID={`text-body-${item.id}`} />
      )}

      {item.repostId && item.repostAuthorUsername ? (
        <View style={[ss.repostPreviewBox, { backgroundColor: theme.screenBg, borderColor: theme.divider }]}>
          <View style={ss.repostPreviewHeader}>
            <Avatar username={item.repostAuthorUsername} size={22} />
            <Text style={[ss.repostPreviewAuthor, { color: theme.textSecondary }]} numberOfLines={1}>{item.repostAuthorUsername}</Text>
          </View>
          {item.repostComment ? (
            <RichText text={item.repostComment} style={[ss.repostPreviewContent, { color: theme.textPrimary }]} />
          ) : null}
        </View>
      ) : null}

      {item.imageUrl ? (
        <PostImage uri={item.imageUrl} postId={item.id} />
      ) : null}

      {/* Action row: Reply | Repost | Share */}
      {/* Locked (dimmed) when postCommentLikeUserWall = false (Level 1 Newbie) */}
      <View style={[ss.cardActions, { borderTopColor: theme.divider }]}>
        <TouchableOpacity
          style={[ss.actionBtn, !canInteract && ss.actionBtnLocked]}
          onPress={handleReply}
          testID={`button-reply-${item.id}`}
          activeOpacity={0.7}
        >
          <Image
            source={require('../../assets/icons/ad_reply_grey.png')}
            style={[ss.actionIcon, !canInteract && ss.actionIconLocked]}
            resizeMode="contain"
          />
          <Text style={[ss.actionCount, { color: theme.textSecondary }, !canInteract && ss.actionCountLocked]}>{item.numComments}</Text>
        </TouchableOpacity>

        <View style={[ss.actionDivider, { backgroundColor: theme.divider }]} />

        <TouchableOpacity
          style={[ss.actionBtn, !canInteract && ss.actionBtnLocked]}
          onPress={handleRepost}
          testID={`button-repost-${item.id}`}
          activeOpacity={0.7}
        >
          <Image
            source={reposted ? require('../../assets/icons/ad_repost_green.png') : require('../../assets/icons/ad_repost_grey.png')}
            style={[ss.actionIcon, !canInteract && ss.actionIconLocked]}
            resizeMode="contain"
          />
          <Text style={[ss.actionCount, { color: theme.textSecondary }, reposted && ss.actionCountActive, !canInteract && ss.actionCountLocked]}>{localLikes}</Text>
        </TouchableOpacity>

        <View style={[ss.actionDivider, { backgroundColor: theme.divider }]} />

        <TouchableOpacity style={ss.actionBtn} onPress={() => onShare(item)} testID={`button-share-${item.id}`} activeOpacity={0.7}>
          <Image source={require('../../assets/icons/ad_share_grey.png')} style={[ss.actionIcon, { tintColor: theme.textSecondary }]} resizeMode="contain" />
          <Text style={[ss.actionCount, { color: theme.textSecondary }]}>Share</Text>
        </TouchableOpacity>
      </View>
    </View>
  );
}

const PAGE_SIZE = 15;

// ─── Feed Screen ──────────────────────────────────────────────────────────────
export default function FeedScreen() {
  const theme = useAppTheme();
  const [posts, setPosts] = useState<FeedPost[]>([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [loadingMore, setLoadingMore] = useState(false);
  const [hasMore, setHasMore] = useState(false);
  const [offset, setOffset] = useState(0);
  const [createVisible, setCreateVisible] = useState(false);
  const [sharePost, setSharePost] = useState<FeedPost | null>(null);
  const [commentPost, setCommentPost] = useState<FeedPost | null>(null);
  const [repostTarget, setRepostTarget] = useState<FeedPost | null>(null);

  // ── Reputation privileges (mirrors ReputationLevelData.java) ───────────────
  const [privileges, setPrivileges] = useState<ReputationPrivileges>(DEFAULT_PRIVILEGES);

  const allUsernames = [...new Set(posts.map(p => p.authorUsername))];

  const normalizePosts = (rawPosts: FeedPost[]) =>
    rawPosts.map(p => ({
      ...p,
      imageUrl: p.imageUrl
        ? (p.imageUrl.startsWith('http') ? p.imageUrl : `${API_BASE}${p.imageUrl}`)
        : null,
    }));

  const loadFeed = useCallback(async () => {
    try {
      const headers = await buildHeaders();
      const res = await fetch(`${API_BASE}/api/feed?limit=${PAGE_SIZE}&offset=0`, { headers, credentials: 'include' });
      const data = await res.json();
      const rawPosts: FeedPost[] = Array.isArray(data) ? data : (data.posts ?? []);
      setPosts(normalizePosts(rawPosts));
      setHasMore(data.hasMore ?? false);
      setOffset(PAGE_SIZE);
    } catch { setPosts([]); setHasMore(false); }
    finally { setLoading(false); setRefreshing(false); }
  }, []);

  const loadMore = useCallback(async () => {
    if (loadingMore || !hasMore) return;
    setLoadingMore(true);
    try {
      const headers = await buildHeaders();
      const res = await fetch(`${API_BASE}/api/feed?limit=${PAGE_SIZE}&offset=${offset}`, { headers, credentials: 'include' });
      const data = await res.json();
      const rawPosts: FeedPost[] = Array.isArray(data) ? data : (data.posts ?? []);
      setPosts(prev => [...prev, ...normalizePosts(rawPosts)]);
      setHasMore(data.hasMore ?? false);
      setOffset(prev => prev + PAGE_SIZE);
    } catch {}
    finally { setLoadingMore(false); }
  }, [loadingMore, hasMore, offset]);

  // Fetch the current user's level privileges from the reputation system.
  // Mirrors Java's ReputationServiceI.getUserLevel() / getLevelDataForScore().
  const loadPrivileges = useCallback(async () => {
    try {
      const me = await getMe();
      if (!me?.username) return;
      const res = await fetch(`${API_BASE}/api/reputation/${me.username}/level`, { credentials: 'include' });
      if (res.ok) {
        const data = await res.json();
        if (data.privileges) {
          setPrivileges({
            level:                    data.level,
            levelName:                data.levelName,
            publishPhoto:             data.privileges.publishPhoto        ?? false,
            postCommentLikeUserWall:  data.privileges.postCommentLikeUserWall ?? false,
            addToPhotoWall:           data.privileges.addToPhotoWall      ?? false,
          });
        }
      }
    } catch {}
  }, []);

  useEffect(() => { loadFeed(); loadPrivileges(); }, [loadFeed, loadPrivileges]);

  const handleCommented = (postId: string) => {
    setPosts(prev => prev.map(p => p.id === postId ? { ...p, numComments: p.numComments + 1 } : p));
  };

  const handleRepostLike = async (postId: string) => {
    try {
      await fetch(`${API_BASE}/api/feed/post/${postId}/like`, { method: 'POST', credentials: 'include' });
      setPosts(prev => prev.map(p => p.id === postId ? { ...p, numLikes: p.numLikes + 1 } : p));
    } catch {}
  };

  // ── FAB tap handler — gate by postCommentLikeUserWall privilege ───────────
  const handleFabPress = () => {
    if (!privileges.postCommentLikeUserWall) {
      Alert.alert(
        'Level terlalu rendah',
        `Kamu saat ini Level ${privileges.level} (${privileges.levelName}). Capai Level 2 (Newcomer) untuk bisa membuat postingan.`,
      );
      return;
    }
    setCreateVisible(true);
  };

  return (
    <View style={[ss.container, { backgroundColor: theme.screenBg }]}>
      {loading ? (
        <View style={ss.center}><ActivityIndicator color={theme.accent} size="large" /></View>
      ) : posts.length === 0 ? (
        <View style={ss.emptyState}>
          <Image source={require('../../assets/icons/ad_feed_grey.png')} style={[ss.emptyIcon, { tintColor: theme.textSecondary }]} resizeMode="contain" />
          <Text style={[ss.emptyTitle, { color: theme.textPrimary }]}>No posts yet</Text>
          <Text style={[ss.emptySubtitle, { color: theme.textSecondary }]}>Be the first to share something!</Text>
        </View>
      ) : (
        <FlatList
          data={posts}
          keyExtractor={item => item.id}
          renderItem={({ item }) => (
            <PostCard
              item={item}
              onReply={p => setCommentPost(p)}
              onRepost={p => { handleRepostLike(p.id); setRepostTarget(p); }}
              onShare={p => setSharePost(p)}
              canInteract={privileges.postCommentLikeUserWall}
            />
          )}
          contentContainerStyle={ss.list}
          refreshControl={
            <RefreshControl refreshing={refreshing} onRefresh={() => { setRefreshing(true); loadFeed(); }} tintColor={theme.accent} />
          }
          ItemSeparatorComponent={() => <View style={ss.sep} />}
          ListFooterComponent={
            hasMore ? (
              <TouchableOpacity
                style={[ss.loadMoreBtn, { backgroundColor: theme.cardBg, borderColor: theme.divider }]}
                onPress={loadMore}
                disabled={loadingMore}
                testID="button-load-more"
              >
                {loadingMore
                  ? <ActivityIndicator color={theme.accent} size="small" />
                  : <Text style={[ss.loadMoreText, { color: theme.accent }]}>Muat lebih banyak</Text>
                }
              </TouchableOpacity>
            ) : posts.length > 0 ? (
              <Text style={[ss.noMoreText, { color: theme.textSecondary }]}>Semua post sudah ditampilkan</Text>
            ) : null
          }
        />
      )}

      {/* FAB create post button — locked when postCommentLikeUserWall = false */}
      <TouchableOpacity
        style={[ss.fab, { backgroundColor: theme.accent }, !privileges.postCommentLikeUserWall && ss.fabLocked]}
        onPress={handleFabPress}
        activeOpacity={0.85}
        testID="button-create-post"
      >
        <Image source={require('../../assets/icons/ad_plus_white.png')} style={ss.fabIcon} resizeMode="contain" />
      </TouchableOpacity>

      {/* Create new post (action = new_post) */}
      <CreatePostModal
        visible={createVisible}
        onClose={() => setCreateVisible(false)}
        onPosted={loadFeed}
        action="new_post"
        allUsernames={allUsernames}
        canPhoto={privileges.publishPhoto}
      />

      {/* Repost modal (action = repost) */}
      <CreatePostModal
        visible={repostTarget !== null}
        onClose={() => setRepostTarget(null)}
        onPosted={() => { setRepostTarget(null); loadFeed(); }}
        action="repost"
        originalPost={repostTarget}
        allUsernames={allUsernames}
        canPhoto={privileges.publishPhoto}
      />

      {/* Share popup (ShareToFragment) */}
      <SharePopup visible={sharePost !== null} post={sharePost} onClose={() => setSharePost(null)} />

      {/* Comment/Reply modal (SinglePostFragment REPLY_TAB) */}
      <CommentModal
        visible={commentPost !== null}
        post={commentPost}
        onClose={() => setCommentPost(null)}
        onCommented={handleCommented}
        allUsernames={allUsernames}
      />
    </View>
  );
}

// ─── Styles ───────────────────────────────────────────────────────────────────
const ss = StyleSheet.create({
  container: { flex: 1, backgroundColor: C.bg },
  center:    { flex: 1, alignItems: 'center', justifyContent: 'center' },
  list:      { padding: 10, paddingBottom: 90 },
  sep:       { height: 6 },

  // ── PostCard ────────────────────────────────────────────────────────────────
  card: {
    backgroundColor: C.cardBg, borderRadius: 4, paddingTop: 14, paddingHorizontal: 14,
    shadowColor: '#000', shadowOpacity: 0.06, shadowRadius: 3, shadowOffset: { width: 0, height: 1 }, elevation: 1,
  },
  cardHeader:       { flexDirection: 'row', alignItems: 'center', marginBottom: 10 },
  cardMeta:         { flex: 1, marginLeft: 10 },
  avatarCircle:     { backgroundColor: C.avatarBg, alignItems: 'center', justifyContent: 'center', marginRight: 10 },
  avatarText:       { color: C.white, fontWeight: 'bold' },
  username:         { color: C.green, fontWeight: '700', fontSize: 14 },
  timestamp:        { color: C.ts, fontSize: 11, marginTop: 1 },
  postBody:         { color: C.text, fontSize: 14, lineHeight: 20, marginBottom: 12 },
  shareCornerBtn:   { padding: 6 },
  shareCornerIcon:  { width: 16, height: 16, tintColor: C.ts },
  cardActions:      { flexDirection: 'row', borderTopWidth: 1, borderTopColor: C.actionSep, alignItems: 'center' },
  actionBtn:        { flex: 1, flexDirection: 'row', alignItems: 'center', justifyContent: 'center', paddingVertical: 10, gap: 6 },
  actionDivider:    { width: 1, height: 22, backgroundColor: C.actionSep },
  actionIcon:       { width: 18, height: 18 },
  actionCount:      { color: C.grey, fontSize: 12, fontWeight: '500' },
  actionCountActive:{ color: C.green },

  // ── Empty state ─────────────────────────────────────────────────────────────
  emptyState:    { flex: 1, alignItems: 'center', justifyContent: 'center', padding: 40 },
  emptyIcon:     { width: 64, height: 64, tintColor: C.ts, marginBottom: 16 },
  emptyTitle:    { color: C.text, fontSize: 18, fontWeight: 'bold', marginBottom: 6 },
  emptySubtitle: { color: C.ts, fontSize: 13, textAlign: 'center', lineHeight: 18 },

  // ── FAB ─────────────────────────────────────────────────────────────────────
  fab: {
    position: 'absolute', bottom: 80, right: 20, width: 52, height: 52, borderRadius: 26,
    backgroundColor: C.fabBg, alignItems: 'center', justifyContent: 'center',
    shadowColor: '#000', shadowOpacity: 0.25, shadowRadius: 6, shadowOffset: { width: 0, height: 3 }, elevation: 6,
  },
  fabLocked:  { backgroundColor: '#BDBDBD' },
  fabIcon:    { width: 22, height: 22, tintColor: C.white },

  // ── Locked states (privilege gates — mirrors ReputationLevelData.java) ──────
  footerBtnLocked:    { opacity: 0.35 },
  footerIconLocked:   {},
  actionBtnLocked:    { opacity: 0.35 },
  actionIconLocked:   {},
  actionCountLocked:  {},

  // ── Shared modal ─────────────────────────────────────────────────────────────
  modalOuter: { flex: 1, justifyContent: 'flex-end', backgroundColor: C.overlayBg },
  modalSheet: {
    backgroundColor: C.white, borderTopLeftRadius: 14, borderTopRightRadius: 14,
    minHeight: 220, paddingBottom: Platform.OS === 'ios' ? 30 : 16,
  },
  modalHeader: {
    flexDirection: 'row', alignItems: 'center', paddingHorizontal: 14, paddingVertical: 12,
    borderBottomWidth: 1, borderBottomColor: C.sep,
  },
  modalCloseBtn:  { padding: 4, marginRight: 8 },
  modalCloseIcon: { width: 20, height: 20, tintColor: C.grey },
  modalTitle:     { flex: 1, color: C.text, fontWeight: '700', fontSize: 16 },
  sendIcon:       { width: 24, height: 24 },

  // ── Create Post sheet ────────────────────────────────────────────────────────
  // Explicit height so flex:1 on ScrollView inside works correctly
  createSheet: {
    backgroundColor: C.white, borderTopLeftRadius: 14, borderTopRightRadius: 14,
    height: SCREEN_H * 0.60,
    flexDirection: 'column',
  },

  // ── Author row ───────────────────────────────────────────────────────────────
  authorRow:  { flexDirection: 'row', alignItems: 'center', padding: 12, gap: 10 },
  authorMeta: { flex: 1 },
  authorName: { color: C.text, fontWeight: '700', fontSize: 14 },
  privacyBtn: {
    flexDirection: 'row', alignItems: 'center', gap: 4, marginTop: 3,
    backgroundColor: C.previewBg, borderRadius: 10, paddingHorizontal: 8, paddingVertical: 3, alignSelf: 'flex-start',
  },
  privacyIcon: { width: 14, height: 14 },
  privacyText: { color: C.grey, fontSize: 11 },

  // ── Content area wrapper (flex:1 so it fills space between author row and footer)
  contentArea:       { flex: 1 },
  contentScroll:     { flex: 1 },
  contentScrollInner:{ paddingHorizontal: 14, paddingBottom: 8, flexGrow: 1 },

  // ── Autocomplete suggestions ─────────────────────────────────────────────────
  suggestBox: {
    backgroundColor: C.white, borderWidth: 1, borderColor: C.sep, borderRadius: 6,
    marginBottom: 6, maxHeight: 130,
  },
  suggestRow:       { flexDirection: 'row', alignItems: 'center', padding: 10, gap: 8 },
  suggestRowBorder: { borderBottomWidth: 1, borderBottomColor: C.sep },
  suggestIcon:      { width: 16, height: 16, tintColor: C.grey },
  suggestText:      { color: C.text, fontSize: 13 },

  // ── Text field (share_field) ─────────────────────────────────────────────────
  shareField: {
    minHeight: 120, color: C.text, fontSize: 15, lineHeight: 22,
    paddingTop: 8, paddingBottom: 8,
    textAlignVertical: 'top',
  },

  // ── Photo thumbnail (thumbnail_box) ──────────────────────────────────────────
  thumbBox:       { width: 100, height: 100, marginBottom: 10, borderRadius: 6, overflow: 'hidden' },
  thumbImage:     { width: 100, height: 100 },
  thumbRemove:    { position: 'absolute', top: 4, right: 4, backgroundColor: 'rgba(0,0,0,0.5)', borderRadius: 10, padding: 3 },
  thumbRemoveIcon:{ width: 12, height: 12, tintColor: C.white },

  // ── Repost preview (SimplePostPreviewHolder) ─────────────────────────────────
  postPreviewBox: {
    backgroundColor: C.previewBg, borderRadius: 6, padding: 10,
    marginBottom: 10, borderLeftWidth: 3, borderLeftColor: C.green,
  },
  postPreviewAuthor:  { color: C.green, fontWeight: '700', fontSize: 13, marginBottom: 2 },
  postPreviewContent: { color: C.grey, fontSize: 12, lineHeight: 17 },

  // ── Repost preview inside PostCard ────────────────────────────────────────────
  repostPreviewBox: {
    backgroundColor: C.previewBg, borderRadius: 8, padding: 10,
    marginBottom: 12, borderWidth: 1, borderColor: C.sep,
  },
  repostPreviewHeader: { flexDirection: 'row', alignItems: 'center', gap: 6, marginBottom: 5 },
  repostPreviewAuthor: { color: C.green, fontWeight: '700', fontSize: 13, flex: 1 },
  repostPreviewContent:{ color: C.grey, fontSize: 13, lineHeight: 18 },

  // ── Location row (location_text) ─────────────────────────────────────────────
  locationRow: {
    flexDirection: 'row', alignItems: 'center', gap: 6,
    backgroundColor: C.previewBg, borderRadius: 6, paddingHorizontal: 10, paddingVertical: 6, marginBottom: 8,
  },
  locationIcon:  { width: 16, height: 16, tintColor: C.grey },
  locationInput: { flex: 1, color: C.text, fontSize: 13 },

  // ── Char counter (char_count_container) ──────────────────────────────────────
  charCount: { textAlign: 'right', fontSize: 12, paddingHorizontal: 14, paddingBottom: 4 },

  // ── Footer separator (action_buttons_separator) ───────────────────────────────
  footerSep: { height: 1, backgroundColor: C.actionSep },

  // ── Footer bar (action_buttons_container) ────────────────────────────────────
  footerBar:      { flexDirection: 'row', alignItems: 'center', paddingHorizontal: 6, paddingTop: 4, paddingBottom: Platform.OS === 'android' ? 20 : 8, minHeight: 44 },
  footerBtn:      { padding: 8 },
  footerIcon:     { width: 22, height: 22 },
  footerIconActive:{ tintColor: C.green },

  // ── Send button ──────────────────────────────────────────────────────────────
  postSendBtn:        { backgroundColor: C.green, borderRadius: 20, paddingHorizontal: 14, paddingVertical: 6, alignItems: 'center', justifyContent: 'center', minWidth: 44 },
  postSendBtnDisabled:{ backgroundColor: '#BDBDBD' },

  // ── Emoji grid (emoticon_grid) ────────────────────────────────────────────────
  emojiGrid: { backgroundColor: C.previewBg, paddingVertical: 8, paddingHorizontal: 6, maxHeight: 180 },
  emojiWrap: { flexDirection: 'row', flexWrap: 'wrap', justifyContent: 'flex-start' },
  emojiCell: { width: 40, height: 40, alignItems: 'center', justifyContent: 'center' },
  emojiChar: { fontSize: 24 },

  // ── Privacy popup ─────────────────────────────────────────────────────────────
  popupOverlay:  { flex: 1, backgroundColor: 'rgba(0,0,0,0.4)', justifyContent: 'center', alignItems: 'center' },
  popupBox:      { backgroundColor: C.white, borderRadius: 12, width: 260, paddingVertical: 8, shadowColor: '#000', shadowOpacity: 0.2, shadowRadius: 8, elevation: 8 },
  popupTitle:    { color: C.ts, fontSize: 12, fontWeight: '600', textTransform: 'uppercase', paddingHorizontal: 16, paddingVertical: 8 },
  popupRow:      { flexDirection: 'row', alignItems: 'center', paddingHorizontal: 16, paddingVertical: 12, gap: 12 },
  popupIcon:     { width: 20, height: 20 },
  popupLabel:    { flex: 1, color: C.text, fontSize: 15 },
  popupLabelActive:{ color: C.green, fontWeight: '700' },
  popupCheck:    { width: 8, height: 8, borderRadius: 4, backgroundColor: C.green },
  popupSep:      { height: 1, backgroundColor: C.privacySep, marginHorizontal: 16 },
  toggleDot:     { width: 32, height: 18, borderRadius: 9, backgroundColor: '#CCC', borderWidth: 2, borderColor: '#CCC' },
  toggleDotOn:   { backgroundColor: C.green, borderColor: C.green },

  // ── Share popup ───────────────────────────────────────────────────────────────
  shareHeader:     { paddingHorizontal: 16, paddingVertical: 14, borderBottomWidth: 1, borderBottomColor: C.sep },
  shareHeaderText: { color: C.text, fontWeight: '700', fontSize: 16 },
  shareRow:        { flexDirection: 'row', alignItems: 'center', paddingHorizontal: 16, paddingVertical: 14, gap: 14 },
  shareItemIcon:   { width: 28, height: 28 },
  shareItemLabel:  { color: C.text, fontSize: 15 },
  shareDivider:    { height: 1, backgroundColor: C.sep, marginHorizontal: 16 },

  // ── Post image ────────────────────────────────────────────────────────────────
  postImageBox: {
    width: '100%', aspectRatio: 16 / 9, borderRadius: 6, overflow: 'hidden',
    backgroundColor: C.previewBg, marginBottom: 12,
  },
  postImage: { width: '100%', height: '100%' },

  commentSheet: {
    backgroundColor: C.white, borderTopLeftRadius: 14, borderTopRightRadius: 14,
    minHeight: SCREEN_H * 0.60,
    maxHeight: SCREEN_H * 0.88,
  },
  commentCount:    { color: C.ts, fontSize: 13 },
  originalPostBox: { flexDirection: 'row', alignItems: 'flex-start', padding: 14, backgroundColor: C.previewBg },
  originalPostText:{ color: C.grey, fontSize: 13, marginTop: 2, lineHeight: 18 },
  commentListSep:  { height: 1, backgroundColor: C.sep },
  commentList:     { flex: 1, paddingHorizontal: 14, paddingTop: 10 },
  commentCenter:   { flex: 1, alignItems: 'center', justifyContent: 'center', paddingVertical: 30 },
  noCommentText:   { color: C.ts, fontSize: 14 },
  commentItem:     { flexDirection: 'row', alignItems: 'flex-start' },
  commentBubble:   { flex: 1, marginLeft: 10, backgroundColor: C.previewBg, borderRadius: 10, padding: 10 },
  commentAuthor:   { color: C.green, fontWeight: '700', fontSize: 13 },
  commentText:     { color: C.text, fontSize: 13, marginTop: 2, lineHeight: 18 },
  commentTs:       { color: C.ts, fontSize: 10, marginTop: 4 },

  // ── Load more footer ──────────────────────────────────────────────────────────
  loadMoreBtn: {
    marginTop: 8, marginHorizontal: 10, marginBottom: 4,
    borderRadius: 8, borderWidth: 1, paddingVertical: 14,
    alignItems: 'center', justifyContent: 'center',
  },
  loadMoreText: { fontSize: 14, fontWeight: '600' },
  noMoreText:   { textAlign: 'center', fontSize: 12, paddingVertical: 16, opacity: 0.6 },

  // ── Reply input bar ───────────────────────────────────────────────────────────
  replyInputBar:  { flexDirection: 'row', alignItems: 'flex-end', padding: 10, gap: 8, borderTopWidth: 1, borderTopColor: C.sep },
  replyInput:     { flex: 1, backgroundColor: C.previewBg, borderRadius: 18, paddingHorizontal: 14, paddingVertical: 8, fontSize: 14, color: C.text, maxHeight: 100 },
  replySendBtn:        { backgroundColor: C.green, borderRadius: 18, padding: 8, alignItems: 'center', justifyContent: 'center' },
  replySendBtnDisabled:{ backgroundColor: '#BDBDBD' },
  replyTrigger:     { flexDirection: 'row', alignItems: 'center', padding: 12, gap: 8, borderTopWidth: 1, borderTopColor: C.sep, backgroundColor: C.previewBg },
  replyTriggerText: { flex: 1, color: '#BDBDBD', fontSize: 14 },
  replyTriggerIcon: { width: 18, height: 18, tintColor: C.grey },
});
