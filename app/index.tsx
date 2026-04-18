import { useRouter } from 'expo-router';
import { useEffect, useState } from 'react';
import {
  ActivityIndicator,
  Alert,
  Image,
  KeyboardAvoidingView,
  Linking,
  Platform,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  TouchableOpacity,
  View,
} from 'react-native';
import { getMe, login } from '../services/auth';
import { useAppTheme } from '../services/themeContext';

export default function LoginScreen() {
  const router = useRouter();
  const theme = useAppTheme();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [checking, setChecking] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    getMe().then(user => {
      if (user) router.replace('/(home)/feed');
      else setChecking(false);
    });
  }, []);

  const handleLogin = async () => {
    if (!username.trim() || !password.trim()) {
      setError('Username dan password wajib diisi.');
      return;
    }
    setError('');
    setLoading(true);
    try {
      await login(username.trim(), password);
      router.replace('/(home)/feed');
    } catch (e: unknown) {
      const msg = e instanceof Error ? e.message : 'Login gagal. Coba lagi.';
      if (msg.toLowerCase().includes('suspended')) {
        Alert.alert('Account Suspended', 'Your account has been suspended. Please contact support for assistance.');
      } else {
        setError(msg);
      }
    } finally {
      setLoading(false);
    }
  };

  if (checking) {
    return (
      <View style={{ flex: 1, alignItems: 'center', justifyContent: 'center', backgroundColor: theme.headerBg }}>
        <ActivityIndicator color={theme.accent} size="large" />
      </View>
    );
  }

  const handleForgotPassword = () => {
    router.push('/forgot-password');
  };

  const handleFacebookConnect = () => {
    Alert.alert('Facebook', 'Fitur ini belum tersedia.');
  };

  const handlePeekInside = () => {
    Linking.openURL('https://web.migxchat.net').catch(() => {
      Alert.alert('Error', 'Tidak dapat membuka browser. Coba lagi.');
    });
  };

  const styles = makeStyles(theme);

  return (
    <KeyboardAvoidingView
      style={styles.container}
      behavior={Platform.OS === 'ios' ? 'padding' : undefined}
    >
      <ScrollView style={styles.scroll} keyboardShouldPersistTaps="handled">
        <View style={styles.header}>
          <View style={styles.logoContainer}>
            <Image
              source={require('../assets/logo_new.png')}
              style={styles.logo}
              resizeMode="contain"
            />
            <Text style={styles.logoText}>Migchat</Text>
          </View>

          <View style={styles.tabBar}>
            <TouchableOpacity style={styles.tabActive} testID="tab-login">
              <Text style={styles.tabTextActive}>LOGIN</Text>
              <View style={styles.tabIndicator} />
            </TouchableOpacity>

            <TouchableOpacity
              style={styles.tabInactive}
              testID="tab-register"
              onPress={() => router.push('/register')}
            >
              <Text style={styles.tabTextInactive}>REGISTER</Text>
            </TouchableOpacity>
          </View>
        </View>

        <View style={styles.botSection}>
          <View style={styles.botAvatar}>
            <Text style={styles.botEmoji}>🤖</Text>
          </View>
          <Text style={styles.botText}>
            Selamat datang kembali! Masukkan kredensial kamu untuk masuk ke akun migchat.
          </Text>
        </View>

        {error ? (
          <View style={styles.errorBanner}>
            <Text style={styles.errorText}>{error}</Text>
          </View>
        ) : null}

        <View style={styles.formSection}>
          <View style={styles.inputRow}>
            <TextInput
              style={styles.input}
              placeholder="Username"
              placeholderTextColor={theme.textSecondary}
              value={username}
              onChangeText={(v) => { setUsername(v); setError(''); }}
              autoCapitalize="none"
              returnKeyType="next"
              testID="input-username"
              editable={!loading}
            />
          </View>

          <View style={styles.separator} />

          <View style={styles.inputRow}>
            <TextInput
              style={[styles.input, styles.inputFlex]}
              placeholder="Password"
              placeholderTextColor={theme.textSecondary}
              value={password}
              onChangeText={(v) => { setPassword(v); setError(''); }}
              secureTextEntry={!showPassword}
              returnKeyType="done"
              onSubmitEditing={handleLogin}
              testID="input-password"
              editable={!loading}
            />
            {loading ? (
              <ActivityIndicator color={theme.accent} style={styles.arrowBtn} />
            ) : (
              <TouchableOpacity
                style={styles.arrowBtn}
                onPress={handleLogin}
                testID="button-login"
              >
                <Text style={styles.arrowText}>▶</Text>
              </TouchableOpacity>
            )}
          </View>

          <View style={styles.separator} />

          <View style={styles.optionsRow}>
            <TouchableOpacity
              onPress={() => setShowPassword(!showPassword)}
              testID="button-showhide"
            >
              <Text style={styles.accentLink}>
                {showPassword ? 'Sembunyikan' : 'Tampilkan'}
              </Text>
            </TouchableOpacity>
            <TouchableOpacity onPress={handleForgotPassword} testID="button-forgot">
              <Text style={styles.accentLink}>Lupa?</Text>
            </TouchableOpacity>
          </View>

          <View style={styles.orRow}>
            <View style={styles.orLine} />
            <Text style={styles.orText}>atau</Text>
            <View style={styles.orLine} />
          </View>

          <TouchableOpacity
            style={styles.fbButton}
            onPress={handleFacebookConnect}
            testID="button-facebook"
          >
            <Text style={styles.fbIcon}>f</Text>
            <Text style={styles.fbText}>Connect with Facebook</Text>
          </TouchableOpacity>

          <View style={styles.spacer} />

          <View style={styles.legalRow}>
            <Text style={styles.legalText}>Dengan login kamu menyetujui </Text>
            <TouchableOpacity testID="link-terms">
              <Text style={styles.accentLink}>Syarat Penggunaan</Text>
            </TouchableOpacity>
          </View>
          <View style={styles.legalRow}>
            <Text style={styles.legalText}>dan </Text>
            <TouchableOpacity testID="link-privacy">
              <Text style={styles.accentLink}>Kebijakan Privasi</Text>
            </TouchableOpacity>
          </View>

          <View style={styles.spacer} />

          <TouchableOpacity
            style={styles.peekButton}
            onPress={handlePeekInside}
            testID="button-peek"
          >
            <Text style={styles.peekText}>PEEK INSIDE</Text>
          </TouchableOpacity>

          <View style={styles.spacer} />
        </View>
      </ScrollView>
    </KeyboardAvoidingView>
  );
}

function makeStyles(theme: ReturnType<typeof useAppTheme>) {
  return StyleSheet.create({
    container: {
      flex: 1,
      backgroundColor: theme.screenBg,
    },
    scroll: {
      flex: 1,
    },
    header: {
      backgroundColor: theme.headerBg,
      paddingTop: 48,
    },
    logoContainer: {
      alignItems: 'center',
      paddingVertical: 20,
    },
    logo: {
      width: 130,
      height: 130,
    },
    logoText: {
      color: '#FFFFFF',
      fontSize: 28,
      fontWeight: 'bold',
      letterSpacing: 2,
      marginTop: 2,
    },
    tabBar: {
      flexDirection: 'row',
      marginTop: 8,
    },
    tabActive: {
      flex: 1,
      alignItems: 'center',
      paddingBottom: 12,
      paddingTop: 8,
      position: 'relative',
    },
    tabInactive: {
      flex: 1,
      alignItems: 'center',
      paddingBottom: 12,
      paddingTop: 8,
    },
    tabTextActive: {
      color: theme.accent,
      fontWeight: 'bold',
      fontSize: 14,
      letterSpacing: 1,
    },
    tabTextInactive: {
      color: '#FFFFFF',
      fontSize: 14,
      letterSpacing: 1,
      opacity: 0.8,
    },
    tabIndicator: {
      position: 'absolute',
      bottom: 0,
      left: '50%',
      marginLeft: -6,
      width: 0,
      height: 0,
      borderLeftWidth: 6,
      borderRightWidth: 6,
      borderBottomWidth: 8,
      borderLeftColor: 'transparent',
      borderRightColor: 'transparent',
      borderBottomColor: theme.accent,
    },
    botSection: {
      backgroundColor: theme.drawerBg,
      flexDirection: 'row',
      alignItems: 'center',
      paddingHorizontal: 20,
      paddingVertical: 16,
      minHeight: 80,
    },
    botAvatar: {
      width: 52,
      height: 52,
      borderRadius: 26,
      backgroundColor: 'rgba(255,255,255,0.15)',
      alignItems: 'center',
      justifyContent: 'center',
      marginRight: 14,
    },
    botEmoji: {
      fontSize: 28,
    },
    botText: {
      flex: 1,
      color: theme.isDark ? theme.textPrimary : '#FFFFFF',
      fontSize: 13,
      lineHeight: 18,
    },
    errorBanner: {
      backgroundColor: '#FFEDED',
      borderLeftWidth: 3,
      borderLeftColor: '#C64F44',
      paddingHorizontal: 16,
      paddingVertical: 10,
      marginHorizontal: 0,
    },
    errorText: {
      color: '#C64F44',
      fontSize: 13,
    },
    formSection: {
      backgroundColor: theme.cardBg,
    },
    inputRow: {
      flexDirection: 'row',
      alignItems: 'center',
      paddingHorizontal: 20,
      paddingVertical: 4,
    },
    input: {
      flex: 1,
      paddingVertical: 14,
      fontSize: 15,
      color: theme.textPrimary,
    },
    inputFlex: {
      flex: 1,
    },
    arrowBtn: {
      paddingLeft: 16,
      paddingRight: 8,
      paddingVertical: 14,
    },
    arrowText: {
      color: theme.accent,
      fontSize: 20,
    },
    separator: {
      height: 1,
      backgroundColor: theme.divider,
      marginHorizontal: 24,
    },
    optionsRow: {
      flexDirection: 'row',
      justifyContent: 'space-between',
      paddingHorizontal: 20,
      paddingVertical: 10,
    },
    accentLink: {
      color: theme.accent,
      fontSize: 13,
      fontWeight: '500',
    },
    orRow: {
      flexDirection: 'row',
      alignItems: 'center',
      paddingHorizontal: 40,
      marginVertical: 12,
    },
    orLine: {
      flex: 1,
      height: 1,
      backgroundColor: theme.divider,
    },
    orText: {
      color: theme.textSecondary,
      fontSize: 12,
      marginHorizontal: 12,
    },
    fbButton: {
      flexDirection: 'row',
      alignItems: 'center',
      justifyContent: 'center',
      marginHorizontal: 20,
      paddingVertical: 12,
      borderRadius: 4,
      backgroundColor: '#3D5A98',
      marginBottom: 4,
    },
    fbIcon: {
      color: '#FFFFFF',
      fontWeight: 'bold',
      fontSize: 18,
      marginRight: 10,
      fontStyle: 'italic',
    },
    fbText: {
      color: '#FFFFFF',
      fontSize: 14,
      fontWeight: '500',
    },
    spacer: {
      height: 12,
    },
    legalRow: {
      flexDirection: 'row',
      justifyContent: 'center',
      flexWrap: 'wrap',
      paddingHorizontal: 16,
    },
    legalText: {
      color: theme.textSecondary,
      fontSize: 11,
    },
    peekButton: {
      alignSelf: 'center',
      borderWidth: 1.5,
      borderColor: theme.accent,
      borderRadius: 20,
      paddingHorizontal: 32,
      paddingVertical: 10,
      marginBottom: 8,
    },
    peekText: {
      color: theme.accent,
      fontWeight: 'bold',
      fontSize: 13,
      letterSpacing: 1,
    },
  });
}
