package net.spacerulerwill.skygrid_reloaded;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.spacerulerwill.skygrid_reloaded.worldgen.SkyGridPreset;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class PresetsReloadListener implements SimpleSynchronousResourceReloadListener {
    public static final ResourceLocation FABRIC_ID = ResourceLocation.fromNamespaceAndPath(SkyGridReloaded.MOD_ID, "presets");
    private final HolderLookup.Provider wrapperLookup;

    public PresetsReloadListener(HolderLookup.Provider wrapperLookup) {
        this.wrapperLookup = wrapperLookup;
    }

    @Override
    public ResourceLocation getFabricId() {
        return FABRIC_ID;
    }

    @Override
    public void onResourceManagerReload(ResourceManager manager) {
        // Load all presets
        SkyGridReloaded.LOGGER.debug("Loading presets");
        SkyGridReloaded.PRESETS.clear();
        SkyGridReloaded.DEFAULT_PRESET = null;
        DynamicOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, this.wrapperLookup);
        for (Map.Entry<ResourceLocation, Resource> entry : manager.listResources("presets", path -> path.toString().endsWith(".json")).entrySet()) {
            ResourceLocation identifier = entry.getKey();
            Resource resource = entry.getValue();
            try (InputStream stream = resource.open()) {
                SkyGridReloaded.LOGGER.debug("Loading preset {}", identifier);
                JsonElement json = JsonParser.parseString(new String(stream.readAllBytes(), StandardCharsets.UTF_8));
                SkyGridPreset preset = SkyGridPreset.CODEC.parse(ops, json).getOrThrow();
                SkyGridReloaded.PRESETS.add(preset);
                if (identifier.equals(ResourceLocation.fromNamespaceAndPath(SkyGridReloaded.MOD_ID, "presets/modern.json"))) {
                    SkyGridReloaded.DEFAULT_PRESET = preset;
                }
                SkyGridReloaded.LOGGER.info("Loaded preset {}", identifier);
            } catch (Exception e) {
                SkyGridReloaded.LOGGER.error("Error occurred while loading preset json: {}", identifier, e);
            }
        }
        SkyGridReloaded.LOGGER.info("Loaded presets");
    }
}
