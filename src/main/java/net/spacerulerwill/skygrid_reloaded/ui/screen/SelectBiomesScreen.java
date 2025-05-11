package net.spacerulerwill.skygrid_reloaded.ui.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.CheckerboardBiomeSource;
import net.minecraft.world.dimension.DimensionOptions;
import net.spacerulerwill.skygrid_reloaded.worldgen.SkyGridChunkGeneratorConfig;
import net.spacerulerwill.skygrid_reloaded.worldgen.SkyGridConfig;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Environment(EnvType.CLIENT)
public class SelectBiomesScreen extends DimensionSpecificCustomizableListWidgetScreen<SelectBiomesScreen.BiomeListWidgetEntry, RegistryEntry<Biome>> {
    private final Registry<Biome> biomeRegistry;

    public SelectBiomesScreen(CustomizeSkyGridScreen parent, Registry<Biome> biomeRegistry, RegistryKey<DimensionOptions> initialDimension, SkyGridConfig currentConfig) {
        super(parent, initialDimension, currentConfig, Text.translatable("createWorld.customize.skygrid.spawners"), Text.translatable("createWorld.customize.skygrid.spawners.placeholder"), 15);
        this.biomeRegistry = biomeRegistry;
    }

    private SkyGridChunkGeneratorConfig getConfig() {
        SkyGridChunkGeneratorConfig config;
        if (this.currentDimension == DimensionOptions.OVERWORLD) {
            config = this.currentConfig.overworldConfig();
        } else if (this.currentDimension == DimensionOptions.NETHER) {
            config = this.currentConfig.netherConfig();
        } else if (this.currentDimension == DimensionOptions.END) {
            config = this.currentConfig.endConfig();
        } else {
            throw new IllegalStateException("Current dimension is not one of overworld, nether or end: " + this.currentDimension.getValue().toTranslationKey());
        }
        return config;
    }

    @Override
    protected void onClear() {
        SkyGridChunkGeneratorConfig currentConfig = this.getConfig();
        CheckerboardBiomeSource currentBiomeSouurce = currentConfig.checkerboardBiomeSource;
        this.getConfig().checkerboardBiomeSource = new CheckerboardBiomeSource(
                RegistryEntryList.of(),
                currentBiomeSouurce.scale
        );
    }

    @Override
    protected Optional<RegistryEntry<Biome>> getThingFromString(String text) {
        try {
            return this.biomeRegistry
                    .getOptionalValue(Identifier.of(text))
                    .map(this.biomeRegistry::getEntry);
        } catch (InvalidIdentifierException e) {
            return Optional.empty();
        }
    }


    @Override
    protected List<AutocompleteListWidget.Entry> getAutocompleteSuggestions(String text) {
        List<AutocompleteListWidget.Entry> results = new ArrayList<>();
        if (text.isBlank()) {
            return results;
        }
        this.biomeRegistry.forEach(biome -> {
            String key = this.biomeRegistry.getKey(biome).get().getValue().toTranslationKey("biome");
            String displayString = Text.translatable(key).getString();
            String valueString = this.biomeRegistry.getEntry(biome).getIdAsString();

            if (displayString.trim().toLowerCase().startsWith(text) || valueString.startsWith(text)) {
                results.add(new AutocompleteListWidget.Entry(null, displayString, valueString, this.textRenderer));
            }
        });
        return results;
    }

    @Override
    protected void onAdd(RegistryEntry<Biome> thing) {
        SkyGridChunkGeneratorConfig config = this.getConfig();
        if (config.checkerboardBiomeSource.getBiomes().contains(thing)) {
            throw new IllegalStateException("Add button called while item to add is already present");
        }
        CheckerboardBiomeSource currentBiomeSource = config.checkerboardBiomeSource;
        Set<RegistryEntry<Biome>> biomes = new LinkedHashSet<>(currentBiomeSource.getBiomes());
        biomes.add(thing);
        config.checkerboardBiomeSource = new CheckerboardBiomeSource(
                RegistryEntryList.of(biomes.stream().toList()),
                currentBiomeSource.scale
        );
        this.listWidget.addEntry(new SelectBiomesScreen.BiomeListWidgetEntry(thing));
    }

    @Override
    protected boolean canAdd(RegistryEntry<Biome> thing) {
        return !this.getConfig().checkerboardBiomeSource.getBiomes().contains(thing);
    }


    @Override
    protected void onDelete(BiomeListWidgetEntry entry) {
        SkyGridChunkGeneratorConfig currentConfig = this.getConfig();
        CheckerboardBiomeSource currentBiomeSource = currentConfig.checkerboardBiomeSource;
        Set<RegistryEntry<Biome>> biomes = new LinkedHashSet<>(currentBiomeSource.getBiomes());
        biomes.remove(entry.biome);
        this.getConfig().checkerboardBiomeSource = new CheckerboardBiomeSource(
                RegistryEntryList.of(biomes.stream().toList()),
                currentBiomeSource.scale
        );
    }

    @Override
    protected List<SelectBiomesScreen.BiomeListWidgetEntry> getEntriesFromConfig() {
        List<SelectBiomesScreen.BiomeListWidgetEntry> entries = new ArrayList<>();
        Set<RegistryEntry<Biome>> biomes = this.getConfig().checkerboardBiomeSource.getBiomes();
        for (RegistryEntry<Biome> biome : biomes) {
            entries.add(new BiomeListWidgetEntry(biome));
        }
        return entries;
    }


    @Environment(EnvType.CLIENT)
    protected class BiomeListWidgetEntry extends AlwaysSelectedEntryListWidget.Entry<BiomeListWidgetEntry> {
        private final RegistryEntry<Biome> biome;

        public BiomeListWidgetEntry(RegistryEntry<Biome> biome) {
            this.biome = biome;
        }

        @Override
        public Text getNarration() {
            return Text.empty();
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            String key = this.biome.getKey().get().getValue().toTranslationKey("biome");
            context.drawText(SelectBiomesScreen.this.textRenderer, Text.translatable(key), x + 3, y + 2, 16777215, false);
        }
    }
}
