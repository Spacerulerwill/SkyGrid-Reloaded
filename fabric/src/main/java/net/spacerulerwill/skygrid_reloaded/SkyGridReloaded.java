package net.spacerulerwill.skygrid_reloaded;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.spacerulerwill.skygrid_reloaded.worldgen.SkyGridChunkGenerator;

public class SkyGridReloaded implements ModInitializer {
    @Override
    public void onInitialize() {
        Registry.register(BuiltInRegistries.CHUNK_GENERATOR, ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "skygrid"), SkyGridChunkGenerator.MAP_CODEC);
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(Constants.PRESETS_RESOURCE_LOCATION, PresetsReloadListener::new);
        Constants.LOGGER.info("SkyGrid mod is initialised!");
    }
}
