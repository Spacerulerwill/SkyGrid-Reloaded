package net.spacerulerwill.skygrid_reloaded.ui.screen;

import net.minecraft.ResourceLocationException;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.CheckerboardColumnBiomeSource;
import net.minecraft.world.level.dimension.LevelStem;
import net.spacerulerwill.skygrid_reloaded.util.CheckerboardColumnBiomeSourceSizeAccessor;
import net.spacerulerwill.skygrid_reloaded.worldgen.SkyGridChunkGeneratorConfig;
import net.spacerulerwill.skygrid_reloaded.worldgen.SkyGridConfig;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class SelectBiomesScreen extends DimensionSpecificCustomizableListWidgetScreen<SelectBiomesScreen.BiomeListWidgetEntry, Holder<Biome>> {
    private final Registry<Biome> biomeRegistry;

    public SelectBiomesScreen(CustomizeSkyGridScreen parent, Registry<Biome> biomeRegistry, List<ResourceKey<LevelStem>> dimensions, ResourceKey<LevelStem> initialDimension, SkyGridConfig currentConfig) {
        super(parent, dimensions, initialDimension, currentConfig, Component.translatable("createWorld.customize.skygrid.biomes"), Component.translatable("createWorld.customize.skygrid.spawners.placeholder"), 15);
        this.biomeRegistry = biomeRegistry;
    }

    private SkyGridChunkGeneratorConfig getConfig() {
        return this.currentConfig.dimensions.get(this.currentDimension);
    }

    @Override
    protected void onClear() {
        SkyGridChunkGeneratorConfig currentConfig = this.getConfig();
        CheckerboardColumnBiomeSource currentBiomeSouurce = currentConfig.checkerboardBiomeSource;
        this.getConfig().checkerboardBiomeSource = new CheckerboardColumnBiomeSource(
                HolderSet.direct(),
                ((CheckerboardColumnBiomeSourceSizeAccessor) currentBiomeSouurce).skygrid_reloaded$getSize()
        );
    }

    @Override
    protected Optional<Holder<Biome>> getThingFromString(String text) {
        try {
            return this.biomeRegistry
                    .getOptional(ResourceLocation.parse(text))
                    .map(this.biomeRegistry::wrapAsHolder);
        } catch (ResourceLocationException e) {
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
            String key = this.biomeRegistry.getResourceKey(biome).get().location().toLanguageKey("biome");
            String displayString = Component.translatable(key).getString();
            String valueString = this.biomeRegistry.wrapAsHolder(biome).getRegisteredName();

            if (displayString.trim().toLowerCase().startsWith(text) || valueString.startsWith(text)) {
                results.add(new AutocompleteListWidget.Entry(null, displayString, valueString, this.font));
            }
        });
        return results;
    }

    @Override
    protected void onAdd(Holder<Biome> thing) {
        SkyGridChunkGeneratorConfig config = this.getConfig();
        if (config.checkerboardBiomeSource.possibleBiomes().contains(thing)) {
            throw new IllegalStateException("Add button called while item to add is already present");
        }
        CheckerboardColumnBiomeSource currentBiomeSource = config.checkerboardBiomeSource;
        Set<Holder<Biome>> biomes = new LinkedHashSet<>(currentBiomeSource.possibleBiomes());
        biomes.add(thing);
        config.checkerboardBiomeSource = new CheckerboardColumnBiomeSource(
                HolderSet.direct(biomes.stream().toList()),
                ((CheckerboardColumnBiomeSourceSizeAccessor) currentBiomeSource).skygrid_reloaded$getSize()
        );
        this.listWidget.addEntry(new BiomeListWidgetEntry(thing));
    }

    @Override
    protected boolean canAdd(Holder<Biome> thing) {
        return !this.getConfig().checkerboardBiomeSource.possibleBiomes().contains(thing);
    }


    @Override
    protected void onDelete(BiomeListWidgetEntry entry) {
        SkyGridChunkGeneratorConfig currentConfig = this.getConfig();
        CheckerboardColumnBiomeSource currentBiomeSource = currentConfig.checkerboardBiomeSource;
        Set<Holder<Biome>> biomes = new LinkedHashSet<>(currentBiomeSource.possibleBiomes());
        biomes.remove(entry.biome);
        this.getConfig().checkerboardBiomeSource = new CheckerboardColumnBiomeSource(
                HolderSet.direct(biomes.stream().toList()),
                ((CheckerboardColumnBiomeSourceSizeAccessor) currentBiomeSource).skygrid_reloaded$getSize()
        );
    }

    @Override
    protected List<BiomeListWidgetEntry> getEntriesFromConfig() {
        List<BiomeListWidgetEntry> entries = new ArrayList<>();
        Set<Holder<Biome>> biomes = this.getConfig().checkerboardBiomeSource.possibleBiomes();
        for (Holder<Biome> biome : biomes) {
            entries.add(new BiomeListWidgetEntry(biome));
        }
        return entries;
    }


    protected class BiomeListWidgetEntry extends ObjectSelectionList.Entry<BiomeListWidgetEntry> {
        private final Holder<Biome> biome;

        public BiomeListWidgetEntry(Holder<Biome> biome) {
            this.biome = biome;
        }

        @Override
        public Component getNarration() {
            return Component.empty();
        }

        @Override
        public void render(GuiGraphics context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            String key = this.biome.unwrapKey().get().location().toLanguageKey("biome");
            context.drawString(SelectBiomesScreen.this.font, Component.translatable(key), x + 3, y + 2, 16777215, false);
        }
    }
}
