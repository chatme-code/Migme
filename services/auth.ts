import { Platform } from 'react-native';
import { saveSession, getSession, clearSession, saveUser, saveAuthToken, getAuthToken, type StoredUser } from './storage';
import { API_BASE as _API_BASE, Connection, GATEWAY_HOST, GATEWAY_PORT } from '../config/connection';
import { sendLogoutSignal } from './wsManager';
import { tcpGateway } from './tcpGatewayClient';

// Re-export so all existing imports of API_BASE from this file keep working
export const API_BASE = _API_BASE;
export { Connection };

export async function buildHeaders(extra?: Record<string, string>): Promise<HeadersInit> {
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...extra,
  };
  if (Platform.OS !== 'web') {
    // Prefer JWT Bearer token (works reliably on native without cookie jar)
    const authToken = await getAuthToken();
    if (authToken) {
      headers['Authorization'] = `Bearer ${authToken}`;
    } else {
      // Fallback: try session cookie (may not work on all React Native versions)
      const cookie = await getSession();
      if (cookie) headers['Cookie'] = cookie;
    }
  }
  return headers;
}

function getFetchOptions(method: string): RequestInit {
  return Platform.OS === 'web'
    ? { credentials: 'include' as RequestCredentials }
    : {};
}

export interface AuthUser {
  id: string;
  username: string;
  displayName: string | null;
  email: string;
}

export interface AuthResponse {
  user?: AuthUser;
  message?: string;
  errors?: unknown;
  tcpToken?: string;
  authToken?: string;
}

export async function login(username: string, password: string): Promise<AuthResponse> {
  const headers = await buildHeaders();
  const res = await fetch(`${API_BASE}/api/auth/login`, {
    method: 'POST',
    headers,
    body: JSON.stringify({ username, password }),
    ...getFetchOptions('POST'),
  });

  if (Platform.OS !== 'web') {
    const rawCookie = res.headers.get('set-cookie');
    if (rawCookie) {
      const sid = rawCookie.split(';')[0];
      await saveSession(sid);
    }
  }

  let data: AuthResponse;
  try {
    data = await res.json();
  } catch {
    throw new Error('Server tidak merespons dengan benar. Periksa koneksi dan coba lagi.');
  }

  if (res.ok && data.user) {
    await saveUser(data.user as StoredUser);

    // Save JWT authToken for all subsequent HTTP API requests (replaces cookie approach).
    // authToken is a 30-day JWT that works reliably on React Native without a cookie jar.
    if (Platform.OS !== 'web' && data.authToken) {
      await saveAuthToken(data.authToken);
    }

    // Start TCP gateway connection after HTTP login succeeds (native only).
    // Pass tcpToken as the TCP "password" — the server accepts it in the JSON LOGIN
    // without consuming it, so automatic reconnects keep working for 7 days.
    if (Platform.OS !== 'web') {
      const tcpPassword = data.tcpToken ?? password;
      tcpGateway.connect({ host: GATEWAY_HOST, port: GATEWAY_PORT, username, password: tcpPassword });
    }
  }

  if (!res.ok) {
    throw new Error(data.message || 'Login gagal');
  }

  return data;
}

export async function register(
  username: string,
  email: string,
  password: string,
  displayName?: string,
): Promise<AuthResponse> {
  const headers = await buildHeaders();
  const res = await fetch(`${API_BASE}/api/auth/register`, {
    method: 'POST',
    headers,
    body: JSON.stringify({ username, email, password, displayName: displayName || username }),
    ...getFetchOptions('POST'),
  });

  const data: AuthResponse = await res.json();

  if (!res.ok) {
    throw new Error(data.message || 'Registrasi gagal');
  }

  return data;
}

export async function logout(): Promise<void> {
  // Send LOGOUT over TCP Fusion (native) or WebSocket (web) so the gateway
  // immediately broadcasts "[username] has left" without waiting for the
  // 15-second grace period. Mirrors fusion SSOResource.logout() flow.
  if (Platform.OS !== 'web') {
    await tcpGateway.logout();
  } else {
    await sendLogoutSignal();
  }

  const headers = await buildHeaders();
  await fetch(`${API_BASE}/api/auth/logout`, {
    method: 'POST',
    headers,
    ...getFetchOptions('POST'),
  });
  await clearSession();
}

export async function forgotPassword(emailOrUsername: string): Promise<{ message: string }> {
  const res = await fetch(`${API_BASE}/api/auth/forgot-password`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ emailOrUsername }),
    ...getFetchOptions('POST'),
  });
  const data = await res.json();
  if (!res.ok) throw new Error(data.message || 'Terjadi kesalahan. Coba lagi.');
  return data;
}

export async function getMe(): Promise<AuthUser | null> {
  try {
    const headers = await buildHeaders();
    const res = await fetch(`${API_BASE}/api/auth/me`, {
      headers,
      ...getFetchOptions('GET'),
    });
    if (!res.ok) return null;
    const data: AuthResponse = await res.json();
    return data.user ?? null;
  } catch {
    return null;
  }
}
