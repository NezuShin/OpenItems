package su.nezushin.openitems.rp.sound;

import java.util.List;

public class SoundEvent {

    private boolean replace;
    private String subtitle;

    private List<Sound> sounds;

    public SoundEvent(boolean replace, String subtitle, List<Sound> sounds) {
        this.replace = replace;
        this.subtitle = subtitle;
        this.sounds = sounds;
    }

    public SoundEvent() {
    }

    public boolean isReplace() {
        return replace;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public List<Sound> getSounds() {
        return sounds;
    }
}
