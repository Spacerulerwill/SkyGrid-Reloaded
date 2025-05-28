package net.spacerulerwill.skygrid_reloaded.mixin;

import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.LevelStem;
import net.spacerulerwill.skygrid_reloaded.util.CreateWorldScreenExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Mixin(CreateWorldScreen.class)
public abstract class CreateWorldScreenMixin implements CreateWorldScreenExtension {
    @Unique
    public Map<ResourceKey<LevelStem>, LevelStem> skygrid_reloaded$defaultLevelStems = new HashMap<>();

    @Shadow
    public abstract WorldCreationUiState getUiState();

    @Override
    public Map<ResourceKey<LevelStem>, LevelStem> skygrid_reloaded$getDefaultLevelStems() {
        return this.skygrid_reloaded$defaultLevelStems;
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void injected(CallbackInfo ci) {
        WorldCreationContext worldCreationContext = this.getUiState().getSettings();

        this.skygrid_reloaded$defaultLevelStems.putAll(worldCreationContext.selectedDimensions().dimensions());
        worldCreationContext.datapackDimensions().forEach((levelStem) -> {
            ResourceKey<LevelStem> levelStemResourceKey = worldCreationContext.datapackDimensions().getResourceKey(levelStem).get();
            skygrid_reloaded$defaultLevelStems.put(levelStemResourceKey, levelStem);
        });
    }
}
