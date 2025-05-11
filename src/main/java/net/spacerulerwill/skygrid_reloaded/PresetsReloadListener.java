package net.spacerulerwill.skygrid_reloaded;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.spacerulerwill.skygrid_reloaded.worldgen.SkyGridPreset;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class PresetsReloadListener implements SimpleSynchronousResourceReloadListener {
    public static final Identifier FABRIC_ID = Identifier.of(SkyGridReloaded.MOD_ID, "presets");
    private final RegistryWrapper.WrapperLookup wrapperLookup;

    public PresetsReloadListener(RegistryWrapper.WrapperLookup wrapperLookup) {
        this.wrapperLookup = wrapperLookup;
    }

    @Override
    public Identifier getFabricId() {
        return FABRIC_ID;
    }

    @Override
    public void reload(ResourceManager manager) {
        // Load all presets
        SkyGridReloaded.LOGGER.debug("Loading presets");
        SkyGridReloaded.PRESETS.clear();
        SkyGridReloaded.DEFAULT_PRESET = null;
        DynamicOps<JsonElement> ops = RegistryOps.of(JsonOps.INSTANCE, this.wrapperLookup);
        for (Map.Entry<Identifier, Resource> entry : manager.findResources("presets", path -> path.toString().endsWith(".json")).entrySet()) {
            Identifier identifier = entry.getKey();
            Resource resource = entry.getValue();
            try (InputStream stream = resource.getInputStream()) {
                SkyGridReloaded.LOGGER.debug("Loading preset {}", identifier);
                JsonElement json = JsonParser.parseString(new String(stream.readAllBytes(), StandardCharsets.UTF_8));
                SkyGridPreset preset = SkyGridPreset.CODEC.parse(ops, json).getOrThrow();
                SkyGridReloaded.PRESETS.add(preset);
                if (identifier.equals(Identifier.of(SkyGridReloaded.MOD_ID, "presets/modern.json"))) {
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
