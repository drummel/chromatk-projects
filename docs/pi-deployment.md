# Raspberry Pi Deployment

How to set up, deploy to, and manage the Naga Raspberry Pi.

## Pi Environment

- **User:** `naga`
- **Working directory:** `/home/naga/Chromatik/`
- **Java:** 17+ via SDKMAN (`/home/naga/.sdkman/candidates/java/current/bin/java`)
- **Chromatik:** 1.0.1-SNAPSHOT (headless, licensed)
- **OS:** Raspberry Pi OS (Bookworm)
- **mDNS hostname:** `naga-pi.local`

## Initial Setup

Run `raspberrypi-setup.sh` on a fresh Pi. It does:

1. Downloads Chromatik 1.0.1 from GitHub releases (linux-aarch64)
2. Unzips into `/home/naga/Chromatik/Chromatik-1.0.1-SNAPSHOT/`
3. Prompts for a Chromatik license code and authorizes
4. Installs Tailscale (if not present) and brings it up
5. Advertises subnet routes for the local networks:
   - `192.168.7.0/24` — Shore WiFi
   - `192.168.8.0/24` — Head WiFi
   - `192.168.5.1/32` — LTE modem

**Prerequisites:** Java 17+ must already be installed (via SDKMAN).

## Directory Layout on the Pi

```
/home/naga/Chromatik/
├── Chromatik-1.0.1-SNAPSHOT/
│   └── chromatik-1.0.1-SNAPSHOT-linux-aarch64.jar
├── Models/
│   └── Naga-Model.lxm                 # Fixture definitions
├── Fixtures/                           # Reusable fixture geometry
├── Projects/
│   └── naga-ggp.lxp                   # Active project (loaded at runtime)
├── Packages/
│   └── lxpackage-naga-0.0.1.jar       # Compiled patterns
├── chromatik-scheduled.sh             # Sunset scheduler
├── raspberrypi-run.sh                 # Manual run script
├── chromatik.service                  # systemd unit
└── raspberrypi-setup-service.sh       # Service installer
```

## Deploying Updates

Copy assets from your development machine to the Pi:

```bash
scp -r Models/ Fixtures/ Projects/ naga@naga-pi.local:/home/naga/Chromatik/
```

Deploy a new pattern JAR after building:

```bash
scp Packages/lxpackage-naga-0.0.1.jar naga@naga-pi.local:/home/naga/Chromatik/Packages/
```

Restart to pick up changes:

```bash
ssh naga@naga-pi.local sudo systemctl restart chromatik.service
```

## systemd Service

The main service unit is `chromatik.service`. Install it with `raspberrypi-setup-service.sh`:

```bash
sudo cp chromatik.service /etc/systemd/system/chromatik.service
sudo systemctl daemon-reload
sudo systemctl enable chromatik.service
sudo systemctl start chromatik.service
```

### Service Configuration

```ini
[Unit]
Description=Chromatik Service
After=network.target
Requires=avahi-daemon.service

[Service]
Type=simple
User=naga
WorkingDirectory=/home/naga/Chromatik
ExecStart=/bin/bash /home/naga/Chromatik/chromatik-scheduled.sh
Restart=always
RestartSec=10
```

The service runs `chromatik-scheduled.sh`, which handles sunset-based scheduling internally (see [docs/scheduling.md](scheduling.md)).

### Useful Commands

```bash
# View live logs
journalctl -u chromatik.service -f

# Check status
sudo systemctl status chromatik.service

# Restart
sudo systemctl restart chromatik.service

# Stop
sudo systemctl stop chromatik.service
```

## Running Manually

For testing without systemd, use `raspberrypi-run.sh`:

```bash
cd /home/naga/Chromatik
./raspberrypi-run.sh
```

This launches Chromatik directly with:
- mDNS DNS provider flags for ESP32 discovery
- `inetaddr.ttl=0` so hostname lookups refresh immediately
- Headless mode (no GUI)
- Loads `Projects/naga-ggp.lxp`

## Health Checks

`naga-check.py` pings all network devices and reports their status:

```bash
python3 naga-check.py
```

Devices checked:
- `naga-head.local` — Head ESP32
- `naga-h1.local` — Body segment 1 ESP32
- `naga-h2.local` — Body segment 2 ESP32
- `naga-h3.local` — Body segment 3 ESP32
- `naga-tail.local` — Tail ESP32
- `naga-pi.local` — The Pi itself

## Network

The Pi connects to the ESP32 controllers via local WiFi and uses mDNS (avahi-daemon) for name resolution. Remote access is through Tailscale VPN over the LTE connection.

### Known Issues

1. **Script name mismatch:** `chromatik.service` currently references `raspberrypi-scheduled.sh` in `ExecStart`, but the actual file is named `chromatik-scheduled.sh`. Whichever name is used on the Pi must match.
2. **First-boot sleep:** The service has a 30-second `ExecStartPre` sleep on first boot (guarded by a temp file) to wait for network. A better approach would be `After=network-online.target`.
3. **PATH includes java binary:** The `Environment=PATH=` line includes the full path to the `java` binary instead of just its parent directory.
