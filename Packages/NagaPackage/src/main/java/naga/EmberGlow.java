package naga;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.color.ColorParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.pattern.LXPattern;
import heronarts.lx.model.LXPoint;

import java.util.Random;

public class EmberGlow extends LXPattern {

    private final Random random = new Random();
    private double elapsedTime = 0; // Accumulates the elapsed time
    private final double[] phaseOffsets; // Random phase offsets for each pixel
    private final int[] currentColors; // Stores the current color for each pixel
    private final double[] hueSpeeds; // Stores the hue rotation speed for each pixel

    // Parameters for controlling the pattern
    final ColorParameter baseColor = new ColorParameter("BaseColor", LXColor.hsb(30, 100, 100))
            .setDescription("Sets the base color of the embers (default: deep orange)");

    final CompoundParameter flickerDepth = new CompoundParameter("FlickerDepth", 1, 0, 10)
            .setDescription("Controls the depth of the flicker effect");

    final CompoundParameter flickerFrequency = new CompoundParameter("FlickerFreq", 1.0, 0.1, 5.0)
            .setDescription("Controls the frequency of the flicker effect");

    final CompoundParameter hueRange = new CompoundParameter("HueRange", 25, 0, 180)
            .setDescription("Controls the range of hue variation around the base color");

    final CompoundParameter hueSpeed = new CompoundParameter("HueSpeed", 0.1, 0.01, 1.0)
            .setDescription("Controls how fast the hue rotates over time");

    public EmberGlow(LX lx) {
        super(lx);
        addParameter("baseColor", this.baseColor);
        addParameter("flickerDepth", this.flickerDepth);
        addParameter("flickerFrequency", this.flickerFrequency);
        addParameter("hueRange", this.hueRange);
        addParameter("hueSpeed", this.hueSpeed);

        // Initialize phase offsets, current colors, and hue speeds for each pixel
        phaseOffsets = new double[model.size];
        currentColors = new int[model.size];
        hueSpeeds = new double[model.size];
        for (int i = 0; i < model.size; i++) {
            phaseOffsets[i] = random.nextDouble() * Math.PI * 2; // Random phase between 0 and 2*PI
            currentColors[i] = getRandomColor(); // Initialize with a random color
            hueSpeeds[i] = 10.0 * (-0.5 + random.nextDouble()); // Vary the hue speed slightly
        }
    }

    @Override
    public void run(double deltaMs) {
        // Update the elapsed time
        elapsedTime += deltaMs / 1000.0; // Convert milliseconds to seconds

        // Get the base color saturation
        float baseHue = LXColor.h(baseColor.getColor());
        float baseSaturation = LXColor.s(baseColor.getColor());

        for (int i = 0; i < model.size; i++) {
            LXPoint p = model.points[i];

            // Flicker intensity varies over time using a sine wave with a random phase
            // offset
            double flickerIntensity = (flickerDepth.getValue() / 500.0)
                    * (0.5 + 0.5 * Math.sin(2 * Math.PI * flickerFrequency.getValue() * elapsedTime + phaseOffsets[i]));

            // Adjust brightness based on flicker intensity
            float baseBrightness = LXColor.b(currentColors[i]);
            float flickerBrightness = baseBrightness * (1 - (float) flickerIntensity);

            // Rotate hue by a small amount in the direction and magnitued determined by the
            // hue speed
            float rotatedHue = (LXColor.h(currentColors[i]) + (float) (hueSpeeds[i] * hueSpeed.getValue())) % 360;

            // If the new hue is beyond the range of the base color +/- hue range, reset it
            if (rotatedHue < baseHue - hueRange.getValue()) {
                rotatedHue = (float) (baseHue - hueRange.getValue());
                hueSpeeds[i] = -hueSpeeds[i]; // Reverse direction
            } else if (rotatedHue > baseHue + hueRange.getValue()) {
                rotatedHue = (float) (baseHue + hueRange.getValue());
                hueSpeeds[i] = -hueSpeeds[i]; // Reverse direction
            }

            // Apply the new color to the pixel
            currentColors[p.index] = LXColor.hsb(rotatedHue, 100, 100);
            colors[p.index] = LXColor.hsb(rotatedHue, baseSaturation, flickerBrightness);
        }
    }

    // Method to get a random color based on the selected base color and hue range
    private int getRandomColor() {
        float baseHue = LXColor.h(baseColor.getColor());
        float hueShift = (float) ((random.nextDouble() * 2 - 1) * hueRange.getValue());
        return LXColor.hsb((baseHue + hueShift + 360) % 360, 100, 100);
    }
}
