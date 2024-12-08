package naga;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.color.ColorParameter;
import heronarts.lx.pattern.LXPattern;

public class HeartbeatSync extends LXPattern {

    final CompoundParameter pulseRate = new CompoundParameter("Rate", 0.5, 0.1, 2)
            .setDescription("Controls the speed of the heartbeat");

    final CompoundParameter intensity = new CompoundParameter("Intensity", 1, 0.5, 2)
            .setDescription("Adjusts the peak brightness of the heartbeat");

    final ColorParameter baseColor = new ColorParameter("BaseColor", LXColor.hsb(0, 100, 100))
            .setDescription("Sets the color of the heartbeat pulse");

    private float time = 0;

    public HeartbeatSync(LX lx) {
        super(lx);
        addParameter("pulseRate", this.pulseRate);
        addParameter("intensity", this.intensity);
        addParameter("baseColor", this.baseColor);
    }

    @Override
    public void run(double deltaMs) {
        time += deltaMs * pulseRate.getValuef();

        float brightness = (float) (Math.sin(time) * intensity.getValuef());

        if (brightness < 0) {
            brightness = 0;
        }

        for (int i = 0; i < model.size; i++) {
            colors[i] = LXColor.scaleBrightness(baseColor.getColor(), brightness);
        }

        if (time > Math.PI * 2) {
            time = 0;
        }
    }
}
