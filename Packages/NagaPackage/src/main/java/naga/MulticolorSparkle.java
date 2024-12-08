package naga;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.color.ColorParameter;
import heronarts.lx.pattern.LXPattern;

import java.util.Random;

public class MulticolorSparkle extends LXPattern {

    final ColorParameter color1 = new ColorParameter("Color1", LXColor.BLUE)
            .setDescription("First color of the sparkles");
    final ColorParameter color2 = new ColorParameter("Color2", LXColor.GREEN)
            .setDescription("Second color of the sparkles");
    final ColorParameter color3 = new ColorParameter("Color3", LXColor.RED)
            .setDescription("Third color of the sparkles");
    final ColorParameter color4 = new ColorParameter("Color4", LXColor.hsb(60, 100, 100)) // Yellow
            .setDescription("Fourth color of the sparkles");
    final ColorParameter color5 = new ColorParameter("Color5", LXColor.hsb(300, 100, 100)) // Purple
            .setDescription("Fifth color of the sparkles");

    final DiscreteParameter numColors = new DiscreteParameter("NumColors", 1, 1, 5)
            .setDescription("Controls how many colors to use for the sparkles");

    final CompoundParameter sparkleSpeed = new CompoundParameter("Speed", 0.5, 0.1, 2)
            .setDescription("Controls the speed at which sparkles appear and disappear");

    final CompoundParameter sparkleIntensity = new CompoundParameter("Intensity", 1, 0.5, 2)
            .setDescription("Adjusts the brightness of each sparkle");

    final CompoundParameter sparkleDensity = new CompoundParameter("Density", 0.2, 0.01, 1)
            .setDescription("Controls the density of sparkles along the strip");

    final CompoundParameter twinkleDuration = new CompoundParameter("Twinkle", 500, 100, 2000)
            .setDescription("Sets how long each sparkle remains visible");

    private final Random random = new Random();
    private final float[] sparkleTimers;

    public MulticolorSparkle(LX lx) {
        super(lx);
        sparkleTimers = new float[model.size];
        addParameter("color1", this.color1);
        addParameter("color2", this.color2);
        addParameter("color3", this.color3);
        addParameter("color4", this.color4);
        addParameter("color5", this.color5);
        addParameter("numColors", this.numColors);
        addParameter("sparkleSpeed", this.sparkleSpeed);
        addParameter("sparkleIntensity", this.sparkleIntensity);
        addParameter("sparkleDensity", this.sparkleDensity);
        addParameter("twinkleDuration", this.twinkleDuration);
    }

    @Override
    public void run(double deltaMs) {
        int colorsToUse = numColors.getValuei();

        for (int i = 0; i < model.size; i++) {
            if (sparkleTimers[i] > 0) {
                sparkleTimers[i] -= deltaMs;
            } else if (random.nextFloat() < sparkleDensity.getValuef()) {
                sparkleTimers[i] = random.nextFloat() * twinkleDuration.getValuef();
            }

            if (sparkleTimers[i] > 0) {
                colors[i] = LXColor.scaleBrightness(getRandomColor(colorsToUse), sparkleIntensity.getValuef());
            } else {
                colors[i] = LXColor.BLACK;
            }
        }
    }

    private int getRandomColor(int colorsToUse) {
        switch (random.nextInt(colorsToUse)) {
            case 0:
                return color1.getColor();
            case 1:
                return color2.getColor();
            case 2:
                return color3.getColor();
            case 3:
                return color4.getColor();
            case 4:
                return color5.getColor();
            default:
                return LXColor.BLACK;
        }
    }
}
