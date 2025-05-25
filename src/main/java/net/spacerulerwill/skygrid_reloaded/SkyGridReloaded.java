package net.spacerulerwill.skygrid_reloaded;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.spacerulerwill.skygrid_reloaded.worldgen.SkyGridChunkGenerator;
import net.spacerulerwill.skygrid_reloaded.worldgen.SkyGridPreset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class SkyGridReloaded implements ModInitializer {
    public static final String MOD_ID = "skygrid_reloaded";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
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
        CUSTOM_PRESETS.clear();
        LOGGER.debug("Loading custom presets");
        Path configDir = FabricLoader.getInstance().getConfigDir();
        Path modConfigDir = configDir.resolve(MOD_ID);

        // Create our config directory
        try {
            Files.createDirectories(modConfigDir);
        } catch (IOException e) {
            LOGGER.error("Failed to create the mod's config directory: {}", modConfigDir, e);
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
            LOGGER.debug("Loading custom preset: {}", child.toPath());
            try {
                SkyGridReloaded.CUSTOM_PRESETS.add(loadCustomPreset(child.toPath(), wrapperLookup));
            } catch (Exception e) {
                LOGGER.error("Error loading while loading preset {}: {}", child.toPath(), e);
            }
            LOGGER.info("Loaded custom preset: {}", child.toPath());
        }
        LOGGER.info("Loaded custom presets");
    }

    @Override
    public void onInitialize() {
        Registry.register(BuiltInRegistries.CHUNK_GENERATOR, ResourceLocation.fromNamespaceAndPath(MOD_ID, "skygrid"), SkyGridChunkGenerator.MAP_CODEC);
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(PresetsReloadListener.FABRIC_ID, PresetsReloadListener::new);
        LOGGER.info("SkyGrid mod is initialised!");
    }
}
