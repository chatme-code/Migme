import { Platform } from 'react-native';
import { getSession } from './storage';
import { API_BASE } from './auth';

export interface CreditBalance {
  username: string;
  currency: string;
  balance: number;
  fundedBalance: number;
  formatted: string;
  updatedAt: string;
}

export interface CreditTransaction {
  id: string;
  username: string;
  type: number;
  typeName: string;
  reference: string | null;
  description: string | null;
  currency: string;
  amount: number;
  fundedAmount: number;
  tax: number;
  runningBalance: number;
  createdAt: string;
}

export interface TransferResult {
  success: boolean;
  fromUsername: string;
  toUsername: string;
  transferAmount: number;
  fee: number;
  netReceived: number;
  fromBalance: number;
  toBalance: number;
  currency: string;
}

async function buildHeaders(): Promise<HeadersInit> {
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
  };
  if (Platform.OS !== 'web') {
    const cookie = await getSession();
    if (cookie) headers['Cookie'] = cookie;
  }
  return headers;
}

function fetchOptions(): RequestInit {
  return Platform.OS === 'web' ? { credentials: 'include' } : {};
}

export async function getCreditBalance(username: string): Promise<CreditBalance | null> {
  try {
    const headers = await buildHeaders();
    const res = await fetch(
      `${API_BASE}/api/credit/balance/${encodeURIComponent(username)}`,
      { headers, ...fetchOptions() },
    );
    if (!res.ok) return null;
    return await res.json();
  } catch {
    return null;
  }
}

export async function getCreditTransactions(
  username: string,
  limit = 50,
): Promise<CreditTransaction[]> {
  try {
    const headers = await buildHeaders();
    const res = await fetch(
      `${API_BASE}/api/credit/transactions?username=${encodeURIComponent(username)}&limit=${limit}`,
      { headers, ...fetchOptions() },
    );
    if (!res.ok) return [];
    const data = await res.json();
    return data.transactions ?? [];
  } catch {
    return [];
  }
}

export async function transferCredit(
  fromUsername: string,
  toUsername: string,
  amount: number,
  pin: string,
): Promise<TransferResult> {
  const headers = await buildHeaders();
  const res = await fetch(`${API_BASE}/api/credit/transfer`, {
    method: 'POST',
    headers,
    ...fetchOptions(),
    body: JSON.stringify({ fromUsername, toUsername, amount, pin, feeType: 1 }),
  });
  const data = await res.json();
  if (!res.ok) throw new Error(data.error ?? 'Transfer failed');
  return data;
}

// USD to IDR flat conversion rate
export const USD_TO_IDR = 10_000;

// Format a credit amount based on currency code.
// IDR → Indonesian Rupiah format (IDR 5.000)
// USD → US Dollar (USD 5.00), also shows IDR equivalent
export function formatCredit(amount: number, currency: string): string {
  if (currency === 'USD') return `USD ${amount.toFixed(2)}`;
  // Default: IDR (and anything else falls back to IDR)
  return `IDR ${Math.round(amount).toLocaleString('id-ID')}`;
}

// Convert USD to IDR at flat rate 1 USD = 10,000 IDR
export function usdToIdr(usd: number): number {
  return usd * USD_TO_IDR;
}

export function formatUSD(amount: number): string {
  return `USD ${amount.toFixed(2)}`;
}

export function formatIDR(amount: number): string {
  return `IDR ${Math.round(amount).toLocaleString('id-ID')}`;
}
