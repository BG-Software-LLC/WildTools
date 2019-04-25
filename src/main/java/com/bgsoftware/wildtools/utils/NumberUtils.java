package com.bgsoftware.wildtools.utils;

public final class NumberUtils {

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

}
