package net.acomputerdog.togglepvp;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a Player-vs-player combat setting.  The "ModeList" subclass is needed to work around a quirk with enums.
 */
public enum PVPMode {
    /**
     * PVP is disabled globally
     */
    OFF(false),

    /**
     * PVP is enabled globally
     */
    ON(true),

    /**
     * PVP is enabled, but disabled with events
     */
    MIXED(true),

    /**
     * Don't change PVP setting
     */
    BYPASS(false);

    private final boolean worldState;

    PVPMode(boolean worldState) {
        this.worldState = worldState;
        ModeList.pvpMap.put(this.name(), this);
        ModeList.pvpMap.put(this.name().toUpperCase(), this);
    }

    public boolean getWorldState() {
        return worldState;
    }

    public static PVPMode parse(String name) {
        if (name == null) {
            return null;
        }

        PVPMode mode = ModeList.pvpMap.get(name);
        if (mode == null) {
            mode = ModeList.pvpMap.get(name.toUpperCase());
        }

        return mode;
    }

    private static class ModeList { //this really shouldn't be necessary.
        private static Map<String, PVPMode> pvpMap = new HashMap<>();
    }
}
