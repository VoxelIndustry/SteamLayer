package net.voxelindustry.steamlayer.test;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class SteamLayerExtension implements BeforeAllCallback, ExtensionContext.Store.CloseableResource
{
    private static boolean started = false;

    @Override
    public void beforeAll(ExtensionContext context)
    {
        if (!started)
        {
            started = true;

            System.out.println("#######################################\nBootstrap Minecraft for test runner...");
            SharedConstants.tryDetectVersion();
            Bootstrap.bootStrap();
            System.out.println("Bootstrap complete!\nVanilla data is now accessible" +
                    ".\n######################################");
        }
    }

    @Override
    public void close()
    {
    }
}