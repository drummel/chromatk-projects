package naga;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.color.ColorParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.pattern.LXPattern;
import heronarts.lx.model.LXPoint;

import java.util.Random;

public class SignalBurst extends LXPattern {

    private final Random random = new Random();

    // Parameters for controlling the pattern
    final CompoundParameter burstWidth = new CompoundParameter("BurstWidth", .03, 0.01, 0.25)
            .setDescription("Controls the width of the bursts");

    final CompoundParameter totalBurstTime = new CompoundParameter("BurstTime", 1000, 400, 3000)
            .setDescription("Controls the total time of the bursts");

    final CompoundParameter burstDelay = new CompoundParameter("BurstDelay", 500, 1, 3000)
            .setDescription("Controls the delay between bursts");

    final DiscreteParameter numColors = new DiscreteParameter("NumColors", 1, 1, 6)
            .setDescription("Selects how many of the three color parameters to use");

    final ColorParameter color1 = new ColorParameter("Color1", LXColor.RED);
    final ColorParameter color2 = new ColorParameter("Color2", LXColor.GREEN);
    final ColorParameter color3 = new ColorParameter("Color3", LXColor.BLUE);
    final ColorParameter color4 = new ColorParameter("Color4", LXColor.hsb(60, 100, 100)); // Yellow
    final ColorParameter color5 = new ColorParameter("Color5", LXColor.hsb(300, 100, 100)); // Purple

    private double originZ = 0; // The origin z-coordinate of the current burst
    private double maxZ = 0; // Maximum z-coordinate in the model
    private double maxDistance = 0; // Maximum distance for fading calculation
    private double delay = 0; // Time until the next burst
    private double burstTime = 0; // Time since the current burst started
    private int color; // The color of the current burst

    public SignalBurst(LX lx) {
        super(lx);

        // Burst Parameters
        addParameter("burstWidth", this.burstWidth);
        addParameter("totalBurstTime", this.totalBurstTime);
        addParameter("burstDelay", this.burstDelay);

        // Color Parameters
        addParameter("numColors", this.numColors);
        addParameter("color1", this.color1);
        addParameter("color2", this.color2);
        addParameter("color3", this.color3);
        addParameter("color4", this.color4);
        addParameter("color5", this.color5);

        // Find the maxZ and maxDistance
        for (LXPoint p : model.points) {
            if (p.z > maxZ) {
                maxZ = p.z;
            }
        }
        maxDistance = maxZ;
    }

    @Override
    public void run(double deltaMs) {
        // If a burst is ongoing
        if (burstTime > 0) {
            burstTime -= deltaMs;
            // If the burst is over
            if (burstTime <= 0) {
                delay = burstDelay.getValue(); // Set the delay until the next burst
            } else {
                // Calculate the color of the burst
                // Find the peak of two burst pulses that travel out from the origin
                // The distance of the pulses is determined by the burst time and the max
                // distance
                // double burstPulseDistance = ((totalBurstTime.getValue() - burstTime) /
                // 1000.0) * maxDistance;
                // An exponential pulse distance slow at first then speeding up
                double burstPulseDistance = Math.pow(((totalBurstTime.getValue() - burstTime + deltaMs) / 1000.0), 1.5)
                        * maxDistance;
                double burstPulseOneZ = originZ + burstPulseDistance;
                double burstPulseTwoZ = originZ - burstPulseDistance;
                for (int i = 0; i < model.size; i++) {
                    LXPoint p = model.points[i];
                    double distanceCenter = Math.abs(p.z - originZ);
                    double distanceOne = Math.abs(p.z - burstPulseOneZ);
                    double distanceTwo = Math.abs(p.z - burstPulseTwoZ);
                    double distance = Math.min(distanceOne, distanceTwo);
                    distance = Math.min(distance, distanceCenter / 2.0);
                    // Calculate the fade factor due to distance
                    double distanceFadeFactor = 1.0 - (distance / maxDistance) * (1.0 / burstWidth.getValue());
                    // Calculate the fade factor due to time, completely faded out at the end of the
                    // burst
                    double timeFadeFactor = 1.0 - (burstTime / totalBurstTime.getValue());
                    // Make the time factpr exponential
                    // timeFadeFactor = Math.pow(timeFadeFactor, 2);
                    // Combine the fade factors
                    double fadeFactor = distanceFadeFactor - Math.pow(timeFadeFactor, 2.0);
                    fadeFactor = Math.max(0, fadeFactor); // Prevent negative values
                    if (fadeFactor > 0) {
                        colors[i] = LXColor.scaleBrightness(color, fadeFactor);
                    } else {
                        colors[i] = LXColor.BLACK;
                    }
                }
            }
        } else {
            delay -= deltaMs;
            if (delay <= 0) {
                // Start a new burst
                originZ = random.nextDouble() * maxZ; // Select a random z-coordinate
                color = getRandomColor(); // Get a random color
                burstTime = deltaMs + totalBurstTime.getBaseValue(); // Adjust the duration of the burst (in ms)
                delay = 0; // Reset delay
            }
        }
    }

    // Method to get a random color based on the selected number of colors
    private int getRandomColor() {
        switch (random.nextInt(numColors.getValuei())) {
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
