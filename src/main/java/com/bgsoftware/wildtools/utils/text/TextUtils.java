package com.bgsoftware.wildtools.utils.text;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TextUtils {

    private TextUtils() {

    }

    public static String buildStringFromSet(Set<String> set){
        if (set.isEmpty()) {
            return "None";
        }

        List<String> formattedList = new ArrayList<>();

        set.forEach(string -> formattedList.add(getFormattedName(string)));

        return String.join(", ", formattedList);
    }

    public static String getFormattedName(String string){
        StringBuilder name = new StringBuilder();
        String[] split = string.split("_");

        for (int i = 0; i < split.length; i++) {
            name.append(split[i].substring(0, 1).toUpperCase()).append(split[i].substring(1).toLowerCase());
            if (i != split.length - 1) {
                name.append(" ");
            }
        }

        return name.toString();
    }

}
