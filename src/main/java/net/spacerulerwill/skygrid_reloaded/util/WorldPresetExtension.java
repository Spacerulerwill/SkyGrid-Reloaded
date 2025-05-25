package net.spacerulerwill.skygrid_reloaded.util;

import java.util.Optional;
import net.minecraft.world.level.dimension.LevelStem;

public interface WorldPresetExtension {
    Optional<LevelStem> skygrid$GetNether();

    Optional<LevelStem> skygrid$GetEnd();
}
