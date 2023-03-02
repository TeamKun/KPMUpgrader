package org.kunlab.inetrnalkpm.upgrader.mocks;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.kpm.meta.InstallOperator;
import org.kunlab.kpm.meta.interfaces.PluginMetaManager;
import org.kunlab.kpm.meta.interfaces.PluginMetaProvider;

public class PluginMetaManagerMock implements PluginMetaManager
{
    @Override
    public void preparePluginModify(@NotNull String pluginName)
    {

    }

    @Override
    public void preparePluginModify(@NotNull Plugin plugin)
    {

    }

    @Override
    public void onInstalled(@NotNull Plugin plugin, @NotNull InstallOperator operator, @Nullable String resolveQuery, long installedAt, boolean isDependency)
    {

    }

    @Override
    public void onInstalled(@NotNull Plugin plugin, @NotNull InstallOperator operator, @Nullable String resolveQuery, boolean isDependency)
    {

    }

    @Override
    public void onUninstalled(@NotNull String pluginName)
    {

    }

    @Override
    public boolean hasPluginMeta(@NotNull String pluginName)
    {
        return false;
    }

    @Override
    public boolean hasPluginMeta(@NotNull Plugin plugin)
    {
        return false;
    }

    @Override
    public void crawlAll()
    {

    }

    @Override
    public PluginMetaProvider getProvider()
    {
        return new PluginMetaProviderMock();
    }
}
