package org.kunlab.inetrnalkpm.upgrader.migrator.migrators;

import org.kunlab.inetrnalkpm.upgrader.migrator.KPMMigrateAction;
import net.kunmc.lab.kpm.interfaces.KPMRegistry;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;

public class TokenMigrator implements KPMMigrateAction
{
    @Override
    public void migrate(@NotNull KPMRegistry daemon, @NotNull Path kpmDataFolder)
    {
        daemon.getLogger().info("Migrating token...");

        Path tokenPath = kpmDataFolder.toAbsolutePath().getParent().getParent().resolve("kpm.vault");

        if (!Files.exists(tokenPath))
        {
            daemon.getLogger().info("OOPS! Token file not found.");
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(tokenPath))
        {
            String token = reader.readLine();
            daemon.getTokenStore().storeToken(token, false);

            daemon.getLogger().info("Token migration completed.");

            daemon.getLogger().info("Deleting old token file...");
            Files.delete(tokenPath);
        }
        catch (Exception e)
        {
            daemon.getLogger().warning("An error occurred while migrating token: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public String getNeedMigrateVersionRange()
    {
        return "...v2.9.9";
    }
}
