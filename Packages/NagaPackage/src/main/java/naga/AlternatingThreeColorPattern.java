package naga;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.color.ColorParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.pattern.LXPattern;

public class AlternatingThreeColorPattern extends LXPattern {

    final ColorParameter color1 = new ColorParameter("Color1", LXColor.RED);
    final ColorParameter color2 = new ColorParameter("Color2", LXColor.BLUE);
    final ColorParameter color3 = new ColorParameter("Color3", LXColor.GREEN);
    final CompoundParameter speed = new CompoundParameter("Speed", 1, 0.1, 5);

    public AlternatingThreeColorPattern(LX lx) {
        super(lx);
        addParameter("color1", this.color1);
        addParameter("color2", this.color2);
        addParameter("color3", this.color3);
        addParameter("speed", this.speed);
    }

    @Override
    public void run(double deltaMs) {
        for (int i = 0; i < model.size; i++) {
            switch (i % 3) {
                case 0:
                    colors[i] = color1.getColor();
                    break;
                case 1:
                    colors[i] = color2.getColor();
                    break;
                case 2:
                    colors[i] = color3.getColor();
                    break;
            }
        }
    }
}
