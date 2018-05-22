package com.trackdealer.helpersUI;

import com.trackdealer.R;

public enum TStatus {

    TRACKLISTENER(0, R.drawable.ic_svg_crystal_0),
    TRACKMASTER(1, R.drawable.ic_svg_crystal_2),
    TRACKKING(2, R.drawable.ic_svg_crystal_3),
    TRACKDEALER(3, R.drawable.app_logo_bold);

    public static TStatus getStatusByName(String name) {
        for (TStatus tStatus : values()) {
            if (tStatus.name().equals(name))
                return tStatus;
        }
        return TRACKLISTENER;
    }

    public static TStatus getStatusByLevel(int level) {
        for (TStatus tStatus : values()) {
            if (tStatus.getLevel() == level)
                return tStatus;
        }
        return TRACKDEALER;
    }

    public static String getUpgradedStatus(String oldStatus) {
        TStatus tStatus = getStatusByName(oldStatus);
        int level = tStatus.getLevel() + 1;
        TStatus newStatus = getStatusByLevel(level);
        return newStatus.name();
    }

    private int level;
    private int icon;

    TStatus(int level, int icon) {
        this.level = level;
        this.icon = icon;
    }

    public int getLevel() {
        return level;
    }

    public int getIcon() {
        return icon;
    }
}
