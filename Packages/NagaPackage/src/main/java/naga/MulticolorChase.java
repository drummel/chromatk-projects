package naga;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.color.ColorParameter;
import heronarts.lx.pattern.LXPattern;

public class MulticolorChase extends LXPattern {

    final ColorParameter color1 = new ColorParameter("Color1", LXColor.BLUE)
            .setDescription("First color of the chase segments");
    final ColorParameter color2 = new ColorParameter("Color2", LXColor.GREEN)
            .setDescription("Second color of the chase segments");
    final ColorParameter color3 = new ColorParameter("Color3", LXColor.RED)
            .setDescription("Third color of the chase segments");
    final ColorParameter color4 = new ColorParameter("Color4", LXColor.hsb(60, 100, 100))
            .setDescription("Fourth color of the chase segments");
    final ColorParameter color5 = new ColorParameter("Color5", LXColor.hsb(300, 100, 100))
            .setDescription("Fifth color of the chase segments");

    final CompoundParameter numColors = new CompoundParameter("NumColors", 1, 1, 5)
            .setDescription("Controls how many colors to use for the chase segments");

    final CompoundParameter chaseSpeed = new CompoundParameter("Speed", 0.5, -2, 2)
            .setDescription("Controls the speed and direction of the chase movement");

    final CompoundParameter segmentLength = new CompoundParameter("Length", 10, 1, 50)
            .setDescription("Adjusts the length of each color segment in the chase");

    final CompoundParameter fadeRate = new CompoundParameter("FadeRate", 0.5, 0.1, 1)
            .setDescription("Controls how quickly each segment fades out before the next segment appears");

    final BooleanParameter biDirectional = new BooleanParameter("BiDirectional", false)
            .setDescription("Toggle for enabling bi-directional chase movement");

    private float position = 0;

    public MulticolorChase(LX lx) {
        super(lx);
        addParameter("color1", this.color1);
        addParameter("color2", this.color2);
        addParameter("color3", this.color3);
        addParameter("color4", this.color4);
        addParameter("color5", this.color5);
        addParameter("numColors", this.numColors);
        addParameter("chaseSpeed", this.chaseSpeed);
        addParameter("segmentLength", this.segmentLength);
        addParameter("fadeRate", this.fadeRate);
        addParameter("biDirectional", this.biDirectional);
    }

    @Override
    public void run(double deltaMs) {
        position += deltaMs * chaseSpeed.getValuef() / 1000.0;

        int colorsToUse = (int) numColors.getValuef();
        float segmentPosition = position % 1.0f;

        for (int i = 0; i < model.size; i++) {
            float pos = (float) i / model.size;
            float distanceFromSegment = Math.abs(pos - segmentPosition);

            if (distanceFromSegment < segmentLength.getValuef() / model.size) {
                float fade = 1.0f
                        - (distanceFromSegment / (segmentLength.getValuef() / model.size)) * fadeRate.getValuef();
                colors[i] = LXColor.scaleBrightness(getColorByIndex((int) ((pos * colorsToUse) % colorsToUse)), fade);
            } else {
                colors[i] = LXColor.BLACK;
            }

            if (biDirectional.isOn()) {
                float reversePosition = (1.0f - segmentPosition) % 1.0f;
                float reverseDistanceFromSegment = Math.abs(pos - reversePosition);

                if (reverseDistanceFromSegment < segmentLength.getValuef() / model.size) {
                    float reverseFade = 1.0f - (reverseDistanceFromSegment / (segmentLength.getValuef() / model.size))
                            * fadeRate.getValuef();
                    colors[i] = LXColor.add(colors[i], LXColor
                            .scaleBrightness(getColorByIndex((int) ((pos * colorsToUse) % colorsToUse)), reverseFade));
                }
            }
        }
    }

    private int getColorByIndex(int index) {
        switch (index) {
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
