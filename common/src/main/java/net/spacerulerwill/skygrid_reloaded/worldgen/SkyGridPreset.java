package net.spacerulerwill.skygrid_reloaded.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;

public class SkyGridPreset {
    public final Item item;
    public final String name;
    public final SkyGridConfig config;

    public SkyGridPreset(Item item, String name, SkyGridConfig config) {
        this.item = item;
        this.name = name;
        this.config = config;
    }

    public static final Codec<SkyGridPreset> CODEC_V1 = RecordCodecBuilder.create(
            instance -> instance.group(
                    BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(preset -> preset.item),
                    ExtraCodecs.NON_EMPTY_STRING.fieldOf("name").forGetter(preset -> preset.name),
                    SkyGridConfig.CODEC_V1.fieldOf("config").forGetter(preset -> preset.config)
            ).apply(instance, SkyGridPreset::new)
    );

    public static final Codec<SkyGridPreset> CODEC_V2 = RecordCodecBuilder.create(
            instance -> instance.group(
                    BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(preset -> preset.item),
                    ExtraCodecs.NON_EMPTY_STRING.fieldOf("name").forGetter(preset -> preset.name),
                    SkyGridConfig.CODEC_V2.fieldOf("config").forGetter(preset -> preset.config)
            ).apply(instance, SkyGridPreset::new)
    );
}
