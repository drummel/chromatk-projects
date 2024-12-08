package naga;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.color.ColorParameter;
import heronarts.lx.pattern.LXPattern;

public class SnakeCrawl extends LXPattern {

    final CompoundParameter speed = new CompoundParameter("Speed", 0.5, 0.1, 2)
            .setDescription("Controls the speed of the wave movement");

    final CompoundParameter waveRate = new CompoundParameter("WaveRate", 0.2, 0.05, 1)
            .setDescription("Controls the rate of speed variation, creating the crawling effect");

    final ColorParameter color = new ColorParameter("Color", LXColor.hsb(120, 100, 100))
            .setDescription("Base color for the wave");

    private float zOffset = 0;
    private float waveFactor = 0;

    public SnakeCrawl(LX lx) {
        super(lx);
        addParameter("speed", this.speed);
        addParameter("waveRate", this.waveRate);
        addParameter("color", this.color);
    }

    @Override
    public void run(double deltaMs) {
        waveFactor += waveRate.getValuef() * deltaMs / 1000.0f;
        float wave = (float) Math.sin(waveFactor * 2 * Math.PI);

        zOffset += speed.getValuef() * (1 + wave) * deltaMs / 1000.0f;

        for (int i = 0; i < model.size; i++) {
            float z = model.points[i].z + zOffset;
            int colorValue = LXColor.lightest(color.getColor(), LXColor.BLACK,
                    0.5f * (1 + (float) Math.sin(z / 20.0f)));
            colors[i] = colorValue;
        }
    }
}
