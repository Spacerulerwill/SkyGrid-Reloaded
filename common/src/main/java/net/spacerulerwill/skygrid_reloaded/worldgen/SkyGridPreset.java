package net.spacerulerwill.skygrid_reloaded.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;

public record SkyGridPreset(Item item, String name, SkyGridConfig config) {
    public static final Codec<SkyGridPreset> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(SkyGridPreset::item),
                    ExtraCodecs.NON_EMPTY_STRING.fieldOf("name").forGetter(SkyGridPreset::name),
                    SkyGridConfig.CODEC.fieldOf("config").forGetter(SkyGridPreset::config)
            ).apply(instance, SkyGridPreset::new)
    );
}