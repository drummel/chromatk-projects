# Naga Interactive — Architecture Plan (DRAFT)

> Status: Draft — March 2026

## Goal

Let park visitors control the Naga LED dragon sculpture from their phones. A visitor scans a QR code, picks a color or pattern, and the dragon responds in real time — all over the existing LTE + Tailscale connection.

## System Overview

```
Visitor phone
    ↓ HTTPS
Nuxt web app (static, Render.com)
    ↓ HTTP POST
Cloud API (FastAPI, Render.com)
    ↓ HTTP over Tailscale
Pi Controller (FastAPI, port 8080)
    ↓ Art-Net UDP
5x ESP32 controllers → 540 LEDs
```

Commands, not pixels. The Pi renders locally. ~200 bytes per interaction. Well within the 5 MB/month LTE budget.

## New Components

### 1. `controller/` — Pi-side Python pattern engine + API

Runs on the Raspberry Pi alongside Chromatik. In Phase 1, it only listens and logs — no Art-Net output. Later phases take over LED output for interactive sessions.

```
controller/
├── pyproject.toml                  # fastapi, uvicorn, pydantic
├── naga_controller/
│   ├── main.py                     # Starts engine + API server
│   ├── config.py                   # Loads naga-topology.json, settings
│   ├── artnet.py                   # Art-Net DMX packet construction + UDP send
│   ├── engine.py                   # 30fps render loop, frame timing, crossfades
│   ├── api.py                      # /health, POST /command
│   ├── session.py                  # Session tracking, idle timeout
│   └── patterns/
│       ├── base.py                 # Abstract pattern (mirrors LXPattern)
│       ├── ember_glow.py           # Port of EmberGlow.java
│       ├── breathing.py            # Port of BreathingLight.java
│       ├── snake_crawl.py          # Port of SnakeCrawl.java
│       └── solid_color.py          # Visitor picks a color
└── tests/
```

**Key design decisions:**
- Pattern base class mirrors LXPattern's `run(deltaMs, colors)` interface
- Art-Net module constructs proper DMX packets (14-byte header + channel data) per Art-Net spec
- mDNS hostname resolution via avahi (already running on the Pi)
- CI/CD lives inside this directory (e.g., GitHub Actions workflow for SCP deploy to Pi)

### 2. `cloud-api/` — Cloud backend

Sits between the web app and the Pi. Handles session management, rate limiting, and command forwarding.

```
cloud-api/
├── pyproject.toml                  # fastapi, uvicorn, httpx, pydantic
├── Dockerfile
├── render.yaml                     # Render.com service definition
├── naga_api/
│   ├── main.py                     # FastAPI app with CORS
│   ├── config.py                   # Pi Tailscale IP, timeouts
│   ├── routers/
│   │   ├── sessions.py             # POST /sessions
│   │   ├── commands.py             # POST /commands
│   │   └── status.py               # GET /status
│   └── services/
│       ├── pi_client.py            # httpx client to Pi over Tailscale
│       └── rate_limiter.py         # 1 active session, 5-min max, queue
└── tests/
```

**Render.com deployment:** Docker web service. Needs Tailscale installed in the container (or host-level) to reach the Pi's Tailscale IP. CI/CD config lives in this directory.

### 3. `web/` — Visitor-facing web app

Static Nuxt 3 site. Visitor scans QR, lands on the page, picks a color or pattern.

```
web/
├── nuxt.config.ts
├── package.json
├── Dockerfile                      # Static build for Render.com
├── pages/
│   ├── index.vue                   # Landing: "Touch the Dragon"
│   └── interact.vue                # Color picker, pattern selector
├── components/
│   ├── ColorPicker.vue             # Touch-friendly color wheel
│   ├── PatternSelector.vue         # Pattern thumbnail grid
│   └── NagaPreview.vue             # 2D dragon outline showing live color
└── composables/
    ├── useSession.ts               # Session lifecycle
    └── useNagaApi.ts               # Typed API client
```

**Render.com deployment:** Static site. Nuxt generates to `.output/public/`. CI/CD config lives in this directory.

### 4. `naga-topology.json` — Machine-readable fixture map

Extracted once from `Models/Naga-Model.lxm`. Maps every fixture to its ESP32 host, Art-Net universe, and pixel count. The Python controller loads this at startup instead of parsing the LXM file.

```json
{
  "fixtures": [
    {
      "label": "Body head",
      "host": "naga-head.local",
      "universe": 10,
      "numPoints": 38,
      "brightness": 0.85,
      "reverse": true,
      "tags": ["body", "head"]
    }
  ],
  "totalPixels": 540
}
```

## What Stays the Same

All existing files remain in place:
- `Models/`, `Fixtures/`, `Projects/`, `Scripts/` — Chromatik assets
- `Packages/NagaPackage/` — Java LX package
- `chromatik.service`, `chromatik-scheduled.sh`, `raspberrypi-*.sh` — Pi deployment
- `naga-check.py` — health monitoring

No files are moved or deleted. The new components are purely additive.

## Phased Pi Transition

### Phase 1 — Controller as observer (start here)

Chromatik continues running patterns and sending Art-Net as it does today. The new Python controller runs alongside it in a separate systemd unit, listening for cloud commands and logging what it *would* do. This validates the full cloud → Pi communication path without touching the lights.

```ini
# /etc/systemd/system/naga-controller.service
[Unit]
Description=Naga Interactive Controller
After=network-online.target

[Service]
Type=simple
User=naga
WorkingDirectory=/home/naga/naga-controller
ExecStart=/home/naga/naga-controller/.venv/bin/python -m naga_controller.main
Restart=always
RestartSec=5

[Install]
WantedBy=multi-user.target
```

### Phase 2 — Controller handles interactive sessions

When a visitor starts a session, the controller begins sending Art-Net at a slightly higher frame rate than Chromatik. Since Art-Net is stateless UDP, the last sender wins at the ESP32. When the session ends, the controller stops sending, and Chromatik's ambient patterns immediately take over.

### Phase 3 — Controller primary

Chromatik retired from the Pi runtime. Controller handles both ambient and interactive patterns. Chromatik remains in the repo for pattern development on laptops with its GUI.

## Technology Stack

| Component | Stack |
|-----------|-------|
| Pi Controller | Python 3.11+, FastAPI, Uvicorn, stdlib UDP sockets |
| Cloud API | Python 3.12+, FastAPI, httpx, Pydantic v2 |
| Web App | Nuxt 3, Vue 3, TypeScript, Tailwind CSS 4 |
| Cloud Hosting | Render.com (Docker for API, static site for web) |
| Pi Deploy | SCP (same as today, scriptable) |
| CI/CD | GitHub Actions, per-component (each app owns its workflow) |

## Security

The cloud API is publicly accessible. Minimum protections:
- Rate limiting: 1 active session at a time, 5-minute max duration, queue for waiting visitors
- Optional: QR code contains a daily-rotating token, or a passphrase displayed on a sign near the dragon
- Cloud API debounces rapid interactions (e.g., color picker drag) to 2-4 req/sec

## LTE Bandwidth Budget

- Each command is ~200 bytes JSON
- 1 req/sec for 1 hour = ~720 KB
- Use on-demand HTTP (not WebSockets) to avoid keepalive overhead
- Projected: well under 5 MB/month even with daily sessions

## Known Issues to Address Later

| Issue | Notes |
|-------|-------|
| `chromatik.service` references `raspberrypi-scheduled.sh` but file is `chromatik-scheduled.sh` | Name mismatch — reconcile when touching service file |
| `chromatik.service` has 30-second sleep hack in `ExecStartPre` | Replace with `After=network-online.target` |
| 4 redundant timer/service files (`chromatik.timer`, `chromatik-start.*`, `chromatik-stop.*`) | Superseded by sunset scheduler — delete when cleaning up |
| Duplicate files in `Packages/NagaPackage/` (model, project, SSH artifact) | Delete when restructuring |
| Empty `.lxf` placeholder files in `Fixtures/` | Delete when restructuring |

## Future: Full Repo Restructure

When ready, migrate existing files into organized directories:
1. `Models/` → `shared/models/`, `Fixtures/` → `shared/fixtures/`, etc.
2. `Packages/NagaPackage/` → `chromatik/packages/NagaPackage/`
3. Root scripts/services → `pi/scripts/`, `pi/systemd/`
4. Create `pi/deploy.sh` to map new paths to Pi's expected layout
5. Delete dead files listed above

## Implementation Order

1. **Generate `naga-topology.json`** — parse `Models/Naga-Model.lxm`, extract fixture map
2. **Scaffold `controller/`** — Art-Net, engine, patterns (port from Java), API, tests
3. **Scaffold `cloud-api/`** — session/command/status routers, Pi client, rate limiter, Dockerfile
4. **Scaffold `web/`** — Nuxt 3 + Tailwind, color picker, pattern selector, API composable
5. **Deploy + integrate** — controller to Pi (Phase 1), cloud components to Render.com, end-to-end test
