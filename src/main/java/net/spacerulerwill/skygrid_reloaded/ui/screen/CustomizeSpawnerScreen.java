package net.spacerulerwill.skygrid_reloaded.ui.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ResourceLocationException;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.dimension.LevelStem;
import net.spacerulerwill.skygrid_reloaded.worldgen.SkyGridConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Environment(EnvType.CLIENT)
public class CustomizeSpawnerScreen extends DimensionSpecificCustomizableListWidgetScreen<CustomizeSpawnerScreen.EntityListWidgetEntry, EntityType<?>> {
    public CustomizeSpawnerScreen(CustomizeSkyGridScreen parent, ResourceKey<LevelStem> initialDimension, SkyGridConfig currentConfig) {
        super(parent, initialDimension, currentConfig, Component.translatable("createWorld.customize.skygrid.spawners"), Component.translatable("createWorld.customize.skygrid.spawners.placeholder"), 15);
    }

    private Set<EntityType<?>> getSpawnerEntities() {
        Set<EntityType<?>> entities;
        if (this.currentDimension == LevelStem.OVERWORLD) {
            entities = this.currentConfig.overworldConfig().spawnerEntities;
        } else if (this.currentDimension == LevelStem.NETHER) {
            entities = this.currentConfig.netherConfig().spawnerEntities;
        } else if (this.currentDimension == LevelStem.END) {
            entities = this.currentConfig.endConfig().spawnerEntities;
        } else {
            throw new IllegalStateException("Current dimension is not one of overworld, nether or end: " + this.currentDimension.location().toLanguageKey());
        }
        return entities;
    }

    @Override
    protected void onClear() {
        Set<EntityType<?>> entities = this.getSpawnerEntities();
        entities.clear();
    }

    @Override
    protected Optional<EntityType<?>> getThingFromString(String text) {
        try {
            return BuiltInRegistries.ENTITY_TYPE.getOptional(ResourceLocation.parse(text));
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
        BuiltInRegistries.ENTITY_TYPE.forEach(entityType -> {
            String displayString = Component.translatable(entityType.getDescriptionId()).getString();
            String valueString = BuiltInRegistries.ENTITY_TYPE.getKey(entityType).toString();
            if (displayString.trim().toLowerCase().startsWith(text) || valueString.startsWith(text)) {
                results.add(new AutocompleteListWidget.Entry(null, displayString, valueString, this.font));
            }
        });
        return results;
    }

    @Override
    protected void onAdd(EntityType<?> entity) {
        Set<EntityType<?>> entities = this.getSpawnerEntities();
        if (entities.contains(entity)) {
            throw new IllegalStateException("Add button called while item to add is already present");
        }
        entities.add(entity);
        this.listWidget.addEntry(new EntityListWidgetEntry(entity));
    }

    @Override
    protected boolean canAdd(EntityType<?> entity) {
        return !this.getSpawnerEntities().contains(entity);
    }

    @Override
    protected void onDelete(EntityListWidgetEntry entry) {
        Set<EntityType<?>> entities = getSpawnerEntities();
        entities.remove(entry.entityType);
    }

    @Override
    protected List<EntityListWidgetEntry> getEntriesFromConfig() {
        List<EntityListWidgetEntry> entries = new ArrayList<>();
        Set<EntityType<?>> entities = this.getSpawnerEntities();
        for (EntityType<?> entity : entities) {
            entries.add(new EntityListWidgetEntry(entity));
        }
        return entries;
    }

    @Environment(EnvType.CLIENT)
    protected class EntityListWidgetEntry extends ObjectSelectionList.Entry<EntityListWidgetEntry> {
        private final EntityType<?> entityType;

        public EntityListWidgetEntry(EntityType<?> entityType) {
            this.entityType = entityType;
        }

        @Override
        public Component getNarration() {
            return Component.empty();
        }

        @Override
        public void render(GuiGraphics context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            context.drawString(CustomizeSpawnerScreen.this.font, this.entityType.getDescription(), x + 3, y + 2, 16777215, false);
        }
    }
}
