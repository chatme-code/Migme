# Fusion Modernisation Dashboard

## Project Overview

A web dashboard that visualises the full dependency modernisation analysis for the **Fusion** enterprise Java project (`com.projectgoth/Fusion v8.75.132`), migrating it from Java 1.5/1.6 to Java 21 with Spring Boot 3.

## Architecture

- **Backend**: Node.js / Express (TypeScript) — serves the modernisation data via REST API
- **Frontend**: React 18 + Vite + Tailwind CSS + Shadcn UI + Recharts
- **Java project**: `pom.xml` at the root — fully rewritten from legacy Maven to modern Spring Boot 3.3.1

## Key Files

| File | Purpose |
|------|---------|
| `pom.xml` | Modernised Maven build — Java 21, Spring Boot 3.3.1, all deps updated |
| `server/routes.ts` | API endpoints serving the dependency analysis data |
| `client/src/pages/Dashboard.tsx` | Main dashboard UI with charts, filters, tabs |
| `client/src/index.css` | Design tokens — dark navy / blue professional theme |

## API Endpoints

| Endpoint | Description |
|----------|-------------|
| `GET /api/analysis` | Full analysis data |
| `GET /api/analysis/summary` | Project info + summary counts |
| `GET /api/analysis/dependencies?category=X&status=Y` | Filtered dependencies |
| `GET /api/analysis/code-changes` | Per-file code migration steps |
| `GET /api/analysis/plugins` | Removed + added Maven plugins |

## Java Modernisation Summary

### Java Version
- **Old**: Java 1.5 / 1.6 (Build JDK 1.6.0_24)
- **New**: Java 21 LTS (`<release>21</release>` with `--enable-preview`)

### Framework
- **Old**: Spring 2.5.5 (2007 release, no security patches)
- **New**: Spring Boot 3.3.1 (Jakarta EE 10, Spring 6.1.x)

### Critical Security Fixes
1. `log4j:log4j:1.2.9` → SLF4J + Logback (fixes CVE-2019-17571)
2. `redis.clients:jedis:2.5.2` → Jedis 5.1.3 (fixes CVE-2016-5725 via JSch)
3. `org.codehaus.jackson:jackson-mapper-asl:1.8.9` → removed (fixes CVE-2019-10172)
4. `commons-collections:3.2` → commons-collections4:4.4 (fixes CVE-2015-6420 RCE)

### Removed Obsolete Dependencies
- SWT (Eclipse widget toolkit — not needed server-side)
- jpcap (native packet capture — not on Maven Central)
- ICE middleware (replaced by Spring WebFlux / gRPC)
- Keyczar (unmaintained since 2016 — use Spring Security Crypto)
- XDoclet Maven plugin (replaced by standard annotations)
- JBoss EJB 2.0 (replaced by Spring @Service components)
- Codehaus Jackson 1.8.9 (unmaintained since 2013)
- Xalan XSLT (bundled in JDK 11+)

### Namespace Migration Required
All source files importing `javax.*` must be updated to `jakarta.*`:
```
javax.servlet → jakarta.servlet
javax.mail    → jakarta.mail
javax.xml.bind → jakarta.xml.bind
javax.ws.rs   → jakarta.ws.rs
```

## Running the App

```bash
npm run dev   # starts on port 5000
```
