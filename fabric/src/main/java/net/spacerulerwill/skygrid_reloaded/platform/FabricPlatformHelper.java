package net.spacerulerwill.skygrid_reloaded.platform;

import net.spacerulerwill.skygrid_reloaded.platform.services.IPlatformHelper;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class FabricPlatformHelper implements IPlatformHelper {
    @Override
    public Path getConfigPath() {
        return FabricLoader.getInstance().getConfigDir();
    }
}
