# Naga Hardware & Network Topology

The Naga dragon sculpture has ~540 individually addressable LEDs controlled by 5 ESP32 microcontrollers, orchestrated by a Raspberry Pi running Chromatik.

## Physical Layout

The dragon is divided into 5 sections, each with a dedicated ESP32 controller:

```
 ┌─────────┐   ┌─────────┐   ┌─────────┐   ┌─────────┐   ┌─────────┐
 │  HEAD   │───│   H1    │───│   H2    │───│   H3    │───│  TAIL   │
 │ naga-   │   │ naga-   │   │ naga-   │   │ naga-   │   │ naga-   │
 │ head    │   │ h1      │   │ h2      │   │ h3      │   │ tail    │
 └─────────┘   └─────────┘   └─────────┘   └─────────┘   └─────────┘
```

Each section has three types of fixtures:
- **Body** — main spiral wrapping around the body segment
- **Fins** — flat structures extending from the sides
- **Spikes** — small protruding elements

## ESP32 Controllers

| Controller | Hostname | Fixtures | Universes | Total Pixels |
|-----------|----------|----------|-----------|-------------|
| Head | `naga-head.local` | Body head, Fin Head M/mR/mL/R/L, Spike head M/R/L/mR/mL | 10, 20–24, 30–34 | 155 |
| H1 | `naga-h1.local` | Body h1, Fin h1, Spike h1 | 15, 25, 35 | 106 |
| H2 | `naga-h2.local` | Body h2, Fin h2, Spike h2 | 16, 26, 36 | 89 |
| H3 | `naga-h3.local` | Body h3, Fin h3, Spike h3 | 17, 27, 37 | 61 |
| Tail | `naga-tail.local` | Body tail, Fin tail top/bottom, Spike tail top/bottom | 18, 28–29, 38–39 | 129 |

**Total: ~540 LEDs across 28 fixtures**

## Complete Fixture Map

### Body Fixtures

| Fixture | Host | Universe | Pixels | Brightness | Reverse | Tags |
|---------|------|----------|--------|-----------|---------|------|
| Body head | naga-head.local | 10 | 38 | 0.85 | yes | body head |
| Body h1 | naga-h1.local | 15 | 38 | 1.0 | no | body h1 |
| Body h2 | naga-h2.local | 16 | 30 | 1.0 | no | body h2 |
| Body h3 | naga-h3.local | 17 | 20 | 1.0 | no | body h3 |
| Body tail | naga-tail.local | 18 | 22 | 1.0 | no | body tail |

### Fin Fixtures

| Fixture | Host | Universe | Pixels | Reverse | Tags |
|---------|------|----------|--------|---------|------|
| Fin Head M | naga-head.local | 20 | 50 | yes | head fin headfin |
| Fin head mR | naga-head.local | 21 | 9 | no | head fin sidefin right |
| Fin head mL | naga-head.local | 22 | 9 | no | head fin sidefin |
| Fin head R | naga-head.local | 23 | 10 | no | head fin sidefin right |
| Fin head L | naga-head.local | 24 | 10 | no | head fin sidefin left |
| Fin h1 | naga-h1.local | 25 | 60 | no | h1 fin |
| Fin h2 | naga-h2.local | 26 | 52 | no | h2 fin |
| Fin h3 | naga-h3.local | 27 | 36 | no | h3 fin |
| Fin tail top | naga-tail.local | 28 | 50 | no | tail fin |
| Fin tail bottom | naga-tail.local | 29 | 50 | no | tail fin |

### Spike Fixtures

| Fixture | Host | Universe | Pixels | Reverse | Tags |
|---------|------|----------|--------|---------|------|
| Spike head M | naga-head.local | 30 | 7 | no | spike head |
| Spike head R | naga-head.local | 31 | 6 | no | head spike sidespike right |
| Spike head L | naga-head.local | 32 | 6 | no | head spike sidespike left |
| Spike head mR | naga-head.local | 33 | 5 | no | head spike sidespike right |
| Spike head mL | naga-head.local | 34 | 5 | no | head spike sidespike left |
| Spike h1 | naga-h1.local | 35 | 8 | yes | spike h1 |
| Spike h2 | naga-h2.local | 36 | 7 | no | spike h2 |
| Spike h3 | naga-h3.local | 37 | 5 | yes | spike h3 |
| Spike tail top | naga-tail.local | 38 | 4 | no | spike tail |
| Spike tail bottom | naga-tail.local | 39 | 3 | yes | spike tail |

## Art-Net Protocol

- **Protocol:** Art-Net (DMX over UDP)
- **Transport:** UDP broadcast
- **Port:** 7890
- **Encoding:** 3 bytes per pixel (RGB), so a 38-pixel fixture uses 114 bytes of DMX channel data
- **Universe range:** 10–39

Each fixture occupies one Art-Net universe. The ESP32s receive Art-Net packets addressed to their universe numbers and drive the corresponding LED strip.

## Fixture Geometry

All fixtures use the `SpiralFixture` type — a helix/spiral of LEDs wrapping around a cylindrical form. Key geometry parameters:

- **numTurns** — number of spiral revolutions (e.g., 8.5 for the head body)
- **radius** — spiral radius in model units (typically 2.0)
- **length** — total length along the Z axis
- **roll** — rotation offset in degrees
- **z** — position along the dragon's length (head=0, tail=max)

The Z-coordinate is important: many patterns (MovingStripes, SnakeCrawl) use it to create effects that travel along the dragon's body from head to tail.

## Network Architecture

```
                          Internet
                             │
                         ┌───┴───┐
                         │  LTE  │
                         │ Modem │
                         │ 192.168.5.1
                         └───┬───┘
                             │
                     ┌───────┴───────┐
                     │  Raspberry Pi │
                     │ naga-pi.local │
                     │   Tailscale   │──── Remote access
                     └───────┬───────┘
                             │ WiFi
              ┌──────┬───────┼───────┬──────┐
              │      │       │       │      │
          ┌───┴──┐┌──┴──┐┌──┴──┐┌──┴──┐┌──┴──┐
          │ HEAD ││ H1  ││ H2  ││ H3  ││ TAIL│
          │ESP32 ││ESP32││ESP32││ESP32││ESP32│
          └──┬───┘└──┬──┘└──┬──┘└──┬──┘└──┬──┘
             │       │      │      │      │
            LEDs    LEDs   LEDs   LEDs   LEDs
```

- **Pi ↔ ESP32:** Local WiFi, mDNS discovery via avahi-daemon
- **Pi ↔ Internet:** LTE modem
- **Pi ↔ Remote:** Tailscale VPN over LTE
- **Subnets:** Shore WiFi (192.168.7.0/24), Head WiFi (192.168.8.0/24), LTE (192.168.5.1/32)

## Model Files

The fixture topology is defined in `Models/Naga-Model.lxm` — a JSON file containing all 28 fixture definitions with their network config, geometry, and tags.

Variant models:
- `Naga-Model-Petaluma-Dan.lxm` — alternate location configuration
- `Naga-Simple-Model.lxm` — simplified version for testing

## Tags and Views

Fixtures are tagged for grouping into views (used in Chromatik's UI and for pattern targeting):

| Tag | Meaning |
|-----|---------|
| `body` | Main body spiral fixtures |
| `fin` | Fin fixtures |
| `spike` | Spike fixtures |
| `head`, `h1`, `h2`, `h3`, `tail` | Section identifiers |
| `headfin`, `sidefin` | Fin subtypes |
| `sidespike` | Spike subtypes |
| `left`, `right` | Lateral position |

Common view combinations: Body, Fins, Spikes, Body+Spikes, All.
