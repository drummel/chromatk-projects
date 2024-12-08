package naga;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.color.ColorParameter;
import heronarts.lx.pattern.LXPattern;

import java.util.Random;

public class FogDrift extends LXPattern {

    final ColorParameter color1 = new ColorParameter("Color1", LXColor.BLUE)
            .setDescription("First color of the fog patches");
    final ColorParameter color2 = new ColorParameter("Color2", LXColor.GREEN)
            .setDescription("Second color of the fog patches");
    final ColorParameter color3 = new ColorParameter("Color3", LXColor.RED)
            .setDescription("Third color of the fog patches");
    final ColorParameter color4 = new ColorParameter("Color4", LXColor.hsb(60, 100, 100))
            .setDescription("Fourth color of the fog patches");
    final ColorParameter color5 = new ColorParameter("Color5", LXColor.hsb(300, 100, 100))
            .setDescription("Fifth color of the fog patches");

    final DiscreteParameter numColors = new DiscreteParameter("NumColors", 1, 1, 5)
            .setDescription("Controls how many colors to use for the fog patches");

    final CompoundParameter driftSpeed = new CompoundParameter("Speed", 0.5, 0.1, 2)
            .setDescription("Controls the speed of the drifting fog patches");

    final CompoundParameter driftDirection = new CompoundParameter("Direction", 0.5, 0, 1)
            .setDescription("Controls the direction of drift for the fog patches");

    final CompoundParameter opacity = new CompoundParameter("Opacity", 0.5, 0.1, 1)
            .setDescription("Adjusts the transparency of the fog patches");

    final CompoundParameter fadeRate = new CompoundParameter("FadeRate", 0.5, 0.1, 1)
            .setDescription("Controls how quickly the fog patches fade in and out");

    private final Random random = new Random();
    private final float[] patchPositions;
    private final float[] patchDirections;

    public FogDrift(LX lx) {
        super(lx);
        patchPositions = new float[model.size];
        patchDirections = new float[model.size];
        addParameter("color1", this.color1);
        addParameter("color2", this.color2);
        addParameter("color3", this.color3);
        addParameter("color4", this.color4);
        addParameter("color5", this.color5);
        addParameter("numColors", this.numColors);
        addParameter("driftSpeed", this.driftSpeed);
        addParameter("driftDirection", this.driftDirection);
        addParameter("opacity", this.opacity);
        addParameter("fadeRate", this.fadeRate);

        initializePatchPositions();
    }

    private void initializePatchPositions() {
        for (int i = 0; i < model.size; i++) {
            patchPositions[i] = random.nextFloat();
            patchDirections[i] = (random.nextFloat() - 0.5f) * 2 * driftDirection.getValuef();
        }
    }

    @Override
    public void run(double deltaMs) {
        int colorsToUse = numColors.getValuei();

        for (int i = 0; i < model.size; i++) {
            patchPositions[i] += driftSpeed.getValuef() * patchDirections[i] * deltaMs / 1000.0f;

            if (patchPositions[i] > 1.0) {
                patchPositions[i] -= 1.0;
            } else if (patchPositions[i] < 0.0) {
                patchPositions[i] += 1.0;
            }

            float fade = Math.max(0, 1.0f - Math.abs(patchPositions[i] - 0.5f) * 2.0f) * fadeRate.getValuef();
            int color = LXColor.scaleBrightness(getRandomColor(colorsToUse), opacity.getValuef() * fade);

            colors[i] = LXColor.add(colors[i], color);
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
