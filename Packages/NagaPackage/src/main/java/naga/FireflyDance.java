package naga;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.color.ColorParameter;
import heronarts.lx.pattern.LXPattern;

import java.util.Random;

public class FireflyDance extends LXPattern {

    final CompoundParameter speed = new CompoundParameter("Speed", 1, 0.1, 20)
            .setDescription("Controls the speed of the fireflies");

    final CompoundParameter groupSize = new CompoundParameter("GroupSize", 5, 1, 20)
            .setDescription("Controls the number of fireflies in each group");

    final CompoundParameter randomness = new CompoundParameter("Randomness", 0.5, 0, 1)
            .setDescription("Controls the randomness of the firefly movement");

    final CompoundParameter spread = new CompoundParameter("Spread", 1, 0.1, 5)
            .setDescription("Controls the spread of the fireflies across the fixture");

    final ColorParameter fireflyColor = new ColorParameter("Color", LXColor.hsb(60, 100, 100)) // Yellow color using HSB
            .setDescription("Color of the fireflies");

    private final Random random = new Random();
    private final float[] fireflyPositions;

    public FireflyDance(LX lx) {
        super(lx);
        fireflyPositions = new float[20]; // 20 is the max group size
        addParameter("speed", this.speed);
        addParameter("groupSize", this.groupSize);
        addParameter("randomness", this.randomness);
        addParameter("spread", this.spread);
        addParameter("color", this.fireflyColor);
    }

    @Override
    public void run(double deltaMs) {
        int activeFireflies = (int) groupSize.getValuef();

        for (int i = 0; i < activeFireflies; i++) {
            // Update position based on speed, spread, and randomness
            float moveAmount = speed.getValuef() * (float) deltaMs / 1000.0f * spread.getValuef();
            fireflyPositions[i] += moveAmount * (1 + randomness.getValuef() * (random.nextFloat() - 0.5f) * 2);

            // Wrap around the ends
            if (fireflyPositions[i] >= model.size) {
                fireflyPositions[i] -= model.size;
            } else if (fireflyPositions[i] < 0) {
                fireflyPositions[i] += model.size;
            }

            // Set color at the current firefly position
            int position = (int) fireflyPositions[i];
            colors[position] = fireflyColor.getColor();
        }

        // Fade out all other LEDs to create the trailing effect
        for (int i = 0; i < model.size; i++) {
            if (!isFireflyAtPosition(i, activeFireflies)) {
                colors[i] = LXColor.scaleBrightness(colors[i], 0.9f);
            }
        }
    }

    private boolean isFireflyAtPosition(int position, int activeFireflies) {
        for (int i = 0; i < activeFireflies; i++) {
            if ((int) fireflyPositions[i] == position) {
                return true;
            }
        }
        return false;
    }
}
