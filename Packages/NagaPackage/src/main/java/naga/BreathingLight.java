package naga;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.color.ColorParameter;
import heronarts.lx.pattern.LXPattern;

public class BreathingLight extends LXPattern {

    final CompoundParameter breathingSpeed = new CompoundParameter("Speed", 0.5, 0.1, 2)
            .setDescription("Controls the speed of the breathing effect");

    final CompoundParameter brightnessRange = new CompoundParameter("Brightness", 0.5, 0.1, 1)
            .setDescription("Adjusts the range of brightness during the breathing cycle");

    final DiscreteParameter numColors = new DiscreteParameter("NumColors", 1, 1, 5)
            .setDescription("Controls how many colors to use for the breathing effect");

    final ColorParameter color1 = new ColorParameter("Color1", LXColor.BLUE)
            .setDescription("First color of the breathing effect");
    final ColorParameter color2 = new ColorParameter("Color2", LXColor.GREEN)
            .setDescription("Second color of the breathing effect");
    final ColorParameter color3 = new ColorParameter("Color3", LXColor.RED)
            .setDescription("Third color of the breathing effect");
    final ColorParameter color4 = new ColorParameter("Color4", LXColor.hsb(60, 100, 100)) // Yellow
            .setDescription("Fourth color of the breathing effect");
    final ColorParameter color5 = new ColorParameter("Color5", LXColor.hsb(300, 100, 100)) // Purple
            .setDescription("Fifth color of the breathing effect");

    private float phase = 0;

    public BreathingLight(LX lx) {
        super(lx);
        addParameter("breathingSpeed", this.breathingSpeed);
        addParameter("brightnessRange", this.brightnessRange);
        addParameter("numColors", this.numColors);
        addParameter("color1", this.color1);
        addParameter("color2", this.color2);
        addParameter("color3", this.color3);
        addParameter("color4", this.color4);
        addParameter("color5", this.color5);
    }

    @Override
    public void run(double deltaMs) {
        // Increment the phase based on breathing speed
        phase += deltaMs * breathingSpeed.getValuef() / 1000.0f;
        if (phase > 1.0f) {
            phase -= 1.0f;
        }

        int colorsToUse = numColors.getValuei();
        float brightness = calculateBrightness(phase);

        for (int i = 0; i < model.size; i++) {
            colors[i] = LXColor.scaleBrightness(getColorForPhase(phase, colorsToUse), brightness);
        }
    }

    private float calculateBrightness(float phase) {
        // Quadratic curve for smooth breathing effect
        if (phase < 0.5f) {
            return brightnessRange.getValuef() * (4 * phase * phase);
        } else {
            return brightnessRange.getValuef() * (-4 * phase * phase + 4 * phase - 1);
        }
    }

    private int getColorForPhase(float phase, int colorsToUse) {
        float cyclePosition = phase * colorsToUse;
        int colorIndex = (int) cyclePosition % colorsToUse;

        switch (colorIndex) {
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
