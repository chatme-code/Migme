import { useRouter } from 'expo-router';
import { useState } from 'react';
import {
  ActivityIndicator,
  Alert,
  KeyboardAvoidingView,
  Platform,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  TouchableOpacity,
  View,
} from 'react-native';
import { register } from '../services/auth';
import { useAppTheme } from '../services/themeContext';

type Step = 'email' | 'name' | 'password';

const STEP_CONFIG: Record<Step, { title: string; botText: string }> = {
  email: {
    title: 'Email Kamu',
    botText: 'Masukkan alamat email untuk memulai. Kami akan menjaganya tetap aman.',
  },
  name: {
    title: 'Username',
    botText: 'Pilih username unik yang akan dilihat orang lain di migchat. Minimal 6, maksimal 18 karakter.',
  },
  password: {
    title: 'Password',
    botText: 'Buat password yang kuat untuk melindungi akun kamu.',
  },
};

const STEP_ORDER: Step[] = ['email', 'name', 'password'];

export default function RegisterScreen() {
  const router = useRouter();
  const theme = useAppTheme();
  const [step, setStep] = useState<Step>('email');
  const [email, setEmail] = useState('');
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const stepIndex = STEP_ORDER.indexOf(step);
  const config = STEP_CONFIG[step];

  const handleBack = () => {
    setError('');
    if (stepIndex === 0) {
      router.back();
    } else {
      setStep(STEP_ORDER[stepIndex - 1]);
    }
  };

  const handleNext = async () => {
    setError('');
    if (step === 'email') {
      if (!email.trim() || !email.includes('@')) {
        setError('Masukkan alamat email yang valid.');
        return;
      }
      setStep('name');
    } else if (step === 'name') {
      const trimmed = username.trim();
      if (!trimmed || trimmed.length < 6) {
        setError('Username minimal 6 karakter.');
        return;
      }
      if (trimmed.length > 18) {
        setError('Username maksimal 18 karakter.');
        return;
      }
      if (!/^[a-zA-Z0-9_]+$/.test(trimmed)) {
        setError('Username hanya boleh huruf, angka, dan underscore.');
        return;
      }
      setStep('password');
    } else if (step === 'password') {
      if (!password.trim() || password.length < 6) {
        setError('Password minimal 6 karakter.');
        return;
      }
      setLoading(true);
      try {
        await register(username.trim(), email.trim(), password, username.trim());
        Alert.alert(
          'Registrasi Berhasil!',
          'Akun berhasil dibuat. Cek email kamu untuk verifikasi, lalu login.',
          [{ text: 'Login Sekarang', onPress: () => router.replace('/') }],
        );
      } catch (e: unknown) {
        const msg = e instanceof Error ? e.message : 'Registrasi gagal. Coba lagi.';
        setError(msg);
      } finally {
        setLoading(false);
      }
    }
  };

  const getHintText = () => {
    if (step === 'email') return 'Kami akan kirim informasi login ke alamat ini.';
    if (step === 'name') return 'Huruf, angka, dan underscore saja. 6–18 karakter.';
    return 'Minimal 6 karakter dengan kombinasi huruf dan angka.';
  };

  const styles = makeStyles(theme);

  const renderInput = () => {
    if (step === 'email') {
      return (
        <TextInput
          style={styles.input}
          placeholder="Alamat email"
          placeholderTextColor={theme.textSecondary}
          value={email}
          onChangeText={(v) => { setEmail(v); setError(''); }}
          keyboardType="email-address"
          autoCapitalize="none"
          returnKeyType="next"
          onSubmitEditing={handleNext}
          editable={!loading}
          testID="input-email"
        />
      );
    }
    if (step === 'name') {
      return (
        <TextInput
          style={styles.input}
          placeholder="Username (6–18 karakter)"
          placeholderTextColor={theme.textSecondary}
          value={username}
          onChangeText={(v) => { setUsername(v); setError(''); }}
          autoCapitalize="none"
          autoCorrect={false}
          maxLength={18}
          returnKeyType="next"
          onSubmitEditing={handleNext}
          editable={!loading}
          testID="input-username"
        />
      );
    }
    return (
      <TextInput
        style={styles.input}
        placeholder="Password"
        placeholderTextColor={theme.textSecondary}
        value={password}
        onChangeText={(v) => { setPassword(v); setError(''); }}
        secureTextEntry={!showPassword}
        returnKeyType="done"
        onSubmitEditing={handleNext}
        editable={!loading}
        testID="input-password"
      />
    );
  };

  return (
    <KeyboardAvoidingView
      style={styles.container}
      behavior={Platform.OS === 'ios' ? 'padding' : undefined}
    >
      <ScrollView style={styles.scroll} keyboardShouldPersistTaps="handled">
        <View style={styles.header}>
          <TouchableOpacity
            style={styles.backBtn}
            onPress={handleBack}
            testID="button-back"
            disabled={loading}
          >
            <Text style={styles.backArrow}>◀</Text>
          </TouchableOpacity>
          <Text style={styles.headerTitle}>{config.title}</Text>

          <View style={styles.tabBar}>
            <TouchableOpacity
              style={styles.tabInactive}
              testID="tab-login"
              onPress={() => router.replace('/')}
              disabled={loading}
            >
              <Text style={styles.tabTextInactive}>LOGIN</Text>
            </TouchableOpacity>

            <TouchableOpacity style={styles.tabActive} testID="tab-register">
              <Text style={styles.tabTextActive}>REGISTER</Text>
              <View style={styles.tabIndicator} />
            </TouchableOpacity>
          </View>
        </View>

        <View style={styles.botSection}>
          <View style={styles.botAvatar}>
            <Text style={styles.botEmoji}>🤖</Text>
          </View>
          <Text style={styles.botText}>{config.botText}</Text>
        </View>

        <View style={styles.progressBar}>
          {STEP_ORDER.map((s, i) => (
            <View
              key={s}
              style={[
                styles.progressDot,
                i <= stepIndex ? styles.progressDotActive : styles.progressDotInactive,
              ]}
            />
          ))}
        </View>

        {error ? (
          <View style={styles.errorBanner}>
            <Text style={styles.errorText}>{error}</Text>
          </View>
        ) : null}

        <View style={styles.formSection}>
          <View style={styles.inputRow}>
            {renderInput()}
            {loading ? (
              <ActivityIndicator color={theme.accent} style={styles.arrowBtn} />
            ) : (
              <TouchableOpacity
                style={styles.arrowBtn}
                onPress={handleNext}
                testID="button-next"
              >
                <Text style={styles.arrowText}>▶</Text>
              </TouchableOpacity>
            )}
          </View>

          <View style={styles.separator} />

          {step === 'password' && (
            <View style={styles.optionsRow}>
              <TouchableOpacity
                onPress={() => setShowPassword(!showPassword)}
                testID="button-showhide"
              >
                <Text style={styles.accentLink}>
                  {showPassword ? 'Sembunyikan password' : 'Tampilkan password'}
                </Text>
              </TouchableOpacity>
            </View>
          )}

          <Text style={styles.hintText}>{getHintText()}</Text>

          <View style={styles.spacer} />

          <View style={styles.legalRow}>
            <Text style={styles.legalText}>Dengan mendaftar kamu menyetujui </Text>
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
      position: 'relative',
    },
    backBtn: {
      position: 'absolute',
      top: 52,
      left: 16,
      zIndex: 10,
      padding: 8,
    },
    backArrow: {
      color: theme.accent,
      fontSize: 18,
    },
    headerTitle: {
      color: '#FFFFFF',
      fontSize: 22,
      fontWeight: 'bold',
      textAlign: 'center',
      paddingVertical: 20,
    },
    tabBar: {
      flexDirection: 'row',
      marginTop: 4,
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
    progressBar: {
      flexDirection: 'row',
      justifyContent: 'center',
      paddingVertical: 12,
      backgroundColor: theme.cardBg,
      gap: 8,
    },
    progressDot: {
      width: 8,
      height: 8,
      borderRadius: 4,
    },
    progressDotActive: {
      backgroundColor: theme.accent,
    },
    progressDotInactive: {
      backgroundColor: theme.divider,
    },
    errorBanner: {
      backgroundColor: '#FFEDED',
      borderLeftWidth: 3,
      borderLeftColor: '#C64F44',
      paddingHorizontal: 16,
      paddingVertical: 10,
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
      paddingLeft: 20,
      paddingRight: 8,
      paddingVertical: 4,
    },
    input: {
      flex: 1,
      paddingVertical: 14,
      fontSize: 15,
      color: theme.textPrimary,
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
      paddingHorizontal: 20,
      paddingVertical: 10,
    },
    accentLink: {
      color: theme.accent,
      fontSize: 13,
      fontWeight: '500',
    },
    hintText: {
      color: theme.textSecondary,
      fontSize: 12,
      paddingHorizontal: 20,
      paddingTop: 10,
      lineHeight: 18,
    },
    spacer: {
      height: 16,
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
  });
}
