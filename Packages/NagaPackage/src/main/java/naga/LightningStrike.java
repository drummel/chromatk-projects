package naga;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.color.ColorParameter;
import heronarts.lx.pattern.LXPattern;

import java.util.Random;

public class LightningStrike extends LXPattern {

    final CompoundParameter intensity = new CompoundParameter("Intensity", 5, 1, 10)
            .setDescription("Controls the intensity of the lightning strikes");

    final CompoundParameter frequency = new CompoundParameter("Frequency", 5, 1, 10)
            .setDescription("Controls the frequency of the lightning strikes");

    final CompoundParameter spread = new CompoundParameter("Spread", 10, 1, 50)
            .setDescription("Controls how far the lightning spreads from the strike point");

    final CompoundParameter speed = new CompoundParameter("Speed", 1, 0.1, 5)
            .setDescription("Controls the speed of the strobe and fade effect");

    final CompoundParameter duration = new CompoundParameter("Duration", 1000, 500, 3000)
            .setDescription("Controls the total duration of each strike in milliseconds");

    final DiscreteParameter numColors = new DiscreteParameter("NumColors", 1, 1, 5)
            .setDescription("Controls how many colors to use for the lightning strikes");

    final BooleanParameter mixColors = new BooleanParameter("MixColors", false)
            .setDescription(
                    "If enabled, uses a random color for each flash during the strike instead of a fixed color");

    final ColorParameter color1 = new ColorParameter("Color1", LXColor.WHITE)
            .setDescription("First color of the lightning strikes");
    final ColorParameter color2 = new ColorParameter("Color2", LXColor.hsb(60, 100, 100)) // Yellow
            .setDescription("Second color of the lightning strikes");
    final ColorParameter color3 = new ColorParameter("Color3", LXColor.hsb(30, 100, 100)) // Orange
            .setDescription("Third color of the lightning strikes");
    final ColorParameter color4 = new ColorParameter("Color4", LXColor.hsb(0, 100, 100)) // Red
            .setDescription("Fourth color of the lightning strikes");
    final ColorParameter color5 = new ColorParameter("Color5", LXColor.hsb(300, 100, 100)) // Purple
            .setDescription("Fifth color of the lightning strikes");

    private final Random random = new Random();
    private final float[] strikeTimers;
    private final int[] strobeCount;
    private final int[] activeColor; // Stores the color selected for each strike

    public LightningStrike(LX lx) {
        super(lx);
        strikeTimers = new float[model.size];
        strobeCount = new int[model.size];
        activeColor = new int[model.size];
        addParameter("intensity", this.intensity);
        addParameter("frequency", this.frequency);
        addParameter("spread", this.spread);
        addParameter("speed", this.speed);
        addParameter("duration", this.duration);
        addParameter("numColors", this.numColors);
        addParameter("mixColors", this.mixColors);
        addParameter("color1", this.color1);
        addParameter("color2", this.color2);
        addParameter("color3", this.color3);
        addParameter("color4", this.color4);
        addParameter("color5", this.color5);
    }

    @Override
    public void run(double deltaMs) {
        int strikeIndex = random.nextInt(model.size);
        float strikeProbability = frequency.getValuef() * ((float) deltaMs / 1000.0f);
        int colorsToUse = numColors.getValuei();

        for (int i = 0; i < model.size; i++) {
            if (strikeTimers[i] > 0) {
                strikeTimers[i] -= deltaMs * speed.getValuef();

                if (strobeCount[i] > 0) {
                    if ((int) (strikeTimers[i] / 50) % 2 == 0) {
                        strobeCount[i]--;
                        for (int j = Math.max(0, i - (int) spread.getValuef()); j < Math.min(model.size,
                                i + (int) spread.getValuef()); j++) {
                            colors[j] = LXColor.scaleBrightness(
                                    mixColors.isOn() ? getRandomColor(colorsToUse) : activeColor[i],
                                    intensity.getValuef() * (1.0f - (Math.abs(i - j) / spread.getValuef()))
                                            * (strobeCount[i] / 6.0f));
                        }
                    } else {
                        colors[i] = LXColor.BLACK;
                    }
                } else if ((int) (strikeTimers[i] / 200) % 2 == 0) {
                    for (int j = Math.max(0, i - (int) spread.getValuef()); j < Math.min(model.size,
                            i + (int) spread.getValuef()); j++) {
                        float fadeFactor = (1.0f - (Math.abs(i - j) / spread.getValuef()))
                                * (strikeTimers[i] / duration.getValuef());
                        colors[j] = LXColor.scaleBrightness(
                                mixColors.isOn() ? getRandomColor(colorsToUse) : activeColor[i],
                                intensity.getValuef() * fadeFactor);
                    }
                } else {
                    colors[i] = LXColor.BLACK;
                }
            } else if (random.nextFloat() < strikeProbability && i == strikeIndex) {
                strikeTimers[i] = duration.getValuef();
                strobeCount[i] = random.nextInt(4) + 3; // 3 to 6 initial strobes
                activeColor[i] = getRandomColor(colorsToUse); // Store the color for this strike
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
