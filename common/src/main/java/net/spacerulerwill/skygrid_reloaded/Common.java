package net.spacerulerwill.skygrid_reloaded;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.spacerulerwill.skygrid_reloaded.platform.Services;
import net.spacerulerwill.skygrid_reloaded.worldgen.SkyGridPreset;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;

public class Common {
    public static ArrayList<SkyGridPreset> PRESETS = new ArrayList<>();
    public static ArrayList<SkyGridPreset> CUSTOM_PRESETS = new ArrayList<>();
    public static SkyGridPreset DEFAULT_PRESET;


    private static SkyGridPreset loadCustomPreset(Path filepath, HolderLookup.Provider wrapperLookup) throws IOException {
        String fileContent = Files.readString(filepath);
        JsonElement json = JsonParser.parseString(fileContent);
        DynamicOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, wrapperLookup);
        return SkyGridPreset.CODEC.parse(ops, json).getOrThrow();
    }

    public static void reloadCustomPresets(HolderLookup.Provider wrapperLookup) {
        Common.CUSTOM_PRESETS.clear();
        Constants.LOGGER.debug("Loading custom presets");
        Path configDir = Services.PLATFORM.getConfigPath();
        Path modConfigDir = configDir.resolve(Constants.MOD_ID);

        // Create our config directory
        try {
            Files.createDirectories(modConfigDir);
        } catch (IOException e) {
            Constants.LOGGER.error("Failed to create the mod's config directory: {}", modConfigDir, e);
            return;
        }

        // List the files in the mod's directory
        File[] directoryListing = modConfigDir.toFile().listFiles();
        if (directoryListing == null) {
            return;
        }
        Arrays.sort(directoryListing, Comparator.comparing(File::getName));

        // Process each custom preset
        for (File child : directoryListing) {
            if (!child.isFile() || !child.getPath().endsWith(".json")) {
                continue;
            }
            Constants.LOGGER.debug("Loading custom preset: {}", child.toPath());
            try {
                Common.CUSTOM_PRESETS.add(loadCustomPreset(child.toPath(), wrapperLookup));
            } catch (Exception e) {
                Constants.LOGGER.error("Error loading while loading preset {}: {}", child.toPath(), e);
            }
            Constants.LOGGER.info("Loaded custom preset: {}", child.toPath());
        }
        Constants.LOGGER.info("Loaded custom presets");
    }

    public static void onResourceManagerReload(HolderLookup.Provider wrapperLookup, ResourceManager manager) {
        // Load all presets
        Constants.LOGGER.debug("Loading presets");
        Common.PRESETS.clear();
        Common.DEFAULT_PRESET = null;
        DynamicOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, wrapperLookup);
        for (Map.Entry<ResourceLocation, Resource> entry : manager.listResources("presets", path -> path.toString().endsWith(".json")).entrySet()) {
            ResourceLocation identifier = entry.getKey();
            Resource resource = entry.getValue();
            try (InputStream stream = resource.open()) {
                Constants.LOGGER.debug("Loading preset {}", identifier);
                JsonElement json = JsonParser.parseString(new String(stream.readAllBytes(), StandardCharsets.UTF_8));
                SkyGridPreset preset = SkyGridPreset.CODEC.parse(ops, json).getOrThrow();
                Common.PRESETS.add(preset);
                if (identifier.equals(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "presets/modern.json"))) {
                    Common.DEFAULT_PRESET = preset;
                }
                Constants.LOGGER.info("Loaded preset {}", identifier);
            } catch (Exception e) {
                Constants.LOGGER.error("Error occurred while loading preset json: {}", identifier, e);
            }
        }
        Constants.LOGGER.info("Loaded presets");
    }
}
