package naga.effects;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.effect.LXEffect;

public class StrobeWaveEffect extends LXEffect {

    final CompoundParameter strobeSpeed = new CompoundParameter("StrobeSpeed", 1, 0.1, 5)
            .setDescription("Controls the speed of the strobing effect");

    final CompoundParameter waveSpeed = new CompoundParameter("WaveSpeed", 0.5, -2, 2)
            .setDescription("Controls the speed and direction of the wave movement");

    final CompoundParameter intensity = new CompoundParameter("Intensity", 1, 0, 1)
            .setDescription("Adjusts the brightness of the strobe effect");

    private float position = 0;

    public StrobeWaveEffect(LX lx) {
        super(lx);
        addParameter("strobeSpeed", this.strobeSpeed);
        addParameter("waveSpeed", this.waveSpeed);
        addParameter("intensity", this.intensity);
    }

    @Override
    protected void run(double deltaMs, double amount) {
        position += deltaMs * waveSpeed.getValuef() / 1000.0;

        for (int i = 0; i < model.size; i++) {
            float pos = (float) i / model.size;
            float distanceFromWave = Math.abs(pos - position % 1.0f);

            float strobeEffect = (Math.sin(pos * strobeSpeed.getValuef() * Math.PI * 2) > 0) ? 1.0f : 0.0f;
            float waveEffect = Math.max(0, 1.0f - (distanceFromWave * 2)) * intensity.getValuef();

            colors[i] = LXColor.scaleBrightness(colors[i], waveEffect * strobeEffect * amount);
        }
    }
}
