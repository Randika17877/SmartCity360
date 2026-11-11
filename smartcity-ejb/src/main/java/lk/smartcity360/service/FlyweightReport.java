package lk.smartcity360.service;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Report containing metrics, memory comparisons, and sample lights from the Flyweight pool. */
public class FlyweightReport implements Serializable {

    public static class SharedConfigItem implements Serializable {
        private final String configuration;
        private final String objectId;
        private final String lightsCountFormatted;
        private final String brightness;

        public SharedConfigItem(String configuration, String objectId, String lightsCountFormatted, String brightness) {
            this.configuration = configuration;
            this.objectId = objectId;
            this.lightsCountFormatted = lightsCountFormatted;
            this.brightness = brightness;
        }

        public String getConfiguration() { return configuration; }
        public String getObjectId() { return objectId; }
        public String getLightsCountFormatted() { return lightsCountFormatted; }
        public String getBrightness() { return brightness; }
    }

    public static class SampleLightItem implements Serializable {
        private final String id;
        private final String configuration;
        private final String objectId;
        private final String location;
        private final String state;
        private final String brightness;

        public SampleLightItem(String id, String configuration, String objectId, String location, String state, String brightness) {
            this.id = id;
            this.configuration = configuration;
            this.objectId = objectId;
            this.location = location;
            this.state = state;
            this.brightness = brightness;
        }

        public String getId() { return id; }
        public String getConfiguration() { return configuration; }
        public String getObjectId() { return objectId; }
        public String getLocation() { return location; }
        public String getState() { return state; }
        public String getBrightness() { return brightness; }
    }

    private final String source;
    private final int totalLights;
    private final int sharedObjectsUsed;
    private final double loadTimeMs;
    private final int configsInPool;
    private final String withoutFlyweightFormatted;
    private final String withoutFlyweightDetails;
    private final String withFlyweightFormatted;
    private final String withFlyweightDetails;
    private final String activeStatusHeader;
    private final List<SharedConfigItem> sharedConfigs;
    private final List<SampleLightItem> sampleLights;
    private final List<String> lines;

    public FlyweightReport(List<String> lines, int lights, int sharedTypes) {
        this("Flyweight pool", lights, sharedTypes, 1.0, sharedTypes,
                "0 KB", "", "0 bytes", "", "", Collections.emptyList(), Collections.emptyList(), lines);
    }

    public FlyweightReport(String source, int totalLights, int sharedObjectsUsed, double loadTimeMs, int configsInPool,
                           String withoutFlyweightFormatted, String withoutFlyweightDetails,
                           String withFlyweightFormatted, String withFlyweightDetails,
                           String activeStatusHeader,
                           List<SharedConfigItem> sharedConfigs,
                           List<SampleLightItem> sampleLights,
                           List<String> lines) {
        this.source = source;
        this.totalLights = totalLights;
        this.sharedObjectsUsed = sharedObjectsUsed;
        this.loadTimeMs = loadTimeMs;
        this.configsInPool = configsInPool;
        this.withoutFlyweightFormatted = withoutFlyweightFormatted;
        this.withoutFlyweightDetails = withoutFlyweightDetails;
        this.withFlyweightFormatted = withFlyweightFormatted;
        this.withFlyweightDetails = withFlyweightDetails;
        this.activeStatusHeader = activeStatusHeader;
        this.sharedConfigs = sharedConfigs != null ? sharedConfigs : new ArrayList<>();
        this.sampleLights = sampleLights != null ? sampleLights : new ArrayList<>();
        this.lines = lines != null ? lines : new ArrayList<>();
    }

    public String getSource() { return source; }
    public int getTotalLights() { return totalLights; }
    public String getTotalLightsFormatted() { return new DecimalFormat("#,###").format(totalLights); }
    public int getSharedObjectsUsed() { return sharedObjectsUsed; }
    public double getLoadTimeMs() { return loadTimeMs; }
    public String getLoadTimeMsFormatted() { return String.format(java.util.Locale.US, "%.1f ms", loadTimeMs); }
    public int getConfigsInPool() { return configsInPool; }

    public String getWithoutFlyweightFormatted() { return withoutFlyweightFormatted; }
    public String getWithoutFlyweightDetails() { return withoutFlyweightDetails; }
    public String getWithFlyweightFormatted() { return withFlyweightFormatted; }
    public String getWithFlyweightDetails() { return withFlyweightDetails; }

    public String getActiveStatusHeader() { return activeStatusHeader; }
    public List<SharedConfigItem> getSharedConfigs() { return sharedConfigs; }
    public List<SampleLightItem> getSampleLights() { return sampleLights; }

    // Backward compatibility
    public List<String> lines() { return lines; }
    public int lights() { return totalLights; }
    public int sharedTypes() { return sharedObjectsUsed; }
    public String summary() {
        return totalLights + " streetlights share " + sharedObjectsUsed + " light type object(s)";
    }
}
