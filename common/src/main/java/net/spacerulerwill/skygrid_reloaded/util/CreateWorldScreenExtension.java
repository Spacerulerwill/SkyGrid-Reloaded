package net.spacerulerwill.skygrid_reloaded.util;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.LevelStem;

import java.util.Map;

public interface CreateWorldScreenExtension {
    Map<ResourceKey<LevelStem>, LevelStem> skygrid_reloaded$getDefaultLevelStems();
}
