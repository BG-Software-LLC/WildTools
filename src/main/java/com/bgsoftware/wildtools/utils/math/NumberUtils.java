package com.bgsoftware.wildtools.utils.math;

import java.text.DecimalFormat;

public class NumberUtils {

    private static final DecimalFormat numberFormatter = new DecimalFormat("###,###,###,###,###,###,###,###,###,##0.00");

    public static String format(double d) {
        String s = numberFormatter.format(d);
        return s.endsWith(".00") ? s.replace(".00", "") : s;
    }

    public static boolean range(int num, int min, int max) {
        return num >= min && num <= max;
    }

}
