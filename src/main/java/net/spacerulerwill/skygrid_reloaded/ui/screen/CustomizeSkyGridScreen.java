package net.spacerulerwill.skygrid_reloaded.ui.screen;

import com.google.common.collect.ImmutableMap;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.CheckerboardColumnBiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.spacerulerwill.skygrid_reloaded.SkyGridReloaded;
import net.spacerulerwill.skygrid_reloaded.ui.widget.ClickableWidgetList;
import net.spacerulerwill.skygrid_reloaded.ui.widget.IntSlider;
import net.spacerulerwill.skygrid_reloaded.util.WorldPresetExtension;
import net.spacerulerwill.skygrid_reloaded.worldgen.SkyGridChunkGenerator;
import net.spacerulerwill.skygrid_reloaded.worldgen.SkyGridChunkGeneratorConfig;
import net.spacerulerwill.skygrid_reloaded.worldgen.SkyGridConfig;
import net.spacerulerwill.skygrid_reloaded.worldgen.SkyGridPreset;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomizeSkyGridScreen extends Screen {
    public static final List<ResourceKey<LevelStem>> DIMENSIONS = List.of(LevelStem.OVERWORLD, LevelStem.NETHER, LevelStem.END);

    private static final Component TITLE_TEXT = Component.translatable("createWorld.customize.skygrid.title");
    private static final Component BLOCKS_TEXT = Component.translatable("createWorld.customize.skygrid.blocks");
    private static final Component BiOMES_TEXT = Component.translatable("createWorld.customize.skygrid.biomes");
    private static final Component SPAWNERS_TEXT = Component.translatable("createWorld.customize.skygrid.spawners");
    private static final Component LOOT_TEXT = Component.translatable("createWorld.customize.skygrid.loot");
    private static final Component PRESETS_TEXT = Component.translatable("createWorld.customize.skygrid.presets");


    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    private final CreateWorldScreen parent;
    private final IntSlider biomeScaleSlider;
    private ClickableWidgetList body;
    private CycleButton<ResourceKey<LevelStem>> dimensionsSelector;
    private SkyGridConfig currentConfig = new SkyGridConfig(SkyGridReloaded.DEFAULT_PRESET.config());
    private ResourceKey<LevelStem> currentDimension = LevelStem.OVERWORLD;
    private boolean initialised = false;


    public CustomizeSkyGridScreen(CreateWorldScreen parent) {
        super(TITLE_TEXT);
        this.parent = parent;
        this.biomeScaleSlider = new IntSlider(0, 0, 100, 20, Component.translatable("createWorld.customize.skygrid.biome_scale"), 0, 62, this.getCurrentConfig().checkerboardBiomeSource.size, newValue -> {
            SkyGridChunkGeneratorConfig currentConfig = this.getCurrentConfig();
            currentConfig.checkerboardBiomeSource = new CheckerboardColumnBiomeSource(
                    HolderSet.direct(currentConfig.checkerboardBiomeSource.possibleBiomes().stream().toList()),
                    newValue
            );
        });
    }

    private SkyGridChunkGeneratorConfig getCurrentConfig() {
        SkyGridChunkGeneratorConfig config;
        if (this.currentDimension == LevelStem.OVERWORLD) {
            config = this.currentConfig.overworldConfig();
        } else if (this.currentDimension == LevelStem.NETHER) {
            config = this.currentConfig.netherConfig();
        } else if (this.currentDimension == LevelStem.END) {
            config = this.currentConfig.endConfig();
        } else {
            throw new IllegalStateException("Current dimension is not one of overworld, nether or end: " + this.currentDimension.location().toLanguageKey());
        }
        return config;
    }

    public void updateBiomeScaleSlider() {
        this.biomeScaleSlider.setValue(this.getCurrentConfig().checkerboardBiomeSource.size);
    }

    @Override
    protected void init() {
        // Header for title
        if (!this.initialised) {
            this.layout.addTitleHeader(TITLE_TEXT, this.font);
            // Body
            List<AbstractWidget> firstRow = List.of(new CycleButton.Builder<ResourceKey<LevelStem>>(value -> Component.translatable(value.location().toLanguageKey()))
                    .withValues(DIMENSIONS)
                    .create(0, 0, 158, 20, Component.translatable("createWorld.customize.skygrid.dimension"), ((button, dimension) -> {
                        this.currentDimension = dimension;
                        this.updateBiomeScaleSlider();
                    }))
            );
            List<AbstractWidget> secondRow = List.of(
                    Button.builder(BLOCKS_TEXT, (button) -> {
                        if (this.minecraft != null) {
                            this.minecraft.setScreen(new CustomizeBlocksScreen(this, this.currentDimension, this.currentConfig));
                        }
                    }).build(),
                    Button.builder(SPAWNERS_TEXT, (button) -> {
                        if (this.minecraft != null) {
                            this.minecraft.setScreen(new CustomizeSpawnerScreen(this, this.currentDimension, this.currentConfig));
                        }
                    }).build()
            );
            List<AbstractWidget> thirdRow = List.of(
                    Button.builder(BiOMES_TEXT, (button) -> {
                        if (this.minecraft != null) {
                            Registry<Biome> biomeRegistry = this.parent.getUiState().getSettings().worldgenLoadContext().registryOrThrow(Registries.BIOME);
                            this.minecraft.setScreen(new SelectBiomesScreen(this, biomeRegistry, this.currentDimension, this.currentConfig));
                        }
                    }).build(),
                    this.biomeScaleSlider
            );
            List<AbstractWidget> fourthRow = List.of(
                    Button.builder(LOOT_TEXT, (button) -> {
                        if (this.minecraft != null) {
                            this.minecraft.setScreen(new CustomizeLootScreen(this, this.currentDimension, this.currentConfig));
                        }
                    }).build(),
                    Button.builder(PRESETS_TEXT, (button) -> {
                        if (this.minecraft != null) {
                            this.minecraft.setScreen(new SkyGridPresetsScreen(this, this.parent.getUiState().getSettings().worldgenLoadContext()));
                        }
                    }).build()
            );
            List<List<AbstractWidget>> rows = List.of(firstRow, secondRow, thirdRow, fourthRow);
            this.body = this.layout.addToContents(new ClickableWidgetList(this.minecraft, rows, this.width, this.layout.getContentHeight(), this.layout.getHeaderHeight()));
            // Footer
            LinearLayout footerRow = LinearLayout.horizontal().spacing(8);
            footerRow.addChild(Button.builder(CommonComponents.GUI_DONE, (button) -> {
                this.done();
                this.onClose();
            }).build());
            footerRow.addChild(Button.builder(CommonComponents.GUI_CANCEL, (button) -> {
                this.onClose();
            }).build());
            this.layout.addToFooter(footerRow);
            this.initialised = true;
        }
        this.layout.visitWidgets(this::addRenderableWidget);
        this.refreshWidgetPositions();
    }

    protected void refreshWidgetPositions() {
        this.layout.arrangeElements();
        if (this.body != null) {
            this.body.updateSize(this.width, this.layout);
        }
    }

    public void updateSkyGridConfig(SkyGridConfig config) {
        this.currentConfig = config;
    }

    public SkyGridConfig getCurrentSkyGridConfig() {
        return this.currentConfig;
    }

    private void done() {
        this.parent.getUiState().updateDimensions(applyChunkCGeneratorConfigs());
    }

    public void setConfigFromPreset(SkyGridPreset preset) {
        this.currentConfig = new SkyGridConfig(preset.config());
    }

    private WorldCreationContext.DimensionsUpdater applyChunkCGeneratorConfigs() {
        Map<ResourceKey<LevelStem>, SkyGridChunkGeneratorConfig> dimensionOptionsToChunkGeneratorConfigMap = Map.of(
                LevelStem.OVERWORLD, currentConfig.overworldConfig(),
                LevelStem.NETHER, currentConfig.netherConfig(),
                LevelStem.END, currentConfig.endConfig()
        );

        return (dynamicRegistryManager, dimensionsRegistryHolder) -> {
            Registry<NoiseGeneratorSettings> chunkGeneratorSettingsRegistry = dynamicRegistryManager.registryOrThrow(Registries.NOISE_SETTINGS);
            Map<ResourceKey<LevelStem>, LevelStem> updatedDimensions = new HashMap<>(dimensionsRegistryHolder.dimensions());
            dimensionOptionsToChunkGeneratorConfigMap.forEach((dimensionOptionsRegistryKey, config) -> {
                boolean hasNonZeroBlock = config.blocks.values().stream().anyMatch(weight -> weight > 0);
                if (hasNonZeroBlock) {
                    // Chunk generator settings
                    ResourceKey<NoiseGeneratorSettings> chunkGeneratorSettingsRegistryKey = null;
                    if (dimensionOptionsRegistryKey == LevelStem.OVERWORLD) {
                        chunkGeneratorSettingsRegistryKey = NoiseGeneratorSettings.OVERWORLD;
                    } else if (dimensionOptionsRegistryKey == LevelStem.NETHER) {
                        chunkGeneratorSettingsRegistryKey = NoiseGeneratorSettings.NETHER;
                    } else if (dimensionOptionsRegistryKey == LevelStem.END) {
                        chunkGeneratorSettingsRegistryKey = NoiseGeneratorSettings.END;
                    }

                    // Biome source
                    if (config.checkerboardBiomeSource.possibleBiomes().isEmpty()) {
                        // Select single biome
                        Registry<Biome> biomeRegistry = dynamicRegistryManager.registryOrThrow(Registries.BIOME);
                        ResourceKey<Biome> biomeKey = null;
                        if (dimensionOptionsRegistryKey == LevelStem.OVERWORLD) {
                            biomeKey = Biomes.PLAINS;
                        } else if (dimensionOptionsRegistryKey == LevelStem.NETHER) {
                            biomeKey = Biomes.NETHER_WASTES;
                        } else if (dimensionOptionsRegistryKey == LevelStem.END) {
                            biomeKey = Biomes.THE_END;
                        }
                        Holder<Biome> biomeRegistryEntry = biomeRegistry.wrapAsHolder(biomeRegistry.get(biomeKey));
                        config.checkerboardBiomeSource = new CheckerboardColumnBiomeSource(HolderSet.direct(biomeRegistryEntry), 1);
                    }
                    Holder<NoiseGeneratorSettings> chunkGeneratorSettingsRegistryEntry = chunkGeneratorSettingsRegistry.wrapAsHolder(chunkGeneratorSettingsRegistry.get(chunkGeneratorSettingsRegistryKey));
                    ChunkGenerator chunkGenerator = new SkyGridChunkGenerator(chunkGeneratorSettingsRegistryEntry, config);
                    LevelStem dimensionOptions = parent.getUiState().getSettings().selectedDimensions().dimensions().get(dimensionOptionsRegistryKey);

                    Holder<DimensionType> dimensionTypeRegistryEntry = dimensionOptions.type();
                    LevelStem newDimensionOptions = new LevelStem(dimensionTypeRegistryEntry, chunkGenerator);
                    updatedDimensions.put(dimensionOptionsRegistryKey, newDimensionOptions);
                } else {
                    /*
                    There is no non-zero weighted block, so we must use default generation for this dimension. If it's
                    a vanilla dimension we must get it from a registry due to the fact that the default is overwritten by
                    our world preset json file. However for modded dimensions, we can leave it as they will have not
                    been overwritten by our world preset json
                     */
                    if (dimensionOptionsRegistryKey == LevelStem.OVERWORLD) {
                        LevelStem defaultOverworld = (dynamicRegistryManager.registryOrThrow(Registries.WORLD_PRESET).get(WorldPresets.NORMAL)).overworld().orElseThrow();
                        updatedDimensions.put(dimensionOptionsRegistryKey, defaultOverworld);
                    } else if (dimensionOptionsRegistryKey == LevelStem.NETHER) {
                        WorldPreset preset = (dynamicRegistryManager.registryOrThrow(Registries.WORLD_PRESET).get(WorldPresets.NORMAL));
                        LevelStem defaultNether = ((WorldPresetExtension) preset).skygrid$GetNether().orElseThrow();
                        updatedDimensions.put(dimensionOptionsRegistryKey, defaultNether);
                    } else if (dimensionOptionsRegistryKey == LevelStem.END) {
                        WorldPreset preset = (dynamicRegistryManager.registryOrThrow(Registries.WORLD_PRESET).get(WorldPresets.NORMAL));
                        LevelStem defaultEnd = ((WorldPresetExtension) preset).skygrid$GetEnd().orElseThrow();
                        updatedDimensions.put(dimensionOptionsRegistryKey, defaultEnd);
                    }

                }
            });
            return new WorldDimensions(ImmutableMap.copyOf(updatedDimensions));
        };
    }

    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }
}
