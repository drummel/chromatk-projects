# Chromatik Projects — Naga LED Dragon

Control system for the Naga dragon sculpture in Golden Gate Park, San Francisco. ~540 LEDs across 28 fixtures, driven by 5 ESP32 controllers and orchestrated by a Raspberry Pi running [Chromatik](https://chromatik.co/) (LX Studio).

## Architecture

```
Raspberry Pi (naga-pi.local)
├── Chromatik (Java, headless)
│   └── Renders patterns at ~30 FPS
│       └── Art-Net UDP → 5x ESP32s → 540 LEDs
├── Sunset scheduler (bash)
│   └── Runs 30 min before sunset → midnight
└── Tailscale VPN (remote access over LTE)
```

## Repository Layout

```
├── Models/                  Chromatik fixture models (.lxm)
├── Fixtures/                Reusable fixture geometry (.lxf)
├── Projects/                Chromatik project files (.lxp)
├── Packages/NagaPackage/    Java pattern package (Maven)
├── Scripts/                 Example JS patterns
├── docs/                    Documentation
│
├── chromatik-scheduled.sh   Sunset-based scheduler
├── chromatik.service        systemd service unit
├── raspberrypi-setup.sh     Initial Pi setup
├── raspberrypi-run.sh       Manual Chromatik launch
├── naga-check.py            Network health checker
└── naga-topology.json       Machine-readable fixture map (planned)
```

## Documentation

- **[Hardware & Topology](docs/naga-hardware.md)** — ESP32 network, fixture map, Art-Net config, physical layout
- **[Pattern Catalog](docs/chromatik-patterns.md)** — All 15 patterns and 2 effects with parameters
- **[Pi Deployment](docs/pi-deployment.md)** — Setup, deploy, systemd, troubleshooting
- **[Scheduling](docs/scheduling.md)** — Sunset scheduler, runtime lifecycle, JVM flags
- **[Interactive Naga Plan](docs/interactive-naga-plan.md)** — Draft architecture for visitor web control

## Quick Reference

### Deploy updates to the Pi

```bash
scp -r Models/ Fixtures/ Projects/ naga@naga-pi.local:/home/naga/Chromatik/
scp Packages/lxpackage-naga-0.0.1.jar naga@naga-pi.local:/home/naga/Chromatik/Packages/
ssh naga@naga-pi.local sudo systemctl restart chromatik.service
```

### Build the pattern package

```bash
cd Packages/NagaPackage
mvn package
```

### Check device health

```bash
python3 naga-check.py
```

### View live logs

```bash
ssh naga@naga-pi.local journalctl -u chromatik.service -f
```
