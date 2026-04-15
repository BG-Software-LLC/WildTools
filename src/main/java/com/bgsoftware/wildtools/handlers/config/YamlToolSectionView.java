package com.bgsoftware.wildtools.handlers.config;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.wildtools.api.objects.ToolSectionView;

import java.util.List;

public final class YamlToolSectionView implements ToolSectionView {
    private final CommentedConfiguration root;
    private final String base;

    public YamlToolSectionView(CommentedConfiguration root, String basePath) {
        this.root = root;
        this.base = basePath.endsWith(".") ? basePath : basePath + ".";
    }

    @Override
    public boolean contains(String path) {
        return root.contains(base + path);
    }

    @Override
    public String getString(String path, String def) {
        String v = root.getString(base + path);
        return v == null ? def : v;
    }

    @Override
    public int getInt(String path, int def) {
        return root.getInt(base + path, def);
    }

    @Override
    public long getLong(String path, long def) {
        return root.getLong(base + path, def);
    }

    @Override
    public double getDouble(String path, double def) {
        return root.getDouble(base + path, def);
    }

    @Override
    public boolean getBoolean(String path, boolean def) {
        return root.getBoolean(base + path, def);
    }

    @Override
    public List<String> getStringList(String path) {
        return root.getStringList(base + path);
    }
}
