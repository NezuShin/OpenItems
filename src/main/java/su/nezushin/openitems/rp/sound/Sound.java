package su.nezushin.openitems.rp.sound;

import java.util.List;

/**
 * Used for resource pack json serialization
 */
public class Sound {
    private String name;
    private double volume;
    private double pith;
    private double weight;
    private boolean stream;
    private double attenuation_distance;
    private boolean preload;
    private String type;

    public Sound() {
    }

    public Sound(String name, double volume, double pith, double weight, boolean stream, double attenuation_distance, boolean preload, String type) {
        this.name = name;
        this.volume = volume;
        this.pith = pith;
        this.weight = weight;
        this.stream = stream;
        this.attenuation_distance = attenuation_distance;
        this.preload = preload;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public double getVolume() {
        return volume;
    }

    public double getPith() {
        return pith;
    }

    public double getWeight() {
        return weight;
    }

    public boolean isStream() {
        return stream;
    }

    public boolean isPreload() {
        return preload;
    }

    public double getAttenuation_distance() {
        return attenuation_distance;
    }

    public String getType() {
        return type;
    }
}
