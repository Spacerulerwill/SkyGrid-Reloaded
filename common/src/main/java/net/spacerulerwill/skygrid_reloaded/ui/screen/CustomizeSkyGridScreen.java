package net.spacerulerwill.skygrid_reloaded.ui.screen;

import com.mojang.serialization.Lifecycle;
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
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.CheckerboardColumnBiomeSource;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.spacerulerwill.skygrid_reloaded.ui.widget.ClickableWidgetList;
import net.spacerulerwill.skygrid_reloaded.ui.widget.IntSlider;
import net.spacerulerwill.skygrid_reloaded.util.CheckerboardColumnBiomeSourceSizeAccessor;
import net.spacerulerwill.skygrid_reloaded.util.CreateWorldScreenExtension;
import net.spacerulerwill.skygrid_reloaded.worldgen.SkyGridChunkGenerator;
import net.spacerulerwill.skygrid_reloaded.worldgen.SkyGridChunkGeneratorConfig;
import net.spacerulerwill.skygrid_reloaded.worldgen.SkyGridConfig;
import net.spacerulerwill.skygrid_reloaded.worldgen.SkyGridPreset;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static net.spacerulerwill.skygrid_reloaded.Common.DEFAULT_PRESET;

public class CustomizeSkyGridScreen extends Screen {
    public static final List<ResourceKey<LevelStem>> VANILLA_DIMENSIONS = List.of(LevelStem.OVERWORLD, LevelStem.NETHER, LevelStem.END);

    private static final Component TITLE_TEXT = Component.translatable("createWorld.customize.skygrid.title");
    private static final Component BLOCKS_TEXT = Component.translatable("createWorld.customize.skygrid.blocks");
    private static final Component BiOMES_TEXT = Component.translatable("createWorld.customize.skygrid.biomes");
    private static final Component SPAWNERS_TEXT = Component.translatable("createWorld.customize.skygrid.spawners");
    private static final Component LOOT_TEXT = Component.translatable("createWorld.customize.skygrid.loot");
    private static final Component PRESETS_TEXT = Component.translatable("createWorld.customize.skygrid.presets");


    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    private final CreateWorldScreen parent;
    private final IntSlider biomeScaleSlider;
    private final List<ResourceKey<LevelStem>> dimensions;
    private ClickableWidgetList body;
    private CycleButton<ResourceKey<LevelStem>> dimensionsSelector;
    private SkyGridConfig currentConfig = new SkyGridConfig(DEFAULT_PRESET.config);
    private ResourceKey<LevelStem> currentDimension = LevelStem.OVERWORLD;

    public CustomizeSkyGridScreen(CreateWorldScreen parent) {
        super(TITLE_TEXT);
        this.parent = parent;
        this.biomeScaleSlider = new IntSlider(0, 0, 100, 20, Component.translatable("createWorld.customize.skygrid.biome_scale"), 0, 62, ((CheckerboardColumnBiomeSourceSizeAccessor) this.getCurrentConfig().checkerboardBiomeSource).skygrid_reloaded$getSize(), newValue -> {
            SkyGridChunkGeneratorConfig currentConfig = this.getCurrentConfig();
            currentConfig.checkerboardBiomeSource = new CheckerboardColumnBiomeSource(
                    HolderSet.direct(currentConfig.checkerboardBiomeSource.possibleBiomes().stream().toList()),
                    newValue
            );
        });

        // all our dimensions
        this.dimensions = this.getAllDimensions();
        // add empty configs for any dimensions not specified
        this.ensureAllDimensionsHaveConfigs();
    }

    private SkyGridChunkGeneratorConfig getCurrentConfig() {
        return this.currentConfig.dimensions.get(this.currentDimension);
    }

    public void updateBiomeScaleSlider() {
        this.biomeScaleSlider.setValue(((CheckerboardColumnBiomeSourceSizeAccessor) this.getCurrentConfig().checkerboardBiomeSource).skygrid_reloaded$getSize());
    }

    private List<ResourceKey<LevelStem>> getAllDimensions() {
        List<ResourceKey<LevelStem>> result = new ArrayList<>();
        WorldCreationContext worldCreationContext = parent.getUiState().getSettings();

        // Add selected dimensions first, unsorted
        worldCreationContext.selectedDimensions().dimensions().forEach((levelStemResourceKey, levelStem) -> {
            result.add(levelStemResourceKey);
        });

        // Get datapack dimensions, sort them alphabetically, then add
        List<ResourceKey<LevelStem>> datapackDims = new ArrayList<>(worldCreationContext.datapackDimensions().registryKeySet());
        datapackDims.sort(Comparator.comparing(key -> key.toString()));

        result.addAll(datapackDims);

        return result;
    }


    @Override
    protected void init() {
        // Header for title
        this.layout.addTitleHeader(TITLE_TEXT, this.font);
        // Body
        List<AbstractWidget> firstRow = List.of(new CycleButton.Builder<ResourceKey<LevelStem>>(value -> Component.translatable(value.location().toLanguageKey()))
                .withValues(this.dimensions)
                .create(0, 0, 158, 20, Component.translatable("createWorld.customize.skygrid.dimension"), ((button, dimension) -> {
                    this.currentDimension = dimension;
                    this.updateBiomeScaleSlider();
                }))
        );
        List<AbstractWidget> secondRow = List.of(
                Button.builder(BLOCKS_TEXT, (button) -> {
                    if (this.minecraft != null) {
                        this.minecraft.setScreen(new CustomizeBlocksScreen(this, this.dimensions, this.currentDimension, this.currentConfig));
                    }
                }).build(),
                Button.builder(SPAWNERS_TEXT, (button) -> {
                    if (this.minecraft != null) {
                        this.minecraft.setScreen(new CustomizeSpawnerScreen(this, this.dimensions, this.currentDimension, this.currentConfig));
                    }
                }).build()
        );
        List<AbstractWidget> thirdRow = List.of(
                Button.builder(BiOMES_TEXT, (button) -> {
                    if (this.minecraft != null) {
                        Registry<Biome> biomeRegistry = this.parent.getUiState().getSettings().worldgenLoadContext().lookupOrThrow(Registries.BIOME);
                        this.minecraft.setScreen(new SelectBiomesScreen(this, biomeRegistry, this.dimensions, this.currentDimension, this.currentConfig));
                    }
                }).build(),
                this.biomeScaleSlider
        );
        List<AbstractWidget> fourthRow = List.of(
                Button.builder(LOOT_TEXT, (button) -> {
                    if (this.minecraft != null) {
                        this.minecraft.setScreen(new CustomizeLootScreen(this, this.dimensions, this.currentDimension, this.currentConfig));
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
        this.layout.visitWidgets(this::addRenderableWidget);
        this.repositionElements();
    }

    protected void repositionElements() {
        this.layout.arrangeElements();
        if (this.body != null) {
            this.body.updateSize(this.width, this.layout);
        }
    }

    private void ensureAllDimensionsHaveConfigs() {
        this.dimensions.forEach((levelStemResourceKey) -> {
            if (!this.currentConfig.dimensions.containsKey(levelStemResourceKey)) {
                this.currentConfig.dimensions.put(levelStemResourceKey, new SkyGridChunkGeneratorConfig());
            }
        });
    }

    public void updateSkyGridConfig(SkyGridConfig config) {
        this.currentConfig = config;
        this.ensureAllDimensionsHaveConfigs();
    }

    public SkyGridConfig getCurrentSkyGridConfig() {
        return this.currentConfig;
    }

    private void done() {
        this.updateDatapackDimensions();
    }

    private void updateDatapackDimensions() {
        WorldCreationContext settings = this.parent.getUiState().getSettings();
        Registry<LevelStem> oldDatapackDimensions = settings.datapackDimensions();
        MappedRegistry<LevelStem> newDatapackDimensions = new MappedRegistry<>(Registries.LEVEL_STEM, Lifecycle.stable());
        RegistryAccess.Frozen dynamicRegistryManager = settings.worldgenLoadContext();
        Registry<Biome> biomeRegistry = dynamicRegistryManager.lookupOrThrow(Registries.BIOME);
        this.currentConfig.dimensions.forEach((levelStemResourceKey, config) -> {
            boolean hasNonZeroBlock = config.blocks.values().stream().anyMatch(weight -> weight > 0);
            if (hasNonZeroBlock) {
                LevelStem levelStem;
                if (VANILLA_DIMENSIONS.contains(levelStemResourceKey)) {
                    levelStem = this.parent.getUiState().getSettings().selectedDimensions().dimensions().get(levelStemResourceKey);
                } else {
                    levelStem = oldDatapackDimensions.getValue(levelStemResourceKey);
                }
                if (config.checkerboardBiomeSource.possibleBiomes().isEmpty()) {
                    List<Holder<Biome>> biomes = levelStem.generator().getBiomeSource().possibleBiomes().stream().toList();
                    config.checkerboardBiomeSource = new CheckerboardColumnBiomeSource(HolderSet.direct(biomes), 1);
                }
                DimensionType type = levelStem.type().value();
                SkyGridChunkGenerator chunkGenerator = new SkyGridChunkGenerator(type.minY(), type.height(), config);
                LevelStem newLevelStem = new LevelStem(levelStem.type(), chunkGenerator);
                newDatapackDimensions.register(levelStemResourceKey, newLevelStem, RegistrationInfo.BUILT_IN);
            } else {
                newDatapackDimensions.register(levelStemResourceKey, ((CreateWorldScreenExtension) this.parent).skygrid_reloaded$getDefaultLevelStems().get(levelStemResourceKey), RegistrationInfo.BUILT_IN);
            }
        });

        settings = parent.getUiState().getSettings();
        WorldCreationContext worldCreationContext = new WorldCreationContext(
                settings.options(),
                newDatapackDimensions,
                settings.selectedDimensions(),
                settings.worldgenRegistries(),
                settings.dataPackResources(),
                settings.dataConfiguration(),
                settings.initialWorldCreationOptions()
        );
        parent.getUiState().setSettings(worldCreationContext);
    }

    public void setConfigFromPreset(SkyGridPreset preset) {
        this.currentConfig = new SkyGridConfig(preset.config);
        ensureAllDimensionsHaveConfigs();
    }

    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }
}
