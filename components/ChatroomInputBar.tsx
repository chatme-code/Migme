import { useEffect, useRef, useState } from 'react';
import type { MutableRefObject, RefObject } from 'react';
import {
  Animated,
  Image,
  Keyboard,
  NativeSyntheticEvent,
  Platform,
  Pressable,
  StyleSheet,
  TextInput,
  TextInputContentSizeChangeEventData,
  TouchableOpacity,
  View,
} from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { useAppTheme, type AppTheme } from '../services/themeContext';

const ICON_GIFT = require('../assets/icons/ad_chat_inputgift.png');
const ICON_SEND_ON = require('../assets/icons/ad_send_green.png');
const ICON_SEND_OFF = require('../assets/icons/ad_send_grey.png');

interface Props {
  inputRef: RefObject<TextInput | null>;
  inputText: string;
  inputTextRef: MutableRefObject<string>;
  onChangeInputText: (text: string) => void;
  onOpenPicker: () => void;
  onSendMessage: () => void;
}

function makePalette(appTheme: AppTheme) {
  return {
    dropBg: appTheme.cardBg,
    inputBg: appTheme.inputBg,
    inputBorder: appTheme.border,
    text: appTheme.textPrimary,
    ts: appTheme.textSecondary,
  };
}

export default function ChatroomInputBar({
  inputRef,
  inputText,
  inputTextRef,
  onChangeInputText,
  onOpenPicker,
  onSendMessage,
}: Props) {
  const insets = useSafeAreaInsets();
  const C = makePalette(useAppTheme());
  const styles = makeStyles(C);
  const [inputHeight, setInputHeight] = useState(36);
  const keyboardOffset = useRef(new Animated.Value(0)).current;
  const sendLockRef = useRef(false);

  useEffect(() => {
    const showEvent = Platform.OS === 'ios' ? 'keyboardWillShow' : 'keyboardDidShow';
    const hideEvent = Platform.OS === 'ios' ? 'keyboardWillHide' : 'keyboardDidHide';

    const showSub = Keyboard.addListener(showEvent, (event) => {
      const height = event.endCoordinates?.height ?? 0;
      Animated.timing(keyboardOffset, {
        toValue: -height,
        duration: Platform.OS === 'ios' ? 250 : 220,
        useNativeDriver: true,
      }).start();
    });

    const hideSub = Keyboard.addListener(hideEvent, () => {
      Animated.timing(keyboardOffset, {
        toValue: 0,
        duration: Platform.OS === 'ios' ? 200 : 180,
        useNativeDriver: true,
      }).start(() => keyboardOffset.setValue(0));
    });

    return () => {
      showSub.remove();
      hideSub.remove();
    };
  }, [keyboardOffset]);

  const handleContentSizeChange = (event: NativeSyntheticEvent<TextInputContentSizeChangeEventData>) => {
    const nextHeight = Math.min(Math.max(36, event.nativeEvent.contentSize.height), 100);
    setInputHeight(nextHeight);
  };

  const sendAndDismiss = () => {
    if (sendLockRef.current) return;
    if (!inputTextRef.current.trim()) return;
    sendLockRef.current = true;
    onSendMessage();
    setInputHeight(36);
    inputRef.current?.blur();
    Keyboard.dismiss();
    setTimeout(() => {
      sendLockRef.current = false;
    }, 180);
  };

  return (
    <Animated.View
      style={[
        styles.wrapper,
        { paddingBottom: insets.bottom > 0 ? insets.bottom : 8 },
        { transform: [{ translateY: keyboardOffset }] },
      ]}
    >
      <View style={styles.inputBar}>
        <TouchableOpacity
          style={styles.inputIconBtn}
          onPress={onOpenPicker}
          testID="button-gift"
        >
          <Image source={ICON_GIFT} style={styles.inputIcon} resizeMode="contain" />
        </TouchableOpacity>

        <View style={styles.inputWrap}>
          <TextInput
            ref={inputRef}
            style={[styles.input, { height: inputHeight }]}
            placeholder="Ketik pesan..."
            placeholderTextColor={C.ts}
            value={inputText}
            onChangeText={(text) => {
              inputTextRef.current = text;
              onChangeInputText(text);
            }}
            multiline
            maxLength={500}
            returnKeyType="send"
            blurOnSubmit={false}
            textAlignVertical="top"
            scrollEnabled={inputHeight >= 100}
            onContentSizeChange={handleContentSizeChange}
            onSubmitEditing={sendAndDismiss}
            testID="input-message"
          />
        </View>

        <Pressable
          style={styles.sendBtn}
          onTouchStart={sendAndDismiss}
          onPressIn={sendAndDismiss}
          android_ripple={null}
          testID="button-send"
        >
          <Image
            source={inputText.trim() ? ICON_SEND_ON : ICON_SEND_OFF}
            style={styles.sendIcon}
            resizeMode="contain"
          />
        </Pressable>
      </View>
    </Animated.View>
  );
}

const makeStyles = (C: ReturnType<typeof makePalette>) => StyleSheet.create({
  wrapper: {
    backgroundColor: C.dropBg,
    borderTopWidth: StyleSheet.hairlineWidth,
    borderTopColor: C.inputBorder,
  },
  inputBar: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: C.dropBg,
    paddingHorizontal: 8,
    paddingVertical: 8,
    gap: 6,
  },
  inputIconBtn: {
    padding: 6,
    alignItems: 'center',
    justifyContent: 'center',
  },
  inputIcon: {
    width: 22,
    height: 22,
  },
  inputWrap: {
    flex: 1,
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: C.inputBg,
    borderRadius: 20,
    paddingHorizontal: 14,
    paddingVertical: 2,
    minHeight: 36,
    maxHeight: 100,
  },
  input: {
    flex: 1,
    fontSize: 14,
    color: C.text,
    maxHeight: 100,
    minHeight: 36,
    paddingVertical: Platform.OS === 'ios' ? 8 : 6,
    paddingLeft: 0,
    fontFamily: 'Roboto_400Regular',
  },
  sendBtn: {
    padding: 6,
    alignItems: 'center',
    justifyContent: 'center',
  },
  sendIcon: {
    width: 28,
    height: 28,
  },
});