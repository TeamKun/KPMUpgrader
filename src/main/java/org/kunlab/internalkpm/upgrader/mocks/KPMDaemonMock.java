package org.kunlab.internalkpm.upgrader.mocks;

import lombok.Getter;
import org.kunlab.internalkpm.upgrader.LegacySupport;
import org.kunlab.kpm.ExceptionHandler;
import org.kunlab.kpm.TokenStore;
import org.kunlab.kpm.alias.interfaces.AliasProvider;
import org.kunlab.kpm.hook.interfaces.HookExecutor;
import org.kunlab.kpm.http.Requests;
import org.kunlab.kpm.installer.interfaces.InstallManager;
import org.kunlab.kpm.installer.interfaces.loader.PluginLoader;
import org.kunlab.kpm.interfaces.KPMEnvironment;
import org.kunlab.kpm.interfaces.KPMRegistry;
import org.kunlab.kpm.kpminfo.interfaces.KPMInfoManager;
import org.kunlab.kpm.meta.interfaces.PluginMetaManager;
import org.kunlab.kpm.resolver.PluginResolverImpl;
import org.kunlab.kpm.resolver.impl.github.GitHubURLResolver;
import org.kunlab.kpm.resolver.interfaces.PluginResolver;
import org.kunlab.kpm.task.loader.PluginLoaderImpl;
import org.kunlab.kpm.utils.ServerConditionChecker;
import org.kunlab.kpm.versioning.Version;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.logging.Logger;

@Getter
public class KPMDaemonMock implements KPMRegistry
{
    private final KPMEnvironment environment;
    private final Logger logger;
    private final PluginResolver pluginResolver;
    private final TokenStore tokenStore;
    private final PluginLoader loader;
    private final ExceptionHandler exceptionHandler;

    public KPMDaemonMock(@NotNull KPMEnvironment env)
    {
        this.environment = env;

        this.logger = env.getLogger();
        this.pluginResolver = new PluginResolverImpl();
        this.exceptionHandler = new ExceptionHandlerMock();
        this.tokenStore = new TokenStore(env.getTokenPath(), env.getTokenKeyPath(), this.exceptionHandler);
        this.loader = new PluginLoaderImpl(this);
        this.init();
    }

    private void init()
    {
        this.getPluginResolver().addResolver(new GitHubURLResolver(), "$");

        Requests.getExtraHeaders().put(
                "User-Agent",
                "TeamKUNPluginManager+Upgrader/" + this.getVersion()
        );
        Requests.setTokenStore(this.getTokenStore());

        try
        {
            if (!this.getTokenStore().loadToken())
            {
                String tokenEnv = System.getenv("TOKEN");

                if (tokenEnv == null || tokenEnv.isEmpty())
                {
                    String oldToken;
                    if ((oldToken = LegacySupport.retrieveLegacyToken()) != null)
                        this.getTokenStore().storeToken(oldToken, false);
                    else
                        throw new IllegalStateException("Token is not set.");
                }

                this.getTokenStore().fromEnv();
            }
        }
        catch (IOException e)
        {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Version getVersion()
    {
        return Version.of("0.0.0");
    }

    @Override
    public KPMEnvironment getEnvironment()
    {
        return this.environment;
    }

    @Override
    public AliasProvider getAliasProvider()
    {
        return null;
    }

    @Override
    public PluginMetaManager getPluginMetaManager()
    {
        return new PluginMetaManagerMock();
    }

    @Override
    public KPMInfoManager getKpmInfoManager()
    {
        return new KPMInfoManagerMock();
    }

    @Override
    public HookExecutor getHookExecutor()
    {
        return new HookExecutorMock(this);
    }

    @Override
    public InstallManager getInstallManager()
    {
        return null;
    }

    @Override
    public PluginLoader getPluginLoader()
    {
        return this.loader;
    }

    @Override
    public ServerConditionChecker getServerConditionChecker()
    {
        return null;
    }

    @Override
    public org.kunlab.kpm.ExceptionHandler getExceptionHandler()
    {
        return this.exceptionHandler;
    }

    @Override
    public void shutdown()
    {

    }
}
