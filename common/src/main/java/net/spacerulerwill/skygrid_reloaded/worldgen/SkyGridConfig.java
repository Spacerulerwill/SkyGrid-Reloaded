package net.spacerulerwill.skygrid_reloaded.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.LevelStem;

import java.util.HashMap;
import java.util.Map;

public class SkyGridConfig {
    public final Map<ResourceKey<LevelStem>, SkyGridChunkGeneratorConfig> dimensions;

    public SkyGridConfig(Map<ResourceKey<LevelStem>, SkyGridChunkGeneratorConfig> dimensions) {
        this.dimensions = new HashMap<>(dimensions);
    }

    public static final Codec<SkyGridConfig> CODEC_V1 = RecordCodecBuilder.create(
            instance -> instance.group(
                    SkyGridChunkGeneratorConfig.CODEC.fieldOf("overworld_config").forGetter(cfg -> cfg.dimensions.get(LevelStem.OVERWORLD)),
                    SkyGridChunkGeneratorConfig.CODEC.fieldOf("nether_config").forGetter(cfg -> cfg.dimensions.get(LevelStem.NETHER)),
                    SkyGridChunkGeneratorConfig.CODEC.fieldOf("end_config").forGetter(cfg -> cfg.dimensions.get(LevelStem.END))
            ).apply(instance, (overworld, nether, end) -> new SkyGridConfig(
                    Map.of(LevelStem.OVERWORLD, overworld, LevelStem.NETHER, nether, LevelStem.END, end)
            ))
    );

    public static final Codec<SkyGridConfig> CODEC_V2 = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.unboundedMap(ResourceKey.codec(Registries.LEVEL_STEM), SkyGridChunkGeneratorConfig.CODEC).fieldOf("dimensions").forGetter(skyGridConfig -> skyGridConfig.dimensions)
            ).apply(instance, SkyGridConfig::new)
    );

    public SkyGridConfig(SkyGridConfig other) {
        this(
                Map.copyOf(other.dimensions)
        );
    }
}