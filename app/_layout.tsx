import { useEffect } from 'react';
import { Stack } from 'expo-router';
import { StatusBar } from 'expo-status-bar';
import * as SplashScreen from 'expo-splash-screen';
import {
  useFonts,
  Roboto_400Regular,
  Roboto_500Medium,
  Roboto_700Bold,
} from '@expo-google-fonts/roboto';
import { FusionTcpProvider } from '../services/fusionTcpContext';
import { networkMonitor } from '../services/networkMonitor';
import { notificationService } from '../services/notificationService';
import { ThemeProvider } from '../services/themeContext';

SplashScreen.preventAutoHideAsync();

export default function RootLayout() {
  const [fontsLoaded] = useFonts({
    Roboto_400Regular,
    Roboto_500Medium,
    Roboto_700Bold,
  });

  useEffect(() => {
    if (fontsLoaded) SplashScreen.hideAsync();
  }, [fontsLoaded]);

  // ── App-level services (mirrors NetworkService.onCreate → init()) ────────
  useEffect(() => {
    // Start network monitoring (WiFi/Mobile detection + online/offline events)
    // Mirrors: ConnectivityManager + NetworkBroadcastReceiver registration
    networkMonitor.start();

    // Init push notifications (permission request + channel setup)
    // Mirrors: NotificationHandler + AppEvents.Notification.UPDATE_AVAILABLE listener
    notificationService.init().catch(() => {});

    return () => {
      networkMonitor.stop();
      notificationService.destroy();
    };
  }, []);

  if (!fontsLoaded) return null;

  return (
    <ThemeProvider>
      <FusionTcpProvider>
        <Stack screenOptions={{ headerShown: false }}>
          <Stack.Screen name="index" />
          <Stack.Screen name="register" />
          <Stack.Screen name="(home)" />
        </Stack>
        <StatusBar style="light" />
      </FusionTcpProvider>
    </ThemeProvider>
  );
}
