package net.kunmc.lab.internalkpm.upgrader;

import net.kunmc.lab.internalkpm.upgrader.migrator.KPMMigrator;
import net.kunmc.lab.kpm.installer.impls.install.InstallArgument;
import net.kunmc.lab.kpm.installer.impls.install.InstallTasks;
import net.kunmc.lab.kpm.installer.impls.install.PluginInstaller;
import net.kunmc.lab.kpm.installer.impls.uninstall.PluginUninstaller;
import net.kunmc.lab.kpm.installer.impls.uninstall.UnInstallTasks;
import net.kunmc.lab.kpm.installer.impls.uninstall.UninstallArgument;
import net.kunmc.lab.kpm.interfaces.KPMRegistry;
import net.kunmc.lab.kpm.interfaces.installer.InstallResult;
import net.kunmc.lab.kpm.interfaces.resolver.result.ErrorResult;
import net.kunmc.lab.kpm.interfaces.resolver.result.ResolveResult;
import net.kunmc.lab.kpm.interfaces.resolver.result.SuccessResult;
import net.kunmc.lab.kpm.signal.SignalHandleManager;
import net.kunmc.lab.kpm.versioning.Version;
import net.kunmc.lab.peyangpaperutils.lib.utils.Runner;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Logger;

public class UpgradeImpl
{
    private static final String KPM_OWNER = "TeamKUN";
    private static final String KPM_NAME = "TeamKunPluginManager";

    private final KPMUpgrader plugin;
    private final Logger logger;
    private final Plugin currentKPM;
    private final Version currentKPMVersion;

    private KPMRegistry registry;

    public UpgradeImpl(KPMUpgrader plugin)
    {
        this.plugin = plugin;
        this.logger = plugin.getLogger();

        if ((this.currentKPM = plugin.getServer().getPluginManager().getPlugin(KPM_NAME)) == null)
        {
            this.logger.severe("KPM がこのサーバーにインストールされていません。");
            this.logger.info("KPMUpgrader は、 KPM による自動インストールでのみ使用できます。");
            this.destructSelf();
            this.currentKPMVersion = null;
            this.registry = null;
            return;
        }

        if ((this.currentKPMVersion = Version.ofNullable(this.currentKPM.getDescription().getVersion())) == null)
        {
            this.logger.severe("KPM のバージョンの取得に失敗しました。");
            this.destructSelf();
        }
    }

    public void initDaemon()
    {
        Path kpmDataFolder = this.currentKPM.getDataFolder().toPath();
        this.registry = new KPMDaemonMock(
                new KPMEnvironmentMock(
                        this.currentKPM,
                        kpmDataFolder.resolve("token.dat"),
                        kpmDataFolder.resolve("token_key.dat")
                )
        );
    }

    private void destructSelf()
    {
        this.logger.info("お使いの KPM は、自動アッグレートに対応していないません。手動で KPM をアッグレートしてください。");
        Runner.run(() -> this.plugin.getServer().dispatchCommand(
                this.plugin.getServer().getConsoleSender(),
                "kpm upgrade-kpm destruct"
        ));
    }

    private SuccessResult resolveKPM(String version)
    {
        this.logger.info("最新の KPM を解決しています。");

        String query = "$>https://github.com/" + KPM_OWNER + "/" + KPM_NAME + "/releases/tag/" + version;

        ResolveResult resolveResult = this.registry.getPluginResolver().resolve(query);

        if (resolveResult instanceof SuccessResult)
        {
            this.logger.info("KPM を解決しました：" + ((SuccessResult) resolveResult).getVersion());
            return (SuccessResult) resolveResult;
        }

        assert resolveResult instanceof ErrorResult;
        ErrorResult errorResult = (ErrorResult) resolveResult;

        this.logger.severe("KPM の取得に失敗しました：" + errorResult.getMessage());
        this.destructSelf();
        return null;
    }

    private boolean removeCurrentKPM()
    {
        this.logger.info("現在の KPM を削除しています。");

        SignalHandleManager signalHandleManager = new SignalHandleManager();

        try
        {
            InstallResult<UnInstallTasks> uninstallResult = new PluginUninstaller(this.registry, signalHandleManager)
                    .run(UninstallArgument.builder(this.currentKPM)
                            .autoConfirm(true)
                            .forceUninstall(true)
                            .build()
                    );

            if (uninstallResult.isSuccess())
            {
                this.logger.info("KPM の削除に成功しました。");
                return true;
            }

            this.logger.warning("KPM の削除は " + uninstallResult.getProgress().getCurrentTask() + " で失敗しました。");
            return false;
        }
        catch (IOException e)
        {
            this.logger.severe("アンインストーラの初期化に失敗しました。");
            e.printStackTrace();
            return false;
        }
    }

    private boolean installNewKPM(SuccessResult resolveResult)
    {
        this.logger.info("新しい KPM をインストールしています。");

        SignalHandleManager signalHandleManager = new SignalHandleManager();

        try
        {
            InstallResult<InstallTasks> installResult = new PluginInstaller(this.registry, signalHandleManager)
                    .run(InstallArgument.builder(resolveResult)
                            .onyLocate(true)
                            .build()
                    );

            if (installResult.isSuccess())
            {
                this.logger.info("KPM のインストールに成功しました。");
                return true;
            }

            this.logger.warning("KPM のインストールは " + installResult.getProgress().getCurrentTask() + " で失敗しました。");

            return false;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public void runUpgrade(String version)
    {
        this.logger.info("KPM をアッグレートしています ...");

        SuccessResult result = this.resolveKPM(version);
        if (result == null)
            return;

        if (!this.removeCurrentKPM())
            return;

        this.logger.info("データを移行しています ...");

        assert result.getVersion() != null;
        Version toVersion = Version.ofNullable(result.getVersion());

        assert toVersion != null;
        KPMMigrator.doMigrate(this.registry, this.currentKPM.getDataFolder().toPath(), this.currentKPMVersion, toVersion);

        if (!this.installNewKPM(result))
            return;

        this.logger.info("サーバをリロードしています ...");
        this.plugin.getServer().reload();

        this.logger.info("KPM のアッグレートが完了しました。");
    }
}