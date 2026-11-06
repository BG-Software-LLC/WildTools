package com.bgsoftware.wildtools.utils.text;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class TextUtils {

    private TextUtils() {

    }

    public static String buildStringFromSet(Set<String> set){
        if (set.isEmpty()) {
            return "None";
        }

        List<String> formattedList = new LinkedList<>();

        set.forEach(string -> formattedList.add(getFormattedName(string)));

        return String.join(", ", formattedList);
    }

    public static String getFormattedName(String string){
        if (string == null || string.isEmpty()) {
            return "";
        }

        StringBuilder stringBuilder = new StringBuilder();
        String[] split = string.split("_");

        for (String part : split) {
            if (part.isEmpty()) {
                continue;
            }

            if (stringBuilder.length() > 0) {
                stringBuilder.append(" ");
            }

            stringBuilder.append(part.substring(0, 1).toUpperCase()).append(part.substring(1).toLowerCase());
        }

        return stringBuilder.toString();
    }

}
