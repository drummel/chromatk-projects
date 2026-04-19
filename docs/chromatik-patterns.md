# Chromatik Pattern & Effect Catalog

All patterns and effects live in `Packages/NagaPackage/src/main/java/naga/` and extend the LX framework's `LXPattern` or `LXEffect` base classes. Each pattern implements `run(double deltaMs)` and writes RGB values into a `colors[]` array indexed by pixel.

## Patterns

### EmberGlow

Flickering ember/fire simulation. Each pixel gets a random phase offset so the flicker looks organic rather than uniform.

| Parameter | Type | Description |
|-----------|------|-------------|
| BaseColor | Color | Base ember color |
| FlickerDepth | Float | How much brightness varies per flicker |
| FlickerFrequency | Float | Speed of flicker oscillation |
| HueRange | Float | How far hue drifts from base |
| HueSpeed | Float | Speed of hue drift |

**How it works:** Sine-wave flicker per pixel with per-pixel random phase offsets and slow hue drift. Good ambient pattern.

---

### BreathingLight

Pulsing brightness that smoothly ramps up and down, like breathing. Supports up to 5 colors.

| Parameter | Type | Description |
|-----------|------|-------------|
| Speed | Float | Breathing rate |
| Brightness | Float | Min/max brightness range |
| NumColors | Int (1–5) | Number of active colors |
| Color1–Color5 | Color | Color slots |

**How it works:** Quadratic ease-in/out curve for smooth brightness transitions. Colors cycle as the breathing progresses.

---

### SnakeCrawl

A wave that moves along the dragon's body with speed modulation that creates a crawling feel.

| Parameter | Type | Description |
|-----------|------|-------------|
| Speed | Float | Base movement speed |
| WaveRate | Float | Speed modulation frequency |
| Color | Color | Wave color |

**How it works:** Sine-wave brightness variation based on each pixel's Z-coordinate. The wave factor multiplies speed to create the crawling effect.

---

### FireflyDance

Glowing point lights that drift along the dragon with fading trails.

| Parameter | Type | Description |
|-----------|------|-------------|
| Speed | Float | Movement speed |
| GroupSize | Int | Number of active fireflies |
| Randomness | Float | Position jitter |
| Spread | Float | How far apart fireflies are |
| Color | Color | Firefly color |

**How it works:** Maintains a position array for each firefly. Non-firefly pixels fade at 0.9x per frame, creating trails. Positions wrap around.

---

### FogDrift

Colored fog patches that drift across the sculpture.

| Parameter | Type | Description |
|-----------|------|-------------|
| NumColors | Int (1–5) | Number of fog colors |
| Speed | Float | Drift speed |
| Direction | Float | Movement direction |
| Opacity | Float | Fog density |
| FadeRate | Float | How quickly fog fades at edges |

**How it works:** Per-pixel position and direction with fade envelope. Multiple fog colors overlap.

---

### HeartbeatSync

Synchronized pulse across all pixels, like a heartbeat.

| Parameter | Type | Description |
|-----------|------|-------------|
| PulseRate | Float | Beats per minute feel |
| Intensity | Float | Pulse brightness |
| BaseColor | Color | Pulse color |

**How it works:** Sine-wave brightness modulation applied uniformly to all pixels.

---

### LightningStrike

Random lightning bolts with strobe flashes that spread outward from a strike point.

| Parameter | Type | Description |
|-----------|------|-------------|
| Intensity | Float | Flash brightness |
| Frequency | Float | How often strikes occur |
| Spread | Float | How far the bolt spreads |
| Speed | Float | Strike animation speed |
| Duration | Float | How long each strike lasts |
| NumColors | Int | Number of colors |
| MixColors | Boolean | Blend colors at boundaries |

**How it works:** Per-pixel strike timers and strobe counts. Radial spread from a random origin with distance attenuation.

---

### MovingStripes

Animated color bands that move along the dragon using Z-coordinates.

| Parameter | Type | Description |
|-----------|------|-------------|
| Speed | Float (bipolar) | Movement speed and direction |
| StripeLength | Float | Width of each band |
| Blend | Boolean | Smooth boundaries between stripes |
| Color1–Color5 | Color | Stripe colors |

**How it works:** `floor(z / length) % 5` maps each pixel to a color index. Optional smooth blending at stripe boundaries.

---

### MulticolorChase

Moving color segments that chase along the dragon.

| Parameter | Type | Description |
|-----------|------|-------------|
| NumColors | Int | Colors in the chase |
| ChaseSpeed | Float (bipolar) | Speed and direction |
| SegmentLength | Float | Length of each color segment |
| FadeRate | Float | Fade between segments |
| BiDirectional | Boolean | Chase from both ends |

**How it works:** Normalized position tracking with distance-based fade between segments.

---

### MulticolorSparkle

Random twinkling sparkles across the dragon.

| Parameter | Type | Description |
|-----------|------|-------------|
| NumColors | Int | Number of sparkle colors |
| SparkleSpeed | Float | How fast sparkles appear/fade |
| SparkleIntensity | Float | Maximum brightness |
| SparkleDensity | Float | How many pixels sparkle at once |
| TwinkleDuration | Float | How long each sparkle lasts |

**How it works:** Per-pixel timers with random brightness levels. Sparkles appear and fade independently.

---

### SparkleMulticolor

Advanced sparkle system with configurable waveshapes for the sparkle envelope.

| Parameter | Type | Description |
|-----------|------|-------------|
| NumColors | Int | Active colors |
| SparkleSpeed | Float | Animation speed |
| SparkleDensity | Float | Sparkle frequency |
| Waveshape | Enum | TRI, SIN, UP, DOWN, SQUARE |
| Color1–Color5 | Color | Sparkle colors |

**How it works:** Up to 1024 concurrent sparkles, each with a 0–1 lifecycle. Waveshape controls the brightness envelope (triangle, sine, ramp up, ramp down, or square). Per-pixel output is the aggregate of all overlapping sparkles.

---

### SparkleTrail

Moving pulse with trailing sparkles behind it.

| Parameter | Type | Description |
|-----------|------|-------------|
| Intensity | Float | Pulse brightness |
| Speed | Float (bipolar) | Movement speed and direction |
| Density | Float | Trail density |
| Length | Float | Trail length |
| SparkleRate | Float | How fast sparkles fire in the trail |
| NumColors | Int | Trail colors |
| NumTrails | Int (1–5) | Simultaneous trails |
| HueRotation | Float | Color shift over time |

**How it works:** Multiple simultaneous trails supported. Each trail has independent sparkle timing.

---

### SignalBurst

Expanding wave that radiates outward from a random origin point.

| Parameter | Type | Description |
|-----------|------|-------------|
| BurstWidth | Float | Width of the wave front |
| BurstTime | Float | Duration of each burst (ms) |
| BurstDelay | Float | Time between bursts |
| NumColors | Int | Wave colors |

**How it works:** Two opposing wave fronts expand from a random origin. Exponential distance expansion with time-based fade.

---

### AlternatingThreeColorPattern

Simple cycling between three colors in sequence.

| Parameter | Type | Description |
|-----------|------|-------------|
| Color1 | Color | First color (default: red) |
| Color2 | Color | Second color (default: blue) |
| Color3 | Color | Third color (default: green) |
| Speed | Float | Cycle speed |

---

### MulticolorSparkle (Test Variant)

Utility pattern for testing. Isolates a specific pixel range and sets it to a target color. All other pixels are black.

| Parameter | Type | Description |
|-----------|------|-------------|
| StartPixel | Int | First pixel in range |
| nPixels | Int | Number of pixels to light |
| TargetColor | Color | Color for the selected range |

Located in `patterns/` subdirectory (separate from the main `MulticolorSparkle`).

---

## Effects

Effects are applied after patterns and modify the pattern's output.

### PixelTuner

Fine-tune brightness, hue, and saturation for a specific pixel range. Useful for per-fixture color correction.

| Parameter | Type | Description |
|-----------|------|-------------|
| StartPixel | Int | Range start |
| EndPixel | Int | Range end |
| BrightnessAdjust | Float | Brightness offset |
| HueShift | Float | Hue rotation |
| SaturationAdjust | Float | Saturation offset |

---

### StrobeWaveEffect

Strobe effect with a moving wave envelope that controls which pixels are strobing.

| Parameter | Type | Description |
|-----------|------|-------------|
| StrobeSpeed | Float | Flash rate |
| WaveSpeed | Float (bipolar) | Wave movement speed and direction |
| Intensity | Float | Strobe brightness |

**How it works:** Sine-wave strobe combined with a moving wave envelope that sweeps across the dragon.

---

## Building

The NagaPackage is a Maven project. From `Packages/NagaPackage/`:

```bash
mvn package
```

This builds `lxpackage-naga-0.0.1.jar` and copies it to `~/Chromatik/Packages/` via the `copy-resources` plugin phase.
