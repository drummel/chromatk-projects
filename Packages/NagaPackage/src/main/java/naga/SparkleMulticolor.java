package naga;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.LXWaveshape;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.ObjectParameter;
import heronarts.lx.color.ColorParameter;
import heronarts.lx.pattern.LXPattern;
import heronarts.lx.utils.LXUtils;

import java.util.Random;

public class SparkleMulticolor extends LXPattern {

    // Inner class representing an individual sparkle
    public static class Sparkle {
        private boolean isOn = false; // Whether the sparkle is currently active
        private double basis; // Position of the sparkle in its lifecycle (0-1)
        // private double randomVar; // Random variance factor for timing
        private double randomLevel; // Random brightness level
        private int sparkleColor; // The color of the sparkle
        private final int[] indexBuffer; // The pixels this sparkle affects

        // Constructor initializes a sparkle with random settings
        private Sparkle(LXModel model, int maxPixelsPerSparkle) {
            this.basis = 0;
            // this.randomVar = Math.random();
            this.randomLevel = Math.random();
            this.indexBuffer = new int[maxPixelsPerSparkle];
            reindex(model);
        }

        // Assign random pixels for this sparkle to affect
        private void reindex(LXModel model) {
            for (int i = 0; i < this.indexBuffer.length; ++i) {
                this.indexBuffer[i] = LXUtils.randomi(model.size - 1);
            }
        }

        // Resets the sparkle with new timing and color
        private void reset(LXModel model, int newColor) {
            this.basis = 0;
            this.isOn = true;
            this.sparkleColor = newColor;
            reindex(model);
        }
    }

    private final static int MAX_SPARKLES = 1024; // Maximum number of sparkles
    private final Sparkle[] sparkles; // Array to hold all sparkles
    private final double[] outputLevels; // Brightness levels for each pixel

    // Parameters for controlling the sparkle effect
    final DiscreteParameter numColors = new DiscreteParameter("NumColors", 3, 1, 5)
            .setDescription("Selects how many of the five color parameters to use");

    final CompoundParameter sparkleSpeed = new CompoundParameter("Speed", 0.5, 0, 1)
            .setDescription("Controls the speed at which sparkles appear and disappear");

    final CompoundParameter sparkleDensity = new CompoundParameter("Density", 0.5, 0, 1)
            .setDescription("Controls how densely packed the sparkles are along the strip");

    final ObjectParameter<LXWaveshape> waveshape = new ObjectParameter<>("Wave", new LXWaveshape[] {
            LXWaveshape.TRI,
            LXWaveshape.SIN,
            LXWaveshape.UP,
            LXWaveshape.DOWN,
            LXWaveshape.SQUARE
    }).setDescription("Waveshape used for the sparkle effect");

    // Parameters for the colors that sparkles can use
    final ColorParameter color1 = new ColorParameter("Color1", LXColor.RED);
    final ColorParameter color2 = new ColorParameter("Color2", LXColor.BLUE);
    final ColorParameter color3 = new ColorParameter("Color3", LXColor.GREEN);
    final ColorParameter color4 = new ColorParameter("Color4", LXColor.hsb(60, 100, 100)); // Yellow
    final ColorParameter color5 = new ColorParameter("Color5", LXColor.hsb(300, 100, 100)); // Magenta

    private final Random random = new Random();

    // Constructor initializes the sparkle array and sets up parameters
    public SparkleMulticolor(LX lx) {
        super(lx);
        int numSparkles = LXUtils.min(model.size, MAX_SPARKLES);
        int maxPixelsPerSparkle = (int) Math.ceil(4.0 * model.size / numSparkles);

        sparkles = new Sparkle[numSparkles];
        for (int i = 0; i < numSparkles; ++i) {
            sparkles[i] = new Sparkle(model, maxPixelsPerSparkle);
        }

        outputLevels = new double[model.size];
        addParameter("numColors", this.numColors);
        addParameter("sparkleSpeed", this.sparkleSpeed);
        addParameter("sparkleDensity", this.sparkleDensity);
        addParameter("waveshape", this.waveshape);
        addParameter("color1", this.color1);
        addParameter("color2", this.color2);
        addParameter("color3", this.color3);
        addParameter("color4", this.color4);
        addParameter("color5", this.color5);
    }

    @Override
    public void run(double deltaMs) {
        int colorsToUse = numColors.getValuei();
        float speed = sparkleSpeed.getValuef();
        float density = sparkleDensity.getValuef();
        LXWaveshape shape = waveshape.getObject();

        // Clear the output levels before computing new values
        for (int i = 0; i < model.size; ++i) {
            outputLevels[i] = 0;
        }

        // Process each sparkle
        for (Sparkle sparkle : sparkles) {
            // Check if the sparkle should be turned on based on density
            if (!sparkle.isOn && random.nextFloat() < density) {
                sparkle.reset(model, getRandomColor(colorsToUse));
            }

            // If the sparkle is active, update its lifecycle
            if (sparkle.isOn) {
                sparkle.basis += speed * deltaMs / 1000.0;
                if (sparkle.basis > 1) {
                    sparkle.isOn = false; // Turn off the sparkle after one cycle
                }

                // Calculate the brightness using the waveshape and apply to the pixels
                double brightness = shape.compute(sparkle.basis) * sparkle.randomLevel * 100;
                for (int c = 0; c < sparkle.indexBuffer.length; ++c) {
                    outputLevels[sparkle.indexBuffer[c]] += brightness;
                }
            }
        }

        // Apply the brightness and color to each pixel
        int i = 0;
        for (LXPoint p : model.points) {
            colors[p.index] = LXColor.scaleBrightness(sparkles[i].sparkleColor,
                    LXUtils.clamp(outputLevels[i++], 0, 100));
        }
    }

    // Method to get a random color based on the selected number of colors
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
