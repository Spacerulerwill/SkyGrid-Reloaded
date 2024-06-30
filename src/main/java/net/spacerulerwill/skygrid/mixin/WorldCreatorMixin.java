package net.spacerulerwill.skygrid.mixin;

import net.minecraft.client.gui.screen.world.LevelScreenProvider;
import net.minecraft.client.gui.screen.world.WorldCreator;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.WorldPreset;
import net.spacerulerwill.skygrid.CustomizeSkyGridScreen;
import net.spacerulerwill.skygrid.SkyGrid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldCreator.class)
public class WorldCreatorMixin {
    @Inject(method = "getLevelScreenProvider", at = @At("HEAD"), cancellable = true)
    private void injected(CallbackInfoReturnable<LevelScreenProvider> cir) {
        RegistryEntry<WorldPreset> registryEntry = ((WorldCreator)(Object)this).getWorldType().preset();
        if (registryEntry != null && registryEntry.matchesId(Identifier.of(SkyGrid.MOD_ID, "skygrid"))) {
            cir.setReturnValue((parent, generatorOptionsHolder) ->
                    new CustomizeSkyGridScreen(parent));
            cir.cancel();
        }
    }
}