package net.spacerulerwill.skygrid_reloaded.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;
import net.spacerulerwill.skygrid_reloaded.util.MinecraftRandomAdapter;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.sampling.DiscreteProbabilityCollectionSampler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SkyGridChunkGenerator extends ChunkGenerator {
    public static final MapCodec<SkyGridChunkGenerator> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.INT.fieldOf("min_y").forGetter((generator) -> generator.minY),
                    ExtraCodecs.NON_NEGATIVE_INT.fieldOf("height").forGetter((generator) -> generator.height),
                    SkyGridChunkGeneratorConfig.CODEC.fieldOf("skygrid_settings").forGetter(SkyGridChunkGenerator::getConfig)
            ).apply(instance, SkyGridChunkGenerator::new)
    );

    public static final int MAX_BOOK_ENCHANTS = 5;

    public static final List<ResourceKey<LootTable>> ARCHEOLOGY_LOOT_TABLES = Arrays.asList(
            BuiltInLootTables.DESERT_PYRAMID_ARCHAEOLOGY,
            BuiltInLootTables.DESERT_WELL_ARCHAEOLOGY,
            BuiltInLootTables.OCEAN_RUIN_COLD_ARCHAEOLOGY,
            BuiltInLootTables.OCEAN_RUIN_WARM_ARCHAEOLOGY,
            BuiltInLootTables.TRAIL_RUINS_ARCHAEOLOGY_COMMON,
            BuiltInLootTables.TRAIL_RUINS_ARCHAEOLOGY_RARE
    );

    private final SkyGridChunkGeneratorConfig config;
    private final List<EntityType<?>> entities;
    private final int minY;
    private final int height;
    private DiscreteProbabilityCollectionSampler<Block> blockProbabilities;
    private DiscreteProbabilityCollectionSampler<Item> chestItemProbabilities;

    public SkyGridChunkGenerator(int minY, int height, SkyGridChunkGeneratorConfig config) {
        super(config.checkerboardBiomeSource);
        this.minY = minY;
        this.height = height;
        this.config = config;
        this.blockProbabilities = new DiscreteProbabilityCollectionSampler<>(new MinecraftRandomAdapter(), config.blocks);

        if (config.chestItems.isEmpty()) {
            this.chestItemProbabilities = null;
        } else {
            this.chestItemProbabilities = new DiscreteProbabilityCollectionSampler<>(new MinecraftRandomAdapter(), config.chestItems);
        }
        this.entities = config.spawnerEntities.stream().toList();

    }

    private static RandomSource getRandomForChunk(RandomState noiseConfig, int x, int z) {
        return noiseConfig.oreRandom().fromSeed((1610612741L * (long) x + 805306457L * (long) z + 402653189L) ^ 201326611L);
    }

    private static void addRandomEnchantmentToItemStack(ItemStack itemStack, RandomSource random, Registry<Enchantment> enchantmentRegistry) {
        Holder<Enchantment> enchantmentRegistryEntry = enchantmentRegistry.getRandom(random).get();
        int level = random.nextIntBetweenInclusive(1, enchantmentRegistryEntry.value().getMaxLevel());
        itemStack.enchant(enchantmentRegistryEntry, level);
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return MAP_CODEC;
    }

    @Override
    public void applyCarvers(WorldGenRegion chunkRegion, long seed, RandomState noiseConfig, BiomeManager biomeAccess, StructureManager structureAccessor, ChunkAccess chunk) {
    }

    public SkyGridChunkGeneratorConfig getConfig() {
        return config;
    }

    /*
    Empty methods - irrelevant for now
     */
    @Override
    public void applyBiomeDecoration(WorldGenLevel world, ChunkAccess chunk, StructureManager structureAccessor) {
    }

    @Override
    public void buildSurface(WorldGenRegion region, StructureManager structures, RandomState noiseConfig, ChunkAccess chunk) {
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion region) {
    }

    @Override
    public void addDebugScreenInfo(List<String> text, RandomState noiseConfig, BlockPos pos) {
    }

    /*
    Used for getting the max block height of any given column in the terrain. Used for structure generation.
    We have no structures so is irrelevant for now - a zero value is fine.
     */
    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types heightmap, LevelHeightAccessor world, RandomState noiseConfig) {
        return 0;
    }

    // Max world height, how many blocks high from minimumY the chunks generate
    @Override
    public int getGenDepth() {
        return this.height;
    }

    // No oceans in skygrid
    @Override
    public int getSeaLevel() {
        return 0;
    }

    // Bottom of the world is here
    @Override
    public int getMinY() {
        return this.minY;
    }

    private void fillChestBlockEntityWithItems(RandomizableContainerBlockEntity blockEntity, RandomSource random, RegistryAccess dynamicRegistryManager) {
        if (this.chestItemProbabilities != null) {
            // How many items for chest
            int numItems = Math.clamp(random.nextIntBetweenInclusive(2, 5), 0, blockEntity.getContainerSize());
            // Generate 26 numbers and shuffle them
            ArrayList<Integer> slots = new ArrayList<>();
            for (int i = 0; i < blockEntity.getContainerSize(); i++) {
                slots.add(i);
            }
            Collections.shuffle(slots);
            // Add the items
            int nextSlotIdx = 0;
            for (int i = 0; i < numItems; i++) {
                Item item = this.chestItemProbabilities.sample();
                ItemStack itemStack = item.getDefaultInstance();
                if (item instanceof PotionItem || item.equals(Items.TIPPED_ARROW) || item.equals(Items.SUSPICIOUS_STEW)) {
                    itemStack.set(DataComponents.POTION_CONTENTS, new PotionContents(BuiltInRegistries.POTION.getRandom(random).get()));
                } else if (item.equals(Items.GOAT_HORN)) {
                    Registry<Instrument> instrumentRegistry = dynamicRegistryManager.lookupOrThrow(Registries.INSTRUMENT);
                    itemStack.set(DataComponents.INSTRUMENT, instrumentRegistry.getRandom(random).get());
                } else if (item.equals(Items.ENCHANTED_BOOK)) {
                    // always have 1 enchantment at least
                    Registry<Enchantment> enchantmentRegistry = dynamicRegistryManager.lookupOrThrow(Registries.ENCHANTMENT);
                    float chance = 1.0f;
                    for (int j = 0; j < MAX_BOOK_ENCHANTS; j++) {
                        if (random.nextFloat() < chance) {
                            addRandomEnchantmentToItemStack(itemStack, random, enchantmentRegistry);
                        }
                        chance *= 0.66f;
                    }
                }
                int slotIdx = slots.get(nextSlotIdx);
                nextSlotIdx += 1;
                blockEntity.setItem(slotIdx, itemStack);
            }
        }
    }

    // Doing it all here is good enough for now
    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Blender blender, RandomState noiseConfig, StructureManager structureAccessor, ChunkAccess chunk) {
        RegistryAccess dynamicRegistryManager = structureAccessor.registryAccess();
        RandomSource random = getRandomForChunk(noiseConfig, chunk.getPos().x, chunk.getPos().z);
        UniformRandomProvider uniformRandomProvider = new MinecraftRandomAdapter(random);
        this.blockProbabilities = this.blockProbabilities.withUniformRandomProvider(uniformRandomProvider);
        if (this.chestItemProbabilities != null) {
            this.chestItemProbabilities = this.chestItemProbabilities.withUniformRandomProvider(uniformRandomProvider);
        }
        for (int x = 0; x < 16; x += 4) {
            for (int z = 0; z < 16; z += 4) {
                int worldX = chunk.getPos().x * 16 + x;
                int worldZ = chunk.getPos().z * 16 + z;
                for (int y = getMinY(); y < getMinY() + getGenDepth(); y += 4) {
                    BlockPos blockPos = new BlockPos(x, y, z);
                    Block block = blockProbabilities.sample();
                    BlockState state = block.defaultBlockState().trySetValue(BlockStateProperties.PERSISTENT, true);
                    chunk.setBlockState(blockPos, state, false);
                    if (block instanceof EntityBlock provider) {
                        BlockPos blockEntityPos = new BlockPos(worldX, y, worldZ);
                        BlockEntity blockEntity = provider.newBlockEntity(blockEntityPos, state);
                        if (blockEntity instanceof RandomizableContainerBlockEntity lootableContainerBlockEntity) {
                            fillChestBlockEntityWithItems(lootableContainerBlockEntity, random, dynamicRegistryManager);
                        } else if (blockEntity instanceof SpawnerBlockEntity mobSpawnerBlockEntity && !this.entities.isEmpty()) {
                            mobSpawnerBlockEntity.setEntityId(this.entities.get(random.nextInt(config.spawnerEntities.size())), random);
                        } else if (blockEntity instanceof BrushableBlockEntity brushableBlockEntity) {
                            int lootTableIndex = random.nextIntBetweenInclusive(0, ARCHEOLOGY_LOOT_TABLES.size() - 1);
                            brushableBlockEntity.setLootTable(ARCHEOLOGY_LOOT_TABLES.get(lootTableIndex), random.nextInt());
                        }
                        chunk.setBlockEntity(blockEntity);
                    }
                }
            }
        }

        // End portal placement
        if (chunk.getPos().x == 0 && chunk.getPos().z == 0) {
            chunk.setBlockState(new BlockPos(0, -64, 0), Blocks.AIR.defaultBlockState(), false);
            chunk.setBlockState(new BlockPos(2, -64, 0), Blocks.END_PORTAL_FRAME.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.WEST), false);  // Face towards center (1, -64, 1)
            chunk.setBlockState(new BlockPos(0, -64, 2), Blocks.END_PORTAL_FRAME.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH), false); // Face towards center (1, -64, 1)
            chunk.setBlockState(new BlockPos(2, -64, 1), Blocks.END_PORTAL_FRAME.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.WEST), false); // Face towards center (1, -64, 1)
            chunk.setBlockState(new BlockPos(1, -64, 2), Blocks.END_PORTAL_FRAME.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH), false);  // Face towards center (1, -64, 1)
        } else if (chunk.getPos().x == -1 && chunk.getPos().z == 0) {
            chunk.setBlockState(new BlockPos(-2, -64, 0), Blocks.END_PORTAL_FRAME.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST), false);   // Face towards center (1, -64, 1)
            chunk.setBlockState(new BlockPos(-2, -64, 1), Blocks.END_PORTAL_FRAME.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST), false);  // Face towards center (1, -64, 1)
            chunk.setBlockState(new BlockPos(-1, -64, 2), Blocks.END_PORTAL_FRAME.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH), false);  // Face towards center (1, -64, 1)
        } else if (chunk.getPos().x == 0 && chunk.getPos().z == -1) {
            chunk.setBlockState(new BlockPos(0, -64, -2), Blocks.END_PORTAL_FRAME.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH), false); // Face towards center (1, -64, 1)
            chunk.setBlockState(new BlockPos(1, -64, -2), Blocks.END_PORTAL_FRAME.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH), false);  // Face towards center (1, -64, 1)
            chunk.setBlockState(new BlockPos(2, -64, -1), Blocks.END_PORTAL_FRAME.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.WEST), false); // Face towards center (1, -64, 1)
        } else if (chunk.getPos().x == -1 && chunk.getPos().z == -1) {
            chunk.setBlockState(new BlockPos(-2, -64, -1), Blocks.END_PORTAL_FRAME.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST), false);  // Face towards center (1, -64, 1)
            chunk.setBlockState(new BlockPos(-1, -64, -2), Blocks.END_PORTAL_FRAME.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH), false);   // Face towards center (1, -64, 1)
        }


        return CompletableFuture.completedFuture(chunk);
    }

    // Get one column of the terrain
    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor world, RandomState noiseConfig) {
        RandomSource random = getRandomForChunk(noiseConfig, x >> 4, z >> 4);
        blockProbabilities = blockProbabilities.withUniformRandomProvider(new MinecraftRandomAdapter(random));
        BlockState[] states = new BlockState[getGenDepth() / 4];
        for (int y = getMinY(); y < getMinY() + getGenDepth(); y += 4) {
            states[(y - getMinY()) / 4] = blockProbabilities.sample().defaultBlockState();
        }
        return new NoiseColumn(getMinY(), states);
    }
}
