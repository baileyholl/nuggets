package com.hollingsworth.nuggets.common.inventory;


import java.util.Comparator;

public class SortPref {
    public static final SortPref INVALID = new SortPref(-1);
    public static final SortPref LOW = new SortPref(0);
    public static final SortPref HIGH = new SortPref(1);
    public static final SortPref HIGHEST = new SortPref(2);
    public static final SortPref[] VALUES = new SortPref[]{INVALID, LOW, HIGH, HIGHEST};

    public static Comparator<SortPref> comparator = Comparator.comparingInt(SortPref::ordinal);

    protected int ordinal;

    public SortPref(int ordinal) {
        this.ordinal = ordinal;
    }

    public int ordinal() {
        return ordinal;
    }

}
