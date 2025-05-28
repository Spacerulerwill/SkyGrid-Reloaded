package net.spacerulerwill.skygrid_reloaded.ui.screen;

import net.minecraft.ResourceLocationException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.dimension.LevelStem;
import net.spacerulerwill.skygrid_reloaded.ui.widget.WeightSliderListWidgetEntry;
import net.spacerulerwill.skygrid_reloaded.worldgen.SkyGridConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CustomizeLootScreen extends DimensionSpecificCustomizableListWidgetScreen<CustomizeLootScreen.ItemWeightListEntry, Item> {
    private static final double INITIAL_ITEM_WEIGHT = 50;
    private static final double MIN_ITEM_WEIGHT = 0;
    private static final double MAX_ITEM_WEIGHT = 100;

    public CustomizeLootScreen(CustomizeSkyGridScreen parent, List<ResourceKey<LevelStem>> dimensions, ResourceKey<LevelStem> initialDimension, SkyGridConfig currentConfig) {
        super(parent, dimensions, initialDimension, currentConfig, Component.translatable("createWorld.customize.skygrid.loot"), Component.translatable("createWorld.customize.loot.placeholder"), 25);
    }

    @Override
    public void onClear() {
        Map<Item, Double> blocks = this.getChestItems();
        blocks.clear();
    }

    @Override
    protected Optional<Item> getThingFromString(String text) {
        try {
            return BuiltInRegistries.ITEM.getOptional(ResourceLocation.parse(text));
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
        BuiltInRegistries.ITEM.forEach(item -> {
            if (item == Items.AIR) return;

            String displayString = Component.translatable(item.getDescriptionId()).getString();
            String valueString = BuiltInRegistries.ITEM.getKey(item).toString();
            if (displayString.trim().toLowerCase().startsWith(text) || valueString.startsWith(text)) {
                results.add(new AutocompleteListWidget.Entry(item, displayString, valueString, this.font));
            }
        });
        return results;
    }

    @Override
    protected void onAdd(Item item) {
        Map<Item, Double> chestItems = this.getChestItems();
        if (chestItems.containsKey(item)) {
            throw new IllegalStateException("Add button called while item to add is already present");
        }
        chestItems.put(item, INITIAL_ITEM_WEIGHT);

        this.listWidget.addEntry(new ItemWeightListEntry(item));
    }

    @Override
    protected boolean canAdd(Item item) {
        return !getChestItems().containsKey(item);
    }

    @Override
    protected void onDelete(ItemWeightListEntry entry) {
        Map<Item, Double> chestItems = getChestItems();
        chestItems.remove(entry.item);
    }

    private Map<Item, Double> getChestItems() {
        return this.currentConfig.dimensions.get(this.currentDimension).chestItems;
    }

    @Override
    protected List<ItemWeightListEntry> getEntriesFromConfig() {
        List<ItemWeightListEntry> entries = new ArrayList<>();
        Map<Item, Double> chestItems = this.getChestItems();
        for (Map.Entry<Item, Double> entry : chestItems.entrySet()) {
            Item item = entry.getKey();
            double weight = entry.getValue();
            entries.add(new ItemWeightListEntry(item, weight));
        }
        return entries;
    }

    public class ItemWeightListEntry extends WeightSliderListWidgetEntry<ItemWeightListEntry> {
        private final Item item;

        public ItemWeightListEntry(Item item) {
            super(item.getDescription(), MIN_ITEM_WEIGHT, MAX_ITEM_WEIGHT, INITIAL_ITEM_WEIGHT);
            this.item = item;
        }

        public ItemWeightListEntry(Item item, double initialWeight) {
            super(item.getDescription(), MIN_ITEM_WEIGHT, MAX_ITEM_WEIGHT, initialWeight);
            this.item = item;
        }

        @Override
        public void applyWeight(double weight) {
            Map<Item, Double> items = CustomizeLootScreen.this.getChestItems();
            items.put(this.item, weight);
        }

        @Override
        public Item getIcon() {
            return this.item;
        }
    }
}
