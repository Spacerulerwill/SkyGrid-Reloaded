package net.spacerulerwill.skygrid_reloaded.platform;

import net.minecraftforge.fml.loading.FMLConfig;
import net.minecraftforge.fml.loading.FMLPaths;
import net.spacerulerwill.skygrid_reloaded.platform.services.IPlatformHelper;

import java.nio.file.Path;

public class ForgePlatformHelper implements IPlatformHelper {
    @Override
    public Path getConfigPath() {
        return FMLPaths.GAMEDIR.get().resolve(FMLConfig.defaultConfigPath());
    }
}