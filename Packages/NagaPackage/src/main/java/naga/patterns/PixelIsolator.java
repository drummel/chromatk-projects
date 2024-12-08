package naga.patterns;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.color.ColorParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.pattern.LXPattern;

public class PixelIsolator extends LXPattern {

        // Parameters for controlling the effect
        final DiscreteParameter startPixel = new DiscreteParameter("Start", 0, 0, model.size - 1)
                        .setDescription("Starting pixel of the range to be adjusted");

        final DiscreteParameter nPixels = new DiscreteParameter("nPixels", 10, 1, model.size)
                        .setDescription("Number of pixels to be adjusted");

        final ColorParameter targetColor = new ColorParameter("Color", LXColor.WHITE)
                        .setDescription("Sets a specific color for the selected pixel range");

        public PixelIsolator(LX lx) {
                super(lx);
                addParameter("startPixel", this.startPixel);
                addParameter("nPixels", this.nPixels);
                addParameter("targetColor", this.targetColor);
        }

        @Override
        public void run(double deltaMs) {
                int start = startPixel.getValuei();
                int end = Math.min(start + nPixels.getValuei(), colors.length);

                // loop through and make all the pixels black unless they are in the range
                for (int i = 0; i < colors.length; i++) {
                        if (i < start || i >= end) {
                                colors[i] = LXColor.BLACK;
                        } else {
                                colors[i] = targetColor.getColor();
                        }
                }
        }
}