package net.spacerulerwill.skygrid_reloaded.mixin;

import net.minecraft.world.level.biome.CheckerboardColumnBiomeSource;
import net.spacerulerwill.skygrid_reloaded.util.CheckerboardColumnBiomeSourceSizeAccessor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(CheckerboardColumnBiomeSource.class)
public class CheckerboardColumnBiomeSourceMixin implements CheckerboardColumnBiomeSourceSizeAccessor {
    @Final
    @Shadow
    private int size;

    @Override
    public int skygrid_reloaded$getSize() {
        return this.size;
    }
}
