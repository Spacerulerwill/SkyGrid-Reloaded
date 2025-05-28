package net.spacerulerwill.skygrid_reloaded.ui.screen;

import net.minecraft.ResourceLocationException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.dimension.LevelStem;
import net.spacerulerwill.skygrid_reloaded.ui.widget.WeightSliderListWidgetEntry;
import net.spacerulerwill.skygrid_reloaded.worldgen.SkyGridConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CustomizeBlocksScreen extends DimensionSpecificCustomizableListWidgetScreen<CustomizeBlocksScreen.BlockWeightListEntry, Block> {
    private static final double INITIAL_BLOCK_WEIGHT = 160;
    private static final double MIN_BLOCK_WEIGHT = 0;
    private static final double MAX_BLOCK_WEIGHT = 500;

    public CustomizeBlocksScreen(CustomizeSkyGridScreen parent, List<ResourceKey<LevelStem>> dimensions, ResourceKey<LevelStem> initialDimension, SkyGridConfig currentConfig) {
        super(parent, dimensions, initialDimension, currentConfig, Component.translatable("createWorld.customize.skygrid.blocks"), Component.translatable("createWorld.customize.skygrid.blocks.placeholder"), 25);
    }

    private static Item getBlockItem(Block block) {
        if (block.equals(Blocks.LAVA)) {
            return Items.LAVA_BUCKET;
        } else if (block.equals(Blocks.WATER)) {
            return Items.WATER_BUCKET;
        } else {
            return block.asItem();
        }
    }

    @Override
    public void onClear() {
        Map<Block, Double> blocks = this.getCurrentBlocks();
        blocks.clear();
    }

    @Override
    protected Optional<Block> getThingFromString(String text) {
        try {
            return BuiltInRegistries.BLOCK.getOptional(ResourceLocation.parse(text));
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
        BuiltInRegistries.BLOCK.forEach(block -> {
            String displayString = Component.translatable(block.getDescriptionId()).getString();
            String valueString = BuiltInRegistries.BLOCK.getKey(block).toString();
            if (displayString.trim().toLowerCase().startsWith(text) || valueString.startsWith(text)) {
                results.add(new AutocompleteListWidget.Entry(getBlockItem(block), displayString, valueString, this.font));
            }
        });
        return results;
    }

    @Override
    protected void onAdd(Block block) {
        Map<Block, Double> blocks = this.getCurrentBlocks();
        if (blocks.containsKey(block)) {
            throw new IllegalStateException("Add button called while item to add is already present");
        }
        blocks.put(block, INITIAL_BLOCK_WEIGHT);
        this.listWidget.addEntry(new BlockWeightListEntry(block));
    }

    @Override
    protected boolean canAdd(Block block) {
        return !getCurrentBlocks().containsKey(block);
    }

    @Override
    protected void onDelete(BlockWeightListEntry entry) {
        Map<Block, Double> blocks = getCurrentBlocks();
        blocks.remove(entry.block);
    }

    private Map<Block, Double> getCurrentBlocks() {
        return this.currentConfig.dimensions.get(this.currentDimension).blocks;
    }

    @Override
    protected List<BlockWeightListEntry> getEntriesFromConfig() {
        List<BlockWeightListEntry> entries = new ArrayList<>();
        Map<Block, Double> blocks = this.getCurrentBlocks();
        for (Map.Entry<Block, Double> entry : blocks.entrySet()) {
            Block block = entry.getKey();
            double weight = entry.getValue();
            entries.add(new BlockWeightListEntry(block, weight));
        }
        return entries;
    }

    public class BlockWeightListEntry extends WeightSliderListWidgetEntry<BlockWeightListEntry> {
        private final Block block;

        public BlockWeightListEntry(Block block) {
            super(block.getName(), MIN_BLOCK_WEIGHT, MAX_BLOCK_WEIGHT, INITIAL_BLOCK_WEIGHT);
            this.block = block;
        }

        public BlockWeightListEntry(Block block, double initialWeight) {
            super(block.getName(), MIN_BLOCK_WEIGHT, MAX_BLOCK_WEIGHT, initialWeight);
            this.block = block;
        }

        @Override
        public void applyWeight(double weight) {
            Map<Block, Double> blocks = CustomizeBlocksScreen.this.getCurrentBlocks();
            blocks.put(this.block, weight);
        }

        @Override
        public Item getIcon() {
            return getBlockItem(this.block);
        }
    }
}
