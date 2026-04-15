package com.bgsoftware.wildtools.api.objects;

import java.util.List;

public interface ToolSectionView {
    boolean contains(String path);

    String getString(String path, String def);

    int getInt(String path, int def);

    long getLong(String path, long def);

    double getDouble(String path, double def);

    boolean getBoolean(String path, boolean def);

    List<String> getStringList(String path);
}
