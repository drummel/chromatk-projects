package naga;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.color.ColorParameter;
import heronarts.lx.pattern.LXPattern;

public class MovingStripes extends LXPattern {

    final CompoundParameter speed = new CompoundParameter("Speed", 0, -1, 1)
            .setUnits(CompoundParameter.Units.PERCENT)
            .setDescription("Controls the speed and direction of the stripe movement")
            .setPolarity(LXParameter.Polarity.BIPOLAR);

    final CompoundParameter stripeLength = new CompoundParameter("Length", 50, 1, 200);
    final BooleanParameter blend = new BooleanParameter("Blend", false);

    // Default colors set to a rainbow-like sequence
    final ColorParameter color1 = new ColorParameter("Color1", LXColor.hsb(0, 100, 100)); // Red
    final ColorParameter color2 = new ColorParameter("Color2", LXColor.hsb(45, 100, 100)); // Orange
    final ColorParameter color3 = new ColorParameter("Color3", LXColor.hsb(120, 100, 100)); // Green
    final ColorParameter color4 = new ColorParameter("Color4", LXColor.hsb(180, 100, 100)); // Cyan
    final ColorParameter color5 = new ColorParameter("Color5", LXColor.hsb(270, 100, 100)); // Violet

    private float zOffset = 0;

    public MovingStripes(LX lx) {
        super(lx);
        addParameter("speed", this.speed);
        addParameter("length", this.stripeLength);
        addParameter("blend", this.blend);
        addParameter("color1", this.color1);
        addParameter("color2", this.color2);
        addParameter("color3", this.color3);
        addParameter("color4", this.color4);
        addParameter("color5", this.color5);
    }

    @Override
    public void run(double deltaMs) {
        zOffset += speed.getValuef() * deltaMs;

        for (int i = 0; i < model.size; i++) {
            float z = model.points[i].z + zOffset;
            float adjustedLength = stripeLength.getValuef();

            int colorIndex = ((int) Math.floor(z / adjustedLength)) % 5;
            if (colorIndex < 0) {
                colorIndex += 5;
            }

            int nextColorIndex = (colorIndex + 1) % 5;
            float blendAmount = (z % adjustedLength) / adjustedLength;

            if (blend.getValueb() && blendAmount > 0.6) { // 40% overlap
                blendAmount = (blendAmount - 0.6f) / 0.4f;
                colors[i] = LXColor.lerp(getColorByIndex(colorIndex), getColorByIndex(nextColorIndex), blendAmount);
            } else {
                colors[i] = getColorByIndex(colorIndex);
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
