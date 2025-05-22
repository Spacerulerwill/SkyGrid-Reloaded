package net.spacerulerwill.skygrid_reloaded.mixin;

import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.spacerulerwill.skygrid_reloaded.util.WorldPresetExtension;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;
import java.util.Optional;

/*
This mixin provides two methods for getting the default nether and end generation options.
There was already one implemented for the overworld, but I needed these 2 also.
 */
@Mixin(WorldPreset.class)
public class WorldPresetMixin implements WorldPresetExtension {
    @Final
    @Shadow
    private Map<LevelStem, LevelStem> dimensions;

    @Override
    public Optional<LevelStem> skygrid$GetNether() {
        return Optional.ofNullable(dimensions.get(LevelStem.NETHER));
    }

    @Override
    public Optional<LevelStem> skygrid$GetEnd() {
        return Optional.ofNullable(dimensions.get(LevelStem.END));
    }
}
