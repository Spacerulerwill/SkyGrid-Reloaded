package net.spacerulerwill.skygrid_reloaded.util;

import net.minecraft.world.level.dimension.LevelStem;

import java.util.Optional;

public interface WorldPresetExtension {
    default Optional<LevelStem> skygrid_reloaded$GetNether() {
        throw new AssertionError();
    }

    default Optional<LevelStem> skygrid_reloaded$GetEnd() {
        throw new AssertionError();
    }

}
