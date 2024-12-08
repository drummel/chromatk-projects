package naga.effects;

import heronarts.lx.LX;
import heronarts.lx.effect.LXEffect;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.color.LXColor;
import heronarts.lx.utils.LXUtils;

public class PixelTuner extends LXEffect {

    // Parameters for controlling the effect
    final DiscreteParameter startPixel = new DiscreteParameter("Start", 4, 0, model.size - 1)
            .setDescription("Starting pixel of the range to be adjusted");

    final DiscreteParameter endPixel = new DiscreteParameter("End", Math.min(model.size - 1, 10), 0, model.size - 1)
            .setDescription("Ending pixel of the range to be adjusted");

    final CompoundParameter brightnessAdjust = new CompoundParameter("Brightness", 0.5, 0.0, 2.0)
            .setDescription("Adjusts the brightness of the selected pixel range (0.0 to 2.0)");

    final CompoundParameter hueShift = new CompoundParameter("HueShift", 0, -180, 180)
            .setDescription("Shifts the hue of the selected pixel range (-180 to 180 degrees)");

    final CompoundParameter saturationAdjust = new CompoundParameter("Saturation", 1.0, 0.0, 2.0)
            .setDescription("Adjusts the saturation of the selected pixel range (0.0 to 2.0)");

    public PixelTuner(LX lx) {
        super(lx);
        addParameter("startPixel", this.startPixel);
        addParameter("endPixel", this.endPixel);
        addParameter("brightnessAdjust", this.brightnessAdjust);
        addParameter("hueShift", this.hueShift);
        addParameter("saturationAdjust", this.saturationAdjust);
    }

    @Override
    protected void run(double deltaMs, double amount) {
        int start = startPixel.getValuei();
        int end = endPixel.getValuei();

        for (int i = start; i <= end; i++) {
            float hue = LXColor.h(colors[i]) + hueShift.getValuef();
            hue = (hue + 360) % 360; // Ensure hue stays within 0-360 degrees

            double saturation = LXColor.s(colors[i]) * saturationAdjust.getValuef();
            saturation = LXUtils.clamp(saturation, 0, 100);

            double brightness = LXColor.b(colors[i]) * brightnessAdjust.getValuef();
            brightness = LXUtils.clamp(brightness, 0, 100);

            colors[i] = LXColor.hsb(hue, saturation, brightness);
        }
    }
}
