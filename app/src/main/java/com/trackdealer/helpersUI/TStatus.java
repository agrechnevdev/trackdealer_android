package com.trackdealer.helpersUI;

import com.trackdealer.R;

public enum TStatus {

    TRACKLISTENER(0, R.drawable.ic_hearphone, "TRACKLISTENER"),
    TRACKVIP(1, R.drawable.ic_vip, "TRACKVIP"),
    TRACKKING(2, R.drawable.ic_crown_1, "TRACKKING"),
    TRACKKING_2(3, R.drawable.ic_crown_1, "TRACKKING"),
    TRACKDEALER(4, R.drawable.app_logo_bold_small, "TRACKDEALER");

    public static TStatus getStatusByName(String name) {
        for (TStatus tStatus : values()) {
            if (tStatus.getName().equals(name))
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
        return newStatus.getName();
    }

    private int level;
    private int icon;
    private String name;

    TStatus(int level, int icon, String name) {
        this.level = level;
        this.icon = icon;
        this.name = name;

    }

    public int getLevel() {
        return level;
    }

    public int getIcon() {
        return icon;
    }

    public String getName() {
        return name;
    }
}
