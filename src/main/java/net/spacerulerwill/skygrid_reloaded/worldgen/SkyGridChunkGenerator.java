package net.spacerulerwill.skygrid_reloaded.worldgen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BrushableBlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.InstrumentComponent;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Instrument;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PotionItem;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.VerticalBlockSample;
import net.minecraft.world.gen.noise.NoiseConfig;
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
                    BiomeSource.CODEC.fieldOf("biome_source").forGetter(SkyGridChunkGenerator::getBiomeSource),
                    ChunkGeneratorSettings.REGISTRY_CODEC.fieldOf("settings").forGetter((generator) -> generator.settings),
                    SkyGridChunkGeneratorConfig.CODEC.fieldOf("skygrid_settings").forGetter(SkyGridChunkGenerator::getConfig)
            ).apply(instance, SkyGridChunkGenerator::new)
    );

    public static final int MAX_BOOK_ENCHANTS = 5;

    public static final List<RegistryKey<LootTable>> ARCHEOLOGY_LOOT_TABLES = Arrays.asList(
            LootTables.DESERT_PYRAMID_ARCHAEOLOGY,
            LootTables.DESERT_WELL_ARCHAEOLOGY,
            LootTables.OCEAN_RUIN_COLD_ARCHAEOLOGY,
            LootTables.OCEAN_RUIN_WARM_ARCHAEOLOGY,
            LootTables.TRAIL_RUINS_COMMON_ARCHAEOLOGY,
            LootTables.TRAIL_RUINS_RARE_ARCHAEOLOGY
    );

    private final SkyGridChunkGeneratorConfig config;
    private final List<EntityType<?>> entities;
    private final RegistryEntry<ChunkGeneratorSettings> settings;
    private DiscreteProbabilityCollectionSampler<Block> blockProbabilities;
    private DiscreteProbabilityCollectionSampler<Item> chestItemProbabilities;


    public SkyGridChunkGenerator(BiomeSource biomeSource, RegistryEntry<ChunkGeneratorSettings> settings, SkyGridChunkGeneratorConfig config) {
        super(biomeSource);
        this.settings = settings;
        this.config = config;
        this.blockProbabilities = new DiscreteProbabilityCollectionSampler<>(new MinecraftRandomAdapter(), config.blocks());

        if (config.chestItems().isEmpty()) {
            this.chestItemProbabilities = null;
        } else {
            this.chestItemProbabilities = new DiscreteProbabilityCollectionSampler<>(new MinecraftRandomAdapter(), config.chestItems());
        }
        this.entities = config.spawnerEntities().stream().toList();

    }

    private static Random getRandomForChunk(NoiseConfig noiseConfig, int x, int z) {
        return noiseConfig.getOreRandomDeriver().split((1610612741L * (long) x + 805306457L * (long) z + 402653189L) ^ 201326611L);
    }

    private static void addRandomEnchantmentToItemStack(ItemStack itemStack, Random random, Registry<Enchantment> enchantmentRegistry) {
        RegistryEntry<Enchantment> enchantmentRegistryEntry = enchantmentRegistry.getRandom(random).get();
        int level = random.nextBetween(1, enchantmentRegistryEntry.value().getMaxLevel());
        itemStack.addEnchantment(enchantmentRegistryEntry, level);
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> getCodec() {
        return MAP_CODEC;
    }

    @Override
    public void carve(ChunkRegion chunkRegion, long seed, NoiseConfig noiseConfig, BiomeAccess biomeAccess, StructureAccessor structureAccessor, Chunk chunk) {
    }

    public SkyGridChunkGeneratorConfig getConfig() {
        return config;
    }

    /*
    Empty methods - irrelevant for now
     */
    @Override
    public void generateFeatures(StructureWorldAccess world, Chunk chunk, StructureAccessor structureAccessor) {
    }

    @Override
    public void buildSurface(ChunkRegion region, StructureAccessor structures, NoiseConfig noiseConfig, Chunk chunk) {
    }

    @Override
    public void populateEntities(ChunkRegion region) {
    }

    @Override
    public void appendDebugHudText(List<String> text, NoiseConfig noiseConfig, BlockPos pos) {
    }

    /*
    Used for getting the max block height of any given column in the terrain. Used for structure generation.
    We have no structures so is irrelevant for now - a zero value is fine.
     */
    @Override
    public int getHeight(int x, int z, Heightmap.Type heightmap, HeightLimitView world, NoiseConfig noiseConfig) {
        return 0;
    }

    // Max world height, how many blocks high from minimumY the chunks generate
    @Override
    public int getWorldHeight() {
        return this.settings.value().generationShapeConfig().height();
    }

    // No oceans in skygrid
    @Override
    public int getSeaLevel() {
        return 0;
    }

    // Bottom of the world is here
    @Override
    public int getMinimumY() {
        return this.settings.value().generationShapeConfig().minimumY();
    }

    private void fillChestBlockEntityWithItems(LootableContainerBlockEntity blockEntity, Random random, DynamicRegistryManager dynamicRegistryManager) {
        if (this.chestItemProbabilities != null) {
            // How many items for chest
            int numItems = Math.clamp(random.nextBetween(2, 5), 0, blockEntity.size());
            // Generate 26 numbers and shuffle them
            ArrayList<Integer> slots = new ArrayList<>();
            for (int i = 0; i < blockEntity.size(); i++) {
                slots.add(i);
            }
            Collections.shuffle(slots);
            // Add the items
            int nextSlotIdx = 0;
            for (int i = 0; i < numItems; i++) {
                Item item = this.chestItemProbabilities.sample();
                ItemStack itemStack = item.getDefaultStack();
                if (item instanceof PotionItem || item.equals(Items.TIPPED_ARROW) || item.equals(Items.SUSPICIOUS_STEW)) {
                    itemStack.set(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(Registries.POTION.getRandom(random).get()));
                } else if (item.equals(Items.GOAT_HORN)) {
                    Registry<Instrument> instrumentRegistry = dynamicRegistryManager.getOrThrow(RegistryKeys.INSTRUMENT);
                    RegistryEntry.Reference<Instrument> instrument = instrumentRegistry.getRandom(random).get();
                    itemStack.set(DataComponentTypes.INSTRUMENT, new InstrumentComponent(instrument));
                } else if (item.equals(Items.ENCHANTED_BOOK)) {
                    // always have 1 enchantment at least
                    Registry<Enchantment> enchantmentRegistry = dynamicRegistryManager.getOrThrow(RegistryKeys.ENCHANTMENT);
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
                blockEntity.setStack(slotIdx, itemStack);
            }
        }
    }

    // Doing it all here is good enough for now
    @Override
    public CompletableFuture<Chunk> populateNoise(Blender blender, NoiseConfig noiseConfig, StructureAccessor structureAccessor, Chunk chunk) {
        DynamicRegistryManager dynamicRegistryManager = structureAccessor.getRegistryManager();
        Random random = getRandomForChunk(noiseConfig, chunk.getPos().x, chunk.getPos().z);
        UniformRandomProvider uniformRandomProvider = new MinecraftRandomAdapter(random);
        this.blockProbabilities = this.blockProbabilities.withUniformRandomProvider(uniformRandomProvider);
        if (this.chestItemProbabilities != null) {
            this.chestItemProbabilities = this.chestItemProbabilities.withUniformRandomProvider(uniformRandomProvider);
        }
        for (int x = 0; x < 16; x += 4) {
            for (int z = 0; z < 16; z += 4) {
                int worldX = chunk.getPos().x * 16 + x;
                int worldZ = chunk.getPos().z * 16 + z;
                for (int y = getMinimumY(); y < getMinimumY() + getWorldHeight(); y += 4) {
                    BlockPos blockPos = new BlockPos(x, y, z);
                    Block block = blockProbabilities.sample();
                    BlockState state = block.getDefaultState().withIfExists(Properties.PERSISTENT, true);
                    chunk.setBlockState(blockPos, state);
                    if (block instanceof BlockEntityProvider provider) {
                        BlockPos blockEntityPos = new BlockPos(worldX, y, worldZ);
                        BlockEntity blockEntity = provider.createBlockEntity(blockEntityPos, state);
                        if (blockEntity instanceof LootableContainerBlockEntity lootableContainerBlockEntity) {
                            fillChestBlockEntityWithItems(lootableContainerBlockEntity, random, dynamicRegistryManager);
                        }
                        else if (blockEntity instanceof MobSpawnerBlockEntity mobSpawnerBlockEntity && !this.entities.isEmpty()) {
                            mobSpawnerBlockEntity.setEntityType(this.entities.get(random.nextInt(config.spawnerEntities().size())), random);
                        }
                        else if (blockEntity instanceof BrushableBlockEntity brushableBlockEntity) {
                            int lootTableIndex = random.nextBetween(0, ARCHEOLOGY_LOOT_TABLES.size() - 1);
                            brushableBlockEntity.setLootTable(ARCHEOLOGY_LOOT_TABLES.get(lootTableIndex), random.nextInt());
                        }
                        chunk.setBlockEntity(blockEntity);
                    }
                }
            }
        }

        // End portal placement
        if (chunk.getPos().x == 0 && chunk.getPos().z == 0) {
            chunk.setBlockState(new BlockPos(0, -64, 0), Blocks.AIR.getDefaultState());
            chunk.setBlockState(new BlockPos(2, -64, 0), Blocks.END_PORTAL_FRAME.getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.WEST));  // Face towards center (1, -64, 1)
            chunk.setBlockState(new BlockPos(0, -64, 2), Blocks.END_PORTAL_FRAME.getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.NORTH)); // Face towards center (1, -64, 1)
            chunk.setBlockState(new BlockPos(2, -64, 1), Blocks.END_PORTAL_FRAME.getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.WEST)); // Face towards center (1, -64, 1)
            chunk.setBlockState(new BlockPos(1, -64, 2), Blocks.END_PORTAL_FRAME.getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.NORTH));  // Face towards center (1, -64, 1)
        } else if (chunk.getPos().x == -1 && chunk.getPos().z == 0) {
            chunk.setBlockState(new BlockPos(-2, -64, 0), Blocks.END_PORTAL_FRAME.getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.EAST));   // Face towards center (1, -64, 1)
            chunk.setBlockState(new BlockPos(-2, -64, 1), Blocks.END_PORTAL_FRAME.getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.EAST));  // Face towards center (1, -64, 1)
            chunk.setBlockState(new BlockPos(-1, -64, 2), Blocks.END_PORTAL_FRAME.getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.NORTH));  // Face towards center (1, -64, 1)
        } else if (chunk.getPos().x == 0 && chunk.getPos().z == -1) {
            chunk.setBlockState(new BlockPos(0, -64, -2), Blocks.END_PORTAL_FRAME.getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.SOUTH)); // Face towards center (1, -64, 1)
            chunk.setBlockState(new BlockPos(1, -64, -2), Blocks.END_PORTAL_FRAME.getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.SOUTH));  // Face towards center (1, -64, 1)
            chunk.setBlockState(new BlockPos(2, -64, -1), Blocks.END_PORTAL_FRAME.getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.WEST)); // Face towards center (1, -64, 1)
        } else if (chunk.getPos().x == -1 && chunk.getPos().z == -1) {
            chunk.setBlockState(new BlockPos(-2, -64, -1), Blocks.END_PORTAL_FRAME.getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.EAST));  // Face towards center (1, -64, 1)
            chunk.setBlockState(new BlockPos(-1, -64, -2), Blocks.END_PORTAL_FRAME.getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.SOUTH));   // Face towards center (1, -64, 1)
        }


        return CompletableFuture.completedFuture(chunk);
    }

    // Get one column of the terrain
    @Override
    public VerticalBlockSample getColumnSample(int x, int z, HeightLimitView world, NoiseConfig noiseConfig) {
        Random random = getRandomForChunk(noiseConfig, x >> 4, z >> 4);
        blockProbabilities = blockProbabilities.withUniformRandomProvider(new MinecraftRandomAdapter(random));
        BlockState[] states = new BlockState[getWorldHeight() / 4];
        for (int y = getMinimumY(); y < getMinimumY() + getWorldHeight(); y += 4) {
            states[(y - getMinimumY()) / 4] = blockProbabilities.sample().getDefaultState();
        }
        return new VerticalBlockSample(getMinimumY(), states);
    }
}
