# Scheduling & Runtime Lifecycle

Chromatik runs on a sunset-based schedule: it starts 30 minutes before sunset and shuts down at midnight. This is managed by `chromatik-scheduled.sh`, which runs continuously as a systemd service.

## How It Works

```
Boot
 │
 ▼
chromatik.service starts
 │
 ▼
chromatik-scheduled.sh enters main loop
 │
 ├─ Fetches today's sunset time from API
 ├─ Calculates start time (sunset - 30 min)
 │
 ▼
┌──────────────────────────────────┐
│  Wait loop (checks every 60s)    │◄─── Before active window
│  Is current time >= start time?  │
└──────────┬───────────────────────┘
           │ yes
           ▼
┌──────────────────────────────────┐
│  Launch Chromatik Java process   │
│  Monitor every 30s               │
│                                  │
│  If process crashes:             │
│    Wait 10s, restart             │
│                                  │
│  If midnight (00:00):            │
│    Kill process, exit inner loop │
└──────────┬───────────────────────┘
           │
           ▼
     Back to wait loop
     (next day's sunset fetched)
```

## Sunset API

The scheduler fetches sunset times from `api.sunrise-sunset.org`:

- **Location:** San Francisco (37.7749, -122.4194) — configurable in the script
- **Caching:** Sunset time is cached in `/tmp/chromatik_sunset_cache` and only re-fetched when the date changes
- **Fallback:** If the API is unreachable, defaults to 20:00 (8 PM)

## Active Window

- **Start:** 30 minutes before sunset (dynamically calculated)
- **End:** Midnight (00:00)
- **Example:** If sunset is at 19:45, Chromatik runs from 19:15 to 00:00

The window varies by season. In San Francisco:
- Summer: ~20:00–00:00 (sunset ~20:30)
- Winter: ~16:30–00:00 (sunset ~17:00)

## Crash Recovery

If the Java process exits unexpectedly during the active window, the scheduler waits 10 seconds and restarts it. This continues until midnight. The systemd unit also has `Restart=always` with `RestartSec=10` as a safety net for the scheduler script itself.

## Systemd Integration

The scheduler runs as a long-lived process under systemd:

```
systemd
 └─ chromatik.service
     └─ chromatik-scheduled.sh (bash, runs forever)
         └─ java ... Chromatik (during active hours only)
```

The service starts at boot and the scheduler handles all timing internally.

## Legacy Timer Files (Unused)

The repo contains several timer/service files from an earlier approach that used systemd timers instead of the sunset scheduler. These are **no longer used** and are superseded by `chromatik-scheduled.sh`:

| File | What it did | Why it's obsolete |
|------|------------|-------------------|
| `chromatik.timer` | Start Chromatik at 20:30 daily | Hardcoded time doesn't track sunset |
| `chromatik-start.timer` | Same as above (duplicate) | Same reason |
| `chromatik-start.service` | `systemctl start chromatik.service` | Unnecessary indirection |
| `chromatik-stop.service` | `systemctl stop chromatik.service` | Unnecessary indirection |

These can be safely deleted. If they're enabled on the Pi, disable them:

```bash
sudo systemctl disable --now chromatik.timer chromatik-start.timer
```

## JVM Flags

The scheduler launches Java with:

| Flag | Purpose |
|------|---------|
| `-Dsun.net.inetaddr.ttl=0` | Don't cache DNS lookups (ESP32 IPs may change) |
| `-Dsun.net.dns.spi.nameservice.provider.1=dns,sun` | Use standard DNS first |
| `-Dsun.net.dns.spi.nameservice.provider.2=mdns,sun` | Fall back to mDNS for `.local` hostnames |

The project loaded is `Projects/naga-ggp.lxp` (the current production project).
