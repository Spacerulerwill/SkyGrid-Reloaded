package net.spacerulerwill.skygrid_reloaded.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.CheckerboardColumnBiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.spacerulerwill.skygrid_reloaded.util.CheckerboardColumnBiomeSourceSizeAccessor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

public class SkyGridChunkGeneratorConfig {
    static Codec<LinkedHashMap<Block, Double>> BLOCK_WEIGHT_MAP_CODEC = Codec.unboundedMap(BuiltInRegistries.BLOCK.byNameCodec(), Codec.DOUBLE)
            .xmap(LinkedHashMap::new, map -> map);
    static Codec<LinkedHashMap<Item, Double>> ITEM_WEIGHT_MAP_CODEC = Codec.unboundedMap(BuiltInRegistries.ITEM.byNameCodec(), Codec.DOUBLE)
            .xmap(LinkedHashMap::new, map -> map);
    public static final Codec<SkyGridChunkGeneratorConfig> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    BLOCK_WEIGHT_MAP_CODEC.fieldOf("blocks").forGetter(config -> config.blocks),
                    BuiltInRegistries.ENTITY_TYPE.byNameCodec().listOf().fieldOf("spawner_entities")
                            .xmap(
                                    LinkedHashSet::new,
                                    ArrayList::new
                            )
                            .forGetter(config -> config.spawnerEntities),
                    ITEM_WEIGHT_MAP_CODEC.fieldOf("chest_items").forGetter(config -> config.chestItems),
                    CheckerboardColumnBiomeSource.CODEC.fieldOf("checkerboard")
                            .orElse(new CheckerboardColumnBiomeSource(HolderSet.direct(), 1))
                            .forGetter(config -> config.checkerboardBiomeSource)
            ).apply(instance, SkyGridChunkGeneratorConfig::new)
    );
    public LinkedHashMap<Block, Double> blocks;
    public LinkedHashSet<EntityType<?>> spawnerEntities;
    public LinkedHashMap<Item, Double> chestItems;
    public CheckerboardColumnBiomeSource checkerboardBiomeSource;

    public SkyGridChunkGeneratorConfig(LinkedHashMap<Block, Double> blocks,
                                       LinkedHashSet<EntityType<?>> spawnerEntities,
                                       LinkedHashMap<Item, Double> chestItems,
                                       CheckerboardColumnBiomeSource checkerboardBiomeSource) {
        this.blocks = blocks;
        this.spawnerEntities = spawnerEntities;
        this.chestItems = chestItems;
        this.checkerboardBiomeSource = checkerboardBiomeSource;
    }

    public SkyGridChunkGeneratorConfig() {
        this.blocks = new LinkedHashMap<>();
        this.spawnerEntities = new LinkedHashSet<>();
        this.chestItems = new LinkedHashMap<>();
        // Provide a default CheckerboardColumnBiomeSource with empty biomes and size 1
        this.checkerboardBiomeSource = new CheckerboardColumnBiomeSource(HolderSet.direct(), 1);
    }

    public boolean hasSkygrid() {
        return this.blocks.values().stream().anyMatch(weight -> weight > 0);
    }

    public boolean hasContainerBlock() {
        return this.blocks.entrySet().stream().anyMatch(entry -> {
            Block block = entry.getKey();
            Double value = entry.getValue();
            BlockPos pos = new BlockPos(0, 0, 0);
            return value > 0.0 && block instanceof EntityBlock entityBlock && entityBlock.newBlockEntity(pos, block.defaultBlockState()) instanceof RandomizableContainerBlockEntity;
        });
    }

    public boolean hasMobSpawner() {
        return this.blocks.containsKey(Blocks.SPAWNER) && this.blocks.get(Blocks.SPAWNER) > 0.0;
    }
}
