package net.spacerulerwill.skygrid_reloaded.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.world.biome.source.CheckerboardBiomeSource;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

public class SkyGridChunkGeneratorConfig {
    static Codec<LinkedHashMap<Block, Double>> BLOCK_WEIGHT_MAP_CODEC = Codec.unboundedMap(Registries.BLOCK.getCodec(), Codec.DOUBLE)
            .xmap(LinkedHashMap::new, map -> map);
    static Codec<LinkedHashMap<Item, Double>> ITEM_WEIGHT_MAP_CODEC = Codec.unboundedMap(Registries.ITEM.getCodec(), Codec.DOUBLE)
            .xmap(LinkedHashMap::new, map -> map);
    public static final Codec<SkyGridChunkGeneratorConfig> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    BLOCK_WEIGHT_MAP_CODEC.fieldOf("blocks").forGetter(config -> config.blocks),
                    Registries.ENTITY_TYPE.getCodec().listOf().fieldOf("spawner_entities")
                            .xmap(
                                    LinkedHashSet::new,
                                    ArrayList::new
                            )
                            .forGetter(config -> config.spawnerEntities),
                    ITEM_WEIGHT_MAP_CODEC.fieldOf("chest_items").forGetter(config -> config.chestItems),
                    CheckerboardBiomeSource.CODEC.fieldOf("checkerboard")
                            .orElse(new CheckerboardBiomeSource(RegistryEntryList.of(), 1))
                            .forGetter(config -> config.checkerboardBiomeSource)
            ).apply(instance, SkyGridChunkGeneratorConfig::new)
    );
    public LinkedHashMap<Block, Double> blocks;
    public LinkedHashSet<EntityType<?>> spawnerEntities;
    public LinkedHashMap<Item, Double> chestItems;
    public CheckerboardBiomeSource checkerboardBiomeSource;

    public SkyGridChunkGeneratorConfig(LinkedHashMap<Block, Double> blocks,
                                       LinkedHashSet<EntityType<?>> spawnerEntities,
                                       LinkedHashMap<Item, Double> chestItems,
                                       CheckerboardBiomeSource checkerboardBiomeSource) {
        this.blocks = blocks;
        this.spawnerEntities = spawnerEntities;
        this.chestItems = chestItems;
        this.checkerboardBiomeSource = checkerboardBiomeSource;
    }

    public SkyGridChunkGeneratorConfig(SkyGridChunkGeneratorConfig other) {
        this(
                new LinkedHashMap<>(other.blocks),
                new LinkedHashSet<>(other.spawnerEntities),
                new LinkedHashMap<>(other.chestItems),
                new CheckerboardBiomeSource(RegistryEntryList.of(other.checkerboardBiomeSource.getBiomes().stream().toList()), other.checkerboardBiomeSource.scale)
        );
    }
}
