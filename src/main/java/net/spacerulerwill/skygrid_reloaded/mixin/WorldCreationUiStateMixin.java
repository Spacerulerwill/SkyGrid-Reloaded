package net.spacerulerwill.skygrid_reloaded.mixin;

import net.minecraft.client.gui.screens.worldselection.PresetEditor;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.spacerulerwill.skygrid_reloaded.SkyGridReloaded;
import net.spacerulerwill.skygrid_reloaded.ui.screen.CustomizeSkyGridScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldCreationUiState.class)
public class WorldCreationUiStateMixin {
    /*
    This mixin allows the customize button for our skygrid world preset in the world creation menu to open
    our CustomizeSkyGridScreen
     */
    @Inject(method = "getPresetEditor", at = @At("HEAD"), cancellable = true)
    private void injected(CallbackInfoReturnable<PresetEditor> cir) {
        Holder<WorldPreset> registryEntry = ((WorldCreationUiState) (Object) this).getWorldType().preset();
        if (registryEntry != null && registryEntry.is(ResourceLocation.fromNamespaceAndPath(SkyGridReloaded.MOD_ID, "skygrid"))) {
            cir.setReturnValue((parent, generatorOptionsHolder) ->
                    new CustomizeSkyGridScreen(parent));
            cir.cancel();
        }
    }
}