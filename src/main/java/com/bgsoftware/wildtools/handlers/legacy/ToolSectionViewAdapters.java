package com.bgsoftware.wildtools.handlers.legacy;

import com.bgsoftware.wildtools.api.objects.ToolSectionView;

import java.util.Collections;
import java.util.List;

public final class ToolSectionViewAdapters {

    private ToolSectionViewAdapters() {
    }

    public static ToolSectionView fromLegacyArg(final Object arg) {
        return new ToolSectionView() {

            @Override
            public boolean contains(String path) {
                if (arg == null)
                    return false;
                if (path == null)
                    return false;

                return path.equals("length") || path.equals("tnt-amount") ||
                        path.equals("break-level") || path.equals("radius") ||
                        path.equals("craftings") || path.equals("commands-on-use");
            }

            @Override
            public String getString(String path, String def) {
                return def;
            }

            @Override
            public int getInt(String path, int def) {
                if (arg instanceof Number)
                    return ((Number) arg).intValue();
                return def;
            }

            @Override
            public long getLong(String path, long def) {
                return def;
            }

            @Override
            public double getDouble(String path, double def) {
                return def;
            }

            @Override
            public boolean getBoolean(String path, boolean def) {
                return def;
            }

            @Override
            @SuppressWarnings("unchecked")
            public List<String> getStringList(String path) {
                if (arg instanceof List)
                    return (List<String>) arg;
                return Collections.emptyList();
            }
        };
    }
}
