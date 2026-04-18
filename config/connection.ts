/**
 * ConnectionConfig
 *
 * Centralized connection configuration for the migchat Expo app.
 * Mirrors ConnectionDetail.java from the Android client.
 *
 * Three environments:
 *   DEV     — Replit dev server (debug builds)
 *   STAGING — AWS EC2 via public IP langsung (Docker, tanpa Nginx)
 *   PROD    — Production via Nginx + SSL (migxchat.net)
 *
 * CHANGE FORCE_ENV untuk switch environment:
 *   null      = auto ('dev' di debug build, 'prod' di release build)
 *   'dev'     = paksa Replit dev
 *   'staging' = paksa AWS EC2 IP langsung (HTTP/WS, tanpa SSL)
 *   'prod'    = paksa production domain dengan Nginx + SSL
 */
export type EnvType = 'dev' | 'staging' | 'prod';

const FORCE_ENV: EnvType | null = 'prod'; // <── UBAH INI sesuai kebutuhan

// ─── Replit dev domain ───────────────────────────────────────────────────────
const REPLIT_DEV_DOMAIN =
  '06c86222-911a-4a09-980f-b16d51a4969e-00-1420szihiipoo.sisko.replit.dev';

// ─── AWS EC2 ─────────────────────────────────────────────────────────────────
const EC2_IP   = '13.212.78.52';
const EC2_PORT = 5000;

// ─── Production domain (Nginx + SSL) ────────────────────────────────────────
// Semua traffic API dan WebSocket melalui Nginx di migxchat.net
// TCP gateway (port 9119) langsung ke EC2, tidak lewat Nginx
const PROD_DOMAIN = 'migxchat.net';

// ─── Config shape ────────────────────────────────────────────────────────────
export interface ConnectionConfig {
  env: EnvType;
  apiBase: string;
  wsUrl: string;
  gatewayHost: string;
  gatewayPort: number;
  webServer: string;
  discoverUrl: string;
  imageUrl: string;
  imagesUrl: string;
  ssoUrl: string;
  dataServiceUrl: string;
  multiPartUrl: string;
  signupUrl: string;
  facebookAppId: string;
}

// ─── DEV config (Replit) ─────────────────────────────────────────────────────
const DEV_CONFIG: ConnectionConfig = {
  env:            'dev',
  apiBase:        `https://${REPLIT_DEV_DOMAIN}`,
  wsUrl:          `wss://${REPLIT_DEV_DOMAIN}/gateway`,
  gatewayHost:    REPLIT_DEV_DOMAIN,
  gatewayPort:    9119,
  webServer:      REPLIT_DEV_DOMAIN,
  discoverUrl:    `https://${REPLIT_DEV_DOMAIN}`,
  imageUrl:       `https://${REPLIT_DEV_DOMAIN}/img/`,
  imagesUrl:      `https://${REPLIT_DEV_DOMAIN}/resources/img`,
  ssoUrl:         `https://${REPLIT_DEV_DOMAIN}/touch/datasvc`,
  dataServiceUrl: `https://${REPLIT_DEV_DOMAIN}/touch/datasvc`,
  multiPartUrl:   `https://${REPLIT_DEV_DOMAIN}/touch/post/hidden_post`,
  signupUrl:      `https://${REPLIT_DEV_DOMAIN}`,
  facebookAppId:  '161865877194414',
};

// ─── STAGING config (EC2 IP langsung, tanpa Nginx/SSL) ───────────────────────
// Untuk testing langsung tanpa domain. Pakai HTTP dan WS (bukan HTTPS/WSS).
// Pastikan Security Group EC2 buka port 5000 dan 9119.
const STAGING_CONFIG: ConnectionConfig = {
  env:            'staging',
  apiBase:        `http://${EC2_IP}:${EC2_PORT}`,
  wsUrl:          `ws://${EC2_IP}:${EC2_PORT}/gateway`,
  gatewayHost:    EC2_IP,
  gatewayPort:    9119,
  webServer:      `${EC2_IP}:${EC2_PORT}`,
  discoverUrl:    `http://${EC2_IP}:${EC2_PORT}`,
  imageUrl:       `http://${EC2_IP}:${EC2_PORT}/img/`,
  imagesUrl:      `http://${EC2_IP}:${EC2_PORT}/resources/img`,
  ssoUrl:         `http://${EC2_IP}:${EC2_PORT}/touch/datasvc`,
  dataServiceUrl: `http://${EC2_IP}:${EC2_PORT}/touch/datasvc`,
  multiPartUrl:   `http://${EC2_IP}:${EC2_PORT}/touch/post/hidden_post`,
  signupUrl:      `http://${EC2_IP}:${EC2_PORT}`,
  facebookAppId:  '161865877194414',
};

// ─── PROD config (migxchat.net via Nginx + Let's Encrypt SSL) ────────────────
// API dan WebSocket lewat Nginx (port 443, HTTPS/WSS).
// TCP gateway langsung ke EC2 port 9119 (tidak lewat Nginx).
const PROD_CONFIG: ConnectionConfig = {
  env:            'prod',
  apiBase:        `https://${PROD_DOMAIN}`,
  wsUrl:          `wss://${PROD_DOMAIN}/gateway`,
  gatewayHost:    EC2_IP,        // TCP langsung ke EC2, bypass Nginx
  gatewayPort:    9119,
  webServer:      PROD_DOMAIN,
  discoverUrl:    `https://${PROD_DOMAIN}`,
  imageUrl:       `https://${PROD_DOMAIN}/img/`,
  imagesUrl:      `https://${PROD_DOMAIN}/resources/img`,
  ssoUrl:         `https://${PROD_DOMAIN}/touch/datasvc`,
  dataServiceUrl: `https://${PROD_DOMAIN}/touch/datasvc`,
  multiPartUrl:   `https://${PROD_DOMAIN}/touch/post/hidden_post`,
  signupUrl:      `https://${PROD_DOMAIN}`,
  facebookAppId:  '161865877194414',
};

// ─── Active config ───────────────────────────────────────────────────────────
function resolveConfig(): ConnectionConfig {
  if (FORCE_ENV === 'staging') return STAGING_CONFIG;
  if (FORCE_ENV === 'dev')     return DEV_CONFIG;
  if (FORCE_ENV === 'prod')    return PROD_CONFIG;
  return __DEV__ ? DEV_CONFIG : PROD_CONFIG;
}

export const Connection: ConnectionConfig = resolveConfig();

export const API_BASE     = Connection.apiBase;
export const WS_URL       = Connection.wsUrl;
export const GATEWAY_HOST = Connection.gatewayHost;
export const GATEWAY_PORT = Connection.gatewayPort;
export const IMAGE_URL    = Connection.imageUrl;
export const DISCOVER_URL = Connection.discoverUrl;
