package com.bgsoftware.wildtools.utils;

import java.text.DecimalFormat;

public final class NumberUtils {

    private static DecimalFormat numberFormatter = new DecimalFormat("###,###,###,###,###,###,###,###,###,##0.00");

    public static boolean isDigits(String str){
        if (str.isEmpty()) {
            return false;
        } else {
            int sz = str.length();

            for(int i = 0; i < sz; ++i) {
                if (!Character.isDigit(str.charAt(i))) {
                    return false;
                }
            }

            return true;
        }
    }

    public static String format(double d){
        String s = numberFormatter.format(d);
        return s.endsWith(".00") ? s.replace(".00", "") : s;
    }

}
